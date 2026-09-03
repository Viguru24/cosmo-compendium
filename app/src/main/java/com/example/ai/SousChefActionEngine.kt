package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

sealed class SousChefIntent {
    data class ScanCamera(val targetProfile: String?) : SousChefIntent()
    data class ImportUrl(val url: String, val targetProfile: String?) : SousChefIntent()
    data class SwitchProfile(val targetProfile: String) : SousChefIntent()
    data class SearchRecipes(val query: String) : SousChefIntent()
    data class MoveRecipes(val sourceQuery: String?, val targetProfile: String, val isAll: Boolean) : SousChefIntent()
    data class KitchenAdvice(val query: String) : SousChefIntent()
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val intent: SousChefIntent? = null,
    val matchingRecipes: List<com.example.data.local.RecipeEntity> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

object SousChefActionEngine {

    /**
     * Parses the user's prompt (text or voice) and resolves the intent
     */
    fun parseIntent(prompt: String, currentActiveProfile: String, knownProfiles: List<String>): SousChefIntent {
        val lower = prompt.lowercase().trim()

        // 1. Detect URL inside prompt
        val urlMatcher = Pattern.compile("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]+").matcher(prompt)
        if (urlMatcher.find()) {
            val url = urlMatcher.group(0) ?: ""
            val targetProfile = extractMentionedProfile(lower, knownProfiles) ?: currentActiveProfile
            return SousChefIntent.ImportUrl(url, targetProfile)
        }

        // 2. Camera / Scanning Intent
        if (lower.contains("scan") || lower.contains("camera") || lower.contains("picture") || 
            lower.contains("photo") || lower.contains("snap") || lower.contains("card") ||
            lower.contains("scannen") || lower.contains("scanner")) {
            val targetProfile = extractMentionedProfile(lower, knownProfiles)
            return SousChefIntent.ScanCamera(targetProfile)
        }

        // 3. Switch Profile Intent
        if (lower.startsWith("switch to") || lower.startsWith("change to") || lower.contains("open cookbook") || lower.contains("profile") || lower.contains("kochbuch wechseln")) {
            for (p in knownProfiles) {
                if (lower.contains(p.lowercase())) {
                    return SousChefIntent.SwitchProfile(p)
                }
            }
            if (lower.contains("all") || lower.contains("family") || lower.contains("alle")) {
                return SousChefIntent.SwitchProfile("All Family")
            }
        }

        // 4. Move / Reassign Recipes Intent
        if (lower.contains("move") || lower.contains("transfer") || lower.contains("assign") || lower.contains("reassign") || lower.contains("put into") || lower.contains("put in") || lower.contains("verschieben")) {
            val isAll = lower.contains("all") || lower.contains("everything") || lower.contains("whole") || lower.contains("alle")
            val targetProfile = extractTargetProfileFromMove(lower, knownProfiles) ?: "Wife"

            val specificRecipe = if (!isAll) {
                lower
                    .replace("move", "")
                    .replace("transfer", "")
                    .replace("assign", "")
                    .replace("reassign", "")
                    .replace("put into", "")
                    .replace("put in", "")
                    .replace("verschieben", "")
                    .replace("to $targetProfile", "")
                    .replace("to ${targetProfile.lowercase()}", "")
                    .replace("to wife's cookbook", "")
                    .replace("to wife", "")
                    .replace("to louis", "")
                    .replace("to daughter", "")
                    .replace("cookbook", "")
                    .replace("recipe", "")
                    .replace("recipes", "")
                    .replace("the", "")
                    .trim()
            } else null

            return SousChefIntent.MoveRecipes(
                sourceQuery = if (specificRecipe.isNullOrBlank()) null else specificRecipe,
                targetProfile = targetProfile,
                isAll = isAll || specificRecipe.isNullOrBlank()
            )
        }

        // 5. Search Recipes Intent (Explicit keywords or recipe search phrases)
        val searchPrefixes = listOf(
            "bring up my recipes for", "bring up recipes for", "bring up my recipe for", "bring up recipe for", "bring up my", "bring up",
            "pull up my recipes for", "pull up recipes for", "pull up my recipe for", "pull up recipe for", "pull up my", "pull up",
            "get my recipes for", "get recipes for", "get my recipe for", "get recipe for", "get my", "get me", "get",
            "open my recipes for", "open recipes for", "open my recipe for", "open recipe for", "open my", "open recipe", "open",
            "show me my recipes for", "show me recipes for", "show me my recipe for", "show me recipes with", "show me recipe for", "show me my", "show me", "show my", "show",
            "give me my recipes for", "give me recipes for", "give me my recipe for", "give me recipe for", "give me",
            "find me my recipes for", "find me recipes for", "find my recipes for", "find recipes for", "find my recipe for", "find recipe for", "find me", "find my", "find",
            "search for my recipes for", "search for recipes for", "search for recipe for", "search for my", "search for", "search",
            "look up my recipes for", "look up recipes for", "look up recipe for", "look up", "look for",
            "display my recipes for", "display recipes for", "display",
            "list my recipes for", "list recipes for", "list my", "list",
            "what recipes do i have for", "what recipes do we have for", "what recipe do i have for", "what recipes have we got for", "what recipes do we have", "what recipes",
            "do we have any recipes for", "do we have recipes for", "do we have a recipe for", "do we have any", "do we have", "have we got",
            "where is my recipe for", "where is the recipe for", "where is", "where are",
            "suche nach", "suche", "finde", "zeig mir", "zeig", "bring mir", "hol mir", "öffne", "wo ist", "haben wir"
        )

        var detectedSearchQuery: String? = null

        for (prefix in searchPrefixes) {
            if (lower.startsWith(prefix)) {
                var query = lower.removePrefix(prefix).trim()
                query = query
                    .replace(Regex("^my\\s+"), "")
                    .replace(Regex("^our\\s+"), "")
                    .replace(Regex("^the\\s+"), "")
                    .replace(Regex("^a\\s+"), "")
                    .replace(Regex("^an\\s+"), "")
                    .replace(Regex("\\b(recipes|recipe|rezepte|rezept)\\b"), "")
                    .replace(Regex("\\b(for|with|of|für|mit)\\b"), "")
                    .trim()
                if (query.isNotBlank()) {
                    detectedSearchQuery = query
                    break
                }
            }
        }

        if (detectedSearchQuery == null) {
            if (lower.endsWith(" recipes") || lower.endsWith(" recipe") || lower.endsWith(" rezepte") || lower.endsWith(" rezept")) {
                var query = lower
                    .replace(Regex("\\b(recipes|recipe|rezepte|rezept)\\b"), "")
                    .replace(Regex("^my\\s+"), "")
                    .replace(Regex("^our\\s+"), "")
                    .replace(Regex("^the\\s+"), "")
                    .trim()
                if (query.isNotBlank()) {
                    detectedSearchQuery = query
                }
            }
        }

        if (detectedSearchQuery != null && detectedSearchQuery.isNotBlank()) {
            return SousChefIntent.SearchRecipes(detectedSearchQuery)
        }

        // 6. Check if query is asking for culinary substitution/technique vs recipe advice
        return SousChefIntent.KitchenAdvice(prompt)
    }

    private fun extractTargetProfileFromMove(text: String, knownProfiles: List<String>): String? {
        val lower = text.lowercase()
        for (p in knownProfiles) {
            if (lower.contains(p.lowercase())) return p
        }
        if (lower.contains("wife") || lower.contains("annette") || lower.contains("mom") || lower.contains("mother") || lower.contains("her")) {
            return knownProfiles.find { it.contains("annette", ignoreCase = true) || it.contains("wife", ignoreCase = true) || !it.equals("Louis", ignoreCase = true) } ?: "Annette"
        }
        if (lower.contains("daughter") || lower.contains("girl")) {
            return knownProfiles.find { it.contains("daughter", ignoreCase = true) } ?: "Daughter"
        }
        if (lower.contains("louis") || lower.contains("dad") || lower.contains("husband") || lower.contains("mine") || lower.contains("my cookbook")) {
            return knownProfiles.find { it.contains("louis", ignoreCase = true) } ?: "Louis"
        }
        return knownProfiles.firstOrNull { !it.equals("Louis", ignoreCase = true) } ?: knownProfiles.firstOrNull() ?: "Annette"
    }

    private fun extractMentionedProfile(text: String, knownProfiles: List<String>): String? {
        val lower = text.lowercase()
        for (p in knownProfiles) {
            if (lower.contains(p.lowercase())) return p
        }
        if (lower.contains("wife") || lower.contains("annette")) {
            return knownProfiles.find { it.contains("annette", ignoreCase = true) || it.contains("wife", ignoreCase = true) || !it.equals("Louis", ignoreCase = true) } ?: "Annette"
        }
        if (lower.contains("daughter")) {
            return knownProfiles.find { it.contains("daughter", ignoreCase = true) } ?: "Daughter"
        }
        if (lower.contains("louis") || lower.contains("dad") || lower.contains("me") || lower.contains("my cookbook")) {
            return knownProfiles.find { it.contains("louis", ignoreCase = true) } ?: "Louis"
        }
        return null
    }

    /**
     * Generates a conversational response for kitchen advice & questions via Gemini
     */
    suspend fun getKitchenAdvice(question: String): String = withContext(Dispatchers.IO) {
        try {
            val liveAnswer = GeminiClient.askSousChefAssistant(question)
            if (liveAnswer.isNotBlank()) {
                return@withContext liveAnswer
            }
        } catch (e: Exception) {
            // Fall back to offline advice
        }
        return@withContext getOfflineKitchenAdvice(question)
    }

    private fun getOfflineKitchenAdvice(question: String): String {
        val q = question.lowercase().trim()
        return when {
            // Milk Substitutions
            q.contains("milk") && (q.contains("instead") || q.contains("substitute") || q.contains("replace") || q.contains("alternative") || q.contains("without")) ->
                "🥛 Milk Substitutions (for 1 cup):\n• Plant-based: 1 cup Oat milk, Almond milk, or Soy milk (1:1 ratio).\n• Dairy alternatives: 1/2 cup Evaporated Milk + 1/2 cup Water, or 1/2 cup Plain Yogurt/Sour Cream + 1/2 cup Water.\n• In baking: 1 cup Water + 1.5 tablespoons melted Butter."

            // Buttermilk Substitutions
            q.contains("buttermilk") ->
                "🥣 Buttermilk Substitute (for 1 cup):\nAdd 1 tablespoon fresh Lemon Juice or White Vinegar to 1 cup of Milk. Stir and let sit for 5 minutes until slightly curdled and thick."

            // Heavy Cream Substitutions
            q.contains("cream") && (q.contains("heavy") || q.contains("whipping") || q.contains("double")) ->
                "🍶 Heavy Cream Substitute (for 1 cup):\n• For cooking/sauces: 3/4 cup Whole Milk + 1/4 cup melted Butter.\n• Dairy-free: 1 cup full-fat Coconut Cream.\n• For baking: 1 cup Evaporated Milk."

            // Egg Substitutions
            q.contains("egg") && (q.contains("instead") || q.contains("substitute") || q.contains("replace") || q.contains("without") || q.contains("alternative")) ->
                "🥚 Egg Substitutes (for 1 large egg in baking):\n• 1/4 cup unsweetened Applesauce (moist cakes & muffins)\n• 1/4 cup Plain Greek Yogurt or Sour Cream\n• 1/2 mashed ripe Banana (adds subtle sweetness)\n• 1 tablespoon ground Flaxseed + 3 tablespoons warm water (let sit 5 min to thicken)."

            // Butter Substitutions
            q.contains("butter") && (q.contains("instead") || q.contains("substitute") || q.contains("replace") || q.contains("alternative")) ->
                "🧈 Butter Substitutes (for 1 cup):\n• In baking: 1 cup Coconut Oil (solid), or 3/4 cup neutral Vegetable/Canola Oil.\n• For richness: 1 cup Plain Greek Yogurt (reduces fat while keeping moisture).\n• On savory dishes: Extra Virgin Olive Oil (3/4 cup per 1 cup butter)."

            // Sugar Substitutions
            (q.contains("sugar") || q.contains("sweetener")) && (q.contains("instead") || q.contains("substitute") || q.contains("replace") || q.contains("alternative")) ->
                "🍯 Sugar Substitutes (for 1 cup granulated sugar):\n• Honey or Pure Maple Syrup: Use 3/4 cup and reduce recipe liquids by 3-4 tablespoons.\n• Brown Sugar: 1 cup Brown Sugar (adds moisture and caramel depth).\n• Coconut Sugar: 1:1 replacement."

            // Flour Substitutions
            q.contains("flour") && (q.contains("instead") || q.contains("substitute") || q.contains("replace") || q.contains("gluten")) ->
                "🌾 Flour Substitutes:\n• Gluten-free: 1:1 1-to-1 Gluten-Free Baking Blend (with xanthan gum).\n• Almond Flour: Works best in dense cakes/cookies (use 1:1, may require +1 egg for structure).\n• Oat Flour: Grind rolled oats in a blender (use 1:1 by weight)."

            // Baking Powder & Soda
            q.contains("baking powder") && (q.contains("substitute") || q.contains("replace") || q.contains("instead") || q.contains("make")) ->
                "🥄 Baking Powder Substitute (for 1 teaspoon):\nMix 1/4 teaspoon Baking Soda + 1/2 teaspoon Cream of Tartar."

            q.contains("baking soda") && (q.contains("substitute") || q.contains("replace") || q.contains("instead")) ->
                "🥄 Baking Soda Substitute (for 1 teaspoon):\nUse 3 teaspoons (1 tablespoon) of Baking Powder, but reduce the salt in the recipe slightly."

            // Cornstarch Substitutions
            q.contains("cornstarch") || q.contains("corn starch") ->
                "🌽 Cornstarch Thickener Substitute (for 1 tablespoon):\nUse 2 tablespoons All-Purpose Flour, or 1 tablespoon Arrowroot Powder, or 1 tablespoon Tapioca Starch."

            // Sour Cream Substitutions
            q.contains("sour cream") || q.contains("schmand") || q.contains("creme fraiche") ->
                "🥛 Sour Cream Substitute (1:1):\nUse Plain Whole-Milk Greek Yogurt (nearly identical texture and acidity), or 1 cup heavy cream + 1 tbsp lemon juice."

            // Yeast Substitutions
            q.contains("yeast") && (q.contains("instead") || q.contains("substitute") || q.contains("without")) ->
                "🍞 Yeast Substitute (for quick breads/pizza dough):\nUse equal parts Baking Soda and Lemon Juice/Vinegar (e.g., 1 tsp baking soda + 1 tsp lemon juice for 1 packet active dry yeast)."

            // Honey & Volume Weights
            q.contains("honey") && (q.contains("gram") || q.contains("cup")) ->
                "🍯 1 cup of pure honey weighs approximately 340 grams (or 1 tbsp = 21g), as honey is 40% denser than water."
            q.contains("butter") && (q.contains("gram") || q.contains("cup")) ->
                "🧈 1 cup of unsalted butter is 227 grams (2 sticks / 16 tablespoons / 8 oz)."
            q.contains("flour") && (q.contains("gram") || q.contains("cup")) ->
                "🌾 1 cup of all-purpose flour equals approximately 120-125 grams (spooned and leveled)."
            q.contains("sugar") && (q.contains("gram") || q.contains("cup")) ->
                "🍚 1 cup of granulated white sugar is 200 grams, while 1 cup of powdered sugar is 120 grams."

            // General Friendly Advice
            else ->
                "I'm your kitchen Sous-Chef! Ask me any culinary substitution (e.g. 'what can I use instead of milk/eggs/butter'), baking conversions, cooking temperatures, or tell me to 'Scan recipe cards for Annette'!"
        }
    }
}
