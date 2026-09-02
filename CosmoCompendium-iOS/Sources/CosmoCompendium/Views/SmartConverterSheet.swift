import SwiftUI

public struct SmartConverterSheet: View {
    @Environment(\.dismiss) private var dismiss

    @State private var inputAmount = "1"
    @State private var fromUnit = "cup"
    @State private var selectedIngredient = "All-Purpose Flour"

    private let units = ["cup", "tbsp", "tsp", "stick", "g", "ml", "oz", "lb"]
    private let ingredients = [
        "All-Purpose Flour",
        "Granulated Sugar",
        "Brown Sugar",
        "Powdered Sugar",
        "Butter",
        "Honey / Syrup",
        "Cocoa Powder",
        "Breadcrumbs",
        "Water / Milk"
    ]

    public init() {}

    public var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 0xF9 / 255.0, green: 0xF6 / 255.0, blue: 0xEE / 255.0)
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {
                        // Input Controls
                        VStack(alignment: .leading, spacing: 14) {
                            Text("CONVERT MEASUREMENTS")
                                .font(.system(size: 11, weight: .black, design: .serif))
                                .tracking(2)
                                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                            HStack(spacing: 12) {
                                TextField("Amount", text: $inputAmount)
                                    .keyboardType(.decimalPad)
                                    .font(.system(size: 20, weight: .bold, design: .serif))
                                    .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                                    .padding(12)
                                    .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
                                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                                    .frame(width: 100)

                                Picker("From Unit", selection: $fromUnit) {
                                    ForEach(units, id: \.self) { u in
                                        Text(u).tag(u)
                                    }
                                }
                                .pickerStyle(.menu)
                                .tint(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                .padding(8)
                                .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                            }

                            // Ingredient Selector
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Ingredient Density (for dry vs liquid accuracy):")
                                    .font(.system(size: 12, design: .serif))
                                    .foregroundStyle(.secondary)

                                Picker("Ingredient", selection: $selectedIngredient) {
                                    ForEach(ingredients, id: \.self) { ing in
                                        Text(ing).tag(ing)
                                    }
                                }
                                .pickerStyle(.menu)
                                .tint(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(8)
                                .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                            }
                        }
                        .padding(18)
                        .background(Color(red: 0xEFE8 / 255.0, green: 0xDF / 255.0, blue: 0xD0 / 255.0).opacity(0.5), in: RoundedRectangle(cornerRadius: 14))

                        // Results Cards for all 4 systems
                        let amt = Double(inputAmount) ?? 1.0
                        let results = UnitConverterService.convert(amount: amt, fromUnit: fromUnit, ingredientName: selectedIngredient)

                        VStack(spacing: 12) {
                            ForEach(results) { res in
                                HStack(spacing: 14) {
                                    Text(res.system.icon)
                                        .font(.system(size: 28))
                                        .frame(width: 44, height: 44)
                                        .background(Color.white, in: Circle())

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(res.system.label)
                                            .font(.system(size: 11, weight: .black, design: .serif))
                                            .tracking(1)
                                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                                        Text(res.formattedValue)
                                            .font(.system(size: 20, weight: .bold, design: .serif))
                                            .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))

                                        Text(res.note)
                                            .font(.system(size: 10, design: .serif))
                                            .foregroundStyle(.secondary)
                                    }

                                    Spacer()
                                }
                                .padding(16)
                                .background(Color.white, in: RoundedRectangle(cornerRadius: 12))
                                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.brown.opacity(0.15), lineWidth: 1))
                                .shadow(color: Color.black.opacity(0.04), radius: 3, y: 2)
                            }
                        }
                    }
                    .padding(20)
                }
            }
            .navigationTitle("Smart Kitchen Converter")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                }
            }
        }
    }
}
