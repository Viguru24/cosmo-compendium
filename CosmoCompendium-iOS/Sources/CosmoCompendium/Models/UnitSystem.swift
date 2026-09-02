import Foundation

public enum UnitSystem: String, CaseIterable, Identifiable, Codable {
    case cupsUS = "CUPS_US"
    case metricGrams = "METRIC_GRAMS"
    case ukImperial = "UK_IMPERIAL"
    case bakersPrecision = "BAKERS_PRECISION"

    public var id: String { rawValue }

    public var label: String {
        switch self {
        case .cupsUS: return "US Cups & Spoons"
        case .metricGrams: return "Metric Weights & Volume"
        case .ukImperial: return "UK Kitchen Standard"
        case .bakersPrecision: return "Baker's Precision Grams"
        }
    }

    public var shortLabel: String {
        switch self {
        case .cupsUS: return "Cups / Spoons"
        case .metricGrams: return "Metric (g, ml)"
        case .ukImperial: return "UK (g, ml, Spoons)"
        case .bakersPrecision: return "Baker's Grams"
        }
    }

    public var icon: String {
        switch self {
        case .cupsUS: return "🥣"
        case .metricGrams: return "⚖️"
        case .ukImperial: return "🇬🇧"
        case .bakersPrecision: return "🧑‍🍳"
        }
    }

    public var descriptionText: String {
        switch self {
        case .cupsUS: return "Cups, tablespoons (tbsp), teaspoons (tsp), oz, lbs, °F"
        case .metricGrams: return "Grams (g), kilograms (kg), milliliters (ml), liters (l), °C"
        case .ukImperial: return "Grams (g), millilitres (ml), UK spoons (tsp/tbsp), °C & Gas Mark"
        case .bakersPrecision: return "Exact decimal grams (e.g. 250.0g) for precision weighing"
        }
    }
}

public enum LanguageMode: String, CaseIterable, Identifiable, Codable {
    case english = "en"
    case german = "de"
    case both = "both"

    public var id: String { rawValue }

    public var label: String {
        switch self {
        case .english: return "English"
        case .german: return "Deutsch"
        case .both: return "Bilingual (DE / EN)"
        }
    }
}
