import SwiftUI
import SwiftData

public struct BookletJournalPageView: View {
    @Bindable var recipe: Recipe
    @Binding var journalNotes: String
    let onBackToCover: () -> Void

    @Environment(\.modelContext) private var modelContext

    public init(
        recipe: Recipe,
        journalNotes: Binding<String>,
        onBackToCover: @escaping () -> Void
    ) {
        self.recipe = recipe
        self._journalNotes = journalNotes
        self.onBackToCover = onBackToCover
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("COOK'S JOURNAL & MEMORIES")
                .font(.system(size: 11, weight: .black, design: .serif))
                .tracking(1.5)
                .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

            // Times Cooked Counter & Button
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Times Cooked: \(recipe.timesCooked)")
                        .font(.system(size: 16, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                    Text("Track every time you prepare this meal")
                        .font(.system(size: 11, design: .serif))
                        .foregroundStyle(.secondary)
                }

                Spacer()

                Button {
                    recipe.timesCooked += 1
                    try? modelContext.save()
                    AudioEffectManager.shared.playToggle(enabled: true)
                } label: {
                    HStack(spacing: 4) {
                        Text("🔥")
                        Text("+1 Cooked Today")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), in: Capsule())
                }
            }
            .padding(14)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.18), lineWidth: 1))

            // Rating Stars Editor
            VStack(alignment: .leading, spacing: 8) {
                Text("RECIPE RATING")
                    .font(.system(size: 10, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                HStack(spacing: 8) {
                    ForEach(1...5, id: \.self) { star in
                        Button {
                            recipe.rating = star
                            try? modelContext.save()
                        } label: {
                            Image(systemName: star <= recipe.rating ? "star.fill" : "star")
                                .font(.system(size: 24))
                                .foregroundStyle(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0))
                        }
                    }
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.18), lineWidth: 1))

            // Notes Editor
            VStack(alignment: .leading, spacing: 8) {
                Text("PERSONAL CHEF NOTES & MODIFICATIONS")
                    .font(.system(size: 10, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                TextEditor(text: $journalNotes)
                    .frame(minHeight: 120)
                    .font(.system(size: 14, design: .serif))
                    .padding(8)
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                    .onChange(of: journalNotes) { _, newNotes in
                        recipe.notes = newNotes
                        try? modelContext.save()
                    }
            }

            // Return to Cover Button
            Button(action: onBackToCover) {
                HStack {
                    Image(systemName: "arrow.uturn.backward")
                    Text("Back to Cover Page")
                }
                .font(.system(size: 14, weight: .bold, design: .serif))
                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.25), lineWidth: 1))
            }
            .padding(.top, 8)
        }
    }
}
