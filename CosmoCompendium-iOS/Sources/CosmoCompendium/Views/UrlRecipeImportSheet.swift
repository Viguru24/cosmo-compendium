import SwiftUI
import SwiftData

public struct UrlRecipeImportSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var profileManager = ProfileManager.shared

    @State private var urlString: String = ""
    @State private var isExtracting: Bool = false
    @State private var statusMessage: String = ""
    @State private var errorMessage: String? = nil
    @State private var showErrorAlert: Bool = false

    let onRecipeImported: (Recipe) -> Void

    public init(onRecipeImported: @escaping (Recipe) -> Void) {
        self.onRecipeImported = onRecipeImported
    }

    public var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                // Header icon
                ZStack {
                    Circle()
                        .fill(Color(red: 0xFF / 255.0, green: 0xF7 / 255.0, blue: 0xED / 255.0))
                        .frame(width: 72, height: 72)
                    Image(systemName: "link.badge.plus")
                        .font(.system(size: 32))
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }
                .padding(.top, 24)

                Text("Import Recipe from Web")
                    .font(.system(size: 20, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))

                Text("Paste a link from any cooking website, culinary blog, or food magazine. Gemini AI will extract the ingredients and steps directly into your collection.")
                    .font(.system(size: 13))
                    .foregroundStyle(Color.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)

                // URL Input Field with Paste button
                HStack {
                    TextField("https://www.example.com/recipe...", text: $urlString)
                        .font(.system(size: 14))
                        .autocapitalization(.none)
                        .keyboardType(.URL)

                    if !urlString.isEmpty {
                        Button {
                            urlString = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(Color.gray)
                        }
                    } else if let pasted = UIPasteboard.general.string, pasted.hasPrefix("http") {
                        Button {
                            urlString = pasted
                        } label: {
                            Text("Paste")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                        }
                    }
                }
                .padding(14)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 14))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0).opacity(0.5), lineWidth: 1.5)
                )
                .padding(.horizontal, 20)

                // Target Profile Indicator
                HStack(spacing: 6) {
                    Text("Saving into:")
                        .font(.system(size: 12))
                        .foregroundStyle(.secondary)
                    Text("\(ProfileManager.getProfileEmoji(name: profileManager.activeProfile)) \(profileManager.activeProfile)'s Cookbook")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }

                if isExtracting {
                    VStack(spacing: 8) {
                        ProgressView()
                            .controlSize(.large)
                            .tint(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                        Text(statusMessage)
                            .font(.system(size: 13, design: .serif))
                            .foregroundStyle(Color.secondary)
                    }
                    .padding(.top, 10)
                }

                Spacer()

                // Extract Recipe Button
                Button {
                    Task {
                        await extractRecipe()
                    }
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "sparkles")
                        Text(isExtracting ? "Extracting Recipe..." : "Extract Recipe with AI")
                    }
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(Color.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(
                        urlString.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isExtracting ? Color.gray.opacity(0.4) : Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0),
                        in: RoundedRectangle(cornerRadius: 14)
                    )
                }
                .disabled(urlString.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isExtracting)
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
            .background(Color(red: 0xFF / 255.0, green: 0xFD / 255.0, blue: 0xF9 / 255.0))
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                        .foregroundStyle(Color.secondary)
                }
            }
            .alert("Extraction Error", isPresented: $showErrorAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(errorMessage ?? "Unable to extract recipe from the provided URL.")
            }
        }
    }

    private func extractRecipe() async {
        let cleanUrl = urlString.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let url = URL(string: cleanUrl), url.scheme != nil else {
            errorMessage = "Please enter a valid URL starting with http:// or https://"
            showErrorAlert = true
            return
        }

        isExtracting = true
        statusMessage = "Fetching webpage content..."

        do {
            var request = URLRequest(url: url)
            request.setValue("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1", forHTTPHeaderField: "User-Agent")

            let (data, _) = try await URLSession.shared.data(for: request)
            guard let htmlString = String(data: data, encoding: .utf8) ?? String(data: data, encoding: .isoLatin1) else {
                throw NSError(domain: "UrlExtractor", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to decode webpage text."])
            }

            statusMessage = "Parsing recipe structure with Gemini..."

            // Truncate HTML to avoid prompt limits while capturing recipe body
            let bodySnippet = String(htmlString.prefix(15000))

            let prompt = """
            You are a culinary transcription specialist. Extract the recipe from this webpage HTML into a clean JSON format.
            Respond ONLY with a valid JSON object matching:
            {
              "title": "...",
              "category": "Baking & Desserts | Main Dishes | Soups & Stews | Family Classics",
              "servings": "...",
              "prepTimeMinutes": 20,
              "cookTimeMinutes": 30,
              "difficulty": "Easy | Medium | Advanced",
              "ingredients": [{"name": "...", "amount": "...", "unit": "..."}],
              "steps": [{"stepNumber": 1, "instructionEnglish": "...", "timerMinutes": 0}],
              "notes": "..."
            }

            HTML:
            \(bodySnippet)
            """

            let text = try await GeminiRecipeService.shared.generateText(prompt: prompt)

            var cleanJson = text.trimmingCharacters(in: .whitespacesAndNewlines)
            if let firstBrace = cleanJson.firstIndex(of: "{"),
               let lastBrace = cleanJson.lastIndex(of: "}") {
                cleanJson = String(cleanJson[firstBrace...lastBrace])
            }

            guard let recipeJsonData = cleanJson.data(using: .utf8),
                  let parsed = try? JSONDecoder().decode(GeminiRecipeResponse.self, from: recipeJsonData) else {
                throw NSError(domain: "UrlExtractor", code: -5, userInfo: [NSLocalizedDescriptionKey: "Invalid recipe data returned."])
            }

            let ings = (parsed.ingredients ?? []).map {
                RecipeIngredient(name: $0.nameEnglish ?? $0.name ?? "Ingredient", amount: $0.amount ?? "", unit: $0.unit ?? "")
            }
            let steps = (parsed.steps ?? []).enumerated().map { (idx, s) in
                RecipeStep(stepNumber: s.stepNumber ?? (idx + 1), instructionEnglish: s.instructionEnglish ?? "", timerMinutes: s.timerMinutes ?? 0)
            }

            let newRecipe = Recipe(
                title: parsed.title ?? "Imported Web Recipe",
                category: parsed.category ?? "Family Classics",
                servings: parsed.servings ?? "4 servings",
                prepTimeMinutes: parsed.prepTimeMinutes ?? 15,
                cookTimeMinutes: parsed.cookTimeMinutes ?? 30,
                difficulty: parsed.difficulty ?? "Medium",
                ingredients: ings,
                steps: steps,
                notes: parsed.notes ?? "",
                originStory: "Imported from \(cleanUrl)",
                profileName: profileManager.activeProfile
            )
            newRecipe.profileName = profileManager.activeProfile

            await MainActor.run {
                modelContext.insert(newRecipe)
                try? modelContext.save()
                isExtracting = false
                dismiss()
                onRecipeImported(newRecipe)
            }
        } catch {
            await MainActor.run {
                isExtracting = false
                errorMessage = error.localizedDescription
                showErrorAlert = true
            }
        }
    }
}
