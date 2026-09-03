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
}
