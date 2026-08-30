package com.example.ui.util

import com.example.data.model.RecipeIngredient
import com.example.data.model.UnitSystem

/**
 * Matched ingredient item with its converted amount string for display in step instructions.
 */
data class MatchedStepIngredient(
    val ingredient: RecipeIngredient,
    val matchedName: String,
    val displayAmount: String
)

/**
 * Intelligent helper that extracts and matches ingredient references from step instructions.
 * This works dynamically at runtime for BOTH newly scanned and all existing 100+ saved recipes
 * without requiring database migrations or re-scanning!
 */
object StepIngredientMatcher {

    // Common non-ingredient stop words to avoid false positive substring matches
    private val STOP_WORDS = setOf(
        "in", "at", "to", "for", "with", "from", "by", "on", "off", "into", "onto",
        "and", "or", "a", "an", "the", "all", "each", "every", "both", "few", "more",
        "top", "bottom", "side", "pan", "pot", "bowl", "dish", "oven", "heat", "cook",
        "bake", "stir", "mix", "whisk", "fold", "pour", "add", "place", "set", "let",
        "until", "after", "before", "while", "when", "then", "now", "well", "gently",
        "thoroughly", "together", "aside", "ready", "done", "warm", "hot", "cold",
        "medium", "high", "low", "minutes", "min", "hours", "hr", "degrees", "c", "f"
    )

    /**
     * Finds all ingredients mentioned in the given step instruction text.
     * Evaluates against ingredient name, English name, and German name.
     */
    fun findIngredientsInStep(
        stepInstruction: String,
        ingredients: List<RecipeIngredient>,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        multiplier: Float = 1.0f
    ): List<MatchedStepIngredient> {
        if (stepInstruction.isBlank() || ingredients.isEmpty()) return emptyList()

        val lowerInstruction = stepInstruction.lowercase()
        val matchedList = mutableListOf<MatchedStepIngredient>()
        val seenIngredients = mutableSetOf<String>()

        for (ingredient in ingredients) {
            val convertedAmount = ingredient.getConvertedAmount(unitSystem, multiplier)
            if (convertedAmount.isBlank()) continue

            // Extract possible search keywords for this ingredient
            val candidateNames = getCandidateKeywords(ingredient)

            for (keyword in candidateNames) {
                if (keyword.length < 3) continue
                if (STOP_WORDS.contains(keyword)) continue

                // Check if keyword is found as a whole word or significant part in the step text
                val regex = Regex("\\b${Regex.escape(keyword)}[s|es|n|en]?\\b", RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn(lowerInstruction)) {
                    val key = ingredient.name.lowercase().trim()
                    if (!seenIngredients.contains(key)) {
                        seenIngredients.add(key)
                        val cleanDisplayName = RecipeIngredient.cleanIngredientName(ingredient.getDisplayName())
                        matchedList.add(
                            MatchedStepIngredient(
                                ingredient = ingredient,
                                matchedName = cleanDisplayName,
                                displayAmount = convertedAmount
                            )
                        )
                        break
                    }
                }
            }
        }

        return matchedList
    }

    /**
     * Extracts search keywords from ingredient names (splitting compound descriptions, removing parentheses).
     */
    private fun getCandidateKeywords(ingredient: RecipeIngredient): List<String> {
        val keywords = mutableListOf<String>()

        val rawNames = listOfNotNull(
            ingredient.name,
            ingredient.nameEnglish,
            ingredient.nameGerman
        )

        for (raw in rawNames) {
            // Remove parenthetical notes e.g., "flour (all-purpose)" -> "flour"
            val cleaned = raw.replace(Regex("\\(.*?\\)"), "").trim().lowercase()
            
            // Add full cleaned name
            if (cleaned.isNotBlank()) {
                keywords.add(cleaned)
            }

            // Split slashes e.g. "Flour / Mehl"
            if (cleaned.contains("/")) {
                cleaned.split("/").forEach { part ->
                    val p = part.trim()
                    if (p.isNotBlank()) keywords.add(p)
                }
            }

            // Split commas e.g. "large eggs, beaten" -> "large eggs", "eggs"
            if (cleaned.contains(",")) {
                cleaned.split(",").firstOrNull()?.trim()?.let { firstPart ->
                    if (firstPart.isNotBlank()) keywords.add(firstPart)
                }
            }

            // Extract core noun if multiple words e.g., "all-purpose flour" -> "flour", "granulated sugar" -> "sugar"
            val words = cleaned.split(Regex("[\\s,-]+")).filter { it.length >= 3 && !STOP_WORDS.contains(it) }
            for (w in words) {
                keywords.add(w)
            }
        }

        return keywords.distinct()
    }
}
