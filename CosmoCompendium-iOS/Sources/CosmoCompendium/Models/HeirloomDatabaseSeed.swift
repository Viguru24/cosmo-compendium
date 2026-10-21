import Foundation

public struct HeirloomRecipeDTO: Codable {
    public let id: String
    public let title: String
    public let titleGerman: String?
    public let titleEnglish: String?
    public let category: String?
    public let servings: String?
    public let prepTimeMinutes: Int?
    public let cookTimeMinutes: Int?
    public let difficulty: String?
    public let ingredients: [RecipeIngredient]?
    public let steps: [RecipeStep]?
    public let notes: String?
    public let notesGerman: String?
    public let sourceLanguage: String?
    public let coverTheme: String?
    public let isFavorite: Bool?
    public let rating: Int?
    public let timesCooked: Int?
    public let originStory: String?
    public let createdAt: Double?
    public let updatedAt: Double?
    public let isDeleted: Bool?
    public let coverPhotoName: String?
    public let profileName: String?

    public func toRecipe() -> Recipe {
        let theme = CoverTheme(rawValue: coverTheme ?? "VINTAGE_LEATHER") ?? .vintageLeather
        let cDate = createdAt.map { val in Date(timeIntervalSince1970: val > 1_000_000_000_000 ? val / 1000.0 : val) } ?? Date()
        let uDate = updatedAt.map { val in Date(timeIntervalSince1970: val > 1_000_000_000_000 ? val / 1000.0 : val) } ?? Date()

        var resolvedImagePath: String? = nil
        if let coverName = coverPhotoName, !coverName.isEmpty {
            // Check app bundle SyncedImages first
            if let bundleUrl = Bundle.main.url(forResource: (coverName as NSString).deletingPathExtension, withExtension: (coverName as NSString).pathExtension, subdirectory: "SyncedImages") ?? Bundle.main.url(forResource: coverName, withExtension: nil) {
                resolvedImagePath = bundleUrl.path
            } else {
                // Check Documents directory
                let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
                let localFile = docs.appendingPathComponent(coverName)
                if FileManager.default.fileExists(atPath: localFile.path) {
                    resolvedImagePath = localFile.path
                }
            }
        }

        return Recipe(
            id: id,
            title: title,
            titleGerman: titleGerman ?? "",
            titleEnglish: (titleEnglish?.isEmpty == false) ? titleEnglish! : title,
            category: category ?? "Family Classics",
            servings: servings ?? "4 servings",
            prepTimeMinutes: prepTimeMinutes ?? 20,
            cookTimeMinutes: cookTimeMinutes ?? 30,
            difficulty: difficulty ?? "Medium",
            ingredients: ingredients ?? [],
            steps: steps ?? [],
            notes: notes ?? "",
            notesGerman: notesGerman ?? "",
            sourceLanguage: sourceLanguage ?? "en",
            imagePath: resolvedImagePath,
            coverTheme: theme,
            isFavorite: isFavorite ?? false,
            rating: rating ?? 5,
            timesCooked: timesCooked ?? 0,
            originStory: originStory ?? "Handwritten heirloom family recipe.",
            createdAt: cDate,
            updatedAt: uDate,
            isDeleted: isDeleted ?? false,
            coverPhotoName: coverPhotoName,
            profileName: (profileName?.isEmpty == false) ? profileName! : "Annette"
        )
    }
}

public enum HeirloomDatabaseSeed {
    public static func loadRecipes() -> [Recipe] {
        if let url = Bundle.main.url(forResource: "synced_vps_recipes", withExtension: "json"),
           let data = try? Data(contentsOf: url),
           let dtos = try? JSONDecoder().decode([HeirloomRecipeDTO].self, from: data) {
            let recipes = dtos.map { $0.toRecipe() }
            if !recipes.isEmpty {
                return recipes
            }
        }

        if let url = Bundle.main.url(forResource: "heirloom_recipes", withExtension: "json"),
           let data = try? Data(contentsOf: url),
           let dtos = try? JSONDecoder().decode([HeirloomRecipeDTO].self, from: data) {
            return dtos.map { $0.toRecipe() }
        }

        return DefaultRecipes.fallbackHardcodedRecipes()
    }
}
