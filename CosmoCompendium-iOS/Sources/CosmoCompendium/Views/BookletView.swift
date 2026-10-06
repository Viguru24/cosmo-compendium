import SwiftUI
import SwiftData
import PDFKit
import PhotosUI
import AVFoundation

public struct BookletView: View {
    @Bindable var recipe: Recipe
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @AppStorage("image_gen_engine") private var imageGenEngineRaw = ImageGenEngine.comfyUi.rawValue
    @AppStorage("comfyui_url") private var comfyUiUrl = "http://192.168.1.54:8188"
    @AppStorage("comfyui_checkpoint") private var comfyUiCheckpoint = "v1-5-pruned-emaonly.safetensors"

    @StateObject private var tts = TextToSpeechManager.shared

    // Current page in the cookbook (0: Cover, 1: TOC, 2: Ingredients, 3..N: Steps, Final: Journal)
    @State private var currentPage = 0
    @State private var unitSystem: UnitSystem = .ukImperial
    @State private var servingMultiplier: Double = 1.0
    @State private var checkedIngredients: Set<String> = []
    @State private var completedSteps: Set<Int> = []

    // Step timers dictionary: stepIndex -> (remainingSeconds, isRunning, totalSeconds)
    @State private var stepTimerRemaining: [Int: Int] = [:]
    @State private var stepTimerRunning: [Int: Bool] = [:]
    @State private var stepTimerActiveTask: [Int: Timer] = [:]

    // Photo generation & editing
    @State private var isGeneratingPhoto = false
    @State private var photoGenStatus = ""
    @State private var photoGenError: String? = nil
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var isShowingCamera = false

    // Export & dialogs
    @State private var isShowingShareSheet = false
    @State private var pdfData: Data? = nil
    @State private var selectedGlossaryItem: GlossaryItem? = nil
    @State private var journalNotes: String = ""

    public init(recipe: Recipe) {
        self.recipe = recipe
    }

    private var totalPages: Int {
        // 0: Cover, 1: Overview & TOC, 2: Ingredients, 3..<3+steps: Step Pages, Last: Journal
        3 + max(recipe.steps.count, 1) + 1
    }

    public var body: some View {
        ZStack {
            // Warm Cream Parchment Paper Background
            Color(red: 0xF9 / 255.0, green: 0xF6 / 255.0, blue: 0xEE / 255.0)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Top Page Header Banner
                topPageIndicatorBar

                // Horizontal Page-Turning Cookbook Pager (Swipe Left/Right)
                TabView(selection: $currentPage) {
                    // Page 0: Book Cover Page
                    ScrollView {
                        coverPage
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                            .padding(.bottom, 40)
                    }
                    .tag(0)

                    // Page 1: Lore & Table of Contents Page
                    ScrollView {
                        overviewAndTOCPage
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                            .padding(.bottom, 40)
                    }
                    .tag(1)

                    // Page 2: Ingredients & Portions Page
                    ScrollView {
                        ingredientsPage
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                            .padding(.bottom, 40)
                    }
                    .tag(2)

                    // Pages 3..<3+steps: Individual Step Pages (One step per page!)
                    if recipe.steps.isEmpty {
                        ScrollView {
                            emptyStepsPage
                                .padding(.horizontal, 20)
                                .padding(.top, 24)
                        }
                        .tag(3)
                    } else {
                        ForEach(Array(recipe.steps.enumerated()), id: \.element.id) { index, step in
                            ScrollView {
                                stepPage(step: step, stepIndex: index)
                                    .padding(.horizontal, 18)
                                    .padding(.top, 14)
                                    .padding(.bottom, 40)
                            }
                            .tag(3 + index)
                        }
                    }

                    // Final Page: Cook's Journal & Memories
                    ScrollView {
                        cookJournalPage
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                            .padding(.bottom, 40)
                    }
                    .tag(recipe.steps.isEmpty ? 4 : (3 + recipe.steps.count))
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .onChange(of: currentPage) { _, newPage in
                    AudioEffectManager.shared.playPageTurn()
                    // Stop TTS if user turns page
                    tts.stop()
                }
            }

            if isGeneratingPhoto {
                photoGeneratingOverlay
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
        .onAppear {
            journalNotes = recipe.notes
            initStepTimers()
        }
        .onDisappear {
            tts.stop()
            invalidateAllTimers()
        }
        .sheet(item: $selectedGlossaryItem) { item in
            GlossaryDetailSheet(item: item)
        }
        .sheet(isPresented: $isShowingShareSheet) {
            if let data = pdfData {
                ShareSheet(items: [data])
            }
        }
        .photosPicker(isPresented: .constant(false), selection: $selectedPhotoItem, matching: .images)
        .onChange(of: selectedPhotoItem) { _, newItem in
            guard let newItem else { return }
            Task {
                if let data = try? await newItem.loadTransferable(type: Data.self),
                   let image = UIImage(data: data) {
                    await MainActor.run {
                        saveDishPhoto(image)
                        selectedPhotoItem = nil
                    }
                }
            }
        }
        .fullScreenCover(isPresented: $isShowingCamera) {
            CameraImagePicker(selectedImage: Binding(
                get: { nil },
                set: { img in
                    if let img {
                        saveDishPhoto(img)
                    }
                    isShowingCamera = false
                }
            ))
            .ignoresSafeArea()
        }
    }

    // MARK: - Top Page Indicator Bar
    private var topPageIndicatorBar: some View {
        HStack {
            Button {
                if currentPage > 0 {
                    withAnimation(.easeInOut(duration: 0.25)) { currentPage -= 1 }
                }
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(currentPage > 0 ? Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0) : Color.black.opacity(0.15))
                    .padding(6)
            }
            .disabled(currentPage == 0)

            Spacer()

            VStack(spacing: 2) {
                Text(pageTitle(for: currentPage))
                    .font(.system(size: 13, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))

                Text("Page \(currentPage + 1) of \(totalPages)")
                    .font(.system(size: 10, design: .serif))
                    .foregroundStyle(Color(red: 0x8C / 255.0, green: 0x7A / 255.0, blue: 0x6B / 255.0))
            }

            Spacer()

            Button {
                if currentPage < totalPages - 1 {
                    withAnimation(.easeInOut(duration: 0.25)) { currentPage += 1 }
                }
            } label: {
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(currentPage < totalPages - 1 ? Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0) : Color.black.opacity(0.15))
                    .padding(6)
            }
            .disabled(currentPage == totalPages - 1)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 6)
        .background(Color(red: 0xF2 / 255.0, green: 0xEA / 255.0, blue: 0xDC / 255.0).opacity(0.8))
        .overlay(Divider().overlay(Color.brown.opacity(0.15)), alignment: .bottom)
    }

    private func pageTitle(for page: Int) -> String {
        switch page {
        case 0: return recipe.displayTitle()
        case 1: return "Overview & Contents"
        case 2: return "Ingredients (\(recipe.ingredients.count))"
        default:
            let stepIdx = page - 3
            if stepIdx < recipe.steps.count {
                return "Step \(stepIdx + 1) of \(recipe.steps.count)"
            } else {
                return "Cook's Journal & Notes"
            }
        }
    }

    // MARK: - Page 0: Cover Page
    private var coverPage: some View {
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
            Button {
                withAnimation(.easeInOut(duration: 0.3)) { currentPage = 1 }
            } label: {
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

    // MARK: - Page 1: Lore & Table of Contents Page
    private var overviewAndTOCPage: some View {
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
                    withAnimation(.easeInOut(duration: 0.25)) { currentPage = 2 }
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
                        withAnimation(.easeInOut(duration: 0.25)) { currentPage = 3 + idx }
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
                    withAnimation(.easeInOut(duration: 0.25)) { currentPage = journalIdx }
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

    // MARK: - Page 2: Ingredients Page
    private var ingredientsPage: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header with portion multiplier
            HStack {
                Text("INGREDIENTS (\(recipe.ingredients.count))")
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

            // Grouped Ingredients Checklist
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

            // Next Page CTA Button (Proceeds to Step 1)
            Button {
                withAnimation(.easeInOut(duration: 0.25)) { currentPage = 3 }
            } label: {
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

    // MARK: - Pages 3..N: Step-by-Step Pages (One Step Per Page, Swipeable)
    private func stepPage(step: RecipeStep, stepIndex: Int) -> some View {
        let isDone = completedSteps.contains(stepIndex)
        let instruction = step.instruction(unitSystem: unitSystem)
        let matched = StepIngredientMatcher.findIngredients(
            in: instruction,
            ingredients: recipe.ingredients,
            unitSystem: unitSystem,
            multiplier: servingMultiplier
        )

        return VStack(alignment: .leading, spacing: 18) {
            // Step Header Card Banner
            HStack(alignment: .center) {
                // Step Number Circle
                Text("\(step.stepNumber)")
                    .font(.system(size: 16, weight: .bold, design: .serif))
                    .foregroundStyle(.white)
                    .frame(width: 32, height: 32)
                    .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), in: Circle())

                Text("Step \(step.stepNumber) of \(recipe.steps.count)")
                    .font(.system(size: 17, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

                Spacer()

                // High-Visibility "Read Aloud" TTS Button
                Button {
                    tts.speak(text: instruction)
                } label: {
                    HStack(spacing: 5) {
                        Image(systemName: (tts.isSpeaking && tts.currentlySpeakingText == instruction) ? "stop.fill" : "speaker.wave.2.fill")
                            .font(.system(size: 13))
                        Text((tts.isSpeaking && tts.currentlySpeakingText == instruction) ? "Stop" : "Read Aloud")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color(red: 0xF2 / 255.0, green: 0xEA / 255.0, blue: 0xDC / 255.0), in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                }

                // Complete Checkmark
                Button {
                    if isDone {
                        completedSteps.remove(stepIndex)
                    } else {
                        completedSteps.insert(stepIndex)
                    }
                } label: {
                    Image(systemName: isDone ? "checkmark.circle.fill" : "circle")
                        .font(.system(size: 24))
                        .foregroundStyle(isDone ? Color.green : Color.brown.opacity(0.4))
                }
            }
            .padding(14)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.brown.opacity(0.18), lineWidth: 1))

            // Ingredients for this step (Context-aware chips)
            if !matched.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 5) {
                        Text("🥣")
                            .font(.system(size: 12))
                        Text("Amounts needed for this step:")
                            .font(.system(size: 11, weight: .bold, design: .serif))
                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    }

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(matched) { item in
                                HStack(spacing: 4) {
                                    Text(item.displayAmount)
                                        .fontWeight(.bold)
                                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                    Text(item.ingredient.displayName())
                                        .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                                }
                                .font(.system(size: 12, design: .serif))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color(red: 0xFA / 255.0, green: 0xF5 / 255.0, blue: 0xEC / 255.0), in: RoundedRectangle(cornerRadius: 6))
                                .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color.brown.opacity(0.15), lineWidth: 1))
                            }
                        }
                    }
                }
                .padding(12)
                .background(Color.white.opacity(0.8), in: RoundedRectangle(cornerRadius: 10))
            }

            // Large, Comfortable Step Instructions
            Text(instruction)
                .font(.system(size: 19, weight: .medium, design: .serif))
                .foregroundStyle(Color(red: 0x22 / 255.0, green: 0x1A / 255.0, blue: 0x14 / 255.0))
                .lineSpacing(7)
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.brown.opacity(0.18), lineWidth: 1))
                .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)

            // Chef's Tip Box
            if let tip = step.localizedTip() {
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: "lightbulb.fill")
                        .font(.system(size: 14))
                        .foregroundStyle(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0))
                    Text(tip)
                        .font(.system(size: 13, design: .serif))
                        .italic()
                        .foregroundStyle(Color(red: 0x5D / 255.0, green: 0x40 / 255.0, blue: 0x37 / 255.0))
                        .lineSpacing(3)
                }
                .padding(12)
                .background(Color(red: 0xFF / 255.0, green: 0xFA / 255.0, blue: 0xED / 255.0), in: RoundedRectangle(cornerRadius: 8))
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0).opacity(0.4), lineWidth: 1))
            }

            // Step Timer Widget
            if step.timerMinutes > 0 || (stepTimerRemaining[stepIndex] ?? 0) > 0 {
                stepTimerView(stepIndex: stepIndex, defaultMinutes: step.timerMinutes)
            }

            // Mark Step Done Big Button
            Button {
                if isDone {
                    completedSteps.remove(stepIndex)
                } else {
                    completedSteps.insert(stepIndex)
                }
            } label: {
                HStack {
                    Image(systemName: isDone ? "checkmark.circle.fill" : "checkmark.circle")
                    Text(isDone ? "Step Completed ✓ (Tap to Undo)" : "Mark Step Done ✓")
                }
                .font(.system(size: 15, weight: .bold, design: .serif))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(isDone ? Color.green : Color(red: 0x45 / 255.0, green: 0x1A / 255.0, blue: 0x03 / 255.0), in: RoundedRectangle(cornerRadius: 10))
            }

            // Bottom Navigation Stepper Buttons
            HStack(spacing: 12) {
                Button {
                    withAnimation(.easeInOut(duration: 0.25)) { currentPage -= 1 }
                } label: {
                    HStack {
                        Image(systemName: "chevron.left")
                        Text("Previous")
                    }
                    .font(.system(size: 13, weight: .semibold, design: .serif))
                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                }

                Button {
                    withAnimation(.easeInOut(duration: 0.25)) { currentPage += 1 }
                } label: {
                    HStack {
                        Text(stepIndex < recipe.steps.count - 1 ? "Next Step" : "Journal & Notes 🎉")
                        Image(systemName: "chevron.right")
                    }
                    .font(.system(size: 13, weight: .bold, design: .serif))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), in: RoundedRectangle(cornerRadius: 8))
                }
            }
        }
    }

    private var emptyStepsPage: some View {
        VStack(spacing: 14) {
            Image(systemName: "text.book.closed")
                .font(.system(size: 40))
                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
            Text("No step-by-step instructions available.")
                .font(.system(size: 16, design: .serif))
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, minHeight: 200)
    }

    // MARK: - Final Page: Cook's Journal & Notes
    private var cookJournalPage: some View {
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
                    AudioEffectManager.shared.playSuccess()
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
            Button {
                withAnimation(.easeInOut(duration: 0.3)) { currentPage = 0 }
            } label: {
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

    // MARK: - Step Timer Widget
    private func stepTimerView(stepIndex: Int, defaultMinutes: Int) -> some View {
        let remaining = stepTimerRemaining[stepIndex] ?? (defaultMinutes * 60)
        let isRunning = stepTimerRunning[stepIndex] ?? false
        let mins = remaining / 60
        let secs = remaining % 60

        return HStack {
            Image(systemName: "timer")
                .font(.system(size: 20))
                .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

            VStack(alignment: .leading, spacing: 2) {
                Text(String(format: "%02d:%02d", mins, secs))
                    .font(.system(size: 22, weight: .black, design: .monospaced))
                    .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))

                Text(isRunning ? "Timer Running" : (remaining == 0 ? "Timer Complete! 🔔" : "Step Timer"))
                    .font(.system(size: 10, design: .serif))
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button {
                resetStepTimer(stepIndex: stepIndex, defaultMinutes: defaultMinutes)
            } label: {
                Image(systemName: "arrow.counterclockwise")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.brown)
                    .padding(8)
                    .background(Color.brown.opacity(0.1), in: Circle())
            }

            Button {
                toggleStepTimer(stepIndex: stepIndex, defaultMinutes: defaultMinutes)
            } label: {
                HStack(spacing: 4) {
                    Image(systemName: isRunning ? "pause.fill" : "play.fill")
                    Text(isRunning ? "Pause" : "Start")
                        .font(.system(size: 13, weight: .bold))
                }
                .foregroundStyle(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0), in: Capsule())
            }
        }
        .padding(12)
        .background(Color(red: 0xFF / 255.0, green: 0xF8 / 255.0, blue: 0xEB / 255.0), in: RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(red: 0xD9 / 255.0, green: 0x77 / 255.0, blue: 0x06 / 255.0).opacity(0.4), lineWidth: 1))
    }

    private func initStepTimers() {
        for (idx, step) in recipe.steps.enumerated() {
            if step.timerMinutes > 0 {
                stepTimerRemaining[idx] = step.timerMinutes * 60
                stepTimerRunning[idx] = false
            }
        }
    }

    private func toggleStepTimer(stepIndex: Int, defaultMinutes: Int) {
        let isRunning = stepTimerRunning[stepIndex] ?? false
        if isRunning {
            stepTimerActiveTask[stepIndex]?.invalidate()
            stepTimerActiveTask[stepIndex] = nil
            stepTimerRunning[stepIndex] = false
        } else {
            if (stepTimerRemaining[stepIndex] ?? 0) == 0 {
                stepTimerRemaining[stepIndex] = defaultMinutes * 60
            }
            stepTimerRunning[stepIndex] = true
            stepTimerActiveTask[stepIndex] = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
                if let cur = stepTimerRemaining[stepIndex], cur > 0 {
                    stepTimerRemaining[stepIndex] = cur - 1
                } else {
                    stepTimerActiveTask[stepIndex]?.invalidate()
                    stepTimerActiveTask[stepIndex] = nil
                    stepTimerRunning[stepIndex] = false
                    AudioServicesPlaySystemSound(1005)
                }
            }
        }
    }

    private func resetStepTimer(stepIndex: Int, defaultMinutes: Int) {
        stepTimerActiveTask[stepIndex]?.invalidate()
        stepTimerActiveTask[stepIndex] = nil
        stepTimerRunning[stepIndex] = false
        stepTimerRemaining[stepIndex] = defaultMinutes * 60
    }

    private func invalidateAllTimers() {
        for (_, t) in stepTimerActiveTask {
            t.invalidate()
        }
        stepTimerActiveTask.removeAll()
    }

    // MARK: - Helper Actions
    private func addAllToShoppingList() {
        for ing in recipe.ingredients {
            let amount = ing.convertedAmount(targetSystem: unitSystem, multiplier: servingMultiplier)
            let item = ShoppingItem(name: "\(amount) \(ing.displayName())")
            modelContext.insert(item)
        }
        try? modelContext.save()
        AudioEffectManager.shared.playSuccess()
    }

    private func saveDishPhoto(_ image: UIImage) {
        let filename = "recipe_cover_\(recipe.id)_\(Int(Date().timeIntervalSince1970)).jpg"
        if let data = image.jpegData(compressionQuality: 0.88) {
            let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let path = docs.appendingPathComponent(filename)
            try? data.write(to: path)
            recipe.imagePath = path.path
            recipe.coverPhotoName = filename
            recipe.updatedAt = Date()
            try? modelContext.save()
        }
    }

    private var photoGeneratingOverlay: some View {
        ZStack {
            Color.black.opacity(0.4).ignoresSafeArea()
            VStack(spacing: 14) {
                ProgressView().controlSize(.large).tint(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                Text(photoGenStatus)
                    .font(.system(size: 14, weight: .semibold, design: .serif))
                    .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                    .multilineTextAlignment(.center)
            }
            .padding(24)
            .background(Color(red: 0xFF / 255.0, green: 0xFD / 255.0, blue: 0xF9 / 255.0))
            .cornerRadius(16)
            .shadow(radius: 20)
            .padding(32)
        }
    }

    private func exportPdf() {
        let renderer = Image(uiImage: recipe.resolvedCoverImage ?? UIImage())
        let text = "\(recipe.displayTitle())\n\n" + recipe.ingredients.map { "- \($0.displayName()): \($0.amount) \($0.unit)" }.joined(separator: "\n") + "\n\n" + recipe.steps.map { "\($0.stepNumber). \($0.instructionEnglish)" }.joined(separator: "\n")
        pdfData = text.data(using: .utf8)
        isShowingShareSheet = true
    }
}

// MARK: - Text to Speech Manager for Step-by-Step Read Aloud
public final class TextToSpeechManager: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    public static let shared = TextToSpeechManager()

    private let synthesizer = AVSpeechSynthesizer()
    @Published public var isSpeaking: Bool = false
    @Published public var currentlySpeakingText: String? = nil

    private override init() {
        super.init()
        synthesizer.delegate = self
    }

    public func speak(text: String, isGerman: Bool = false) {
        let cleanText = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !cleanText.isEmpty else { return }

        if isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
            if currentlySpeakingText == cleanText {
                isSpeaking = false
                currentlySpeakingText = nil
                return
            }
        }

        let utterance = AVSpeechUtterance(string: cleanText)
        let localeCode = isGerman ? "de-DE" : "en-US"
        utterance.voice = AVSpeechSynthesisVoice(language: localeCode) ?? AVSpeechSynthesisVoice(language: "en-US")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * 0.92
        utterance.pitchMultiplier = 1.0

        currentlySpeakingText = cleanText
        isSpeaking = true

        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
        try? AVAudioSession.sharedInstance().setActive(true)

        synthesizer.speak(utterance)
    }

    public func stop() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
        currentlySpeakingText = nil
    }

    public func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        DispatchQueue.main.async {
            self.isSpeaking = false
            self.currentlySpeakingText = nil
        }
    }

    public func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        DispatchQueue.main.async {
            self.isSpeaking = false
            self.currentlySpeakingText = nil
        }
    }
}

// MARK: - Step Ingredient Matcher
public struct MatchedStepIngredient: Identifiable {
    public var id: String { ingredient.id }
    public let ingredient: Ingredient
    public let displayAmount: String

    public init(ingredient: Ingredient, displayAmount: String) {
        self.ingredient = ingredient
        self.displayAmount = displayAmount
    }
}

public enum StepIngredientMatcher {
    private static let stopWords: Set<String> = [
        "in", "at", "to", "for", "with", "from", "by", "on", "off", "into", "onto",
        "and", "or", "a", "an", "the", "all", "each", "every", "both", "few", "more",
        "top", "bottom", "side", "pan", "pot", "bowl", "dish", "oven", "heat", "cook",
        "bake", "stir", "mix", "whisk", "fold", "pour", "add", "place", "set", "let",
        "until", "after", "before", "while", "when", "then", "now", "well", "gently",
        "thoroughly", "together", "aside", "ready", "done", "warm", "hot", "cold",
        "medium", "high", "low", "minutes", "min", "hours", "hr", "degrees", "c", "f"
    ]

    public static func findIngredients(
        in stepInstruction: String,
        ingredients: [Ingredient],
        unitSystem: UnitSystem = .ukImperial,
        multiplier: Double = 1.0
    ) -> [MatchedStepIngredient] {
        guard !stepInstruction.isEmpty, !ingredients.isEmpty else { return [] }

        let lower = stepInstruction.lowercased()
        var results: [MatchedStepIngredient] = []
        var seenIds = Set<String>()

        for ing in ingredients {
            guard !seenIds.contains(ing.id) else { continue }
            let amountStr = ing.convertedAmount(targetSystem: unitSystem, multiplier: multiplier)

            let candidateNames = [
                ing.nameEnglish.lowercased(),
                ing.name.lowercased(),
                ing.nameGerman.lowercased()
            ].filter { !$0.isEmpty }

            var matched = false
            for cand in candidateNames {
                let words = cand.components(separatedBy: CharacterSet.alphanumerics.inverted).filter { $0.count >= 3 && !stopWords.contains($0) }
                for word in words {
                    if lower.contains(word) {
                        matched = true
                        break
                    }
                }
                if matched { break }
            }

            if matched {
                seenIds.insert(ing.id)
                results.append(MatchedStepIngredient(ingredient: ing, displayAmount: amountStr))
            }
        }

        return results
    }
}
