package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupManifest
import com.example.data.backup.SavedBackupFile
import com.example.data.local.AppDatabase
import com.example.data.local.RecipeEntity
import com.example.data.local.ShoppingItemEntity
import com.example.data.model.CoverTheme
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.data.repository.RecipeRepository
import com.example.ui.util.getDisplayTitle
import com.example.ui.util.RecipeImageClassifier
import com.example.ui.util.RecipeImageType
import com.example.ui.util.RecipePhotoStats
import com.example.ui.util.BatchCoverFilter
import com.example.util.pdf.RecipePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository
    private val prefs = application.getSharedPreferences("heirloom_recipe_prefs", android.content.Context.MODE_PRIVATE)

    init {
        val db = AppDatabase.getInstance(application)
        repository = RecipeRepository(application, db.recipeDao(), db.shoppingDao())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hasSeeded = prefs.getBoolean("pref_has_seeded_initial_recipes", false)
                if (!hasSeeded && repository.getRecipeCount() == 0) {
                    repository.restoreDefaultRecipes(replaceExisting = false)
                    prefs.edit().putBoolean("pref_has_seeded_initial_recipes", true).apply()
                } else {
                    repository.deduplicateCollection()
                }
                // Automatic weekly backup if enabled
                if (autoWeeklyBackupEnabled.value) {
                    val all = repository.getAllRecipesDirect()
                    BackupManager.performWeeklyBackupIfDue(application, all)
                }
                refreshSavedBackups()
                // Instant pull delta sync on app launch if sync is enabled
                syncManager?.triggerImmediateSync()
            } catch (e: Exception) {
                // Ignore initialization errors
            }
        }
    }

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val onlyFavorites = MutableStateFlow(false)

    // Category Management
    val defaultCategories = listOf(
        "Baking & Desserts",
        "Main Dishes",
        "Soups & Stews",
        "Salads & Starters",
        "Holiday & Traditions",
        "Family Classics"
    )

    private fun loadCategoriesFromPrefs(): List<String> {
        val stored = prefs.getString("pref_custom_categories", null) ?: return defaultCategories
        return try {
            val list = stored.split("|||").map { it.trim() }.filter { it.isNotBlank() }
            if (list.isNotEmpty()) list else defaultCategories
        } catch (e: Exception) {
            defaultCategories
        }
    }

    val categories = MutableStateFlow<List<String>>(loadCategoriesFromPrefs())

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !categories.value.any { it.equals(trimmed, ignoreCase = true) }) {
            val updated = categories.value + trimmed
            categories.value = updated
            saveCategories(updated)
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || oldName.equals(trimmed, ignoreCase = true)) return
        val updated = categories.value.map { if (it.equals(oldName, ignoreCase = true)) trimmed else it }
        categories.value = updated
        saveCategories(updated)
        if (selectedCategory.value.equals(oldName, ignoreCase = true)) {
            selectedCategory.value = trimmed
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategoryName(oldName, trimmed)
        }
    }

    fun deleteCategory(name: String) {
        if (categories.value.size <= 1) return
        val updated = categories.value.filterNot { it.equals(name, ignoreCase = true) }
        categories.value = updated
        saveCategories(updated)
        if (selectedCategory.value.equals(name, ignoreCase = true)) {
            selectedCategory.value = "All"
        }
    }

    private fun saveCategories(list: List<String>) {
        prefs.edit().putString("pref_custom_categories", list.joinToString("|||")).apply()
    }
    
    // Persistent user preferences
    val languageMode = MutableStateFlow(
        try {
            LanguageMode.valueOf(prefs.getString("pref_language_mode", LanguageMode.ENGLISH.name) ?: LanguageMode.ENGLISH.name)
        } catch (e: Exception) {
            LanguageMode.ENGLISH
        }
    )
    
    val unitSystem = MutableStateFlow(
        try {
            UnitSystem.valueOf(prefs.getString("pref_unit_system", UnitSystem.CUPS_US.name) ?: UnitSystem.CUPS_US.name)
        } catch (e: Exception) {
            UnitSystem.CUPS_US
        }
    )

    // Image Generation Provider Configuration (Gemini vs. Local ComfyUI)
    val imageGenEngine = MutableStateFlow(
        try {
            com.example.ai.ImageGenEngine.valueOf(prefs.getString("pref_image_gen_engine", com.example.ai.ImageGenEngine.GEMINI.name) ?: com.example.ai.ImageGenEngine.GEMINI.name)
        } catch (e: Exception) {
            com.example.ai.ImageGenEngine.GEMINI
        }
    )

    val comfyUiUrl = MutableStateFlow(prefs.getString("pref_comfy_ui_url", "http://192.168.1.54:8188") ?: "http://192.168.1.54:8188")
    val comfyUiCheckpoint = MutableStateFlow(prefs.getString("pref_comfy_ui_ckpt", "v1-5-pruned-emaonly.safetensors") ?: "v1-5-pruned-emaonly.safetensors")
    val comfyUiCustomWorkflow = MutableStateFlow(prefs.getString("pref_comfy_ui_custom_workflow", "") ?: "")
    val comfyUiTestStatus = MutableStateFlow<String?>(null)
    val isTestingComfyConnection = MutableStateFlow(false)

    // ==========================================
    // CLOUD & FAMILY SYNC (SELF-HOSTED VPS)
    // ==========================================
    val syncConfig = repository.syncConfig
    val syncManager = repository.syncManager

    val isCloudSyncEnabled = MutableStateFlow(syncConfig?.isSyncEnabled ?: false)
    val syncServerUrl = MutableStateFlow(syncConfig?.serverUrl ?: "")
    val syncSecretToken = MutableStateFlow(syncConfig?.syncToken ?: "")
    val isAutoSyncWifi = MutableStateFlow(syncConfig?.autoSyncWifi ?: false)
    val isTestingSyncConnection = MutableStateFlow(false)
    val syncConnectionTestResult = MutableStateFlow<Pair<Boolean, String>?>(null)

    val isSyncing: StateFlow<Boolean> = syncManager?.isSyncing ?: MutableStateFlow(false)
    val lastSyncStatus: StateFlow<String?> = syncManager?.lastSyncStatus ?: MutableStateFlow(null)
    val lastSyncTimestamp: StateFlow<Long> = syncManager?.lastSyncTimestamp ?: MutableStateFlow(0L)

    fun setCloudSyncEnabled(enabled: Boolean) {
        isCloudSyncEnabled.value = enabled
        syncConfig?.isSyncEnabled = enabled
        if (enabled && syncManager != null && syncConfig?.serverUrl?.isNotBlank() == true) {
            triggerSyncNow()
        }
    }

    fun setSyncServerUrl(url: String) {
        syncServerUrl.value = url.trim()
        syncConfig?.serverUrl = url.trim()
        syncConnectionTestResult.value = null
    }

    fun setSyncSecretToken(token: String) {
        syncSecretToken.value = token.trim()
        syncConfig?.syncToken = token.trim()
        syncConnectionTestResult.value = null
    }

    fun setAutoSyncWifi(enabled: Boolean) {
        isAutoSyncWifi.value = enabled
        syncConfig?.autoSyncWifi = enabled
    }

    fun testSyncConnection() {
        val manager = syncManager ?: return
        viewModelScope.launch {
            isTestingSyncConnection.value = true
            syncConnectionTestResult.value = null
            val result = manager.testConnection()
            if (result.isSuccess) {
                syncConnectionTestResult.value = Pair(true, result.getOrNull() ?: "Connected successfully (Server Online)")
            } else {
                syncConnectionTestResult.value = Pair(false, result.exceptionOrNull()?.message ?: "Connection Failed")
            }
            isTestingSyncConnection.value = false
        }
    }

    fun triggerSyncNow() {
        val manager = syncManager ?: return
        viewModelScope.launch {
            manager.performSync()
        }
    }

    /**
     * Called when the app is brought to the foreground / resumed.
     * Silently pulls delta changes from the VPS in the background.
     */
    fun onAppForegroundResume() {
        syncManager?.triggerImmediateSync()
    }

    fun setImageGenEngine(engine: com.example.ai.ImageGenEngine) {
        imageGenEngine.value = engine
        prefs.edit().putString("pref_image_gen_engine", engine.name).apply()
    }

    fun setComfyUiUrl(url: String) {
        comfyUiUrl.value = url.trim()
        prefs.edit().putString("pref_comfy_ui_url", url.trim()).apply()
    }

    fun setComfyUiCheckpoint(ckpt: String) {
        comfyUiCheckpoint.value = ckpt.trim()
        prefs.edit().putString("pref_comfy_ui_ckpt", ckpt.trim()).apply()
    }

    fun setComfyUiCustomWorkflow(workflowJson: String) {
        comfyUiCustomWorkflow.value = workflowJson.trim()
        prefs.edit().putString("pref_comfy_ui_custom_workflow", workflowJson.trim()).apply()
    }

    fun testComfyUiConnection() {
        viewModelScope.launch {
            isTestingComfyConnection.value = true
            comfyUiTestStatus.value = "Testing connection to ${comfyUiUrl.value}..."
            val res = com.example.ai.ComfyUiClient.testConnection(comfyUiUrl.value)
            if (res.isSuccess) {
                val msg = res.getOrNull() ?: "Connected to ComfyUI successfully!"
                comfyUiTestStatus.value = "✓ $msg"
                // Automatically switch and persist ComfyUI as active image generation engine
                setImageGenEngine(com.example.ai.ImageGenEngine.COMFY_UI)
            } else {
                comfyUiTestStatus.value = "✗ ${res.exceptionOrNull()?.localizedMessage ?: "Failed to connect"}"
            }
            isTestingComfyConnection.value = false
        }
    }

    val isSettingsOpen = MutableStateFlow(false)
    val isShoppingListOpen = MutableStateFlow(false)

    // Shopping list flows
    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.allShoppingItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uncheckedShoppingCount: StateFlow<Int> = repository.uncheckedShoppingCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setLanguageMode(mode: LanguageMode) {
        languageMode.value = mode
        prefs.edit().putString("pref_language_mode", mode.name).apply()
    }

    fun setUnitSystem(system: UnitSystem) {
        unitSystem.value = system
        prefs.edit().putString("pref_unit_system", system.name).apply()
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        soundEffectsEnabled.value = enabled
        prefs.edit().putBoolean("pref_sound_effects", enabled).apply()
    }

    fun setKeepScreenOn(enabled: Boolean) {
        keepScreenOn.value = enabled
        prefs.edit().putBoolean("pref_keep_screen_on", enabled).apply()
    }

    fun openSettings() {
        isSettingsOpen.value = true
    }

    fun closeSettings() {
        isSettingsOpen.value = false
    }

    // Total Recipe Count & Collection Statistics
    val totalRecipeCount: StateFlow<Int> = repository.allRecipes.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    data class CookbookStats(
        val totalRecipes: Int = 0,
        val totalFavorites: Int = 0,
        val recipesWithPhotos: Int = 0,
        val aiPhotosCount: Int = 0,
        val scannedCardsCount: Int = 0,
        val unphotographedCount: Int = 0,
        val estimatedStorageMb: Double = 0.0
    )

    val cookbookStats: StateFlow<CookbookStats> = repository.allRecipes.map { all ->
        var favs = 0
        var photos = 0
        var aiPhotos = 0
        var scannedCards = 0
        var unphotographed = 0
        var totalBytes = 0L
        for (r in all) {
            if (r.isFavorite) favs++
            val imgType = RecipeImageClassifier.getImageType(r.imageUri)
            when (imgType) {
                RecipeImageType.AI_GENERATED -> aiPhotos++
                RecipeImageType.SCANNED_CARD -> scannedCards++
                RecipeImageType.NONE -> unphotographed++
            }
            r.imageUri?.let { path ->
                try {
                    val f = File(path)
                    if (f.exists()) {
                        photos++
                        totalBytes += f.length()
                    }
                } catch (_: Exception) {}
            }
        }
        CookbookStats(
            totalRecipes = all.size,
            totalFavorites = favs,
            recipesWithPhotos = photos,
            aiPhotosCount = aiPhotos,
            scannedCardsCount = scannedCards,
            unphotographedCount = unphotographed,
            estimatedStorageMb = totalBytes / (1024.0 * 1024.0)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CookbookStats()
    )

    val recipePhotoStats: StateFlow<RecipePhotoStats> = repository.allRecipes.map { all ->
        RecipeImageClassifier.computeStats(all)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipePhotoStats()
    )

    fun quickAssignCategory(recipe: RecipeEntity, newCategory: String) {
        val trimmed = newCategory.trim()
        if (trimmed.isBlank()) return
        // Ensure the new category is also registered in the categories list if not present
        addCategory(trimmed)
        val updated = recipe.copy(category = trimmed)
        if (selectedRecipe.value?.id == recipe.id) {
            selectedRecipe.value = updated
        }
        viewModelScope.launch {
            repository.updateRecipe(updated)
        }
    }

    // Filtered recipes
    val recipes: StateFlow<List<RecipeEntity>> = combine(
        repository.allRecipes,
        searchQuery,
        selectedCategory,
        onlyFavorites
    ) { all, query, category, favOnly ->
        all.filter { recipe ->
            val matchesCategory = if (category == "All") {
                true
            } else {
                recipe.category.equals(category, ignoreCase = true) ||
                    recipe.category.split(",", "/", "&", ";").map { it.trim() }
                        .any { it.equals(category, ignoreCase = true) }
            }
            val matchesFav = !favOnly || recipe.isFavorite
            val matchesQuery = com.example.ui.util.FuzzySearchHelper.matches(recipe, query)
            matchesCategory && matchesFav && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active booklet view
    val selectedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val bookletCurrentPage = MutableStateFlow(0)
    val servingMultiplier = MutableStateFlow(1.0f)
    val isVintageCardMode = MutableStateFlow(false)
    val soundEffectsEnabled = MutableStateFlow(true)
    val selectedGlossaryItem = MutableStateFlow<com.example.data.model.GlossaryItem?>(null)
    val isRecipeEditorOpen = MutableStateFlow(false)
    val recipeEditorInitialTab = MutableStateFlow(0)
    val editingRecipeDraft = MutableStateFlow<RecipeEntity?>(null)
    val isShareDialogOpen = MutableStateFlow(false)

    fun openNewRecipeEditor() {
        editingRecipeDraft.value = RecipeEntity(
            id = 0,
            title = "",
            titleGerman = "",
            titleEnglish = "",
            category = categories.value.firstOrNull() ?: "Baking & Desserts",
            servings = "4 servings",
            prepTimeMinutes = 15,
            cookTimeMinutes = 30,
            difficulty = "Easy",
            ingredients = listOf(
                com.example.data.model.RecipeIngredient(name = "", amount = "", unit = "", nameEnglish = "")
            ),
            steps = listOf(
                com.example.data.model.RecipeStep(stepNumber = 1, instructionEnglish = "", instructionGerman = "")
            ),
            notes = "",
            originStory = ""
        )
        recipeEditorInitialTab.value = 0
        isRecipeEditorOpen.value = true
    }

    // Cook Mode (Keep screen awake, high brightness, step tracker, timer)
    val isCookMode = MutableStateFlow(false)
    val activeCookStep = MutableStateFlow(0)
    val checkedIngredients = MutableStateFlow<Set<Int>>(emptySet())
    val checkedSteps = MutableStateFlow<Set<Int>>(emptySet())
    val keepScreenOn = MutableStateFlow(true)
    val highContrastLargeText = MutableStateFlow(false)

    // Timer
    val timerSecondsRemaining = MutableStateFlow(0)
    val timerTotalSeconds = MutableStateFlow(0)
    val isTimerActive = MutableStateFlow(false)
    private var timerJob: Job? = null

    // Scanner state
    val isScanning = MutableStateFlow(false)
    val scannedDraftRecipe = MutableStateFlow<RecipeEntity?>(null)
    val scanErrorMessage = MutableStateFlow<String?>(null)
    val navigateToRecipeEvent = MutableStateFlow<RecipeEntity?>(null)

    fun clearNavigateToRecipeEvent() {
        navigateToRecipeEvent.value = null
    }

    // Text To Speech
    private var tts: TextToSpeech? = null
    val isTtsReady = MutableStateFlow(false)

    init {
        initTts(application)
    }

    private fun initTts(application: Application) {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady.value = true
                tts?.language = Locale.ENGLISH
            }
        }
    }

    fun speakStep(text: String, isGerman: Boolean) {
        if (!isTtsReady.value) return
        try {
            tts?.language = if (isGerman) Locale.GERMAN else Locale.ENGLISH
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "step_tts")
        } catch (e: Exception) {
            // Ignore speech errors
        }
    }

    fun stopTts() {
        tts?.stop()
    }

    fun selectRecipe(recipe: RecipeEntity?) {
        selectedRecipe.value = recipe
        bookletCurrentPage.value = 0
        activeCookStep.value = 0
        servingMultiplier.value = 1.0f
        isVintageCardMode.value = false
        checkedIngredients.value = emptySet()
        checkedSteps.value = emptySet()
    }

    fun setServingMultiplier(multiplier: Float) {
        servingMultiplier.value = multiplier.coerceIn(0.25f, 6.0f)
    }

    fun incrementServingMultiplier() {
        val current = servingMultiplier.value
        val next = when {
            current < 1.0f -> current + 0.5f
            current < 3.0f -> current + 0.5f
            else -> current + 1.0f
        }
        setServingMultiplier(next)
    }

    fun decrementServingMultiplier() {
        val current = servingMultiplier.value
        val prev = when {
            current <= 0.5f -> 0.5f
            current <= 1.0f -> current - 0.5f
            current <= 3.0f -> current - 0.5f
            else -> current - 1.0f
        }
        setServingMultiplier(prev)
    }

    fun playPageTurnSound() {
        if (!soundEffectsEnabled.value) return
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 30)
            toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (e: Exception) {
            // fallback
        }
    }

    fun openGlossaryFor(term: String) {
        val item = com.example.data.model.GermanCulinaryGlossary.findSubstitute(term)
        if (item != null) {
            selectedGlossaryItem.value = item
        }
    }

    fun closeGlossary() {
        selectedGlossaryItem.value = null
    }

    fun openRecipeEditor(recipe: RecipeEntity? = null, initialTab: Int = 0) {
        recipeEditorInitialTab.value = initialTab
        editingRecipeDraft.value = recipe ?: RecipeEntity(
            title = "",
            titleGerman = "",
            titleEnglish = "",
            category = "Family Classics",
            servings = "4 servings",
            prepTimeMinutes = 15,
            cookTimeMinutes = 30,
            difficulty = "Medium",
            ingredients = listOf(
                com.example.data.model.RecipeIngredient("Flour / Mehl", "500", "g", "Mehl", "Flour"),
                com.example.data.model.RecipeIngredient("Butter", "200", "g", "Butter", "Butter")
            ),
            steps = listOf(
                com.example.data.model.RecipeStep(1, "Preheat oven and mix ingredients.", "Den Ofen vorheizen und die Zutaten vermengen.", 0)
            ),
            originStory = "Cherished family heirloom recipe."
        )
        isRecipeEditorOpen.value = true
    }

    fun closeRecipeEditor() {
        isRecipeEditorOpen.value = false
        editingRecipeDraft.value = null
    }

    fun saveEditedRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            if (recipe.id == 0L) {
                val id = repository.insertRecipe(recipe)
                val inserted = repository.getRecipeDirect(id)
                if (inserted != null) {
                    selectRecipe(inserted)
                }
            } else {
                repository.updateRecipe(recipe)
                if (selectedRecipe.value?.id == recipe.id) {
                    selectedRecipe.value = recipe
                }
            }
            closeRecipeEditor()
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
            if (selectedRecipe.value?.id == recipe.id) {
                selectedRecipe.value = null
            }
            closeRecipeEditor()
        }
    }

    fun saveRecipeJournal(recipeId: Long, notes: String, rating: Int) {
        val current = selectedRecipe.value ?: return
        if (current.id == recipeId) {
            val updated = current.copy(notes = notes, rating = rating)
            selectedRecipe.value = updated
            viewModelScope.launch {
                repository.updateRecipe(updated)
            }
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe.id, !recipe.isFavorite)
            if (selectedRecipe.value?.id == recipe.id) {
                selectedRecipe.value = selectedRecipe.value?.copy(isFavorite = !recipe.isFavorite)
            }
        }
    }

    fun toggleIngredientChecked(index: Int) {
        val current = checkedIngredients.value.toMutableSet()
        if (current.contains(index)) current.remove(index) else current.add(index)
        checkedIngredients.value = current
    }

    fun toggleStepChecked(index: Int) {
        val current = checkedSteps.value.toMutableSet()
        if (current.contains(index)) current.remove(index) else current.add(index)
        checkedSteps.value = current
    }

    fun startTimerForStep(minutes: Int) {
        if (minutes <= 0) return
        timerJob?.cancel()
        timerTotalSeconds.value = minutes * 60
        timerSecondsRemaining.value = minutes * 60
        isTimerActive.value = true

        timerJob = viewModelScope.launch {
            while (timerSecondsRemaining.value > 0 && isTimerActive.value) {
                delay(1000)
                timerSecondsRemaining.value -= 1
            }
            if (timerSecondsRemaining.value == 0 && isTimerActive.value) {
                isTimerActive.value = false
                triggerTimerChime()
            }
        }
    }

    fun pauseTimer() {
        isTimerActive.value = false
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (timerSecondsRemaining.value > 0) {
            isTimerActive.value = true
            timerJob = viewModelScope.launch {
                while (timerSecondsRemaining.value > 0 && isTimerActive.value) {
                    delay(1000)
                    timerSecondsRemaining.value -= 1
                }
                if (timerSecondsRemaining.value == 0 && isTimerActive.value) {
                    isTimerActive.value = false
                    triggerTimerChime()
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        isTimerActive.value = false
        timerSecondsRemaining.value = 0
        timerTotalSeconds.value = 0
    }

    private fun triggerTimerChime() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
        } catch (e: Exception) {
            // Tone fallback
        }
    }

    // Duplicate Recipe Prompt Data
    data class DuplicatePromptData(
        val existingRecipe: RecipeEntity,
        val scannedRecipe: RecipeEntity,
        val rawBitmaps: List<Bitmap> = emptyList()
    )

    val duplicatePrompt = MutableStateFlow<DuplicatePromptData?>(null)
    val lastAutoSavedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val scanBatchSuccessEvent = MutableStateFlow<Long?>(null)
    val scanBatchCount = MutableStateFlow(0)

    fun resetScanBatchSession() {
        scanBatchCount.value = 0
        lastAutoSavedRecipe.value = null
        scanBatchSuccessEvent.value = null
    }

    fun scanRecipe(bitmap: Bitmap?, rawText: String?, imageUri: String? = null) {
        val bitmaps = if (bitmap != null) listOf(bitmap) else emptyList()
        scanRecipe(bitmaps, rawText, imageUri)
    }

    fun scanRecipe(bitmaps: List<Bitmap>, rawText: String?, imageUri: String? = null) {
        viewModelScope.launch {
            isScanning.value = true
            scanErrorMessage.value = null
            try {
                val parsed = repository.scanAndProcessRecipe(bitmaps, rawText, imageUri)
                val duplicate = repository.findDuplicateRecipe(parsed)
                if (duplicate != null) {
                    duplicatePrompt.value = DuplicatePromptData(
                        existingRecipe = duplicate,
                        scannedRecipe = parsed,
                        rawBitmaps = bitmaps
                    )
                } else {
                    // Auto-save recipe directly into the database for continuous rapid scanning
                    val id = repository.insertRecipe(parsed)
                    val inserted = repository.getRecipeDirect(id) ?: parsed.copy(id = id)
                    lastAutoSavedRecipe.value = inserted
                    scanBatchCount.value += 1
                    scannedDraftRecipe.value = null
                    scanBatchSuccessEvent.value = System.currentTimeMillis()
                }
            } catch (t: Throwable) {
                scanErrorMessage.value = "Scanning error: ${t.localizedMessage ?: "Unable to read recipe"}"
            } finally {
                isScanning.value = false
            }
        }
    }

    fun resolveDuplicateUpdate(prompt: DuplicatePromptData) {
        viewModelScope.launch {
            val merged = prompt.existingRecipe.copy(
                title = prompt.scannedRecipe.title.ifBlank { prompt.existingRecipe.title },
                titleEnglish = prompt.scannedRecipe.titleEnglish.ifBlank { prompt.existingRecipe.titleEnglish },
                titleGerman = prompt.scannedRecipe.titleGerman.ifBlank { prompt.existingRecipe.titleGerman },
                category = prompt.scannedRecipe.category.ifBlank { prompt.existingRecipe.category },
                servings = prompt.scannedRecipe.servings.ifBlank { prompt.existingRecipe.servings },
                prepTimeMinutes = prompt.scannedRecipe.prepTimeMinutes,
                cookTimeMinutes = prompt.scannedRecipe.cookTimeMinutes,
                difficulty = prompt.scannedRecipe.difficulty,
                ingredients = if (prompt.scannedRecipe.ingredients.isNotEmpty()) prompt.scannedRecipe.ingredients else prompt.existingRecipe.ingredients,
                steps = if (prompt.scannedRecipe.steps.isNotEmpty()) prompt.scannedRecipe.steps else prompt.existingRecipe.steps,
                notes = if (prompt.scannedRecipe.notes.isNotBlank()) prompt.scannedRecipe.notes else prompt.existingRecipe.notes,
                notesGerman = if (prompt.scannedRecipe.notesGerman.isNotBlank()) prompt.scannedRecipe.notesGerman else prompt.existingRecipe.notesGerman,
                imageUri = prompt.scannedRecipe.imageUri ?: prompt.existingRecipe.imageUri,
                originStory = if (prompt.scannedRecipe.originStory.isNotBlank() && !prompt.scannedRecipe.originStory.contains("Scanned recipe")) prompt.scannedRecipe.originStory else prompt.existingRecipe.originStory
            )
            repository.updateRecipe(merged)
            val updated = repository.getRecipeDirect(merged.id) ?: merged
            duplicatePrompt.value = null
            scannedDraftRecipe.value = null
            lastAutoSavedRecipe.value = updated
            scanBatchCount.value += 1
            scanBatchSuccessEvent.value = System.currentTimeMillis()
        }
    }

    fun resolveDuplicateSaveAsCopy(prompt: DuplicatePromptData) {
        viewModelScope.launch {
            val baseTitle = prompt.scannedRecipe.title.replace(Regex("\\s*\\((?:Variation|Copy).*?\\)$"), "")
            val all = repository.getAllRecipesDirect()
            val copiesCount = all.count { it.title.startsWith(baseTitle, ignoreCase = true) }
            val newTitle = if (copiesCount <= 1) "$baseTitle (Variation)" else "$baseTitle (Variation $copiesCount)"

            val variationRecipe = prompt.scannedRecipe.copy(
                id = 0,
                title = newTitle,
                titleEnglish = if (prompt.scannedRecipe.titleEnglish.isNotBlank()) newTitle else "",
                titleGerman = if (prompt.scannedRecipe.titleGerman.isNotBlank()) newTitle else ""
            )
            val newId = repository.insertRecipe(variationRecipe)
            val inserted = repository.getRecipeDirect(newId) ?: variationRecipe.copy(id = newId)
            duplicatePrompt.value = null
            scannedDraftRecipe.value = null
            lastAutoSavedRecipe.value = inserted
            scanBatchCount.value += 1
            scanBatchSuccessEvent.value = System.currentTimeMillis()
        }
    }

    fun dismissDuplicatePrompt() {
        duplicatePrompt.value = null
    }

    fun saveDraftRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            val id = repository.insertRecipe(recipe)
            val inserted = repository.getRecipeDirect(id) ?: recipe.copy(id = id)
            scannedDraftRecipe.value = null
            selectRecipe(inserted)
            navigateToRecipeEvent.value = inserted
        }
    }

    fun updateCoverTheme(theme: CoverTheme) {
        val current = selectedRecipe.value ?: return
        val updated = current.copy(coverTheme = theme.name)
        selectedRecipe.value = updated
        viewModelScope.launch {
            repository.updateRecipe(updated)
        }
    }

    // AI Cover Image Generation State & Actions
    val isGeneratingCover = MutableStateFlow(false)
    val coverGenerationError = MutableStateFlow<String?>(null)
    val lastCoverGenerationLog = MutableStateFlow<String?>(null)
    val showCoverErrorDialog = MutableStateFlow(false)

    // Batch AI Cover Generation
    val isBatchGeneratingCovers = MutableStateFlow(false)
    val batchCoverProgress = MutableStateFlow(Pair(0, 0)) // current, total
    val batchCoverCurrentTitle = MutableStateFlow("")
    val batchCoverSuccessCount = MutableStateFlow(0)
    val batchCoverFailCount = MutableStateFlow(0)
    val batchCoverIsCancelled = MutableStateFlow(false)
    val batchCoverLog = MutableStateFlow<String?>(null)
    val showBatchCoverDialog = MutableStateFlow(false)
    val selectedBatchFilter = MutableStateFlow(BatchCoverFilter.MISSING_AI_PHOTOS)

    val recipesMissingPhotosCount: StateFlow<Int> = recipes.map { list ->
        list.count { RecipeImageClassifier.isMissingAiPhoto(it.imageUri) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun openBatchCoverDialog() {
        showBatchCoverDialog.value = true
    }

    fun closeBatchCoverDialog() {
        showBatchCoverDialog.value = false
    }

    fun setBatchFilter(filter: BatchCoverFilter) {
        selectedBatchFilter.value = filter
    }

    fun startBatchGenerateMissingCovers(context: Context, customFilter: BatchCoverFilter? = null) {
        if (isBatchGeneratingCovers.value) return
        val filterToUse = customFilter ?: selectedBatchFilter.value
        viewModelScope.launch(Dispatchers.IO) {
            isBatchGeneratingCovers.value = true
            showBatchCoverDialog.value = true
            batchCoverIsCancelled.value = false
            batchCoverSuccessCount.value = 0
            batchCoverFailCount.value = 0

            val allRecipes = repository.getAllRecipesDirect()
            val targetRecipes = RecipeImageClassifier.filterRecipes(allRecipes, filterToUse)

            val total = targetRecipes.size
            batchCoverProgress.value = Pair(0, total)

            if (total == 0) {
                batchCoverLog.value = "No recipes found matching '${filterToUse.shortLabel}'!"
                isBatchGeneratingCovers.value = false
                return@launch
            }

            batchCoverLog.value = "Starting batch photo generation for $total recipes (${filterToUse.shortLabel}) using ${imageGenEngine.value.displayName}..."

            var success = 0
            var failed = 0

            for ((index, recipe) in targetRecipes.withIndex()) {
                if (batchCoverIsCancelled.value) {
                    batchCoverLog.value = "Generation cancelled ($success finished)."
                    break
                }

                val currentNum = index + 1
                batchCoverProgress.value = Pair(currentNum, total)
                batchCoverCurrentTitle.value = recipe.getDisplayTitle()
                batchCoverLog.value = "[$currentNum/$total] Generating photo for '${recipe.getDisplayTitle()}'..."

                try {
                    val ingList = recipe.ingredients.map { "${it.amount} ${it.unit} ${it.name}".trim() }
                    val stepsList = recipe.steps.take(4).map { it.getInstruction() }
                    val title = recipe.getDisplayTitle()

                    // If recipe already has a scanned card photo, try loading it as reference bitmap for ComfyUI if desired
                    var refBitmap: Bitmap? = null
                    if (!recipe.imageUri.isNullOrBlank() && File(recipe.imageUri).exists()) {
                        try {
                            refBitmap = BitmapFactory.decodeFile(recipe.imageUri)
                        } catch (_: Exception) {}
                    }

                    val result = if (imageGenEngine.value == com.example.ai.ImageGenEngine.COMFY_UI) {
                        com.example.ai.ComfyUiClient.generateRecipeImage(
                            baseUrl = comfyUiUrl.value,
                            title = title,
                            titleGerman = recipe.titleGerman,
                            category = recipe.category,
                            ingredients = ingList,
                            steps = stepsList,
                            notes = recipe.notes.ifBlank { recipe.originStory },
                            referenceBitmap = refBitmap,
                            customCheckPoint = comfyUiCheckpoint.value,
                            customWorkflowJson = comfyUiCustomWorkflow.value.ifBlank { null }
                        )
                    } else {
                        com.example.ai.GeminiClient.generateRecipeCoverImage(
                            title = title,
                            titleGerman = recipe.titleGerman,
                            category = recipe.category,
                            ingredients = ingList,
                            steps = stepsList,
                            notes = recipe.notes.ifBlank { recipe.originStory },
                            referenceBitmap = refBitmap
                        )
                    }

                    if (result.isSuccess && result.getOrNull() != null) {
                        val bitmap = result.getOrNull()!!
                        val file = File(context.filesDir, "recipe_cover_${recipe.id}_${System.currentTimeMillis()}.jpg")
                        file.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        val updated = recipe.copy(imageUri = file.absolutePath)
                        repository.updateRecipe(updated)
                        success++
                        batchCoverSuccessCount.value = success
                        batchCoverLog.value = "✓ [$currentNum/$total] Saved AI photo for '${recipe.getDisplayTitle()}'"
                    } else {
                        failed++
                        batchCoverFailCount.value = failed
                        val err = result.exceptionOrNull()?.localizedMessage ?: "Generation failed"
                        batchCoverLog.value = "✗ [$currentNum/$total] Failed '${recipe.getDisplayTitle()}': $err"
                    }
                } catch (e: Exception) {
                    failed++
                    batchCoverFailCount.value = failed
                    batchCoverLog.value = "✗ [$currentNum/$total] Error: ${e.localizedMessage}"
                }

                if (index < total - 1 && !batchCoverIsCancelled.value) {
                    delay(800)
                }
            }

            if (!batchCoverIsCancelled.value) {
                batchCoverLog.value = "Completed! Successfully generated $success photos ($failed failed)."
            }
            isBatchGeneratingCovers.value = false
        }
    }

    fun cancelBatchCoverGeneration() {
        batchCoverIsCancelled.value = true
    }

    fun clearCoverGenerationError() {
        coverGenerationError.value = null
        showCoverErrorDialog.value = false
    }

    fun openCoverErrorDialog() {
        showCoverErrorDialog.value = true
    }

    fun closeCoverErrorDialog() {
        showCoverErrorDialog.value = false
    }

    fun generateRecipeCoverArt(recipe: RecipeEntity, context: Context, referenceBitmap: Bitmap? = null) {
        if (isGeneratingCover.value) return
        viewModelScope.launch {
            isGeneratingCover.value = true
            coverGenerationError.value = null
            lastCoverGenerationLog.value = "Starting image generation using ${imageGenEngine.value.displayName}..."
            try {
                // Only use referenceBitmap if explicitly passed in (e.g. from camera/gallery scanner)
                val refBitmap = referenceBitmap

                val ingList = recipe.ingredients.map { "${it.amount} ${it.unit} ${it.name}".trim() }
                val stepsList = recipe.steps.take(4).map { it.getInstruction() }
                val title = recipe.getDisplayTitle()

                val result = if (imageGenEngine.value == com.example.ai.ImageGenEngine.COMFY_UI) {
                    lastCoverGenerationLog.value = "Connecting to ComfyUI at ${comfyUiUrl.value} with checkpoint '${comfyUiCheckpoint.value}'..."
                    com.example.ai.ComfyUiClient.generateRecipeImage(
                        baseUrl = comfyUiUrl.value,
                        title = title,
                        titleGerman = recipe.titleGerman,
                        category = recipe.category,
                        ingredients = ingList,
                        steps = stepsList,
                        notes = recipe.notes.ifBlank { recipe.originStory },
                        referenceBitmap = refBitmap,
                        customCheckPoint = comfyUiCheckpoint.value,
                        customWorkflowJson = comfyUiCustomWorkflow.value.ifBlank { null }
                    )
                } else {
                    lastCoverGenerationLog.value = "Sending image generation request to Google Cloud AI..."
                    com.example.ai.GeminiClient.generateRecipeCoverImage(
                        title = title,
                        titleGerman = recipe.titleGerman,
                        category = recipe.category,
                        ingredients = ingList,
                        steps = stepsList,
                        notes = recipe.notes.ifBlank { recipe.originStory },
                        referenceBitmap = refBitmap
                    )
                }
                if (result.isSuccess) {
                    val bitmap = result.getOrNull()
                    if (bitmap != null) {
                        val file = File(context.filesDir, "recipe_cover_${recipe.id}_${System.currentTimeMillis()}.jpg")
                        file.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        val updated = recipe.copy(imageUri = file.absolutePath)
                        repository.updateRecipe(updated)
                        selectedRecipe.value = updated
                        lastCoverGenerationLog.value = "✓ Image generated and saved successfully (${file.length() / 1024} KB)"
                    }
                } else {
                    val errMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to generate cover photo"
                    coverGenerationError.value = errMsg
                    lastCoverGenerationLog.value = "✗ Error: $errMsg"
                    showCoverErrorDialog.value = true
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: "Error generating cover photo"
                coverGenerationError.value = errMsg
                lastCoverGenerationLog.value = "✗ Exception: $errMsg"
                showCoverErrorDialog.value = true
            } finally {
                isGeneratingCover.value = false
            }
        }
    }

    fun removeRecipeCoverPhoto(recipe: RecipeEntity) {
        viewModelScope.launch {
            val updated = recipe.copy(imageUri = null)
            repository.updateRecipe(updated)
            selectedRecipe.value = updated
        }
    }

    fun markRecipeCooked() {
        val current = selectedRecipe.value ?: return
        viewModelScope.launch {
            repository.incrementCooked(current.id)
            selectedRecipe.value = current.copy(timesCooked = current.timesCooked + 1)
        }
    }

    // Smart Converter State
    val isSmartConverterOpen = MutableStateFlow(false)
    val converterInitialIngredient = MutableStateFlow<String?>(null)
    val converterInitialAmount = MutableStateFlow("2")
    val converterInitialUnit = MutableStateFlow("g")

    fun openSmartConverter(
        ingredientName: String? = null,
        amount: String = "2",
        unit: String = "g"
    ) {
        converterInitialIngredient.value = ingredientName
        converterInitialAmount.value = amount
        converterInitialUnit.value = unit
        isSmartConverterOpen.value = true
    }

    fun closeSmartConverter() {
        isSmartConverterOpen.value = false
        converterInitialIngredient.value = null
    }

    // Backup and Restore State
    val isBackupDialogOpen = MutableStateFlow(false)
    val backupStatusMessage = MutableStateFlow<String?>(null)
    val pendingRestoreManifest = MutableStateFlow<BackupManifest?>(null)
    val isRestoring = MutableStateFlow(false)
    val lastBackupDate = MutableStateFlow<String?>(prefs.getString("pref_last_backup_date", null))
    val autoWeeklyBackupEnabled = MutableStateFlow(prefs.getBoolean("pref_auto_weekly_backup", true))
    val savedBackupsList = MutableStateFlow<List<SavedBackupFile>>(emptyList())

    fun setAutoWeeklyBackupEnabled(enabled: Boolean) {
        autoWeeklyBackupEnabled.value = enabled
        prefs.edit().putBoolean("pref_auto_weekly_backup", enabled).apply()
        if (enabled) {
            checkAndPerformAutoWeeklyBackup()
        }
    }

    fun checkAndPerformAutoWeeklyBackup() {
        if (!autoWeeklyBackupEnabled.value) return
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            if (all.isNotEmpty()) {
                val created = BackupManager.performWeeklyBackupIfDue(getApplication(), all)
                if (created) {
                    refreshSavedBackups()
                }
            }
        }
    }

    fun refreshSavedBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = BackupManager.listAllLocalBackups(getApplication())
            withContext(Dispatchers.Main) {
                savedBackupsList.value = list
            }
        }
    }

    fun createInstantBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            if (all.isEmpty()) {
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Your cookbook is currently empty. Add recipes first to create a backup."
                }
                return@launch
            }
            val saved = BackupManager.createLocalBackup(getApplication(), all, "Instant Backup")
            withContext(Dispatchers.Main) {
                refreshSavedBackups()
                if (saved != null) {
                    val dateStr = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())
                    lastBackupDate.value = dateStr
                    prefs.edit().putString("pref_last_backup_date", dateStr).apply()
                    backupStatusMessage.value = "Backup created successfully! (${saved.recipeCount} recipes saved)"
                } else {
                    backupStatusMessage.value = "Failed to create backup file."
                }
            }
        }
    }

    fun deleteSavedBackup(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            BackupManager.deleteBackupFile(file)
            refreshSavedBackups()
        }
    }

    fun directRestoreBackupFile(file: File, replaceExisting: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            isRestoring.value = true
            try {
                val content = BackupManager.readBackupFileContent(file)
                if (content.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        backupStatusMessage.value = "Unable to read backup file."
                    }
                    return@launch
                }
                val parseResult = BackupManager.parseBackup(content, getApplication())
                val manifest = parseResult.getOrNull()
                if (manifest == null || manifest.recipes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        backupStatusMessage.value = "No valid recipes found in this backup file."
                    }
                    return@launch
                }
                repository.restoreRecipes(manifest.recipes, replaceExisting)
                val updated = repository.getAllRecipesDirect()
                BackupManager.saveLocalSnapshot(getApplication(), updated)
                withContext(Dispatchers.Main) {
                    val count = manifest.recipes.size
                    backupStatusMessage.value = if (replaceExisting) {
                        "Successfully restored $count recipes (Replaced library)!"
                    } else {
                        "Successfully merged $count recipes into library!"
                    }
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Restore failed: ${e.localizedMessage}"
                }
            } finally {
                isRestoring.value = false
            }
        }
    }

    fun deleteAllRecipes(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getAllRecipesDirect()
            if (current.isNotEmpty()) {
                BackupManager.createLocalBackup(getApplication(), current, "Pre-Deletion Safety Backup")
            }
            repository.deleteAllRecipes()
            refreshSavedBackups()
            withContext(Dispatchers.Main) {
                backupStatusMessage.value = "All recipes cleared. A safety backup was saved automatically."
                onComplete()
            }
        }
    }

    fun openBackupDialog() {
        isBackupDialogOpen.value = true
        backupStatusMessage.value = null
        refreshSavedBackups()
    }

    fun closeBackupDialog() {
        isBackupDialogOpen.value = false
        pendingRestoreManifest.value = null
    }

    fun clearPendingRestore() {
        pendingRestoreManifest.value = null
    }

    fun generateBackupJson(onReady: (String, Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            val json = BackupManager.exportToJson(all, getApplication())
            // Also ensure it's saved locally
            if (all.isNotEmpty()) {
                BackupManager.createLocalBackup(getApplication(), all, "Manual Export")
            }
            withContext(Dispatchers.Main) {
                refreshSavedBackups()
                onReady(json, all.size)
            }
        }
    }

    fun onBackupExportSuccess(count: Int) {
        val dateStr = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())
        lastBackupDate.value = dateStr
        prefs.edit().putString("pref_last_backup_date", dateStr).apply()
        backupStatusMessage.value = "Backup saved successfully! ($count recipes preserved)"
    }

    fun shareBackup(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            val json = BackupManager.exportToJson(all, getApplication())
            if (all.isNotEmpty()) {
                BackupManager.createLocalBackup(getApplication(), all, "Shared Backup")
            }
            val uri = BackupManager.createShareableBackupUri(context, json)
            withContext(Dispatchers.Main) {
                refreshSavedBackups()
                if (uri != null) {
                    BackupManager.shareBackup(context, uri, all.size)
                    onBackupExportSuccess(all.size)
                } else {
                    backupStatusMessage.value = "Unable to create shareable backup file."
                }
            }
        }
    }

    fun inspectBackupFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = BackupManager.readFromUri(context, uri)
            withContext(Dispatchers.Main) {
                if (content.isNullOrBlank()) {
                    backupStatusMessage.value = "Could not read data from selected file. You can also paste the JSON or recipe text directly."
                    return@withContext
                }
                inspectBackupText(content)
            }
        }
    }

    fun inspectBackupText(text: String) {
        if (text.isBlank()) {
            backupStatusMessage.value = "Please enter or paste recipe JSON or text."
            return
        }

        val parseResult = BackupManager.parseBackup(text, getApplication())
        if (parseResult.isSuccess) {
            val manifest = parseResult.getOrThrow()
            if (manifest.recipes.isEmpty()) {
                backupStatusMessage.value = "No recipes found in the provided backup."
            } else {
                pendingRestoreManifest.value = manifest
                val firstTitle = manifest.recipes.firstOrNull()?.title ?: "Recipe"
                val extraText = if (manifest.recipes.size > 1) " (including \"$firstTitle\" and ${manifest.recipes.size - 1} more)" else " (\"$firstTitle\")"
                backupStatusMessage.value = "Found ${manifest.recipes.size} recipe(s)$extraText. Choose below to restore into your library."
            }
        } else {
            backupStatusMessage.value = "Invalid format: ${parseResult.exceptionOrNull()?.message ?: "Unrecognized format"}"
        }
    }

    fun restoreLocalSnapshot(replaceExisting: Boolean) {
        val snapshot = BackupManager.getLocalSnapshot(getApplication())
        if (snapshot.isNullOrBlank()) {
            backupStatusMessage.value = "No local snapshot found yet."
            return
        }
        inspectBackupText(snapshot)
    }

    fun executeRestore(replaceExisting: Boolean, onComplete: (Int) -> Unit = {}) {
        val manifest = pendingRestoreManifest.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            isRestoring.value = true
            try {
                repository.restoreRecipes(manifest.recipes, replaceExisting)
                val count = manifest.recipes.size
                // Save updated snapshot
                val updated = repository.getAllRecipesDirect()
                BackupManager.saveLocalSnapshot(getApplication(), updated)
                withContext(Dispatchers.Main) {
                    pendingRestoreManifest.value = null
                    backupStatusMessage.value = if (replaceExisting) {
                        "Successfully restored $count recipes (Replaced library)!"
                    } else {
                        "Successfully merged $count recipes into your cookbook!"
                    }
                    onComplete(count)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Restore failed: ${e.localizedMessage}"
                }
            } finally {
                isRestoring.value = false
            }
        }
    }

    fun restoreStarterRecipes(replaceExisting: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            isRestoring.value = true
            try {
                repository.restoreDefaultRecipes(replaceExisting)
                val updated = repository.getAllRecipesDirect()
                BackupManager.saveLocalSnapshot(getApplication(), updated)
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Starter Heirloom Recipes (including Chocolate Chip Cookies) reloaded successfully!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Failed to load starter recipes: ${e.localizedMessage}"
                }
            } finally {
                isRestoring.value = false
            }
        }
    }

    // ==========================================
    // SHOPPING LIST METHODS
    // ==========================================
    fun openShoppingList() {
        isShoppingListOpen.value = true
    }

    fun closeShoppingList() {
        isShoppingListOpen.value = false
    }

    fun addRecipeToShoppingList(
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        selectedIndices: Set<Int>? = null,
        onAdded: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val addedCount = repository.addRecipeIngredientsToShoppingList(
                recipe = recipe,
                multiplier = multiplier,
                unitSystem = unitSystem,
                selectedIndices = selectedIndices
            )
            onAdded(addedCount)
        }
    }

    fun addIngredientsToShoppingList(
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        selectedIndices: Set<Int>? = null,
        onAdded: (Int) -> Unit = {}
    ) {
        addRecipeToShoppingList(recipe, multiplier, unitSystem, selectedIndices, onAdded)
    }

    fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.toggleShoppingItemChecked(item.id, !item.isChecked)
        }
    }

    fun addCustomShoppingItem(name: String, amount: String, unit: String, category: String? = null) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addCustomShoppingItem(name, amount, unit, category)
            }
        }
    }

    fun addShoppingItem(name: String, amount: String, unit: String, category: String? = null) {
        addCustomShoppingItem(name, amount, unit, category)
    }

    fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearCompletedShoppingItems() {
        viewModelScope.launch {
            repository.clearCompletedShoppingItems()
        }
    }

    fun clearAllShoppingItems() {
        viewModelScope.launch {
            repository.clearAllShoppingItems()
        }
    }

    fun shareShoppingList(context: Context) {
        val items = shoppingItems.value
        if (items.isEmpty()) return

        val text = buildString {
            appendLine("🛒 GROCERY SHOPPING LIST")
            appendLine("Vintage Heirloom Cookbook")
            appendLine("-------------------------")

            val grouped = items.groupBy { it.category }
            grouped.forEach { (cat, list) ->
                appendLine("\n[$cat]")
                list.forEach { item ->
                    val checkMark = if (item.isChecked) "✓ " else "☐ "
                    val qty = if (item.amount.isNotBlank()) "${item.amount} ${item.unit} " else ""
                    val recipeRef = if (!item.recipeTitle.isNullOrBlank()) " (for ${item.recipeTitle})" else ""
                    appendLine("$checkMark$qty${item.name}$recipeRef".trim())
                }
            }
            appendLine("\n-------------------------")
            appendLine("Shared from Heirloom Cookbook")
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Heirloom Cookbook Grocery List")
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Grocery List via..."))
    }

    // ==========================================
    // PDF RECIPE CARD METHODS
    // ==========================================
    fun shareRecipeAsPdf(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ) {
        viewModelScope.launch {
            val uri = RecipePdfGenerator.createShareablePdfUri(
                context = context,
                recipe = recipe,
                multiplier = multiplier,
                unitSystem = unitSystem,
                includeLore = includeLore,
                includeTips = includeTips,
                includeNotes = includeNotes
            )
            if (uri != null) {
                RecipePdfGenerator.shareRecipePdf(context, uri, recipe.getDisplayTitle())
            }
        }
    }

    fun generatePdfFile(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ): File {
        return RecipePdfGenerator.generateRecipePdf(
            context = context,
            recipe = recipe,
            multiplier = multiplier,
            unitSystem = unitSystem,
            includeLore = includeLore,
            includeTips = includeTips,
            includeNotes = includeNotes
        )
    }

    fun savePdfToTargetUri(context: Context, targetUri: Uri, pdfFile: File): Boolean {
        return RecipePdfGenerator.savePdfToUri(context, targetUri, pdfFile)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
    }
}
