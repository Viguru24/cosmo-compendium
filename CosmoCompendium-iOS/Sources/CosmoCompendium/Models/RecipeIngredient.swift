import Foundation

public struct RecipeIngredient: Identifiable, Codable, Hashable {
    public var id: String = UUID().uuidString
    public var name: String
    public var amount: String
    public var unit: String
    public var nameGerman: String?
    public var nameEnglish: String?
    public var isOptional: Bool
    public var group: String?

    public init(
        id: String = UUID().uuidString,
        name: String,
        amount: String = "",
        unit: String = "",
        nameGerman: String? = nil,
        nameEnglish: String? = nil,
        isOptional: Bool = false,
        group: String? = nil
    ) {
        self.id = id
        self.name = name
        self.amount = amount
        self.unit = unit
        self.nameGerman = nameGerman
        self.nameEnglish = nameEnglish
        self.isOptional = isOptional
        self.group = group
    }

    public func displayName(language: LanguageMode = .english) -> String {
        let raw: String
        switch language {
        case .german:
            raw = nameGerman?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty == false ? nameGerman! : name
        case .english, .both:
            let en = nameEnglish?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty == false ? nameEnglish! : name
            raw = en.contains("/") ? (en.components(separatedBy: "/").first?.trimmingCharacters(in: .whitespaces) ?? en) : en
        }
        return Self.cleanIngredientName(raw)
    }

    public static func cleanIngredientName(_ raw: String) -> String {
        var str = raw
        str = str.replacingOccurrences(of: "(?i)\\btipo,\\s*0,\\s*0\\b", with: "Tipo 00", options: .regularExpression)
        str = str.replacingOccurrences(of: "(?i)\\btipo\\s+0\\s+0\\b", with: "Tipo 00", options: .regularExpression)
        str = str.replacingOccurrences(of: "(?i)\\btype,\\s*0,\\s*0\\b", with: "Type 00", options: .regularExpression)
        str = str.replacingOccurrences(of: "(?i)\\bflower\\b", with: "flour", options: .regularExpression)
        str = str.replacingOccurrences(of: "(?i)\\btipo\\s*00\\s*flour\\b", with: "Tipo 00 Flour", options: .regularExpression)
        str = str.replacingOccurrences(of: "\\s+", with: " ", options: .regularExpression)
        return str.trimmingCharacters(in: .whitespaces)
    }

    public func localizedGroup(language: LanguageMode = .english) -> String? {
        guard let g = group, !g.isEmpty else { return nil }
        if g.contains("/") {
            return g.components(separatedBy: "/").first?.trimmingCharacters(in: .whitespaces) ?? g
        }
        return g
    }

    /**
     Converts the ingredient amount and unit according to the selected measuring style:
     - METRIC_GRAMS: g, kg, ml, l
     - CUPS_US: cups, tbsp, tsp, oz, lbs, fl oz
     - UK_IMPERIAL: Funneled UK Blueprint (UK spoons for <15g/<15ml, Grams for dry/solids, ml for liquids)
     - BAKERS_PRECISION: exact decimal grams (e.g. 250.0 g)
     */
    public func convertedAmount(targetSystem: UnitSystem, multiplier: Double = 1.0) -> String {
        guard !amount.trimmingCharacters(in: .whitespaces).isEmpty else { return "" }
        guard let numericAmount = parseAmountToDouble(amount) else {
            return multiplier != 1.0 ? "\(amount) (x\(multiplier))" : amount
        }
        let scaled = numericAmount * multiplier
        let u = unit.lowercased().trimmingCharacters(in: .whitespaces)
        let itemName = (nameEnglish ?? name).lowercased()

        // Ingredient density (grams per 1 cup / ~240ml)
        let density: Double
        if itemName.contains("powdered sugar") || itemName.contains("puderzucker") || itemName.contains("icing sugar") {
            density = 120.0
        } else if itemName.contains("flour") || itemName.contains("mehl") || itemName.contains("stärke") || itemName.contains("starch") || itemName.contains("cornstarch") || itemName.contains("cornflour") {
            density = 125.0
        } else if itemName.contains("brown sugar") || itemName.contains("brauner zucker") || itemName.contains("muscovado") {
            density = 220.0
        } else if itemName.contains("sugar") || itemName.contains("zucker") || itemName.contains("caster") || itemName.contains("castor") {
            density = 200.0
        } else if itemName.contains("butter") || itemName.contains("margarine") {
            density = 227.0
        } else if itemName.contains("cocoa") || itemName.contains("kakao") {
            density = 100.0
        } else if itemName.contains("honey") || itemName.contains("honig") || itemName.contains("syrup") || itemName.contains("sirup") || itemName.contains("treacle") || itemName.contains("molasses") {
            density = 340.0
        } else if itemName.contains("bread crumb") || itemName.contains("semmelbrösel") || itemName.contains("breadcrumbs") || itemName.contains("paniermehl") {
            density = 110.0
        } else if itemName.contains("oat") || itemName.contains("haferflocken") || itemName.contains("porridge") {
            density = 90.0
        } else if itemName.contains("nut") || itemName.contains("mandel") || itemName.contains("nuss") || itemName.contains("almond") || itemName.contains("hazelnut") || itemName.contains("walnut") {
            density = 100.0
        } else if itemName.contains("chocolate chip") || itemName.contains("schokotropfen") {
            density = 170.0
        } else if itemName.contains("raisin") || itemName.contains("rosinen") || itemName.contains("sultana") || itemName.contains("craisin") {
            density = 150.0
        } else {
            density = 240.0 // standard liquid
        }

        switch targetSystem {
        case .ukImperial:
            return convertToUkFormat(scaled: scaled, u: u, itemName: itemName, density: density)

        case .metricGrams:
            switch u {
            case "cup", "cups", "tasse", "tassen":
                if isLiquid(itemName) {
                    return "\(formatScaledNumber(scaled * 240.0)) ml"
                } else {
                    return "\(formatScaledNumber(scaled * density)) g"
                }
            case "stick", "sticks":
                let grams = scaled * 113.4
                return "\(Int(roundTo5(grams))) g"
            case "tbsp", "tablespoon", "tablespoons", "el", "esslöffel":
                return isLiquid(itemName) ? "\(formatScaledNumber(scaled * 15.0)) ml" : "\(formatScaledNumber(scaled * (density / 16.0))) g"
            case "tsp", "teaspoon", "teaspoons", "tl", "teelöffel":
                return isLiquid(itemName) ? "\(formatScaledNumber(scaled * 5.0)) ml" : "\(formatScaledNumber(scaled * (density / 48.0))) g"
            case "oz", "ounce", "ounces":
                return "\(formatScaledNumber(scaled * 28.3495)) g"
            case "fl oz", "fluid ounce":
                return "\(formatScaledNumber(scaled * 29.57)) ml"
            case "lb", "lbs", "pound", "pounds":
                let grams = scaled * 453.592
                return grams >= 1000 ? "\(formatScaledNumber(grams / 1000.0)) kg" : "\(formatScaledNumber(grams)) g"
            case "g", "gram", "grams", "gramm":
                return scaled >= 1000 ? "\(formatScaledNumber(scaled / 1000.0)) kg" : "\(formatScaledNumber(scaled)) g"
            case "kg":
                return "\(formatScaledNumber(scaled)) kg"
            case "ml":
                return "\(formatScaledNumber(scaled)) ml"
            case "l", "liter", "litre":
                return "\(formatScaledNumber(scaled)) l"
            case "pinch", "prise", "msp.", "messerspitze":
                return scaled > 1.5 ? "2 pinches" : "1 pinch"
            case "pck.", "päckchen", "packet", "packets":
                return "\(formatScaledNumber(scaled)) pkt."
            default:
                return "\(formatScaledNumber(scaled)) \(unit)".trimmingCharacters(in: .whitespaces)
            }

        case .cupsUS:
            switch u {
            case "g", "gram", "grams", "gramm":
                if isLiquid(itemName) {
                    return formatCupsFromMl(scaled)
                } else {
                    let cups = scaled / density
                    if cups >= 0.2 {
                        return "\(formatFraction(cups)) cups"
                    } else {
                        let tbsp = scaled / (density / 16.0)
                        if tbsp >= 0.9 {
                            return "\(formatFraction(tbsp)) tbsp"
                        } else {
                            let tsp = scaled / (density / 48.0)
                            return tsp <= 0.35 && scaled <= 1.0 ? "1 pinch" : "\(formatFraction(tsp)) tsp"
                        }
                    }
                }
            case "kg":
                return "\(formatScaledNumber(scaled * 2.20462)) lbs"
            case "ml":
                return formatCupsFromMl(scaled)
            case "l", "liter", "litre":
                return "\(formatScaledNumber(scaled * 4.22675)) cups"
            case "el", "esslöffel":
                return "\(formatScaledNumber(scaled)) tbsp"
            case "tl", "teelöffel":
                return "\(formatScaledNumber(scaled)) tsp"
            case "tasse", "tassen", "cup", "cups":
                return "\(formatFraction(scaled)) cups"
            case "stick", "sticks":
                return "\(formatFraction(scaled)) stick\(scaled > 1 ? "s" : "")"
            case "tbsp", "tablespoon", "tablespoons":
                return "\(formatFraction(scaled)) tbsp"
            case "tsp", "teaspoon", "teaspoons":
                return "\(formatFraction(scaled)) tsp"
            case "oz", "ounce", "ounces":
                return "\(formatScaledNumber(scaled)) oz"
            case "fl oz":
                return "\(formatScaledNumber(scaled)) fl oz"
            case "lb", "lbs":
                return "\(formatScaledNumber(scaled)) lbs"
            case "pinch", "prise", "msp.":
                return scaled > 1.5 ? "2 pinches" : "1 pinch"
            case "pck.", "päckchen", "packet":
                return "\(formatScaledNumber(scaled)) packet"
            default:
                return "\(formatScaledNumber(scaled)) \(unit)".trimmingCharacters(in: .whitespaces)
            }

        case .bakersPrecision:
            let grams: Double
            switch u {
            case "cup", "cups", "tasse":
                grams = isLiquid(itemName) ? scaled * 240.0 : scaled * density
            case "stick", "sticks":
                grams = scaled * 113.4
            case "tbsp", "tablespoon", "el":
                grams = isLiquid(itemName) ? scaled * 15.0 : scaled * (density / 16.0)
            case "tsp", "teaspoon", "tl":
                grams = isLiquid(itemName) ? scaled * 5.0 : scaled * (density / 48.0)
            case "oz", "ounce":
                grams = scaled * 28.3495
            case "lb", "lbs":
                grams = scaled * 453.592
            case "kg":
                grams = scaled * 1000.0
            case "g", "gram", "gramm":
                grams = scaled
            case "ml":
                return "\(formatScaledNumber(scaled)) ml"
            case "l":
                return "\(formatScaledNumber(scaled * 1000.0)) ml"
            default:
                return "\(formatScaledNumber(scaled)) \(unit)".trimmingCharacters(in: .whitespaces)
            }
            return "\(formatScaledNumber(grams)) g"
        }
    }

    private func convertToUkFormat(scaled: Double, u: String, itemName: String, density: Double) -> String {
        let liquid = isLiquid(itemName)

        if u.contains("stick") {
            let butterGrams = scaled * 115.0
            return butterGrams <= 15.0 ? formatUkSpoon(butterGrams, isLiquid: false) : "\(Int(roundTo5(butterGrams))) g"
        }

        if ["tsp", "teaspoon", "teaspoons", "tl", "teelöffel"].contains(u) {
            return scaled >= 3.0 ? "\(formatFraction(scaled / 3.0)) tbsp" : "\(formatFraction(scaled)) tsp"
        }
        if ["tbsp", "tablespoon", "tablespoons", "el", "esslöffel"].contains(u) {
            if scaled <= 3.0 {
                return "\(formatFraction(scaled)) tbsp"
            } else {
                let metricEquivalent = liquid ? scaled * 15.0 : scaled * (density / 16.0)
                return liquid ? "\(Int(roundTo5(metricEquivalent))) ml" : "\(Int(roundTo5(metricEquivalent))) g"
            }
        }
        if ["pinch", "prise", "msp.", "messerspitze"].contains(u) {
            return scaled > 1.5 ? "2 pinches" : "1 pinch"
        }
        if ["pck.", "päckchen", "packet", "packets", "sachet", "sachets"].contains(u) {
            return "\(formatScaledNumber(scaled)) sachet\(scaled > 1 ? "s" : "")"
        }

        var metricMl: Double? = nil
        var metricGrams: Double? = nil

        switch u {
        case "cup", "cups", "tasse", "tassen":
            if liquid { metricMl = scaled * 250.0 } else { metricGrams = scaled * density }
        case "g", "gram", "grams", "gramm":
            metricGrams = scaled
        case "kg":
            metricGrams = scaled * 1000.0
        case "ml":
            metricMl = scaled
        case "l", "liter", "litre":
            metricMl = scaled * 1000.0
        case "oz", "ounce", "ounces":
            if liquid { metricMl = scaled * 28.413 } else { metricGrams = scaled * 28.3495 }
        case "fl oz", "fluid ounce", "fluid ounces":
            metricMl = scaled * 28.413
        case "pt", "pint", "pints":
            metricMl = scaled * 568.261
        case "lb", "lbs", "pound", "pounds":
            metricGrams = scaled * 453.592
        default:
            return "\(formatScaledNumber(scaled)) \(u)".trimmingCharacters(in: .whitespaces)
        }

        if let ml = metricMl {
            if ml <= 15.0 { return formatUkSpoon(ml, isLiquid: true) }
            if ml >= 1000.0 { return "\(formatScaledNumber(ml / 1000.0)) L" }
            return "\(Int(roundTo5(ml))) ml"
        }

        if let grams = metricGrams {
            if grams <= 15.0 { return formatUkSpoon(grams, isLiquid: false) }
            if grams >= 1000.0 { return "\(formatScaledNumber(grams / 1000.0)) kg" }
            return "\(Int(roundTo5(grams))) g"
        }

        return "\(formatScaledNumber(scaled)) \(u)".trimmingCharacters(in: .whitespaces)
    }

    private func formatUkSpoon(_ metricAmt: Double, isLiquid: Bool) -> String {
        switch metricAmt {
        case ...0.8: return "1 pinch"
        case ...1.8: return "¼ tsp"
        case ...3.2: return "½ tsp"
        case ...4.2: return "¾ tsp"
        case ...6.5: return "1 tsp"
        case ...8.5: return "1½ tsp"
        case ...12.0: return "2 tsp"
        case ...18.0: return "1 tbsp"
        case ...25.0: return "1½ tbsp"
        case ...35.0: return "2 tbsp"
        default:
            return isLiquid ? "\(Int(roundTo5(metricAmt))) ml" : "\(Int(roundTo5(metricAmt))) g"
        }
    }

    private func roundTo5(_ num: Double) -> Double {
        (round(num / 5.0) * 5.0)
    }

    private func isLiquid(_ name: String) -> Bool {
        let n = name.lowercased()
        return n.contains("water") || n.contains("wasser") ||
               n.contains("milk") || n.contains("milch") ||
               n.contains("oil") || n.contains("öl") ||
               n.contains("cream") || n.contains("sahne") ||
               n.contains("juice") || n.contains("saft") ||
               n.contains("wine") || n.contains("wein") ||
               n.contains("cider") || n.contains("beer") || n.contains("bier") ||
               n.contains("broth") || n.contains("brühe") || n.contains("stock") || n.contains("fond") ||
               n.contains("vinegar") || n.contains("essig") ||
               n.contains("rum") || n.contains("kirschwasser") || n.contains("liqueur") ||
               n.contains("coffee") || n.contains("kaffee") || n.contains("tea") || n.contains("tee")
    }

    private func formatCupsFromMl(_ ml: Double) -> String {
        let cups = ml / 240.0
        if cups >= 0.2 { return "\(formatFraction(cups)) cups" }
        if ml >= 15.0 { return "\(formatScaledNumber(ml / 15.0)) tbsp" }
        return "\(formatScaledNumber(ml / 5.0)) tsp"
    }

    private func parseAmountToDouble(_ amt: String) -> Double? {
        var clean = amt.trimmingCharacters(in: .whitespaces)
            .replacingOccurrences(of: ",", with: ".")
            .replacingOccurrences(of: "½", with: "0.5")
            .replacingOccurrences(of: "¼", with: "0.25")
            .replacingOccurrences(of: "¾", with: "0.75")
            .replacingOccurrences(of: "⅓", with: "0.33")
            .replacingOccurrences(of: "⅔", with: "0.67")

        if clean.contains("/") {
            let parts = clean.components(separatedBy: "/")
            if parts.count == 2,
               let num = Double(parts[0].trimmingCharacters(in: .whitespaces)),
               let den = Double(parts[1].trimmingCharacters(in: .whitespaces)), den != 0 {
                return num / den
            }
        }
        if clean.contains(" ") {
            let parts = clean.components(separatedBy: " ")
            if parts.count == 2,
               let whole = Double(parts[0].trimmingCharacters(in: .whitespaces)),
               parts[1].contains("/") {
                let fracParts = parts[1].components(separatedBy: "/")
                if fracParts.count == 2,
                   let num = Double(fracParts[0].trimmingCharacters(in: .whitespaces)),
                   let den = Double(fracParts[1].trimmingCharacters(in: .whitespaces)), den != 0 {
                    return whole + (num / den)
                }
            }
        }
        return Double(clean)
    }

    private func formatScaledNumber(_ num: Double) -> String {
        let rounded = round(num * 100.0) / 100.0
        if abs(rounded - round(rounded)) < 0.001 {
            return "\(Int(rounded))"
        } else if abs(rounded * 10.0 - round(rounded * 10.0)) < 0.001 {
            return String(format: "%.1f", rounded)
        } else {
            return String(format: "%.2f", rounded)
        }
    }

    private func formatFraction(_ num: Double) -> String {
        let whole = Int(num)
        let frac = num - Double(whole)
        let fracStr: String
        switch frac {
        case 0.15...0.29: fracStr = "1/4"
        case 0.30...0.40: fracStr = "1/3"
        case 0.41...0.59: fracStr = "1/2"
        case 0.60...0.70: fracStr = "2/3"
        case 0.71...0.85: fracStr = "3/4"
        default: fracStr = ""
        }
        if whole > 0 && !fracStr.isEmpty { return "\(whole) \(fracStr)" }
        if whole > 0 && fracStr.isEmpty { return formatScaledNumber(num) }
        if !fracStr.isEmpty { return fracStr }
        return formatScaledNumber(num)
    }
}
