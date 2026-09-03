import SwiftUI
import SwiftData

public struct BookshelfView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Recipe.createdAt, order: .reverse) private var recipes: [Recipe]

    @State private var searchText = ""
    @State private var selectedCategory: String = "All"
    @State private var showFavoritesOnly = false
    @State private var selectedRecipe: Recipe? = nil
    @State private var isShowingScanner = false
    @State private var isShowingShoppingList = false
    @State private var isShowingConverter = false
    @State private var isShowingSettings = false
    @State private var recipesScannedInSession = 0

    private let categories = [
        "All",
        "Baking & Desserts",
        "Main Dishes",
        "Family Classics",
        "Artisan Crafts"
    ]

    private let columns = [
        GridItem(.adaptive(minimum: 155, maximum: 190), spacing: 20)
    ]

    public init() {}

    public var body: some View {
        NavigationStack {
            ZStack {
                // Warm Walnut Woodgrain Library Background
                LinearGradient(
                    colors: [
                        Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0),
                        Color(red: 0x1A / 255.0, green: 0x0E / 255.0, blue: 0x08 / 255.0)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()

                VStack(spacing: 0) {
                    // Header Bar
                    headerBar

                    // Category Filter ScrollView
                    categoryChips

                    // Bookshelf Grid
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: 28) {
                            ForEach(filteredRecipes) { recipe in
                                RecipeBookCard(recipe: recipe) {
                                    selectedRecipe = recipe
                                }
                            }
                        }
                        .padding(.horizontal, 18)
                        .padding(.top, 16)
                        .padding(.bottom, 100)
                    }
                }
            }
            .navigationDestination(item: $selectedRecipe) { recipe in
                BookletView(recipe: recipe)
            }
            .sheet(isPresented: $isShowingScanner) {
                DocumentScannerView { scannedImages in
                    Task {
                        await processScannedPages(scannedImages)
                    }
                }
            }
            .sheet(isPresented: $isShowingShoppingList) {
                ShoppingListView()
            }
            .sheet(isPresented: $isShowingConverter) {
                SmartConverterSheet()
            }
            .sheet(isPresented: $isShowingSettings) {
                SettingsView()
            }
            .safeAreaInset(edge: .bottom) {
                bottomActionBar
            }
            .onAppear {
                seedInitialRecipesIfNeeded()
            }
        }
    }

    private var headerBar: some View {
        VStack(spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("COSMO COMPENDIUM")
                        .font(.system(size: 11, weight: .black, design: .serif))
                        .tracking(3)
                        .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))

                    Text("Heirloom Library")
                        .font(.system(size: 24, weight: .bold, design: .serif))
                        .foregroundStyle(Color(red: 0xF7 / 255.0, green: 0xEE / 255.0, blue: 0xE4 / 255.0))
                }

                Spacer()

                Button {
                    isShowingSettings = true
                } label: {
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 18))
                        .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                        .padding(8)
                        .background(Color.white.opacity(0.08), in: Circle())
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)

            // Search Bar
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                    .font(.system(size: 15))

                TextField("Search recipes, ingredients, formulas...", text: $searchText)
                    .foregroundStyle(.white)
                    .tint(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                    .font(.system(size: 14, design: .serif))

                if !searchText.isEmpty {
                    Button {
                        searchText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(.white.opacity(0.6))
                    }
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(Color.black.opacity(0.35), in: RoundedRectangle(cornerRadius: 10))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0).opacity(0.3), lineWidth: 1)
            )
            .padding(.horizontal, 20)
        }
        .padding(.bottom, 8)
    }

    private var categoryChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(categories, id: \.self) { cat in
                    let isSelected = selectedCategory == cat
                    Button {
                        selectedCategory = cat
                    } label: {
                        Text(cat)
                            .font(.system(size: 12, weight: isSelected ? .bold : .medium, design: .serif))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 6)
                            .background(
                                isSelected ?
                                Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0) :
                                Color.white.opacity(0.08),
                                in: Capsule()
                            )
                            .overlay(
                                Capsule()
                                    .stroke(
                                        isSelected ?
                                        Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0) :
                                        Color.white.opacity(0.12),
                                        lineWidth: 1
                                    )
                            )
                            .foregroundStyle(isSelected ? Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0) : .white.opacity(0.8))
                    }
                }

                // Favorites toggle chip
                Button {
                    showFavoritesOnly.toggle()
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: showFavoritesOnly ? "star.fill" : "star")
                        Text("Favorites")
                    }
                    .font(.system(size: 12, weight: showFavoritesOnly ? .bold : .medium, design: .serif))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(showFavoritesOnly ? Color.yellow.opacity(0.25) : Color.white.opacity(0.08), in: Capsule())
                    .overlay(
                        Capsule()
                            .stroke(showFavoritesOnly ? Color.yellow : Color.white.opacity(0.12), lineWidth: 1)
                    )
                    .foregroundStyle(showFavoritesOnly ? Color.yellow : .white.opacity(0.8))
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 6)
        }
    }

    private var bottomActionBar: some View {
        HStack(spacing: 16) {
            // Smart Converter Button
            Button {
                isShowingConverter = true
            } label: {
                VStack(spacing: 3) {
                    Image(systemName: "scalemass.fill")
                        .font(.system(size: 16))
                    Text("Converter")
                        .font(.system(size: 10, weight: .semibold))
                }
                .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(Color.black.opacity(0.7), in: RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.white.opacity(0.15), lineWidth: 1)
                )
            }

            // Central Scan Button (VisionKit Continuous Camera Loop)
            Button {
                isShowingScanner = true
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "camera.viewfinder")
                        .font(.system(size: 18, weight: .bold))
                    Text("Scan Recipe")
                        .font(.system(size: 14, weight: .bold, design: .serif))
                }
                .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 13)
                .background(
                    LinearGradient(
                        colors: [
                            Color(red: 0xFF / 255.0, green: 0xDF / 255.0, blue: 0x73 / 255.0),
                            Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0)
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    ),
                    in: RoundedRectangle(cornerRadius: 14)
                )
                .shadow(color: Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0).opacity(0.35), radius: 8, y: 3)
            }

            // Shopping List Button
            Button {
                isShowingShoppingList = true
            } label: {
                VStack(spacing: 3) {
                    Image(systemName: "cart.fill")
                        .font(.system(size: 16))
                    Text("Groceries")
                        .font(.system(size: 10, weight: .semibold))
                }
                .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(Color.black.opacity(0.7), in: RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.white.opacity(0.15), lineWidth: 1)
                )
            }
        }
        .padding(.horizontal, 18)
        .padding(.top, 8)
        .padding(.bottom, 6)
        .background(
            LinearGradient(
                colors: [Color.clear, Color.black.opacity(0.9), Color.black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
        )
    }

    private var filteredRecipes: [Recipe] {
        recipes.filter { recipe in
            if showFavoritesOnly && !recipe.isFavorite { return false }
            if selectedCategory != "All" && recipe.category != selectedCategory { return false }
            if searchText.isEmpty { return true }

            let q = searchText.lowercased()
            let titleMatch = recipe.title.lowercased().contains(q) ||
                             recipe.titleGerman.lowercased().contains(q) ||
                             recipe.titleEnglish.lowercased().contains(q)
            let ingMatch = recipe.ingredients.contains { $0.name.lowercased().contains(q) }
            let notesMatch = recipe.notes.lowercased().contains(q)
            return titleMatch || ingMatch || notesMatch
        }
    }

    private func seedInitialRecipesIfNeeded() {
        if recipes.isEmpty {
            let defaults = DefaultRecipes.initialRecipes()
            for r in defaults {
                modelContext.insert(r)
            }
            try? modelContext.save()
        }
    }

    private func processScannedPages(_ pages: [UIImage]) async {
        guard !pages.isEmpty else { return }
        do {
            let (recipe, croppedCover) = try await GeminiRecipeService.shared.scanRecipePages(images: pages)

            if let cover = croppedCover {
                let filename = "recipe_cover_\(recipe.id).jpg"
                if let data = cover.jpegData(compressionQuality: 0.85) {
                    let path = getDocumentsDirectory().appendingPathComponent(filename)
                    try? data.write(to: path)
                    recipe.imagePath = path.path
                }
            }

            await MainActor.run {
                modelContext.insert(recipe)
                try? modelContext.save()
                recipesScannedInSession += 1
                selectedRecipe = recipe
            }
        } catch {
            print("Scan failed: \(error.localizedDescription)")
        }
    }

    private func getDocumentsDirectory() -> URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }
}

// Visual Book Card with Realistic Leather & Debossed Gold Text
struct RecipeBookCard: View {
    let recipe: Recipe
    let onOpen: () -> Void

    var body: some View {
        Button(action: onOpen) {
            RealisticLeatherCover(theme: recipe.coverTheme, isCard: true) {
                VStack(spacing: 8) {
                    // Category & Prep Time header
                    HStack {
                        Text(recipe.category.uppercased())
                            .font(.system(size: 8, weight: .black, design: .serif))
                            .tracking(1.5)
                            .foregroundStyle(recipe.coverTheme.goldFoilColor.opacity(0.8))

                        Spacer()

                        if recipe.isFavorite {
                            Image(systemName: "star.fill")
                                .font(.system(size: 10))
                                .foregroundStyle(Color.yellow)
                        }
                    }

                    Spacer()

                    // Debossed Gold Leaf Title
                    Text(recipe.displayTitle())
                        .font(.system(size: 15, weight: .bold, design: .serif))
                        .foregroundStyle(recipe.coverTheme.goldFoilColor)
                        .multilineTextAlignment(.center)
                        .lineLimit(3)
                        .shadow(color: Color.black.opacity(0.8), radius: 1, x: 0, y: 1)
                        .padding(.horizontal, 10)

                    if let badge = recipe.characteristicBadge {
                        Text(badge.uppercased())
                            .font(.system(size: 8, weight: .black, design: .serif))
                            .tracking(1.2)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(recipe.coverTheme.goldFoilColor.opacity(0.18), in: Capsule())
                            .overlay(Capsule().stroke(recipe.coverTheme.goldFoilColor.opacity(0.45), lineWidth: 0.8))
                            .foregroundStyle(recipe.coverTheme.goldFoilColor)
                    }

                    Spacer()

                    // Footer meta: Servings and Total Time
                    HStack {
                        Label("\(recipe.totalTimeMinutes)m", systemImage: "clock")
                            .font(.system(size: 10, design: .serif))
                        Spacer()
                        Text(recipe.servings)
                            .font(.system(size: 10, design: .serif))
                    }
                    .foregroundStyle(recipe.coverTheme.goldFoilColor.opacity(0.85))
                }
                .padding(14)
            }
            .frame(height: 220)
        }
        .buttonStyle(.plain)
    }
}
