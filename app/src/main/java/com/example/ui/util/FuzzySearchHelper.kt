package com.example.ui.util

import com.example.data.local.RecipeEntity

object FuzzySearchHelper {

    /**
     * Checks if the recipe matches the search query.
     */
    fun matches(recipe: RecipeEntity, rawQuery: String): Boolean {
        return score(recipe, rawQuery) > 0
    }

    /**
     * Calculates relevance score for ranking recipes against the search query.
     * Higher score = stronger match. 0 = no match.
     */
    fun score(recipe: RecipeEntity, rawQuery: String): Int {
        if (rawQuery.isBlank()) return 1
        val query = rawQuery.trim().lowercase()
        val queryTokens = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
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

        // 1. Exact full query in title (e.g. "chocolate cake" in "grandma's chocolate cake") -> 10,000 pts
        if (titleCombined.contains(query)) {
            return 10000 + (100 - titleCombined.length).coerceAtLeast(0)
        }

        // 2. All query words in title (e.g. "chocolate" and "cake" both in title) -> 5,000 pts
        val allTokensInTitle = queryTokens.all { token ->
            titleCombined.contains(token) || isWordMatch(titleCombined, token)
        }
        if (allTokensInTitle) {
            return 5000 + (100 - titleCombined.length).coerceAtLeast(0)
        }

        // 3. Exact full query in category (e.g. "Baking & Desserts") -> 3,000 pts
        if (categoryCombined.contains(query)) {
            return 3000
        }

        // 4. Exact full query in ingredient names (e.g. "cocoa powder") -> 2,000 pts
        if (ingredientsCombined.contains(query)) {
            return 2000
        }

        // 5. Query tokens distributed across Title + Category + Ingredients
        // For a multi-word query (e.g. "chocolate cake"), ALL tokens MUST match meaningful recipe core fields!
        val allTokensInCore = queryTokens.all { token ->
            titleCombined.contains(token) ||
                    categoryCombined.contains(token) ||
                    ingredientsCombined.contains(token) ||
                    isWordMatch(titleCombined, token) ||
                    isWordMatch(ingredientsCombined, token)
        }

        if (allTokensInCore) {
            var tokenScore = 500
            if (queryTokens.any { titleCombined.contains(it) }) tokenScore += 400
            if (queryTokens.any { ingredientsCombined.contains(it) }) tokenScore += 200
            return tokenScore
        }

        // 6. Single-token query fallback for notes / instructions
        if (queryTokens.size == 1 && notesAndStepsCombined.contains(query)) {
            return 100
        }

        return 0
    }

    /**
     * Checks if any word in the text matches the pattern (exact prefix or single typo for words >= 5 chars).
     * Strictly avoids subsequence letter-skipping like "package" matching "cake".
     */
    private fun isWordMatch(text: String, pattern: String): Boolean {
        if (text.contains(pattern)) return true
        val words = text.split("[^\\p{L}\\p{Nd}]+".toRegex()).filter { it.length >= 2 }

        return words.any { word ->
            if (word == pattern) return@any true
            if (word.startsWith(pattern) && pattern.length >= 3) return@any true
            // Levenshtein distance 1 only for typo resilience on words with length >= 5
            if (pattern.length >= 5 && Math.abs(word.length - pattern.length) <= 1) {
                return@any levenshteinDistance(pattern, word) <= 1
            }
            false
        }
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

