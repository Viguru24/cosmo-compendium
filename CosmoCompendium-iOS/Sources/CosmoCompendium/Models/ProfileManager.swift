import SwiftUI
import Foundation

public final class ProfileManager: ObservableObject {
    public static let shared = ProfileManager()

    @AppStorage("active_profile_name") public var activeProfile: String = "Louis"
    @AppStorage("default_profile_name") public var defaultProfile: String = "Louis"

    @Published public var profiles: [String] = []

    private let profilesStorageKey = "saved_family_profiles"

    private init() {
        loadProfiles()
    }

    public func loadProfiles() {
        if let data = UserDefaults.standard.array(forKey: profilesStorageKey) as? [String], !data.isEmpty {
            profiles = data
        } else {
            profiles = ["Louis", "Wife"]
            saveProfiles()
        }
    }

    public func saveProfiles() {
        UserDefaults.standard.set(profiles, forKey: profilesStorageKey)
    }

    public func addProfile(_ name: String) {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, !profiles.contains(where: { $0.caseInsensitiveCompare(trimmed) == .orderedSame }) else { return }
        profiles.append(trimmed)
        saveProfiles()
        activeProfile = trimmed
    }

    public func renameProfile(oldName: String, newName: String) {
        let trimmed = newName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        if let idx = profiles.firstIndex(where: { $0.caseInsensitiveCompare(oldName) == .orderedSame }) {
            profiles[idx] = trimmed
            saveProfiles()
            if activeProfile.caseInsensitiveCompare(oldName) == .orderedSame {
                activeProfile = trimmed
            }
            if defaultProfile.caseInsensitiveCompare(oldName) == .orderedSame {
                defaultProfile = trimmed
            }
        }
    }

    public func deleteProfile(_ name: String) {
        profiles.removeAll { $0.caseInsensitiveCompare(name) == .orderedSame }
        if profiles.isEmpty {
            profiles = ["Louis"]
        }
        saveProfiles()
        if activeProfile.caseInsensitiveCompare(name) == .orderedSame {
            activeProfile = profiles.first ?? "Louis"
        }
    }

    /**
     Exact 80+ avatar emoji mapping matching Android ProfileComponents.kt
     */
    public static func getProfileEmoji(name: String) -> String {
        let lower = name.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
        let femaleNames: Set<String> = [
            "wife", "mom", "mother", "sarah", "emma", "annette", "isabel", "isabella",
            "anna", "anne", "marie", "maria", "clara", "claire", "julia", "julie",
            "sophie", "sophia", "laura", "linda", "lisa", "kate", "katie", "katherine",
            "mary", "margaret", "carol", "carolyn", "barbara", "betty", "patricia",
            "rose", "ruth", "helen", "grace", "alice", "amy", "jane", "jennifer",
            "jessica", "michelle", "nicole", "rachel", "rebecca", "susan", "karen",
            "elizabeth", "charlotte", "victoria", "hannah", "emily", "natalie", "lucy"
        ]

        let maleNames: Set<String> = [
            "dad", "father", "louis", "husband", "james", "john", "robert", "michael",
            "william", "david", "richard", "joseph", "thomas", "charles", "mark",
            "daniel", "paul", "steven", "andrew", "george", "edward", "kevin", "brian",
            "peter", "frank", "henry", "jack", "sam", "samuel", "ben", "benjamin",
            "chris", "christopher", "alex", "alexander", "ryan", "matt", "matthew"
        ]

        if lower.contains("all") || lower.contains("family") { return "👨‍👩‍👧" }
        if lower.contains("grandma") || lower.contains("oma") || lower.contains("nana") || lower.contains("omi") { return "👵" }
        if lower.contains("grandpa") || lower.contains("opa") || lower.contains("opi") { return "👴" }
        if lower.contains("daughter") || lower.contains("girl") || lower.contains("sister") || lower.contains("lily") || lower.contains("mia") { return "👧" }
        if lower.contains("son") || lower.contains("boy") || lower.contains("brother") { return "👦" }
        if lower.contains("bake") || lower.contains("cake") || lower.contains("pastry") { return "🧁" }

        for fn in femaleNames where lower.contains(fn) { return "👩‍🍳" }
        for mn in maleNames where lower.contains(mn) { return "👨‍🍳" }

        // Deterministic warm fallback
        let hash = abs(lower.hashValue)
        return (hash % 2 == 0) ? "👩‍🍳" : "👨‍🍳"
    }
}
