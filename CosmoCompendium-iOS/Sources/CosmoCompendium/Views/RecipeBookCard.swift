import SwiftUI

// MARK: - RecipeBookCard (Matching Android 1:1 RecipeBookCard in BookshelfScreen.kt)
public struct RecipeBookCard: View {
    let recipe: Recipe
    let isAllFamilyView: Bool
    let onOpen: () -> Void
    let onToggleFavorite: () -> Void
    let onEdit: () -> Void
    let onChoosePhoto: () -> Void
    let onTakePhoto: () -> Void
    let onGenerateAiPhoto: () -> Void
    let onAssignProfile: () -> Void
    let onAssignCategory: () -> Void
    let onDelete: () -> Void

    public init(
        recipe: Recipe,
        isAllFamilyView: Bool,
        onOpen: @escaping () -> Void,
        onToggleFavorite: @escaping () -> Void,
        onEdit: @escaping () -> Void,
        onChoosePhoto: @escaping () -> Void,
        onTakePhoto: @escaping () -> Void,
        onGenerateAiPhoto: @escaping () -> Void,
        onAssignProfile: @escaping () -> Void,
        onAssignCategory: @escaping () -> Void,
        onDelete: @escaping () -> Void
    ) {
        self.recipe = recipe
        self.isAllFamilyView = isAllFamilyView
        self.onOpen = onOpen
        self.onToggleFavorite = onToggleFavorite
        self.onEdit = onEdit
        self.onChoosePhoto = onChoosePhoto
        self.onTakePhoto = onTakePhoto
        self.onGenerateAiPhoto = onGenerateAiPhoto
        self.onAssignProfile = onAssignProfile
        self.onAssignCategory = onAssignCategory
        self.onDelete = onDelete
    }

    private var coverImage: UIImage? {
        recipe.resolvedCoverImage
    }

    private var hasPhoto: Bool {
        coverImage != nil
    }

    private var catStyle: CategoryBadgeStyle {
        CategoryBadgeStyle.style(for: recipe.category)
    }

    public var body: some View {
        Button(action: onOpen) {
            VStack(alignment: .leading, spacing: 0) {
                if let uiImage = coverImage {
                    // Framed Dish Photo Header (Matching Android AsyncImage)
                    ZStack(alignment: .top) {
                        Image(uiImage: uiImage)
                            .resizable()
                            .scaledToFill()
                            .frame(height: 130)
                            .frame(maxWidth: .infinity)
                            .clipped()

                        // Top Gradient for Badge Visibility
                        LinearGradient(
                            colors: [Color.black.opacity(0.55), Color.black.opacity(0.15), Color.clear],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .frame(height: 52)

                        // Top Bar over photo: Category on left, Favorite & Menu on right
                        HStack(alignment: .center) {
                            HStack(spacing: 4) {
                                Text(recipe.category)
                                    .font(.system(size: 9.5, weight: .bold))
                                    .foregroundStyle(catStyle.text)
                                    .padding(.horizontal, 7)
                                    .padding(.vertical, 2.5)
                                    .background(Color.white.opacity(0.95), in: Capsule())
                                    .overlay(Capsule().stroke(catStyle.border, lineWidth: 0.8))
                                    .lineLimit(1)

                                if isAllFamilyView {
                                    Text("\(ProfileManager.getProfileEmoji(name: recipe.profileName)) \(recipe.profileName)")
                                        .font(.system(size: 9, weight: .bold))
                                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                                        .padding(.horizontal, 5)
                                        .padding(.vertical, 2.5)
                                        .background(Color(hex: 0xFFF7ED).opacity(0.95), in: Capsule())
                                        .overlay(Capsule().stroke(CookbookTheme.terracottaPrimary.opacity(0.5), lineWidth: 0.8))
                                        .lineLimit(1)
                                }
                            }

                            Spacer()

                            cardActionButtons(isOverPhoto: true)
                        }
                        .padding(6)
                    }
                }

                // Card Body
                VStack(alignment: .leading, spacing: 6) {
                    if !hasPhoto {
                        // Top Bar: Category Pill + Characteristic Badge + Actions (Matching Android BookshelfScreen.kt:1288)
                        HStack(alignment: .center) {
                            HStack(spacing: 4) {
                                Text(recipe.category)
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundStyle(catStyle.text)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(catStyle.background, in: Capsule())
                                    .overlay(Capsule().stroke(catStyle.border, lineWidth: 1))
                                    .lineLimit(1)

                                if let characteristic = recipe.characteristicBadge {
                                    Text("✦ \(characteristic)")
                                        .font(.system(size: 9.5, weight: .bold))
                                        .foregroundStyle(Color(hex: 0x92400E))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .background(Color(hex: 0xFEF3C7), in: Capsule())
                                        .overlay(Capsule().stroke(Color(hex: 0xF59E0B).opacity(0.6), lineWidth: 1))
                                        .lineLimit(1)
                                }

                                if isAllFamilyView {
                                    Text("\(ProfileManager.getProfileEmoji(name: recipe.profileName)) \(recipe.profileName)")
                                        .font(.system(size: 9, weight: .bold))
                                        .foregroundStyle(CookbookTheme.terracottaPrimary)
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .background(Color(hex: 0xFFF7ED), in: Capsule())
                                        .overlay(Capsule().stroke(CookbookTheme.terracottaPrimary.opacity(0.5), lineWidth: 1))
                                        .lineLimit(1)
                                }
                            }

                            Spacer()

                            cardActionButtons(isOverPhoto: false)
                        }
                    }

                    // Recipe Title (Serif, Bold)
                    Text(recipe.title)
                        .font(.system(size: 15, weight: .bold, design: .serif))
                        .foregroundStyle(CookbookTheme.textPrimaryLight)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, hasPhoto ? 2 : 2)

                    // Star Rating if rated
                    if recipe.rating > 0 {
                        HStack(spacing: 2) {
                            ForEach(0..<recipe.rating, id: \.self) { _ in
                                Text("★")
                                    .font(.system(size: 10.5))
                                    .foregroundStyle(Color(hex: 0xF59E0B))
                            }
                        }
                    }

                    Spacer(minLength: 4)

                    // Bottom Row: Cook Time, Difficulty & Servings
                    HStack(alignment: .center) {
                        // Cook time pill
                        HStack(spacing: 3) {
                            Image(systemName: "clock")
                                .font(.system(size: 9.5))
                                .foregroundStyle(CookbookTheme.terracottaPrimary)
                            Text("\(recipe.cookTimeMinutes)m")
                                .font(.system(size: 10.5, weight: .semibold))
                                .foregroundStyle(Color(hex: 0x431407))
                        }
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2.5)
                        .background(Color(hex: 0xFAF3EC), in: RoundedRectangle(cornerRadius: 6))
                        .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color(hex: 0xEDE0D2), lineWidth: 1))

                        // Difficulty pill
                        Text(recipe.difficulty)
                            .font(.system(size: 9.5, weight: .medium))
                            .foregroundStyle(Color(hex: 0x5A4535))
                            .padding(.horizontal, 5)
                            .padding(.vertical, 2.5)
                            .background(Color(hex: 0xF5EFEB), in: RoundedRectangle(cornerRadius: 6))

                        Spacer()

                        // Servings
                        Text(recipe.servings)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundStyle(CookbookTheme.textMutedLight)
                            .lineLimit(1)
                    }
                }
                .padding(10)
            }
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(CookbookTheme.warmBorderSubtle, lineWidth: 1)
            )
            .shadow(color: Color(hex: 0x3D2615).opacity(0.12), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func cardActionButtons(isOverPhoto: Bool) -> some View {
        HStack(spacing: 4) {
            // Favorite Button
            Button(action: onToggleFavorite) {
                Image(systemName: recipe.isFavorite ? "heart.fill" : "heart")
                    .font(.system(size: 11))
                    .foregroundStyle(recipe.isFavorite ? Color(hex: 0xE11D48) : Color(hex: 0x4A3423))
                    .frame(width: 26, height: 26)
                    .background(Color.white.opacity(isOverPhoto ? 0.92 : 0.95), in: Circle())
                    .shadow(color: Color.black.opacity(0.08), radius: 1, y: 1)
            }
            .buttonStyle(.plain)

            // Menu Button with Photo Options
            Menu {
                Button(action: onChoosePhoto) {
                    Label("Choose Photo from Library", systemImage: "photo.on.rectangle")
                }
                Button(action: onTakePhoto) {
                    Label("Take Dish Photo", systemImage: "camera")
                }
                Button(action: onGenerateAiPhoto) {
                    Label(hasPhoto ? "Regenerate AI Food Photo" : "Generate AI Food Photo", systemImage: "sparkles")
                }
                Divider()
                Button(action: onEdit) {
                    Label("Edit Recipe", systemImage: "pencil")
                }
                Button(action: onAssignProfile) {
                    Label("Move to Family Member", systemImage: "person.2")
                }
                Button(action: onAssignCategory) {
                    Label("Assign Category", systemImage: "tag")
                }
                Divider()
                Button(role: .destructive, action: onDelete) {
                    Label("Delete Recipe", systemImage: "trash")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(Color(hex: 0x4A3423))
                    .frame(width: 26, height: 26)
                    .background(Color.white.opacity(isOverPhoto ? 0.92 : 0.95), in: Circle())
                    .shadow(color: Color.black.opacity(0.08), radius: 1, y: 1)
            }
        }
    }
}
