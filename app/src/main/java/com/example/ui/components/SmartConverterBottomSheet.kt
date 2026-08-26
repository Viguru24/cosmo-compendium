package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

/**
 * Data class representing an ingredient category with density for smart volumetric conversions.
 */
data class ConverterIngredient(
    val id: String,
    val name: String,
    val germanName: String,
    val emoji: String,
    val densityGramsPerCup: Double,
    val isBakingSpiceOrLeavener: Boolean = false,
    val category: String = "Common"
)

object SmartConverterData {
    val ingredients = listOf(
        ConverterIngredient("baking_soda", "Baking Soda / Bicarb (Natron)", "Natron / Speisenatron", "🫧", 220.0, isBakingSpiceOrLeavener = true, "Baking & Spices"),
        ConverterIngredient("baking_powder", "Baking Powder (Backpulver)", "Backpulver", "🧁", 190.0, isBakingSpiceOrLeavener = true, "Baking & Spices"),
        ConverterIngredient("salt", "Fine Table Salt (Salz)", "Speisesalz", "🧂", 290.0, isBakingSpiceOrLeavener = true, "Baking & Spices"),
        ConverterIngredient("cinnamon", "Ground Cinnamon (Zimt)", "Zimt gemahlen", "🪵", 130.0, isBakingSpiceOrLeavener = true, "Baking & Spices"),
        ConverterIngredient("vanilla_sugar", "Vanilla Sugar (Vanillezucker)", "Vanillezucker", "🍦", 200.0, isBakingSpiceOrLeavener = true, "Baking & Spices"),
        ConverterIngredient("yeast", "Active Dry Yeast (Trockenhefe)", "Trockenhefe", "🍞", 150.0, isBakingSpiceOrLeavener = true, "Baking & Spices"),
        ConverterIngredient("cornstarch", "Cornstarch / Starch (Stärke)", "Speisestärke / Mondamin", "🌽", 128.0, false, "Flours & Grains"),
        ConverterIngredient("all_purpose_flour", "All-Purpose Flour (Mehl 405/550)", "Weizenmehl Type 405", "🌾", 125.0, false, "Flours & Grains"),
        ConverterIngredient("bread_flour", "Bread Flour (Mehl Type 550)", "Weizenmehl Type 550", "🥖", 130.0, false, "Flours & Grains"),
        ConverterIngredient("granulated_sugar", "Granulated White Sugar", "Haushaltszucker", "🍬", 200.0, false, "Sugars & Sweeteners"),
        ConverterIngredient("powdered_sugar", "Powdered / Icing Sugar", "Puderzucker", "❄️", 120.0, false, "Sugars & Sweeteners"),
        ConverterIngredient("brown_sugar", "Brown Sugar (packed)", "Brauner Rohrzucker", "🟤", 220.0, false, "Sugars & Sweeteners"),
        ConverterIngredient("honey", "Honey / Golden Syrup", "Honig / Sirup", "🍯", 340.0, false, "Sugars & Sweeteners"),
        ConverterIngredient("butter", "Butter / Margarine", "Butter / Margarine", "🧈", 227.0, false, "Dairy & Liquids"),
        ConverterIngredient("water_milk", "Water / Milk / Liquid", "Wasser / Milch", "🥛", 240.0, false, "Dairy & Liquids"),
        ConverterIngredient("heavy_cream", "Heavy Whipping Cream (Sahne)", "Schlagsahne (30%)", "🥛", 238.0, false, "Dairy & Liquids"),
        ConverterIngredient("cocoa", "Unsweetened Cocoa Powder", "Backkakao", "🍫", 100.0, false, "Baking & Spices"),
        ConverterIngredient("rolled_oats", "Rolled Oats (Haferflocken)", "Zarte Haferflocken", "🥣", 90.0, false, "Flours & Grains"),
        ConverterIngredient("ground_almonds", "Ground Almonds / Hazelnuts", "Gemahlene Mandeln / Nüsse", "🌰", 100.0, false, "Baking & Spices")
    )
}

enum class ConverterUnit(val symbol: String, val label: String, val type: UnitType) {
    GRAM("g", "Grams (g)", UnitType.WEIGHT),
    KG("kg", "Kilograms (kg)", UnitType.WEIGHT),
    OZ("oz", "Ounces (oz)", UnitType.WEIGHT),
    LB("lbs", "Pounds (lbs)", UnitType.WEIGHT),
    TSP("tsp", "Teaspoon (tsp / TL)", UnitType.VOLUME),
    TBSP("tbsp", "Tablespoon (tbsp / EL)", UnitType.VOLUME),
    CUP("cup", "Cup (US ~240ml)", UnitType.VOLUME),
    ML("ml", "Milliliters (ml)", UnitType.VOLUME),
    LITER("l", "Liters (l)", UnitType.VOLUME),
    FL_OZ("fl oz", "Fluid Ounce (fl oz)", UnitType.VOLUME),
    PINCH("pinch", "Pinch (Prise)", UnitType.SPECIAL),
    KNIFE_TIP("knife-tip", "Knife-Tip (Messerspitze / Msp.)", UnitType.SPECIAL)
}

enum class UnitType {
    WEIGHT, VOLUME, SPECIAL
}

data class ConversionResult(
    val valueStr: String,
    val unitStr: String,
    val note: String? = null,
    val handyKitchenHint: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartConverterBottomSheet(
    initialIngredientName: String? = null,
    initialAmount: String = "2",
    initialUnit: String = "g",
    onDismiss: () -> Unit,
    onApplyToRecipe: ((String, String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    // Find initial matching ingredient
    var selectedIngredient by remember {
        val match = if (!initialIngredientName.isNullOrBlank()) {
            val q = initialIngredientName.lowercase()
            SmartConverterData.ingredients.firstOrNull {
                it.id.contains(q) || it.name.lowercase().contains(q) || it.germanName.lowercase().contains(q)
            }
        } else null
        mutableStateOf(match ?: SmartConverterData.ingredients[0]) // Default Baking soda/Natron for easy 2g example
    }

    var inputAmount by remember { mutableStateOf(initialAmount.ifBlank { "2" }) }
    var inputUnit by remember {
        val found = ConverterUnit.values().firstOrNull { it.symbol.equals(initialUnit.trim(), ignoreCase = true) }
        mutableStateOf(found ?: ConverterUnit.GRAM)
    }

    // Quick presets based on ingredient
    val quickPresets = remember(selectedIngredient) {
        if (selectedIngredient.isBakingSpiceOrLeavener) {
            listOf("0.5", "1", "2", "3", "5", "7", "10", "15", "16")
        } else {
            listOf("50", "100", "125", "150", "200", "250", "500", "1")
        }
    }

    // Compute smart multi-unit conversion breakdown
    val conversions = remember(inputAmount, inputUnit, selectedIngredient) {
        calculateConversions(inputAmount, inputUnit, selectedIngredient)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFAF7F2),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                color = Color(0xFFD6CCC0),
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(modifier = Modifier.size(width = 40.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚖️", fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SMART KITCHEN CONVERTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TerracottaPrimary,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = "Small Weights, Spoons & German Measures",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E241E)
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF786B60))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ingredient Selector Horizontal Carousel
            Text(
                text = "1. SELECT INGREDIENT DENSITY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C6C5E)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SmartConverterData.ingredients) { ing ->
                    val isSelected = selectedIngredient.id == ing.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TerracottaPrimary else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else Color(0xFFE2D7C8)),
                        modifier = Modifier.clickable { selectedIngredient = ing }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ing.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ing.name.split("/")[0].split("(")[0].trim(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF2C241E)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input + Unit Selector
            Text(
                text = "2. ENTER RECIPE AMOUNT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C6C5E)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("converter_amount_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = Color(0xFFD8CCBE)
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF231A12)
                    )
                )

                // Quick Unit Picker Chips
                LazyRow(
                    modifier = Modifier.weight(1.4f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val primaryUnits = listOf(
                        ConverterUnit.GRAM,
                        ConverterUnit.TSP,
                        ConverterUnit.TBSP,
                        ConverterUnit.CUP,
                        ConverterUnit.OZ,
                        ConverterUnit.ML,
                        ConverterUnit.KNIFE_TIP,
                        ConverterUnit.PINCH
                    )
                    items(primaryUnits) { unit ->
                        val isSelected = inputUnit == unit
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF451A03) else Color(0xFFEFE7DC),
                            modifier = Modifier.clickable { inputUnit = unit }
                        ) {
                            Text(
                                text = unit.symbol,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 11.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF4B3C31)
                                )
                            )
                        }
                    }
                }
            }

            // Quick Preset Amount Chips (e.g. 2g, 5g, 10g or standard pinch)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Quick:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8C7A6B))
                )
                quickPresets.take(6).forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (inputAmount == preset) TerracottaPrimary.copy(alpha = 0.15f) else Color(0xFFF0EAE1),
                        border = BorderStroke(1.dp, if (inputAmount == preset) TerracottaPrimary else Color.Transparent),
                        modifier = Modifier.clickable { inputAmount = preset }
                    ) {
                        Text(
                            text = "$preset ${inputUnit.symbol}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (inputAmount == preset) TerracottaPrimary else Color(0xFF5A4839)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RESULT CARD: High-visibility Easy Measurement Spotlight
            Text(
                text = "3. EASIEST KITCHEN MEASUREMENTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C6C5E)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)),
                border = BorderStroke(1.5.dp, Color(0xFFE6DCCE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Spotlight Primary Best Match
                    val primaryBest = conversions.firstOrNull()
                    if (primaryBest != null) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "✨", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "RECOMMENDED EASY MEASURE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF92400E),
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                        Text(
                                            text = "${primaryBest.valueStr} ${primaryBest.unitStr}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF451A03),
                                                fontFamily = FontFamily.Serif
                                            )
                                        )
                                        if (primaryBest.handyKitchenHint != null) {
                                            Text(
                                                text = primaryBest.handyKitchenHint,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF78350F),
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Copy / Apply Action
                                if (onApplyToRecipe != null) {
                                    Button(
                                        onClick = { onApplyToRecipe(primaryBest.valueStr, primaryBest.unitStr) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Use in Recipe", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString("${primaryBest.valueStr} ${primaryBest.unitStr}"))
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = TerracottaPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Other Standard Equivalent Table
                    Text(
                        text = "Equivalent Conversions:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B5B4E)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    conversions.drop(1).take(5).forEach { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conv.unitStr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF524439),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1EAE0)
                            ) {
                                Text(
                                    text = conv.valueStr,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF322820)
                                    )
                                )
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEFE8DE))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // German Vintage Handy Guide Card (Messerspitze, Prise, Gestrichen)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5EFE6),
                border = BorderStroke(1.dp, Color(0xFFE0D5C5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "💡 Vintage Rule of Thumb for Spices & Leaveners:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF451A03)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• 2g Baking Soda ≈ 1/2 level teaspoon (or 2 generous knife-tips / Messerspitze)\n• 1 tsp ≈ 4.8g to 5g | 1/2 tsp ≈ 2.4g | 1/4 tsp ≈ 1.2g\n• 1 Messerspitze (Msp.) ≈ 0.5g to 1.0g on the flat end of a butter knife",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF5E4E42)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculates user-friendly kitchen conversions tailored for ease of use (spoons, fractions, knife-tips).
 */
private fun calculateConversions(
    amountStr: String,
    fromUnit: ConverterUnit,
    ingredient: ConverterIngredient
): List<ConversionResult> {
    val cleanAmt = amountStr.trim().replace(",", ".").replace("½", "0.5").replace("¼", "0.25").replace("¾", "0.75")
    val numeric = cleanAmt.toDoubleOrNull() ?: 2.0
    val density = ingredient.densityGramsPerCup // grams per ~240ml

    // 1. Normalize input to Grams
    val grams: Double = when (fromUnit) {
        ConverterUnit.GRAM -> numeric
        ConverterUnit.KG -> numeric * 1000.0
        ConverterUnit.OZ -> numeric * 28.3495
        ConverterUnit.LB -> numeric * 453.592
        ConverterUnit.TSP -> numeric * (density / 48.0) // 1 cup = 48 tsp
        ConverterUnit.TBSP -> numeric * (density / 16.0) // 1 cup = 16 tbsp
        ConverterUnit.CUP -> numeric * density
        ConverterUnit.ML -> numeric * (density / 240.0)
        ConverterUnit.LITER -> numeric * 1000.0 * (density / 240.0)
        ConverterUnit.FL_OZ -> numeric * 29.57 * (density / 240.0)
        ConverterUnit.PINCH -> numeric * 0.4
        ConverterUnit.KNIFE_TIP -> numeric * 0.8
    }

    val results = mutableListOf<ConversionResult>()

    // Special logic for Small Leaveners & Spices (< 15g) like 2g Baking Soda / Natron
    if (ingredient.isBakingSpiceOrLeavener && grams <= 15.0) {
        val tspFraction = grams / (density / 48.0)
        val tspRounded = when {
            tspFraction in 0.15..0.35 -> "1/4"
            tspFraction in 0.36..0.65 -> "1/2"
            tspFraction in 0.66..0.85 -> "3/4"
            tspFraction in 0.86..1.20 -> "1"
            tspFraction in 1.21..1.60 -> "1 1/2"
            tspFraction in 1.61..2.20 -> "2"
            else -> String.format("%.1f", tspFraction)
        }

        val knifeTips = (grams / 0.8).toInt().coerceAtLeast(1)

        results.add(
            ConversionResult(
                valueStr = tspRounded,
                unitStr = "tsp (Teelöffel)",
                note = "Easy spoon measure",
                handyKitchenHint = if (grams in 1.5..2.5) "Exact kitchen substitute for ~2 grams!" else null
            )
        )

        results.add(
            ConversionResult(
                valueStr = "$knifeTips",
                unitStr = "Messerspitze (Knife-Tip / Msp.)",
                note = "Traditional German pinch"
            )
        )

        if (grams >= 4.0) {
            val tbsp = grams / (density / 16.0)
            results.add(
                ConversionResult(
                    valueStr = if (tbsp in 0.4..0.6) "1/2" else if (tbsp in 0.9..1.1) "1" else String.format("%.1f", tbsp),
                    unitStr = "tbsp (Esslöffel / EL)",
                    note = "Tablespoon"
                )
            )
        }

        results.add(
            ConversionResult(
                valueStr = if (grams % 1.0 == 0.0) grams.toInt().toString() else String.format("%.1f", grams),
                unitStr = "grams (g)",
                note = "Weight on digital scale"
            )
        )

        val oz = grams / 28.3495
        results.add(
            ConversionResult(
                valueStr = String.format("%.2f", oz),
                unitStr = "ounces (oz)",
                note = "Imperial weight"
            )
        )

        return results
    }

    // Standard Volumetric & Weight logic for Flours, Sugars, Butter, Liquids
    val cups = grams / density
    val tbsp = grams / (density / 16.0)
    val tsp = grams / (density / 48.0)
    val oz = grams / 28.3495

    // Pick top recommendation based on magnitude
    when {
        cups >= 0.2 -> {
            val cupFraction = formatToKitchenFraction(cups)
            results.add(
                ConversionResult(
                    valueStr = cupFraction,
                    unitStr = "cups (Tassen)",
                    note = "Volumetric standard",
                    handyKitchenHint = "Fill cup loosely, level with straight edge"
                )
            )
        }
        tbsp >= 1.0 -> {
            val tbspFraction = formatToKitchenFraction(tbsp)
            results.add(
                ConversionResult(
                    valueStr = tbspFraction,
                    unitStr = "tbsp (Esslöffel / EL)",
                    note = "Tablespoon measure"
                )
            )
        }
        else -> {
            val tspFraction = formatToKitchenFraction(tsp)
            results.add(
                ConversionResult(
                    valueStr = tspFraction,
                    unitStr = "tsp (Teelöffel / TL)",
                    note = "Teaspoon measure"
                )
            )
        }
    }

    results.add(
        ConversionResult(
            valueStr = if (grams >= 1000) String.format("%.2f kg", grams / 1000.0) else "${grams.toInt()} g",
            unitStr = "Metric Weight (Gramm)",
            note = "Digital scale weight"
        )
    )

    results.add(
        ConversionResult(
            valueStr = String.format("%.1f oz", oz),
            unitStr = "US Ounces (oz)",
            note = "Imperial weight"
        )
    )

    if (tbsp >= 0.5 && !results.any { it.unitStr.contains("tbsp") }) {
        results.add(
            ConversionResult(
                valueStr = formatToKitchenFraction(tbsp),
                unitStr = "tbsp (Esslöffel / EL)",
                note = "Tablespoon"
            )
        )
    }

    if (tsp >= 0.5 && !results.any { it.unitStr.contains("tsp") }) {
        results.add(
            ConversionResult(
                valueStr = formatToKitchenFraction(tsp),
                unitStr = "tsp (Teelöffel / TL)",
                note = "Teaspoon"
            )
        )
    }

    val ml = grams * (240.0 / density)
    results.add(
        ConversionResult(
            valueStr = "${ml.toInt()} ml",
            unitStr = "Volume (ml / Milliliter)",
            note = "Liquid measuring jug"
        )
    )

    return results
}

private fun formatToKitchenFraction(num: Double): String {
    val whole = num.toInt()
    val frac = num - whole
    val fracStr = when {
        frac in 0.12..0.28 -> "1/4"
        frac in 0.29..0.40 -> "1/3"
        frac in 0.41..0.58 -> "1/2"
        frac in 0.59..0.70 -> "2/3"
        frac in 0.71..0.88 -> "3/4"
        else -> ""
    }
    return when {
        whole > 0 && fracStr.isNotEmpty() -> "$whole $fracStr"
        whole > 0 && fracStr.isEmpty() -> "$whole"
        fracStr.isNotEmpty() -> fracStr
        else -> String.format("%.1f", num)
    }
}
