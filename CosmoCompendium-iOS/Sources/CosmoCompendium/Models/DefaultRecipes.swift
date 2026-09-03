import Foundation

public enum DefaultRecipes {
    public static func initialRecipes() -> [Recipe] {
        return [
            Recipe(
                id: "1",
                title: "Grandma's Traditional Apple Strudel",
                titleGerman: "Omas Traditioneller Apfelstrudel",
                titleEnglish: "Grandma's Traditional Apple Strudel",
                category: "Baking & Desserts",
                servings: "8 slices",
                prepTimeMinutes: 45,
                cookTimeMinutes: 40,
                difficulty: "Medium",
                ingredients: [
                    RecipeIngredient(name: "All-Purpose Flour", amount: "250", unit: "g", nameEnglish: "All-Purpose Flour", group: "Dough"),
                    RecipeIngredient(name: "Warm Water", amount: "100", unit: "ml", nameEnglish: "Warm Water", group: "Dough"),
                    RecipeIngredient(name: "Vegetable Oil", amount: "2", unit: "tbsp", nameEnglish: "Vegetable Oil", group: "Dough"),
                    RecipeIngredient(name: "Pinch of Salt", amount: "1", unit: "pinch", nameEnglish: "Pinch of Salt", group: "Dough"),
                    RecipeIngredient(name: "Tart Baking Apples (Granny Smith or Bramley)", amount: "1", unit: "kg", nameEnglish: "Tart Baking Apples", group: "Filling"),
                    RecipeIngredient(name: "Granulated Sugar", amount: "80", unit: "g", nameEnglish: "Granulated Sugar", group: "Filling"),
                    RecipeIngredient(name: "Ground Cinnamon", amount: "1.5", unit: "tsp", nameEnglish: "Ground Cinnamon", group: "Filling"),
                    RecipeIngredient(name: "Rum-soaked Raisins", amount: "60", unit: "g", nameEnglish: "Rum-soaked Raisins", isOptional: true, group: "Filling"),
                    RecipeIngredient(name: "Butter-toasted Breadcrumbs", amount: "60", unit: "g", nameEnglish: "Butter-toasted Breadcrumbs", group: "Filling"),
                    RecipeIngredient(name: "Melted Butter (for brushing)", amount: "75", unit: "g", nameEnglish: "Melted Butter", group: "Finishing"),
                    RecipeIngredient(name: "Powdered Sugar for dusting", amount: "2", unit: "tbsp", nameEnglish: "Powdered Sugar", group: "Finishing")
                ],
                steps: [
                    RecipeStep(stepNumber: 1, instructionEnglish: "Knead flour, warm water, oil, and salt into a silky, smooth dough. Form a ball, brush with oil, cover with a warm bowl, and let rest for 30 minutes.", timerMinutes: 30, tip: "The dough must rest under a warm bowl to stretch paper-thin without tearing."),
                    RecipeStep(stepNumber: 2, instructionEnglish: "Peel, core, and thinly slice the apples. Toss with sugar, ground cinnamon, rum raisins, and lemon juice.", timerMinutes: 0, tip: "Use tart, firm baking apples for the best balance of sweetness and texture."),
                    RecipeStep(stepNumber: 3, instructionEnglish: "Melt 30g butter in a skillet and gently toast the breadcrumbs until golden brown and fragrant.", timerMinutes: 5, tip: "The breadcrumbs soak up apple juices to keep the bottom pastry crisp."),
                    RecipeStep(stepNumber: 4, instructionEnglish: "Lay a clean cotton kitchen towel on the table and dust with flour. Roll the dough thinly, then gently stretch using the backs of your hands until translucent.", timerMinutes: 10, tip: "Stretch slowly and evenly from the center outwards."),
                    RecipeStep(stepNumber: 5, instructionEnglish: "Brush the stretched dough with melted butter. Scatter toasted breadcrumbs over two-thirds of the area, then spread the spiced apple mixture on top.", timerMinutes: 0),
                    RecipeStep(stepNumber: 6, instructionEnglish: "Roll up the strudel tightly using the kitchen towel. Transfer seam-side down to a lined baking sheet and brush generously with melted butter.", timerMinutes: 0),
                    RecipeStep(stepNumber: 7, instructionEnglish: "Bake at 190°C (375°F) for 35-40 minutes until deeply golden and flaky. Dust with powdered sugar while warm and serve with warm vanilla custard or cream.", timerMinutes: 38, tip: "Brush with more melted butter halfway through baking for extra crispness.")
                ],
                notes: "Grandmother's secret was soaking the raisins in dark rum overnight and rolling the dough on her vintage cotton tablecloth.",
                notesGerman: "Omas Geheimnis war es, die Rosinen über Nacht in dunklem Rum einzulegen und den Teig auf ihrer Vintage-Baumwolltischdecke hauchdünn auszuziehen.",
                sourceLanguage: "both",
                coverTheme: .warmTerracotta,
                isFavorite: true,
                rating: 5,
                timesCooked: 12,
                originStory: "Passed down from grandmother's handwritten recipe collection."
            ),
            Recipe(
                id: "2",
                title: "Traditional Sunday Roast with Yorkshire Puddings",
                titleGerman: "Traditioneller Sonntagsbraten mit Yorkshire Puddings",
                titleEnglish: "Traditional Sunday Roast with Yorkshire Puddings",
                category: "Main Dishes",
                servings: "6 servings",
                prepTimeMinutes: 30,
                cookTimeMinutes: 90,
                difficulty: "Medium",
                ingredients: [
                    RecipeIngredient(name: "Beef Topside Joint", amount: "1.5", unit: "kg", nameEnglish: "Beef Topside Joint", group: "Roast"),
                    RecipeIngredient(name: "English Mustard Powder", amount: "1", unit: "tbsp", nameEnglish: "English Mustard Powder", group: "Roast"),
                    RecipeIngredient(name: "Flour", amount: "1", unit: "tbsp", nameEnglish: "Flour", group: "Roast"),
                    RecipeIngredient(name: "Sea Salt & Freshly Cracked Black Pepper", amount: "1", unit: "tsp", nameEnglish: "Salt & Pepper", group: "Roast"),
                    RecipeIngredient(name: "Plain Flour", amount: "140", unit: "g", nameEnglish: "Plain Flour", group: "Yorkshire Puddings"),
                    RecipeIngredient(name: "Large Eggs", amount: "4", unit: "eggs", nameEnglish: "Large Eggs", group: "Yorkshire Puddings"),
                    RecipeIngredient(name: "Whole Milk", amount: "200", unit: "ml", nameEnglish: "Whole Milk", group: "Yorkshire Puddings"),
                    RecipeIngredient(name: "Beef Dripping or Vegetable Oil", amount: "4", unit: "tbsp", nameEnglish: "Beef Dripping", group: "Yorkshire Puddings")
                ],
                steps: [
                    RecipeStep(stepNumber: 1, instructionEnglish: "Rub beef joint with mustard powder, flour, salt, and black pepper. Sear in a hot pan on all sides.", timerMinutes: 8),
                    RecipeStep(stepNumber: 2, instructionEnglish: "Roast at 200°C (400°F) for 20 minutes, then reduce heat to 160°C (320°F) and cook for 1 hour for medium-rare.", timerMinutes: 80, tip: "Rest beef under foil for at least 20 minutes before carving."),
                    RecipeStep(stepNumber: 3, instructionEnglish: "Whisk flour, eggs, and milk with a pinch of salt until smooth. Rest batter at room temperature for 30 minutes.", timerMinutes: 30),
                    RecipeStep(stepNumber: 4, instructionEnglish: "Divide dripping into a 12-hole muffin tin and heat in 220°C oven until smoking hot. Pour batter in quickly and bake for 22 minutes without opening oven door.", timerMinutes: 22, tip: "Never open the oven during the first 20 minutes!")
                ],
                notes: "A British family Sunday institution. Serve with roasted potatoes, honey carrots, and rich onion gravy.",
                sourceLanguage: "en",
                coverTheme: .vintageLeather,
                isFavorite: true,
                rating: 5,
                timesCooked: 18,
                originStory: "Classic family Sunday dinner ritual."
            ),
            Recipe(
                id: "3",
                title: "Authentic Black Forest Cherry Torte",
                titleGerman: "Schwarzwälder Kirschtorte",
                titleEnglish: "Authentic Black Forest Cherry Torte",
                category: "Baking & Desserts",
                servings: "12 slices",
                prepTimeMinutes: 60,
                cookTimeMinutes: 35,
                difficulty: "Advanced",
                ingredients: [
                    RecipeIngredient(name: "Sour Cherries (Schattenmorellen)", amount: "700", unit: "g", nameEnglish: "Sour Cherries", group: "Cherry Layer"),
                    RecipeIngredient(name: "Kirschwasser (Cherry Schnapps)", amount: "60", unit: "ml", nameEnglish: "Kirschwasser", group: "Cherry Layer"),
                    RecipeIngredient(name: "Cornstarch (Speisestärke)", amount: "35", unit: "g", nameEnglish: "Cornstarch", group: "Cherry Layer"),
                    RecipeIngredient(name: "Sugar", amount: "40", unit: "g", nameEnglish: "Sugar", group: "Cherry Layer"),
                    RecipeIngredient(name: "Heavy Whipping Cream (Schlagsahne)", amount: "800", unit: "ml", nameEnglish: "Heavy Cream", group: "Cream Layer"),
                    RecipeIngredient(name: "Whip It Stabilizer (Sahnesteif)", amount: "3", unit: "packets", nameEnglish: "Cream Stabilizer", group: "Cream Layer"),
                    RecipeIngredient(name: "Vanilla Sugar (Vanillezucker)", amount: "3", unit: "packets", nameEnglish: "Vanilla Sugar", group: "Cream Layer"),
                    RecipeIngredient(name: "Dark Chocolate Curls", amount: "100", unit: "g", nameEnglish: "Dark Chocolate Curls", group: "Decoration")
                ],
                steps: [
                    RecipeStep(stepNumber: 1, instructionEnglish: "Bake a deep chocolate sponge cake and let cool completely. Slice horizontally into 3 even layers.", timerMinutes: 35),
                    RecipeStep(stepNumber: 2, instructionEnglish: "Simmer sour cherries with cherry juice, sugar, and cornstarch until thick and glossy. Stir in half the Kirschwasser and cool.", timerMinutes: 10),
                    RecipeStep(stepNumber: 3, instructionEnglish: "Whip cold heavy cream with vanilla sugar and stabilizer until stiff peaks form. Fold remaining Kirsch into bottom cake layer.", timerMinutes: 8),
                    RecipeStep(stepNumber: 4, instructionEnglish: "Assemble layers: sponge soaked in Kirsch, piped cream rings with cherries nestled between, topped with final sponge. Frost entire cake in cream and garnish generously with dark chocolate curls.", timerMinutes: 20)
                ],
                notes: "Traditional Black Forest recipe requiring authentic double-distilled Kirschwasser and sour cherries.",
                notesGerman: "Echtes Schwarzwälder Originalrezept mit klarem Schwarzwälder Kirschwasser und sonnengereiften Schattenmorellen.",
                sourceLanguage: "both",
                coverTheme: .forestSage,
                isFavorite: true,
                rating: 5,
                timesCooked: 7,
                originStory: "Grandmother's celebratory birthday cake for three generations."
            ),
            Recipe(
                id: "4",
                title: "Handmade Cheese Egg Noodles (Käsespätzle)",
                titleGerman: "Schwäbische Käsespätzle mit Röstzwiebeln",
                titleEnglish: "Handmade Cheese Egg Noodles with Crispy Onions",
                category: "Main Dishes",
                servings: "4 servings",
                prepTimeMinutes: 25,
                cookTimeMinutes: 20,
                difficulty: "Medium",
                ingredients: [
                    RecipeIngredient(name: "Spätzlemehl (or Type 00 / Dunst)", amount: "400", unit: "g", nameEnglish: "Coarse Flour", group: "Spätzle Dough"),
                    RecipeIngredient(name: "Large Fresh Eggs", amount: "4", unit: "eggs", nameEnglish: "Fresh Eggs", group: "Spätzle Dough"),
                    RecipeIngredient(name: "Cold Water", amount: "120", unit: "ml", nameEnglish: "Cold Water", group: "Spätzle Dough"),
                    RecipeIngredient(name: "Salt", amount: "1", unit: "tsp", nameEnglish: "Salt", group: "Spätzle Dough"),
                    RecipeIngredient(name: "Bergkäse (Alpine Mountain Cheese), grated", amount: "200", unit: "g", nameEnglish: "Alpine Cheese", group: "Cheese Layer"),
                    RecipeIngredient(name: "Emmental Cheese, grated", amount: "150", unit: "g", nameEnglish: "Emmental Cheese", group: "Cheese Layer"),
                    RecipeIngredient(name: "Yellow Onions, thinly sliced", amount: "3", unit: "onions", nameEnglish: "Onions", group: "Crispy Onions"),
                    RecipeIngredient(name: "Butter", amount: "40", unit: "g", nameEnglish: "Butter", group: "Crispy Onions")
                ],
                steps: [
                    RecipeStep(stepNumber: 1, instructionEnglish: "Beat flour, eggs, water, and salt with a wooden spoon until dough bubbles and pulls cleanly off spoon. Rest 15 mins.", timerMinutes: 15),
                    RecipeStep(stepNumber: 2, instructionEnglish: "Drop dough through a Spätzle press or scrape off board into boiling salted water. Remove with slotted spoon as soon as they float.", timerMinutes: 10),
                    RecipeStep(stepNumber: 3, instructionEnglish: "Layer piping hot spätzle in a warm dish with grated Bergkäse and Emmental. Toss gently until strings of melted cheese form.", timerMinutes: 5),
                    RecipeStep(stepNumber: 4, instructionEnglish: "Fry onions in foaming butter over medium-low heat until mahogany brown and crispy. Heap over hot noodles.", timerMinutes: 15)
                ],
                notes: "The blend of sharp aged Alpine Bergkäse with melting Emmental gives the iconic gooey stretch.",
                sourceLanguage: "both",
                coverTheme: .goldenParchment,
                isFavorite: false,
                rating: 5,
                timesCooked: 9,
                originStory: "Swabian culinary staple."
            ),
            Recipe(
                id: "5",
                title: "Artisan Lavender & Goat's Milk Soap Bar",
                titleGerman: "Handgemachte Lavendel & Ziegenmilch Seife",
                titleEnglish: "Artisan Lavender & Goat's Milk Soap Bar",
                category: "Artisan Crafts",
                servings: "8 bars (100g each)",
                prepTimeMinutes: 40,
                cookTimeMinutes: 20,
                difficulty: "Medium",
                ingredients: [
                    RecipeIngredient(name: "Olive Oil (Pomace)", amount: "350", unit: "g", nameEnglish: "Olive Oil", group: "Oils Base"),
                    RecipeIngredient(name: "Coconut Oil (76 deg)", amount: "200", unit: "g", nameEnglish: "Coconut Oil", group: "Oils Base"),
                    RecipeIngredient(name: "Shea Butter (Unrefined)", amount: "150", unit: "g", nameEnglish: "Shea Butter", group: "Oils Base"),
                    RecipeIngredient(name: "Sodium Hydroxide (Lye / NaOH)", amount: "98", unit: "g", nameEnglish: "Sodium Hydroxide", group: "Lye Solution"),
                    RecipeIngredient(name: "Frozen Goat's Milk", amount: "210", unit: "g", nameEnglish: "Goat's Milk", group: "Lye Solution"),
                    RecipeIngredient(name: "Lavender Essential Oil (Lavandula Angustifolia)", amount: "25", unit: "g", nameEnglish: "Lavender Essential Oil", group: "Additives"),
                    RecipeIngredient(name: "Colloidal Oatmeal", amount: "1", unit: "tbsp", nameEnglish: "Colloidal Oatmeal", group: "Additives"),
                    RecipeIngredient(name: "Dried Lavender Buds", amount: "1", unit: "tbsp", nameEnglish: "Dried Lavender", isOptional: true, group: "Topping")
                ],
                steps: [
                    RecipeStep(stepNumber: 1, instructionEnglish: "Weigh frozen goat milk cubes into heat-resistant pitcher. Slowly sprinkle lye over milk, stirring constantly in an ice bath to prevent scorching.", timerMinutes: 15, tip: "Keep lye-milk solution under 35°C (95°F) to preserve milk sugars."),
                    RecipeStep(stepNumber: 2, instructionEnglish: "Melt shea butter and coconut oil, then blend with liquid olive oil. Bring oils to 38°C (100°F).", timerMinutes: 10),
                    RecipeStep(stepNumber: 3, instructionEnglish: "Pour lye-milk solution through fine strainer into oils. Stick blend in short bursts until reaching a light trace.", timerMinutes: 5),
                    RecipeStep(stepNumber: 4, instructionEnglish: "Hand whisk in lavender essential oil and colloidal oatmeal until evenly distributed.", timerMinutes: 3),
                    RecipeStep(stepNumber: 5, instructionEnglish: "Pour into silicone loaf mold, sprinkle dried lavender buds along top ridge, and insulate gently for 24 hours.", timerMinutes: 1440),
                    RecipeStep(stepNumber: 6, instructionEnglish: "Unmold and slice into 8 bars. Place on drying rack in a well-ventilated room to cure for 4-6 weeks.", timerMinutes: 0, tip: "Curing hardens the bar and makes the lather gentler and richer.")
                ],
                notes: "Superfatting at 6% leaves skin silky and hydrated. Uses cold process formulation.",
                sourceLanguage: "en",
                coverTheme: .floralLinen,
                isFavorite: true,
                rating: 5,
                timesCooked: 4,
                originStory: "Handcrafted maker recipe for apothecary herbal soap.",
                craftType: "Cold Process Soap",
                lyeRatio: "2.14:1",
                waterDiscount: "30%",
                fragranceLoad: "3.5%",
                cureTimeWeeks: 6,
                batchSizeGrams: 800.0
            )
        ]
    }
}
