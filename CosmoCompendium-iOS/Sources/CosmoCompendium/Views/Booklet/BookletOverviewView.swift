import SwiftUI

public struct BookletOverviewView: View {
    let recipe: Recipe
    let totalPages: Int
    let onNavigateToPage: (Int) -> Void

    public init(recipe: Recipe, totalPages: Int, onNavigateToPage: @escaping (Int) -> Void) {
        self.recipe = recipe
        self.totalPages = totalPages
        self.onNavigateToPage = onNavigateToPage
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            // Origin Story / Heritage Notes
            if !recipe.originStory.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text("HERITAGE & ORIGIN STORY")
                        .font(.system(size: 10, weight: .black, design: .serif))
                        .tracking(1.5)
                        .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

                    Text(recipe.originStory)
                        .font(.system(size: 14, design: .serif))
                        .italic()
                        .foregroundStyle(Color(red: 0x3E / 255.0, green: 0x27 / 255.0, blue: 0x23 / 255.0))
                        .lineSpacing(5)
                        .padding(14)
                        .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.18), lineWidth: 1))
                }
            }

            // Craft parameters if present
            if let craft = recipe.craftType {
                VStack(alignment: .leading, spacing: 8) {
                    Text("CRAFT FORMULA PARAMETERS")
                        .font(.system(size: 10, weight: .black, design: .serif))
                        .tracking(1.5)
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                    Grid(alignment: .leading, horizontalSpacing: 20, verticalSpacing: 8) {
                        GridRow {
                            Text("Type:").font(.system(size: 12, weight: .bold, design: .serif))
                            Text(craft).font(.system(size: 12, design: .serif))
                        }
                        if let lye = recipe.lyeRatio {
                            GridRow {
                                Text("Lye Ratio:").font(.system(size: 12, weight: .bold, design: .serif))
                                Text(lye).font(.system(size: 12, design: .serif))
                            }
                        }
                        if let cure = recipe.cureTimeWeeks {
                            GridRow {
                                Text("Cure Time:").font(.system(size: 12, weight: .bold, design: .serif))
                                Text("\(cure) weeks").font(.system(size: 12, design: .serif))
                            }
                        }
                    }
                    .padding(12)
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.brown.opacity(0.18), lineWidth: 1))
                }
            }

            // Table of Contents Section
            VStack(alignment: .leading, spacing: 10) {
                Text("TABLE OF CONTENTS")
                    .font(.system(size: 10, weight: .black, design: .serif))
                    .tracking(1.5)
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                // Jump 1: Ingredients
                Button {
                    withAnimation(.easeInOut(duration: 0.25)) { onNavigateToPage(2) }
                } label: {
                    HStack {
                        Text("Page 3: Ingredients (\(recipe.ingredients.count) items)")
                            .font(.system(size: 14, weight: .semibold, design: .serif))
                            .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                        Spacer()
                        Image(systemName: "arrow.right")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                    }
                    .padding(12)
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.15), lineWidth: 1))
                }

                // Jump 2: Step Pages
                ForEach(Array(recipe.steps.enumerated()), id: \.element.id) { idx, step in
                    Button {
                        withAnimation(.easeInOut(duration: 0.25)) { onNavigateToPage(3 + idx) }
                    } label: {
                        HStack(spacing: 10) {
                            Text("\(idx + 1)")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundStyle(.white)
                                .frame(width: 20, height: 20)
                                .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), in: Circle())

                            Text("Step \(idx + 1): \(String(step.instructionEnglish.prefix(38)))...")
                                .font(.system(size: 13, design: .serif))
                                .foregroundStyle(Color(red: 0x3E / 255.0, green: 0x27 / 255.0, blue: 0x23 / 255.0))
                                .lineLimit(1)

                            Spacer()

                            Text("p.\(4 + idx)")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 9)
                        .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.15), lineWidth: 1))
                    }
                }

                // Jump 3: Cook's Journal
                Button {
                    let journalIdx = recipe.steps.isEmpty ? 4 : (3 + recipe.steps.count)
                    withAnimation(.easeInOut(duration: 0.25)) { onNavigateToPage(journalIdx) }
                } label: {
                    HStack {
                        Text("Page \(totalPages): Cook's Journal & Notes")
                            .font(.system(size: 14, weight: .semibold, design: .serif))
                            .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                        Spacer()
                        Image(systemName: "arrow.right")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                    }
                    .padding(12)
                    .background(Color(red: 0xF5 / 255.0, green: 0xED / 255.0, blue: 0xDF / 255.0), in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                }
            }
        }
    }
}
