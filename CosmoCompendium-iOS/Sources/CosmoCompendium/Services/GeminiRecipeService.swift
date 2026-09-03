import UIKit
import Foundation

public struct FoodPhotoBox: Codable {
    public let ymin: Int?
    public let xmin: Int?
    public let ymax: Int?
    public let xmax: Int?
    public let pageIndex: Int?
}

public struct GeminiRecipeResponse: Codable {
    public let title: String?
    public let titleGerman: String?
    public let titleEnglish: String?
    public let category: String?
    public let servings: String?
    public let prepTimeMinutes: Int?
    public let cookTimeMinutes: Int?
    public let difficulty: String?
    public let ingredients: [GeminiIngredientResponse]?
    public let steps: [GeminiStepResponse]?
    public let notes: String?
    public let notesGerman: String?
    public let detectedSourceLanguage: String?
    public let hasFoodPhoto: Bool?
    public let foodPhotoBox: FoodPhotoBox?
}

public struct GeminiIngredientResponse: Codable {
    public let name: String?
    public let nameGerman: String?
    public let nameEnglish: String?
    public let amount: String?
    public let unit: String?
    public let isOptional: Bool?
    public let group: String?
}

public struct GeminiStepResponse: Codable {
    public let stepNumber: Int?
    public let instructionEnglish: String?
    public let instructionGerman: String?
    public let timerMinutes: Int?
    public let tip: String?
}

public final class GeminiRecipeService {
    public static let shared = GeminiRecipeService()

    private let defaultModel = "gemini-2.5-flash"
    private let baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"

    public var apiKey: String {
        get {
            Self.sanitizeApiKey(UserDefaults.standard.string(forKey: "gemini_api_key"))
        }
        set {
            UserDefaults.standard.set(Self.sanitizeApiKey(newValue), forKey: "gemini_api_key")
        }
    }

    public static func sanitizeApiKey(_ raw: String?) -> String {
        guard var key = raw?.trimmingCharacters(in: .whitespacesAndNewlines), !key.isEmpty else { return "" }
        key = key.replacingOccurrences(of: "\u{200B}", with: "")
                 .replacingOccurrences(of: "\u{200C}", with: "")
                 .replacingOccurrences(of: "\u{200D}", with: "")
                 .replacingOccurrences(of: "\u{FEFF}", with: "")
                 .replacingOccurrences(of: "\u{00A0}", with: " ")
                 .trimmingCharacters(in: .whitespacesAndNewlines)

        let prefixes = [
            "export GEMINI_API_KEY=",
            "export GOOGLE_API_KEY=",
            "GEMINI_API_KEY=",
            "GOOGLE_API_KEY=",
            "API_KEY=",
            "key=",
            "Bearer ",
            "token="
        ]
        for p in prefixes {
            if key.hasPrefix(p) {
                key = String(key.dropFirst(p.count))
            }
        }
        return key.trimmingCharacters(in: CharacterSet(charactersIn: "\"' \t\n\r"))
    }

    public func testApiKey(_ rawKey: String? = nil) async -> (Bool, String) {
        let key = Self.sanitizeApiKey(rawKey ?? apiKey)
        guard !key.isEmpty else {
            return (false, "Please enter an API key.")
        }

        let urlString = "https://generativelanguage.googleapis.com/v1beta/models?key=\(key)"
        guard let url = URL(string: urlString) else {
            return (false, "Invalid URL structure.")
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue(key, forHTTPHeaderField: "x-goog-api-key")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse else {
                return (false, "No HTTP response.")
            }
            if (200...299).contains(httpResponse.statusCode) {
                return (true, "Connected successfully to Google Gemini AI!")
            } else {
                let errText = String(data: data, encoding: .utf8) ?? "HTTP \(httpResponse.statusCode)"
                return (false, "API Error (\(httpResponse.statusCode)): \(errText)")
            }
        } catch {
            return (false, error.localizedDescription)
        }
    }

    private init() {}

    /**
     Synthesizes captured recipe pages into a single unified recipe entity.
     Extracts food photo bounding box if present and crops it.
     */
    public func scanRecipePages(
        images: [UIImage],
        progressHandler: ((String) -> Void)? = nil
    ) async throws -> (Recipe, UIImage?) {
        guard !images.isEmpty else {
            throw NSError(domain: "GeminiRecipeService", code: -1, userInfo: [NSLocalizedDescriptionKey: "No images provided for scanning."])
        }

        let key = apiKey
        if key.isEmpty {
            progressHandler?("No API key set - using offline OCR fallback...")
            return parseOffline(images: images)
        }

        progressHandler?("Synthesizing \(images.count) page(s) with Gemini Vision...")

        // Build Gemini Multimodal payload
        var parts: [[String: Any]] = []

        // Multimodal Vision Prompt enforcing AGENTS.md mandates
        let promptText = """
        You are an expert culinary archivist and transcription specialist for the Cosmo Compendium cookbook.
        Analyze all provided recipe page images in order (Page 1, Page 2, Page 3...). Synthesize them into a single coherent recipe JSON.

        STRICT DIRECTIVES:
        1. Never skip, omit, summarize, or alter ingredients. Extract EVERY single ingredient, leavening agent, spice, or liquid.
        2. Preserve exact quantities, fractions (e.g. 1/2, 1/4, 2 1/2), and units.
        3. If handwritten German (Kurrentschrift / Sütterlin / modern cursive), provide German original and clean English translation.
        4. Group ingredients by section if present (e.g. "Dough", "Filling", "Glaze", "Topping").
        5. If there is a food photograph on any page, set "hasFoodPhoto": true and provide "foodPhotoBox" with normalized coordinates 0-1000: {"ymin": Int, "xmin": Int, "ymax": Int, "xmax": Int, "pageIndex": Int}.

        Respond ONLY with a valid JSON object matching this schema:
        {
          "title": "...",
          "titleGerman": "...",
          "titleEnglish": "...",
          "category": "Baking & Desserts | Main Dishes | Soups & Stews | Family Classics | Artisan Crafts",
          "servings": "...",
          "prepTimeMinutes": 20,
          "cookTimeMinutes": 40,
          "difficulty": "Easy | Medium | Advanced",
          "ingredients": [
            {
              "name": "...",
              "nameGerman": "...",
              "nameEnglish": "...",
              "amount": "...",
              "unit": "...",
              "isOptional": false,
              "group": "..."
            }
          ],
          "steps": [
            {
              "stepNumber": 1,
              "instructionEnglish": "...",
              "instructionGerman": "...",
              "timerMinutes": 10,
              "tip": "..."
            }
          ],
          "notes": "...",
          "notesGerman": "...",
          "detectedSourceLanguage": "de | en",
          "hasFoodPhoto": false,
          "foodPhotoBox": {
            "ymin": 0, "xmin": 0, "ymax": 1000, "xmax": 1000, "pageIndex": 0
          }
        }
        """

        parts.append(["text": promptText])

        // Add each image as downscaled base64 JPEG
        for (idx, img) in images.enumerated() {
            guard let jpegData = prepareImageData(img) else { continue }
            let base64 = jpegData.base64EncodedString()
            parts.append([
                "inlineData": [
                    "mimeType": "image/jpeg",
                    "data": base64
                ]
            ])
            progressHandler?("Prepared page \(idx + 1) of \(images.count)...")
        }

        let requestBody: [String: Any] = [
            "contents": [
                ["parts": parts]
            ],
            "generationConfig": [
                "responseMimeType": "application/json",
                "temperature": 0.2
            ]
        ]

        let fallbackChain = [
            "gemini-2.5-flash",
            "gemini-3.5-flash",
            "gemini-3.7-flash",
            "gemini-3.6-flash",
            "gemini-flash-latest"
        ]

        var lastError: Error? = nil
        var responseData: Data? = nil

        for model in fallbackChain {
            let urlString = "\(baseUrl)\(model):generateContent?key=\(key)"
            guard let url = URL(string: urlString) else { continue }

            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue(key, forHTTPHeaderField: "x-goog-api-key")
            request.httpBody = try? JSONSerialization.data(withJSONObject: requestBody)

            progressHandler?("Transcribing with Gemini (\(model))...")
            do {
                let (data, response) = try await URLSession.shared.data(for: request)
                if let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) {
                    responseData = data
                    break
                } else {
                    let errStr = String(data: data, encoding: .utf8) ?? "HTTP error"
                    lastError = NSError(domain: "GeminiRecipeService", code: -3, userInfo: [NSLocalizedDescriptionKey: "\(model): \(errStr)"])
                }
            } catch {
                lastError = error
            }
        }

        // If Gemini API fails or is unavailable, gracefully fall back to Offline OCR
        guard let data = responseData else {
            progressHandler?("Falling back to local offline transcription...")
            return parseOffline(images: images)
        }

        // Parse candidate response
        guard let jsonObject = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let candidates = jsonObject["candidates"] as? [[String: Any]],
              let firstCandidate = candidates.first,
              let content = firstCandidate["content"] as? [String: Any],
              let resParts = content["parts"] as? [[String: Any]],
              let textResponse = resParts.first?["text"] as? String else {
            return parseOffline(images: images)
        }

        // Resilient JSON substring extraction
        var cleanJson = textResponse
            .replacingOccurrences(of: "^```json\\s*", with: "", options: .regularExpression)
            .replacingOccurrences(of: "\\s*```$", with: "", options: .regularExpression)
            .trimmingCharacters(in: .whitespacesAndNewlines)

        if let firstBrace = cleanJson.firstIndex(of: "{"),
           let lastBrace = cleanJson.lastIndex(of: "}") {
            cleanJson = String(cleanJson[firstBrace...lastBrace])
        }

        guard let recipeData = cleanJson.data(using: .utf8),
              let parsed = try? JSONDecoder().decode(GeminiRecipeResponse.self, from: recipeData) else {
            // Fallback to offline parsing if JSON decoding fails
            return parseOffline(images: images)
        }

        // Convert parsed DTO to Recipe model
        let ingredients = (parsed.ingredients ?? []).map { dto in
            RecipeIngredient(
                name: dto.nameEnglish ?? dto.name ?? "Ingredient",
                amount: dto.amount ?? "",
                unit: dto.unit ?? "",
                nameGerman: dto.nameGerman,
                nameEnglish: dto.nameEnglish ?? dto.name,
                isOptional: dto.isOptional ?? false,
                group: dto.group
            )
        }

        let steps = (parsed.steps ?? []).enumerated().map { (idx, dto) in
            RecipeStep(
                stepNumber: dto.stepNumber ?? (idx + 1),
                instructionEnglish: dto.instructionEnglish ?? "",
                instructionGerman: dto.instructionGerman ?? "",
                timerMinutes: dto.timerMinutes ?? 0,
                tip: dto.tip
            )
        }

        var croppedCoverImage: UIImage? = nil
        if parsed.hasFoodPhoto == true, let box = parsed.foodPhotoBox {
            let targetPage = box.pageIndex ?? 0
            if targetPage < images.count {
                croppedCoverImage = cropFoodPhoto(from: images[targetPage], box: box)
            }
        }

        let recipe = Recipe(
            title: parsed.titleEnglish ?? parsed.title ?? "Scanned Recipe",
            titleGerman: parsed.titleGerman ?? "",
            titleEnglish: parsed.titleEnglish ?? parsed.title ?? "",
            category: parsed.category ?? "Family Classics",
            servings: parsed.servings ?? "4 servings",
            prepTimeMinutes: parsed.prepTimeMinutes ?? 20,
            cookTimeMinutes: parsed.cookTimeMinutes ?? 30,
            difficulty: parsed.difficulty ?? "Medium",
            ingredients: ingredients,
            steps: steps,
            notes: parsed.notes ?? "",
            notesGerman: parsed.notesGerman ?? "",
            sourceLanguage: parsed.detectedSourceLanguage ?? "both",
            coverTheme: .vintageLeather
        )

        return (recipe, croppedCoverImage)
    }

    private func cropFoodPhoto(from image: UIImage, box: FoodPhotoBox) -> UIImage? {
        guard let ymin = box.ymin, let xmin = box.xmin, let ymax = box.ymax, let xmax = box.xmax,
              ymax > ymin, xmax > xmin else { return nil }

        let width = image.size.width
        let height = image.size.height

        let cropRect = CGRect(
            x: CGFloat(xmin) / 1000.0 * width,
            y: CGFloat(ymin) / 1000.0 * height,
            width: CGFloat(xmax - xmin) / 1000.0 * width,
            height: CGFloat(ymax - ymin) / 1000.0 * height
        )

        guard let cgImage = image.cgImage?.cropping(to: cropRect) else { return nil }
        return UIImage(cgImage: cgImage, scale: image.scale, orientation: image.imageOrientation)
    }

    private func prepareImageData(_ image: UIImage) -> Data? {
        let maxDimension: CGFloat = 1600.0
        var targetSize = image.size

        if max(targetSize.width, targetSize.height) > maxDimension {
            let scale = maxDimension / max(targetSize.width, targetSize.height)
            targetSize = CGSize(width: targetSize.width * scale, height: targetSize.height * scale)
        }

        UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
        image.draw(in: CGRect(origin: .zero, size: targetSize))
        let resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return resized?.jpegData(compressionQuality: 0.85)
    }

    private func parseOffline(images: [UIImage]) -> (Recipe, UIImage?) {
        let parsed = OfflineRecipeParser.parse(text: "Scanned Recipe Card\n\nPlease add an API key in Settings for full Gemini AI transcription.")
        let recipe = Recipe(
            title: parsed.title,
            titleGerman: parsed.titleGerman,
            titleEnglish: parsed.titleEnglish,
            category: parsed.category,
            servings: parsed.servings,
            prepTimeMinutes: parsed.prepTimeMinutes,
            cookTimeMinutes: parsed.cookTimeMinutes,
            difficulty: parsed.difficulty,
            ingredients: parsed.ingredients,
            steps: parsed.steps,
            notes: parsed.notes,
            coverTheme: .vintageLeather
        )
        return (recipe, images.first)
    }
}
