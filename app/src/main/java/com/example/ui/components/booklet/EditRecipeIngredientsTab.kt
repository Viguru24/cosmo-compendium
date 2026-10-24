package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.RecipeIngredient
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

@Composable
internal fun EditRecipeIngredientsTab(
    ingredients: SnapshotStateList<RecipeIngredient>,
    editingIngIndex: Int?,
    onEditingIngIndexChange: (Int?) -> Unit,
    editIngAmount: String,
    onEditIngAmountChange: (String) -> Unit,
    editIngUnit: String,
    onEditIngUnitChange: (String) -> Unit,
    editIngName: String,
    onEditIngNameChange: (String) -> Unit,
    editIngGroup: String,
    onEditIngGroupChange: (String) -> Unit,
    editIngOptional: Boolean,
    onEditIngOptionalChange: (Boolean) -> Unit,
    newIngAmount: String,
    onNewIngAmountChange: (String) -> Unit,
    newIngUnit: String,
    onNewIngUnitChange: (String) -> Unit,
    newIngName: String,
    onNewIngNameChange: (String) -> Unit,
    newIngGroup: String,
    onNewIngGroupChange: (String) -> Unit,
    newIngOptional: Boolean,
    onNewIngOptionalChange: (Boolean) -> Unit,
    draggedIngIndex: Int?,
    onDraggedIngIndexChange: (Int?) -> Unit,
    dragIngOffsetY: Float,
    onDragIngOffsetYChange: (Float) -> Unit,
    quickUnits: List<String>,
    quickGroups: List<String>,
    editorTextFieldColors: TextFieldColors,
    onCommitActiveIngEdit: () -> Unit,
    onStartEditIngredient: (Int, RecipeIngredient) -> Unit
) {
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
                            onValueChange = onEditIngAmountChange,
                            label = { Text("Amount") },
                            colors = editorTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editIngUnit,
                            onValueChange = onEditIngUnitChange,
                            label = { Text("Unit") },
                            colors = editorTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = editIngName,
                        onValueChange = onEditIngNameChange,
                        label = { Text("Ingredient Name") },
                        colors = editorTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editIngGroup,
                        onValueChange = onEditIngGroupChange,
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
                            onCheckedChange = onEditIngOptionalChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SageGreen)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onEditingIngIndexChange(null) }) {
                            Text("Cancel", color = Color(0xFF786555))
                        }
                        Button(
                            onClick = onCommitActiveIngEdit,
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
                                            onDraggedIngIndexChange(index)
                                            onDragIngOffsetYChange(0f)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val newOffset = dragIngOffsetY + dragAmount.y
                                            onDragIngOffsetYChange(newOffset)
                                            val currentIdx = draggedIngIndex ?: return@detectDragGestures
                                            val itemHeightPx = 48.dp.toPx()
                                            if (newOffset > itemHeightPx && currentIdx < ingredients.size - 1) {
                                                val item = ingredients.removeAt(currentIdx)
                                                ingredients.add(currentIdx + 1, item)
                                                onDraggedIngIndexChange(currentIdx + 1)
                                                onDragIngOffsetYChange(newOffset - itemHeightPx)
                                            } else if (newOffset < -itemHeightPx && currentIdx > 0) {
                                                val item = ingredients.removeAt(currentIdx)
                                                ingredients.add(currentIdx - 1, item)
                                                onDraggedIngIndexChange(currentIdx - 1)
                                                onDragIngOffsetYChange(newOffset + itemHeightPx)
                                            }
                                        },
                                        onDragEnd = {
                                            onDraggedIngIndexChange(null)
                                            onDragIngOffsetYChange(0f)
                                        },
                                        onDragCancel = {
                                            onDraggedIngIndexChange(null)
                                            onDragIngOffsetYChange(0f)
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
                            onClick = { onStartEditIngredient(index, ing) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                if (editingIngIndex == index) onEditingIngIndexChange(null)
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
                    onValueChange = onNewIngAmountChange,
                    placeholder = { Text("Qty (e.g. 250, 1/2)", fontSize = 12.sp) },
                    colors = editorTextFieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = newIngUnit,
                    onValueChange = onNewIngUnitChange,
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
                        modifier = Modifier.clickable { onNewIngUnitChange(u) }
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
                onValueChange = onNewIngNameChange,
                placeholder = { Text("Ingredient name (e.g. All-purpose flour)", fontSize = 12.sp) },
                colors = editorTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newIngGroup,
                onValueChange = onNewIngGroupChange,
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
                        modifier = Modifier.clickable { onNewIngGroupChange(g) }
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
                        onNewIngNameChange("")
                        onNewIngAmountChange("")
                        onNewIngUnitChange("g")
                        onNewIngGroupChange("")
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
