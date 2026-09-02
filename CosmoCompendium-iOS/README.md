# Cosmo Compendium — iPhone App (Native Swift & SwiftUI)

A native iOS application for **Cosmo Compendium: Recipe & Formula Keeper**, crafted with Swift, SwiftUI, SwiftData, and VisionKit.

---

## Features

- **Heirloom Bookshelf & Realistic Leather Journal**:
  - High-fidelity SwiftUI `Canvas` shader rendering antique full-grain leather, scorched carbon corners, aged coffee/oil stains, waxed saddle stitching, and antique weathered brass corner brackets.
  - Category filtering, instant fuzzy search, and tactile book cover cards.

- **Continuous Multi-Page Vision Scanning (VisionKit + Gemini AI)**:
  - Built with `VNDocumentCameraViewController` for rapid continuous page snapping (Page 1 $\rightarrow$ Page 2 $\rightarrow$ Page 3 $\rightarrow$ Done).
  - Gemini 2.5/1.5 Flash multimodal vision API synthesizes all pages into a unified recipe entity.
  - Enforces strict ingredient completeness directive and auto-crops food photos using normalized bounding boxes.
  - Offline heuristic regex parsing engine when network or API key is absent.

- **Kitchen Cook Mode**:
  - High-contrast, OLED-friendly distraction-free interface.
  - Screen keep-awake lock (`UIApplication.shared.isIdleTimerDisabled = true`).
  - Active step countdown timers with auditory and haptic alerts.
  - Ingredients quick drawer to cross-reference measurements while cooking.

- **Smart Culinary Unit Converter**:
  - Funneled UK Kitchen Blueprint (UK spoons for $<15$g/$<15$ml, grams for dry, ml for liquids, °C & Gas Mark).
  - US Cups & Spoons, Metric Weights & Volumes, and Baker's Precision Grams with ingredient density factors (flour, sugar, butter, honey, etc.).

- **Pantry & Grocery Basket**:
  - Automatic department categorization (Produce, Dairy, Meat, Bakery, Spices, Pantry).
  - One-tap ingredient checklist and batch export.

- **Printable Heirloom PDF Generation**:
  - Native `PDFKit` renderer producing high-resolution, vintage-bordered printable recipe cards.

---

## Opening and Running in Xcode

1. Open the project in Xcode:
   ```bash
   open CosmoCompendium-iOS/CosmoCompendium.xcodeproj
   ```
2. Select an iOS Simulator (e.g. **iPhone 15** or **iPhone 16 Pro**) or your physical iPhone.
3. Press **Cmd + R** to Build & Run!

---

## Build from Terminal

```bash
xcodebuild -project CosmoCompendium-iOS/CosmoCompendium.xcodeproj \
           -scheme CosmoCompendium \
           -destination "generic/platform=iOS Simulator" \
           clean build
```
