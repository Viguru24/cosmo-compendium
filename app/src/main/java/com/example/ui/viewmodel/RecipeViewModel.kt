package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
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
import com.example.util.pdf.RecipePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
                }
                // Automatic weekly backup if enabled
                if (autoWeeklyBackupEnabled.value) {
                    val all = repository.getAllRecipesDirect()
                    BackupManager.performWeeklyBackupIfDue(application, all)
                }
                refreshSavedBackups()
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

    // Filtered recipes
    val recipes: StateFlow<List<RecipeEntity>> = combine(
        repository.allRecipes,
        searchQuery,
        selectedCategory,
        onlyFavorites
    ) { all, query, category, favOnly ->
        all.filter { recipe ->
            val matchesCategory = category == "All" || recipe.category.equals(category, ignoreCase = true)
            val matchesFav = !favOnly || recipe.isFavorite
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                recipe.title.lowercase().contains(q) ||
                        recipe.titleGerman.lowercase().contains(q) ||
                        recipe.titleEnglish.lowercase().contains(q) ||
                        recipe.category.lowercase().contains(q) ||
                        recipe.notes.lowercase().contains(q) ||
                        recipe.ingredients.any {
                            it.name.lowercase().contains(q) ||
                                    it.nameGerman?.lowercase()?.contains(q) == true ||
                                    it.nameEnglish?.lowercase()?.contains(q) == true
                        }
            }
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
                    // Open the interactive review & edit sheet so user can inspect or adjust the AI extracted recipe before finalizing
                    scannedDraftRecipe.value = parsed
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
            selectRecipe(updated)
            navigateToRecipeEvent.value = updated
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
            selectRecipe(inserted)
            navigateToRecipeEvent.value = inserted
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
                val parseResult = BackupManager.parseBackup(content)
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
            val json = BackupManager.exportToJson(all)
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
            val json = BackupManager.exportToJson(all)
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

        val parseResult = BackupManager.parseBackup(text)
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
