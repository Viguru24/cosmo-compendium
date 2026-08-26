package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val shoppingDao: ShoppingDao
) {

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
        recipeDao.insertAll(com.example.data.local.DefaultRecipes.getInitialRecipes())
    }

    suspend fun insertRecipe(recipe: RecipeEntity): Long = recipeDao.insertRecipe(recipe)

    suspend fun insertAll(recipes: List<RecipeEntity>) = recipeDao.insertAll(recipes)

    suspend fun getAllRecipesDirect(): List<RecipeEntity> = recipeDao.getAllRecipesDirect()

    suspend fun restoreRecipes(recipes: List<RecipeEntity>, replaceExisting: Boolean) {
        if (replaceExisting) {
            recipeDao.deleteAll()
        }
        // If replacing or merging, generate clean IDs if needed so autoincrement doesn't conflict
        val sanitized = if (replaceExisting) {
            recipes
        } else {
            recipes.map { it.copy(id = 0) }
        }
        recipeDao.insertAll(sanitized)
    }

    suspend fun updateRecipe(recipe: RecipeEntity) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = recipeDao.updateFavorite(id, isFavorite)

    suspend fun incrementCooked(id: Long) = recipeDao.incrementCooked(id)

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

        val enTitle = dto.titleEnglish?.takeIf { it.isNotBlank() } ?: dto.titleGerman?.takeIf { it.isNotBlank() } ?: "Grandma's Family Recipe"
        val displayTitle = enTitle

        val ingredientsList = dto.ingredients?.map { ing ->
            val name = ing.nameEnglish?.takeIf { it.isNotBlank() } ?: ing.nameGerman ?: "Ingredient"
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

        val stepsList = dto.steps?.mapIndexed { index, step ->
            val instr = step.instructionEnglish?.takeIf { it.isNotBlank() } ?: step.instructionGerman ?: ""
            RecipeStep(
                stepNumber = step.stepNumber ?: (index + 1),
                instructionEnglish = instr,
                instructionGerman = instr,
                timerMinutes = step.timerMinutes ?: 0,
                tip = step.tip
            )
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
            originStory = "Scanned and translated from family recipe card.",
            imageUri = imageUri,
            createdAt = System.currentTimeMillis()
        )
    }
}
