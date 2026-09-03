package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@Composable
fun CategoryManagerDialog(
    categories: List<String>,
    allRecipes: List<RecipeEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onMoveUp: (Int) -> Unit = {},
    onMoveDown: (Int) -> Unit = {},
    onResetDefaults: () -> Unit = {},
    languageMode: LanguageMode = LanguageMode.ENGLISH
) {
    var newCategoryText by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var inlineEditText by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val quickSuggestions = listOf(
        "🍝 Pasta & Grains",
        "🥖 Breads & Baking",
        "🎄 Holiday & Festive",
        "🥩 BBQ & Grilling",
        "🫙 Sauces & Preserves",
        "🥨 Snacks & Quick Bites"
    )

    fun getCategoryIcon(cat: String): String {
        val lower = cat.lowercase()
        return when {
            lower.contains("baking") || lower.contains("dessert") || lower.contains("cake") -> "🍰"
            lower.contains("main") || lower.contains("dinner") || lower.contains("meat") -> "🍲"
            lower.contains("soup") || lower.contains("stew") -> "🥣"
            lower.contains("salad") || lower.contains("starter") -> "🥗"
            lower.contains("pasta") || lower.contains("noodle") -> "🍝"
            lower.contains("bread") || lower.contains("dough") -> "🥖"
            lower.contains("sauce") || lower.contains("dip") || lower.contains("condiment") -> "🫙"
            lower.contains("beverage") || lower.contains("drink") || lower.contains("cocktail") -> "🍹"
            lower.contains("snack") || lower.contains("appetizer") -> "🥨"
            lower.contains("bbq") || lower.contains("grill") -> "🥩"
            lower.contains("holiday") || lower.contains("festive") || lower.contains("tradition") -> "🎄"
            lower.contains("breakfast") || lower.contains("brunch") -> "🥞"
            else -> "🏷️"
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.5.dp, Color(0xFFDECDB8)),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.86f)
                .padding(vertical = 12.dp)
                .testTag("category_manager_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // ==========================================
                // HEADER
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "Rezept-Kategorien" else "Recipe Categories",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF2C1E14)
                            )
                            Text(
                                text = "${categories.size} ${if (languageMode == LanguageMode.GERMAN) "Kategorien • Hinzufügen, Bearbeiten & Löschen" else "Categories • Add, rename, or delete"}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF786555),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF7A6E65),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // ADD NEW CATEGORY INPUT CARD
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF9F5EE),
                    border = BorderStroke(1.dp, Color(0xFFE5DDD3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.GERMAN) "Neue Kategorie erstellen" else "Create New Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3728)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCategoryText,
                                onValueChange = { newCategoryText = it },
                                placeholder = {
                                    Text(
                                        text = if (languageMode == LanguageMode.GERMAN) "Kategoriename (z.B. Pasta, BBQ)..." else "Category name (e.g. Pasta, BBQ)...",
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF9E8E81)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TerracottaPrimary,
                                    unfocusedBorderColor = Color(0xFFD6C7B2),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF2C1E14),
                                    unfocusedTextColor = Color(0xFF2C1E14)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_category_input")
                            )

                            Button(
                                onClick = {
                                    if (newCategoryText.isNotBlank()) {
                                        val nameToAdd = newCategoryText.trim()
                                        onAddCategory(nameToAdd)
                                        newCategoryText = ""
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(120)
                                            if (categories.isNotEmpty()) {
                                                listState.animateScrollToItem(categories.size - 1)
                                            }
                                        }
                                    }
                                },
                                enabled = newCategoryText.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TerracottaPrimary,
                                    disabledContainerColor = Color(0xFFD6C7B2)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier.testTag("add_category_confirm_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.GERMAN) "Hinzufügen" else "Add",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        // Quick Suggestion Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(top = 2.dp),
                            modifier = Modifier.heightIn(max = 32.dp)
                        ) {
                            items(quickSuggestions) { suggestion ->
                                val cleanName = suggestion.substringAfter(" ").trim()
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                                    modifier = Modifier.clickable { newCategoryText = cleanName }
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF5A4535),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE5DDD3), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // CATEGORIES LIST (ADD / EDIT / DELETE)
                // ==========================================
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(categories, key = { _, cat -> cat }) { index, category ->
                        val count = allRecipes.count { it.category.equals(category, ignoreCase = true) }
                        val isEditingThis = editingCategory == category
                        val iconStr = getCategoryIcon(category)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEditingThis) Color(0xFFFFF9F2) else Color.White,
                            border = BorderStroke(if (isEditingThis) 1.5.dp else 1.dp, if (isEditingThis) TerracottaPrimary else Color(0xFFE8DFD5)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isEditingThis) {
                                // Inline Edit Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(iconStr, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                                    OutlinedTextField(
                                        value = inlineEditText,
                                        onValueChange = { inlineEditText = it },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = TerracottaPrimary,
                                            unfocusedBorderColor = Color(0xFFD6C7B2),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF2C1E14),
                                            unfocusedTextColor = Color(0xFF2C1E14)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("inline_edit_category_input")
                                    )
                                    IconButton(
                                        onClick = {
                                            if (inlineEditText.isNotBlank() && !inlineEditText.equals(category, ignoreCase = true)) {
                                                onRenameCategory(category, inlineEditText.trim())
                                            }
                                            editingCategory = null
                                        },
                                        modifier = Modifier.size(32.dp).testTag("save_rename_category_button")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save", tint = SageGreen)
                                    }
                                    IconButton(
                                        onClick = { editingCategory = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFF9E8E81))
                                    }
                                }
                            } else {
                                // Standard Category Row (compact & clean)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Emoji + Name + Count pill
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = iconStr, fontSize = 16.sp)
                                        Text(
                                            text = category,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2C1E14)
                                        )
                                        if (count > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF3EDE4)
                                            ) {
                                                Text(
                                                    text = "$count",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF7A6E65),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Edit + Delete
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingCategory = category
                                                inlineEditText = category
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("edit_category_${category.lowercase().replace(" ", "_")}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename",
                                                tint = Color(0xFF786555),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { categoryToDelete = category },
                                            enabled = categories.size > 1,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("delete_category_${category.lowercase().replace(" ", "_")}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = if (categories.size > 1) Color(0xFFDC2626) else Color(0xFFD6C7B2),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // BOTTOM FOOTER (RESET & DONE)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showResetConfirm = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF8C7A6B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.GERMAN) "Standard wiederherstellen" else "Reset to Defaults",
                            fontSize = 12.sp,
                            color = Color(0xFF8C7A6B),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("close_category_manager_button")
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.GERMAN) "Fertig" else "Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // DELETE CONFIRMATION DIALOG
    // ==========================================
    categoryToDelete?.let { catName ->
        val recipeCount = allRecipes.count { it.category.equals(catName, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.GERMAN) "Kategorie löschen?" else "Delete Category?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF2C1E14)
                )
            },
            text = {
                Text(
                    text = if (recipeCount > 0) {
                        if (languageMode == LanguageMode.GERMAN) {
                            "Möchten Sie die Kategorie '$catName' wirklich löschen? Ihre $recipeCount Rezepte bleiben erhalten und werden sicher nach 'Family Classics' verschoben."
                        } else {
                            "Are you sure you want to delete '$catName'? Its $recipeCount recipe(s) will stay safe and automatically move to 'Family Classics'."
                        }
                    } else {
                        if (languageMode == LanguageMode.GERMAN) {
                            "Möchten Sie die Kategorie '$catName' wirklich löschen?"
                        } else {
                            "Are you sure you want to delete the '$catName' category?"
                        }
                    },
                    fontSize = 13.5.sp,
                    color = Color(0xFF5A4535)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(catName)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.GERMAN) "Löschen" else "Delete",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { categoryToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (languageMode == LanguageMode.GERMAN) "Abbrechen" else "Cancel")
                }
            }
        )
    }

    // ==========================================
    // RESET CONFIRMATION DIALOG
    // ==========================================
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.GERMAN) "Kategorien zurücksetzen?" else "Reset Default Categories?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF2C1E14)
                )
            },
            text = {
                Text(
                    text = if (languageMode == LanguageMode.GERMAN) {
                        "Dadurch werden die Standard-Kategorien (Baking & Desserts, Main Dishes, Soups & Stews, etc.) wiederhergestellt."
                    } else {
                        "This will restore the standard category list (Baking & Desserts, Main Dishes, Soups & Stews, etc.)."
                    },
                    fontSize = 13.5.sp,
                    color = Color(0xFF5A4535)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.GERMAN) "Zurücksetzen" else "Reset",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetConfirm = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (languageMode == LanguageMode.GERMAN) "Abbrechen" else "Cancel")
                }
            }
        )
    }
}

