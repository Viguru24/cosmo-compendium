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
                        BookletCoverView(recipe: recipe) {
                            withAnimation(.easeInOut(duration: 0.3)) { currentPage = 1 }
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 14)
                        .padding(.bottom, 40)
                    }
                    .tag(0)

                    // Page 1: Lore & Table of Contents Page
                    ScrollView {
                        BookletOverviewView(recipe: recipe, totalPages: totalPages) { targetPage in
                            currentPage = targetPage
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 14)
                        .padding(.bottom, 40)
                    }
                    .tag(1)

                    // Page 2: Ingredients & Portions Page
                    ScrollView {
                        BookletIngredientsPageView(
                            ingredients: recipe.ingredients,
                            unitSystem: $unitSystem,
                            servingMultiplier: $servingMultiplier,
                            checkedIngredients: $checkedIngredients,
                            onAddAllToShoppingList: addAllToShoppingList,
                            onSelectGlossaryItem: { item in selectedGlossaryItem = item },
                            onProceedToSteps: {
                                withAnimation(.easeInOut(duration: 0.25)) { currentPage = 3 }
                            }
                        )
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
                                let instruction = step.instruction(unitSystem: unitSystem)
                                BookletStepPageView(
                                    step: step,
                                    stepIndex: index,
                                    totalSteps: recipe.steps.count,
                                    ingredients: recipe.ingredients,
                                    unitSystem: unitSystem,
                                    servingMultiplier: servingMultiplier,
                                    isDone: completedSteps.contains(index),
                                    isSpeaking: (tts.isSpeaking && tts.currentlySpeakingText == instruction),
                                    timerRemaining: stepTimerRemaining[index] ?? (step.timerMinutes * 60),
                                    isTimerRunning: stepTimerRunning[index] ?? false,
                                    onToggleReadAloud: {
                                        tts.speak(text: instruction)
                                    },
                                    onToggleDone: {
                                        if completedSteps.contains(index) {
                                            completedSteps.remove(index)
                                        } else {
                                            completedSteps.insert(index)
                                        }
                                    },
                                    onToggleTimer: {
                                        toggleStepTimer(stepIndex: index, defaultMinutes: step.timerMinutes)
                                    },
                                    onResetTimer: {
                                        resetStepTimer(stepIndex: index, defaultMinutes: step.timerMinutes)
                                    },
                                    onPrevious: {
                                        withAnimation(.easeInOut(duration: 0.25)) { currentPage -= 1 }
                                    },
                                    onNext: {
                                        withAnimation(.easeInOut(duration: 0.25)) { currentPage += 1 }
                                    }
                                )
                                .padding(.horizontal, 18)
                                .padding(.top, 14)
                                .padding(.bottom, 40)
                            }
                            .tag(3 + index)
                        }
                    }

                    // Final Page: Cook's Journal & Memories
                    ScrollView {
                        BookletJournalPageView(
                            recipe: recipe,
                            journalNotes: $journalNotes,
                            onBackToCover: {
                                withAnimation(.easeInOut(duration: 0.3)) { currentPage = 0 }
                            }
                        )
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

    private var emptyStepsPage: some View {
        VStack(spacing: 14) {
            Image(systemName: "text.book.closed")
                .font(.system(size: 40))
                .foregroundStyle(Color.brown.opacity(0.4))
            Text("No Steps Defined")
                .font(.system(size: 15, design: .serif))
                .italic()
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 40)
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
        AudioEffectManager.shared.playToggle(enabled: true)
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
        let text = "\(recipe.displayTitle())\n\n" + recipe.ingredients.map { "- \($0.displayName()): \($0.amount) \($0.unit)" }.joined(separator: "\n") + "\n\n" + recipe.steps.map { "\($0.stepNumber). \($0.instructionEnglish)" }.joined(separator: "\n")
        pdfData = text.data(using: .utf8)
        isShowingShareSheet = true
    }
}
