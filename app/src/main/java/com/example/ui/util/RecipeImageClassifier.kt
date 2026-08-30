package com.example.ui.util

import com.example.data.local.RecipeEntity
import java.io.File

enum class RecipeImageType {
    NONE,
    SCANNED_CARD,
    AI_GENERATED
}

enum class BatchCoverFilter(val label: String, val shortLabel: String, val description: String) {
    MISSING_AI_PHOTOS(
        "Recipes Missing AI Photography",
        "Missing AI Photos",
        "Includes recipes with no photos and recipes with scanned physical recipe cards"
    ),
    SCANNED_CARDS_ONLY(
        "Only Scanned Physical Recipe Cards",
        "Scanned Cards",
        "Generates AI food photography to replace scanned physical card photos"
    ),
    NO_PHOTO_ONLY(
        "Completely Unphotographed Only",
        "No Photos",
        "Generates photos only for recipes with zero images"
    ),
    ALL_RECIPES(
        "All Recipes (Regenerate Entire Cookbook)",
        "All Recipes",
        "Recreates fresh AI photography for every recipe in your cookbook"
    )
}

data class RecipePhotoStats(
    val total: Int = 0,
    val aiGeneratedCount: Int = 0,
    val scannedCardCount: Int = 0,
    val unphotographedCount: Int = 0,
    val missingAiCount: Int = 0
)

object RecipeImageClassifier {

    fun getImageType(imageUri: String?): RecipeImageType {
        if (imageUri.isNullOrBlank()) return RecipeImageType.NONE

        if (imageUri.startsWith("content://")) {
            return RecipeImageType.SCANNED_CARD
        }

        val file = File(imageUri)
        if (!file.exists()) return RecipeImageType.NONE

        val name = file.name.lowercase()
        val path = imageUri.lowercase()

        // Naming conventions for AI-generated food photography
        if (name.startsWith("recipe_cover_") ||
            name.startsWith("ai_food_") ||
            name.startsWith("ai_dish_") ||
            name.startsWith("comfy_") ||
            name.startsWith("gemini_") ||
            path.contains("recipe_cover_") ||
            path.contains("ai_food_") ||
            path.contains("ai_dish_") ||
            path.contains("comfy_output")) {
            return RecipeImageType.AI_GENERATED
        }

        return RecipeImageType.SCANNED_CARD
    }

    fun isAiGenerated(imageUri: String?): Boolean =
        getImageType(imageUri) == RecipeImageType.AI_GENERATED

    fun isScannedCard(imageUri: String?): Boolean =
        getImageType(imageUri) == RecipeImageType.SCANNED_CARD

    fun hasNoPhoto(imageUri: String?): Boolean =
        getImageType(imageUri) == RecipeImageType.NONE

    fun isMissingAiPhoto(imageUri: String?): Boolean =
        getImageType(imageUri) != RecipeImageType.AI_GENERATED

    fun computeStats(recipes: List<RecipeEntity>): RecipePhotoStats {
        var aiCount = 0
        var scanCount = 0
        var noneCount = 0

        for (r in recipes) {
            when (getImageType(r.imageUri)) {
                RecipeImageType.AI_GENERATED -> aiCount++
                RecipeImageType.SCANNED_CARD -> scanCount++
                RecipeImageType.NONE -> noneCount++
            }
        }

        return RecipePhotoStats(
            total = recipes.size,
            aiGeneratedCount = aiCount,
            scannedCardCount = scanCount,
            unphotographedCount = noneCount,
            missingAiCount = scanCount + noneCount
        )
    }

    fun filterRecipes(recipes: List<RecipeEntity>, filter: BatchCoverFilter): List<RecipeEntity> {
        return when (filter) {
            BatchCoverFilter.MISSING_AI_PHOTOS -> recipes.filter { isMissingAiPhoto(it.imageUri) }
            BatchCoverFilter.SCANNED_CARDS_ONLY -> recipes.filter { isScannedCard(it.imageUri) }
            BatchCoverFilter.NO_PHOTO_ONLY -> recipes.filter { hasNoPhoto(it.imageUri) }
            BatchCoverFilter.ALL_RECIPES -> recipes
        }
    }
}
