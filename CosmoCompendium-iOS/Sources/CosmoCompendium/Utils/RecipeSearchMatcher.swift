import Foundation

public struct RecipeSearchMatcher {
    private static let searchExcludedPrefixMatches: [String: Set<String>] = [
        "pie": ["piece", "pieces", "piecemeal", "piecework", "pierce", "pierced", "pierces", "piercing", "pierogi", "pierogis", "pierogie", "pierogies", "pier", "piers", "piety", "pietism", "pipe", "pipes", "piping", "pipeline"],
        "pea": ["pear", "pears", "pearl", "pearls", "peace", "peasant"],
        "gin": ["ginger", "gingersnap", "gingersnaps", "gingery"],
        "bar": ["bare", "bark", "barks", "barley"],
        "cur": ["curry", "curries", "current", "currents", "currant", "currants"],
        "fig": ["fight", "fights", "figure", "figures"],
        "can": ["candle", "candles", "candy", "candies", "candied", "cantaloupe", "cantaloupes"]
    ]

    public static func recipeMatchesSearch(_ recipe: Recipe, query: String) -> Bool {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if trimmed.isEmpty { return true }

        let queryTokens = trimmed.components(separatedBy: CharacterSet.alphanumerics.inverted).filter { !$0.isEmpty }
        if queryTokens.isEmpty { return true }

        let titleCombined = [recipe.title, recipe.titleEnglish, recipe.titleGerman]
            .filter { !$0.isEmpty }
            .joined(separator: " ")
            .lowercased()

        let categoryCombined = recipe.category.lowercased()

        let ingredientsCombined = recipe.ingredients
            .flatMap { [$0.name, $0.nameEnglish ?? "", $0.nameGerman ?? ""] }
            .filter { !$0.isEmpty }
            .joined(separator: " ")
            .lowercased()

        let stepTexts = recipe.steps.flatMap { [$0.instructionEnglish, $0.instructionGerman, $0.tip ?? ""] }
        let notesAndStepsCombined = ([recipe.notes, recipe.notesGerman, recipe.originStory] + stepTexts)
            .filter { !$0.isEmpty }
            .joined(separator: " ")
            .lowercased()

        // 1. Exact phrase match in title or single-token word match in title
        if matchesPhrase(in: titleCombined, phrase: trimmed) ||
           (queryTokens.count == 1 && matchesAnyWord(in: titleCombined, token: queryTokens[0])) {
            return true
        }

        // 2. All tokens match title
        if queryTokens.allSatisfy({ matchesAnyWord(in: titleCombined, token: $0) }) {
            return true
        }

        // 3. Category match
        if matchesPhrase(in: categoryCombined, phrase: trimmed) ||
           (queryTokens.count == 1 && matchesAnyWord(in: categoryCombined, token: queryTokens[0])) {
            return true
        }

        // 4. Ingredient match
        if matchesPhrase(in: ingredientsCombined, phrase: trimmed) ||
           (queryTokens.count == 1 && matchesAnyWord(in: ingredientsCombined, token: queryTokens[0])) {
            return true
        }

        // 5. Query tokens distributed across Title + Category + Ingredients
        let allTokensInCore = queryTokens.allSatisfy { token in
            matchesAnyWord(in: titleCombined, token: token) ||
            matchesAnyWord(in: categoryCombined, token: token) ||
            matchesAnyWord(in: ingredientsCombined, token: token)
        }
        if allTokensInCore { return true }

        // 6. Single-token query fallback for notes / instructions (only for true word matches)
        if queryTokens.count == 1 && matchesAnyWord(in: notesAndStepsCombined, token: queryTokens[0]) {
            return true
        }

        return false
    }

    public static func matchesPhrase(in text: String, phrase: String) -> Bool {
        if phrase.isEmpty || text.isEmpty { return false }
        let tokens = phrase.components(separatedBy: CharacterSet.alphanumerics.inverted).filter { !$0.isEmpty }
        if tokens.isEmpty { return false }
        if tokens.count == 1 { return matchesAnyWord(in: text, token: tokens[0]) }

        let escapedTokens = tokens.map { NSRegularExpression.escapedPattern(for: $0) }
        let pattern = "(?:^|[^\\p{L}\\p{Nd}])" + escapedTokens.joined(separator: "[^\\p{L}\\p{Nd}]+") + "(?:$|[^\\p{L}\\p{Nd}])"
        return text.range(of: pattern, options: [.regularExpression, .caseInsensitive]) != nil
    }

    public static func matchesAnyWord(in text: String, token: String) -> Bool {
        if text.isEmpty || token.isEmpty { return false }
        let words = text.components(separatedBy: CharacterSet.alphanumerics.inverted).filter { !$0.isEmpty }
        return words.contains { isWordMatch(candidate: $0, token: token) }
    }

    public static func isWordMatch(candidate: String, token: String) -> Bool {
        let word = candidate.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let query = token.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if word.isEmpty || query.isEmpty { return false }

        // 1. Exact match
        if word == query { return true }

        // 2. Plural / singular inflection
        if isPluralOrSingularMatch(word: word, query: query) { return true }

        // 3. Exclude known false prefix collisions
        if let excluded = searchExcludedPrefixMatches[query], excluded.contains(where: { word.hasPrefix($0) }) {
            return false
        }

        // 4. Prefix match for typing / compound words
        if query.count >= 3 && word.hasPrefix(query) {
            if query.count >= 4 { return true }
            if word.count >= 6 { return true }
        }

        // 5. Compound word ending match (e.g. applepie or potpie matching pie)
        if query.count >= 3 && word.hasSuffix(query) && word.count >= query.count + 3 {
            return true
        }

        // 6. Typo resilience via Levenshtein distance 1
        if query.count >= 5 && abs(word.count - query.count) <= 1 {
            return levenshteinDistance(query, word) <= 1
        }

        return false
    }

    private static func isPluralOrSingularMatch(word: String, query: String) -> Bool {
        if word == query + "s" || query == word + "s" { return true }
        if word == query + "es" || query == word + "es" { return true }
        if query.hasSuffix("y") && word == String(query.dropLast()) + "ies" { return true }
        if word.hasSuffix("y") && query == String(word.dropLast()) + "ies" { return true }
        if word == query + "n" || query == word + "n" { return true }
        if word == query + "en" || query == word + "en" { return true }
        if word == query + "e" || query == word + "e" { return true }
        return false
    }

    private static func levenshteinDistance(_ s1: String, _ s2: String) -> Int {
        let a = Array(s1)
        let b = Array(s2)
        var dp = Array(repeating: Array(repeating: 0, count: b.count + 1), count: a.count + 1)
        for i in 0...a.count { dp[i][0] = i }
        for j in 0...b.count { dp[0][j] = j }
        for i in 1...a.count {
            for j in 1...b.count {
                let cost = a[i - 1] == b[j - 1] ? 0 : 1
                dp[i][j] = min(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.count][b.count]
    }
}
