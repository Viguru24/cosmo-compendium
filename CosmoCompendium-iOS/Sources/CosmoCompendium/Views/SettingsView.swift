import SwiftUI
import SwiftData

public struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    // AI & Gemini Settings
    @AppStorage("gemini_api_key") private var geminiApiKey = GeminiRecipeService.defaultApiKey
    @State private var isTestingApiKey = false
    @State private var apiTestResult: (success: Bool, message: String)? = nil
    @State private var showApiKey = false

    // Image Generation Engine (ComfyUI Local Wi-Fi vs Gemini Cloud)
    @AppStorage("image_gen_engine") private var imageGenEngineRaw = ImageGenEngine.comfyUi.rawValue
    @AppStorage("comfyui_url") private var comfyUiUrl = "http://192.168.1.54:8188"
    @AppStorage("comfyui_checkpoint") private var comfyUiCheckpoint = "v1-5-pruned-emaonly.safetensors"
    @State private var isTestingComfy = false
    @State private var comfyTestStatus: (success: Bool, message: String)? = nil
    @State private var showComfyGuide = false

    // Cloud Hub & Multi-Device Sync
    @AppStorage("sync_enabled") private var isCloudSyncEnabled = false
    @AppStorage("server_url") private var syncServerUrl = "https://api.cosmowhisper.com/cookbook"
    @AppStorage("sync_token") private var syncSecretToken = "heirloom-cookbook-sync-key-2026"
    @State private var isTestingSync = false
    @State private var syncTestResult: (success: Bool, message: String)? = nil
    @State private var isSyncing = false
    @State private var syncStatusMessage: String? = nil
    @State private var showSecretToken = false

    // App Preferences
    @AppStorage("default_unit_system") private var defaultUnitSystemRaw = UnitSystem.ukImperial.rawValue
    @AppStorage("language_mode") private var languageModeRaw = LanguageMode.both.rawValue
    @State private var showingResetAlert = false

    public init() {}

    public var body: some View {
        NavigationStack {
            Form {
                // MARK: - SECTION 1: ☁️ CLOUD HUB & MULTI-DEVICE SYNC
                Section {
                    Toggle(isOn: $isCloudSyncEnabled) {
                        VStack(alignment: .leading, spacing: 2) {
                            HStack(spacing: 6) {
                                Image(systemName: "arrow.triangle.2.circlepath.circle.fill")
                                    .foregroundStyle(isCloudSyncEnabled ? Color(red: 0x4A / 255.0, green: 0x7C / 255.0, blue: 0x59 / 255.0) : .secondary)
                                Text("Cloud Hub & Multi-Device Sync")
                                    .font(.system(size: 15, weight: .bold))
                            }
                            Text(isCloudSyncEnabled ? "Active • Connected to Private VPS" : "Disabled • 100% Offline Local Mode")
                                .font(.caption2)
                                .foregroundStyle(isCloudSyncEnabled ? Color(red: 0x4A / 255.0, green: 0x7C / 255.0, blue: 0x59 / 255.0) : .secondary)
                        }
                    }
                    .tint(Color(red: 0x4A / 255.0, green: 0x7C / 255.0, blue: 0x59 / 255.0))

                    if isCloudSyncEnabled {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Server URL (VPS / Domain)")
                                .font(.caption.bold())
                                .foregroundStyle(.secondary)
                            TextField("https://api.cosmowhisper.com/cookbook", text: $syncServerUrl)
                                .font(.system(size: 13, design: .monospaced))
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Sync Security Token")
                                .font(.caption.bold())
                                .foregroundStyle(.secondary)
                            HStack {
                                if showSecretToken {
                                    TextField("Secret VPS API key", text: $syncSecretToken)
                                        .font(.system(size: 13, design: .monospaced))
                                } else {
                                    SecureField("Secret VPS API key", text: $syncSecretToken)
                                        .font(.system(size: 13, design: .monospaced))
                                }
                                Button {
                                    showSecretToken.toggle()
                                } label: {
                                    Image(systemName: showSecretToken ? "eye.slash" : "eye")
                                        .foregroundStyle(.secondary)
                                }
                                .buttonStyle(.borderless)
                            }
                        }

                        HStack(spacing: 12) {
                            Button {
                                Task {
                                    isTestingSync = true
                                    let res = await CloudSyncManager.shared.testConnection(serverUrl: syncServerUrl, token: syncSecretToken)
                                    syncTestResult = res
                                    isTestingSync = false
                                }
                            } label: {
                                HStack(spacing: 4) {
                                    if isTestingSync {
                                        ProgressView().controlSize(.small)
                                    } else {
                                        Image(systemName: "network")
                                    }
                                    Text("Test Connection")
                                        .font(.system(size: 12, weight: .semibold))
                                }
                            }
                            .buttonStyle(.bordered)
                            .disabled(isTestingSync || syncServerUrl.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)

                            Spacer()

                            Button {
                                Task {
                                    isSyncing = true
                                    do {
                                        let msg = try await CloudSyncManager.shared.syncNow(serverUrl: syncServerUrl, token: syncSecretToken, modelContext: modelContext)
                                        syncStatusMessage = msg
                                    } catch {
                                        syncStatusMessage = "Sync failed: \(error.localizedDescription)"
                                    }
                                    isSyncing = false
                                }
                            } label: {
                                HStack(spacing: 4) {
                                    if isSyncing {
                                        ProgressView().controlSize(.small)
                                    } else {
                                        Image(systemName: "arrow.triangle.2.circlepath")
                                    }
                                    Text("Sync Now")
                                        .font(.system(size: 12, weight: .bold))
                                }
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(Color(red: 0x4A / 255.0, green: 0x7C / 255.0, blue: 0x59 / 255.0))
                            .disabled(isSyncing || syncServerUrl.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                        }

                        if let res = syncTestResult {
                            HStack(spacing: 6) {
                                Image(systemName: res.success ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                                    .foregroundStyle(res.success ? .green : .red)
                                Text(res.message)
                                    .font(.caption2)
                                    .foregroundStyle(res.success ? Color.secondary : Color.red)
                            }
                        }

                        if let status = syncStatusMessage {
                            Text(status)
                                .font(.caption2.bold())
                                .foregroundStyle(Color(red: 0x4A / 255.0, green: 0x7C / 255.0, blue: 0x59 / 255.0))
                        }
                    }
                } header: {
                    Text("CLOUD HUB & FAMILY SYNC")
                } footer: {
                    Text("Syncs recipes, cover photos, and profiles seamlessly between your phone and tablet.")
                }

                // MARK: - SECTION 2: 🎨 RECIPE COVER PHOTO GENERATOR (LOCAL WI-FI PC / CLOUD)
                Section {
                    Picker("Photo Generator Engine", selection: $imageGenEngineRaw) {
                        ForEach(ImageGenEngine.allCases) { engine in
                            Text(engine.label).tag(engine.rawValue)
                        }
                    }
                    .pickerStyle(.segmented)

                    if imageGenEngineRaw == ImageGenEngine.comfyUi.rawValue {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("ComfyUI Server Endpoint (Wi-Fi PC / LAN)")
                                .font(.caption.bold())
                                .foregroundStyle(.secondary)
                            TextField("e.g. http://192.168.1.54:8188", text: $comfyUiUrl)
                                .font(.system(size: 13, design: .monospaced))
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)

                            // Quick Helper Chips
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 6) {
                                    ForEach([
                                        ("192.168.1.54:8188", "Wi-Fi LAN"),
                                        ("192.168.1.100:8188", "Alternative LAN"),
                                        ("localhost:8188", "USB Tunnel")
                                    ], id: \.0) { item in
                                        Button {
                                            comfyUiUrl = "http://\(item.0)"
                                        } label: {
                                            Text("\(item.1) (\(item.0))")
                                                .font(.caption2)
                                                .padding(.horizontal, 6)
                                                .padding(.vertical, 3)
                                                .background(Color(uiColor: .systemGray6))
                                                .cornerRadius(4)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                            }
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Checkpoint Model (.safetensors)")
                                .font(.caption.bold())
                                .foregroundStyle(.secondary)
                            TextField("e.g. v1-5-pruned-emaonly.safetensors", text: $comfyUiCheckpoint)
                                .font(.system(size: 13, design: .monospaced))
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                        }

                        HStack {
                            Button {
                                Task {
                                    isTestingComfy = true
                                    let res = await ComfyUiClient.shared.testConnection(baseUrl: comfyUiUrl)
                                    comfyTestStatus = res
                                    isTestingComfy = false
                                }
                            } label: {
                                HStack(spacing: 4) {
                                    if isTestingComfy {
                                        ProgressView().controlSize(.small)
                                    } else {
                                        Image(systemName: "desktopcomputer")
                                    }
                                    Text("Test Wi-Fi PC Connection")
                                        .font(.system(size: 12, weight: .semibold))
                                }
                            }
                            .buttonStyle(.bordered)
                            .disabled(isTestingComfy || comfyUiUrl.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)

                            Spacer()

                            Button(showComfyGuide ? "Hide Guide" : "4-Step Setup Guide") {
                                showComfyGuide.toggle()
                            }
                            .font(.caption.bold())
                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                        }

                        if let res = comfyTestStatus {
                            HStack(spacing: 6) {
                                Image(systemName: res.success ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                                    .foregroundStyle(res.success ? .green : .red)
                                Text(res.message)
                                    .font(.caption2)
                                    .foregroundStyle(res.success ? Color.secondary : Color.red)
                            }
                        }

                        if showComfyGuide {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("HOW TO CONNECT TO YOUR PERSONAL PC:")
                                    .font(.caption.bold())
                                    .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                                Text("1. Launch ComfyUI with `--listen 0.0.0.0` so other devices on your home Wi-Fi can reach it.")
                                Text("2. Find your PC's Wi-Fi IP address (e.g. 192.168.1.54) in Windows Settings / Mac System Settings.")
                                Text("3. Enter `http://<your-pc-ip>:8188` in the field above.")
                                Text("4. Tap 'Test Wi-Fi PC Connection'. You can now generate food cover photos directly using your PC GPU!")
                            }
                            .font(.caption2)
                            .padding(8)
                            .background(Color(red: 0xFE / 255.0, green: 0xF3 / 255.0, blue: 0xC7 / 255.0))
                            .cornerRadius(6)
                        }
                    }
                } header: {
                    Text("RECIPE COVER IMAGE GENERATION ENGINE")
                } footer: {
                    Text("Choose whether recipe cover photos are generated via Google Cloud AI or rendered directly on your personal graphics card PC over local Wi-Fi.")
                }

                // MARK: - SECTION 3: ✨ GEMINI VISION AI SCANNER & SOUS CHEF
                Section {
                    // Friendly Setup Guidance Card
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 6) {
                            Image(systemName: "key.fill")
                                .foregroundStyle(Color(red: 0xD9 / 255.0, green: 0x77 / 255.0, blue: 0x06 / 255.0))
                            Text("FREE GOOGLE AI KEY REQUIRED")
                                .font(.system(size: 11, weight: .black, design: .serif))
                                .tracking(1)
                                .foregroundStyle(Color(red: 0x92 / 255.0, green: 0x40 / 255.0, blue: 0x0E / 255.0))
                        }

                        Text("Google Cloud disabled the old test project key (403 error). To enable AI card scanning, Sous Chef chat, and Cloud food art, create your free personal key at Google AI Studio:")
                            .font(.system(size: 11.5))
                            .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                            .fixedSize(horizontal: false, vertical: true)

                        HStack(spacing: 8) {
                            Link(destination: URL(string: "https://aistudio.google.com/app/apikey")!) {
                                HStack(spacing: 4) {
                                    Image(systemName: "safari")
                                    Text("Open Google AI Studio")
                                        .font(.system(size: 12, weight: .bold))
                                }
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color(red: 0x92 / 255.0, green: 0x40 / 255.0, blue: 0x0E / 255.0), in: Capsule())
                                .foregroundStyle(.white)
                            }

                            Button {
                                if let clip = UIPasteboard.general.string?.trimmingCharacters(in: .whitespacesAndNewlines), !clip.isEmpty {
                                    geminiApiKey = clip
                                    Task {
                                        isTestingApiKey = true
                                        let res = await GeminiRecipeService.shared.testApiKey(geminiApiKey)
                                        apiTestResult = (res.0, res.1)
                                        isTestingApiKey = false
                                    }
                                }
                            } label: {
                                HStack(spacing: 4) {
                                    Image(systemName: "doc.on.clipboard")
                                    Text("Paste & Test")
                                        .font(.system(size: 12, weight: .bold))
                                }
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color(red: 0xFD / 255.0, green: 0xE6 / 255.0, blue: 0x8A / 255.0), in: Capsule())
                                .foregroundStyle(Color(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0))
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.top, 2)
                    }
                    .padding(8)
                    .background(Color(red: 0xFF / 255.0, green: 0xFB / 255.0, blue: 0xEB / 255.0), in: RoundedRectangle(cornerRadius: 10))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(red: 0xFD / 255.0, green: 0xE6 / 255.0, blue: 0x8A / 255.0), lineWidth: 1))

                    HStack {
                        if showApiKey {
                            TextField("AI Studio Gemini API Key", text: $geminiApiKey)
                                .font(.system(size: 13, design: .monospaced))
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                        } else {
                            SecureField("AI Studio Gemini API Key", text: $geminiApiKey)
                                .font(.system(size: 13, design: .monospaced))
                        }

                        Button {
                            showApiKey.toggle()
                        } label: {
                            Image(systemName: showApiKey ? "eye.slash" : "eye")
                                .foregroundStyle(.secondary)
                        }
                        .buttonStyle(.plain)

                        Button {
                            if let clip = UIPasteboard.general.string?.trimmingCharacters(in: .whitespacesAndNewlines), !clip.isEmpty {
                                geminiApiKey = clip
                            }
                        } label: {
                            Text("Paste")
                                .font(.system(size: 12, weight: .bold))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color(red: 0xFE / 255.0, green: 0xF3 / 255.0, blue: 0xC7 / 255.0), in: Capsule())
                                .foregroundStyle(Color(red: 0x92 / 255.0, green: 0x40 / 255.0, blue: 0x0E / 255.0))
                        }
                        .buttonStyle(.plain)
                    }

                    HStack {
                        Button {
                            Task {
                                isTestingApiKey = true
                                GeminiRecipeService.shared.apiKey = geminiApiKey
                                let res = await GeminiRecipeService.shared.testApiKey(geminiApiKey)
                                apiTestResult = (res.0, res.1)
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
                            .font(.caption2.bold())
                            .foregroundStyle(res.success ? Color(red: 0x4A / 255.0, green: 0x7C / 255.0, blue: 0x59 / 255.0) : Color.red)
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
                    Text("Primary: Gemini 2.5 Flash • Fallback: Gemini 3.5 Flash. Used for multi-page scanning, handwriting OCR, and the culinary Sous Chef.")
                }

                // MARK: - SECTION 4: MEASUREMENTS & LOCALIZATION
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

                // MARK: - SECTION 5: DATA MANAGEMENT
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
                    Text("Restores all 128 heirloom family recipes (including Annette's 122 recipes).")
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
                    Text("ABOUT COOKBOOK")
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
                if geminiApiKey.isEmpty {
                    geminiApiKey = GeminiRecipeService.defaultApiKey
                }
                if syncSecretToken.isEmpty {
                    syncSecretToken = "heirloom-cookbook-sync-key-2026"
                }
            }
        }
    }

    private func restoreDefaultRecipes() {
        let defaults = DefaultRecipes.initialRecipes()
        let fetchDescriptor = FetchDescriptor<Recipe>()
        let existing = (try? modelContext.fetch(fetchDescriptor)) ?? []
        let existingIds = Set(existing.map { $0.id })
        let existingTitles = Set(existing.map { $0.title.lowercased().trimmingCharacters(in: .whitespacesAndNewlines) })
        for r in defaults {
            let titleKey = r.title.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
            if !existingIds.contains(r.id) && !existingTitles.contains(titleKey) {
                modelContext.insert(r)
            }
        }
        try? modelContext.save()
    }
}
