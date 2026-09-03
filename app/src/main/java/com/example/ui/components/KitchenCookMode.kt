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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KitchenCookModeScreen(
    recipe: RecipeEntity,
    activeStepIndex: Int,
    onStepChange: (Int) -> Unit,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onUnitSystemChange: ((UnitSystem) -> Unit)? = null,
    servingMultiplier: Float = 1.0f,
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
    var currentUnitSystem by remember(unitSystem) { mutableStateOf(unitSystem) }

    KeepScreenAwakeEffect(keepAwake = keepScreenOn)

    val totalSteps = recipe.steps.size
    val pagerState = rememberPagerState(
        initialPage = activeStepIndex.coerceIn(0, (totalSteps - 1).coerceAtLeast(0))
    ) {
        totalSteps.coerceAtLeast(1)
    }

    LaunchedEffect(activeStepIndex) {
        if (activeStepIndex in 0 until totalSteps && pagerState.currentPage != activeStepIndex) {
            pagerState.animateScrollToPage(activeStepIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != activeStepIndex && pagerState.currentPage in 0 until totalSteps) {
            onStepChange(pagerState.currentPage)
        }
    }

    val currentStep = recipe.steps.getOrNull(activeStepIndex)
    val isStepDone = checkedSteps.contains(activeStepIndex)

    // Dynamic Context-Aware Step Ingredient Matcher
    val matchedIngredients = remember(currentStep?.getInstruction(languageMode), recipe.ingredients, currentUnitSystem, servingMultiplier) {
        if (currentStep != null) {
            com.example.ui.util.StepIngredientMatcher.findIngredientsInStep(
                stepInstruction = currentStep.getInstruction(languageMode),
                ingredients = recipe.ingredients,
                unitSystem = currentUnitSystem,
                multiplier = servingMultiplier
            )
        } else {
            emptyList()
        }
    }

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
                                contentDescription = if (languageMode == LanguageMode.GERMAN) "Kochmodus beenden" else "Exit Cook Mode",
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
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 650.dp

            if (isTablet) {
                TabletKitchenCookModeLayout(
                    recipe = recipe,
                    activeStepIndex = activeStepIndex,
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    isStepDone = isStepDone,
                    languageMode = languageMode,
                    currentUnitSystem = currentUnitSystem,
                    servingMultiplier = servingMultiplier,
                    checkedIngredients = checkedIngredients,
                    checkedSteps = checkedSteps,
                    matchedIngredients = matchedIngredients,
                    extraLargeFont = extraLargeFont,
                    timerSecondsRemaining = timerSecondsRemaining,
                    timerTotalSeconds = timerTotalSeconds,
                    isTimerRunning = isTimerRunning,
                    onStepChange = onStepChange,
                    onToggleStep = onToggleStep,
                    onToggleIngredient = onToggleIngredient,
                    onUnitSystemChange = { newUnitSystem: UnitSystem ->
                        currentUnitSystem = newUnitSystem
                        onUnitSystemChange?.invoke(newUnitSystem)
                    },
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer,
                    onResetTimer = onResetTimer,
                    onSpeak = onSpeak,
                    onExitCookMode = onExitCookMode
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val step = recipe.steps.getOrNull(pageIndex)
                        val isThisStepDone = checkedSteps.contains(pageIndex)
                        val matchedForStep = remember(step?.getInstruction(languageMode), recipe.ingredients, currentUnitSystem, servingMultiplier) {
                            if (step != null) {
                                com.example.ui.util.StepIngredientMatcher.findIngredientsInStep(
                                    stepInstruction = step.getInstruction(languageMode),
                                    ingredients = recipe.ingredients,
                                    unitSystem = currentUnitSystem,
                                    multiplier = servingMultiplier
                                )
                            } else {
                                emptyList()
                            }
                        }

                        if (step != null) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
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
                                            IconButton(
                                                onClick = {
                                                    onSpeak(step.getInstruction(languageMode, currentUnitSystem), false)
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
                                                                val isSel = currentUnitSystem == sys
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = if (isSel) TerracottaPrimary else Color(0xFFE2D7C7),
                                                                    modifier = Modifier.clickable {
                                                                        currentUnitSystem = sys
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
                                                                        val isSelected = currentUnitSystem == sys
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
                                                                                currentUnitSystem = sys
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
                                            text = step.getInstruction(languageMode, currentUnitSystem),
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
    currentStep: com.example.data.model.RecipeStep?,
    totalSteps: Int,
    isStepDone: Boolean,
    languageMode: LanguageMode,
    currentUnitSystem: UnitSystem,
    servingMultiplier: Float,
    checkedIngredients: Set<Int>,
    checkedSteps: Set<Int>,
    matchedIngredients: List<com.example.ui.util.MatchedStepIngredient>,
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

