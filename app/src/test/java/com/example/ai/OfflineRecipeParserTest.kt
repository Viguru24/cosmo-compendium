package com.example.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineRecipeParserTest {

    @Test
    fun testParseBlankInputReturnsDefaultRecipe() {
        val result = OfflineRecipeParser.parse("")
        assertEquals("Untitled Recipe", result.titleEnglish)
        assertEquals("Family Classics", result.category)
        assertTrue(result.ingredients.orEmpty().isEmpty())
        assertTrue(result.steps.orEmpty().isEmpty())
    }

    @Test
    fun testParseSimpleEnglishRecipe() {
        val recipeText = """
            Grandma's Apple Pie
            
            Ingredients:
            2 cups Flour
            1 cup Sugar
            1/2 cup Butter
            4 Apples
            1 tsp Cinnamon
            
            Directions:
            1. Preheat oven to 350°F.
            2. Mix flour, sugar, and butter in a large bowl.
            3. Slice apples and mix with cinnamon.
            4. Bake for 45 minutes until golden brown.
        """.trimIndent()

        val result = OfflineRecipeParser.parse(recipeText)
        assertEquals("Grandma's Apple Pie", result.titleEnglish)
        assertEquals("Baking & Desserts", result.category)
        val ingredients = result.ingredients.orEmpty()
        assertEquals(5, ingredients.size)
        assertTrue(ingredients.any { it.nameEnglish?.contains("Flour", ignoreCase = true) == true })
        assertTrue(ingredients.any { it.nameEnglish?.contains("Apple", ignoreCase = true) == true })
        val steps = result.steps.orEmpty()
        assertEquals(4, steps.size)
        assertEquals(45, result.cookTimeMinutes)
    }

    @Test
    fun testParseGermanRecipeWithSectionGroups() {
        val recipeText = """
            Schwarzwälder Kirschtorte
            
            Für den Teig:
            200 g Mehl
            150 g Zucker
            4 Eier
            50 g Kakao
            1 Pck. Backpulver
            
            Für die Füllung:
            1 Glas Sauerkirschen
            500 ml Sahne
            2 EL Kirschwasser
            
            Zubereitung:
            1. Eier mit Zucker schaumig schlagen.
            2. Mehl, Kakao und Backpulver unterheben.
            3. Bei 180 Grad für 30 Minuten backen.
            4. Mit Kirschen und Sahne füllen.
        """.trimIndent()

        val result = OfflineRecipeParser.parse(recipeText)
        assertEquals("de", result.detectedSourceLanguage)
        assertEquals("Baking & Desserts", result.category)
        val ingredients = result.ingredients.orEmpty()
        assertTrue(ingredients.isNotEmpty())

        val doughIngredients = ingredients.filter { it.group?.contains("Teig", ignoreCase = true) == true || it.group?.contains("Dough", ignoreCase = true) == true }
        assertTrue(doughIngredients.isNotEmpty())

        val fillingIngredients = ingredients.filter { it.group?.contains("Füllung", ignoreCase = true) == true || it.group?.contains("Filling", ignoreCase = true) == true }
        assertTrue(fillingIngredients.isNotEmpty())

        val steps = result.steps.orEmpty()
        assertEquals(4, steps.size)
        assertEquals(30, result.cookTimeMinutes)
    }

    @Test
    fun testMultiPageRecipeCardStripping() {
        val multiPageText = """
            --- Page 1 ---
            Bavarian Beef Roast
            
            Ingredients:
            1 kg Beef
            2 Onions
            2 Carrots
            500 ml Beef Broth
            
            --- Page 2 ---
            Directions:
            1. Sear beef on all sides in a hot pot.
            2. Add chopped onions and carrots.
            3. Pour in beef broth and simmer for 90 minutes.
        """.trimIndent()

        val result = OfflineRecipeParser.parse(multiPageText)
        assertEquals("Bavarian Beef Roast", result.titleEnglish)
        assertEquals("Main Dishes", result.category)
        val ingredients = result.ingredients.orEmpty()
        val steps = result.steps.orEmpty()
        assertEquals(4, ingredients.size)
        assertEquals(3, steps.size)
        assertEquals(90, result.cookTimeMinutes)
    }

    @Test
    fun testSpokenFractionNormalization() {
        val line = "quarter / half spoon sugar"
        val parsed = OfflineRecipeParser.parseIngredientLine(line, isSourceGerman = false)
        assertEquals("1/4 - 1/2", parsed.amount)
        assertEquals("spoon", parsed.unit)
        assertTrue(parsed.nameEnglish?.contains("Sugar", ignoreCase = true) == true)
    }

    @Test
    fun testPfundUnitConversion() {
        val line = "1 pfd Mehl"
        val parsed = OfflineRecipeParser.parseIngredientLine(line, isSourceGerman = true)
        assertEquals("500", parsed.amount)
        assertEquals("g", parsed.unit)
    }

    @Test
    fun testDifficultyExtraction() {
        val easyRecipe = OfflineRecipeParser.parse("""
            Easy Quick Toast
            1 slice bread
            1 tbsp butter
            Toast bread and spread butter.
        """.trimIndent())
        assertEquals("Easy", easyRecipe.difficulty)

        val longRecipe = OfflineRecipeParser.parse("""
            Slow-Cooked Sauerbraten
            1 kg beef
            Cook for 120 minutes.
        """.trimIndent())
        assertEquals("Advanced", longRecipe.difficulty)
    }
}
