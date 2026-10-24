package com.example.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ImageGenEngine
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary

@Composable
internal fun SettingsAiFoodPhotoCard(
    cardBackground: Color,
    cardBorder: Color,
    headerDark: Color,
    textMuted: Color,
    currentLanguage: LanguageMode,
    imageGenEngine: ImageGenEngine,
    onImageGenEngineChange: (ImageGenEngine) -> Unit,
    comfyUiUrl: String,
    onComfyUiUrlChange: (String) -> Unit,
    comfyUiCheckpoint: String,
    onComfyUiCheckpointChange: (String) -> Unit,
    availableComfyCheckpoints: List<String>,
    comfyUiCustomWorkflow: String,
    onComfyUiCustomWorkflowChange: (String) -> Unit,
    isTestingComfyConnection: Boolean,
    comfyUiTestStatus: String?,
    onTestComfyUiConnection: () -> Unit
) {
    var comfyCheckpointDropdownExpanded by remember { mutableStateOf(false) }
    var showSetupGuide by remember { mutableStateOf(false) }

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
                            placeholder = { Text("https://api.cosmowhisper.com/comfyui", fontSize = 11.5.sp, color = textMuted) },
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

                        // Quick URL Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "https://api.cosmowhisper.com/comfyui" to "Cloud VPS (Anywhere)",
                                "http://192.168.1.55:8188" to "Wi-Fi LAN"
                            ).forEach { (presetUrl, label) ->
                                val isSelected = comfyUiUrl == presetUrl
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) TerracottaPrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                    border = BorderStroke(0.5.dp, if (isSelected) TerracottaPrimary else cardBorder),
                                    modifier = Modifier.clickable { onComfyUiUrlChange(presetUrl) }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            color = if (isSelected) TerracottaPrimary else headerDark,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
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
