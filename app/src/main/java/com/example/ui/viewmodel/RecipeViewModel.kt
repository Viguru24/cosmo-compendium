package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiProvider
import com.example.ai.ChatMessage
import com.example.ai.GeminiModelConfig
import com.example.ai.ImageGenEngine
import com.example.data.backup.BackupManifest
import com.example.data.backup.SavedBackupFile
import com.example.data.local.AppDatabase
import com.example.data.local.RecipeEntity
import com.example.data.local.ShoppingItemEntity
import com.example.data.model.CoverTheme
import com.example.data.model.DietaryClassifier
import com.example.data.model.DietaryFilter
import com.example.data.model.GlossaryItem
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.data.model.UnitSystem
import com.example.data.repository.RecipeRepository
import com.example.data.sync.SyncConfig
import com.example.data.sync.SyncManager
import com.example.ui.components.GuideTopic
import com.example.ui.util.BatchCoverFilter
import com.example.ui.util.FuzzySearchHelper
import com.example.ui.util.ImageUtils
import com.example.ui.util.RecipeImageClassifier
import com.example.ui.util.RecipeImageType
import com.example.ui.util.RecipePhotoStats
import com.example.util.AppLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Primary ViewModel orchestrating recipe data flows, active navigation states,
 * and delegating specialized domain operations to modular delegates.
 */
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    data class DuplicatePromptData(
        val existingRecipe: RecipeEntity,
        val scannedRecipe: RecipeEntity,
        val rawBitmaps: List<Bitmap> = emptyList()
    )

    private val defaultExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.e("RecipeViewModel", "Unhandled coroutine error: ${throwable.message}", throwable)
    }

    val repository: RecipeRepository
    val prefs: SharedPreferences = application.getSharedPreferences("heirloom_recipe_prefs", Context.MODE_PRIVATE)

    // ==========================================
    // DELEGATES
    // ==========================================
    val profileCategoryDelegate: RecipeProfileCategoryDelegate
    val aiSettingsDelegate: RecipeAiSettingsDelegate
    val kitchenCookDelegate: RecipeKitchenCookDelegate
    val backupDelegate: RecipeBackupDelegate
    val batchCoverDelegate: RecipeBatchCoverDelegate
    val shoppingDelegate: RecipeShoppingDelegate
    val pdfDelegate: RecipePdfDelegate
    val sousChefDelegate: RecipeSousChefDelegate
    val scanDelegate: RecipeScanDelegate

    // Core filter and search states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val onlyFavorites = MutableStateFlow(false)
    val selectedDietaryFilter = MutableStateFlow<DietaryFilter>(DietaryFilter.ALL)

    // User preferences
    val languageMode = MutableStateFlow(
        try {
            LanguageMode.valueOf(prefs.getString("pref_language_mode", LanguageMode.GERMAN.name) ?: LanguageMode.GERMAN.name)
        } catch (_: Exception) {
            LanguageMode.GERMAN
        }
    )

    val unitSystem = MutableStateFlow(
        try {
            UnitSystem.valueOf(prefs.getString("pref_unit_system", UnitSystem.CUPS_US.name) ?: UnitSystem.CUPS_US.name)
        } catch (_: Exception) {
            UnitSystem.CUPS_US
        }
    )

    val soundEffectsEnabled = MutableStateFlow(prefs.getBoolean("pref_sound_effects", true))
    val keepScreenOn = MutableStateFlow(prefs.getBoolean("pref_keep_screen_on", true))
    val isSettingsOpen = MutableStateFlow(false)

    // Booklet / Cook / Editor States
    val selectedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val bookletCurrentPage = MutableStateFlow(0)
    val isVintageCardMode = MutableStateFlow(false)
    val selectedGlossaryItem = MutableStateFlow<GlossaryItem?>(null)
    val isRecipeEditorOpen = MutableStateFlow(false)
    val recipeEditorInitialTab = MutableStateFlow(0)
    val editingRecipeDraft = MutableStateFlow<RecipeEntity?>(null)
    val isShareDialogOpen = MutableStateFlow(false)

    val isCookMode = MutableStateFlow(false)
    val activeCookStep = MutableStateFlow(0)
    val checkedIngredients = MutableStateFlow<Set<Int>>(emptySet())
    val checkedSteps = MutableStateFlow<Set<Int>>(emptySet())
    val highContrastLargeText = MutableStateFlow(false)

    val navigateToRecipeEvent = MutableStateFlow<RecipeEntity?>(null)
    val customPromptDialogRecipe = MutableStateFlow<RecipeEntity?>(null)

    // Smart Converter State
    val isSmartConverterOpen = MutableStateFlow(false)
    val converterInitialIngredient = MutableStateFlow<String?>(null)
    val converterInitialAmount = MutableStateFlow("2")
    val converterInitialUnit = MutableStateFlow("g")

    init {
        val db = AppDatabase.getInstance(application)
        repository = RecipeRepository(application, db.recipeDao(), db.shoppingDao())

        // Saved Gemini API key initialization
        val savedKey = prefs.getString("pref_gemini_api_key", null)
        if (!savedKey.isNullOrBlank()) {
            com.example.ai.GeminiClient.setCustomApiKey(savedKey)
        }

        val initAiProvider = try {
            AiProvider.valueOf(prefs.getString("pref_ai_provider", AiProvider.GOOGLE_GEMINI.name) ?: AiProvider.GOOGLE_GEMINI.name)
        } catch (_: Exception) {
            AiProvider.GOOGLE_GEMINI
        }
        val initGeminiModel = prefs.getString("pref_gemini_model", GeminiModelConfig.PRIMARY_MODEL) ?: GeminiModelConfig.PRIMARY_MODEL

        com.example.ai.UniversalAiRecipeService.setProvider(initAiProvider)
        val providerKey = prefs.getString("pref_ai_key_${initAiProvider.id}", if (initAiProvider == AiProvider.GOOGLE_GEMINI) savedKey else null)
        if (!providerKey.isNullOrBlank()) {
            com.example.ai.UniversalAiRecipeService.setApiKey(initAiProvider, providerKey)
        }
        val providerModel = prefs.getString("pref_ai_model_${initAiProvider.id}", if (initAiProvider == AiProvider.GOOGLE_GEMINI) initGeminiModel else initAiProvider.defaultModel)
        if (!providerModel.isNullOrBlank()) {
            com.example.ai.UniversalAiRecipeService.setModel(initAiProvider, providerModel)
        }
        val providerBaseUrl = prefs.getString("pref_ai_base_url_${initAiProvider.id}", initAiProvider.defaultBaseUrl)
        if (!providerBaseUrl.isNullOrBlank()) {
            com.example.ai.UniversalAiRecipeService.setCustomBaseUrl(initAiProvider, providerBaseUrl)
        }

        // Initialize delegates
        profileCategoryDelegate = RecipeProfileCategoryDelegate(this, repository, prefs)
        aiSettingsDelegate = RecipeAiSettingsDelegate(this, repository, prefs)
        kitchenCookDelegate = RecipeKitchenCookDelegate(this, application, repository)
        backupDelegate = RecipeBackupDelegate(this, application, repository, prefs)
        batchCoverDelegate = RecipeBatchCoverDelegate(this, application, repository)
        shoppingDelegate = RecipeShoppingDelegate(this, repository)
        pdfDelegate = RecipePdfDelegate(this, repository)
        sousChefDelegate = RecipeSousChefDelegate(this, application, repository, prefs)
        scanDelegate = RecipeScanDelegate(this, application, repository)

        // Observe repository and initialize profiles
        viewModelScope.launch {
            repository.allRecipes.collect { recipeList ->
                if (recipeList.isNotEmpty()) {
                    var migrationNeeded = false
                    recipeList.forEach { r ->
                        val fixedProfile = when {
                            r.profileName.equals("Wife", ignoreCase = true) -> "Annette"
                            r.profileName.equals("Daughter", ignoreCase = true) -> "Isabel"
                            else -> null
                        }
                        if (fixedProfile != null) {
                            migrationNeeded = true
                            launch(Dispatchers.IO) {
                                repository.updateRecipeProfile(r.id, fixedProfile)
                            }
                        }
                    }
                    if (migrationNeeded && isCloudSyncEnabled.value) {
                        launch(Dispatchers.IO) { syncManager?.performSync() }
                    }
                }

                val dbProfiles = recipeList.map { it.profileName.trim() }
                    .map { name ->
                        when {
                            name.equals("Wife", ignoreCase = true) -> "Annette"
                            name.equals("Daughter", ignoreCase = true) -> "Isabel"
                            else -> name
                        }
                    }
                    .filter { it.isNotBlank() }
                    .distinct()

                val current = familyProfiles.value
                val combined = (current + dbProfiles).distinct()
                if (combined != current) {
                    withContext(Dispatchers.Main) {
                        familyProfiles.value = combined
                        profileCategoryDelegate.saveProfiles(combined)
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                prefs.edit().putBoolean("pref_has_seeded_initial_recipes", true).commit()
                repository.purgeDemoRecipes()
                repository.deduplicateCollection()
                if (autoWeeklyBackupEnabled.value) {
                    val all = repository.getAllRecipesDirect()
                    com.example.data.backup.BackupManager.performWeeklyBackupIfDue(application, all)
                }
                refreshSavedBackups()
                syncManager?.triggerImmediateSync()
            } catch (t: Throwable) {
                AppLogger.e("RecipeViewModel", "Error during background initialization", t)
            }

            while (kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]?.isActive == true) {
                kotlinx.coroutines.delay(20000)
                if (syncConfig?.isSyncEnabled == true && isSyncing.value == false) {
                    try {
                        syncManager?.performSync()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // ==========================================
    // FAMILY PROFILES & CATEGORIES FORWARDERS
    // ==========================================
    val defaultProfiles: List<String> get() = profileCategoryDelegate.defaultProfiles
    val familyProfiles: MutableStateFlow<List<String>> get() = profileCategoryDelegate.familyProfiles
    val defaultProfile: MutableStateFlow<String> get() = profileCategoryDelegate.defaultProfile
    val alwaysStartOnDefault: MutableStateFlow<Boolean> get() = profileCategoryDelegate.alwaysStartOnDefault
    val activeProfile: MutableStateFlow<String> get() = profileCategoryDelegate.activeProfile
    val isProfileSwitcherOpen: MutableStateFlow<Boolean> get() = profileCategoryDelegate.isProfileSwitcherOpen
    val defaultCategories: List<String> get() = profileCategoryDelegate.defaultCategories
    val categories: MutableStateFlow<List<String>> get() = profileCategoryDelegate.categories
    val isCategoryManagerOpen: MutableStateFlow<Boolean> get() = profileCategoryDelegate.isCategoryManagerOpen

    fun setActiveProfile(profile: String) = profileCategoryDelegate.setActiveProfile(profile)
    fun setDeviceDefaultProfile(profile: String) = profileCategoryDelegate.setDeviceDefaultProfile(profile)
    fun setAlwaysStartOnDefault(enabled: Boolean) = profileCategoryDelegate.setAlwaysStartOnDefault(enabled)
    fun addFamilyProfile(name: String) = profileCategoryDelegate.addFamilyProfile(name)
    fun renameFamilyProfile(oldName: String, newName: String) = profileCategoryDelegate.renameFamilyProfile(oldName, newName)
    fun deleteFamilyProfile(name: String) = profileCategoryDelegate.deleteFamilyProfile(name)
    fun assignRecipeToProfile(recipe: RecipeEntity, targetProfile: String) = profileCategoryDelegate.assignRecipeToProfile(recipe, targetProfile)
    fun bulkMoveRecipes(sourceProfile: String, targetProfile: String, onComplete: (Int) -> Unit = {}) = profileCategoryDelegate.bulkMoveRecipes(sourceProfile, targetProfile, onComplete)
    fun openProfileSwitcher() = profileCategoryDelegate.openProfileSwitcher()
    fun closeProfileSwitcher() = profileCategoryDelegate.closeProfileSwitcher()
    fun addCategory(name: String) = profileCategoryDelegate.addCategory(name)
    fun renameCategory(oldName: String, newName: String) = profileCategoryDelegate.renameCategory(oldName, newName)
    fun deleteCategory(name: String) = profileCategoryDelegate.deleteCategory(name)
    fun moveCategoryUp(index: Int) = profileCategoryDelegate.moveCategoryUp(index)
    fun moveCategoryDown(index: Int) = profileCategoryDelegate.moveCategoryDown(index)
    fun resetCategoriesToDefault() = profileCategoryDelegate.resetCategoriesToDefault()
    fun openCategoryManager() = profileCategoryDelegate.openCategoryManager()
    fun closeCategoryManager() = profileCategoryDelegate.closeCategoryManager()
    fun quickAssignCategory(recipe: RecipeEntity, newCategory: String) = profileCategoryDelegate.quickAssignCategory(recipe, newCategory)
    fun extractCategoriesFromRecipe(catStr: String): List<String> = profileCategoryDelegate.extractCategoriesFromRecipe(catStr)

    // ==========================================
    // AI SETTINGS & SYNC FORWARDERS
    // ==========================================
    val aiProvider: MutableStateFlow<AiProvider> get() = aiSettingsDelegate.aiProvider
    val geminiApiKey: MutableStateFlow<String> get() = aiSettingsDelegate.geminiApiKey
    val geminiModel: MutableStateFlow<String> get() = aiSettingsDelegate.geminiModel
    val geminiApiTestStatus: MutableStateFlow<String?> get() = aiSettingsDelegate.geminiApiTestStatus
    val isTestingGeminiApi: MutableStateFlow<Boolean> get() = aiSettingsDelegate.isTestingGeminiApi
    val imageGenEngine: MutableStateFlow<ImageGenEngine> get() = aiSettingsDelegate.imageGenEngine
    val comfyUiUrl: MutableStateFlow<String> get() = aiSettingsDelegate.comfyUiUrl
    val comfyUiCheckpoint: MutableStateFlow<String> get() = aiSettingsDelegate.comfyUiCheckpoint
    val comfyUiCustomWorkflow: MutableStateFlow<String> get() = aiSettingsDelegate.comfyUiCustomWorkflow
    val comfyUiTestStatus: MutableStateFlow<String?> get() = aiSettingsDelegate.comfyUiTestStatus
    val isTestingComfyConnection: MutableStateFlow<Boolean> get() = aiSettingsDelegate.isTestingComfyConnection
    val availableComfyCheckpoints: MutableStateFlow<List<String>> get() = aiSettingsDelegate.availableComfyCheckpoints
    val syncConfig: SyncConfig? get() = aiSettingsDelegate.syncConfig
    val syncManager: SyncManager? get() = aiSettingsDelegate.syncManager
    val isCloudSyncEnabled: MutableStateFlow<Boolean> get() = aiSettingsDelegate.isCloudSyncEnabled
    val syncServerUrl: MutableStateFlow<String> get() = aiSettingsDelegate.syncServerUrl
    val syncSecretToken: MutableStateFlow<String> get() = aiSettingsDelegate.syncSecretToken
    val isAutoSyncWifi: MutableStateFlow<Boolean> get() = aiSettingsDelegate.isAutoSyncWifi
    val isTestingSyncConnection: MutableStateFlow<Boolean> get() = aiSettingsDelegate.isTestingSyncConnection
    val syncConnectionTestResult: MutableStateFlow<Pair<Boolean, String>?> get() = aiSettingsDelegate.syncConnectionTestResult
    val isSyncing: StateFlow<Boolean> get() = aiSettingsDelegate.isSyncing
    val lastSyncStatus: StateFlow<String?> get() = aiSettingsDelegate.lastSyncStatus
    val lastSyncTimestamp: StateFlow<Long> get() = aiSettingsDelegate.lastSyncTimestamp

    fun getAiApiKey(provider: AiProvider): String = aiSettingsDelegate.getAiApiKey(provider)
    fun getAiModel(provider: AiProvider): String = aiSettingsDelegate.getAiModel(provider)
    fun getAiBaseUrl(provider: AiProvider): String = aiSettingsDelegate.getAiBaseUrl(provider)
    fun setAiProvider(provider: AiProvider) = aiSettingsDelegate.setAiProvider(provider)
    fun setAiApiKey(provider: AiProvider, key: String) = aiSettingsDelegate.setAiApiKey(provider, key)
    fun setAiModel(provider: AiProvider, model: String) = aiSettingsDelegate.setAiModel(provider, model)
    fun setAiBaseUrl(provider: AiProvider, url: String) = aiSettingsDelegate.setAiBaseUrl(provider, url)
    fun setGeminiApiKey(key: String) = aiSettingsDelegate.setGeminiApiKey(key)
    fun setGeminiModel(model: String) = aiSettingsDelegate.setGeminiModel(model)
    fun testGeminiApiConnection() = aiSettingsDelegate.testGeminiApiConnection()
    fun testAiConnection(provider: AiProvider = aiProvider.value) = aiSettingsDelegate.testAiConnection(provider)
    fun setCloudSyncEnabled(enabled: Boolean) = aiSettingsDelegate.setCloudSyncEnabled(enabled)
    fun setSyncServerUrl(url: String) = aiSettingsDelegate.setSyncServerUrl(url)
    fun setSyncSecretToken(token: String) = aiSettingsDelegate.setSyncSecretToken(token)
    fun setAutoSyncWifi(enabled: Boolean) = aiSettingsDelegate.setAutoSyncWifi(enabled)
    fun testSyncConnection() = aiSettingsDelegate.testSyncConnection()
    fun triggerSyncNow() = aiSettingsDelegate.triggerSyncNow()
    fun onAppForegroundResume() = aiSettingsDelegate.onAppForegroundResume()
    fun setImageGenEngine(engine: ImageGenEngine) = aiSettingsDelegate.setImageGenEngine(engine)
    fun setComfyUiUrl(url: String) = aiSettingsDelegate.setComfyUiUrl(url)
    fun setComfyUiCheckpoint(ckpt: String) = aiSettingsDelegate.setComfyUiCheckpoint(ckpt)
    fun setComfyUiCustomWorkflow(workflowJson: String) = aiSettingsDelegate.setComfyUiCustomWorkflow(workflowJson)
    fun testComfyUiConnection() = aiSettingsDelegate.testComfyUiConnection()

    // ==========================================
    // SOUS-CHEF & GUIDE FORWARDERS
    // ==========================================
    val isSousChefOpen: MutableStateFlow<Boolean> get() = sousChefDelegate.isSousChefOpen
    val sousChefMessages: MutableStateFlow<List<ChatMessage>> get() = sousChefDelegate.sousChefMessages
    val isSousChefProcessing: MutableStateFlow<Boolean> get() = sousChefDelegate.isSousChefProcessing
    val triggerScanFromSousChef: MutableStateFlow<Boolean> get() = sousChefDelegate.triggerScanFromSousChef
    val showErrorLogDialog: MutableStateFlow<Boolean> get() = sousChefDelegate.showErrorLogDialog
    val isGuideOpen: MutableStateFlow<Boolean> get() = sousChefDelegate.isGuideOpen
    val guideInitialTopic: MutableStateFlow<GuideTopic> get() = sousChefDelegate.guideInitialTopic
    val isVideoAnalyzing: MutableStateFlow<Boolean> get() = sousChefDelegate.isVideoAnalyzing

    fun openSousChef() = sousChefDelegate.openSousChef()
    fun closeSousChef() = sousChefDelegate.closeSousChef()
    fun openErrorLogDialog() = sousChefDelegate.openErrorLogDialog()
    fun closeErrorLogDialog() = sousChefDelegate.closeErrorLogDialog()
    fun openGuide(topic: GuideTopic = GuideTopic.CARD_SCANNING) = sousChefDelegate.openGuide(topic)
    fun closeGuide() = sousChefDelegate.closeGuide()
    fun clearTriggerScanFromSousChef() = sousChefDelegate.clearTriggerScanFromSousChef()
    fun cancelSousChefProcessing() = sousChefDelegate.cancelSousChefProcessing()
    fun importRecipeFromVideo(context: Context, videoUri: Uri, targetProfile: String = activeProfile.value) =
        sousChefDelegate.importRecipeFromVideo(context, videoUri, targetProfile)
    fun handleSousChefInput(prompt: String) = sousChefDelegate.handleSousChefInput(prompt)

    // ==========================================
    // SHOPPING LIST FORWARDERS
    // ==========================================
    val isShoppingListOpen: MutableStateFlow<Boolean> get() = shoppingDelegate.isShoppingListOpen
    val shoppingItems: StateFlow<List<ShoppingItemEntity>> get() = shoppingDelegate.shoppingItems
    val uncheckedShoppingCount: StateFlow<Int> get() = shoppingDelegate.uncheckedShoppingCount

    fun openShoppingList() = shoppingDelegate.openShoppingList()
    fun closeShoppingList() = shoppingDelegate.closeShoppingList()
    fun addRecipeToShoppingList(
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        selectedIndices: Set<Int>? = null,
        onAdded: (Int) -> Unit = {}
    ) = shoppingDelegate.addRecipeToShoppingList(recipe, multiplier, unitSystem, selectedIndices, onAdded)
    fun addIngredientsToShoppingList(
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        selectedIndices: Set<Int>? = null,
        onAdded: (Int) -> Unit = {}
    ) = shoppingDelegate.addIngredientsToShoppingList(recipe, multiplier, unitSystem, selectedIndices, onAdded)
    fun toggleShoppingItem(item: ShoppingItemEntity) = shoppingDelegate.toggleShoppingItem(item)
    fun addCustomShoppingItem(name: String, amount: String, unit: String, category: String? = null) =
        shoppingDelegate.addCustomShoppingItem(name, amount, unit, category)
    fun addShoppingItem(name: String, amount: String, unit: String, category: String? = null) =
        shoppingDelegate.addShoppingItem(name, amount, unit, category)
    fun deleteShoppingItem(item: ShoppingItemEntity) = shoppingDelegate.deleteShoppingItem(item)
    fun clearCompletedShoppingItems() = shoppingDelegate.clearCompletedShoppingItems()
    fun clearAllShoppingItems() = shoppingDelegate.clearAllShoppingItems()
    fun shareShoppingList(context: Context) = shoppingDelegate.shareShoppingList(context)

    // ==========================================
    // PDF GENERATION & EXPORT FORWARDERS
    // ==========================================
    val isExportingFullPdf: MutableStateFlow<Boolean> get() = pdfDelegate.isExportingFullPdf

    fun exportFullCookbookPdf(context: Context) = pdfDelegate.exportFullCookbookPdf(context)
    fun shareRecipeAsPdf(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ) = pdfDelegate.shareRecipeAsPdf(context, recipe, multiplier, unitSystem, includeLore, includeTips, includeNotes)
    fun generatePdfFile(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ): File = pdfDelegate.generatePdfFile(context, recipe, multiplier, unitSystem, includeLore, includeTips, includeNotes)
    fun savePdfToTargetUri(context: Context, targetUri: Uri, pdfFile: File): Boolean =
        pdfDelegate.savePdfToTargetUri(context, targetUri, pdfFile)

    // ==========================================
    // BACKUP & RESTORE FORWARDERS
    // ==========================================
    val isBackupDialogOpen: MutableStateFlow<Boolean> get() = backupDelegate.isBackupDialogOpen
    val backupStatusMessage: MutableStateFlow<String?> get() = backupDelegate.backupStatusMessage
    val pendingRestoreManifest: MutableStateFlow<BackupManifest?> get() = backupDelegate.pendingRestoreManifest
    val isRestoring: MutableStateFlow<Boolean> get() = backupDelegate.isRestoring
    val lastBackupDate: MutableStateFlow<String?> get() = backupDelegate.lastBackupDate
    val autoWeeklyBackupEnabled: MutableStateFlow<Boolean> get() = backupDelegate.autoWeeklyBackupEnabled
    val savedBackupsList: MutableStateFlow<List<SavedBackupFile>> get() = backupDelegate.savedBackupsList

    fun setAutoWeeklyBackupEnabled(enabled: Boolean) = backupDelegate.setAutoWeeklyBackupEnabled(enabled)
    fun checkAndPerformAutoWeeklyBackup() = backupDelegate.checkAndPerformAutoWeeklyBackup()
    fun refreshSavedBackups() = backupDelegate.refreshSavedBackups()
    fun createInstantBackup() = backupDelegate.createInstantBackup()
    fun deleteSavedBackup(file: File) = backupDelegate.deleteSavedBackup(file)
    fun directRestoreBackupFile(file: File, replaceExisting: Boolean, onComplete: () -> Unit = {}) =
        backupDelegate.directRestoreBackupFile(file, replaceExisting, onComplete)
    fun deleteAllRecipes(onComplete: () -> Unit = {}) = backupDelegate.deleteAllRecipes(onComplete)
    fun openBackupDialog() = backupDelegate.openBackupDialog()
    fun closeBackupDialog() = backupDelegate.closeBackupDialog()
    fun clearPendingRestore() = backupDelegate.clearPendingRestore()
    fun generateBackupJson(onReady: (String, Int) -> Unit) = backupDelegate.generateBackupJson(onReady)
    fun onBackupExportSuccess(count: Int) = backupDelegate.onBackupExportSuccess(count)
    fun shareBackup(context: Context) = backupDelegate.shareBackup(context)
    fun inspectBackupFile(context: Context, uri: Uri) = backupDelegate.inspectBackupFile(context, uri)
    fun inspectBackupText(text: String) = backupDelegate.inspectBackupText(text)
    fun restoreLocalSnapshot(replaceExisting: Boolean) = backupDelegate.restoreLocalSnapshot(replaceExisting)
    fun executeRestore(replaceExisting: Boolean, onComplete: (Int) -> Unit = {}) =
        backupDelegate.executeRestore(replaceExisting, onComplete)
    fun restoreStarterRecipes(replaceExisting: Boolean = false) = backupDelegate.restoreStarterRecipes(replaceExisting)

    // ==========================================
    // BATCH COVER GENERATION FORWARDERS
    // ==========================================
    val isGeneratingCover: MutableStateFlow<Boolean> get() = batchCoverDelegate.isGeneratingCover
    val generatingCoverRecipeId: MutableStateFlow<Long?> get() = batchCoverDelegate.generatingCoverRecipeId
    val coverGenerationError: MutableStateFlow<String?> get() = batchCoverDelegate.coverGenerationError
    val showCoverErrorDialog: MutableStateFlow<Boolean> get() = batchCoverDelegate.showCoverErrorDialog
    val lastCoverGenerationLog: MutableStateFlow<String?> get() = batchCoverDelegate.lastCoverGenerationLog
    val isBatchGeneratingCovers: MutableStateFlow<Boolean> get() = batchCoverDelegate.isBatchGeneratingCovers
    val batchCoverProgress: MutableStateFlow<Pair<Int, Int>> get() = batchCoverDelegate.batchCoverProgress
    val batchCoverCurrentTitle: MutableStateFlow<String> get() = batchCoverDelegate.batchCoverCurrentTitle
    val batchCoverSuccessCount: MutableStateFlow<Int> get() = batchCoverDelegate.batchCoverSuccessCount
    val batchCoverFailCount: MutableStateFlow<Int> get() = batchCoverDelegate.batchCoverFailCount
    val batchCoverIsCancelled: MutableStateFlow<Boolean> get() = batchCoverDelegate.batchCoverIsCancelled
    val batchCoverLog: MutableStateFlow<String?> get() = batchCoverDelegate.batchCoverLog
    val showBatchCoverDialog: MutableStateFlow<Boolean> get() = batchCoverDelegate.showBatchCoverDialog
    val selectedBatchFilter: MutableStateFlow<BatchCoverFilter> get() = batchCoverDelegate.selectedBatchFilter
    val recipesMissingPhotosCount: StateFlow<Int> get() = batchCoverDelegate.recipesMissingPhotosCount

    fun openBatchCoverDialog() = batchCoverDelegate.openBatchCoverDialog()
    fun closeBatchCoverDialog() = batchCoverDelegate.closeBatchCoverDialog()
    fun setBatchFilter(filter: BatchCoverFilter) = batchCoverDelegate.setBatchFilter(filter)
    fun startBatchGenerateMissingCovers(context: Context, customFilter: BatchCoverFilter? = null) =
        batchCoverDelegate.startBatchGenerateMissingCovers(context, customFilter)
    fun cancelBatchCoverGeneration() = batchCoverDelegate.cancelBatchCoverGeneration()
    fun clearCoverGenerationError() = batchCoverDelegate.clearCoverGenerationError()
    fun openCoverErrorDialog() = batchCoverDelegate.openCoverErrorDialog()
    fun closeCoverErrorDialog() = batchCoverDelegate.closeCoverErrorDialog()
    fun generateRecipeCoverArt(recipe: RecipeEntity, context: Context? = null, referenceBitmap: Bitmap? = null, customPrompt: String? = null) =
        batchCoverDelegate.generateRecipeCoverArt(recipe, context, referenceBitmap, customPrompt)

    // ==========================================
    // KITCHEN COOK, TTS, TIMER & JOURNAL FORWARDERS
    // ==========================================
    val isTtsReady: MutableStateFlow<Boolean> get() = kitchenCookDelegate.isTtsReady
    val isTtsSpeaking: MutableStateFlow<Boolean> get() = kitchenCookDelegate.isTtsSpeaking
    val timerSecondsRemaining: MutableStateFlow<Int> get() = kitchenCookDelegate.timerSecondsRemaining
    val timerTotalSeconds: MutableStateFlow<Int> get() = kitchenCookDelegate.timerTotalSeconds
    val isTimerActive: MutableStateFlow<Boolean> get() = kitchenCookDelegate.isTimerActive
    val servingMultiplier: MutableStateFlow<Float> get() = kitchenCookDelegate.servingMultiplier

    fun toggleSpeakStep(text: String, isGerman: Boolean, lang: LanguageMode = LanguageMode.ENGLISH) =
        kitchenCookDelegate.toggleSpeakStep(text, isGerman, lang)
    fun speakStep(text: String, isGerman: Boolean) = kitchenCookDelegate.speakStep(text, isGerman)
    fun stopTts() = kitchenCookDelegate.stopTts()
    fun startTimerForStep(minutes: Int) = kitchenCookDelegate.startTimerForStep(minutes)
    fun pauseTimer() = kitchenCookDelegate.pauseTimer()
    fun resumeTimer() = kitchenCookDelegate.resumeTimer()
    fun resetTimer() = kitchenCookDelegate.resetTimer()
    fun setServingMultiplier(multiplier: Float) = kitchenCookDelegate.setServingMultiplier(multiplier)
    fun incrementServingMultiplier() = kitchenCookDelegate.incrementServingMultiplier()
    fun decrementServingMultiplier() = kitchenCookDelegate.decrementServingMultiplier()
    fun playPageTurnSound() = kitchenCookDelegate.playPageTurnSound()
    fun saveRecipeJournal(recipeId: Long, notes: String, rating: Int) =
        kitchenCookDelegate.saveRecipeJournal(recipeId, notes, rating)
    fun markRecipeCooked() = kitchenCookDelegate.markRecipeCooked()

    // ==========================================
    // RECIPE SCANNER & DUPLICATE FORWARDERS
    // ==========================================
    val isScanning: MutableStateFlow<Boolean> get() = scanDelegate.isScanning
    val scannedDraftRecipe: MutableStateFlow<RecipeEntity?> get() = scanDelegate.scannedDraftRecipe
    val scanErrorMessage: MutableStateFlow<String?> get() = scanDelegate.scanErrorMessage
    val duplicatePrompt: MutableStateFlow<DuplicatePromptData?> get() = scanDelegate.duplicatePrompt
    val lastAutoSavedRecipe: MutableStateFlow<RecipeEntity?> get() = scanDelegate.lastAutoSavedRecipe
    val scanBatchSuccessEvent: MutableStateFlow<Long?> get() = scanDelegate.scanBatchSuccessEvent
    val scanBatchCount: MutableStateFlow<Int> get() = scanDelegate.scanBatchCount

    fun resetScanBatchSession() = scanDelegate.resetScanBatchSession()
    fun scanRecipe(bitmap: Bitmap?, rawText: String?, imageUri: String? = null) =
        scanDelegate.scanRecipe(bitmap, rawText, imageUri)
    fun scanRecipe(bitmaps: List<Bitmap>, rawText: String?, imageUri: String? = null) =
        scanDelegate.scanRecipe(bitmaps, rawText, imageUri)
    fun resolveDuplicateUpdate(prompt: DuplicatePromptData) = scanDelegate.resolveDuplicateUpdate(prompt)
    fun resolveDuplicateSaveAsCopy(prompt: DuplicatePromptData) = scanDelegate.resolveDuplicateSaveAsCopy(prompt)
    fun dismissDuplicatePrompt() = scanDelegate.dismissDuplicatePrompt()
    fun saveDraftRecipe(recipe: RecipeEntity) = scanDelegate.saveDraftRecipe(recipe)

    // ==========================================
    // TOTAL RECIPE COUNT & STATS
    // ==========================================
    val totalRecipeCount: StateFlow<Int> = repository.allRecipes.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
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
        started = SharingStarted.Lazily,
        initialValue = CookbookStats()
    )

    val recipePhotoStats: StateFlow<RecipePhotoStats> = repository.allRecipes.map { all ->
        RecipeImageClassifier.computeStats(all)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = RecipePhotoStats()
    )

    // Profile Recipe Counts Map
    val profileRecipeCounts: StateFlow<Map<String, Int>> = combine(
        repository.allRecipes,
        familyProfiles
    ) { all, profiles ->
        val counts = mutableMapOf<String, Int>()
        counts["All"] = all.size
        counts["All Family"] = all.size
        for (p in profiles) {
            counts[p] = all.count { it.profileName.equals(p, ignoreCase = true) }
        }
        counts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    // Unfiltered recipes for the currently active profile
    val activeProfileRecipes: StateFlow<List<RecipeEntity>> = combine(
        repository.allRecipes,
        activeProfile
    ) { all, profile ->
        if (profile == "All" || profile == "All Family") {
            all
        } else {
            all.filter { it.profileName.equals(profile, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val activeProfileTotalCount: StateFlow<Int> = activeProfileRecipes.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0
    )

    val activeProfileFavoritesCount: StateFlow<Int> = activeProfileRecipes.map { list ->
        list.count { it.isFavorite }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0
    )

    val allAvailableCategories: StateFlow<List<String>> = combine(
        categories,
        activeProfileRecipes
    ) { customCats, recipesList ->
        val foundCats = linkedSetOf<String>()
        customCats.forEach { if (it.isNotBlank()) foundCats.add(it.trim()) }
        recipesList.forEach { r ->
            extractCategoriesFromRecipe(r.category).forEach { c ->
                if (c.isNotBlank() && foundCats.none { it.equals(c, ignoreCase = true) }) {
                    foundCats.add(c)
                }
            }
        }
        foundCats.toList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = defaultCategories
    )

    val activeProfileCategoryCounts: StateFlow<Map<String, Int>> = activeProfileRecipes.map { list ->
        val counts = mutableMapOf<String, Int>()
        list.forEach { r ->
            val cats = extractCategoriesFromRecipe(r.category)
            for (cat in cats) {
                counts[cat] = (counts[cat] ?: 0) + 1
            }
        }
        counts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    val activeProfileDietaryCounts: StateFlow<Map<DietaryFilter, Int>> = activeProfileRecipes.map { list ->
        val counts = mutableMapOf<DietaryFilter, Int>()
        counts[DietaryFilter.ALL] = list.size
        var celiac = 0
        var gf = 0
        var veg = 0
        var vegan = 0
        var dairyFree = 0
        var nutFree = 0
        for (recipe in list) {
            val analysis = DietaryClassifier.analyze(recipe)
            if (analysis.isCeliacSafe) celiac++
            if (analysis.isGlutenFree) gf++
            if (analysis.isVegetarian) veg++
            if (analysis.isVegan) vegan++
            if (analysis.isDairyFree) dairyFree++
            if (analysis.isNutFree) nutFree++
        }
        counts[DietaryFilter.CELIAC_SAFE] = celiac
        counts[DietaryFilter.GLUTEN_FREE] = gf
        counts[DietaryFilter.VEGETARIAN] = veg
        counts[DietaryFilter.VEGAN] = vegan
        counts[DietaryFilter.DAIRY_FREE] = dairyFree
        counts[DietaryFilter.NUT_FREE] = nutFree
        counts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    fun setDietaryFilter(filter: DietaryFilter) {
        selectedDietaryFilter.value = filter
    }

    // Filtered recipes
    val recipes: StateFlow<List<RecipeEntity>> = combine(
        combine(activeProfileRecipes, searchQuery, selectedCategory) { all, q, c -> Triple(all, q, c) },
        onlyFavorites,
        selectedDietaryFilter
    ) { (all, query, category), favOnly, dietaryFilter ->
        val hasQuery = query.isNotBlank()
        val filtered = all.filter { recipe ->
            if (favOnly && !recipe.isFavorite) return@filter false
            if (dietaryFilter != DietaryFilter.ALL && !DietaryClassifier.matchesFilter(recipe, dietaryFilter)) return@filter false
            if (category != "All") {
                val matchesCategory = recipe.category.equals(category, ignoreCase = true) ||
                        extractCategoriesFromRecipe(recipe.category).any { it.equals(category, ignoreCase = true) }
                if (!matchesCategory) return@filter false
            }
            if (hasQuery && !FuzzySearchHelper.matches(recipe, query)) return@filter false
            true
        }

        if (hasQuery) {
            filtered.sortedByDescending { FuzzySearchHelper.score(it, query) }
        } else {
            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    // ==========================================
    // UI ACTIONS & NAVIGATION
    // ==========================================
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

    fun openNewRecipeEditor() {
        val targetProfile = if (activeProfile.value == "All" || activeProfile.value == "All Family") "Louis" else activeProfile.value
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
                RecipeIngredient(name = "", amount = "", unit = "", nameEnglish = "")
            ),
            steps = listOf(
                RecipeStep(stepNumber = 1, instructionEnglish = "", instructionGerman = "")
            ),
            notes = "",
            originStory = "",
            profileName = targetProfile
        )
        recipeEditorInitialTab.value = 0
        isRecipeEditorOpen.value = true
    }

    fun clearNavigateToRecipeEvent() {
        navigateToRecipeEvent.value = null
    }

    fun selectRecipe(recipe: RecipeEntity?) {
        selectedRecipe.value = recipe
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
                RecipeIngredient("Flour / Mehl", "500", "g", "Mehl", "Flour"),
                RecipeIngredient("Butter", "200", "g", "Butter", "Butter")
            ),
            steps = listOf(
                RecipeStep(1, "Preheat oven and mix ingredients.", "Den Ofen vorheizen und die Zutaten vermengen.", 0)
            ),
            originStory = "Cherished family recipe & formula."
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
                    if (inserted.imageUri.isNullOrBlank()) {
                        generateRecipeCoverArt(inserted, getApplication())
                    }
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

    fun updateCoverTheme(theme: CoverTheme) {
        val current = selectedRecipe.value ?: return
        val updated = current.copy(coverTheme = theme.name)
        selectedRecipe.value = updated
        viewModelScope.launch {
            repository.updateRecipe(updated)
        }
    }

    fun openCustomPromptDialog(recipe: RecipeEntity) {
        customPromptDialogRecipe.value = recipe
    }

    fun closeCustomPromptDialog() {
        customPromptDialogRecipe.value = null
    }

    fun removeRecipeCoverPhoto(recipe: RecipeEntity) {
        viewModelScope.launch {
            val updated = recipe.copy(imageUri = null)
            repository.updateRecipe(updated)
            selectedRecipe.value = updated
        }
    }

    fun rotateOriginalCardPhoto(recipe: RecipeEntity) {
        val targetPath = recipe.originalCardPhotoUri ?: recipe.imageUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val rotated = ImageUtils.rotateImageFile(targetPath, 90f)
            if (rotated) {
                val updated = recipe.copy(updatedAt = System.currentTimeMillis())
                repository.updateRecipe(updated)
                selectedRecipe.value = updated
            }
        }
    }

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

    override fun onCleared() {
        super.onCleared()
        kitchenCookDelegate.onCleared()
    }
}
