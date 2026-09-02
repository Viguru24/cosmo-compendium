import SwiftUI
import SwiftData

@main
public struct CosmoCompendiumApp: App {
    public init() {}

    public var body: some Scene {
        WindowGroup {
            BookshelfView()
        }
        .modelContainer(for: [Recipe.self, ShoppingItem.self])
    }
}
