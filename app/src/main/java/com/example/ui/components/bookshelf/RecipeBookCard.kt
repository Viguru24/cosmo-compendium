package com.example.ui.components.bookshelf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.data.model.getCharacteristicBadge
import com.example.data.model.dietaryAnalysis
import com.example.ui.components.getProfileEmoji
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle
import java.io.File

@Composable
fun RecipeBookCard(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    activeProfile: String = "Louis",
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditClick: () -> Unit,
    onGeneratePhotoClick: () -> Unit = {},
    onEditPromptClick: () -> Unit = {},
    onAssignCategoryClick: () -> Unit = {},
    onAssignProfileClick: () -> Unit = {},
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasPhoto = !recipe.imageUri.isNullOrBlank()
    val isAllFamilyView = activeProfile.equals("All", ignoreCase = true) || activeProfile.equals(AppLocalization.getFilterAllFamily(languageMode), ignoreCase = true)

    // Determine pleasant category pill colors
    val displayCat = recipe.getDisplayCategory(languageMode)
    val catLower = recipe.category.lowercase()
    val (catBg, catText, catBorder) = when {
        catLower.contains("baking") || catLower.contains("dessert") ->
            Triple(Color(0xFFFFF1E6), Color(0xFFC2410C), Color(0xFFFFD7BE))
        catLower.contains("main") ->
            Triple(Color(0xFFF0FDF4), Color(0xFF15803D), Color(0xFFBBF7D0))
        catLower.contains("soup") || catLower.contains("stew") ->
            Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
        catLower.contains("salad") || catLower.contains("side") ->
            Triple(Color(0xFFECFDF5), Color(0xFF047857), Color(0xFFA7F3D0))
        else ->
            Triple(Color(0xFFF5EFEB), Color(0xFF78350F), Color(0xFFE2D6C7))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = Color(0x223D2615), ambientColor = Color(0x113D2615))
            .testTag("recipe_book_card_${recipe.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8DFD5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            if (hasPhoto) {
                // Framed Dish Photo Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFFEFE8DE))
                ) {
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
                        contentDescription = recipe.getDisplayTitle(languageMode),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Subtle top gradient for chip visibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x66000000),
                                        Color.Transparent,
                                        Color(0x22000000)
                                    )
                                )
                            )
                    )

                    // Top Bar over photo: Category on left, Favorite & Menu on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xF2FFFFFF),
                                border = BorderStroke(0.8.dp, catBorder)
                            ) {
                                Text(
                                    text = displayCat,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = catText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isAllFamilyView) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xF2FFF7ED),
                                    border = BorderStroke(0.8.dp, TerracottaPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${getProfileEmoji(recipe.profileName)} ${recipe.profileName}",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TerracottaPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = onToggleFavorite,
                                shape = CircleShape,
                                color = Color(0xEEFFFFFF),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (recipe.isFavorite) Color(0xFFE11D48) else Color(0xFF4A3423),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Box {
                                Surface(
                                    onClick = { showMenu = !showMenu },
                                    shape = CircleShape,
                                    color = Color(0xEEFFFFFF),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "More Options",
                                            tint = Color(0xFF4A3423),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(if (!recipe.imageUri.isNullOrBlank()) "✨ Regenerate AI Food Photo" else "✨ Generate AI Food Photo")
                                                Text(
                                                    text = if (recipe.category.contains("Baking", ignoreCase = true)) "Smart Culinary Vision" else "Gourmet Food Art",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF78350F),
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onGeneratePhotoClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🎨 Edit AI Photo Prompt...") },
                                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onEditPromptClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Recipe") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onEditClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move to Family Member") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onAssignProfileClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Assign Category") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onAssignCategoryClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Recipe") },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onShareClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (languageMode == LanguageMode.GERMAN) "Rezept löschen" else "Delete Recipe", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onDeleteClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Text and Metadata Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (if no photo) or Title area
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!hasPhoto) {
                        // Top Bar: Category Pill Badge & Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = catBg,
                                    border = BorderStroke(1.dp, catBorder)
                                ) {
                                    Text(
                                        text = displayCat,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = catText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                val characteristicBadge = recipe.getCharacteristicBadge(languageMode)
                                if (characteristicBadge != null) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = "✦ $characteristicBadge",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF92400E),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isAllFamilyView) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFFFF7ED),
                                        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "${getProfileEmoji(recipe.profileName)} ${recipe.profileName}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TerracottaPrimary,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF5EFEB),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    IconButton(
                                        onClick = onToggleFavorite,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (recipe.isFavorite) Color(0xFFE11D48) else Color(0xFF8C7A6B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Box {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF5EFEB),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        IconButton(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "More Options",
                                                tint = Color(0xFF5A4535),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit Recipe") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onEditClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Move to Family Member") },
                                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onAssignProfileClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Assign Category") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onAssignCategoryClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Share Recipe") },
                                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onShareClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (languageMode == LanguageMode.GERMAN) "Rezept löschen" else "Delete Recipe", color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                onDeleteClick()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Space under category badge
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Title placed top-left
                    val displayTitle = recipe.getDisplayTitle(languageMode)
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF1E140C),
                            lineHeight = 19.sp,
                            fontSize = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recipe.rating > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(recipe.rating) {
                                Text("★", color = Color(0xFFF59E0B), fontSize = 11.5.sp)
                            }
                        }
                    }
                }

                // Dietary Suitability Badges
                val dietaryAnalysis = remember(recipe.ingredients) {
                    recipe.dietaryAnalysis
                }
                val dietaryBadges = remember(dietaryAnalysis) {
                    val list = mutableListOf<Pair<String, Color>>()
                    if (dietaryAnalysis.isCeliacSafe) {
                        list.add("🛡️ Celiac" to Color(0xFF047857))
                    } else if (dietaryAnalysis.isGlutenFree) {
                        list.add("🌾 GF" to Color(0xFFB45309))
                    }
                    if (dietaryAnalysis.isVegan) {
                        list.add("🌱 Vegan" to Color(0xFF059669))
                    } else if (dietaryAnalysis.isVegetarian) {
                        list.add("🥬 Veg" to Color(0xFF16A34A))
                    }
                    if (dietaryAnalysis.isDairyFree) {
                        list.add("🥛 DF" to Color(0xFF0284C7))
                    }
                    if (dietaryAnalysis.isNutFree) {
                        list.add("🥜 Nut-Free" to Color(0xFFD97706))
                    }
                    list
                }

                if (dietaryBadges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dietaryBadges.take(3).forEach { (label, color) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = color.copy(alpha = 0.10f),
                                border = BorderStroke(0.6.dp, color.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = color,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                        if (dietaryBadges.size > 3) {
                            Text(
                                text = "+${dietaryBadges.size - 3}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF8C7A6B),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Bottom Metadata Row: Cook Time, Difficulty & Servings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cook time
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFAF3EC),
                        border = BorderStroke(1.dp, Color(0xFFEDE0D2))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Cook Time",
                                tint = Color(0xFF9A3412),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${recipe.cookTimeMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF431407),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Difficulty
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF5EFEB)
                    ) {
                        Text(
                            text = AppLocalization.getDifficultyLabel(recipe.difficulty, languageMode),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF5A4535),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Servings
                    Text(
                        text = AppLocalization.getServingsLabel(recipe.servings, languageMode),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF786250),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
