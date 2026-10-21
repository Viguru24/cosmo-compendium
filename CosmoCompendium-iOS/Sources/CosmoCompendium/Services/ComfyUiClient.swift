import Foundation
import UIKit

public enum ImageGenEngine: String, CaseIterable, Identifiable {
    case gemini = "gemini"
    case comfyUi = "comfy_ui"

    public var id: String { rawValue }
    public var label: String {
        switch self {
        case .gemini: return "☁️ Gemini Cloud"
        case .comfyUi: return "🖥️ ComfyUI (Local PC)"
        }
    }
    public var description: String {
        switch self {
        case .gemini: return "Google Gemini Cloud API"
        case .comfyUi: return "Local GPU Machine on Wi-Fi"
        }
    }
}

public final class ComfyUiClient {
    public static let shared = ComfyUiClient()
    private init() {}

    public static func normalizeBaseUrl(_ input: String) -> String {
        var trimmed = input.trimmingCharacters(in: .whitespacesAndNewlines)
        while trimmed.hasSuffix("/") {
            trimmed.removeLast()
        }
        if !trimmed.lowercased().hasPrefix("http://") && !trimmed.lowercased().hasPrefix("https://") {
            trimmed = "http://\(trimmed)"
        }
        return trimmed
    }

    public func testConnection(baseUrl: String) async -> (Bool, String) {
        let base = Self.normalizeBaseUrl(baseUrl)
        guard let url = URL(string: "\(base)/system_stats") else {
            return (false, "Invalid URL structure.")
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 8.0

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                let checkpoints = await fetchAvailableCheckpoints(baseUrl: base)
                if !checkpoints.isEmpty {
                    let summary = checkpoints.prefix(3).joined(separator: ", ")
                    return (true, "Connected to ComfyUI! Checkpoints: \(summary)")
                }
                return (true, "Connected to ComfyUI successfully!")
            } else {
                // Try fallback to root /
                if let rootUrl = URL(string: base) {
                    var rootReq = URLRequest(url: rootUrl)
                    rootReq.timeoutInterval = 5.0
                    if let (_, rResp) = try? await URLSession.shared.data(for: rootReq),
                       let rHttp = rResp as? HTTPURLResponse, (200...299).contains(rHttp.statusCode) {
                        return (true, "Connected to ComfyUI!")
                    }
                }
                return (false, "ComfyUI returned HTTP \((response as? HTTPURLResponse)?.statusCode ?? -1)")
            }
        } catch {
            return (false, "Cannot reach ComfyUI at \(base). Make sure your PC is running ComfyUI with '--listen 0.0.0.0' and your iPhone is on the same Wi-Fi.")
        }
    }

    public func fetchAvailableCheckpoints(baseUrl: String) async -> [String] {
        let base = Self.normalizeBaseUrl(baseUrl)
        guard let url = URL(string: "\(base)/object_info/CheckpointLoaderSimple") else { return [] }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 8.0

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode),
                  let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let loader = json["CheckpointLoaderSimple"] as? [String: Any],
                  let input = loader["input"] as? [String: Any],
                  let required = input["required"] as? [String: Any],
                  let ckptTuple = required["ckpt_name"] as? [Any],
                  let list = ckptTuple.first as? [String] else {
                return []
            }
            return list.filter { !$0.lowercased().contains("audio") }
        } catch {
            return []
        }
    }

    public func generateRecipeImage(
        baseUrl: String,
        title: String,
        category: String,
        ingredients: [String],
        steps: [String],
        customCheckpoint: String? = nil,
        progressHandler: ((String) -> Void)? = nil
    ) async throws -> UIImage {
        let base = Self.normalizeBaseUrl(baseUrl)
        progressHandler?("Connecting to local ComfyUI on Wi-Fi...")

        let available = await fetchAvailableCheckpoints(baseUrl: base)
        var ckpt = customCheckpoint?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if ckpt.isEmpty || (!available.isEmpty && !available.contains(ckpt)) {
            let preferred = available.first(where: { $0.localizedCaseInsensitiveContains("turbo") || $0.localizedCaseInsensitiveContains("xl") || $0.localizedCaseInsensitiveContains("v1-5") })
            ckpt = preferred ?? available.first ?? "v1-5-pruned-emaonly.safetensors"
        }

        let ingList = ingredients.prefix(5).joined(separator: ", ")
        let positivePrompt = "Professional culinary food photography of \(title), \(category), prepared with \(ingList). Delicious presentation, garnished on vintage rustic tableware, warm studio lighting, 85mm lens, sharp focus, vibrant natural food textures, photorealistic masterpiece, Michelin dining aesthetic."
        let negativePrompt = "text, watermark, typography, logo, badge, blurry, cartoon, illustration, low quality, oversaturated, deformed, plastic, raw, unappetizing"

        let clientId = "ios-cookbook-\(UUID().uuidString.prefix(8))"
        let seed = Int.random(in: 100_000_000...999_999_999)

        // Construct standard ComfyUI workflow JSON
        let promptWorkflow: [String: Any] = [
            "1": [
                "class_type": "CheckpointLoaderSimple",
                "inputs": ["ckpt_name": ckpt]
            ],
            "2": [
                "class_type": "EmptyLatentImage",
                "inputs": ["width": 768, "height": 768, "batch_size": 1]
            ],
            "3": [
                "class_type": "CLIPTextEncode",
                "inputs": ["text": positivePrompt, "clip": ["1", 1]]
            ],
            "4": [
                "class_type": "CLIPTextEncode",
                "inputs": ["text": negativePrompt, "clip": ["1", 1]]
            ],
            "5": [
                "class_type": "KSampler",
                "inputs": [
                    "seed": seed,
                    "steps": ckpt.lowercased().contains("turbo") ? 8 : 20,
                    "cfg": ckpt.lowercased().contains("turbo") ? 2.0 : 7.0,
                    "sampler_name": "euler",
                    "scheduler": "normal",
                    "denoise": 1.0,
                    "model": ["1", 0],
                    "positive": ["3", 0],
                    "negative": ["4", 0],
                    "latent_image": ["2", 0]
                ]
            ],
            "6": [
                "class_type": "VAEDecode",
                "inputs": ["samples": ["5", 0], "vae": ["1", 2]]
            ],
            "7": [
                "class_type": "SaveImage",
                "inputs": ["filename_prefix": "CosmoCompendium", "images": ["6", 0]]
            ]
        ]

        let requestPayload: [String: Any] = [
            "prompt": promptWorkflow,
            "client_id": clientId
        ]

        guard let promptUrl = URL(string: "\(base)/prompt") else {
            throw NSError(domain: "ComfyUI", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid ComfyUI URL"])
        }

        var promptReq = URLRequest(url: promptUrl)
        promptReq.httpMethod = "POST"
        promptReq.setValue("application/json", forHTTPHeaderField: "Content-Type")
        promptReq.httpBody = try JSONSerialization.data(withJSONObject: requestPayload)
        promptReq.timeoutInterval = 20.0

        progressHandler?("Queuing generation on Wi-Fi GPU (\(ckpt))...")
        let (promptData, promptResp) = try await URLSession.shared.data(for: promptReq)
        guard let pHttp = promptResp as? HTTPURLResponse, (200...299).contains(pHttp.statusCode),
              let pJson = try? JSONSerialization.jsonObject(with: promptData) as? [String: Any],
              let promptId = pJson["prompt_id"] as? String else {
            let err = String(data: promptData, encoding: .utf8) ?? "HTTP error"
            throw NSError(domain: "ComfyUI", code: -2, userInfo: [NSLocalizedDescriptionKey: "ComfyUI error: \(err)"])
        }

        progressHandler?("Rendering photo on your PC GPU...")

        // Poll /history/{prompt_id} until image is completed (timeout 90s)
        let startTime = Date()
        var imageFilename: String?
        var subfolder = ""
        var type = "output"

        while Date().timeIntervalSince(startTime) < 90.0 {
            try await Task.sleep(nanoseconds: 1_500_000_000) // 1.5s
            guard let histUrl = URL(string: "\(base)/history/\(promptId)") else { continue }
            var hReq = URLRequest(url: histUrl)
            hReq.timeoutInterval = 8.0

            if let (hData, hResp) = try? await URLSession.shared.data(for: hReq),
               let hHttp = hResp as? HTTPURLResponse, (200...299).contains(hHttp.statusCode),
               let hJson = try? JSONSerialization.jsonObject(with: hData) as? [String: Any],
               let item = hJson[promptId] as? [String: Any],
               let outputs = item["outputs"] as? [String: Any] {
                for (_, outVal) in outputs {
                    if let outObj = outVal as? [String: Any],
                       let imagesArr = outObj["images"] as? [[String: Any]],
                       let firstImg = imagesArr.first,
                       let fname = firstImg["filename"] as? String {
                        imageFilename = fname
                        subfolder = firstImg["subfolder"] as? String ?? ""
                        type = firstImg["type"] as? String ?? "output"
                        break
                    }
                }
            }

            if imageFilename != nil { break }
        }

        guard let filename = imageFilename else {
            throw NSError(domain: "ComfyUI", code: -3, userInfo: [NSLocalizedDescriptionKey: "Generation timed out after 90 seconds. Check your ComfyUI terminal window."])
        }

        progressHandler?("Downloading high-resolution photo from PC...")
        var viewComponents = URLComponents(string: "\(base)/view")
        viewComponents?.queryItems = [
            URLQueryItem(name: "filename", value: filename),
            URLQueryItem(name: "subfolder", value: subfolder),
            URLQueryItem(name: "type", value: type)
        ]

        guard let viewUrl = viewComponents?.url else {
            throw NSError(domain: "ComfyUI", code: -4, userInfo: [NSLocalizedDescriptionKey: "Invalid view URL"])
        }

        var viewReq = URLRequest(url: viewUrl)
        viewReq.timeoutInterval = 20.0
        let (viewData, viewResp) = try await URLSession.shared.data(for: viewReq)
        guard let vHttp = viewResp as? HTTPURLResponse, (200...299).contains(vHttp.statusCode),
              let downloadedImage = UIImage(data: viewData) else {
            throw NSError(domain: "ComfyUI", code: -5, userInfo: [NSLocalizedDescriptionKey: "Failed to download generated image from ComfyUI."])
        }

        return downloadedImage
    }
}
