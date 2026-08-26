package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupManifest(
    val app: String = "Grandma's Heirloom Cookbook",
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val exportedAtFormatted: String = "",
    val recipeCount: Int = 0,
    val recipes: List<RecipeEntity> = emptyList()
)

object BackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val manifestAdapter = moshi.adapter(BackupManifest::class.java).indent("  ")
    private val recipeListAdapter = moshi.adapter<List<RecipeEntity>>(
        Types.newParameterizedType(List::class.java, RecipeEntity::class.java)
    )

    fun getSuggestedFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        return "Heirloom_Cookbook_Backup_$dateStr.json"
    }

    fun exportToJson(recipes: List<RecipeEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val manifest = BackupManifest(
            app = "Grandma's Heirloom Cookbook",
            schemaVersion = 1,
            exportedAt = System.currentTimeMillis(),
            exportedAtFormatted = dateFormat.format(Date()),
            recipeCount = recipes.size,
            recipes = recipes
        )
        return manifestAdapter.toJson(manifest)
    }

    fun parseBackup(jsonContent: String): Result<BackupManifest> {
        return try {
            val trimmed = jsonContent.trim()
            if (trimmed.startsWith("{")) {
                // Structured manifest format
                val parsed = manifestAdapter.fromJson(trimmed)
                if (parsed != null && parsed.recipes.isNotEmpty()) {
                    Result.success(parsed)
                } else if (parsed != null && parsed.recipes.isEmpty()) {
                    // Try parsing with manual JSON extraction in case of field variations
                    val manual = parseManualJson(trimmed)
                    Result.success(manual)
                } else {
                    Result.failure(IllegalArgumentException("Invalid or empty recipe backup file."))
                }
            } else if (trimmed.startsWith("[")) {
                // Direct array of recipes
                val recipeList = recipeListAdapter.fromJson(trimmed) ?: emptyList()
                val manifest = BackupManifest(
                    recipeCount = recipeList.size,
                    recipes = recipeList
                )
                Result.success(manifest)
            } else {
                Result.failure(IllegalArgumentException("Unsupported backup file format."))
            }
        } catch (e: Exception) {
            // Fallback manual parser
            try {
                val manual = parseManualJson(jsonContent)
                Result.success(manual)
            } catch (fallbackEx: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseManualJson(jsonStr: String): BackupManifest {
        val root = JSONObject(jsonStr)
        val recipesArray = if (root.has("recipes")) {
            root.getJSONArray("recipes")
        } else {
            JSONArray()
        }

        val recipes = mutableListOf<RecipeEntity>()
        for (i in 0 until recipesArray.length()) {
            val obj = recipesArray.getJSONObject(i)
            val title = obj.optString("title", "Untitled Recipe")
            val titleGerman = obj.optString("titleGerman", title)
            val titleEnglish = obj.optString("titleEnglish", title)
            val category = obj.optString("category", "Family Classics")
            val servings = obj.optString("servings", "4-6 servings")
            val prepTime = obj.optInt("prepTimeMinutes", 20)
            val cookTime = obj.optInt("cookTimeMinutes", 30)
            val difficulty = obj.optString("difficulty", "Medium")
            val notes = obj.optString("notes", "")
            val notesGerman = obj.optString("notesGerman", notes)
            val coverTheme = obj.optString("coverTheme", "VINTAGE_LEATHER")
            val isFavorite = obj.optBoolean("isFavorite", false)
            val originStory = obj.optString("originStory", "Family heirloom recipe.")

            // Ingredients
            val ingList = mutableListOf<RecipeIngredient>()
            if (obj.has("ingredients")) {
                val ingArr = obj.getJSONArray("ingredients")
                for (j in 0 until ingArr.length()) {
                    val ingObj = ingArr.getJSONObject(j)
                    ingList.add(
                        RecipeIngredient(
                            name = ingObj.optString("name", "Ingredient"),
                            amount = ingObj.optString("amount", ""),
                            unit = ingObj.optString("unit", ""),
                            nameGerman = if (ingObj.has("nameGerman") && !ingObj.isNull("nameGerman")) ingObj.getString("nameGerman") else null,
                            nameEnglish = if (ingObj.has("nameEnglish") && !ingObj.isNull("nameEnglish")) ingObj.getString("nameEnglish") else null,
                            isOptional = ingObj.optBoolean("isOptional", false),
                            group = if (ingObj.has("group") && !ingObj.isNull("group")) ingObj.getString("group") else null
                        )
                    )
                }
            }

            // Steps
            val stepList = mutableListOf<RecipeStep>()
            if (obj.has("steps")) {
                val stepArr = obj.getJSONArray("steps")
                for (k in 0 until stepArr.length()) {
                    val stepObj = stepArr.getJSONObject(k)
                    val instr = stepObj.optString("instructionEnglish", stepObj.optString("instructionGerman", ""))
                    stepList.add(
                        RecipeStep(
                            stepNumber = stepObj.optInt("stepNumber", k + 1),
                            instructionEnglish = stepObj.optString("instructionEnglish", instr),
                            instructionGerman = stepObj.optString("instructionGerman", instr),
                            timerMinutes = stepObj.optInt("timerMinutes", 0),
                            tip = if (stepObj.has("tip") && !stepObj.isNull("tip")) stepObj.getString("tip") else null
                        )
                    )
                }
            }

            recipes.add(
                RecipeEntity(
                    id = 0,
                    title = title,
                    titleGerman = titleGerman,
                    titleEnglish = titleEnglish,
                    category = category,
                    servings = servings,
                    prepTimeMinutes = prepTime,
                    cookTimeMinutes = cookTime,
                    difficulty = difficulty,
                    ingredients = ingList,
                    steps = stepList,
                    notes = notes,
                    notesGerman = notesGerman,
                    coverTheme = coverTheme,
                    isFavorite = isFavorite,
                    originStory = originStory,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        return BackupManifest(
            app = root.optString("app", "Grandma's Heirloom Cookbook"),
            schemaVersion = root.optInt("schemaVersion", 1),
            exportedAt = root.optLong("exportedAt", System.currentTimeMillis()),
            exportedAtFormatted = root.optString("exportedAtFormatted", ""),
            recipeCount = recipes.size,
            recipes = recipes
        )
    }

    fun writeToUri(context: Context, uri: Uri, json: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createShareableBackupUri(context: Context, json: String): Uri? {
        return try {
            val backupsDir = File(context.cacheDir, "backups")
            if (!backupsDir.exists()) backupsDir.mkdirs()

            val file = File(backupsDir, getSuggestedFileName())
            FileOutputStream(file).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareBackup(context: Context, uri: Uri, recipeCount: Int) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Heirloom Cookbook Backup ($recipeCount Recipes)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Here is my complete Grandma's Heirloom Cookbook backup ($recipeCount recipes). You can restore this into the app anytime!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Save or Send Recipe Backup")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
