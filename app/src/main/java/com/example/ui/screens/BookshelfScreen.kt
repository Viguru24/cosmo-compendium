package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import com.example.ui.components.RealisticLeatherBackground
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
import com.example.ui.components.EditRecipeDialog
import com.example.ui.components.ScanRecipeBottomSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShareRecipeCardDialog
import com.example.ui.components.ShoppingListDialog
import com.example.ui.components.SmartConverterBottomSheet
import android.widget.Toast
import com.example.ui.theme.CreamBackgroundLight
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
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
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isRecipeEditorOpen by viewModel.isRecipeEditorOpen.collectAsStateWithLifecycle()
    val editingRecipeDraft by viewModel.editingRecipeDraft.collectAsStateWithLifecycle()
    val isShoppingListOpen by viewModel.isShoppingListOpen.collectAsStateWithLifecycle()
    val uncheckedShoppingCount by viewModel.uncheckedShoppingCount.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val navigateToRecipe by viewModel.navigateToRecipeEvent.collectAsStateWithLifecycle()

    var showScanSheet by remember { mutableStateOf(false) }
    var recipePendingDelete by remember { mutableStateOf<RecipeEntity?>(null) }
    var recipePendingShare by remember { mutableStateOf<RecipeEntity?>(null) }

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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📖", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppLocalization.getAppTitle(languageMode),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Shopping List button with badge
                    IconButton(
                        onClick = { viewModel.openShoppingList() },
                        modifier = Modifier.testTag("open_shopping_list_button")
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Add Recipe Manual button
                    IconButton(
                        onClick = {
                            viewModel.openNewRecipeEditor()
                        },
                        modifier = Modifier.testTag("add_manual_recipe_button")
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = "New Recipe",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Settings Gear Button
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFFDF9)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showScanSheet = true },
                containerColor = TerracottaPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .testTag("scan_recipe_fab")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppLocalization.getScanButtonLabel(languageMode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
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
            // Search Bar & Filter Section
            Surface(
                color = Color(0xFFFFFDF9),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE8DFD5))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Instant Fuzzy Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("recipe_search_input"),
                        placeholder = {
                            Text(
                                AppLocalization.getSearchPlaceholder(languageMode),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF5A4D41), fontSize = 13.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp), tint = Color(0xFF18120C))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF18120C),
                            unfocusedTextColor = Color(0xFF18120C),
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = Color(0xFF8C7B6B),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = onlyFavorites,
                                onClick = { viewModel.onlyFavorites.value = !onlyFavorites },
                                label = {
                                    Text(
                                        AppLocalization.getFavoritesLabel(languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = if (onlyFavorites) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (onlyFavorites) Color(0xFF9F1239) else Color(0xFF5A4D41),
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFE4E6),
                                    selectedLabelColor = Color(0xFF9F1239),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF18120C)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = onlyFavorites,
                                    borderColor = if (onlyFavorites) Color(0xFF9F1239) else Color(0xFF8C7B6B),
                                    borderWidth = 1.5.dp
                                )
                            )
                        }

                        items(rawCategories) { cat ->
                            val isSelected = selectedCategory == cat && !onlyFavorites
                            val displayLabel = AppLocalization.getCategoryLabel(cat, languageMode)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.onlyFavorites.value = false
                                    viewModel.selectedCategory.value = cat
                                },
                                label = {
                                    Text(
                                        displayLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFEDD5),
                                    selectedLabelColor = Color(0xFF431407),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF18120C)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) TerracottaPrimary else Color(0xFF8C7B6B),
                                    borderWidth = 1.5.dp
                                )
                            )
                        }
                    }
                }
            }

            // Recipe Bookshelf Grid
            if (recipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📜", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Recipes Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No recipes currently showing. You can load the starter recipe collection or restore a backup file.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF382D24),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.restoreStarterRecipes(replaceExisting = false) },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load Starter Recipes", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.openBackupDialog() },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF8C7B6B))
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Backup", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val columns = if (maxWidth < 600.dp) {
                        GridCells.Fixed(2)
                    } else {
                        GridCells.Adaptive(minSize = 160.dp)
                    }
                    LazyVerticalGrid(
                        columns = columns,
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(recipes, key = { it.id }) { recipe ->
                            RecipeBookCard(
                                recipe = recipe,
                                languageMode = languageMode,
                                onClick = { onRecipeClick(recipe) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                                onEditClick = { viewModel.openRecipeEditor(recipe) },
                                onShareClick = { recipePendingShare = recipe },
                                onDeleteClick = { recipePendingDelete = recipe }
                            )
                        }
                    }
                }
            }
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
                onOpenBackup = {
                    viewModel.closeSettings()
                    viewModel.openBackupDialog()
                },
                onOpenSmartConverter = {
                    viewModel.closeSettings()
                    viewModel.openSmartConverter("baking_soda", "2", "g")
                },
                onDeleteAllRecipes = {
                    viewModel.deleteAllRecipes()
                },
                onDismiss = { viewModel.closeSettings() }
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
                onResolveDuplicateUpdate = { prompt ->
                    viewModel.resolveDuplicateUpdate(prompt)
                    showScanSheet = false
                    Toast.makeText(context, "Updated existing '${prompt.existingRecipe.title}' with new scan details", Toast.LENGTH_SHORT).show()
                },
                onResolveDuplicateSaveCopy = { prompt ->
                    viewModel.resolveDuplicateSaveAsCopy(prompt)
                    showScanSheet = false
                    Toast.makeText(context, "Saved as new recipe variation", Toast.LENGTH_SHORT).show()
                },
                onDismissDuplicate = {
                    viewModel.dismissDuplicatePrompt()
                    showScanSheet = false
                },
                onClearError = { viewModel.scanErrorMessage.value = null },
                onScan = { bitmaps, text, imageUri ->
                    viewModel.scanRecipe(bitmaps, text, imageUri)
                },
                onSaveDraft = { recipe ->
                    viewModel.saveDraftRecipe(recipe)
                    showScanSheet = false
                },
                onDismiss = {
                    showScanSheet = false
                    viewModel.scannedDraftRecipe.value = null
                    viewModel.scanErrorMessage.value = null
                    viewModel.dismissDuplicatePrompt()
                }
            )
        }

        // Manual Recipe Editor Dialog
        if (isRecipeEditorOpen && editingRecipeDraft != null) {
            EditRecipeDialog(
                initialRecipe = editingRecipeDraft!!,
                categories = categories,
                onSave = { updated ->
                    viewModel.saveEditedRecipe(updated)
                },
                onDelete = { toDelete ->
                    viewModel.deleteRecipe(toDelete)
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
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasPhoto = !recipe.imageUri.isNullOrBlank()

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
            .height(if (hasPhoto) 250.dp else 185.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x333D2615), ambientColor = Color(0x1A3D2615))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("recipe_book_card_${recipe.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5DDD3))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (hasPhoto) {
                // Framed Dish Photo Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .background(Color(0xFFEFE8DE))
                ) {
                    AsyncImage(
                        model = File(recipe.imageUri!!),
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
                                        Color(0x77000000),
                                        Color.Transparent,
                                        Color(0x33000000)
                                    )
                                )
                            )
                    )

                    // Top Bar over photo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xF0FFFFFF),
                            border = BorderStroke(1.dp, catBorder),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = displayCat,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = catText,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xE6FFFFFF),
                                modifier = Modifier.size(28.dp)
                            ) {
                                IconButton(
                                    onClick = onToggleFavorite,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (recipe.isFavorite) Color(0xFFE11D48) else Color(0xFF4A3423),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Box {
                                Surface(
                                shape = CircleShape,
                                color = Color(0xE6FFFFFF),
                                modifier = Modifier.size(28.dp)
                            ) {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = Color(0xFF4A3423),
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
                                    text = { Text("Share Recipe") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onShareClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Recipe", color = MaterialTheme.colorScheme.error) },
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
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = catBg,
                                border = BorderStroke(1.dp, catBorder),
                                modifier = Modifier.weight(1f, fill = false)
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
                                            text = { Text("Share Recipe") },
                                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onShareClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete Recipe", color = MaterialTheme.colorScheme.error) },
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
                            text = recipe.difficulty,
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
                        text = recipe.servings,
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
