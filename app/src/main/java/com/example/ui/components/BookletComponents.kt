package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.util.pdf.RecipePdfGenerator
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.ui.util.getDisplayTitle
import com.example.data.model.CoverTheme
import com.example.data.model.GermanCulinaryGlossary
import com.example.data.model.GlossaryItem
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.data.model.UnitSystem
import com.example.ui.theme.GermanFlagGold
import com.example.ui.theme.GermanFlagRed
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.ParchmentPageEdge
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

// ==========================================
// 1. HARDCOVER COVER PAGE (Page 0)
// ==========================================
@Composable
fun RecipeCoverPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    onOpenBook: () -> Unit,
    onOpenStory: () -> Unit,
    onEditRecipe: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = try {
        CoverTheme.valueOf(recipe.coverTheme)
    } catch (e: Exception) {
        CoverTheme.VINTAGE_LEATHER
    }

    val hasPhoto = !recipe.imageUri.isNullOrBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Book shadow & hardcover container
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxSize(0.96f)
                .shadow(24.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenBook() }
                .testTag("book_cover_surface"),
            color = Color(0xFF231F1D)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasPhoto) {
                    AsyncImage(
                        model = java.io.File(recipe.imageUri!!),
                        contentDescription = recipe.getDisplayTitle(),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // High-contrast gradient overlay for readable typography
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xB3000000),
                                        Color(0x55000000),
                                        Color(0xB3000000),
                                        Color(0xF20F0D0B)
                                    )
                                )
                            )
                    )
                } else {
                    RealisticLeatherBackground(
                        theme = theme,
                        isCard = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Inscription
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "RECIPE COLLECTION",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEADBCE)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Preserved Family Heritage & Kitchen Secrets",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFD4C3B2),
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Center Embossed Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = recipe.getDisplayTitle(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFFFFFDF9),
                                textAlign = TextAlign.Center,
                                fontSize = 28.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category & Difficulty badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33000000),
                            border = BorderStroke(1.dp, Color(0x55E5D4B8))
                        ) {
                            Text(
                                text = "${recipe.category} • ${recipe.difficulty}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFEADBCE),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Bottom info & turn page CTA
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFEADBCE),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFEADBCE),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFFEADBCE),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = recipe.servings,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFEADBCE),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onOpenBook,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEDE4D6),
                                contentColor = Color(0xFF382314)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, Color(0xFFD6C8B8)),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .testTag("open_recipe_button")
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF382314)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Recipe Book",
                                color = Color(0xFF382314),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap cover or swipe to turn pages",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xCCEADBCE),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}// ==========================================
// ==========================================
// 2. RECIPE OVERVIEW & TABLE OF CONTENTS (Page 1)
// ==========================================
@Composable
fun RecipeLoreTableOfContentsPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    onJumpToPage: (Int) -> Unit,
    onEditDetails: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showFullReferencePhotoDialog by remember { mutableStateOf(false) }

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
                                text = "PREP",
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
                                text = "Prep Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // COOK TIME
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "COOK",
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
                                text = "Cook Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // TOTAL TIME
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL",
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
                                text = "Total Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // PORTIONS / SERVINGS
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PORTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B5B4E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = recipe.servings,
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
            if (!recipe.imageUri.isNullOrBlank()) {
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
                            model = File(recipe.imageUri),
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
                                    text = "Original Recipe Photo",
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
                                text = "Tap to inspect scanned photo.",
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
                        "Page 2: Ingredients (${recipe.ingredients.size} items) & Scaler",
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

    if (showFullReferencePhotoDialog && !recipe.imageUri.isNullOrBlank()) {
        Dialog(onDismissRequest = { showFullReferencePhotoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1610),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Original Recipe Photo",
                            color = Color(0xFFEADBCE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { showFullReferencePhotoDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = File(recipe.imageUri),
                        contentDescription = "Original recipe photo reference",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. INGREDIENTS & PORTION SCALER (Page 2)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeIngredientsPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit = {},
    onOpenConverter: ((String?, String, String) -> Unit)? = null,
    servingMultiplier: Float,
    onSetMultiplier: (Float) -> Unit,
    onOpenGlossary: (String) -> Unit,
    checkedIngredients: Set<Int>,
    onToggleCheck: (Int) -> Unit,
    onNextPage: () -> Unit,
    onEditIngredients: (() -> Unit)? = null,
    onAddToShoppingList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page 2 • Ingredients",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF6B5B4E),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ingredients Checklist",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                    if (onEditIngredients != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onEditIngredients,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Ingredients",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = Color(0xFFD6C7B2)
            )

            // UNIT SYSTEM SELECTOR (Metric / US Cups / UK Imperial / Baker's)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
                border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Units of Measurement",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5A4535)
                            )
                        )
                        Text(
                            text = unitSystem.shortLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitSystem.values().forEach { system ->
                            val isSelected = unitSystem == system
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFFE5DCD0),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUnitSystemChange(system) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = when (system) {
                                            UnitSystem.METRIC_GRAMS -> "Metric"
                                            UnitSystem.CUPS_US -> "US (Cups)"
                                            UnitSystem.UK_IMPERIAL -> "British"
                                            UnitSystem.BAKERS_PRECISION -> "Baker's"
                                        },
                                        color = if (isSelected) Color.White else Color(0xFF451A03),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (onOpenConverter != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.2.dp, Color(0xFFFCD34D)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenConverter("baking_soda", "2", "g") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Smart Spoon & Knife-Tip Converter",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F),
                                        fontSize = 11.5.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(0.8.dp, Color(0xFFF59E0B))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Open",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFB45309),
                                                fontSize = 10.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color(0xFFB45309),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PORTION SCALER BAR (0.5x, 1x, 1.5x, 2x, 3x)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
                border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Portion Scaler",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5A4535)
                            )
                        )

                        Text(
                            text = "Multiplier: ${if (servingMultiplier % 1.0f == 0f) "${servingMultiplier.toInt()}x" else "${servingMultiplier}x"}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TerracottaPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f, 3.0f).forEach { scale ->
                            val isSelected = servingMultiplier == scale
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFFE5DCD0),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetMultiplier(scale) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${if (scale % 1f == 0f) scale.toInt() else scale}x",
                                        color = if (isSelected) Color.White else Color(0xFF451A03),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (onAddToShoppingList != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF7F0E4),
                    border = BorderStroke(1.2.dp, Color(0xFFD8BEA0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAddToShoppingList() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFEDD5),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Send to Shopping List",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF451A03),
                                    fontSize = 13.sp
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "Add all ${recipe.ingredients.size} items (${if (servingMultiplier % 1f == 0f) "${servingMultiplier.toInt()}x" else "${servingMultiplier}x"} scaled)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78350F),
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerracottaPrimary,
                            shadowElevation = 2.dp,
                            modifier = Modifier.clickable { onAddToShoppingList() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Add All",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (onEditIngredients != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F2E8), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2D6C5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients (${recipe.ingredients.size} items)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4535)
                        )
                    )
                    IconButton(
                        onClick = onEditIngredients,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Ingredients",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ingredients List by Group
            val groupedIngredients = recipe.ingredients.groupBy { it.group ?: "Main Ingredients" }

            var overallIndex = 0
            groupedIngredients.forEach { (groupName, items) ->
                if (groupName.isNotBlank() && groupedIngredients.size > 1) {
                    Text(
                        text = "• $groupName",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4535),
                            fontFamily = FontFamily.Serif
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items.forEach { ingredient ->
                    val index = overallIndex++
                    val isChecked = checkedIngredients.contains(index)
                    val isGermanIngredient = GermanCulinaryGlossary.findSubstitute(ingredient.name) != null ||
                            GermanCulinaryGlossary.findSubstitute(ingredient.nameGerman ?: "") != null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCheck(index) }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleCheck(index) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SageGreen,
                                uncheckedColor = Color(0xFF8C7A6B)
                            ),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Scaled Measurement Amount & Unit
                        val measurementText = ingredient.getConvertedAmount(unitSystem, servingMultiplier)
                        if (measurementText.isNotBlank()) {
                            Surface(
                                color = if (isChecked) Color(0xFFD8D2C5) else Color(0xFFEDE5D8),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable(enabled = onOpenConverter != null) {
                                    onOpenConverter?.invoke(
                                        ingredient.name,
                                        ingredient.amount,
                                        ingredient.unit
                                    )
                                }
                            ) {
                                Text(
                                    text = measurementText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChecked) Color(0xFF78716C) else Color(0xFF451A03)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Ingredient Name
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ingredient.getDisplayName(languageMode),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (isChecked) Color(0xFF8C857B) else Color(0xFF292524)
                                    )
                                )
                            }

                            if (ingredient.isOptional) {
                                Text(
                                    text = "(optional)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF8C7A6B)
                                    )
                                )
                            }
                        }

                        // German Substitution Glossary Button
                        if (isGermanIngredient) {
                            IconButton(
                                onClick = { onOpenGlossary(ingredient.nameGerman ?: ingredient.name) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "Substitution info",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Page Turn CTA
            Button(
                onClick = onNextPage,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Step 1 ➔", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 4. STEP-BY-STEP PAGE (Pages 3..N+2)
// ==========================================
@Composable
fun RecipeStepPage(
    step: RecipeStep,
    totalSteps: Int,
    languageMode: LanguageMode,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onNextPage: () -> Unit,
    onEditStep: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Step Page Number Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${step.stepNumber}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Step ${step.stepNumber} of $totalSteps",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEditStep != null) {
                            IconButton(
                                onClick = onEditStep,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Step",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Step Read Loud / TTS button
                        IconButton(
                            onClick = {
                                onSpeak(step.getInstruction(), false)
                            }
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Read Step Aloud",
                                tint = Color(0xFF6B5B4E)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = Color(0xFFD6C7B2)
                )

                // Step Instructions
                Text(
                    text = step.getInstruction(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF292524),
                        lineHeight = 26.sp,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Kitchen Tip Box
                if (!step.tip.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2D5C3))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chef's Tip: ${step.tip}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF5A4535),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }

                // Step Timer Button
                if (step.timerMinutes > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onStartTimer(step.timerMinutes) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TerracottaPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start ${step.timerMinutes} Min Timer",
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom completion checkbox
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFF5EFEB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleCompleted() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { onToggleCompleted() },
                            colors = CheckboxDefaults.colors(checkedColor = SageGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleted) "Step Completed" else "Mark Step as Done",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) Color(0xFF166534) else Color(0xFF451A03)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. COOK'S JOURNAL & FAMILY NOTES (Final Page)
// ==========================================
@Composable
fun RecipeCookJournalPage(
    recipe: RecipeEntity,
    onSaveJournal: (String, Int) -> Unit,
    onIncrementCooked: () -> Unit,
    onStartCookingMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notesText by remember(recipe.id) { mutableStateOf(recipe.notes) }
    var rating by remember(recipe.id) { mutableStateOf(recipe.rating) }
    var showSavedBadge by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Auto-save debounce when typing notes or selecting stars
    LaunchedEffect(notesText, rating) {
        if (notesText != recipe.notes || rating != recipe.rating) {
            delay(500)
            onSaveJournal(notesText, rating)
            showSavedBadge = true
            delay(1500)
            showSavedBadge = false
        }
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
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Final Page • Notes",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF6B5B4E),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showSavedBadge) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "✓ Saved",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Cook's Journal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
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

            // Family Star Rating
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2D5C3))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Family Star Rating",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4535)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (rating > 0) "$rating / 5 Stars" else "Tap to rate this recipe",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (rating > 0) TerracottaPrimary else Color(0xFF8C7A6B),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    rating = star
                                    onSaveJournal(notesText, star)
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) TerracottaPrimary else Color(0xFFD1C7B7),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Times Cooked Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECE4D7), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Times Cooked",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535))
                    )
                    Text(
                        text = "${recipe.timesCooked} meals prepared",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF3B2E24))
                    )
                }

                Button(
                    onClick = onIncrementCooked,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cooked Today", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personal Notes & secret adjustments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cook's Notes & Kitchen Secrets:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4535)
                    )
                )
                TextButton(
                    onClick = {
                        onSaveJournal(notesText, rating)
                        showSavedBadge = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Save Notes", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                singleLine = false,
                minLines = 5,
                maxLines = 12,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFF1E140C),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                ),
                placeholder = {
                    Text(
                        "Write your notes here: e.g. baked for 5 extra minutes, added a dash of nutmeg, special oven setting...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF786250),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1E140C),
                    unfocusedTextColor = Color(0xFF1E140C),
                    focusedPlaceholderColor = Color(0xFF786250),
                    unfocusedPlaceholderColor = Color(0xFF786250),
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = Color(0xFF8C7B6B),
                    focusedContainerColor = Color(0xFFFFFFFF),
                    unfocusedContainerColor = Color(0xFFFFFDF9),
                    cursorColor = TerracottaPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Cook Mode Button
            Button(
                onClick = onStartCookingMode,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Kitchen Cook Mode", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 6. VINTAGE HANDWRITTEN RECIPE CARD VIEW
// ==========================================
@Composable
fun VintageHandwrittenCardView(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFFC7B198), RoundedCornerShape(16.dp)),
        color = ParchmentPage
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "VINTAGE RECIPE CARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                    Text(
                        text = "Heirloom Recipe Collection",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF6B5B4E)
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFECE4D7),
                    border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "HEIRLOOM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4535),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = Color(0xFFD6C7B2)
            )

            // Title
            Text(
                text = recipe.getDisplayTitle(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF3B2E24),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Servings: ${recipe.servings} • Prep: ${recipe.prepTimeMinutes}m • Cook: ${recipe.cookTimeMinutes}m",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF6B5B4E)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients
            Text(
                text = "Ingredients:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4535)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            recipe.ingredients.forEach { ing ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("• ", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${ing.getConvertedAmount(unitSystem)} ${ing.getDisplayName()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF292524)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Method:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4535)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            recipe.steps.forEachIndexed { idx, step ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "${idx + 1}. ${step.getInstruction()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF292524),
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Return to Book View", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 7. CULINARY GLOSSARY BOTTOM SHEET
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryBottomSheet(
    item: GlossaryItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDF9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.germanName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TerracottaPrimary
                    )
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "English: ${item.englishName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B5B4E)
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2D6C5))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF3B2E24), lineHeight = 20.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Best Substitutes:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535))
            )
            Spacer(modifier = Modifier.height(4.dp))
            item.substitutes.forEach { sub ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("• ", color = SageGreen, fontWeight = FontWeight.Bold)
                    Text(text = sub, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF292524)))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2D5C3))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.culinaryTip,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic, color = Color(0xFF5A4535))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 8. SHARE RECIPE CARD & PDF DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRecipeCardDialog(
    recipe: RecipeEntity,
    servingMultiplier: Float = 1.0f,
    unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
    onAddToShoppingList: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0: PDF Card, 1: Plain Text
    var includeLore by remember { mutableStateOf(true) }
    var includeTips by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    var currentMultiplier by remember { mutableStateOf(servingMultiplier) }
    var currentUnitSystem by remember { mutableStateOf(unitSystem) }

    // Launcher for saving PDF to storage
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        if (targetUri != null) {
            try {
                val pdfFile = RecipePdfGenerator.generateRecipePdf(
                    context = context,
                    recipe = recipe,
                    multiplier = currentMultiplier,
                    unitSystem = currentUnitSystem,
                    includeLore = includeLore,
                    includeTips = includeTips,
                    includeNotes = includeNotes
                )
                val success = RecipePdfGenerator.savePdfToUri(context, targetUri, pdfFile)
                if (success) {
                    Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val shareableText = remember(recipe, includeLore, includeTips, includeNotes, currentMultiplier) {
        buildString {
            appendLine(recipe.getDisplayTitle())
            val servStr = if (currentMultiplier != 1.0f) "${recipe.servings} (${currentMultiplier}x scaled)" else recipe.servings
            appendLine("Category: ${recipe.category} | Servings: $servStr | Total Time: ${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min")
            appendLine("\nINGREDIENTS:")
            var currentGroup: String? = null
            recipe.ingredients.forEach { ing ->
                val grp = ing.getLocalizedGroup()
                if (!grp.isNullOrBlank() && grp != currentGroup) {
                    currentGroup = grp
                    appendLine("\n[$grp]")
                }
                val name = ing.getDisplayName()
                val opt = if (ing.isOptional) " (optional)" else ""
                val scaledAmt = try {
                    val rawVal = ing.amount.trim().toDoubleOrNull()
                    if (rawVal != null && currentMultiplier != 1.0f) {
                        val scaled = rawVal * currentMultiplier
                        if (scaled % 1.0 == 0.0) scaled.toInt().toString() else String.format(java.util.Locale.US, "%.1f", scaled)
                    } else ing.amount
                } catch (e: Exception) {
                    ing.amount
                }
                appendLine("• $scaledAmt ${ing.unit} $name$opt".trim())
            }
            appendLine("\nINSTRUCTIONS:")
            recipe.steps.forEachIndexed { i, s ->
                val instr = s.getInstruction()
                val timerStr = if (s.timerMinutes > 0) " (${s.timerMinutes} min)" else ""
                appendLine("${i + 1}. $instr$timerStr")
                if (includeTips && !s.tip.isNullOrBlank()) {
                    val tip = s.getLocalizedTip() ?: s.tip
                    appendLine("   Tip: $tip")
                }
            }
            if (includeLore && recipe.originStory.isNotBlank()) {
                appendLine("\nHERITAGE LORE:\n${recipe.originStory}")
            }
            if (includeNotes && recipe.notes.isNotBlank()) {
                appendLine("\nFAMILY NOTES:\n${recipe.notes}")
            }
            appendLine("\nShared from Vintage Heirloom Cookbook")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Recipe",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        fontFamily = FontFamily.Serif
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab Selection: PDF vs Plain Text
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF3EAD8),
                    contentColor = TerracottaPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Printable PDF Card", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Plain Text Card", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                }

                // Options
                Text("Customize Content:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535)))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Lore & Heritage Story", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = includeLore, onCheckedChange = { includeLore = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Chef & Secret Tips", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = includeTips, onCheckedChange = { includeTips = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Family Notes", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = includeNotes, onCheckedChange = { includeNotes = it })
                }

                // Portion Multiplier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portions: ${if (currentMultiplier % 1f == 0f) "${currentMultiplier.toInt()}x" else "${currentMultiplier}x"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.5f, 1.0f, 2.0f, 3.0f).forEach { m ->
                            val isSel = currentMultiplier == m
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) TerracottaPrimary else Color(0xFFE5DCD0),
                                modifier = Modifier.clickable { currentMultiplier = m }
                            ) {
                                Text(
                                    text = "${if (m % 1f == 0f) m.toInt() else m}x",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) Color.White else Color(0xFF451A03),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // PDF Info Card Preview
                    Surface(
                        color = Color(0xFFFAF7F0),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFC89B6D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${recipe.getDisplayTitle()} (PDF)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF451A03)
                                        )
                                    )
                                    Text(
                                        text = "A4 Printable Format • Includes decorative borders, checkboxes & notes",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF78350F), fontSize = 10.5.sp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val uri = RecipePdfGenerator.createShareablePdfUri(
                                            context = context,
                                            recipe = recipe,
                                            multiplier = currentMultiplier,
                                            unitSystem = currentUnitSystem,
                                            includeLore = includeLore,
                                            includeTips = includeTips,
                                            includeNotes = includeNotes
                                        )
                                        if (uri != null) {
                                            RecipePdfGenerator.shareRecipePdf(context, uri, recipe.getDisplayTitle())
                                            onDismiss()
                                        } else {
                                            Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val safeName = recipe.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                                        savePdfLauncher.launch("${safeName}_Recipe.pdf")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Plain Text Preview Card
                    Text("Live Text Preview:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535)))
                    Surface(
                        color = Color(0xFFFAF7F0),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD6C5AD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = shareableText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFF3B2E24),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Recipe: ${recipe.title}")
                                    putExtra(Intent.EXTRA_TEXT, shareableText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Recipe via..."))
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Text", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(shareableText))
                                Toast.makeText(context, "Recipe text copied to clipboard", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Text", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Quick add to shopping list button inside share dialog
                if (onAddToShoppingList != null) {
                    HorizontalDivider(color = Color(0xFFE2D6C5))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF7F2E8),
                        border = BorderStroke(1.dp, Color(0xFFD6C5AD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAddToShoppingList()
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send Ingredients to Shopping List",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF451A03))
                                )
                            }
                            Text(
                                text = "Add +",
                                style = MaterialTheme.typography.labelSmall.copy(color = TerracottaPrimary, fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// ==========================================
// 9. RECIPE EDITOR & CREATOR DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeDialog(
    initialRecipe: RecipeEntity,
    initialTab: Int = 0,
    categories: List<String> = emptyList(),
    onSave: (RecipeEntity) -> Unit,
    onDelete: ((RecipeEntity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    // Basic Details
    var title by remember { mutableStateOf(initialRecipe.title) }
    var titleEnglish by remember { mutableStateOf(initialRecipe.titleEnglish.ifBlank { initialRecipe.title }) }
    var titleGerman by remember { mutableStateOf(initialRecipe.titleGerman) }
    var category by remember { mutableStateOf(initialRecipe.category) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var servings by remember { mutableStateOf(initialRecipe.servings) }
    var prepTime by remember { mutableStateOf(initialRecipe.prepTimeMinutes.toString()) }
    var cookTime by remember { mutableStateOf(initialRecipe.cookTimeMinutes.toString()) }
    var difficulty by remember { mutableStateOf(initialRecipe.difficulty) }
    var coverTheme by remember { mutableStateOf(initialRecipe.coverTheme) }
    var rating by remember { mutableStateOf(initialRecipe.rating) }
    var isFavorite by remember { mutableStateOf(initialRecipe.isFavorite) }
    var originStory by remember { mutableStateOf(initialRecipe.originStory) }
    var notes by remember { mutableStateOf(initialRecipe.notes) }
    var notesGerman by remember { mutableStateOf(initialRecipe.notesGerman) }
    var sourceLanguage by remember { mutableStateOf(initialRecipe.sourceLanguage) }

    // Ingredients & Steps
    val ingredients = remember { mutableStateListOf(*initialRecipe.ingredients.toTypedArray()) }
    val steps = remember { mutableStateListOf(*initialRecipe.steps.toTypedArray()) }

    // Active Editor Tab (0 = Basics & Theme, 1 = Ingredients, 2 = Steps, 3 = Lore & Notes)
    var selectedTab by remember { mutableStateOf(initialTab) }

    // Index of ingredient being edited inline (-1 = none)
    var editingIngIndex by remember { mutableStateOf<Int?>(null) }
    // New Ingredient form fields
    var newIngAmount by remember { mutableStateOf("") }
    var newIngUnit by remember { mutableStateOf("g") }
    var newIngName by remember { mutableStateOf("") }
    var newIngNameDe by remember { mutableStateOf("") }
    var newIngGroup by remember { mutableStateOf("") }
    var newIngOptional by remember { mutableStateOf(false) }

    // Index of step being edited inline (-1 = none)
    var editingStepIndex by remember { mutableStateOf<Int?>(null) }
    // New Step form fields
    var newStepEn by remember { mutableStateOf("") }
    var newStepDe by remember { mutableStateOf("") }
    var newStepTimer by remember { mutableStateOf("0") }
    var newStepTip by remember { mutableStateOf("") }

    var showDeleteConfirmInEditor by remember { mutableStateOf(false) }

    val presetCategories = listOf("Baking & Cakes", "Main Dishes", "Soups & Stews", "Desserts", "Breakfast & Brunch", "Family Classics")
    val allAvailableCategories = (categories + presetCategories).filter { it.isNotBlank() }.distinct()
    val quickUnits = listOf("g", "ml", "cup", "tbsp", "tsp", "EL", "TL", "pinch", "oz", "kg", "l")
    val quickGroups = listOf("Dough", "Filling", "Topping", "Sauce", "Garnish")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (initialRecipe.id == 0L) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialRecipe.id == 0L) "Add Recipe" else "Edit Recipe",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) TerracottaPrimary else Color.Gray
                        )
                    }
                    if (initialRecipe.id != 0L && onDelete != null) {
                        IconButton(onClick = { showDeleteConfirmInEditor = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                // Section Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = TerracottaPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Basics", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Ingredients (${ingredients.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Steps (${steps.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Notes", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        // ================= TAB 0: BASICS & THEME =================
                        0 -> {
                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    titleEnglish = it
                                },
                                label = { Text("Recipe Title *") },
                                placeholder = { Text("e.g. Traditional Apple Pie") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Category selector with Pull-Down Dropdown Menu & Quick Chips
                            Text("Category:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = { category = it },
                                    label = { Text("Category (Pull-down or type)") },
                                    trailingIcon = {
                                        IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select Category",
                                                tint = TerracottaPrimary
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    allAvailableCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(allAvailableCategories.size) { idx ->
                                    val cat = allAvailableCategories[idx]
                                    FilterChip(
                                        selected = category.equals(cat, ignoreCase = true),
                                        onClick = { category = cat },
                                        label = { Text(cat, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Servings, Prep Time, Cook Time
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = prepTime,
                                    onValueChange = { prepTime = it },
                                    label = { Text("Prep (min)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cookTime,
                                    onValueChange = { cookTime = it },
                                    label = { Text("Cook (min)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Total Time Live Calculation Banner
                            val pMin = prepTime.toIntOrNull() ?: 0
                            val cMin = cookTime.toIntOrNull() ?: 0
                            val totMin = pMin + cMin

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Total Time:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF78350F)))
                                    }
                                    Text(
                                        text = "$totMin min ($pMin prep + $cMin cook)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = servings,
                                    onValueChange = { servings = it },
                                    label = { Text("Servings (e.g. 4-6 servings)") },
                                    modifier = Modifier.weight(1.3f)
                                )
                                OutlinedTextField(
                                    value = difficulty,
                                    onValueChange = { difficulty = it },
                                    label = { Text("Difficulty") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Difficulty Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Easy", "Medium", "Advanced").forEach { diff ->
                                    FilterChip(
                                        selected = difficulty.equals(diff, ignoreCase = true),
                                        onClick = { difficulty = diff },
                                        label = { Text(diff, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Cover Theme Color Swatches
                            Text("Cookbook Cover Theme:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary))
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(CoverTheme.values().size) { idx ->
                                    val theme = CoverTheme.values()[idx]
                                    val isSelected = coverTheme == theme.name
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { coverTheme = theme.name }
                                            .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) TerracottaPrimary else Color.LightGray, RoundedCornerShape(8.dp)),
                                        color = Color(theme.primaryHex)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = theme.displayName,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isSelected) {
                                                Text("✓ Selected", color = Color(0xFFF5EFE6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Star Rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rating: ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF5A4535))
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { rating = star },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text(
                                            text = if (star <= rating) "★" else "☆",
                                            color = if (star <= rating) TerracottaPrimary else Color(0xFFD1C7B7),
                                            fontSize = 22.sp
                                        )
                                    }
                                }
                            }
                        }

                        // ================= TAB 1: INGREDIENTS =================
                        1 -> {
                            Text(
                                text = "Manage recipe ingredients, quantities, units, and categories:",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5B4E))
                            )

                            // List of existing ingredients
                            ingredients.forEachIndexed { index, ing ->
                                if (editingIngIndex == index) {
                                    // Inline editor card for this ingredient
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                                        border = BorderStroke(1.5.dp, TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        var editAmt by remember { mutableStateOf(ing.amount) }
                                        var editUnit by remember { mutableStateOf(ing.unit) }
                                        var editName by remember { mutableStateOf(ing.nameEnglish ?: ing.name) }
                                        var editGroup by remember { mutableStateOf(ing.group ?: "") }
                                        var editOpt by remember { mutableStateOf(ing.isOptional) }

                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Edit Ingredient #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaPrimary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editAmt,
                                                    onValueChange = { editAmt = it },
                                                    label = { Text("Amount") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = editUnit,
                                                    onValueChange = { editUnit = it },
                                                    label = { Text("Unit") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            OutlinedTextField(
                                                value = editName,
                                                onValueChange = { editName = it },
                                                label = { Text("Ingredient Name") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = editGroup,
                                                onValueChange = { editGroup = it },
                                                label = { Text("Section/Group (e.g. Dough, Filling, Sauce)") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Optional ingredient?", fontSize = 12.sp)
                                                Switch(checked = editOpt, onCheckedChange = { editOpt = it })
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { editingIngIndex = null }) {
                                                    Text("Cancel")
                                                }
                                                Button(
                                                    onClick = {
                                                        ingredients[index] = RecipeIngredient(
                                                            name = editName.ifBlank { "Ingredient" },
                                                            amount = editAmt,
                                                            unit = editUnit,
                                                            nameEnglish = editName.ifBlank { null },
                                                            isOptional = editOpt,
                                                            group = editGroup.ifBlank { null }
                                                        )
                                                        editingIngIndex = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                                                ) {
                                                    Text("Apply Edit")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                                        border = BorderStroke(1.dp, Color(0xFFE2D5C3)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${ing.amount} ${ing.unit} ${ing.getDisplayName(LanguageMode.ENGLISH)}".trim(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF3B2E24))
                                                )
                                                if (!ing.group.isNullOrBlank()) {
                                                    Text(ing.group, fontSize = 10.sp, color = Color(0xFF6B5B4E), fontWeight = FontWeight.Medium)
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (index > 0) {
                                                    IconButton(
                                                        onClick = {
                                                            val item = ingredients.removeAt(index)
                                                            ingredients.add(index - 1, item)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                if (index < ingredients.size - 1) {
                                                    IconButton(
                                                        onClick = {
                                                            val item = ingredients.removeAt(index)
                                                            ingredients.add(index + 1, item)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { editingIngIndex = index },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { ingredients.removeAt(index) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Add New Ingredient Section
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0)),
                                border = BorderStroke(1.dp, Color(0xFFE2D5C3)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Add New Ingredient", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TerracottaPrimary)

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = newIngAmount,
                                            onValueChange = { newIngAmount = it },
                                            placeholder = { Text("Qty (e.g. 250, 1/2)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newIngUnit,
                                            onValueChange = { newIngUnit = it },
                                            placeholder = { Text("Unit (g, ml, cup)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Quick Unit Chips
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(quickUnits.size) { idx ->
                                            val u = quickUnits[idx]
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable { newIngUnit = u }
                                                    .border(1.dp, if (newIngUnit == u) TerracottaPrimary else Color.LightGray, RoundedCornerShape(4.dp)),
                                                color = if (newIngUnit == u) Color(0xFFF3ECE0) else Color.White
                                            ) {
                                                Text(u, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newIngName,
                                        onValueChange = { newIngName = it },
                                        placeholder = { Text("Ingredient name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = newIngGroup,
                                        onValueChange = { newIngGroup = it },
                                        placeholder = { Text("Group / Section (e.g. Dough, Filling)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Quick Group Chips
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(quickGroups.size) { idx ->
                                            val g = quickGroups[idx]
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable { newIngGroup = g }
                                                    .border(1.dp, if (newIngGroup == g) TerracottaPrimary else Color.LightGray, RoundedCornerShape(4.dp)),
                                                color = if (newIngGroup == g) Color(0xFFF3ECE0) else Color.White
                                            ) {
                                                Text(g, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
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
                                                newIngName = ""
                                                newIngAmount = ""
                                                newIngGroup = ""
                                            }
                                        },
                                        enabled = newIngName.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Ingredient", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ================= TAB 2: INSTRUCTIONS / STEPS =================
                        2 -> {
                            Text(
                                text = "Edit step instructions, timers, and cooking tips:",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5B4E))
                            )

                            // List of existing steps
                            steps.forEachIndexed { index, step ->
                                if (editingStepIndex == index) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                                        border = BorderStroke(1.5.dp, TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        var editStepEn by remember { mutableStateOf(step.getInstruction(LanguageMode.ENGLISH)) }
                                        var editStepTimer by remember { mutableStateOf(step.timerMinutes.toString()) }
                                        var editStepTip by remember { mutableStateOf(step.tip ?: "") }

                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Edit Step #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaPrimary)
                                            OutlinedTextField(
                                                value = editStepEn,
                                                onValueChange = { editStepEn = it },
                                                label = { Text("Step Instruction") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editStepTimer,
                                                    onValueChange = { editStepTimer = it },
                                                    label = { Text("Timer (minutes)") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = editStepTip,
                                                    onValueChange = { editStepTip = it },
                                                    label = { Text("Chef's Tip") },
                                                    modifier = Modifier.weight(2f)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { editingStepIndex = null }) {
                                                    Text("Cancel")
                                                }
                                                Button(
                                                    onClick = {
                                                        steps[index] = step.copy(
                                                            instructionEnglish = editStepEn,
                                                            timerMinutes = editStepTimer.toIntOrNull() ?: 0,
                                                            tip = editStepTip.ifBlank { null }
                                                        )
                                                        editingStepIndex = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                                                ) {
                                                    Text("Apply Step Edit")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                                        border = BorderStroke(1.dp, Color(0xFFE2D5C3)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Step ${index + 1}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                                )
                                                Text(
                                                    text = step.getInstruction(LanguageMode.ENGLISH),
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF3B2E24))
                                                )
                                                if (step.timerMinutes > 0) {
                                                    Text("Timer: ${step.timerMinutes} min", fontSize = 10.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                                                }
                                                if (!step.tip.isNullOrBlank()) {
                                                    Text("Tip: ${step.tip}", fontSize = 10.sp, color = SageGreen, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (index > 0) {
                                                    IconButton(
                                                        onClick = {
                                                            val item = steps.removeAt(index)
                                                            steps.add(index - 1, item)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                if (index < steps.size - 1) {
                                                    IconButton(
                                                        onClick = {
                                                            val item = steps.removeAt(index)
                                                            steps.add(index + 1, item)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { editingStepIndex = index },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { steps.removeAt(index) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Add New Step Section
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0)),
                                border = BorderStroke(1.dp, Color(0xFFE2D5C3)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Add Step #${steps.size + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TerracottaPrimary)

                                    OutlinedTextField(
                                        value = newStepEn,
                                        onValueChange = { newStepEn = it },
                                        placeholder = { Text("Step instruction") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = newStepTimer,
                                            onValueChange = { newStepTimer = it },
                                            placeholder = { Text("Timer (min)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newStepTip,
                                            onValueChange = { newStepTip = it },
                                            placeholder = { Text("Chef's tip (optional)") },
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
                                                newStepEn = ""
                                                newStepTimer = "0"
                                                newStepTip = ""
                                            }
                                        },
                                        enabled = newStepEn.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Step", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ================= TAB 3: NOTES =================
                        3 -> {
                            OutlinedTextField(
                                value = originStory,
                                onValueChange = { originStory = it },
                                label = { Text("Recipe Description / Story") },
                                placeholder = { Text("e.g. Traditional recipe, ideal for family dinner or celebrations...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Chef Notes & Cooking Tips") },
                                placeholder = { Text("e.g. Bake on the middle rack at 180°C...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalRecipe = initialRecipe.copy(
                        title = title.ifBlank { titleEnglish.ifBlank { "Family Recipe" } },
                        titleGerman = titleGerman,
                        titleEnglish = titleEnglish.ifBlank { title },
                        category = category.ifBlank { "Family Classics" },
                        servings = servings.ifBlank { "4 servings" },
                        prepTimeMinutes = prepTime.toIntOrNull() ?: 20,
                        cookTimeMinutes = cookTime.toIntOrNull() ?: 30,
                        difficulty = difficulty.ifBlank { "Medium" },
                        coverTheme = coverTheme,
                        rating = rating,
                        isFavorite = isFavorite,
                        originStory = originStory,
                        notes = notes,
                        notesGerman = notesGerman,
                        sourceLanguage = sourceLanguage,
                        ingredients = ingredients.mapIndexed { idx, ing -> ing }.toList(),
                        steps = steps.mapIndexed { idx, s -> s.copy(stepNumber = idx + 1) }.toList()
                    )
                    onSave(finalRecipe)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Recipe", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDeleteConfirmInEditor && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmInEditor = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Recipe?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("Are you sure you want to delete '${initialRecipe.title}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmInEditor = false
                        onDelete(initialRecipe)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmInEditor = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// 10. INTERACTIVE 3D PAGE CURL CORNER
// ==========================================
@Composable
fun PageCurlCorner(
    alignment: Alignment = Alignment.TopEnd,
    onCornerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(x = 6.dp, y = (-6).dp)
            .size(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCornerClick() },
        contentAlignment = alignment
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width, 0f)
                lineTo(0f, 0f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFFD6C5AD),
                style = Fill
            )
            val shadowPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height)
                lineTo(size.width - 4f, size.height)
                lineTo(0f, 4f)
                close()
            }
            drawPath(
                path = shadowPath,
                color = Color(0x33000000),
                style = Fill
            )
        }
    }
}

// ==========================================
// 11. FLOATING KITCHEN TIMER WIDGET
// ==========================================
@Composable
fun FloatingKitchenTimerWidget(
    secondsRemaining: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onAddMinute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Surface(
        modifier = modifier
            .padding(16.dp)
            .shadow(12.dp, CircleShape),
        shape = CircleShape,
        color = if (secondsRemaining == 0) TerracottaPrimary else Color(0xFF292524),
        border = BorderStroke(2.dp, Color(0xFFD6C7B2))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            IconButton(
                onClick = { if (isRunning) onPause() else onResume() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Reset timer",
                    tint = Color(0xFFA8A29E),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
