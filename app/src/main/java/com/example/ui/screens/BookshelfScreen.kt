package com.example.ui.screens

import com.example.data.model.getCharacteristicBadge
import com.example.ui.components.CategoryManagerDialog
import com.example.ui.components.EditAiPhotoPromptDialog
import com.example.ui.components.bookshelf.BookshelfSearchBar
import com.example.ui.components.bookshelf.BookshelfTopBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import java.io.File
import com.example.ui.components.RealisticLeatherBackground
import com.example.ui.components.CookbookGuideDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RecipeEntity
import com.example.data.model.CoverTheme
import com.example.data.model.LanguageMode
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.BatchCoverGenerationDialog
import com.example.ui.components.EditRecipeDialog
import com.example.ui.components.ErrorLogDialog
import com.example.ui.components.ScanRecipeBottomSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShareRecipeCardDialog
import com.example.ui.components.ShoppingListDialog
import com.example.ui.components.SmartConverterBottomSheet
import android.net.Uri
import android.widget.Toast
import com.example.ui.theme.CreamBackgroundLight
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import com.example.ui.components.ProfilePill
import com.example.ui.components.ProfileSwitcherSheet
import com.example.ui.components.AssignRecipeProfileDialog
import com.example.ui.components.SousChefChatSheet
import com.example.ui.components.VoiceInputOrb
import com.example.ui.components.getProfileEmoji
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle
import com.example.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (RecipeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val defaultProfile by viewModel.defaultProfile.collectAsStateWithLifecycle()
    val familyProfiles by viewModel.familyProfiles.collectAsStateWithLifecycle()
    val isProfileSwitcherOpen by viewModel.isProfileSwitcherOpen.collectAsStateWithLifecycle()
    val profileRecipeCounts by viewModel.profileRecipeCounts.collectAsStateWithLifecycle()

    val isSousChefOpen by viewModel.isSousChefOpen.collectAsStateWithLifecycle()
    val sousChefMessages by viewModel.sousChefMessages.collectAsStateWithLifecycle()
    val isSousChefProcessing by viewModel.isSousChefProcessing.collectAsStateWithLifecycle()
    val triggerScanFromSousChef by viewModel.triggerScanFromSousChef.collectAsStateWithLifecycle()

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scannedDraftRecipe by viewModel.scannedDraftRecipe.collectAsStateWithLifecycle()
    val scanErrorMessage by viewModel.scanErrorMessage.collectAsStateWithLifecycle()
    val duplicatePrompt by viewModel.duplicatePrompt.collectAsStateWithLifecycle()
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
    val autoWeeklyBackupEnabled by viewModel.autoWeeklyBackupEnabled.collectAsStateWithLifecycle()
    val savedBackups by viewModel.savedBackupsList.collectAsStateWithLifecycle()
    val categories by viewModel.allAvailableCategories.collectAsStateWithLifecycle()
    val aiProvider by viewModel.aiProvider.collectAsStateWithLifecycle()
    val isGuideOpen by viewModel.isGuideOpen.collectAsStateWithLifecycle()
    val guideInitialTopic by viewModel.guideInitialTopic.collectAsStateWithLifecycle()
    val geminiApiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val geminiModel by viewModel.geminiModel.collectAsStateWithLifecycle()
    val geminiApiTestStatus by viewModel.geminiApiTestStatus.collectAsStateWithLifecycle()
    val isTestingGeminiApi by viewModel.isTestingGeminiApi.collectAsStateWithLifecycle()
    val imageGenEngine by viewModel.imageGenEngine.collectAsStateWithLifecycle()
    val comfyUiUrl by viewModel.comfyUiUrl.collectAsStateWithLifecycle()
    val comfyUiCheckpoint by viewModel.comfyUiCheckpoint.collectAsStateWithLifecycle()
    val comfyUiCustomWorkflow by viewModel.comfyUiCustomWorkflow.collectAsStateWithLifecycle()
    val comfyUiTestStatus by viewModel.comfyUiTestStatus.collectAsStateWithLifecycle()
    val isTestingComfyConnection by viewModel.isTestingComfyConnection.collectAsStateWithLifecycle()
    val availableComfyCheckpoints by viewModel.availableComfyCheckpoints.collectAsStateWithLifecycle()

    // Cloud & Family Sync States
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsStateWithLifecycle()
    val syncServerUrl by viewModel.syncServerUrl.collectAsStateWithLifecycle()
    val syncSecretToken by viewModel.syncSecretToken.collectAsStateWithLifecycle()
    val isAutoSyncWifi by viewModel.isAutoSyncWifi.collectAsStateWithLifecycle()
    val isTestingSyncConnection by viewModel.isTestingSyncConnection.collectAsStateWithLifecycle()
    val syncConnectionTestResult by viewModel.syncConnectionTestResult.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncStatus by viewModel.lastSyncStatus.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isRecipeEditorOpen by viewModel.isRecipeEditorOpen.collectAsStateWithLifecycle()
    val editingRecipeDraft by viewModel.editingRecipeDraft.collectAsStateWithLifecycle()
    val isShoppingListOpen by viewModel.isShoppingListOpen.collectAsStateWithLifecycle()
    val uncheckedShoppingCount by viewModel.uncheckedShoppingCount.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val navigateToRecipe by viewModel.navigateToRecipeEvent.collectAsStateWithLifecycle()
    val lastAutoSavedRecipe by viewModel.lastAutoSavedRecipe.collectAsStateWithLifecycle()
    val scanBatchSuccessEvent by viewModel.scanBatchSuccessEvent.collectAsStateWithLifecycle()
    val scanBatchCount by viewModel.scanBatchCount.collectAsStateWithLifecycle()
    val totalRecipeCount by viewModel.totalRecipeCount.collectAsStateWithLifecycle()
    val activeProfileTotalCount by viewModel.activeProfileTotalCount.collectAsStateWithLifecycle()
    val activeProfileFavoritesCount by viewModel.activeProfileFavoritesCount.collectAsStateWithLifecycle()
    val activeProfileCategoryCounts by viewModel.activeProfileCategoryCounts.collectAsStateWithLifecycle()
    val cookbookStats by viewModel.cookbookStats.collectAsStateWithLifecycle()
    val recipePhotoStats by viewModel.recipePhotoStats.collectAsStateWithLifecycle()
    val selectedBatchFilter by viewModel.selectedBatchFilter.collectAsStateWithLifecycle()
    val isBatchGeneratingCovers by viewModel.isBatchGeneratingCovers.collectAsStateWithLifecycle()
    val batchCoverProgress by viewModel.batchCoverProgress.collectAsStateWithLifecycle()
    val batchCoverCurrentTitle by viewModel.batchCoverCurrentTitle.collectAsStateWithLifecycle()
    val batchCoverSuccessCount by viewModel.batchCoverSuccessCount.collectAsStateWithLifecycle()
    val batchCoverFailCount by viewModel.batchCoverFailCount.collectAsStateWithLifecycle()
    val batchCoverLog by viewModel.batchCoverLog.collectAsStateWithLifecycle()
    val showBatchCoverDialog by viewModel.showBatchCoverDialog.collectAsStateWithLifecycle()
    val recipesMissingPhotosCount by viewModel.recipesMissingPhotosCount.collectAsStateWithLifecycle()
    val isExportingFullPdf by viewModel.isExportingFullPdf.collectAsStateWithLifecycle()
    val showErrorLogDialog by viewModel.showErrorLogDialog.collectAsStateWithLifecycle()

    var showScanSheet by remember { mutableStateOf(false) }
    var recipePendingDelete by remember { mutableStateOf<RecipeEntity?>(null) }
    var recipePendingShare by remember { mutableStateOf<RecipeEntity?>(null) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var recipeForQuickCategoryAssign by remember { mutableStateOf<RecipeEntity?>(null) }
    var recipeForProfileAssign by remember { mutableStateOf<RecipeEntity?>(null) }

    LaunchedEffect(triggerScanFromSousChef) {
        if (triggerScanFromSousChef) {
            viewModel.clearTriggerScanFromSousChef()
            viewModel.closeSousChef()
            showScanSheet = true
        }
    }

    LaunchedEffect(navigateToRecipe) {
        navigateToRecipe?.let { recipe ->
            showScanSheet = false
            onRecipeClick(recipe)
            viewModel.clearNavigateToRecipeEvent()
        }
    }

    val rawCategories = listOf("All") + categories

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("bookshelf_screen"),
        containerColor = Color(0xFFF3EDE4),
        topBar = {
            BookshelfTopBar(
                activeProfile = activeProfile,
                uncheckedShoppingCount = uncheckedShoppingCount,
                soundEffectsEnabled = soundEffectsEnabled,
                languageMode = languageMode,
                onOpenProfileSwitcher = { viewModel.openProfileSwitcher() },
                onOpenSousChef = { viewModel.openSousChef() },
                onOpenShoppingList = { viewModel.openShoppingList() },
                onToggleSoundEffects = {
                    viewModel.setSoundEffectsEnabled(!soundEffectsEnabled)
                },
                onOpenGuide = { viewModel.openGuide() },
                onOpenSettings = { viewModel.openSettings() }
            )
        },
        floatingActionButton = {
            if (!isSousChefOpen && !isProfileSwitcherOpen && !isSmartConverterOpen) {
                Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E140C),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Color(0xFF4A3A2F)),
                modifier = Modifier
                    .padding(bottom = 4.dp, end = 4.dp)
                    .height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    // Sous-Chef AI Segment
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.openSousChef() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("👨‍🍳", fontSize = 14.sp)
                        Text(
                            "Sous-Chef",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Subtle Divider
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .width(1.dp)
                            .background(Color(0xFF5A493E))
                    )

                    // Scan Recipe Segment
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showScanSheet = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("scan_recipe_fab"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Scan",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = AppLocalization.getScanButtonLabel(languageMode),
                            color = Color(0xFFFFD1B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF6F0E8),
                            Color(0xFFECE4D8),
                            Color(0xFFE7DDD0)
                        )
                    )
                )
        ) {
            // Modular Search Bar & Filter Section
            BookshelfSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.searchQuery.value = it },
                activeProfile = activeProfile,
                activeProfileTotalCount = activeProfileTotalCount,
                activeProfileFavoritesCount = activeProfileFavoritesCount,
                selectedCategory = selectedCategory,
                onlyFavorites = onlyFavorites,
                rawCategories = rawCategories,
                activeProfileCategoryCounts = activeProfileCategoryCounts,
                totalRecipeCount = totalRecipeCount,
                languageMode = languageMode,
                onSelectFavorites = { viewModel.onlyFavorites.value = true },
                onSelectCategory = { cat ->
                    viewModel.onlyFavorites.value = false
                    viewModel.selectedCategory.value = cat
                },
                onExportCompleteCookbookPdf = { viewModel.exportFullCookbookPdf(context) },
                onOpenCategoryManager = { showCategoryManagerDialog = true }
            )

            // Recipe Bookshelf Grid
            if (recipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 500.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TerracottaPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (activeProfile == AppLocalization.getFilterAllFamily(languageMode)) AppLocalization.getEmptyStateTitle(languageMode) else AppLocalization.getEmptyProfileTitle(activeProfile, languageMode),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C1E14),
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = AppLocalization.getEmptyStateSubtitle(languageMode),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF786555),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    fontSize = 12.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Primary Action: Scan Recipe Cards
                            Button(
                                onClick = { showScanSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(AppLocalization.getScanPhysicalCardsButton(languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Guide Tour Button
                            Surface(
                                onClick = { viewModel.openGuide() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("✨", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (languageMode == com.example.data.model.LanguageMode.GERMAN) "Schnelle 1-Minuten-Anleitung ansehen" else "Take the 1-Minute Cookbook Tour",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Secondary Action: Restore Backup
                            OutlinedButton(
                                onClick = { viewModel.openBackupDialog() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFB0A294)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Text(AppLocalization.getRestoreBackupButton(languageMode), color = Color(0xFF2C1E14), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val isTablet = maxWidth >= 600.dp
                    val columns = if (!isTablet) {
                        GridCells.Fixed(2)
                    } else if (maxWidth >= 1000.dp) {
                        GridCells.Adaptive(minSize = 220.dp)
                    } else {
                        GridCells.Adaptive(minSize = 190.dp)
                    }
                    LazyVerticalGrid(
                        columns = columns,
                        contentPadding = PaddingValues(
                            start = if (isTablet) 24.dp else 12.dp,
                            end = if (isTablet) 24.dp else 12.dp,
                            top = if (isTablet) 18.dp else 12.dp,
                            bottom = 96.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1280.dp)
                    ) {
                        items(recipes, key = { it.id }) { recipe ->
                            RecipeBookCard(
                                recipe = recipe,
                                languageMode = languageMode,
                                activeProfile = activeProfile,
                                onClick = { onRecipeClick(recipe) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                                onEditClick = { viewModel.openRecipeEditor(recipe) },
                                onGeneratePhotoClick = { viewModel.generateRecipeCoverArt(recipe, context) },
                                onEditPromptClick = { viewModel.openCustomPromptDialog(recipe) },
                                onAssignCategoryClick = { recipeForQuickCategoryAssign = recipe },
                                onAssignProfileClick = { recipeForProfileAssign = recipe },
                                onShareClick = { recipePendingShare = recipe },
                                onDeleteClick = { recipePendingDelete = recipe }
                            )
                        }
                    }
                }
            }
        }

        // Profile Switcher Sheet ("Who's Cooking?")
        if (isProfileSwitcherOpen) {
            ProfileSwitcherSheet(
                activeProfile = activeProfile,
                profiles = familyProfiles,
                recipeCounts = profileRecipeCounts,
                defaultProfile = defaultProfile,
                onSelectProfile = { viewModel.setActiveProfile(it) },
                onSetDefaultProfile = { viewModel.setDeviceDefaultProfile(it) },
                onAddProfile = { viewModel.addFamilyProfile(it) },
                onRenameProfile = { old, new -> viewModel.renameFamilyProfile(old, new) },
                onDeleteProfile = { viewModel.deleteFamilyProfile(it) },
                onBulkMove = { from, to -> viewModel.bulkMoveRecipes(from, to) },
                onDismiss = { viewModel.closeProfileSwitcher() }
            )
        }

        // AI Photo Prompt Studio Dialog
        val customPromptDialogRecipe by viewModel.customPromptDialogRecipe.collectAsStateWithLifecycle()
        val isGeneratingCover by viewModel.isGeneratingCover.collectAsStateWithLifecycle()

        customPromptDialogRecipe?.let { promptRecipe ->
            EditAiPhotoPromptDialog(
                recipe = promptRecipe,
                isGenerating = isGeneratingCover,
                languageMode = languageMode,
                onDismiss = { viewModel.closeCustomPromptDialog() },
                onGenerate = { customPrompt ->
                    viewModel.generateRecipeCoverArt(
                        recipe = promptRecipe,
                        context = context,
                        customPrompt = customPrompt
                    )
                    viewModel.closeCustomPromptDialog()
                }
            )
        }

        // Sous-Chef AI Chat Sheet
        if (isSousChefOpen) {
            SousChefChatSheet(
                activeProfile = activeProfile,
                messages = sousChefMessages,
                isProcessing = isSousChefProcessing,
                onSendMessage = { viewModel.handleSousChefInput(it) },
                onQuickActionClick = { viewModel.handleSousChefInput(it) },
                onSelectRecipe = { recipe ->
                    viewModel.closeSousChef()
                    onRecipeClick(recipe)
                },
                onOpenErrorLogs = { viewModel.openErrorLogDialog() },
                onCancelProcessing = { viewModel.cancelSousChefProcessing() },
                onImportVideo = { uri -> viewModel.importRecipeFromVideo(context, uri) },
                onDismiss = { viewModel.closeSousChef() },
                languageMode = languageMode
            )
        }

        // 1-Tap Assign Profile Dialog
        if (recipeForProfileAssign != null) {
            AssignRecipeProfileDialog(
                recipe = recipeForProfileAssign!!,
                profiles = familyProfiles,
                onAssign = { targetProfile ->
                    viewModel.assignRecipeToProfile(recipeForProfileAssign!!, targetProfile)
                    Toast.makeText(context, "Moved to $targetProfile's Cookbook", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { recipeForProfileAssign = null }
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
                aiProvider = aiProvider,
                onAiProviderChange = { viewModel.setAiProvider(it) },
                aiBaseUrl = viewModel.getAiBaseUrl(aiProvider),
                onAiBaseUrlChange = { viewModel.setAiBaseUrl(aiProvider, it) },
                geminiApiKey = geminiApiKey,
                onGeminiApiKeyChange = { viewModel.setGeminiApiKey(it) },
                geminiModel = geminiModel,
                onGeminiModelChange = { viewModel.setGeminiModel(it) },
                onTestGeminiApiKey = { viewModel.testGeminiApiConnection() },
                geminiApiTestStatus = geminiApiTestStatus,
                isTestingGeminiApi = isTestingGeminiApi,
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
                availableComfyCheckpoints = availableComfyCheckpoints,
                // Cloud & Family Sync bindings
                isCloudSyncEnabled = isCloudSyncEnabled,
                onToggleCloudSync = { viewModel.setCloudSyncEnabled(it) },
                syncServerUrl = syncServerUrl,
                onSyncServerUrlChange = { viewModel.setSyncServerUrl(it) },
                syncSecretToken = syncSecretToken,
                onSyncSecretTokenChange = { viewModel.setSyncSecretToken(it) },
                onTestSyncConnection = { viewModel.testSyncConnection() },
                isTestingSyncConnection = isTestingSyncConnection,
                syncConnectionTestResult = syncConnectionTestResult,
                onTriggerSyncNow = { viewModel.triggerSyncNow() },
                isSyncing = isSyncing,
                lastSyncStatus = lastSyncStatus,
                lastSyncTimestamp = lastSyncTimestamp,
                autoSyncWifi = isAutoSyncWifi,
                onToggleAutoSyncWifi = { viewModel.setAutoSyncWifi(it) },
                onOpenBackup = {
                    viewModel.closeSettings()
                    viewModel.openBackupDialog()
                },
                onOpenSmartConverter = {
                    viewModel.closeSettings()
                    viewModel.openSmartConverter("baking_soda", "2", "g")
                },
                onOpenGuide = {
                    viewModel.closeSettings()
                    viewModel.openGuide()
                },
                onDeleteAllRecipes = {
                    viewModel.deleteAllRecipes()
                },
                totalRecipeCount = totalRecipeCount,
                recipesWithPhotos = cookbookStats.recipesWithPhotos,
                aiPhotosCount = cookbookStats.aiPhotosCount,
                scannedCardsCount = cookbookStats.scannedCardsCount,
                unphotographedCount = cookbookStats.unphotographedCount,
                photoStorageMb = cookbookStats.estimatedStorageMb,
                defaultProfile = defaultProfile,
                familyProfiles = familyProfiles,
                onSetDefaultProfile = { viewModel.setDeviceDefaultProfile(it) },
                onOpenBatchCoverGen = {
                    viewModel.closeSettings()
                    viewModel.openBatchCoverDialog()
                },
                onExportFullCookbookPdf = {
                    viewModel.closeSettings()
                    Toast.makeText(context, "Compiling Master Cookbook PDF (with AI Photos & Table of Contents)...", Toast.LENGTH_SHORT).show()
                    viewModel.exportFullCookbookPdf(context)
                },
                onOpenErrorLogs = { viewModel.openErrorLogDialog() },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // Interactive Cookbook Guide & Onboarding Tour Dialog
        if (isGuideOpen) {
            CookbookGuideDialog(
                initialTopic = guideInitialTopic,
                onDismiss = { viewModel.closeGuide() },
                onStartScanCards = {
                    viewModel.closeGuide()
                    showScanSheet = true
                },
                onStartImportVideo = {
                    viewModel.closeGuide()
                    showScanSheet = true
                },
                onOpenSousChef = {
                    viewModel.closeGuide()
                    viewModel.openSousChef()
                },
                onOpenConverter = {
                    viewModel.closeGuide()
                    viewModel.openSmartConverter()
                }
            )
        }

        // Diagnostic Error Logs Dialog
        if (showErrorLogDialog) {
            ErrorLogDialog(
                onDismiss = { viewModel.closeErrorLogDialog() }
            )
        }

        // Batch AI Photo Studio Dialog
        if (showBatchCoverDialog) {
            BatchCoverGenerationDialog(
                isGenerating = isBatchGeneratingCovers,
                progress = batchCoverProgress,
                currentTitle = batchCoverCurrentTitle,
                successCount = batchCoverSuccessCount,
                failCount = batchCoverFailCount,
                stats = recipePhotoStats,
                selectedFilter = selectedBatchFilter,
                engine = imageGenEngine,
                statusLog = batchCoverLog,
                onFilterChange = { viewModel.setBatchFilter(it) },
                onStartBatch = { filter -> viewModel.startBatchGenerateMissingCovers(context, filter) },
                onCancelBatch = { viewModel.cancelBatchCoverGeneration() },
                onDismiss = { viewModel.closeBatchCoverDialog() }
            )
        }

        // Backup and Restore Dialog
        if (isBackupDialogOpen) {
            BackupRestoreDialog(
                totalRecipeCount = recipes.size,
                lastBackupDate = lastBackupDate,
                pendingRestoreManifest = pendingRestoreManifest,
                statusMessage = backupStatusMessage,
                isRestoring = isRestoring,
                autoWeeklyBackupEnabled = autoWeeklyBackupEnabled,
                onToggleAutoWeeklyBackup = { viewModel.setAutoWeeklyBackupEnabled(it) },
                savedBackups = savedBackups,
                onCreateInstantBackup = {
                    viewModel.createInstantBackup()
                },
                onDirectRestoreBackup = { file, replaceExisting ->
                    viewModel.directRestoreBackupFile(file, replaceExisting)
                },
                onDeleteSavedBackup = { file ->
                    viewModel.deleteSavedBackup(file)
                },
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
                onInspectText = { text ->
                    viewModel.inspectBackupText(text)
                },
                onRestoreStarterRecipes = { replaceExisting ->
                    viewModel.restoreStarterRecipes(replaceExisting)
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

        // Scan Sheet Dialog
        if (showScanSheet || scannedDraftRecipe != null || isScanning || duplicatePrompt != null) {
            ScanRecipeBottomSheet(
                isScanning = isScanning,
                draftRecipe = scannedDraftRecipe,
                errorMessage = scanErrorMessage,
                duplicatePrompt = duplicatePrompt,
                lastAutoSavedRecipe = lastAutoSavedRecipe,
                scanBatchSuccessTimestamp = scanBatchSuccessEvent,
                scanBatchCount = scanBatchCount,
                onResolveDuplicateUpdate = { prompt ->
                    viewModel.resolveDuplicateUpdate(prompt)
                    Toast.makeText(context, "Updated existing '${prompt.existingRecipe.title}' with new scan details", Toast.LENGTH_SHORT).show()
                },
                onResolveDuplicateSaveCopy = { prompt ->
                    viewModel.resolveDuplicateSaveAsCopy(prompt)
                    Toast.makeText(context, "Saved as new recipe variation", Toast.LENGTH_SHORT).show()
                },
                onDismissDuplicate = {
                    viewModel.dismissDuplicatePrompt()
                },
                onClearError = { viewModel.scanErrorMessage.value = null },
                onScan = { bitmaps, text, imageUri ->
                    viewModel.scanRecipe(bitmaps, text, imageUri)
                },
                onSaveDraft = { recipe ->
                    viewModel.saveDraftRecipe(recipe)
                    showScanSheet = false
                },
                onImportVideo = { uri ->
                    viewModel.importRecipeFromVideo(context, uri)
                },
                onDismiss = {
                    showScanSheet = false
                    viewModel.scannedDraftRecipe.value = null
                    viewModel.scanErrorMessage.value = null
                    viewModel.dismissDuplicatePrompt()
                    viewModel.resetScanBatchSession()
                }
            )
        }

        // Manual Recipe Editor Dialog
        if (isRecipeEditorOpen && editingRecipeDraft != null) {
            val draft = editingRecipeDraft!!
            EditRecipeDialog(
                initialRecipe = draft,
                categories = categories,
                onSave = { updated ->
                    viewModel.saveEditedRecipe(updated)
                },
                onDelete = { toDelete ->
                    viewModel.deleteRecipe(toDelete)
                },
                onOpenPromptStudio = {
                    viewModel.openCustomPromptDialog(draft)
                },
                onDismiss = { viewModel.closeRecipeEditor() }
            )
        }

        // Share Recipe Card Dialog
        if (recipePendingShare != null) {
            val shareRecipe = recipePendingShare!!
            ShareRecipeCardDialog(
                recipe = shareRecipe,
                onAddToShoppingList = {
                    viewModel.addIngredientsToShoppingList(
                        recipe = shareRecipe,
                        multiplier = 1.0f,
                        unitSystem = unitSystem
                    )
                    Toast.makeText(context, "Added ${shareRecipe.ingredients.size} ingredients to shopping list!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { recipePendingShare = null }
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

        // Quick Assign Category / Family Member Dialog
        if (recipeForQuickCategoryAssign != null) {
            val targetRecipe = recipeForQuickCategoryAssign!!
            QuickAssignCategoryDialog(
                recipe = targetRecipe,
                categories = categories,
                languageMode = languageMode,
                onAssign = { selectedCat ->
                    viewModel.quickAssignCategory(targetRecipe, selectedCat)
                    recipeForQuickCategoryAssign = null
                },
                onDismiss = { recipeForQuickCategoryAssign = null }
            )
        }

        // Category Manager Dialog (Add, Edit, Delete, Reorder)
        if (showCategoryManagerDialog) {
            CategoryManagerDialog(
                categories = categories,
                allRecipes = recipes,
                onDismiss = { showCategoryManagerDialog = false },
                onAddCategory = { newName -> viewModel.addCategory(newName) },
                onRenameCategory = { oldName, newName -> viewModel.renameCategory(oldName, newName) },
                onDeleteCategory = { toDelete -> viewModel.deleteCategory(toDelete) },
                onMoveUp = { index -> viewModel.moveCategoryUp(index) },
                onMoveDown = { index -> viewModel.moveCategoryDown(index) },
                onResetDefaults = { viewModel.resetCategoriesToDefault() },
                languageMode = languageMode
            )
        }

        // Bookshelf Recipe Delete Confirmation Dialog
        if (recipePendingDelete != null) {
            val toDelete = recipePendingDelete!!
            AlertDialog(
                onDismissRequest = { recipePendingDelete = null },
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
                        text = "Are you sure you want to delete '${toDelete.getDisplayTitle(languageMode)}'? This action cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRecipe(toDelete)
                            recipePendingDelete = null
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
                    TextButton(onClick = { recipePendingDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun RecipeBookCard(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    activeProfile: String = "Louis",
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditClick: () -> Unit,
    onGeneratePhotoClick: () -> Unit = {},
    onEditPromptClick: () -> Unit = {},
    onAssignCategoryClick: () -> Unit = {},
    onAssignProfileClick: () -> Unit = {},
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasPhoto = !recipe.imageUri.isNullOrBlank()
    val isAllFamilyView = activeProfile.equals("All", ignoreCase = true) || activeProfile.equals(AppLocalization.getFilterAllFamily(languageMode), ignoreCase = true)

    // Determine pleasant category pill colors
    val displayCat = recipe.getDisplayCategory(languageMode)
    val catLower = recipe.category.lowercase()
    val (catBg, catText, catBorder) = when {
        catLower.contains("baking") || catLower.contains("dessert") ->
            Triple(Color(0xFFFFF1E6), Color(0xFFC2410C), Color(0xFFFFD7BE))
        catLower.contains("main") ->
            Triple(Color(0xFFF0FDF4), Color(0xFF15803D), Color(0xFFBBF7D0))
        catLower.contains("soup") || catLower.contains("stew") ->
            Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
        catLower.contains("salad") || catLower.contains("side") ->
            Triple(Color(0xFFECFDF5), Color(0xFF047857), Color(0xFFA7F3D0))
        else ->
            Triple(Color(0xFFF5EFEB), Color(0xFF78350F), Color(0xFFE2D6C7))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = Color(0x223D2615), ambientColor = Color(0x113D2615))
            .testTag("recipe_book_card_${recipe.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8DFD5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            if (hasPhoto) {
                // Framed Dish Photo Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFFEFE8DE))
                ) {
                    val context = LocalContext.current
                    val imgData = if (recipe.imageUri!!.startsWith("/") || recipe.imageUri!!.startsWith("file://")) {
                        File(recipe.imageUri!!.removePrefix("file://"))
                    } else {
                        recipe.imageUri!!
                    }
                    val imageRequest = remember(recipe.imageUri, recipe.updatedAt, recipe.coverPhotoName) {
                        ImageRequest.Builder(context)
                            .data(imgData)
                            .memoryCacheKey("${recipe.imageUri}_${recipe.coverPhotoName}_${recipe.updatedAt}")
                            .diskCacheKey("${recipe.imageUri}_${recipe.coverPhotoName}_${recipe.updatedAt}")
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = recipe.getDisplayTitle(languageMode),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Subtle top gradient for chip visibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x66000000),
                                        Color.Transparent,
                                        Color(0x22000000)
                                    )
                                )
                            )
                    )

                    // Top Bar over photo: Category on left, Favorite & Menu on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xF2FFFFFF),
                                border = BorderStroke(0.8.dp, catBorder)
                            ) {
                                Text(
                                    text = displayCat,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = catText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isAllFamilyView) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xF2FFF7ED),
                                    border = BorderStroke(0.8.dp, TerracottaPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${getProfileEmoji(recipe.profileName)} ${recipe.profileName}",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TerracottaPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = onToggleFavorite,
                                shape = CircleShape,
                                color = Color(0xEEFFFFFF),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (recipe.isFavorite) Color(0xFFE11D48) else Color(0xFF4A3423),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Box {
                                Surface(
                                    onClick = { showMenu = !showMenu },
                                    shape = CircleShape,
                                    color = Color(0xEEFFFFFF),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "More Options",
                                            tint = Color(0xFF4A3423),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(if (!recipe.imageUri.isNullOrBlank()) "✨ Regenerate AI Food Photo" else "✨ Generate AI Food Photo")
                                                Text(
                                                    text = if (recipe.category.contains("Baking", ignoreCase = true)) "Smart Culinary Vision" else "Gourmet Food Art",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF78350F),
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onGeneratePhotoClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🎨 Edit AI Photo Prompt...") },
                                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onEditPromptClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Recipe") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onEditClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move to Family Member") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onAssignProfileClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Assign Category") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onAssignCategoryClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Recipe") },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onShareClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (languageMode == LanguageMode.GERMAN) "Rezept löschen" else "Delete Recipe", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onDeleteClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Text and Metadata Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (if no photo) or Title area
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!hasPhoto) {
                        // Top Bar: Category Pill Badge & Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = catBg,
                                    border = BorderStroke(1.dp, catBorder)
                                ) {
                                    Text(
                                        text = displayCat,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = catText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                val characteristicBadge = recipe.getCharacteristicBadge(languageMode)
                                if (characteristicBadge != null) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = "✦ $characteristicBadge",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF92400E),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isAllFamilyView) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFFFF7ED),
                                        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "${getProfileEmoji(recipe.profileName)} ${recipe.profileName}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TerracottaPrimary,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF5EFEB),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    IconButton(
                                        onClick = onToggleFavorite,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (recipe.isFavorite) Color(0xFFE11D48) else Color(0xFF8C7A6B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Box {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF5EFEB),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        IconButton(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "More Options",
                                                tint = Color(0xFF5A4535),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit Recipe") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onEditClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Move to Family Member") },
                                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onAssignProfileClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Assign Category") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onAssignCategoryClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Share Recipe") },
                                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onShareClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (languageMode == LanguageMode.GERMAN) "Rezept löschen" else "Delete Recipe", color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onDeleteClick()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Space under category badge
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Title placed top-left
                    val displayTitle = recipe.getDisplayTitle(languageMode)
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF1E140C),
                            lineHeight = 19.sp,
                            fontSize = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recipe.rating > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(recipe.rating) {
                                Text("★", color = Color(0xFFF59E0B), fontSize = 11.5.sp)
                            }
                        }
                    }
                }

                // Bottom Metadata Row: Cook Time, Difficulty & Servings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cook time
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFAF3EC),
                        border = BorderStroke(1.dp, Color(0xFFEDE0D2))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Cook Time",
                                tint = Color(0xFF9A3412),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${recipe.cookTimeMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF431407),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Difficulty
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF5EFEB)
                    ) {
                        Text(
                            text = AppLocalization.getDifficultyLabel(recipe.difficulty, languageMode),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF5A4535),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Servings
                    Text(
                        text = AppLocalization.getServingsLabel(recipe.servings, languageMode),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF786250),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}



@Composable
fun QuickAssignCategoryDialog(
    recipe: RecipeEntity,
    categories: List<String>,
    languageMode: LanguageMode,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customCategory by remember { mutableStateOf("") }
    val allOptions = (listOf("Wife's Recipes", "Daughter's Recipes") + categories).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Assign Category / Person",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Assign '${recipe.getDisplayTitle(languageMode)}' to:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5A4535), fontWeight = FontWeight.Medium)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allOptions.forEach { cat ->
                        val isAssigned = recipe.category.equals(cat, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAssign(cat) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAssigned) Color(0xFFFFEDD5) else Color(0xFFF9F6F0),
                            border = BorderStroke(1.dp, if (isAssigned) TerracottaPrimary else Color(0xFFE5DDD3))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAssigned) Color(0xFF431407) else Color(0xFF18120C)
                                )
                                if (isAssigned) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        placeholder = { Text("Or enter new name...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            focusedLabelColor = TerracottaPrimary
                        )
                    )
                    Button(
                        onClick = {
                            if (customCategory.isNotBlank()) {
                                onAssign(customCategory.trim())
                            }
                        },
                        enabled = customCategory.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Assign", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

