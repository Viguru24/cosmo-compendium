package com.example.ui.components.bookshelf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.getDisplayTitle

@Composable
fun QuickAssignCategoryDialog(
    recipe: RecipeEntity,
    categories: List<String>,
    languageMode: LanguageMode,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customCategory by remember { mutableStateOf("") }
    val allOptions = (listOf("Annette's Recipes", "Isabel's Recipes") + categories).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Assign Category / Person",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Assign '${recipe.getDisplayTitle(languageMode)}' to:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5A4535), fontWeight = FontWeight.Medium)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allOptions.forEach { cat ->
                        val isAssigned = recipe.category.equals(cat, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAssign(cat) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAssigned) Color(0xFFFFEDD5) else Color(0xFFF9F6F0),
                            border = BorderStroke(1.dp, if (isAssigned) TerracottaPrimary else Color(0xFFE5DDD3))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAssigned) Color(0xFF431407) else Color(0xFF18120C)
                                )
                                if (isAssigned) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        placeholder = { Text("Or enter new name...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            focusedLabelColor = TerracottaPrimary
                        )
                    )
                    Button(
                        onClick = {
                            if (customCategory.isNotBlank()) {
                                onAssign(customCategory.trim())
                            }
                        },
                        enabled = customCategory.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Assign", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
