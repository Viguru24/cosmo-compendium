package com.example.ui.components.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.io.File
import com.example.data.local.RecipeEntity
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.RecipeViewModel

@Composable
fun DuplicateRecipeComparisonView(
    prompt: com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData,
    onUpdate: () -> Unit,
    onSaveCopy: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existing = prompt.existingRecipe
    val scanned = prompt.scannedRecipe

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFFFDFBF7))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("duplicate_recipe_comparison_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning Icon Badge
        Surface(
            shape = CircleShape,
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            modifier = Modifier.size(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Duplicate Recipe Detected",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF431407),
                fontFamily = FontFamily.Serif
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You already have a recipe titled \"${existing.title}\" in your ${existing.category} collection.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF573C27),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Side-by-side / Comparison Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Existing in Library
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F2)),
                border = BorderStroke(1.5.dp, Color(0xFFD6C8B8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "📖", fontSize = 16.sp)
                            Text(
                                text = "Current Recipe in Cookbook",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            )
                        }
                        if (existing.isFavorite) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = "★ Favorite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = existing.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF292524),
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️ ${existing.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5848))
                        )
                        Text(
                            text = "⏱️ ${existing.cookTimeMinutes}m cook",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5848))
                        )
                        Text(
                            text = "🍳 Cooked ${existing.timesCooked}x",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5848))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingredients (${existing.ingredients.size}): " + existing.ingredients.take(4).joinToString(", ") { it.name } + if (existing.ingredients.size > 4) "..." else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF78716C),
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 2
                    )
                }
            }

            // Newly Scanned Candidate
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                border = BorderStroke(1.5.dp, Color(0xFFFDBA74)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "📸", fontSize = 16.sp)
                            Text(
                                text = "Newly Scanned Version",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEDD5),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA))
                        ) {
                            Text(
                                text = "New Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = scanned.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF431407),
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️ ${scanned.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E48))
                        )
                        Text(
                            text = "⏱️ ${scanned.cookTimeMinutes}m cook",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E48))
                        )
                        Text(
                            text = "📋 ${scanned.steps.size} steps",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E48))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingredients (${scanned.ingredients.size}): " + scanned.ingredients.take(4).joinToString(", ") { it.name } + if (scanned.ingredients.size > 4) "..." else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8C7355),
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Update Existing Recipe
            Button(
                onClick = onUpdate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("duplicate_update_existing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update Existing Recipe",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(
                text = "Updates ingredients, steps, and photos while preserving your favorites status and cooking history.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Option 2: Save as New Copy (Variation)
            OutlinedButton(
                onClick = onSaveCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("duplicate_save_copy_button"),
                border = BorderStroke(1.5.dp, SageGreen),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save as New Copy (Variation)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(
                text = "Keeps both recipes separately in your collection as \"${scanned.title} (Variation)\".",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Option 3: Discard Scanned Recipe
            TextButton(
                onClick = onDiscard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("duplicate_discard_button")
            ) {
                Text(
                    text = "Discard Scanned Recipe",
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DuplicateRecipeDialog(
    prompt: com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData,
    onUpdate: () -> Unit,
    onSaveCopy: () -> Unit,
    onDiscard: () -> Unit
) {
    Dialog(onDismissRequest = onDiscard) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFFDF8),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            DuplicateRecipeComparisonView(
                prompt = prompt,
                onUpdate = onUpdate,
                onSaveCopy = onSaveCopy,
                onDiscard = onDiscard
            )
        }
    }
}
