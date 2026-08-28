package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@Composable
fun SettingsDialog(
    currentLanguage: LanguageMode,
    onLanguageChange: (LanguageMode) -> Unit,
    currentUnitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    soundEffectsEnabled: Boolean,
    onSoundEffectsChange: (Boolean) -> Unit,
    keepScreenAwake: Boolean,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    autoWeeklyBackupEnabled: Boolean = true,
    onToggleAutoWeeklyBackup: (Boolean) -> Unit = {},
    categories: List<String> = emptyList(),
    onAddCategory: (String) -> Unit = {},
    onRenameCategory: (String, String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit = {},
    onDeleteAllRecipes: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenSmartConverter: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleIngredient = RecipeIngredient("Flour", "250", "g", nameEnglish = "All-Purpose Flour")
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var editingCategoryName by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var newCategoryInput by remember { mutableStateOf("") }
    var showAddCategoryField by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete All Recipes?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all recipes from your cookbook library? All recipes will be removed immediately. (You can restore your recipes anytime from automatic weekly backups or reload starter recipes in Backup & Restore).",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF451A03), lineHeight = 20.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteAllRecipes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_all_recipes_button")
                ) {
                    Text("Yes, Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = Color(0xFF57534E))
                }
            },
            containerColor = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(16.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("settings_dialog"),
        shape = RoundedCornerShape(20.dp),
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "App Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TerracottaPrimary
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF18120C))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Measuring Styles (Cups, Metric, UK Imperial, Baker's)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Measuring Style & Conversions",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF431407)
                            )
                        )
                    }

                    Text(
                        text = "Convert recipes automatically between US Cups, Metric weights, or UK Imperial.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF431407), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    )

                    UnitSystem.values().forEach { system ->
                        val isSelected = currentUnitSystem == system
                        val previewValue = sampleIngredient.getConvertedAmount(system)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                            border = BorderStroke(if (isSelected) 2.dp else 1.5.dp, if (isSelected) Color(0xFF1D4ED8) else Color(0xFF8C7B6B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUnitSystemChange(system) }
                                .testTag("unit_system_${system.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = system.icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = system.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF18120C)
                                            )
                                        )
                                        Text(
                                            text = system.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF334155),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF93C5FD) else Color(0xFFCBD5E1))
                                        ) {
                                            Text(
                                                text = "Example (250g Flour): $previewValue",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF1D4ED8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (onOpenSmartConverter != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSmartConverter() }
                                .testTag("open_smart_converter_settings_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Open Smart Spoon & Knife-Tip Converter",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF78350F)
                                            )
                                        )
                                        Text(
                                            text = "Convert grams to knife-tips, pinches & spoons instantly",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF92400E),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF8C7B6B))

                // Section 2: Manage Recipe Categories
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.5.dp, Color(0xFFFDBA74)),
                    modifier = Modifier.fillMaxWidth().testTag("settings_categories_section")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Recipe Categories",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF9A3412)
                                        )
                                    )
                                    Text(
                                        text = "Customize, rename, or add custom categories",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFC2410C),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showAddCategoryField = !showAddCategoryField },
                                modifier = Modifier.size(32.dp).testTag("settings_add_category_button")
                            ) {
                                Icon(
                                    if (showAddCategoryField) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = "Add Category",
                                    tint = TerracottaPrimary
                                )
                            }
                        }

                        // Add category input field
                        if (showAddCategoryField) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newCategoryInput,
                                    onValueChange = { newCategoryInput = it },
                                    placeholder = { Text("New category name...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp).testTag("new_category_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerracottaPrimary,
                                        unfocusedBorderColor = Color(0xFFFDBA74),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (newCategoryInput.isNotBlank()) {
                                            onAddCategory(newCategoryInput.trim())
                                            newCategoryInput = ""
                                            showAddCategoryField = false
                                        }
                                    },
                                    enabled = newCategoryInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("save_new_category_button")
                                ) {
                                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Category List Items
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                val isEditingThis = editingCategoryName == cat
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isEditingThis) {
                                        Row(
                                            modifier = Modifier.padding(6.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = renameInput,
                                                onValueChange = { renameInput = it },
                                                modifier = Modifier.weight(1f).height(46.dp).testTag("rename_category_input"),
                                                shape = RoundedCornerShape(6.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = TerracottaPrimary,
                                                    unfocusedBorderColor = Color(0xFFFDBA74)
                                                ),
                                                singleLine = true
                                            )
                                            IconButton(
                                                onClick = {
                                                    if (renameInput.isNotBlank()) {
                                                        onRenameCategory(cat, renameInput.trim())
                                                    }
                                                    editingCategoryName = null
                                                },
                                                modifier = Modifier.size(32.dp).testTag("confirm_rename_category_button")
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Save", tint = SageGreen)
                                            }
                                            IconButton(
                                                onClick = { editingCategoryName = null },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF431407)
                                                )
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        editingCategoryName = cat
                                                        renameInput = cat
                                                    },
                                                    modifier = Modifier.size(28.dp).testTag("edit_category_${cat.lowercase().replace(" ", "_")}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Rename Category",
                                                        tint = TerracottaPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                if (categories.size > 1) {
                                                    IconButton(
                                                        onClick = { onDeleteCategory(cat) },
                                                        modifier = Modifier.size(28.dp).testTag("delete_category_${cat.lowercase().replace(" ", "_")}")
                                                    ) {
                                                        Icon(
                                                            Icons.Default.DeleteOutline,
                                                            contentDescription = "Delete Category",
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(16.dp)
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

                HorizontalDivider(color = Color(0xFF8C7B6B))

                // Section 3: Automatic Weekly Backup
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Automatic Weekly Backup",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    )
                                    Text(
                                        text = "Regular automated local preservation",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFB45309),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Switch(
                                checked = autoWeeklyBackupEnabled,
                                onCheckedChange = onToggleAutoWeeklyBackup,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFD97706)
                                ),
                                modifier = Modifier.testTag("toggle_auto_weekly_backup")
                            )
                        }

                        Text(
                            text = "Backs up your cookbook library automatically once a week to protect against accidental loss.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF78350F),
                                fontSize = 11.5.sp
                            )
                        )

                        Button(
                            onClick = onOpenBackup,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_backup_from_settings_button")
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Backup & Restore Tool", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF8C7B6B))

                // Section 3: App Audio & Cook Options
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF431407), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sound Effects & Page Turns",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF18120C))
                                )
                                Text(
                                    text = "Tactile page flip sounds and timer chimes",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                        Switch(
                            checked = soundEffectsEnabled,
                            onCheckedChange = onSoundEffectsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TerracottaPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFF431407), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Keep Screen Awake in Cook Mode",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF18120C))
                                )
                                Text(
                                    text = "Prevents display timeout while cooking",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                        Switch(
                            checked = keepScreenAwake,
                            onCheckedChange = onKeepScreenAwakeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SageGreen
                            )
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF8C7B6B))

                // Section 4: Recipe Library Management / Danger Zone (Delete All Button)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.5.dp, Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recipe Library Management",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            )
                        }

                        Text(
                            text = "Need to start fresh? One button to delete all recipes currently stored in your cookbook.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF7F1D1D),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )

                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_all_recipes_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete All Recipes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("close_settings_button")
            ) {
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

