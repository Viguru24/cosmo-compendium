import SwiftUI
import SwiftData

public struct ShoppingListView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @Query(sort: \ShoppingItem.createdAt, order: .reverse) private var items: [ShoppingItem]

    @State private var newItemName = ""
    @State private var newItemAmount = ""

    public init() {}

    public var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 0xF9 / 255.0, green: 0xF6 / 255.0, blue: 0xEE / 255.0)
                    .ignoresSafeArea()

                VStack(spacing: 0) {
                    // Quick add input bar
                    HStack(spacing: 10) {
                        TextField("New ingredient...", text: $newItemName)
                            .font(.system(size: 14, design: .serif))
                            .padding(10)
                            .background(Color.white, in: RoundedRectangle(cornerRadius: 8))

                        TextField("Amount", text: $newItemAmount)
                            .font(.system(size: 14, design: .serif))
                            .frame(width: 80)
                            .padding(10)
                            .background(Color.white, in: RoundedRectangle(cornerRadius: 8))

                        Button {
                            addItem()
                        } label: {
                            Image(systemName: "plus.circle.fill")
                                .font(.system(size: 24))
                                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                        }
                        .disabled(newItemName.trimmingCharacters(in: .whitespaces).isEmpty)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color(red: 0xE8 / 255.0, green: 0xDF / 255.0, blue: 0xD0 / 255.0).opacity(0.6))

                    // Grouped List by Department
                    if items.isEmpty {
                        VStack(spacing: 12) {
                            Spacer()
                            Image(systemName: "cart")
                                .font(.system(size: 48))
                                .foregroundStyle(Color.brown.opacity(0.4))
                            Text("Your grocery basket is empty.")
                                .font(.system(size: 16, design: .serif))
                                .foregroundStyle(Color.brown)
                            Text("Tap 'Add All Ingredients' on any recipe card to populate.")
                                .font(.system(size: 12, design: .serif))
                                .foregroundStyle(.secondary)
                            Spacer()
                        }
                    } else {
                        let grouped = Dictionary(grouping: items, by: { $0.category })
                        List {
                            ForEach(grouped.keys.sorted(), id: \.self) { category in
                                Section {
                                    ForEach(grouped[category] ?? []) { item in
                                        HStack(spacing: 12) {
                                            Button {
                                                item.isChecked.toggle()
                                                try? modelContext.save()
                                            } label: {
                                                Image(systemName: item.isChecked ? "checkmark.circle.fill" : "circle")
                                                    .foregroundStyle(item.isChecked ? .green : Color.brown.opacity(0.5))
                                                    .font(.system(size: 20))
                                            }
                                            .buttonStyle(.plain)

                                            VStack(alignment: .leading, spacing: 2) {
                                                HStack {
                                                    if !item.amount.isEmpty {
                                                        Text(item.amount)
                                                            .font(.system(size: 14, weight: .bold, design: .serif))
                                                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                                    }
                                                    Text(item.name)
                                                        .font(.system(size: 15, design: .serif))
                                                        .foregroundStyle(item.isChecked ? .secondary : Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                                                        .strikethrough(item.isChecked)
                                                }

                                                if let title = item.recipeTitle {
                                                    Text("For: \(title)")
                                                        .font(.system(size: 11, design: .serif)).italic()
                                                        .foregroundStyle(.secondary)
                                                }
                                            }

                                            Spacer()
                                        }
                                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                            Button(role: .destructive) {
                                                modelContext.delete(item)
                                                try? modelContext.save()
                                            } label: {
                                                Label("Delete", systemImage: "trash")
                                            }
                                        }
                                    }
                                } header: {
                                    Text(category.uppercased())
                                        .font(.system(size: 11, weight: .bold, design: .serif))
                                        .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                                }
                            }
                        }
                        .listStyle(.insetGrouped)
                    }
                }
            }
            .navigationTitle("Pantry & Grocery Basket")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    if items.contains(where: { $0.isChecked }) {
                        Button("Clear Checked") {
                            clearChecked()
                        }
                        .font(.system(size: 13))
                        .foregroundStyle(.red)
                    }
                }

                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                }
            }
        }
    }

    private func addItem() {
        let clean = newItemName.trimmingCharacters(in: .whitespaces)
        guard !clean.isEmpty else { return }
        let item = ShoppingItem(name: clean, amount: newItemAmount.trimmingCharacters(in: .whitespaces))
        modelContext.insert(item)
        try? modelContext.save()
        newItemName = ""
        newItemAmount = ""
    }

    private func clearChecked() {
        for item in items where item.isChecked {
            modelContext.delete(item)
        }
        try? modelContext.save()
    }
}
