import SwiftUI
import SwiftData
import PhotosUI

// MARK: - Color Hex Helper
extension Color {
    init(hex: UInt, opacity: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: opacity
        )
    }
}

// MARK: - 1:1 Android Culinary Heirloom Theme Tokens
public enum CookbookTheme {
    public static let terracottaPrimary = Color(hex: 0x9A3412)
    public static let terracottaPrimaryDark = Color(hex: 0xEA580C)
    public static let terracottaAccent = Color(hex: 0x7C2D12)
    public static let amberSecondary = Color(hex: 0x92400E)
    public static let amberSecondaryDark = Color(hex: 0xF59E0B)
    public static let honeyTertiary = Color(hex: 0xB45309)
    public static let sageGreen = Color(hex: 0x3F6212)

    public static let creamBackgroundLight = Color(hex: 0xFCF9F2)
    public static let creamSurfaceLight = Color(hex: 0xFFFFFF)
    public static let warmBorderLight = Color(hex: 0x8C7B6B)
    public static let warmBorderSubtle = Color(hex: 0xE8DFD5)
    public static let textPrimaryLight = Color(hex: 0x18120C)
    public static let textSecondaryLight = Color(hex: 0x3B2E25)
    public static let textMutedLight = Color(hex: 0x5A4D41)

    public static let parchmentPage = Color(hex: 0xFAF6EE)
    public static let parchmentPageEdge = Color(hex: 0x8C7B6B)
    public static let ribbonRed = Color(hex: 0xBE123C)

    public static var bookshelfBackgroundGradient: LinearGradient {
        LinearGradient(
            colors: [
                Color(hex: 0xF6F0E8),
                Color(hex: 0xECE4D8),
                Color(hex: 0xE7DDD0)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}

// MARK: - Category Badge Theming (Matching Android BookshelfScreen.kt:1037)
public struct CategoryBadgeStyle {
    public let background: Color
    public let text: Color
    public let border: Color

    public static func style(for category: String) -> CategoryBadgeStyle {
        let cat = category.lowercased()
        if cat.contains("baking") || cat.contains("dessert") {
            return CategoryBadgeStyle(background: Color(hex: 0xFFF1E6), text: Color(hex: 0xC2410C), border: Color(hex: 0xFFFFD7BE))
        } else if cat.contains("main") {
            return CategoryBadgeStyle(background: Color(hex: 0xF0FDF4), text: Color(hex: 0x15803D), border: Color(hex: 0xBBF7D0))
        } else if cat.contains("soup") || cat.contains("stew") {
            return CategoryBadgeStyle(background: Color(hex: 0xFEF3C7), text: Color(hex: 0xB45309), border: Color(hex: 0xFDE68A))
        } else if cat.contains("salad") || cat.contains("side") {
            return CategoryBadgeStyle(background: Color(hex: 0xECFDF5), text: Color(hex: 0x047857), border: Color(hex: 0xA7F3D0))
        } else if cat.contains("ferment") || cat.contains("preserve") || cat.contains("jam") {
            return CategoryBadgeStyle(background: Color(hex: 0xF5F3FF), text: Color(hex: 0x6D28D9), border: Color(hex: 0xDDD6FE))
        } else {
            return CategoryBadgeStyle(background: Color(hex: 0xF5EFEB), text: Color(hex: 0x78350F), border: Color(hex: 0xE2D6C7))
        }
    }
}

public struct BookshelfView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Recipe.createdAt, order: .reverse) private var recipes: [Recipe]
    @Query private var shoppingItems: [ShoppingItem]

    @ObservedObject var profileManager = ProfileManager.shared

    @State private var searchText = ""
    @State private var selectedCategory: String = "All"
    @State private var showFavoritesOnly = false
    @State private var soundEffectsEnabled = true
    @State private var selectedRecipe: Recipe? = nil

    // Sheets & Dialogs
    @State private var isShowingScanner = false
    @State private var isShowingShoppingList = false
    @State private var isShowingConverter = false
    @State private var isShowingSettings = false
    @State private var isShowingProfileSwitcher = false
    @State private var isShowingSousChef = false
    @State private var isShowingUrlImport = false
    @State private var isShowingGuide = false
    @State private var isShowingCategoryManager = false

    // Quick edit/assign dialogs
    @State private var recipeForEdit: Recipe? = nil
    @State private var recipeForAssignProfile: Recipe? = nil
    @State private var recipeForAssignCategory: Recipe? = nil
    @State private var recipePendingDelete: Recipe? = nil
    @State private var showDeleteAlert = false

    // Scanning states
    @State private var recipesScannedInSession = 0
    @State private var isProcessingScan = false
    @State private var scanStatusMessage = ""
    @State private var scanErrorMessage: String? = nil
    @State private var isShowingErrorAlert = false

    // Photo management states
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var recipeForPhotoSelection: Recipe? = nil
    @State private var isShowingPhotoLibrary = false
    @State private var isShowingCameraPicker = false
    @State private var isShowingPhotoGenLoading = false
    @State private var photoGenStatus = ""
    @State private var photoGenError: String? = nil
    @State private var isShowingPhotoGenError = false

    private let categories = [
        "All",
        "Baking & Desserts",
        "Main Dishes",
        "Family Classics",
        "Artisan Crafts"
    ]

    // 2-column layout matching Android GridCells.Fixed(2)
    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]

    private var uncheckedShoppingCount: Int {
        shoppingItems.reduce(0) { count, item in count + (item.isChecked ? 0 : 1) }
    }

    public init() {}

    public var body: some View {
        NavigationStack {
            ZStack {
                // Android 1:1 Warm Parchment / Linen Background Gradient
                CookbookTheme.bookshelfBackgroundGradient
                    .ignoresSafeArea()

                VStack(spacing: 0) {
                    // Top Bar
                    bookshelfTopBar

                    // Search Bar & Category Chips Surface
                    searchAndFilterSection

                    // Recipe Grid
                    if filteredRecipes.isEmpty {
                        emptyStateView
                    } else {
                        ScrollView {
                            LazyVGrid(columns: columns, spacing: 12) {
                                ForEach(filteredRecipes) { recipe in
                                    RecipeBookCard(
                                        recipe: recipe,
                                        isAllFamilyView: profileManager.activeProfile == "All Family",
                                        onOpen: {
                                            AudioEffectManager.shared.playPageTurn()
                                            selectedRecipe = recipe
                                        },
                                        onToggleFavorite: {
                                            recipe.isFavorite.toggle()
                                            try? modelContext.save()
                                        },
                                        onEdit: {
                                            recipeForEdit = recipe
                                        },
                                        onChoosePhoto: {
                                            recipeForPhotoSelection = recipe
                                            isShowingPhotoLibrary = true
                                        },
                                        onTakePhoto: {
                                            recipeForPhotoSelection = recipe
                                            isShowingCameraPicker = true
                                        },
                                        onGenerateAiPhoto: {
                                            generateAiPhoto(for: recipe)
                                        },
                                        onAssignProfile: {
                                            recipeForAssignProfile = recipe
                                        },
                                        onAssignCategory: {
                                            recipeForAssignCategory = recipe
                                        },
                                        onDelete: {
                                            recipePendingDelete = recipe
                                            showDeleteAlert = true
                                        }
                                    )
                                }
                            }
                            .padding(.horizontal, 12)
                            .padding(.top, 12)
                            .padding(.bottom, 96) // Space for floating dock
                        }
                    }
                }

                // Floating Bottom Pill Dock
                VStack {
                    Spacer()
                    floatingBottomDock
                        .padding(.bottom, 14)
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
            .sheet(isPresented: $isShowingProfileSwitcher) {
                ProfileSwitcherSheet()
            }
            .sheet(isPresented: $isShowingSousChef) {
                SousChefChatSheet()
            }
            .sheet(isPresented: $isShowingUrlImport) {
                UrlRecipeImportSheet { r in
                    selectedRecipe = r
                }
            }
            .sheet(isPresented: $isShowingGuide) {
                CookbookGuideSheet()
            }
            .sheet(isPresented: $isShowingCategoryManager) {
                CategoryManagerSheet()
            }
            .sheet(item: $recipeForAssignProfile) { recipe in
                assignProfileSheet(for: recipe)
            }
            .sheet(item: $recipeForAssignCategory) { recipe in
                assignCategorySheet(for: recipe)
            }
            .alert("Delete Recipe", isPresented: $showDeleteAlert) {
                Button("Delete", role: .destructive) {
                    if let toDelete = recipePendingDelete {
                        toDelete.isDeleted = true
                        try? modelContext.save()
                        recipePendingDelete = nil
                    }
                }
                Button("Cancel", role: .cancel) {
                    recipePendingDelete = nil
                }
            } message: {
                Text("Are you sure you want to delete '\(recipePendingDelete?.title ?? "this recipe")'?")
            }
            .photosPicker(isPresented: $isShowingPhotoLibrary, selection: $selectedPhotoItem, matching: .images)
            .onChange(of: selectedPhotoItem) { _, newItem in
                guard let newItem, let targetRecipe = recipeForPhotoSelection else { return }
                Task {
                    if let data = try? await newItem.loadTransferable(type: Data.self),
                       let img = UIImage(data: data) {
                        await MainActor.run {
                            saveDishPhoto(image: img, for: targetRecipe)
                            selectedPhotoItem = nil
                            recipeForPhotoSelection = nil
                        }
                    }
                }
            }
            .fullScreenCover(isPresented: $isShowingCameraPicker) {
                if let targetRecipe = recipeForPhotoSelection {
                    CameraImagePicker(selectedImage: Binding(
                        get: { nil },
                        set: { img in
                            if let img {
                                saveDishPhoto(image: img, for: targetRecipe)
                            }
                            recipeForPhotoSelection = nil
                            isShowingCameraPicker = false
                        }
                    ))
                    .ignoresSafeArea()
                }
            }
            .alert("Food Photo Generation", isPresented: Binding(get: { photoGenError != nil }, set: { if !$0 { photoGenError = nil } })) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(photoGenError ?? "")
            }
            .overlay {
                if isProcessingScan {
                    scanningOverlay
                } else if isShowingPhotoGenLoading {
                    photoGenOverlay
                }
            }
            .alert("Notice", isPresented: $isShowingErrorAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(scanErrorMessage ?? "An unexpected error occurred.")
            }
            .onAppear {
                seedInitialRecipesIfNeeded()
            }
        }
    }

    // MARK: - Top Bar (Matching BookshelfTopBar.kt)
    private var bookshelfTopBar: some View {
        HStack(spacing: 8) {
            // Profile Pill
            ProfilePill(onClick: {
                isShowingProfileSwitcher = true
            })

            // Sous-Chef Pill Badge
            Button {
                isShowingSousChef = true
            } label: {
                HStack(spacing: 4) {
                    Text("👨‍🍳")
                        .font(.system(size: 13))
                    Text("Sous-Chef")
                        .font(.system(size: 11.5, weight: .bold))
                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(CookbookTheme.terracottaPrimary.opacity(0.12), in: Capsule())
                .overlay(
                    Capsule().stroke(CookbookTheme.terracottaPrimary.opacity(0.4), lineWidth: 1)
                )
            }
            .buttonStyle(.plain)

            Spacer()

            // Sous-Chef Copilot icon button
            Button {
                isShowingSousChef = true
            } label: {
                Text("👨‍🍳")
                    .font(.system(size: 18))
                    .frame(width: 32, height: 32)
            }

            // Shopping List with Badge
            Button {
                isShowingShoppingList = true
            } label: {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "cart.fill")
                        .font(.system(size: 16))
                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                        .frame(width: 32, height: 32)

                    if uncheckedShoppingCount > 0 {
                        Text("\(uncheckedShoppingCount)")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundStyle(.white)
                            .padding(3)
                            .background(Color.orange, in: Circle())
                            .offset(x: 2, y: -2)
                    }
                }
            }

            // Sound Effects Toggle
            Button {
                AudioEffectManager.shared.isSoundEnabled.toggle()
                soundEffectsEnabled = AudioEffectManager.shared.isSoundEnabled
                AudioEffectManager.shared.playToggle(enabled: soundEffectsEnabled)
            } label: {
                Image(systemName: soundEffectsEnabled ? "speaker.wave.2.fill" : "speaker.slash.fill")
                    .font(.system(size: 14))
                    .foregroundStyle(soundEffectsEnabled ? CookbookTheme.terracottaPrimary : CookbookTheme.textSecondaryLight)
                    .frame(width: 28, height: 28)
            }

            // More Menu
            Menu {
                Button {
                    isShowingGuide = true
                } label: {
                    Label("Cookbook Guide", systemImage: "book.closed")
                }

                Button {
                    isShowingConverter = true
                } label: {
                    Label("Smart Unit Converter", systemImage: "scalemass")
                }

                Button {
                    isShowingCategoryManager = true
                } label: {
                    Label("Manage Categories", systemImage: "tag")
                }

                Button {
                    isShowingUrlImport = true
                } label: {
                    Label("Import from Web Link", systemImage: "link")
                }

                Divider()

                Button {
                    isShowingSettings = true
                } label: {
                    Label("Settings", systemImage: "gearshape")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16, weight: .bold))
                    .rotationEffect(.degrees(90))
                    .foregroundStyle(CookbookTheme.textSecondaryLight)
                    .frame(width: 28, height: 28)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(Color(hex: 0xFFFDF9).opacity(0.95))
        .overlay(
            Rectangle()
                .fill(CookbookTheme.warmBorderSubtle)
                .frame(height: 1),
            alignment: .bottom
        )
    }

    // MARK: - Search & Category Filter Section (Matching BookshelfSearchBar.kt)
    private var searchAndFilterSection: some View {
        VStack(spacing: 8) {
            // Search Input
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(CookbookTheme.terracottaPrimary)
                    .font(.system(size: 14))

                TextField("Search recipes, ingredients, formulas...", text: $searchText)
                    .font(.system(size: 13, design: .serif))
                    .foregroundStyle(CookbookTheme.textPrimaryLight)

                if !searchText.isEmpty {
                    Button {
                        searchText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(CookbookTheme.textMutedLight)
                            .font(.system(size: 14))
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 10))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(CookbookTheme.warmBorderSubtle, lineWidth: 1)
            )
            .padding(.horizontal, 14)

            // Category & Favorites Filter Chips (Horizontally Scrollable)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 7) {
                    // Favorites toggle chip
                    Button {
                        showFavoritesOnly.toggle()
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: showFavoritesOnly ? "heart.fill" : "heart")
                                .font(.system(size: 11))
                                .foregroundStyle(showFavoritesOnly ? Color(hex: 0xE11D48) : CookbookTheme.textSecondaryLight)
                            Text("Favorites")
                                .font(.system(size: 11.5, weight: showFavoritesOnly ? .bold : .medium))
                                .foregroundStyle(showFavoritesOnly ? Color(hex: 0xE11D48) : CookbookTheme.textSecondaryLight)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            showFavoritesOnly ? Color(hex: 0xFFF1F2) : Color(hex: 0xFFFAF6F0),
                            in: Capsule()
                        )
                        .overlay(
                            Capsule().stroke(
                                showFavoritesOnly ? Color(hex: 0xE11D48).opacity(0.6) : CookbookTheme.warmBorderSubtle,
                                lineWidth: 1
                            )
                        )
                    }
                    .buttonStyle(.plain)

                    // Categories
                    ForEach(categories, id: \.self) { cat in
                        let isSelected = selectedCategory == cat && !showFavoritesOnly
                        Button {
                            showFavoritesOnly = false
                            selectedCategory = cat
                        } label: {
                            Text(cat)
                                .font(.system(size: 11.5, weight: isSelected ? .bold : .medium))
                                .foregroundStyle(isSelected ? .white : CookbookTheme.textSecondaryLight)
                                .padding(.horizontal, 11)
                                .padding(.vertical, 5)
                                .background(
                                    isSelected ? CookbookTheme.terracottaPrimary : Color(hex: 0xFFFAF6F0),
                                    in: Capsule()
                                )
                                .overlay(
                                    Capsule().stroke(
                                        isSelected ? CookbookTheme.terracottaPrimary : CookbookTheme.warmBorderSubtle,
                                        lineWidth: 1
                                    )
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 14)
            }
            .padding(.bottom, 6)
        }
        .padding(.top, 8)
        .background(Color(hex: 0xFFFDF9))
        .overlay(
            Rectangle()
                .fill(CookbookTheme.warmBorderSubtle)
                .frame(height: 1),
            alignment: .bottom
        )
        .shadow(color: Color.black.opacity(0.04), radius: 2, y: 1)
    }

    // MARK: - Floating Bottom Dock (Matching Android BookshelfScreen.kt)
    private var floatingBottomDock: some View {
        HStack(spacing: 0) {
            // Sous-Chef Copilot
            Button {
                isShowingSousChef = true
            } label: {
                HStack(spacing: 6) {
                    Text("👨‍🍳")
                        .font(.system(size: 14))
                    Text("Sous-Chef")
                        .font(.system(size: 12.5, weight: .bold))
                        .foregroundStyle(.white)
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
            }
            .buttonStyle(.plain)

            // Subtle vertical divider
            Rectangle()
                .fill(Color(hex: 0x5A493E))
                .frame(width: 1, height: 20)

            // Scan Recipe Button
            Button {
                isShowingScanner = true
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 12.5))
                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                    Text("Scan Recipe")
                        .font(.system(size: 12.5, weight: .bold))
                        .foregroundStyle(Color(hex: 0xFFD1B8))
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
            }
            .buttonStyle(.plain)
        }
        .background(Color(hex: 0x24140C), in: Capsule())
        .overlay(
            Capsule()
                .stroke(Color(hex: 0x5A493E), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.35), radius: 10, y: 4)
    }

    // MARK: - Empty State
    private var emptyStateView: some View {
        VStack(spacing: 14) {
            Spacer()
            Image(systemName: "book.pages")
                .font(.system(size: 48))
                .foregroundStyle(CookbookTheme.terracottaPrimary.opacity(0.7))

            Text("No Recipes Found")
                .font(.system(size: 18, weight: .bold, design: .serif))
                .foregroundStyle(CookbookTheme.textPrimaryLight)

            Text("Tap 'Scan Recipe' below to snap a recipe card or explore grandma's classics.")
                .font(.system(size: 13, design: .serif))
                .foregroundStyle(CookbookTheme.textMutedLight)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 36)

            Button {
                isShowingScanner = true
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "camera.fill")
                    Text("Scan First Recipe")
                }
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(.white)
                .padding(.horizontal, 16)
                .padding(.vertical, 9)
                .background(CookbookTheme.terracottaPrimary, in: Capsule())
            }
            .buttonStyle(.plain)
            .padding(.top, 6)

            Spacer()
        }
    }

    // MARK: - Filter Logic
    private var filteredRecipes: [Recipe] {
        recipes.filter { recipe in
            if recipe.isDeleted { return false }

            // Filter by active profile
            if profileManager.activeProfile != "All Family" {
                let prof = recipe.profileName.isEmpty ? "Louis" : recipe.profileName
                if prof.caseInsensitiveCompare(profileManager.activeProfile) != .orderedSame {
                    return false
                }
            }

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

    // MARK: - Seed Default Recipes
    private func seedInitialRecipesIfNeeded() {
        let defaults = DefaultRecipes.initialRecipes()
        let existingById = Dictionary(uniqueKeysWithValues: recipes.map { ($0.id, $0) })
        let existingByTitle = Dictionary(uniqueKeysWithValues: recipes.map { ($0.title.lowercased().trimmingCharacters(in: .whitespacesAndNewlines), $0) })

        for r in defaults {
            let titleKey = r.title.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
            if let existing = existingById[r.id] ?? existingByTitle[titleKey] {
                // Update missing cover photo or coverPhotoName on existing recipe
                if (existing.coverPhotoName == nil || existing.coverPhotoName?.isEmpty == true) && r.coverPhotoName != nil {
                    existing.coverPhotoName = r.coverPhotoName
                }
                if existing.imagePath == nil || existing.imagePath?.isEmpty == true {
                    existing.imagePath = r.imagePath
                }
            } else {
                modelContext.insert(r)
            }
        }
        try? modelContext.save()

        // Always ensure active profile defaults to Annette if Louis was selected or empty
        if profileManager.activeProfile.caseInsensitiveCompare("Louis") == .orderedSame || profileManager.activeProfile.isEmpty {
            profileManager.activeProfile = "Annette"
        }
    }

    // MARK: - Scanning Handler
    private func processScannedPages(_ pages: [UIImage]) async {
        guard !pages.isEmpty else { return }
        await MainActor.run {
            isProcessingScan = true
            scanStatusMessage = "Processing \(pages.count) captured page(s)..."
        }

        do {
            let (recipe, croppedCover) = try await GeminiRecipeService.shared.scanRecipePages(images: pages) { msg in
                Task { @MainActor in
                    scanStatusMessage = msg
                }
            }

            if let cover = croppedCover {
                let filename = "recipe_cover_\(recipe.id).jpg"
                if let data = cover.jpegData(compressionQuality: 0.85) {
                    let path = getDocumentsDirectory().appendingPathComponent(filename)
                    try? data.write(to: path)
                    recipe.imagePath = path.path
                }
            }

            await MainActor.run {
                recipe.profileName = profileManager.activeProfile == "All Family" ? "Louis" : profileManager.activeProfile
                modelContext.insert(recipe)
                try? modelContext.save()
                recipesScannedInSession += 1
                isProcessingScan = false
                selectedRecipe = recipe
            }
        } catch {
            await MainActor.run {
                isProcessingScan = false
                scanErrorMessage = error.localizedDescription
                isShowingErrorAlert = true
            }
        }
    }

    private func getDocumentsDirectory() -> URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }

    // MARK: - Scanning Overlay
    private var scanningOverlay: some View {
        ZStack {
            Color.black.opacity(0.65)
                .ignoresSafeArea()

            VStack(spacing: 14) {
                ProgressView()
                    .controlSize(.large)
                    .tint(CookbookTheme.terracottaPrimaryDark)

                Text("PARSING RECIPE")
                    .font(.system(size: 13, weight: .black, design: .serif))
                    .tracking(2)
                    .foregroundStyle(Color(hex: 0xFFD1B8))

                Text(scanStatusMessage)
                    .font(.system(size: 12.5, design: .serif))
                    .foregroundStyle(.white.opacity(0.9))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)
            }
            .padding(22)
            .background(Color(hex: 0x24140C), in: RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(hex: 0x5A493E), lineWidth: 1)
            )
            .shadow(radius: 20)
        }
    }

    // MARK: - Food Photo Generation Overlay
    private var photoGenOverlay: some View {
        ZStack {
            Color.black.opacity(0.65)
                .ignoresSafeArea()

            VStack(spacing: 14) {
                ProgressView()
                    .controlSize(.large)
                    .tint(CookbookTheme.terracottaPrimaryDark)

                Text("GENERATING FOOD PHOTO")
                    .font(.system(size: 13, weight: .black, design: .serif))
                    .tracking(2)
                    .foregroundStyle(Color(hex: 0xFFD1B8))

                Text(photoGenStatus)
                    .font(.system(size: 12.5, design: .serif))
                    .foregroundStyle(.white.opacity(0.9))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)
            }
            .padding(22)
            .background(Color(hex: 0x24140C), in: RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(hex: 0x5A493E), lineWidth: 1)
            )
            .shadow(radius: 20)
        }
    }

    // MARK: - Dish Photo Helpers
    private func saveDishPhoto(image: UIImage, for recipe: Recipe) {
        let filename = "recipe_cover_\(recipe.id)_\(Int(Date().timeIntervalSince1970)).jpg"
        if let data = image.jpegData(compressionQuality: 0.88) {
            let path = getDocumentsDirectory().appendingPathComponent(filename)
            try? data.write(to: path)
            recipe.imagePath = path.path
            recipe.coverPhotoName = filename
            recipe.updatedAt = Date()
            try? modelContext.save()
        }
    }

    private func generateAiPhoto(for recipe: Recipe) {
        isShowingPhotoGenLoading = true
        photoGenStatus = "Generating food photo with AI..."
        Task {
            do {
                let img: UIImage
                let engine = UserDefaults.standard.string(forKey: "image_gen_engine") ?? ImageGenEngine.gemini.rawValue
                if engine == ImageGenEngine.comfyUi.rawValue {
                    let comfyUrl = UserDefaults.standard.string(forKey: "comfyui_url") ?? "http://192.168.1.54:8188"
                    let ckpt = UserDefaults.standard.string(forKey: "comfyui_checkpoint") ?? "v1-5-pruned-emaonly.safetensors"
                    img = try await ComfyUiClient.shared.generateRecipeImage(
                        baseUrl: comfyUrl,
                        title: recipe.title,
                        category: recipe.category,
                        ingredients: recipe.ingredients.map { $0.nameEnglish ?? $0.name },
                        steps: recipe.steps.map(\.instructionEnglish),
                        customCheckpoint: ckpt
                    ) { status in
                        Task { @MainActor in photoGenStatus = status }
                    }
                } else {
                    img = try await GeminiRecipeService.shared.generateRecipeCoverImage(
                        title: recipe.title,
                        titleGerman: recipe.titleGerman,
                        category: recipe.category,
                        ingredients: recipe.ingredients.map { $0.nameEnglish ?? $0.name },
                        steps: recipe.steps.map(\.instructionEnglish),
                        notes: recipe.notes
                    )
                }

                await MainActor.run {
                    saveDishPhoto(image: img, for: recipe)
                    isShowingPhotoGenLoading = false
                }
            } catch {
                await MainActor.run {
                    isShowingPhotoGenLoading = false
                    photoGenError = error.localizedDescription
                }
            }
        }
    }

    // MARK: - Assign Profile Sheet
    private func assignProfileSheet(for recipe: Recipe) -> some View {
        NavigationStack {
            List {
                ForEach(profileManager.profiles, id: \.self) { prof in
                    Button {
                        recipe.profileName = prof
                        try? modelContext.save()
                        recipeForAssignProfile = nil
                    } label: {
                        HStack {
                            Text(ProfileManager.getProfileEmoji(name: prof))
                            Text(prof)
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(CookbookTheme.textPrimaryLight)
                            Spacer()
                            if recipe.profileName == prof {
                                Image(systemName: "checkmark")
                                    .foregroundStyle(CookbookTheme.terracottaPrimary)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Move to Family Member")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { recipeForAssignProfile = nil }
                }
            }
        }
    }

    // MARK: - Assign Category Sheet
    private func assignCategorySheet(for recipe: Recipe) -> some View {
        NavigationStack {
            List {
                ForEach(categories.filter { $0 != "All" }, id: \.self) { cat in
                    Button {
                        recipe.category = cat
                        try? modelContext.save()
                        recipeForAssignCategory = nil
                    } label: {
                        HStack {
                            Text(cat)
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(CookbookTheme.textPrimaryLight)
                            Spacer()
                            if recipe.category == cat {
                                Image(systemName: "checkmark")
                                    .foregroundStyle(CookbookTheme.terracottaPrimary)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Assign Category")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { recipeForAssignCategory = nil }
                }
            }
        }
    }
}

// MARK: - RecipeBookCard (Matching Android 1:1 RecipeBookCard in BookshelfScreen.kt)
public struct RecipeBookCard: View {
    let recipe: Recipe
    let isAllFamilyView: Bool
    let onOpen: () -> Void
    let onToggleFavorite: () -> Void
    let onEdit: () -> Void
    let onChoosePhoto: () -> Void
    let onTakePhoto: () -> Void
    let onGenerateAiPhoto: () -> Void
    let onAssignProfile: () -> Void
    let onAssignCategory: () -> Void
    let onDelete: () -> Void

    private var coverImage: UIImage? {
        recipe.resolvedCoverImage
    }

    private var hasPhoto: Bool {
        coverImage != nil
    }

    private var catStyle: CategoryBadgeStyle {
        CategoryBadgeStyle.style(for: recipe.category)
    }

    public var body: some View {
        Button(action: onOpen) {
            VStack(alignment: .leading, spacing: 0) {
                if let uiImage = coverImage {
                    // Framed Dish Photo Header (Matching Android AsyncImage)
                    ZStack(alignment: .top) {
                        Image(uiImage: uiImage)
                            .resizable()
                            .scaledToFill()
                            .frame(height: 130)
                            .frame(maxWidth: .infinity)
                            .clipped()

                        // Top Gradient for Badge Visibility
                        LinearGradient(
                            colors: [Color.black.opacity(0.55), Color.black.opacity(0.15), Color.clear],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .frame(height: 52)

                        // Top Bar over photo: Category on left, Favorite & Menu on right
                        HStack(alignment: .center) {
                            HStack(spacing: 4) {
                                Text(recipe.category)
                                    .font(.system(size: 9.5, weight: .bold))
                                    .foregroundStyle(catStyle.text)
                                    .padding(.horizontal, 7)
                                    .padding(.vertical, 2.5)
                                    .background(Color.white.opacity(0.95), in: Capsule())
                                    .overlay(Capsule().stroke(catStyle.border, lineWidth: 0.8))
                                    .lineLimit(1)

                                if isAllFamilyView {
                                    Text("\(ProfileManager.getProfileEmoji(name: recipe.profileName)) \(recipe.profileName)")
                                        .font(.system(size: 9, weight: .bold))
                                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                                        .padding(.horizontal, 5)
                                        .padding(.vertical, 2.5)
                                        .background(Color(hex: 0xFFF7ED).opacity(0.95), in: Capsule())
                                        .overlay(Capsule().stroke(CookbookTheme.terracottaPrimary.opacity(0.5), lineWidth: 0.8))
                                        .lineLimit(1)
                                }
                            }

                            Spacer()

                            cardActionButtons(isOverPhoto: true)
                        }
                        .padding(6)
                    }
                }

                // Card Body
                VStack(alignment: .leading, spacing: 6) {
                    if !hasPhoto {
                        // Top Bar: Category Pill + Characteristic Badge + Actions (Matching Android BookshelfScreen.kt:1288)
                        HStack(alignment: .center) {
                            HStack(spacing: 4) {
                                Text(recipe.category)
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundStyle(catStyle.text)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(catStyle.background, in: Capsule())
                                    .overlay(Capsule().stroke(catStyle.border, lineWidth: 1))
                                    .lineLimit(1)

                                if let characteristic = recipe.characteristicBadge {
                                    Text("✦ \(characteristic)")
                                        .font(.system(size: 9.5, weight: .bold))
                                        .foregroundStyle(Color(hex: 0x92400E))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .background(Color(hex: 0xFEF3C7), in: Capsule())
                                        .overlay(Capsule().stroke(Color(hex: 0xF59E0B).opacity(0.6), lineWidth: 1))
                                        .lineLimit(1)
                                }

                                if isAllFamilyView {
                                    Text("\(ProfileManager.getProfileEmoji(name: recipe.profileName)) \(recipe.profileName)")
                                        .font(.system(size: 9, weight: .bold))
                                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .background(Color(hex: 0xFFF7ED), in: Capsule())
                                        .overlay(Capsule().stroke(CookbookTheme.terracottaPrimary.opacity(0.5), lineWidth: 1))
                                        .lineLimit(1)
                                }
                            }

                            Spacer()

                            cardActionButtons(isOverPhoto: false)
                        }
                    }

                    // Recipe Title (Serif, Bold)
                    Text(recipe.title)
                        .font(.system(size: 15, weight: .bold, design: .serif))
                        .foregroundStyle(CookbookTheme.textPrimaryLight)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, hasPhoto ? 2 : 2)

                    // Star Rating if rated
                    if recipe.rating > 0 {
                        HStack(spacing: 2) {
                            ForEach(0..<recipe.rating, id: \.self) { _ in
                                Text("★")
                                    .font(.system(size: 10.5))
                                    .foregroundStyle(Color(hex: 0xF59E0B))
                            }
                        }
                    }

                    Spacer(minLength: 4)

                    // Bottom Row: Cook Time, Difficulty & Servings
                    HStack(alignment: .center) {
                        // Cook time pill
                        HStack(spacing: 3) {
                            Image(systemName: "clock")
                                .font(.system(size: 9.5))
                                .foregroundStyle(CookbookTheme.terracottaPrimary)
                            Text("\(recipe.cookTimeMinutes)m")
                                .font(.system(size: 10.5, weight: .semibold))
                                .foregroundStyle(Color(hex: 0x431407))
                        }
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2.5)
                        .background(Color(hex: 0xFAF3EC), in: RoundedRectangle(cornerRadius: 6))
                        .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color(hex: 0xEDE0D2), lineWidth: 1))

                        // Difficulty pill
                        Text(recipe.difficulty)
                            .font(.system(size: 9.5, weight: .medium))
                            .foregroundStyle(Color(hex: 0x5A4535))
                            .padding(.horizontal, 5)
                            .padding(.vertical, 2.5)
                            .background(Color(hex: 0xF5EFEB), in: RoundedRectangle(cornerRadius: 6))

                        Spacer()

                        // Servings
                        Text(recipe.servings)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundStyle(CookbookTheme.textMutedLight)
                            .lineLimit(1)
                    }
                }
                .padding(10)
            }
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(CookbookTheme.warmBorderSubtle, lineWidth: 1)
            )
            .shadow(color: Color(hex: 0x3D2615).opacity(0.12), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func cardActionButtons(isOverPhoto: Bool) -> some View {
        HStack(spacing: 4) {
            // Favorite Button
            Button(action: onToggleFavorite) {
                Image(systemName: recipe.isFavorite ? "heart.fill" : "heart")
                    .font(.system(size: 11))
                    .foregroundStyle(recipe.isFavorite ? Color(hex: 0xE11D48) : Color(hex: 0x4A3423))
                    .frame(width: 26, height: 26)
                    .background(Color.white.opacity(isOverPhoto ? 0.92 : 0.95), in: Circle())
                    .shadow(color: Color.black.opacity(0.08), radius: 1, y: 1)
            }
            .buttonStyle(.plain)

            // Menu Button with Photo Options
            Menu {
                Button(action: onChoosePhoto) {
                    Label("Choose Photo from Library", systemImage: "photo.on.rectangle")
                }
                Button(action: onTakePhoto) {
                    Label("Take Dish Photo", systemImage: "camera")
                }
                Button(action: onGenerateAiPhoto) {
                    Label(hasPhoto ? "Regenerate AI Food Photo" : "Generate AI Food Photo", systemImage: "sparkles")
                }
                Divider()
                Button(action: onEdit) {
                    Label("Edit Recipe", systemImage: "pencil")
                }
                Button(action: onAssignProfile) {
                    Label("Move to Family Member", systemImage: "person.2")
                }
                Button(action: onAssignCategory) {
                    Label("Assign Category", systemImage: "tag")
                }
                Divider()
                Button(role: .destructive, action: onDelete) {
                    Label("Delete Recipe", systemImage: "trash")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(Color(hex: 0x4A3423))
                    .frame(width: 26, height: 26)
                    .background(Color.white.opacity(isOverPhoto ? 0.92 : 0.95), in: Circle())
                    .shadow(color: Color.black.opacity(0.08), radius: 1, y: 1)
            }
        }
    }
}
