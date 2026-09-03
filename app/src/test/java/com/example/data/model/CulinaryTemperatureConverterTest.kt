package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CulinaryTemperatureConverterTest {

    @Test
    fun testFahrenheitToUkImperialConversion() {
        val instruction = "Preheat oven to 350°F and grease the pan."
        val converted = CulinaryTemperatureConverter.formatTemperatures(instruction, UnitSystem.UK_IMPERIAL)
        assertTrue(converted.contains("175°C"))
        assertTrue(converted.contains("Gas Mark 3"))
        assertTrue(converted.contains("(350°F)"))
    }

    @Test
    fun testFahrenheitToMetricConversion() {
        val instruction = "Bake at 400°F for 20 minutes."
        val converted = CulinaryTemperatureConverter.formatTemperatures(instruction, UnitSystem.METRIC_GRAMS)
        assertTrue(converted.contains("205°C"))
        assertTrue(converted.contains("(400°F)"))
    }

    @Test
    fun testCelsiusToUsCupsConversion() {
        val instruction = "Preheat to 180°C."
        val converted = CulinaryTemperatureConverter.formatTemperatures(instruction, UnitSystem.CUPS_US)
        assertTrue(converted.contains("355°F"))
        assertTrue(converted.contains("(180°C)"))
    }

    @Test
    fun testGasMarkRatings() {
        assertEquals("Gas Mark ½", CulinaryTemperatureConverter.getGasMark(120))
        assertEquals("Gas Mark 1", CulinaryTemperatureConverter.getGasMark(130))
        assertEquals("Gas Mark 4", CulinaryTemperatureConverter.getGasMark(180))
        assertEquals("Gas Mark 7", CulinaryTemperatureConverter.getGasMark(220))
        assertEquals("Gas Mark 9", CulinaryTemperatureConverter.getGasMark(250))
    }
}
