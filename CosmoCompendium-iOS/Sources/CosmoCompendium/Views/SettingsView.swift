import SwiftUI
import SwiftData

public struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @AppStorage("gemini_api_key") private var geminiApiKey = ""
    @AppStorage("default_unit_system") private var defaultUnitSystemRaw = UnitSystem.ukImperial.rawValue
    @AppStorage("language_mode") private var languageModeRaw = LanguageMode.both.rawValue

    @AppStorage("gemini_selected_model") private var selectedModel = "gemini-2.5-flash"
    @State private var availableModels: [String] = [
        "gemini-2.5-flash",
        "gemini-3.5-flash",
        "gemini-flash-latest",
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-3.7-flash"
    ]

    @State private var showingResetAlert = false
    @State private var isTestingApiKey = false
    @State private var apiTestResult: (success: Bool, message: String)? = nil

    public init() {}

    public var body: some View {
        NavigationStack {
            Form {
                Section {
                    SecureField("AI Studio Gemini API Key", text: $geminiApiKey)
                        .font(.system(size: 14, design: .monospaced))

                    HStack {
                        Button {
                            Task {
                                isTestingApiKey = true
                                let res = await GeminiRecipeService.shared.testApiKey(geminiApiKey)
                                apiTestResult = (res.0, res.1)
                                if !res.2.isEmpty {
                                    availableModels = res.2
                                    if !res.2.contains(selectedModel) {
                                        let flash = res.2.filter { $0.contains("flash") }
                                        selectedModel = flash.first ?? res.2.first ?? "gemini-2.0-flash"
                                    }
                                }
                                isTestingApiKey = false
                            }
                        } label: {
                            HStack(spacing: 6) {
                                if isTestingApiKey {
                                    ProgressView()
                                        .controlSize(.small)
                                } else {
                                    Image(systemName: "bolt.badge.checkmark.fill")
                                }
                                Text("Test Connection")
                                    .font(.system(size: 13, weight: .semibold))
                            }
                        }
                        .disabled(geminiApiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isTestingApiKey)

                        Spacer()

                        if let res = apiTestResult {
                            HStack(spacing: 4) {
                                Image(systemName: res.success ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                                    .foregroundStyle(res.success ? .green : .red)
                                Text(res.success ? "Active" : "Failed")
                                    .font(.caption.bold())
                                    .foregroundStyle(res.success ? .green : .red)
                            }
                        }
                    }

                    if let res = apiTestResult {
                        Text(res.message)
                            .font(.caption2)
                            .foregroundStyle(res.success ? Color.secondary : Color.red)
                    }

                    // Model Selection
                    Picker("Active Gemini Model", selection: $selectedModel) {
                        ForEach(availableModels, id: \.self) { m in
                            Text(m).tag(m)
                        }
                    }
                    .pickerStyle(.menu)

                    HStack {
                        Text("Model ID:")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(.secondary)
                        TextField("e.g. gemini-2.5-flash", text: $selectedModel)
                            .font(.system(size: 13, design: .monospaced))
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                    }

                    Link(destination: URL(string: "https://aistudio.google.com/app/apikey")!) {
                        HStack {
                            Text("Get a Free Gemini API Key")
                            Spacer()
                            Image(systemName: "arrow.up.right.square")
                        }
                        .font(.system(size: 13))
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                    }
                } header: {
                    Text("GEMINI VISION AI SCANNER")
                } footer: {
                    Text("Select or enter the exact model ID for your API key tier. The app uses this model first for instant scanning and Sous Chef answers.")
                }

                Section {
                    Picker("Default Unit System", selection: $defaultUnitSystemRaw) {
                        ForEach(UnitSystem.allCases) { sys in
                            Text("\(sys.icon) \(sys.label)").tag(sys.rawValue)
                        }
                    }

                    Picker("Language Mode", selection: $languageModeRaw) {
                        ForEach(LanguageMode.allCases) { mode in
                            Text(mode.label).tag(mode.rawValue)
                        }
                    }
                } header: {
                    Text("MEASUREMENTS & LOCALIZATION")
                }

                Section {
                    Button(role: .destructive) {
                        showingResetAlert = true
                    } label: {
                        HStack {
                            Image(systemName: "arrow.counterclockwise")
                            Text("Restore Default Recipe Collection")
                        }
                    }
                } header: {
                    Text("DATA MANAGEMENT")
                } footer: {
                    Text("Restores the original classic recipes (Apple Strudel, Sunday Roast, Black Forest Gateau, and Artisan Soap).")
                }

                Section {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0 (Native iOS)")
                            .foregroundStyle(.secondary)
                    }
                    HStack {
                        Text("Engine")
                        Spacer()
                        Text("Swift 5.9 / SwiftData")
                            .foregroundStyle(.secondary)
                    }
                } header: {
                    Text("ABOUT COSMO COMPENDIUM")
                }
            }
            .navigationTitle("Settings & AI Config")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                }
            }
            .alert("Restore Classic Recipes?", isPresented: $showingResetAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Restore", role: .destructive) {
                    restoreDefaultRecipes()
                }
            } message: {
                Text("This will insert any missing default recipes into your collection.")
            }
            .onAppear {
                let saved = GeminiRecipeService.shared.getDiscoveredModels()
                for s in saved where !availableModels.contains(s) {
                    availableModels.append(s)
                }
            }
        }
    }

    private func restoreDefaultRecipes() {
        let defaults = DefaultRecipes.initialRecipes()
        for r in defaults {
            modelContext.insert(r)
        }
        try? modelContext.save()
    }
}
