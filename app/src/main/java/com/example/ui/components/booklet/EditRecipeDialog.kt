package com.example.ui.components.booklet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.ImageUtils
import java.io.File

/**
 * Full recipe editor and creator dialog with multi-tab support:
 * 1. Basics & Cover Photo (Camera, Gallery, AI Studio, Category, Servings, Times)
 * 2. Ingredients (Interactive reordering, inline editing, quick measurement pills)
 * 3. Step Instructions (Drag-and-drop ordering, timers, tips)
 * 4. Heritage Lore & Notes (Cooking tips, origin story)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeDialog(
    initialRecipe: RecipeEntity,
    initialTab: Int = 0,
    categories: List<String> = emptyList(),
    onSave: (RecipeEntity) -> Unit,
    onDelete: ((RecipeEntity) -> Unit)? = null,
    onOpenPromptStudio: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isCompact = screenWidth < 380

    // Basic Details
    var title by remember { mutableStateOf(initialRecipe.title) }
    var titleEnglish by remember { mutableStateOf(initialRecipe.titleEnglish.ifBlank { initialRecipe.title }) }
    var titleGerman by remember { mutableStateOf(initialRecipe.titleGerman) }
    var category by remember { mutableStateOf(initialRecipe.category.ifBlank { "Family Classics" }) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var servings by remember { mutableStateOf(initialRecipe.servings) }
    var prepTime by remember { mutableStateOf(initialRecipe.prepTimeMinutes.toString()) }
    var cookTime by remember { mutableStateOf(initialRecipe.cookTimeMinutes.toString()) }
    var difficulty by remember { mutableStateOf(initialRecipe.difficulty.ifBlank { "Medium" }) }
    var rating by remember { mutableStateOf(initialRecipe.rating) }
    var isFavorite by remember { mutableStateOf(initialRecipe.isFavorite) }
    var originStory by remember { mutableStateOf(initialRecipe.originStory) }
    var notes by remember { mutableStateOf(initialRecipe.notes) }
    var notesGerman by remember { mutableStateOf(initialRecipe.notesGerman) }
    var sourceLanguage by remember { mutableStateOf(initialRecipe.sourceLanguage) }

    // Cover Photo State & Launchers
    var imageUri by remember { mutableStateOf(initialRecipe.imageUri) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageUtils.saveImageFromUri(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = tempCameraUri
        if (success && uri != null) {
            val savedPath = ImageUtils.saveImageFromUri(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    // Ingredients & Steps
    val ingredients = remember { mutableStateListOf(*initialRecipe.ingredients.toTypedArray()) }
    val steps = remember { mutableStateListOf(*initialRecipe.steps.toTypedArray()) }

    // Active Editor Tab (0 = Basics & Photo, 1 = Ingredients, 2 = Steps, 3 = Lore & Notes)
    var selectedTab by remember { mutableStateOf(initialTab) }

    // Drag and drop reorder state
    var draggedIngIndex by remember { mutableStateOf<Int?>(null) }
    var dragIngOffsetY by remember { mutableStateOf(0f) }

    var draggedStepIndex by remember { mutableStateOf<Int?>(null) }
    var dragStepOffsetY by remember { mutableStateOf(0f) }

    // Index of ingredient being edited inline (-1 = none)
    var editingIngIndex by remember { mutableStateOf<Int?>(null) }
    var editIngAmount by remember { mutableStateOf("") }
    var editIngUnit by remember { mutableStateOf("") }
    var editIngName by remember { mutableStateOf("") }
    var editIngGroup by remember { mutableStateOf("") }
    var editIngOptional by remember { mutableStateOf(false) }

    // New Ingredient form fields
    var newIngAmount by remember { mutableStateOf("") }
    var newIngUnit by remember { mutableStateOf("g") }
    var newIngName by remember { mutableStateOf("") }
    var newIngGroup by remember { mutableStateOf("") }
    var newIngOptional by remember { mutableStateOf(false) }

    // Index of step being edited inline (-1 = none)
    var editingStepIndex by remember { mutableStateOf<Int?>(null) }
    var editStepEn by remember { mutableStateOf("") }
    var editStepTimer by remember { mutableStateOf("0") }
    var editStepTip by remember { mutableStateOf("") }

    // New Step form fields
    var newStepEn by remember { mutableStateOf("") }
    var newStepTimer by remember { mutableStateOf("0") }
    var newStepTip by remember { mutableStateOf("") }

    fun commitActiveIngEdit() {
        val idx = editingIngIndex ?: return
        if (idx in ingredients.indices) {
            val rawName = editIngName.ifBlank { "Ingredient" }
            val cleanName = RecipeIngredient.cleanIngredientName(rawName).ifBlank { rawName }
            ingredients[idx] = RecipeIngredient(
                name = cleanName,
                amount = editIngAmount.trim(),
                unit = editIngUnit.trim(),
                nameEnglish = cleanName,
                nameGerman = ingredients[idx].nameGerman?.let { RecipeIngredient.cleanIngredientName(it) } ?: cleanName,
                isOptional = editIngOptional,
                group = editIngGroup.trim().ifBlank { null }
            )
        }
        editingIngIndex = null
    }

    fun commitActiveStepEdit() {
        val idx = editingStepIndex ?: return
        if (idx in steps.indices) {
            steps[idx] = steps[idx].copy(
                instructionEnglish = editStepEn.trim().ifBlank { steps[idx].instructionEnglish },
                instructionGerman = steps[idx].instructionGerman.ifBlank { editStepEn.trim() },
                timerMinutes = editStepTimer.toIntOrNull() ?: 0,
                tip = editStepTip.trim().ifBlank { null }
            )
        }
        editingStepIndex = null
    }

    var showDeleteConfirmInEditor by remember { mutableStateOf(false) }

    val presetCategories = listOf("Baking & Cakes", "Main Dishes", "Soups & Stews", "Desserts", "Breakfast & Brunch", "Family Classics")
    val allAvailableCategories = (categories + presetCategories).filter { it.isNotBlank() }.distinct()
    val quickUnits = listOf("g", "ml", "cup", "tbsp", "tsp", "oz", "pkg", "pinch", "kg", "l")
    val quickGroups = listOf("Dough", "Filling", "Topping", "Sauce", "Garnish")

    // Theme color palette for editor
    val editorTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF2C1E14),
        unfocusedTextColor = Color(0xFF2C1E14),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFFFFDF9),
        focusedBorderColor = TerracottaPrimary,
        unfocusedBorderColor = Color(0xFFD6C7B2),
        focusedLabelColor = TerracottaPrimary,
        unfocusedLabelColor = Color(0xFF786555),
        cursorColor = TerracottaPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFFFFDF9),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (initialRecipe.id == 0L) Icons.Default.Add else Icons.Default.Edit,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (initialRecipe.id == 0L) "Add Recipe" else "Edit Recipe",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF431407),
                            fontFamily = FontFamily.Serif,
                            fontSize = if (isCompact) 17.sp else 19.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) TerracottaPrimary else Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (initialRecipe.id != 0L && onDelete != null) {
                        IconButton(
                            onClick = { showDeleteConfirmInEditor = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp)
            ) {
                // Modern Section Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF7F2E8),
                    contentColor = TerracottaPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Basics",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Ingredients (${ingredients.size})",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Steps (${steps.size})",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 2) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                "Notes",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 3) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        // ================= TAB 0: BASICS & COVER PHOTO =================
                        0 -> {
                            // Cover Photo Management Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFBF8F2),
                                border = BorderStroke(1.dp, Color(0xFFE8DED1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Dish Cover Photo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF431407)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Photo Thumbnail or Placeholder
                                        if (!imageUri.isNullOrBlank() && (imageUri!!.startsWith("http") || File(imageUri!!).exists() || imageUri!!.startsWith("content://"))) {
                                            val modelData = if (imageUri!!.startsWith("/") || imageUri!!.startsWith("file://")) {
                                                File(imageUri!!.removePrefix("file://"))
                                            } else {
                                                imageUri!!
                                            }
                                            AsyncImage(
                                                model = modelData,
                                                contentDescription = "Cover Photo",
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFD6C7B2), RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFEDE4D6),
                                                border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                                                modifier = Modifier.size(72.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF8C7A6B), modifier = Modifier.size(24.dp))
                                                    Text("No Photo", fontSize = 9.sp, color = Color(0xFF8C7A6B), fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Photo Action Buttons
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = { galleryLauncher.launch("image/*") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, TerracottaPrimary),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        val uri = ImageUtils.createTempCameraUri(context)
                                                        tempCameraUri = uri
                                                        cameraLauncher.launch(uri)
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFD97706)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (onOpenPromptStudio != null) {
                                                    OutlinedButton(
                                                        onClick = onOpenPromptStudio,
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, SageGreen),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("AI Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (!imageUri.isNullOrBlank()) {
                                                    OutlinedButton(
                                                        onClick = { imageUri = null },
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Remove", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Recipe Title Field
                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    titleEnglish = it
                                },
                                label = { Text("Recipe Title *") },
                                placeholder = { Text("e.g. Traditional Apple Pie") },
                                colors = editorTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Category Selector with Pull-Down Menu ONLY (No redundant chips!)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = { category = it },
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select Category",
                                                tint = TerracottaPrimary
                                            )
                                        }
                                    },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFFFFFDF9)).border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(10.dp))
                                ) {
                                    allAvailableCategories.forEach { cat ->
                                        val isSelected = category.equals(cat, ignoreCase = true)
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = cat,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) TerracottaPrimary else Color(0xFF2C1E14)
                                                )
                                            },
                                            trailingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Servings, Prep Time, Cook Time
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = servings,
                                    onValueChange = { servings = it },
                                    label = { Text("Servings") },
                                    placeholder = { Text("e.g. 4") },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = prepTime,
                                    onValueChange = { prepTime = it },
                                    label = { Text("Prep (min)") },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cookTime,
                                    onValueChange = { cookTime = it },
                                    label = { Text("Cook (min)") },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Total Time Live Calculation Banner
                            val pMin = prepTime.toIntOrNull() ?: 0
                            val cMin = cookTime.toIntOrNull() ?: 0
                            val totMin = pMin + cMin

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Total Time:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF78350F)))
                                    }
                                    Text(
                                        text = "$totMin min ($pMin prep + $cMin cook)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    )
                                }
                            }

                            // Difficulty Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Difficulty:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF5A4535))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Easy", "Medium", "Advanced").forEach { diff ->
                                        val isSel = difficulty.equals(diff, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSel) Color(0xFFFFEDD5) else Color(0xFFF3ECE0),
                                            border = BorderStroke(1.dp, if (isSel) TerracottaPrimary else Color(0xFFD6C7B2)),
                                            modifier = Modifier.clickable { difficulty = diff }
                                        ) {
                                            Text(
                                                text = diff,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) TerracottaPrimary else Color(0xFF6B5B4E),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Star Rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rating:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF5A4535))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = { rating = star },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Text(
                                                text = if (star <= rating) "★" else "☆",
                                                color = if (star <= rating) Color(0xFFD97706) else Color(0xFFD1C7B7),
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ================= TAB 1: INGREDIENTS =================
                        1 -> {
                            Text(
                                text = "Recipe Ingredients (${ingredients.size}):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                            )

                            // List of existing ingredients
                            ingredients.forEachIndexed { index, ing ->
                                if (editingIngIndex == index) {
                                    // Inline editor card for this ingredient
                                    Surface(
                                        color = Color(0xFFFFF8EE),
                                        border = BorderStroke(1.5.dp, TerracottaPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Edit Ingredient #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaPrimary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editIngAmount,
                                                    onValueChange = { editIngAmount = it },
                                                    label = { Text("Amount") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = editIngUnit,
                                                    onValueChange = { editIngUnit = it },
                                                    label = { Text("Unit") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            OutlinedTextField(
                                                value = editIngName,
                                                onValueChange = { editIngName = it },
                                                label = { Text("Ingredient Name") },
                                                colors = editorTextFieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = editIngGroup,
                                                onValueChange = { editIngGroup = it },
                                                label = { Text("Group (e.g. Dough, Filling)") },
                                                colors = editorTextFieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Optional ingredient?", fontSize = 12.sp, color = Color(0xFF6B5B4E))
                                                Switch(
                                                    checked = editIngOptional,
                                                    onCheckedChange = { editIngOptional = it },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SageGreen)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { editingIngIndex = null }) {
                                                    Text("Cancel", color = Color(0xFF786555))
                                                }
                                                Button(
                                                    onClick = { commitActiveIngEdit() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                                                ) {
                                                    Text("Apply Edit", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val isDragged = draggedIngIndex == index
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDragged) Color(0xFFFFF3E0) else Color(0xFFFBF8F2),
                                        border = BorderStroke(if (isDragged) 1.5.dp else 1.dp, if (isDragged) TerracottaPrimary else Color(0xFFE8DED1)),
                                        shadowElevation = if (isDragged) 6.dp else 0.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .zIndex(if (isDragged) 1f else 0f)
                                            .graphicsLayer {
                                                translationY = if (isDragged) dragIngOffsetY else 0f
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Drag & Drop Handle
                                                Box(
                                                    modifier = Modifier
                                                        .pointerInput(ingredients.size) {
                                                            detectDragGestures(
                                                                onDragStart = {
                                                                    draggedIngIndex = index
                                                                    dragIngOffsetY = 0f
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragIngOffsetY += dragAmount.y
                                                                    val currentIdx = draggedIngIndex ?: return@detectDragGestures
                                                                    val itemHeightPx = 48.dp.toPx()
                                                                    if (dragIngOffsetY > itemHeightPx && currentIdx < ingredients.size - 1) {
                                                                        val item = ingredients.removeAt(currentIdx)
                                                                        ingredients.add(currentIdx + 1, item)
                                                                        draggedIngIndex = currentIdx + 1
                                                                        dragIngOffsetY -= itemHeightPx
                                                                    } else if (dragIngOffsetY < -itemHeightPx && currentIdx > 0) {
                                                                        val item = ingredients.removeAt(currentIdx)
                                                                        ingredients.add(currentIdx - 1, item)
                                                                        draggedIngIndex = currentIdx - 1
                                                                        dragIngOffsetY += itemHeightPx
                                                                    }
                                                                },
                                                                onDragEnd = {
                                                                    draggedIngIndex = null
                                                                    dragIngOffsetY = 0f
                                                                },
                                                                onDragCancel = {
                                                                    draggedIngIndex = null
                                                                    dragIngOffsetY = 0f
                                                                }
                                                            )
                                                        }
                                                        .padding(end = 6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DragHandle,
                                                        contentDescription = "Drag to reorder",
                                                        tint = if (isDragged) TerracottaPrimary else Color(0xFFB0A395),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                // Non-wrapping Measurement Badge Pill
                                                if (ing.amount.isNotBlank() || ing.unit.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFFEDD5),
                                                        border = BorderStroke(1.dp, Color(0xFFFED7AA))
                                                    ) {
                                                        Text(
                                                            text = "${ing.amount} ${ing.unit}".trim(),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF9A3412),
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }

                                                val displayName = RecipeIngredient.cleanIngredientName(ing.nameEnglish ?: ing.name).ifBlank { ing.name }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = displayName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF2C1E14),
                                                            fontSize = 13.sp
                                                        )
                                                    )
                                                    if (!ing.group.isNullOrBlank()) {
                                                        Text(
                                                            text = ing.group,
                                                            fontSize = 10.sp,
                                                            color = SageGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    if (ing.isOptional) {
                                                        Text(
                                                            text = "(optional)",
                                                            fontSize = 10.sp,
                                                            fontStyle = FontStyle.Italic,
                                                            color = Color(0xFF8C7A6B)
                                                        )
                                                    }
                                                }
                                            }

                                            // Action icons (Edit, Delete)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        commitActiveIngEdit()
                                                        editingIngIndex = index
                                                        editIngAmount = ing.amount
                                                        editIngUnit = ing.unit
                                                        editIngName = ing.nameEnglish ?: ing.name
                                                        editIngGroup = ing.group ?: ""
                                                        editIngOptional = ing.isOptional
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (editingIngIndex == index) editingIngIndex = null
                                                        ingredients.removeAt(index)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE5DDD3))

                            // Add New Ingredient Section
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFBF8F2),
                                border = BorderStroke(1.dp, Color(0xFFE8DED1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Add New Ingredient", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaPrimary)

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = newIngAmount,
                                            onValueChange = { newIngAmount = it },
                                            placeholder = { Text("Qty (e.g. 250, 1/2)", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newIngUnit,
                                            onValueChange = { newIngUnit = it },
                                            placeholder = { Text("Unit", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Quick Unit Chips
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(quickUnits.size) { idx ->
                                            val u = quickUnits[idx]
                                            val isSel = newIngUnit == u
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSel) Color(0xFFFFEDD5) else Color.White,
                                                border = BorderStroke(1.dp, if (isSel) TerracottaPrimary else Color(0xFFD6C7B2)),
                                                modifier = Modifier.clickable { newIngUnit = u }
                                            ) {
                                                Text(
                                                    text = u,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) TerracottaPrimary else Color(0xFF6B5B4E),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newIngName,
                                        onValueChange = { newIngName = it },
                                        placeholder = { Text("Ingredient name (e.g. All-purpose flour)", fontSize = 12.sp) },
                                        colors = editorTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = newIngGroup,
                                        onValueChange = { newIngGroup = it },
                                        placeholder = { Text("Group (e.g. Dough, Filling, Sauce)", fontSize = 12.sp) },
                                        colors = editorTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Quick Group Chips
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(quickGroups.size) { idx ->
                                            val g = quickGroups[idx]
                                            val isSel = newIngGroup == g
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSel) Color(0xFFFFEDD5) else Color.White,
                                                border = BorderStroke(1.dp, if (isSel) TerracottaPrimary else Color(0xFFD6C7B2)),
                                                modifier = Modifier.clickable { newIngGroup = g }
                                            ) {
                                                Text(
                                                    text = g,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) TerracottaPrimary else Color(0xFF6B5B4E),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (newIngName.isNotBlank()) {
                                                ingredients.add(
                                                    RecipeIngredient(
                                                        name = newIngName,
                                                        amount = newIngAmount,
                                                        unit = newIngUnit,
                                                        nameEnglish = newIngName,
                                                        isOptional = newIngOptional,
                                                        group = newIngGroup.ifBlank { null }
                                                    )
                                                )
                                                newIngName = ""
                                                newIngAmount = ""
                                                newIngUnit = "g"
                                                newIngGroup = ""
                                            }
                                        },
                                        enabled = newIngName.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Ingredient", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ================= TAB 2: INSTRUCTIONS / STEPS =================
                        2 -> {
                            Text(
                                text = "Preparation Steps (${steps.size}):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                            )

                            // List of existing steps
                            steps.forEachIndexed { index, step ->
                                if (editingStepIndex == index) {
                                    Surface(
                                        color = Color(0xFFFFF8EE),
                                        border = BorderStroke(1.5.dp, TerracottaPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Edit Step #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaPrimary)
                                            OutlinedTextField(
                                                value = editStepEn,
                                                onValueChange = { editStepEn = it },
                                                label = { Text("Step Instruction") },
                                                colors = editorTextFieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editStepTimer,
                                                    onValueChange = { editStepTimer = it },
                                                    label = { Text("Timer (min)") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = editStepTip,
                                                    onValueChange = { editStepTip = it },
                                                    label = { Text("Chef's Tip") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(2f)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { editingStepIndex = null }) {
                                                    Text("Cancel", color = Color(0xFF786555))
                                                }
                                                Button(
                                                    onClick = { commitActiveStepEdit() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                                                ) {
                                                    Text("Apply Step Edit", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val isDragged = draggedStepIndex == index
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDragged) Color(0xFFFFF3E0) else Color(0xFFFBF8F2),
                                        border = BorderStroke(if (isDragged) 1.5.dp else 1.dp, if (isDragged) TerracottaPrimary else Color(0xFFE8DED1)),
                                        shadowElevation = if (isDragged) 6.dp else 0.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .zIndex(if (isDragged) 1f else 0f)
                                            .graphicsLayer {
                                                translationY = if (isDragged) dragStepOffsetY else 0f
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Drag & Drop Handle
                                                Box(
                                                    modifier = Modifier
                                                        .pointerInput(steps.size) {
                                                            detectDragGestures(
                                                                onDragStart = {
                                                                    draggedStepIndex = index
                                                                    dragStepOffsetY = 0f
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragStepOffsetY += dragAmount.y
                                                                    val currentIdx = draggedStepIndex ?: return@detectDragGestures
                                                                    val itemHeightPx = 54.dp.toPx()
                                                                    if (dragStepOffsetY > itemHeightPx && currentIdx < steps.size - 1) {
                                                                        val item = steps.removeAt(currentIdx)
                                                                        steps.add(currentIdx + 1, item)
                                                                        draggedStepIndex = currentIdx + 1
                                                                        dragStepOffsetY -= itemHeightPx
                                                                    } else if (dragStepOffsetY < -itemHeightPx && currentIdx > 0) {
                                                                        val item = steps.removeAt(currentIdx)
                                                                        steps.add(currentIdx - 1, item)
                                                                        draggedStepIndex = currentIdx - 1
                                                                        dragStepOffsetY += itemHeightPx
                                                                    }
                                                                },
                                                                onDragEnd = {
                                                                    draggedStepIndex = null
                                                                    dragStepOffsetY = 0f
                                                                },
                                                                onDragCancel = {
                                                                    draggedStepIndex = null
                                                                    dragStepOffsetY = 0f
                                                                }
                                                            )
                                                        }
                                                        .padding(end = 6.dp, top = 2.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DragHandle,
                                                        contentDescription = "Drag to reorder",
                                                        tint = if (isDragged) TerracottaPrimary else Color(0xFFB0A395),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                // Step number badge
                                                Surface(
                                                    shape = CircleShape,
                                                    color = TerracottaPrimary,
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = step.getInstruction(LanguageMode.ENGLISH),
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = Color(0xFF2C1E14),
                                                            fontSize = 12.5.sp,
                                                            lineHeight = 17.sp
                                                        )
                                                    )
                                                    if (step.timerMinutes > 0) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFFFEF3C7)
                                                        ) {
                                                            Text(
                                                                text = "⏱ ${step.timerMinutes} min",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF92400E),
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (!step.tip.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text("💡 ${step.tip}", fontSize = 10.5.sp, color = SageGreen, fontWeight = FontWeight.Medium)
                                                    }
                                                }
                                            }

                                            // Step action buttons (Edit, Delete)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        commitActiveStepEdit()
                                                        editingStepIndex = index
                                                        editStepEn = step.getInstruction(LanguageMode.ENGLISH)
                                                        editStepTimer = step.timerMinutes.toString()
                                                        editStepTip = step.tip ?: ""
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (editingStepIndex == index) editingStepIndex = null
                                                        steps.removeAt(index)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE5DDD3))

                            // Add New Step Section
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFBF8F2),
                                border = BorderStroke(1.dp, Color(0xFFE8DED1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Add Step #${steps.size + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaPrimary)

                                    OutlinedTextField(
                                        value = newStepEn,
                                        onValueChange = { newStepEn = it },
                                        placeholder = { Text("Step instruction (e.g. Whisk eggs and sugar until pale)", fontSize = 12.sp) },
                                        colors = editorTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = newStepTimer,
                                            onValueChange = { newStepTimer = it },
                                            placeholder = { Text("Timer (min)", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newStepTip,
                                            onValueChange = { newStepTip = it },
                                            placeholder = { Text("Chef's tip (optional)", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(2f)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (newStepEn.isNotBlank()) {
                                                steps.add(
                                                    RecipeStep(
                                                        stepNumber = steps.size + 1,
                                                        instructionEnglish = newStepEn,
                                                        timerMinutes = newStepTimer.toIntOrNull() ?: 0,
                                                        tip = newStepTip.ifBlank { null }
                                                    )
                                                )
                                                newStepEn = ""
                                                newStepTimer = "0"
                                                newStepTip = ""
                                            }
                                        },
                                        enabled = newStepEn.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Step", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ================= TAB 3: NOTES =================
                        3 -> {
                            OutlinedTextField(
                                value = originStory,
                                onValueChange = { originStory = it },
                                label = { Text("Recipe Description / Story") },
                                placeholder = { Text("e.g. Traditional family recipe from Oma, perfect for Sunday dinners...") },
                                colors = editorTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Chef Notes & Cooking Tips") },
                                placeholder = { Text("e.g. Bake on the middle rack at 180°C. Best served warm with vanilla custard...") },
                                colors = editorTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    commitActiveIngEdit()
                    commitActiveStepEdit()

                    val finalIngredients = ingredients.map { ing ->
                        val rawName = ing.nameEnglish ?: ing.name
                        val clean = RecipeIngredient.cleanIngredientName(rawName).ifBlank { rawName }
                        ing.copy(
                            name = clean,
                            nameEnglish = clean,
                            nameGerman = ing.nameGerman?.let { RecipeIngredient.cleanIngredientName(it) } ?: clean
                        )
                    }.toList()

                    val finalSteps = steps.mapIndexed { idx, s -> s.copy(stepNumber = idx + 1) }.toList()

                    val finalRecipe = initialRecipe.copy(
                        title = title.ifBlank { titleEnglish.ifBlank { "Family Recipe" } },
                        titleGerman = titleGerman.ifBlank { title },
                        titleEnglish = titleEnglish.ifBlank { title },
                        category = category.ifBlank { "Family Classics" },
                        servings = servings.ifBlank { "4 servings" },
                        prepTimeMinutes = prepTime.toIntOrNull() ?: 20,
                        cookTimeMinutes = cookTime.toIntOrNull() ?: 30,
                        difficulty = difficulty.ifBlank { "Medium" },
                        imageUri = imageUri,
                        rating = rating,
                        isFavorite = isFavorite,
                        originStory = originStory,
                        notes = notes,
                        notesGerman = notesGerman.ifBlank { notes },
                        sourceLanguage = sourceLanguage,
                        ingredients = finalIngredients,
                        steps = finalSteps
                    )
                    onSave(finalRecipe)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Recipe", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF786555))
            }
        }
    )

    if (showDeleteConfirmInEditor && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmInEditor = false },
            containerColor = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(14.dp),
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Recipe?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                )
            },
            text = {
                Text("Are you sure you want to delete '${initialRecipe.title}'? This action cannot be undone.", color = Color(0xFF5A4535))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmInEditor = false
                        onDelete(initialRecipe)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmInEditor = false }) {
                    Text("Cancel", color = Color(0xFF786555))
                }
            }
        )
    }
}
