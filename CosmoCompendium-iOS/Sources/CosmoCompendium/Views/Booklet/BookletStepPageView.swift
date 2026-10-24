import SwiftUI

public struct BookletStepPageView: View {
    let step: RecipeStep
    let stepIndex: Int
    let totalSteps: Int
    let ingredients: [RecipeIngredient]
    let unitSystem: UnitSystem
    let servingMultiplier: Double
    let isDone: Bool
    let isSpeaking: Bool
    let timerRemaining: Int
    let isTimerRunning: Bool
    let onToggleReadAloud: () -> Void
    let onToggleDone: () -> Void
    let onToggleTimer: () -> Void
    let onResetTimer: () -> Void
    let onPrevious: () -> Void
    let onNext: () -> Void

    public init(
        step: RecipeStep,
        stepIndex: Int,
        totalSteps: Int,
        ingredients: [RecipeIngredient],
        unitSystem: UnitSystem,
        servingMultiplier: Double,
        isDone: Bool,
        isSpeaking: Bool,
        timerRemaining: Int,
        isTimerRunning: Bool,
        onToggleReadAloud: @escaping () -> Void,
        onToggleDone: @escaping () -> Void,
        onToggleTimer: @escaping () -> Void,
        onResetTimer: @escaping () -> Void,
        onPrevious: @escaping () -> Void,
        onNext: @escaping () -> Void
    ) {
        self.step = step
        self.stepIndex = stepIndex
        self.totalSteps = totalSteps
        self.ingredients = ingredients
        self.unitSystem = unitSystem
        self.servingMultiplier = servingMultiplier
        self.isDone = isDone
        self.isSpeaking = isSpeaking
        self.timerRemaining = timerRemaining
        self.isTimerRunning = isTimerRunning
        self.onToggleReadAloud = onToggleReadAloud
        self.onToggleDone = onToggleDone
        self.onToggleTimer = onToggleTimer
        self.onResetTimer = onResetTimer
        self.onPrevious = onPrevious
        self.onNext = onNext
    }

    public var body: some View {
        let instruction = step.instruction(unitSystem: unitSystem)
        let matched = StepIngredientMatcher.findIngredients(
            in: instruction,
            ingredients: ingredients,
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

                Text("Step \(step.stepNumber) of \(totalSteps)")
                    .font(.system(size: 17, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

                Spacer()

                // High-Visibility "Read Aloud" TTS Button
                Button(action: onToggleReadAloud) {
                    HStack(spacing: 5) {
                        Image(systemName: isSpeaking ? "stop.fill" : "speaker.wave.2.fill")
                            .font(.system(size: 13))
                        Text(isSpeaking ? "Stop" : "Read Aloud")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color(red: 0xF2 / 255.0, green: 0xEA / 255.0, blue: 0xDC / 255.0), in: RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.brown.opacity(0.2), lineWidth: 1))
                }

                // Complete Checkmark
                Button(action: onToggleDone) {
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
            if step.timerMinutes > 0 || timerRemaining > 0 {
                StepTimerView(
                    stepIndex: stepIndex,
                    defaultMinutes: step.timerMinutes,
                    remainingSeconds: timerRemaining,
                    isRunning: isTimerRunning,
                    onToggle: onToggleTimer,
                    onReset: onResetTimer
                )
            }

            // Mark Step Done Big Button
            Button(action: onToggleDone) {
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
                Button(action: onPrevious) {
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

                Button(action: onNext) {
                    HStack {
                        Text(stepIndex < totalSteps - 1 ? "Next Step" : "Journal & Notes 🎉")
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
}
