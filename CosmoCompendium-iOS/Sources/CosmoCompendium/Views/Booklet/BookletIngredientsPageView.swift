import SwiftUI

public struct BookletIngredientsPageView: View {
    let ingredients: [RecipeIngredient]
    @Binding var unitSystem: UnitSystem
    @Binding var servingMultiplier: Double
    @Binding var checkedIngredients: Set<String>
    let onAddAllToShoppingList: () -> Void
    let onSelectGlossaryItem: (GlossaryItem) -> Void
    let onProceedToSteps: () -> Void

    public init(
        ingredients: [RecipeIngredient],
        unitSystem: Binding<UnitSystem>,
        servingMultiplier: Binding<Double>,
        checkedIngredients: Binding<Set<String>>,
        onAddAllToShoppingList: @escaping () -> Void,
        onSelectGlossaryItem: @escaping (GlossaryItem) -> Void,
        onProceedToSteps: @escaping () -> Void
    ) {
        self.ingredients = ingredients
        self._unitSystem = unitSystem
        self._servingMultiplier = servingMultiplier
        self._checkedIngredients = checkedIngredients
        self.onAddAllToShoppingList = onAddAllToShoppingList
        self.onSelectGlossaryItem = onSelectGlossaryItem
        self.onProceedToSteps = onProceedToSteps
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header with portion multiplier
            HStack {
                Text("INGREDIENTS (\(ingredients.count))")
                    .font(.system(size: 11, weight: .black, design: .serif))
                    .tracking(1.5)
                    .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

                Spacer()

                // Portions Scaler Stepper (- / +)
                HStack(spacing: 6) {
                    Button {
                        if servingMultiplier > 0.5 { servingMultiplier -= 0.5 }
                    } label: {
                        Image(systemName: "minus.circle.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    }

                    Text(String(format: "%.1fx", servingMultiplier))
                        .font(.system(size: 12, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                        .frame(minWidth: 32)

                    Button {
                        if servingMultiplier < 5.0 { servingMultiplier += 0.5 }
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    }
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.white, in: Capsule())
                .overlay(Capsule().stroke(Color.brown.opacity(0.2), lineWidth: 1))
            }

            // Measurement System Picker Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(UnitSystem.allCases) { sys in
                        let isSel = unitSystem == sys
                        Button {
                            unitSystem = sys
                        } label: {
                            HStack(spacing: 4) {
                                Text(sys.icon)
                                Text(sys.shortLabel)
                            }
                            .font(.system(size: 11, weight: isSel ? .bold : .medium))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(isSel ? Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0) : Color.white, in: Capsule())
                            .overlay(Capsule().stroke(Color.brown.opacity(0.25), lineWidth: 1))
                            .foregroundStyle(isSel ? .white : Color(red: 0x3E / 255.0, green: 0x27 / 255.0, blue: 0x23 / 255.0))
                        }
                    }
                }
            }

            // Add all to Grocery List Button
            Button(action: onAddAllToShoppingList) {
                HStack {
                    Image(systemName: "cart.badge.plus")
                    Text("Add All Ingredients to Grocery List")
                }
                .font(.system(size: 13, weight: .semibold, design: .serif))
                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.2), lineWidth: 1))
            }

            // Grouped Ingredients Checklist
            let grouped = Dictionary(grouping: ingredients, by: { $0.group ?? "" })
            ForEach(grouped.keys.sorted(), id: \.self) { groupKey in
                VStack(alignment: .leading, spacing: 8) {
                    if !groupKey.isEmpty {
                        Text(groupKey.uppercased())
                            .font(.system(size: 11, weight: .bold, design: .serif))
                            .tracking(1)
                            .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                            .padding(.top, 4)
                    }

                    ForEach(grouped[groupKey] ?? [], id: \.id) { ing in
                        let isChecked = checkedIngredients.contains(ing.id)
                        HStack(spacing: 12) {
                            Button {
                                if isChecked {
                                    checkedIngredients.remove(ing.id)
                                } else {
                                    checkedIngredients.insert(ing.id)
                                }
                            } label: {
                                Image(systemName: isChecked ? "checkmark.circle.fill" : "circle")
                                    .foregroundStyle(isChecked ? Color.green : Color.brown.opacity(0.5))
                                    .font(.system(size: 18))
                            }

                            Text(ing.convertedAmount(targetSystem: unitSystem, multiplier: servingMultiplier))
                                .font(.system(size: 14, weight: .bold, design: .serif))
                                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                .strikethrough(isChecked)

                            Text(ing.displayName())
                                .font(.system(size: 14, design: .serif))
                                .foregroundStyle(isChecked ? .secondary : Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                                .strikethrough(isChecked)

                            Spacer()

                            if let gloss = GermanCulinaryGlossary.findSubstitute(query: ing.name) {
                                Button {
                                    onSelectGlossaryItem(gloss)
                                } label: {
                                    Image(systemName: "questionmark.circle")
                                        .font(.system(size: 13))
                                        .foregroundStyle(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0))
                                }
                            }
                        }
                        .padding(.vertical, 4)
                        Divider().overlay(Color.brown.opacity(0.1))
                    }
                }
            }

            // Next Page CTA Button (Proceeds to Step 1)
            Button(action: onProceedToSteps) {
                HStack {
                    Text("Proceed to Step 1")
                        .font(.system(size: 15, weight: .bold, design: .serif))
                    Image(systemName: "arrow.right")
                }
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), in: RoundedRectangle(cornerRadius: 10))
            }
            .padding(.top, 8)
        }
    }
}
