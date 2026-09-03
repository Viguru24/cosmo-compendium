package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.SmartPromptBuilder
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAiPhotoPromptDialog(
    recipe: RecipeEntity,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit,
    isGenerating: Boolean = false,
    languageMode: LanguageMode = LanguageMode.ENGLISH
) {
    var promptText by remember(recipe.id) {
        mutableStateOf(SmartPromptBuilder.buildPromptForRecipe(recipe))
    }

    val styleChips = remember {
        listOf(
            "Clear Glass Bottle & Blossoms" to "in a vintage clear glass bottle with swing-top stopper, fresh delicate blossoms and lemon slices",
            "Artisanal Jam Jar" to "in an open rustic glass canning jar with clamp lid, wooden spoon resting alongside with glossy fruit preserve",
            "Ceramic Cake Stand" to "on an elegant vintage ceramic cake pedestal stand, one perfect slice cut, dusting of powdered sugar",
            "Rustic Pottery Dish" to "beautifully plated on an artisanal handcrafted ceramic dish, warm dark wooden table",
            "Steaming Earthenware Bowl" to "in a steaming handcrafted rustic earthenware bowl, fresh herb garnish",
            "Sunlit Morning Window" to "soft natural morning window lighting, warm golden glow, cinematic shallow depth of field"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF9F5EE),
            border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TerracottaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "AI Photo Prompt Studio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF2C2420)
                            )
                            Text(
                                text = recipe.title,
                                fontSize = 13.sp,
                                color = Color(0xFF6B5E55)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF7A6E65))
                    }
                }

                Text(
                    text = "Fine-tune the description below to customize exactly how the AI visualizes this dish, vessel, background, and garnishes:",
                    fontSize = 13.sp,
                    color = Color(0xFF5C5248),
                    lineHeight = 18.sp
                )

                // Editable Prompt TextField
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    label = { Text("AI Image Prompt", color = Color(0xFF6B5E55)) },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF2C2420),
                        unfocusedTextColor = Color(0xFF2C2420),
                        focusedLabelColor = TerracottaPrimary,
                        unfocusedLabelColor = Color(0xFF6B5E55),
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = Color(0xFFD6C7B2),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = TerracottaPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF2C2420),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Style Additions
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Style Enhancements (+):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A6E65)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        styleChips.forEach { (label, textToAppend) ->
                            SuggestionChip(
                                onClick = {
                                    if (!promptText.contains(textToAppend, ignoreCase = true)) {
                                        promptText = (promptText.trimEnd('.') + ", " + textToAppend).trim()
                                    }
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.White,
                                    labelColor = TerracottaPrimary
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = Color(0xFFD6C7B2)
                                )
                            )
                        }
                    }
                }

                // Reset to smart default
                TextButton(
                    onClick = { promptText = SmartPromptBuilder.buildPromptForRecipe(recipe) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF7A6E65))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset to Smart Culinary Default", fontSize = 12.sp, color = Color(0xFF7A6E65))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF5C5248))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onGenerate(promptText) },
                        enabled = !isGenerating && promptText.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Photo")
                        }
                    }
                }
            }
        }
    }
}
