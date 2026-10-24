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
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.example.ui.components.kitchen.KitchenPhoneStepCard
import com.example.ui.components.kitchen.TabletKitchenCookModeLayout
import com.example.ui.components.kitchen.IngredientsChecklistBottomSheet

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
    val coroutineScope = rememberCoroutineScope()

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

                        if (step != null) {
                            KitchenPhoneStepCard(
                                pageIndex = pageIndex,
                                totalSteps = totalSteps,
                                step = step,
                                isThisStepDone = isThisStepDone,
                                languageMode = languageMode,
                                currentUnitSystem = currentUnitSystem,
                                servingMultiplier = servingMultiplier,
                                extraLargeFont = extraLargeFont,
                                timerSecondsRemaining = timerSecondsRemaining,
                                timerTotalSeconds = timerTotalSeconds,
                                isTimerRunning = isTimerRunning,
                                ingredients = recipe.ingredients,
                                onUnitSystemChange = { newSys ->
                                    currentUnitSystem = newSys
                                    onUnitSystemChange?.invoke(newSys)
                                },
                                onToggleStep = onToggleStep,
                                onSpeak = onSpeak,
                                onStartTimer = onStartTimer,
                                onPauseTimer = onPauseTimer,
                                onResumeTimer = onResumeTimer,
                                onResetTimer = onResetTimer,
                                onPreviousStep = {
                                    if (pageIndex > 0) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pageIndex - 1)
                                        }
                                    }
                                },
                                onNextStep = {
                                    if (pageIndex < totalSteps - 1) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pageIndex + 1)
                                        }
                                    }
                                },
                                onExitCookMode = onExitCookMode
                            )
                        }
                    }
                }
            }
        }
    }

    // Ingredients Slide-in Checklist Modal Bottom Sheet
    if (showIngredientsSheet) {
        IngredientsChecklistBottomSheet(
            recipe = recipe,
            unitSystem = currentUnitSystem,
            languageMode = languageMode,
            checkedIngredients = checkedIngredients,
            onToggleIngredient = onToggleIngredient,
            onDismiss = { showIngredientsSheet = false }
        )
    }
}

