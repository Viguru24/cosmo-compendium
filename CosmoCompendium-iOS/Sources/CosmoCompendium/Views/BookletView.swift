import SwiftUI
import SwiftData
import PDFKit

public struct BookletView: View {
    @Bindable var recipe: Recipe
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @AppStorage("image_gen_engine") private var imageGenEngineRaw = ImageGenEngine.comfyUi.rawValue
    @AppStorage("comfyui_url") private var comfyUiUrl = "http://192.168.1.54:8188"
    @AppStorage("comfyui_checkpoint") private var comfyUiCheckpoint = "v1-5-pruned-emaonly.safetensors"

    @State private var isGeneratingPhoto = false
    @State private var photoGenStatus = ""
    @State private var photoGenError: String? = nil

    @State private var activeTab = 0 // 0: Overview & Ingredients, 1: Directions, 2: Craft / Notes
    @State private var unitSystem: UnitSystem = .ukImperial
    @State private var checkedIngredients: Set<String> = []
    @State private var isShowingCookMode = false
    @State private var isShowingShareSheet = false
    @State private var pdfData: Data? = nil
    @State private var selectedGlossaryItem: GlossaryItem? = nil

    public init(recipe: Recipe) {
        self.recipe = recipe
    }

    public var body: some View {
        ZStack {
            // Aged Cream Parchment Paper Background
            Color(red: 0xF9 / 255.0, green: 0xF6 / 255.0, blue: 0xEE / 255.0)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Segmented Tab Picker
                tabPicker

                // Main Scroll Content
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        recipeHeader

                        if activeTab == 0 {
                            ingredientsSection
                        } else if activeTab == 1 {
                            directionsSection
                        } else {
                            notesAndCraftSection
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    .padding(.bottom, 90)
                }
            }

            if isGeneratingPhoto {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                VStack(spacing: 14) {
                    ProgressView()
                        .controlSize(.large)
                        .tint(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    Text(photoGenStatus)
                        .font(.system(size: 14, weight: .semibold, design: .serif))
                        .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 20)
                }
                .padding(24)
                .background(Color(red: 0xFF / 255.0, green: 0xFD / 255.0, blue: 0xF9 / 255.0))
                .cornerRadius(16)
                .shadow(radius: 20)
                .padding(32)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                HStack(spacing: 12) {
                    Button {
                        recipe.isFavorite.toggle()
                        try? modelContext.save()
                    } label: {
                        Image(systemName: recipe.isFavorite ? "star.fill" : "star")
                            .foregroundStyle(recipe.isFavorite ? .yellow : .brown)
                    }

                    Button {
                        exportPdf()
                    } label: {
                        Image(systemName: "square.and.arrow.up")
                            .foregroundStyle(.brown)
                    }
                }
            }
        }
        .safeAreaInset(edge: .bottom) {
            cookModeFloatingBar
        }
        .fullScreenCover(isPresented: $isShowingCookMode) {
            KitchenCookModeView(recipe: recipe, unitSystem: unitSystem)
        }
        .sheet(item: $selectedGlossaryItem) { item in
            GlossaryDetailSheet(item: item)
        }
        .sheet(isPresented: $isShowingShareSheet) {
            if let data = pdfData {
                ShareSheet(items: [data])
            }
        }
        .alert("Cover Photo Generation", isPresented: Binding(get: { photoGenError != nil }, set: { if !$0 { photoGenError = nil } })) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(photoGenError ?? "")
        }
    }

    private var tabPicker: some View {
        HStack(spacing: 0) {
            tabButton(title: "Ingredients", tag: 0)
            tabButton(title: "Directions", tag: 1)
            tabButton(title: recipe.craftType != nil ? "Craft Formula" : "Recipe Notes", tag: 2)
        }
        .padding(4)
        .background(Color(red: 0xE8 / 255.0, green: 0xDF / 255.0, blue: 0xD0 / 255.0), in: RoundedRectangle(cornerRadius: 10))
        .padding(.horizontal, 20)
        .padding(.vertical, 8)
    }

    private func tabButton(title: String, tag: Int) -> some View {
        Button {
            withAnimation(.spring(response: 0.3)) { activeTab = tag }
        } label: {
            Text(title)
                .font(.system(size: 13, weight: activeTab == tag ? .bold : .medium, design: .serif))
                .foregroundStyle(activeTab == tag ? Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0) : Color(red: 0x79 / 255.0, green: 0x55 / 255.0, blue: 0x48 / 255.0))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 7)
                .background(activeTab == tag ? Color.white : Color.clear, in: RoundedRectangle(cornerRadius: 8))
                .shadow(color: activeTab == tag ? Color.black.opacity(0.08) : .clear, radius: 2)
        }
    }

    private var recipeHeader: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Category & difficulty
            HStack {
                Text(recipe.category.uppercased())
                    .font(.system(size: 10, weight: .black, design: .serif))
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
                .font(.system(size: 26, weight: .bold, design: .serif))
                .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))

            // Time, Servings, Cook count
            HStack(spacing: 16) {
                Label("\(recipe.prepTimeMinutes)m prep", systemImage: "timer")
                Label("\(recipe.cookTimeMinutes)m cook", systemImage: "flame")
                Label(recipe.servings, systemImage: "person.2")
            }
            .font(.system(size: 12, design: .serif))
            .foregroundStyle(Color(red: 0x5D / 255.0, green: 0x40 / 255.0, blue: 0x37 / 255.0))

            // Cover Photo Banner & Generator
            if let path = recipe.imagePath, let uiImg = UIImage(contentsOfFile: path) {
                ZStack(alignment: .bottomTrailing) {
                    Image(uiImage: uiImg)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 200)
                        .frame(maxWidth: .infinity)
                        .clipped()
                        .cornerRadius(12)
                        .shadow(color: .black.opacity(0.12), radius: 6, y: 3)

                    Button {
                        generateCoverPhoto()
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "sparkles")
                            Text("Regenerate Photo")
                                .font(.caption.bold())
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial, in: Capsule())
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    }
                    .padding(10)
                }
            } else {
                Button {
                    generateCoverPhoto()
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 15))
                        Text(imageGenEngineRaw == ImageGenEngine.comfyUi.rawValue ? "Generate Cover Photo with Wi-Fi PC" : "Generate Cover Photo with Cloud AI")
                            .font(.system(size: 13, weight: .bold, design: .serif))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color(red: 0xFE / 255.0, green: 0xF3 / 255.0, blue: 0xC7 / 255.0))
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    .cornerRadius(10)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0).opacity(0.3), lineWidth: 1))
                }
            }

            Divider()
                .overlay(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0).opacity(0.2))
        }
    }

    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Measurement Unit System Switcher
            VStack(alignment: .leading, spacing: 6) {
                Text("MEASUREMENT SYSTEM")
                    .font(.system(size: 10, weight: .black, design: .serif))
                    .tracking(1.5)
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

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
            }

            // Add all to shopping list button
            Button {
                addAllToShoppingList()
            } label: {
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

            // Ingredient list with groups
            let grouped = Dictionary(grouping: recipe.ingredients, by: { $0.group ?? "" })
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

                            Text(ing.convertedAmount(targetSystem: unitSystem))
                                .font(.system(size: 14, weight: .bold, design: .serif))
                                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                .strikethrough(isChecked)

                            Text(ing.displayName())
                                .font(.system(size: 14, design: .serif))
                                .foregroundStyle(isChecked ? .secondary : Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                                .strikethrough(isChecked)

                            Spacer()

                            // Glossary lookup chip if German term
                            if let gloss = GermanCulinaryGlossary.findSubstitute(query: ing.name) {
                                Button {
                                    selectedGlossaryItem = gloss
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
        }
    }

    private var directionsSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            ForEach(recipe.steps, id: \.id) { step in
                HStack(alignment: .top, spacing: 14) {
                    Text("\(step.stepNumber)")
                        .font(.system(size: 16, weight: .bold, design: .serif))
                        .foregroundStyle(.white)
                        .frame(width: 28, height: 28)
                        .background(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0), in: Circle())

                    VStack(alignment: .leading, spacing: 8) {
                        Text(step.instruction(unitSystem: unitSystem))
                            .font(.system(size: 15, design: .serif))
                            .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                            .lineSpacing(4)

                        if let tip = step.localizedTip() {
                            HStack(alignment: .top, spacing: 6) {
                                Image(systemName: "lightbulb.fill")
                                    .font(.system(size: 11))
                                    .foregroundStyle(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0))
                                Text(tip)
                                    .font(.system(size: 12, design: .serif))
                                    .italic()
                                    .foregroundStyle(Color(red: 0x5D / 255.0, green: 0x40 / 255.0, blue: 0x37 / 255.0))
                            }
                            .padding(8)
                            .background(Color(red: 0xFF / 255.0, green: 0xFA / 255.0, blue: 0xED / 255.0), in: RoundedRectangle(cornerRadius: 6))
                            .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0).opacity(0.4), lineWidth: 0.8))
                        }

                        if step.timerMinutes > 0 {
                            Label("\(step.timerMinutes) minutes", systemImage: "timer")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0).opacity(0.1), in: Capsule())
                        }
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }

    private var notesAndCraftSection: some View {
        VStack(alignment: .leading, spacing: 18) {
            if let craft = recipe.craftType {
                VStack(alignment: .leading, spacing: 10) {
                    Text("CRAFT FORMULA PARAMETERS")
                        .font(.system(size: 11, weight: .black, design: .serif))
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
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                Text("ORIGIN STORY & FAMILY NOTES")
                    .font(.system(size: 11, weight: .black, design: .serif))
                    .tracking(1.5)
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))

                Text(recipe.originStory)
                    .font(.system(size: 14, design: .serif))
                    .italic()
                    .foregroundStyle(Color(red: 0x3E / 255.0, green: 0x27 / 255.0, blue: 0x23 / 255.0))
                    .lineSpacing(4)
                    .padding(14)
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.15), lineWidth: 1))

                if !recipe.notes.isEmpty {
                    Text(recipe.notes)
                        .font(.system(size: 13, design: .serif))
                        .foregroundStyle(Color(red: 0x5D / 255.0, green: 0x40 / 255.0, blue: 0x37 / 255.0))
                        .padding(12)
                }
            }
        }
    }

    private var cookModeFloatingBar: some View {
        Button {
            isShowingCookMode = true
        } label: {
            HStack(spacing: 10) {
                Image(systemName: "flame.fill")
                    .font(.system(size: 18))
                Text("Start Kitchen Cook Mode")
                    .font(.system(size: 16, weight: .bold, design: .serif))
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(
                LinearGradient(
                    colors: [
                        Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0),
                        Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                ),
                in: RoundedRectangle(cornerRadius: 14)
            )
            .shadow(color: Color.black.opacity(0.3), radius: 8, y: 4)
            .padding(.horizontal, 20)
            .padding(.bottom, 8)
        }
    }

    private func addAllToShoppingList() {
        for ing in recipe.ingredients {
            let item = ShoppingItem(
                recipeId: recipe.id,
                recipeTitle: recipe.displayTitle(),
                name: ing.displayName(),
                amount: ing.amount,
                unit: ing.unit
            )
            modelContext.insert(item)
        }
        try? modelContext.save()
    }

    private func exportPdf() {
        let data = RecipePdfGenerator.generatePdf(for: recipe, unitSystem: unitSystem)
        self.pdfData = data
        self.isShowingShareSheet = true
    }

    private func generateCoverPhoto() {
        Task {
            isGeneratingPhoto = true
            photoGenStatus = "Preparing recipe details..."
            do {
                let img: UIImage
                if imageGenEngineRaw == ImageGenEngine.comfyUi.rawValue {
                    img = try await ComfyUiClient.shared.generateRecipeImage(
                        baseUrl: comfyUiUrl,
                        title: recipe.title,
                        category: recipe.category,
                        ingredients: recipe.ingredients.map { $0.nameEnglish ?? $0.name },
                        steps: recipe.steps.map(\.instructionEnglish),
                        customCheckpoint: comfyUiCheckpoint
                    ) { status in
                        Task { @MainActor in
                            photoGenStatus = status
                        }
                    }
                } else {
                    photoGenStatus = "Connecting to Gemini AI..."
                    img = try await ComfyUiClient.shared.generateRecipeImage(
                        baseUrl: comfyUiUrl,
                        title: recipe.title,
                        category: recipe.category,
                        ingredients: recipe.ingredients.map { $0.nameEnglish ?? $0.name },
                        steps: recipe.steps.map(\.instructionEnglish),
                        customCheckpoint: comfyUiCheckpoint
                    ) { status in
                        Task { @MainActor in
                            photoGenStatus = status
                        }
                    }
                }

                let filename = "recipe_cover_\(recipe.id)_\(Int(Date().timeIntervalSince1970)).jpg"
                if let data = img.jpegData(compressionQuality: 0.88) {
                    let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
                    let path = docs.appendingPathComponent(filename)
                    try data.write(to: path)
                    recipe.imagePath = path.path
                    try? modelContext.save()
                }
            } catch {
                photoGenError = error.localizedDescription
            }
            isGeneratingPhoto = false
        }
    }
}

// Subsheet for German culinary glossary substitution
struct GlossaryDetailSheet: View {
    let item: GlossaryItem
    @Environment(\.dismiss) private var dismiss

    var body: some View {
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

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
