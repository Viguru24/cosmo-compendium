import Foundation

// MARK: - Step Ingredient Matcher
public struct MatchedStepIngredient: Identifiable {
    public var id: String { ingredient.id }
    public let ingredient: RecipeIngredient
    public let displayAmount: String

    public init(ingredient: RecipeIngredient, displayAmount: String) {
        self.ingredient = ingredient
        self.displayAmount = displayAmount
    }
}

public enum StepIngredientMatcher {
    private static let stopWords: Set<String> = [
        "in", "at", "to", "for", "with", "from", "by", "on", "off", "into", "onto",
        "and", "or", "a", "an", "the", "all", "each", "every", "both", "few", "more",
        "top", "bottom", "side", "pan", "pot", "bowl", "dish", "oven", "heat", "cook",
        "bake", "stir", "mix", "whisk", "fold", "pour", "add", "place", "set", "let",
        "until", "after", "before", "while", "when", "then", "now", "well", "gently",
        "thoroughly", "together", "aside", "ready", "done", "warm", "hot", "cold",
        "medium", "high", "low", "minutes", "min", "hours", "hr", "degrees", "c", "f"
    ]

    public static func findIngredients(
        in stepInstruction: String,
        ingredients: [RecipeIngredient],
        unitSystem: UnitSystem = .ukImperial,
        multiplier: Double = 1.0
    ) -> [MatchedStepIngredient] {
        guard !stepInstruction.isEmpty, !ingredients.isEmpty else { return [] }

        let lower = stepInstruction.lowercased()
        var results: [MatchedStepIngredient] = []
        var seenIds = Set<String>()

        for ing in ingredients {
            guard !seenIds.contains(ing.id) else { continue }
            let amountStr = ing.convertedAmount(targetSystem: unitSystem, multiplier: multiplier)

            let candidateNames = [
                ing.nameEnglish?.lowercased(),
                ing.name.lowercased(),
                ing.nameGerman?.lowercased()
            ].compactMap { $0 }.filter { !$0.isEmpty }

            var matched = false
            for cand in candidateNames {
                let words = cand.components(separatedBy: CharacterSet.alphanumerics.inverted).filter { $0.count >= 3 && !stopWords.contains($0) }
                for word in words {
                    if lower.contains(word) {
                        matched = true
                        break
                    }
                }
                if matched { break }
            }

            if matched {
                seenIds.insert(ing.id)
                results.append(MatchedStepIngredient(ingredient: ing, displayAmount: amountStr))
            }
        }

        return results
    }
}
