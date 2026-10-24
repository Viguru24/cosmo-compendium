package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.ui.theme.TerracottaPrimary

/**
 * 1-Tap Recipe Profile Assignment Dialog (from recipe card 3-dots menu)
 */
@Composable
fun AssignRecipeProfileDialog(
    recipe: RecipeEntity,
    profiles: List<String>,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newCustomProfile by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Move to Family Cookbook",
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary,
                    fontSize = 17.sp
                )
                Text(
                    text = "Assign \"${recipe.title}\" to:",
                    fontSize = 12.sp,
                    color = Color(0xFF6B5848),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                profiles.forEach { profileName ->
                    val isCurrent = recipe.profileName.equals(profileName, ignoreCase = true)
                    val emoji = getProfileEmoji(profileName)

                    Surface(
                        onClick = {
                            onAssign(profileName)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) Color(0xFFFFEDD5) else Color(0xFFFFFDF9),
                        border = BorderStroke(1.dp, if (isCurrent) TerracottaPrimary else Color(0xFFE8DFD5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$profileName's Cookbook",
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isCurrent) Color(0xFF431407) else Color(0xFF18120C)
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = "Current", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isAddingNew) {
                    TextButton(
                        onClick = { isAddingNew = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ New Member", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = newCustomProfile,
                            onValueChange = { newCustomProfile = it },
                            placeholder = { Text("Name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCustomProfile.isNotBlank()) {
                                    onAssign(newCustomProfile.trim())
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Text("Move")
                        }
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
