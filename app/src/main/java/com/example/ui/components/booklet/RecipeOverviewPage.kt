package com.example.ui.components.booklet

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.ParchmentPageEdge
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import java.io.File

/**
 * Page 1: Recipe Overview & Table of Contents.
 * Displays key recipe statistics (prep, cook, total, portions), category badge, notes,
 * original scanned recipe card photo preview with full inspection dialog, and quick-jump links to all pages.
 */
@Composable
fun RecipeLoreTableOfContentsPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    onJumpToPage: (Int) -> Unit,
    onEditDetails: (() -> Unit)? = null,
    onRotateOriginalPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showFullReferencePhotoDialog by remember { mutableStateOf(false) }
    val originalCardPath = remember(recipe.originalCardPhotoUri, recipe.imageUri, recipe.updatedAt) {
        recipe.originalCardPhotoUri?.takeIf { it.isNotBlank() && File(it).exists() }
            ?: (if (recipe.imageUri != null && !recipe.imageUri.contains("recipe_cover_") && File(recipe.imageUri).exists()) recipe.imageUri else null)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(10.dp, RoundedCornerShape(12.dp))
            .border(1.dp, ParchmentPageEdge, RoundedCornerShape(12.dp)),
        color = ParchmentPage
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page 1 • Recipe Overview",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF6B5B4E),
                        fontWeight = FontWeight.Bold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFEDD5),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Text(
                        text = recipe.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = Color(0xFFD6C7B2)
            )

            // Primary Recipe Stats Grid (Cook Time, Portions, Difficulty)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.2.dp, Color(0xFFE2D5C3))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recipe Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF431407),
                                fontFamily = FontFamily.Serif
                            )
                        )
                        if (onEditDetails != null) {
                            OutlinedButton(
                                onClick = onEditDetails,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 4-Column Key Stats Row (Prep, Cook, Total, Portions)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEDE4D6), RoundedCornerShape(8.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // PREP TIME
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "VORBEREITUNG" else if (languageMode == LanguageMode.FRENCH) "PRÉP" else "PREP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${recipe.prepTimeMinutes}m",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1917)
                                )
                            )
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "Vorbereitung" else if (languageMode == LanguageMode.FRENCH) "Préparation" else "Prep Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // COOK TIME
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "KOCHZEIT" else if (languageMode == LanguageMode.FRENCH) "CUISSON" else "COOK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${recipe.cookTimeMinutes}m",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1917)
                                )
                            )
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "Koch-/Backzeit" else if (languageMode == LanguageMode.FRENCH) "Cuisson" else "Cook Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // TOTAL TIME
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "GESAMT" else if (languageMode == LanguageMode.FRENCH) "TOTAL" else "TOTAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${recipe.totalTimeMinutes}m",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            )
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "Gesamtzeit" else if (languageMode == LanguageMode.FRENCH) "Temps Total" else "Total Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // PORTIONS / SERVINGS
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "PORTIONEN" else if (languageMode == LanguageMode.FRENCH) "PORTIONS" else "PORTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppLocalization.getServingsLabel(recipe.servings, languageMode),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1917)
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = recipe.difficulty,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    // Recipe Notes (if notes exist)
                    val displayNotes = recipe.notes.ifBlank { recipe.originStory }.trim()
                    if (displayNotes.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Notes:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5A4535)
                                )
                            )
                            Text(
                                text = displayNotes,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF44403C),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            // Reference Scanned Card Photo (if available)
            if (!originalCardPath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFullReferencePhotoDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2D5C3))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = File(originalCardPath),
                            contentDescription = "Original recipe photo reference",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFD1C7B7), RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Original Recipe Card Photo",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5A4535)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ZoomIn,
                                    contentDescription = "Inspect",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Tap to inspect & rotate scanned photo.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Table of Contents / Quick Jump Index
            Text(
                text = "TABLE OF CONTENTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B5B4E),
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Jump 1: Ingredients
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJumpToPage(2) }
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF7F2E8),
                border = BorderStroke(1.dp, Color(0xFFE2D5C3))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Page 2: Ingredients (${recipe.ingredients.size} items)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3B2E24)
                        )
                    )
                    Text("➔", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Jump 2: Steps
            recipe.steps.forEachIndexed { idx, step ->
                val stepPage = 3 + idx
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onJumpToPage(stepPage) }
                        .padding(vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFAF7F0),
                    border = BorderStroke(1.dp, Color(0xFFEDE6DC))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Step ${idx + 1}: ${step.getInstruction().take(36)}...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF292524)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text("p.$stepPage ➔", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Jump 3: Notes & Journal
            val journalPage = 3 + recipe.steps.size
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJumpToPage(journalPage) }
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF3ECE0),
                border = BorderStroke(1.dp, Color(0xFFDFD6C8))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Page $journalPage: Recipe Notes & Kitchen Journal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3828)
                        )
                    )
                    Text("➔", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showFullReferencePhotoDialog && !originalCardPath.isNullOrBlank()) {
        ZoomableImageLightboxDialog(
            imageModel = File(originalCardPath),
            title = "Original Recipe Card Photo",
            subtitle = "Pinch to zoom handwriting & details • Drag to explore",
            onDismiss = { showFullReferencePhotoDialog = false },
            onRotate = onRotateOriginalPhoto
        )
    }
}
