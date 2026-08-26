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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository
    private val prefs = application.getSharedPreferences("heirloom_recipe_prefs", android.content.Context.MODE_PRIVATE)

    init {
        val db = AppDatabase.getInstance(application)
        repository = RecipeRepository(db.recipeDao(), db.shoppingDao())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.getRecipeCount() == 0) {
                    repository.restoreDefaultRecipes(replaceExisting = false)
                }
            } catch (e: Exception) {
                // Ignore initialization errors
            }
        }
    }

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val onlyFavorites = MutableStateFlow(false)
    
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
                scannedDraftRecipe.value = parsed
            } catch (t: Throwable) {
                scanErrorMessage.value = "Scanning error: ${t.localizedMessage ?: "Unable to read recipe"}"
            } finally {
                isScanning.value = false
            }
        }
    }

    fun saveDraftRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            val id = repository.insertRecipe(recipe)
            val inserted = repository.getRecipeDirect(id)
            scannedDraftRecipe.value = null
            if (inserted != null) {
                selectRecipe(inserted)
            }
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

    fun openBackupDialog() {
        isBackupDialogOpen.value = true
        backupStatusMessage.value = null
    }

    fun closeBackupDialog() {
        isBackupDialogOpen.value = false
        pendingRestoreManifest.value = null
    }

    fun clearPendingRestore() {
        pendingRestoreManifest.value = null
    }

    fun generateBackupJson(onReady: (String, Int) -> Unit) {
        viewModelScope.launch {
            val all = repository.getAllRecipesDirect()
            val json = BackupManager.exportToJson(all)
            onReady(json, all.size)
        }
    }

    fun onBackupExportSuccess(count: Int) {
        val dateStr = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())
        lastBackupDate.value = dateStr
        prefs.edit().putString("pref_last_backup_date", dateStr).apply()
        backupStatusMessage.value = "Backup saved successfully! ($count recipes preserved)"
    }

    fun shareBackup(context: Context) {
        viewModelScope.launch {
            val all = repository.getAllRecipesDirect()
            val json = BackupManager.exportToJson(all)
            val uri = BackupManager.createShareableBackupUri(context, json)
            if (uri != null) {
                BackupManager.shareBackup(context, uri, all.size)
                onBackupExportSuccess(all.size)
            } else {
                backupStatusMessage.value = "Unable to create shareable backup file."
            }
        }
    }

    fun inspectBackupFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val content = BackupManager.readFromUri(context, uri)
            if (content.isNullOrBlank()) {
                backupStatusMessage.value = "Failed to read backup file."
                return@launch
            }

            val parseResult = BackupManager.parseBackup(content)
            if (parseResult.isSuccess) {
                val manifest = parseResult.getOrThrow()
                if (manifest.recipes.isEmpty()) {
                    backupStatusMessage.value = "The selected backup file contains 0 recipes."
                } else {
                    pendingRestoreManifest.value = manifest
                    backupStatusMessage.value = null
                }
            } else {
                backupStatusMessage.value = "Invalid backup file: ${parseResult.exceptionOrNull()?.message ?: "Unrecognized format"}"
            }
        }
    }

    fun executeRestore(replaceExisting: Boolean, onComplete: (Int) -> Unit = {}) {
        val manifest = pendingRestoreManifest.value ?: return
        viewModelScope.launch {
            isRestoring.value = true
            try {
                repository.restoreRecipes(manifest.recipes, replaceExisting)
                val count = manifest.recipes.size
                pendingRestoreManifest.value = null
                backupStatusMessage.value = if (replaceExisting) {
                    "Successfully restored $count recipes (Replaced existing library)!"
                } else {
                    "Successfully merged $count recipes into your cookbook!"
                }
                onComplete(count)
            } catch (e: Exception) {
                backupStatusMessage.value = "Restore failed: ${e.localizedMessage}"
            } finally {
                isRestoring.value = false
            }
        }
    }

    fun restoreStarterRecipes(replaceExisting: Boolean = false) {
        viewModelScope.launch {
            isRestoring.value = true
            try {
                repository.restoreDefaultRecipes(replaceExisting)
                backupStatusMessage.value = "Starter Heirloom Recipes reloaded successfully!"
            } catch (e: Exception) {
                backupStatusMessage.value = "Failed to load starter recipes: ${e.localizedMessage}"
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
