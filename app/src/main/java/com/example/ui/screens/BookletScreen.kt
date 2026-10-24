package com.example.ui.screens

import com.example.ui.components.booklet.TabletBookletSpreadView
import com.example.ui.components.booklet.BookletTopBar
import com.example.ui.components.booklet.AiGenerationErrorDialog

import com.example.ui.components.EditAiPhotoPromptDialog


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ImageGenEngine
import com.example.data.local.RecipeEntity
import com.example.ui.util.getDisplayTitle
import com.example.data.model.CoverTheme
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.EditRecipeDialog
import com.example.ui.components.FloatingKitchenTimerWidget
import com.example.ui.components.GlossaryBottomSheet
import com.example.ui.components.KitchenCookModeScreen
import com.example.ui.components.RecipeCookJournalPage
import com.example.ui.components.RecipeCoverPage
import com.example.ui.components.RecipeIngredientsPage
import com.example.ui.components.RecipeLoreTableOfContentsPage
import com.example.ui.components.RecipeStepPage
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShareRecipeCardDialog
import com.example.ui.components.ShoppingListDialog
import com.example.ui.components.SmartConverterBottomSheet
import com.example.ui.components.dietary.RecipeAdaptationDialog
import com.example.ui.components.costing.RecipeCostingDialog
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.AttachMoney
import android.widget.Toast
import com.example.ui.components.VintageHandwrittenCardView
import com.example.ui.theme.CreamBackgroundLight
import com.example.ui.theme.GermanFlagGold
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle
import com.example.ui.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookletScreen(
    viewModel: RecipeViewModel,
    recipe: RecipeEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedRecipeState by viewModel.selectedRecipe.collectAsStateWithLifecycle()
    val activeRecipe = selectedRecipeState?.takeIf { it.id == recipe.id } ?: recipe
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val servingMultiplier by viewModel.servingMultiplier.collectAsStateWithLifecycle()
    val isVintageCardMode by viewModel.isVintageCardMode.collectAsStateWithLifecycle()
    val checkedIngredients by viewModel.checkedIngredients.collectAsStateWithLifecycle()
    val checkedSteps by viewModel.checkedSteps.collectAsStateWithLifecycle()
    val isCookMode by viewModel.isCookMode.collectAsStateWithLifecycle()
    val activeCookStep by viewModel.activeCookStep.collectAsStateWithLifecycle()
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
    val isTimerActive by viewModel.isTimerActive.collectAsStateWithLifecycle()
    val selectedGlossaryItem by viewModel.selectedGlossaryItem.collectAsStateWithLifecycle()
    val isRecipeEditorOpen by viewModel.isRecipeEditorOpen.collectAsStateWithLifecycle()
    val isShareDialogOpen by viewModel.isShareDialogOpen.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isSmartConverterOpen by viewModel.isSmartConverterOpen.collectAsStateWithLifecycle()
    val converterInitialIngredient by viewModel.converterInitialIngredient.collectAsStateWithLifecycle()
    val converterInitialAmount by viewModel.converterInitialAmount.collectAsStateWithLifecycle()
    val converterInitialUnit by viewModel.converterInitialUnit.collectAsStateWithLifecycle()
    val isBackupDialogOpen by viewModel.isBackupDialogOpen.collectAsStateWithLifecycle()
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()
    val pendingRestoreManifest by viewModel.pendingRestoreManifest.collectAsStateWithLifecycle()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val recipeEditorInitialTab by viewModel.recipeEditorInitialTab.collectAsStateWithLifecycle()
    val editingRecipeDraft by viewModel.editingRecipeDraft.collectAsStateWithLifecycle()
    val isShoppingListOpen by viewModel.isShoppingListOpen.collectAsStateWithLifecycle()
    val uncheckedShoppingCount by viewModel.uncheckedShoppingCount.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val autoWeeklyBackupEnabled by viewModel.autoWeeklyBackupEnabled.collectAsStateWithLifecycle()
    val isGeneratingCover by viewModel.isGeneratingCover.collectAsStateWithLifecycle()
    val coverGenerationError by viewModel.coverGenerationError.collectAsStateWithLifecycle()
    val imageGenEngine by viewModel.imageGenEngine.collectAsStateWithLifecycle()
    val comfyUiUrl by viewModel.comfyUiUrl.collectAsStateWithLifecycle()
    val comfyUiCheckpoint by viewModel.comfyUiCheckpoint.collectAsStateWithLifecycle()
    val comfyUiCustomWorkflow by viewModel.comfyUiCustomWorkflow.collectAsStateWithLifecycle()
    val comfyUiTestStatus by viewModel.comfyUiTestStatus.collectAsStateWithLifecycle()
    val isTestingComfyConnection by viewModel.isTestingComfyConnection.collectAsStateWithLifecycle()
    val lastCoverGenerationLog by viewModel.lastCoverGenerationLog.collectAsStateWithLifecycle()
    val showCoverErrorDialog by viewModel.showCoverErrorDialog.collectAsStateWithLifecycle()
    val customPromptDialogRecipe by viewModel.customPromptDialogRecipe.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(coverGenerationError) {
        if (!coverGenerationError.isNullOrBlank()) {
            Toast.makeText(context, "Cover Generation: $coverGenerationError", Toast.LENGTH_LONG).show()
        }
    }

    // Pages: 0: Cover, 1: Lore & TOC, 2: Ingredients & Scaler, 3..3+steps: Steps, last: Journal
    val totalPages = 4 + activeRecipe.steps.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    var showCoverThemeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDietaryAdaptationDialog by remember { mutableStateOf(false) }
    var showRecipeCostingDialog by remember { mutableStateOf(false) }

    // Play subtle audio/haptic click on page turn
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage > 0) {
            viewModel.playPageTurnSound()
        }
    }

    customPromptDialogRecipe?.let { promptRecipe ->
        EditAiPhotoPromptDialog(
            recipe = promptRecipe,
            onDismiss = { viewModel.closeCustomPromptDialog() },
            onGenerate = { customPrompt ->
                viewModel.generateRecipeCoverArt(
                    recipe = promptRecipe,
                    context = context,
                    customPrompt = customPrompt
                )
                viewModel.closeCustomPromptDialog()
            },
            isGenerating = isGeneratingCover,
            languageMode = languageMode
        )
    }

    if (isCookMode) {
        KitchenCookModeScreen(
            recipe = activeRecipe,
            activeStepIndex = activeCookStep,
            onStepChange = { viewModel.activeCookStep.value = it },
            languageMode = languageMode,
            unitSystem = unitSystem,
            onUnitSystemChange = { viewModel.unitSystem.value = it },
            servingMultiplier = servingMultiplier,
            checkedIngredients = checkedIngredients,
            onToggleIngredient = { viewModel.toggleIngredientChecked(it) },
            checkedSteps = checkedSteps,
            onToggleStep = { viewModel.toggleStepChecked(it) },
            timerSecondsRemaining = timerSecondsRemaining,
            timerTotalSeconds = timerTotalSeconds,
            isTimerRunning = isTimerActive,
            onStartTimer = { viewModel.startTimerForStep(it) },
            onPauseTimer = { viewModel.pauseTimer() },
            onResumeTimer = { viewModel.resumeTimer() },
            onResetTimer = { viewModel.resetTimer() },
            onSpeak = { text, isGerman -> viewModel.speakStep(text, isGerman) },
            onExitCookMode = { viewModel.isCookMode.value = false }
        )
    } else if (isVintageCardMode) {
        VintageHandwrittenCardView(
            recipe = activeRecipe,
            languageMode = languageMode,
            unitSystem = unitSystem,
            onClose = { viewModel.isVintageCardMode.value = false }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize().testTag("booklet_screen"),
            containerColor = CreamBackgroundLight,
            topBar = {
                BookletTopBar(
                    recipe = activeRecipe,
                    languageMode = languageMode,
                    soundEffectsEnabled = soundEffectsEnabled,
                    uncheckedShoppingCount = uncheckedShoppingCount,
                    onBack = onBack,
                    onStartCookMode = { viewModel.isCookMode.value = true },
                    onEditRecipe = {
                        val targetTab = when (pagerState.currentPage) {
                            2 -> 1
                            in 3 until (totalPages - 1) -> 2
                            totalPages - 1 -> 3
                            else -> 0
                        }
                        viewModel.openRecipeEditor(activeRecipe, initialTab = targetTab)
                    },
                    onOpenShoppingList = { viewModel.openShoppingList() },
                    onShareRecipe = { viewModel.isShareDialogOpen.value = true },
                    onToggleSound = {
                        val next = !soundEffectsEnabled
                        viewModel.setSoundEffectsEnabled(next)
                        Toast.makeText(context, if (next) "Sound Effects Enabled" else "Sound Effects Muted", Toast.LENGTH_SHORT).show()
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(activeRecipe) },
                    onGenerateAiPhoto = { viewModel.generateRecipeCoverArt(activeRecipe, context) },
                    onEditCustomPrompt = { viewModel.openCustomPromptDialog(activeRecipe) },
                    onAdaptDietary = { showDietaryAdaptationDialog = true },
                    onRecipeCosting = { showRecipeCostingDialog = true },
                    onOpenSettings = { viewModel.openSettings() },
                    onDeleteRecipe = { showDeleteConfirmationDialog = true }
                )
            },
            bottomBar = {
                // Floating Kitchen Cook Mode Bar (Matching iOS cookModeFloatingBar)
                Surface(
                    color = Color.Black.copy(alpha = 0.88f),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.isCookMode.value = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF9A3412),
                                        Color(0xFF78350F)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🔥", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Kitchen Cook Mode",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
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
                    // Two-Page Open Book Spread for Tablets
                    TabletBookletSpreadView(
                        recipe = activeRecipe,
                        languageMode = languageMode,
                        unitSystem = unitSystem,
                        onUnitSystemChange = { viewModel.setUnitSystem(it) },
                        onOpenConverter = { ing, amt, unit -> viewModel.openSmartConverter(ing, amt, unit) },
                        servingMultiplier = servingMultiplier,
                        onSetMultiplier = { viewModel.setServingMultiplier(it) },
                        onOpenGlossary = { term -> viewModel.openGlossaryFor(term) },
                        checkedIngredients = checkedIngredients,
                        onToggleIngredient = { viewModel.toggleIngredientChecked(it) },
                        checkedSteps = checkedSteps,
                        onToggleStep = { viewModel.toggleStepChecked(it) },
                        onStartTimer = { minutes -> viewModel.startTimerForStep(minutes) },
                        onSpeak = { text, isGerman -> viewModel.speakStep(text, isGerman) },
                        onEditRecipe = { tab -> viewModel.openRecipeEditor(activeRecipe, tab) },
                        onGenerateAiCover = { viewModel.generateRecipeCoverArt(activeRecipe, context) },
                        onRemoveCoverPhoto = { viewModel.removeRecipeCoverPhoto(activeRecipe) },
                        isGeneratingCover = isGeneratingCover,
                        onAddToShoppingList = {
                            viewModel.addRecipeToShoppingList(
                                recipe = activeRecipe,
                                multiplier = servingMultiplier,
                                unitSystem = unitSystem
                            )
                            Toast.makeText(context, "Added ${activeRecipe.ingredients.size} ingredients to shopping list!", Toast.LENGTH_SHORT).show()
                        },
                        onSaveJournal = { notes, rating ->
                            viewModel.saveRecipeJournal(activeRecipe.id, notes, rating)
                            Toast.makeText(context, "Cook's journal entry saved!", Toast.LENGTH_SHORT).show()
                        },
                        onIncrementCooked = {
                            viewModel.markRecipeCooked()},
                        onPlayTurnSound = { viewModel.playPageTurnSound() 
                            Toast.makeText(context, "Times cooked updated: ${recipe.timesCooked + 1}", Toast.LENGTH_SHORT).show()
                        },
                        onStartCookingMode = { viewModel.isCookMode.value = true },
                        onOpenDietaryAdaptation = { showDietaryAdaptationDialog = true },
                        onOpenRecipeCosting = { showRecipeCostingDialog = true }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                // Book spine simulation background shadow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0x1F000000),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0x15000000)
                                )
                            )
                        )
                )

                // 3D Skeuomorphic Page-Turning Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // Compute dynamic 3D page curl rotation
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // 3D Camera depth
                                cameraDistance = 24 * density

                                // Pivot along spine
                                if (pageOffset <= 0f) {
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                    rotationY = (pageOffset * -50f).coerceIn(-85f, 0f)
                                } else {
                                    transformOrigin = TransformOrigin(1f, 0.5f)
                                    rotationY = (pageOffset * -50f).coerceIn(0f, 85f)
                                }

                                val scale = 1f - (0.04f * abs(pageOffset).coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
                                alpha = if (abs(pageOffset) > 0.98f) 0.02f else 1f
                            }
                    ) {
                        when (page) {
                            0 -> {
                                // Cover Page
                                RecipeCoverPage(
                                    recipe = activeRecipe,
                                    languageMode = languageMode,
                                    onOpenBook = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    },
                                    onOpenStory = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    },
                                    onEditRecipe = {
                                        viewModel.openRecipeEditor(activeRecipe, initialTab = 0)
                                    },
                                    onGenerateAiCover = {
                                        viewModel.generateRecipeCoverArt(activeRecipe, context)
                                    },
                                    onRemoveCoverPhoto = {
                                        viewModel.removeRecipeCoverPhoto(activeRecipe)
                                    },
                                    isGeneratingCover = isGeneratingCover
                                )
                            }
                            1 -> {
                                // Lore & Table of Contents Page
                                RecipeLoreTableOfContentsPage(
                                    recipe = activeRecipe,
                                    languageMode = languageMode,
                                    onJumpToPage = { target ->
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(target.coerceIn(0, totalPages - 1))
                                        }
                                    },
                                    onEditDetails = {
                                        viewModel.openRecipeEditor(activeRecipe, initialTab = 3)
                                    },
                                    onRotateOriginalPhoto = {
                                        viewModel.rotateOriginalCardPhoto(activeRecipe)
                                    },
                                    onStartCookingMode = {
                                        viewModel.isCookMode.value = true
                                    }
                                )
                            }
                            2 -> {
                                // Ingredients & Portion Scaler Page
                                RecipeIngredientsPage(
                                    recipe = activeRecipe,
                                    languageMode = languageMode,
                                    unitSystem = unitSystem,
                                    onUnitSystemChange = { viewModel.setUnitSystem(it) },
                                    onOpenConverter = { ingName, amt, u ->
                                        viewModel.openSmartConverter(ingName, amt, u)
                                    },
                                    servingMultiplier = servingMultiplier,
                                    onSetMultiplier = { viewModel.setServingMultiplier(it) },
                                    onOpenGlossary = { viewModel.openGlossaryFor(it) },
                                    checkedIngredients = checkedIngredients,
                                    onToggleCheck = { viewModel.toggleIngredientChecked(it) },
                                    onNextPage = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(3)
                                        }
                                    },
                                    onEditIngredients = {
                                        viewModel.openRecipeEditor(activeRecipe, initialTab = 1)
                                    },
                                    onAddToShoppingList = {
                                        viewModel.addIngredientsToShoppingList(
                                            recipe = activeRecipe,
                                            multiplier = servingMultiplier,
                                            unitSystem = unitSystem
                                        )
                                        Toast.makeText(context, "Added ${activeRecipe.ingredients.size} ingredients to shopping list!", Toast.LENGTH_SHORT).show()
                                    },
                                    onStartCooking = {
                                        viewModel.isCookMode.value = true
                                    },
                                    onOpenDietaryAdaptation = {
                                        showDietaryAdaptationDialog = true
                                    },
                                    onOpenRecipeCosting = {
                                        showRecipeCostingDialog = true
                                    }
                                )
                            }
                            totalPages - 1 -> {
                                // Cook's Journal & Memories (Final Page)
                                RecipeCookJournalPage(
                                    recipe = activeRecipe,
                                    onSaveJournal = { notes, rating ->
                                        viewModel.saveRecipeJournal(activeRecipe.id, notes, rating)
                                    },
                                    onIncrementCooked = { viewModel.markRecipeCooked() },
                                    onStartCookingMode = { viewModel.isCookMode.value = true }
                                )
                            }
                            else -> {
                                // Step-by-Step Pages
                                val stepIndex = page - 3
                                val step = activeRecipe.steps.getOrNull(stepIndex)
                                if (step != null) {
                                    RecipeStepPage(
                                        step = step,
                                        totalSteps = activeRecipe.steps.size,
                                        recipe = activeRecipe,
                                        unitSystem = unitSystem,
                                        onUnitSystemChange = { viewModel.setUnitSystem(it) },
                                        onOpenConverter = { name, amt, u ->
                                            viewModel.openSmartConverter(name, amt, u)
                                        },
                                        servingMultiplier = servingMultiplier,
                                        languageMode = languageMode,
                                        isCompleted = checkedSteps.contains(stepIndex),
                                        onToggleCompleted = { viewModel.toggleStepChecked(stepIndex) },
                                        onStartTimer = { viewModel.startTimerForStep(it) },
                                        onSpeak = { text, isGerman -> viewModel.speakStep(text, isGerman) },
                                        onEditStep = {
                                            viewModel.openRecipeEditor(activeRecipe, initialTab = 2)
                                        }
                                    )
                                }
                            }
                        }

                        // Page fold lighting gradient overlay while dragging
                        if (abs(pageOffset) > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            if (pageOffset > 0) listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = (0.25f * abs(pageOffset)).coerceIn(0f, 0.4f))
                                            ) else listOf(
                                                Color.Black.copy(alpha = (0.25f * abs(pageOffset)).coerceIn(0f, 0.4f)),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }

                    }
                }

                // Error banner for AI Cover Generation with "View Full Error Log" action
                AnimatedVisibility(
                    visible = !coverGenerationError.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Photo Generation Failed",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                )
                                Text(
                                    text = coverGenerationError ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFFB91C1C)),
                                    maxLines = 2
                                )
                            }
                            FilledTonalButton(
                                onClick = { viewModel.openCoverErrorDialog() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFFEE2E2),
                                    contentColor = Color(0xFF991B1B)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("View Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Floating Kitchen Multi-Timer Banner if timer is active or paused
                if (timerSecondsRemaining > 0 || isTimerActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 60.dp, end = 12.dp)
                    ) {
                        FloatingKitchenTimerWidget(
                            secondsRemaining = timerSecondsRemaining,
                            totalSeconds = timerTotalSeconds,
                            isRunning = isTimerActive,
                            onPause = { viewModel.pauseTimer() },
                            onResume = { viewModel.resumeTimer() },
                            onReset = { viewModel.resetTimer() },
                            onAddMinute = { viewModel.startTimerForStep((timerSecondsRemaining / 60) + 1) }
                        )
                    }
                }
            }
        }

        // Cover Customizer Dialog
        if (showCoverThemeDialog) {
            AlertDialog(
                onDismissRequest = { showCoverThemeDialog = false },
                title = {
                    Text(
                        text = "Choose Book Cover Theme",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoverTheme.values().forEach { theme ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateCoverTheme(theme)
                                        showCoverThemeDialog = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(theme.primaryHex)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(theme.secondaryHex))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = theme.displayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCoverThemeDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // German Culinary Glossary Bottom Sheet
        if (selectedGlossaryItem != null) {
            GlossaryBottomSheet(
                item = selectedGlossaryItem!!,
                onDismiss = { viewModel.closeGlossary() }
            )
        }

        // Share Recipe Card Dialog
        if (isShareDialogOpen) {
            ShareRecipeCardDialog(
                recipe = activeRecipe,
                servingMultiplier = servingMultiplier,
                unitSystem = unitSystem,
                onAddToShoppingList = {
                    viewModel.addIngredientsToShoppingList(
                        recipe = activeRecipe,
                        multiplier = servingMultiplier,
                        unitSystem = unitSystem
                    )
                    Toast.makeText(context, "Added ${activeRecipe.ingredients.size} ingredients to shopping list!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.isShareDialogOpen.value = false }
            )
        }

        // Shopping List Dialog
        if (isShoppingListOpen) {
            ShoppingListDialog(
                items = shoppingItems,
                onToggleItem = { item -> viewModel.toggleShoppingItem(item) },
                onAddItem = { name, amount, unit, category ->
                    viewModel.addShoppingItem(name, amount, unit, category)
                },
                onDeleteItem = { item -> viewModel.deleteShoppingItem(item) },
                onClearCompleted = { viewModel.clearCompletedShoppingItems() },
                onClearAll = { viewModel.clearAllShoppingItems() },
                onShareList = { viewModel.shareShoppingList(context) },
                onDismiss = { viewModel.closeShoppingList() }
            )
        }

        // Smart Kitchen Converter Bottom Sheet
        if (isSmartConverterOpen) {
            SmartConverterBottomSheet(
                initialIngredientName = converterInitialIngredient,
                initialAmount = converterInitialAmount,
                initialUnit = converterInitialUnit,
                onDismiss = { viewModel.closeSmartConverter() }
            )
        }

        // Settings Dialog
        if (isSettingsOpen) {
            SettingsDialog(
                currentLanguage = languageMode,
                onLanguageChange = { viewModel.setLanguageMode(it) },
                currentUnitSystem = unitSystem,
                onUnitSystemChange = { viewModel.setUnitSystem(it) },
                soundEffectsEnabled = soundEffectsEnabled,
                onSoundEffectsChange = { viewModel.setSoundEffectsEnabled(it) },
                keepScreenAwake = keepScreenOn,
                onKeepScreenAwakeChange = { viewModel.setKeepScreenOn(it) },
                autoWeeklyBackupEnabled = autoWeeklyBackupEnabled,
                onToggleAutoWeeklyBackup = { viewModel.setAutoWeeklyBackupEnabled(it) },
                categories = categories,
                onAddCategory = { viewModel.addCategory(it) },
                onRenameCategory = { old, new -> viewModel.renameCategory(old, new) },
                onDeleteCategory = { viewModel.deleteCategory(it) },
                imageGenEngine = imageGenEngine,
                onImageGenEngineChange = { viewModel.setImageGenEngine(it) },
                comfyUiUrl = comfyUiUrl,
                onComfyUiUrlChange = { viewModel.setComfyUiUrl(it) },
                comfyUiCheckpoint = comfyUiCheckpoint,
                onComfyUiCheckpointChange = { viewModel.setComfyUiCheckpoint(it) },
                comfyUiCustomWorkflow = comfyUiCustomWorkflow,
                onComfyUiCustomWorkflowChange = { viewModel.setComfyUiCustomWorkflow(it) },
                onTestComfyUiConnection = { viewModel.testComfyUiConnection() },
                comfyUiTestStatus = comfyUiTestStatus,
                isTestingComfyConnection = isTestingComfyConnection,
                onOpenBackup = {
                    viewModel.closeSettings()
                    viewModel.openBackupDialog()
                },
                onOpenSmartConverter = {
                    viewModel.closeSettings()
                    viewModel.openSmartConverter("baking_soda", "2", "g")
                },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // Backup and Restore Dialog
        if (isBackupDialogOpen) {
            BackupRestoreDialog(
                totalRecipeCount = 1,
                lastBackupDate = lastBackupDate,
                pendingRestoreManifest = pendingRestoreManifest,
                statusMessage = backupStatusMessage,
                isRestoring = isRestoring,
                onExportToJson = { callback ->
                    viewModel.generateBackupJson(callback)
                },
                onSaveSuccess = { count ->
                    viewModel.onBackupExportSuccess(count)
                },
                onShareBackup = {
                    viewModel.shareBackup(context)
                },
                onInspectFile = { uri ->
                    viewModel.inspectBackupFile(context, uri)
                },
                onExecuteRestore = { replaceExisting ->
                    viewModel.executeRestore(replaceExisting)
                },
                onClearPendingRestore = {
                    viewModel.clearPendingRestore()
                },
                onDismiss = { viewModel.closeBackupDialog() }
            )
        }

        // Edit Recipe Dialog
        if (isRecipeEditorOpen) {
            val draft = editingRecipeDraft ?: activeRecipe
            EditRecipeDialog(
                initialRecipe = draft,
                initialTab = recipeEditorInitialTab,
                categories = categories,
                onSave = { updated ->
                    viewModel.saveEditedRecipe(updated)
                },
                onDelete = { toDelete ->
                    viewModel.deleteRecipe(toDelete)
                    onBack()
                },
                onOpenPromptStudio = {
                    viewModel.openCustomPromptDialog(draft)
                },
                onDismiss = { viewModel.closeRecipeEditor() }
            )
        }

        // Detailed AI Generation Error & Debug Log Dialog
        // Detailed AI Generation Error & Debug Log Dialog
        if (showCoverErrorDialog) {
            AiGenerationErrorDialog(
                imageGenEngine = imageGenEngine,
                comfyUiUrl = comfyUiUrl,
                comfyUiCheckpoint = comfyUiCheckpoint,
                comfyUiCustomWorkflow = comfyUiCustomWorkflow,
                recipe = activeRecipe,
                coverGenerationError = coverGenerationError,
                lastCoverGenerationLog = lastCoverGenerationLog,
                onRetry = {
                    viewModel.closeCoverErrorDialog()
                    viewModel.generateRecipeCoverArt(activeRecipe, context)
                },
                onDismiss = { viewModel.closeCoverErrorDialog() }
            )
        }


        // Delete Recipe Confirmation Dialog from top app bar
        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                icon = {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Delete Recipe?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete '${activeRecipe.getDisplayTitle()}' from your cookbook? This action cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmationDialog = false
                            viewModel.deleteRecipe(recipe)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDietaryAdaptationDialog) {
            RecipeAdaptationDialog(
                recipe = activeRecipe,
                languageMode = languageMode,
                onDismiss = { showDietaryAdaptationDialog = false },
                onSaveAsVariation = { adapted ->
                    showDietaryAdaptationDialog = false
                    viewModel.saveDraftRecipe(adapted)
                    Toast.makeText(context, "Saved variation: ${adapted.title}", Toast.LENGTH_SHORT).show()
                },
                onApplyToCurrent = { adapted ->
                    showDietaryAdaptationDialog = false
                    viewModel.saveDraftRecipe(adapted)
                }
            )
        }

        if (showRecipeCostingDialog) {
            RecipeCostingDialog(
                recipe = activeRecipe,
                onDismiss = { showRecipeCostingDialog = false }
            )
        }
    }
}

/**
 * Heirloom Two-Page Open Book Spread for Tablet Viewports.
 * Displays Left & Right pages simultaneously with vintage stitched spine depth.
 */
