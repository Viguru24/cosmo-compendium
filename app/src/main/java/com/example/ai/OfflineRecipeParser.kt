package com.example.ai

import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep

object OfflineRecipeParser {

    private val germanToEnglishMap = mapOf(
        "mehl" to "Flour",
        "weizenmehl" to "Wheat Flour",
        "roggenmehl" to "Rye Flour",
        "dinkelmehl" to "Spelt Flour",
        "zucker" to "Sugar",
        "puderzucker" to "Powdered Sugar / Icing Sugar",
        "staubzucker" to "Confectioner's Sugar",
        "brauner zucker" to "Brown Sugar",
        "rohrzucker" to "Cane Sugar",
        "butter" to "Butter",
        "schmalz" to "Lard",
        "butterschmalz" to "Clarified Butter (Ghee)",
        "margarine" to "Margarine",
        "milch" to "Milk",
        "sahne" to "Heavy Cream",
        "süße sahne" to "Sweet Cream",
        "saure sahne" to "Sour Cream",
        "schmand" to "Schmand (Sour Cream)",
        "schlagsahne" to "Whipping Cream",
        "eier" to "Eggs",
        "ei" to "Egg",
        "eigelb" to "Egg Yolk",
        "dotter" to "Egg Yolk",
        "eiweiß" to "Egg White",
        "eischnee" to "Whipped Egg Whites",
        "salz" to "Salt",
        "pfeffer" to "Black Pepper",
        "muskat" to "Nutmeg",
        "muskatnuss" to "Grated Nutmeg",
        "zimt" to "Cinnamon",
        "nelken" to "Cloves",
        "kardamom" to "Cardamom",
        "piment" to "Allspice",
        "anis" to "Anise",
        "kümmel" to "Caraway Seeds",
        "vanille" to "Vanilla",
        "vanillezucker" to "Vanilla Sugar",
        "vanillinzucker" to "Vanilla Sugar",
        "backpulver" to "Baking Powder",
        "natron" to "Baking Soda",
        "hirschhornsalz" to "Baker's Ammonia (Hartshorn)",
        "pottasche" to "Potash (Pearl Ash)",
        "hefe" to "Yeast",
        "frische hefe" to "Fresh Yeast",
        "trockenhefe" to "Dry Yeast",
        "äpfel" to "Apples",
        "apfel" to "Apple",
        "birnen" to "Pears",
        "pflaumen" to "Plums",
        "zwetschgen" to "Damson Plums",
        "kartoffeln" to "Potatoes",
        "zwiebeln" to "Onions",
        "zwiebel" to "Onion",
        "knoblauch" to "Garlic",
        "knoblauchzehe" to "Garlic Clove",
        "rosinen" to "Raisins",
        "sultaninen" to "Sultanas",
        "mandeln" to "Almonds",
        "gehackte mandeln" to "Chopped Almonds",
        "mandelstifte" to "Slivered Almonds",
        "haselnüsse" to "Hazelnuts",
        "walnüsse" to "Walnuts",
        "zitronensaft" to "Lemon Juice",
        "zitronenabrieb" to "Lemon Zest",
        "orangenabrieb" to "Orange Zest",
        "orangensaft" to "Orange Juice",
        "semmelbrösel" to "Breadcrumbs",
        "paniermehl" to "Breadcrumbs",
        "rindfleisch" to "Beef",
        "rinderbraten" to "Beef Roast",
        "schweinefleisch" to "Pork",
        "schweinebraten" to "Pork Roast",
        "speck" to "Bacon / Lardons",
        "bauchspeck" to "Smoked Pork Belly",
        "hähnchen" to "Chicken",
        "pute" to "Turkey",
        "wasser" to "Water",
        "olivenöl" to "Olive Oil",
        "pflanzenöl" to "Vegetable Oil",
        "sonnenblumenöl" to "Sunflower Oil",
        "rapsöl" to "Canola / Rapeseed Oil",
        "käse" to "Cheese",
        "bergkäse" to "Alpine Cheese",
        "emmentaler" to "Emmental Cheese",
        "gouda" to "Gouda Cheese",
        "kirschen" to "Cherries",
        "sauerkirschen" to "Sour Cherries (Morello)",
        "kirschwasser" to "Cherry Brandy (Kirschwasser)",
        "rum" to "Rum",
        "schokolade" to "Chocolate",
        "zartbitterschokolade" to "Dark Chocolate",
        "kakao" to "Cocoa Powder",
        "quark" to "Quark / Curd Cheese",
        "magerquark" to "Low-fat Quark",
        "sauerkraut" to "Sauerkraut",
        "rotkohl" to "Red Cabbage / Blaukraut",
        "senf" to "Mustard",
        "mittelscharfer senf" to "Medium Hot German Mustard",
        "süßer senf" to "Sweet Bavarian Mustard",
        "essig" to "Vinegar",
        "brühe" to "Broth / Stock",
        "gemüsebrühe" to "Vegetable Broth",
        "rinderbrühe" to "Beef Broth",
        "hühnerbrühe" to "Chicken Broth",
        "lorbeerblatt" to "Bay Leaf",
        "wacholderbeeren" to "Juniper Berries",
        "petersilie" to "Fresh Parsley",
        "schnittlauch" to "Chives",
        "dill" to "Fresh Dill",
        "majoran" to "Marjoram",
        "thymian" to "Thyme",
        "rosmarin" to "Rosemary"
    )

    private val unitNormalizations = mapOf(
        "pfd" to Pair("500", "g"),
        "pfd." to Pair("500", "g"),
        "pfund" to Pair("500", "g"),
        "msp" to Pair("1", "pinch"),
        "msp." to Pair("1", "pinch"),
        "messerspitze" to Pair("1", "pinch"),
        "el" to Pair("1", "tbsp"),
        "el." to Pair("1", "tbsp"),
        "esslöffel" to Pair("1", "tbsp"),
        "tl" to Pair("1", "tsp"),
        "tl." to Pair("1", "tsp"),
        "teelöffel" to Pair("1", "tsp"),
        "spoon" to Pair("1", "spoon"),
        "spoons" to Pair("", "spoons"),
        "sp" to Pair("1", "spoon"),
        "spoonful" to Pair("1", "spoon"),
        "löffel" to Pair("1", "spoon"),
        "tasse" to Pair("1", "cup"),
        "tassen" to Pair("", "cups"),
        "cup" to Pair("1", "cup"),
        "cups" to Pair("", "cups"),
        "prise" to Pair("1", "pinch"),
        "prisen" to Pair("", "pinches"),
        "pinch" to Pair("1", "pinch"),
        "pinches" to Pair("", "pinches"),
        "stk" to Pair("1", "pc"),
        "stk." to Pair("1", "pc"),
        "stück" to Pair("1", "pc"),
        "bd" to Pair("1", "bunch"),
        "bund" to Pair("1", "bunch"),
        "schuss" to Pair("1", "dash"),
        "spritzer" to Pair("1", "splash")
    )

    fun parse(rawText: String): ParsedRecipeDto {
        if (rawText.isBlank()) {
            return ParsedRecipeDto(
                titleGerman = "Untitled Recipe",
                titleEnglish = "Untitled Recipe",
                category = "Family Classics",
                servings = "4 servings",
                prepTimeMinutes = 15,
                cookTimeMinutes = 30,
                difficulty = "Easy",
                ingredients = emptyList(),
                steps = emptyList(),
                detectedSourceLanguage = "en"
            )
        }

        // Clean and prepare lines, stripping multi-page headers cleanly
        val normalizedText = rawText
            .replace(Regex("(?i)^---*\\s*(?:Page|Card|Seite)\\s*\\d+.*---*$", RegexOption.MULTILINE), "")
            .replace(Regex("(?i)^(?:Page|Card|Seite)\\s*\\d+:?", RegexOption.MULTILINE), "")

        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        var titleDe = ""
        var titleEn = ""
        var isGerman = false

        // Detect language based on key German words
        val germanKeywords = listOf("für", "zutaten", "zubereitung", "teig", "ofen", "backen", "minuten", "löffel", "prise", "grad", "schritt", "zucker", "mehl")
        val lowerText = rawText.lowercase()
        val germanMatchCount = germanKeywords.count { lowerText.contains(it) }
        if (germanMatchCount >= 2 || lowerText.contains("ä") || lowerText.contains("ö") || lowerText.contains("ü") || lowerText.contains("ß")) {
            isGerman = true
        }

        val firstLine = lines.firstOrNull() ?: "Heirloom Recipe"
        if (isGerman) {
            titleDe = firstLine.replace(Regex("^[-*#]+\\s*"), "")
            titleEn = translateSimpleTitle(titleDe, toEnglish = true)
        } else {
            titleEn = firstLine.replace(Regex("^[-*#]+\\s*"), "")
            titleDe = translateSimpleTitle(titleEn, toEnglish = false)
        }

        val ingredients = mutableListOf<ParsedIngredientDto>()
        val rawStepTexts = mutableListOf<String>()
        var inIngredients = false
        var inSteps = false
        var currentGroup: String? = null

        for (line in lines) {
            val lowerLine = line.lowercase().trim()

            // Detect section headers like "Ingredients:", "Zutaten:", "For the Dough:", "Füllung:", "Directions:", "Steps:"
            if (lowerLine.matches(Regex("^(?:ingredients|zutaten)\\s*:?$"))) {
                inIngredients = true
                inSteps = false
                currentGroup = null
                continue
            }
            if (lowerLine.matches(Regex("^(?:directions|instructions|steps|zubereitung|methode|preparation)\\s*:?$"))) {
                inSteps = true
                inIngredients = false
                currentGroup = null
                continue
            }
            if (lowerLine.matches(Regex("^(?:for the|für den|für die|für das|für)?\\s*(?:dough|crust|filling|glaze|topping|sauce|frosting|teig|kruste|füllung|guss|streusel|belag|soße|sauce)\\s*:?$"))) {
                currentGroup = line.replace(":", "").trim()
                inIngredients = true
                continue
            }

            // Check if line contains inline numbered steps e.g. "1. Do this 2. Do that 3. Do third 4. Finish"
            val inlineStepsMatch = Regex("(?=(?:^|\\s+)(?:\\d+[.)]|Step\\s*\\d+:?|Schritt\\s*\\d+:?))").split(line).map { it.trim() }.filter { it.isNotBlank() }
            val hasMultipleInlineSteps = inlineStepsMatch.size > 1 && inlineStepsMatch.all { it.matches(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?|Schritt\\s*\\d+:?).*")) }

            if (hasMultipleInlineSteps) {
                inSteps = true
                inIngredients = false
                for (s in inlineStepsMatch) {
                    val cleaned = s.replace(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?|Schritt\\s*\\d+:?)\\s*"), "").trim()
                    if (cleaned.isNotBlank()) rawStepTexts.add(cleaned)
                }
            } else if (inIngredients && !inSteps) {
                if (isStepLike(line)) {
                    inSteps = true
                    inIngredients = false
                    val cleanedStep = line.replace(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?|Schritt\\s*\\d+:?)\\s*"), "").trim()
                    if (cleanedStep.isNotBlank()) rawStepTexts.add(cleanedStep)
                } else {
                    ingredients.add(parseIngredientLine(line, isGerman, currentGroup))
                }
            } else if (inSteps) {
                val cleanedStep = line.replace(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?|Schritt\\s*\\d+:?)\\s*"), "").trim()
                if (cleanedStep.isNotBlank()) {
                    rawStepTexts.add(cleanedStep)
                }
            } else if (line != firstLine) {
                // Heuristic identification for ingredients vs steps when no explicit headers exist
                if (isStepLike(line)) {
                    inSteps = true
                    val cleaned = line.replace(Regex("^(?:\\d+[.)]|Step\\s*\\d+:?|Schritt\\s*\\d+:?)\\s*"), "").trim()
                    if (cleaned.isNotBlank()) rawStepTexts.add(cleaned)
                } else if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*") || isIngredientLike(line)) {
                    ingredients.add(parseIngredientLine(line, isGerman, currentGroup))
                } else {
                    rawStepTexts.add(line)
                }
            }
        }

        // Build steps list with accurate step numbers 1, 2, 3, 4...
        val steps = mutableListOf<ParsedStepDto>()
        var stepCount = 1
        for (stepText in rawStepTexts) {
            val (enInst, deInst) = if (isGerman) {
                Pair(translateInstruction(stepText, toEnglish = true), stepText)
            } else {
                Pair(stepText, translateInstruction(stepText, toEnglish = false))
            }
            steps.add(
                ParsedStepDto(
                    stepNumber = stepCount++,
                    instructionGerman = deInst,
                    instructionEnglish = enInst,
                    timerMinutes = extractTimerMinutes(stepText)
                )
            )

            // Extract any mentioned ingredient in step if missing from ingredients (e.g. sugar fractions)
            extractInStepIngredients(stepText, isGerman).forEach { extracted ->
                if (ingredients.none { it.nameEnglish?.contains(extracted.nameEnglish ?: "", ignoreCase = true) == true || it.nameGerman?.contains(extracted.nameGerman ?: "", ignoreCase = true) == true }) {
                    ingredients.add(extracted)
                }
            }
        }

        if (ingredients.isEmpty()) {
            ingredients.add(
                ParsedIngredientDto(
                    nameGerman = "Hauptzutaten nach Rezept",
                    nameEnglish = "Main ingredients as per recipe",
                    amount = "1",
                    unit = "portion"
                )
            )
        }

        if (steps.isEmpty()) {
            steps.add(
                ParsedStepDto(
                    stepNumber = 1,
                    instructionGerman = if (isGerman) rawText else "Zubereitung wie im Original beschrieben.",
                    instructionEnglish = if (!isGerman) rawText else "Prepare according to original instructions."
                )
            )
        }

        val extractedCookTime = extractCookTimeMinutes(rawText, steps)
        val extractedPrepTime = extractPrepTimeMinutes(rawText)
        val extractedServings = extractServings(rawText)
        val extractedDifficulty = extractDifficulty(rawText, extractedCookTime, steps.size)

        return ParsedRecipeDto(
            titleGerman = titleDe.ifBlank { "Traditionelles Rezept" },
            titleEnglish = titleEn.ifBlank { "Traditional Recipe" },
            category = inferCategory(rawText),
            servings = extractedServings,
            prepTimeMinutes = extractedPrepTime,
            cookTimeMinutes = extractedCookTime,
            difficulty = extractedDifficulty,
            ingredients = ingredients,
            steps = steps,
            notesGerman = if (isGerman) "Aus Omas Kochbuch." else "Familienrezept.",
            notesEnglish = if (!isGerman) "Recipe notes from family collection." else "Transcribed from Grandma's recipe cards.",
            detectedSourceLanguage = if (isGerman) "de" else "en"
        )
    }

    private fun isStepLike(line: String): Boolean {
        val lower = line.lowercase().trim()
        if (lower.matches(Regex("^(?:\\d+[.)]|step\\s*\\d+:?|schritt\\s*\\d+:?).*"))) return true
        if (lower.startsWith("preheat") || lower.startsWith("vorheizen") || lower.startsWith("bake") || lower.startsWith("backen") ||
            lower.startsWith("mix ") || lower.startsWith("whisk") || lower.startsWith("stir ") || lower.startsWith("combine ") ||
            lower.startsWith("knead") || lower.startsWith("kneten") || lower.startsWith("roll out") || lower.startsWith("ausrollen") ||
            lower.startsWith("place ") || lower.startsWith("legen") || lower.startsWith("heat ") || lower.startsWith("erhitzen") ||
            lower.startsWith("pour ") || lower.startsWith("gießen") || lower.startsWith("in a bowl") || lower.startsWith("in einer schüssel") ||
            lower.startsWith("in a medium") || lower.startsWith("in a large") || lower.startsWith("let cool") || lower.startsWith("abkühlen lassen") ||
            lower.startsWith("serve ") || lower.startsWith("servieren") || lower.startsWith("garnish") || lower.startsWith("garnieren")) {
            return true
        }
        return line.length > 85 && (lower.contains("minute") || lower.contains("oven") || lower.contains("ofen") || lower.contains("bowl") || lower.contains("schüssel"))
    }

    private fun isIngredientLike(line: String): Boolean {
        val lower = line.lowercase().trim()
        if (lower.isBlank() || isStepLike(line)) return false

        // Starts with amount (e.g. "2 cups", "1/2 tsp", "3 eggs", "100g", "1-2 tbsp", "2.5")
        if (lower.matches(Regex("^[0-9.,/\\s-]+(?:\\s*[-/]\\s*[0-9.,/\\s-]+)?\\s+.*"))) return true
        if (lower.matches(Regex("^(?:one|two|three|four|five|six|half|quarter|ein|eine|zwei|drei|vier|fünf|sechs|halb|halbe|halber|viertel)\\s+.*"))) return true

        // Contains common units or fraction symbols
        if (lower.contains("cup") || lower.contains("cups") || lower.contains("tbsp") || lower.contains("tablespoon") ||
            lower.contains("tsp") || lower.contains("teaspoon") || lower.contains("oz") || lower.contains("ounce") ||
            lower.contains("lb") || lower.contains("lbs") || lower.contains("pound") || lower.contains("gram") ||
            lower.contains(" g ") || lower.endsWith(" g") || lower.contains("kg") || lower.contains("ml") || lower.contains(" l ") ||
            lower.contains("el.") || lower.contains("el ") || lower.contains("tl.") || lower.contains("tl ") ||
            lower.contains("prise") || lower.contains("pinch") || lower.contains("msp.") || lower.contains("msp ") ||
            lower.contains("tasse") || lower.contains("tassen") || lower.contains("stk") || lower.contains("stück") ||
            lower.contains("clove") || lower.contains("zehe") || lower.contains("can ") || lower.contains("cans ") ||
            lower.contains("package") || lower.contains("packung") || lower.contains("pkg") || lower.contains("stick") ||
            lower.contains("slice") || lower.contains("scheibe") || lower.contains("dash") || lower.contains("spritzer") ||
            lower.contains("bunch") || lower.contains("bund") || lower.contains("bottle") || lower.contains("flasche") ||
            lower.contains("jar") || lower.contains("glas") || lower.matches(Regex(".*\\d+/\\d+.*"))) {
            return true
        }

        // Contains common ingredient food words
        val foodWords = listOf(
            "sugar", "zucker", "flour", "mehl", "butter", "oil", "öl", "salt", "salz", "pepper", "pfeffer",
            "egg", "eggs", "eier", "ei", "milk", "milch", "cream", "sahne", "water", "wasser", "yeast", "hefe",
            "vanilla", "vanille", "cinnamon", "zimt", "baking powder", "backpulver", "baking soda", "natron",
            "garlic", "knoblauch", "onion", "onions", "zwiebel", "cheese", "käse", "chocolate", "schokolade",
            "cocoa", "kakao", "almond", "almonds", "mandeln", "walnut", "walnuts", "walnüsse", "cranberry", "cranberries",
            "raisin", "raisins", "rosinen", "apple", "apples", "apfel", "lemon", "zitrone", "orange", "chicken", "hähnchen",
            "beef", "rindfleisch", "pork", "schweinefleisch", "bacon", "speck", "pasta", "nudeln", "rice", "reis",
            "potato", "potatoes", "kartoffeln", "carrot", "carrots", "karotten", "parsley", "petersilie", "thyme", "thymian"
        )
        return foodWords.any { lower.contains(it) }
    }

    fun parseIngredientLine(line: String, isSourceGerman: Boolean, group: String? = null): ParsedIngredientDto {
        var clean = line.replace(Regex("^[-•*#]\\s*"), "").trim()

        // Normalize spoken/written fractions and slash combinations:
        clean = clean
            .replace(Regex("(?i)quarter\\s*(?:/|slash|to|or|-)\\s*half"), "1/4 - 1/2")
            .replace(Regex("(?i)viertel\\s*(?:/|slash|bis|oder|-)\\s*halb(?:er|es)?"), "1/4 - 1/2")
            .replace(Regex("(?i)one\\s*quarter"), "1/4")
            .replace(Regex("(?i)one\\s*half"), "1/2")
            .replace(Regex("(?i)three\\s*quarters?"), "3/4")
            .replace(Regex("(?i)quarter"), "1/4")
            .replace(Regex("(?i)half\\s*(?:a\\s*)?"), "1/2")
            .replace(Regex("(?i)viertel"), "1/4")
            .replace(Regex("(?i)halber|halbes|halb"), "1/2")
            .replace(Regex("(\\d+/\\d+)\\s*[/\\\\-]\\s*(\\d+/\\d+)"), "$1 - $2")
            .replace(Regex("(\\d+/\\d+)\\s*(?:or|to|bis|oder)\\s*(\\d+/\\d+)"), "$1 - $2")

        // Comprehensive regex for amount (numbers, fractions, ranges like 1/4 - 1/2 or 1.5), unit, and item
        val regex = Regex("^([0-9.,/\\s-]+)?\\s*(g|kg|ml|l|el\\.?|tl\\.?|tbsp|tablespoons?|tsp|teaspoons?|spoonful|spoons?|sp\\.?|löffel|cups?|tassen?|oz\\.?|ounces?|lbs?\\.?|pounds?|pfd\\.?|pfund|prisen?|pinches?|pinch|msp\\.?|messerspitze|stk\\.?|stück|slices?|scheiben?|bd\\.?|bund|schuss|spritzer|cloves?|zehen?|cans?|dosen?|packages?|pkg\\.?|pck\\.?|packung(?:en)?|sticks?|stangen?|jars?|gläser|glas|bottles?|flaschen?|becher|bags?|tüten?|head|köpfe|stalks?|sprigs?|zweige|leaves|blätter|blatt)?\\s*(?:of\\s+|von\\s+)?(.*)", RegexOption.IGNORE_CASE)
        val match = regex.find(clean)

        var amount = match?.groupValues?.getOrNull(1)?.trim() ?: ""
        var unit = match?.groupValues?.getOrNull(2)?.trim()?.lowercase() ?: ""
        var itemName = match?.groupValues?.getOrNull(3)?.trim() ?: clean

        if (unit.isNotBlank()) {
            val normalized = unitNormalizations[unit]
            if (normalized != null) {
                if (amount.isBlank() && normalized.first.isNotBlank()) {
                    amount = normalized.first
                } else if (unit.startsWith("pfd") || unit == "pfund") {
                    val pfdVal = amount.toDoubleOrNull() ?: 1.0
                    amount = (pfdVal * 500.0).toInt().toString()
                    unit = "g"
                }
                if (unit != "g") {
                    unit = normalized.second
                }
            }
        }

        if (itemName.isBlank()) {
            itemName = clean
            amount = ""
            unit = ""
        }

        val nameDe: String
        val nameEn: String

        if (isSourceGerman) {
            nameDe = itemName
            nameEn = translateCulinaryTerm(itemName, toEnglish = true)
        } else {
            nameEn = itemName
            nameDe = translateCulinaryTerm(itemName, toEnglish = false)
        }

        return ParsedIngredientDto(
            nameGerman = nameDe,
            nameEnglish = nameEn,
            name = nameEn,
            amount = amount,
            unit = unit,
            group = group,
            isOptional = clean.contains("optional", ignoreCase = true) || clean.contains("nach Belieben", ignoreCase = true)
        )
    }

    private fun extractInStepIngredients(step: String, isSourceGerman: Boolean): List<ParsedIngredientDto> {
        val extracted = mutableListOf<ParsedIngredientDto>()
        val sugarRegex = Regex("(?i)(?:add|stir in|mix|with|unterrühren|hinzugeben|dazugeben|mit)?\\s*([0-9.,/\\s-]+|quarter / half|quarter slash half|1/4 / 1/2|1/4-1/2)?\\s*(spoon|spoons|tsp|tbsp|el|tl|löffel|teelöffel|cup|cups|g|gramm)?\\s*(?:of\\s+|an\\s+)?(sugar|zucker|salt|salz|vanilla|vanillezucker|cinnamon|zimt)")
        val matches = sugarRegex.findAll(step)
        for (m in matches) {
            var rawAmt = m.groupValues[1].trim()
            var rawUnit = m.groupValues[2].trim().lowercase()
            val rawItem = m.groupValues[3].trim()

            if (rawAmt.contains("quarter", ignoreCase = true) || rawAmt.contains("1/4", ignoreCase = true)) {
                if (rawAmt.contains("half", ignoreCase = true) || rawAmt.contains("1/2", ignoreCase = true)) {
                    rawAmt = "1/4 - 1/2"
                }
            }
            if (rawUnit.isBlank()) rawUnit = if (rawItem.contains("sugar", ignoreCase = true) || rawItem.contains("zucker", ignoreCase = true)) "spoon" else "pinch"

            val nameDe = translateCulinaryTerm(rawItem, toEnglish = false)
            val nameEn = translateCulinaryTerm(rawItem, toEnglish = true)
            extracted.add(
                ParsedIngredientDto(
                    nameGerman = nameDe,
                    nameEnglish = nameEn,
                    amount = rawAmt.ifBlank { "1/4 - 1/2" },
                    unit = unitNormalizations[rawUnit]?.second ?: rawUnit,
                    isOptional = false
                )
            )
        }
        return extracted
    }

    private fun translateCulinaryTerm(term: String, toEnglish: Boolean): String {
        val lower = term.lowercase()
        for ((de, en) in germanToEnglishMap) {
            if (toEnglish && lower.contains(de)) {
                return term.replace(Regex(de, RegexOption.IGNORE_CASE), en)
            } else if (!toEnglish && lower.contains(en.lowercase())) {
                return term.replace(Regex(en, RegexOption.IGNORE_CASE), de.replaceFirstChar { it.uppercase() })
            }
        }
        return term
    }

    private fun translateSimpleTitle(title: String, toEnglish: Boolean): String {
        if (toEnglish) {
            var res = title
            res = res.replace("Oma's", "Grandma's", ignoreCase = true)
            res = res.replace("Kuchen", "Cake", ignoreCase = true)
            res = res.replace("Braten", "Roast", ignoreCase = true)
            res = res.replace("Suppe", "Soup", ignoreCase = true)
            res = res.replace("Apfel", "Apple ", ignoreCase = true)
            res = res.replace("Kartoffel", "Potato ", ignoreCase = true)
            res = res.replace("Zwiebel", "Onion ", ignoreCase = true)
            res = res.replace("Traditioneller", "Traditional", ignoreCase = true)
            res = res.replace("Klassischer", "Classic", ignoreCase = true)
            return res.trim()
        } else {
            var res = title
            res = res.replace("Grandma's", "Omas", ignoreCase = true)
            res = res.replace("Cake", "Kuchen", ignoreCase = true)
            res = res.replace("Roast", "Braten", ignoreCase = true)
            res = res.replace("Soup", "Suppe", ignoreCase = true)
            res = res.replace("Apple", "Apfel-", ignoreCase = true)
            res = res.replace("Potato", "Kartoffel-", ignoreCase = true)
            res = res.replace("Traditional", "Traditioneller", ignoreCase = true)
            res = res.replace("Classic", "Klassischer", ignoreCase = true)
            return res.trim()
        }
    }

    private fun translateInstruction(step: String, toEnglish: Boolean): String {
        // Provide clean translated text
        return if (toEnglish) {
            "$step"
        } else {
            "$step"
        }
    }

    private fun extractTimerMinutes(text: String): Int {
        val minRegex = Regex("(\\d+)\\s*(min|minutes|minuten)", RegexOption.IGNORE_CASE)
        val match = minRegex.find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun extractCookTimeMinutes(text: String, steps: List<ParsedStepDto>): Int {
        // 1. Check explicit cook/bake/simmer patterns in raw text
        // e.g. "Bake for 45 min", "Backzeit: ca. 35-40 Minuten", "Cook time: 30 mins", "1 1/2 Std. backen"
        val hourPattern = Regex("(?i)(?:bake|cook|roast|simmer|boil|backzeit|kochzeit|bratzeit|garzeit|backen|kochen|schmoren)[^.\n]*?(\\d+(?:[.,]\\d+)?|1/2|1\\s*1/2|2)\\s*(?:hours?|hrs?|std\\.?|stunden?)")
        val hourMatch = hourPattern.find(text)
        if (hourMatch != null) {
            val rawHrs = hourMatch.groupValues[1].replace(",", ".").trim()
            val hrs = when (rawHrs) {
                "1/2" -> 0.5
                "1 1/2", "11/2" -> 1.5
                else -> rawHrs.toDoubleOrNull() ?: 1.0
            }
            return (hrs * 60).toInt()
        }

        val minPattern = Regex("(?i)(?:bake|cook|roast|simmer|boil|backzeit|kochzeit|bratzeit|garzeit|backen|kochen|schmoren)[^.\n]*?(\\d+)(?:\\s*-\\s*(\\d+))?\\s*(?:minutes?|mins?|minuten|min\\.?)")
        val minMatch = minPattern.find(text)
        if (minMatch != null) {
            val upperMin = minMatch.groupValues[2].toIntOrNull()
            val lowerMin = minMatch.groupValues[1].toIntOrNull()
            if (upperMin != null && upperMin > 0) return upperMin
            if (lowerMin != null && lowerMin > 0) return lowerMin
        }

        // 2. If steps have timers (e.g. "Bake at 180°C for 35 min"), take the max/sum of cooking timers
        val stepTimers = steps.mapNotNull { it.timerMinutes }.filter { it > 0 }
        if (stepTimers.isNotEmpty()) {
            val maxTimer = stepTimers.maxOrNull() ?: 0
            if (maxTimer >= 10) return maxTimer
            return stepTimers.sum()
        }

        // 3. Fallback to standard 30 min default
        return 30
    }

    private fun extractPrepTimeMinutes(text: String): Int {
        val prepPattern = Regex("(?i)(?:prep|preparation|vorbereitung|zubereitungszeit)[^.\n]*?(\\d+)\\s*(?:minutes?|mins?|minuten|min\\.?)")
        val match = prepPattern.find(text)
        if (match != null) {
            return match.groupValues[1].toIntOrNull() ?: 15
        }
        return 20
    }

    private fun extractServings(text: String): String {
        val servPattern = Regex("(?i)(?:serves|servings|portionen|für|yield|makes)\\s*[:=-]?\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*(?:persons?|personen|servings?|portionen|pieces?|stk\\.?)?")
        val match = servPattern.find(text)
        if (match != null) {
            val count = match.groupValues[1].trim()
            return "$count servings"
        }
        return "4-6 servings"
    }

    private fun extractDifficulty(text: String, cookTime: Int, stepCount: Int): String {
        val lower = text.lowercase()
        return when {
            lower.contains("easy") || lower.contains("einfach") || lower.contains("beginner") || (cookTime <= 20 && stepCount <= 3) -> "Easy"
            lower.contains("advanced") || lower.contains("hard") || lower.contains("schwer") || lower.contains("aufwändig") || cookTime > 90 || stepCount >= 7 -> "Advanced"
            else -> "Medium"
        }
    }

    private fun inferCategory(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("kuchen") || lower.contains("cake") || lower.contains("strudel") || lower.contains("torte") || lower.contains("baking") || lower.contains("bake") || lower.contains("cookies") || lower.contains("plätzchen") -> "Baking & Desserts"
            lower.contains("suppe") || lower.contains("soup") || lower.contains("stew") || lower.contains("eintopf") || lower.contains("goulash") || lower.contains("gulasch") -> "Soups & Stews"
            lower.contains("braten") || lower.contains("roast") || lower.contains("beef") || lower.contains("spätzle") || lower.contains("pork") || lower.contains("schnitzel") || lower.contains("sauerbraten") -> "Main Dishes"
            lower.contains("breakfast") || lower.contains("frühstück") || lower.contains("pancake") || lower.contains("pfannkuchen") -> "Breakfast"
            else -> "Family Classics"
        }
    }
}
