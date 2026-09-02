import Foundation

public struct GlossaryItem: Identifiable, Codable, Hashable {
    public var id: String { germanName }
    public let germanName: String
    public let englishName: String
    public let substitutes: [String]
    public let descriptionText: String
    public let culinaryTip: String

    public init(
        germanName: String,
        englishName: String,
        substitutes: [String],
        descriptionText: String,
        culinaryTip: String
    ) {
        self.germanName = germanName
        self.englishName = englishName
        self.substitutes = substitutes
        self.descriptionText = descriptionText
        self.culinaryTip = culinaryTip
    }
}

public enum GermanCulinaryGlossary {
    public static let items: [GlossaryItem] = [
        GlossaryItem(
            germanName: "Quark",
            englishName: "Quark (German curd cheese)",
            substitutes: ["Greek Yogurt + Ricotta (1:1 ratio)", "Sour cream + Cream cheese (whipped)", "Fromage Blanc"],
            descriptionText: "A smooth, mildly tart fresh dairy cheese staple in German baking and cheesecakes (Käsekuchen).",
            culinaryTip: "Strain Greek yogurt in a cheesecloth for 2 hours to achieve authentic German Quark density."
        ),
        GlossaryItem(
            germanName: "Speisestärke",
            englishName: "Cornstarch / Potato starch",
            substitutes: ["Cornstarch (1:1)", "Potato starch", "Arrowroot powder", "Tapioca starch"],
            descriptionText: "Fine pure starch used in German baking (Mondamin) to make cakes velvety and sauces glossy.",
            culinaryTip: "Always dissolve in cold liquid before stirring into hot soups or fruit compotes (Rote Grütze)."
        ),
        GlossaryItem(
            germanName: "Vanillezucker",
            englishName: "Vanilla Sugar",
            substitutes: ["1 tsp pure vanilla extract + 1 tbsp sugar", "Vanilla bean paste", "Home-infused vanilla bean sugar"],
            descriptionText: "Pre-packaged vanilla scented sugar packets (usually 8g) standard in every German baked good.",
            culinaryTip: "Make your own by burying spent dried vanilla bean pods in a jar of granulated sugar for 2 weeks."
        ),
        GlossaryItem(
            germanName: "Kirschwasser",
            englishName: "Kirsch / Clear Cherry Schnapps",
            substitutes: ["Cherry juice + 1/2 tsp almond extract", "Maraschino liqueur", "Brandy or Rum"],
            descriptionText: "Double-distilled, clear tart cherry spirit from the Black Forest. Essential for authentic Schwarzwälder Kirschtorte.",
            culinaryTip: "Use pure tart cherry juice reduced with a touch of sugar for a completely alcohol-free version."
        ),
        GlossaryItem(
            germanName: "Hirschhornsalz",
            englishName: "Baker's Ammonia (Ammonium Carbonate)",
            substitutes: ["3/4 tsp Baking powder + 1/4 tsp Baking soda", "Double-acting baking powder"],
            descriptionText: "Traditional leavening salt used in crisp German holiday cookies like Lebkuchen and Springerle.",
            culinaryTip: "Only use for flat, dry cookies so the strong aroma completely evaporates during baking."
        ),
        GlossaryItem(
            germanName: "Backpulver",
            englishName: "German Single-Acting Baking Powder (Backin)",
            substitutes: ["Standard Double-acting Baking powder (1:1)", "1/4 tsp baking soda + 1/2 tsp cream of tartar"],
            descriptionText: "Sold in small single-bake 16g sachets (Dr. Oetker style), formulated for 500g of flour.",
            culinaryTip: "One German packet (16g) equals roughly 3 to 4 teaspoons of US baking powder."
        ),
        GlossaryItem(
            germanName: "Semmelbrösel / Paniermehl",
            englishName: "Fine Breadcrumbs",
            substitutes: ["Panko breadcrumbs (crushed finely)", "Dry toasted baguette crumbs", "Matzo meal"],
            descriptionText: "Finely ground stale bread rolls used for crispy Schnitzel coatings and dumpling binders.",
            culinaryTip: "Do not press breadcrumbs firmly onto Schnitzel; gentle airy coating creates signature soufflé bubbling."
        ),
        GlossaryItem(
            germanName: "Preiselbeeren",
            englishName: "Lingonberry Compote / Wild Cranberries",
            substitutes: ["Whole cranberry sauce + hint of lemon", "Redcurrant jelly", "Pomegranate molasses"],
            descriptionText: "Tart wild berries traditionally served alongside Viennese Schnitzel, venison, and Camembert.",
            culinaryTip: "Warm gently with a squeeze of fresh orange juice to drizzle over savory roasted meats."
        )
    ]

    public static func findSubstitute(query: String) -> GlossaryItem? {
        let q = query.lowercased().trimmingCharacters(in: .whitespaces)
        return items.first {
            $0.germanName.lowercased().contains(q) ||
            q.contains($0.germanName.lowercased()) ||
            $0.englishName.lowercased().contains(q)
        }
    }
}
