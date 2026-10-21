package com.example.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SousChefActionEngineTest {

    @Test
    fun parseIntent_scanCards_returnsScanCameraIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Please scan recipe cards for Annette",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue(result is SousChefIntent.ScanCamera)
        assertEquals("Annette", (result as SousChefIntent.ScanCamera).targetProfile)
    }

    @Test
    fun parseIntent_urlImport_returnsImportUrlIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Import https://example.com/recipe for Louis",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue(result is SousChefIntent.ImportUrl)
        assertEquals("https://example.com/recipe", (result as SousChefIntent.ImportUrl).url)
    }

    @Test
    fun parseIntent_switchProfile_returnsSwitchProfileIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Switch to Annette's cookbook",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue(result is SousChefIntent.SwitchProfile)
        assertEquals("Annette", (result as SousChefIntent.SwitchProfile).targetProfile)
    }

    @Test
    fun parseIntent_putTogetherRecipe_returnsCreateRecipeIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "put together a recipe for Oat milk",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue("Expected CreateRecipe intent, got: $result", result is SousChefIntent.CreateRecipe)
        val createIntent = result as SousChefIntent.CreateRecipe
        assertEquals("oat milk", createIntent.dishQuery.lowercase())
        assertEquals("Louis", createIntent.targetProfile)
    }

    @Test
    fun parseIntent_createRecipeWithTargetProfile_returnsCreateRecipeIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Create a recipe for chocolate chip cookies for Annette",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue(result is SousChefIntent.CreateRecipe)
        val createIntent = result as SousChefIntent.CreateRecipe
        assertTrue(createIntent.dishQuery.contains("chocolate chip cookies"))
        assertEquals("Annette", createIntent.targetProfile)
    }

    @Test
    fun parseIntent_pastedRecipeText_returnsImportTextRecipeIntent() {
        val pastedRecipe = """
            Homemade Oat Milk
            Ingredients:
            - 1 cup rolled oats
            - 4 cups cold water
            - 1 pinch of salt
            - 1 tbsp maple syrup
            
            Instructions:
            1. Add oats and cold water into blender.
            2. Blend on high speed for 30 seconds.
            3. Strain through a nut milk bag or fine mesh towel.
        """.trimIndent()

        val result = SousChefActionEngine.parseIntent(
            prompt = pastedRecipe,
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue("Expected ImportTextRecipe intent, got: $result", result is SousChefIntent.ImportTextRecipe)
        val importIntent = result as SousChefIntent.ImportTextRecipe
        assertEquals(pastedRecipe, importIntent.text)
    }

    @Test
    fun parseIntent_youTubeUrlWithoutScheme_returnsImportUrlIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Can you import youtube.com/watch?v=dQw4w9WgXcQ for Louis",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue("Expected ImportUrl, got $result", result is SousChefIntent.ImportUrl)
        val importIntent = result as SousChefIntent.ImportUrl
        assertEquals("https://youtube.com/watch?v=dQw4w9WgXcQ", importIntent.url)
        assertEquals("Louis", importIntent.targetProfile)
    }

    @Test
    fun parseIntent_youtuBeUrlWithTrailingPunctuation_cleansUrl() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Check out this video: https://youtu.be/dQw4w9WgXcQ?si=123.",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue("Expected ImportUrl, got $result", result is SousChefIntent.ImportUrl)
        val importIntent = result as SousChefIntent.ImportUrl
        assertEquals("https://youtu.be/dQw4w9WgXcQ?si=123", importIntent.url)
    }

    @Test
    fun parseIntent_wwwUrl_returnsImportUrlIntent() {
        val result = SousChefActionEngine.parseIntent(
            prompt = "Import www.youtube.com/watch?v=dQw4w9WgXcQ for Annette",
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue(result is SousChefIntent.ImportUrl)
        val importIntent = result as SousChefIntent.ImportUrl
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", importIntent.url)
        assertEquals("Annette", importIntent.targetProfile)
    }

    @Test
    fun kitchenAdvice_inquiryAboutBringingYouTubeVideos_explainsCapabilities() {
        val prompt = "I thought you said it could bring in YouTube videos into recipes"
        val intent = SousChefActionEngine.parseIntent(
            prompt = prompt,
            currentActiveProfile = "Louis",
            knownProfiles = listOf("Louis", "Annette")
        )
        assertTrue(intent is SousChefIntent.KitchenAdvice)
        
        // Test advice output
        val advice = SousChefActionEngine.getOfflineAdviceForTesting(prompt)
        assertTrue(advice.contains("Paste any YouTube link"))
        assertTrue(advice.contains("Video"))
    }
}
