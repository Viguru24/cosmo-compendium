package com.example.ui.components.scan

data class ScanSamplePreset(
    val title: String,
    val text: String
)

val samplePresetRecipes = listOf(
    ScanSamplePreset(
        "🇩🇪 Vintage Handwritten German Apple Strudel (Auto-Translates to English)",
        """
            --- Page 1: Oma's Apfelstrudel Zutaten & Teig ---
            Zutaten für den Strudelteig:
            - 250 g Weizenmehl
            - 1 Prise Salz
            - 1 Ei
            - 100 ml Lauwarmes Wasser
            - 2 EL Pflanzenöl
            - 1/4 / 1/2 spoon sugar (Zucker für den Teig)
            
            --- Page 2: Füllung & Zubereitung (Schritte 1 - 4) ---
            Zutaten für die Füllung:
            - 1 kg Säuerliche Äpfel (Boskoop)
            - 100 g Semmelbrösel in 50 g Butter geröstet
            - 80 g Rosinen (in 2 EL Rum eingeweicht)
            - 1 TL Zimt
            - 1 Päckchen Vanillezucker
            
            Schritte:
            1. Mehl auf Arbeitsfläche sieben, mit Ei, Öl, Wasser und Salz zu geschmeidigem Teig kneten.
            2. Teig auf bemehltem Strudeltuch hauchdünn ausziehen, bis man die Zeitung durchlesen kann.
            3. Äpfel schälen und blättrig schneiden. Mit gerösteten Bröseln, Zimt, Zucker und Rosinen vermengen.
            4. Füllung auf dem Teig verteilen, mit Hilfe des Tuchs einrollen und bei 190 Grad Ober-/Unterhitze 45 Min goldgelb backen.
        """.trimIndent()
    ),
    ScanSamplePreset(
        "🇵🇱 Babcia's Polish Pierogi Ruskie (From Vintage Scrapbook Page)",
        """
            Dough:
            - 3 cups all-purpose flour
            - 1 cup warm water
            - 1 large egg
            - 1/2 tsp salt
            - 2 tbsp melted butter
            
            Filling:
            - 1 lb russet potatoes, peeled, boiled, and mashed
            - 1 cup farmer's cheese (twaróg)
            - 1 large yellow onion, finely diced and caramelized in butter
            - Salt and freshly cracked black pepper to taste
            
            Instructions:
            1. Mix flour, egg, warm water, and melted butter into a soft elastic dough. Rest covered for 30 minutes.
            2. Roll dough thin (1/8 inch) on floured board. Cut rounds using a drinking glass.
            3. Mash warm potatoes with twaróg cheese and cooled caramelized onions until smooth.
            4. Place 1 tablespoon filling in center of each round, fold over into half-moon and crimp edges tightly with fork.
            5. Boil in salted water in batches for 3-4 minutes until they float. Serve with sour cream and fried onions.
        """.trimIndent()
    ),
    ScanSamplePreset(
        "🇺🇸 Handwritten Chocolate Chip Cookies (Front & Back Index Card)",
        """
            Card Front - Ingredients:
            - 2 1/4 cups all-purpose flour
            - 1 tsp baking soda
            - 1 tsp salt
            - 1 cup butter, softened
            - 3/4 cup granulated sugar
            - 3/4 cup packed brown sugar
            - 1 tsp vanilla extract
            - 2 large eggs
            - 2 cups Nestlé Toll House semi-sweet chocolate morsels
            - 1 cup chopped walnuts
            
            Card Back - Directions:
            1. Preheat oven to 375°F.
            2. Combine flour, baking soda and salt in small bowl. Beat butter, granulated sugar, brown sugar and vanilla extract in large mixer bowl until creamy.
            3. Add eggs, one at a time, beating well after each addition. Gradually beat in flour mixture. Stir in morsels and nuts.
            4. Drop by rounded tablespoon onto ungreased baking sheets.
            5. Bake for 9 to 11 minutes or until golden brown. Cool on baking sheets for 2 minutes; remove to wire racks to cool completely.
        """.trimIndent()
    )
)
