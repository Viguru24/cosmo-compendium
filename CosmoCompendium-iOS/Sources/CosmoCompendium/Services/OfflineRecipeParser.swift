import Foundation

public struct ParsedRecipeResult {
    public var title: String
    public var titleGerman: String
    public var titleEnglish: String
    public var category: String
    public var servings: String
    public var prepTimeMinutes: Int
    public var cookTimeMinutes: Int
    public var difficulty: String
    public var ingredients: [RecipeIngredient]
    public var steps: [RecipeStep]
    public var notes: String
    public var detectedSourceLanguage: String
}

public enum OfflineRecipeParser {

    private static let germanToEnglishMap: [String: String] = [
        "mehl": "Flour", "weizenmehl": "Wheat Flour", "roggenmehl": "Rye Flour",
        "zucker": "Sugar", "puderzucker": "Powdered Sugar", "brauner zucker": "Brown Sugar",
        "butter": "Butter", "milch": "Milk", "sahne": "Heavy Cream", "schlagsahne": "Whipping Cream",
        "eier": "Eggs", "ei": "Egg", "eigelb": "Egg Yolk", "eiweiß": "Egg White",
        "salz": "Salt", "pfeffer": "Black Pepper", "zimt": "Cinnamon", "vanillezucker": "Vanilla Sugar",
        "backpulver": "Baking Powder", "natron": "Baking Soda", "hefe": "Yeast",
        "äpfel": "Apples", "apfel": "Apple", "kartoffeln": "Potatoes", "zwiebeln": "Onions",
        "knoblauch": "Garlic", "rosinen": "Raisins", "mandeln": "Almonds", "haselnüsse": "Hazelnuts",
        "semmelbrösel": "Breadcrumbs", "paniermehl": "Breadcrumbs", "rindfleisch": "Beef",
        "schweinefleisch": "Pork", "wasser": "Water", "olivenöl": "Olive Oil", "käse": "Cheese",
        "kirschen": "Cherries", "kirschwasser": "Kirschwasser", "rum": "Rum", "schokolade": "Chocolate"
    ]

    public static func parse(text: String) -> ParsedRecipeResult {
        let lines = text.components(separatedBy: .newlines)
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        var title = "Scanned Heirloom Recipe"
        var ingredients: [RecipeIngredient] = []
        var steps: [RecipeStep] = []
        var currentGroup: String? = nil
        var inStepsSection = false

        let unitRegex = try? NSRegularExpression(
            pattern: "^(?:[-*•]\\s*)?([0-9/.,½¼¾⅓⅔]+(?:\\s+[0-9/.,½¼¾⅓⅔]+)?)\\s*(g|kg|ml|l|liter|tl|el|esslöffel|teelöffel|cup|cups|tbsp|tsp|oz|lb|lbs|prise|prisen|pck\\.?|päckchen|bund|dose|dosen|glas|gläser|stk\\.?|stück)?\\s*(.*)$",
            options: .caseInsensitive
        )

        for (index, line) in lines.enumerated() {
            let lower = line.lowercased()

            // Header detection
            if index == 0 && !lower.contains("zutaten") && !lower.contains("ingredients") {
                title = line
                continue
            }

            // Section separators
            if lower.contains("zubereitung") || lower.contains("anweisung") || lower.contains("directions") || lower.contains("instructions") || lower.contains("steps") {
                inStepsSection = true
                continue
            }

            if lower.contains("zutaten") || lower.contains("ingredients") {
                inStepsSection = false
                continue
            }

            // Subgroup headers (e.g. "For the Dough:", "Für die Füllung:")
            if (line.hasSuffix(":") || line.hasPrefix("#")) && !inStepsSection {
                currentGroup = line.replacingOccurrences(of: "[:#*]", with: "", options: .regularExpression).trimmingCharacters(in: .whitespaces)
                continue
            }

            if inStepsSection {
                let cleanStep = line.replacingOccurrences(of: "^(?:\\d+[.)]|[-*•])\\s*", with: "", options: .regularExpression).trimmingCharacters(in: .whitespaces)
                if !cleanStep.isEmpty {
                    // Extract timer if mentioned (e.g. "30 min", "1 Stunde")
                    var timerMins = 0
                    if let timerMatch = cleanStep.range(of: "\\b(\\d+)\\s*(?:minuten?|mins?|min|stunden?|hours?|hrs?)\\b", options: .regularExpression) {
                        let timerStr = String(cleanStep[timerMatch])
                        if let num = Int(timerStr.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()) {
                            timerMins = timerStr.contains("stunde") || timerStr.contains("hour") || timerStr.contains("hr") ? num * 60 : num
                        }
                    }
                    steps.append(RecipeStep(stepNumber: steps.count + 1, instructionEnglish: cleanStep, timerMinutes: timerMins))
                }
            } else {
                // Parse ingredient line
                var matched = false
                if let regex = unitRegex {
                    let ns = line as NSString
                    let results = regex.matches(in: line, range: NSRange(location: 0, length: ns.length))
                    if let first = results.first {
                        let amount = first.range(at: 1).location != NSNotFound ? ns.substring(with: first.range(at: 1)).trimmingCharacters(in: .whitespaces) : ""
                        let unit = first.range(at: 2).location != NSNotFound ? ns.substring(with: first.range(at: 2)).trimmingCharacters(in: .whitespaces) : ""
                        let rawName = first.range(at: 3).location != NSNotFound ? ns.substring(with: first.range(at: 3)).trimmingCharacters(in: .whitespaces) : ""

                        if !rawName.isEmpty {
                            let english = translateGermanIngredient(rawName)
                            ingredients.append(RecipeIngredient(
                                name: rawName,
                                amount: amount,
                                unit: unit,
                                nameGerman: rawName,
                                nameEnglish: english,
                                group: currentGroup
                            ))
                            matched = true
                        }
                    }
                }

                if !matched && !line.hasPrefix("---") {
                    let clean = line.replacingOccurrences(of: "^[-*•]\\s*", with: "", options: .regularExpression)
                    ingredients.append(RecipeIngredient(name: clean, group: currentGroup))
                }
            }
        }

        let isGerman = text.lowercased().contains("zutaten") || text.lowercased().contains("zubereitung") || text.lowercased().contains("teig")

        return ParsedRecipeResult(
            title: title,
            titleGerman: isGerman ? title : "",
            titleEnglish: isGerman ? "" : title,
            category: "Family Classics",
            servings: "4 servings",
            prepTimeMinutes: 20,
            cookTimeMinutes: 30,
            difficulty: "Medium",
            ingredients: ingredients,
            steps: steps,
            notes: "Scanned and parsed with offline fallback engine.",
            detectedSourceLanguage: isGerman ? "de" : "en"
        )
    }

    private static func translateGermanIngredient(_ name: String) -> String {
        let lower = name.lowercased()
        for (de, en) in germanToEnglishMap {
            if lower.contains(de) {
                return en
            }
        }
        return name
    }
}
