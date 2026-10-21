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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeStep
import com.example.data.model.UnitSystem
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.ParchmentPageEdge
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

/**
 * Page 3..N+2: Step-by-Step Cooking Page.
 * Displays step instruction with context-aware ingredient badges, integrated step timers,
 * Chef's tips, and TTS audio narration.
 */
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
                                Icons.AutoMirrored.Filled.VolumeUp,
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
