package com.example.data.model

object ShoppingCategorizer {

    fun categorizeIngredient(ingredientName: String): String {
        val lower = ingredientName.lowercase().trim()

        return when {
            // Pantry & Condiments / Staples (Priority over produce flavors e.g. "Apple Cider Vinegar", "Garlic Olive Oil")
            lower.containsAny(
                "vinegar", "essig", "mustard", "senf", "broth", "stock", "brühe", "fond",
                "wine", "wein", "beer", "bier", "rum", "honey", "honig", "jam",
                "marmelade", "konfitüre", "pasta", "nudeln", "spätzle", "rice", "reis",
                "oil", "öl", "olive", "schmalz"
            ) -> "Pantry & Staples"

            // Fresh Produce
            lower.containsAny(
                "apple", "apples", "apfel", "äpfel", "lemon", "lemons", "zitrone", "zitronen",
                "onion", "onions", "zwiebel", "zwiebeln", "garlic", "knoblauch",
                "potato", "potatoes", "kartoffel", "kartoffeln", "carrot", "carrots", "möhre", "möhren", "karotte", "karotten",
                "parsley", "petersilie", "herb", "herbs", "kräuter", "celery", "sellerie",
                "cabbage", "kohl", "sauerkraut", "mushroom", "mushrooms", "pilz", "pilze", "champignon", "champignons",
                "berry", "berries", "beere", "beeren", "cherry", "cherries", "kirsche", "kirschen",
                "plum", "plums", "zwetschge", "zwetschgen", "pflaume", "pflaumen",
                "raisin", "raisins", "rosine", "rosinen", "sultanine", "sultaninen",
                "tomato", "tomatoes", "tomate", "tomaten", "spinach", "spinat"
            ) -> "Fresh Produce"

            // Dairy & Refrigerated
            lower.containsAny(
                "butter", "milk", "milch", "egg", "eggs", "eier", "ei",
                "quark", "magerquark", "speisequark", "topfen", "cream", "sahne", "schmand", "crème",
                "cheese", "käse", "parmesan", "gouda", "sour cream", "sauerrahm",
                "buttermilch", "yogurt", "joghurt"
            ) -> "Dairy & Refrigerated"

            // Meat & Seafood
            lower.containsAny(
                "beef", "rind", "rinder", "pork", "schwein", "schweinebraten", "speck", "bacon",
                "veal", "kalb", "kalbfleisch", "chicken", "hähnchen", "huhn", "geflügel",
                "sausage", "sausages", "wurst", "würste", "bratwurst", "wiener", "meat", "fleisch", "hackfleisch",
                "fish", "fisch", "salmon", "lachs", "ham", "schinken"
            ) -> "Meat & Seafood"

            // Bakery & Bread
            lower.containsAny(
                "bread", "brot", "brötchen", "semmel", "semmeln", "roll", "rolls", "baguette",
                "toast", "pastry", "puff pastry", "blätterteig", "breadcrumbs",
                "semmelbrösel", "paniermehl"
            ) -> "Bakery & Bread"

            // Baking & Spices
            lower.containsAny(
                "flour", "mehl", "sugar", "zucker", "puderzucker", "yeast", "hefe",
                "vanilla", "vanille", "vanillezucker", "baking powder", "backpulver",
                "natron", "cinnamon", "zimt", "nutmeg", "muskat", "salt", "salz",
                "pepper", "pfeffer", "clove", "cloves", "nelke", "nelken", "allspice", "piment",
                "paprika", "caraway", "kümmel", "almond", "almonds", "mandel", "mandeln",
                "walnut", "walnuts", "walnuss", "walnüsse", "hazelnut", "hazelnuts", "haselnuss", "haselnüsse",
                "cocoa", "kakao", "chocolate", "schokolade", "cornstarch", "speisestärke"
            ) -> "Baking & Spices"

            else -> "Pantry & Staples"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { kw ->
            val pattern = Regex("(?:^|[^a-zA-ZäöüÄÖÜß])${Regex.escape(kw)}(?:$|[^a-zA-ZäöüÄÖÜß])", RegexOption.IGNORE_CASE)
            pattern.containsMatchIn(this) || (kw.length >= 6 && this.contains(kw, ignoreCase = true))
        }
    }
}
