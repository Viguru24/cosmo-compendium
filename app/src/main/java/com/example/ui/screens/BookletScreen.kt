package com.example.ui.screens

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
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = activeRecipe.getDisplayTitle(languageMode),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = TerracottaPrimary,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = activeRecipe.getDisplayCategory(languageMode),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("back_to_bookshelf_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF451A03), modifier = Modifier.size(18.dp))
                        }
                    },
                    actions = {
                        // Compact Edit Recipe Button
                        FilledTonalButton(
                            onClick = {
                                val targetTab = when (pagerState.currentPage) {
                                    2 -> 1 // Ingredients tab
                                    in 3 until (totalPages - 1) -> 2 // Steps tab
                                    totalPages - 1 -> 3 // Lore & Notes tab
                                    else -> 0 // Basics tab
                                }
                                viewModel.openRecipeEditor(activeRecipe, initialTab = targetTab)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFEDE4D6),
                                contentColor = Color(0xFF4A3828)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .padding(horizontal = 1.dp)
                                .testTag("edit_recipe_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Recipe", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(AppLocalization.getEditButtonLabel(languageMode), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Shopping List button with badge
                        IconButton(
                            onClick = { viewModel.openShoppingList() },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("booklet_open_shopping_list_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (uncheckedShoppingCount > 0) {
                                        Badge(
                                            containerColor = TerracottaPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "$uncheckedShoppingCount", fontSize = 8.5.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping List",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // Share Recipe Card
                        IconButton(
                            onClick = { viewModel.isShareDialogOpen.value = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share Recipe",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Quick Sound Toggle (Mute / Unmute)
                        IconButton(
                            onClick = {
                                val next = !soundEffectsEnabled
                                viewModel.setSoundEffectsEnabled(next)
                                Toast.makeText(context, if (next) "🔊 Sound Effects Enabled" else "🔇 Sound Effects Muted", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (soundEffectsEnabled) "Mute Sound" else "Unmute Sound",
                                tint = if (soundEffectsEnabled) TerracottaPrimary else Color(0xFF9E8E81),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Favorite Toggle
                        IconButton(
                            onClick = { viewModel.toggleFavorite(activeRecipe) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                if (activeRecipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (activeRecipe.isFavorite) Color(0xFFB91C1C) else Color(0xFF8C7A6B),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // More Actions Menu
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color(0xFF5A493B),
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(if (!recipe.imageUri.isNullOrBlank()) "✨ Regenerate AI Food Photo" else "✨ Generate AI Food Photo")
                                            Text(
                                                text = if (imageGenEngine == ImageGenEngine.COMFY_UI) "via ComfyUI (Local)" else "via Gemini AI (Cloud)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF78350F),
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706)) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.generateRecipeCoverArt(activeRecipe, context)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("✨ Edit AI Photo Prompt...") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.openCustomPromptDialog(activeRecipe)
                                    }
                                )
                                if (!recipe.imageUri.isNullOrBlank()) {
                                    DropdownMenuItem(
                                        text = { Text("Remove Cover Photo") },
                                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = TerracottaPrimary) },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.removeRecipeCoverPhoto(activeRecipe)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Change Book Cover Theme") },
                                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = TerracottaPrimary) },
                                    onClick = {
                                        showMoreMenu = false
                                        showCoverThemeDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Units: ${unitSystem.label}") },
                                    leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = TerracottaPrimary) },
                                    onClick = {
                                        showMoreMenu = false
                                        val systems = UnitSystem.values()
                                        val nextIdx = (unitSystem.ordinal + 1) % systems.size
                                        viewModel.setUnitSystem(systems[nextIdx])
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Smart Unit & Spoon Converter") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.openSmartConverter("baking_soda", "2", "g")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Vintage Card View") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.isVintageCardMode.value = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = TerracottaPrimary) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.openSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Recipe", color = Color(0xFFDC2626)) },
                                    leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showDeleteConfirmationDialog = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7F2))
                )
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
                        }
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
                                    onIncrementCooked = { viewModel.markRecipeCooked() }
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
        if (showCoverErrorDialog) {
            val errorText = coverGenerationError ?: "Unknown error"
            val logText = lastCoverGenerationLog ?: "No log output available."
            val fullReport = "=== AI PHOTO GENERATION DIAGNOSTIC LOG ===\n" +
                    "Engine: ${imageGenEngine.displayName}\n" +
                    (if (imageGenEngine == com.example.ai.ImageGenEngine.COMFY_UI) {
                        "ComfyUI Endpoint: $comfyUiUrl\n" +
                        "Checkpoint Model: $comfyUiCheckpoint\n" +
                        "Custom Workflow JSON: ${if (comfyUiCustomWorkflow.isNotBlank()) "Provided (${comfyUiCustomWorkflow.length} chars)" else "Standard Built-in Default Graph"}\n"
                    } else "Google Cloud Imagen 3 / Gemini 2.0 Flash\n") +
                    "Recipe: ${activeRecipe.getDisplayTitle()} (${activeRecipe.category})\n" +
                    "Status: FAILED\n\n" +
                    "--- ERROR MESSAGE ---\n$errorText\n\n" +
                    "--- LAST LOG TRAIL ---\n$logText"

            AlertDialog(
                onDismissRequest = { viewModel.closeCoverErrorDialog() },
                icon = {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "AI Photo Generation Error Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Active Engine: ${imageGenEngine.displayName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                )
                                if (imageGenEngine == com.example.ai.ImageGenEngine.COMFY_UI) {
                                    Text(
                                        text = "Target URL: $comfyUiUrl\nModel: $comfyUiCheckpoint",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF7F1D1D))
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Diagnostic Details & Raw Server Response:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = fullReport,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        if (imageGenEngine == com.example.ai.ImageGenEngine.COMFY_UI) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Common Fixes for ComfyUI 400 Errors:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    )
                                    Text(
                                        text = "• Checkpoint not found: Ensure '$comfyUiCheckpoint' exists in your ComfyUI/models/checkpoints folder.\n• Custom Workflow: If using custom workflow JSON, ensure it is exported via 'Save (API Format)' in Dev Mode, not the standard browser Save format.\n• Missing custom nodes: Check if the workflow requires custom nodes not installed.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = Color(0xFF78350F), lineHeight = 15.sp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(fullReport))
                                Toast.makeText(context, "Diagnostic log copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Log", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.closeCoverErrorDialog()
                                viewModel.generateRecipeCoverArt(activeRecipe, context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 12.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeCoverErrorDialog() }) {
                        Text("Dismiss")
                    }
                }
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
                        text = "Are you sure you want to permanently delete '${activeRecipe.getDisplayTitle()}' from your compendium? This action cannot be undone."
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
    }
}

/**
 * Heirloom Two-Page Open Book Spread for Tablet Viewports.
 * Displays Left & Right pages simultaneously with vintage stitched spine depth.
 */
@Composable
fun TabletBookletSpreadView(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onOpenConverter: ((String?, String, String) -> Unit)? = null,
    servingMultiplier: Float,
    onSetMultiplier: (Float) -> Unit,
    onOpenGlossary: (String) -> Unit,
    checkedIngredients: Set<Int>,
    onToggleIngredient: (Int) -> Unit,
    checkedSteps: Set<Int>,
    onToggleStep: (Int) -> Unit,
    onStartTimer: (Int) -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onEditRecipe: (Int) -> Unit,
    onGenerateAiCover: () -> Unit,
    onRemoveCoverPhoto: () -> Unit,
    isGeneratingCover: Boolean,
    onAddToShoppingList: () -> Unit,
    onSaveJournal: (String, Int) -> Unit,
    onIncrementCooked: () -> Unit,
    onStartCookingMode: (() -> Unit)? = null,
    onRotateOriginalPhoto: (() -> Unit)? = null,
    onPlayTurnSound: () -> Unit = {},
    soundEffectsEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSpread by remember { mutableStateOf(1) } // 0: Cover & Lore, 1: Ingredients & Method, 2: Journal & Notes
    var activeStepIndex by remember { mutableStateOf(0) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val changeSpread = { target: Int ->
        if (target in 0..2 && target != currentSpread) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onPlayTurnSound()
            currentSpread = target
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEDE5D8))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top Spread Navigation Bar with Stitched Paper Header
        Surface(
            color = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFD6C8B4)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Spread Button
                TextButton(
                    onClick = { changeSpread(currentSpread - 1) },
                    enabled = currentSpread > 0,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = if (currentSpread > 0) TerracottaPrimary else Color(0xFFA89F91), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppLocalization.getTurnBackLabel(languageMode), fontSize = 12.sp, color = if (currentSpread > 0) TerracottaPrimary else Color(0xFFA89F91), fontWeight = FontWeight.SemiBold)
                }

                // Spread Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val spreads = listOf(
                        Triple(0, "📖 Cover & Lore", "Cover"),
                        Triple(1, "🥗 Ingredients & Steps", "Recipe"),
                        Triple(2, "✍️ Cook's Journal", "Journal")
                    )

                    spreads.forEach { (idx, label, _) ->
                        val isSelected = currentSpread == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TerracottaPrimary else Color(0xFFF3ECE1),
                            border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else Color(0xFFD6C7B2)),
                            modifier = Modifier.clickable { changeSpread(idx) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF451A03),
                                    fontSize = 12.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Next Spread Button & Quick Mute
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { changeSpread(currentSpread + 1) },
                        enabled = currentSpread < 2,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(AppLocalization.getTurnPageLabel(languageMode), fontSize = 12.sp, color = if (currentSpread < 2) TerracottaPrimary else Color(0xFFA89F91), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (currentSpread < 2) TerracottaPrimary else Color(0xFFA89F91), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (soundEffectsEnabled) "Mute" else "Unmute",
                            tint = if (soundEffectsEnabled) TerracottaPrimary else Color(0xFF9E8E81),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Two-Page Open Book Container with 3D Animated Turn
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFAF7F2),
            shadowElevation = 8.dp,
            border = BorderStroke(2.dp, Color(0xFFD1C3B0)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(currentSpread, activeStepIndex, recipe.steps.size) {
                    var totalDragX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount
                        },
                        onDragEnd = {
                            val swipeThreshold = 50.dp.toPx()
                            if (totalDragX < -swipeThreshold) {
                                // Swiped Left -> Move Forward
                                if (currentSpread == 0) {
                                    changeSpread(1)
                                    activeStepIndex = 0
                                } else if (currentSpread == 1) {
                                    if (activeStepIndex < recipe.steps.size - 1) {
                                        activeStepIndex++
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    } else {
                                        changeSpread(2)
                                    }
                                }
                            } else if (totalDragX > swipeThreshold) {
                                // Swiped Right -> Move Backward
                                if (currentSpread == 2) {
                                    changeSpread(1)
                                    activeStepIndex = (recipe.steps.size - 1).coerceAtLeast(0)
                                } else if (currentSpread == 1) {
                                    if (activeStepIndex > 0) {
                                        activeStepIndex--
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    } else {
                                        changeSpread(0)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = currentSpread,
                transitionSpec = {
                    if (targetState > initialState) {
                        (fadeIn(animationSpec = tween(380)) + slideInHorizontally(animationSpec = tween(380)) { width -> width / 3 })
                            .togetherWith(fadeOut(animationSpec = tween(320)) + slideOutHorizontally(animationSpec = tween(320)) { width -> -width / 3 })
                    } else {
                        (fadeIn(animationSpec = tween(380)) + slideInHorizontally(animationSpec = tween(380)) { width -> -width / 3 })
                            .togetherWith(fadeOut(animationSpec = tween(320)) + slideOutHorizontally(animationSpec = tween(320)) { width -> width / 3 })
                    }
                },
                label = "TabletSpreadTurnAnimation",
                modifier = Modifier.fillMaxSize()
            ) { spreadIndex ->
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Left Page
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFFDF9))
                    ) {
                        when (spreadIndex) {
                            0 -> {
                                RecipeCoverPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onOpenBook = { changeSpread(1) },
                                    onOpenStory = { changeSpread(0) },
                                    onEditRecipe = { onEditRecipe(0) },
                                    onGenerateAiCover = onGenerateAiCover,
                                    onRemoveCoverPhoto = onRemoveCoverPhoto,
                                    isGeneratingCover = isGeneratingCover
                                )
                            }
                            1 -> {
                                RecipeIngredientsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    unitSystem = unitSystem,
                                    onUnitSystemChange = onUnitSystemChange,
                                    onOpenConverter = onOpenConverter,
                                    servingMultiplier = servingMultiplier,
                                    onSetMultiplier = onSetMultiplier,
                                    onOpenGlossary = onOpenGlossary,
                                    checkedIngredients = checkedIngredients,
                                    onToggleCheck = onToggleIngredient,
                                    onNextPage = { changeSpread(2) },
                                    onEditIngredients = { onEditRecipe(1) },
                                    onAddToShoppingList = onAddToShoppingList
                                )
                            }
                            2 -> {
                                RecipeLoreTableOfContentsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onJumpToPage = { target ->
                                        if (target == 0) changeSpread(0)
                                        else if (target == 2) changeSpread(1)
                                        else changeSpread(2)
                                    },
                                    onEditDetails = { onEditRecipe(3) },
                                    onRotateOriginalPhoto = onRotateOriginalPhoto
                                )
                            }
                        }
                    }

                    // Center Stitched Book Spine & Deep Leather Shadow
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0x3D000000),
                                        Color(0x15000000),
                                        Color(0x05000000),
                                        Color(0x15000000),
                                        Color(0x3D000000)
                                    )
                                )
                            )
                    )

                    // Right Page
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFFDF9))
                    ) {
                        when (spreadIndex) {
                            0 -> {
                                RecipeLoreTableOfContentsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onJumpToPage = { target ->
                                        if (target == 2) changeSpread(1)
                                        else if (target >= 3) {
                                            changeSpread(1)
                                            activeStepIndex = (target - 3).coerceIn(0, (recipe.steps.size - 1).coerceAtLeast(0))
                                        } else changeSpread(0)
                                    },
                                    onEditDetails = { onEditRecipe(3) },
                                    onRotateOriginalPhoto = onRotateOriginalPhoto
                                )
                            }
                            1 -> {
                                if (recipe.steps.isNotEmpty()) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        // Step Selector Pill Bar
                                        if (recipe.steps.size > 1) {
                                            Surface(
                                                color = Color(0xFFF7F2E8),
                                                border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                LazyRow(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    itemsIndexed(recipe.steps) { idx, _ ->
                                                        val isSelected = activeStepIndex == idx
                                                        val isDone = checkedSteps.contains(idx)
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = when {
                                                                isSelected -> TerracottaPrimary
                                                                isDone -> SageGreen
                                                                else -> Color(0xFFEAE0D2)
                                                            },
                                                            modifier = Modifier.clickable {
                                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                                activeStepIndex = idx
                                                            }
                                                        ) {
                                                            Text(
                                                                text = "Step ${idx + 1}${if (isDone) " ✓" else ""}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = if (isSelected || isDone) Color.White else Color(0xFF451A03),
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                    fontSize = 11.sp
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Active Step View
                                        val safeStep = recipe.steps.getOrNull(activeStepIndex.coerceIn(0, recipe.steps.size - 1))
                                        if (safeStep != null) {
                                            RecipeStepPage(
                                                step = safeStep,
                                                totalSteps = recipe.steps.size,
                                                recipe = recipe,
                                                unitSystem = unitSystem,
                                                onUnitSystemChange = onUnitSystemChange,
                                                onOpenConverter = onOpenConverter,
                                                servingMultiplier = servingMultiplier,
                                                languageMode = languageMode,
                                                isCompleted = checkedSteps.contains(activeStepIndex),
                                                onToggleCompleted = { onToggleStep(activeStepIndex) },
                                                onStartTimer = onStartTimer,
                                                onSpeak = onSpeak,
                                                onNextPage = {
                                                    if (activeStepIndex < recipe.steps.size - 1) {
                                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                        activeStepIndex++
                                                    } else {
                                                        changeSpread(2)
                                                    }
                                                },
                                                onEditStep = { onEditRecipe(2) }
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No preparation steps listed for this recipe.",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF8C7B6B))
                                        )
                                    }
                                }
                            }
                            2 -> {
                                RecipeCookJournalPage(
                                    recipe = recipe,
                                    onSaveJournal = onSaveJournal,
                                    onIncrementCooked = onIncrementCooked,
                                    onStartCookingMode = onStartCookingMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
