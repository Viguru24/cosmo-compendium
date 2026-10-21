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
import com.example.data.model.dietaryAnalysis
import com.example.data.model.DietaryTag
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.GermanCulinaryGlossary
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.ParchmentPageEdge
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

/**
 * Interactive Measurement Dropdown Pill allowing the cook to seamlessly switch
 * between Metric, US Cups, UK Imperial, and Baker's Precision units.
 */
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

/**
 * Page 2: Ingredients & Portion Scaler.
 * Shows grouped ingredients with checkboxes, dynamic scaling, unit conversion,
 * substitution lookups for vintage/international terms, and one-tap shopping list export.
 */
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

            // Dietary & Allergen Safety Banner
            val dietaryAnalysis = remember(recipe.ingredients, recipe.tags) {
                recipe.dietaryAnalysis
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF9F5EC),
                border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Suitable Diets Chips
                    if (dietaryAnalysis.suitableDiets.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            dietaryAnalysis.suitableDiets.forEach { tag ->
                                val tagColor = when (tag) {
                                    DietaryTag.CELIAC_SAFE -> Color(0xFF047857)
                                    DietaryTag.GLUTEN_FREE -> Color(0xFFB45309)
                                    DietaryTag.VEGAN -> Color(0xFF059669)
                                    DietaryTag.VEGETARIAN -> Color(0xFF16A34A)
                                    DietaryTag.DAIRY_FREE -> Color(0xFF0284C7)
                                    DietaryTag.NUT_FREE -> Color(0xFFD97706)
                                    DietaryTag.EGG_FREE -> Color(0xFF7C3AED)
                                    DietaryTag.KETO -> Color(0xFFBE185D)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tagColor.copy(alpha = 0.12f),
                                    border = BorderStroke(0.8.dp, tagColor.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(text = tag.emoji, fontSize = 11.sp)
                                        Text(
                                            text = tag.getLabel(languageMode),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = tagColor,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Allergen Notice
                    if (dietaryAnalysis.detectedAllergens.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 11.sp
                            )
                            Text(
                                text = buildString {
                                    append(if (languageMode == LanguageMode.GERMAN) "Enthält: " else "Contains: ")
                                    append(dietaryAnalysis.detectedAllergens.joinToString(", ") { it.getLabel(languageMode) })
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF9A3412),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Celiac Cautions (Items that need certified GF labels)
                    if (dietaryAnalysis.celiacCautions.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ℹ️",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                            Text(
                                text = buildString {
                                    append(if (languageMode == LanguageMode.GERMAN) "Zöliakie-Hinweis: " else "Celiac Note: ")
                                    append(if (languageMode == LanguageMode.GERMAN) {
                                        "Auf glutenfreie Zertifizierung achten bei: "
                                    } else {
                                        "Verify gluten-free certified label on: "
                                    })
                                    append(dietaryAnalysis.celiacCautions.joinToString(", "))
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF854D0E),
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

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
                                    Icons.AutoMirrored.Filled.HelpOutline,
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
