package com.example.data.local

object DefaultRecipes {
    val DEMO_TITLES = setOf(
        "Grandma's Traditional Apple Strudel",
        "Traditional Sunday Roast with Yorkshire Puddings",
        "Classic Victoria Sponge Cake",
        "Creamy Roasted Tomato & Basil Soup",
        "Old-Fashioned Beef Stew with Herb Dumplings",
        "Traditional Cottage Pie with Cheddar Mash",
        "Artisanal Sourdough Bread",
        "Grandma's Famous Chocolate Chip Cookies"
    )

    fun getInitialRecipes(): List<RecipeEntity> {
        return emptyList()
    }
}
