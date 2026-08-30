package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.ai.GeminiClient
import com.example.ai.ParsedIngredientDto
import com.example.ai.ParsedRecipeDto
import com.example.ai.ParsedStepDto
import com.example.data.local.RecipeDao
import com.example.data.local.RecipeEntity
import com.example.data.local.ShoppingDao
import com.example.data.local.ShoppingItemEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.data.model.ShoppingCategorizer
import com.example.data.model.UnitSystem
import com.example.data.sync.SyncConfig
import com.example.data.sync.SyncManager
import com.example.data.sync.SyncResult
import com.example.ui.util.ImageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class RecipeRepository(
    private val context: Context? = null,
    private val recipeDao: RecipeDao,
    private val shoppingDao: ShoppingDao
) {

    val syncConfig: SyncConfig? = context?.let { SyncConfig(it) }
    val syncManager: SyncManager? = if (context != null && syncConfig != null) {
        SyncManager(context, recipeDao, syncConfig)
    } else null

    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()
    val favoriteRecipes: Flow<List<RecipeEntity>> = recipeDao.getFavoriteRecipes()

    // Shopping List Flows
    val allShoppingItems: Flow<List<ShoppingItemEntity>> = shoppingDao.getAllItemsFlow()
    val uncheckedShoppingCount: Flow<Int> = shoppingDao.getUncheckedCountFlow()

    suspend fun addRecipeIngredientsToShoppingList(
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        selectedIndices: Set<Int>? = null
    ): Int {
        val itemsToAdd = mutableListOf<ShoppingItemEntity>()
        recipe.ingredients.forEachIndexed { index, ing ->
            if (selectedIndices == null || selectedIndices.contains(index)) {
                val displayName = ing.getDisplayName()
                val scaledAmount = try {
                    val rawVal = ing.amount.trim().toDoubleOrNull()
                    if (rawVal != null && multiplier != 1.0f) {
                        val scaled = rawVal * multiplier
                        if (scaled % 1.0 == 0.0) scaled.toInt().toString() else String.format(Locale.US, "%.1f", scaled)
                    } else ing.amount
                } catch (e: Exception) {
                    ing.amount
                }

                val category = ShoppingCategorizer.categorizeIngredient(displayName)
                itemsToAdd.add(
                    ShoppingItemEntity(
                        recipeId = recipe.id,
                        recipeTitle = recipe.title,
                        name = displayName,
                        amount = scaledAmount,
                        unit = ing.unit,
                        isChecked = false,
                        category = category
                    )
                )
            }
        }

        if (itemsToAdd.isNotEmpty()) {
            shoppingDao.insertItems(itemsToAdd)
        }
        return itemsToAdd.size
    }

    suspend fun addCustomShoppingItem(
        name: String,
        amount: String = "",
        unit: String = "",
        category: String? = null
    ): Long {
        val cat = category?.takeIf { it.isNotBlank() } ?: ShoppingCategorizer.categorizeIngredient(name)
        return shoppingDao.insertItem(
            ShoppingItemEntity(
                name = name.trim(),
                amount = amount.trim(),
                unit = unit.trim(),
                category = cat,
                isChecked = false
            )
        )
    }

    suspend fun toggleShoppingItemChecked(id: Long, isChecked: Boolean) {
        shoppingDao.setChecked(id, isChecked)
    }

    suspend fun deleteShoppingItem(item: ShoppingItemEntity) {
        shoppingDao.deleteItem(item)
    }

    suspend fun clearCompletedShoppingItems() {
        shoppingDao.clearCompletedItems()
    }

    suspend fun clearAllShoppingItems() {
        shoppingDao.clearAllItems()
    }

    fun getRecipeById(id: Long): Flow<RecipeEntity?> = recipeDao.getRecipeById(id)

    suspend fun getRecipeDirect(id: Long): RecipeEntity? = recipeDao.getRecipeDirect(id)

    fun searchRecipes(query: String): Flow<List<RecipeEntity>> {
        if (query.isBlank()) return allRecipes
        return allRecipes.map { list ->
            list.filter { recipe ->
                fuzzyMatch(recipe, query.trim().lowercase())
            }
        }
    }

    private fun fuzzyMatch(recipe: RecipeEntity, q: String): Boolean {
        // Direct containment in titles
        if (recipe.title.lowercase().contains(q)) return true
        if (recipe.titleGerman.lowercase().contains(q)) return true
        if (recipe.titleEnglish.lowercase().contains(q)) return true
        if (recipe.category.lowercase().contains(q)) return true
        if (recipe.notes.lowercase().contains(q)) return true
        if (recipe.notesGerman.lowercase().contains(q)) return true

        // Ingredients match (German and English names)
        for (ing in recipe.ingredients) {
            if (ing.name.lowercase().contains(q)) return true
            if (ing.nameGerman?.lowercase()?.contains(q) == true) return true
            if (ing.nameEnglish?.lowercase()?.contains(q) == true) return true
        }

        // Steps match
        for (step in recipe.steps) {
            if (step.instructionEnglish.lowercase().contains(q)) return true
            if (step.instructionGerman.lowercase().contains(q)) return true
        }

        // Levenshtein fuzzy match on title words
        val queryTokens = q.split(" ").filter { it.isNotBlank() }
        val titleTokens = (recipe.title + " " + recipe.titleGerman + " " + recipe.titleEnglish)
            .lowercase()
            .split(" ", "-", "/", ",")
            .filter { it.isNotBlank() }

        for (qt in queryTokens) {
            val hasCloseMatch = titleTokens.any { tt ->
                levenshteinDistance(qt, tt) <= if (qt.length > 5) 2 else if (qt.length > 3) 1 else 0
            }
            if (hasCloseMatch) return true
        }

        return false
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    suspend fun getRecipeCount(): Int = recipeDao.getCount()

    suspend fun restoreDefaultRecipes(replaceExisting: Boolean = false) {
        if (replaceExisting) {
            recipeDao.deleteAll()
        }
        val defaultList = com.example.data.local.DefaultRecipes.getInitialRecipes()
        defaultList.forEach { recipe ->
            recipeDao.insertRecipe(recipe.copy(id = 0))
        }
    }

    suspend fun insertRecipe(recipe: RecipeEntity): Long {
        val now = System.currentTimeMillis()
        val prepared = recipe.copy(
            updatedAt = now,
            isDeleted = false,
            syncStatus = "PENDING"
        )
        val id = recipeDao.insertRecipe(prepared)
        syncManager?.triggerImmediateSync()
        return id
    }

    suspend fun insertAll(recipes: List<RecipeEntity>) {
        val now = System.currentTimeMillis()
        recipes.forEach { recipe ->
            recipeDao.insertRecipe(
                recipe.copy(
                    id = 0,
                    updatedAt = now,
                    isDeleted = false,
                    syncStatus = "PENDING"
                )
            )
        }
        syncManager?.triggerImmediateSync()
    }

    suspend fun getAllRecipesDirect(): List<RecipeEntity> = recipeDao.getAllRecipesDirect()

    suspend fun deleteAllRecipes() = recipeDao.deleteAll()

    suspend fun restoreRecipes(recipes: List<RecipeEntity>, replaceExisting: Boolean) {
        if (replaceExisting) {
            recipeDao.deleteAll()
        }
        val now = System.currentTimeMillis()
        recipes.forEach { recipe ->
            val cleanTitle = recipe.title.ifBlank {
                recipe.titleEnglish.ifBlank {
                    recipe.titleGerman.ifBlank { "Restored Recipe" }
                }
            }
            val titleGerman = recipe.titleGerman.ifBlank { cleanTitle }
            val titleEnglish = recipe.titleEnglish.ifBlank { cleanTitle }
            
            recipeDao.insertRecipe(
                recipe.copy(
                    id = 0,
                    title = cleanTitle,
                    titleGerman = titleGerman,
                    titleEnglish = titleEnglish,
                    updatedAt = now,
                    isDeleted = false,
                    syncStatus = "PENDING"
                )
            )
        }
        syncManager?.triggerImmediateSync()
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        val updated = recipe.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        recipeDao.updateRecipe(updated)
        syncManager?.triggerImmediateSync()
    }

    suspend fun updateCategoryName(oldCategory: String, newCategory: String) = recipeDao.updateCategoryName(oldCategory, newCategory)

    suspend fun deleteRecipe(recipe: RecipeEntity) {
        if (syncConfig?.isSyncEnabled == true) {
            // Soft delete (isDeleted = true) so it propagates cleanly during delta sync to VPS
            recipeDao.softDeleteRecipe(recipe.id)
            syncManager?.triggerImmediateSync()
        } else {
            recipeDao.deleteRecipe(recipe)
        }
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        recipeDao.updateFavorite(id, isFavorite)
        syncManager?.triggerImmediateSync()
    }

    suspend fun incrementCooked(id: Long) {
        recipeDao.incrementCooked(id)
        syncManager?.triggerImmediateSync()
    }

    suspend fun scanAndProcessRecipe(
        imageBitmap: Bitmap?,
        recipeText: String?,
        imageUri: String? = null
    ): RecipeEntity {
        val bitmaps = if (imageBitmap != null) listOf(imageBitmap) else emptyList()
        return scanAndProcessRecipe(bitmaps, recipeText, imageUri)
    }

    suspend fun scanAndProcessRecipe(
        imageBitmaps: List<Bitmap>,
        recipeText: String?,
        imageUri: String? = null
    ): RecipeEntity {
        val result = GeminiClient.parseRecipeWithAi(imageBitmaps, recipeText)
        val dto = result.getOrNull() ?: ParsedRecipeDto()

        val enTitle = dto.titleEnglish?.takeIf { it.isNotBlank() }
            ?: dto.title?.takeIf { it.isNotBlank() }
            ?: dto.name?.takeIf { it.isNotBlank() }
            ?: dto.titleGerman?.takeIf { it.isNotBlank() }
            ?: "Recipe"
        val displayTitle = enTitle

        // Detect and crop ONLY the food photograph from the scanned page
        var finalFoodImageUri: String? = null
        if (context != null && dto.hasFoodPhoto == true && dto.foodPhotoBox != null && imageBitmaps.isNotEmpty()) {
            val pageIdx = (dto.foodPhotoBox.pageIndex ?: 0).coerceIn(0, imageBitmaps.size - 1)
            val sourceBitmap = imageBitmaps[pageIdx]
            val ymin = dto.foodPhotoBox.ymin ?: 0
            val xmin = dto.foodPhotoBox.xmin ?: 0
            val ymax = dto.foodPhotoBox.ymax ?: 1000
            val xmax = dto.foodPhotoBox.xmax ?: 1000
            finalFoodImageUri = ImageUtils.cropAndSaveFoodPhoto(context, sourceBitmap, ymin, xmin, ymax, xmax)
        }

        val ingredientsList = dto.ingredients?.mapNotNull { ing ->
            val name = ing.nameEnglish?.takeIf { it.isNotBlank() }
                ?: ing.name?.takeIf { it.isNotBlank() }
                ?: ing.ingredient?.takeIf { it.isNotBlank() }
                ?: ing.item?.takeIf { it.isNotBlank() }
                ?: ing.nameGerman?.takeIf { it.isNotBlank() }
                ?: ing.raw?.takeIf { it.isNotBlank() }

            val amt = ing.amount?.takeIf { it.isNotBlank() }
                ?: ing.quantity?.takeIf { it.isNotBlank() }
                ?: ing.qty?.takeIf { it.isNotBlank() }
                ?: ""

            val unitStr = ing.unit?.takeIf { it.isNotBlank() }
                ?: ing.measurement?.takeIf { it.isNotBlank() }
                ?: ing.measure?.takeIf { it.isNotBlank() }
                ?: ""

            if (name.isNullOrBlank() && amt.isBlank() && unitStr.isBlank()) {
                null
            } else {
                val finalName = name ?: "Ingredient"
                RecipeIngredient(
                    name = finalName,
                    amount = amt,
                    unit = unitStr,
                    nameGerman = ing.nameGerman ?: finalName,
                    nameEnglish = ing.nameEnglish ?: finalName,
                    isOptional = ing.isOptional ?: false,
                    group = ing.group
                )
            }
        } ?: emptyList()

        val stepsList = dto.steps?.mapIndexedNotNull { index, step ->
            val instr = step.instructionEnglish?.takeIf { it.isNotBlank() }
                ?: step.instruction?.takeIf { it.isNotBlank() }
                ?: step.step?.takeIf { it.isNotBlank() }
                ?: step.text?.takeIf { it.isNotBlank() }
                ?: step.description?.takeIf { it.isNotBlank() }
                ?: step.instructionGerman?.takeIf { it.isNotBlank() }

            if (instr.isNullOrBlank()) {
                null
            } else {
                RecipeStep(
                    stepNumber = step.stepNumber ?: (index + 1),
                    instructionEnglish = instr,
                    instructionGerman = step.instructionGerman ?: instr,
                    timerMinutes = step.timerMinutes ?: 0,
                    tip = step.tip
                )
            }
        } ?: emptyList()

        return RecipeEntity(
            title = displayTitle,
            titleGerman = displayTitle,
            titleEnglish = displayTitle,
            category = dto.category ?: "Family Classics",
            servings = dto.servings ?: "4-6 servings",
            prepTimeMinutes = dto.prepTimeMinutes ?: 20,
            cookTimeMinutes = dto.cookTimeMinutes ?: 30,
            difficulty = dto.difficulty ?: "Medium",
            ingredients = ingredientsList,
            steps = stepsList,
            notes = dto.notesEnglish?.takeIf { it.isNotBlank() } ?: dto.notesGerman ?: "",
            notesGerman = dto.notesEnglish?.takeIf { it.isNotBlank() } ?: dto.notesGerman ?: "",
            sourceLanguage = "en",
            coverTheme = if (dto.category?.contains("Baking", ignoreCase = true) == true) "FLORAL_LINEN" else "VINTAGE_LEATHER",
            isFavorite = false,
            rating = 5,
            originStory = "Scanned recipe.",
            imageUri = finalFoodImageUri ?: imageUri,
            createdAt = System.currentTimeMillis()
        )
    }

    suspend fun deduplicateCollection() {
        val all = recipeDao.getAllRecipesDirect()
        val seen = mutableListOf<RecipeEntity>()
        for (recipe in all) {
            val duplicate = seen.firstOrNull { existing ->
                normalizeTitle(existing.title) == normalizeTitle(recipe.title) ||
                (existing.titleEnglish.isNotBlank() && existing.titleEnglish.equals(recipe.titleEnglish, ignoreCase = true)) ||
                (existing.titleGerman.isNotBlank() && existing.titleGerman.equals(recipe.titleGerman, ignoreCase = true)) ||
                existing.title.equals(recipe.titleEnglish, ignoreCase = true) ||
                recipe.title.equals(existing.titleEnglish, ignoreCase = true)
            }

            if (duplicate != null) {
                // Merge richer info into the duplicate and hard delete the redundant recipe
                val merged = duplicate.copy(
                    titleEnglish = if (duplicate.titleEnglish.isNotBlank()) duplicate.titleEnglish else recipe.titleEnglish,
                    titleGerman = if (duplicate.titleGerman.isNotBlank()) duplicate.titleGerman else recipe.titleGerman,
                    imageUri = duplicate.imageUri ?: recipe.imageUri,
                    coverPhotoName = duplicate.coverPhotoName ?: recipe.coverPhotoName,
                    notes = if (duplicate.notes.isNotBlank()) duplicate.notes else recipe.notes,
                    notesGerman = if (duplicate.notesGerman.isNotBlank()) duplicate.notesGerman else recipe.notesGerman,
                    originStory = if (duplicate.originStory.isNotBlank()) duplicate.originStory else recipe.originStory,
                    isFavorite = duplicate.isFavorite || recipe.isFavorite,
                    timesCooked = maxOf(duplicate.timesCooked, recipe.timesCooked)
                )
                recipeDao.updateRecipe(merged)
                recipeDao.hardDeleteRecipe(recipe.id)
            } else {
                seen.add(recipe)
            }
        }
    }

    suspend fun findDuplicateRecipe(candidate: RecipeEntity): RecipeEntity? {
        val all = recipeDao.getAllRecipesDirect()
        val cleanCandidateTitle = normalizeTitle(candidate.title)

        for (existing in all) {
            if (existing.id == candidate.id) continue
            val cleanExistingTitle = normalizeTitle(existing.title)

            // 1. Exact or strongly matched normalized title
            if (cleanCandidateTitle.isNotBlank() && cleanCandidateTitle == cleanExistingTitle) {
                return existing
            }

            // 2. Direct equality on titleEnglish or titleGerman or title
            if (candidate.titleEnglish.isNotBlank() && candidate.titleEnglish.equals(existing.titleEnglish, ignoreCase = true)) {
                return existing
            }
            if (candidate.titleGerman.isNotBlank() && candidate.titleGerman.equals(existing.titleGerman, ignoreCase = true)) {
                return existing
            }
            if (candidate.title.isNotBlank() && candidate.title.equals(existing.title, ignoreCase = true)) {
                return existing
            }

            // 3. Substring matching for distinctive heirloom titles (e.g. "Bread & Butter Pudding")
            if (cleanCandidateTitle.length >= 6 && cleanExistingTitle.length >= 6) {
                if (cleanCandidateTitle.contains(cleanExistingTitle) || cleanExistingTitle.contains(cleanCandidateTitle)) {
                    return existing
                }
            }

            // 4. High ingredient list overlap similarity check (if both have >= 3 ingredients)
            if (candidate.ingredients.size >= 3 && existing.ingredients.size >= 3) {
                val candidateIngs = candidate.ingredients.map { normalizeIngName(it.name) }.filter { it.length >= 3 }.toSet()
                val existingIngs = existing.ingredients.map { normalizeIngName(it.name) }.filter { it.length >= 3 }.toSet()
                if (candidateIngs.isNotEmpty() && existingIngs.isNotEmpty()) {
                    val intersection = candidateIngs.intersect(existingIngs)
                    val overlapRatio = intersection.size.toFloat() / minOf(candidateIngs.size, existingIngs.size)
                    if (overlapRatio >= 0.70f) {
                        return existing
                    }
                }
            }
        }
        return null
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("(?i)grandma's|grandmas|omas|oma's|traditional|vintage|classic|authentic|homemade|recipe|card"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }

    private fun normalizeIngName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
