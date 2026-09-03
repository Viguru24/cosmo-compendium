package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitSystemConversionTest {

    @Test
    fun testMetricToAllSystems() {
        val flour = RecipeIngredient(name = "All-Purpose Flour", amount = "500", unit = "g")
        val butter = RecipeIngredient(name = "Butter", amount = "250", unit = "g")
        val milk = RecipeIngredient(name = "Whole Milk", amount = "250", unit = "ml")

        // 1. Metric
        assertEquals("500 g", flour.getConvertedAmount(UnitSystem.METRIC_GRAMS))
        assertEquals("250 g", butter.getConvertedAmount(UnitSystem.METRIC_GRAMS))
        assertEquals("250 ml", milk.getConvertedAmount(UnitSystem.METRIC_GRAMS))

        // 2. US Cups
        assertEquals("4 cups", flour.getConvertedAmount(UnitSystem.CUPS_US))
        assertEquals("1 1/8 cups", butter.getConvertedAmount(UnitSystem.CUPS_US))
        assertEquals("1 cup", milk.getConvertedAmount(UnitSystem.CUPS_US))

        // 3. UK Imperial (Ounces & Pounds, Fluid Ounces)
        assertTrue(flour.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("lb") || flour.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("oz"))
        assertTrue(butter.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("oz"))
        assertTrue(milk.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("fl oz") || milk.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("pt"))

        // 4. Baker's Precision
        assertEquals("500.0 g", flour.getConvertedAmount(UnitSystem.BAKERS_PRECISION))
        assertEquals("250.0 g", butter.getConvertedAmount(UnitSystem.BAKERS_PRECISION))
        assertEquals("250.0 ml", milk.getConvertedAmount(UnitSystem.BAKERS_PRECISION))
    }

    @Test
    fun testCupsToAllSystems() {
        val flour = RecipeIngredient(name = "Flour", amount = "2", unit = "cups")
        val sugar = RecipeIngredient(name = "Granulated Sugar", amount = "1", unit = "cup")
        val vanilla = RecipeIngredient(name = "Vanilla Extract", amount = "1", unit = "tsp")

        // 1. US Cups
        assertEquals("2 cups", flour.getConvertedAmount(UnitSystem.CUPS_US))
        assertEquals("1 cup", sugar.getConvertedAmount(UnitSystem.CUPS_US))
        assertEquals("1 tsp", vanilla.getConvertedAmount(UnitSystem.CUPS_US))

        // 2. Metric
        assertEquals("250 g", flour.getConvertedAmount(UnitSystem.METRIC_GRAMS))
        assertEquals("200 g", sugar.getConvertedAmount(UnitSystem.METRIC_GRAMS))
        assertEquals("5 ml", vanilla.getConvertedAmount(UnitSystem.METRIC_GRAMS))

        // 3. UK Imperial
        assertTrue(flour.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("oz"))
        assertTrue(sugar.getConvertedAmount(UnitSystem.UK_IMPERIAL).contains("oz"))
        assertEquals("1 tsp", vanilla.getConvertedAmount(UnitSystem.UK_IMPERIAL))

        // 4. Baker's Precision
        assertEquals("250.0 g", flour.getConvertedAmount(UnitSystem.BAKERS_PRECISION))
        assertEquals("200.0 g", sugar.getConvertedAmount(UnitSystem.BAKERS_PRECISION))
        assertEquals("5.0 g", vanilla.getConvertedAmount(UnitSystem.BAKERS_PRECISION))
    }

    @Test
    fun testPortionMultiplier() {
        val flour = RecipeIngredient(name = "Flour", amount = "250", unit = "g")
        assertEquals("500 g", flour.getConvertedAmount(UnitSystem.METRIC_GRAMS, multiplier = 2.0f))
        assertEquals("125 g", flour.getConvertedAmount(UnitSystem.METRIC_GRAMS, multiplier = 0.5f))
    }
}
