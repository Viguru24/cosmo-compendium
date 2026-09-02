import Foundation

public struct RecipeStep: Identifiable, Codable, Hashable {
    public var id: String = UUID().uuidString
    public var stepNumber: Int
    public var instructionEnglish: String
    public var instructionGerman: String
    public var timerMinutes: Int
    public var tip: String?

    public init(
        id: String = UUID().uuidString,
        stepNumber: Int,
        instructionEnglish: String,
        instructionGerman: String = "",
        timerMinutes: Int = 0,
        tip: String? = nil
    ) {
        self.id = id
        self.stepNumber = stepNumber
        self.instructionEnglish = instructionEnglish
        self.instructionGerman = instructionGerman
        self.timerMinutes = timerMinutes
        self.tip = tip
    }

    public func instruction(language: LanguageMode = .english, unitSystem: UnitSystem? = nil) -> String {
        let base: String
        switch language {
        case .german:
            base = !instructionGerman.isEmpty ? instructionGerman : instructionEnglish
        case .english, .both:
            base = !instructionEnglish.isEmpty ? instructionEnglish : instructionGerman
        }
        if let unitSystem = unitSystem {
            return CulinaryTemperatureConverter.formatTemperatures(base, unitSystem: unitSystem)
        }
        return base
    }

    public func localizedTip(language: LanguageMode = .english) -> String? {
        guard let t = tip, !t.isEmpty else { return nil }
        if t.contains("||") {
            let parts = t.components(separatedBy: "||").map { $0.trimmingCharacters(in: .whitespaces) }
            return parts.first ?? t
        }
        return t
    }
}

public enum CulinaryTemperatureConverter {
    public static func formatTemperatures(_ text: String, unitSystem: UnitSystem) -> String {
        let regexF = try! NSRegularExpression(pattern: "(?i)\\b(\\d{3})\\s*(?:°\\s*F|degrees?\\s*F(?:ahrenheit)?|F\\b)")
        let nsString = text as NSString
        var result = text

        if unitSystem == .ukImperial || unitSystem == .metricGrams {
            let matches = regexF.matches(in: text, range: NSRange(location: 0, length: nsString.length)).reversed()
            for match in matches {
                if let range = Range(match.range(at: 1), in: text),
                   let fVal = Int(text[range]),
                   fVal >= 200 && fVal <= 550 {
                    let cVal = Int(round(Double(fVal - 32) * 5.0 / 9.0))
                    let cRounded = Int(round(Double(cVal) / 5.0) * 5.0)
                    let gasMark = gasMarkString(forCelsius: cRounded)
                    let replacement = unitSystem == .ukImperial ? "\(cRounded)°C / \(gasMark) (\(fVal)°F)" : "\(cRounded)°C (\(fVal)°F)"
                    if let fullRange = Range(match.range, in: result) {
                        result.replaceSubrange(fullRange, with: replacement)
                    }
                }
            }
        }
        return result
    }

    private static func gasMarkString(forCelsius c: Int) -> String {
        switch c {
        case ...135: return "Gas Mark 1"
        case 136...150: return "Gas Mark 2"
        case 151...165: return "Gas Mark 3"
        case 166...180: return "Gas Mark 4"
        case 181...190: return "Gas Mark 5"
        case 191...205: return "Gas Mark 6"
        case 206...220: return "Gas Mark 7"
        case 221...230: return "Gas Mark 8"
        default: return "Gas Mark 9"
        }
    }
}
