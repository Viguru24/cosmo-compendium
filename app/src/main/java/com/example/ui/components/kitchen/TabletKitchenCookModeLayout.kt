package com.example.ui.components.kitchen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeStep
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.MatchedStepIngredient

/**
 * Split Two-Column Kitchen Station Layout for Tablets.
 * Left: Ingredients Checklist & Kitchen Multi-Timer
 * Right: Step-by-Step Focus Instruction with high tactile controls
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabletKitchenCookModeLayout(
    recipe: RecipeEntity,
    activeStepIndex: Int,
    currentStep: RecipeStep?,
    totalSteps: Int,
    isStepDone: Boolean,
    languageMode: LanguageMode,
    currentUnitSystem: UnitSystem,
    servingMultiplier: Float,
    checkedIngredients: Set<Int>,
    checkedSteps: Set<Int>,
    matchedIngredients: List<MatchedStepIngredient>,
    extraLargeFont: Boolean,
    timerSecondsRemaining: Int,
    timerTotalSeconds: Int,
    isTimerRunning: Boolean,
    onStepChange: (Int) -> Unit,
    onToggleStep: (Int) -> Unit,
    onToggleIngredient: (Int) -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onStartTimer: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onExitCookMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: Ingredients Checklist & Kitchen Multi-Timer Station (width 360.dp)
        Surface(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFDF8),
            border = BorderStroke(1.5.dp, Color(0xFFE2D7C8)),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🥕", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INGREDIENTS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Unit System Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFAF2E6),
                        border = BorderStroke(1.dp, Color(0xFFE0CFB9))
                    ) {
                        Text(
                            text = "Units: ${currentUnitSystem.label}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5A4D41),
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEAE0D2))

                // Scrollable Checklist
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(recipe.ingredients) { index, ing ->
                        val isChecked = checkedIngredients.contains(index)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleIngredient(index) }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onToggleIngredient(index) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SageGreen,
                                    uncheckedColor = Color(0xFF8C7B6B)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ing.getDisplayName(languageMode),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isChecked) FontWeight.Normal else FontWeight.SemiBold,
                                        color = if (isChecked) Color(0xFF8C7B6B) else Color(0xFF231B15),
                                        fontSize = 12.5.sp
                                    )
                                )
                                Text(
                                    text = ing.getConvertedAmount(currentUnitSystem, servingMultiplier),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isChecked) Color(0xFFA89F91) else TerracottaPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEAE0D2))

                // Digital Timer Widget
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAF6EE),
                    border = BorderStroke(1.dp, Color(0xFFE0CFB9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val minutes = timerSecondsRemaining / 60
                        val seconds = timerSecondsRemaining % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ TIMER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5A4D41)
                                )
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (timerSecondsRemaining > 0) TerracottaPrimary else Color(0xFF5A4D41),
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isTimerRunning) {
                                Button(
                                    onClick = onPauseTimer,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Pause", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (timerSecondsRemaining > 0) {
                                Button(
                                    onClick = onResumeTimer,
                                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (timerSecondsRemaining > 0) {
                                OutlinedButton(
                                    onClick = onResetTimer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Quick Add Timer Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(1, 3, 5, 10).forEach { mins ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEDE3D3),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onStartTimer(mins) }
                                ) {
                                    Text(
                                        text = "+${mins}m",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF451A03),
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Active Step Instruction & Actions (weight 1f)
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFDF8),
            border = BorderStroke(1.5.dp, Color(0xFFE2D7C8)),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Step Navigation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TerracottaPrimary
                    ) {
                        Text(
                            text = "STEP ${activeStepIndex + 1} OF $totalSteps",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // TTS Audio Read Aloud
                        if (currentStep != null) {
                            IconButton(
                                onClick = { onSpeak(currentStep.getInstruction(languageMode, currentUnitSystem), false) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFAF2E6), CircleShape)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Read Step Aloud", tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Done Status Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isStepDone) Color(0xFFE6F4EA) else Color(0xFFF3ECE1)
                        ) {
                            Text(
                                text = if (isStepDone) "Done ✓" else "In Progress",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStepDone) SageGreen else Color(0xFF6B5B4E)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEAE0D2))

                // Step Content Body (Scrollable)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (currentStep != null) {
                        // Large High-Contrast Step Text
                        Text(
                            text = currentStep.getInstruction(languageMode, currentUnitSystem),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (extraLargeFont) 24.sp else 19.sp,
                                lineHeight = if (extraLargeFont) 34.sp else 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F1610)
                            ),
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        // Matched Ingredients for this Step
                        if (matchedIngredients.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF8F0),
                                border = BorderStroke(1.dp, Color(0xFFFFDFC4)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "📍 INGREDIENTS FOR THIS STEP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TerracottaPrimary,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        matchedIngredients.forEach { ing ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFFFFD1B2))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${ing.displayAmount} ${ing.ingredient.getDisplayName(languageMode)}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF451A03)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Chef's Tip or Step Timer Trigger
                        val effectiveLandscapeTimer = currentStep.getEffectiveTimerMinutes()
                        if (effectiveLandscapeTimer > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFAF2E6),
                                border = BorderStroke(1.dp, Color(0xFFE2D2BC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStartTimer(effectiveLandscapeTimer) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Timer Preset Available",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF451A03))
                                        )
                                        Text(
                                            text = "Tap to set timer for $effectiveLandscapeTimer minutes",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5B4E))
                                        )
                                    }
                                    Button(
                                        onClick = { onStartTimer(effectiveLandscapeTimer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Start ${effectiveLandscapeTimer}m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEAE0D2))

                // Bottom Step Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (activeStepIndex > 0) onStepChange(activeStepIndex - 1) },
                        enabled = activeStepIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (languageMode == LanguageMode.GERMAN) "Vorheriger Schritt" else "Previous Step", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onToggleStep(activeStepIndex) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStepDone) SageGreen else Color(0xFF451A03)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Icon(
                            if (isStepDone) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isStepDone) if (languageMode == LanguageMode.GERMAN) "Erledigt ✓" else "Completed ✓" else "Mark Step Done", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (activeStepIndex < totalSteps - 1) {
                                onStepChange(activeStepIndex + 1)
                            } else {
                                onExitCookMode()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Text(
                            text = if (activeStepIndex < totalSteps - 1) "Next Step ➔" else "Finish Cooking 🎉",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
