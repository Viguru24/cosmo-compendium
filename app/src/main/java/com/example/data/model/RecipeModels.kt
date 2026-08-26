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
        if (en.contains("/")) {
            return en.split("/").firstOrNull()?.trim() ?: en
        }
        return en
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
     * - GERMAN_TRADITIONAL: Gramm, EL (Esslöffel), TL (Teelöffel), Prise, Msp., Pck., Tasse
     * - UK_IMPERIAL: oz, lbs, fl oz, pt, tbsp, tsp
     * - BAKERS_PRECISION: exact decimal grams (e.g. 250.0 g)
     */
    fun getConvertedAmount(targetSystem: UnitSystem, multiplier: Float = 1.0f): String {
        if (amount.isBlank()) return ""
        val numericAmount = parseAmountToDouble(amount) ?: return if (multiplier != 1.0f) "$amount (x$multiplier)" else amount
        val scaled = numericAmount * multiplier
        val u = unit.lowercase().trim()
        val itemName = (nameEnglish ?: name).lowercase()

        // Approximate density factors (grams per 1 cup / 240ml):
        // Flour ~ 125g, Sugar ~ 200g, Brown Sugar ~ 220g, Butter ~ 227g, Powdered Sugar ~ 120g, Liquids ~ 240g
        val density = when {
            itemName.contains("powdered sugar") || itemName.contains("puderzucker") -> 120.0
            itemName.contains("flour") || itemName.contains("mehl") || itemName.contains("stärke") || itemName.contains("starch") -> 125.0
            itemName.contains("brown sugar") || itemName.contains("brauner zucker") -> 220.0
            itemName.contains("sugar") || itemName.contains("zucker") -> 200.0
            itemName.contains("butter") || itemName.contains("margarine") -> 227.0
            itemName.contains("cocoa") || itemName.contains("kakao") -> 100.0
            itemName.contains("honey") || itemName.contains("honig") || itemName.contains("syrup") || itemName.contains("sirup") -> 340.0
            itemName.contains("bread crumb") || itemName.contains("semmelbrösel") -> 110.0
            itemName.contains("oat") || itemName.contains("haferflocken") -> 90.0
            itemName.contains("nut") || itemName.contains("mandel") || itemName.contains("nuss") -> 120.0
            else -> 240.0 // standard liquid
        }

        return when (targetSystem) {
            UnitSystem.METRIC_GRAMS -> {
                when (u) {
                    "cup", "cups", "tasse", "tassen" -> {
                        if (isLiquid(itemName)) {
                            "${(scaled * 240).toInt()} ml"
                        } else {
                            "${(scaled * density).toInt()} g"
                        }
                    }
                    "tbsp", "tablespoon", "tablespoons", "el", "esslöffel" -> {
                        if (isLiquid(itemName)) "${(scaled * 15).toInt()} ml" else "${(scaled * (density / 16.0)).toInt().coerceAtLeast(1)} g"
                    }
                    "tsp", "teaspoon", "teaspoons", "tl", "teelöffel" -> {
                        if (isLiquid(itemName)) "${(scaled * 5).toInt()} ml" else "${(scaled * (density / 48.0)).toInt().coerceAtLeast(1)} g"
                    }
                    "oz", "ounce", "ounces" -> "${(scaled * 28.3495).toInt()} g"
                    "fl oz", "fluid ounce" -> "${(scaled * 29.57).toInt()} ml"
                    "lb", "lbs", "pound", "pounds" -> {
                        val grams = scaled * 453.592
                        if (grams >= 1000) "${String.format("%.1f", grams / 1000.0)} kg" else "${grams.toInt()} g"
                    }
                    "g", "gram", "grams", "gramm" -> {
                        if (scaled >= 1000) "${String.format("%.1f", scaled / 1000.0)} kg" else "${scaled.toInt()} g"
                    }
                    "kg" -> "${String.format("%.1f", scaled)} kg"
                    "ml" -> "${scaled.toInt()} ml"
                    "l", "liter", "litre" -> "${String.format("%.1f", scaled)} l"
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
                    "kg" -> "${String.format("%.1f", scaled * 2.20462)} lbs"
                    "ml" -> formatCupsFromMl(scaled)
                    "l", "liter", "litre" -> "${String.format("%.1f", scaled * 4.22675)} cups"
                    "el", "esslöffel" -> "${formatScaledNumber(scaled)} tbsp"
                    "tl", "teelöffel" -> "${formatScaledNumber(scaled)} tsp"
                    "tasse", "tassen" -> "${formatFraction(scaled)} cups"
                    "cup", "cups" -> "${formatFraction(scaled)} cups"
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

            UnitSystem.UK_IMPERIAL -> {
                when (u) {
                    "g", "gram", "grams", "gramm" -> {
                        val oz = scaled / 28.3495
                        if (oz >= 16.0) {
                            "${String.format("%.1f", oz / 16.0)} lbs"
                        } else {
                            "${String.format("%.1f", oz)} oz"
                        }
                    }
                    "kg" -> "${String.format("%.1f", scaled * 2.20462)} lbs"
                    "ml" -> {
                        val flOz = scaled / 28.413
                        if (flOz >= 20.0) {
                            "${String.format("%.1f", flOz / 20.0)} pt (pints)"
                        } else if (flOz >= 1.0) {
                            "${String.format("%.1f", flOz)} fl oz"
                        } else {
                            "${(scaled / 5.0).toInt()} tsp"
                        }
                    }
                    "l", "liter" -> "${String.format("%.1f", scaled * 1.75975)} pt"
                    "cup", "cups" -> "${formatFraction(scaled)} cups"
                    "tbsp", "tablespoon", "el" -> "${formatScaledNumber(scaled)} tbsp"
                    "tsp", "teaspoon", "tl" -> "${formatScaledNumber(scaled)} tsp"
                    "oz", "ounce" -> "${formatScaledNumber(scaled)} oz"
                    "lb", "lbs" -> "${formatScaledNumber(scaled)} lbs"
                    "fl oz" -> "${formatScaledNumber(scaled)} fl oz"
                    "pinch", "prise" -> if (scaled > 1.5) "2 pinches" else "1 pinch"
                    else -> "${formatScaledNumber(scaled)} $unit".trim()
                }
            }

            UnitSystem.BAKERS_PRECISION -> {
                when (u) {
                    "cup", "cups", "tasse" -> {
                        val grams = if (isLiquid(itemName)) scaled * 240.0 else scaled * density
                        "${String.format("%.1f", grams)} g"
                    }
                    "tbsp", "tablespoon", "el" -> {
                        val grams = if (isLiquid(itemName)) scaled * 15.0 else scaled * (density / 16.0)
                        "${String.format("%.1f", grams)} g"
                    }
                    "tsp", "teaspoon", "tl" -> {
                        val grams = if (isLiquid(itemName)) scaled * 5.0 else scaled * (density / 48.0)
                        "${String.format("%.1f", grams)} g"
                    }
                    "oz", "ounce" -> "${String.format("%.1f", scaled * 28.3495)} g"
                    "lb", "lbs" -> "${String.format("%.1f", scaled * 453.592)} g"
                    "kg" -> "${String.format("%.1f", scaled * 1000.0)} g"
                    "g", "gram", "gramm" -> "${String.format("%.1f", scaled)} g"
                    "ml" -> "${String.format("%.1f", scaled)} ml"
                    "l" -> "${String.format("%.1f", scaled * 1000.0)} ml"
                    else -> "${String.format("%.1f", scaled)} $unit".trim()
                }
            }
        }
    }

    private fun isLiquid(name: String): Boolean {
        return name.contains("water") || name.contains("wasser") ||
                name.contains("milk") || name.contains("milch") ||
                name.contains("oil") || name.contains("öl") ||
                name.contains("cream") || name.contains("sahne") ||
                name.contains("juice") || name.contains("saft") ||
                name.contains("wine") || name.contains("wein") ||
                name.contains("broth") || name.contains("brühe") ||
                name.contains("rum") || name.contains("kirschwasser")
    }

    private fun formatCupsFromMl(ml: Double): String {
        val cups = ml / 240.0
        return when {
            cups >= 0.2 -> "${formatFraction(cups)} cups"
            ml >= 15.0 -> "${(ml / 15.0).toInt()} tbsp"
            else -> "${(ml / 5.0).toInt()} tsp"
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
        return if (num % 1.0 == 0.0) num.toInt().toString() else String.format("%.1f", num)
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
            whole > 0 && fracStr.isEmpty() -> String.format("%.1f", num)
            fracStr.isNotEmpty() -> fracStr
            else -> String.format("%.1f", num)
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
    fun getInstruction(language: LanguageMode = LanguageMode.ENGLISH): String {
        return instructionEnglish.ifBlank { instructionGerman }
    }

    fun getLocalizedTip(language: LanguageMode = LanguageMode.ENGLISH): String? {
        val t = tip ?: return null
        if (!t.contains("||")) return t
        val parts = t.split("||").map { it.trim() }
        return parts.firstOrNull() ?: t
    }
}

enum class LanguageMode(val label: String, val flag: String, val description: String) {
    ENGLISH("English", "🇬🇧", "View all recipes, ingredients, instructions and menus in English")
}

enum class UnitSystem(val label: String, val shortLabel: String, val icon: String, val description: String) {
    CUPS_US("US Cups & Spoons", "Cups / Spoons", "🥣", "Cups, tablespoons (tbsp), teaspoons (tsp), oz, lbs, °F"),
    METRIC_GRAMS("Metric Weights & Volume", "Metric (g, ml)", "⚖️", "Grams (g), kilograms (kg), milliliters (ml), liters (l), °C"),
    UK_IMPERIAL("UK Imperial System", "UK Imperial", "🇬🇧", "Ounces (oz), pounds (lbs), fluid ounces (fl oz), pints (pt), °C"),
    BAKERS_PRECISION("Baker's Precision Grams", "Baker's Grams", "🧑‍🍳", "Exact decimal grams (e.g. 250.0g) for precision weighing")
}

enum class CoverTheme(val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    VINTAGE_LEATHER("Vintage Leather", 0xFF78350F, 0xFF451A03),
    WARM_TERRACOTTA("Warm Terracotta", 0xFF9A3412, 0xFFC2410C),
    FOREST_SAGE("Bavarian Forest", 0xFF14532D, 0xFF166534),
    FLORAL_LINEN("Antique Linen", 0xFFB45309, 0xFFD97706),
    GOLDEN_PARCHMENT("Golden Heritage", 0xFF854D0E, 0xFFA16207)
}
