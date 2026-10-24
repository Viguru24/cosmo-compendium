package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecipeEntity
import com.example.data.local.ShoppingItemEntity
import com.example.data.model.UnitSystem
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Delegate responsible for shopping and supplies list state and actions.
 */
class RecipeShoppingDelegate(
    private val vm: RecipeViewModel,
    private val repository: RecipeRepository
) {
    val isShoppingListOpen = MutableStateFlow(false)

    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.allShoppingItems.stateIn(
        scope = vm.viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val uncheckedShoppingCount: StateFlow<Int> = repository.uncheckedShoppingCount.stateIn(
        scope = vm.viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0
    )

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
        vm.viewModelScope.launch {
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
        vm.viewModelScope.launch {
            repository.toggleShoppingItemChecked(item.id, !item.isChecked)
        }
    }

    fun addCustomShoppingItem(name: String, amount: String, unit: String, category: String? = null) {
        vm.viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addCustomShoppingItem(name, amount, unit, category)
            }
        }
    }

    fun addShoppingItem(name: String, amount: String, unit: String, category: String? = null) {
        addCustomShoppingItem(name, amount, unit, category)
    }

    fun deleteShoppingItem(item: ShoppingItemEntity) {
        vm.viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearCompletedShoppingItems() {
        vm.viewModelScope.launch {
            repository.clearCompletedShoppingItems()
        }
    }

    fun clearAllShoppingItems() {
        vm.viewModelScope.launch {
            repository.clearAllShoppingItems()
        }
    }

    fun shareShoppingList(context: Context) {
        val items = shoppingItems.value
        if (items.isEmpty()) return

        val text = buildString {
            appendLine("🛒 SUPPLIES & GROCERY LIST")
            appendLine("Cosmo Cookbook")
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
            appendLine("Shared from Cosmo Cookbook")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Supplies & Ingredients")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Grocery List via..."))
    }
}
