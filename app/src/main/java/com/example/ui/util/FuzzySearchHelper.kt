package com.example.ui.util

import com.example.data.local.RecipeEntity

object FuzzySearchHelper {

    /**
     * Checks if the recipe matches the search query.
     */
    fun matches(recipe: RecipeEntity, rawQuery: String): Boolean {
        return score(recipe, rawQuery) > 0
    }

    // Excluded false-positive prefix collisions where a short culinary word happens to be
    // an orthographic prefix of completely unrelated words (e.g. "pie" matching "piece" or "pierce").
    private val EXCLUDED_PREFIX_MATCHES = mapOf(
        "pie" to setOf(
            "piece", "pieces", "piecemeal", "piecework", "pierce", "pierced", "pierces", "piercing",
            "pierogi", "pierogis", "pierogie", "pierogies", "pier", "piers", "piety", "pietism",
            "pipe", "pipes", "piping", "pipeline"
        ),
        "pea" to setOf("pear", "pears", "pearl", "pearls", "peace", "peasant"),
        "gin" to setOf("ginger", "gingersnap", "gingersnaps", "gingery"),
        "bar" to setOf("bare", "bark", "barks", "barley"),
        "cur" to setOf("curry", "curries", "current", "currents", "currant", "currants"),
        "fig" to setOf("fight", "fights", "figure", "figures"),
        "can" to setOf("candle", "candles", "candy", "candies", "candied", "cantaloupe", "cantaloupes")
    )

    /**
     * Calculates relevance score for ranking recipes against the search query.
     * Higher score = stronger match. 0 = no match.
     */
    fun score(recipe: RecipeEntity, rawQuery: String): Int {
        if (rawQuery.isBlank()) return 1
        val query = rawQuery.trim().lowercase()
        val queryTokens = query.split("[^\\p{L}\\p{Nd}]+".toRegex()).filter { it.isNotBlank() }
        if (queryTokens.isEmpty()) return 1

        val titleCombined = listOfNotNull(
            recipe.title,
            recipe.titleEnglish.takeIf { it.isNotBlank() },
            recipe.titleGerman.takeIf { it.isNotBlank() }
        ).joinToString(" ").lowercase()

        val categoryCombined = recipe.category.lowercase()

        val ingredientsCombined = recipe.ingredients.mapNotNull { ing ->
            listOfNotNull(ing.name, ing.nameEnglish, ing.nameGerman).joinToString(" ").takeIf { it.isNotBlank() }
        }.joinToString(" ").lowercase()

        val notesAndStepsCombined = (listOf(recipe.notes, recipe.notesGerman, recipe.originStory) +
                recipe.steps.flatMap { listOfNotNull(it.instructionEnglish, it.instructionGerman, it.tip) })
            .joinToString(" ").lowercase()

        // 1. Exact full query phrase in title (e.g. "chocolate cake" or "apple pie") -> 10,000 pts
        if (matchesPhrase(titleCombined, query) || (queryTokens.size == 1 && matchesAnyWordInText(titleCombined, queryTokens[0]))) {
            return 10000 + (100 - titleCombined.length).coerceAtLeast(0)
        }

        // 2. All query words in title (e.g. "chocolate" and "cake" both in title) -> 5,000 pts
        val allTokensInTitle = queryTokens.all { token ->
            matchesAnyWordInText(titleCombined, token)
        }
        if (allTokensInTitle) {
            return 5000 + (100 - titleCombined.length).coerceAtLeast(0)
        }

        // 3. Exact full query in category (e.g. "Baking & Desserts") -> 3,000 pts
        if (matchesPhrase(categoryCombined, query) || (queryTokens.size == 1 && matchesAnyWordInText(categoryCombined, queryTokens[0]))) {
            return 3000
        }

        // 4. Exact full query phrase or token in ingredient names (e.g. "cocoa powder", "cashew") -> 2,000 pts
        if (matchesPhrase(ingredientsCombined, query) || (queryTokens.size == 1 && matchesAnyWordInText(ingredientsCombined, queryTokens[0]))) {
            return 2000
        }

        // 5. Query tokens distributed across Title + Category + Ingredients
        // For a multi-word query (e.g. "chocolate cake"), ALL tokens MUST match meaningful recipe core fields!
        val allTokensInCore = queryTokens.all { token ->
            matchesAnyWordInText(titleCombined, token) ||
                    matchesAnyWordInText(categoryCombined, token) ||
                    matchesAnyWordInText(ingredientsCombined, token)
        }

        if (allTokensInCore) {
            var tokenScore = 500
            if (queryTokens.any { matchesAnyWordInText(titleCombined, it) }) tokenScore += 400
            if (queryTokens.any { matchesAnyWordInText(ingredientsCombined, it) }) tokenScore += 200
            return tokenScore
        }

        // 6. Single-token query fallback for notes / instructions (only for true word matches)
        if (queryTokens.size == 1 && matchesAnyWordInText(notesAndStepsCombined, queryTokens[0])) {
            return 100
        }

        return 0
    }

    /**
     * Checks if [text] contains the multi-word [phrase] respecting word boundaries.
     */
    fun matchesPhrase(text: String, phrase: String): Boolean {
        if (phrase.isBlank() || text.isBlank()) return false
        val tokens = phrase.split("[^\\p{L}\\p{Nd}]+".toRegex()).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false
        if (tokens.size == 1) return matchesAnyWordInText(text, tokens[0])

        val escapedTokens = tokens.map { Regex.escape(it) }
        val regexStr = "(?:^|[^\\p{L}\\p{Nd}])" + escapedTokens.joinToString("[^\\p{L}\\p{Nd}]+") + "(?:$|[^\\p{L}\\p{Nd}])"
        return Regex(regexStr, RegexOption.IGNORE_CASE).containsMatchIn(text)
    }

    /**
     * Checks if any individual word in [text] matches [token] (exact, inflection/plural, prefix, or typo).
     */
    fun matchesAnyWordInText(text: String, token: String): Boolean {
        if (text.isBlank() || token.isBlank()) return false
        val words = text.split("[^\\p{L}\\p{Nd}]+".toRegex()).filter { it.isNotBlank() }
        return words.any { isWordMatch(it, token) }
    }

    /**
     * Checks if an individual word matches the pattern.
     */
    fun isWordMatch(candidateWord: String, token: String): Boolean {
        val word = candidateWord.trim().lowercase()
        val query = token.trim().lowercase()
        if (word.isEmpty() || query.isEmpty()) return false

        // 1. Exact match
        if (word == query) return true

        // 2. Direct English & German plural / singular inflection
        if (isPluralOrSingularMatch(word, query)) return true

        // 3. Exclude known false prefix collisions (e.g. "pie" matching "piece" or "pierce")
        val excludedWords = EXCLUDED_PREFIX_MATCHES[query]
        if (excludedWords != null && excludedWords.any { word.startsWith(it) }) {
            return false
        }

        // 4. Prefix match for typing / compound words
        if (query.length >= 3 && word.startsWith(query)) {
            // For >= 4 chars, standard incremental prefix typing (e.g. choc -> chocolate, chick -> chicken)
            if (query.length >= 4) return true
            // For 3-letter queries, match legitimate compound words (e.g. piecrust, piepan, icecream)
            if (word.length >= 6) return true
        }

        // 5. Compound word ending match (e.g. "applepie" or "potpie" matching "pie", "rotkohl" matching "kohl")
        if (query.length >= 3 && word.endsWith(query) && word.length >= query.length + 3) {
            return true
        }

        // 6. Typo resilience via Levenshtein distance 1 (only for words with length >= 5)
        if (query.length >= 5 && Math.abs(word.length - query.length) <= 1) {
            return levenshteinDistance(query, word) <= 1
        }

        return false
    }

    private fun isPluralOrSingularMatch(word: String, query: String): Boolean {
        // Standard English -s / -es
        if (word == query + "s" || query == word + "s") return true
        if (word == query + "es" || query == word + "es") return true

        // -y to -ies (e.g. berry -> berries, cherry -> cherries)
        if (query.endsWith("y") && word == query.dropLast(1) + "ies") return true
        if (word.endsWith("y") && query == word.dropLast(1) + "ies") return true

        // German plurals (-n, -en, -e)
        if (word == query + "n" || query == word + "n") return true
        if (word == query + "en" || query == word + "en") return true
        if (word == query + "e" || query == word + "e") return true

        return false
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
