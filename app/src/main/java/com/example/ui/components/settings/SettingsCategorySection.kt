package com.example.ui.components.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

@Composable
fun SettingsCategorySection(
    categories: List<String>,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    cardBackground: Color = Color(0xFFFDFBF7),
    cardBorder: Color = Color(0xFFE8DFD5),
    headerDark: Color = Color(0xFF2C1E14),
    textMuted: Color = Color(0xFF786555)
) {
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    var editingCategoryName by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var newCategoryInput by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth().testTag("settings_categories_section")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCategoriesExpanded = !isCategoriesExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recipe Categories",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = headerDark
                            )
                        )
                        Text(
                            text = if (isCategoriesExpanded) "${categories.size} categories • Tap to collapse" else "${categories.size} categories • Tap to pull down & manage",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCategoriesExpanded) TerracottaPrimary else textMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isCategoriesExpanded) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { isCategoriesExpanded = !isCategoriesExpanded },
                    modifier = Modifier.size(32.dp).testTag("settings_toggle_categories_button")
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = if (isCategoriesExpanded) "Collapse" else "Expand",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(28.dp).rotate(if (isCategoriesExpanded) 180f else 0f)
                    )
                }
            }

            if (isCategoriesExpanded) {
                HorizontalDivider(color = cardBorder.copy(alpha = 0.8f))

                // Add category input field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        placeholder = { Text("New category name...", fontSize = 12.sp, color = textMuted) },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("new_category_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF18120C),
                            unfocusedTextColor = Color(0xFF18120C),
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = cardBorder,
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
                            }
                        },
                        enabled = newCategoryInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_new_category_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Category List Items
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        val isEditingThis = editingCategoryName == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, cardBorder),
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
                                            focusedTextColor = Color(0xFF18120C),
                                            unfocusedTextColor = Color(0xFF18120C),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = TerracottaPrimary,
                                            unfocusedBorderColor = cardBorder
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
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = headerDark
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
    }
}
