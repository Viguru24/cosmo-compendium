import SwiftUI

// MARK: - Color Hex Helper
extension Color {
    public init(hex: UInt, opacity: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: opacity
        )
    }
}

// MARK: - 1:1 Android Culinary Heirloom Theme Tokens
public enum CookbookTheme {
    public static let terracottaPrimary = Color(hex: 0x9A3412)
    public static let terracottaPrimaryDark = Color(hex: 0xEA580C)
    public static let terracottaAccent = Color(hex: 0x7C2D12)
    public static let amberSecondary = Color(hex: 0x92400E)
    public static let amberSecondaryDark = Color(hex: 0xF59E0B)
    public static let honeyTertiary = Color(hex: 0xB45309)
    public static let sageGreen = Color(hex: 0x3F6212)

    public static let creamBackgroundLight = Color(hex: 0xFCF9F2)
    public static let creamSurfaceLight = Color(hex: 0xFFFFFF)
    public static let warmBorderLight = Color(hex: 0x8C7B6B)
    public static let warmBorderSubtle = Color(hex: 0xE8DFD5)
    public static let textPrimaryLight = Color(hex: 0x18120C)
    public static let textSecondaryLight = Color(hex: 0x3B2E25)
    public static let textMutedLight = Color(hex: 0x5A4D41)

    public static let parchmentPage = Color(hex: 0xFAF6EE)
    public static let parchmentPageEdge = Color(hex: 0x8C7B6B)
    public static let ribbonRed = Color(hex: 0xBE123C)

    public static var bookshelfBackgroundGradient: LinearGradient {
        LinearGradient(
            colors: [
                Color(hex: 0xF6F0E8),
                Color(hex: 0xECE4D8),
                Color(hex: 0xE7DDD0)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}

// MARK: - Category Badge Theming (Matching Android BookshelfScreen.kt:1037)
public struct CategoryBadgeStyle {
    public let background: Color
    public let text: Color
    public let border: Color

    public static func style(for category: String) -> CategoryBadgeStyle {
        let cat = category.lowercased()
        if cat.contains("baking") || cat.contains("dessert") {
            return CategoryBadgeStyle(background: Color(hex: 0xFFF1E6), text: Color(hex: 0xC2410C), border: Color(hex: 0xFFFFD7BE))
        } else if cat.contains("main") {
            return CategoryBadgeStyle(background: Color(hex: 0xF0FDF4), text: Color(hex: 0x15803D), border: Color(hex: 0xBBF7D0))
        } else if cat.contains("soup") || cat.contains("stew") {
            return CategoryBadgeStyle(background: Color(hex: 0xFEF3C7), text: Color(hex: 0xB45309), border: Color(hex: 0xFDE68A))
        } else if cat.contains("salad") || cat.contains("side") {
            return CategoryBadgeStyle(background: Color(hex: 0xECFDF5), text: Color(hex: 0x047857), border: Color(hex: 0xA7F3D0))
        } else if cat.contains("ferment") || cat.contains("preserve") || cat.contains("jam") {
            return CategoryBadgeStyle(background: Color(hex: 0xF5F3FF), text: Color(hex: 0x6D28D9), border: Color(hex: 0xDDD6FE))
        } else {
            return CategoryBadgeStyle(background: Color(hex: 0xF5EFEB), text: Color(hex: 0x78350F), border: Color(hex: 0xE2D6C7))
        }
    }
}
