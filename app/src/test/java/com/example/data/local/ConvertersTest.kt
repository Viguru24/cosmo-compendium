package com.example.data.local

import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun testIngredientListRoundTrip() {
        val originalList = listOf(
            RecipeIngredient(
                name = "Flour",
                amount = "500",
                unit = "g",
                nameGerman = "Mehl",
                nameEnglish = "Flour",
                isOptional = false,
                group = "Dough"
            ),
            RecipeIngredient(
                name = "Sugar",
                amount = "200",
                unit = "g",
                nameGerman = "Zucker",
                nameEnglish = "Sugar",
                isOptional = true,
                group = "Dough"
            )
        )

        val json = converters.fromIngredientList(originalList)
        assertTrue(json.isNotBlank())
        assertTrue(json.contains("Flour"))

        val deserialized = converters.toIngredientList(json)
        assertEquals(2, deserialized.size)
        assertEquals("Flour", deserialized[0].name)
        assertEquals("500", deserialized[0].amount)
        assertEquals("g", deserialized[0].unit)
        assertEquals("Dough", deserialized[0].group)
        assertTrue(deserialized[1].isOptional)
    }

    @Test
    fun testEmptyIngredientListHandling() {
        val emptyJson = converters.fromIngredientList(emptyList())
        assertEquals("[]", emptyJson)

        val fromNull = converters.toIngredientList(null)
        assertTrue(fromNull.isEmpty())

        val fromBlank = converters.toIngredientList("")
        assertTrue(fromBlank.isEmpty())

        val fromEmptyBracket = converters.toIngredientList("[]")
        assertTrue(fromEmptyBracket.isEmpty())
    }

    @Test
    fun testIngredientListLenientPlainStringFallback() {
        val legacyStringArrayJson = "[\"2 cups Flour\", \"1 cup Sugar\", \"1 tsp Vanilla\"]"
        val parsed = converters.toIngredientList(legacyStringArrayJson)

        assertEquals(3, parsed.size)
        assertEquals("2 cups Flour", parsed[0].name)
        assertEquals("1 cup Sugar", parsed[1].name)
        assertEquals("1 tsp Vanilla", parsed[2].name)
    }

    @Test
    fun testStepListRoundTrip() {
        val originalSteps = listOf(
            RecipeStep(
                stepNumber = 1,
                instructionEnglish = "Preheat oven to 350°F.",
                instructionGerman = "Ofen auf 180°C vorheizen.",
                timerMinutes = 0,
                tip = "Make sure the rack is centered."
            ),
            RecipeStep(
                stepNumber = 2,
                instructionEnglish = "Bake for 30 minutes.",
                instructionGerman = "30 Minuten backen.",
                timerMinutes = 30
            )
        )

        val json = converters.fromStepList(originalSteps)
        assertTrue(json.isNotBlank())

        val deserialized = converters.toStepList(json)
        assertEquals(2, deserialized.size)
        assertEquals(1, deserialized[0].stepNumber)
        assertEquals("Preheat oven to 350°F.", deserialized[0].instructionEnglish)
        assertEquals(30, deserialized[1].timerMinutes)
        assertEquals("Make sure the rack is centered.", deserialized[0].tip)
    }

    @Test
    fun testStepListLenientPlainStringFallback() {
        val legacyStepsJson = "[\"Mix ingredients in a bowl.\", \"Bake for 20 minutes.\"]"
        val parsed = converters.toStepList(legacyStepsJson)

        assertEquals(2, parsed.size)
        assertEquals(1, parsed[0].stepNumber)
        assertEquals("Mix ingredients in a bowl.", parsed[0].instructionEnglish)
        assertEquals(2, parsed[1].stepNumber)
        assertEquals("Bake for 20 minutes.", parsed[1].instructionEnglish)
    }
}
