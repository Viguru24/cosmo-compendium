package com.example.data.model

import com.example.data.local.RecipeEntity
import java.util.Locale

/**
 * Currency configuration for Restaurant Recipe Costing.
 */
enum class CostCurrency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    EUR("EUR", "€", "Euro (€)"),
    USD("USD", "$", "US Dollar ($)"),
    GBP("GBP", "£", "British Pound (£)"),
    CHF("CHF", "CHF", "Swiss Franc (CHF)"),
    AUD("AUD", "A$", "Australian Dollar (A$)"),
    CAD("CAD", "C$", "Canadian Dollar (C$)");

    fun format(amount: Double): String {
        return when (this) {
            EUR -> String.format(Locale.US, "%.2f €", amount)
            CHF -> String.format(Locale.US, "CHF %.2f", amount)
            else -> String.format(Locale.US, "%s%.2f", symbol, amount)
        }
    }
}

/**
 * Individual costing line item for an ingredient.
 */
data class IngredientCostLine(
    val originalIngredient: RecipeIngredient,
    val parsedAmount: Double,
    val parsedUnit: String,
    val unitPrice: Double,         // Price per base unit (e.g. per kg or per liter)
    val baseUnit: String,          // e.g. "kg", "L", "item"
    val wastePercent: Double = 0.0, // Kitchen prep trim / shrinkage (e.g. 10% for peeling onions)
    val calculatedCost: Double,    // Final calculated cost for recipe
    val isAutoEstimated: Boolean = true
)

/**
 * Complete restaurant financial analysis for a recipe.
 */
data class RecipeCostAnalysis(
    val recipeTitle: String,
    val portions: Int,
    val lineItems: List<IngredientCostLine>,
    val totalBatchCost: Double,
    val costPerPortion: Double,
    val targetFoodCostPercent: Double = 30.0,
    val suggestedMenuPrice: Double,
    val grossProfitPerPortion: Double,
    val grossMarginPercent: Double,
    val totalWasteCost: Double,
    val currency: CostCurrency = CostCurrency.EUR
) {
    fun generateKitchenSpecSheet(): String {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("🍽️ KITCHEN SPEC & FOOD COST SPEC SHEET")
        sb.appendLine("Recipe: $recipeTitle")
        sb.appendLine("Yield / Portions: $portions")
        sb.appendLine("Currency: ${currency.code}")
        sb.appendLine("Target Food Cost: ${String.format(Locale.US, "%.1f", targetFoodCostPercent)}%")
        sb.appendLine("------------------------------------------")
        sb.appendLine("INGREDIENT BREAKDOWN:")
        lineItems.forEach { item ->
            val wasteStr = if (item.wastePercent > 0) " (Waste: ${item.wastePercent.toInt()}%)" else ""
            sb.appendLine(
                "- ${item.originalIngredient.name}: ${item.parsedAmount} ${item.parsedUnit} " +
                        "[@ ${currency.format(item.unitPrice)}/${item.baseUnit}] = ${currency.format(item.calculatedCost)}$wasteStr"
            )
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("FINANCIAL SUMMARY:")
        sb.appendLine("Total Batch Cost:       ${currency.format(totalBatchCost)}")
        sb.appendLine("Cost Per Portion:       ${currency.format(costPerPortion)}")
        sb.appendLine("Target Food Cost %%:     ${String.format(Locale.US, "%.1f", targetFoodCostPercent)}%")
        sb.appendLine("Suggested Menu Price:   ${currency.format(suggestedMenuPrice)}")
        sb.appendLine("Gross Profit / Portion: ${currency.format(grossProfitPerPortion)} (${String.format(Locale.US, "%.1f", grossMarginPercent)}%)")
        sb.appendLine("==========================================")
        return sb.toString()
    }
}

/**
 * Intelligent Restaurant Recipe Costing Engine with built-in pantry pricing benchmarks.
 */
object RecipeCostingEngine {

    // Benchmark baseline pantry price database (per base unit: kg, L, or piece)
    private val PANTRY_BENCHMARK_PRICES = mapOf(
        // Baking & Staples (EUR / kg or L)
        "flour" to 1.39,
        "all-purpose" to 1.39,
        "weizenmehl" to 1.39,
        "mehl" to 1.39,
        "bread flour" to 1.69,
        "spelt" to 2.49,
        "dinkelmehl" to 2.49,
        "sugar" to 1.29,
        "zucker" to 1.29,
        "brown sugar" to 1.89,
        "brauner zucker" to 1.89,
        "powdered sugar" to 1.99,
        "puderzucker" to 1.99,
        "butter" to 7.90, // ~1.99 per 250g
        "yeast" to 12.00, // dry yeast
        "hefe" to 0.49, // fresh cube
        "baking powder" to 10.00,
        "backpulver" to 0.35,
        "vanilla" to 80.00, // extract
        "vanillezucker" to 0.35,
        "cocoa" to 14.00,
        "kakao" to 14.00,
        "chocolate" to 12.00,
        "schokolade" to 12.00,

        // Dairy & Liquids
        "milk" to 1.15,
        "milch" to 1.15,
        "heavy cream" to 4.90,
        "schlagsahne" to 4.90,
        "sahne" to 4.90,
        "sour cream" to 3.80,
        "schmand" to 3.80,
        "quark" to 3.20,
        "mascarpone" to 7.50,
        "olive oil" to 9.50,
        "olivenöl" to 9.50,
        "oil" to 2.50,
        "öl" to 2.50,
        "sunflower oil" to 2.30,
        "sonnenblumenöl" to 2.30,

        // Produce & Aromatics
        "egg" to 0.32, // per egg
        "ei" to 0.32,
        "onion" to 1.49,
        "zwiebel" to 1.49,
        "garlic" to 6.90,
        "knoblauch" to 6.90,
        "potato" to 1.29,
        "kartoffel" to 1.29,
        "carrot" to 1.39,
        "karotte" to 1.39,
        "möhre" to 1.39,
        "lemon" to 0.65, // per lemon
        "zitrone" to 0.65,
        "apple" to 2.49,
        "apfel" to 2.49,
        "berries" to 9.90,
        "beeren" to 9.90,
        "cherries" to 8.50,
        "kirschen" to 8.50,

        // Proteins & Meats
        "chicken breast" to 9.90,
        "hähnchenbrust" to 9.90,
        "chicken" to 8.50,
        "hähnchen" to 8.50,
        "beef" to 14.50,
        "rindfleisch" to 14.50,
        "ground beef" to 11.90,
        "hackfleisch" to 10.90,
        "pork" to 8.90,
        "schwein" to 8.90,
        "bacon" to 13.50,
        "speck" to 13.50,
        "salmon" to 22.00,
        "lachs" to 22.00,
        "parmesan" to 24.00,
        "cheese" to 11.00,
        "käse" to 11.00,

        // Grains & Pantry
        "rice" to 2.20,
        "reis" to 2.20,
        "pasta" to 1.80,
        "nudeln" to 1.80,
        "salt" to 0.89,
        "salz" to 0.89,
        "pepper" to 28.00,
        "pfeffer" to 28.00,
        "almonds" to 14.50,
        "mandeln" to 14.50,
        "walnuts" to 16.50,
        "walnüsse" to 16.50
    )

    fun calculateCost(
        recipe: RecipeEntity,
        portions: Int = extractServingsCount(recipe.servings),
        targetFoodCostPercent: Double = 30.0,
        currency: CostCurrency = CostCurrency.EUR,
        customUnitPrices: Map<String, Double> = emptyMap()
    ): RecipeCostAnalysis {
        val safePortions = portions.coerceAtLeast(1)
        val lineItems = mutableListOf<IngredientCostLine>()
        var totalBatchCost = 0.0
        var totalWasteCost = 0.0

        recipe.ingredients.forEach { ingredient ->
            val parsed = parseIngredientQuantity(ingredient.amount, ingredient.unit)
            val nameClean = ingredient.name.lowercase().trim()

            // Lookup benchmark or custom price
            val (unitPrice, baseUnit, wastePercent) = findPriceAndBaseUnit(
                name = nameClean,
                unit = parsed.second,
                customPrices = customUnitPrices
            )

            // Convert ingredient quantity into base unit (kg, L, or items)
            val quantityInBaseUnit = convertToBaseUnit(parsed.first, parsed.second, baseUnit)

            // Factor in waste / prep shrinkage
            val shrinkageFactor = if (wastePercent > 0.0) 1.0 / (1.0 - (wastePercent / 100.0)) else 1.0
            val effectiveQuantity = quantityInBaseUnit * shrinkageFactor
            val itemCost = effectiveQuantity * unitPrice

            val rawItemCost = quantityInBaseUnit * unitPrice
            val wasteItemCost = (itemCost - rawItemCost).coerceAtLeast(0.0)

            totalBatchCost += itemCost
            totalWasteCost += wasteItemCost

            lineItems.add(
                IngredientCostLine(
                    originalIngredient = ingredient,
                    parsedAmount = parsed.first,
                    parsedUnit = parsed.second,
                    unitPrice = unitPrice,
                    baseUnit = baseUnit,
                    wastePercent = wastePercent,
                    calculatedCost = itemCost,
                    isAutoEstimated = !customUnitPrices.containsKey(nameClean)
                )
            )
        }

        // Prevent zero cost divide by zero
        val safeBatchCost = totalBatchCost.coerceAtLeast(0.01)
        val costPerPortion = safeBatchCost / safePortions
        val safeTargetFoodCost = targetFoodCostPercent.coerceIn(10.0, 90.0)

        val suggestedMenuPrice = costPerPortion / (safeTargetFoodCost / 100.0)
        val grossProfitPerPortion = suggestedMenuPrice - costPerPortion
        val grossMarginPercent = (grossProfitPerPortion / suggestedMenuPrice) * 100.0

        return RecipeCostAnalysis(
            recipeTitle = recipe.title,
            portions = safePortions,
            lineItems = lineItems,
            totalBatchCost = safeBatchCost,
            costPerPortion = costPerPortion,
            targetFoodCostPercent = safeTargetFoodCost,
            suggestedMenuPrice = suggestedMenuPrice,
            grossProfitPerPortion = grossProfitPerPortion,
            grossMarginPercent = grossMarginPercent,
            totalWasteCost = totalWasteCost,
            currency = currency
        )
    }

    private fun extractServingsCount(servingsText: String): Int {
        val digits = Regex("\\d+").findAll(servingsText).mapNotNull { it.value.toIntOrNull() }.toList()
        return when {
            digits.size >= 2 -> digits[0] // e.g. "4-6 servings" -> 4
            digits.size == 1 -> digits[0]
            else -> 4
        }
    }

    private fun parseIngredientQuantity(amountStr: String, unitStr: String): Pair<Double, String> {
        val trimmedAmt = amountStr.trim().replace(",", ".")
        val amt = when {
            trimmedAmt.contains(" ") -> {
                // e.g. "1 1/2"
                val parts = trimmedAmt.split(" ")
                val whole = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val frac = parseFraction(parts.getOrNull(1).orEmpty())
                whole + frac
            }
            trimmedAmt.contains("/") -> parseFraction(trimmedAmt)
            else -> trimmedAmt.toDoubleOrNull() ?: 1.0
        }
        return Pair(if (amt <= 0.0) 1.0 else amt, unitStr.trim().lowercase())
    }

    private fun parseFraction(str: String): Double {
        val parts = str.split("/")
        if (parts.size == 2) {
            val num = parts[0].trim().toDoubleOrNull() ?: 0.0
            val den = parts[1].trim().toDoubleOrNull() ?: 1.0
            if (den != 0.0) return num / den
        }
        return 0.0
    }

    private fun findPriceAndBaseUnit(
        name: String,
        unit: String,
        customPrices: Map<String, Double>
    ): Triple<Double, String, Double> {
        // Check custom overrides first
        val custom = customPrices[name]
        if (custom != null) {
            val base = if (unit in listOf("ml", "l", "liter", "cup", "tbsp", "tsp")) "L" else "kg"
            return Triple(custom, base, 0.0)
        }

        // Check if egg or single item
        if (unit in listOf("stk", "stück", "stueck", "piece", "item", "egg", "eier") || name.contains("egg") || name.contains("ei")) {
            return Triple(0.32, "item", 0.0)
        }

        // Check produce waste factors
        val waste = when {
            name.contains("onion") || name.contains("zwiebel") -> 12.0
            name.contains("garlic") || name.contains("knoblauch") -> 15.0
            name.contains("potato") || name.contains("kartoffel") -> 15.0
            name.contains("chicken") || name.contains("rind") || name.contains("beef") -> 8.0
            else -> 0.0
        }

        // Match benchmark database
        for ((key, price) in PANTRY_BENCHMARK_PRICES) {
            if (name.contains(key)) {
                val base = if (key in listOf("milk", "milch", "cream", "sahne", "oil", "öl")) "L" else if (key in listOf("egg", "ei", "lemon", "zitrone")) "item" else "kg"
                return Triple(price, base, waste)
            }
        }

        // Generic fallback defaults
        val isLiquid = unit in listOf("ml", "l", "liter", "cup", "tbsp", "tsp")
        val baseUnit = if (isLiquid) "L" else "kg"
        val defaultPrice = if (isLiquid) 2.50 else 3.50

        return Triple(defaultPrice, baseUnit, waste)
    }

    private fun convertToBaseUnit(amount: Double, unit: String, baseUnit: String): Double {
        val u = unit.lowercase().replace(".", "")
        return when (baseUnit) {
            "kg" -> when (u) {
                "g", "gram", "grams", "gramm" -> amount / 1000.0
                "kg", "kilogram" -> amount
                "oz", "ounce" -> amount * 0.0283495
                "lb", "lbs", "pound", "pounds" -> amount * 0.453592
                "tbsp", "el", "esslöffel" -> (amount * 15.0) / 1000.0 // approx 15g per tbsp
                "tsp", "tl", "teelöffel" -> (amount * 5.0) / 1000.0
                "pinch", "prise" -> 0.001 // 1g
                "dash", "msp", "messerspitze" -> 0.001
                "cup", "tasse", "cups" -> (amount * 140.0) / 1000.0 // approx 140g flour per cup
                "pck", "pkg", "päckchen" -> (amount * 16.0) / 1000.0 // standard 16g Dr Oetker
                else -> amount / 1000.0
            }
            "L" -> when (u) {
                "ml", "milliliter" -> amount / 1000.0
                "l", "liter", "litre" -> amount
                "cl" -> amount / 100.0
                "dl" -> amount / 10.0
                "cup", "tasse", "cups" -> amount * 0.240
                "tbsp", "el", "esslöffel" -> amount * 0.015
                "tsp", "tl", "teelöffel" -> amount * 0.005
                "fl oz", "floz" -> amount * 0.0295735
                "schuss", "splash" -> 0.020
                "tropfen", "drops" -> 0.001
                else -> amount / 1000.0
            }
            "item" -> amount
            else -> amount
        }
    }
}
