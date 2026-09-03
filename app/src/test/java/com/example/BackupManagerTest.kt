package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupManagerTest {

    @Test
    fun testExportAndParseBackup() {
        val sampleIngredients = listOf(
            RecipeIngredient(name = "Flour", amount = "500", unit = "g", nameEnglish = "All-Purpose Flour"),
            RecipeIngredient(name = "Butter", amount = "250", unit = "g", nameEnglish = "Unsalted Butter")
        )
        val sampleSteps = listOf(
            RecipeStep(stepNumber = 1, instructionEnglish = "Preheat oven to 350F", timerMinutes = 0),
            RecipeStep(stepNumber = 2, instructionEnglish = "Mix flour and butter until smooth", timerMinutes = 5)
        )
        val sampleRecipes = listOf(
            RecipeEntity(
                id = 1,
                title = "Oma's Apple Strudel",
                titleGerman = "Apfelstrudel",
                titleEnglish = "Grandma's Apple Strudel",
                category = "Baking & Desserts",
                servings = "8 slices",
                prepTimeMinutes = 30,
                cookTimeMinutes = 45,
                difficulty = "Medium",
                ingredients = sampleIngredients,
                steps = sampleSteps,
                notes = "Always use crisp baking apples.",
                originStory = "From our Munich family kitchen, 1948."
            )
        )

        // Export to JSON
        val exportedJson = BackupManager.exportToJson(sampleRecipes)
        assertTrue(exportedJson.isNotBlank())
        assertTrue(exportedJson.contains("Oma's Apple Strudel"))
        assertTrue(exportedJson.contains("Apfelstrudel"))

        // Parse backup
        val parseResult = BackupManager.parseBackup(exportedJson)
        assertTrue(parseResult.isSuccess)

        val manifest = parseResult.getOrNull()
        assertNotNull(manifest)
        assertEquals(1, manifest?.recipeCount)
        assertEquals("Oma's Apple Strudel", manifest?.recipes?.firstOrNull()?.title)
        assertEquals(2, manifest?.recipes?.firstOrNull()?.ingredients?.size)
        assertEquals(2, manifest?.recipes?.firstOrNull()?.steps?.size)
    }

    @Test
    fun testParseArrayJsonFallback() {
        val rawArrayJson = """
            [
              {
                "title": "Family Pot Roast",
                "titleGerman": "Schmorbraten",
                "category": "Main Dishes",
                "servings": "6 servings",
                "prepTimeMinutes": 20,
                "cookTimeMinutes": 90,
                "difficulty": "Easy",
                "ingredients": [
                  {"name": "Beef Chuck Roast", "amount": "1.5", "unit": "kg"}
                ],
                "steps": [
                  {"stepNumber": 1, "instructionEnglish": "Sear meat on all sides", "timerMinutes": 10}
                ]
              }
            ]
        """.trimIndent()

        val parseResult = BackupManager.parseBackup(rawArrayJson)
        assertTrue(parseResult.isSuccess)
        val manifest = parseResult.getOrNull()
        assertNotNull(manifest)
        assertEquals(1, manifest?.recipeCount)
        assertEquals("Family Pot Roast", manifest?.recipes?.firstOrNull()?.title)
    }

    @Test
    fun testParseChocolateChipCookiesFormats() {
        // Single object with string array ingredients and instructions (like common recipe backups or websites)
        val cookieJson = """
            {
              "name": "Classic Chocolate Chip Cookies",
              "category": "Baking & Desserts",
              "yield": "24 cookies",
              "prepTime": "15 mins",
              "cookTime": "12 mins",
              "recipeIngredient": [
                "2 1/4 cups all-purpose flour",
                "1 tsp baking soda",
                "1 cup unsalted butter, softened",
                "3/4 cup granulated sugar",
                "3/4 cup packed brown sugar",
                "2 large eggs",
                "2 cups semi-sweet chocolate chips"
              ],
              "recipeInstructions": [
                "Preheat oven to 375°F (190°C).",
                "Combine flour and baking soda in small bowl.",
                "Beat butter, granulated sugar, and brown sugar in large mixer bowl until creamy.",
                "Add eggs one at a time, beating well after each addition.",
                "Gradually beat in flour mixture, then stir in chocolate chips.",
                "Drop by rounded tablespoon onto ungreased baking sheets.",
                "Bake for 9 to 11 minutes or until golden brown. Cool on baking sheets for 2 minutes."
              ]
            }
        """.trimIndent()

        val parseResult = BackupManager.parseBackup(cookieJson)
        assertTrue(parseResult.isSuccess)
        val manifest = parseResult.getOrNull()
        assertNotNull(manifest)
        assertEquals(1, manifest?.recipeCount)
        val recipe = manifest?.recipes?.firstOrNull()
        assertNotNull(recipe)
        assertEquals("Classic Chocolate Chip Cookies", recipe?.title)
        assertEquals(7, recipe?.ingredients?.size)
        assertEquals(7, recipe?.steps?.size)
        assertEquals(12, recipe?.cookTimeMinutes)
    }

    @Test
    fun testParseWrappedDataJson() {
        val wrappedJson = """
            {
              "app": "Cookbook",
              "data": [
                {
                  "title": "Grandma's Chocolate Chip Cookies",
                  "ingredients": [
                    {"name": "Chocolate Chips", "amount": "2", "unit": "cups"}
                  ],
                  "steps": [
                    {"instructionEnglish": "Bake 10 minutes", "timerMinutes": 10}
                  ]
                }
              ]
            }
        """.trimIndent()

        val parseResult = BackupManager.parseBackup(wrappedJson)
        assertTrue(parseResult.isSuccess)
        val manifest = parseResult.getOrNull()
        assertNotNull(manifest)
        assertEquals(1, manifest?.recipeCount)
        assertEquals("Grandma's Chocolate Chip Cookies", manifest?.recipes?.firstOrNull()?.title)
    }

    @Test
    fun testExportAndRestoreImagesAndProfile() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val dummyCoverFile = java.io.File(context.cacheDir, "test_cover.jpg").apply {
            writeBytes("FAKE_COVER_IMAGE_DATA_12345".toByteArray())
        }
        val dummyCardFile = java.io.File(context.cacheDir, "test_card.jpg").apply {
            writeBytes("FAKE_CARD_IMAGE_DATA_67890".toByteArray())
        }

        val sampleRecipe = RecipeEntity(
            id = 42,
            title = "Secret Family Apple Tart",
            category = "Baking & Desserts",
            profileName = "Louis",
            imageUri = dummyCoverFile.absolutePath,
            originalCardPhotoUri = dummyCardFile.absolutePath,
            coverPhotoName = "apple_tart_cover",
            isFavorite = true,
            rating = 5,
            ingredients = listOf(RecipeIngredient(name = "Apples", amount = "4", unit = "pcs")),
            steps = listOf(RecipeStep(stepNumber = 1, instructionEnglish = "Slice apples thin"))
        )

        // 1. Export to JSON with Context (bundles Base64 image data)
        val exportedJson = BackupManager.exportToJson(listOf(sampleRecipe), context)
        assertTrue(exportedJson.contains("imageBase64"))
        assertTrue(exportedJson.contains("originalCardPhotoBase64"))
        assertTrue(exportedJson.contains("profileName"))
        assertTrue(exportedJson.contains("Louis"))

        // 2. Parse and Restore (decodes Base64 into real local files)
        val parseResult = BackupManager.parseBackup(exportedJson, context)
        assertTrue(parseResult.isSuccess)

        val restoredRecipe = parseResult.getOrNull()?.recipes?.firstOrNull()
        assertNotNull(restoredRecipe)
        assertEquals("Secret Family Apple Tart", restoredRecipe?.title)
        assertEquals("Louis", restoredRecipe?.profileName)
        assertEquals("apple_tart_cover", restoredRecipe?.coverPhotoName)
        assertTrue(restoredRecipe?.isFavorite == true)

        // Verify cover image file was restored and matches data
        val restoredCoverPath = restoredRecipe?.imageUri
        assertNotNull(restoredCoverPath)
        val restoredCoverFile = java.io.File(restoredCoverPath!!)
        assertTrue(restoredCoverFile.exists())
        assertEquals("FAKE_COVER_IMAGE_DATA_12345", restoredCoverFile.readText())

        // Verify card photo file was restored and matches data
        val restoredCardPath = restoredRecipe.originalCardPhotoUri
        assertNotNull(restoredCardPath)
        val restoredCardFile = java.io.File(restoredCardPath!!)
        assertTrue(restoredCardFile.exists())
        assertEquals("FAKE_CARD_IMAGE_DATA_67890", restoredCardFile.readText())
    }

    @Test
    fun testGrandfatherFatherSonRotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val backupsDir = java.io.File(context.filesDir, "saved_backups").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val baseTime = System.currentTimeMillis()

        // 1. Create 10 daily backups (Son) -> Should prune to 7
        for (i in 1..10) {
            java.io.File(backupsDir, "son_daily_2026090${i}_120000.json").apply {
                writeText("{}", Charsets.UTF_8)
                setLastModified(baseTime + (i * 1000L))
            }
        }

        // 2. Create 6 weekly backups (Father) -> Should prune to 4
        for (i in 1..6) {
            java.io.File(backupsDir, "father_weekly_20260${i}01_120000.json").apply {
                writeText("{}", Charsets.UTF_8)
                setLastModified(baseTime + (i * 1000L))
            }
        }

        // 3. Create 15 monthly backups (Grandfather) -> Should prune to 12
        for (i in 1..15) {
            java.io.File(backupsDir, "grandfather_monthly_2025_${i}_120000.json").apply {
                writeText("{}", Charsets.UTF_8)
                setLastModified(baseTime + (i * 1000L))
            }
        }

        // 4. Create 8 pre-deletion safety backups -> Should prune to 5
        for (i in 1..8) {
            java.io.File(backupsDir, "pre_delete_2026090${i}_120000.json").apply {
                writeText("{}", Charsets.UTF_8)
                setLastModified(baseTime + (i * 1000L))
            }
        }

        // Execute GFS pruning
        BackupManager.pruneGfsBackups(backupsDir)

        val files = backupsDir.listFiles() ?: emptyArray()
        val dailyCount = files.count { it.name.startsWith("son_daily_") }
        val weeklyCount = files.count { it.name.startsWith("father_weekly_") }
        val monthlyCount = files.count { it.name.startsWith("grandfather_monthly_") }
        val safetyCount = files.count { it.name.startsWith("pre_delete_") }

        assertEquals("Son tier should retain exactly 7 daily backups", 7, dailyCount)
        assertEquals("Father tier should retain exactly 4 weekly backups", 4, weeklyCount)
        assertEquals("Grandfather tier should retain exactly 12 monthly backups", 12, monthlyCount)
        assertEquals("Safety tier should retain exactly 5 pre-deletion backups", 5, safetyCount)
    }

    @Test
    fun testOneDriveCloudMockBackupAndRestore() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Simulate a mock cloud storage directory (e.g. OneDrive synced folder)
        val mockCloudStorageDir = java.io.File(context.cacheDir, "mock_onedrive_cloud").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        // Create sample recipes with rich metadata across multiple profiles
        val recipesToBackup = listOf(
            RecipeEntity(
                id = 101,
                title = "Grandma's Cinnamon Rolls",
                titleGerman = "Omas Zimtschnecken",
                category = "Baking & Desserts",
                profileName = "Louis",
                servings = "12 rolls",
                prepTimeMinutes = 30,
                cookTimeMinutes = 25,
                difficulty = "Medium",
                notes = "Best served warm with cream cheese frosting.",
                isFavorite = true,
                rating = 5,
                ingredients = listOf(
                    RecipeIngredient(name = "Flour", amount = "500", unit = "g", group = "Dough"),
                    RecipeIngredient(name = "Cinnamon", amount = "2", unit = "tbsp", group = "Filling")
                ),
                steps = listOf(
                    RecipeStep(stepNumber = 1, instructionEnglish = "Mix yeast and warm milk.", timerMinutes = 10),
                    RecipeStep(stepNumber = 2, instructionEnglish = "Roll dough and sprinkle cinnamon.", timerMinutes = 15)
                )
            ),
            RecipeEntity(
                id = 102,
                title = "Annette's Hearty Beef Stew",
                titleGerman = "Annettes Rindereintopf",
                category = "Soups & Stews",
                profileName = "Annette",
                servings = "6 servings",
                prepTimeMinutes = 20,
                cookTimeMinutes = 90,
                difficulty = "Easy",
                notes = "Slow simmer for tender beef.",
                isFavorite = true,
                rating = 5,
                ingredients = listOf(
                    RecipeIngredient(name = "Beef Chuck", amount = "800", unit = "g", group = "Stew"),
                    RecipeIngredient(name = "Carrots", amount = "4", unit = "pcs", group = "Vegetables")
                ),
                steps = listOf(
                    RecipeStep(stepNumber = 1, instructionEnglish = "Brown the beef cubes in olive oil.", timerMinutes = 8),
                    RecipeStep(stepNumber = 2, instructionEnglish = "Add broth and simmer gently.", timerMinutes = 80)
                )
            )
        )

        // 1. Export to mock OneDrive storage JSON file
        val exportedJson = BackupManager.exportToJson(recipesToBackup, context)
        val mockOneDriveFile = java.io.File(mockCloudStorageDir, "Cookbook_Backup_OneDrive_Sync.json")
        mockOneDriveFile.writeText(exportedJson, Charsets.UTF_8)

        assertTrue(mockOneDriveFile.exists())
        assertTrue(mockOneDriveFile.length() > 0)

        // 2. Read from mock OneDrive storage JSON file
        val fileContentFromOneDrive = mockOneDriveFile.readText(Charsets.UTF_8)
        assertNotNull(fileContentFromOneDrive)

        // 3. Mock Restore
        val restoreResult = BackupManager.parseBackup(fileContentFromOneDrive, context)
        assertTrue("Restore should succeed", restoreResult.isSuccess)

        val restoredManifest = restoreResult.getOrNull()
        assertNotNull(restoredManifest)
        assertEquals(2, restoredManifest?.recipeCount)
        assertEquals(2, restoredManifest?.recipes?.size)

        val restoredRecipe1 = restoredManifest?.recipes?.find { it.id == 101L }
        assertNotNull(restoredRecipe1)
        assertEquals("Grandma's Cinnamon Rolls", restoredRecipe1?.title)
        assertEquals("Louis", restoredRecipe1?.profileName)
        assertEquals("Baking & Desserts", restoredRecipe1?.category)
        assertEquals(2, restoredRecipe1?.ingredients?.size)
        assertEquals("Dough", restoredRecipe1?.ingredients?.first()?.group)
        assertEquals(2, restoredRecipe1?.steps?.size)

        val restoredRecipe2 = restoredManifest?.recipes?.find { it.id == 102L }
        assertNotNull(restoredRecipe2)
        assertEquals("Annette's Hearty Beef Stew", restoredRecipe2?.title)
        assertEquals("Annette", restoredRecipe2?.profileName)
        assertEquals("Soups & Stews", restoredRecipe2?.category)
        assertEquals(2, restoredRecipe2?.ingredients?.size)
        assertEquals("Stew", restoredRecipe2?.ingredients?.first()?.group)
    }
}

