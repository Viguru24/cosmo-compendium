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
        var inspectorKeyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
        var isInspectorKeyVisible by remember { mutableStateOf(true) }
        var localTestStatus by remember { mutableStateOf<String?>(null) }
        var isLocalTesting by remember { mutableStateOf(false) }
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

                    Text(
                        text = "Inspect and edit your Google Gemini API key below. You can test the connection directly with Google servers:",
                        style = MaterialTheme.typography.bodySmall.copy(color = textMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    )

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

                    val sanitizedCandidate = GeminiClient.sanitizeApiKey(inspectorKeyInput)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text?.toString() ?: ""
                                if (clip.isNotBlank()) {
                                    inspectorKeyInput = GeminiClient.sanitizeApiKey(clip)
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

                    Button(
                        onClick = {
                            val toTest = GeminiClient.sanitizeApiKey(inspectorKeyInput)
                            inspectorKeyInput = toTest
                            onGeminiApiKeyChange(toTest)
                            coroutineScope.launch {
                                isLocalTesting = true
                                localTestStatus = "Testing connection with Google Gemini..."
                                val res = GeminiClient.testApiKeyDetailed(toTest)
                                if (res.isSuccess) {
                                    localTestStatus = "✓ Connected to Google Gemini successfully!"
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
                                val clean = GeminiClient.sanitizeApiKey(inspectorKeyInput)
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
                        text = if (currentLanguage == LanguageMode.GERMAN) "KI-Food-Fotografie Engine" else "AI Photo Generation Engine",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = headerDark
                        )
                    )
                    Text(
                        text = if (currentLanguage == LanguageMode.GERMAN) "Wähle Cloud Gemini KI oder lokales ComfyUI auf deinem PC" else "Choose Cloud Gemini AI or your local PC's ComfyUI",
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
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TerracottaPrimary.copy(alpha = 0.08f) else Color.White,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) TerracottaPrimary else cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onImageGenEngineChange(engine) }
                            .testTag("engine_${engine.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = engine.displayName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TerracottaPrimary else headerDark
                                    )
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = TerracottaPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = engine.description,
                                style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 10.sp),
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // ComfyUI Dedicated Settings Drawer
            if (imageGenEngine == ImageGenEngine.COMFY_UI) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ComfyUI Server URL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                        )
                        OutlinedTextField(
                            value = comfyUiUrl,
                            onValueChange = onComfyUiUrlChange,
                            placeholder = { Text("http://192.168.1.54:8188", fontSize = 11.5.sp, color = textMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("comfyui_url_input"),
                            shape = RoundedCornerShape(8.dp),
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
                    }

                    // Checkpoint Model Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Checkpoint Model (.safetensors)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = comfyUiCheckpoint,
                                onValueChange = onComfyUiCheckpointChange,
                                modifier = Modifier.fillMaxWidth().testTag("comfyui_checkpoint_input"),
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    IconButton(onClick = { comfyCheckpointDropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Checkpoint", tint = TerracottaPrimary)
                                    }
                                },
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
                            DropdownMenu(
                                expanded = comfyCheckpointDropdownExpanded,
                                onDismissRequest = { comfyCheckpointDropdownExpanded = false }
                            ) {
                                val checkpoints = if (availableComfyCheckpoints.isNotEmpty()) {
                                    availableComfyCheckpoints
                                } else {
                                    listOf(
                                        "v1-5-pruned-emaonly.safetensors",
                                        "sd_xl_base_1.0.safetensors",
                                        "deliberate_v2.safetensors",
                                        "dreamshaper_8.safetensors"
                                    )
                                }
                                checkpoints.forEach { cp ->
                                    DropdownMenuItem(
                                        text = { Text(cp, fontSize = 12.sp) },
                                        onClick = {
                                            onComfyUiCheckpointChange(cp)
                                            comfyCheckpointDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Custom Workflow JSON field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Custom Workflow API JSON (Optional)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                        )
                        OutlinedTextField(
                            value = comfyUiCustomWorkflow,
                            onValueChange = onComfyUiCustomWorkflowChange,
                            placeholder = { Text("Paste exported 'Save (API Format)' JSON from ComfyUI here, or leave empty for auto txt2img default", fontSize = 11.sp, color = textMuted) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 120.dp).testTag("comfyui_workflow_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF18120C),
                                unfocusedTextColor = Color(0xFF18120C),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = TerracottaPrimary,
                                unfocusedBorderColor = cardBorder
                            ),
                            maxLines = 4
                        )
                    }

                    // Test Connection & Quick Setup Guide Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestComfyUiConnection,
                            enabled = !isTestingComfyConnection && comfyUiUrl.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TerracottaPrimary),
                            modifier = Modifier.weight(1f).testTag("test_comfyui_button")
                        ) {
                            if (isTestingComfyConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TerracottaPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 11.5.sp, color = TerracottaPrimary)
                            } else {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Connection", fontSize = 11.5.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showSetupGuide = !showSetupGuide },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (showSetupGuide) "Hide Guide" else "Setup Guide", fontSize = 11.5.sp, color = headerDark)
                        }
                    }

                    if (comfyUiTestStatus != null) {
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
}
