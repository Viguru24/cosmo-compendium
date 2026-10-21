package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.RecipeEntity
import com.example.data.model.CoverTheme
import com.example.data.model.dietaryAnalysis
import com.example.data.model.LanguageMode
import com.example.ui.components.RealisticLeatherBackground
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
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
    } catch (_: Exception) {
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
                        File(recipe.imageUri!!.removePrefix("file://"))
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
                    Surface(
                        shape = CircleShape,
                        color = Color(0x55000000),
                        border = BorderStroke(0.75.dp, Color(0x4DFFFFFF)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { showFullCoverPhotoDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "View Full Picture",
                                tint = Color(0xFFF3E7DA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
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

                        val dietaryAnalysis = remember(recipe.ingredients) {
                            recipe.dietaryAnalysis
                        }
                        val coverBadges = remember(dietaryAnalysis) {
                            val list = mutableListOf<String>()
                            if (dietaryAnalysis.isCeliacSafe) list.add("🛡️ Celiac-Safe")
                            else if (dietaryAnalysis.isGlutenFree) list.add("🌾 Gluten-Free")
                            if (dietaryAnalysis.isVegan) list.add("🌱 Vegan")
                            else if (dietaryAnalysis.isVegetarian) list.add("🥬 Vegetarian")
                            if (dietaryAnalysis.isDairyFree) list.add("🥛 Dairy-Free")
                            if (dietaryAnalysis.isNutFree) list.add("🥜 Nut-Free")
                            list
                        }
                        if (coverBadges.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                coverBadges.take(4).forEach { badge ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x38000000),
                                        border = BorderStroke(0.8.dp, Color(0x55E5D4B8))
                                    ) {
                                        Text(
                                            text = badge,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFF3E7DA),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        } else if (onGenerateAiCover != null || isGeneratingCover) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // AI Cover Art Generator Action Button
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
                                    CircularProgressIndicator(
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
                                    Icons.AutoMirrored.Filled.ArrowForward,
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
}
