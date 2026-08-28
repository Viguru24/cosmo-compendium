package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.ai.OfflineRecipeParser
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.squareup.moshi.JsonClass
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

@JsonClass(generateAdapter = true)
data class BackupManifest(
    val app: String = "Cookbook",
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val exportedAtFormatted: String = "",
    val recipeCount: Int = 0,
    val recipes: List<RecipeEntity> = emptyList()
)

data class SavedBackupFile(
    val file: File,
    val title: String,
    val timestamp: Long,
    val formattedDate: String,
    val recipeCount: Int,
    val sizeBytes: Long,
    val tag: String = "Backup"
) {
    val displayName: String get() = title
    val formattedSize: String get() = if (sizeBytes < 1024) "$sizeBytes B" else "${sizeBytes / 1024} KB"
}

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
        return "Cookbook_Backup_$dateStr.json"
    }

    fun exportToJson(recipes: List<RecipeEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val manifest = BackupManifest(
            app = "Cookbook",
            schemaVersion = 1,
            exportedAt = System.currentTimeMillis(),
            exportedAtFormatted = dateFormat.format(Date()),
            recipeCount = recipes.size,
            recipes = recipes
        )
        return try {
            manifestAdapter.toJson(manifest)
        } catch (e: Exception) {
            // Fallback manual JSON serializer
            serializeManualJson(manifest)
        }
    }

    private fun serializeManualJson(manifest: BackupManifest): String {
        val root = JSONObject()
        root.put("app", manifest.app)
        root.put("schemaVersion", manifest.schemaVersion)
        root.put("exportedAt", manifest.exportedAt)
        root.put("exportedAtFormatted", manifest.exportedAtFormatted)
        root.put("recipeCount", manifest.recipeCount)

        val arr = JSONArray()
        for (r in manifest.recipes) {
            val robj = JSONObject()
            robj.put("id", r.id)
            robj.put("title", r.title)
            robj.put("titleGerman", r.titleGerman)
            robj.put("titleEnglish", r.titleEnglish)
            robj.put("category", r.category)
            robj.put("servings", r.servings)
            robj.put("prepTimeMinutes", r.prepTimeMinutes)
            robj.put("cookTimeMinutes", r.cookTimeMinutes)
            robj.put("difficulty", r.difficulty)
            robj.put("notes", r.notes)
            robj.put("notesGerman", r.notesGerman)
            robj.put("sourceLanguage", r.sourceLanguage)
            robj.put("imageUri", r.imageUri ?: "")
            robj.put("coverTheme", r.coverTheme)
            robj.put("isFavorite", r.isFavorite)
            robj.put("rating", r.rating)
            robj.put("timesCooked", r.timesCooked)
            robj.put("originStory", r.originStory)
            robj.put("createdAt", r.createdAt)

            val ingArr = JSONArray()
            for (ing in r.ingredients) {
                val ingObj = JSONObject()
                ingObj.put("name", ing.name)
                ingObj.put("amount", ing.amount)
                ingObj.put("unit", ing.unit)
                ingObj.put("nameGerman", ing.nameGerman ?: "")
                ingObj.put("nameEnglish", ing.nameEnglish ?: "")
                ingObj.put("isOptional", ing.isOptional)
                ingObj.put("group", ing.group ?: "")
                ingArr.put(ingObj)
            }
            robj.put("ingredients", ingArr)

            val stepArr = JSONArray()
            for (step in r.steps) {
                val stepObj = JSONObject()
                stepObj.put("stepNumber", step.stepNumber)
                stepObj.put("instructionEnglish", step.instructionEnglish)
                stepObj.put("instructionGerman", step.instructionGerman)
                stepObj.put("timerMinutes", step.timerMinutes)
                stepObj.put("tip", step.tip ?: "")
                stepArr.put(stepObj)
            }
            robj.put("steps", stepArr)

            arr.put(robj)
        }
        root.put("recipes", arr)
        return root.toString(2)
    }

    fun parseBackup(jsonContent: String): Result<BackupManifest> {
        val clean = jsonContent.trim().removePrefix("\uFEFF").trim()
        if (clean.isBlank()) {
            return Result.failure(IllegalArgumentException("Backup content is empty."))
        }

        // 1. Try Moshi first if it starts with standard JSON braces
        try {
            if (clean.startsWith("{")) {
                val parsed = manifestAdapter.fromJson(clean)
                if (parsed != null && parsed.recipes.isNotEmpty()) {
                    return Result.success(parsed)
                }
            } else if (clean.startsWith("[")) {
                val recipeList = recipeListAdapter.fromJson(clean)
                if (!recipeList.isNullOrEmpty()) {
                    return Result.success(
                        BackupManifest(
                            recipeCount = recipeList.size,
                            recipes = recipeList
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Fall through to resilient manual parser
        }

        // 2. Resilient Manual JSON Parser (handles all schemas, JSON-LD, Paprika, string arrays, wrapped objects)
        try {
            if (clean.startsWith("{") || clean.startsWith("[")) {
                val manual = parseManualJson(clean)
                if (manual.recipes.isNotEmpty()) {
                    return Result.success(manual)
                }
            }
        } catch (e: Exception) {
            // Fall through to text parser
        }

        // 3. Fallback: Parse as a plain text or markdown recipe (e.g. pasted recipe text)
        return try {
            val parsedDto = OfflineRecipeParser.parse(clean)
            val enTitle: String = parsedDto.titleEnglish?.takeIf { it.isNotBlank() }
                ?: (parsedDto.titleGerman?.takeIf { it.isNotBlank() } ?: "Restored Recipe")

            val ingList: List<RecipeIngredient> = parsedDto.ingredients?.map { ing ->
                val name: String = ing.nameEnglish?.takeIf { it.isNotBlank() } ?: (ing.nameGerman ?: "Ingredient")
                RecipeIngredient(
                    name = name,
                    amount = ing.amount ?: "",
                    unit = ing.unit ?: "",
                    nameGerman = name,
                    nameEnglish = name,
                    isOptional = ing.isOptional ?: false,
                    group = ing.group
                )
            } ?: emptyList()

            val stepList: List<RecipeStep> = parsedDto.steps?.mapIndexed { index, step ->
                val instr: String = step.instructionEnglish?.takeIf { it.isNotBlank() } ?: (step.instructionGerman ?: "")
                RecipeStep(
                    stepNumber = step.stepNumber ?: (index + 1),
                    instructionEnglish = instr,
                    instructionGerman = instr,
                    timerMinutes = step.timerMinutes ?: 0,
                    tip = step.tip
                )
            } ?: emptyList()

            val recipe = RecipeEntity(
                id = 0,
                title = enTitle,
                titleGerman = enTitle,
                titleEnglish = enTitle,
                category = parsedDto.category ?: "Baking & Desserts",
                servings = parsedDto.servings ?: "4-6 servings",
                prepTimeMinutes = parsedDto.prepTimeMinutes ?: 15,
                cookTimeMinutes = parsedDto.cookTimeMinutes ?: 25,
                difficulty = parsedDto.difficulty ?: "Medium",
                ingredients = ingList,
                steps = stepList,
                notes = parsedDto.notesEnglish ?: "",
                notesGerman = parsedDto.notesEnglish ?: "",
                sourceLanguage = "en",
                coverTheme = if (enTitle.contains("cookie", ignoreCase = true) || enTitle.contains("cake", ignoreCase = true)) "WARM_TERRACOTTA" else "VINTAGE_LEATHER",
                isFavorite = true,
                rating = 5,
                timesCooked = 0,
                originStory = "Restored from recipe text.",
                createdAt = System.currentTimeMillis()
            )

            Result.success(
                BackupManifest(
                    app = "Cookbook",
                    schemaVersion = 1,
                    exportedAt = System.currentTimeMillis(),
                    exportedAtFormatted = "Restored from Recipe Text",
                    recipeCount = 1,
                    recipes = listOf(recipe)
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Unable to restore recipe: ${e.message}"))
        }
    }

    private fun parseManualJson(jsonStr: String): BackupManifest {
        val clean = jsonStr.trim().removePrefix("\uFEFF").trim()
        val recipes = mutableListOf<RecipeEntity>()
        var appName = "Cookbook"
        var formattedDate = ""

        if (clean.startsWith("[")) {
            val arr = JSONArray(clean)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i)
                if (obj != null) {
                    val r = parseSingleRecipeObject(obj)
                    if (r != null) recipes.add(r)
                }
            }
        } else if (clean.startsWith("{")) {
            val root = JSONObject(clean)
            appName = root.optString("app", "Cookbook")
            formattedDate = root.optString("exportedAtFormatted", "")

            // Check various array wrapper keys
            val arrayKeys = listOf("recipes", "items", "data", "cookbook", "results", "recipeList", "@graph", "itemListElement")
            var foundArray = false

            for (k in arrayKeys) {
                val arr = root.optJSONArray(k)
                if (arr != null && arr.length() > 0) {
                    foundArray = true
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val r = parseSingleRecipeObject(obj)
                        if (r != null) recipes.add(r)
                    }
                    if (recipes.isNotEmpty()) break
                }
            }

            if (!foundArray) {
                // Check single wrapped recipe object
                val singleObjKeys = listOf("recipe", "data", "item", "result")
                var foundSingleObj = false
                for (sk in singleObjKeys) {
                    val rObj = root.optJSONObject(sk)
                    if (rObj != null) {
                        val r = parseSingleRecipeObject(rObj)
                        if (r != null) {
                            recipes.add(r)
                            foundSingleObj = true
                            break
                        }
                    }
                }

                // If not found in wrappers, try root itself as a single recipe object
                if (!foundSingleObj) {
                    val r = parseSingleRecipeObject(root)
                    if (r != null) recipes.add(r)
                }
            }
        }

        return BackupManifest(
            app = appName,
            schemaVersion = 1,
            exportedAt = System.currentTimeMillis(),
            exportedAtFormatted = formattedDate,
            recipeCount = recipes.size,
            recipes = recipes
        )
    }

    private fun parseSingleRecipeObject(obj: JSONObject): RecipeEntity? {
        val titleCandidates = listOf(
            obj.optString("title"),
            obj.optString("name"),
            obj.optString("titleEnglish"),
            obj.optString("titleGerman"),
            obj.optString("recipeName"),
            obj.optString("recipe_title"),
            obj.optString("headline"),
            obj.optString("item")
        )
        val rawTitle = titleCandidates.firstOrNull { it.isNotBlank() }

        val hasIngs = obj.has("ingredients") || obj.has("recipeIngredient") || obj.has("ingredientList") || obj.has("recipe_ingredients") || obj.has("items")
        val hasSteps = obj.has("steps") || obj.has("instructions") || obj.has("recipeInstructions") || obj.has("directions") || obj.has("method")

        if (rawTitle.isNullOrBlank() && !hasIngs && !hasSteps) {
            return null
        }

        val displayTitle = rawTitle?.ifBlank { "Restored Recipe" } ?: "Restored Recipe"
        val titleGerman = obj.optString("titleGerman").takeIf { it.isNotBlank() } ?: displayTitle
        val titleEnglish = obj.optString("titleEnglish").takeIf { it.isNotBlank() } ?: displayTitle

        val category = listOf(
            obj.optString("category"),
            obj.optString("recipeCategory"),
            obj.optString("cuisine"),
            obj.optString("type")
        ).firstOrNull { it.isNotBlank() } ?: "Family Classics"

        val servings = listOf(
            obj.optString("servings"),
            obj.optString("recipeYield"),
            obj.optString("yield"),
            obj.optString("portion")
        ).firstOrNull { it.isNotBlank() } ?: "4-6 servings"

        val prepTime = parseTimeMinutes(obj, listOf("prepTimeMinutes", "prep_time_minutes", "prepTime", "prep_time", "prep"), 15)
        val cookTime = parseTimeMinutes(obj, listOf("cookTimeMinutes", "cook_time_minutes", "cookTime", "cook_time", "cook", "totalTime", "total_time"), 25)
        val difficulty = obj.optString("difficulty").takeIf { it.isNotBlank() } ?: "Medium"

        val notes = listOf(
            obj.optString("notes"),
            obj.optString("description"),
            obj.optString("summary"),
            obj.optString("about"),
            obj.optString("notesEnglish")
        ).firstOrNull { it.isNotBlank() } ?: ""
        val notesGerman = obj.optString("notesGerman").takeIf { it.isNotBlank() } ?: notes

        val coverTheme = obj.optString("coverTheme").takeIf { it.isNotBlank() }
            ?: (if (displayTitle.contains("cookie", ignoreCase = true) || displayTitle.contains("cake", ignoreCase = true)) "WARM_TERRACOTTA" else "VINTAGE_LEATHER")

        val isFavorite = obj.optBoolean("isFavorite", true)
        val rating = obj.optInt("rating", 5)
        val timesCooked = obj.optInt("timesCooked", 0)
        val originStory = obj.optString("originStory").takeIf { it.isNotBlank() } ?: "Restored recipe collection."
        val imageUri = obj.optString("imageUri").takeIf { it.isNotBlank() } ?: obj.optString("image").takeIf { it.isNotBlank() }
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

        val ingList = mutableListOf<RecipeIngredient>()
        val ingKey = when {
            obj.has("ingredients") -> "ingredients"
            obj.has("recipeIngredient") -> "recipeIngredient"
            obj.has("ingredientList") -> "ingredientList"
            obj.has("recipe_ingredients") -> "recipe_ingredients"
            obj.has("items") -> "items"
            else -> null
        }

        if (ingKey != null) {
            val ingVal = obj.opt(ingKey)
            if (ingVal is JSONArray) {
                for (j in 0 until ingVal.length()) {
                    val item = ingVal.get(j)
                    if (item is JSONObject) {
                        val iName = listOf(
                            item.optString("name"),
                            item.optString("nameEnglish"),
                            item.optString("nameGerman"),
                            item.optString("ingredient"),
                            item.optString("item"),
                            item.optString("title")
                        ).firstOrNull { it.isNotBlank() } ?: "Ingredient"

                        val amount = listOf(
                            item.optString("amount"),
                            item.optString("quantity"),
                            item.optString("qty"),
                            item.optString("count")
                        ).firstOrNull { it.isNotBlank() } ?: ""

                        val unit = listOf(
                            item.optString("unit"),
                            item.optString("measurement"),
                            item.optString("measure")
                        ).firstOrNull { it.isNotBlank() } ?: ""

                        ingList.add(
                            RecipeIngredient(
                                name = iName,
                                amount = amount,
                                unit = unit,
                                nameGerman = item.optString("nameGerman").takeIf { it.isNotBlank() } ?: iName,
                                nameEnglish = item.optString("nameEnglish").takeIf { it.isNotBlank() } ?: iName,
                                isOptional = item.optBoolean("isOptional", false),
                                group = item.optString("group").takeIf { it.isNotBlank() }
                            )
                        )
                    } else if (item is String && item.isNotBlank()) {
                        ingList.add(parseIngredientFromString(item))
                    }
                }
            } else if (ingVal is String) {
                ingVal.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
                    ingList.add(parseIngredientFromString(line))
                }
            }
        }

        val stepList = mutableListOf<RecipeStep>()
        val stepKey = when {
            obj.has("steps") -> "steps"
            obj.has("instructions") -> "instructions"
            obj.has("recipeInstructions") -> "recipeInstructions"
            obj.has("directions") -> "directions"
            obj.has("method") -> "method"
            obj.has("recipe_steps") -> "recipe_steps"
            else -> null
        }

        if (stepKey != null) {
            val stepVal = obj.opt(stepKey)
            if (stepVal is JSONArray) {
                for (k in 0 until stepVal.length()) {
                    val item = stepVal.get(k)
                    if (item is JSONObject) {
                        val instr = listOf(
                            item.optString("instructionEnglish"),
                            item.optString("instructionGerman"),
                            item.optString("text"),
                            item.optString("instruction"),
                            item.optString("direction"),
                            item.optString("step"),
                            item.optString("description")
                        ).firstOrNull { it.isNotBlank() } ?: ""

                        val timer = item.optInt("timerMinutes", extractTimerMinutes(instr))

                        stepList.add(
                            RecipeStep(
                                stepNumber = item.optInt("stepNumber", k + 1),
                                instructionEnglish = instr,
                                instructionGerman = instr,
                                timerMinutes = timer,
                                tip = item.optString("tip").takeIf { it.isNotBlank() }
                            )
                        )
                    } else if (item is String && item.isNotBlank()) {
                        val cleaned = item.replace(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?)\\s*"), "").trim()
                        stepList.add(
                            RecipeStep(
                                stepNumber = k + 1,
                                instructionEnglish = cleaned,
                                instructionGerman = cleaned,
                                timerMinutes = extractTimerMinutes(cleaned),
                                tip = null
                            )
                        )
                    }
                }
            } else if (stepVal is String) {
                stepVal.lines().map { it.trim() }.filter { it.isNotBlank() }.forEachIndexed { idx, line ->
                    val cleaned = line.replace(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?)\\s*"), "").trim()
                    stepList.add(
                        RecipeStep(
                            stepNumber = idx + 1,
                            instructionEnglish = cleaned,
                            instructionGerman = cleaned,
                            timerMinutes = extractTimerMinutes(cleaned)
                        )
                    )
                }
            }
        }

        return RecipeEntity(
            id = 0,
            title = displayTitle,
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
            sourceLanguage = "en",
            imageUri = imageUri,
            coverTheme = coverTheme,
            isFavorite = isFavorite,
            rating = rating,
            timesCooked = timesCooked,
            originStory = originStory,
            createdAt = createdAt
        )
    }

    private fun parseTimeMinutes(obj: JSONObject, keys: List<String>, defaultVal: Int): Int {
        for (key in keys) {
            if (obj.has(key)) {
                val num = obj.optInt(key, -1)
                if (num >= 0) return num

                val str = obj.optString(key, "")
                if (str.isNotBlank()) {
                    val isoMatch = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?", RegexOption.IGNORE_CASE).find(str)
                    if (isoMatch != null) {
                        val hrs = isoMatch.groupValues[1].toIntOrNull() ?: 0
                        val mins = isoMatch.groupValues[2].toIntOrNull() ?: 0
                        if (hrs > 0 || mins > 0) return (hrs * 60) + mins
                    }
                    val minMatch = Regex("(\\d+)\\s*(?:min|minute)", RegexOption.IGNORE_CASE).find(str)
                    if (minMatch != null) {
                        return minMatch.groupValues[1].toIntOrNull() ?: defaultVal
                    }
                    val rawNum = str.filter { it.isDigit() }.toIntOrNull()
                    if (rawNum != null && rawNum > 0) return rawNum
                }
            }
        }
        return defaultVal
    }

    private fun parseIngredientFromString(line: String): RecipeIngredient {
        val cleaned = line.replace(Regex("^[-*•]\\s*"), "").trim()
        val match = Regex("^([0-9/.,\\s½¼¾⅓⅔]+)?\\s*(cups?|tbsps?|tsps?|tablespoons?|teaspoons?|grams?|g|kg|ml|l|liters?|oz|ounces?|lbs?|pounds?|pinch|prise|pieces?|stk|can|jars?|packets?|pck\\.?)?\\s*(?:of\\s+)?(.*)$", RegexOption.IGNORE_CASE).find(cleaned)

        return if (match != null) {
            val amt = match.groupValues[1].trim()
            val unit = match.groupValues[2].trim()
            val name = match.groupValues[3].trim().ifBlank { cleaned }
            RecipeIngredient(
                name = name,
                amount = amt,
                unit = unit,
                nameEnglish = name,
                nameGerman = name
            )
        } else {
            RecipeIngredient(
                name = cleaned,
                amount = "",
                unit = "",
                nameEnglish = cleaned,
                nameGerman = cleaned
            )
        }
    }

    private fun extractTimerMinutes(text: String): Int {
        val match = Regex("(\\d+)\\s*(?:minutes?|mins?)", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
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
                val bytes = inputStream.readBytes()
                if (bytes.isEmpty()) return null
                var str = String(bytes, Charsets.UTF_8)
                if (str.startsWith("\uFEFF")) {
                    str = str.substring(1)
                }
                if (str.contains("\u0000")) {
                    str = try {
                        String(bytes, Charsets.UTF_16)
                    } catch (e: Exception) {
                        str
                    }
                }
                str.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveLocalSnapshot(context: Context, recipes: List<RecipeEntity>): Boolean {
        return try {
            val file = File(context.filesDir, "cookbook_local_snapshot.json")
            val json = exportToJson(recipes)
            file.writeText(json, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getLocalSnapshot(context: Context): String? {
        return try {
            val file = File(context.filesDir, "cookbook_local_snapshot.json")
            if (file.exists() && file.length() > 0) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
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
            putExtra(Intent.EXTRA_SUBJECT, "Cookbook Backup ($recipeCount Recipes)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Here is my complete recipe backup ($recipeCount recipes). You can restore this into the app anytime!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Save or Send Recipe Backup")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun createLocalBackup(context: Context, recipes: List<RecipeEntity>, tag: String = "Manual Backup"): SavedBackupFile? {
        if (recipes.isEmpty()) return null
        return try {
            val backupsDir = File(context.filesDir, "saved_backups")
            if (!backupsDir.exists()) backupsDir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val fileFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val displayFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            val prefix = when {
                tag.contains("pre_delete", ignoreCase = true) || tag.contains("safety", ignoreCase = true) -> "pre_delete_"
                tag.contains("weekly", ignoreCase = true) -> "weekly_"
                else -> "manual_"
            }
            val fileName = "${prefix}${fileFormat.format(Date(timestamp))}.json"
            val file = File(backupsDir, fileName)

            val json = exportToJson(recipes)
            file.writeText(json, Charsets.UTF_8)

            // Also keep working snapshot
            saveLocalSnapshot(context, recipes)

            // Retain up to 10 latest backups
            val all = backupsDir.listFiles { _, name -> name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() } ?: emptyList()
            if (all.size > 10) {
                all.drop(10).forEach { it.delete() }
            }

            SavedBackupFile(
                file = file,
                title = tag,
                timestamp = timestamp,
                formattedDate = displayFormat.format(Date(timestamp)),
                recipeCount = recipes.size,
                sizeBytes = file.length(),
                tag = tag
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun performWeeklyBackupIfDue(context: Context, recipes: List<RecipeEntity>): Boolean {
        if (recipes.isEmpty()) return false
        val prefs = context.getSharedPreferences("heirloom_recipe_prefs", Context.MODE_PRIVATE)
        val lastWeeklyMillis = prefs.getLong("pref_last_weekly_backup_timestamp", 0L)
        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7L * 24 * 60 * 60 * 1000

        if (now - lastWeeklyMillis >= sevenDaysMillis) {
            val created = createLocalBackup(context, recipes, "Weekly Auto-Backup")
            if (created != null) {
                prefs.edit().putLong("pref_last_weekly_backup_timestamp", now).apply()
                return true
            }
        }
        return false
    }

    fun listAllLocalBackups(context: Context): List<SavedBackupFile> {
        val backupsDir = File(context.filesDir, "saved_backups")
        if (!backupsDir.exists()) return emptyList()

        val files = backupsDir.listFiles { _, name -> name.endsWith(".json") } ?: return emptyList()
        val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

        return files.mapNotNull { file ->
            try {
                val content = file.readText(Charsets.UTF_8)
                val parseRes = parseBackup(content)
                val count = parseRes.getOrNull()?.recipeCount ?: 0
                val tag = when {
                    file.name.startsWith("pre_delete_") -> "Pre-Deletion Safety Backup"
                    file.name.startsWith("weekly_") -> "Weekly Auto-Backup"
                    else -> "Saved Backup"
                }

                SavedBackupFile(
                    file = file,
                    title = tag,
                    timestamp = file.lastModified(),
                    formattedDate = dateFormat.format(Date(file.lastModified())),
                    recipeCount = count,
                    sizeBytes = file.length(),
                    tag = tag
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }
    }

    fun deleteBackupFile(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    fun readBackupFileContent(file: File): String? {
        return try {
            if (file.exists() && file.length() > 0) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

