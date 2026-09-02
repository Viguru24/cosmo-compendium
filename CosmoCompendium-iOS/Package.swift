// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CosmoCompendium",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "CosmoCompendium",
            targets: ["CosmoCompendium"]
        )
    ],
    targets: [
        .target(
            name: "CosmoCompendium",
            path: "Sources/CosmoCompendium"
        ),
        .testTarget(
            name: "CosmoCompendiumTests",
            dependencies: ["CosmoCompendium"],
            path: "Tests/CosmoCompendiumTests"
        )
    ]
)
