import SwiftUI
import SwiftData

public struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @AppStorage("gemini_api_key") private var geminiApiKey = ""
    @AppStorage("default_unit_system") private var defaultUnitSystemRaw = UnitSystem.ukImperial.rawValue
    @AppStorage("language_mode") private var languageModeRaw = LanguageMode.both.rawValue

    @State private var showingResetAlert = false

    public init() {}

    public var body: some View {
        NavigationStack {
            Form {
                Section {
                    SecureField("AI Studio Gemini API Key", text: $geminiApiKey)
                        .font(.system(size: 14, design: .monospaced))

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
                    Text("Empowers continuous multi-page synthesis, Kurrentschrift/cursive handwritten recipe parsing, and food photo auto-cropping. If no key is set, the offline OCR engine will be used.")
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
                            Text("Restore Heirloom Recipe Collection")
                        }
                    }
                } header: {
                    Text("DATA MANAGEMENT")
                } footer: {
                    Text("Restores the original family recipes (Apple Strudel, Sunday Roast, Black Forest Gateau, and Artisan Soap).")
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
            .alert("Restore Heirloom Recipes?", isPresented: $showingResetAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Restore", role: .destructive) {
                    restoreDefaultRecipes()
                }
            } message: {
                Text("This will insert any missing heirloom recipes into your collection.")
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
