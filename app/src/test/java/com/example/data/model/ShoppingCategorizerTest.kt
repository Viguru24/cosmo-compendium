package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingCategorizerTest {

    @Test
    fun testFreshProduceCategorization() {
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Honeycrisp Apples"))
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Äpfel"))
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Yellow Onions"))
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Knoblauchzehen"))
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Fresh Parsley"))
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Champignons"))
        assertEquals("Fresh Produce", ShoppingCategorizer.categorizeIngredient("Spinach"))
    }

    @Test
    fun testDairyAndRefrigeratedCategorization() {
        assertEquals("Dairy & Refrigerated", ShoppingCategorizer.categorizeIngredient("Unsalted Butter"))
        assertEquals("Dairy & Refrigerated", ShoppingCategorizer.categorizeIngredient("Whole Milk"))
        assertEquals("Dairy & Refrigerated", ShoppingCategorizer.categorizeIngredient("Large Eggs"))
        assertEquals("Dairy & Refrigerated", ShoppingCategorizer.categorizeIngredient("Magerquark"))
        assertEquals("Dairy & Refrigerated", ShoppingCategorizer.categorizeIngredient("Heavy Cream"))
        assertEquals("Dairy & Refrigerated", ShoppingCategorizer.categorizeIngredient("Grated Parmesan Cheese"))
    }

    @Test
    fun testMeatAndSeafoodCategorization() {
        assertEquals("Meat & Seafood", ShoppingCategorizer.categorizeIngredient("Ground Beef"))
        assertEquals("Meat & Seafood", ShoppingCategorizer.categorizeIngredient("Schweinebraten"))
        assertEquals("Meat & Seafood", ShoppingCategorizer.categorizeIngredient("Smoked Bacon"))
        assertEquals("Meat & Seafood", ShoppingCategorizer.categorizeIngredient("Chicken Breast"))
        assertEquals("Meat & Seafood", ShoppingCategorizer.categorizeIngredient("Fresh Salmon Fillet"))
        assertEquals("Meat & Seafood", ShoppingCategorizer.categorizeIngredient("Bratwurst"))
    }

    @Test
    fun testBakeryAndBreadCategorization() {
        assertEquals("Bakery & Bread", ShoppingCategorizer.categorizeIngredient("Sourdough Bread"))
        assertEquals("Bakery & Bread", ShoppingCategorizer.categorizeIngredient("Semmeln"))
        assertEquals("Bakery & Bread", ShoppingCategorizer.categorizeIngredient("Puff Pastry Sheets"))
        assertEquals("Bakery & Bread", ShoppingCategorizer.categorizeIngredient("Breadcrumbs / Paniermehl"))
    }

    @Test
    fun testBakingAndSpicesCategorization() {
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("All-Purpose Flour"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Puderzucker"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Active Dry Yeast"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Vanilla Extract"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Ground Cinnamon"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Baking Powder"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Chopped Almonds"))
        assertEquals("Baking & Spices", ShoppingCategorizer.categorizeIngredient("Cocoa Powder"))
    }

    @Test
    fun testPantryAndStaplesCategorization() {
        assertEquals("Pantry & Staples", ShoppingCategorizer.categorizeIngredient("Extra Virgin Olive Oil"))
        assertEquals("Pantry & Staples", ShoppingCategorizer.categorizeIngredient("Apple Cider Vinegar"))
        assertEquals("Pantry & Staples", ShoppingCategorizer.categorizeIngredient("Dijon Mustard"))
        assertEquals("Pantry & Staples", ShoppingCategorizer.categorizeIngredient("Beef Broth"))
        assertEquals("Pantry & Staples", ShoppingCategorizer.categorizeIngredient("Spätzle Noodles"))
        assertEquals("Pantry & Staples", ShoppingCategorizer.categorizeIngredient("Jasmine Rice"))
    }
}
