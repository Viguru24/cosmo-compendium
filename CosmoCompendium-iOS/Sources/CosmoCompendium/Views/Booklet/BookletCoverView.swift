import SwiftUI

public struct BookletCoverView: View {
    let recipe: Recipe
    let onOpenRecipeBook: () -> Void

    public init(recipe: Recipe, onOpenRecipeBook: @escaping () -> Void) {
        self.recipe = recipe
        self.onOpenRecipeBook = onOpenRecipeBook
    }

    public var body: some View {
        VStack(spacing: 16) {
            // Category & rating
            HStack {
                Text(recipe.category.uppercased())
                    .font(.system(size: 11, weight: .black, design: .serif))
                    .tracking(2)
                    .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

                Spacer()

                HStack(spacing: 2) {
                    ForEach(1...5, id: \.self) { star in
                        Image(systemName: star <= recipe.rating ? "star.fill" : "star")
                            .font(.system(size: 11))
                            .foregroundStyle(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0))
                    }
                }
            }

            Text(recipe.displayTitle())
                .font(.system(size: 28, weight: .bold, design: .serif))
                .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                .multilineTextAlignment(.center)

            // Times & Servings Chips
            HStack(spacing: 16) {
                Label("\(recipe.prepTimeMinutes)m prep", systemImage: "timer")
                Label("\(recipe.cookTimeMinutes)m cook", systemImage: "flame")
                Label(recipe.servings, systemImage: "person.2")
            }
            .font(.system(size: 12, design: .serif))
            .foregroundStyle(Color(red: 0x5D / 255.0, green: 0x40 / 255.0, blue: 0x37 / 255.0))

            // Cover Image
            if let uiImg = recipe.resolvedCoverImage {
                Image(uiImage: uiImg)
                    .resizable()
                    .scaledToFill()
                    .frame(maxWidth: .infinity)
                    .frame(height: 250)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.brown.opacity(0.3), lineWidth: 1.5))
                    .shadow(color: Color.black.opacity(0.12), radius: 8, y: 4)
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 14)
                        .fill(Color(red: 0xEE / 255.0, green: 0xE4 / 255.0, blue: 0xD4 / 255.0))
                        .frame(height: 220)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.brown.opacity(0.3), lineWidth: 1))

                    VStack(spacing: 10) {
                        Image(systemName: "fork.knife.circle")
                            .font(.system(size: 44))
                            .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0).opacity(0.5))
                        Text("Heritage Recipe Card")
                            .font(.system(size: 13, design: .serif))
                            .italic()
                            .foregroundStyle(Color(red: 0x6B / 255.0, green: 0x5B / 255.0, blue: 0x4E / 255.0))
                    }
                }
            }

            // Quick Stats
            HStack(spacing: 20) {
                VStack {
                    Text("\(recipe.ingredients.count)")
                        .font(.system(size: 20, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    Text("Ingredients")
                        .font(.system(size: 11, design: .serif))
                        .foregroundStyle(.secondary)
                }

                Divider().frame(height: 30)

                VStack {
                    Text("\(recipe.steps.count)")
                        .font(.system(size: 20, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    Text("Steps")
                        .font(.system(size: 11, design: .serif))
                        .foregroundStyle(.secondary)
                }

                Divider().frame(height: 30)

                VStack {
                    Text("\(recipe.timesCooked)")
                        .font(.system(size: 20, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    Text("Cooked")
                        .font(.system(size: 11, design: .serif))
                        .foregroundStyle(.secondary)
                }
            }
            .padding(.vertical, 8)

            // Open Recipe Book Button
            Button(action: onOpenRecipeBook) {
                HStack(spacing: 8) {
                    Text("Open Recipe Book")
                        .font(.system(size: 16, weight: .bold, design: .serif))
                    Image(systemName: "arrow.right")
                }
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    LinearGradient(
                        colors: [Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    in: RoundedRectangle(cornerRadius: 12)
                )
                .shadow(color: Color.black.opacity(0.18), radius: 6, y: 3)
            }
            .padding(.top, 8)
        }
    }
}
