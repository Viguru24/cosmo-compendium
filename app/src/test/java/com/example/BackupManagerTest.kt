package com.example

import com.example.data.backup.BackupManager
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    @Test
    fun testExportAndParseBackup() {
        val sampleIngredients = listOf(
            RecipeIngredient(name = "Flour", amount = "500", unit = "g", nameEnglish = "All-Purpose Flour"),
            RecipeIngredient(name = "Butter", amount = "250", unit = "g", nameEnglish = "Unsalted Butter")
        )
        val sampleSteps = listOf(
            RecipeStep(stepNumber = 1, instructionEnglish = "Preheat oven to 350F", timerMinutes = 0),
            RecipeStep(stepNumber = 2, instructionEnglish = "Mix flour and butter until smooth", timerMinutes = 5)
        )
        val sampleRecipes = listOf(
            RecipeEntity(
                id = 1,
                title = "Oma's Apple Strudel",
                titleGerman = "Apfelstrudel",
                titleEnglish = "Grandma's Apple Strudel",
                category = "Baking & Desserts",
                servings = "8 slices",
                prepTimeMinutes = 30,
                cookTimeMinutes = 45,
                difficulty = "Medium",
                ingredients = sampleIngredients,
                steps = sampleSteps,
                notes = "Always use crisp baking apples.",
                originStory = "From our Munich family kitchen, 1948."
            )
        )

        // Export to JSON
        val exportedJson = BackupManager.exportToJson(sampleRecipes)
        assertTrue(exportedJson.isNotBlank())
        assertTrue(exportedJson.contains("Oma's Apple Strudel"))
        assertTrue(exportedJson.contains("Apfelstrudel"))

        // Parse backup
        val parseResult = BackupManager.parseBackup(exportedJson)
        assertTrue(parseResult.isSuccess)

        val manifest = parseResult.getOrNull()
        assertNotNull(manifest)
        assertEquals(1, manifest?.recipeCount)
        assertEquals("Oma's Apple Strudel", manifest?.recipes?.firstOrNull()?.title)
        assertEquals(2, manifest?.recipes?.firstOrNull()?.ingredients?.size)
        assertEquals(2, manifest?.recipes?.firstOrNull()?.steps?.size)
    }

    @Test
    fun testParseArrayJsonFallback() {
        val rawArrayJson = """
            [
              {
                "title": "Family Pot Roast",
                "titleGerman": "Schmorbraten",
                "category": "Main Dishes",
                "servings": "6 servings",
                "prepTimeMinutes": 20,
                "cookTimeMinutes": 90,
                "difficulty": "Easy",
                "ingredients": [
                  {"name": "Beef Chuck Roast", "amount": "1.5", "unit": "kg"}
                ],
                "steps": [
                  {"stepNumber": 1, "instructionEnglish": "Sear meat on all sides", "timerMinutes": 10}
                ]
              }
            ]
        """.trimIndent()

        val parseResult = BackupManager.parseBackup(rawArrayJson)
        assertTrue(parseResult.isSuccess)
        val manifest = parseResult.getOrNull()
        assertNotNull(manifest)
        assertEquals(1, manifest?.recipeCount)
        assertEquals("Family Pot Roast", manifest?.recipes?.firstOrNull()?.title)
    }
}
