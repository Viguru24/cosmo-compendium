package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.util.AppLogger
import com.example.data.local.RecipeDao
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import com.example.data.network.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class SyncResult {
    data class Success(val pushedCount: Int, val pulledCount: Int, val timestamp: Long) : SyncResult()
    data class Error(val message: String, val throwable: Throwable? = null) : SyncResult()
    data class Aborted(val reason: String) : SyncResult()
}

class SyncManager(
    private val context: Context,
    private val recipeDao: RecipeDao,
    private val syncConfig: SyncConfig
) {
    private val tag = "SyncManager"
    private val tombstoneManager = com.example.data.local.TombstoneManager(context)

    private val httpClient = NetworkModule.okHttpClient.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncStatus = MutableStateFlow<String?>(syncConfig.lastSyncStatusMessage.ifBlank { null })
    val lastSyncStatus: StateFlow<String?> = _lastSyncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(syncConfig.lastSyncTimestamp)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private var connectivityCallback: ConnectivityManager.NetworkCallback? = null

    init {
        setupWifiAutoSync()
    }

    /**
     * Test connection to the live FastAPI/SQLite VPS backend
     * Sends GET {Server_URL}/api/health with header x-sync-token: {Token}
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val serverUrl = syncConfig.getCleanServerUrl()
        if (serverUrl.isBlank()) {
            return@withContext Result.failure(Exception("Server URL is empty. Please enter your server address."))
        }

        val healthUrl = "$serverUrl/api/health"
        val requestBuilder = Request.Builder()
            .url(healthUrl)
            .get()

        if (syncConfig.syncToken.isNotBlank()) {
            requestBuilder.addHeader("x-sync-token", syncConfig.syncToken)
            requestBuilder.addHeader("Authorization", "Bearer ${syncConfig.syncToken}")
        }

        try {
            val response = httpClient.newCall(requestBuilder.build()).execute()
            response.use { res ->
                if (res.isSuccessful) {
                    val body = res.body?.string() ?: ""
                    var info = "Connected (Server Online)"
                    try {
                        val json = JSONObject(body)
                        val activeRecipes = json.optInt("activeRecipes", -1)
                        val serviceName = json.optString("service", "")
                        if (activeRecipes >= 0) {
                            info = "Connected ($serviceName • $activeRecipes remote recipes)"
                        } else if (serviceName.isNotBlank()) {
                            info = "Connected ($serviceName Online)"
                        }
                    } catch (_: Exception) {}
                    Result.success(info)
                } else if (res.code == 401 || res.code == 403) {
                    Result.failure(Exception("Authentication Failed: Invalid Sync Secret Token (HTTP ${res.code})"))
                } else {
                    Result.failure(Exception("Server responded with HTTP ${res.code}: ${res.message}"))
                }
            }
        } catch (e: Exception) {
            AppLogger.e(tag, "Health check failed: ${e.message}", e)
            val cleanMsg = when {
                e.message?.contains("Failed to connect") == true || e.message?.contains("Unable to resolve host") == true ->
                    "Cannot reach server at $serverUrl. Check your network or URL."
                e.message?.contains("timeout") == true -> "Connection timed out. Server may be offline."
                e.message?.contains("CLEARTEXT") == true -> "HTTP cleartext traffic blocked. Use HTTPS or configure network security."
                else -> e.message ?: "Network error"
            }
            Result.failure(Exception("Connection Failed: $cleanMsg"))
        }
    }

    /**
     * Executes the bidirectional delta sync process with the FastAPI VPS backend
     */
    suspend fun performSync(): SyncResult = withContext(Dispatchers.IO) {
        if (!syncConfig.isSyncEnabled) {
            return@withContext SyncResult.Aborted("Sync is disabled in settings.")
        }

        val serverUrl = syncConfig.getCleanServerUrl()
        if (serverUrl.isBlank()) {
            return@withContext SyncResult.Aborted("Server URL is empty.")
        }

        if (_isSyncing.value) {
            return@withContext SyncResult.Aborted("Sync already in progress.")
        }

        _isSyncing.value = true
        _lastSyncStatus.value = "Connecting to sync server..."

        try {
            // Step 1: Query pending local changes
            val pendingEntities = recipeDao.getPendingSyncRecipes()
            var pushedCount = 0

            // Step 2: Upload local images if any are pending
            for (entity in pendingEntities) {
                if (!entity.imageUri.isNullOrBlank() && !entity.isDeleted) {
                    val uploadedFilename = uploadImageIfNeeded(serverUrl, entity)
                    if (uploadedFilename != null && entity.coverPhotoName != uploadedFilename) {
                        recipeDao.updateRecipe(entity.copy(coverPhotoName = uploadedFilename))
                    }
                }
            }

            // Refresh pending entities in case coverPhotoName was updated
            val readyPendingEntities = recipeDao.getPendingSyncRecipes()

            // Step 3: Prepare push payload matching FastAPI backend specification
            val recipesJsonArr = JSONArray()
            for (entity in readyPendingEntities) {
                val coverName = entity.coverPhotoName ?: entity.imageUri?.let { path ->
                    try {
                        val f = File(path)
                        if (f.exists()) f.name else null
                    } catch (_: Exception) { null }
                }

                val recipeObj = JSONObject().apply {
                    put("id", "recipe_${entity.id}")
                    put("title", entity.title)
                    put("titleGerman", entity.titleGerman)
                    put("titleEnglish", entity.titleEnglish)
                    put("category", entity.category)
                    put("prepTime", entity.prepTimeMinutes)
                    put("cookTime", entity.cookTimeMinutes)
                    put("prepTimeMinutes", entity.prepTimeMinutes)
                    put("cookTimeMinutes", entity.cookTimeMinutes)

                    // Extract servings numeric count or string
                    val numericServings = entity.servings.filter { it.isDigit() }.toIntOrNull() ?: 4
                    put("servings", numericServings)
                    put("servingsText", entity.servings)

                    put("difficulty", entity.difficulty)
                    put("coverPhotoName", coverName ?: "")
                    put("notes", entity.notes)
                    put("notesGerman", entity.notesGerman)
                    put("originStory", entity.originStory)
                    put("coverTheme", entity.coverTheme)
                    put("isFavorite", entity.isFavorite)
                    put("rating", entity.rating)
                    put("timesCooked", entity.timesCooked)
                    put("isDeleted", entity.isDeleted)
                    put("updatedAt", entity.updatedAt)
                    put("profileName", entity.profileName)

                    // ingredientsJson string serialization
                    val ingArr = JSONArray()
                    for (ing in entity.ingredients) {
                        val ingObj = JSONObject().apply {
                            put("name", ing.name)
                            put("amount", ing.amount)
                            put("unit", ing.unit)
                            put("nameGerman", ing.nameGerman ?: "")
                            put("nameEnglish", ing.nameEnglish ?: "")
                            put("isOptional", ing.isOptional)
                            put("group", ing.group ?: "")
                        }
                        ingArr.put(ingObj)
                    }
                    put("ingredientsJson", ingArr.toString())

                    // stepsJson string serialization
                    val stepArr = JSONArray()
                    for (step in entity.steps) {
                        val stepObj = JSONObject().apply {
                            put("stepNumber", step.stepNumber)
                            put("instructionEnglish", step.instructionEnglish)
                            put("instructionGerman", step.instructionGerman)
                            put("timerMinutes", step.timerMinutes)
                            put("tip", step.tip ?: "")
                        }
                        stepArr.put(stepObj)
                    }
                    put("stepsJson", stepArr.toString())
                }
                recipesJsonArr.put(recipeObj)
            }

            val payloadJson = JSONObject().apply {
                put("lastSyncTimestamp", syncConfig.lastSyncTimestamp)
                put("recipes", recipesJsonArr)
                put("clientTimestamp", System.currentTimeMillis())
            }.toString()

            _lastSyncStatus.value = "Synchronizing with Cloud Hub..."

            // Send sync request to POST {Server_URL}/api/recipes/sync
            val syncRequest = Request.Builder()
                .url("$serverUrl/api/recipes/sync")
                .post(payloadJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .apply {
                    if (syncConfig.syncToken.isNotBlank()) {
                        addHeader("x-sync-token", syncConfig.syncToken)
                        addHeader("Authorization", "Bearer ${syncConfig.syncToken}")
                    }
                }
                .build()

            val response = httpClient.newCall(syncRequest).execute()
            if (!response.isSuccessful) {
                val errorMsg = "Server sync failed with HTTP ${response.code}: ${response.message}"
                _lastSyncStatus.value = errorMsg
                syncConfig.lastSyncStatusMessage = errorMsg
                return@withContext SyncResult.Error(errorMsg)
            }

            val responseBody = response.body?.string() ?: "{}"
            val responseJson = JSONObject(responseBody)

            val serverTimestamp = responseJson.optLong("serverTimestamp", System.currentTimeMillis())
            val remoteChangesArr = responseJson.optJSONArray("remoteChanges")
                ?: responseJson.optJSONArray("recipes")
                ?: JSONArray()

            var pulledCount = 0

            // Step 4: Process remote changes received from VPS backend
            for (i in 0 until remoteChangesArr.length()) {
                val itemObj = remoteChangesArr.optJSONObject(i) ?: continue
                val remoteTitle = itemObj.optString("title", "").trim()
                if (remoteTitle.isBlank()) continue

                val isDeleted = itemObj.optBoolean("isDeleted", false)
                val updatedAt = itemObj.optLong("updatedAt", serverTimestamp)

                // Reject zombie recipes that were deleted by the user on this device
                if (tombstoneManager.isTombstoned(remoteTitle, updatedAt)) {
                    AppLogger.d(tag, "Rejecting tombstoned deleted recipe '$remoteTitle' from server.")
                    val local = recipeDao.getRecipeByTitle(remoteTitle)
                    if (local != null) {
                        recipeDao.hardDeleteRecipe(local.id)
                    }
                    continue
                }

                if (isDeleted) {
                    // Remote recipe was deleted
                    val local = recipeDao.getRecipeByTitle(remoteTitle)
                    if (local != null) {
                        recipeDao.hardDeleteRecipe(local.id)
                        pulledCount++
                    }
                } else {
                    val coverPhotoName = itemObj.optString("coverPhotoName").ifBlank { null }
                    var localImageUri: String? = null
                    if (!coverPhotoName.isNullOrBlank()) {
                        localImageUri = downloadCoverPhotoIfNeeded(serverUrl, coverPhotoName)
                    }

                    val ingredients = parseIngredientsJson(itemObj)
                    val steps = parseStepsJson(itemObj)

                    val prepTime = itemObj.optInt("prepTime", itemObj.optInt("prepTimeMinutes", 20))
                    val cookTime = itemObj.optInt("cookTime", itemObj.optInt("cookTimeMinutes", 40))
                    val servingsStr = itemObj.optString("servingsText").ifBlank {
                        val servingsInt = itemObj.optInt("servings", 4)
                        "$servingsInt servings"
                    }
                    val category = itemObj.optString("category", "Family Classics")
                    val difficulty = itemObj.optString("difficulty", "Medium")
                    val notes = itemObj.optString("notes", "")
                    val notesGerman = itemObj.optString("notesGerman", "")
                    val originStory = itemObj.optString("originStory", "")
                    val coverTheme = itemObj.optString("coverTheme", "VINTAGE_LEATHER")
                    val titleGerman = itemObj.optString("titleGerman", "")
                    val titleEnglish = itemObj.optString("titleEnglish", remoteTitle)
                    val isFavorite = itemObj.optBoolean("isFavorite", false)
                    val rating = itemObj.optInt("rating", 5)
                    val timesCooked = itemObj.optInt("timesCooked", 0)
                    val profileName = itemObj.optString("profileName").ifBlank {
                        itemObj.optString("profile_name").ifBlank { "Louis" }
                    }
                    val createdAt = itemObj.optLong("createdAt", updatedAt)

                    val existing = recipeDao.getRecipeByTitle(remoteTitle)
                    if (existing != null) {
                        // Strict Last-Write-Wins: Do NOT overwrite newer local edits with stale remote data
                        if (existing.updatedAt > updatedAt) {
                            AppLogger.d(tag, "Local recipe '$remoteTitle' is newer (${existing.updatedAt} > $updatedAt). Skipping stale remote update.")
                            continue
                        }
                        val merged = existing.copy(
                            titleGerman = titleGerman.ifBlank { existing.titleGerman },
                            titleEnglish = titleEnglish.ifBlank { existing.titleEnglish },
                            category = category,
                            servings = servingsStr,
                            prepTimeMinutes = prepTime,
                            cookTimeMinutes = cookTime,
                            difficulty = difficulty,
                            ingredients = if (ingredients.isNotEmpty()) ingredients else existing.ingredients,
                            steps = if (steps.isNotEmpty()) steps else existing.steps,
                            notes = notes.ifBlank { existing.notes },
                            notesGerman = notesGerman.ifBlank { existing.notesGerman },
                            coverTheme = coverTheme,
                            imageUri = localImageUri ?: (if (coverPhotoName != null && coverPhotoName.isNotBlank()) File(context.filesDir, coverPhotoName).absolutePath else existing.imageUri),
                            coverPhotoName = coverPhotoName ?: existing.coverPhotoName,
                            isFavorite = if (itemObj.has("isFavorite")) isFavorite else existing.isFavorite,
                            rating = if (itemObj.has("rating")) rating else existing.rating,
                            timesCooked = maxOf(timesCooked, existing.timesCooked),
                            originStory = originStory.ifBlank { existing.originStory },
                            profileName = profileName,
                            updatedAt = updatedAt,
                            isDeleted = false,
                            syncStatus = "SYNCED"
                        )
                        recipeDao.updateRecipe(merged)
                    } else {
                        val newEntity = RecipeEntity(
                            id = 0,
                            title = remoteTitle,
                            titleGerman = titleGerman,
                            titleEnglish = titleEnglish,
                            category = category,
                            servings = servingsStr,
                            prepTimeMinutes = prepTime,
                            cookTimeMinutes = cookTime,
                            difficulty = difficulty,
                            ingredients = ingredients,
                            steps = steps,
                            notes = notes,
                            notesGerman = notesGerman,
                            sourceLanguage = "both",
                            coverTheme = coverTheme,
                            imageUri = localImageUri,
                            coverPhotoName = coverPhotoName,
                            isFavorite = isFavorite,
                            rating = rating,
                            timesCooked = timesCooked,
                            originStory = originStory,
                            profileName = profileName,
                            createdAt = createdAt,
                            updatedAt = updatedAt,
                            isDeleted = false,
                            syncStatus = "SYNCED"
                        )
                        recipeDao.insertRecipe(newEntity)
                    }
                    pulledCount++
                }
            }

            // Step 5: Mark local pending items as SYNCED
            if (readyPendingEntities.isNotEmpty()) {
                val syncedIds = readyPendingEntities.map { it.id }
                recipeDao.markRecipesSynced(syncedIds)
                pushedCount = readyPendingEntities.size
            }

            // Save sync timestamps
            syncConfig.lastSyncTimestamp = serverTimestamp
            _lastSyncTimestamp.value = serverTimestamp

            val successMsg = "Synced: $pushedCount uploaded, $pulledCount updated"
            _lastSyncStatus.value = successMsg
            syncConfig.lastSyncStatusMessage = successMsg
            SyncResult.Success(pushedCount, pulledCount, serverTimestamp)
        } catch (e: Exception) {
            AppLogger.e(tag, "Sync failed: ${e.message}", e)
            val friendlyMsg = "Offline - changes saved locally and will sync when reconnected"
            _lastSyncStatus.value = friendlyMsg
            syncConfig.lastSyncStatusMessage = friendlyMsg
            SyncResult.Error(friendlyMsg, e)
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Uploads local cover photo to POST /api/recipes/images with multipart parameter "file"
     */
    private fun uploadImageIfNeeded(serverUrl: String, entity: RecipeEntity): String? {
        try {
            val uri = entity.imageUri ?: return null
            val file = File(uri)
            if (!file.exists() || file.length() <= 0) return null

            val fileName = entity.coverPhotoName?.ifBlank { null }
                ?: "recipe_${entity.id}_cover.jpg"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("$serverUrl/api/recipes/images")
                .post(requestBody)
                .apply {
                    if (syncConfig.syncToken.isNotBlank()) {
                        addHeader("x-sync-token", syncConfig.syncToken)
                        addHeader("Authorization", "Bearer ${syncConfig.syncToken}")
                    }
                }
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    val serverFilename = json.optString("filename", fileName)
                    response.close()
                    return serverFilename
                } catch (_: Exception) {
                    response.close()
                    return fileName
                }
            }
            response.close()
        } catch (e: Exception) {
            AppLogger.w(tag, "Failed to upload image for recipe ${entity.title}: ${e.message}")
        }
        return null
    }

    /**
     * Downloads remote cover photo from GET /api/recipes/images/{filename}
     */
    private fun downloadCoverPhotoIfNeeded(serverUrl: String, coverPhotoName: String): String? {
        try {
            val localFile = File(context.filesDir, coverPhotoName)
            if (localFile.exists() && localFile.length() > 0) {
                return localFile.absolutePath
            }

            val request = Request.Builder()
                .url("$serverUrl/api/recipes/images/$coverPhotoName")
                .get()
                .apply {
                    if (syncConfig.syncToken.isNotBlank()) {
                        addHeader("x-sync-token", syncConfig.syncToken)
                        addHeader("Authorization", "Bearer ${syncConfig.syncToken}")
                    }
                }
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    FileOutputStream(localFile).use { it.write(bytes) }
                    return localFile.absolutePath
                }
            }
            response.close()
        } catch (e: Exception) {
            AppLogger.w(tag, "Failed to download cover photo $coverPhotoName: ${e.message}")
        }
        return null
    }

    private fun parseIngredientsJson(itemObj: JSONObject): List<RecipeIngredient> {
        val list = mutableListOf<RecipeIngredient>()
        val ingJsonStr = itemObj.optString("ingredientsJson", "")
        val jsonArr = if (ingJsonStr.isNotBlank()) {
            try { JSONArray(ingJsonStr) } catch (_: Exception) { null }
        } else {
            itemObj.optJSONArray("ingredients")
        }
        if (jsonArr != null) {
            for (i in 0 until jsonArr.length()) {
                val ingObj = jsonArr.optJSONObject(i) ?: continue
                list.add(
                    RecipeIngredient(
                        name = ingObj.optString("name", ""),
                        amount = ingObj.optString("amount", ""),
                        unit = ingObj.optString("unit", ""),
                        nameGerman = ingObj.optString("nameGerman", "").ifBlank { null },
                        nameEnglish = ingObj.optString("nameEnglish", "").ifBlank { null },
                        isOptional = ingObj.optBoolean("isOptional", false),
                        group = ingObj.optString("group", "").ifBlank { null }
                    )
                )
            }
        }
        return list
    }

    private fun parseStepsJson(itemObj: JSONObject): List<RecipeStep> {
        val list = mutableListOf<RecipeStep>()
        val stepsJsonStr = itemObj.optString("stepsJson", "")
        val jsonArr = if (stepsJsonStr.isNotBlank()) {
            try { JSONArray(stepsJsonStr) } catch (_: Exception) { null }
        } else {
            itemObj.optJSONArray("steps")
        }
        if (jsonArr != null) {
            for (i in 0 until jsonArr.length()) {
                val stepObj = jsonArr.optJSONObject(i)
                if (stepObj != null) {
                    val instrEn = stepObj.optString("instructionEnglish", stepObj.optString("instruction", stepObj.optString("text", "")))
                    val instrDe = stepObj.optString("instructionGerman", instrEn)
                    list.add(
                        RecipeStep(
                            stepNumber = stepObj.optInt("stepNumber", i + 1),
                            instructionEnglish = instrEn,
                            instructionGerman = instrDe,
                            timerMinutes = stepObj.optInt("timerMinutes", 0),
                            tip = stepObj.optString("tip", "").ifBlank { null }
                        )
                    )
                } else {
                    val str = jsonArr.optString(i, "").trim()
                    if (str.isNotBlank()) {
                        list.add(
                            RecipeStep(
                                stepNumber = i + 1,
                                instructionEnglish = str,
                                instructionGerman = str,
                                timerMinutes = 0
                            )
                        )
                    }
                }
            }
        }
        return list
    }

    /**
     * Initializes network connectivity listener to automatically sync when WiFi reconnects.
     */
    fun setupWifiAutoSync() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (syncConfig.isSyncEnabled && syncConfig.autoSyncWifi && !_isSyncing.value) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                performSync()
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
            connectivityManager.registerNetworkCallback(request, connectivityCallback!!)
        } catch (e: Exception) {
            AppLogger.w(tag, "Could not register wifi sync listener: ${e.message}")
        }
    }

    /**
     * Triggers an immediate, asynchronous background delta sync on Dispatchers.IO.
     * Silent and non-blocking to prevent any UI stutter or frame drop.
     */
    fun triggerImmediateSync() {
        if (!syncConfig.isSyncEnabled || syncConfig.getCleanServerUrl().isBlank()) return
        if (_isSyncing.value) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                performSync()
            } catch (e: Exception) {
                AppLogger.w(tag, "Immediate auto-sync encountered non-fatal error: ${e.message}")
            }
        }
    }
}
