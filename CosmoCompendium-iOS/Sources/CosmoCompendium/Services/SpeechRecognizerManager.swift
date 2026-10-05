import Foundation
import Speech
import AVFoundation

@MainActor
public final class SpeechRecognizerManager: ObservableObject {
    public static let shared = SpeechRecognizerManager()

    @Published public var isRecording: Bool = false
    @Published public var transcribedText: String = ""
    @Published public var errorMessage: String? = nil

    private var speechRecognizer: SFSpeechRecognizer?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()

    public init() {
        speechRecognizer = SFSpeechRecognizer(locale: Locale.current) ?? SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
    }

    public func requestPermissions() async -> Bool {
        // Speech authorization
        let speechStatus = SFSpeechRecognizer.authorizationStatus()
        let speechGranted: Bool
        switch speechStatus {
        case .authorized:
            speechGranted = true
        case .notDetermined:
            speechGranted = await withCheckedContinuation { continuation in
                SFSpeechRecognizer.requestAuthorization { status in
                    continuation.resume(returning: status == .authorized)
                }
            }
        default:
            speechGranted = false
        }

        guard speechGranted else {
            errorMessage = "Speech recognition permission is required for voice input."
            return false
        }

        // Microphone authorization
        let audioGranted: Bool
        if #available(iOS 17.0, *) {
            audioGranted = await AVAudioApplication.requestRecordPermission()
        } else {
            let audioSession = AVAudioSession.sharedInstance()
            switch audioSession.recordPermission {
            case .granted:
                audioGranted = true
            case .undetermined:
                audioGranted = await withCheckedContinuation { continuation in
                    audioSession.requestRecordPermission { granted in
                        continuation.resume(returning: granted)
                    }
                }
            default:
                audioGranted = false
            }
        }

        guard audioGranted else {
            errorMessage = "Microphone access is required to speak your questions."
            return false
        }

        return true
    }

    public func startRecording(onTranscription: @escaping (String) -> Void) async {
        if isRecording {
            stopRecording()
            return
        }

        let hasPermissions = await requestPermissions()
        guard hasPermissions else { return }

        // Cancel previous task if still running
        recognitionTask?.cancel()
        recognitionTask = nil

        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.playAndRecord, mode: .measurement, options: [.duckOthers, .defaultToSpeaker])
            try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            errorMessage = "Audio session setup failed: \(error.localizedDescription)"
            return
        }

        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        guard let recognitionRequest = recognitionRequest else {
            errorMessage = "Could not initialize speech recognition request."
            return
        }
        recognitionRequest.shouldReportPartialResults = true

        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)

        inputNode.removeTap(onBus: 0)
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { [weak self] buffer, _ in
            self?.recognitionRequest?.append(buffer)
        }

        audioEngine.prepare()
        do {
            try audioEngine.start()
            isRecording = true
            transcribedText = ""
            errorMessage = nil
        } catch {
            errorMessage = "Microphone engine failed to start: \(error.localizedDescription)"
            stopRecording()
            return
        }

        guard let recognizer = speechRecognizer, recognizer.isAvailable else {
            errorMessage = "Speech recognizer is temporarily unavailable."
            stopRecording()
            return
        }

        recognitionTask = recognizer.recognitionTask(with: recognitionRequest) { [weak self] result, error in
            guard let self = self else { return }

            if let result = result {
                let spoken = result.bestTranscription.formattedString
                Task { @MainActor in
                    self.transcribedText = spoken
                    onTranscription(spoken)
                }
            }

            if error != nil || (result?.isFinal == true) {
                Task { @MainActor in
                    self.stopRecording()
                }
            }
        }
    }

    public func stopRecording() {
        if audioEngine.isRunning {
            audioEngine.stop()
            audioEngine.inputNode.removeTap(onBus: 0)
        }
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        recognitionTask = nil
        recognitionRequest = nil

        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
        isRecording = false
    }
}
