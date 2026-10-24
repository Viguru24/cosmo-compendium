package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecipeEntity
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

typealias DuplicatePromptData = RecipeViewModel.DuplicatePromptData

/**
 * Delegate responsible for recipe card scanning, OCR parsing, duplicate detection,
 * duplicate resolution (merge / save as copy), and batch scanning sessions.
 */
class RecipeScanDelegate(
    private val vm: RecipeViewModel,
    private val app: Application,
    private val repository: RecipeRepository
) {

    val isScanning = MutableStateFlow(false)
    val scannedDraftRecipe = MutableStateFlow<RecipeEntity?>(null)
    val scanErrorMessage = MutableStateFlow<String?>(null)

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
        vm.viewModelScope.launch {
            isScanning.value = true
            scanErrorMessage.value = null
            try {
                val parsed = repository.scanAndProcessRecipe(bitmaps, rawText, imageUri)
                val targetProfile = if (vm.activeProfile.value == "All" || vm.activeProfile.value == "All Family") "Louis" else vm.activeProfile.value
                val parsedWithProfile = parsed.copy(profileName = targetProfile)
                val duplicate = repository.findDuplicateRecipe(parsedWithProfile)
                if (duplicate != null) {
                    duplicatePrompt.value = DuplicatePromptData(
                        existingRecipe = duplicate,
                        scannedRecipe = parsedWithProfile,
                        rawBitmaps = bitmaps
                    )
                } else {
                    // Auto-save recipe directly into the database for continuous rapid scanning
                    val id = repository.insertRecipe(parsedWithProfile)
                    val inserted = repository.getRecipeDirect(id) ?: parsedWithProfile.copy(id = id)
                    lastAutoSavedRecipe.value = inserted
                    scanBatchCount.value += 1
                    scannedDraftRecipe.value = null
                    scanBatchSuccessEvent.value = System.currentTimeMillis()

                    // Automatically generate rich AI dish cover art in background upon taking picture/scanning
                    vm.generateRecipeCoverArt(inserted, app)
                }
            } catch (t: Throwable) {
                scanErrorMessage.value = "Scanning error: ${t.localizedMessage ?: "Unable to read recipe"}"
            } finally {
                isScanning.value = false
            }
        }
    }

    fun resolveDuplicateUpdate(prompt: DuplicatePromptData) {
        vm.viewModelScope.launch {
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
        vm.viewModelScope.launch {
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
            vm.generateRecipeCoverArt(inserted)
        }
    }

    fun dismissDuplicatePrompt() {
        duplicatePrompt.value = null
    }

    fun saveDraftRecipe(recipe: RecipeEntity) {
        vm.viewModelScope.launch {
            val id = repository.insertRecipe(recipe)
            val inserted = repository.getRecipeDirect(id) ?: recipe.copy(id = id)
            scannedDraftRecipe.value = null
            vm.selectRecipe(inserted)
            vm.navigateToRecipeEvent.value = inserted
            if (inserted.imageUri.isNullOrBlank()) {
                vm.generateRecipeCoverArt(inserted)
            }
        }
    }
}
