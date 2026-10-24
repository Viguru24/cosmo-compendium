package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecipeEntity
import com.example.data.repository.RecipeRepository
import com.example.ui.util.BatchCoverFilter
import com.example.ui.util.RecipeImageClassifier
import com.example.ui.util.getDisplayTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Delegate responsible for queued single and batch AI cover generation.
 */
class RecipeBatchCoverDelegate(
    private val vm: RecipeViewModel,
    private val app: Application,
    private val repository: RecipeRepository
) {
    data class CoverGenTask(
        val recipeId: Long,
        val referenceBitmap: Bitmap? = null,
        val customPrompt: String? = null
    )

    private val coverGenQueue = Channel<CoverGenTask>(Channel.UNLIMITED)
    val isGeneratingCover = MutableStateFlow(false)
    val generatingCoverRecipeId = MutableStateFlow<Long?>(null)
    val coverGenerationError = MutableStateFlow<String?>(null)
    val showCoverErrorDialog = MutableStateFlow(false)
    val lastCoverGenerationLog = MutableStateFlow<String?>(null)

    val isBatchGeneratingCovers = MutableStateFlow(false)
    val batchCoverProgress = MutableStateFlow(Pair(0, 0)) // current, total
    val batchCoverCurrentTitle = MutableStateFlow("")
    val batchCoverSuccessCount = MutableStateFlow(0)
    val batchCoverFailCount = MutableStateFlow(0)
    val batchCoverIsCancelled = MutableStateFlow(false)
    val batchCoverLog = MutableStateFlow<String?>(null)
    val showBatchCoverDialog = MutableStateFlow(false)
    val selectedBatchFilter = MutableStateFlow(BatchCoverFilter.MISSING_AI_PHOTOS)

    val recipesMissingPhotosCount: StateFlow<Int> = vm.recipes.map { list ->
        list.count { RecipeImageClassifier.isMissingAiPhoto(it.imageUri) }
    }.stateIn(vm.viewModelScope, SharingStarted.Lazily, 0)

    init {
        vm.viewModelScope.launch(Dispatchers.IO) {
            for (task in coverGenQueue) {
                processQueuedCoverGeneration(task)
            }
        }
    }

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
        vm.viewModelScope.launch(Dispatchers.IO) {
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

            batchCoverLog.value = "Starting batch photo generation for $total recipes (${filterToUse.shortLabel}) using ${vm.imageGenEngine.value.displayName}..."

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

                    var refBitmap: Bitmap? = null
                    if (!recipe.imageUri.isNullOrBlank() && File(recipe.imageUri).exists()) {
                        try {
                            refBitmap = BitmapFactory.decodeFile(recipe.imageUri)
                        } catch (_: Exception) {}
                    }

                    val result = if (vm.imageGenEngine.value == com.example.ai.ImageGenEngine.COMFY_UI) {
                        com.example.ai.ComfyUiClient.generateRecipeImage(
                            baseUrl = vm.comfyUiUrl.value,
                            title = title,
                            titleGerman = recipe.titleGerman,
                            category = recipe.category,
                            ingredients = ingList,
                            steps = stepsList,
                            notes = recipe.notes.ifBlank { recipe.originStory },
                            referenceBitmap = refBitmap,
                            customCheckPoint = vm.comfyUiCheckpoint.value,
                            customWorkflowJson = vm.comfyUiCustomWorkflow.value.ifBlank { null }
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

    private suspend fun processQueuedCoverGeneration(task: CoverGenTask) {
        val recipe = repository.getRecipeDirect(task.recipeId) ?: return
        withContext(Dispatchers.Main) {
            isGeneratingCover.value = true
            generatingCoverRecipeId.value = task.recipeId
            coverGenerationError.value = null
            lastCoverGenerationLog.value = "Starting image generation for '${recipe.getDisplayTitle()}' using ${vm.imageGenEngine.value.displayName}..."
        }

        try {
            val refBitmap = task.referenceBitmap
            val ingList = recipe.ingredients.map { "${it.amount} ${it.unit} ${it.name}".trim() }
            val stepsList = recipe.steps.take(4).map { it.getInstruction() }
            val title = recipe.getDisplayTitle()

            val result = if (vm.imageGenEngine.value == com.example.ai.ImageGenEngine.COMFY_UI) {
                withContext(Dispatchers.Main) {
                    lastCoverGenerationLog.value = "Connecting to ComfyUI for '$title' at ${vm.comfyUiUrl.value}..."
                }
                val comfyRes = com.example.ai.ComfyUiClient.generateRecipeImage(
                    baseUrl = vm.comfyUiUrl.value,
                    title = title,
                    titleGerman = recipe.titleGerman,
                    category = recipe.category,
                    ingredients = ingList,
                    steps = stepsList,
                    notes = recipe.notes.ifBlank { recipe.originStory },
                    referenceBitmap = refBitmap,
                    customCheckPoint = vm.comfyUiCheckpoint.value,
                    customWorkflowJson = vm.comfyUiCustomWorkflow.value.ifBlank { null },
                    customPrompt = task.customPrompt
                )
                if (comfyRes.isFailure) {
                    val comfyErr = comfyRes.exceptionOrNull()?.localizedMessage ?: "ComfyUI generation failed"
                    withContext(Dispatchers.Main) {
                        lastCoverGenerationLog.value = "✗ ComfyUI failed: $comfyErr"
                    }
                }
                comfyRes
            } else {
                withContext(Dispatchers.Main) {
                    lastCoverGenerationLog.value = "Sending image generation request for '$title' to Google Cloud AI..."
                }
                com.example.ai.GeminiClient.generateRecipeCoverImage(
                    title = title,
                    titleGerman = recipe.titleGerman,
                    category = recipe.category,
                    ingredients = ingList,
                    steps = stepsList,
                    notes = recipe.notes.ifBlank { recipe.originStory },
                    referenceBitmap = refBitmap,
                    customPrompt = task.customPrompt
                )
            }

            if (result.isSuccess) {
                val bitmap = result.getOrNull()
                if (bitmap != null) {
                    val file = File(app.filesDir, "recipe_cover_${recipe.id}_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    val updated = recipe.copy(
                        imageUri = file.absolutePath,
                        coverPhotoName = file.name,
                        originalCardPhotoUri = recipe.originalCardPhotoUri ?: (if (recipe.imageUri != null && !recipe.imageUri.contains("recipe_cover_")) recipe.imageUri else null),
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateRecipe(updated)
                    withContext(Dispatchers.Main) {
                        if (vm.selectedRecipe.value?.id == recipe.id) {
                            vm.selectedRecipe.value = updated
                        }
                        lastCoverGenerationLog.value = "✓ Image generated and saved successfully for '$title' (${file.length() / 1024} KB)"
                    }
                }
            } else {
                val errMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to generate cover photo"
                withContext(Dispatchers.Main) {
                    coverGenerationError.value = errMsg
                    lastCoverGenerationLog.value = "✗ Error generating photo for '$title': $errMsg"
                    showCoverErrorDialog.value = true
                }
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Error generating cover photo"
            withContext(Dispatchers.Main) {
                coverGenerationError.value = errMsg
                lastCoverGenerationLog.value = "✗ Exception generating photo for '${recipe.getDisplayTitle()}': $errMsg"
                showCoverErrorDialog.value = true
            }
        } finally {
            withContext(Dispatchers.Main) {
                generatingCoverRecipeId.value = null
                isGeneratingCover.value = false
            }
        }
    }

    fun generateRecipeCoverArt(recipe: RecipeEntity, context: Context? = null, referenceBitmap: Bitmap? = null, customPrompt: String? = null) {
        coverGenQueue.trySend(CoverGenTask(
            recipeId = recipe.id,
            referenceBitmap = referenceBitmap,
            customPrompt = customPrompt
        ))
    }
}
