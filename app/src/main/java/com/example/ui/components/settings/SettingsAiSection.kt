package com.example.ui.components.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.AiProvider
import com.example.ai.GeminiClient
import com.example.ai.ImageGenEngine
import com.example.data.model.LanguageMode
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import kotlinx.coroutines.launch

@Composable
fun SettingsAiSection(
    currentLanguage: LanguageMode,
    aiProvider: AiProvider,
    onAiProviderChange: (AiProvider) -> Unit,
    aiBaseUrl: String,
    onAiBaseUrlChange: (String) -> Unit,
    geminiApiKey: String,
    onGeminiApiKeyChange: (String) -> Unit,
    geminiModel: String,
    onGeminiModelChange: (String) -> Unit,
    onTestGeminiApiKey: () -> Unit,
    geminiApiTestStatus: String?,
    isTestingGeminiApi: Boolean,
    imageGenEngine: ImageGenEngine,
    onImageGenEngineChange: (ImageGenEngine) -> Unit,
    comfyUiUrl: String,
    onComfyUiUrlChange: (String) -> Unit,
    comfyUiCheckpoint: String,
    onComfyUiCheckpointChange: (String) -> Unit,
    comfyUiCustomWorkflow: String,
    onComfyUiCustomWorkflowChange: (String) -> Unit,
    onTestComfyUiConnection: () -> Unit,
    comfyUiTestStatus: String?,
    isTestingComfyConnection: Boolean,
    availableComfyCheckpoints: List<String>,
    onOpenErrorLogs: (() -> Unit)?,
    cardBackground: Color = Color(0xFFFDFBF7),
    cardBorder: Color = Color(0xFFE8DFD5),
    headerDark: Color = Color(0xFF2C1E14),
    textMuted: Color = Color(0xFF786555)
) {
    var showApiKey by remember { mutableStateOf(true) }
    var showFullKeyInspector by remember { mutableStateOf(false) }
    var providerDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var comfyCheckpointDropdownExpanded by remember { mutableStateOf(false) }
    var showSetupGuide by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (showFullKeyInspector) {
        FullKeyInspectorDialog(
            initialKey = geminiApiKey,
            currentLanguage = currentLanguage,
            headerDark = headerDark,
            cardBorder = cardBorder,
            textMuted = textMuted,
            onGeminiApiKeyChange = onGeminiApiKeyChange,
            onDismiss = { showFullKeyInspector = false }
        )
    }

    // Main AI Engine Configuration Card
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFBF8F5),
        border = BorderStroke(1.5.dp, if (geminiApiKey.isNotBlank()) Color(0xFFD97706) else cardBorder),
        modifier = Modifier.fillMaxWidth().testTag("settings_gemini_section")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                AiProvider.values().forEach { provider ->
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
                            } catch (_: Exception) {}
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

                if (aiProvider == AiProvider.CUSTOM_OPENAI) {
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

                // Model Selector
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

    // AI Food Photo Studio Engine Section
    SettingsAiFoodPhotoCard(
        cardBackground = cardBackground,
        cardBorder = cardBorder,
        headerDark = headerDark,
        textMuted = textMuted,
        currentLanguage = currentLanguage,
        imageGenEngine = imageGenEngine,
        onImageGenEngineChange = onImageGenEngineChange,
        comfyUiUrl = comfyUiUrl,
        onComfyUiUrlChange = onComfyUiUrlChange,
        comfyUiCheckpoint = comfyUiCheckpoint,
        onComfyUiCheckpointChange = onComfyUiCheckpointChange,
        availableComfyCheckpoints = availableComfyCheckpoints,
        comfyUiCustomWorkflow = comfyUiCustomWorkflow,
        onComfyUiCustomWorkflowChange = onComfyUiCustomWorkflowChange,
        isTestingComfyConnection = isTestingComfyConnection,
        comfyUiTestStatus = comfyUiTestStatus,
        onTestComfyUiConnection = onTestComfyUiConnection
    )
}
