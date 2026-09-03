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
                "titleGerman": r.titleGerman ?? "",
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

        let payload: [String: Any] = [
            "clientTimestamp": Int64(Date().timeIntervalSince1970 * 1000),
            "clientType": "ios-native",
            "recipes": syncList
        ]

        guard let syncUrl = URL(string: "\(cleanUrl)/api/sync") else {
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
        req.timeoutInterval = 25.0

        let (data, response) = try await URLSession.shared.data(for: req)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            let code = (response as? HTTPURLResponse)?.statusCode ?? -1
            throw NSError(domain: "CloudSync", code: -3, userInfo: [NSLocalizedDescriptionKey: "Sync endpoint returned HTTP \(code)"])
        }

        var pulledCount = 0
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            pulledCount = json["pulledCount"] as? Int ?? 0
        }

        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: "last_sync_timestamp")
        UserDefaults.standard.set("Success (Pushed \(localRecipes.count), Pulled \(pulledCount))", forKey: "last_sync_status")

        return "Sync Complete! (Pushed \(localRecipes.count), Pulled \(pulledCount))"
    }
}
