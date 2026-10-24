package com.example.ui.components.booklet

import androidx.compose.material3.TextButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.ui.components.RecipeCookJournalPage
import com.example.ui.components.RecipeCoverPage
import com.example.ui.components.RecipeIngredientsPage
import com.example.ui.components.RecipeLoreTableOfContentsPage
import com.example.ui.components.RecipeStepPage
import com.example.ui.theme.CreamBackgroundLight
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle

@Composable
fun TabletBookletSpreadView(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onOpenConverter: ((String?, String, String) -> Unit)? = null,
    servingMultiplier: Float,
    onSetMultiplier: (Float) -> Unit,
    onOpenGlossary: (String) -> Unit,
    checkedIngredients: Set<Int>,
    onToggleIngredient: (Int) -> Unit,
    checkedSteps: Set<Int>,
    onToggleStep: (Int) -> Unit,
    onStartTimer: (Int) -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onEditRecipe: (Int) -> Unit,
    onGenerateAiCover: () -> Unit,
    onRemoveCoverPhoto: () -> Unit,
    isGeneratingCover: Boolean,
    onAddToShoppingList: () -> Unit,
    onSaveJournal: (String, Int) -> Unit,
    onIncrementCooked: () -> Unit,
    onStartCookingMode: (() -> Unit)? = null,
    onRotateOriginalPhoto: (() -> Unit)? = null,
    onOpenDietaryAdaptation: (() -> Unit)? = null,
    onOpenRecipeCosting: (() -> Unit)? = null,
    onPlayTurnSound: () -> Unit = {},
    soundEffectsEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSpread by remember { mutableStateOf(1) } // 0: Cover & Lore, 1: Ingredients & Method, 2: Journal & Notes
    var activeStepIndex by remember { mutableStateOf(0) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val changeSpread = { target: Int ->
        if (target in 0..2 && target != currentSpread) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onPlayTurnSound()
            currentSpread = target
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEDE5D8))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top Spread Navigation Bar with Stitched Paper Header
        Surface(
            color = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFD6C8B4)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Spread Button
                TextButton(
                    onClick = { changeSpread(currentSpread - 1) },
                    enabled = currentSpread > 0,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = if (currentSpread > 0) TerracottaPrimary else Color(0xFFA89F91), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppLocalization.getTurnBackLabel(languageMode), fontSize = 12.sp, color = if (currentSpread > 0) TerracottaPrimary else Color(0xFFA89F91), fontWeight = FontWeight.SemiBold)
                }

                // Spread Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val spreads = listOf(
                        Triple(0, "📖 Cover & Lore", "Cover"),
                        Triple(1, "🥗 Ingredients & Steps", "Recipe"),
                        Triple(2, "✍️ Cook's Journal", "Journal")
                    )

                    spreads.forEach { (idx, label, _) ->
                        val isSelected = currentSpread == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TerracottaPrimary else Color(0xFFF3ECE1),
                            border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else Color(0xFFD6C7B2)),
                            modifier = Modifier.clickable { changeSpread(idx) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF451A03),
                                    fontSize = 12.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Next Spread Button & Quick Mute
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { changeSpread(currentSpread + 1) },
                        enabled = currentSpread < 2,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(AppLocalization.getTurnPageLabel(languageMode), fontSize = 12.sp, color = if (currentSpread < 2) TerracottaPrimary else Color(0xFFA89F91), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (currentSpread < 2) TerracottaPrimary else Color(0xFFA89F91), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (soundEffectsEnabled) "Mute" else "Unmute",
                            tint = if (soundEffectsEnabled) TerracottaPrimary else Color(0xFF9E8E81),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Two-Page Open Book Container with 3D Animated Turn
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFAF7F2),
            shadowElevation = 8.dp,
            border = BorderStroke(2.dp, Color(0xFFD1C3B0)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(currentSpread, activeStepIndex, recipe.steps.size) {
                    var totalDragX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount
                        },
                        onDragEnd = {
                            val swipeThreshold = 50.dp.toPx()
                            if (totalDragX < -swipeThreshold) {
                                // Swiped Left -> Move Forward
                                if (currentSpread == 0) {
                                    changeSpread(1)
                                    activeStepIndex = 0
                                } else if (currentSpread == 1) {
                                    if (activeStepIndex < recipe.steps.size - 1) {
                                        activeStepIndex++
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    } else {
                                        changeSpread(2)
                                    }
                                }
                            } else if (totalDragX > swipeThreshold) {
                                // Swiped Right -> Move Backward
                                if (currentSpread == 2) {
                                    changeSpread(1)
                                    activeStepIndex = (recipe.steps.size - 1).coerceAtLeast(0)
                                } else if (currentSpread == 1) {
                                    if (activeStepIndex > 0) {
                                        activeStepIndex--
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    } else {
                                        changeSpread(0)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = currentSpread,
                transitionSpec = {
                    if (targetState > initialState) {
                        (fadeIn(animationSpec = tween(380)) + slideInHorizontally(animationSpec = tween(380)) { width -> width / 3 })
                            .togetherWith(fadeOut(animationSpec = tween(320)) + slideOutHorizontally(animationSpec = tween(320)) { width -> -width / 3 })
                    } else {
                        (fadeIn(animationSpec = tween(380)) + slideInHorizontally(animationSpec = tween(380)) { width -> -width / 3 })
                            .togetherWith(fadeOut(animationSpec = tween(320)) + slideOutHorizontally(animationSpec = tween(320)) { width -> width / 3 })
                    }
                },
                label = "TabletSpreadTurnAnimation",
                modifier = Modifier.fillMaxSize()
            ) { spreadIndex ->
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Left Page
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFFDF9))
                    ) {
                        when (spreadIndex) {
                            0 -> {
                                RecipeCoverPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onOpenBook = { changeSpread(1) },
                                    onOpenStory = { changeSpread(0) },
                                    onEditRecipe = { onEditRecipe(0) },
                                    onGenerateAiCover = onGenerateAiCover,
                                    onRemoveCoverPhoto = onRemoveCoverPhoto,
                                    isGeneratingCover = isGeneratingCover
                                )
                            }
                            1 -> {
                                RecipeIngredientsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    unitSystem = unitSystem,
                                    onUnitSystemChange = onUnitSystemChange,
                                    onOpenConverter = onOpenConverter,
                                    servingMultiplier = servingMultiplier,
                                    onSetMultiplier = onSetMultiplier,
                                    onOpenGlossary = onOpenGlossary,
                                    checkedIngredients = checkedIngredients,
                                    onToggleCheck = onToggleIngredient,
                                    onNextPage = { changeSpread(2) },
                                    onEditIngredients = { onEditRecipe(1) },
                                    onAddToShoppingList = onAddToShoppingList,
                                    onOpenDietaryAdaptation = onOpenDietaryAdaptation,
                                    onOpenRecipeCosting = onOpenRecipeCosting
                                )
                            }
                            2 -> {
                                RecipeLoreTableOfContentsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onJumpToPage = { target ->
                                        if (target == 0) changeSpread(0)
                                        else if (target == 2) changeSpread(1)
                                        else changeSpread(2)
                                    },
                                    onEditDetails = { onEditRecipe(3) },
                                    onRotateOriginalPhoto = onRotateOriginalPhoto
                                )
                            }
                        }
                    }

                    // Center Stitched Book Spine & Deep Leather Shadow
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0x3D000000),
                                        Color(0x15000000),
                                        Color(0x05000000),
                                        Color(0x15000000),
                                        Color(0x3D000000)
                                    )
                                )
                            )
                    )

                    // Right Page
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFFDF9))
                    ) {
                        when (spreadIndex) {
                            0 -> {
                                RecipeLoreTableOfContentsPage(
                                    recipe = recipe,
                                    languageMode = languageMode,
                                    onJumpToPage = { target ->
                                        if (target == 2) changeSpread(1)
                                        else if (target >= 3) {
                                            changeSpread(1)
                                            activeStepIndex = (target - 3).coerceIn(0, (recipe.steps.size - 1).coerceAtLeast(0))
                                        } else changeSpread(0)
                                    },
                                    onEditDetails = { onEditRecipe(3) },
                                    onRotateOriginalPhoto = onRotateOriginalPhoto
                                )
                            }
                            1 -> {
                                if (recipe.steps.isNotEmpty()) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        // Step Selector Pill Bar
                                        if (recipe.steps.size > 1) {
                                            Surface(
                                                color = Color(0xFFF7F2E8),
                                                border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                LazyRow(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    itemsIndexed(recipe.steps) { idx, _ ->
                                                        val isSelected = activeStepIndex == idx
                                                        val isDone = checkedSteps.contains(idx)
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = when {
                                                                isSelected -> TerracottaPrimary
                                                                isDone -> SageGreen
                                                                else -> Color(0xFFEAE0D2)
                                                            },
                                                            modifier = Modifier.clickable {
                                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                                activeStepIndex = idx
                                                            }
                                                        ) {
                                                            Text(
                                                                text = "Step ${idx + 1}${if (isDone) " ✓" else ""}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = if (isSelected || isDone) Color.White else Color(0xFF451A03),
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                    fontSize = 11.sp
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Active Step View
                                        val safeStep = recipe.steps.getOrNull(activeStepIndex.coerceIn(0, recipe.steps.size - 1))
                                        if (safeStep != null) {
                                            RecipeStepPage(
                                                step = safeStep,
                                                totalSteps = recipe.steps.size,
                                                recipe = recipe,
                                                unitSystem = unitSystem,
                                                onUnitSystemChange = onUnitSystemChange,
                                                onOpenConverter = onOpenConverter,
                                                servingMultiplier = servingMultiplier,
                                                languageMode = languageMode,
                                                isCompleted = checkedSteps.contains(activeStepIndex),
                                                onToggleCompleted = { onToggleStep(activeStepIndex) },
                                                onStartTimer = onStartTimer,
                                                onSpeak = onSpeak,
                                                onNextPage = {
                                                    if (activeStepIndex < recipe.steps.size - 1) {
                                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                        activeStepIndex++
                                                    } else {
                                                        changeSpread(2)
                                                    }
                                                },
                                                onEditStep = { onEditRecipe(2) }
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No preparation steps listed for this recipe.",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF8C7B6B))
                                        )
                                    }
                                }
                            }
                            2 -> {
                                RecipeCookJournalPage(
                                    recipe = recipe,
                                    onSaveJournal = onSaveJournal,
                                    onIncrementCooked = onIncrementCooked,
                                    onStartCookingMode = onStartCookingMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
