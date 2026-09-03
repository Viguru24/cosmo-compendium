package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.example.ai.AiProvider
import com.example.ai.ImageGenEngine
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
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
    val sampleIngredient = RecipeIngredient("Flour", "250", "g", nameEnglish = "All-Purpose Flour")
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showFullKeyInspector by remember { mutableStateOf(false) }
    var editingCategoryName by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var newCategoryInput by remember { mutableStateOf("") }
    var showAddCategoryField by remember { mutableStateOf(false) }

    val cardBackground = Color(0xFFFDFBF7)
    val cardBorder = Color(0xFFE8DFD5)
    val headerDark = Color(0xFF2C1E14)
    val textMuted = Color(0xFF786555)

    if (showFullKeyInspector) {
        var inspectorKeyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
        var isInspectorKeyVisible by remember { mutableStateOf(true) }
        var localTestStatus by remember { mutableStateOf<String?>(null) }
        var isLocalTesting by remember { mutableStateOf(false) }
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val coroutineScope = rememberCoroutineScope()

        Dialog(
            onDismissRequest = { showFullKeyInspector = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFFDF9),
                border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 580.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == LanguageMode.GERMAN) "Google Gemini API-Schlüssel" else "Google Gemini API Key",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = headerDark
                                    )
                                )
                                Text(
                                    text = if (currentLanguage == LanguageMode.GERMAN) "Vollständiger Schlüssel & Diagnose" else "Full Key Inspector & Editor",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFB45309), fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                        IconButton(onClick = { showFullKeyInspector = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = headerDark)
                        }
                    }

                    HorizontalDivider(color = cardBorder)

                    // Description
                    Text(
                        text = "Inspect and edit your Google Gemini API key below. You can test the connection directly with Google servers:",
                        style = MaterialTheme.typography.bodySmall.copy(color = textMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    )

                    // Multiline Monospace Text Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.5.dp, Color(0xFFD97706).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Key Content (${inspectorKeyInput.length} chars)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (inspectorKeyInput.length >= 20) Color(0xFF16A34A) else Color(0xFFB45309)
                                    )
                                )
                                IconButton(
                                    onClick = { isInspectorKeyVisible = !isInspectorKeyVisible },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        if (isInspectorKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = inspectorKeyInput,
                                onValueChange = { inspectorKeyInput = it },
                                placeholder = { Text("Paste your Gemini API key here", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp, max = 160.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111827),
                                    lineHeight = 20.sp
                                ),
                                visualTransformation = if (isInspectorKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111827),
                                    unfocusedTextColor = Color(0xFF111827),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Color(0xFFD97706),
                                    unfocusedBorderColor = Color(0xFFE5E7EB)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Diagnostics Banner
                    val sanitizedCandidate = com.example.ai.GeminiClient.sanitizeApiKey(inspectorKeyInput)
                    val hasSurroundingJunk = inspectorKeyInput.isNotBlank() && inspectorKeyInput != sanitizedCandidate

                    if (hasSurroundingJunk) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Quotes or extra characters detected.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF92400E),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Button(
                                    onClick = { inspectorKeyInput = sanitizedCandidate },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Auto-Clean", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (inspectorKeyInput.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (inspectorKeyInput.length >= 20) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, if (inspectorKeyInput.length >= 20) Color(0xFFA7F3D0) else Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (inspectorKeyInput.length >= 20) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (inspectorKeyInput.length >= 20) Color(0xFF16A34A) else Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (inspectorKeyInput.length >= 20) {
                                        "✓ Key format valid (${inspectorKeyInput.length} characters)"
                                    } else {
                                        "Key entered (${inspectorKeyInput.length} characters)"
                                    },
                                    fontSize = 11.5.sp,
                                    color = if (inspectorKeyInput.length >= 20) Color(0xFF065F46) else Color(0xFF92400E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Quick Tools Row: Paste, Copy, Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text?.toString() ?: ""
                                if (clip.isNotBlank()) {
                                    inspectorKeyInput = com.example.ai.GeminiClient.sanitizeApiKey(clip)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD97706)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", color = Color(0xFFD97706), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (inspectorKeyInput.isNotBlank()) {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(inspectorKeyInput))
                                }
                            },
                            enabled = inspectorKeyInput.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF4B5563), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = Color(0xFF4B5563), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                inspectorKeyInput = ""
                                localTestStatus = null
                            },
                            enabled = inspectorKeyInput.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear", color = Color(0xFFDC2626), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // In-Inspector Live Test Button
                    Button(
                        onClick = {
                            val toTest = com.example.ai.GeminiClient.sanitizeApiKey(inspectorKeyInput)
                            inspectorKeyInput = toTest
                            onGeminiApiKeyChange(toTest)
                            coroutineScope.launch {
                                isLocalTesting = true
                                localTestStatus = "Testing connection with Google Gemini..."
                                val res = com.example.ai.GeminiClient.testApiKeyDetailed(toTest)
                                if (res.isSuccess) {
                                    localTestStatus = "✓ Connected to Google Gemini 2.5 Flash successfully!"
                                } else {
                                    val err = res.exceptionOrNull()?.localizedMessage ?: "Invalid key"
                                    localTestStatus = "✗ $err"
                                }
                                isLocalTesting = false
                            }
                        },
                        enabled = !isLocalTesting && inspectorKeyInput.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        if (isLocalTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Gemini Connection...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Connection Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (localTestStatus != null) {
                        val isOk = localTestStatus!!.startsWith("✓")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOk) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, if (isOk) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (isOk) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isOk) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = localTestStatus!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isOk) Color(0xFF14532D) else Color(0xFF991B1B),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = cardBorder)

                    // Dialog Actions: Cancel & Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFullKeyInspector = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val clean = com.example.ai.GeminiClient.sanitizeApiKey(inspectorKeyInput)
                                onGeminiApiKeyChange(clean)
                                showFullKeyInspector = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Apply", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete All Recipes?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all recipes from your cookbook library? All recipes will be removed immediately. (You can restore your recipes anytime from automatic weekly backups or reload starter recipes in Backup & Restore).",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF451A03), lineHeight = 20.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteAllRecipes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_all_recipes_button")
                ) {
                    Text(if (currentLanguage == LanguageMode.GERMAN) "Ja, alle löschen" else "Yes, Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = Color(0xFF57534E))
                }
            },
            containerColor = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(16.dp)
        )
    }

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
                    // Section 0A: Sleek Pull-Down Language Selector
                    var languageDropdownExpanded by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TerracottaPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(17.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = AppLocalization.getLanguageSectionTitle(currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = headerDark, fontSize = 13.5.sp)
                                    )
                                    Text(
                                        text = AppLocalization.getLanguageSectionSubtitle(currentLanguage),
                                        style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Pull-Down Trigger Box
                            Box {
                                Surface(
                                    onClick = { languageDropdownExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, if (languageDropdownExpanded) TerracottaPrimary else Color(0xFFD6C8B4)),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(currentLanguage.flag, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = currentLanguage.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = headerDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Language",
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = languageDropdownExpanded,
                                    onDismissRequest = { languageDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(Color(0xFFFFFDF9))
                                        .border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(12.dp))
                                ) {
                                    LanguageMode.values().forEach { lang ->
                                        val isSelected = currentLanguage == lang
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(lang.flag, fontSize = 18.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = when (lang) {
                                                                LanguageMode.GERMAN -> "Deutsch (German)"
                                                                LanguageMode.FRENCH -> "Français (French)"
                                                                LanguageMode.ITALIAN -> "Italiano (Italian)"
                                                                LanguageMode.SPANISH -> "Español (Spanish)"
                                                                LanguageMode.DUTCH -> "Nederlands (Dutch)"
                                                                LanguageMode.ENGLISH -> "English (US / UK)"
                                                            },
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 13.sp,
                                                            color = if (isSelected) TerracottaPrimary else Color(0xFF2C1E14)
                                                        )
                                                    }
                                                    if (isSelected) {
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Active",
                                                            tint = TerracottaPrimary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                languageDropdownExpanded = false
                                                onLanguageChange(lang)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 0B: Sleek Pull-Down Device Owner & Default Cookbook
                    var profileDropdownExpanded by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFD97706).copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("📱", fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Standard-Profil dieses Geräts" else "This Phone's Default Profile",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = headerDark, fontSize = 13.5.sp)
                                        )
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Wessen Kochbuch standardmäßig geöffnet wird" else "Default cookbook opened on this device",
                                            style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Pull-Down Trigger Box
                                Box {
                                    Surface(
                                        onClick = { profileDropdownExpanded = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.5.dp, if (profileDropdownExpanded) Color(0xFFD97706) else Color(0xFFD6C8B4)),
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("⭐", fontSize = 13.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = defaultProfile,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = headerDark
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select Default Profile",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = profileDropdownExpanded,
                                        onDismissRequest = { profileDropdownExpanded = false },
                                        modifier = Modifier
                                            .background(Color(0xFFFFFDF9))
                                            .border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(12.dp))
                                    ) {
                                        val allAvailableProfiles = if (familyProfiles.isNotEmpty()) familyProfiles else listOf("Louis", "Wife", "Daughter")
                                        allAvailableProfiles.forEach { prof ->
                                            val isSelected = defaultProfile.equals(prof, ignoreCase = true)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(if (prof.contains("wife", true)) "👩‍🍳" else if (prof.contains("daughter", true)) "👧" else "👨‍🍳", fontSize = 16.sp)
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = "$prof's Cookbook",
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                fontSize = 13.sp,
                                                                color = if (isSelected) Color(0xFFD97706) else Color(0xFF2C1E14)
                                                            )
                                                        }
                                                        if (isSelected) {
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Active",
                                                                tint = Color(0xFFD97706),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    profileDropdownExpanded = false
                                                    onSetDefaultProfile(prof)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 0C: 📚 Interactive Cookbook Guide & How Everything Works
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenGuide?.invoke() }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📚", fontSize = 18.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "Kochbuch-Anleitung & Funktionen" else "Cookbook Guide & How It Works",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF78350F)
                                        )
                                    )
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "Schritt-für-Schritt Tour: Scannen, Videos, Sous-Chef & mehr" else "Step-by-step tour: Card scanning, video extraction, Sous-Chef & tips",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF92400E),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Section 0: Compact Cookbook Collection Overview & Photo Studio
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = AppLocalization.getCollectionOverviewTitle(currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = headerDark
                                        )
                                    )
                                    val storageStr = if (photoStorageMb < 0.1) "< 0.1 MB" else String.format(Locale.US, "%.1f MB", photoStorageMb)
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "$totalRecipeCount Rezepte ($storageStr) • ✨ $aiPhotosCount KI • 📄 $scannedCardsCount Scans" else "$totalRecipeCount recipes ($storageStr) • ✨ $aiPhotosCount AI • 📄 $scannedCardsCount Scans" + if (unphotographedCount > 0) " • 📷 $unphotographedCount None" else "",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = textMuted,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TerracottaPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "$totalRecipeCount Rezepte" else "$totalRecipeCount Recipes",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            val missingAiCount = scannedCardsCount + unphotographedCount
                            if (onOpenBatchCoverGen != null) {
                                Button(
                                    onClick = onOpenBatchCoverGen,
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_batch_cover_gen_from_collection")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (missingAiCount > 0) "Batch Generate AI Photos ($missingAiCount Ready)" else "Open Batch AI Photo Studio",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }

                            if (onExportFullCookbookPdf != null) {
                                OutlinedButton(
                                    onClick = onExportFullCookbookPdf,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF8C2D19)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("export_master_pdf_settings_button")
                                ) {
                                    Text("📖", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "Gesamtes Kochbuch exportieren (PDF)" else "Export Complete Cookbook (Master PDF)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF8C2D19)
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 1: ☁️ CLOUD & FAMILY MULTI-DEVICE SYNC
                    // ==========================================
                    var showSecretToken by remember { mutableStateOf(false) }
                    val syncDateStr = remember(lastSyncTimestamp) {
                        if (lastSyncTimestamp <= 0L) {
                            "Never"
                        } else {
                            val diff = System.currentTimeMillis() - lastSyncTimestamp
                            if (diff < 60_000) {
                                "Just now"
                            } else {
                                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF6FAF7),
                        border = BorderStroke(1.5.dp, if (isCloudSyncEnabled) SageGreen else cardBorder),
                        modifier = Modifier.fillMaxWidth().testTag("settings_cloud_sync_section")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCloudSyncEnabled) SageGreen.copy(alpha = 0.15f) else Color(0xFFE5E7EB),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.CloudSync,
                                                contentDescription = null,
                                                tint = if (isCloudSyncEnabled) SageGreen else Color(0xFF6B7280),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Cloud Hub & Multi-Geräte Sync" else "Cloud Hub & Multi-Device Sync",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = headerDark
                                            )
                                        )
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) (if (isCloudSyncEnabled) "Aktiv • Verbunden mit privatem VPS" else "Deaktiviert • 100% Offline-Modus") else (if (isCloudSyncEnabled) "Active • Connected to Private VPS" else "Disabled • 100% Offline Local Mode"),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isCloudSyncEnabled) SageGreen else Color(0xFF6B7280),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                                Switch(
                                    checked = isCloudSyncEnabled,
                                    onCheckedChange = onToggleCloudSync,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SageGreen
                                    ),
                                    modifier = Modifier.testTag("toggle_cloud_sync")
                                )
                            }

                            Text(
                                text = if (currentLanguage == LanguageMode.GERMAN) "Synchronisiert Rezepte, Titelbilder und Profile nahtlos zwischen Smartphone und Tablet. Schneller Delta-Sync über WLAN und Mobilfunk." else "Syncs recipes, covers, and family profiles between your phone and tablet seamlessly. Fast delta sync over 4G/5G and Wi-Fi.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )

                            if (isCloudSyncEnabled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFD1E7DD), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Server URL input
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Server-Adresse (VPS / Domain)" else "Server URL (VPS / Domain)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                                        )
                                        OutlinedTextField(
                                            value = syncServerUrl,
                                            onValueChange = onSyncServerUrlChange,
                                            placeholder = { Text("https://api.cosmowhisper.com/cookbook", fontSize = 11.5.sp, color = Color(0xFF9CA3AF)) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Dns, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                                            },
                                            modifier = Modifier.fillMaxWidth().testTag("sync_server_url_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color(0xFF111827),
                                                unfocusedTextColor = Color(0xFF111827),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = SageGreen,
                                                unfocusedBorderColor = Color(0xFFC3E0CC)
                                            ),
                                            singleLine = true
                                        )
                                    }

                                    // Sync Secret Token
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Sync-Sicherheitsschlüssel (Token)" else "Sync Security Token",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                                        )
                                        OutlinedTextField(
                                            value = syncSecretToken,
                                            onValueChange = onSyncSecretTokenChange,
                                            placeholder = { Text(if (currentLanguage == LanguageMode.GERMAN) "Geheimer VPS API-Schlüssel" else "Secret VPS API key", fontSize = 11.5.sp, color = Color(0xFF9CA3AF)) },
                                            leadingIcon = {
                                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { showSecretToken = !showSecretToken }) {
                                                    Icon(
                                                        if (showSecretToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = if (showSecretToken) "Hide token" else "Show token",
                                                        tint = Color(0xFF4B5563),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            },
                                            visualTransformation = if (showSecretToken) VisualTransformation.None else PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth().testTag("sync_secret_token_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color(0xFF111827),
                                                unfocusedTextColor = Color(0xFF111827),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = SageGreen,
                                                unfocusedBorderColor = Color(0xFFC3E0CC)
                                            ),
                                            singleLine = true
                                        )
                                    }

                                    // Test Connection & Sync Now Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = onTestSyncConnection,
                                            enabled = !isTestingSyncConnection && syncServerUrl.isNotBlank(),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, SageGreen),
                                            modifier = Modifier.weight(1f).testTag("test_sync_connection_button")
                                        ) {
                                            if (isTestingSyncConnection) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SageGreen, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (currentLanguage == LanguageMode.GERMAN) "Teste..." else "Testing...", fontSize = 11.5.sp, color = SageGreen)
                                            } else {
                                                Icon(Icons.Default.Dns, contentDescription = null, tint = SageGreen, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (currentLanguage == LanguageMode.GERMAN) "Verbindung testen" else "Test Connection", fontSize = 11.5.sp, color = SageGreen, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = onTriggerSyncNow,
                                            enabled = !isSyncing && syncServerUrl.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("sync_now_button")
                                        ) {
                                            if (isSyncing) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (currentLanguage == LanguageMode.GERMAN) "Synchronisiere..." else "Syncing...", fontSize = 11.5.sp, color = Color.White)
                                            } else {
                                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (currentLanguage == LanguageMode.GERMAN) "Jetzt synchronisieren" else "Sync Now", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    // Connection Test Result Badge
                                    if (syncConnectionTestResult != null) {
                                        val (isSuccess, message) = syncConnectionTestResult
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            border = BorderStroke(1.dp, if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                                            modifier = Modifier.fillMaxWidth().testTag("sync_connection_status_badge")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = if (isSuccess) SageGreen else Color(0xFFDC2626),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = message,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSuccess) Color(0xFF14532D) else Color(0xFF991B1B),
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Last Synced Timestamp & Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Zuletzt synchronisiert: $syncDateStr" else "Last Synced: $syncDateStr",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = textMuted,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        if (lastSyncStatus != null) {
                                            Text(
                                                text = lastSyncStatus,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = SageGreen,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFE5E7EB))

                                    // Auto-Sync on Wi-Fi Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                                        ) {
                                            Icon(Icons.Default.Wifi, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = if (currentLanguage == LanguageMode.GERMAN) "Automatisch im WLAN synchronisieren" else "Auto-Sync on Wi-Fi",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = headerDark
                                                    )
                                                )
                                                Text(
                                                    text = if (currentLanguage == LanguageMode.GERMAN) "Änderungen automatisch im Hintergrund übertragen" else "Syncs changes automatically in background",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = textMuted,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = autoSyncWifi,
                                            onCheckedChange = onToggleAutoSyncWifi,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = SageGreen
                                            ),
                                            modifier = Modifier.testTag("toggle_auto_sync_wifi")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 2: 💾 BACKUP & DATA SAFETY
                    // ==========================================
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = TerracottaPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Archive, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Automatische wöchentliche Sicherung" else "Automatic Weekly Backup",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = headerDark
                                            )
                                        )
                                        Text(
                                            text = if (autoWeeklyBackupEnabled) "Enabled • Auto-saves every 7 days" else "Disabled",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (autoWeeklyBackupEnabled) SageGreen else textMuted,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                                Switch(
                                    checked = autoWeeklyBackupEnabled,
                                    onCheckedChange = onToggleAutoWeeklyBackup,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = TerracottaPrimary
                                    ),
                                    modifier = Modifier.testTag("toggle_auto_weekly_backup")
                                )
                            }

                            Text(
                                text = if (currentLanguage == LanguageMode.GERMAN) "Sichert deine Kochbuch-Bibliothek automatisch, um deine Familienerbstücke zu schützen. ZIP- und SQLite-Backups können jederzeit exportiert werden." else "Backs up your cookbook library automatically to protect against accidental loss. You can also export/import ZIP and SQLite archives anytime.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textMuted,
                                    fontSize = 11.5.sp
                                )
                            )

                            OutlinedButton(
                                onClick = onOpenBackup,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, TerracottaPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_backup_from_settings_button")
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (currentLanguage == LanguageMode.GERMAN) "Datensicherungs- & Wiederherstellungs-Tool öffnen" else "Open Backup & Restore Tool", color = TerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 2: 🧠 UNIVERSAL MULTI-PROVIDER AI ENGINE
                    // ==========================================
                    var showApiKey by remember { mutableStateOf(true) }
                    var providerDropdownExpanded by remember { mutableStateOf(false) }
                    var modelDropdownExpanded by remember { mutableStateOf(false) }
                    val context = LocalContext.current

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFBF8F5),
                        border = BorderStroke(1.5.dp, if (geminiApiKey.isNotBlank()) Color(0xFFD97706) else cardBorder),
                        modifier = Modifier.fillMaxWidth().testTag("settings_gemini_section")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFD97706).copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(aiProvider.emoji, fontSize = 20.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "KI-Engine & Küchen-Assistent" else "AI Engine & Sous-Chef Copilot",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = headerDark
                                            )
                                        )
                                        Text(
                                            text = "${aiProvider.displayName} • $geminiModel",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Powers recipe card OCR, handwriting transcription, German translation, video recipe extraction, and Sous-Chef conversational AI.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )

                            // AI Provider Selection Row
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE5DDD3)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Selected AI Provider",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = headerDark
                                            )
                                        )

                                        Box {
                                            Surface(
                                                onClick = { providerDropdownExpanded = true },
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFFF7ED),
                                                border = BorderStroke(1.dp, Color(0xFFD97706)),
                                                shadowElevation = 1.dp
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(aiProvider.emoji, fontSize = 13.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = aiProvider.displayName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF92400E)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        Icons.Default.ArrowDropDown,
                                                        contentDescription = "Select AI Provider",
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = providerDropdownExpanded,
                                                onDismissRequest = { providerDropdownExpanded = false },
                                                modifier = Modifier
                                                    .background(Color(0xFFFFFDF9))
                                                    .border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(12.dp))
                                            ) {
                                                com.example.ai.AiProvider.values().forEach { provider ->
                                                    val isSelected = provider == aiProvider
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(provider.emoji, fontSize = 16.sp)
                                                                Spacer(modifier = Modifier.width(10.dp))
                                                                Column {
                                                                    Text(
                                                                        text = provider.displayName,
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                        fontSize = 13.sp,
                                                                        color = if (isSelected) Color(0xFFD97706) else Color(0xFF2C1E14)
                                                                    )
                                                                    Text(
                                                                        text = provider.description,
                                                                        fontSize = 10.5.sp,
                                                                        color = Color(0xFF786555)
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        trailingIcon = if (isSelected) {
                                                            {
                                                                Icon(
                                                                    Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = Color(0xFFD97706),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        } else null,
                                                        onClick = {
                                                            providerDropdownExpanded = false
                                                            onAiProviderChange(provider)
                                                            onGeminiModelChange(provider.defaultModel)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Direct Clickable API Key Link Banner
                                    Surface(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(aiProvider.keyUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // ignore browser error
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("🔑", fontSize = 13.sp)
                                                Text(
                                                    text = "Get ${aiProvider.displayName} API Key",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF1D4ED8)
                                                )
                                                Text(
                                                    text = "(${aiProvider.keyUrl})",
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF3B82F6),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "Open Link ↗",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF1E40AF)
                                            )
                                        }
                                    }
                                }
                            }

                            // Key Input Card
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${aiProvider.displayName} API Key",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = headerDark, fontSize = 12.5.sp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            onClick = { showFullKeyInspector = true },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.Fullscreen, contentDescription = "Inspect", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                                                Text("Inspect", fontSize = 10.5.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Surface(
                                            onClick = {
                                                val clip = clipboardManager.getText()?.text?.trim()
                                                if (!clip.isNullOrBlank()) {
                                                    onGeminiApiKeyChange(clip)
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = Color(0xFFB45309), modifier = Modifier.size(13.dp))
                                                Text("Paste", fontSize = 10.5.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = geminiApiKey,
                                    onValueChange = { onGeminiApiKeyChange(it) },
                                    placeholder = { Text("Paste your ${aiProvider.displayName} key here", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { showApiKey = !showApiKey },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (showApiKey) "Hide key" else "Show key",
                                                tint = Color(0xFF6B7280),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 58.dp, max = 100.dp)
                                        .testTag("gemini_api_key_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF111827),
                                        lineHeight = 17.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF111827),
                                        unfocusedTextColor = Color(0xFF111827),
                                        focusedContainerColor = Color(0xFFF9FAFB),
                                        unfocusedContainerColor = Color(0xFFF9FAFB),
                                        focusedBorderColor = Color(0xFFD97706),
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    ),
                                    singleLine = false,
                                    maxLines = 3
                                )

                                // Custom Base URL for Ollama / Custom endpoints
                                if (aiProvider == com.example.ai.AiProvider.CUSTOM_OPENAI) {
                                    OutlinedTextField(
                                        value = aiBaseUrl,
                                        onValueChange = onAiBaseUrlChange,
                                        label = { Text("Custom API Base URL", fontSize = 12.sp) },
                                        placeholder = { Text("http://192.168.1.50:11434/v1", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                // Model Selector & Custom Model input
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE5DDD3)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                Text(
                                                    text = "Model Identifier",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = headerDark
                                                )
                                                Text(
                                                    text = "Active: $geminiModel",
                                                    fontSize = 10.5.sp,
                                                    color = textMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Box {
                                                Surface(
                                                    onClick = { modelDropdownExpanded = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFFFF7ED),
                                                    border = BorderStroke(1.dp, Color(0xFFD97706)),
                                                    shadowElevation = 1.dp
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = geminiModel,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF92400E)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            Icons.Default.ArrowDropDown,
                                                            contentDescription = "Select Model",
                                                            tint = Color(0xFFD97706),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = modelDropdownExpanded,
                                                    onDismissRequest = { modelDropdownExpanded = false },
                                                    modifier = Modifier
                                                        .background(Color(0xFFFFFDF9))
                                                        .border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(12.dp))
                                                ) {
                                                    aiProvider.popularModels.forEach { modelName ->
                                                        val isSelected = modelName.equals(geminiModel, ignoreCase = true)
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = modelName,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                    fontSize = 13.sp,
                                                                    color = if (isSelected) Color(0xFFD97706) else Color(0xFF2C1E14)
                                                                )
                                                            },
                                                            trailingIcon = if (isSelected) {
                                                                {
                                                                    Icon(
                                                                        Icons.Default.Check,
                                                                        contentDescription = "Selected",
                                                                        tint = Color(0xFFD97706),
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            } else null,
                                                            onClick = {
                                                                modelDropdownExpanded = false
                                                                onGeminiModelChange(modelName)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Editable Model Name field
                                        OutlinedTextField(
                                            value = geminiModel,
                                            onValueChange = onGeminiModelChange,
                                            label = { Text("Model Name / ID", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                        )
                                    }
                                }

                                // Test Connection Button
                                Button(
                                    onClick = onTestGeminiApiKey,
                                    enabled = !isTestingGeminiApi,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD97706),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFFF3F4F6),
                                        disabledContentColor = Color(0xFF9CA3AF)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("test_gemini_key_button")
                                ) {
                                    if (isTestingGeminiApi) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Testing ${aiProvider.displayName} Connection...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Test ${aiProvider.displayName} Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (geminiApiTestStatus != null) {
                                    val isOk = geminiApiTestStatus.startsWith("✓")
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isOk) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        border = BorderStroke(1.dp, if (isOk) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                if (isOk) Icons.Default.CheckCircle else Icons.Default.Close,
                                                contentDescription = null,
                                                tint = if (isOk) Color(0xFF16A34A) else Color(0xFFDC2626),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = geminiApiTestStatus,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (isOk) Color(0xFF14532D) else Color(0xFF991B1B),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.5.sp,
                                                    lineHeight = 16.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                if (onOpenErrorLogs != null) {
                                    OutlinedButton(
                                        onClick = onOpenErrorLogs,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFD97706)),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (currentLanguage == LanguageMode.GERMAN) "Fehlerberichte & Systemprotokolle anzeigen" else "View Error Reports & Activity Log",
                                            color = Color(0xFFD97706),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }

                                Text(
                                    text = AppLocalization.getGeminiKeyHelp(currentLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = textMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 3: 🎨 AI FOOD PHOTO STUDIO ENGINE
                    // ==========================================
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth().testTag("settings_image_gen_section")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2C1E14).copy(alpha = 0.08f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = headerDark, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "KI-Food-Fotografie Engine" else if (currentLanguage == LanguageMode.FRENCH) "Moteur de Génération Photo IA" else "AI Photo Generation Engine",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = headerDark
                                        )
                                    )
                                    Text(
                                        text = if (currentLanguage == LanguageMode.GERMAN) "Wähle Cloud Gemini KI oder lokales ComfyUI auf deinem PC" else if (currentLanguage == LanguageMode.FRENCH) "Choisissez Gemini Cloud IA ou ComfyUI sur PC local" else "Choose Cloud Gemini AI or your local PC's ComfyUI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = textMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Engine Selectors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ImageGenEngine.values().forEach { engine ->
                                    val isSelected = imageGenEngine == engine
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) TerracottaPrimary.copy(alpha = 0.12f) else Color.White,
                                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) TerracottaPrimary else cardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onImageGenEngineChange(engine) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (engine == ImageGenEngine.GEMINI) "☁️ Gemini Cloud" else "🖥️ ComfyUI Local",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) TerracottaPrimary else headerDark
                                                    )
                                                )
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(
                                                text = if (currentLanguage == LanguageMode.GERMAN) (if (engine == ImageGenEngine.GEMINI) "Google Cloud API" else "Lokale PC-Grafikkarte") else (if (engine == ImageGenEngine.GEMINI) "Google Cloud API" else "Local GPU Machine"),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = textMuted,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // ComfyUI Configuration Fields
                            if (imageGenEngine == ImageGenEngine.COMFY_UI) {
                                var showSetupGuide by remember { mutableStateOf(false) }
                                var showCustomWorkflowEditor by remember { mutableStateOf(false) }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "ComfyUI Server Endpoint (URL / IP)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                                        )
                                        OutlinedTextField(
                                            value = comfyUiUrl,
                                            onValueChange = onComfyUiUrlChange,
                                            placeholder = { Text("e.g. http://192.168.1.54:8188", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(6.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color(0xFF0F172A),
                                                unfocusedTextColor = Color(0xFF0F172A),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = headerDark,
                                                unfocusedBorderColor = cardBorder
                                            )
                                        )

                                        // Quick IP Helper Chips
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf(
                                                "192.168.1.54:8188" to "Wi-Fi LAN",
                                                "10.0.2.2:8188" to "Emulator",
                                                "127.0.0.1:8188" to "USB / adb"
                                            ).forEach { (presetUrl, label) ->
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (comfyUiUrl.contains(presetUrl)) TerracottaPrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                                    border = BorderStroke(0.5.dp, cardBorder),
                                                    modifier = Modifier.clickable { onComfyUiUrlChange("http://$presetUrl") }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = headerDark, fontWeight = FontWeight.Medium)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Checkpoint Model (.safetensors)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                                        )
                                        OutlinedTextField(
                                            value = comfyUiCheckpoint,
                                            onValueChange = onComfyUiCheckpointChange,
                                            placeholder = { Text("e.g. v1-5-pruned-emaonly.safetensors", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(6.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color(0xFF0F172A),
                                                unfocusedTextColor = Color(0xFF0F172A),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = headerDark,
                                                unfocusedBorderColor = cardBorder
                                            )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = onTestComfyUiConnection,
                                            enabled = !isTestingComfyConnection && comfyUiUrl.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = headerDark),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (isTestingComfyConnection) if (currentLanguage == LanguageMode.GERMAN) "Teste..." else "Testing..." else if (currentLanguage == LanguageMode.GERMAN) "Verbindung testen" else "Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        TextButton(onClick = { showSetupGuide = !showSetupGuide }) {
                                            Text(
                                                if (showSetupGuide) "Hide Guide" else "4-Step Setup Guide",
                                                fontSize = 11.sp,
                                                color = TerracottaPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    if (!comfyUiTestStatus.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (comfyUiTestStatus.startsWith("✓")) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                            border = BorderStroke(1.dp, if (comfyUiTestStatus.startsWith("✓")) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = comfyUiTestStatus,
                                                modifier = Modifier.padding(8.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (comfyUiTestStatus.startsWith("✓")) Color(0xFF065F46) else Color(0xFF991B1B),
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    // 4-Step Setup Guide Expansion
                                    if (showSetupGuide) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFBF8F3),
                                            border = BorderStroke(1.dp, cardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "ComfyUI Quick Setup:",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                                                )
                                                Text(
                                                    text = "1. Launch ComfyUI: python main.py --listen 0.0.0.0 --port 8188\n2. Set PC IP: e.g. http://192.168.1.54:8188\n3. Dev Mode: In browser Settings -> Enable 'Dev mode Options'\n4. Export: Click 'Save (API Format)' if using a custom workflow, or leave blank to use default.",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = textMuted, lineHeight = 15.sp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 4: 🏷️ Recipe Categories Management Card (Pull-Down Accordion)
                    var isCategoriesExpanded by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth().testTag("settings_categories_section")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCategoriesExpanded = !isCategoriesExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = TerracottaPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Recipe Categories",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = headerDark
                                            )
                                        )
                                        Text(
                                            text = if (isCategoriesExpanded) "${categories.size} categories • Tap to collapse" else "${categories.size} categories • Tap to pull down & manage",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isCategoriesExpanded) TerracottaPrimary else textMuted,
                                                fontSize = 11.sp,
                                                fontWeight = if (isCategoriesExpanded) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { isCategoriesExpanded = !isCategoriesExpanded },
                                    modifier = Modifier.size(32.dp).testTag("settings_toggle_categories_button")
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = if (isCategoriesExpanded) "Collapse" else "Expand",
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(28.dp).rotate(if (isCategoriesExpanded) 180f else 0f)
                                    )
                                }
                            }

                            // Expanded Category Content (Add Field + List Items)
                            if (isCategoriesExpanded) {
                                HorizontalDivider(color = cardBorder.copy(alpha = 0.8f))

                                // Add category input field
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newCategoryInput,
                                        onValueChange = { newCategoryInput = it },
                                        placeholder = { Text("New category name...", fontSize = 12.sp, color = textMuted) },
                                        modifier = Modifier.weight(1f).height(50.dp).testTag("new_category_input"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF18120C),
                                            unfocusedTextColor = Color(0xFF18120C),
                                            focusedBorderColor = TerracottaPrimary,
                                            unfocusedBorderColor = cardBorder,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            if (newCategoryInput.isNotBlank()) {
                                                onAddCategory(newCategoryInput.trim())
                                                newCategoryInput = ""
                                            }
                                        },
                                        enabled = newCategoryInput.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("save_new_category_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Category List Items
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    categories.forEach { cat ->
                                        val isEditingThis = editingCategoryName == cat
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, cardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (isEditingThis) {
                                                Row(
                                                    modifier = Modifier.padding(6.dp).fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = renameInput,
                                                        onValueChange = { renameInput = it },
                                                        modifier = Modifier.weight(1f).height(46.dp).testTag("rename_category_input"),
                                                        shape = RoundedCornerShape(6.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color(0xFF18120C),
                                                            unfocusedTextColor = Color(0xFF18120C),
                                                            focusedContainerColor = Color.White,
                                                            unfocusedContainerColor = Color.White,
                                                            focusedBorderColor = TerracottaPrimary,
                                                            unfocusedBorderColor = cardBorder
                                                        ),
                                                        singleLine = true
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (renameInput.isNotBlank()) {
                                                                onRenameCategory(cat, renameInput.trim())
                                                            }
                                                            editingCategoryName = null
                                                        },
                                                        modifier = Modifier.size(32.dp).testTag("confirm_rename_category_button")
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = "Save", tint = SageGreen)
                                                    }
                                                    IconButton(
                                                        onClick = { editingCategoryName = null },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
                                                    }
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = cat,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = headerDark
                                                        )
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(
                                                            onClick = {
                                                                editingCategoryName = cat
                                                                renameInput = cat
                                                            },
                                                            modifier = Modifier.size(28.dp).testTag("edit_category_${cat.lowercase().replace(" ", "_")}")
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Edit,
                                                                contentDescription = "Rename Category",
                                                                tint = TerracottaPrimary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        if (categories.size > 1) {
                                                            IconButton(
                                                                onClick = { onDeleteCategory(cat) },
                                                                modifier = Modifier.size(28.dp).testTag("delete_category_${cat.lowercase().replace(" ", "_")}")
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.DeleteOutline,
                                                                    contentDescription = "Delete Category",
                                                                    tint = Color(0xFFDC2626),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 5: ⚙️ COOKING & APP PREFERENCES (PROPER WEIGHTS)
                    // ==========================================
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = AppLocalization.getCookingAndAppPrefsTitle(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = headerDark
                                )
                            )

                            // Keep Screen Awake
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                                ) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = AppLocalization.getKeepScreenAwakeTitle(currentLanguage),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = headerDark)
                                        )
                                        Text(
                                            text = AppLocalization.getKeepScreenAwakeDesc(currentLanguage),
                                            style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp)
                                        )
                                    }
                                }
                                Switch(
                                    checked = keepScreenAwake,
                                    onCheckedChange = onKeepScreenAwakeChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SageGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = cardBorder)

                            // Sound Effects
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = AppLocalization.getSoundEffectsTitle(currentLanguage),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = headerDark)
                                        )
                                        Text(
                                            text = AppLocalization.getSoundEffectsDesc(currentLanguage),
                                            style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp)
                                        )
                                    }
                                }
                                Switch(
                                    checked = soundEffectsEnabled,
                                    onCheckedChange = onSoundEffectsChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = TerracottaPrimary
                                    )
                                )
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 6: 📏 MEASURING STYLE & CONVERSIONS
                    // ==========================================
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Scale, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = AppLocalization.getUnitSystemTitle(currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = headerDark
                                        )
                                    )
                                    Text(
                                        text = AppLocalization.getUnitSystemDesc(currentLanguage),
                                        style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp)
                                    )
                                }
                            }

                            UnitSystem.values().forEach { system ->
                                val isSelected = currentUnitSystem == system
                                val previewValue = sampleIngredient.getConvertedAmount(system)

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) TerracottaPrimary.copy(alpha = 0.08f) else Color.White,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) TerracottaPrimary else cardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUnitSystemChange(system) }
                                        .testTag("unit_system_${system.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = system.icon, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "${system.label} (e.g. $previewValue)",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) TerracottaPrimary else headerDark
                                                    )
                                                )
                                                Text(
                                                    text = system.description,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = textMuted,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = TerracottaPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (onOpenSmartConverter != null) {
                                OutlinedButton(
                                    onClick = onOpenSmartConverter,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, TerracottaPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_smart_converter_settings_button")
                                ) {
                                    Text("✨", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Smart Spoon & Knife-Tip Converter", color = TerracottaPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 7: ⚠️ DANGER ZONE / RESET
                    // ==========================================
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppLocalization.getDangerZoneTitle(currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                )
                            }

                            Text(
                                text = AppLocalization.getDangerZoneDesc(currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF7F1D1D),
                                    fontSize = 11.sp
                                )
                            )

                            Button(
                                onClick = { showDeleteConfirmation = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delete_all_recipes_button")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppLocalization.getDeleteAllTitle(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
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
