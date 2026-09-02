import Foundation

public struct ConversionResult: Identifiable {
    public var id: String { system.rawValue }
    public let system: UnitSystem
    public let formattedValue: String
    public let note: String
}

public enum UnitConverterService {

    public static func convert(
        amount: Double,
        fromUnit: String,
        ingredientName: String = "flour"
    ) -> [ConversionResult] {
        let dummy = RecipeIngredient(
            name: ingredientName,
            amount: "\(amount)",
            unit: fromUnit,
            nameEnglish: ingredientName
        )

        return UnitSystem.allCases.map { system in
            let converted = dummy.convertedAmount(targetSystem: system)
            return ConversionResult(
                system: system,
                formattedValue: converted.isEmpty ? "\(amount) \(fromUnit)" : converted,
                note: system.descriptionText
            )
        }
    }
}
