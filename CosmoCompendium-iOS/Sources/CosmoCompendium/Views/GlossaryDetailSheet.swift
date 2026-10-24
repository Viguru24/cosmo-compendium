import SwiftUI

// Subsheet for German culinary glossary substitution
public struct GlossaryDetailSheet: View {
    public let item: GlossaryItem
    @Environment(\.dismiss) private var dismiss

    public init(item: GlossaryItem) {
        self.item = item
    }

    public var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text(item.germanName)
                    .font(.system(size: 22, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                Text(item.englishName)
                    .font(.system(size: 15, design: .serif))
                    .italic()
                    .foregroundStyle(.secondary)

                Text(item.descriptionText)
                    .font(.system(size: 14, design: .serif))

                VStack(alignment: .leading, spacing: 8) {
                    Text("RECOMMENDED SUBSTITUTES")
                        .font(.system(size: 11, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

                    ForEach(item.substitutes, id: \.self) { sub in
                        Label(sub, systemImage: "arrow.triangle.swap")
                            .font(.system(size: 13, design: .serif))
                    }
                }
                .padding(12)
                .background(Color(red: 0xFF / 255.0, green: 0xFA / 255.0, blue: 0xED / 255.0), in: RoundedRectangle(cornerRadius: 8))

                Spacer()
            }
            .padding(20)
            .navigationTitle("Glossary & Substitutes")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
