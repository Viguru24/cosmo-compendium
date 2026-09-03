import SwiftUI

public struct CategoryManagerSheet: View {
    @Environment(\.dismiss) private var dismiss

    @State private var categories: [String] = [
        "Baking & Desserts", "Main Dishes", "Soups & Stews", "Family Classics", "Artisan Crafts"
    ]
    @State private var newCategoryName = ""

    public init() {}

    public var body: some View {
        NavigationStack {
            List {
                Section {
                    HStack {
                        TextField("New Category Name...", text: $newCategoryName)
                        Button("Add") {
                            let trimmed = newCategoryName.trimmingCharacters(in: .whitespacesAndNewlines)
                            if !trimmed.isEmpty && !categories.contains(trimmed) {
                                categories.append(trimmed)
                                newCategoryName = ""
                            }
                        }
                        .disabled(newCategoryName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                    }
                } header: {
                    Text("ADD CUSTOM CATEGORY")
                }

                Section {
                    ForEach(categories, id: \.self) { cat in
                        HStack {
                            Text(categoryIcon(for: cat))
                            Text(cat)
                                .font(.system(size: 15))
                        }
                    }
                    .onDelete { indexSet in
                        categories.remove(atOffsets: indexSet)
                    }
                } header: {
                    Text("ACTIVE RECIPE CATEGORIES")
                } footer: {
                    Text("Swipe left to delete custom categories.")
                }
            }
            .navigationTitle("Category Manager")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }
            }
        }
    }

    private func categoryIcon(for name: String) -> String {
        let lower = name.lowercased()
        if lower.contains("bake") || lower.contains("dessert") { return "🍰" }
        if lower.contains("main") || lower.contains("meat") { return "🥩" }
        if lower.contains("soup") || lower.contains("stew") { return "🍲" }
        if lower.contains("craft") || lower.contains("soap") { return "🧼" }
        return "📖"
    }
}
