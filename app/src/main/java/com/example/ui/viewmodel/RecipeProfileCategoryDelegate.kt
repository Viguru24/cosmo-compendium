package com.example.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecipeEntity
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Delegate responsible for managing Family Profiles ("Who's Cooking?") and Custom Recipe Categories.
 */
class RecipeProfileCategoryDelegate(
    private val vm: RecipeViewModel,
    private val repository: RecipeRepository,
    private val prefs: SharedPreferences
) {
    val defaultProfiles = listOf("Louis", "Annette", "Isabel")

    fun loadProfilesFromPrefs(): List<String> {
        val stored = prefs.getString("pref_family_profiles", null)
        val rawList = if (stored != null) {
            try {
                stored.split("|||").map { it.trim() }.filter { it.isNotBlank() }
            } catch (e: Exception) {
                defaultProfiles
            }
        } else {
            defaultProfiles
        }
        val migrated = rawList.map { name ->
            when {
                name.equals("Wife", ignoreCase = true) -> "Annette"
                name.equals("Daughter", ignoreCase = true) -> "Isabel"
                else -> name
            }
        }.distinct()
        val finalList = if (migrated.isNotEmpty()) migrated else defaultProfiles
        if (finalList != rawList) {
            saveProfiles(finalList)
        }
        return finalList
    }

    val familyProfiles = MutableStateFlow<List<String>>(loadProfilesFromPrefs())
    val defaultProfile = MutableStateFlow<String>(
        run {
            val raw = prefs.getString("pref_device_default_profile", "Louis") ?: "Louis"
            when {
                raw.equals("Wife", ignoreCase = true) -> "Annette"
                raw.equals("Daughter", ignoreCase = true) -> "Isabel"
                else -> raw
            }
        }
    )
    val alwaysStartOnDefault = MutableStateFlow<Boolean>(prefs.getBoolean("pref_always_start_on_default", true))

    val activeProfile = MutableStateFlow<String>(
        run {
            val raw = if (prefs.getBoolean("pref_always_start_on_default", true)) {
                prefs.getString("pref_device_default_profile", "Louis") ?: "Louis"
            } else {
                prefs.getString("pref_active_profile", "Louis") ?: "Louis"
            }
            when {
                raw.equals("Wife", ignoreCase = true) -> "Annette"
                raw.equals("Daughter", ignoreCase = true) -> "Isabel"
                else -> raw
            }
        }
    )
    val isProfileSwitcherOpen = MutableStateFlow(false)

    fun setActiveProfile(profile: String) {
        val trimmed = profile.trim()
        activeProfile.value = trimmed
        prefs.edit().putString("pref_active_profile", trimmed).commit()
    }

    fun setDeviceDefaultProfile(profile: String) {
        val trimmed = profile.trim()
        if (trimmed.isNotBlank()) {
            defaultProfile.value = trimmed
            prefs.edit().putString("pref_device_default_profile", trimmed).commit()
        }
    }

    fun setAlwaysStartOnDefault(enabled: Boolean) {
        alwaysStartOnDefault.value = enabled
        prefs.edit().putBoolean("pref_always_start_on_default", enabled).commit()
    }

    fun addFamilyProfile(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !familyProfiles.value.any { it.equals(trimmed, ignoreCase = true) }) {
            val updated = familyProfiles.value + trimmed
            familyProfiles.value = updated
            saveProfiles(updated)
            setActiveProfile(trimmed)
        }
    }

    fun renameFamilyProfile(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || oldName.equals(trimmed, ignoreCase = true)) return
        val updated = familyProfiles.value.map { if (it.equals(oldName, ignoreCase = true)) trimmed else it }
        familyProfiles.value = updated
        saveProfiles(updated)
        if (activeProfile.value.equals(oldName, ignoreCase = true)) {
            setActiveProfile(trimmed)
        }
        if (defaultProfile.value.equals(oldName, ignoreCase = true)) {
            setDeviceDefaultProfile(trimmed)
        }
        vm.viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            all.filter { it.profileName.equals(oldName, ignoreCase = true) }.forEach { recipe ->
                repository.updateRecipeProfile(recipe.id, trimmed)
            }
            if (vm.isCloudSyncEnabled.value) {
                vm.syncManager?.performSync()
            }
        }
    }

    fun deleteFamilyProfile(name: String) {
        if (familyProfiles.value.size <= 1) return
        val updated = familyProfiles.value.filterNot { it.equals(name, ignoreCase = true) }
        familyProfiles.value = updated
        saveProfiles(updated)
        if (activeProfile.value.equals(name, ignoreCase = true)) {
            setActiveProfile(updated.firstOrNull() ?: "Louis")
        }
        if (defaultProfile.value.equals(name, ignoreCase = true)) {
            setDeviceDefaultProfile(updated.firstOrNull() ?: "Louis")
        }
    }

    fun saveProfiles(list: List<String>) {
        prefs.edit().putString("pref_family_profiles", list.joinToString("|||")).commit()
    }

    fun assignRecipeToProfile(recipe: RecipeEntity, targetProfile: String) {
        val trimmed = targetProfile.trim()
        if (trimmed.isBlank()) return
        if (!familyProfiles.value.any { it.equals(trimmed, ignoreCase = true) }) {
            addFamilyProfile(trimmed)
        }
        val updated = recipe.copy(profileName = trimmed)
        if (vm.selectedRecipe.value?.id == recipe.id) {
            vm.selectedRecipe.value = updated
        }
        vm.viewModelScope.launch {
            repository.updateRecipeProfile(recipe.id, trimmed)
            if (vm.isCloudSyncEnabled.value) {
                vm.syncManager?.performSync()
            }
        }
    }

    fun bulkMoveRecipes(sourceProfile: String, targetProfile: String, onComplete: (Int) -> Unit = {}) {
        vm.viewModelScope.launch(Dispatchers.IO) {
            val movedCount = repository.bulkMoveRecipesProfile(sourceProfile, targetProfile)
            if (vm.isCloudSyncEnabled.value) {
                vm.syncManager?.performSync()
            }
            withContext(Dispatchers.Main) {
                if (activeProfile.value.equals(sourceProfile, ignoreCase = true) || activeProfile.value == "All Family") {
                    setActiveProfile(targetProfile)
                }
                onComplete(movedCount)
            }
        }
    }

    fun openProfileSwitcher() {
        isProfileSwitcherOpen.value = true
    }

    fun closeProfileSwitcher() {
        isProfileSwitcherOpen.value = false
    }

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
    val isCategoryManagerOpen = MutableStateFlow(false)

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
        if (vm.selectedCategory.value.equals(oldName, ignoreCase = true)) {
            vm.selectedCategory.value = trimmed
        }
        vm.viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategoryName(oldName, trimmed)
        }
    }

    fun deleteCategory(name: String) {
        if (categories.value.size <= 1) return
        val updated = categories.value.filterNot { it.equals(name, ignoreCase = true) }
        categories.value = updated
        saveCategories(updated)
        val fallback = updated.firstOrNull() ?: "Family Classics"
        if (vm.selectedCategory.value.equals(name, ignoreCase = true)) {
            vm.selectedCategory.value = "All"
        }
        vm.viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategoryName(name, fallback)
        }
    }

    fun moveCategoryUp(index: Int) {
        if (index <= 0 || index >= categories.value.size) return
        val list = categories.value.toMutableList()
        val item = list.removeAt(index)
        list.add(index - 1, item)
        categories.value = list
        saveCategories(list)
    }

    fun moveCategoryDown(index: Int) {
        if (index < 0 || index >= categories.value.size - 1) return
        val list = categories.value.toMutableList()
        val item = list.removeAt(index)
        list.add(index + 1, item)
        categories.value = list
        saveCategories(list)
    }

    fun resetCategoriesToDefault() {
        categories.value = defaultCategories
        saveCategories(defaultCategories)
    }

    fun saveCategories(list: List<String>) {
        prefs.edit().putString("pref_custom_categories", list.joinToString("|||")).apply()
    }

    fun openCategoryManager() {
        isCategoryManagerOpen.value = true
    }

    fun closeCategoryManager() {
        isCategoryManagerOpen.value = false
    }

    fun quickAssignCategory(recipe: RecipeEntity, newCategory: String) {
        val trimmed = newCategory.trim()
        if (trimmed.isBlank()) return
        addCategory(trimmed)
        val updated = recipe.copy(category = trimmed)
        if (vm.selectedRecipe.value?.id == recipe.id) {
            vm.selectedRecipe.value = updated
        }
        vm.viewModelScope.launch {
            repository.updateRecipe(updated)
        }
    }

    fun extractCategoriesFromRecipe(catStr: String): List<String> {
        val trimmed = catStr.trim()
        if (trimmed.isBlank()) return listOf("Uncategorized")
        val parts = trimmed.split(",", ";", "|||", "|").map { it.trim() }.filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts else listOf(trimmed)
    }
}
