import Foundation
import SwiftData

@Model
public final class ShoppingItem {
    @Attribute(.unique) public var id: String
    public var recipeId: String?
    public var recipeTitle: String?
    public var name: String
    public var amount: String
    public var unit: String
    public var isChecked: Bool
    public var category: String
    public var createdAt: Date

    public init(
        id: String = UUID().uuidString,
        recipeId: String? = nil,
        recipeTitle: String? = nil,
        name: String,
        amount: String = "",
        unit: String = "",
        isChecked: Bool = false,
        category: String? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.recipeId = recipeId
        self.recipeTitle = recipeTitle
        self.name = name
        self.amount = amount
        self.unit = unit
        self.isChecked = isChecked
        self.category = category ?? ShoppingCategorizer.categorize(name: name)
        self.createdAt = createdAt
    }
}

public enum ShoppingCategorizer {
    public static let categories = [
        "Produce",
        "Dairy & Refrigerated",
        "Meat & Seafood",
        "Bakery",
        "Spices & Baking",
        "Pantry",
        "Other"
    ]

    public static func categorize(name: String) -> String {
        let n = name.lowercased()

        if n.contains("apple") || n.contains("apfel") || n.contains("lemon") || n.contains("zitrone") ||
           n.contains("onion") || n.contains("zwiebel") || n.contains("garlic") || n.contains("knoblauch") ||
           n.contains("potato") || n.contains("kartoffel") || n.contains("carrot") || n.contains("karotte") ||
           n.contains("herbs") || n.contains("parsley") || n.contains("petersilie") || n.contains("thyme") ||
           n.contains("rosemary") || n.contains("berry") || n.contains("beere") {
            return "Produce"
        }

        if n.contains("milk") || n.contains("milch") || n.contains("butter") || n.contains("cream") ||
           n.contains("sahne") || n.contains("egg") || n.contains("ei") || n.contains("quark") ||
           n.contains("cheese") || n.contains("käse") || n.contains("yogurt") || n.contains("joghurt") {
            return "Dairy & Refrigerated"
        }

        if n.contains("beef") || n.contains("rind") || n.contains("pork") || n.contains("schwein") ||
           n.contains("chicken") || n.contains("hähnchen") || n.contains("veal") || n.contains("kalb") ||
           n.contains("fish") || n.contains("fisch") || n.contains("sausage") || n.contains("wurst") {
            return "Meat & Seafood"
        }

        if n.contains("bread") || n.contains("brot") || n.contains("roll") || n.contains("brötchen") ||
           n.contains("crumb") || n.contains("semmel") || n.contains("croissant") {
            return "Bakery"
        }

        if n.contains("flour") || n.contains("mehl") || n.contains("sugar") || n.contains("zucker") ||
           n.contains("baking powder") || n.contains("backpulver") || n.contains("vanilla") ||
           n.contains("cinnamon") || n.contains("zimt") || n.contains("yeast") || n.contains("hefe") ||
           n.contains("cocoa") || n.contains("kakao") || n.contains("salt") || n.contains("salz") ||
           n.contains("starch") || n.contains("stärke") {
            return "Spices & Baking"
        }

        if n.contains("oil") || n.contains("öl") || n.contains("vinegar") || n.contains("essig") ||
           n.contains("pasta") || n.contains("rice") || n.contains("reis") || n.contains("raisin") ||
           n.contains("rosinen") || n.contains("nut") || n.contains("nuss") || n.contains("chocolate") ||
           n.contains("broth") || n.contains("brühe") || n.contains("honey") || n.contains("honig") {
            return "Pantry"
        }

        return "Other"
    }
}
