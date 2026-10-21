package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.AiProvider
import com.example.ai.ImageGenEngine
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.ui.components.settings.SettingsAiSection
import com.example.ui.components.settings.SettingsCategorySection
import com.example.ui.components.settings.SettingsProfileSection
import com.example.ui.components.settings.SettingsStorageSection
import com.example.ui.components.settings.SettingsSyncSection
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@Composable
fun SettingsDialog(
    currentLanguage: LanguageMode,
    onLanguageChange: (LanguageMode) -> Unit,
    currentUnitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    soundEffectsEnabled: Boolean,
    onSoundEffectsChange: (Boolean) -> Unit,
    keepScreenAwake: Boolean,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    autoWeeklyBackupEnabled: Boolean = true,
    onToggleAutoWeeklyBackup: (Boolean) -> Unit = {},
    categories: List<String> = emptyList(),
    onAddCategory: (String) -> Unit = {},
    onRenameCategory: (String, String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit = {},
    aiProvider: AiProvider = AiProvider.GOOGLE_GEMINI,
    onAiProviderChange: (AiProvider) -> Unit = {},
    aiBaseUrl: String = "",
    onAiBaseUrlChange: (String) -> Unit = {},
    geminiApiKey: String = "",
    onGeminiApiKeyChange: (String) -> Unit = {},
    geminiModel: String = "gemini-2.5-flash",
    onGeminiModelChange: (String) -> Unit = {},
    onTestGeminiApiKey: () -> Unit = {},
    geminiApiTestStatus: String? = null,
    isTestingGeminiApi: Boolean = false,
    imageGenEngine: ImageGenEngine = ImageGenEngine.GEMINI,
    onImageGenEngineChange: (ImageGenEngine) -> Unit = {},
    comfyUiUrl: String = "http://192.168.1.54:8188",
    onComfyUiUrlChange: (String) -> Unit = {},
    comfyUiCheckpoint: String = "v1-5-pruned-emaonly.safetensors",
    onComfyUiCheckpointChange: (String) -> Unit = {},
    comfyUiCustomWorkflow: String = "",
    onComfyUiCustomWorkflowChange: (String) -> Unit = {},
    onTestComfyUiConnection: () -> Unit = {},
    comfyUiTestStatus: String? = null,
    isTestingComfyConnection: Boolean = false,
    availableComfyCheckpoints: List<String> = emptyList(),
    // Cloud & Family Sync Parameters
    isCloudSyncEnabled: Boolean = false,
    onToggleCloudSync: (Boolean) -> Unit = {},
    syncServerUrl: String = "",
    onSyncServerUrlChange: (String) -> Unit = {},
    syncSecretToken: String = "",
    onSyncSecretTokenChange: (String) -> Unit = {},
    onTestSyncConnection: () -> Unit = {},
    isTestingSyncConnection: Boolean = false,
    syncConnectionTestResult: Pair<Boolean, String>? = null,
    onTriggerSyncNow: () -> Unit = {},
    isSyncing: Boolean = false,
    lastSyncStatus: String? = null,
    lastSyncTimestamp: Long = 0L,
    autoSyncWifi: Boolean = false,
    onToggleAutoSyncWifi: (Boolean) -> Unit = {},
    onDeleteAllRecipes: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenSmartConverter: (() -> Unit)? = null,
    onOpenGuide: (() -> Unit)? = null,
    totalRecipeCount: Int = 0,
    recipesWithPhotos: Int = 0,
    aiPhotosCount: Int = 0,
    scannedCardsCount: Int = 0,
    unphotographedCount: Int = 0,
    photoStorageMb: Double = 0.0,
    onOpenBatchCoverGen: (() -> Unit)? = null,
    onExportFullCookbookPdf: (() -> Unit)? = null,
    defaultProfile: String = "Louis",
    familyProfiles: List<String> = emptyList(),
    onSetDefaultProfile: (String) -> Unit = {},
    alwaysStartOnDefault: Boolean = true,
    onToggleAlwaysStartOnDefault: (Boolean) -> Unit = {},
    onOpenErrorLogs: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground = Color(0xFFFDFBF7)
    val cardBorder = Color(0xFFE8DFD5)
    val headerDark = Color(0xFF2C1E14)
    val textMuted = Color(0xFF786555)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFFFFDF9),
            shadowElevation = 12.dp,
            modifier = modifier
                .fillMaxWidth(0.95f)
                .widthIn(min = 340.dp, max = 860.dp)
                .fillMaxHeight(0.90f)
                .testTag("settings_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Row (Pinned)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppLocalization.getSettingsTitle(currentLanguage),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = headerDark
                                )
                            )
                            Text(
                                text = AppLocalization.getSettingsSubtitle(currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.5.sp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = headerDark)
                    }
                }

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(bottom = 12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Profile & Language Preferences Section
                    SettingsProfileSection(
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange,
                        defaultProfile = defaultProfile,
                        familyProfiles = familyProfiles,
                        onSetDefaultProfile = onSetDefaultProfile,
                        currentUnitSystem = currentUnitSystem,
                        onUnitSystemChange = onUnitSystemChange,
                        soundEffectsEnabled = soundEffectsEnabled,
                        onSoundEffectsChange = onSoundEffectsChange,
                        keepScreenAwake = keepScreenAwake,
                        onKeepScreenAwakeChange = onKeepScreenAwakeChange,
                        onOpenGuide = onOpenGuide,
                        onOpenSmartConverter = onOpenSmartConverter,
                        cardBackground = cardBackground,
                        cardBorder = cardBorder,
                        headerDark = headerDark,
                        textMuted = textMuted
                    )

                    // 2. Storage, Collection Stats & Backups Section
                    SettingsStorageSection(
                        currentLanguage = currentLanguage,
                        totalRecipeCount = totalRecipeCount,
                        recipesWithPhotos = recipesWithPhotos,
                        aiPhotosCount = aiPhotosCount,
                        scannedCardsCount = scannedCardsCount,
                        unphotographedCount = unphotographedCount,
                        photoStorageMb = photoStorageMb,
                        onOpenBatchCoverGen = onOpenBatchCoverGen,
                        onExportFullCookbookPdf = onExportFullCookbookPdf,
                        autoWeeklyBackupEnabled = autoWeeklyBackupEnabled,
                        onToggleAutoWeeklyBackup = onToggleAutoWeeklyBackup,
                        onOpenBackup = onOpenBackup,
                        onDeleteAllRecipes = onDeleteAllRecipes,
                        cardBackground = cardBackground,
                        cardBorder = cardBorder,
                        headerDark = headerDark,
                        textMuted = textMuted
                    )

                    // 3. Cloud Hub & Multi-Device Sync Section
                    SettingsSyncSection(
                        currentLanguage = currentLanguage,
                        isCloudSyncEnabled = isCloudSyncEnabled,
                        onToggleCloudSync = onToggleCloudSync,
                        syncServerUrl = syncServerUrl,
                        onSyncServerUrlChange = onSyncServerUrlChange,
                        syncSecretToken = syncSecretToken,
                        onSyncSecretTokenChange = onSyncSecretTokenChange,
                        onTestSyncConnection = onTestSyncConnection,
                        isTestingSyncConnection = isTestingSyncConnection,
                        syncConnectionTestResult = syncConnectionTestResult,
                        onTriggerSyncNow = onTriggerSyncNow,
                        isSyncing = isSyncing,
                        lastSyncStatus = lastSyncStatus,
                        lastSyncTimestamp = lastSyncTimestamp,
                        autoSyncWifi = autoSyncWifi,
                        onToggleAutoSyncWifi = onToggleAutoSyncWifi,
                        cardBorder = cardBorder,
                        headerDark = headerDark,
                        textMuted = textMuted
                    )

                    // 4. Universal Multi-Provider AI & ComfyUI Photo Section
                    SettingsAiSection(
                        currentLanguage = currentLanguage,
                        aiProvider = aiProvider,
                        onAiProviderChange = onAiProviderChange,
                        aiBaseUrl = aiBaseUrl,
                        onAiBaseUrlChange = onAiBaseUrlChange,
                        geminiApiKey = geminiApiKey,
                        onGeminiApiKeyChange = onGeminiApiKeyChange,
                        geminiModel = geminiModel,
                        onGeminiModelChange = onGeminiModelChange,
                        onTestGeminiApiKey = onTestGeminiApiKey,
                        geminiApiTestStatus = geminiApiTestStatus,
                        isTestingGeminiApi = isTestingGeminiApi,
                        imageGenEngine = imageGenEngine,
                        onImageGenEngineChange = onImageGenEngineChange,
                        comfyUiUrl = comfyUiUrl,
                        onComfyUiUrlChange = onComfyUiUrlChange,
                        comfyUiCheckpoint = comfyUiCheckpoint,
                        onComfyUiCheckpointChange = onComfyUiCheckpointChange,
                        comfyUiCustomWorkflow = comfyUiCustomWorkflow,
                        onComfyUiCustomWorkflowChange = onComfyUiCustomWorkflowChange,
                        onTestComfyUiConnection = onTestComfyUiConnection,
                        comfyUiTestStatus = comfyUiTestStatus,
                        isTestingComfyConnection = isTestingComfyConnection,
                        availableComfyCheckpoints = availableComfyCheckpoints,
                        onOpenErrorLogs = onOpenErrorLogs,
                        cardBackground = cardBackground,
                        cardBorder = cardBorder,
                        headerDark = headerDark,
                        textMuted = textMuted
                    )

                    // 5. Recipe Categories Management Section
                    SettingsCategorySection(
                        categories = categories,
                        onAddCategory = onAddCategory,
                        onRenameCategory = onRenameCategory,
                        onDeleteCategory = onDeleteCategory,
                        cardBackground = cardBackground,
                        cardBorder = cardBorder,
                        headerDark = headerDark,
                        textMuted = textMuted
                    )
                }

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))

                // Footer (Pinned)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("close_settings_button")
                ) {
                    Text(
                        text = AppLocalization.getDoneButton(currentLanguage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
