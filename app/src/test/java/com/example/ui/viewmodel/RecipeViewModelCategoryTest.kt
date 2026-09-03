package com.example.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeViewModelCategoryTest {

    private fun extractCategoriesFromRecipe(catStr: String): List<String> {
        val trimmed = catStr.trim()
        if (trimmed.isBlank()) return listOf("Uncategorized")
        val parts = trimmed.split(",", ";", "|||", "|").map { it.trim() }.filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts else listOf(trimmed)
    }

    @Test
    fun testExtractCategoriesPreservesAmpersand() {
        val cats1 = extractCategoriesFromRecipe("Baking & Desserts")
        assertEquals(listOf("Baking & Desserts"), cats1)

        val cats2 = extractCategoriesFromRecipe("Soups & Stews, Holiday & Traditions")
        assertEquals(listOf("Soups & Stews", "Holiday & Traditions"), cats2)

        val cats3 = extractCategoriesFromRecipe("Salads & Starters; Main Dishes")
        assertEquals(listOf("Salads & Starters", "Main Dishes"), cats3)
    }

    @Test
    fun testCategoryCountingWithMultiCategories() {
        val testCategories = listOf(
            "Baking & Desserts",
            "Baking & Desserts",
            "Main Dishes",
            "Soups & Stews, Family Classics",
            "Custom Dessert"
        )

        val counts = mutableMapOf<String, Int>()
        testCategories.forEach { catStr ->
            extractCategoriesFromRecipe(catStr).forEach { c ->
                counts[c] = (counts[c] ?: 0) + 1
            }
        }

        assertEquals(2, counts["Baking & Desserts"])
        assertEquals(1, counts["Main Dishes"])
        assertEquals(1, counts["Soups & Stews"])
        assertEquals(1, counts["Family Classics"])
        assertEquals(1, counts["Custom Dessert"])
    }
}
