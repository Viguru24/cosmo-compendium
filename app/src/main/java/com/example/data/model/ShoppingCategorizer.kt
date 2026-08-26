package com.example.data.model

object ShoppingCategorizer {

    fun categorizeIngredient(ingredientName: String): String {
        val lower = ingredientName.lowercase().trim()

        return when {
            // Fresh Produce
            lower.contains("apple") || lower.contains("apfel") ||
            lower.contains("lemon") || lower.contains("zitrone") ||
            lower.contains("onion") || lower.contains("zwiebel") ||
            lower.contains("garlic") || lower.contains("knoblauch") ||
            lower.contains("potato") || lower.contains("kartoffel") ||
            lower.contains("carrot") || lower.contains("möhre") || lower.contains("karotte") ||
            lower.contains("parsley") || lower.contains("petersilie") ||
            lower.contains("herb") || lower.contains("kraut") ||
            lower.contains("celery") || lower.contains("sellerie") ||
            lower.contains("cabbage") || lower.contains("kohl") || lower.contains("kraut") ||
            lower.contains("mushroom") || lower.contains("pilz") || lower.contains("champignon") ||
            lower.contains("berry") || lower.contains("beere") || lower.contains("cherry") || lower.contains("kirsche") ||
            lower.contains("plum") || lower.contains("zwetschge") || lower.contains("pflaume") ||
            lower.contains("raisin") || lower.contains("rosine") || lower.contains("sultanine") ||
            lower.contains("tomato") || lower.contains("tomate") || lower.contains("spinach") || lower.contains("spinat") -> "Fresh Produce"

            // Dairy & Refrigerated
            lower.contains("butter") || lower.contains("milk") || lower.contains("milch") ||
            lower.contains("egg") || lower.contains("ei") || lower.contains("eier") ||
            lower.contains("quark") || lower.contains("topfen") ||
            lower.contains("cream") || lower.contains("sahne") || lower.contains("schmand") || lower.contains("crème") ||
            lower.contains("cheese") || lower.contains("käse") || lower.contains("parmesan") || lower.contains("gouda") ||
            lower.contains("sour cream") || lower.contains("sauerrahm") || lower.contains("buttermilch") ||
            lower.contains("yogurt") || lower.contains("joghurt") -> "Dairy & Refrigerated"

            // Meat & Seafood
            lower.contains("beef") || lower.contains("rind") || lower.contains("rinder") ||
            lower.contains("pork") || lower.contains("schwein") || lower.contains("speck") || lower.contains("bacon") ||
            lower.contains("veal") || lower.contains("kalb") || lower.contains("kalbfleisch") ||
            lower.contains("chicken") || lower.contains("hähnchen") || lower.contains("huhn") || lower.contains("geflügel") ||
            lower.contains("sausage") || lower.contains("wurst") || lower.contains("bratwurst") || lower.contains("wiener") ||
            lower.contains("meat") || lower.contains("fleisch") || lower.contains("hackfleisch") ||
            lower.contains("fish") || lower.contains("fisch") || lower.contains("salmon") || lower.contains("lachs") ||
            lower.contains("ham") || lower.contains("schinken") -> "Meat & Seafood"

            // Bakery & Bread
            lower.contains("bread") || lower.contains("brot") || lower.contains("brötchen") || lower.contains("semmel") ||
            lower.contains("roll") || lower.contains("baguette") || lower.contains("toast") ||
            lower.contains("pastry") || lower.contains("puff pastry") || lower.contains("blätterteig") ||
            lower.contains("breadcrumbs") || lower.contains("semmelbrösel") || lower.contains("paniermehl") -> "Bakery & Bread"

            // Baking & Spices
            lower.contains("flour") || lower.contains("mehl") ||
            lower.contains("sugar") || lower.contains("zucker") || lower.contains("puderzucker") ||
            lower.contains("yeast") || lower.contains("hefe") ||
            lower.contains("vanilla") || lower.contains("vanille") || lower.contains("vanillezucker") ||
            lower.contains("baking powder") || lower.contains("backpulver") || lower.contains("natron") ||
            lower.contains("cinnamon") || lower.contains("zimt") ||
            lower.contains("nutmeg") || lower.contains("muskat") ||
            lower.contains("salt") || lower.contains("salz") ||
            lower.contains("pepper") || lower.contains("pfeffer") ||
            lower.contains("clove") || lower.contains("nelke") ||
            lower.contains("allspice") || lower.contains("piment") ||
            lower.contains("paprika") || lower.contains("caraway") || lower.contains("kümmel") ||
            lower.contains("almond") || lower.contains("mandel") || lower.contains("walnut") || lower.contains("walnuss") ||
            lower.contains("hazelnut") || lower.contains("haselnuss") || lower.contains("cocoa") || lower.contains("kakao") ||
            lower.contains("chocolate") || lower.contains("schokolade") || lower.contains("cornstarch") || lower.contains("speisestärke") -> "Baking & Spices"

            // Pantry & Canned
            lower.contains("oil") || lower.contains("öl") || lower.contains("olive") || lower.contains("schmalz") ||
            lower.contains("vinegar") || lower.contains("essig") ||
            lower.contains("mustard") || lower.contains("senf") ||
            lower.contains("broth") || lower.contains("stock") || lower.contains("brühe") || lower.contains("fond") ||
            lower.contains("wine") || lower.contains("wein") || lower.contains("beer") || lower.contains("bier") || lower.contains("rum") ||
            lower.contains("honey") || lower.contains("honig") || lower.contains("jam") || lower.contains("marmelade") || lower.contains("konfitüre") ||
            lower.contains("pasta") || lower.contains("nudeln") || lower.contains("spätzle") || lower.contains("rice") || lower.contains("reis") -> "Pantry & Staples"

            else -> "Pantry & Staples"
        }
    }
}
