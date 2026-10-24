import SwiftUI

public struct StepTimerView: View {
    let stepIndex: Int
    let defaultMinutes: Int
    let remainingSeconds: Int
    let isRunning: Bool
    let onToggle: () -> Void
    let onReset: () -> Void

    public init(
        stepIndex: Int,
        defaultMinutes: Int,
        remainingSeconds: Int,
        isRunning: Bool,
        onToggle: @escaping () -> Void,
        onReset: @escaping () -> Void
    ) {
        self.stepIndex = stepIndex
        self.defaultMinutes = defaultMinutes
        self.remainingSeconds = remainingSeconds
        self.isRunning = isRunning
        self.onToggle = onToggle
        self.onReset = onReset
    }

    public var body: some View {
        let mins = remainingSeconds / 60
        let secs = remainingSeconds % 60

        return HStack {
            Image(systemName: "timer")
                .font(.system(size: 20))
                .foregroundStyle(Color(red: 0x9A / 255.0, green: 0x34 / 255.0, blue: 0x12 / 255.0))

            VStack(alignment: .leading, spacing: 2) {
                Text(String(format: "%02d:%02d", mins, secs))
                    .font(.system(size: 22, weight: .black, design: .monospaced))
                    .foregroundStyle(Color(red: 0x2A / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))

                Text(isRunning ? "Timer Running" : (remainingSeconds == 0 ? "Timer Complete! 🔔" : "Step Timer"))
                    .font(.system(size: 10, design: .serif))
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button(action: onReset) {
                Image(systemName: "arrow.counterclockwise")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.brown)
                    .padding(8)
                    .background(Color.brown.opacity(0.1), in: Circle())
            }

            Button(action: onToggle) {
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
}
