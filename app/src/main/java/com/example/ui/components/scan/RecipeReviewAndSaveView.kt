package com.example.ui.components.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.ui.components.booklet.ZoomableImageLightboxDialog
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

@Composable
fun RecipeReviewAndSaveView(
    initialDraft: RecipeEntity,
    onSave: (RecipeEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleEnglish by remember { mutableStateOf(initialDraft.titleEnglish.ifBlank { initialDraft.title }) }
    var titleGerman by remember { mutableStateOf(initialDraft.titleGerman) }
    var category by remember { mutableStateOf(initialDraft.category) }
    var servings by remember { mutableStateOf(initialDraft.servings) }
    var prepTime by remember { mutableStateOf(initialDraft.prepTimeMinutes.toString()) }
    var cookTime by remember { mutableStateOf(initialDraft.cookTimeMinutes.toString()) }
    var notes by remember { mutableStateOf(initialDraft.notes) }
    var ingredients by remember { mutableStateOf(initialDraft.ingredients) }
    var steps by remember { mutableStateOf(initialDraft.steps) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "✨ AI Extracted & Translated Recipe",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        fontFamily = FontFamily.Serif
                    )
                )
                Text(
                    text = "English translation is primary. Review fields before saving to your book.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF431407), fontWeight = FontWeight.Medium)
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFF18120C))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF8C7B6B))

        // Low-resolution Reference Image Card Preview
        if (!initialDraft.imageUri.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFullImageDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4EB)),
                border = BorderStroke(1.5.dp, Color(0xFF8C7B6B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = File(initialDraft.imageUri),
                        contentDescription = "Original recipe photo reference",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD1C7B7), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📸 Original Photo Stored",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ZoomIn,
                                contentDescription = "Inspect",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reference image saved for verification. Tap to inspect original card.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF431407),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // English Title (Primary)
        OutlinedTextField(
            value = titleEnglish,
            onValueChange = { titleEnglish = it },
            label = { Text("Recipe Title", color = Color(0xFF431407), fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF18120C),
                unfocusedTextColor = Color(0xFF18120C),
                focusedBorderColor = TerracottaPrimary,
                unfocusedBorderColor = Color(0xFF8C7B6B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category & Times
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF18120C),
                    unfocusedTextColor = Color(0xFF18120C),
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = Color(0xFF8C7B6B),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it },
                label = { Text("Servings", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF18120C),
                    unfocusedTextColor = Color(0xFF18120C),
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = Color(0xFF8C7B6B),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = prepTime,
                onValueChange = { prepTime = it },
                label = { Text("Prep (min)", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF18120C),
                    unfocusedTextColor = Color(0xFF18120C),
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = Color(0xFF8C7B6B),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            OutlinedTextField(
                value = cookTime,
                onValueChange = { cookTime = it },
                label = { Text("Cook (min)", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF18120C),
                    unfocusedTextColor = Color(0xFF18120C),
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = Color(0xFF8C7B6B),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        val totalMinutes = (prepTime.toIntOrNull() ?: 0) + (cookTime.toIntOrNull() ?: 0)
        if (totalMinutes > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFFF9F5EC),
                border = BorderStroke(1.dp, Color(0xFF8C7B6B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏱️ Total Time (Prep + Cook):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                    )
                    Text(
                        text = "$totalMinutes mins",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients summary
        Text(
            text = "🥕 Ingredients (${ingredients.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF431407)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        ingredients.forEachIndexed { i, ing ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                color = Color(0xFFFAF6EE),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8C7B6B))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ing.amount} ${ing.unit} • ${ing.getDisplayName()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF18120C)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Steps summary
        Text(
            text = "📝 Preparation Steps (${steps.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF431407)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        steps.forEachIndexed { i, step ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = Color(0xFFFAF6EE),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8C7B6B))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Step ${step.stepNumber}:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = TerracottaPrimary)
                    )
                    Text(
                        text = step.getInstruction(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF18120C),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Family Secrets / Chef Notes", color = Color(0xFF431407)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF18120C),
                unfocusedTextColor = Color(0xFF18120C),
                focusedBorderColor = TerracottaPrimary,
                unfocusedBorderColor = Color(0xFF8C7B6B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val finalRecipe = initialDraft.copy(
                    title = titleEnglish.ifBlank { titleGerman },
                    titleGerman = titleGerman,
                    titleEnglish = titleEnglish,
                    category = category,
                    servings = servings,
                    prepTimeMinutes = prepTime.toIntOrNull() ?: 20,
                    cookTimeMinutes = cookTime.toIntOrNull() ?: 30,
                    notes = notes,
                    ingredients = ingredients,
                    steps = steps,
                    imageUri = initialDraft.imageUri
                )
                onSave(finalRecipe)
            },
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("save_scanned_recipe_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Recipe", fontWeight = FontWeight.Bold)
        }
    }

    if (showFullImageDialog && !initialDraft.imageUri.isNullOrBlank()) {
        ZoomableImageLightboxDialog(
            imageModel = File(initialDraft.imageUri),
            title = "📸 Captured Recipe Card",
            subtitle = "Pinch to zoom • Drag to explore scanned card details",
            onDismiss = { showFullImageDialog = false }
        )
    }
}

