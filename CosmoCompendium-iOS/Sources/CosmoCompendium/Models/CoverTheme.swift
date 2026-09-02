import SwiftUI

public enum CoverTheme: String, CaseIterable, Identifiable, Codable {
    case vintageLeather = "VINTAGE_LEATHER"
    case warmTerracotta = "WARM_TERRACOTTA"
    case forestSage = "FOREST_SAGE"
    case floralLinen = "FLORAL_LINEN"
    case goldenParchment = "GOLDEN_PARCHMENT"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .vintageLeather: return "Vintage Leather"
        case .warmTerracotta: return "Warm Terracotta"
        case .forestSage: return "Bavarian Forest"
        case .floralLinen: return "Antique Linen"
        case .goldenParchment: return "Golden Heritage"
        }
    }

    public var primaryColor: Color {
        switch self {
        case .vintageLeather: return Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0)
        case .warmTerracotta: return Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0)
        case .forestSage: return Color(red: 0x14 / 255.0, green: 0x53 / 255.0, blue: 0x2D / 255.0)
        case .floralLinen: return Color(red: 0xB4 / 255.0, green: 0x53 / 255.0, blue: 0x09 / 255.0)
        case .goldenParchment: return Color(red: 0x85 / 255.0, green: 0x4D / 255.0, blue: 0x0E / 255.0)
        }
    }

    public var secondaryColor: Color {
        switch self {
        case .vintageLeather: return Color(red: 0x45 / 255.0, green: 0x1A / 255.0, blue: 0x03 / 255.0)
        case .warmTerracotta: return Color(red: 0xC2 / 255.0, green: 0x41 / 255.0, blue: 0x0C / 255.0)
        case .forestSage: return Color(red: 0x16 / 255.0, green: 0x65 / 255.0, blue: 0x34 / 255.0)
        case .floralLinen: return Color(red: 0xD9 / 255.0, green: 0x77 / 255.0, blue: 0x06 / 255.0)
        case .goldenParchment: return Color(red: 0xA1 / 255.0, green: 0x62 / 255.0, blue: 0x07 / 255.0)
        }
    }

    public var goldFoilColor: Color {
        Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0)
    }

    public var goldFoilShine: Color {
        Color(red: 0xFF / 255.0, green: 0xDF / 255.0, blue: 0x73 / 255.0)
    }

    public var brassCornerColor: Color {
        Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0)
    }
}
