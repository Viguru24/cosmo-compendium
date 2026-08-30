package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ShoppingItemEntity
import com.example.ui.theme.SageGreen
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("shopping_list_dialog"),
            color = Color(0xFFFDFBF7)
        ) {
            Scaffold(
                containerColor = Color(0xFFFDFBF7),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = TerracottaPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Market Grocery List",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF382314)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val statusSubtitle = if (items.isEmpty()) {
                                        "Your basket is empty"
                                    } else if (uncheckedCount == 0) {
                                        "All ${items.size} items collected! ✓"
                                    } else {
                                        "$uncheckedCount remaining of ${items.size} items"
                                    }
                                    Text(
                                        text = statusSubtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (uncheckedCount == 0 && items.isNotEmpty()) SageGreen else Color(0xFF786250),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_shopping_list_top")) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close",
                                    tint = TerracottaPrimary
                                )
                            }
                        },
                        actions = {
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
                                        text = { Text("Clear Entire List", color = Color(0xFFDC2626)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                                        onClick = {
                                            showMenu = false
                                            onClearAll()
                                        },
                                        enabled = items.isNotEmpty()
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFF9F5EE)
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        color = Color(0xFFF9F5EE),
                        border = BorderStroke(1.dp, Color(0xFFEADBCE)),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (completedCount > 0) {
                                TextButton(
                                    onClick = onClearCompleted,
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF8C532E))
                                ) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear $completedCount Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Text(
                                    text = if (items.isNotEmpty()) "${items.size} items" else "Empty",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8C7B6B))
                                )
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                                modifier = Modifier.testTag("done_shopping_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Done Shopping", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Quick Add Item Card (Spacious 2-Row Design)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF3ECE1),
                        border = BorderStroke(1.2.dp, Color(0xFFDFD2C0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Add Item to Basket",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )

                            // Row 1: Item Name input
                            OutlinedTextField(
                                value = newItemName,
                                onValueChange = { newItemName = it },
                                placeholder = {
                                    Text(
                                        "e.g. Vanilla Beans, Milk, Sourdough Bread...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF8C7E72)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_shopping_item_name"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF18120C),
                                    unfocusedTextColor = Color(0xFF18120C),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = TerracottaPrimary,
                                    unfocusedBorderColor = Color(0xFFD6C5AD)
                                )
                            )

                            // Row 2: Qty, Unit, and Add Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newItemQty,
                                    onValueChange = { newItemQty = it },
                                    placeholder = { Text("Qty", fontSize = 12.sp, color = Color(0xFF8C7E72)) },
                                    modifier = Modifier
                                        .width(72.dp)
                                        .testTag("new_shopping_item_qty"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF18120C),
                                        unfocusedTextColor = Color(0xFF18120C),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = TerracottaPrimary,
                                        unfocusedBorderColor = Color(0xFFD6C5AD)
                                    )
                                )

                                OutlinedTextField(
                                    value = newItemUnit,
                                    onValueChange = { newItemUnit = it },
                                    placeholder = { Text("Unit", fontSize = 12.sp, color = Color(0xFF8C7E72)) },
                                    modifier = Modifier
                                        .width(82.dp)
                                        .testTag("new_shopping_item_unit"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF18120C),
                                        unfocusedTextColor = Color(0xFF18120C),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = TerracottaPrimary,
                                        unfocusedBorderColor = Color(0xFFD6C5AD)
                                    )
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        if (newItemName.isNotBlank()) {
                                            onAddItem(newItemName.trim(), newItemQty.trim(), newItemUnit.trim(), selectedCategory)
                                            newItemName = ""
                                            newItemQty = ""
                                            newItemUnit = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    enabled = newItemName.isNotBlank(),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    modifier = Modifier.testTag("add_shopping_item_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }
                            }

                            // Category Selector Pills
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { cat ->
                                    val isSelected = selectedCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategory = cat },
                                        label = {
                                            Text(
                                                text = cat,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TerracottaPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFEAE1D3),
                                            labelColor = Color(0xFF451A03)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) TerracottaPrimary else Color(0xFFD6C8B8),
                                            selectedBorderColor = TerracottaPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Items List Content
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
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEDE4D6),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.ShoppingBag,
                                            contentDescription = null,
                                            tint = Color(0xFF9E8A78),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Your Shopping List is Empty",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF451A03),
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add ingredients from any recipe card with 1 tap, or enter items above to build your grocery list.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF786555),
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val groupedItems = items.groupBy { it.category }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groupedItems.forEach { (catName, categoryItems) ->
                                item(key = "header_$catName") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 2.dp),
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
                                        Text(
                                            text = "${categoryItems.count { !it.isChecked }}/${categoryItems.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF8C7B6B),
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(start = 6.dp)
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
