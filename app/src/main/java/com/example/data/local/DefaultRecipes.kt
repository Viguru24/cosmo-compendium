package com.example.data.local

import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep

object DefaultRecipes {
    fun getInitialRecipes(): List<RecipeEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            RecipeEntity(
                id = 1,
                title = "Grandma's Traditional Apple Strudel",
                titleGerman = "Grandma's Traditional Apple Strudel",
                titleEnglish = "Grandma's Traditional Apple Strudel",
                category = "Baking & Desserts",
                servings = "8 slices",
                prepTimeMinutes = 45,
                cookTimeMinutes = 40,
                difficulty = "Medium",
                ingredients = listOf(
                    RecipeIngredient(name = "All-Purpose Flour", amount = "250", unit = "g", nameEnglish = "All-Purpose Flour", group = "Dough"),
                    RecipeIngredient(name = "Warm Water", amount = "100", unit = "ml", nameEnglish = "Warm Water", group = "Dough"),
                    RecipeIngredient(name = "Vegetable Oil", amount = "2", unit = "tbsp", nameEnglish = "Vegetable Oil", group = "Dough"),
                    RecipeIngredient(name = "Pinch of Salt", amount = "1", unit = "pinch", nameEnglish = "Pinch of Salt", group = "Dough"),
                    RecipeIngredient(name = "Tart Baking Apples (Granny Smith or Bramley)", amount = "1", unit = "kg", nameEnglish = "Tart Baking Apples", group = "Filling"),
                    RecipeIngredient(name = "Granulated Sugar", amount = "80", unit = "g", nameEnglish = "Granulated Sugar", group = "Filling"),
                    RecipeIngredient(name = "Ground Cinnamon", amount = "1.5", unit = "tsp", nameEnglish = "Ground Cinnamon", group = "Filling"),
                    RecipeIngredient(name = "Rum-soaked Raisins", amount = "60", unit = "g", nameEnglish = "Rum-soaked Raisins", isOptional = true, group = "Filling"),
                    RecipeIngredient(name = "Butter-toasted Breadcrumbs", amount = "60", unit = "g", nameEnglish = "Butter-toasted Breadcrumbs", group = "Filling"),
                    RecipeIngredient(name = "Melted Butter (for brushing)", amount = "75", unit = "g", nameEnglish = "Melted Butter", group = "Finishing"),
                    RecipeIngredient(name = "Powdered Sugar for dusting", amount = "2", unit = "tbsp", nameEnglish = "Powdered Sugar", group = "Finishing")
                ),
                steps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionEnglish = "Knead flour, warm water, oil, and salt into a silky, smooth dough. Form a ball, brush with oil, cover with a warm bowl, and let rest for 30 minutes.",
                        instructionGerman = "Knead flour, warm water, oil, and salt into a silky, smooth dough. Form a ball, brush with oil, cover with a warm bowl, and let rest for 30 minutes.",
                        timerMinutes = 30,
                        tip = "The dough must rest under a warm bowl to stretch paper-thin without tearing."
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionEnglish = "Peel, core, and thinly slice the apples. Toss with sugar, ground cinnamon, rum raisins, and lemon juice.",
                        instructionGerman = "Peel, core, and thinly slice the apples. Toss with sugar, ground cinnamon, rum raisins, and lemon juice.",
                        timerMinutes = 0,
                        tip = "Use tart, firm baking apples for the best balance of sweetness and texture."
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionEnglish = "Melt 30g butter in a skillet and gently toast the breadcrumbs until golden brown and fragrant.",
                        instructionGerman = "Melt 30g butter in a skillet and gently toast the breadcrumbs until golden brown and fragrant.",
                        timerMinutes = 5,
                        tip = "The breadcrumbs soak up apple juices to keep the bottom pastry crisp."
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionEnglish = "Lay a clean cotton kitchen towel on the table and dust with flour. Roll the dough thinly, then gently stretch using the backs of your hands until translucent (you should be able to read through it).",
                        instructionGerman = "Lay a clean cotton kitchen towel on the table and dust with flour. Roll the dough thinly, then gently stretch using the backs of your hands until translucent.",
                        timerMinutes = 10,
                        tip = "Stretch slowly and evenly from the center outwards."
                    ),
                    RecipeStep(
                        stepNumber = 5,
                        instructionEnglish = "Brush the stretched dough with melted butter. Scatter toasted breadcrumbs over two-thirds of the area, then spread the spiced apple mixture on top.",
                        instructionGerman = "Brush the stretched dough with melted butter. Scatter toasted breadcrumbs over two-thirds of the area, then spread the spiced apple mixture on top.",
                        timerMinutes = 0
                    ),
                    RecipeStep(
                        stepNumber = 6,
                        instructionEnglish = "Roll up the strudel tightly using the kitchen towel. Transfer seam-side down to a lined baking sheet and brush generously with melted butter.",
                        instructionGerman = "Roll up the strudel tightly using the kitchen towel. Transfer seam-side down to a lined baking sheet and brush generously with melted butter.",
                        timerMinutes = 0
                    ),
                    RecipeStep(
                        stepNumber = 7,
                        instructionEnglish = "Bake at 190°C (375°F) for 35-40 minutes until deeply golden and flaky. Dust with powdered sugar while warm and serve with warm vanilla custard or cream.",
                        instructionGerman = "Bake at 190°C (375°F) for 35-40 minutes until deeply golden and flaky. Dust with powdered sugar while warm and serve with warm vanilla custard or cream.",
                        timerMinutes = 38,
                        tip = "Brush with more melted butter halfway through baking for extra crispness."
                    )
                ),
                notes = "Grandmother's secret was soaking the raisins in dark rum overnight and rolling the dough on her vintage cotton tablecloth.",
                notesGerman = "Grandmother's secret was soaking the raisins in dark rum overnight and rolling the dough on her vintage cotton tablecloth.",
                sourceLanguage = "en",
                coverTheme = "WARM_TERRACOTTA",
                isFavorite = true,
                rating = 5,
                timesCooked = 12,
                originStory = "Passed down from grandmother's handwritten recipe collection.",
                createdAt = now - 100000
            ),
            RecipeEntity(
                id = 2,
                title = "Traditional Sunday Roast with Yorkshire Puddings",
                titleGerman = "Traditional Sunday Roast with Yorkshire Puddings",
                titleEnglish = "Traditional Sunday Roast with Yorkshire Puddings",
                category = "Main Dishes",
                servings = "6 servings",
                prepTimeMinutes = 30,
                cookTimeMinutes = 90,
                difficulty = "Medium",
                ingredients = listOf(
                    RecipeIngredient(name = "Prime Rib Roast or Beef Topside", amount = "1.5", unit = "kg", nameEnglish = "Prime Rib Roast or Beef Topside", group = "Roast Beef"),
                    RecipeIngredient(name = "English Mustard Powder", amount = "1", unit = "tbsp", nameEnglish = "English Mustard Powder", group = "Roast Beef"),
                    RecipeIngredient(name = "Fresh Rosemary & Thyme", amount = "4", unit = "sprigs", nameEnglish = "Fresh Rosemary & Thyme", group = "Roast Beef"),
                    RecipeIngredient(name = "Garlic Cloves (crushed)", amount = "6", unit = "cloves", nameEnglish = "Garlic Cloves", group = "Roast Beef"),
                    RecipeIngredient(name = "Roasting Potatoes (King Edward or Russet)", amount = "1.2", unit = "kg", nameEnglish = "Roasting Potatoes", group = "Roast Potatoes"),
                    RecipeIngredient(name = "Beef Dripping or Goose Fat", amount = "4", unit = "tbsp", nameEnglish = "Beef Dripping or Goose Fat", group = "Roast Potatoes"),
                    RecipeIngredient(name = "Large Eggs", amount = "4", unit = "large", nameEnglish = "Large Eggs", group = "Yorkshire Puddings"),
                    RecipeIngredient(name = "All-Purpose Flour", amount = "200", unit = "g", nameEnglish = "All-Purpose Flour", group = "Yorkshire Puddings"),
                    RecipeIngredient(name = "Whole Milk", amount = "200", unit = "ml", nameEnglish = "Whole Milk", group = "Yorkshire Puddings"),
                    RecipeIngredient(name = "Red Wine & Rich Beef Stock", amount = "350", unit = "ml", nameEnglish = "Red Wine & Rich Beef Stock", group = "Gravy")
                ),
                steps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionEnglish = "Bring beef to room temperature for 1 hour. Rub with mustard powder, crushed garlic, sea salt, cracked black pepper, and olive oil.",
                        instructionGerman = "Bring beef to room temperature for 1 hour. Rub with mustard powder, crushed garlic, sea salt, cracked black pepper, and olive oil.",
                        timerMinutes = 60,
                        tip = "Never roast cold meat directly from the fridge."
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionEnglish = "Preheat oven to 220°C (425°F). Roast beef for 20 minutes to sear, then reduce to 170°C (340°F) and roast 15 minutes per 500g for medium-rare.",
                        instructionGerman = "Preheat oven to 220°C (425°F). Roast beef for 20 minutes to sear, then reduce to 170°C (340°F) and roast 15 minutes per 500g for medium-rare.",
                        timerMinutes = 65,
                        tip = "Internal meat thermometer should read 54°C (130°F) for tender pink meat."
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionEnglish = "Parboil potatoes in salted water for 8 minutes. Drain, shake vigorously in the colander to rough up the edges, and roast in smoking hot beef dripping for 45 minutes until ultra-crispy.",
                        instructionGerman = "Parboil potatoes in salted water for 8 minutes. Drain, shake vigorously in the colander to rough up the edges, and roast in smoking hot beef dripping for 45 minutes until ultra-crispy.",
                        timerMinutes = 45,
                        tip = "Fluffing the edges in the colander creates maximum crunchy glass-like crust."
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionEnglish = "Whisk eggs, flour, milk, and salt until completely smooth. Let batter rest at room temperature. Pour a dash of oil into a 12-hole muffin tin and heat until smoking at 220°C. Pour in batter and bake for 22 minutes without opening the oven door.",
                        instructionGerman = "Whisk eggs, flour, milk, and salt until completely smooth. Let batter rest at room temperature. Pour a dash of oil into a 12-hole muffin tin and heat until smoking at 220°C. Pour in batter and bake for 22 minutes without opening the oven door.",
                        timerMinutes = 22,
                        tip = "Never open the oven door during the first 20 minutes or Yorkshire puddings will collapse."
                    ),
                    RecipeStep(
                        stepNumber = 5,
                        instructionEnglish = "Deglaze roasting pan juices with red wine, beef stock, and a sprig of thyme. Simmer down into a glossy, rich gravy.",
                        instructionGerman = "Deglaze roasting pan juices with red wine, beef stock, and a sprig of thyme. Simmer down into a glossy, rich gravy.",
                        timerMinutes = 10
                    )
                ),
                notes = "A traditional Sunday staple. Always rest the beef wrapped in foil for at least 20 minutes before carving.",
                notesGerman = "A traditional Sunday staple. Always rest the beef wrapped in foil for at least 20 minutes before carving.",
                sourceLanguage = "en",
                coverTheme = "VINTAGE_LEATHER",
                isFavorite = true,
                rating = 5,
                timesCooked = 18,
                originStory = "Sunday dinner family tradition recorded in 1974.",
                createdAt = now - 200000
            ),
            RecipeEntity(
                id = 3,
                title = "Handmade Cheese Egg Noodles with Crispy Caramelized Onions",
                titleGerman = "Handmade Cheese Egg Noodles with Crispy Caramelized Onions",
                titleEnglish = "Handmade Cheese Egg Noodles with Crispy Caramelized Onions",
                category = "Main Dishes",
                servings = "4 generous bowls",
                prepTimeMinutes = 25,
                cookTimeMinutes = 20,
                difficulty = "Easy",
                ingredients = listOf(
                    RecipeIngredient(name = "Coarse Wheat Flour", amount = "400", unit = "g", nameEnglish = "Coarse Wheat Flour", group = "Noodle Dough"),
                    RecipeIngredient(name = "Fresh Farm Eggs", amount = "4", unit = "large", nameEnglish = "Fresh Farm Eggs", group = "Noodle Dough"),
                    RecipeIngredient(name = "Sparkling Water", amount = "100", unit = "ml", nameEnglish = "Sparkling Water", group = "Noodle Dough"),
                    RecipeIngredient(name = "Aged Alpine Cheese (Gruyère or Bergkäse)", amount = "150", unit = "g", nameEnglish = "Aged Alpine Cheese", group = "Cheese"),
                    RecipeIngredient(name = "Shredded Emmental Cheese", amount = "150", unit = "g", nameEnglish = "Shredded Emmental Cheese", group = "Cheese"),
                    RecipeIngredient(name = "Yellow Onions (thinly sliced)", amount = "3", unit = "large", nameEnglish = "Yellow Onions", group = "Caramelized Onions"),
                    RecipeIngredient(name = "Pure Butter", amount = "50", unit = "g", nameEnglish = "Pure Butter", group = "Caramelized Onions"),
                    RecipeIngredient(name = "Fresh Chopped Chives", amount = "1", unit = "bunch", nameEnglish = "Fresh Chopped Chives", group = "Garnish")
                ),
                steps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionEnglish = "Beat flour, eggs, sparkling water, and salt with a wooden spoon until bubbles form and the dough stretches viscously.",
                        instructionGerman = "Beat flour, eggs, sparkling water, and salt with a wooden spoon until bubbles form and the dough stretches viscously.",
                        timerMinutes = 8,
                        tip = "Beat vigorously until air pockets form in the dough."
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionEnglish = "Caramelize onion rings slowly in butter with a pinch of paprika and salt until deeply golden brown and crisp.",
                        instructionGerman = "Caramelize onion rings slowly in butter with a pinch of paprika and salt until deeply golden brown and crisp.",
                        timerMinutes = 15
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionEnglish = "Drop noodles into boiling salted water in batches. When they float to the top (about 1-2 mins), lift out with a slotted spoon.",
                        instructionGerman = "Drop noodles into boiling salted water in batches. When they float to the top (about 1-2 mins), lift out with a slotted spoon.",
                        timerMinutes = 2
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionEnglish = "Layer steaming hot noodles in a warm baking dish alternately with grated Alpine and Emmental cheeses. Top with crispy onions and freshly snipped chives. Serve immediately with long strings of melted cheese!",
                        instructionGerman = "Layer steaming hot noodles in a warm baking dish alternately with grated Alpine and Emmental cheeses. Top with crispy onions and freshly snipped chives.",
                        timerMinutes = 0,
                        tip = "Mix sharp aged cheese for flavor and Emmental for glorious stretch."
                    )
                ),
                notes = "Authentic handmade comfort food. Best served piping hot with a crisp side salad.",
                notesGerman = "Authentic handmade comfort food. Best served piping hot with a crisp side salad.",
                sourceLanguage = "en",
                coverTheme = "FOREST_SAGE",
                isFavorite = true,
                rating = 5,
                timesCooked = 24,
                originStory = "Traditional alpine comfort food recipe from grandmother's kitchen.",
                createdAt = now - 300000
            ),
            RecipeEntity(
                id = 4,
                title = "Classic Victoria Sponge Cake",
                titleGerman = "Classic Victoria Sponge Cake",
                titleEnglish = "Classic Victoria Sponge Cake",
                category = "Baking & Desserts",
                servings = "8-10 slices",
                prepTimeMinutes = 20,
                cookTimeMinutes = 25,
                difficulty = "Easy",
                ingredients = listOf(
                    RecipeIngredient(name = "Unsalted Butter (softened)", amount = "200", unit = "g", nameEnglish = "Unsalted Butter (softened)", group = "Cake Sponge"),
                    RecipeIngredient(name = "Caster Sugar", amount = "200", unit = "g", nameEnglish = "Caster Sugar", group = "Cake Sponge"),
                    RecipeIngredient(name = "Eggs (beaten)", amount = "4", unit = "large", nameEnglish = "Eggs (beaten)", group = "Cake Sponge"),
                    RecipeIngredient(name = "Self-Raising Flour", amount = "200", unit = "g", nameEnglish = "Self-Raising Flour", group = "Cake Sponge"),
                    RecipeIngredient(name = "Pure Vanilla Extract", amount = "1", unit = "tsp", nameEnglish = "Pure Vanilla Extract", group = "Cake Sponge"),
                    RecipeIngredient(name = "Quality Strawberry Jam", amount = "150", unit = "g", nameEnglish = "Quality Strawberry Jam", group = "Filling"),
                    RecipeIngredient(name = "Double Cream (whipped)", amount = "200", unit = "ml", nameEnglish = "Double Cream (whipped)", group = "Filling"),
                    RecipeIngredient(name = "Icing Sugar (for dusting)", amount = "2", unit = "tbsp", nameEnglish = "Icing Sugar", group = "Finishing")
                ),
                steps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionEnglish = "Cream softened butter and caster sugar together until pale, fluffy, and light as air.",
                        instructionGerman = "Cream softened butter and caster sugar together until pale, fluffy, and light as air.",
                        timerMinutes = 5
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionEnglish = "Gradually add beaten eggs, one at a time, along with vanilla extract and a spoonful of flour to prevent curdling.",
                        instructionGerman = "Gradually add beaten eggs, one at a time, along with vanilla extract and a spoonful of flour to prevent curdling.",
                        timerMinutes = 3
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionEnglish = "Fold in the sifted flour with a metal spoon until just combined. Divide evenly between two lined 20cm (8-inch) cake tins.",
                        instructionGerman = "Fold in the sifted flour with a metal spoon until just combined. Divide evenly between two lined 20cm (8-inch) cake tins.",
                        timerMinutes = 0
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionEnglish = "Bake at 190°C (375°F) for 20-25 minutes until golden and the sponges spring back when lightly touched.",
                        instructionGerman = "Bake at 190°C (375°F) for 20-25 minutes until golden and the sponges spring back when lightly touched.",
                        timerMinutes = 23,
                        tip = "Let cool completely on a wire rack before assembling."
                    ),
                    RecipeStep(
                        stepNumber = 5,
                        instructionEnglish = "Spread generous strawberry jam over one sponge, top with freshly whipped cream, sandwich with the second sponge, and dust with icing sugar.",
                        instructionGerman = "Spread generous strawberry jam over one sponge, top with freshly whipped cream, sandwich with the second sponge, and dust with icing sugar.",
                        timerMinutes = 0
                    )
                ),
                notes = "The quintessential afternoon tea cake named after Queen Victoria. Perfect alongside a pot of hot tea.",
                notesGerman = "The quintessential afternoon tea cake named after Queen Victoria. Perfect alongside a pot of hot tea.",
                sourceLanguage = "en",
                coverTheme = "FLORAL_LINEN",
                isFavorite = false,
                rating = 5,
                timesCooked = 9,
                originStory = "Traditional afternoon tea recipe.",
                createdAt = now - 400000
            ),
            RecipeEntity(
                id = 5,
                title = "Authentic Black Forest Cherry Torte",
                titleGerman = "Authentic Black Forest Cherry Torte",
                titleEnglish = "Authentic Black Forest Cherry Torte",
                category = "Baking & Desserts",
                servings = "12 slices",
                prepTimeMinutes = 60,
                cookTimeMinutes = 35,
                difficulty = "Advanced",
                ingredients = listOf(
                    RecipeIngredient(name = "Large Eggs (separated)", amount = "6", unit = "large", nameEnglish = "Eggs", group = "Chocolate Sponge"),
                    RecipeIngredient(name = "Granulated Sugar", amount = "200", unit = "g", nameEnglish = "Granulated Sugar", group = "Chocolate Sponge"),
                    RecipeIngredient(name = "Dutch-process Cocoa Powder", amount = "50", unit = "g", nameEnglish = "Dark Cocoa Powder", group = "Chocolate Sponge"),
                    RecipeIngredient(name = "Flour & Cornstarch", amount = "150", unit = "g", nameEnglish = "Flour & Cornstarch", group = "Chocolate Sponge"),
                    RecipeIngredient(name = "Sour Cherries in Jar", amount = "1", unit = "jar (700g)", nameEnglish = "Sour Cherries", group = "Cherry Filling"),
                    RecipeIngredient(name = "Cherry Brandy (Kirschwasser)", amount = "80", unit = "ml", nameEnglish = "Cherry Brandy", group = "Cherry Filling & Soak"),
                    RecipeIngredient(name = "Heavy Whipping Cream", amount = "800", unit = "ml", nameEnglish = "Heavy Whipping Cream", group = "Cream Layer"),
                    RecipeIngredient(name = "Dark Chocolate curls & shavings", amount = "100", unit = "g", nameEnglish = "Dark Chocolate Shavings", group = "Garnish")
                ),
                steps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionEnglish = "Whip egg whites with half the sugar until stiff peaks form. Beat yolks with remaining sugar, then fold whites and sifted cocoa flour mixture together.",
                        instructionGerman = "Whip egg whites with half the sugar until stiff peaks form. Beat yolks with remaining sugar, then fold whites and sifted cocoa flour mixture together.",
                        timerMinutes = 10
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionEnglish = "Bake sponge at 175°C (350°F) for 30-35 minutes. Once cooled, slice horizontally into three even cake discs.",
                        instructionGerman = "Bake sponge at 175°C (350°F) for 30-35 minutes. Once cooled, slice horizontally into three even cake discs.",
                        timerMinutes = 32
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionEnglish = "Simmer cherries with starch and 2 tbsp cherry brandy until thickened. Cool completely.",
                        instructionGerman = "Simmer cherries with starch and 2 tbsp cherry brandy until thickened. Cool completely.",
                        timerMinutes = 5
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionEnglish = "Drizzle each cake layer generously with cherry brandy. Layer cake disc, thickened sour cherries, fresh whipped cream, repeat for 2nd layer.",
                        instructionGerman = "Drizzle each cake layer generously with cherry brandy. Layer cake disc, thickened sour cherries, fresh whipped cream, repeat for 2nd layer.",
                        timerMinutes = 15
                    ),
                    RecipeStep(
                        stepNumber = 5,
                        instructionEnglish = "Frost the entire cake with whipped cream, coat sides with chocolate curls, and pipe 12 cream rosettes topped with whole cherries.",
                        instructionGerman = "Frost the entire cake with whipped cream, coat sides with chocolate curls, and pipe 12 cream rosettes topped with whole cherries.",
                        timerMinutes = 10
                    )
                ),
                notes = "Authentic Black Forest cake made with rich chocolate layers, sour cherries, and fragrant cherry brandy.",
                notesGerman = "Authentic Black Forest cake made with rich chocolate layers, sour cherries, and fragrant cherry brandy.",
                sourceLanguage = "en",
                coverTheme = "GOLDEN_PARCHMENT",
                isFavorite = true,
                rating = 5,
                timesCooked = 7,
                originStory = "Celebration heirloom cake from vintage cookbook collection.",
                createdAt = now - 500000
            ),
            RecipeEntity(
                id = 6,
                title = "Easy Indian Mint Sauce (Restaurant Style Pudina Chutney)",
                titleGerman = "Easy Indian Mint Sauce (Restaurant Style Pudina Chutney)",
                titleEnglish = "Easy Indian Mint Sauce (Restaurant Style Pudina Chutney)",
                category = "Family Classics",
                servings = "6-8 servings (1 jar)",
                prepTimeMinutes = 10,
                cookTimeMinutes = 0,
                difficulty = "Easy",
                ingredients = listOf(
                    RecipeIngredient(name = "Fresh Mint Leaves (Pudina)", amount = "1", unit = "cup (tightly packed)", nameEnglish = "Fresh Mint Leaves (Pudina)", group = "Herbs"),
                    RecipeIngredient(name = "Fresh Coriander / Cilantro Leaves", amount = "1", unit = "cup (tightly packed)", nameEnglish = "Fresh Coriander / Cilantro Leaves", group = "Herbs"),
                    RecipeIngredient(name = "Green Chillies", amount = "1-2", unit = "whole", nameEnglish = "Green Chillies", group = "Aromatics"),
                    RecipeIngredient(name = "Garlic Cloves", amount = "2", unit = "cloves", nameEnglish = "Garlic Cloves", group = "Aromatics"),
                    RecipeIngredient(name = "Fresh Ginger", amount = "1/2", unit = "inch piece", nameEnglish = "Fresh Ginger", group = "Aromatics"),
                    RecipeIngredient(name = "Plain Greek Yogurt or Dahi", amount = "4-5", unit = "tbsp", nameEnglish = "Plain Greek Yogurt or Dahi", group = "Base"),
                    RecipeIngredient(name = "Roasted Cumin Powder (Bhuna Jeera)", amount = "1/2", unit = "tsp", nameEnglish = "Roasted Cumin Powder", group = "Spices"),
                    RecipeIngredient(name = "Chaat Masala", amount = "1/2", unit = "tsp", nameEnglish = "Chaat Masala", group = "Spices"),
                    RecipeIngredient(name = "Fresh Lemon Juice", amount = "1", unit = "tbsp", nameEnglish = "Fresh Lemon Juice", group = "Finishing"),
                    RecipeIngredient(name = "Black Salt (Kala Namak) or Sea Salt", amount = "1/2", unit = "tsp", nameEnglish = "Black Salt or Sea Salt", group = "Finishing"),
                    RecipeIngredient(name = "Granulated Sugar", amount = "1/2", unit = "tsp", nameEnglish = "Granulated Sugar", group = "Finishing")
                ),
                steps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionEnglish = "Thoroughly wash the fresh mint and coriander leaves in cold water, discarding any tough stems. Drain well.",
                        instructionGerman = "Thoroughly wash the fresh mint and coriander leaves in cold water, discarding any tough stems. Drain well.",
                        timerMinutes = 3,
                        tip = "Discard thick mint stems as they can introduce bitterness to the chutney."
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionEnglish = "In a blender jar, add the washed mint, coriander, green chillies, garlic, ginger, roasted cumin powder, chaat masala, salt, sugar, and fresh lemon juice.",
                        instructionGerman = "In a blender jar, add the washed mint, coriander, green chillies, garlic, ginger, roasted cumin powder, chaat masala, salt, sugar, and fresh lemon juice.",
                        timerMinutes = 2,
                        tip = "Lemon juice prevents the mint and coriander from oxidizing, keeping the sauce vibrant green."
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionEnglish = "Add 1 to 2 tablespoons of water or yogurt. Blend everything together into a smooth, bright green herb paste.",
                        instructionGerman = "Add 1 to 2 tablespoons of water or yogurt. Blend everything together into a smooth, bright green herb paste.",
                        timerMinutes = 1
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionEnglish = "Transfer the herb paste into a bowl. Gently whisk in the remaining chilled yogurt until velvety and smooth. Chill in refrigerator for 20 minutes before serving with samosas, pakoras, poppadoms, or tandoori grills.",
                        instructionGerman = "Transfer the herb paste into a bowl. Gently whisk in the remaining chilled yogurt until velvety and smooth. Chill in refrigerator for 20 minutes before serving.",
                        timerMinutes = 20,
                        tip = "Whisking yogurt by hand rather than over-blending prevents it from turning watery."
                    )
                ),
                notes = "Authentic British-Indian restaurant style mint chutney. For a dairy-free / vegan version, replace the yogurt with 2 tablespoons of cold water and an extra splash of lemon juice.",
                notesGerman = "Authentic British-Indian restaurant style mint chutney. For a dairy-free / vegan version, replace the yogurt with 2 tablespoons of cold water and an extra splash of lemon juice.",
                sourceLanguage = "en",
                coverTheme = "FOREST_LINEN",
                isFavorite = true,
                rating = 5,
                timesCooked = 3,
                originStory = "Authentic restaurant & family staple recipe.",
                createdAt = now - 50000
            )
        )
    }
}
