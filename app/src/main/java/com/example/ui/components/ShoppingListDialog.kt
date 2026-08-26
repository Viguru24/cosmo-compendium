package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ShoppingItemEntity
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDialog(
    items: List<ShoppingItemEntity>,
    onToggleItem: (ShoppingItemEntity) -> Unit,
    onAddItem: (name: String, amount: String, unit: String, category: String) -> Unit,
    onDeleteItem: (ShoppingItemEntity) -> Unit,
    onClearCompleted: () -> Unit,
    onClearAll: () -> Unit,
    onShareList: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") }
    var newItemUnit by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Fresh Produce") }
    var showMenu by remember { mutableStateOf(false) }

    val uncheckedCount = items.count { !it.isChecked }
    val completedCount = items.count { it.isChecked }

    val categories = listOf(
        "Fresh Produce",
        "Dairy & Refrigerated",
        "Meat & Seafood",
        "Bakery & Bread",
        "Baking & Spices",
        "Pantry & Staples",
        "General"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("shopping_list_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFAF7F0),
            border = BorderStroke(2.dp, Color(0xFFC89B6D)),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Market Grocery List",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = Color(0xFF451A03)
                                )
                            )
                            Text(
                                text = if (items.isEmpty()) "Your basket is empty" else "$uncheckedCount items remaining (${items.size} total)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (uncheckedCount == 0 && items.isNotEmpty()) Color(0xFF15803D) else Color(0xFF78350F)
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (items.isNotEmpty()) {
                            IconButton(
                                onClick = onShareList,
                                modifier = Modifier.testTag("share_shopping_list_button")
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share Grocery List",
                                    tint = TerracottaPrimary
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color(0xFF5A4535)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Clear Checked ($completedCount)") },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onClearCompleted()
                                    },
                                    enabled = completedCount > 0
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Entire List") },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        onClearAll()
                                    },
                                    enabled = items.isNotEmpty()
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF5A4535))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Add Item Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3EAD8),
                    border = BorderStroke(1.dp, Color(0xFFD6C5AD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = newItemName,
                                onValueChange = { newItemName = it },
                                placeholder = { Text("Add item (e.g. Vanilla Beans, Milk)...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("new_shopping_item_name"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = TerracottaPrimary,
                                    unfocusedBorderColor = Color(0xFFC89B6D)
                                )
                            )

                            OutlinedTextField(
                                value = newItemQty,
                                onValueChange = { newItemQty = it },
                                placeholder = { Text("Qty", fontSize = 12.sp) },
                                modifier = Modifier
                                    .width(60.dp)
                                    .testTag("new_shopping_item_qty"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = TerracottaPrimary,
                                    unfocusedBorderColor = Color(0xFFC89B6D)
                                )
                            )

                            OutlinedTextField(
                                value = newItemUnit,
                                onValueChange = { newItemUnit = it },
                                placeholder = { Text("Unit", fontSize = 12.sp) },
                                modifier = Modifier
                                    .width(65.dp)
                                    .testTag("new_shopping_item_unit"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = TerracottaPrimary,
                                    unfocusedBorderColor = Color(0xFFC89B6D)
                                )
                            )

                            Button(
                                onClick = {
                                    if (newItemName.isNotBlank()) {
                                        onAddItem(newItemName, newItemQty, newItemUnit, selectedCategory)
                                        newItemName = ""
                                        newItemQty = ""
                                        newItemUnit = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                enabled = newItemName.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("add_shopping_item_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        }

                        // Category selector chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) TerracottaPrimary else Color(0xFFE5DCD0),
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else Color(0xFF451A03),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Items list
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFFB09B88),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your Shopping List is Empty",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5A4535),
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add ingredients directly from any recipe card with 1 tap, or add custom groceries above.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF8C7B6B),
                                    fontSize = 12.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    val groupedItems = items.groupBy { it.category }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedItems.forEach { (catName, categoryItems) ->
                            item(key = "header_$catName") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (catName) {
                                        "Fresh Produce" -> "🥬"
                                        "Dairy & Refrigerated" -> "🥛"
                                        "Meat & Seafood" -> "🥩"
                                        "Bakery & Bread" -> "🥖"
                                        "Baking & Spices" -> "🧂"
                                        "Pantry & Staples" -> "🥫"
                                        else -> "📦"
                                    }
                                    Text(
                                        text = "$icon $catName",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF5A4535),
                                            fontFamily = FontFamily.Serif
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = Color(0xFFE2D6C5)
                                    )
                                }
                            }

                            items(categoryItems, key = { it.id }) { item ->
                                ShoppingItemRow(
                                    item = item,
                                    onToggle = { onToggleItem(item) },
                                    onDelete = { onDeleteItem(item) }
                                )
                            }
                        }
                    }
                }

                // Footer Bar
                HorizontalDivider(color = Color(0xFFE2D6C5), modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (completedCount > 0) {
                        TextButton(
                            onClick = onClearCompleted,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF78350F))
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear $completedCount Checked", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Done Shopping", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(if (item.isChecked) 0.55f else 1.0f, label = "alpha")

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (item.isChecked) Color(0xFFEDE7DC) else Color.White,
        border = BorderStroke(1.dp, if (item.isChecked) Color(0xFFE2D6C5) else Color(0xFFD6C5AD)),
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onToggle() }
            .testTag("shopping_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = TerracottaPrimary,
                    uncheckedColor = Color(0xFF8C7B6B)
                ),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.amount.isNotBlank() || item.unit.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.isChecked) Color(0xFFD6C5AD) else Color(0xFFF3EAD8),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "${item.amount} ${item.unit}".trim(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5A4535),
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (item.isChecked) Color(0xFF7A6B5D) else Color(0xFF2C221E),
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!item.recipeTitle.isNullOrBlank()) {
                    Text(
                        text = "From: ${item.recipeTitle}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF8C7B6B),
                            fontSize = 10.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete item",
                    tint = Color(0xFF9C8673),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
