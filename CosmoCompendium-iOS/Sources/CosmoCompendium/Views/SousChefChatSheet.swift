import SwiftUI

public struct ChatMessageItem: Identifiable, Equatable {
    public let id = UUID()
    public let isUser: Bool
    public let text: String
    public let date = Date()
}

public struct SousChefChatSheet: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var speechManager = SpeechRecognizerManager.shared
    @State private var messages: [ChatMessageItem] = [
        ChatMessageItem(
            isUser: false,
            text: "Hello! I am your AI Sous Chef for Cookbook. Ask me for ingredient substitutions, recipe scaling, cooking times, or pairing suggestions while you prepare your meals!"
        )
    ]
    @State private var inputPrompt: String = ""
    @State private var isProcessing: Bool = false
    @State private var showErrorAlert: Bool = false

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

                // Real-time listening indicator banner
                if speechManager.isRecording {
                    HStack(spacing: 8) {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 8, height: 8)
                        Text("Listening to your voice... Speak naturally, then tap mic or send.")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundStyle(Color.red)
                        Spacer()
                        Button("Done") {
                            speechManager.stopRecording()
                        }
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color.red.opacity(0.08))
                }

                // Bottom Input Bar
                HStack(spacing: 10) {
                    // Voice Input Orb (Live Speech Recording)
                    Button {
                        if speechManager.isRecording {
                            speechManager.stopRecording()
                        } else {
                            Task {
                                await speechManager.startRecording { spoken in
                                    inputPrompt = spoken
                                }
                                if speechManager.errorMessage != nil {
                                    showErrorAlert = true
                                }
                            }
                        }
                    } label: {
                        ZStack {
                            if speechManager.isRecording {
                                Circle()
                                    .stroke(Color.red.opacity(0.4), lineWidth: 4)
                                    .frame(width: 44, height: 44)
                            }
                            Circle()
                                .fill(speechManager.isRecording ? Color.red : Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                                .frame(width: 38, height: 38)
                            Image(systemName: speechManager.isRecording ? "waveform" : "mic.fill")
                                .font(.system(size: 16))
                                .foregroundStyle(.white)
                        }
                    }

                    TextField(speechManager.isRecording ? "Listening to what you're saying..." : "Ask your Sous Chef anything...", text: $inputPrompt)
                        .font(.system(size: 14))
                        .foregroundStyle(Color(red: 0x1E / 255.0, green: 0x14 / 255.0, blue: 0x0C / 255.0))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.white, in: RoundedRectangle(cornerRadius: 20))
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(speechManager.isRecording ? Color.red.opacity(0.5) : Color.black.opacity(0.1), lineWidth: speechManager.isRecording ? 1.5 : 1)
                        )
                        .onSubmit {
                            if speechManager.isRecording { speechManager.stopRecording() }
                            let text = inputPrompt
                            inputPrompt = ""
                            sendPrompt(text)
                        }

                    Button {
                        if speechManager.isRecording { speechManager.stopRecording() }
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
                    Button("Close") {
                        speechManager.stopRecording()
                        dismiss()
                    }
                    .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }
            }
            .onDisappear {
                speechManager.stopRecording()
            }
            .alert("Microphone Access", isPresented: $showErrorAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(speechManager.errorMessage ?? "Microphone and speech recognition permissions are required for voice input.")
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
            let systemInstruction = "You are an expert AI culinary sous chef for the Cookbook app. Provide helpful, warm, practical culinary answers regarding ingredient substitutes, measurements, food safety, and kitchen techniques. Keep answers concise, clear, and well-structured."
            return try await GeminiRecipeService.shared.generateText(prompt: question, systemInstruction: systemInstruction)
        } catch {
            let desc = error.localizedDescription
            if desc.contains("403") || desc.lowercased().contains("blocked") || desc.lowercased().contains("disabled") {
                return "👩‍🍳 **Sous Chef Culinary Note:**\n\nI'm answering in offline mode because your Google Gemini API key needs to be configured in Settings. (You can grab a free key in 30 seconds at **aistudio.google.com**).\n\n**Quick Culinary Advice for your question:**\n• For baking substitutes: 1 egg = 1/4 cup unsweetened applesauce or yogurt; 1 cup buttermilk = 1 cup milk + 1 tbsp vinegar/lemon juice.\n• For cooking balance: if a dish is too salty, add a splash of acid (lemon/vinegar) or starch (potato); if too rich, add fresh herbs or zest."
            }
            return "Sous Chef Notice: \(desc)"
        }
    }
}
