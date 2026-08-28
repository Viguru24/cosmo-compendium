package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val titleGerman: String = "",
    val titleEnglish: String = "",
    val category: String = "Family Classics", // Baking, Main Dishes, Soups & Stews, Desserts, Breakfast
    val servings: String = "4-6 servings",
    val prepTimeMinutes: Int = 20,
    val cookTimeMinutes: Int = 40,
    val difficulty: String = "Medium", // Easy, Medium, Advanced
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val notes: String = "",
    val notesGerman: String = "",
    val sourceLanguage: String = "both", // "de", "en", "both"
    val imageUri: String? = null,
    val coverTheme: String = "VINTAGE_LEATHER",
    val isFavorite: Boolean = false,
    val rating: Int = 5,
    val timesCooked: Int = 0,
    val originStory: String = "Handwritten family recipe from grandmother's kitchen.",
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalTimeMinutes: Int get() = prepTimeMinutes + cookTimeMinutes
}

