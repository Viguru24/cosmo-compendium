package com.example.ui.components.costing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.RecipeEntity
import com.example.data.model.CostCurrency
import com.example.data.model.IngredientCostLine
import com.example.data.model.RecipeCostAnalysis
import com.example.data.model.RecipeCostingEngine
import com.example.ui.theme.TerracottaPrimary
import java.util.Locale

/**
 * Professional Restaurant Food Costing & Menu Pricing Spec Sheet Dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCostingDialog(
    recipe: RecipeEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedCurrency by remember { mutableStateOf(CostCurrency.EUR) }
    var portions by remember { mutableIntStateOf(4) }
    var targetFoodCostPercent by remember { mutableDoubleStateOf(30.0) }
    val customPrices = remember { mutableStateMapOf<String, Double>() }

    // Dialog state for editing individual ingredient unit price
    var editingLineItem by remember { mutableStateOf<IngredientCostLine?>(null) }
    var editPriceInput by remember { mutableStateOf("") }

    val analysis: RecipeCostAnalysis = remember(recipe, portions, targetFoodCostPercent, selectedCurrency, customPrices.toMap()) {
        RecipeCostingEngine.calculateCost(
            recipe = recipe,
            portions = portions,
            targetFoodCostPercent = targetFoodCostPercent,
            currency = selectedCurrency,
            customUnitPrices = customPrices
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFBF8F2),
            border = BorderStroke(1.5.dp, Color(0xFFD6C7B2)),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💰", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Restaurant Costing & Margins",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C1810)
                                )
                            )
                            Text(
                                text = "Professional Kitchen Food Cost & Menu Pricing Engine",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF78716C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Control Bar: Currency & Portion Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currency Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(CostCurrency.EUR, CostCurrency.USD, CostCurrency.GBP, CostCurrency.CHF).forEach { curr ->
                            val isSelected = curr == selectedCurrency
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFFEDE5D8),
                                border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else Color(0xFFD6C7B2)),
                                modifier = Modifier.clickable { selectedCurrency = curr }
                            ) {
                                Text(
                                    text = curr.symbol,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Color(0xFF443425),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Yield / Portions Stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Portions:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF5A493B),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE5D8),
                            border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { if (portions > 1) portions-- }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF5A493B), modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "$portions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE5D8),
                            border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { portions++ }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF5A493B), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Dashboard Cards Grid (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Batch Cost
                    KpiCard(
                        title = "TOTAL BATCH COST",
                        value = selectedCurrency.format(analysis.totalBatchCost),
                        subtitle = "${recipe.ingredients.size} ingredients",
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    // Card 2: Cost Per Portion
                    KpiCard(
                        title = "COST PER PORTION",
                        value = selectedCurrency.format(analysis.costPerPortion),
                        subtitle = "Target: ${String.format(Locale.US, "%.0f", targetFoodCostPercent)}% food cost",
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: Suggested Menu Price
                    KpiCard(
                        title = "SUGGESTED MENU PRICE",
                        value = selectedCurrency.format(analysis.suggestedMenuPrice),
                        subtitle = "Gross margin: ${String.format(Locale.US, "%.1f", analysis.grossMarginPercent)}%",
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    // Card 4: Gross Profit / Dish
                    KpiCard(
                        title = "GROSS PROFIT / DISH",
                        value = selectedCurrency.format(analysis.grossProfitPerPortion),
                        subtitle = "Net contribution",
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Food Cost % Slider
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF3ECE0),
                    border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Food Cost Percentage:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5A493B)
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TerracottaPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", targetFoodCostPercent)}% (Standard: 28-32%)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Slider(
                            value = targetFoodCostPercent.toFloat(),
                            onValueChange = { targetFoodCostPercent = it.toDouble() },
                            valueRange = 15f..50f,
                            steps = 34,
                            colors = SliderDefaults.colors(
                                thumbColor = TerracottaPrimary,
                                activeTrackColor = TerracottaPrimary,
                                inactiveTrackColor = Color(0xFFD6C7B2)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ingredient Line Items Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredient Cost Breakdown (${analysis.lineItems.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C1810)
                        )
                    )
                    Text(
                        text = "Tap row to customize unit price",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF78716C),
                            fontSize = 10.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Table of Ingredients
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(analysis.lineItems) { item ->
                        IngredientCostRow(
                            item = item,
                            currency = selectedCurrency,
                            onClick = {
                                editingLineItem = item
                                editPriceInput = String.format(Locale.US, "%.2f", item.unitPrice)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFD6C7B2))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val spec = analysis.generateKitchenSpecSheet()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Kitchen Cost Spec Sheet", spec)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 Costing Spec Sheet copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF9C8B77))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF5A493B))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Spec", color = Color(0xFF5A493B))
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Modal to customize unit price for an ingredient
    editingLineItem?.let { item ->
        Dialog(onDismissRequest = { editingLineItem = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFBF8F2),
                border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize Unit Price",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C1810)
                        )
                    )
                    Text(
                        text = "${item.originalIngredient.name} (${item.parsedAmount} ${item.parsedUnit})",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78716C))
                    )

                    OutlinedTextField(
                        value = editPriceInput,
                        onValueChange = { editPriceInput = it },
                        label = { Text("Price per ${item.baseUnit} (${selectedCurrency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = Color(0xFFD6C7B2)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { editingLineItem = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newPrice = editPriceInput.replace(",", ".").toDoubleOrNull()
                                if (newPrice != null && newPrice >= 0) {
                                    customPrices[item.originalIngredient.name.lowercase().trim()] = newPrice
                                }
                                editingLineItem = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Text("Save Price")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F1E7)),
        border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8C7A6B),
                    fontSize = 9.5.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 20.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF78716C),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun IngredientCostRow(
    item: IngredientCostLine,
    currency: CostCurrency,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF6F1E7),
        border = BorderStroke(0.8.dp, Color(0xFFE5DACB)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.originalIngredient.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1810),
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = "${item.parsedAmount} ${item.parsedUnit}  •  ${currency.format(item.unitPrice)}/${item.baseUnit}" +
                            if (item.wastePercent > 0) " (${item.wastePercent.toInt()}% trim)" else "",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF78716C),
                        fontSize = 11.sp
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currency.format(item.calculatedCost),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        fontSize = 13.5.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Price",
                    tint = Color(0xFF9C8B77),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
