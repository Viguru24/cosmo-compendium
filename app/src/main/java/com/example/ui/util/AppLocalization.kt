package com.example.ui.util

import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode

object AppLocalization {

    fun getAppTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = "Heirloom Recipe Book"

    fun getAppSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = "Cherished Family Heritage & Vintage Recipes"

    fun getSearchPlaceholder(lang: LanguageMode = LanguageMode.ENGLISH): String = "Search recipes, ingredients (apple, flour, roast)..."

    fun getScanButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "Scan Recipe"

    fun getNewRecipeButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "New Recipe"

    fun getCategoryLabel(category: String, lang: LanguageMode = LanguageMode.ENGLISH): String = when (category) {
        "All", "Alle", "All Recipes" -> "All Recipes"
        "Baking & Desserts", "Backen & Desserts" -> "Baking & Desserts"
        "Main Dishes", "Hauptgerichte" -> "Main Dishes"
        "Soups & Stews", "Suppen & Eintöpfe" -> "Soups & Stews"
        "Breakfast", "Frühstück" -> "Breakfast"
        "Family Classics", "Familien-Klassiker" -> "Family Classics"
        "Side Dishes", "Beilagen" -> "Side Dishes"
        else -> category
    }

    fun getEmptyStateTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = "No Heirloom Recipes Found"

    fun getEmptyStateMessage(lang: LanguageMode = LanguageMode.ENGLISH): String = "Try searching for a different ingredient or scan in one of your vintage recipe cards!"

    fun getFavoritesLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "⭐ Favorites"

    // Booklet Page Tabs
    fun getTabCover(lang: LanguageMode = LanguageMode.ENGLISH): String = "Cover"

    fun getTabIndex(lang: LanguageMode = LanguageMode.ENGLISH): String = "Index"

    fun getTabIngredients(lang: LanguageMode = LanguageMode.ENGLISH): String = "Ingredients"

    fun getTabSteps(lang: LanguageMode = LanguageMode.ENGLISH): String = "Steps"

    fun getTabJournal(lang: LanguageMode = LanguageMode.ENGLISH): String = "Journal"

    // Cook Mode
    fun getCookModeTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = "Kitchen Cooking Mode"

    fun getCookModeStepHeader(current: Int, total: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = "Step $current of $total"

    fun getIngredientsChecklistHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Ingredients Checklist"

    fun getOmaSecretTipHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Grandma's Secret Tip"

    fun getMarkDoneLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "Mark Step as Done"

    fun getStepCompletedLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "Completed ✓"

    fun getPreviousStepLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "Previous Step"

    fun getNextStepLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "Next Step"

    fun getFinishCookingLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "🎉 Finish Cooking & Record in Journal"

    // Booklet Page 1 (Lore & TOC)
    fun getTableOfContentsHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Table of Contents"

    fun getFamilyLoreHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Family Heritage & Origin"

    fun getPrepInfoLabels(prep: Int, cook: Int, servings: String, diff: String, lang: LanguageMode = LanguageMode.ENGLISH): List<Pair<String, String>> = listOf(
        "Prep Time" to "${prep}m",
        "Cook Time" to "${cook}m",
        "Yield" to servings,
        "Difficulty" to diff
    )

    // Booklet Page 2 (Ingredients & Scaler)
    fun getIngredientsPageHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Ingredients & Portions"

    fun getServingScalerHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Portion Scaler"

    fun getMeasuringStyleLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = "Measuring Style"

    fun getGlossaryHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Culinary Glossary & Substitutes"

    // Booklet Page 3 (Steps)
    fun getStepPageHeader(stepNum: Int, totalSteps: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = "Step $stepNum of $totalSteps"

    fun getStepTimerButton(minutes: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = "Start ${minutes}m Timer"

    fun getListenVoiceButton(lang: LanguageMode = LanguageMode.ENGLISH): String = "Listen (Voice)"

    // Booklet Page 4 (Journal)
    fun getJournalHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Cook's Journal & Family Memories"

    fun getTimesCookedHeader(times: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = "Cooked $times times at home"

    fun getMarkCookedTodayButton(lang: LanguageMode = LanguageMode.ENGLISH): String = "+1 Cooked Today!"

    fun getFamilyRatingHeader(lang: LanguageMode = LanguageMode.ENGLISH): String = "Family Recipe Rating"

    fun getFamilyNotesPlaceholder(lang: LanguageMode = LanguageMode.ENGLISH): String = "Write your family adjustments, oven tweaks, or special memories here..."

    fun getSaveNotesButton(lang: LanguageMode = LanguageMode.ENGLISH): String = "Save Journal Notes"

    fun getVintageCardButton(lang: LanguageMode = LanguageMode.ENGLISH): String = "Vintage Handwritten Card"

    fun getStartCookingButton(lang: LanguageMode = LanguageMode.ENGLISH): String = "Start Cooking Mode"

    fun getSettingsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = "App & Recipe Settings"
}

fun RecipeEntity.getDisplayTitle(lang: LanguageMode = LanguageMode.ENGLISH): String {
    return if (titleEnglish.isNotBlank()) titleEnglish else title
}

fun RecipeEntity.getDisplayCategory(lang: LanguageMode = LanguageMode.ENGLISH): String {
    return AppLocalization.getCategoryLabel(category, lang)
}

fun RecipeEntity.getDisplayOriginStory(lang: LanguageMode = LanguageMode.ENGLISH): String {
    return originStory
}

fun RecipeEntity.getDisplayNotes(lang: LanguageMode = LanguageMode.ENGLISH): String {
    return if (notes.isNotBlank()) notes else notesGerman
}
