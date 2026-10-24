package com.example.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.GeminiClient
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.launch

@Composable
internal fun FullKeyInspectorDialog(
    initialKey: String,
    currentLanguage: LanguageMode,
    headerDark: Color,
    cardBorder: Color,
    textMuted: Color,
    onGeminiApiKeyChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inspectorKeyInput by remember { mutableStateOf(initialKey) }
    var isInspectorKeyVisible by remember { mutableStateOf(false) }
    var localTestStatus by remember { mutableStateOf<String?>(null) }
    var isLocalTesting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
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
                    IconButton(onClick = onDismiss) {
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
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val clean = GeminiClient.sanitizeApiKey(inspectorKeyInput)
                            onGeminiApiKeyChange(clean)
                            onDismiss()
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
