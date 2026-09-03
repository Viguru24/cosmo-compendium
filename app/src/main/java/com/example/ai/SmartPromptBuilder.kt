package com.example.ai

import com.example.data.local.RecipeEntity

object SmartPromptBuilder {

    fun buildRecipeExtractionPrompt(recipeText: String? = null): String {
        val base = """
            You are an expert master chef, archivist, and formulation specialist specializing in recipe cards, craft formulas (such as soap making, balms, cosmetics, and household preparations), printed recipes, and handwritten compendiums.

            TASK:
            Analyze the provided recipe or formula card photo(s) and/or text. Accurately transcribe and translate the entire entry into clear, structured JSON.

            CRITICAL INSTRUCTIONS:
            1. STRICT FIDELITY & COMPLETE INGREDIENT EXTRACTION:
               - Extract EVERY SINGLE INGREDIENT or component mentioned, listed, or implied.
               - Do NOT skip any ingredient (extract all flours, sugars, oils, lye, scents, eggs, butter, spices, extracts, fruits, nuts, leavening agents, liquids, etc.).
               - For each ingredient, capture amount, unit, nameEnglish, group, and isOptional.
               - When ingredients include brand names or specific descriptors, keep them intact (e.g. "6-ounce package Ocean Spray Craisins").

            2. UNIVERSAL MULTI-LANGUAGE SUPPORT & GLOBAL TRANSLATION:
               - The recipe card or text may be in ANY language worldwide (e.g., German, French, Italian, Spanish, Portuguese, Polish, Russian, Ukrainian, Dutch, Swedish, Danish, Japanese, Chinese, Arabic, Hindi, Greek, etc., or English), including vintage cursive or handwritten notes (such as German Kurrent/Sütterlin).
               - Accurately preserve original language names in the original fields ("nameGerman", "titleGerman", "instructionGerman", "notesGerman").
               - Accurately translate the recipe into clear, natural culinary English in "nameEnglish", "titleEnglish", "instructionEnglish", "notesEnglish".
               - If the recipe is already in English, populate both identically.

            3. STEP TRANSCRIPTION:
               - Provide clear, numbered steps in sequential order.
               - Extract timer duration in minutes into "timerMinutes" if mentioned.

            4. METADATA:
               - Estimate "prepTimeMinutes" and "cookTimeMinutes" if not explicitly stated.
               - Categorize into one of: "Baking & Desserts", "Main Dishes", "Salads & Starters", "Soups & Stews", "Sauces & Condiments", "Beverages", "Snacks & Appetizers".
               - Set "difficulty" to "Easy", "Medium", or "Hard".
               - Set "detectedSourceLanguage" to the 2-letter ISO 639-1 language code (e.g. "en", "de", "fr", "it", "es", "pl", "ru", "uk", "sv", "nl", "ja", "zh", "el", etc.).

            5. DISH PHOTOGRAPH DETECTION:
               - Check if any provided photo contains an actual picture/photo of the cooked food/dish.
               - If present, set "hasFoodPhoto": true and "foodPhotoBox" with coordinates [ymin, xmin, ymax, xmax] in 0..1000 scale and "pageIndex".

            OUTPUT FORMAT: Return ONLY valid JSON adhering strictly to the schema.
        """.trimIndent()
        return if (!recipeText.isNullOrBlank()) "$base\n\nRecipe Text:\n$recipeText" else base
    }

    /**
     * Builds an intelligent, dish-accurate culinary prompt tailored by dish category, title, and ingredients.
     * Prevents generating plates/stew for syrups and drinks, and elevates baked goods and main dishes.
     */
    fun buildSmartCulinaryPrompt(
        title: String,
        titleGerman: String? = null,
        category: String = "",
        ingredients: List<String> = emptyList(),
        steps: List<String> = emptyList(),
        notes: String? = null,
        customPrompt: String? = null
    ): String {
        if (!customPrompt.isNullOrBlank()) {
            return customPrompt.trim()
        }

        val deTitle = titleGerman ?: ""
        val dishName = listOf(title, deTitle).filter { it.isNotBlank() }.distinct().joinToString(" / ")
        val lowerTitle = (title + " " + deTitle).lowercase()
        val lowerCat = category.lowercase()

        // =========================================================================
        // SPECIALIZED HERITAGE & ICONIC REGIONAL DISH KNOWLEDGE
        // =========================================================================

        // 1. South African Bobotie
        if (lowerTitle.contains("bobotie")) {
            return "Authentic traditional South African Bobotie casserole baked in a rustic ceramic gratin dish, featuring a rich aromatic curry-spiced minced meat base topped with a smooth, golden-baked savory egg and milk custard crust, decorated with glossy green bay leaves baked into the golden surface, served alongside a side of fragrant turmeric yellow rice with plump raisins, a small ramekin of golden peach or mango chutney, and sliced banana sambal, warm natural overhead window lighting, gourmet culinary editorial photography, hyper-realistic, no text"
        }

        // 2. Turkish / Balkan Börek (Spiral or layered phyllo)
        if (lowerTitle.contains("börek") || lowerTitle.contains("borek") || lowerTitle.contains("bourek")) {
            return "Traditional artisanal baked Börek pastry, crisp golden-brown flaky spiral phyllo pastry crust blistered to perfection, sprinkled with black nigella seeds and toasted sesame seeds, resting in a vintage round copper baking pan, served with fresh herbs, rustic wooden table, gourmet Mediterranean food styling, soft natural light, no text"
        }

        // 3. German Anise Cookies with 'Feet' (Änisbrötli / Springerle)
        if (lowerTitle.contains("anise") || lowerTitle.contains("anis") || lowerTitle.contains("springerle")) {
            return "Artisanal traditional German Anisbrötli Springerle cookies, crisp ivory-white tops with intricate embossed relief folk art patterns, rising above delicate puffed airy sponge 'feet' (Füßchen) bases, delicately displayed on vintage parchment paper beside whole star anise pods, soft bakery lighting, artisanal German pastry photography, no text"
        }

        // 4. Kaiserschmarrn (Austrian Shredded Caramelized Pancake)
        if (lowerTitle.contains("kaiserschmarrn") || lowerTitle.contains("schmarrn")) {
            return "Authentic Austrian Kaiserschmarrn, fluffy golden-brown torn caramelized shredded pancake pieces dusted with snow-white powdered sugar, served hot in a rustic black cast-iron skillet, alongside a small white ceramic bowl of glistening ruby-red plum compote (Zwetschgenröster), alpine lodge styling, cozy morning light, no text"
        }

        // 5. Sauerbraten / Rouladen (German Braised Classics)
        if (lowerTitle.contains("sauerbraten") || lowerTitle.contains("rouladen") || lowerTitle.contains("roulade")) {
            return "Tender, slow-braised traditional German $dishName, draped in a rich, dark glossy spiced gravy, served on an elegant vintage porcelain plate with two velvety potato dumplings (Kartoffelklöße) and braised spiced red cabbage (Rotkohl), soft steam rising, cozy dining table lighting, gourmet food photography, no text"
        }

        // 6. Syrups, Cordials, Vinegars, Infusions & Extracts
        if (lowerTitle.contains("syrup") || lowerTitle.contains("sirup") || lowerTitle.contains("cordial") || 
            lowerTitle.contains("extract") || lowerTitle.contains("essenz") || lowerTitle.contains("tinktur")) {
            val keyBotanical = when {
                lowerTitle.contains("elderflower") || lowerTitle.contains("holunder") -> "fresh delicate white elderflower blossoms (Holunderblüten) and fresh lemon slices"
                lowerTitle.contains("lavender") || lowerTitle.contains("lavendel") -> "fresh purple lavender sprigs"
                lowerTitle.contains("ginger") || lowerTitle.contains("ingwer") -> "fresh ginger root slices and lemon"
                lowerTitle.contains("mint") || lowerTitle.contains("minze") || lowerTitle.contains("pfefferminz") -> "fresh vibrant green mint leaves"
                lowerTitle.contains("rose") || lowerTitle.contains("rosen") -> "fragrant pink rose petals"
                lowerTitle.contains("vanilla") || lowerTitle.contains("vanille") -> "whole dark vanilla bean pods"
                lowerTitle.contains("berry") || lowerTitle.contains("beeren") -> "fresh ripe seasonal berries"
                else -> "fresh botanical herbs and citrus slices"
            }
            return "Professional culinary photograph of homemade $dishName, filled inside a beautiful clear vintage glass bottle with a ceramic swing-top stopper, translucent golden syrup glowing in soft natural morning sunlight, surrounded by $keyBotanical, rustic distressed wooden farmhouse table, clean artistic culinary magazine cover photography, soft bokeh background, hyper-realistic details, no text, no labels"
        }

        // 2. Jams, Marmalades, Chutneys & Spreads
        if (lowerTitle.contains("jam") || lowerTitle.contains("marmelade") || lowerTitle.contains("marmalade") || 
            lowerTitle.contains("chutney") || lowerTitle.contains("konfitüre") || lowerTitle.contains("gelee") || lowerTitle.contains("jelly") || (lowerTitle.contains("butter") && lowerCat.contains("spread"))) {
            return "Professional culinary photograph of artisanal $dishName, presented in an open rustic glass canning jar with clamp lid, a vintage silver or wooden spoon resting alongside with glistening glossy fruit preserve spread, fresh fruit ingredients scattered nearby on a sunlit weathered wooden kitchen counter, gourmet food magazine editorial"
        }

        // 3. Beverages, Cocktails, Teas & Smoothies
        if (lowerCat.contains("beverage") || lowerCat.contains("drink") || lowerCat.contains("cocktail") || lowerCat.contains("getränk") || lowerCat.contains("tea") || lowerCat.contains("tee") || lowerTitle.contains("punch") || lowerTitle.contains("bowle") || lowerTitle.contains("spritz") || lowerTitle.contains("smoothie") || lowerTitle.contains("limonade") || lowerTitle.contains("lemonade")) {
            return "Stunning culinary portrait of refreshing $dishName, served in an elegant crystal glass with clear ice cubes, fresh botanical garnish and citrus wheel on the rim, backlit by warm golden hour sunlight, droplets of condensation on the cold glass, rustic wooden bar surface, cinematic shallow depth of field, gourmet drink photography"
        }

        // 4. Baking, Cakes, Tarts, Pastries & Pies
        if (lowerCat.contains("baking") || lowerCat.contains("backen") || lowerCat.contains("dessert") || lowerTitle.contains("cake") || lowerTitle.contains("kuchen") || lowerTitle.contains("torte") || lowerTitle.contains("pie") || lowerTitle.contains("tart") || lowerTitle.contains("cookie") || lowerTitle.contains("plätzchen") || lowerTitle.contains("muffin") || lowerTitle.contains("croissant")) {
            val cakeServing = if (lowerTitle.contains("cookie") || lowerTitle.contains("plätzchen")) "artisanal stack on parchment paper" else "on an elegant vintage ceramic cake pedestal stand, one perfect slice cut revealing internal textures"
            return "Mouth-watering culinary photograph of freshly baked $dishName, $cakeServing, dusting of powdered sugar, warm natural bakery lighting, crisp crumb and golden-brown crust, rustic farmhouse wooden table, gourmet pastry photography, shallow depth of field"
        }

        // 5. Bread, Sourdough & Rolls
        if (lowerCat.contains("bread") || lowerCat.contains("brot") || lowerTitle.contains("bread") || lowerTitle.contains("brot") || lowerTitle.contains("baguette") || lowerTitle.contains("brötchen") || lowerTitle.contains("focaccia") || lowerTitle.contains("sourdough") || lowerTitle.contains("sauerteig")) {
            return "Artisan bakery photograph of freshly baked crusty $dishName, beautiful scored blistered golden crust, dusted with flour, rustic linen towel, bread knife and butter dish nearby on a dark wood baker's bench, soft side lighting highlighting deep crust texture"
        }

        // 6. Soups, Stews & Broths
        if (lowerCat.contains("soup") || lowerCat.contains("stew") || lowerCat.contains("suppe") || lowerCat.contains("eintopf") || lowerTitle.contains("soup") || lowerTitle.contains("suppe") || lowerTitle.contains("stew") || lowerTitle.contains("goulash") || lowerTitle.contains("gulasch") || lowerTitle.contains("curry")) {
            return "Steaming hearty bowl of freshly cooked $dishName, served in a handcrafted rustic earthenware bowl, fresh herb garnish on top, warm sourdough bread slice on the side, soft cozy natural lighting, gentle steam rising, gourmet food photography"
        }

        // 7. General Main Dishes, Roasts, Pasta & Salads
        val ingSummary = if (ingredients.isNotEmpty()) ingredients.take(5).joinToString(", ") else ""
        val ingClause = if (ingSummary.isNotBlank()) "with $ingSummary" else ""
        return "Professional culinary photograph of freshly prepared $dishName ($category) $ingClause, beautifully plated on an artisanal handcrafted ceramic dish, warm dark wooden dining table, natural soft morning window lighting, gentle rising steam, sharp focus on appetizing food texture, shallow depth of field, gourmet food magazine editorial"
    }

    fun buildPromptForRecipe(recipe: RecipeEntity, customPrompt: String? = null): String {
        return buildSmartCulinaryPrompt(
            title = recipe.title,
            titleGerman = recipe.titleGerman,
            category = recipe.category,
            ingredients = recipe.ingredients.map { "${it.amount} ${it.unit} ${it.name}".trim() },
            steps = recipe.steps.take(4).map { it.getInstruction() },
            notes = recipe.notes.ifBlank { recipe.originStory },
            customPrompt = customPrompt
        )
    }
}
