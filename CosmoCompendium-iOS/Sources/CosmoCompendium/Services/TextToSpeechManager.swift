import Foundation
import AVFoundation

// MARK: - Text to Speech Manager for Step-by-Step Read Aloud
public final class TextToSpeechManager: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    public static let shared = TextToSpeechManager()

    private let synthesizer = AVSpeechSynthesizer()
    @Published public var isSpeaking: Bool = false
    @Published public var currentlySpeakingText: String? = nil

    private override init() {
        super.init()
        synthesizer.delegate = self
    }

    public func speak(text: String, isGerman: Bool = false) {
        let cleanText = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !cleanText.isEmpty else { return }

        if isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
            if currentlySpeakingText == cleanText {
                isSpeaking = false
                currentlySpeakingText = nil
                return
            }
        }

        let utterance = AVSpeechUtterance(string: cleanText)
        let localeCode = isGerman ? "de-DE" : "en-US"
        utterance.voice = AVSpeechSynthesisVoice(language: localeCode) ?? AVSpeechSynthesisVoice(language: "en-US")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * 0.92
        utterance.pitchMultiplier = 1.0

        currentlySpeakingText = cleanText
        isSpeaking = true

        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
        try? AVAudioSession.sharedInstance().setActive(true)

        synthesizer.speak(utterance)
    }

    public func stop() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
        currentlySpeakingText = nil
    }

    public func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        DispatchQueue.main.async {
            self.isSpeaking = false
            self.currentlySpeakingText = nil
        }
    }

    public func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        DispatchQueue.main.async {
            self.isSpeaking = false
            self.currentlySpeakingText = nil
        }
    }
}
