package com.example.ui.util

import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzySearchHelperTest {

    private fun createRecipe(
        id: Long = 1L,
        title: String,
        category: String = "Main Courses",
        ingredients: List<String> = emptyList(),
        notes: String = "",
        steps: List<String> = emptyList()
    ): RecipeEntity {
        return RecipeEntity(
            id = id,
            title = title,
            titleEnglish = title,
            titleGerman = "",
            category = category,
            servings = "4",
            prepTimeMinutes = 15,
            cookTimeMinutes = 30,
            difficulty = "Easy",
            ingredients = ingredients.map { RecipeIngredient(name = it, amount = "1", unit = "unit", nameEnglish = it) },
            steps = steps.mapIndexed { idx, s -> RecipeStep(stepNumber = idx + 1, instructionEnglish = s) },
            notes = notes,
            sourceLanguage = "en",
            coverTheme = "VINTAGE_LEATHER",
            isFavorite = false,
            rating = 5,
            timesCooked = 0,
            profileName = "Louis"
        )
    }

    @Test
    fun testPieSearch_doesNotMatchThaiCashewChickenOrFineMeringueCake() {
        val thaiCashewChicken = createRecipe(
            id = 153L,
            title = "Thai Cashew Chicken",
            category = "Family Classics",
            ingredients = listOf(
                "/ 1.3lbs boneless skinless Chicken Thighs, diced into bite-sized pieces",
                "White Pepper",
                "/ 7oz Unsalted Cashew Nuts"
            ),
            steps = listOf("Heat the oil in a wok and fry the cashews until deep golden.")
        )

        val fineMeringueCake = createRecipe(
            id = 100L,
            title = "Fine Meringue Cake",
            category = "Baking & Desserts",
            ingredients = listOf(
                "Meringue discs/cookies",
                "Meringue pieces",
                "Heavy whipping cream"
            ),
            notes = "This recipe card only contains a list of ingredients for a 'Fine Meringue Cake'."
        )

        // "pie" must NOT match Thai Cashew Chicken or Fine Meringue Cake
        assertFalse("Thai Cashew Chicken should not match 'pie'", FuzzySearchHelper.matches(thaiCashewChicken, "pie"))
        assertEquals(0, FuzzySearchHelper.score(thaiCashewChicken, "pie"))

        assertFalse("Fine Meringue Cake should not match 'pie'", FuzzySearchHelper.matches(fineMeringueCake, "pie"))
        assertEquals(0, FuzzySearchHelper.score(fineMeringueCake, "pie"))
    }

    @Test
    fun testPieSearch_doesNotMatchPierceInSteps() {
        val roastChicken = createRecipe(
            id = 101L,
            title = "Herb Roasted Chicken",
            steps = listOf("Pierce chicken with a fork to test doneness. Serve piping hot.")
        )
        assertFalse("Roast chicken with 'pierce' and 'piping' should not match 'pie'", FuzzySearchHelper.matches(roastChicken, "pie"))
        assertEquals(0, FuzzySearchHelper.score(roastChicken, "pie"))
    }

    @Test
    fun testPieSearch_matchesRealPieRecipes() {
        val cottagePie = createRecipe(
            id = 6L,
            title = "Traditional Cottage Pie with Cheddar Mash",
            category = "Family Classics"
        )
        val applePie = createRecipe(
            id = 7L,
            title = "Grandma's Apple Pie",
            category = "Baking & Desserts"
        )
        val fruitPies = createRecipe(
            id = 8L,
            title = "Mini Fruit Pies",
            category = "Baking & Desserts"
        )
        val quicheWithPieCrust = createRecipe(
            id = 9L,
            title = "Spinach & Goat Cheese Tart",
            ingredients = listOf("1 unbaked 9-inch pie crust", "3 large eggs", "100g goat cheese")
        )

        assertTrue(FuzzySearchHelper.matches(cottagePie, "pie"))
        assertTrue(FuzzySearchHelper.score(cottagePie, "pie") >= 10000)

        assertTrue(FuzzySearchHelper.matches(applePie, "pie"))
        assertTrue(FuzzySearchHelper.score(applePie, "pie") >= 10000)

        // Plural search "pies" matches singular "pie"
        assertTrue(FuzzySearchHelper.matches(applePie, "pies"))

        // Singular search "pie" matches plural "pies"
        assertTrue(FuzzySearchHelper.matches(fruitPies, "pie"))

        // Pie crust in ingredients matches
        assertTrue(FuzzySearchHelper.matches(quicheWithPieCrust, "pie"))
    }

    @Test
    fun testTargetedSearches_matchIntendedRecipes() {
        val thaiCashewChicken = createRecipe(
            id = 153L,
            title = "Thai Cashew Chicken",
            category = "Family Classics",
            ingredients = listOf(
                "boneless skinless Chicken Thighs, diced into bite-sized pieces",
                "Unsalted Cashew Nuts"
            )
        )
        val fineMeringueCake = createRecipe(
            id = 100L,
            title = "Fine Meringue Cake",
            category = "Baking & Desserts"
        )

        assertTrue(FuzzySearchHelper.matches(thaiCashewChicken, "cashew"))
        assertTrue(FuzzySearchHelper.matches(thaiCashewChicken, "cashews"))
        assertTrue(FuzzySearchHelper.matches(thaiCashewChicken, "chicken"))
        assertTrue(FuzzySearchHelper.matches(thaiCashewChicken, "thai cashew"))

        assertTrue(FuzzySearchHelper.matches(fineMeringueCake, "meringue"))
        assertTrue(FuzzySearchHelper.matches(fineMeringueCake, "cake"))
        assertTrue(FuzzySearchHelper.matches(fineMeringueCake, "fine meringue"))

        // Cross-contamination checks
        assertFalse(FuzzySearchHelper.matches(thaiCashewChicken, "cake"))
        assertFalse(FuzzySearchHelper.matches(fineMeringueCake, "chicken"))
    }

    @Test
    fun testPrefixTypingSearch() {
        val cookies = createRecipe(
            id = 12L,
            title = "Grandma's Famous Chocolate Chip Cookies"
        )
        assertTrue("Typing 'choc' should match 'Chocolate'", FuzzySearchHelper.matches(cookies, "choc"))
        assertTrue("Typing 'cook' should match 'Cookies'", FuzzySearchHelper.matches(cookies, "cook"))
    }
}
