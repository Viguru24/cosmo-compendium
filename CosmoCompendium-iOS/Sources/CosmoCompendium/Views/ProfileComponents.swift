import SwiftUI
import SwiftData

/**
 Top Bar Profile Selector Pill
 */
public struct ProfilePill: View {
    @ObservedObject var profileManager = ProfileManager.shared
    let onClick: () -> Void

    public var body: some View {
        Button(action: onClick) {
            HStack(spacing: 5) {
                Text(ProfileManager.getProfileEmoji(name: profileManager.activeProfile))
                    .font(.system(size: 14))

                Text(profileManager.activeProfile == "All Family" ? "All Family" : "\(profileManager.activeProfile)'s Cookbook")
                    .font(.system(size: 12.5, weight: .bold))
                    .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                    .lineLimit(1)

                Image(systemName: "chevron.down")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
            }
            .padding(.horizontal, 11)
            .padding(.vertical, 6)
            .background(Color(red: 0xFF / 255.0, green: 0xF7 / 255.0, blue: 0xED / 255.0), in: Capsule())
            .overlay(
                Capsule()
                    .stroke(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0).opacity(0.6), lineWidth: 1.5)
            )
            .shadow(color: Color.black.opacity(0.15), radius: 2, y: 1)
        }
        .buttonStyle(.plain)
    }
}

/**
 "Who's Cooking?" Modal Profile Switcher Sheet
 */
public struct ProfileSwitcherSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @Query private var allRecipes: [Recipe]
    @ObservedObject var profileManager = ProfileManager.shared

    @State private var showingAddMember = false
    @State private var profileToRename: String? = nil
    @State private var renameText = ""

    public init() {}

    public var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 0xFF / 255.0, green: 0xFD / 255.0, blue: 0xF9 / 255.0)
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 16) {
                        // All Family Card
                        allFamilyCard

                        // Individual Member Cards
                        VStack(spacing: 12) {
                            ForEach(profileManager.profiles, id: \.self) { profile in
                                memberCard(name: profile)
                            }
                        }

                        // Add Member Card Button
                        Button {
                            showingAddMember = true
                        } label: {
                            HStack(spacing: 10) {
                                Image(systemName: "plus.circle.fill")
                                    .font(.system(size: 20))
                                Text("Add Family Member")
                                    .font(.system(size: 14.5, weight: .bold))
                            }
                            .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color(red: 0xFF / 255.0, green: 0xFB / 255.0, blue: 0xF5 / 255.0), in: RoundedRectangle(cornerRadius: 16))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0).opacity(0.5), lineWidth: 1.5)
                            )
                        }
                        .padding(.top, 4)
                    }
                    .padding(20)
                }
            }
            .navigationTitle("Who's Cooking?")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                }
            }
            .sheet(isPresented: $showingAddMember) {
                AddMemberDialogView { newName in
                    profileManager.addProfile(newName)
                }
            }
            .alert("Rename Profile", isPresented: Binding(
                get: { profileToRename != nil },
                set: { if !$0 { profileToRename = nil } }
            )) {
                TextField("Name", text: $renameText)
                Button("Save") {
                    if let old = profileToRename {
                        profileManager.renameProfile(oldName: old, newName: renameText)
                    }
                    profileToRename = nil
                }
                Button("Cancel", role: .cancel) {
                    profileToRename = nil
                }
            }
        }
    }

    private var allFamilyCard: some View {
        let isSelected = profileManager.activeProfile == "All Family"
        let totalCount = allRecipes.filter { !$0.isDeleted }.count

        return Button {
            profileManager.activeProfile = "All Family"
            dismiss()
        } label: {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(Color(red: 0xE9 / 255.0, green: 0xD5 / 255.0, blue: 0xFF / 255.0))
                        .frame(width: 44, height: 44)
                    Text("👨‍👩‍👧")
                        .font(.system(size: 22))
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text("All Family Recipes")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(isSelected ? Color(red: 0x58 / 255.0, green: 0x1C / 255.0, blue: 0x87 / 255.0) : Color.primary)
                    Text("\(totalCount) total recipes across all members")
                        .font(.system(size: 12))
                        .foregroundStyle(.secondary)
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(Color(red: 0x7E / 255.0, green: 0x22 / 255.0, blue: 0xCE / 255.0))
                }
            }
            .padding(14)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isSelected ? Color(red: 0x7E / 255.0, green: 0x22 / 255.0, blue: 0xCE / 255.0) : Color(red: 0xE8 / 255.0, green: 0xDF / 255.0, blue: 0xD5 / 255.0), lineWidth: isSelected ? 2 : 1)
            )
            .shadow(color: Color.black.opacity(isSelected ? 0.08 : 0.03), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
    }

    private func memberCard(name: String) -> some View {
        let isSelected = profileManager.activeProfile.caseInsensitiveCompare(name) == .orderedSame
        let isDefault = profileManager.defaultProfile.caseInsensitiveCompare(name) == .orderedSame
        let emoji = ProfileManager.getProfileEmoji(name: name)
        let count = allRecipes.filter { !$0.isDeleted && ($0.profileName.caseInsensitiveCompare(name) == .orderedSame || ($0.profileName.isEmpty && name == "Louis")) }.count

        return HStack(spacing: 14) {
            Button {
                profileManager.activeProfile = name
                dismiss()
            } label: {
                HStack(spacing: 14) {
                    ZStack {
                        Circle()
                            .fill(Color(red: 0xFF / 255.0, green: 0xF0 / 255.0, blue: 0xE0 / 255.0))
                            .frame(width: 44, height: 44)
                        Text(emoji)
                            .font(.system(size: 22))
                    }

                    VStack(alignment: .leading, spacing: 3) {
                        HStack(spacing: 6) {
                            Text("\(name)'s Cookbook")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundStyle(isSelected ? Color(red: 0x43 / 255.0, green: 0x14 / 255.0, blue: 0x07 / 255.0) : Color.primary)

                            if isDefault {
                                Text("Default")
                                    .font(.system(size: 9.5, weight: .black))
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color(red: 0xFE / 255.0, green: 0xF3 / 255.0, blue: 0xC7 / 255.0), in: Capsule())
                                    .foregroundStyle(Color(red: 0x92 / 255.0, green: 0x40 / 255.0, blue: 0x0E / 255.0))
                            }
                        }

                        Text("\(count) recipes")
                            .font(.system(size: 12))
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    if isSelected {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))
                    }
                }
            }
            .buttonStyle(.plain)

            Menu {
                Button {
                    profileManager.defaultProfile = name
                } label: {
                    Label(isDefault ? "Default Profile ⭐" : "Set as Phone Default ⭐", systemImage: "star")
                }

                Button {
                    renameText = name
                    profileToRename = name
                } label: {
                    Label("Rename", systemImage: "pencil")
                }

                Button(role: .destructive) {
                    profileManager.deleteProfile(name)
                } label: {
                    Label("Delete", systemImage: "trash")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16))
                    .foregroundStyle(Color.secondary)
                    .padding(8)
            }
        }
        .padding(14)
        .background(Color.white, in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isSelected ? Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0) : Color(red: 0xE8 / 255.0, green: 0xDF / 255.0, blue: 0xD5 / 255.0), lineWidth: isSelected ? 2 : 1)
        )
        .shadow(color: Color.black.opacity(isSelected ? 0.08 : 0.03), radius: 4, y: 2)
    }
}

/**
 Add Member Dialog View with Live Emoji Avatar Preview & Quick Suggestions
 */
public struct AddMemberDialogView: View {
    @Environment(\.dismiss) private var dismiss
    let onAdd: (String) -> Void

    @State private var memberName = ""

    private let suggestions = [
        "Annette", "Isabel", "Louis", "Sarah", "Emma", "Sophie",
        "Wife", "Husband", "Daughter", "Son", "Mom", "Dad", "Grandma", "Grandpa"
    ]

    private var liveEmoji: String {
        memberName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "👨‍🍳" : ProfileManager.getProfileEmoji(name: memberName)
    }

    public var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                // Live 72pt Avatar Preview Circle
                ZStack {
                    Circle()
                        .fill(Color(red: 0xFF / 255.0, green: 0xF0 / 255.0, blue: 0xE0 / 255.0))
                        .frame(width: 80, height: 80)
                    Text(liveEmoji)
                        .font(.system(size: 42))
                }
                .padding(.top, 16)

                Text("Add Family Member")
                    .font(.system(size: 20, weight: .bold, design: .serif))
                    .foregroundStyle(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0))

                Text("Create a personalized cookbook for a member of your family:")
                    .font(.system(size: 13))
                    .foregroundStyle(Color.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)

                // Themed Outlined TextField
                TextField("Member Name (e.g. Annette)", text: $memberName)
                    .font(.system(size: 16, weight: .medium))
                    .padding(14)
                    .background(Color.white, in: RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0).opacity(0.7), lineWidth: 1.5)
                    )
                    .padding(.horizontal, 20)

                // Quick Pick Suggestions
                VStack(alignment: .leading, spacing: 8) {
                    Text("QUICK SUGGESTIONS")
                        .font(.system(size: 10, weight: .black))
                        .foregroundStyle(.secondary)
                        .padding(.horizontal, 20)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(suggestions, id: \.self) { sug in
                                let isSelected = memberName.caseInsensitiveCompare(sug) == .orderedSame
                                Button {
                                    memberName = sug
                                } label: {
                                    HStack(spacing: 4) {
                                        Text(ProfileManager.getProfileEmoji(name: sug))
                                            .font(.system(size: 12))
                                        Text(sug)
                                            .font(.system(size: 12, weight: .semibold))
                                    }
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 6)
                                    .background(
                                        isSelected ? Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0).opacity(0.18) : Color(red: 0xF5 / 255.0, green: 0xF0 / 255.0, blue: 0xEA / 255.0),
                                        in: Capsule()
                                    )
                                    .overlay(
                                        Capsule()
                                            .stroke(isSelected ? Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0) : Color.clear, lineWidth: 1.2)
                                    )
                                    .foregroundStyle(isSelected ? Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0) : Color.primary)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 20)
                    }
                }

                Spacer()

                // Create Cookbook Button
                Button {
                    let trimmed = memberName.trimmingCharacters(in: .whitespacesAndNewlines)
                    guard !trimmed.isEmpty else { return }
                    onAdd(trimmed)
                    dismiss()
                } label: {
                    Text("Create Cookbook")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(Color.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(
                            memberName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? Color.gray.opacity(0.4) : Color(red: 0xCC / 255.0, green: 0x55 / 255.0, blue: 0x00 / 255.0),
                            in: RoundedRectangle(cornerRadius: 14)
                        )
                }
                .disabled(memberName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
            .background(Color(red: 0xFF / 255.0, green: 0xFD / 255.0, blue: 0xF9 / 255.0))
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                        .foregroundStyle(Color.secondary)
                }
            }
        }
    }
}
