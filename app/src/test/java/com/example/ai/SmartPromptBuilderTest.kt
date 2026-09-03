package com.example.ai

import com.example.data.local.RecipeEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartPromptBuilderTest {

    @Test
    fun buildSmartCulinaryPrompt_bobotie_generatesAuthenticEggCustardBayLeavesAndTurmericRice() {
        val recipe = RecipeEntity(
            title = "Bobotie mince rice raisins",
            category = "Main Dishes"
        )
        val prompt = SmartPromptBuilder.buildPromptForRecipe(recipe)

        // Must specify authentic Bobotie elements: egg custard, bay leaves, turmeric yellow rice, chutney
        assertTrue("Expected egg custard crust in prompt", prompt.contains("custard", ignoreCase = true))
        assertTrue("Expected bay leaves in prompt", prompt.contains("bay leaves", ignoreCase = true))
        assertTrue("Expected yellow rice in prompt", prompt.contains("yellow rice", ignoreCase = true) || prompt.contains("turmeric", ignoreCase = true))
        assertTrue("Expected chutney in prompt", prompt.contains("chutney", ignoreCase = true))
    }

    @Test
    fun buildSmartCulinaryPrompt_elderflowerSyrup_generatesGlassBottleAndNoPlates() {
        val recipe = RecipeEntity(
            title = "Elderflower Syrup",
            titleGerman = "Holundersirup",
            category = "Beverages & Drinks"
        )
        val prompt = SmartPromptBuilder.buildPromptForRecipe(recipe)

        // Must generate glass bottle with blossoms
        assertTrue("Expected glass bottle in prompt", prompt.contains("vintage glass bottle", ignoreCase = true) || prompt.contains("glass bottle", ignoreCase = true))
        assertTrue("Expected elderflower blossoms", prompt.contains("elderflower blossoms", ignoreCase = true) || prompt.contains("Holunderblüten", ignoreCase = true))
        
        // Must NOT contain ceramic plate or stew
        assertFalse("Prompt should not contain plated on ceramic plate for syrup", prompt.contains("plated on a rustic ceramic dish", ignoreCase = true))
    }

    @Test
    fun buildSmartCulinaryPrompt_artisanalJam_generatesCanningJar() {
        val recipe = RecipeEntity(
            title = "Grandma's Strawberry Rhubarb Jam",
            category = "Breakfast & Brunch"
        )
        val prompt = SmartPromptBuilder.buildPromptForRecipe(recipe)
        assertTrue(prompt.contains("canning jar", ignoreCase = true) || prompt.contains("jar", ignoreCase = true))
    }

    @Test
    fun buildSmartCulinaryPrompt_customPromptProvided_usesCustomPromptDirectly() {
        val recipe = RecipeEntity(
            title = "Elderflower Syrup",
            category = "Beverages & Drinks"
        )
        val custom = "My custom artistic photo prompt with lemons and mint in sunlight"
        val prompt = SmartPromptBuilder.buildPromptForRecipe(recipe, customPrompt = custom)
        assertTrue(prompt == custom)
    }
}
