package com.example.ui.components.kitchen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.StepIngredientMatcher

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitchenPhoneStepCard(
    pageIndex: Int,
    totalSteps: Int,
    step: RecipeStep,
    isThisStepDone: Boolean,
    languageMode: LanguageMode,
    currentUnitSystem: UnitSystem,
    servingMultiplier: Float,
    extraLargeFont: Boolean,
    timerSecondsRemaining: Int,
    timerTotalSeconds: Int,
    isTimerRunning: Boolean,
    ingredients: List<RecipeIngredient>,
    onUnitSystemChange: ((UnitSystem) -> Unit)?,
    onToggleStep: (Int) -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onStartTimer: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onExitCookMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeUnitSystem by remember(currentUnitSystem) { mutableStateOf(currentUnitSystem) }
    val matchedForStep = remember(step.getInstruction(languageMode), ingredients, activeUnitSystem, servingMultiplier) {
        StepIngredientMatcher.findIngredientsInStep(
            stepInstruction = step.getInstruction(languageMode),
            ingredients = ingredients,
            unitSystem = activeUnitSystem,
            multiplier = servingMultiplier
        )
    }

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)),
        border = BorderStroke(1.5.dp, Color(0xFFE2D7C8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TerracottaPrimary
                ) {
                    Text(
                        text = "STEP ${pageIndex + 1} OF $totalSteps",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isThisStepDone) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SageGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "Erledigt ✓" else "Completed ✓",
                                color = SageGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // TTS Audio Reader Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3ECE0),
                        border = BorderStroke(1.dp, Color(0xFFDFD1BF)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSpeak(step.getInstruction(languageMode, activeUnitSystem), false)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Read Step Aloud",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Read Aloud",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF451A03)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFEDE4D6)
            )

            // Scrollable Main Content (Ensures long text + tips + timer never overflow)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Context-Aware Ingredient Amount Badges & Quick Unit Toggle
                if (matchedForStep.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFF3EDE2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFDDCFBE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🥣", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ingredients for this step:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF451A03)
                                        )
                                    )
                                }

                                // Quick Unit System Switcher Chips
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    UnitSystem.values().forEach { sys ->
                                        val isSel = activeUnitSystem == sys
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSel) TerracottaPrimary else Color(0xFFE2D7C7),
                                            modifier = Modifier.clickable {
                                                activeUnitSystem = sys
                                                onUnitSystemChange?.invoke(sys)
                                            }
                                        ) {
                                            Text(
                                                text = when (sys) {
                                                    UnitSystem.METRIC_GRAMS -> "g"
                                                    UnitSystem.CUPS_US -> "cups"
                                                    UnitSystem.UK_IMPERIAL -> "uk"
                                                    UnitSystem.BAKERS_PRECISION -> "bk"
                                                },
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSel) Color.White else Color(0xFF451A03)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchedForStep.forEach { item ->
                                    var cookBadgeMenuExpanded by remember { mutableStateOf(false) }

                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFAF7F2),
                                            border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                                            modifier = Modifier.clickable {
                                                cookBadgeMenuExpanded = true
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.displayAmount,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF9A3412)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.matchedName,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF292524)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    contentDescription = "Switch measurement unit",
                                                    tint = TerracottaPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = cookBadgeMenuExpanded,
                                            onDismissRequest = { cookBadgeMenuExpanded = false },
                                            modifier = Modifier
                                                .background(Color(0xFFFAF6F0))
                                                .border(1.dp, Color(0xFFD8C9B5), RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                text = "Convert ${item.matchedName}:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF8C7A6B)
                                                ),
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )

                                            HorizontalDivider(color = Color(0xFFE2D6C5))

                                            UnitSystem.values().forEach { sys ->
                                                val isSelected = activeUnitSystem == sys
                                                val convertedAmt = item.ingredient.getConvertedAmount(sys, servingMultiplier)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(sys.icon, fontSize = 14.sp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = "${sys.label} ($convertedAmt)",
                                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                        color = if (isSelected) TerracottaPrimary else Color(0xFF292524)
                                                                    )
                                                                )
                                                                Text(
                                                                    text = sys.shortLabel,
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        color = Color(0xFF78716C),
                                                                        fontSize = 10.sp
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    },
                                                    trailingIcon = {
                                                        if (isSelected) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = TerracottaPrimary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        activeUnitSystem = sys
                                                        onUnitSystemChange?.invoke(sys)
                                                        cookBadgeMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // High-contrast, large instruction text (with dynamic temperature conversion)
                Text(
                    text = step.getInstruction(languageMode, activeUnitSystem),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = if (extraLargeFont) 22.sp else 18.sp,
                        lineHeight = if (extraLargeFont) 32.sp else 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF221B15)
                    )
                )

                // Chef's Tip Box (if present)
                val tipText = step.getLocalizedTip(languageMode) ?: step.tip
                if (!tipText.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFEF9C3),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tip: $tipText",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF78350F),
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }

                // Step Kitchen Timer Widget
                val effectiveTimerMinutes = step.getEffectiveTimerMinutes()
                val showTimerWidget = effectiveTimerMinutes > 0 || (timerSecondsRemaining > 0 && isTimerRunning)
                if (showTimerWidget) {
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val mins = timerSecondsRemaining / 60
                                val secs = timerSecondsRemaining % 60
                                Text(
                                    text = if (timerSecondsRemaining > 0) {
                                        String.format("%02d:%02d", mins, secs)
                                    } else {
                                        "$effectiveTimerMinutes min timer"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF431407),
                                        fontSize = 18.sp
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (timerSecondsRemaining > 0) {
                                    IconButton(
                                        onClick = { if (isTimerRunning) onPauseTimer() else onResumeTimer() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isTimerRunning) "Pause Timer" else "Resume Timer",
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onResetTimer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Replay,
                                            contentDescription = "Reset Timer",
                                            tint = Color(0xFF6B5B4E),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else if (effectiveTimerMinutes > 0) {
                                    Button(
                                        onClick = { onStartTimer(effectiveTimerMinutes) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "Start Timer",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // MARK DONE BUTTON (INSIDE DIRECTIONS BOX)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onToggleStep(pageIndex) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isThisStepDone) SageGreen else Color(0xFF451A03)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        if (isThisStepDone) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isThisStepDone) "Step Completed ✓ (Tap to Undo)" else "Mark Step Done ✓",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // EXPLICIT STEP NAVIGATION CONTROLS (Swipe or Tap)
                if (totalSteps > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            enabled = pageIndex > 0,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Step", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (pageIndex < totalSteps - 1) {
                            Button(
                                onClick = onNextStep,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Next Step", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next Step", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (pageIndex == totalSteps - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onExitCookMode,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Finish Cooking 🎉",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Swipe Navigation Hint
                if (totalSteps > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                pageIndex == 0 -> "Swipe left for next step ➔"
                                pageIndex == totalSteps - 1 -> "‹ Swipe right for previous step"
                                else -> "‹ Swipe left / right to change steps ›"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF9E8F80),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
