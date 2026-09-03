package com.example.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiModelConfigTest {

    @Test
    fun testPrimaryModelIsGemini25Flash() {
        assertEquals("gemini-2.5-flash", GeminiModelConfig.PRIMARY_MODEL)
    }

    @Test
    fun testFallbackModelIsGemini35Flash() {
        assertEquals("gemini-3.5-flash", GeminiModelConfig.FALLBACK_MODEL)
    }

    @Test
    fun testActiveModelsAreOfficial() {
        val validOfficialModels = setOf(
            "gemini-2.5-flash",
            "gemini-3.5-flash",
            "gemini-3.7-flash",
            "gemini-3.6-flash",
            "gemini-flash-latest"
        )
        for (model in GeminiModelConfig.ACTIVE_MODELS) {
            assertTrue("Model $model must be a recognized official Gemini model", validOfficialModels.contains(model))
        }
    }

    @Test
    fun testModelRetirementAutoEvictsFromEffectiveModels() {
        GeminiClient.retiredModels.clear()
        GeminiClient.markModelRetired("gemini-2.5-flash")
        val effective = GeminiClient.getEffectiveModels()
        org.junit.Assert.assertFalse("Retired model must not be present in effective models", effective.contains("gemini-2.5-flash"))
        assertTrue("Fallback model must be present", effective.contains("gemini-3.5-flash"))
        GeminiClient.retiredModels.clear()
    }
}
