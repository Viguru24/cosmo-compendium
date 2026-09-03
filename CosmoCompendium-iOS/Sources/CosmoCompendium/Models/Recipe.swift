import Foundation
import SwiftData

@Model
public final class Recipe {
    @Attribute(.unique) public var id: String
    public var title: String
    public var titleGerman: String
    public var titleEnglish: String
    public var category: String
    public var servings: String
    public var prepTimeMinutes: Int
    public var cookTimeMinutes: Int
    public var difficulty: String
    public var ingredientsData: Data
    public var stepsData: Data
    public var notes: String
    public var notesGerman: String
    public var sourceLanguage: String
    public var imagePath: String?
    public var coverThemeRaw: String
    public var isFavorite: Bool
    public var rating: Int
    public var timesCooked: Int
    public var originStory: String
    public var createdAt: Date
    public var updatedAt: Date
    public var isDeleted: Bool
    public var coverPhotoName: String?
    public var profileName: String = "Louis"

    // Craft / Formula fields (Soap, Balms, Maker notes)
    public var craftType: String?
    public var lyeRatio: String?
    public var waterDiscount: String?
    public var fragranceLoad: String?
    public var cureTimeWeeks: Int?
    public var batchSizeGrams: Double?

    public init(
        id: String = UUID().uuidString,
        title: String,
        titleGerman: String = "",
        titleEnglish: String = "",
        category: String = "Family Classics",
        servings: String = "4-6 servings",
        prepTimeMinutes: Int = 20,
        cookTimeMinutes: Int = 40,
        difficulty: String = "Medium",
        ingredients: [RecipeIngredient] = [],
        steps: [RecipeStep] = [],
        notes: String = "",
        notesGerman: String = "",
        sourceLanguage: String = "both",
        imagePath: String? = nil,
        coverTheme: CoverTheme = .vintageLeather,
        isFavorite: Bool = false,
        rating: Int = 5,
        timesCooked: Int = 0,
        originStory: String = "Handwritten family recipe from grandmother's kitchen.",
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
        isDeleted: Bool = false,
        coverPhotoName: String? = nil,
        profileName: String = "Louis",
        craftType: String? = nil,
        lyeRatio: String? = nil,
        waterDiscount: String? = nil,
        fragranceLoad: String? = nil,
        cureTimeWeeks: Int? = nil,
        batchSizeGrams: Double? = nil
    ) {
        self.id = id
        self.title = title
        self.titleGerman = titleGerman
        self.titleEnglish = titleEnglish
        self.category = category
        self.servings = servings
        self.prepTimeMinutes = prepTimeMinutes
        self.cookTimeMinutes = cookTimeMinutes
        self.difficulty = difficulty
        self.ingredientsData = (try? JSONEncoder().encode(ingredients)) ?? Data()
        self.stepsData = (try? JSONEncoder().encode(steps)) ?? Data()
        self.notes = notes
        self.notesGerman = notesGerman
        self.sourceLanguage = sourceLanguage
        self.imagePath = imagePath
        self.coverThemeRaw = coverTheme.rawValue
        self.isFavorite = isFavorite
        self.rating = rating
        self.timesCooked = timesCooked
        self.originStory = originStory
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.isDeleted = isDeleted
        self.coverPhotoName = coverPhotoName
        self.profileName = profileName
        self.craftType = craftType
        self.lyeRatio = lyeRatio
        self.waterDiscount = waterDiscount
        self.fragranceLoad = fragranceLoad
        self.cureTimeWeeks = cureTimeWeeks
        self.batchSizeGrams = batchSizeGrams
    }

    public var coverTheme: CoverTheme {
        get { CoverTheme(rawValue: coverThemeRaw) ?? .vintageLeather }
        set { coverThemeRaw = newValue.rawValue }
    }

    public var ingredients: [RecipeIngredient] {
        get {
            (try? JSONDecoder().decode([RecipeIngredient].self, from: ingredientsData)) ?? []
        }
        set {
            ingredientsData = (try? JSONEncoder().encode(newValue)) ?? Data()
        }
    }

    public var steps: [RecipeStep] {
        get {
            (try? JSONDecoder().decode([RecipeStep].self, from: stepsData)) ?? []
        }
        set {
            stepsData = (try? JSONEncoder().encode(newValue)) ?? Data()
        }
    }

    public var totalTimeMinutes: Int {
        prepTimeMinutes + cookTimeMinutes
    }

    public func displayTitle(language: LanguageMode = .english) -> String {
        switch language {
        case .german:
            return !titleGerman.isEmpty ? titleGerman : title
        case .english, .both:
            return !titleEnglish.isEmpty ? titleEnglish : title
        }
    }

    public func displayNotes(language: LanguageMode = .english) -> String {
        switch language {
        case .german:
            return !notesGerman.isEmpty ? notesGerman : notes
        case .english, .both:
            return !notes.isEmpty ? notes : notesGerman
        }
    }

    public var characteristicBadge: String? {
        let textComponents: [String] = [title, titleGerman, titleEnglish, notes, notesGerman, originStory]
        let fullText = textComponents.joined(separator: " ").lowercased()

        var ingNames: [String] = []
        for ing in ingredients {
            ingNames.append(ing.name.lowercased())
            if let en = ing.nameEnglish, !en.isEmpty { ingNames.append(en.lowercased()) }
            if let de = ing.nameGerman, !de.isEmpty { ingNames.append(de.lowercased()) }
        }
        let allIngredients = ingNames.joined(separator: " ")

        var stepTexts: [String] = []
        for s in steps {
            stepTexts.append(s.instructionEnglish.lowercased())
            stepTexts.append(s.instructionGerman.lowercased())
        }
        let allSteps = stepTexts.joined(separator: " ")

        let hasFlour = allIngredients.contains("flour") || allIngredients.contains("mehl") || allIngredients.contains("farine") || allIngredients.contains("farina") || allIngredients.contains("harina")
        if (category.localizedCaseInsensitiveContains("Baking") || category.localizedCaseInsensitiveContains("Dessert") || fullText.contains("cake") || fullText.contains("kuchen") || fullText.contains("torte")) && !hasFlour && !ingredients.isEmpty {
            return "Flourless"
        }
        if fullText.contains("bundt") || fullText.contains("gugelhupf") || fullText.contains("napfkuchen") || allSteps.contains("bundt") || allSteps.contains("gugelhupf") {
            return "Bundt"
        }
        if fullText.contains("lava") || fullText.contains("molten") || fullText.contains("fondant") || fullText.contains("flüssiger kern") {
            return "Molten Lava"
        }
        if fullText.contains("sourdough") || fullText.contains("sauerteig") || allIngredients.contains("sourdough") || allIngredients.contains("sauerteig") {
            return "Sourdough"
        }
        if totalTimeMinutes > 0 && totalTimeMinutes <= 35 {
            return "Quick 30-Min"
        }
        if totalTimeMinutes >= 180 || fullText.contains("slow cook") || fullText.contains("schmoren") || fullText.contains("overnight") {
            return "Slow-Cooked"
        }
        if let yearMatch = try? NSRegularExpression(pattern: "\\b(18\\d{2}|19\\d{2})\\b").firstMatch(in: originStory + " " + notes, range: NSRange(location: 0, length: (originStory + " " + notes).utf16.count)),
           let range = Range(yearMatch.range(at: 1), in: originStory + " " + notes) {
            return "Vintage \(originStory + " " + notes)[range]"
        }
        if fullText.contains("one bowl") || fullText.contains("one-bowl") || fullText.contains("one pot") || fullText.contains("one-pot") || fullText.contains("eintopf") {
            return "One-Bowl"
        }
        if fullText.contains("gluten-free") || fullText.contains("glutenfrei") || fullText.contains("sans gluten") {
            return "Gluten-Free"
        }
        return nil
    }
}
