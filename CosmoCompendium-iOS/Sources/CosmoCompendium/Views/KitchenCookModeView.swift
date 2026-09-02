import SwiftUI
import AudioToolbox

public struct KitchenCookModeView: View {
    let recipe: Recipe
    let unitSystem: UnitSystem

    @Environment(\.dismiss) private var dismiss
    @State private var currentStepIndex = 0
    @State private var completedSteps: Set<Int> = []
    @State private var isTimerRunning = false
    @State private var timeRemaining = 0
    @State private var totalTimerDuration = 0
    @State private var timerSubscription: Timer? = nil
    @State private var isShowingIngredients = false

    public init(recipe: Recipe, unitSystem: UnitSystem = .ukImperial) {
        self.recipe = recipe
        self.unitSystem = unitSystem
    }

    public var body: some View {
        ZStack {
            // High-Contrast Deep Kitchen Dark Mode (OLED black with warm amber accents)
            Color(red: 0x12 / 255.0, green: 0x12 / 255.0, blue: 0x14 / 255.0)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Cook Mode Top Bar (Close, Title, Keep-Awake indicator, Ingredients drawer)
                topBar

                // Step Progress Indicator
                stepProgressBar

                // Center Step Instruction & Big Timer
                ScrollView {
                    VStack(spacing: 24) {
                        if !recipe.steps.isEmpty {
                            let currentStep = recipe.steps[currentStepIndex]
                            stepCard(step: currentStep)

                            // Timer Section if step has duration or user sets custom
                            if currentStep.timerMinutes > 0 || totalTimerDuration > 0 {
                                timerView
                            }
                        } else {
                            Text("No instructions found for this recipe.")
                                .foregroundStyle(.white.opacity(0.6))
                        }
                    }
                    .padding(.horizontal, 22)
                    .padding(.top, 20)
                    .padding(.bottom, 120)
                }

                Spacer()
            }
        }
        .safeAreaInset(edge: .bottom) {
            bottomNavigationControls
        }
        .sheet(isPresented: $isShowingIngredients) {
            cookModeIngredientsSheet
        }
        .onAppear {
            // Keep screen on in Cook Mode!
            UIApplication.shared.isIdleTimerDisabled = true
            loadCurrentStepTimer()
        }
        .onDisappear {
            // Restore normal auto-lock
            UIApplication.shared.isIdleTimerDisabled = false
            stopTimer()
        }
    }

    private var topBar: some View {
        HStack {
            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(10)
                    .background(Color.white.opacity(0.12), in: Circle())
            }

            Spacer()

            VStack(spacing: 2) {
                HStack(spacing: 4) {
                    Image(systemName: "sun.max.fill")
                        .font(.system(size: 10))
                        .foregroundStyle(.yellow)
                    Text("KEEP-AWAKE ACTIVE")
                        .font(.system(size: 9, weight: .black))
                        .tracking(1.5)
                        .foregroundStyle(.yellow)
                }

                Text(recipe.displayTitle())
                    .font(.system(size: 14, weight: .bold, design: .serif))
                    .foregroundStyle(.white)
                    .lineLimit(1)
            }

            Spacer()

            Button {
                isShowingIngredients = true
            } label: {
                Image(systemName: "list.bullet")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                    .padding(10)
                    .background(Color.white.opacity(0.12), in: Circle())
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 10)
        .padding(.bottom, 8)
    }

    private var stepProgressBar: some View {
        HStack(spacing: 4) {
            ForEach(0..<recipe.steps.count, id: \.self) { idx in
                Capsule()
                    .fill(
                        idx == currentStepIndex ?
                        Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0) :
                        (completedSteps.contains(idx) ? Color.green : Color.white.opacity(0.2))
                    )
                    .frame(height: 5)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 8)
    }

    private func stepCard(step: RecipeStep) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("STEP \(step.stepNumber) OF \(recipe.steps.count)")
                    .font(.system(size: 12, weight: .black, design: .serif))
                    .tracking(2)
                    .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))

                Spacer()

                if completedSteps.contains(currentStepIndex) {
                    Label("Completed", systemImage: "checkmark.circle.fill")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(.green)
                }
            }

            Text(step.instruction(unitSystem: unitSystem))
                .font(.system(size: 22, weight: .medium, design: .serif))
                .foregroundStyle(.white)
                .lineSpacing(8)

            if let tip = step.localizedTip() {
                HStack(alignment: .top, spacing: 10) {
                    Image(systemName: "lightbulb.fill")
                        .font(.system(size: 16))
                        .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                    Text(tip)
                        .font(.system(size: 14, design: .serif)).italic()
                        .foregroundStyle(.white.opacity(0.9))
                }
                .padding(14)
                .background(Color(red: 0x2A / 255.0, green: 0x1F / 255.0, blue: 0x0E / 255.0), in: RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0).opacity(0.3), lineWidth: 1))
            }
        }
        .padding(22)
        .background(Color.white.opacity(0.06), in: RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color.white.opacity(0.12), lineWidth: 1))
    }

    private var timerView: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .stroke(Color.white.opacity(0.1), lineWidth: 8)
                    .frame(width: 170, height: 170)

                let progress = totalTimerDuration > 0 ? Double(timeRemaining) / Double(totalTimerDuration) : 0.0
                Circle()
                    .trim(from: 0, to: CGFloat(progress))
                    .stroke(
                        Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0),
                        style: StrokeStyle(lineWidth: 8, lineCap: .round)
                    )
                    .rotationEffect(.degrees(-90))
                    .frame(width: 170, height: 170)

                VStack(spacing: 4) {
                    Text(formatTime(timeRemaining))
                        .font(.system(size: 32, weight: .black, design: .monospaced))
                        .foregroundStyle(.white)

                    Text(isTimerRunning ? "ACTIVE TIMER" : (timeRemaining == 0 ? "DONE" : "PAUSED"))
                        .font(.system(size: 10, weight: .bold))
                        .foregroundStyle(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0))
                }
            }

            HStack(spacing: 20) {
                Button {
                    resetTimer()
                } label: {
                    Image(systemName: "arrow.counterclockwise")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(.white)
                        .padding(12)
                        .background(Color.white.opacity(0.15), in: Circle())
                }

                Button {
                    toggleTimer()
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: isTimerRunning ? "pause.fill" : "play.fill")
                        Text(isTimerRunning ? "Pause" : "Start")
                            .fontWeight(.bold)
                    }
                    .font(.system(size: 16))
                    .foregroundStyle(.black)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0), in: Capsule())
                }
            }
        }
        .padding(20)
        .background(Color.black.opacity(0.3), in: RoundedRectangle(cornerRadius: 18))
    }

    private var bottomNavigationControls: some View {
        HStack(spacing: 16) {
            Button {
                if currentStepIndex > 0 {
                    currentStepIndex -= 1
                    loadCurrentStepTimer()
                }
            } label: {
                HStack {
                    Image(systemName: "chevron.left")
                    Text("Previous")
                }
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(currentStepIndex > 0 ? .white : .white.opacity(0.3))
                .padding(.vertical, 14)
                .frame(maxWidth: .infinity)
                .background(Color.white.opacity(0.1), in: RoundedRectangle(cornerRadius: 12))
            }
            .disabled(currentStepIndex == 0)

            Button {
                completedSteps.insert(currentStepIndex)
                if currentStepIndex < recipe.steps.count - 1 {
                    currentStepIndex += 1
                    loadCurrentStepTimer()
                } else {
                    // Finished!
                    recipe.timesCooked += 1
                    AudioServicesPlaySystemSound(1005)
                    dismiss()
                }
            } label: {
                HStack {
                    Text(currentStepIndex < recipe.steps.count - 1 ? "Next Step" : "Finish Recipe! 🎉")
                    Image(systemName: currentStepIndex < recipe.steps.count - 1 ? "chevron.right" : "checkmark")
                }
                .font(.system(size: 15, weight: .bold))
                .foregroundStyle(.black)
                .padding(.vertical, 14)
                .frame(maxWidth: .infinity)
                .background(
                    LinearGradient(
                        colors: [
                            Color(red: 0xFF / 255.0, green: 0xDF / 255.0, blue: 0x73 / 255.0),
                            Color(red: 0xEF / 255.0, green: 0xC0 / 255.0, blue: 0x50 / 255.0)
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    ),
                    in: RoundedRectangle(cornerRadius: 12)
                )
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(Color.black.opacity(0.9).ignoresSafeArea())
    }

    private var cookModeIngredientsSheet: some View {
        NavigationStack {
            List {
                ForEach(recipe.ingredients) { ing in
                    HStack {
                        Text(ing.convertedAmount(targetSystem: unitSystem))
                            .font(.system(size: 14, weight: .bold, design: .serif))
                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                        Text(ing.displayName())
                            .font(.system(size: 14, design: .serif))
                    }
                }
            }
            .navigationTitle("Ingredients Drawer")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { isShowingIngredients = false }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func loadCurrentStepTimer() {
        stopTimer()
        guard currentStepIndex < recipe.steps.count else { return }
        let mins = recipe.steps[currentStepIndex].timerMinutes
        totalTimerDuration = mins * 60
        timeRemaining = totalTimerDuration
        isTimerRunning = false
    }

    private func toggleTimer() {
        if isTimerRunning {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private func startTimer() {
        guard timeRemaining > 0 else { return }
        isTimerRunning = true
        timerSubscription = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
            } else {
                stopTimer()
                AudioServicesPlaySystemSound(1005)
                UIImpactFeedbackGenerator(style: .heavy).impactOccurred()
            }
        }
    }

    private func stopTimer() {
        isTimerRunning = false
        timerSubscription?.invalidate()
        timerSubscription = nil
    }

    private func resetTimer() {
        stopTimer()
        timeRemaining = totalTimerDuration
    }

    private func formatTime(_ totalSeconds: Int) -> String {
        let mins = totalSeconds / 60
        let secs = totalSeconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }
}
