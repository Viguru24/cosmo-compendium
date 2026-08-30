package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeIngredient(
    val name: String,
    val amount: String = "",
    val unit: String = "",
    val nameGerman: String? = null,
    val nameEnglish: String? = null,
    val isOptional: Boolean = false,
    val group: String? = null // e.g. "For the Dough", "Für den Teig", "Filling"
) {
    fun getDisplayName(language: LanguageMode = LanguageMode.ENGLISH): String {
        val en = nameEnglish?.takeIf { it.isNotBlank() } ?: name
        val raw = if (en.contains("/")) {
            en.split("/").firstOrNull()?.trim() ?: en
        } else {
            en
        }
        return cleanIngredientName(raw)
    }

    companion object {
        fun cleanIngredientName(name: String): String {
            return name
                .replace(Regex("(?i)\\btipo,\\s*0,\\s*0\\b"), "Tipo 00")
                .replace(Regex("(?i)\\btipo\\s+0\\s+0\\b"), "Tipo 00")
                .replace(Regex("(?i)\\btype,\\s*0,\\s*0\\b"), "Type 00")
                .replace(Regex("(?i)\\bflower\\b"), "flour")
                .replace(Regex("(?i)\\btipo\\s*00\\s*flour\\b"), "Tipo 00 Flour")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }

    fun getLocalizedGroup(language: LanguageMode = LanguageMode.ENGLISH): String? {
        val g = group ?: return null
        if (g.contains("/")) {
            return g.split("/").firstOrNull()?.trim() ?: g
        }
        return g
    }

    private fun cleanToLanguage(text: String, targetGerman: Boolean): String {
        if (!text.contains("/")) return text
        val parts = text.split("/").map { it.trim() }
        return if (targetGerman) {
            parts.lastOrNull()?.ifBlank { parts.firstOrNull() } ?: text
        } else {
            parts.firstOrNull()?.ifBlank { parts.lastOrNull() } ?: text
        }
    }

    /**
     * Converts the ingredient amount and unit according to the selected measuring style:
     * - METRIC_GRAMS: g, kg, ml, l
     * - CUPS_US: cups, tbsp, tsp, oz, lbs, fl oz
     * - UK_IMPERIAL: Funneled UK Blueprint (UK spoons for <15g/<15ml, Grams for dry/solids, ml for liquids, °C / Gas Mark)
     * - BAKERS_PRECISION: exact decimal grams (e.g. 250.0 g)
     */
    fun getConvertedAmount(targetSystem: UnitSystem, multiplier: Float = 1.0f): String {
        if (amount.isBlank()) return ""
        val numericAmount = parseAmountToDouble(amount) ?: return if (multiplier != 1.0f) "$amount (x$multiplier)" else amount
        val scaled = numericAmount * multiplier
        val u = unit.lowercase().trim()
        val itemName = (nameEnglish ?: name).lowercase()

        // Comprehensive Density factors (grams per 1 cup / ~240ml):
        val density = when {
            itemName.contains("powdered sugar") || itemName.contains("puderzucker") || itemName.contains("icing sugar") -> 120.0
            itemName.contains("flour") || itemName.contains("mehl") || itemName.contains("stärke") || itemName.contains("starch") || itemName.contains("cornstarch") || itemName.contains("cornflour") -> 125.0
            itemName.contains("brown sugar") || itemName.contains("brauner zucker") || itemName.contains("muscovado") -> 220.0
            itemName.contains("sugar") || itemName.contains("zucker") || itemName.contains("caster") || itemName.contains("castor") -> 200.0
            itemName.contains("butter") || itemName.contains("margarine") -> 227.0
            itemName.contains("cocoa") || itemName.contains("kakao") -> 100.0
            itemName.contains("honey") || itemName.contains("honig") || itemName.contains("syrup") || itemName.contains("sirup") || itemName.contains("treacle") || itemName.contains("molasses") -> 340.0
            itemName.contains("bread crumb") || itemName.contains("semmelbrösel") || itemName.contains("breadcrumbs") || itemName.contains("paniermehl") -> 110.0
            itemName.contains("oat") || itemName.contains("haferflocken") || itemName.contains("porridge") -> 90.0
            itemName.contains("nut") || itemName.contains("mandel") || itemName.contains("nuss") || itemName.contains("almond") || itemName.contains("hazelnut") || itemName.contains("walnut") -> 100.0
            itemName.contains("chocolate chip") || itemName.contains("schokotropfen") -> 170.0
            itemName.contains("raisin") || itemName.contains("rosinen") || itemName.contains("sultana") || itemName.contains("craisin") -> 150.0
            else -> 240.0 // standard liquid
        }

        return when (targetSystem) {
            UnitSystem.UK_IMPERIAL -> {
                convertToUkFormat(scaled, u, itemName, density)
            }

            UnitSystem.METRIC_GRAMS -> {
                when (u) {
                    "cup", "cups", "tasse", "tassen" -> {
                        if (isLiquid(itemName)) {
                            "${formatScaledNumber(scaled * 240.0)} ml"
                        } else {
                            "${formatScaledNumber(scaled * density)} g"
                        }
                    }
                    "stick", "sticks" -> {
                        val grams = scaled * 113.4
                        "${roundTo5(grams).toInt()} g"
                    }
                    "tbsp", "tablespoon", "tablespoons", "el", "esslöffel" -> {
                        if (isLiquid(itemName)) "${formatScaledNumber(scaled * 15.0)} ml" else "${formatScaledNumber(scaled * (density / 16.0))} g"
                    }
                    "tsp", "teaspoon", "teaspoons", "tl", "teelöffel" -> {
                        if (isLiquid(itemName)) "${formatScaledNumber(scaled * 5.0)} ml" else "${formatScaledNumber(scaled * (density / 48.0))} g"
                    }
                    "oz", "ounce", "ounces" -> "${formatScaledNumber(scaled * 28.3495)} g"
                    "fl oz", "fluid ounce" -> "${formatScaledNumber(scaled * 29.57)} ml"
                    "lb", "lbs", "pound", "pounds" -> {
                        val grams = scaled * 453.592
                        if (grams >= 1000) "${formatScaledNumber(grams / 1000.0)} kg" else "${formatScaledNumber(grams)} g"
                    }
                    "g", "gram", "grams", "gramm" -> {
                        if (scaled >= 1000) "${formatScaledNumber(scaled / 1000.0)} kg" else "${formatScaledNumber(scaled)} g"
                    }
                    "kg" -> "${formatScaledNumber(scaled)} kg"
                    "ml" -> "${formatScaledNumber(scaled)} ml"
                    "l", "liter", "litre" -> "${formatScaledNumber(scaled)} l"
                    "pinch", "prise", "msp.", "messerspitze" -> if (scaled > 1.5) "2 Prisen" else "1 Prise"
                    "pck.", "päckchen", "packet", "packets" -> "${formatScaledNumber(scaled)} Pck."
                    else -> "${formatScaledNumber(scaled)} $unit".trim()
                }
            }

            UnitSystem.CUPS_US -> {
                when (u) {
                    "g", "gram", "grams", "gramm" -> {
                        if (isLiquid(itemName)) {
                            formatCupsFromMl(scaled)
                        } else {
                            val cups = scaled / density
                            if (cups >= 0.2) {
                                "${formatFraction(cups)} cups"
                            } else {
                                val tbsp = scaled / (density / 16.0)
                                if (tbsp >= 0.9) {
                                    "${formatFraction(tbsp)} tbsp"
                                } else {
                                    val tsp = scaled / (density / 48.0)
                                    if (tsp <= 0.35 && scaled <= 1.0) {
                                        "1 pinch"
                                    } else {
                                        "${formatFraction(tsp)} tsp"
                                    }
                                }
                            }
                        }
                    }
                    "kg" -> "${formatScaledNumber(scaled * 2.20462)} lbs"
                    "ml" -> formatCupsFromMl(scaled)
                    "l", "liter", "litre" -> "${formatScaledNumber(scaled * 4.22675)} cups"
                    "el", "esslöffel" -> "${formatScaledNumber(scaled)} tbsp"
                    "tl", "teelöffel" -> "${formatScaledNumber(scaled)} tsp"
                    "tasse", "tassen" -> "${formatFraction(scaled)} cups"
                    "cup", "cups" -> "${formatFraction(scaled)} cups"
                    "stick", "sticks" -> "${formatFraction(scaled)} stick${if (scaled > 1) "s" else ""}"
                    "tbsp", "tablespoon", "tablespoons" -> "${formatFraction(scaled)} tbsp"
                    "tsp", "teaspoon", "teaspoons" -> "${formatFraction(scaled)} tsp"
                    "oz", "ounce", "ounces" -> "${formatScaledNumber(scaled)} oz"
                    "fl oz" -> "${formatScaledNumber(scaled)} fl oz"
                    "lb", "lbs" -> "${formatScaledNumber(scaled)} lbs"
                    "pinch", "prise", "msp." -> if (scaled > 1.5) "2 pinches" else "1 pinch"
                    "pck.", "päckchen", "packet" -> "${formatScaledNumber(scaled)} packet"
                    else -> "${formatScaledNumber(scaled)} $unit".trim()
                }
            }

            UnitSystem.BAKERS_PRECISION -> {
                when (u) {
                    "cup", "cups", "tasse" -> {
                        val grams = if (isLiquid(itemName)) scaled * 240.0 else scaled * density
                        "${formatScaledNumber(grams)} g"
                    }
                    "stick", "sticks" -> "${formatScaledNumber(scaled * 113.4)} g"
                    "tbsp", "tablespoon", "el" -> {
                        val grams = if (isLiquid(itemName)) scaled * 15.0 else scaled * (density / 16.0)
                        "${formatScaledNumber(grams)} g"
                    }
                    "tsp", "teaspoon", "tl" -> {
                        val grams = if (isLiquid(itemName)) scaled * 5.0 else scaled * (density / 48.0)
                        "${formatScaledNumber(grams)} g"
                    }
                    "oz", "ounce" -> "${formatScaledNumber(scaled * 28.3495)} g"
                    "lb", "lbs" -> "${formatScaledNumber(scaled * 453.592)} g"
                    "kg" -> "${formatScaledNumber(scaled * 1000.0)} g"
                    "g", "gram", "gramm" -> "${formatScaledNumber(scaled)} g"
                    "ml" -> "${formatScaledNumber(scaled)} ml"
                    "l" -> "${formatScaledNumber(scaled * 1000.0)} ml"
                    else -> "${formatScaledNumber(scaled)} $unit".trim()
                }
            }
        }
    }

    /**
     * UK Measurement Logic Blueprint implementation:
     * - Checks if amount is small (< 15ml / < 15g) -> funnels into standardized UK Spoons (tsp, tbsp)
     * - Solid/dry/sticky ingredients -> Grams (g) or Kilograms (kg) rounded to natural 5g/10g kitchen scale units
     * - Liquid ingredients (> 15ml) -> Millilitres (ml) or Litres (L)
     * - Butter sticks -> 115g UK standard block
     * - 1 Cup liquid -> 250ml
     * - 1 Cup dry -> grams by ingredient density (~125g flour, ~200g sugar)
     */
    private fun convertToUkFormat(scaled: Double, u: String, itemName: String, density: Double): String {
        val liquid = isLiquid(itemName)

        // 1. Butter Stick translation: 1 stick = 115g standard UK block
        if (u.contains("stick")) {
            val butterGrams = scaled * 115.0
            return if (butterGrams <= 15.0) {
                formatUkSpoon(butterGrams, isLiquid = false)
            } else {
                "${roundTo5(butterGrams).toInt()} g"
            }
        }

        // 2. Direct Spoons handling
        if (u in listOf("tsp", "teaspoon", "teaspoons", "tl", "teelöffel")) {
            return if (scaled >= 3.0) {
                "${formatFraction(scaled / 3.0)} tbsp"
            } else {
                "${formatFraction(scaled)} tsp"
            }
        }
        if (u in listOf("tbsp", "tablespoon", "tablespoons", "el", "esslöffel")) {
            return if (scaled <= 3.0) {
                "${formatFraction(scaled)} tbsp"
            } else {
                val metricEquivalent = if (liquid) scaled * 15.0 else scaled * (density / 16.0)
                if (liquid) "${roundTo5(metricEquivalent).toInt()} ml" else "${roundTo5(metricEquivalent).toInt()} g"
            }
        }
        if (u in listOf("dstsp", "dessertspoon", "dessertspoons")) {
            return "${formatFraction(scaled)} dstsp"
        }
        if (u in listOf("pinch", "prise", "msp.", "messerspitze")) {
            return if (scaled > 1.5) "2 pinches" else "1 pinch"
        }
        if (u in listOf("pck.", "päckchen", "packet", "packets", "sachet", "sachets")) {
            return "${formatScaledNumber(scaled)} sachet${if (scaled > 1) "s" else ""}"
        }

        // Calculate metric equivalents (weight or volume)
        var metricMl: Double? = null
        var metricGrams: Double? = null

        when (u) {
            "cup", "cups", "tasse", "tassen" -> {
                if (liquid) {
                    metricMl = scaled * 250.0 // UK Standard Metric Cup (250 ml)
                } else {
                    metricGrams = scaled * density // e.g. 125g flour, 200g sugar, 227g butter
                }
            }
            "g", "gram", "grams", "gramm" -> {
                metricGrams = scaled
            }
            "kg" -> {
                metricGrams = scaled * 1000.0
            }
            "ml" -> {
                metricMl = scaled
            }
            "l", "liter", "litre" -> {
                metricMl = scaled * 1000.0
            }
            "oz", "ounce", "ounces" -> {
                if (liquid) {
                    metricMl = scaled * 28.413 // UK fluid ounce
                } else {
                    metricGrams = scaled * 28.3495
                }
            }
            "fl oz", "fluid ounce", "fluid ounces" -> {
                metricMl = scaled * 28.413
            }
            "pt", "pint", "pints" -> {
                metricMl = scaled * 568.261 // UK Imperial Pint (568ml)
            }
            "lb", "lbs", "pound", "pounds" -> {
                metricGrams = scaled * 453.592
            }
            else -> {
                // Non-convertible count units (eggs, cloves, slices, pieces)
                return "${formatScaledNumber(scaled)} $u".trim()
            }
        }

        // Apply Decision Flow:
        // [ Is the Ingredient Amount Small? (< 15ml / < 15g) ]
        if (metricMl != null) {
            return if (metricMl <= 15.0) {
                formatUkSpoon(metricMl, isLiquid = true)
            } else if (metricMl >= 1000.0) {
                "${formatScaledNumber(metricMl / 1000.0)} L"
            } else {
                "${roundTo5(metricMl).toInt()} ml"
            }
        }

        if (metricGrams != null) {
            return if (metricGrams <= 15.0) {
                formatUkSpoon(metricGrams, isLiquid = false)
            } else if (metricGrams >= 1000.0) {
                "${formatScaledNumber(metricGrams / 1000.0)} kg"
            } else {
                "${roundTo5(metricGrams).toInt()} g"
            }
        }

        return "${formatScaledNumber(scaled)} $u".trim()
    }

    /**
     * Standardizes small quantities into friendly UK Spoons (avoiding frustrating "3g salt" or "5ml vanilla").
     */
    private fun formatUkSpoon(metricAmt: Double, isLiquid: Boolean): String {
        return when {
            metricAmt <= 0.8 -> "1 pinch"
            metricAmt <= 1.8 -> "¼ tsp"
            metricAmt <= 3.2 -> "½ tsp"
            metricAmt <= 4.2 -> "¾ tsp"
            metricAmt <= 6.5 -> "1 tsp"
            metricAmt <= 8.5 -> "1½ tsp"
            metricAmt <= 12.0 -> "2 tsp"
            metricAmt <= 18.0 -> "1 tbsp"
            metricAmt <= 25.0 -> "1½ tbsp"
            metricAmt <= 35.0 -> "2 tbsp"
            else -> {
                if (isLiquid) "${roundTo5(metricAmt).toInt()} ml"
                else "${roundTo5(metricAmt).toInt()} g"
            }
        }
    }

    private fun roundTo5(num: Double): Double {
        return (Math.round(num / 5.0) * 5).toDouble()
    }

    private fun isLiquid(name: String): Boolean {
        val n = name.lowercase()
        return n.contains("water") || n.contains("wasser") ||
                n.contains("milk") || n.contains("milch") ||
                n.contains("oil") || n.contains("öl") ||
                n.contains("cream") || n.contains("sahne") ||
                n.contains("juice") || n.contains("saft") ||
                n.contains("wine") || n.contains("wein") ||
                n.contains("cider") || n.contains("beer") || n.contains("bier") ||
                n.contains("broth") || n.contains("brühe") || n.contains("stock") || n.contains("fond") ||
                n.contains("vinegar") || n.contains("essig") ||
                n.contains("rum") || n.contains("kirschwasser") || n.contains("liqueur") ||
                n.contains("coffee") || n.contains("kaffee") || n.contains("tea") || n.contains("tee")
    }

    private fun formatCupsFromMl(ml: Double): String {
        val cups = ml / 240.0
        return when {
            cups >= 0.2 -> "${formatFraction(cups)} cups"
            ml >= 15.0 -> "${formatScaledNumber(ml / 15.0)} tbsp"
            else -> "${formatScaledNumber(ml / 5.0)} tsp"
        }
    }

    private fun parseAmountToDouble(amt: String): Double? {
        val clean = amt.trim().replace(",", ".").replace("½", "0.5").replace("¼", "0.25").replace("¾", "0.75").replace("⅓", "0.33").replace("⅔", "0.67")
        if (clean.contains("/")) {
            val parts = clean.split("/")
            if (parts.size == 2) {
                val num = parts[0].trim().toDoubleOrNull()
                val den = parts[1].trim().toDoubleOrNull()
                if (num != null && den != null && den != 0.0) return num / den
            }
        }
        if (clean.contains(" ")) {
            val parts = clean.split(" ")
            if (parts.size == 2) {
                val whole = parts[0].toDoubleOrNull()
                if (parts[1].contains("/")) {
                    val fracParts = parts[1].split("/")
                    val num = fracParts[0].toDoubleOrNull()
                    val den = fracParts.getOrNull(1)?.toDoubleOrNull()
                    if (whole != null && num != null && den != null && den != 0.0) {
                        return whole + (num / den)
                    }
                }
            }
        }
        return clean.toDoubleOrNull()
    }

    private fun formatScaledNumber(num: Double): String {
        val rounded = Math.round(num * 100.0) / 100.0
        return if (Math.abs(rounded - Math.round(rounded)) < 0.001) {
            Math.round(rounded).toInt().toString()
        } else if (Math.abs(rounded * 10.0 - Math.round(rounded * 10.0)) < 0.001) {
            String.format(java.util.Locale.US, "%.1f", rounded)
        } else {
            String.format(java.util.Locale.US, "%.2f", rounded)
        }
    }

    private fun formatFraction(num: Double): String {
        val whole = num.toInt()
        val frac = num - whole
        val fracStr = when {
            frac in 0.15..0.29 -> "1/4"
            frac in 0.30..0.40 -> "1/3"
            frac in 0.41..0.59 -> "1/2"
            frac in 0.60..0.70 -> "2/3"
            frac in 0.71..0.85 -> "3/4"
            else -> ""
        }
        return when {
            whole > 0 && fracStr.isNotEmpty() -> "$whole $fracStr"
            whole > 0 && fracStr.isEmpty() -> formatScaledNumber(num)
            fracStr.isNotEmpty() -> fracStr
            else -> formatScaledNumber(num)
        }
    }
}

data class GlossaryItem(
    val germanName: String,
    val englishName: String,
    val substitutes: List<String>,
    val description: String,
    val culinaryTip: String
) {
    fun getLocalizedDescription(language: LanguageMode): String {
        return description
    }
}

object GermanCulinaryGlossary {
    val items = listOf(
        GlossaryItem(
            germanName = "Quark",
            englishName = "Quark (German curd cheese)",
            substitutes = listOf("Greek Yogurt + Ricotta (1:1 ratio)", "Sour cream + Cream cheese (whipped)", "Fromage Blanc"),
            description = "A smooth, mildly tart fresh dairy cheese staple in German baking and cheesecakes (Käsekuchen).",
            culinaryTip = "Strain Greek yogurt in a cheesecloth for 2 hours to achieve authentic German Quark density."
        ),
        GlossaryItem(
            germanName = "Speisestärke",
            englishName = "Cornstarch / Potato starch",
            substitutes = listOf("Cornstarch (1:1)", "Potato starch", "Arrowroot powder", "Tapioca starch"),
            description = "Fine pure starch used in German baking (Mondamin) to make cakes velvety and sauces glossy.",
            culinaryTip = "Always dissolve in cold liquid before stirring into hot soups or fruit compotes (Rote Grütze)."
        ),
        GlossaryItem(
            germanName = "Vanillezucker",
            englishName = "Vanilla Sugar",
            substitutes = listOf("1 tsp pure vanilla extract + 1 tbsp sugar", "Vanilla bean paste", "Home-infused vanilla bean sugar"),
            description = "Pre-packaged vanilla scented sugar packets (usually 8g) standard in every German baked good.",
            culinaryTip = "Make your own by burying spent dried vanilla bean pods in a jar of granulated sugar for 2 weeks."
        ),
        GlossaryItem(
            germanName = "Kirschwasser",
            englishName = "Kirsch / Clear Cherry Schnapps",
            substitutes = listOf("Cherry juice + 1/2 tsp almond extract", "Maraschino liqueur", "Brandy or Rum"),
            description = "Double-distilled, clear tart cherry spirit from the Black Forest. Essential for authentic Schwarzwälder Kirschtorte.",
            culinaryTip = "Use pure tart cherry juice reduced with a touch of sugar for a completely alcohol-free version."
        ),
        GlossaryItem(
            germanName = "Hirschhornsalz",
            englishName = "Baker's Ammonia (Ammonium Carbonate)",
            substitutes = listOf("3/4 tsp Baking powder + 1/4 tsp Baking soda", "Double-acting baking powder"),
            description = "Traditional leavening salt used in crisp German holiday cookies like Lebkuchen and Springerle.",
            culinaryTip = "Only use for flat, dry cookies so the strong aroma completely evaporates during baking."
        ),
        GlossaryItem(
            germanName = "Backpulver",
            englishName = "German Single-Acting Baking Powder (Backin)",
            substitutes = listOf("Standard Double-acting Baking powder (1:1)", "1/4 tsp baking soda + 1/2 tsp cream of tartar"),
            description = "Sold in small single-bake 16g sachets (Dr. Oetker style), formulated for 500g of flour.",
            culinaryTip = "One German packet (16g) equals roughly 3 to 4 teaspoons of US baking powder."
        ),
        GlossaryItem(
            germanName = "Semmelbrösel / Paniermehl",
            englishName = "Fine Breadcrumbs",
            substitutes = listOf("Panko breadcrumbs (crushed finely)", "Dry toasted baguette crumbs", "Matzo meal"),
            description = "Finely ground stale bread rolls used for crispy Schnitzel coatings and dumpling binders.",
            culinaryTip = "Do not press breadcrumbs firmly onto Schnitzel; gentle airy coating creates signature soufflé bubbling."
        ),
        GlossaryItem(
            germanName = "Preiselbeeren",
            englishName = "Lingonberry Compote / Wild Cranberries",
            substitutes = listOf("Whole cranberry sauce + hint of lemon", "Redcurrant jelly", "Pomegranate molasses"),
            description = "Tart wild berries traditionally served alongside Viennese Schnitzel, venison, and Camembert.",
            culinaryTip = "Warm gently with a squeeze of fresh orange juice to drizzle over savory roasted meats."
        )
    )

    fun findSubstitute(query: String): GlossaryItem? {
        val q = query.lowercase().trim()
        return items.firstOrNull {
            it.germanName.lowercase().contains(q) ||
            q.contains(it.germanName.lowercase()) ||
            it.englishName.lowercase().contains(q)
        }
    }
}

@JsonClass(generateAdapter = true)
data class RecipeStep(
    val stepNumber: Int,
    val instructionEnglish: String,
    val instructionGerman: String = "",
    val timerMinutes: Int = 0,
    val tip: String? = null
) {
    fun getInstruction(language: LanguageMode = LanguageMode.ENGLISH, unitSystem: UnitSystem? = null): String {
        val base = instructionEnglish.ifBlank { instructionGerman }
        return if (unitSystem != null) {
            CulinaryTemperatureConverter.formatTemperatures(base, unitSystem)
        } else base
    }

    fun getLocalizedTip(language: LanguageMode = LanguageMode.ENGLISH): String? {
        val t = tip ?: return null
        if (!t.contains("||")) return t
        val parts = t.split("||").map { it.trim() }
        return parts.firstOrNull() ?: t
    }
}

/**
 * Intelligent temperature converter translating Fahrenheit/Celsius/Gas Mark for UK and global baking standards.
 */
object CulinaryTemperatureConverter {
    fun formatTemperatures(text: String, unitSystem: UnitSystem): String {
        val regexF = Regex("(?i)\\b(\\d{3})\\s*(?:°\\s*F|degrees?\\s*F(?:ahrenheit)?|F\\b)")
        val regexC = Regex("(?i)\\b(\\d{2,3})\\s*(?:°\\s*C|degrees?\\s*C(?:elsius)?|C\\b)")

        var result = text

        when (unitSystem) {
            UnitSystem.UK_IMPERIAL -> {
                result = regexF.replace(result) { match ->
                    val fVal = match.groupValues[1].toIntOrNull()
                    if (fVal != null && fVal in 200..550) {
                        val cVal = Math.round((fVal - 32) * 5.0 / 9.0).toInt()
                        val cRounded = (Math.round(cVal / 5.0) * 5).toInt()
                        val gasMark = getGasMark(cRounded)
                        "$cRounded°C / $gasMark (${fVal}°F)"
                    } else match.value
                }
            }
            UnitSystem.METRIC_GRAMS, UnitSystem.BAKERS_PRECISION -> {
                result = regexF.replace(result) { match ->
                    val fVal = match.groupValues[1].toIntOrNull()
                    if (fVal != null && fVal in 200..550) {
                        val cVal = Math.round((fVal - 32) * 5.0 / 9.0).toInt()
                        val cRounded = (Math.round(cVal / 5.0) * 5).toInt()
                        "$cRounded°C (${fVal}°F)"
                    } else match.value
                }
            }
            UnitSystem.CUPS_US -> {
                result = regexC.replace(result) { match ->
                    val cVal = match.groupValues[1].toIntOrNull()
                    if (cVal != null && cVal in 100..300) {
                        val fVal = Math.round((cVal * 9.0 / 5.0) + 32).toInt()
                        val fRounded = (Math.round(fVal / 5.0) * 5).toInt()
                        "$fRounded°F (${cVal}°C)"
                    } else match.value
                }
            }
        }
        return result
    }

    fun getGasMark(celsius: Int): String {
        return when {
            celsius < 125 -> "Gas Mark ½"
            celsius in 125..145 -> "Gas Mark 1"
            celsius in 146..160 -> "Gas Mark 2"
            celsius in 161..175 -> "Gas Mark 3"
            celsius in 176..185 -> "Gas Mark 4"
            celsius in 186..195 -> "Gas Mark 5"
            celsius in 196..210 -> "Gas Mark 6"
            celsius in 211..225 -> "Gas Mark 7"
            celsius in 226..240 -> "Gas Mark 8"
            celsius in 241..255 -> "Gas Mark 9"
            else -> "Gas Mark 10"
        }
    }
}

enum class LanguageMode(val label: String, val flag: String, val description: String) {
    ENGLISH("English", "🇬🇧", "View all recipes, ingredients, instructions and menus in English")
}

enum class UnitSystem(val label: String, val shortLabel: String, val icon: String, val description: String) {
    CUPS_US("US Cups & Spoons", "Cups / Spoons", "🥣", "Cups, tablespoons (tbsp), teaspoons (tsp), oz, lbs, °F"),
    METRIC_GRAMS("Metric Weights & Volume", "Metric (g, ml)", "⚖️", "Grams (g), kilograms (kg), milliliters (ml), liters (l), °C"),
    UK_IMPERIAL("UK Kitchen Standard", "UK (g, ml, Spoons)", "🇬🇧", "Grams (g), millilitres (ml), UK spoons (tsp/tbsp), °C & Gas Mark"),
    BAKERS_PRECISION("Baker's Precision Grams", "Baker's Grams", "🧑‍🍳", "Exact decimal grams (e.g. 250.0g) for precision weighing")
}

enum class CoverTheme(val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    VINTAGE_LEATHER("Vintage Leather", 0xFF78350F, 0xFF451A03),
    WARM_TERRACOTTA("Warm Terracotta", 0xFF9A3412, 0xFFC2410C),
    FOREST_SAGE("Bavarian Forest", 0xFF14532D, 0xFF166534),
    FLORAL_LINEN("Antique Linen", 0xFFB45309, 0xFFD97706),
    GOLDEN_PARCHMENT("Golden Heritage", 0xFF854D0E, 0xFFA16207)
}
