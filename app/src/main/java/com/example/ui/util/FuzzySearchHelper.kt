package com.example.ui.util

import com.example.data.local.RecipeEntity

object FuzzySearchHelper {

    /**
     * Checks if the recipe matches the search query using fuzzy matching.
     */
    fun matches(recipe: RecipeEntity, rawQuery: String): Boolean {
        if (rawQuery.isBlank()) return true
        val query = rawQuery.trim().lowercase()

        // Gather all searchable text tokens from the recipe
        val fields = mutableListOf<String>()
        fields.add(recipe.title)
        if (recipe.titleEnglish.isNotBlank()) fields.add(recipe.titleEnglish)
        if (recipe.titleGerman.isNotBlank()) fields.add(recipe.titleGerman)
        if (recipe.category.isNotBlank()) fields.add(recipe.category)
        if (recipe.notes.isNotBlank()) fields.add(recipe.notes)
        if (recipe.notesGerman.isNotBlank()) fields.add(recipe.notesGerman)
        if (recipe.originStory.isNotBlank()) fields.add(recipe.originStory)

        for (ing in recipe.ingredients) {
            if (ing.name.isNotBlank()) fields.add(ing.name)
            if (!ing.nameEnglish.isNullOrBlank()) fields.add(ing.nameEnglish)
            if (!ing.nameGerman.isNullOrBlank()) fields.add(ing.nameGerman)
            if (!ing.group.isNullOrBlank()) fields.add(ing.group)
        }

        for (step in recipe.steps) {
            if (step.instructionEnglish.isNotBlank()) fields.add(step.instructionEnglish)
            if (step.instructionGerman.isNotBlank()) fields.add(step.instructionGerman)
            if (!step.tip.isNullOrBlank()) fields.add(step.tip)
        }

        val allCombinedText = fields.joinToString(" ").lowercase()

        // 1. Direct contains check
        if (allCombinedText.contains(query)) return true

        // 2. Tokenized multi-word search
        val queryTokens = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (queryTokens.isEmpty()) return true

        val allTokensMatch = queryTokens.all { token ->
            // Check substring in combined text
            if (allCombinedText.contains(token)) return@all true

            // Check fuzzy / subsequence match against individual recipe words
            val recipeWords = allCombinedText.split("[^\\p{L}\\p{Nd}]+".toRegex()).filter { it.length >= 2 }
            recipeWords.any { word ->
                isFuzzyMatch(word, token)
            }
        }

        return allTokensMatch
    }

    private fun isFuzzyMatch(target: String, pattern: String): Boolean {
        if (target == pattern) return true
        if (target.contains(pattern)) return true

        // Subsequence match (e.g. "choc" in "chocolate")
        if (pattern.length >= 3 && isSubsequence(pattern, target)) return true

        // Levenshtein distance for typos
        val maxDist = when {
            pattern.length <= 3 -> 0
            pattern.length <= 5 -> 1
            else -> 2
        }

        if (maxDist > 0 && Math.abs(target.length - pattern.length) <= maxDist) {
            return levenshteinDistance(pattern, target) <= maxDist
        }

        return false
    }

    private fun isSubsequence(sub: String, full: String): Boolean {
        var subIdx = 0
        var fullIdx = 0
        while (subIdx < sub.length && fullIdx < full.length) {
            if (sub[subIdx] == full[fullIdx]) {
                subIdx++
            }
            fullIdx++
        }
        return subIdx == sub.length
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
