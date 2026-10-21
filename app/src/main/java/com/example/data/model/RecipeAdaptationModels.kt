package com.example.data.model

import com.example.data.local.RecipeEntity

/**
 * Target diets that a recipe can be adapted to.
 */
enum class DietaryTarget(
    val id: String,
    val icon: String,
    val titleEnglish: String,
    val titleGerman: String,
    val descriptionEnglish: String,
    val descriptionGerman: String
) {
    CELIAC_SAFE(
        id = "celiac_safe",
        icon = "🛡️",
        titleEnglish = "Celiac-Safe / Gluten-Free",
        titleGerman = "Zöliakie-Sicher / Glutenfrei",
        descriptionEnglish = "Replace all wheat, barley, rye, malt, and hidden gluten sources with certified gluten-free alternatives.",
        descriptionGerman = "Ersetzt Weizen, Gerste, Roggen, Malz und versteckte Glutenquellen durch zertifiziert glutenfreie Alternativen."
    ),
    VEGETARIAN(
        id = "vegetarian",
        icon = "🥬",
        titleEnglish = "Vegetarian",
        titleGerman = "Vegetarisch",
        descriptionEnglish = "Replace meat, poultry, seafood, lard, and animal gelatin with plant-based and dairy/egg options.",
        descriptionGerman = "Ersetzt Fleisch, Geflügel, Fisch, Schmalz und Gelatine durch pflanzliche und vegetarische Zutaten."
    ),
    VEGAN(
        id = "vegan",
        icon = "🌱",
        titleEnglish = "100% Vegan (Plant-Based)",
        titleGerman = "100% Vegan (Pflanzlich)",
        descriptionEnglish = "Replace all meat, dairy, eggs, honey, and animal derivatives with completely plant-based alternatives.",
        descriptionGerman = "Ersetzt sämtliche Fleisch-, Milch-, Ei-, Honig- und tierische Produkte durch reine Pflanzenprodukte."
    ),
    DAIRY_FREE(
        id = "dairy_free",
        icon = "🥛",
        titleEnglish = "Dairy-Free / Lactose-Free",
        titleGerman = "Milchfrei / Laktosefrei",
        descriptionEnglish = "Replace cow's milk, butter, cheeses, and heavy cream with rich plant milks and oils.",
        descriptionGerman = "Ersetzt Kuhmilch, Butter, Käse und Sahne durch hochwertige Pflanzenmilch und -fette."
    ),
    NUT_FREE(
        id = "nut_free",
        icon = "🥜",
        titleEnglish = "Nut-Free Safe",
        titleGerman = "Nussfrei",
        descriptionEnglish = "Replace tree nuts and peanuts with toasted seeds, sunflower butter, or oat crunches.",
        descriptionGerman = "Ersetzt Nüsse und Erdnüsse durch geröstete Kerne, Sonnenblumenmus oder Haferflocken."
    ),
    EGG_FREE(
        id = "egg_free",
        icon = "🥚",
        titleEnglish = "Egg-Free Baking",
        titleGerman = "Eifrei Backen",
        descriptionEnglish = "Replace eggs with flax meal, aquafaba, applesauce, or silken tofu binders.",
        descriptionGerman = "Ersetzt Eier durch Leinsamen, Kichererbsenwasser (Aquafaba), Apfelmus oder Seidentofu."
    );

    fun getTitle(language: LanguageMode = LanguageMode.ENGLISH): String =
        if (language == LanguageMode.GERMAN) titleGerman else titleEnglish

    fun getDescription(language: LanguageMode = LanguageMode.ENGLISH): String =
        if (language == LanguageMode.GERMAN) descriptionGerman else descriptionEnglish
}

/**
 * A suggested ingredient substitution with culinary rationale.
 */
data class IngredientSubstitution(
    val original: RecipeIngredient,
    val replacement: RecipeIngredient,
    val reasonEnglish: String,
    val reasonGerman: String,
    val culinaryTipEnglish: String? = null,
    val culinaryTipGerman: String? = null,
    val isSelected: Boolean = true
) {
    fun getReason(language: LanguageMode = LanguageMode.ENGLISH): String =
        if (language == LanguageMode.GERMAN) reasonGerman else reasonEnglish

    fun getCulinaryTip(language: LanguageMode = LanguageMode.ENGLISH): String? =
        if (language == LanguageMode.GERMAN) culinaryTipGerman else culinaryTipEnglish
}

/**
 * Complete adaptation plan containing proposed replacements and adapted recipe preview.
 */
data class RecipeAdaptationPlan(
    val targetDiet: DietaryTarget,
    val substitutions: List<IngredientSubstitution>,
    val adaptedRecipe: RecipeEntity,
    val generalNotesEnglish: List<String> = emptyList(),
    val generalNotesGerman: List<String> = emptyList()
) {
    val hasSubstitutions: Boolean get() = substitutions.isNotEmpty()
}

/**
 * Smart Culinary Engine that inspects a recipe and designs tailored substitutions
 * for Celiac disease, Vegetarian, Vegan, Dairy-Free, and Allergen-Safe variations.
 */
object RecipeAdaptationEngine {

    fun generateAdaptation(recipe: RecipeEntity, target: DietaryTarget): RecipeAdaptationPlan {
        val substitutions = mutableListOf<IngredientSubstitution>()
        val generalNotesEn = mutableListOf<String>()
        val generalNotesDe = mutableListOf<String>()

        recipe.ingredients.forEach { ingredient ->
            val sub = findSubstitution(ingredient, target)
            if (sub != null) {
                substitutions.add(sub)
            }
        }

        // Add targeted culinary tips based on diet
        when (target) {
            DietaryTarget.CELIAC_SAFE -> {
                generalNotesEn.add("Use dedicated clean cookware and certified gluten-free certified baking powders and spices to avoid cross-contamination.")
                generalNotesDe.add("Achten Sie auf saubere Küchenutensilien und zertifiziert glutenfreie Gewürze/Backpulver zur Vermeidung von Kreuzkontamination.")
            }
            DietaryTarget.VEGAN -> {
                generalNotesEn.add("When using aquafaba (chickpea water) as an egg white replacer, whip with 1/8 tsp cream of tartar for stiff peaks.")
                generalNotesDe.add("Beim Aufschlagen von Aquafaba (Kichererbsenwasser) sorgt eine Prise Weinstein-Backpulver für perfekten Eischnee-Stand.")
            }
            DietaryTarget.DAIRY_FREE -> {
                generalNotesEn.add("Canned full-fat coconut milk chilled overnight provides rich whipping cream consistency.")
                generalNotesDe.add("Über Nacht gekühlte Vollfett-Kokosmilch eignet sich hervorragend als aufschlagbare Schlagsahne.")
            }
            else -> {}
        }

        val adaptedRecipe = buildAdaptedRecipe(recipe, substitutions, target)

        return RecipeAdaptationPlan(
            targetDiet = target,
            substitutions = substitutions,
            adaptedRecipe = adaptedRecipe,
            generalNotesEnglish = generalNotesEn,
            generalNotesGerman = generalNotesDe
        )
    }

    fun applySelectedSubstitutions(
        recipe: RecipeEntity,
        substitutions: List<IngredientSubstitution>,
        target: DietaryTarget
    ): RecipeEntity {
        return buildAdaptedRecipe(recipe, substitutions.filter { it.isSelected }, target)
    }

    private fun buildAdaptedRecipe(
        original: RecipeEntity,
        substitutions: List<IngredientSubstitution>,
        target: DietaryTarget
    ): RecipeEntity {
        val subMap = substitutions.associateBy { it.original }

        val newIngredients = original.ingredients.map { ing ->
            subMap[ing]?.replacement ?: ing
        }

        val badge = when (target) {
            DietaryTarget.CELIAC_SAFE -> " [Celiac-Safe 🛡️]"
            DietaryTarget.VEGETARIAN -> " [Vegetarian 🥬]"
            DietaryTarget.VEGAN -> " [Vegan 🌱]"
            DietaryTarget.DAIRY_FREE -> " [Dairy-Free 🥛]"
            DietaryTarget.NUT_FREE -> " [Nut-Free 🥜]"
            DietaryTarget.EGG_FREE -> " [Egg-Free 🥚]"
        }

        val cleanTitle = original.title.replace(Regex("\\s*\\[.*?\\]$"), "")
        val newTitle = "$cleanTitle$badge"

        return original.copy(
            id = 0,
            title = newTitle,
            titleEnglish = if (original.titleEnglish.isNotBlank()) "${original.titleEnglish.replace(Regex("\\s*\\[.*?\\]$"), "")}$badge" else "",
            titleGerman = if (original.titleGerman.isNotBlank()) "${original.titleGerman.replace(Regex("\\s*\\[.*?\\]$"), "")}$badge" else "",
            ingredients = newIngredients,
            notes = if (original.notes.isNotBlank()) "${original.notes}\n\nAdapted for ${target.titleEnglish}." else "Adapted for ${target.titleEnglish}."
        )
    }

    private fun findSubstitution(ingredient: RecipeIngredient, target: DietaryTarget): IngredientSubstitution? {
        val text = "${ingredient.name} ${ingredient.nameEnglish.orEmpty()} ${ingredient.nameGerman.orEmpty()}".lowercase()

        return when (target) {
            DietaryTarget.CELIAC_SAFE -> findCeliacSubstitution(ingredient, text)
            DietaryTarget.VEGETARIAN -> findVegetarianSubstitution(ingredient, text)
            DietaryTarget.VEGAN -> findVeganSubstitution(ingredient, text)
            DietaryTarget.DAIRY_FREE -> findDairyFreeSubstitution(ingredient, text)
            DietaryTarget.NUT_FREE -> findNutFreeSubstitution(ingredient, text)
            DietaryTarget.EGG_FREE -> findEggFreeSubstitution(ingredient, text)
        }
    }

    // --- CELIAC / GLUTEN-FREE SUBSTITUTIONS ---
    private fun findCeliacSubstitution(ingredient: RecipeIngredient, text: String): IngredientSubstitution? {
        return when {
            text.contains("bread flour") || text.contains("all-purpose flour") || text.contains("weizenmehl") || text.contains("flour") || text.contains("mehl") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "1:1 Gluten-Free Measure-for-Measure Baking Blend",
                        nameEnglish = "1:1 Gluten-Free Measure-for-Measure Baking Blend (King Arthur / Bob's Red Mill)",
                        nameGerman = "1:1 Glutenfreie Universal-Backmischung (mit Xanthan)"
                    ),
                    reasonEnglish = "Contains wheat gluten which triggers severe autoimmune reaction in celiac disease.",
                    reasonGerman = "Enthält Weizengluten, das bei Zöliakie autoimmune Darmschädigungen auslöst.",
                    culinaryTipEnglish = "Ensure blend includes xanthan gum. If not, add 1/4 tsp xanthan gum per 120g flour for elasticity.",
                    culinaryTipGerman = "Achten Sie auf enthaltenes Xanthan. Andernfalls 1/4 TL Xanthan pro 120g Mehl für Elastizität zugeben."
                )
            }
            text.contains("semolina") || text.contains("grieß") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Polenta / Coarse Cornmeal or Rice Semolina",
                        nameEnglish = "Certified Gluten-Free Corn Polenta or Rice Semolina",
                        nameGerman = "Glutenfreier Maisgrieß (Polenta) oder Reisgrieß"
                    ),
                    reasonEnglish = "Traditional semolina is milled from durum wheat.",
                    reasonGerman = "Klassischer Grieß wird aus Hartweizen gewonnen.",
                    culinaryTipEnglish = "Polenta gives identical golden color and pleasant grainy texture in puddings and dumplings.",
                    culinaryTipGerman = "Polenta liefert identische goldene Farbe und angenehm körnige Textur in Knödeln und Puddings."
                )
            }
            text.contains("soy sauce") || text.contains("sojasauce") || text.contains("shoyu") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Certified Gluten-Free Tamari Soy Sauce",
                        nameEnglish = "Certified Gluten-Free Tamari Soy Sauce",
                        nameGerman = "Zertifizierte glutenfreie Tamari-Sojasauce"
                    ),
                    reasonEnglish = "Standard soy sauce is brewed with roasted wheat.",
                    reasonGerman = "Konventionelle Sojasauce wird mit geröstetem Weizen gebraut.",
                    culinaryTipEnglish = "Tamari delivers even richer umami depth without a trace of wheat.",
                    culinaryTipGerman = "Tamari bietet noch tieferen Umami-Geschmack ohne jede Spur von Weizen."
                )
            }
            text.contains("breadcrumbs") || text.contains("panko") || text.contains("paniermehl") || text.contains("semmelbrösel") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Gluten-Free Rice/Corn Panko Breadcrumbs",
                        nameEnglish = "Gluten-Free Panko Breadcrumbs",
                        nameGerman = "Glutenfreie Semmelbrösel (aus Reis- und Maismehl)"
                    ),
                    reasonEnglish = "Standard breadcrumbs are made from wheat bread rolls.",
                    reasonGerman = "Herkömmliche Semmelbrösel bestehen aus getrockneten Weizenbrötchen.",
                    culinaryTipEnglish = "Yields an exceptionally light, ultra-crispy crust when pan-frying.",
                    culinaryTipGerman = "Sorgt beim Ausbacken für eine besonders zarte, herrlich knusprige Panade."
                )
            }
            text.contains("pasta") || text.contains("spaghetti") || text.contains("noodles") || text.contains("nudeln") || text.contains("spätzle") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Gluten-Free Brown Rice & Corn Pasta",
                        nameEnglish = "Certified Gluten-Free Brown Rice & Corn Pasta",
                        nameGerman = "Glutenfreie Pasta (aus Vollkornreis und Mais)"
                    ),
                    reasonEnglish = "Traditional pasta contains durum wheat gluten.",
                    reasonGerman = "Traditionelle Pasta wird aus glutenhaltigem Hartweizengrieß hergestellt.",
                    culinaryTipEnglish = "Cook in abundant salted water and rinse briefly in warm water to prevent clumping.",
                    culinaryTipGerman = "In reichlich Salzwasser kochen und kurz warm abspülen, um Verkleben zu vermeiden."
                )
            }
            text.contains("beer") || text.contains("bier") || text.contains("malt") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Certified Gluten-Free Sorghum/Millet Beer or Rich Vegetable Stock",
                        nameEnglish = "Gluten-Free Craft Beer or Vegetable Stock with 1 tsp Apple Cider Vinegar",
                        nameGerman = "Glutenfreies Hirsebier oder Gemüsebrühe mit 1 TL Apfelessig"
                    ),
                    reasonEnglish = "Barley malt and conventional beers are high in gluten.",
                    reasonGerman = "Gerstenmalz und herkömmliches Bier sind stark glutenhaltig.",
                    culinaryTipEnglish = "Provides matching acidity, yeast depth, and deglazing richness.",
                    culinaryTipGerman = "Verleiht Bratenansätzen die identische Würze und säuerliche Tiefe."
                )
            }
            else -> null
        }
    }

    // --- VEGETARIAN SUBSTITUTIONS ---
    private fun findVegetarianSubstitution(ingredient: RecipeIngredient, text: String): IngredientSubstitution? {
        return when {
            text.contains("beef") || text.contains("rindfleisch") || text.contains("rinderhack") || text.contains("ground meat") || text.contains("hackfleisch") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Brown Lentils & Minced Portobello Mushrooms",
                        nameEnglish = "Cooked Brown Lentils & Finely Minced Cremini Mushrooms (or Plant Grounds)",
                        nameGerman = "Braune Berglinsen & fein gehackte Champignons (oder Pflanzenhack)"
                    ),
                    reasonEnglish = "Replaces red meat with fiber and savory culinary umami.",
                    reasonGerman = "Ersetzt Rindfleisch durch ballaststoffreiche, herzhafte Umami-Aromen.",
                    culinaryTipEnglish = "Sauté mushrooms in brown butter or olive oil with a dash of tamari for deep savory depth.",
                    culinaryTipGerman = "Pilze in Butter oder Olivenöl mit einem Spritzer Tamari anbraten für tiefen Fleischgeschmack."
                )
            }
            text.contains("chicken") || text.contains("hähnchen") || text.contains("huhn") || text.contains("poultry") || text.contains("turkey") || text.contains("pute") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Extra-Firm Pressed Tofu or King Oyster Mushrooms",
                        nameEnglish = "Extra-Firm Pressed Tofu or Shredded King Oyster Mushrooms",
                        nameGerman = "Fester Tofu oder marinierte Kräuterseitlinge"
                    ),
                    reasonEnglish = "Replaces poultry with high-protein plant textures that absorb marinades cleanly.",
                    reasonGerman = "Ersetzt Geflügel durch proteinreiche Konsistenz, die Gewürze optimal aufsaugt.",
                    culinaryTipEnglish = "Tear King Oyster mushrooms with a fork to mimic authentic shredded chicken texture.",
                    culinaryTipGerman = "Kräuterseitlinge mit der Gabel zerfasern für täuschend echte Hähnchenstruktur."
                )
            }
            text.contains("bacon") || text.contains("speck") || text.contains("pancetta") || text.contains("prosciutto") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Smoked Paprika & Maple Glazed Shiitake Crisps",
                        nameEnglish = "Smoked Paprika & Tamari Glazed Shiitake Bacon Crisps",
                        nameGerman = "Rauchige Shiitake-Speckchips mit Ahornsirup und Paprika"
                    ),
                    reasonEnglish = "Delivers classic salty, smoky, savory bacon crunch without pork.",
                    reasonGerman = "Liefert die salzig-rauchige Knusprigkeit von Speck ganz ohne Schweinefleisch.",
                    culinaryTipEnglish = "Bake thin shiitake slices at 180°C (350°F) for 15 minutes until shatteringly crisp.",
                    culinaryTipGerman = "Dünne Shiitake-Scheiben bei 180°C 15 Minuten backen, bis sie herrlich kross sind."
                )
            }
            text.contains("chicken broth") || text.contains("beef broth") || text.contains("hühnerbrühe") || text.contains("rinderbrühe") || text.contains("fleischbrühe") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Rich Roasted Vegetable & Mushroom Broth",
                        nameEnglish = "Rich Roasted Vegetable & Mushroom Broth",
                        nameGerman = "Kräftige geröstete Gemüse-Pilz-Brühe"
                    ),
                    reasonEnglish = "Replaces animal bone broths with savory vegetable reduction.",
                    reasonGerman = "Ersetzt Knochen- und Fleischbrühe durch aromatisches Wurzelgemüse.",
                    culinaryTipEnglish = "Add a dried bay leaf and a teaspoon of tomato paste for golden clarity.",
                    culinaryTipGerman = "Ein Lorbeerblatt und ein Teelöffel Tomatenmark verleihen goldene Farbe und Tiefe."
                )
            }
            text.contains("gelatin") || text.contains("gelatine") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        amount = if (ingredient.amount.isNotBlank()) "1/2" else "",
                        unit = "tsp",
                        name = "Agar-Agar Powder",
                        nameEnglish = "Agar-Agar Powder (Natural Red Algae Gel)",
                        nameGerman = "Agar-Agar Pulver (Pflanzliches Algen-Geliermittel)"
                    ),
                    reasonEnglish = "Commercial gelatin is derived from animal collagen.",
                    reasonGerman = "Handelsübliche Gelatine wird aus tierischem Bindegewebe gewonnen.",
                    culinaryTipEnglish = "Agar sets firmer than gelatin at room temperature. Boil for 2 minutes to activate fully.",
                    culinaryTipGerman = "Agar geliert bereits bei Raumtemperatur. Mindestens 2 Minuten aufkochen zum Aktivieren."
                )
            }
            text.contains("lard") || text.contains("schmalz") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Extra Virgin Olive Oil or Pure Coconut Oil",
                        nameEnglish = "High-Oleic Sunflower Oil or Extra Virgin Olive Oil",
                        nameGerman = "Kaltgepresstes Olivenöl oder Butterschmalz (Ghee)"
                    ),
                    reasonEnglish = "Lard is rendered animal fat.",
                    reasonGerman = "Schmalz ist ausgelassenes tierisches Fett.",
                    culinaryTipEnglish = "Provides clean, healthy monounsaturated fat with crisp browning.",
                    culinaryTipGerman = "Erzielt hervorragende Bräunung mit gesunden ungesättigten Fettsäuren."
                )
            }
            else -> null
        }
    }

    // --- VEGAN SUBSTITUTIONS ---
    private fun findVeganSubstitution(ingredient: RecipeIngredient, text: String): IngredientSubstitution? {
        val vegSub = findVegetarianSubstitution(ingredient, text)
        if (vegSub != null) return vegSub

        return when {
            text.contains("butter") && !text.contains("peanut butter") && !text.contains("apple butter") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "European-Style Cultured Plant Butter",
                        nameEnglish = "Cultured European-Style Plant Butter (e.g. Naturli, Miyoko's)",
                        nameGerman = "Vegane Blockbutter auf Mandel- oder Sheabasis"
                    ),
                    reasonEnglish = "Replaces dairy butter with high-fat plant solids that laminate and brown beautifully.",
                    reasonGerman = "Ersetzt Milchbutter durch festes Pflanzenfett mit identischem Schmelzpunkt.",
                    culinaryTipEnglish = "Plant butter behaves exactly 1:1 in puff pastry, cookies, and pie crusts.",
                    culinaryTipGerman = "Verhält sich beim Backen und für Mürbeteig exakt im Verhältnis 1:1."
                )
            }
            text.contains("milk") || text.contains("milch") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Creamy Barista Oat Milk",
                        nameEnglish = "Full-Bodied Barista Oat Milk (Unsweetened)",
                        nameGerman = "Ungesüßte Barista-Hafermilch"
                    ),
                    reasonEnglish = "Replaces cow's milk with silky neutral plant milk that emulsifies well.",
                    reasonGerman = "Ersetzt Kuhmilch durch neutrale, sämige Pflanzenmilch.",
                    culinaryTipEnglish = "Oat milk provides natural sugars that promote golden browning in the oven.",
                    culinaryTipGerman = "Hafermilch karamellisiert im Ofen wie herkömmliche Milch."
                )
            }
            text.contains("heavy cream") || text.contains("whipping cream") || text.contains("schlagsahne") || text.contains("sahne") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Canned Full-Fat Coconut Cream or Soy Cuisine",
                        nameEnglish = "Chilled Coconut Cream or Barista Soy/Oat Cuisine",
                        nameGerman = "Pflanzliche Kochcreme (Hafer/Soja) oder gekühlte Kokoscreme"
                    ),
                    reasonEnglish = "Provides 30%+ fat required for rich sauces, custards, and fillings.",
                    reasonGerman = "Liefert den für sämige Saucen und Füllungen notwendigen Fettgehalt von über 30%.",
                    culinaryTipEnglish = "For sweet desserts, chill canned coconut milk and whip only the hardened top cream layer.",
                    culinaryTipGerman = "Für Desserts feste Kokoscreme aus der gekühlten Dose wie Sahne aufschlagen."
                )
            }
            text.contains("egg") || text.contains("eier") || text.contains("ei ") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Aquafaba or Ground Flax Egg (1 tbsp flax + 2.5 tbsp water)",
                        nameEnglish = "Ground Golden Flax Egg (1 tbsp + 2.5 tbsp water per egg) or Aquafaba",
                        nameGerman = "Leinsamen-Ei (1 EL gemahlene Leinsamen + 2,5 EL Wasser) oder Aquafaba"
                    ),
                    reasonEnglish = "Replaces whole egg binding and moisture in batters and doughs.",
                    reasonGerman = "Ersetzt die bindende und feuchtigkeitsspendende Funktion des Hühnereis.",
                    culinaryTipEnglish = "Let flaxseed gel for 5 minutes before mixing into batter for maximum cohesive strength.",
                    culinaryTipGerman = "Leinsamen 5 Minuten quellen lassen, bis eine bindende Konsistenz entsteht."
                )
            }
            text.contains("honey") || text.contains("honig") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Pure Amber Maple Syrup or Agave Nectar",
                        nameEnglish = "Pure Grade A Maple Syrup or Light Agave Nectar",
                        nameGerman = "Reiner Ahornsirup oder heller Agavendicksaft"
                    ),
                    reasonEnglish = "Honey is an animal product harvested from bees.",
                    reasonGerman = "Honig ist ein tierisches Produkt von Honigbienen.",
                    culinaryTipEnglish = "Maple syrup adds floral sweetness and dissolves instantly in dressings and doughs.",
                    culinaryTipGerman = "Ahornsirup verleiht blumige Süße und löst sich sofort homogen auf."
                )
            }
            text.contains("parmesan") || text.contains("parmigiano") || text.contains("grana padano") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Nutritional Yeast & Ground Cashew Parmesan",
                        nameEnglish = "Nutritional Yeast & Roasted Cashew Blend with Sea Salt",
                        nameGerman = "Würzige Edelhefeflocken mit gemahlenen Cashews"
                    ),
                    reasonEnglish = "Replaces dairy cheese with tangy, nutty, fermented savory umami.",
                    reasonGerman = "Ersetzt Hartkäse durch nussig-herzhafte, käsige Würznoten.",
                    culinaryTipEnglish = "Pulse 1/2 cup cashews with 3 tbsp nutritional yeast and 1/2 tsp salt in a food processor.",
                    culinaryTipGerman = "100g Cashews mit 3 EL Hefeflocken und 1/2 TL Salz kurz im Mixer pulsieren."
                )
            }
            else -> null
        }
    }

    // --- DAIRY-FREE SUBSTITUTIONS ---
    private fun findDairyFreeSubstitution(ingredient: RecipeIngredient, text: String): IngredientSubstitution? {
        return when {
            text.contains("butter") && !text.contains("peanut butter") && !text.contains("apple butter") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Plant Butter or Extra Virgin Olive Oil",
                        nameEnglish = "Cultured Dairy-Free Plant Butter",
                        nameGerman = "Laktosefreie Pflanzenbutter oder Olivenöl"
                    ),
                    reasonEnglish = "Contains dairy fats and trace whey/casein milk proteins.",
                    reasonGerman = "Enthält Milchfett sowie Spuren von Kasein und Molkenprotein.",
                    culinaryTipEnglish = "Use high-quality olive oil for savory roasting or plant butter block for pastries.",
                    culinaryTipGerman = "Verwenden Sie natives Olivenöl für Herzhaftes und feste Pflanzenbutter für Gebäck."
                )
            }
            text.contains("milk") || text.contains("milch") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Oat Milk or Unsweetened Almond Milk",
                        nameEnglish = "Unsweetened Barista Oat or Almond Milk",
                        nameGerman = "Ungesüßte Hafer- oder Mandelmilch"
                    ),
                    reasonEnglish = "Contains lactose and cow's milk allergens.",
                    reasonGerman = "Enthält Laktose und Milcheiweiß.",
                    culinaryTipEnglish = "Almond milk has a clean, light flavor ideal for delicate pastries and custards.",
                    culinaryTipGerman = "Mandelmilch hat ein feines Aroma, das hervorragend zu feinen Kuchen passt."
                )
            }
            text.contains("heavy cream") || text.contains("whipping cream") || text.contains("schlagsahne") || text.contains("sahne") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Coconut Cream or Oat Cuisine",
                        nameEnglish = "Rich Coconut Cream or Dairy-Free Oat Cuisine",
                        nameGerman = "Laktosefreie Hafer-Cuisine oder Kokoscreme"
                    ),
                    reasonEnglish = "High dairy lactose and casein concentration.",
                    reasonGerman = "Hoher Gehalt an Laktose und Milcheiweiß.",
                    culinaryTipEnglish = "Simmer gently; oat cream will not curdle when mixed with citrus or tomato acid.",
                    culinaryTipGerman = "Gerinnt im Gegensatz zu Sahne auch bei Zugabe von Zitrone oder Tomaten nicht."
                )
            }
            text.contains("cheese") || text.contains("käse") || text.contains("quark") || text.contains("mascarpone") || text.contains("ricotta") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Dairy-Free Cashew Cheese or Soy Quark",
                        nameEnglish = "Cultured Cashew Cream Cheese or Soy Curd (Quark)",
                        nameGerman = "Pflanzlicher Quark (auf Sojabasis) oder Cashew-Frischkäse"
                    ),
                    reasonEnglish = "Contains concentrated dairy curd and casein.",
                    reasonGerman = "Besteht aus geronnenem Milchquark und Kasein.",
                    culinaryTipEnglish = "Strained soy yogurt mimics European Quark with identical tart creaminess.",
                    culinaryTipGerman = "Abgetropfter Sojajoghurt im Kaffeefilter ergibt perfekten laktosefreien Magerquark."
                )
            }
            else -> null
        }
    }

    // --- NUT-FREE SUBSTITUTIONS ---
    private fun findNutFreeSubstitution(ingredient: RecipeIngredient, text: String): IngredientSubstitution? {
        return when {
            text.contains("almond") || text.contains("mandeln") || text.contains("walnut") || text.contains("walnuss") ||
            text.contains("hazelnut") || text.contains("haselnuss") || text.contains("pecan") || text.contains("cashew") ||
            text.contains("peanut") || text.contains("erdnuss") || text.contains("pistachio") || text.contains("pistazie") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Roasted Sunflower & Pumpkin Seeds",
                        nameEnglish = "Roasted Sunflower Seeds & Pepitas (Pumpkin Seeds)",
                        nameGerman = "Geröstete Sonnenblumen- und Kürbiskerne"
                    ),
                    reasonEnglish = "Tree nuts and peanuts can trigger life-threatening anaphylactic reactions.",
                    reasonGerman = "Nüsse und Erdnüsse können schwere lebensbedrohliche allergische Schocks auslösen.",
                    culinaryTipEnglish = "Lightly toast pumpkin seeds in a dry skillet for identical nutty crunch and buttery richness.",
                    culinaryTipGerman = "Kürbiskerne trocken in der Pfanne anrösten für herrlich nussigen Crunch."
                )
            }
            text.contains("peanut butter") || text.contains("erdnussbutter") || text.contains("almond butter") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Roasted Sunflower Seed Butter (SunButter)",
                        nameEnglish = "Roasted Sunflower Seed Butter (SunButter)",
                        nameGerman = "Geröstetes Sonnenblumenkernmus (SunButter)"
                    ),
                    reasonEnglish = "Nut-derived spreads pose high cross-contact allergen hazards.",
                    reasonGerman = "Nussmuse bergen hohes Kreuzallergierisiko.",
                    culinaryTipEnglish = "SunButter matches the spreadability, protein density, and baking texture 1:1.",
                    culinaryTipGerman = "Sonnenblumenmus bietet die exakt gleiche Bindekraft und sämige Textur beim Backen."
                )
            }
            else -> null
        }
    }

    // --- EGG-FREE SUBSTITUTIONS ---
    private fun findEggFreeSubstitution(ingredient: RecipeIngredient, text: String): IngredientSubstitution? {
        return when {
            text.contains("egg") || text.contains("eier") || text.contains("ei ") -> {
                IngredientSubstitution(
                    original = ingredient,
                    replacement = ingredient.copy(
                        name = "Flaxseed Gel (1 tbsp ground flax + 2.5 tbsp water)",
                        nameEnglish = "Flaxseed Gel (1 tbsp ground golden flax + 2.5 tbsp warm water)",
                        nameGerman = "Leinsamen-Gel (1 EL gemahlene Leinsamen + 2,5 EL warmes Wasser)"
                    ),
                    reasonEnglish = "Common allergen in children and adults; provides emulsification and structure.",
                    reasonGerman = "Häufiges Allergen; liefert Feuchtigkeit und Teigelastizität.",
                    culinaryTipEnglish = "For cakes where light color matters, use golden flaxseed or 3 tbsp aquafaba.",
                    culinaryTipGerman = "Für helle Kuchenteige goldenen Leinsamen oder 3 EL Kichererbsenwasser verwenden."
                )
            }
            else -> null
        }
    }
}
