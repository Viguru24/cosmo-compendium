package com.example.data.model

import com.example.data.local.RecipeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeAdaptationAndCostingTest {

    private fun createSampleRecipe(): RecipeEntity {
        return RecipeEntity(
            title = "Grandma's Apple Strudel & Beef Ragu",
            category = "Main Course",
            servings = "4 servings",
            cookTimeMinutes = 45,
            ingredients = listOf(
                RecipeIngredient(name = "all-purpose flour", amount = "250", unit = "g"),
                RecipeIngredient(name = "butter", amount = "100", unit = "g"),
                RecipeIngredient(name = "whole milk", amount = "150", unit = "ml"),
                RecipeIngredient(name = "ground beef", amount = "400", unit = "g"),
                RecipeIngredient(name = "eggs", amount = "2", unit = "stk"),
                RecipeIngredient(name = "gelatin", amount = "2", unit = "sheets"),
                RecipeIngredient(name = "honey", amount = "2", unit = "tbsp"),
                RecipeIngredient(name = "apples", amount = "500", unit = "g")
            ),
            steps = listOf(RecipeStep(stepNumber = 1, instructionEnglish = "Mix and cook thoroughly"))
        )
    }

    @Test
    fun adaptationEngine_celiacSafe_replacesFlourWithGlutenFreeBlend() {
        val recipe = createSampleRecipe()
        val plan = RecipeAdaptationEngine.generateAdaptation(recipe, DietaryTarget.CELIAC_SAFE)

        assertTrue("Plan should have substitutions", plan.hasSubstitutions)
        val flourSub = plan.substitutions.firstOrNull { it.original.name.contains("flour") }
        assertNotNull("Flour substitution should be present", flourSub)
        assertTrue("Replacement should be gluten-free", flourSub!!.replacement.name.contains("Gluten-Free"))
        assertNotNull("Should contain culinary advice", flourSub.culinaryTipEnglish)
    }

    @Test
    fun adaptationEngine_vegetarian_replacesMeatAndGelatin() {
        val recipe = createSampleRecipe()
        val plan = RecipeAdaptationEngine.generateAdaptation(recipe, DietaryTarget.VEGETARIAN)

        assertTrue(plan.hasSubstitutions)
        val beefSub = plan.substitutions.firstOrNull { it.original.name.contains("beef") }
        assertNotNull("Beef substitution should be present", beefSub)
        assertTrue("Beef should be replaced with plant protein", beefSub!!.replacement.name.contains("Lentils") || beefSub.replacement.name.contains("Tofu") || beefSub.replacement.name.contains("Mushrooms"))

        val gelatinSub = plan.substitutions.firstOrNull { it.original.name.contains("gelatin") }
        assertNotNull("Gelatin should be replaced", gelatinSub)
        assertTrue("Should replace gelatin with Agar-Agar", gelatinSub!!.replacement.name.contains("Agar-Agar"))
    }

    @Test
    fun adaptationEngine_vegan_replacesDairyEggsHoneyAndMeat() {
        val recipe = createSampleRecipe()
        val plan = RecipeAdaptationEngine.generateAdaptation(recipe, DietaryTarget.VEGAN)

        val replacedNames = plan.substitutions.map { it.original.name.lowercase() }
        assertTrue("Butter should be replaced", replacedNames.any { it.contains("butter") })
        assertTrue("Milk should be replaced", replacedNames.any { it.contains("milk") })
        assertTrue("Eggs should be replaced", replacedNames.any { it.contains("eggs") })
        assertTrue("Honey should be replaced", replacedNames.any { it.contains("honey") })
        assertTrue("Beef should be replaced", replacedNames.any { it.contains("beef") })
    }

    @Test
    fun costingEngine_calculatesPortionCostAndMenuPriceAccurately() {
        val recipe = createSampleRecipe()
        val analysis = RecipeCostingEngine.calculateCost(
            recipe = recipe,
            portions = 4,
            targetFoodCostPercent = 25.0, // 25% target food cost = 4x multiplier
            currency = CostCurrency.EUR
        )

        assertTrue("Total batch cost should be positive", analysis.totalBatchCost > 0.0)
        assertTrue("Cost per portion should be positive", analysis.costPerPortion > 0.0)
        assertEquals(4, analysis.portions)
        assertEquals(25.0, analysis.targetFoodCostPercent, 0.01)

        // At 25% food cost, selling price should be exactly 4x cost per portion
        val expectedSellingPrice = analysis.costPerPortion / 0.25
        assertEquals(expectedSellingPrice, analysis.suggestedMenuPrice, 0.01)

        // Gross profit margin should be 75%
        assertEquals(75.0, analysis.grossMarginPercent, 0.01)

        // Kitchen spec sheet should contain title and breakdown
        val spec = analysis.generateKitchenSpecSheet()
        assertTrue(spec.contains("KITCHEN SPEC & FOOD COST SPEC SHEET"))
        assertTrue(spec.contains("Suggested Menu Price"))
    }
}
