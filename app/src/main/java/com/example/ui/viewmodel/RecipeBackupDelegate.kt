package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupManifest
import com.example.data.backup.SavedBackupFile
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Delegate responsible for backup creation, automatic weekly backups, file/text inspection,
 * sharing, and database restoration.
 */
class RecipeBackupDelegate(
    private val vm: RecipeViewModel,
    private val app: Application,
    private val repository: RecipeRepository,
    private val prefs: SharedPreferences
) {
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
        vm.viewModelScope.launch(Dispatchers.IO) {
            try {
                val all = repository.getAllRecipesDirect()
                if (all.isNotEmpty()) {
                    val created = BackupManager.performWeeklyBackupIfDue(app, all)
                    if (created) {
                        refreshSavedBackups()
                    }
                }
            } catch (t: Throwable) {
                Log.e("RecipeViewModel", "Error in checkAndPerformAutoWeeklyBackup", t)
            }
        }
    }

    fun refreshSavedBackups() {
        vm.viewModelScope.launch(Dispatchers.IO) {
            val list = BackupManager.listAllLocalBackups(app)
            withContext(Dispatchers.Main) {
                savedBackupsList.value = list
            }
        }
    }

    fun createInstantBackup() {
        vm.viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            if (all.isEmpty()) {
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Your cookbook is currently empty. Add recipes first to create a backup."
                }
                return@launch
            }
            val saved = BackupManager.createLocalBackup(app, all, "Instant Backup")
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
        vm.viewModelScope.launch(Dispatchers.IO) {
            BackupManager.deleteBackupFile(file)
            refreshSavedBackups()
        }
    }

    fun directRestoreBackupFile(file: File, replaceExisting: Boolean, onComplete: () -> Unit = {}) {
        vm.viewModelScope.launch(Dispatchers.IO) {
            isRestoring.value = true
            try {
                val content = BackupManager.readBackupFileContent(file)
                if (content.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        backupStatusMessage.value = "Unable to read backup file."
                    }
                    return@launch
                }
                val parseResult = BackupManager.parseBackup(content, app)
                val manifest = parseResult.getOrNull()
                if (manifest == null || manifest.recipes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        backupStatusMessage.value = "No valid recipes found in this backup file."
                    }
                    return@launch
                }
                repository.restoreRecipes(manifest.recipes, replaceExisting)
                val updated = repository.getAllRecipesDirect()
                BackupManager.saveLocalSnapshot(app, updated)
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
        vm.viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getAllRecipesDirect()
            if (current.isNotEmpty()) {
                BackupManager.createLocalBackup(app, current, "Pre-Deletion Safety Backup")
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
        vm.viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllRecipesDirect()
            val json = BackupManager.exportToJson(all, app)
            // Also ensure it's saved locally
            if (all.isNotEmpty()) {
                BackupManager.createLocalBackup(app, all, "Manual Export")
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
        vm.viewModelScope.launch(Dispatchers.IO) {
            try {
                val all = repository.getAllRecipesDirect()
                if (all.isNotEmpty()) {
                    BackupManager.createLocalBackup(app, all, "Shared Backup")
                }
                val uri = BackupManager.createShareableBackupUri(context, all)
                withContext(Dispatchers.Main) {
                    refreshSavedBackups()
                    if (uri != null) {
                        BackupManager.shareBackup(context, uri, all.size)
                        onBackupExportSuccess(all.size)
                    } else {
                        backupStatusMessage.value = "Unable to create shareable backup file."
                    }
                }
            } catch (t: Throwable) {
                Log.e("RecipeViewModel", "Error sharing backup", t)
            }
        }
    }

    fun inspectBackupFile(context: Context, uri: Uri) {
        vm.viewModelScope.launch(Dispatchers.IO) {
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

        val parseResult = BackupManager.parseBackup(text, app)
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
        val snapshot = BackupManager.getLocalSnapshot(app)
        if (snapshot.isNullOrBlank()) {
            backupStatusMessage.value = "No local snapshot found yet."
            return
        }
        inspectBackupText(snapshot)
    }

    fun executeRestore(replaceExisting: Boolean, onComplete: (Int) -> Unit = {}) {
        val manifest = pendingRestoreManifest.value ?: return
        vm.viewModelScope.launch(Dispatchers.IO) {
            isRestoring.value = true
            try {
                repository.restoreRecipes(manifest.recipes, replaceExisting)
                val count = manifest.recipes.size
                // Save updated snapshot
                val updated = repository.getAllRecipesDirect()
                BackupManager.saveLocalSnapshot(app, updated)
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
        vm.viewModelScope.launch(Dispatchers.IO) {
            isRestoring.value = true
            try {
                repository.restoreDefaultRecipes(replaceExisting)
                val updated = repository.getAllRecipesDirect()
                BackupManager.saveLocalSnapshot(app, updated)
                withContext(Dispatchers.Main) {
                    backupStatusMessage.value = "Starter Recipes & Formulas (including Chocolate Chip Cookies) reloaded successfully!"
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
}
