package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.getDisplayTitle

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun KeepScreenAwakeEffect(keepAwake: Boolean) {
    val context = LocalContext.current
    DisposableEffect(keepAwake) {
        val window = context.findActivity()?.window
        if (keepAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenCookModeScreen(
    recipe: RecipeEntity,
    activeStepIndex: Int,
    onStepChange: (Int) -> Unit,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    checkedIngredients: Set<Int>,
    onToggleIngredient: (Int) -> Unit,
    checkedSteps: Set<Int>,
    onToggleStep: (Int) -> Unit,
    timerSecondsRemaining: Int,
    timerTotalSeconds: Int,
    isTimerRunning: Boolean,
    onStartTimer: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onExitCookMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showIngredientsSheet by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(true) }
    var extraLargeFont by remember { mutableStateOf(false) }

    KeepScreenAwakeEffect(keepAwake = keepScreenOn)

    val currentStep = recipe.steps.getOrNull(activeStepIndex)
    val totalSteps = recipe.steps.size
    val isStepDone = checkedSteps.contains(activeStepIndex)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("kitchen_cook_mode_screen"),
        containerColor = Color(0xFFFAF7F2),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = Color(0xFFFAF7F2),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Top Bar Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Exit Button
                        IconButton(
                            onClick = onExitCookMode,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("cook_mode_exit_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Exit Cook Mode",
                                tint = Color(0xFF451A03)
                            )
                        }

                        // Title Area (Fills space safely, never pushes icons away)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "👨‍🍳 KITCHEN COOK MODE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TerracottaPrimary,
                                    letterSpacing = 1.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = recipe.getDisplayTitle(languageMode),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = Color(0xFF231B15)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Action Controls: Font Size, Keep Screen Awake, Ingredients List
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Text Size Toggle
                            IconButton(
                                onClick = { extraLargeFont = !extraLargeFont },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatSize,
                                    contentDescription = "Toggle Font Size",
                                    tint = if (extraLargeFont) TerracottaPrimary else Color(0xFF7A6A5D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Keep Screen Awake Toggle
                            IconButton(
                                onClick = { keepScreenOn = !keepScreenOn },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (keepScreenOn) Icons.Default.BrightnessHigh else Icons.Default.Lock,
                                    contentDescription = if (keepScreenOn) "Screen Awake ON" else "Screen Sleep Normal",
                                    tint = if (keepScreenOn) Color(0xFFD97706) else Color(0xFF9E9287),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Ingredients Drawer Button
                            IconButton(
                                onClick = { showIngredientsSheet = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatListNumbered,
                                    contentDescription = "Show Ingredients",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Step Progress Indicator Bar (Segmented & Clickable)
                    if (totalSteps > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until totalSteps) {
                                val isDone = checkedSteps.contains(i)
                                val isCurrent = i == activeStepIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            when {
                                                isDone -> SageGreen
                                                isCurrent -> TerracottaPrimary
                                                else -> Color(0xFFE2D6C5)
                                            }
                                        )
                                        .clickable { onStepChange(i) }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Fixed Bottom Action Bar: S23/S24/S25 Proportional Non-Wrapping Buttons
            Surface(
                color = Color(0xFFFAF7F2),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, color = Color(0xFFE5DDD0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Previous Step Button
                    OutlinedButton(
                        onClick = { if (activeStepIndex > 0) onStepChange(activeStepIndex - 1) },
                        enabled = activeStepIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (activeStepIndex > 0) Color(0xFF8C7A6B) else Color(0xFFD6CCC0)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Prev",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    // 2. Mark Done / Toggle Step Button
                    Button(
                        onClick = { onToggleStep(activeStepIndex) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStepDone) SageGreen else Color(0xFF451A03)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Icon(
                            if (isStepDone) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isStepDone) "Done ✓" else "Mark Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // 3. Next / Finish Button
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
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = if (activeStepIndex < totalSteps - 1) "Next ➔" else "Finish 🎉",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (currentStep != null) {
                // High-Readability Step Card
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
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
                                    text = "STEP ${activeStepIndex + 1} OF $totalSteps",
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
                                if (isStepDone) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SageGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Completed ✓",
                                            color = SageGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // TTS Audio Reader Button
                                IconButton(
                                    onClick = {
                                        onSpeak(currentStep.getInstruction(languageMode), false)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Read Step Aloud",
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
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
                            // High-contrast, large instruction text
                            Text(
                                text = currentStep.getInstruction(languageMode),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = if (extraLargeFont) 22.sp else 18.sp,
                                    lineHeight = if (extraLargeFont) 32.sp else 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF221B15)
                                )
                            )

                            // Chef's Tip Box (if present)
                            val tipText = currentStep.getLocalizedTip(languageMode) ?: currentStep.tip
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
                            val showTimerWidget = currentStep.timerMinutes > 0 || (timerSecondsRemaining > 0 && isTimerRunning)
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
                                                    "${currentStep.timerMinutes} min timer"
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
                                            } else if (currentStep.timerMinutes > 0) {
                                                Button(
                                                    onClick = { onStartTimer(currentStep.timerMinutes) },
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
                        }
                    }
                }
            }
        }

        // Ingredients Slide-in Checklist Modal Bottom Sheet
        if (showIngredientsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showIngredientsSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFFFAF6EE),
                dragHandle = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🥕 Ingredients Checklist",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        )
                        IconButton(onClick = { showIngredientsSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Checklist")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        itemsIndexed(recipe.ingredients) { index, ing ->
                            val isChecked = checkedIngredients.contains(index)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleIngredient(index) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleIngredient(index) },
                                    colors = CheckboxDefaults.colors(checkedColor = SageGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${ing.getConvertedAmount(unitSystem)} ${ing.getDisplayName(languageMode)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (isChecked) Color.Gray else Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showIngredientsSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done with Ingredients", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

