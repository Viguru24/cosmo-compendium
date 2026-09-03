package com.example.data.model

import com.example.ai.OfflineRecipeParser
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeIngredientSanitizationTest {

    @Test
    fun testCleanIngredientName_stripsLeadingSlashesAndDualUnits() {
        val input1 = "/ 1.3lbs boneless skinless Chicken Thighs, diced into bite-sized pieces"
        val expected1 = "boneless skinless Chicken Thighs, diced into bite-sized pieces"
        assertEquals(expected1, RecipeIngredient.cleanIngredientName(input1))

        val input2 = "+ 2 tsp Light Soy Sauce"
        val expected2 = "Light Soy Sauce"
        assertEquals(expected2, RecipeIngredient.cleanIngredientName(input2))

        val input3 = "/ 1/3 cup Plain Flour"
        val expected3 = "Plain Flour"
        assertEquals(expected3, RecipeIngredient.cleanIngredientName(input3))

        val input4 = "/ 1/2 cup Veg Oil (for frying)"
        val expected4 = "Veg Oil (for frying)"
        assertEquals(expected4, RecipeIngredient.cleanIngredientName(input4))

        val input5 = "/ 7oz Unsalted Cashew Nuts"
        val expected5 = "Unsalted Cashew Nuts"
        assertEquals(expected5, RecipeIngredient.cleanIngredientName(input5))
    }

    @Test
    fun testGetDisplayName_preservesFractionsAndDoesNotSplitDestructively() {
        val ingredient1 = RecipeIngredient(
            name = "Plain Flour",
            amount = "1/2",
            unit = "cup",
            nameEnglish = "Plain Flour"
        )
        assertEquals("Plain Flour", ingredient1.getDisplayName(LanguageMode.ENGLISH))

        val ingredient2 = RecipeIngredient(
            name = "boneless skinless Chicken Thighs, diced into bite-sized pieces",
            amount = "600",
            unit = "g",
            nameEnglish = "/ 1.3lbs boneless skinless Chicken Thighs, diced into bite-sized pieces"
        )
        assertEquals("boneless skinless Chicken Thighs, diced into bite-sized pieces", ingredient2.getDisplayName(LanguageMode.ENGLISH))
    }

    @Test
    fun testOfflineRecipeParser_dualMeasurements() {
        val line = "600g / 1.3lbs boneless skinless Chicken Thighs, diced into bite-sized pieces"
        val parsed = OfflineRecipeParser.parseIngredientLine(line, isSourceGerman = false)
        assertEquals("600", parsed.amount)
        assertEquals("g", parsed.unit)
        assertEquals("boneless skinless Chicken Thighs, diced into bite-sized pieces", parsed.nameEnglish)
    }
}
