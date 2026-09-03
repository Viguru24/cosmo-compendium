package com.example.ui.components

import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.platform.LocalConfiguration
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
    onGenerateAiCover: (() -> Unit)? = null,
    onRemoveCoverPhoto: (() -> Unit)? = null,
    isGeneratingCover: Boolean = false,
    modifier: Modifier = Modifier
) {
    val theme = try {
        CoverTheme.valueOf(recipe.coverTheme)
    } catch (e: Exception) {
        CoverTheme.VINTAGE_LEATHER
    }

    val hasPhoto = !recipe.imageUri.isNullOrBlank()
    var showFullCoverPhotoDialog by remember { mutableStateOf(false) }

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
                .testTag("book_cover_surface"),
            color = Color(0xFF231F1D)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onOpenBook() }
            ) {
                if (hasPhoto) {
                    val context = LocalContext.current
                    val imgData = if (recipe.imageUri!!.startsWith("/") || recipe.imageUri!!.startsWith("file://")) {
                        java.io.File(recipe.imageUri!!.removePrefix("file://"))
                    } else {
                        recipe.imageUri!!
                    }
                    val imageRequest = remember(recipe.imageUri, recipe.updatedAt, recipe.coverPhotoName) {
                        ImageRequest.Builder(context)
                            .data(imgData)
                            .memoryCacheKey("${recipe.imageUri}_${recipe.coverPhotoName}_${recipe.updatedAt}")
                            .diskCacheKey("${recipe.imageUri}_${recipe.coverPhotoName}_${recipe.updatedAt}")
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
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

                // Top-Right Quick Action: View Full Photo (if photo exists)
                if (hasPhoto) {
                    IconButton(
                        onClick = { showFullCoverPhotoDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(38.dp)
                            .background(Color(0x77000000), CircleShape)
                            .border(1.dp, Color(0x66FFFFFF), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "View Full Picture",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
                            text = AppLocalization.getCoverHeader(languageMode),
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEADBCE)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppLocalization.getCoverLore(languageMode),
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
                                text = "${recipe.getDisplayCategory(languageMode)} • ${AppLocalization.getDifficultyLabel(recipe.difficulty, languageMode)}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFEADBCE),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Cover Art Generator Action Button (Full line, spacious)
                        if (isGeneratingCover) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x99000000),
                                border = BorderStroke(1.dp, Color(0xFFE5D4B8))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        color = Color(0xFFFFD54F),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppLocalization.getCreatingAiCoverStatus(languageMode),
                                        color = Color(0xFFFFFDF9),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else if (onGenerateAiCover != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x44000000),
                                border = BorderStroke(1.dp, Color(0x66E5D4B8)),
                                modifier = Modifier.clickable { onGenerateAiCover() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AppLocalization.getGenerateAiCoverButton(hasPhoto, languageMode),
                                        color = Color(0xFFFFFDF9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
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
                                    text = AppLocalization.getServingsLabel(recipe.servings, languageMode),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFEADBCE),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            onClick = onOpenBook,
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                            modifier = Modifier.testTag("open_recipe_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppLocalization.getSwipeOrTapToOpen(languageMode),
                                    color = Color(0xFFFBF7F0),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Swipe to turn page",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFBF7F0)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Screen Cover Photo Modal
        if (showFullCoverPhotoDialog && hasPhoto) {
            Dialog(onDismissRequest = { showFullCoverPhotoDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1610),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
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
                                text = recipe.getDisplayTitle(),
                                color = Color(0xFFEADBCE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showFullCoverPhotoDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val imgData = if (recipe.imageUri!!.startsWith("/") || recipe.imageUri!!.startsWith("file://")) {
                            File(recipe.imageUri!!.removePrefix("file://"))
                        } else {
                            recipe.imageUri!!
                        }
                        AsyncImage(
                            model = imgData,
                            contentDescription = recipe.getDisplayTitle(),
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
                            text = "Original Recipe Card Photo",
                            color = Color(0xFFEADBCE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onRotateOriginalPhoto != null) {
                                IconButton(onClick = { onRotateOriginalPhoto() }) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Rotate 90°",
                                        tint = Color(0xFFEADBCE),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { showFullReferencePhotoDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = File(originalCardPath),
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
// INTERACTIVE MEASUREMENT DROPDOWN PILL
// ==========================================
@Composable
fun InteractiveMeasurementDropdownPill(
    amountText: String,
    currentUnitSystem: UnitSystem,
    onSelectUnitSystem: (UnitSystem) -> Unit,
    onOpenConverter: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isChecked: Boolean = false,
    showChevron: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            color = if (isChecked) Color(0xFFD8D2C5) else Color(0xFFEDE5D8),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, if (isChecked) Color(0xFFC7BFAF) else Color(0xFFD8C9B5)),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = amountText,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isChecked) Color(0xFF78716C) else Color(0xFF451A03)
                    )
                )
                if (showChevron) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Switch measurement unit",
                        tint = if (isChecked) Color(0xFF78716C) else TerracottaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFFFAF6F0))
                .border(1.dp, Color(0xFFD8C9B5), RoundedCornerShape(8.dp))
        ) {
            Text(
                text = "Change Measurement System:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8C7A6B)
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            HorizontalDivider(color = Color(0xFFE2D6C5))

            UnitSystem.values().forEach { sys ->
                val isSelected = currentUnitSystem == sys
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(sys.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = sys.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TerracottaPrimary else Color(0xFF292524)
                                    )
                                )
                                Text(
                                    text = sys.shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF78716C),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = {
                        onSelectUnitSystem(sys)
                        expanded = false
                    }
                )
            }

            if (onOpenConverter != null) {
                HorizontalDivider(color = Color(0xFFE2D6C5))
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📐", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Precision Unit Converter...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = TerracottaPrimary
                                )
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onOpenConverter()
                    }
                )
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
    onNextPage: (() -> Unit)? = null,
    onEditIngredients: (() -> Unit)? = null,
    onAddToShoppingList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .shadow(6.dp, RoundedCornerShape(10.dp))
            .border(1.dp, ParchmentPageEdge, RoundedCornerShape(10.dp)),
        color = ParchmentPage
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(scrollState)
        ) {
            // Clean Minimal Header at Top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredients (${recipe.ingredients.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        fontSize = 17.sp
                    )
                )
                if (onEditIngredients != null) {
                    IconButton(
                        onClick = onEditIngredients,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Ingredients",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                thickness = 1.dp,
                color = Color(0xFFD6C7B2)
            )

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
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
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
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleCheck(index) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SageGreen,
                                uncheckedColor = Color(0xFF8C7A6B)
                            ),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Scaled Measurement Amount & Unit with Pull-Down Menu
                        val measurementText = ingredient.getConvertedAmount(unitSystem, servingMultiplier)
                        if (measurementText.isNotBlank()) {
                            InteractiveMeasurementDropdownPill(
                                amountText = measurementText,
                                currentUnitSystem = unitSystem,
                                onSelectUnitSystem = onUnitSystemChange,
                                onOpenConverter = {
                                    onOpenConverter?.invoke(
                                        ingredient.name,
                                        ingredient.amount,
                                        ingredient.unit
                                    )
                                },
                                isChecked = isChecked
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Ingredient Name
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ingredient.getDisplayName(languageMode),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (isChecked) Color(0xFF8C857B) else Color(0xFF292524),
                                        fontSize = 13.5.sp
                                    )
                                )
                            }

                            if (ingredient.isOptional) {
                                Text(
                                    text = "(optional)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF8C7A6B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // German Substitution Glossary Button
                        if (isGermanIngredient) {
                            IconButton(
                                onClick = { onOpenGlossary(ingredient.nameGerman ?: ingredient.name) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "Substitution info",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                thickness = 0.8.dp,
                color = Color(0xFFD6C7B2)
            )

            // CONTROLS AT THE BOTTOM: Unit Selector & Portion Scaler (Clean & Compact)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF5EFE6),
                border = BorderStroke(0.8.dp, Color(0xFFE2D6C5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Unit selector pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        UnitSystem.values().forEach { system ->
                            val isSelected = unitSystem == system
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFFE5DCD0),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUnitSystemChange(system) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 1.dp)
                                ) {
                                    Text(
                                        text = when (system) {
                                            UnitSystem.METRIC_GRAMS -> "Metric"
                                            UnitSystem.CUPS_US -> "US Cups"
                                            UnitSystem.UK_IMPERIAL -> "UK"
                                            UnitSystem.BAKERS_PRECISION -> "Baker's"
                                        },
                                        color = if (isSelected) Color.White else Color(0xFF451A03),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Portion Scaler row (Clean inline without bulky headers)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f, 3.0f).forEach { scale ->
                            val isSelected = servingMultiplier == scale
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFFE5DCD0),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetMultiplier(scale) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${if (scale % 1f == 0f) scale.toInt() else scale}x",
                                        color = if (isSelected) Color.White else Color(0xFF451A03),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    if (onOpenConverter != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(0.8.dp, Color(0xFFFCD34D)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onOpenConverter("baking_soda", "2", "g") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "Precision Converter",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F),
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Open ➔",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ADD TO SHOPPING LIST BUTTON (Compact)
            if (onAddToShoppingList != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF7F0E4),
                    border = BorderStroke(1.dp, Color(0xFFD8BEA0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddToShoppingList() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add All to Shopping List",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF451A03),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. STEP-BY-STEP PAGE (Pages 3..N+2)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeStepPage(
    step: RecipeStep,
    totalSteps: Int,
    recipe: RecipeEntity? = null,
    unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
    onUnitSystemChange: ((UnitSystem) -> Unit)? = null,
    onOpenConverter: ((String?, String, String) -> Unit)? = null,
    servingMultiplier: Float = 1.0f,
    languageMode: LanguageMode,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onNextPage: (() -> Unit)? = null,
    onEditStep: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Dynamically match ingredients mentioned in this step for both new and existing recipes
    val matchedIngredients = remember(step.getInstruction(), recipe?.ingredients, unitSystem, servingMultiplier) {
        if (recipe != null) {
            com.example.ui.util.StepIngredientMatcher.findIngredientsInStep(
                stepInstruction = step.getInstruction(),
                ingredients = recipe.ingredients,
                unitSystem = unitSystem,
                multiplier = servingMultiplier
            )
        } else {
            emptyList()
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
                                onSpeak(step.getInstruction(languageMode, unitSystem), false)
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

                // INGREDIENT AMOUNTS BADGES FOR THIS STEP (Context-Aware)
                if (matchedIngredients.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFF7F2E8),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2D5C3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🥣", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Amounts needed for this step (${unitSystem.shortLabel}):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5A4535)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchedIngredients.forEach { item ->
                                    var badgeMenuExpanded by remember { mutableStateOf(false) }

                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEDE3D3),
                                            border = BorderStroke(1.dp, Color(0xFFD8C9B5)),
                                            modifier = Modifier.clickable {
                                                if (onUnitSystemChange != null) {
                                                    badgeMenuExpanded = true
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.displayAmount,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF451A03)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.matchedName,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF292524)
                                                    )
                                                )
                                                if (onUnitSystemChange != null) {
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Icon(
                                                        Icons.Default.ArrowDropDown,
                                                        contentDescription = "Switch measurement unit",
                                                        tint = TerracottaPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = badgeMenuExpanded,
                                            onDismissRequest = { badgeMenuExpanded = false },
                                            modifier = Modifier
                                                .background(Color(0xFFFAF6F0))
                                                .border(1.dp, Color(0xFFD8C9B5), RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                text = "Convert ${item.matchedName}:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF8C7A6B)
                                                ),
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )

                                            HorizontalDivider(color = Color(0xFFE2D6C5))

                                            UnitSystem.values().forEach { sys ->
                                                val isSelected = unitSystem == sys
                                                val convertedAmt = item.ingredient.getConvertedAmount(sys, servingMultiplier)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(sys.icon, fontSize = 14.sp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = "${sys.label} ($convertedAmt)",
                                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                        color = if (isSelected) TerracottaPrimary else Color(0xFF292524)
                                                                    )
                                                                )
                                                                Text(
                                                                    text = sys.shortLabel,
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        color = Color(0xFF78716C),
                                                                        fontSize = 10.sp
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    },
                                                    trailingIcon = {
                                                        if (isSelected) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = TerracottaPrimary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        onUnitSystemChange?.invoke(sys)
                                                        badgeMenuExpanded = false
                                                    }
                                                )
                                            }

                                            if (onOpenConverter != null) {
                                                HorizontalDivider(color = Color(0xFFE2D6C5))
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("📐", fontSize = 14.sp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Precision Unit Converter...",
                                                                style = MaterialTheme.typography.bodySmall.copy(
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = TerracottaPrimary
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        badgeMenuExpanded = false
                                                        onOpenConverter.invoke(
                                                            item.ingredient.name,
                                                            item.ingredient.amount,
                                                            item.ingredient.unit
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Step Instructions (with dynamic unit-aware temperature conversion)
                Text(
                    text = step.getInstruction(languageMode, unitSystem),
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
    onStartCookingMode: (() -> Unit)? = null,
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
                        text = "Compendium Collection",
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
                        text = "COMPENDIUM",
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
            appendLine("\nShared from Cosmo Compendium")
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
    onOpenPromptStudio: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isCompact = screenWidth < 380

    // Basic Details
    var title by remember { mutableStateOf(initialRecipe.title) }
    var titleEnglish by remember { mutableStateOf(initialRecipe.titleEnglish.ifBlank { initialRecipe.title }) }
    var titleGerman by remember { mutableStateOf(initialRecipe.titleGerman) }
    var category by remember { mutableStateOf(initialRecipe.category.ifBlank { "Family Classics" }) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var servings by remember { mutableStateOf(initialRecipe.servings) }
    var prepTime by remember { mutableStateOf(initialRecipe.prepTimeMinutes.toString()) }
    var cookTime by remember { mutableStateOf(initialRecipe.cookTimeMinutes.toString()) }
    var difficulty by remember { mutableStateOf(initialRecipe.difficulty.ifBlank { "Medium" }) }
    var rating by remember { mutableStateOf(initialRecipe.rating) }
    var isFavorite by remember { mutableStateOf(initialRecipe.isFavorite) }
    var originStory by remember { mutableStateOf(initialRecipe.originStory) }
    var notes by remember { mutableStateOf(initialRecipe.notes) }
    var notesGerman by remember { mutableStateOf(initialRecipe.notesGerman) }
    var sourceLanguage by remember { mutableStateOf(initialRecipe.sourceLanguage) }

    // Cover Photo State & Launchers
    var imageUri by remember { mutableStateOf(initialRecipe.imageUri) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = com.example.ui.util.ImageUtils.saveImageFromUri(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = tempCameraUri
        if (success && uri != null) {
            val savedPath = com.example.ui.util.ImageUtils.saveImageFromUri(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    // Ingredients & Steps
    val ingredients = remember { mutableStateListOf(*initialRecipe.ingredients.toTypedArray()) }
    val steps = remember { mutableStateListOf(*initialRecipe.steps.toTypedArray()) }

    // Active Editor Tab (0 = Basics & Photo, 1 = Ingredients, 2 = Steps, 3 = Lore & Notes)
    var selectedTab by remember { mutableStateOf(initialTab) }

    // Drag and drop reorder state
    var draggedIngIndex by remember { mutableStateOf<Int?>(null) }
    var dragIngOffsetY by remember { mutableStateOf(0f) }

    var draggedStepIndex by remember { mutableStateOf<Int?>(null) }
    var dragStepOffsetY by remember { mutableStateOf(0f) }

    // Index of ingredient being edited inline (-1 = none)
    var editingIngIndex by remember { mutableStateOf<Int?>(null) }
    var editIngAmount by remember { mutableStateOf("") }
    var editIngUnit by remember { mutableStateOf("") }
    var editIngName by remember { mutableStateOf("") }
    var editIngGroup by remember { mutableStateOf("") }
    var editIngOptional by remember { mutableStateOf(false) }

    // New Ingredient form fields
    var newIngAmount by remember { mutableStateOf("") }
    var newIngUnit by remember { mutableStateOf("g") }
    var newIngName by remember { mutableStateOf("") }
    var newIngGroup by remember { mutableStateOf("") }
    var newIngOptional by remember { mutableStateOf(false) }

    // Index of step being edited inline (-1 = none)
    var editingStepIndex by remember { mutableStateOf<Int?>(null) }
    var editStepEn by remember { mutableStateOf("") }
    var editStepTimer by remember { mutableStateOf("0") }
    var editStepTip by remember { mutableStateOf("") }

    // New Step form fields
    var newStepEn by remember { mutableStateOf("") }
    var newStepTimer by remember { mutableStateOf("0") }
    var newStepTip by remember { mutableStateOf("") }

    fun commitActiveIngEdit() {
        val idx = editingIngIndex ?: return
        if (idx in ingredients.indices) {
            val rawName = editIngName.ifBlank { "Ingredient" }
            val cleanName = RecipeIngredient.cleanIngredientName(rawName).ifBlank { rawName }
            ingredients[idx] = RecipeIngredient(
                name = cleanName,
                amount = editIngAmount.trim(),
                unit = editIngUnit.trim(),
                nameEnglish = cleanName,
                nameGerman = ingredients[idx].nameGerman?.let { RecipeIngredient.cleanIngredientName(it) } ?: cleanName,
                isOptional = editIngOptional,
                group = editIngGroup.trim().ifBlank { null }
            )
        }
        editingIngIndex = null
    }

    fun commitActiveStepEdit() {
        val idx = editingStepIndex ?: return
        if (idx in steps.indices) {
            steps[idx] = steps[idx].copy(
                instructionEnglish = editStepEn.trim().ifBlank { steps[idx].instructionEnglish },
                instructionGerman = steps[idx].instructionGerman.ifBlank { editStepEn.trim() },
                timerMinutes = editStepTimer.toIntOrNull() ?: 0,
                tip = editStepTip.trim().ifBlank { null }
            )
        }
        editingStepIndex = null
    }

    var showDeleteConfirmInEditor by remember { mutableStateOf(false) }

    val presetCategories = listOf("Baking & Cakes", "Main Dishes", "Soups & Stews", "Desserts", "Breakfast & Brunch", "Family Classics")
    val allAvailableCategories = (categories + presetCategories).filter { it.isNotBlank() }.distinct()
    val quickUnits = listOf("g", "ml", "cup", "tbsp", "tsp", "oz", "pkg", "pinch", "kg", "l")
    val quickGroups = listOf("Dough", "Filling", "Topping", "Sauce", "Garnish")

    // Theme color palette for editor
    val editorTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF2C1E14),
        unfocusedTextColor = Color(0xFF2C1E14),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFFFFDF9),
        focusedBorderColor = TerracottaPrimary,
        unfocusedBorderColor = Color(0xFFD6C7B2),
        focusedLabelColor = TerracottaPrimary,
        unfocusedLabelColor = Color(0xFF786555),
        cursorColor = TerracottaPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFFFFDF9),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (initialRecipe.id == 0L) Icons.Default.Add else Icons.Default.Edit,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (initialRecipe.id == 0L) "Add Recipe" else "Edit Recipe",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF431407),
                            fontFamily = FontFamily.Serif,
                            fontSize = if (isCompact) 17.sp else 19.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) TerracottaPrimary else Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (initialRecipe.id != 0L && onDelete != null) {
                        IconButton(
                            onClick = { showDeleteConfirmInEditor = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp)
            ) {
                // Modern Section Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF7F2E8),
                    contentColor = TerracottaPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Basics",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Ingredients (${ingredients.size})",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Steps (${steps.size})",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 2) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                "Notes",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 3) TerracottaPrimary else Color(0xFF786555)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        // ================= TAB 0: BASICS & COVER PHOTO =================
                        0 -> {
                            // Cover Photo Management Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFBF8F2),
                                border = BorderStroke(1.dp, Color(0xFFE8DED1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Dish Cover Photo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF431407)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Photo Thumbnail or Placeholder
                                        if (!imageUri.isNullOrBlank() && (imageUri!!.startsWith("http") || File(imageUri!!).exists() || imageUri!!.startsWith("content://"))) {
                                            val modelData = if (imageUri!!.startsWith("/") || imageUri!!.startsWith("file://")) {
                                                File(imageUri!!.removePrefix("file://"))
                                            } else {
                                                imageUri!!
                                            }
                                            AsyncImage(
                                                model = modelData,
                                                contentDescription = "Cover Photo",
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFD6C7B2), RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFEDE4D6),
                                                border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                                                modifier = Modifier.size(72.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF8C7A6B), modifier = Modifier.size(24.dp))
                                                    Text("No Photo", fontSize = 9.sp, color = Color(0xFF8C7A6B), fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Photo Action Buttons
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = { galleryLauncher.launch("image/*") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, TerracottaPrimary),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        val uri = com.example.ui.util.ImageUtils.createTempCameraUri(context)
                                                        tempCameraUri = uri
                                                        cameraLauncher.launch(uri)
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFD97706)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (onOpenPromptStudio != null) {
                                                    OutlinedButton(
                                                        onClick = onOpenPromptStudio,
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, SageGreen),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("AI Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (!imageUri.isNullOrBlank()) {
                                                    OutlinedButton(
                                                        onClick = { imageUri = null },
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Remove", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Recipe Title Field
                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    titleEnglish = it
                                },
                                label = { Text("Recipe Title *") },
                                placeholder = { Text("e.g. Traditional Apple Pie") },
                                colors = editorTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Category Selector with Pull-Down Menu ONLY (No redundant chips!)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = { category = it },
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select Category",
                                                tint = TerracottaPrimary
                                            )
                                        }
                                    },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFFFFFDF9)).border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(10.dp))
                                ) {
                                    allAvailableCategories.forEach { cat ->
                                        val isSelected = category.equals(cat, ignoreCase = true)
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = cat,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) TerracottaPrimary else Color(0xFF2C1E14)
                                                )
                                            },
                                            trailingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Servings, Prep Time, Cook Time
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = servings,
                                    onValueChange = { servings = it },
                                    label = { Text("Servings") },
                                    placeholder = { Text("e.g. 4") },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = prepTime,
                                    onValueChange = { prepTime = it },
                                    label = { Text("Prep (min)") },
                                    colors = editorTextFieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cookTime,
                                    onValueChange = { cookTime = it },
                                    label = { Text("Cook (min)") },
                                    colors = editorTextFieldColors,
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
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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

                            // Difficulty Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Difficulty:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF5A4535))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Easy", "Medium", "Advanced").forEach { diff ->
                                        val isSel = difficulty.equals(diff, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSel) Color(0xFFFFEDD5) else Color(0xFFF3ECE0),
                                            border = BorderStroke(1.dp, if (isSel) TerracottaPrimary else Color(0xFFD6C7B2)),
                                            modifier = Modifier.clickable { difficulty = diff }
                                        ) {
                                            Text(
                                                text = diff,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) TerracottaPrimary else Color(0xFF6B5B4E),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Star Rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rating:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF5A4535))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = { rating = star },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Text(
                                                text = if (star <= rating) "★" else "☆",
                                                color = if (star <= rating) Color(0xFFD97706) else Color(0xFFD1C7B7),
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ================= TAB 1: INGREDIENTS =================
                        1 -> {
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
                                                    onValueChange = { editIngAmount = it },
                                                    label = { Text("Amount") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = editIngUnit,
                                                    onValueChange = { editIngUnit = it },
                                                    label = { Text("Unit") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            OutlinedTextField(
                                                value = editIngName,
                                                onValueChange = { editIngName = it },
                                                label = { Text("Ingredient Name") },
                                                colors = editorTextFieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = editIngGroup,
                                                onValueChange = { editIngGroup = it },
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
                                                    onCheckedChange = { editIngOptional = it },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SageGreen)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { editingIngIndex = null }) {
                                                    Text("Cancel", color = Color(0xFF786555))
                                                }
                                                Button(
                                                    onClick = { commitActiveIngEdit() },
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
                                                                    draggedIngIndex = index
                                                                    dragIngOffsetY = 0f
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragIngOffsetY += dragAmount.y
                                                                    val currentIdx = draggedIngIndex ?: return@detectDragGestures
                                                                    val itemHeightPx = 48.dp.toPx()
                                                                    if (dragIngOffsetY > itemHeightPx && currentIdx < ingredients.size - 1) {
                                                                        val item = ingredients.removeAt(currentIdx)
                                                                        ingredients.add(currentIdx + 1, item)
                                                                        draggedIngIndex = currentIdx + 1
                                                                        dragIngOffsetY -= itemHeightPx
                                                                    } else if (dragIngOffsetY < -itemHeightPx && currentIdx > 0) {
                                                                        val item = ingredients.removeAt(currentIdx)
                                                                        ingredients.add(currentIdx - 1, item)
                                                                        draggedIngIndex = currentIdx - 1
                                                                        dragIngOffsetY += itemHeightPx
                                                                    }
                                                                },
                                                                onDragEnd = {
                                                                    draggedIngIndex = null
                                                                    dragIngOffsetY = 0f
                                                                },
                                                                onDragCancel = {
                                                                    draggedIngIndex = null
                                                                    dragIngOffsetY = 0f
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
                                                    onClick = {
                                                        commitActiveIngEdit()
                                                        editingIngIndex = index
                                                        editIngAmount = ing.amount
                                                        editIngUnit = ing.unit
                                                        editIngName = ing.nameEnglish ?: ing.name
                                                        editIngGroup = ing.group ?: ""
                                                        editIngOptional = ing.isOptional
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (editingIngIndex == index) editingIngIndex = null
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
                                            onValueChange = { newIngAmount = it },
                                            placeholder = { Text("Qty (e.g. 250, 1/2)", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newIngUnit,
                                            onValueChange = { newIngUnit = it },
                                            placeholder = { Text("Unit", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Quick Unit Chips
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(quickUnits.size) { idx ->
                                            val u = quickUnits[idx]
                                            val isSel = newIngUnit == u
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSel) Color(0xFFFFEDD5) else Color.White,
                                                border = BorderStroke(1.dp, if (isSel) TerracottaPrimary else Color(0xFFD6C7B2)),
                                                modifier = Modifier.clickable { newIngUnit = u }
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
                                        onValueChange = { newIngName = it },
                                        placeholder = { Text("Ingredient name (e.g. All-purpose flour)", fontSize = 12.sp) },
                                        colors = editorTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = newIngGroup,
                                        onValueChange = { newIngGroup = it },
                                        placeholder = { Text("Group (e.g. Dough, Filling, Sauce)", fontSize = 12.sp) },
                                        colors = editorTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Quick Group Chips
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(quickGroups.size) { idx ->
                                            val g = quickGroups[idx]
                                            val isSel = newIngGroup == g
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSel) Color(0xFFFFEDD5) else Color.White,
                                                border = BorderStroke(1.dp, if (isSel) TerracottaPrimary else Color(0xFFD6C7B2)),
                                                modifier = Modifier.clickable { newIngGroup = g }
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
                                                newIngName = ""
                                                newIngAmount = ""
                                                newIngUnit = "g"
                                                newIngGroup = ""
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

                        // ================= TAB 2: INSTRUCTIONS / STEPS =================
                        2 -> {
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
                                                onValueChange = { editStepEn = it },
                                                label = { Text("Step Instruction") },
                                                colors = editorTextFieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editStepTimer,
                                                    onValueChange = { editStepTimer = it },
                                                    label = { Text("Timer (min)") },
                                                    colors = editorTextFieldColors,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = editStepTip,
                                                    onValueChange = { editStepTip = it },
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
                                                TextButton(onClick = { editingStepIndex = null }) {
                                                    Text("Cancel", color = Color(0xFF786555))
                                                }
                                                Button(
                                                    onClick = { commitActiveStepEdit() },
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
                                                                    draggedStepIndex = index
                                                                    dragStepOffsetY = 0f
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragStepOffsetY += dragAmount.y
                                                                    val currentIdx = draggedStepIndex ?: return@detectDragGestures
                                                                    val itemHeightPx = 54.dp.toPx()
                                                                    if (dragStepOffsetY > itemHeightPx && currentIdx < steps.size - 1) {
                                                                        val item = steps.removeAt(currentIdx)
                                                                        steps.add(currentIdx + 1, item)
                                                                        draggedStepIndex = currentIdx + 1
                                                                        dragStepOffsetY -= itemHeightPx
                                                                    } else if (dragStepOffsetY < -itemHeightPx && currentIdx > 0) {
                                                                        val item = steps.removeAt(currentIdx)
                                                                        steps.add(currentIdx - 1, item)
                                                                        draggedStepIndex = currentIdx - 1
                                                                        dragStepOffsetY += itemHeightPx
                                                                    }
                                                                },
                                                                onDragEnd = {
                                                                    draggedStepIndex = null
                                                                    dragStepOffsetY = 0f
                                                                },
                                                                onDragCancel = {
                                                                    draggedStepIndex = null
                                                                    dragStepOffsetY = 0f
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
                                                    onClick = {
                                                        commitActiveStepEdit()
                                                        editingStepIndex = index
                                                        editStepEn = step.getInstruction(LanguageMode.ENGLISH)
                                                        editStepTimer = step.timerMinutes.toString()
                                                        editStepTip = step.tip ?: ""
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (editingStepIndex == index) editingStepIndex = null
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
                                        onValueChange = { newStepEn = it },
                                        placeholder = { Text("Step instruction (e.g. Whisk eggs and sugar until pale)", fontSize = 12.sp) },
                                        colors = editorTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = newStepTimer,
                                            onValueChange = { newStepTimer = it },
                                            placeholder = { Text("Timer (min)", fontSize = 12.sp) },
                                            colors = editorTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newStepTip,
                                            onValueChange = { newStepTip = it },
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
                                                newStepEn = ""
                                                newStepTimer = "0"
                                                newStepTip = ""
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

                        // ================= TAB 3: NOTES =================
                        3 -> {
                            OutlinedTextField(
                                value = originStory,
                                onValueChange = { originStory = it },
                                label = { Text("Recipe Description / Story") },
                                placeholder = { Text("e.g. Traditional family recipe from Oma, perfect for Sunday dinners...") },
                                colors = editorTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Chef Notes & Cooking Tips") },
                                placeholder = { Text("e.g. Bake on the middle rack at 180°C. Best served warm with vanilla custard...") },
                                colors = editorTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    commitActiveIngEdit()
                    commitActiveStepEdit()

                    val finalIngredients = ingredients.map { ing ->
                        val rawName = ing.nameEnglish ?: ing.name
                        val clean = RecipeIngredient.cleanIngredientName(rawName).ifBlank { rawName }
                        ing.copy(
                            name = clean,
                            nameEnglish = clean,
                            nameGerman = ing.nameGerman?.let { RecipeIngredient.cleanIngredientName(it) } ?: clean
                        )
                    }.toList()

                    val finalSteps = steps.mapIndexed { idx, s -> s.copy(stepNumber = idx + 1) }.toList()

                    val finalRecipe = initialRecipe.copy(
                        title = title.ifBlank { titleEnglish.ifBlank { "Family Recipe" } },
                        titleGerman = titleGerman.ifBlank { title },
                        titleEnglish = titleEnglish.ifBlank { title },
                        category = category.ifBlank { "Family Classics" },
                        servings = servings.ifBlank { "4 servings" },
                        prepTimeMinutes = prepTime.toIntOrNull() ?: 20,
                        cookTimeMinutes = cookTime.toIntOrNull() ?: 30,
                        difficulty = difficulty.ifBlank { "Medium" },
                        imageUri = imageUri,
                        rating = rating,
                        isFavorite = isFavorite,
                        originStory = originStory,
                        notes = notes,
                        notesGerman = notesGerman.ifBlank { notes },
                        sourceLanguage = sourceLanguage,
                        ingredients = finalIngredients,
                        steps = finalSteps
                    )
                    onSave(finalRecipe)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Recipe", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF786555))
            }
        }
    )

    if (showDeleteConfirmInEditor && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmInEditor = false },
            containerColor = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(14.dp),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                )
            },
            text = {
                Text("Are you sure you want to delete '${initialRecipe.title}'? This action cannot be undone.", color = Color(0xFF5A4535))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmInEditor = false
                        onDelete(initialRecipe)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmInEditor = false }) {
                    Text("Cancel", color = Color(0xFF786555))
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
