package com.example.data.model

import com.example.data.local.RecipeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DietaryClassifierTest {

    private fun createRecipe(
        title: String = "Test Recipe",
        ingredients: List<RecipeIngredient>,
        tags: List<String> = emptyList()
    ): RecipeEntity {
        return RecipeEntity(
            title = title,
            category = "Main Course",
            ingredients = ingredients,
            instructions = listOf(RecipeStep(stepNumber = 1, text = "Cook thoroughly")),
            servings = "4",
            cookTimeMinutes = 30,
            tags = tags
        )
    }

    @Test
    fun analyze_celiacSafeNaturallyGlutenFreeRecipe_returnsCeliacSafe() {
        val recipe = createRecipe(
            title = "Grilled Lemon Herb Chicken with Jasmine Rice",
            ingredients = listOf(
                RecipeIngredient(name = "chicken breast", amount = "500", unit = "g"),
                RecipeIngredient(name = "jasmine rice", amount = "200", unit = "g"),
                RecipeIngredient(name = "olive oil", amount = "2", unit = "tbsp"),
                RecipeIngredient(name = "lemon juice", amount = "1", unit = "lemon"),
                RecipeIngredient(name = "sea salt", amount = "1", unit = "tsp"),
                RecipeIngredient(name = "fresh rosemary", amount = "2", unit = "sprigs")
            )
        )

        val analysis = DietaryClassifier.analyze(recipe)

        assertTrue("Should be celiac-safe", analysis.isCeliacSafe)
        assertTrue("Should be gluten-free", analysis.isGlutenFree)
        assertTrue("Should be dairy-free", analysis.isDairyFree)
        assertTrue("Should be nut-free", analysis.isNutFree)
        assertFalse("Contains chicken, should not be vegetarian", analysis.isVegetarian)
        assertFalse("Contains chicken, should not be vegan", analysis.isVegan)
        assertFalse("Should have no detected gluten", analysis.detectedAllergens.contains(Allergen.GLUTEN))
    }

    @Test
    fun analyze_recipeWithWheatFlour_detectsGlutenAndNotCeliacSafe() {
        val recipe = createRecipe(
            title = "Traditional German Apple Strudel",
            ingredients = listOf(
                RecipeIngredient(nameGerman = "Weizenmehl Type 405", nameEnglish = "all-purpose wheat flour", amount = "250", unit = "g"),
                RecipeIngredient(name = "apples", amount = "1", unit = "kg"),
                RecipeIngredient(name = "butter", amount = "50", unit = "g"),
                RecipeIngredient(name = "cinnamon", amount = "1", unit = "tsp")
            )
        )

        val analysis = DietaryClassifier.analyze(recipe)

        assertFalse("Should not be celiac-safe", analysis.isCeliacSafe)
        assertFalse("Should not be gluten-free", analysis.isGlutenFree)
        assertTrue("Should detect gluten allergen", analysis.detectedAllergens.contains(Allergen.GLUTEN))
        assertTrue("Should detect dairy allergen from butter", analysis.detectedAllergens.contains(Allergen.DAIRY))
        assertTrue("Should be vegetarian", analysis.isVegetarian)
        assertFalse("Contains butter, not vegan", analysis.isVegan)
    }

    @Test
    fun analyze_recipeWithSoySauceWithoutGfVerification_triggersCeliacCaution() {
        val recipe = createRecipe(
            title = "Stir Fry Veggies with Soy Sauce",
            ingredients = listOf(
                RecipeIngredient(name = "broccoli florets", amount = "300", unit = "g"),
                RecipeIngredient(name = "soy sauce", amount = "2", unit = "tbsp"),
                RecipeIngredient(name = "sesame oil", amount = "1", unit = "tbsp")
            )
        )

        val analysis = DietaryClassifier.analyze(recipe)

        assertFalse("Standard soy sauce should not qualify as certified celiac-safe", analysis.isCeliacSafe)
        assertTrue("Should flag celiac caution for soy sauce", analysis.celiacCautions.any { it.contains("soy sauce", ignoreCase = true) })
        assertTrue("Should detect soy allergen", analysis.detectedAllergens.contains(Allergen.SOY))
        assertTrue("Should detect sesame allergen", analysis.detectedAllergens.contains(Allergen.SESAME))
        assertTrue("Vegetables and sesame oil are vegan", analysis.isVegan)
    }

    @Test
    fun analyze_recipeWithCertifiedGlutenFreeSoySauce_passesCeliacSafe() {
        val recipe = createRecipe(
            title = "Gluten Free Stir Fry",
            ingredients = listOf(
                RecipeIngredient(name = "tofu", amount = "200", unit = "g"),
                RecipeIngredient(name = "gluten-free tamari soy sauce", amount = "2", unit = "tbsp"),
                RecipeIngredient(name = "olive oil", amount = "1", unit = "tbsp")
            )
        )

        val analysis = DietaryClassifier.analyze(recipe)

        assertTrue("Certified GF tamari should be celiac-safe", analysis.isCeliacSafe)
        assertTrue("Should be vegan", analysis.isVegan)
        assertTrue("Should detect soy allergen from tofu/tamari", analysis.detectedAllergens.contains(Allergen.SOY))
    }

    @Test
    fun analyze_veganRecipe_detectsVeganAndVegetarianAndDairyFreeAndEggFree() {
        val recipe = createRecipe(
            title = "Avocado Chickpea Mediterranean Salad",
            ingredients = listOf(
                RecipeIngredient(name = "cooked chickpeas", amount = "400", unit = "g"),
                RecipeIngredient(name = "ripe avocado", amount = "2", unit = ""),
                RecipeIngredient(name = "cherry tomatoes", amount = "150", unit = "g"),
                RecipeIngredient(name = "extra virgin olive oil", amount = "2", unit = "tbsp"),
                RecipeIngredient(name = "lime juice", amount = "1", unit = "tbsp")
            )
        )

        val analysis = DietaryClassifier.analyze(recipe)

        assertTrue("Should be vegan", analysis.isVegan)
        assertTrue("Should be vegetarian", analysis.isVegetarian)
        assertTrue("Should be celiac safe", analysis.isCeliacSafe)
        assertTrue("Should be gluten-free", analysis.isGlutenFree)
        assertTrue("Should be dairy-free", analysis.isDairyFree)
        assertTrue("Should be nut-free", analysis.isNutFree)
        assertTrue("Should be egg-free", analysis.isEggFree)
        assertTrue("No major FDA allergens present", analysis.detectedAllergens.isEmpty())
    }

    @Test
    fun analyze_germanRecipeWithSpeck_detectsNonVegetarian() {
        val recipe = createRecipe(
            title = "Tiroler Gröstl mit Speck",
            ingredients = listOf(
                RecipeIngredient(nameGerman = "Kartoffeln", nameEnglish = "potatoes", amount = "500", unit = "g"),
                RecipeIngredient(nameGerman = "Bauchspeck gewürfelt", nameEnglish = "diced bacon", amount = "150", unit = "g"),
                RecipeIngredient(nameGerman = "Zwiebeln", nameEnglish = "onions", amount = "1", unit = "Stk"),
                RecipeIngredient(nameGerman = "Eier", nameEnglish = "eggs", amount = "2", unit = "Stk")
            )
        )

        val analysis = DietaryClassifier.analyze(recipe)

        assertFalse("Contains Speck, should not be vegetarian", analysis.isVegetarian)
        assertFalse("Contains Speck & Eier, should not be vegan", analysis.isVegan)
        assertTrue("Contains Eier, detects egg allergen", analysis.detectedAllergens.contains(Allergen.EGGS))
        assertTrue("Kartoffeln, Speck, Zwiebeln, Eier are naturally gluten-free", analysis.isGlutenFree)
    }

    @Test
    fun dietaryFilter_matchesCorrectly() {
        val gfRecipe = createRecipe(
            title = "Plain Rice & Veggies",
            ingredients = listOf(
                RecipeIngredient(name = "rice", amount = "100", unit = "g"),
                RecipeIngredient(name = "carrots", amount = "2", unit = "")
            )
        )

        val meatRecipe = createRecipe(
            title = "Beef Steak",
            ingredients = listOf(
                RecipeIngredient(name = "beef ribeye", amount = "300", unit = "g"),
                RecipeIngredient(name = "butter", amount = "20", unit = "g")
            )
        )

        assertTrue("ALL matches everything", DietaryFilter.ALL.matches(gfRecipe))
        assertTrue("ALL matches meat", DietaryFilter.ALL.matches(meatRecipe))

        assertTrue("CELIAC_SAFE matches gfRecipe", DietaryFilter.CELIAC_SAFE.matches(gfRecipe))
        assertFalse("CELIAC_SAFE rejects meat with dairy butter or unverified items if caution, wait butter is GF", DietaryFilter.CELIAC_SAFE.matches(meatRecipe))

        assertTrue("VEGETARIAN matches gfRecipe", DietaryFilter.VEGETARIAN.matches(gfRecipe))
        assertFalse("VEGETARIAN rejects meatRecipe", DietaryFilter.VEGETARIAN.matches(meatRecipe))

        assertTrue("VEGAN matches gfRecipe", DietaryFilter.VEGAN.matches(gfRecipe))
        assertFalse("VEGAN rejects meatRecipe", DietaryFilter.VEGAN.matches(meatRecipe))

        assertTrue("DAIRY_FREE matches gfRecipe", DietaryFilter.DAIRY_FREE.matches(gfRecipe))
        assertFalse("DAIRY_FREE rejects butter in meatRecipe", DietaryFilter.DAIRY_FREE.matches(meatRecipe))
    }
}
