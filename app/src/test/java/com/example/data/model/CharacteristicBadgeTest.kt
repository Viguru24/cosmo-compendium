package com.example.data.model

import com.example.data.local.RecipeEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacteristicBadgeTest {

    @Test
    fun getCharacteristicBadge_flourlessCake_returnsFlourless() {
        val recipe = RecipeEntity(
            title = "Decadent Chocolate Cake",
            category = "Baking & Desserts",
            ingredients = listOf(
                RecipeIngredient(name = "Dark Chocolate", amount = "200", unit = "g"),
                RecipeIngredient(name = "Eggs", amount = "4", unit = "large"),
                RecipeIngredient(name = "Butter", amount = "100", unit = "g")
            )
        )
        val badge = recipe.getCharacteristicBadge(LanguageMode.ENGLISH)
        assertEquals("Flourless", badge)
    }

    @Test
    fun getCharacteristicBadge_bundtCake_returnsBundt() {
        val recipe = RecipeEntity(
            title = "Grandma's Marble Bundt Cake",
            category = "Baking & Desserts",
            steps = listOf(
                RecipeStep(stepNumber = 1, instructionEnglish = "Grease the bundt pan thoroughly with butter and dust with flour.")
            )
        )
        val badge = recipe.getCharacteristicBadge(LanguageMode.ENGLISH)
        assertEquals("Bundt", badge)
    }

    @Test
    fun getCharacteristicBadge_quickMeal_returnsQuick30Min() {
        val recipe = RecipeEntity(
            title = "Garlic Butter Shrimp",
            category = "Main Dishes",
            prepTimeMinutes = 10,
            cookTimeMinutes = 10
        )
        val badge = recipe.getCharacteristicBadge(LanguageMode.ENGLISH)
        assertEquals("Quick 30-Min", badge)
    }
}
