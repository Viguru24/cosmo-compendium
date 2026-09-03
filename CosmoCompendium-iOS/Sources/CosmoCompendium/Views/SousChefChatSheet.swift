import SwiftUI

public struct ChatMessageItem: Identifiable, Equatable {
    public let id = UUID()
    public let isUser: Bool
    public let text: String
    public let date = Date()
}

public struct SousChefChatSheet: View {
    @Environment(\.dismiss) private var dismiss
    @State private var messages: [ChatMessageItem] = [
        ChatMessageItem(
            isUser: false,
            text: "Hello! I am your AI Sous Chef for Cosmo Compendium. Ask me for ingredient substitutions, recipe scaling, cooking times, or pairing suggestions while you prepare your meals!"
        )
    ]
    @State private var inputPrompt: String = ""
    @State private var isProcessing: Bool = false
    @State private var isListeningVoice: Bool = false

    private let quickPrompts = [
        "Substitutes for heavy cream?",
        "Adjust portions for 6 people",
        "Best temperature for roast beef?",
        "What can I cook with sourdough discard?",
        "How to tell when cake is done?"
    ]

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Chat conversation area
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 14) {
                            ForEach(messages) { msg in
                                chatBubble(for: msg)
                            }

                            if isProcessing {
                                HStack(spacing: 8) {
                                    ProgressView()
                                        .controlSize(.small)
                                        .tint(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                                    Text("Sous Chef is thinking...")
                                        .font(.system(size: 13, design: .serif))
                                        .foregroundStyle(Color.secondary)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(Color.white.opacity(0.8), in: Capsule())
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .id("processingIndicator")
                            }
                        }
                        .padding(18)
                    }
                    .onChange(of: messages.count) { _, _ in
                        withAnimation {
                            if let last = messages.last {
                                proxy.scrollTo(last.id, anchor: .bottom)
                            }
                        }
                    }
                }

                // Quick Prompt Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(quickPrompts, id: \.self) { q in
                            Button {
                                sendPrompt(q)
                            } label: {
                                Text(q)
                                    .font(.system(size: 12, weight: .medium))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color(red: 0xFF / 255.0, green: 0xF7 / 255.0, blue: 0xED / 255.0), in: Capsule())
                                    .overlay(
                                        Capsule().stroke(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0).opacity(0.4), lineWidth: 1)
                                    )
                                    .foregroundStyle(Color(red: 0x8C / 255.0, green: 0x3B / 255.0, blue: 0x00 / 255.0))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .background(Color(red: 0xF9 / 255.0, green: 0xF5 / 255.0, blue: 0xEE / 255.0))

                // Bottom Input Bar
                HStack(spacing: 10) {
                    // Voice Input Orb
                    Button {
                        isListeningVoice.toggle()
                        if isListeningVoice {
                            // Demo voice activation
                            sendPrompt("How do I make a golden crust on bread?")
                            isListeningVoice = false
                        }
                    } label: {
                        ZStack {
                            Circle()
                                .fill(isListeningVoice ? Color.red : Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                                .frame(width: 38, height: 38)
                            Image(systemName: isListeningVoice ? "waveform" : "mic.fill")
                                .font(.system(size: 16))
                                .foregroundStyle(.white)
                        }
                    }

                    TextField("Ask your Sous Chef anything...", text: $inputPrompt)
                        .font(.system(size: 14))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.white, in: RoundedRectangle(cornerRadius: 20))
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.black.opacity(0.1), lineWidth: 1)
                        )
                        .onSubmit {
                            let text = inputPrompt
                            inputPrompt = ""
                            sendPrompt(text)
                        }

                    Button {
                        let text = inputPrompt
                        inputPrompt = ""
                        sendPrompt(text)
                    } label: {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.system(size: 32))
                            .foregroundStyle(inputPrompt.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? Color.gray.opacity(0.4) : Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                    }
                    .disabled(inputPrompt.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isProcessing)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(red: 0xFA / 255.0, green: 0xF6 / 255.0, blue: 0xF0 / 255.0))
            }
            .background(Color(red: 0xF4 / 255.0, green: 0xEE / 255.0, blue: 0xE2 / 255.0).ignoresSafeArea())
            .navigationTitle("AI Sous Chef")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Close") { dismiss() }
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }
            }
        }
    }

    private func chatBubble(for message: ChatMessageItem) -> some View {
        HStack {
            if message.isUser { Spacer() }

            HStack(alignment: .top, spacing: 10) {
                if !message.isUser {
                    Text("👨‍🍳")
                        .font(.system(size: 20))
                }

                Text(message.text)
                    .font(.system(size: 14.5, design: message.isUser ? .default : .serif))
                    .foregroundStyle(message.isUser ? Color.white : Color(red: 0x22 / 255.0, green: 0x18 / 255.0, blue: 0x10 / 255.0))
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background(
                        message.isUser ? Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0) : Color.white,
                        in: RoundedRectangle(cornerRadius: 16)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(message.isUser ? Color.clear : Color(red: 0xE8 / 255.0, green: 0xDF / 255.0, blue: 0xD0 / 255.0), lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.04), radius: 3, y: 1)
            }

            if !message.isUser { Spacer() }
        }
    }

    private func sendPrompt(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }

        messages.append(ChatMessageItem(isUser: true, text: trimmed))
        isProcessing = true

        Task {
            let reply = await askGeminiSousChef(trimmed)
            await MainActor.run {
                messages.append(ChatMessageItem(isUser: false, text: reply))
                isProcessing = false
            }
        }
    }

    private func askGeminiSousChef(_ question: String) async -> String {
        do {
            let systemInstruction = "You are an expert AI culinary sous chef for the Cosmo Compendium recipe keeper. Provide helpful, warm, practical culinary answers regarding ingredient substitutes, measurements, food safety, and kitchen techniques. Keep answers concise, clear, and well-structured."
            return try await GeminiRecipeService.shared.generateText(prompt: question, systemInstruction: systemInstruction)
        } catch {
            return "Sous Chef Notice: \(error.localizedDescription)"
        }
    }
}
