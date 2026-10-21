import Foundation
import SwiftData

public final class CloudSyncManager {
    public static let shared = CloudSyncManager()
    private init() {}

    public static func normalizeServerUrl(_ input: String) -> String {
        var trimmed = input.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            return "https://api.cosmowhisper.com/cookbook"
        }
        while trimmed.hasSuffix("/") {
            trimmed.removeLast()
        }
        if !trimmed.lowercased().hasPrefix("http://") && !trimmed.lowercased().hasPrefix("https://") {
            trimmed = "https://\(trimmed)"
        }
        return trimmed
    }

    public func testConnection(serverUrl: String, token: String) async -> (Bool, String) {
        let cleanUrl = Self.normalizeServerUrl(serverUrl)
        guard let url = URL(string: "\(cleanUrl)/api/health") else {
            return (false, "Invalid Server URL.")
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 10.0
        if !token.isEmpty {
            request.setValue(token, forHTTPHeaderField: "x-sync-token")
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse else {
                return (false, "No response from server.")
            }
            if (200...299).contains(http.statusCode) {
                var message = "Connected (Server Online)"
                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                    let active = json["activeRecipes"] as? Int ?? -1
                    let service = json["service"] as? String ?? ""
                    if active >= 0 {
                        message = "Connected (\(service.isEmpty ? "VPS" : service) • \(active) remote recipes)"
                    } else if !service.isEmpty {
                        message = "Connected (\(service) Online)"
                    }
                }
                return (true, message)
            } else if http.statusCode == 401 || http.statusCode == 403 {
                return (false, "Authentication Failed: Invalid Sync Token (HTTP \(http.statusCode))")
            } else {
                return (false, "Server responded with HTTP \(http.statusCode)")
            }
        } catch {
            return (false, "Cannot reach server at \(cleanUrl): \(error.localizedDescription)")
        }
    }

    public func syncNow(serverUrl: String, token: String, modelContext: ModelContext) async throws -> String {
        let cleanUrl = Self.normalizeServerUrl(serverUrl)
        let test = await testConnection(serverUrl: cleanUrl, token: token)
        guard test.0 else {
            throw NSError(domain: "CloudSync", code: -1, userInfo: [NSLocalizedDescriptionKey: test.1])
        }

        // Fetch local recipes to build sync payload
        let fetchDescriptor = FetchDescriptor<Recipe>()
        let localRecipes = (try? modelContext.fetch(fetchDescriptor)) ?? []

        var syncList: [[String: Any]] = []
        for r in localRecipes {
            var item: [String: Any] = [
                "id": r.id,
                "title": r.title,
                "titleGerman": r.titleGerman,
                "category": r.category,
                "servings": r.servings,
                "prepTimeMinutes": r.prepTimeMinutes,
                "cookTimeMinutes": r.cookTimeMinutes,
                "notes": r.notes,
                "isFavorite": r.isFavorite,
                "profileName": r.profileName
            ]
            let ings = r.ingredients.map { [
                "name": $0.name,
                "nameEnglish": $0.nameEnglish ?? $0.name,
                "nameGerman": $0.nameGerman ?? "",
                "amount": $0.amount,
                "unit": $0.unit,
                "group": $0.group ?? ""
            ] }
            let steps = r.steps.map { [
                "stepNumber": $0.stepNumber,
                "instructionEnglish": $0.instructionEnglish,
                "instructionGerman": $0.instructionGerman,
                "timerMinutes": $0.timerMinutes
            ] }
            item["ingredients"] = ings
            item["steps"] = steps
            syncList.append(item)
        }

        let lastSync = Int64(UserDefaults.standard.double(forKey: "last_sync_timestamp") * 1000)
        let payload: [String: Any] = [
            "lastSyncTimestamp": lastSync,
            "clientTimestamp": Int64(Date().timeIntervalSince1970 * 1000),
            "clientType": "ios-native",
            "recipes": syncList
        ]

        guard let syncUrl = URL(string: "\(cleanUrl)/api/recipes/sync") else {
            throw NSError(domain: "CloudSync", code: -2, userInfo: [NSLocalizedDescriptionKey: "Invalid sync endpoint URL"])
        }

        var req = URLRequest(url: syncUrl)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if !token.isEmpty {
            req.setValue(token, forHTTPHeaderField: "x-sync-token")
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        req.httpBody = try JSONSerialization.data(withJSONObject: payload)
        req.timeoutInterval = 30.0

        let (data, response) = try await URLSession.shared.data(for: req)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            let code = (response as? HTTPURLResponse)?.statusCode ?? -1
            throw NSError(domain: "CloudSync", code: -3, userInfo: [NSLocalizedDescriptionKey: "Sync endpoint returned HTTP \(code)"])
        }

        var pulledCount = 0
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            let remoteArr = (json["remoteChanges"] as? [[String: Any]]) ?? (json["recipes"] as? [[String: Any]]) ?? []
            for item in remoteArr {
                guard let title = item["title"] as? String, !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { continue }
                let isDeleted = item["isDeleted"] as? Bool ?? false
                let id = (item["id"] as? String) ?? UUID().uuidString

                let existing = localRecipes.first { $0.id == id || $0.title.caseInsensitiveCompare(title) == .orderedSame }

                if isDeleted {
                    if let ex = existing {
                        modelContext.delete(ex)
                        pulledCount += 1
                    }
                } else {
                    var ings: [RecipeIngredient] = []
                    if let ingArr = item["ingredients"] as? [[String: Any]] {
                        for ingObj in ingArr {
                            let ingName = (ingObj["name"] as? String) ?? ""
                            let ingAmt = "\(ingObj["amount"] ?? "")"
                            let ingUnit = (ingObj["unit"] as? String) ?? ""
                            let ingEn = ingObj["nameEnglish"] as? String
                            let ingDe = ingObj["nameGerman"] as? String
                            let ingOpt = ingObj["isOptional"] as? Bool ?? false
                            let ingGrp = ingObj["group"] as? String
                            ings.append(RecipeIngredient(name: ingName, amount: ingAmt, unit: ingUnit, nameGerman: ingDe, nameEnglish: ingEn, isOptional: ingOpt, group: ingGrp))
                        }
                    } else if let ingStr = item["ingredientsJson"] as? String, let ingData = ingStr.data(using: .utf8) {
                        ings = (try? JSONDecoder().decode([RecipeIngredient].self, from: ingData)) ?? []
                    }

                    var steps: [RecipeStep] = []
                    if let stepArr = item["steps"] as? [[String: Any]] {
                        for stepObj in stepArr {
                            let num = stepObj["stepNumber"] as? Int ?? 1
                            let en = (stepObj["instructionEnglish"] as? String) ?? ""
                            let de = (stepObj["instructionGerman"] as? String) ?? ""
                            let timer = stepObj["timerMinutes"] as? Int ?? 0
                            let tip = stepObj["tip"] as? String
                            steps.append(RecipeStep(stepNumber: num, instructionEnglish: en, instructionGerman: de, timerMinutes: timer, tip: tip))
                        }
                    } else if let stepStr = item["stepsJson"] as? String, let stepData = stepStr.data(using: .utf8) {
                        steps = (try? JSONDecoder().decode([RecipeStep].self, from: stepData)) ?? []
                    }

                    let category = (item["category"] as? String) ?? "Family Classics"
                    let rawServings = (item["servingsText"] as? String) ?? "\(item["servings"] ?? "4 servings")"
                    let servings = Self.normalizeServings(rawServings)
                    let prep = item["prepTimeMinutes"] as? Int ?? (item["prepTime"] as? Int ?? 20)
                    let cook = item["cookTimeMinutes"] as? Int ?? (item["cookTime"] as? Int ?? 30)
                    let diff = (item["difficulty"] as? String) ?? "Medium"
                    let notes = (item["notes"] as? String) ?? ""
                    let notesDe = (item["notesGerman"] as? String) ?? ""
                    let prof = (item["profileName"] as? String) ?? "Annette"
                    let themeRaw = (item["coverTheme"] as? String) ?? "VINTAGE_LEATHER"
                    let fav = item["isFavorite"] as? Bool ?? false
                    let rating = item["rating"] as? Int ?? 5

                    let remoteCoverPhoto = (item["coverPhotoName"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
                    var localImagePath: String? = nil
                    if let coverName = remoteCoverPhoto, !coverName.isEmpty {
                        localImagePath = await self.downloadCoverPhotoIfNeeded(serverUrl: cleanUrl, token: token, filename: coverName)
                    }

                    if let ex = existing {
                        ex.title = title
                        ex.category = category
                        ex.servings = servings
                        ex.prepTimeMinutes = prep
                        ex.cookTimeMinutes = cook
                        ex.difficulty = diff
                        ex.notes = notes
                        ex.notesGerman = notesDe
                        ex.profileName = prof
                        ex.coverThemeRaw = themeRaw
                        ex.isFavorite = fav
                        ex.rating = rating
                        ex.ingredients = ings
                        ex.steps = steps
                        if let cover = remoteCoverPhoto, !cover.isEmpty {
                            ex.coverPhotoName = cover
                        }
                        if let imgPath = localImagePath {
                            ex.imagePath = imgPath
                        }
                    } else {
                        let newRecipe = Recipe(
                            id: id,
                            title: title,
                            titleGerman: (item["titleGerman"] as? String) ?? "",
                            titleEnglish: (item["titleEnglish"] as? String) ?? title,
                            category: category,
                            servings: servings,
                            prepTimeMinutes: prep,
                            cookTimeMinutes: cook,
                            difficulty: diff,
                            ingredients: ings,
                            steps: steps,
                            notes: notes,
                            notesGerman: notesDe,
                            imagePath: localImagePath,
                            coverTheme: CoverTheme(rawValue: themeRaw) ?? .vintageLeather,
                            isFavorite: fav,
                            rating: rating,
                            coverPhotoName: remoteCoverPhoto,
                            profileName: prof
                        )
                        modelContext.insert(newRecipe)
                    }
                    pulledCount += 1
                }
            }
            try? modelContext.save()
        }

        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: "last_sync_timestamp")
        UserDefaults.standard.set("Success (Pushed \(localRecipes.count), Pulled \(pulledCount))", forKey: "last_sync_status")

        return "Sync Complete! (Pushed \(localRecipes.count), Pulled \(pulledCount))"
    }

    /// Downloads remote cover photo from GET /api/recipes/images/{filename} and stores in Documents folder
    public func downloadCoverPhotoIfNeeded(serverUrl: String, token: String, filename: String) async -> String? {
        let cleanName = (filename as NSString).lastPathComponent
        guard !cleanName.isEmpty else { return nil }

        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let localFile = docs.appendingPathComponent(cleanName)

        if FileManager.default.fileExists(atPath: localFile.path) {
            return localFile.path
        }

        // Also check if already bundled with app
        let baseName = (cleanName as NSString).deletingPathExtension
        let ext = (cleanName as NSString).pathExtension
        if let bundleUrl = Bundle.main.url(forResource: baseName, withExtension: ext, subdirectory: "SyncedImages") ?? Bundle.main.url(forResource: cleanName, withExtension: nil) {
            return bundleUrl.path
        }

        guard let url = URL(string: "\(serverUrl)/api/recipes/images/\(cleanName)") else { return nil }

        var req = URLRequest(url: url)
        req.httpMethod = "GET"
        req.timeoutInterval = 15.0
        if !token.isEmpty {
            req.setValue(token, forHTTPHeaderField: "x-sync-token")
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        do {
            let (data, response) = try await URLSession.shared.data(for: req)
            if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode), !data.isEmpty {
                try data.write(to: localFile)
                return localFile.path
            }
        } catch {
            print("Failed to download cover photo \(cleanName): \(error)")
        }
        return nil
    }

    public static func normalizeServings(_ raw: String?) -> String {
        guard let raw = raw?.trimmingCharacters(in: .whitespacesAndNewlines), !raw.isEmpty else {
            return "4-6 servings"
        }
        let lower = raw.lowercased()
        let mapping: [String: String] = [
            "46": "4-6 servings",
            "46 servings": "4-6 servings",
            "810": "8-10 servings",
            "810 servings": "8-10 servings",
            "1012": "10-12 servings",
            "1012 servings": "10-12 servings",
            "68": "6-8 servings",
            "68 servings": "6-8 servings",
            "1618": "16-18 servings",
            "1618 servings": "16-18 servings",
            "3040": "30-40 cookies",
            "3040 servings": "30-40 cookies",
            "4125": "4-5 jars (approx 250ml each)",
            "4125 servings": "4-5 jars (approx 250ml each)",
            "8300": "8 jars (approx 300g each)",
            "8300 servings": "8 jars (approx 300g each)",
            "2200": "2-3 jars (approx 200g each)",
            "2200 servings": "2-3 jars (approx 200g each)",
            "90302": "4-6 jars (approx 300g each)",
            "90302 servings": "4-6 jars (approx 300g each)",
            "45152": "4-5 bottles (approx 150ml each)",
            "45152 servings": "4-5 bottles (approx 150ml each)",
            "1112": "1-2 jars (approx 500g each)",
            "1112 servings": "1-2 jars (approx 500g each)",
            "681": "6-8 servings",
            "681 servings": "6-8 servings",
            "2000": "Serves 8-10 (2kg batch)",
            "2000 servings": "Serves 8-10 (2kg batch)",
            "500": "4-5 jars (approx 500g each)",
            "500 servings": "4-5 jars (approx 500g each)",
            "200": "Makes about 40-50 pieces",
            "200 servings": "Makes about 40-50 pieces",
            "50": "Makes about 50 slices",
            "50 servings": "Makes about 50 slices"
        ]
        return mapping[lower] ?? raw
    }
}
