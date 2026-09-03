import SwiftUI

public struct CookbookGuideSheet: View {
    @Environment(\.dismiss) private var dismiss

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    guideSection(
                        icon: "doc.viewfinder.fill",
                        color: Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0),
                        title: "Continuous Multi-Page Scanning",
                        bodyText: "Snap Page 1, Page 2, Page 3, and more in rapid succession. Cosmo Compendium's Gemini Vision engine automatically detects front cards, back directions, and notes, synthesizing them all into a unified recipe book."
                    )

                    guideSection(
                        icon: "person.2.fill",
                        color: Color(red: 0x7E / 255.0, green: 0x22 / 255.0, blue: 0xCE / 255.0),
                        title: "Family Member Cookbooks",
                        bodyText: "Switch between family member profiles with one tap. Give everyone their own curated cookbook while retaining access to the combined 'All Family' view."
                    )

                    guideSection(
                        icon: "bubble.left.and.text.bubble.right.fill",
                        color: Color(red: 0x25 / 255.0, green: 0x63 / 255.0, blue: 0xEB / 255.0),
                        title: "AI Sous Chef Assistant",
                        bodyText: "Tap the Sous Chef icon on the bookshelf to ask questions while cooking, get substitutions for missing ingredients, and scale recipes seamlessly."
                    )

                    guideSection(
                        icon: "link.badge.plus",
                        color: Color(red: 0x05 / 255.0, green: 0x96 / 255.0, blue: 0x69 / 255.0),
                        title: "Web Recipe Import",
                        bodyText: "Paste any URL from food blogs or cooking websites. The AI reads the page and saves clean ingredients and steps straight to your cookbook."
                    )

                    guideSection(
                        icon: "flame.fill",
                        color: Color(red: 0xDC / 255.0, green: 0x26 / 255.0, blue: 0x26 / 255.0),
                        title: "Kitchen Cook Mode",
                        bodyText: "Hands-free cooking view with large typography, step-by-step checkboxes, integrated multi-step timers, and unit conversions."
                    )
                }
                .padding(20)
            }
            .background(Color(red: 0xFF / 255.0, green: 0xFD / 255.0, blue: 0xF9 / 255.0))
            .navigationTitle("Cookbook Guide")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }
            }
        }
    }

    private func guideSection(icon: String, color: Color, title: String, bodyText: String) -> some View {
        HStack(alignment: .top, spacing: 16) {
            ZStack {
                Circle()
                    .fill(color.opacity(0.15))
                    .frame(width: 48, height: 48)
                Image(systemName: icon)
                    .font(.system(size: 22))
                    .foregroundStyle(color)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 16, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x24 / 255.0, green: 0x14 / 255.0, blue: 0x0C / 255.0))
                Text(bodyText)
                    .font(.system(size: 13.5))
                    .foregroundStyle(Color.secondary)
                    .lineSpacing(3)
            }
        }
        .padding(14)
        .background(Color.white, in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(red: 0xE8 / 255.0, green: 0xDF / 255.0, blue: 0xD5 / 255.0), lineWidth: 1)
        )
    }
}
