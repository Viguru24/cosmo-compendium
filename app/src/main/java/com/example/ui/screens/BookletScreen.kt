package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.VolumeUp
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

    // Pages: 0: Cover, 1: Lore & TOC, 2: Ingredients & Scaler, 3..3+steps: Steps, last: Journal
    val totalPages = 4 + recipe.steps.size
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

    if (isCookMode) {
        KitchenCookModeScreen(
            recipe = recipe,
            activeStepIndex = activeCookStep,
            onStepChange = { viewModel.activeCookStep.value = it },
            languageMode = languageMode,
            unitSystem = unitSystem,
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
            recipe = recipe,
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
                                text = recipe.getDisplayTitle(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = TerracottaPrimary
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "Heirloom Recipe",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_to_bookshelf_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF451A03))
                        }
                    },
                    actions = {
                        // Prominent Edit Recipe Button
                        FilledTonalButton(
                            onClick = {
                                val targetTab = when (pagerState.currentPage) {
                                    2 -> 1 // Ingredients tab
                                    in 3 until (totalPages - 1) -> 2 // Steps tab
                                    totalPages - 1 -> 3 // Lore & Notes tab
                                    else -> 0 // Basics tab
                                }
                                viewModel.openRecipeEditor(recipe, initialTab = targetTab)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFEDE4D6),
                                contentColor = Color(0xFF4A3828)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .testTag("edit_recipe_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Recipe", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Shopping List button with badge
                        IconButton(
                            onClick = { viewModel.openShoppingList() },
                            modifier = Modifier.testTag("booklet_open_shopping_list_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (uncheckedShoppingCount > 0) {
                                        Badge(
                                            containerColor = TerracottaPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "$uncheckedShoppingCount", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping List",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Share Recipe Card
                        IconButton(onClick = { viewModel.isShareDialogOpen.value = true }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share Recipe",
                                tint = TerracottaPrimary
                            )
                        }

                        // Favorite Toggle
                        IconButton(onClick = { viewModel.toggleFavorite(recipe) }) {
                            Icon(
                                if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (recipe.isFavorite) Color(0xFFB91C1C) else Color(0xFF8C7A6B)
                            )
                        }

                        // More Actions Menu
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color(0xFF5A493B)
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
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
            },
            bottomBar = {
                // Bottom Page Turner Navigation Bar & Cook Mode Button
                Surface(
                    color = Color(0xFFFAF7F2),
                    shadowElevation = 8.dp
                ) {
                    Column {
                        // Page quick thumb tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEDE5D8))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                0 to AppLocalization.getTabCover(languageMode),
                                1 to AppLocalization.getTabIndex(languageMode),
                                2 to AppLocalization.getTabIngredients(languageMode),
                                3 to AppLocalization.getTabSteps(languageMode),
                                (totalPages - 1) to AppLocalization.getTabJournal(languageMode)
                            ).forEach { (targetPage, label) ->
                                val isCurrent = when (targetPage) {
                                    3 -> pagerState.currentPage in 3 until totalPages - 1
                                    else -> pagerState.currentPage == targetPage
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCurrent) TerracottaPrimary else Color.Transparent,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(targetPage)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) Color.White else Color(0xFF5A493B),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Page info subtitle
                        Text(
                            text = when (pagerState.currentPage) {
                                0 -> "Book Cover"
                                1 -> "Page 1: Heritage & Table of Contents"
                                2 -> "Page 2: Ingredients & Portion Scaler"
                                totalPages - 1 -> "Cook's Journal & Notes"
                                else -> "Step ${pagerState.currentPage - 2} of ${recipe.steps.size}"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B5B4E),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp)
                        )

                        // Navigation Actions Row: Back | Cook Mode | Next
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Page Button
                            OutlinedButton(
                                onClick = {
                                    if (pagerState.currentPage > 0) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage > 0,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFC7BCAE)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A3828)),
                                modifier = Modifier.testTag("prev_page_button")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Turn Back", modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Back",
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            // Cook Mode button
                            Button(
                                onClick = {
                                    viewModel.activeCookStep.value = (pagerState.currentPage - 3).coerceAtLeast(0)
                                    viewModel.isCookMode.value = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("start_cooking_mode_button")
                            ) {
                                Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cook Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            // Next Page Button
                            OutlinedButton(
                                onClick = {
                                    if (pagerState.currentPage < totalPages - 1) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage < totalPages - 1,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFC7BCAE)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A3828)),
                                modifier = Modifier.testTag("next_page_button")
                            ) {
                                Text(
                                    text = "Next",
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Turn Page", modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onOpenBook = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(2)
                                        }
                                    },
                                    onOpenStory = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    },
                                    onEditRecipe = {
                                        viewModel.openRecipeEditor(recipe, initialTab = 0)
                                    }
                                )
                            }
                            1 -> {
                                // Lore & Table of Contents Page
                                RecipeLoreTableOfContentsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onJumpToPage = { target ->
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(target.coerceIn(0, totalPages - 1))
                                        }
                                    },
                                    onEditDetails = {
                                        viewModel.openRecipeEditor(recipe, initialTab = 3)
                                    }
                                )
                            }
                            2 -> {
                                // Ingredients & Portion Scaler Page
                                RecipeIngredientsPage(
                                    recipe = recipe,
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
                                        viewModel.openRecipeEditor(recipe, initialTab = 1)
                                    },
                                    onAddToShoppingList = {
                                        viewModel.addIngredientsToShoppingList(
                                            recipe = recipe,
                                            multiplier = servingMultiplier,
                                            unitSystem = unitSystem
                                        )
                                        Toast.makeText(context, "Added ${recipe.ingredients.size} ingredients to shopping list!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            totalPages - 1 -> {
                                // Cook's Journal & Memories (Final Page)
                                RecipeCookJournalPage(
                                    recipe = recipe,
                                    onSaveJournal = { notes, rating ->
                                        viewModel.saveRecipeJournal(recipe.id, notes, rating)
                                    },
                                    onIncrementCooked = { viewModel.markRecipeCooked() },
                                    onStartCookingMode = {
                                        viewModel.activeCookStep.value = 0
                                        viewModel.isCookMode.value = true
                                    }
                                )
                            }
                            else -> {
                                // Step-by-Step Pages
                                val stepIndex = page - 3
                                val step = recipe.steps.getOrNull(stepIndex)
                                if (step != null) {
                                    RecipeStepPage(
                                        step = step,
                                        totalSteps = recipe.steps.size,
                                        languageMode = languageMode,
                                        isCompleted = checkedSteps.contains(stepIndex),
                                        onToggleCompleted = { viewModel.toggleStepChecked(stepIndex) },
                                        onStartTimer = { viewModel.startTimerForStep(it) },
                                        onSpeak = { text, isGerman -> viewModel.speakStep(text, isGerman) },
                                        onNextPage = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(page + 1)
                                            }
                                        },
                                        onEditStep = {
                                            viewModel.openRecipeEditor(recipe, initialTab = 2)
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
                recipe = recipe,
                servingMultiplier = servingMultiplier,
                unitSystem = unitSystem,
                onAddToShoppingList = {
                    viewModel.addIngredientsToShoppingList(
                        recipe = recipe,
                        multiplier = servingMultiplier,
                        unitSystem = unitSystem
                    )
                    Toast.makeText(context, "Added ${recipe.ingredients.size} ingredients to shopping list!", Toast.LENGTH_SHORT).show()
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
            EditRecipeDialog(
                initialRecipe = editingRecipeDraft ?: recipe,
                initialTab = recipeEditorInitialTab,
                onSave = { updated ->
                    viewModel.saveEditedRecipe(updated)
                },
                onDelete = { toDelete ->
                    viewModel.deleteRecipe(toDelete)
                    onBack()
                },
                onDismiss = { viewModel.closeRecipeEditor() }
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
                        text = "Are you sure you want to permanently delete '${recipe.getDisplayTitle()}' from your heirloom cookbook? This action cannot be undone."
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
