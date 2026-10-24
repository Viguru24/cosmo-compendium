package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeStep
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

@Composable
internal fun EditRecipeStepsTab(
    steps: SnapshotStateList<RecipeStep>,
    editingStepIndex: Int?,
    onEditingStepIndexChange: (Int?) -> Unit,
    editStepEn: String,
    onEditStepEnChange: (String) -> Unit,
    editStepTimer: String,
    onEditStepTimerChange: (String) -> Unit,
    editStepTip: String,
    onEditStepTipChange: (String) -> Unit,
    newStepEn: String,
    onNewStepEnChange: (String) -> Unit,
    newStepTimer: String,
    onNewStepTimerChange: (String) -> Unit,
    newStepTip: String,
    onNewStepTipChange: (String) -> Unit,
    draggedStepIndex: Int?,
    onDraggedStepIndexChange: (Int?) -> Unit,
    dragStepOffsetY: Float,
    onDragStepOffsetYChange: (Float) -> Unit,
    editorTextFieldColors: TextFieldColors,
    onCommitActiveStepEdit: () -> Unit,
    onStartEditStep: (Int, RecipeStep) -> Unit
) {
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
                        onValueChange = onEditStepEnChange,
                        label = { Text("Step Instruction") },
                        colors = editorTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editStepTimer,
                            onValueChange = onEditStepTimerChange,
                            label = { Text("Timer (min)") },
                            colors = editorTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editStepTip,
                            onValueChange = onEditStepTipChange,
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
                        TextButton(onClick = { onEditingStepIndexChange(null) }) {
                            Text("Cancel", color = Color(0xFF786555))
                        }
                        Button(
                            onClick = onCommitActiveStepEdit,
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
                                            onDraggedStepIndexChange(index)
                                            onDragStepOffsetYChange(0f)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val newOffset = dragStepOffsetY + dragAmount.y
                                            onDragStepOffsetYChange(newOffset)
                                            val currentIdx = draggedStepIndex ?: return@detectDragGestures
                                            val itemHeightPx = 54.dp.toPx()
                                            if (newOffset > itemHeightPx && currentIdx < steps.size - 1) {
                                                val item = steps.removeAt(currentIdx)
                                                steps.add(currentIdx + 1, item)
                                                onDraggedStepIndexChange(currentIdx + 1)
                                                onDragStepOffsetYChange(newOffset - itemHeightPx)
                                            } else if (newOffset < -itemHeightPx && currentIdx > 0) {
                                                val item = steps.removeAt(currentIdx)
                                                steps.add(currentIdx - 1, item)
                                                onDraggedStepIndexChange(currentIdx - 1)
                                                onDragStepOffsetYChange(newOffset + itemHeightPx)
                                            }
                                        },
                                        onDragEnd = {
                                            onDraggedStepIndexChange(null)
                                            onDragStepOffsetYChange(0f)
                                        },
                                        onDragCancel = {
                                            onDraggedStepIndexChange(null)
                                            onDragStepOffsetYChange(0f)
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
                            onClick = { onStartEditStep(index, step) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                if (editingStepIndex == index) onEditingStepIndexChange(null)
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
                onValueChange = onNewStepEnChange,
                placeholder = { Text("Step instruction (e.g. Whisk eggs and sugar until pale)", fontSize = 12.sp) },
                colors = editorTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = newStepTimer,
                    onValueChange = onNewStepTimerChange,
                    placeholder = { Text("Timer (min)", fontSize = 12.sp) },
                    colors = editorTextFieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = newStepTip,
                    onValueChange = onNewStepTipChange,
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
                        onNewStepEnChange("")
                        onNewStepTimerChange("0")
                        onNewStepTipChange("")
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
