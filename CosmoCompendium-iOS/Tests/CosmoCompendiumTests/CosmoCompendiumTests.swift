import XCTest
@testable import CosmoCompendium

final class CosmoCompendiumTests: XCTestCase {

    func testUkImperialSpoonsConversion() {
        // Small dry ingredient should convert to UK spoons (e.g. baking powder, salt)
        let salt = RecipeIngredient(name: "Salt", amount: "3", unit: "g", nameEnglish: "Salt")
        let ukSalt = salt.convertedAmount(targetSystem: .ukImperial)
        XCTAssertTrue(ukSalt.contains("tsp") || ukSalt.contains("pinch"), "Expected UK spoon conversion, got: \(ukSalt)")

        // Vanilla extract (liquid small quantity)
        let vanilla = RecipeIngredient(name: "Vanilla Extract", amount: "5", unit: "ml", nameEnglish: "Vanilla Extract")
        let ukVanilla = vanilla.convertedAmount(targetSystem: .ukImperial)
        XCTAssertEqual(ukVanilla, "1 tsp")
    }

    func testDensityAwareFlourConversion() {
        // 1 cup of flour should be ~125g in metric
        let flour = RecipeIngredient(name: "All-Purpose Flour", amount: "1", unit: "cup", nameEnglish: "All-Purpose Flour")
        let metricFlour = flour.convertedAmount(targetSystem: .metricGrams)
        XCTAssertEqual(metricFlour, "125 g")

        // 1 cup of granulated sugar should be ~200g in metric
        let sugar = RecipeIngredient(name: "Granulated Sugar", amount: "1", unit: "cup", nameEnglish: "Granulated Sugar")
        let metricSugar = sugar.convertedAmount(targetSystem: .metricGrams)
        XCTAssertEqual(metricSugar, "200 g")
    }

    func testCulinaryTemperatureConverter() {
        let text = "Bake at 350°F until golden brown."
        let ukFormatted = CulinaryTemperatureConverter.formatTemperatures(text, unitSystem: .ukImperial)
        XCTAssertTrue(ukFormatted.contains("175°C / Gas Mark 4 (350°F)"), "Expected 175°C / Gas Mark 4, got: \(ukFormatted)")
    }

    func testOfflineRecipeParser() {
        let recipeRaw = """
        Omas Apfelkuchen
        Zutaten:
        250 g Mehl
        100 g Zucker
        1 Prise Salz
        2 Eier

        Zubereitung:
        1. Den Teig 30 Minuten ruhen lassen.
        2. Bei 180 Grad backen.
        """

        let result = OfflineRecipeParser.parse(text: recipeRaw)
        XCTAssertEqual(result.title, "Omas Apfelkuchen")
        XCTAssertEqual(result.ingredients.count, 4)
        XCTAssertEqual(result.steps.count, 2)
        XCTAssertEqual(result.detectedSourceLanguage, "de")
    }

    func testGermanCulinaryGlossary() {
        let quark = GermanCulinaryGlossary.findSubstitute(query: "Quark")
        XCTAssertNotNil(quark)
        XCTAssertTrue(quark?.substitutes.contains(where: { $0.contains("Greek Yogurt") }) ?? false)

        let starch = GermanCulinaryGlossary.findSubstitute(query: "Speisestärke")
        XCTAssertNotNil(starch)
        XCTAssertEqual(starch?.englishName, "Cornstarch / Potato starch")
    }

    func testShoppingCategorizer() {
        XCTAssertEqual(ShoppingCategorizer.categorize(name: "Apples"), "Produce")
        XCTAssertEqual(ShoppingCategorizer.categorize(name: "Heavy Cream"), "Dairy & Refrigerated")
        XCTAssertEqual(ShoppingCategorizer.categorize(name: "Beef Topside"), "Meat & Seafood")
        XCTAssertEqual(ShoppingCategorizer.categorize(name: "Cinnamon"), "Spices & Baking")
        XCTAssertEqual(ShoppingCategorizer.categorize(name: "Olive Oil"), "Pantry")
    }
}
