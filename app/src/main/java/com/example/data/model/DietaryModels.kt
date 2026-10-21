package com.example.data.model

import com.example.data.local.RecipeEntity
import com.squareup.moshi.JsonClass
import java.util.Locale

/**
 * Standard Food Allergen classifications based on FDA and EU 14-Allergen standards.
 */
enum class Allergen(
    val id: String,
    val icon: String,
    val labelEnglish: String,
    val labelGerman: String,
    val isMajor: Boolean = true
) {
    GLUTEN("gluten", "🌾", "Gluten / Wheat", "Gluten / Weizen"),
    DAIRY("dairy", "🥛", "Dairy / Milk", "Milch / Laktose"),
    EGGS("eggs", "🥚", "Eggs", "Eier"),
    PEANUTS("peanuts", "🥜", "Peanuts", "Erdnüsse"),
    TREE_NUTS("tree_nuts", "🌰", "Tree Nuts", "Schalenfrüchte / Nüsse"),
    SOY("soy", "🫘", "Soybeans", "Soja"),
    FISH("fish", "🐟", "Fish", "Fisch"),
    SHELLFISH("shellfish", "🦐", "Crustaceans & Molluscs", "Krebstiere & Weichtiere"),
    SESAME("sesame", "🌱", "Sesame", "Sesam"),
    CELERY("celery", "🥬", "Celery", "Sellerie", false),
    MUSTARD("mustard", "🟡", "Mustard", "Senf", false),
    SULFITES("sulfites", "🍷", "Sulphites", "Sulfite", false),
    LUPIN("lupin", "🌸", "Lupin", "Lupinen", false);

    val emoji: String get() = icon
    fun getLabel(language: LanguageMode = LanguageMode.ENGLISH): String = getDisplayName(language)
    fun getDisplayName(language: LanguageMode = LanguageMode.ENGLISH): String {
        return when (language) {
            LanguageMode.GERMAN -> labelGerman
            else -> labelEnglish
        }
    }
}

/**
 * Dietary Suitability classifications for lifestyle, ethical, and medical diets.
 */
enum class DietaryTag(
    val id: String,
    val icon: String,
    val labelEnglish: String,
    val labelGerman: String,
    val colorHex: Long,
    val descriptionEnglish: String,
    val descriptionGerman: String
) {
    CELIAC_SAFE(
        id = "celiac_safe",
        icon = "🛡️",
        labelEnglish = "Celiac-Safe",
        labelGerman = "Zöliakie-Sicher",
        colorHex = 0xFF059669, // Emerald green
        descriptionEnglish = "Strictly gluten-free with no wheat, barley, rye, or hidden gluten risks",
        descriptionGerman = "Strikt glutenfrei ohne Weizen, Gerste, Roggen oder versteckte Glutenrisiken"
    ),
    GLUTEN_FREE(
        id = "gluten_free",
        icon = "🌾",
        labelEnglish = "Gluten-Free",
        labelGerman = "Glutenfrei",
        colorHex = 0xFF10B981, // Green
        descriptionEnglish = "No gluten-containing grains or flours",
        descriptionGerman = "Frei von glutenhaltigem Getreide und Mehl"
    ),
    VEGETARIAN(
        id = "vegetarian",
        icon = "🥬",
        labelEnglish = "Vegetarian",
        labelGerman = "Vegetarisch",
        colorHex = 0xFF16A34A, // Forest green
        descriptionEnglish = "No meat, poultry, seafood, lard, or animal gelatins",
        descriptionGerman = "Ohne Fleisch, Geflügel, Fisch, Schmalz oder tierische Gelatine"
    ),
    VEGAN(
        id = "vegan",
        icon = "🌱",
        labelEnglish = "Vegan",
        labelGerman = "Vegan",
        colorHex = 0xFF15803D, // Dark vibrant green
        descriptionEnglish = "100% plant-based: no meat, dairy, eggs, or animal products",
        descriptionGerman = "100% pflanzlich: frei von Fleisch, Milch, Eiern und tierischen Erzeugnissen"
    ),
    DAIRY_FREE(
        id = "dairy_free",
        icon = "🥛",
        labelEnglish = "Dairy-Free",
        labelGerman = "Laktose-/Milchfrei",
        colorHex = 0xFF2563EB, // Royal blue
        descriptionEnglish = "No milk, butter, cheese, cream, yogurt, or whey",
        descriptionGerman = "Ohne Milch, Butter, Käse, Sahne, Joghurt oder Molke"
    ),
    NUT_FREE(
        id = "nut_free",
        icon = "🥜",
        labelEnglish = "Nut-Free",
        labelGerman = "Nussfrei",
        colorHex = 0xFFD97706, // Amber
        descriptionEnglish = "Contains no peanuts or tree nuts (almonds, walnuts, cashews, etc.)",
        descriptionGerman = "Frei von Erdnüssen und Schalenfrüchten (Mandeln, Walnüssen, Cashews usw.)"
    ),
    EGG_FREE(
        id = "egg_free",
        icon = "🥚",
        labelEnglish = "Egg-Free",
        labelGerman = "Eifrei",
        colorHex = 0xFFEA580C, // Deep orange
        descriptionEnglish = "Contains no eggs or egg-derived ingredients",
        descriptionGerman = "Frei von Eiern und Eibestandteilen"
    ),
    KETO(
        id = "keto",
        icon = "🥑",
        labelEnglish = "Keto / Low-Carb",
        labelGerman = "Keto / Low-Carb",
        colorHex = 0xFF7C3AED, // Violet
        descriptionEnglish = "Low in carbohydrates and free of sugars and grains",
        descriptionGerman = "Kohlenhydratarm, ohne Getreide und raffinierten Zucker"
    );

    val emoji: String get() = icon
    fun getLabel(language: LanguageMode = LanguageMode.ENGLISH): String = getDisplayName(language)
    fun getDisplayName(language: LanguageMode = LanguageMode.ENGLISH): String {
        return when (language) {
            LanguageMode.GERMAN -> labelGerman
            else -> labelEnglish
        }
    }
}

/**
 * Filter options for the Bookshelf Screen filter bar.
 */
enum class DietaryFilter(
    val id: String,
    val icon: String,
    val labelEnglish: String,
    val labelGerman: String
) {
    ALL("all", "🍽️", "All Diets", "Alle"),
    CELIAC_SAFE("celiac_safe", "🛡️", "Celiac-Safe", "Zöliakie-Sicher"),
    GLUTEN_FREE("gluten_free", "🌾", "Gluten-Free", "Glutenfrei"),
    VEGETARIAN("vegetarian", "🥬", "Vegetarian", "Vegetarisch"),
    VEGAN("vegan", "🌱", "Vegan", "Vegan"),
    DAIRY_FREE("dairy_free", "🥛", "Dairy-Free", "Milchfrei"),
    NUT_FREE("nut_free", "🥜", "Nut-Free", "Nussfrei");

    fun matches(recipe: RecipeEntity): Boolean = DietaryClassifier.matchesFilter(recipe, this)

    val emoji: String get() = icon
    fun getLabel(language: LanguageMode = LanguageMode.ENGLISH): String = getDisplayName(language)
    fun getDisplayName(language: LanguageMode = LanguageMode.ENGLISH): String {
        return when (language) {
            LanguageMode.GERMAN -> labelGerman
            else -> labelEnglish
        }
    }
}

/**
 * Full dietary analysis result for a recipe, including detected tags,
 * allergens, and specific celiac risk warnings.
 */
data class RecipeDietaryAnalysis(
    val dietaryTags: Set<DietaryTag>,
    val detectedAllergens: Set<Allergen>,
    val celiacWarnings: List<String> = emptyList(),
    val isCeliacSafe: Boolean = dietaryTags.contains(DietaryTag.CELIAC_SAFE),
    val isGlutenFree: Boolean = dietaryTags.contains(DietaryTag.GLUTEN_FREE),
    val isVegetarian: Boolean = dietaryTags.contains(DietaryTag.VEGETARIAN),
    val isVegan: Boolean = dietaryTags.contains(DietaryTag.VEGAN),
    val isDairyFree: Boolean = dietaryTags.contains(DietaryTag.DAIRY_FREE),
    val isNutFree: Boolean = dietaryTags.contains(DietaryTag.NUT_FREE),
    val isEggFree: Boolean = dietaryTags.contains(DietaryTag.EGG_FREE),
    val suitableDiets: Set<DietaryTag> = dietaryTags,
    val celiacCautions: List<String> = celiacWarnings
)

/**
 * Multilingual Culinary Classifier for Celiac, Allergens, and Dietary Preferences.
 */
object DietaryClassifier {

    private class FastKeywordMatcher(keywords: Set<String>) {
        val singleWords: Set<String> = keywords.filter { !it.contains(' ') && !it.contains('-') }.map { it.lowercase(Locale.ROOT) }.toSet()
        val phrases: List<String> = keywords.filter { it.contains(' ') || it.contains('-') }.map { it.lowercase(Locale.ROOT) }

        fun matches(textLower: String, words: Set<String>): Boolean {
            for (w in words) {
                if (w in singleWords) return true
            }
            for (phrase in phrases) {
                if (textLower.contains(phrase)) return true
            }
            return false
        }
    }

    private val analysisCache = java.util.concurrent.ConcurrentHashMap<Long, Pair<Int, RecipeDietaryAnalysis>>()

    fun clearCache() {
        analysisCache.clear()
    }

    // --- GLUTEN KEYWORDS ---
    private val GLUTEN_KEYWORDS = setOf(
        "flour", "wheat", "all-purpose", "all purpose", "bread flour", "pastry flour", "cake flour",
        "semolina", "durum", "spelt", "farro", "kamut", "rye", "barley", "malt", "beer", "ale", "lager",
        "couscous", "bulgur", "seitan", "shoyu", "teriyaki", "breadcrumbs", "panko", "croutons",
        "pasta", "noodles", "spaghetti", "macaroni", "penne", "fusilli", "linguine", "fettuccine", "ramen", "udon", "orzo",
        "puff pastry", "filo", "phyllo", "pie crust", "graham", "wafer", "roux",
        // German
        "mehl", "weizen", "weizenmehl", "dinkel", "dinkelmehl", "roggen", "roggenmehl", "gerste", "malz", "bier",
        "grieß", "hartweizengrieß", "semmelbrösel", "paniermehl", "brotkrumen", "krume", "spätzle", "blätterteig", "strudelteig",
        // French / Italian / Spanish
        "farine", "ble", "blé", "orge", "seigle", "epautre", "épautre", "biere", "bière",
        "farina", "frumento", "grano", "orzo", "segale", "birra",
        "harina", "trigo", "cebada", "centeno", "cerveza"
    )

    // Hidden or high-risk gluten ingredients requiring certified GF verification for Celiac safety
    private val CELIAC_RISK_KEYWORDS = setOf(
        "soy sauce", "sojasosse", "sojasoße", "tamari", "worcestershire", "malt vinegar",
        "oats", "oatmeal", "rolled oats", "hafer", "haferflocken", "hafermehl", "avoine", "avena",
        "baking powder", "backpulver", "bouillon", "brühe", "stock powder", "yeast extract",
        "modified food starch", "modifizierte stärke"
    )

    // --- DAIRY KEYWORDS ---
    private val DAIRY_KEYWORDS = setOf(
        "milk", "whole milk", "skim milk", "buttermilk", "cream", "heavy cream", "whipping cream",
        "sour cream", "half and half", "butter", "ghee", "cheese", "cheddar", "mozzarella",
        "parmesan", "parmigiano", "pecorino", "ricotta", "gouda", "brie", "camembert", "feta",
        "goat cheese", "blue cheese", "gorgonzola", "mascarpone", "quark", "topfen", "yogurt", "yoghurt",
        "whey", "casein", "curd",
        // German
        "milch", "vollmilch", "buttermilch", "sahne", "schlagsahne", "süße sahne", "süsse sahne",
        "sauerrahm", "schmand", "crème fraîche", "creme fraiche", "butter", "käse", "hartkäse", "schnittkäse",
        "frischkäse", "speisequark", "magerquark", "joghurt", "molke",
        // French / Italian / Spanish
        "lait", "beurre", "creme", "crème", "fromage",
        "latte", "burro", "panna", "formaggio",
        "leche", "mantequilla", "nata", "queso"
    )

    // --- MEAT KEYWORDS ---
    private val MEAT_KEYWORDS = setOf(
        "beef", "steak", "ground beef", "minced beef", "chuck", "brisket", "sirloin", "ribeye", "tenderloin",
        "veal", "pork", "pork belly", "pork chop", "bacon", "pancetta", "prosciutto", "ham", "sausage",
        "chorizo", "bratwurst", "pepperoni", "salami", "lamb", "mutton", "duck", "goose", "venison", "boar",
        "chicken", "turkey", "poultry", "fowl", "lard", "tallow", "suet", "gelatin", "gelatine",
        "beef broth", "chicken broth", "bone broth", "veal stock",
        // German
        "fleisch", "rind", "rinder", "rindfleisch", "rinderbraten", "rinderhack", "hackfleisch", "hack",
        "schwein", "schweine", "schweinefleisch", "schweinebraten", "speck", "durchwachsener speck",
        "schinken", "kochschinken", "rohschinken", "wurst", "würste", "würstchen", "wiener", "leberwurst", "blutwurst",
        "kalb", "kalbfleisch", "huhn", "hähnchen", "geflügel", "pute", "putenfleisch", "ente", "gans",
        "lamm", "lammfleisch", "wild", "reh", "hirsch", "wildschwein", "schmalz", "griebenschmalz",
        "rindsuppe", "hühnersuppe", "fleischbrühe", "rinderbrühe", "hühnerbrühe",
        // French / Italian / Spanish
        "viande", "boeuf", "bœuf", "porc", "lard", "jambon", "saucisse", "poulet", "dinde", "canard",
        "carne", "manzo", "maiale", "pancetta", "prosciutto", "salsiccia", "pollo", "tacchino",
        "carne", "ternera", "cerdo", "tocino", "jamon", "jamón", "salchicha", "pollo", "pavo"
    )

    // --- SEAFOOD / FISH KEYWORDS ---
    private val SEAFOOD_KEYWORDS = setOf(
        "fish", "salmon", "tuna", "cod", "trout", "sea bass", "halibut", "tilapia", "anchovy", "anchovies",
        "sardine", "sardines", "mackerel", "herring", "snapper", "swordfish", "shrimp", "shrimps", "prawn",
        "prawns", "crab", "lobster", "crawfish", "crayfish", "clam", "clams", "mussel", "mussels", "oyster",
        "oysters", "scallop", "scallops", "squid", "calamari", "octopus", "fish sauce", "worcestershire",
        // German
        "fisch", "lachs", "forelle", "thunfisch", "kabeljau", "zander", "haring", "matjes", "makrele",
        "scholle", "seezunge", "garnele", "garnelen", "krabbe", "krabben", "nordseekrabben", "hummer",
        "krebs", "muschel", "muscheln", "miesmuscheln", "auster", "austern", "jakobsmuscheln", "sardelle", "sardellen",
        "tintenfisch", "kalmar",
        // French / Italian / Spanish
        "poisson", "saumon", "thon", "crevette", "homard", "moule",
        "pesce", "salmone", "tonno", "gambero", "aragosta", "cozza",
        "pescado", "salmon", "salmón", "atun", "atún", "gamba", "langosta", "mejillon", "mejillón"
    )

    // --- EGG KEYWORDS ---
    private val EGG_KEYWORDS = setOf(
        "egg", "eggs", "egg white", "egg whites", "egg yolk", "egg yolks", "mayonnaise", "aioli", "meringue",
        // German
        "ei", "eier", "eigelb", "dotter", "eiweiss", "eiweiß", "eiklar"
    )

    // --- TREE NUT & PEANUT KEYWORDS ---
    private val PEANUT_KEYWORDS = setOf(
        "peanut", "peanuts", "peanut butter", "groundnut",
        "erdnuss", "erdnüsse", "erdnussbutter", "cacahuete", "arachide"
    )

    private val TREE_NUT_KEYWORDS = setOf(
        "almond", "almonds", "almond flour", "almond meal", "walnut", "walnuts", "pecan", "pecans",
        "hazelnut", "hazelnuts", "cashew", "cashews", "pistachio", "pistachios", "macadamia",
        "pine nut", "pine nuts", "brazil nut", "chestnut", "chestnuts", "praline", "marzipan",
        // German
        "mandel", "mandeln", "mandelmehl", "walnuss", "walnüsse", "haselnuss", "haselnüsse",
        "cashew", "cashewkern", "cashewkerne", "pistazie", "pistazien", "pinienkern", "pinienkerne",
        "marone", "maroni", "marzipan"
    )

    // --- SOY KEYWORDS ---
    private val SOY_KEYWORDS = setOf(
        "soy", "soya", "soybean", "soybeans", "tofu", "tempeh", "edamame", "soy milk", "soy sauce", "miso",
        "soja", "sojabohne", "sojamilch", "sojasosse", "sojasoße"
    )

    // --- SESAME KEYWORDS ---
    private val SESAME_KEYWORDS = setOf(
        "sesame", "sesame seeds", "sesame oil", "tahini", "tahina",
        "sesam", "sesamkörner", "sesamöl"
    )

    private val GLUTEN_MATCHER by lazy { FastKeywordMatcher(GLUTEN_KEYWORDS) }
    private val CELIAC_RISK_MATCHER by lazy { FastKeywordMatcher(CELIAC_RISK_KEYWORDS) }
    private val DAIRY_MATCHER by lazy { FastKeywordMatcher(DAIRY_KEYWORDS) }
    private val MEAT_MATCHER by lazy { FastKeywordMatcher(MEAT_KEYWORDS) }
    private val SEAFOOD_MATCHER by lazy { FastKeywordMatcher(SEAFOOD_KEYWORDS) }
    private val EGG_MATCHER by lazy { FastKeywordMatcher(EGG_KEYWORDS) }
    private val PEANUT_MATCHER by lazy { FastKeywordMatcher(PEANUT_KEYWORDS) }
    private val TREE_NUT_MATCHER by lazy { FastKeywordMatcher(TREE_NUT_KEYWORDS) }
    private val SOY_MATCHER by lazy { FastKeywordMatcher(SOY_KEYWORDS) }
    private val SESAME_MATCHER by lazy { FastKeywordMatcher(SESAME_KEYWORDS) }
    private val FISH_MATCHER by lazy { FastKeywordMatcher(setOf("fish", "fisch", "salmon", "lachs", "trout", "forelle", "tuna", "thunfisch", "cod", "kabeljau")) }
    private val SHELLFISH_MATCHER by lazy { FastKeywordMatcher(setOf("shrimp", "garnele", "garnelen", "prawn", "crab", "krabbe", "lobster", "hummer", "clam", "mussel", "muschel", "oyster", "auster")) }
    private val HIGH_CARBS_MATCHER by lazy { FastKeywordMatcher(setOf("sugar", "zucker", "honey", "honig", "potato", "kartoffel", "rice", "reis", "pasta", "nudeln", "corn", "mais")) }

    /**
     * Complete dietary analysis of a recipe (memoized with high-speed token matching).
     */
    fun analyze(recipe: RecipeEntity): RecipeDietaryAnalysis {
        val cacheKey = recipe.id
        val contentHash = recipe.ingredients.hashCode() xor recipe.title.hashCode() xor recipe.notes.hashCode()
        if (cacheKey > 0) {
            val cached = analysisCache[cacheKey]
            if (cached != null && cached.first == contentHash) {
                return cached.second
            }
        }
        val computed = computeAnalysis(recipe)
        if (cacheKey > 0) {
            analysisCache[cacheKey] = Pair(contentHash, computed)
        }
        return computed
    }

    private fun computeAnalysis(recipe: RecipeEntity): RecipeDietaryAnalysis {
        val ingredientTokens = recipe.ingredients.map { ing ->
            listOfNotNull(
                ing.name,
                ing.nameEnglish,
                ing.nameGerman
            ).joinToString(" ").lowercase(Locale.ROOT)
        }

        val allIngredientsJoined = ingredientTokens.joinToString(" ")
        val titleTokens = "${recipe.title} ${recipe.titleEnglish} ${recipe.titleGerman}".lowercase(Locale.ROOT)
        val combinedText = "$titleTokens $allIngredientsJoined ${recipe.category.lowercase(Locale.ROOT)} ${recipe.notes.lowercase(Locale.ROOT)}"
        val wordsSet = combinedText.split(Regex("[^\\p{L}\\p{Nd}]+")).filter { it.isNotEmpty() }.toSet()

        // 1. Detect Allergens
        val allergens = mutableSetOf<Allergen>()

        val hasGluten = GLUTEN_MATCHER.matches(combinedText, wordsSet)
        if (hasGluten) allergens.add(Allergen.GLUTEN)

        val hasDairy = DAIRY_MATCHER.matches(combinedText, wordsSet)
        if (hasDairy) allergens.add(Allergen.DAIRY)

        val hasEggs = EGG_MATCHER.matches(combinedText, wordsSet)
        if (hasEggs) allergens.add(Allergen.EGGS)

        val hasPeanuts = PEANUT_MATCHER.matches(combinedText, wordsSet)
        if (hasPeanuts) allergens.add(Allergen.PEANUTS)

        val hasTreeNuts = TREE_NUT_MATCHER.matches(combinedText, wordsSet)
        if (hasTreeNuts) allergens.add(Allergen.TREE_NUTS)

        val hasSoy = SOY_MATCHER.matches(combinedText, wordsSet)
        if (hasSoy) allergens.add(Allergen.SOY)

        val hasFish = FISH_MATCHER.matches(combinedText, wordsSet)
        if (hasFish) allergens.add(Allergen.FISH)

        val hasShellfish = SHELLFISH_MATCHER.matches(combinedText, wordsSet)
        if (hasShellfish) allergens.add(Allergen.SHELLFISH)

        val hasSesame = SESAME_MATCHER.matches(combinedText, wordsSet)
        if (hasSesame) allergens.add(Allergen.SESAME)

        val hasMeat = MEAT_MATCHER.matches(combinedText, wordsSet)
        val hasSeafood = SEAFOOD_MATCHER.matches(combinedText, wordsSet)

        // 2. Celiac Risk Detection
        val celiacWarnings = mutableListOf<String>()
        val hasCeliacRiskIngredients = CELIAC_RISK_MATCHER.matches(combinedText, wordsSet)

        if (hasGluten) {
            celiacWarnings.add("Contains gluten-containing grains (wheat, barley, rye, or spelt)")
        } else if (hasCeliacRiskIngredients) {
            // Check specific risk items
            if (combinedText.contains("soy sauce") || combinedText.contains("sojasoße")) {
                if (!combinedText.contains("tamari") && !combinedText.contains("gluten-free") && !combinedText.contains("glutenfrei")) {
                    celiacWarnings.add("Soy sauce contains brewed wheat; use certified Gluten-Free Tamari.")
                }
            }
            if (combinedText.contains("oat") || combinedText.contains("hafer")) {
                if (!combinedText.contains("certified gluten-free") && !combinedText.contains("glutenfreie haferflocken")) {
                    celiacWarnings.add("Standard oats may be contaminated; use certified Gluten-Free oats.")
                }
            }
            if (combinedText.contains("baking powder") || combinedText.contains("backpulver")) {
                celiacWarnings.add("Ensure baking powder is certified gluten-free (some contain wheat starch).")
            }
        }

        // 3. Determine Dietary Suitability Tags
        val tags = mutableSetOf<DietaryTag>()

        // A. Celiac-Safe: No gluten grains AND no unverified contamination risks
        val isExplicitlyGlutenFree = combinedText.contains("gluten-free") || combinedText.contains("glutenfrei") || combinedText.contains("sans gluten") || combinedText.contains("senza glutine")
        val isCeliacSafe = !hasGluten && (celiacWarnings.isEmpty() || isExplicitlyGlutenFree)
        if (isCeliacSafe && recipe.ingredients.isNotEmpty()) {
            tags.add(DietaryTag.CELIAC_SAFE)
        }

        // B. Gluten-Free: No direct gluten flours/grains
        if (!hasGluten && recipe.ingredients.isNotEmpty()) {
            tags.add(DietaryTag.GLUTEN_FREE)
        }

        // C. Vegetarian: No meat and no seafood
        val isVegetarian = !hasMeat && !hasSeafood && recipe.ingredients.isNotEmpty()
        if (isVegetarian) {
            tags.add(DietaryTag.VEGETARIAN)
        }

        // D. Vegan: Vegetarian AND no dairy AND no eggs
        val isVegan = isVegetarian && !hasDairy && !hasEggs
        if (isVegan) {
            tags.add(DietaryTag.VEGAN)
        }

        // E. Dairy-Free
        if (!hasDairy && recipe.ingredients.isNotEmpty()) {
            tags.add(DietaryTag.DAIRY_FREE)
        }

        // F. Nut-Free
        if (!hasPeanuts && !hasTreeNuts) {
            tags.add(DietaryTag.NUT_FREE)
        }

        // G. Egg-Free
        if (!hasEggs && recipe.ingredients.isNotEmpty()) {
            tags.add(DietaryTag.EGG_FREE)
        }

        // H. Keto / Low-Carb: Gluten-free, no potatoes, no rice, no sugar, no bread
        val hasHighCarbs = hasGluten || HIGH_CARBS_MATCHER.matches(combinedText, wordsSet)
        if (!hasHighCarbs && (hasMeat || hasFish || hasDairy || hasEggs) && recipe.ingredients.isNotEmpty()) {
            tags.add(DietaryTag.KETO)
        }

        return RecipeDietaryAnalysis(
            dietaryTags = tags,
            detectedAllergens = allergens,
            celiacWarnings = celiacWarnings
        )
    }

    /**
     * Checks if a recipe matches a given dietary filter.
     */
    fun matchesFilter(recipe: RecipeEntity, filter: DietaryFilter): Boolean {
        if (filter == DietaryFilter.ALL) return true
        val analysis = analyze(recipe)
        return when (filter) {
            DietaryFilter.ALL -> true
            DietaryFilter.CELIAC_SAFE -> analysis.isCeliacSafe
            DietaryFilter.GLUTEN_FREE -> analysis.isGlutenFree
            DietaryFilter.VEGETARIAN -> analysis.isVegetarian
            DietaryFilter.VEGAN -> analysis.isVegan
            DietaryFilter.DAIRY_FREE -> analysis.isDairyFree
            DietaryFilter.NUT_FREE -> analysis.isNutFree
        }
    }

    private fun containsKeyword(text: String, keywords: Set<String>): Boolean {
        val textLower = text.lowercase(Locale.ROOT)
        val words = textLower.split(Regex("[^\\p{L}\\p{Nd}]+")).filter { it.isNotEmpty() }.toSet()
        for (kw in keywords) {
            val kwLower = kw.lowercase(Locale.ROOT)
            if (kwLower.contains(' ') || kwLower.contains('-')) {
                if (textLower.contains(kwLower)) return true
            } else {
                if (words.contains(kwLower)) return true
            }
        }
        return false
    }
}

/**
 * Extension helpers on RecipeEntity for easy dietary queries in UI and ViewModels.
 */
val RecipeEntity.dietaryAnalysis: RecipeDietaryAnalysis
    get() = DietaryClassifier.analyze(this)

val RecipeEntity.isCeliacSafe: Boolean
    get() = dietaryAnalysis.isCeliacSafe

val RecipeEntity.isVegetarian: Boolean
    get() = dietaryAnalysis.isVegetarian

val RecipeEntity.isVegan: Boolean
    get() = dietaryAnalysis.isVegan

val RecipeEntity.isGlutenFree: Boolean
    get() = dietaryAnalysis.isGlutenFree

val RecipeEntity.isDairyFree: Boolean
    get() = dietaryAnalysis.isDairyFree

val RecipeEntity.isNutFree: Boolean
    get() = dietaryAnalysis.isNutFree
