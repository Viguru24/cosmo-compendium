# Cookbook AI & Recipe Extraction Guidelines

This document specifies the critical rules, architecture, and extraction contracts for the AI-assisted recipe scanning and parsing engine in the Cookbook application.

---

## 1. Recipe Scanning & Vision Pipeline

### Multimodal Vision Flow
1. **Unlimited Multi-Page Support**:
   - Recipe cards often consist of a Front (Ingredients / Title / Metadata), Back (Directions / Steps), or 3 to 4+ scrapbook pages, notebook spreads, or folded inserts.
   - All captured or uploaded pages (unlimited count) are downscaled and converted to JPEG Base64 parts within a single Gemini `generateContent` request.
   - The multimodal prompt instructs the model to synthesize all provided pages in order into a single unified, coherent recipe entity.

2. **Rapid Continuous Camera Snapping (Page 1 -> Page 2 -> Page 3 -> ... until Back Pressed)**:
   - When the user snaps Page 1, the camera immediately and automatically reopens for Page 2.
   - When user snaps Page 2, it reopens for Page 3, then Page 4, etc.
   - Whenever the user is done with that recipe and presses the Back button / cancel on the camera, the app immediately and automatically scans, synthesizes all captured pages (1, 2, 3, 4+ pages), and saves the recipe into the cookbook without requiring manual button taps!

---

## 2. Strict Ingredient Extraction Mandates

> **CRITICAL DIRECTIVE**: Never skip, omit, summarize, or alter ingredients.

1. **Complete Extraction**:
   - Extract **every single ingredient** mentioned or implied on the recipe card or text.
   - Do NOT omit staples, leavening agents, extracts, spices, seasonings, or liquids.
   - Capture exact quantities/fractions (e.g. `2 1/4`, `1/2`, `3/4`, `1-2`), units of measure (e.g. `cups`, `tbsp`, `tsp`, `oz`, `pkg`, `cloves`, `pinch`), and descriptive names.
   - Preserve brand names, package sizes, and prep notes (e.g., `"6-ounce package Ocean Spray Craisins"`, `"3 large eggs, beaten"`).

2. **Multi-Layer Resilient JSON Parsing**:
   - Primary deserialization via Moshi with flexible JSON keys (`nameEnglish`, `name`, `ingredient`, `item`, `amount`, `quantity`, `unit`, `measurement`).
   - Secondary fallback via `parseLenientJson` to handle nested or unconventional JSON returns.
   - Local fallback via `OfflineRecipeParser` with regex-based ingredient detection for offline/network-free resilience.
   - Sub-grouping support: Section headers such as `"Dough"`, `"Filling"`, `"Crust"`, `"Glaze"`, and `"Topping"` are parsed and assigned to the `group` property of each ingredient.

---

## 3. Bilingual Support & Translation

- Supports handwritten German recipe cards (Kurrentschrift / Sütterlin / modern cursive) and English cards.
- Translates titles, ingredient names, and preparation steps into natural, clean English while preserving the original German text for heirloom authenticity.
- Fractions and spoken units (e.g. "1/4 / 1/2 spoon", "ein Esslöffel", "eine Prise") are standardized to clean display formats.

---

## 4. Food Photo Auto-Cropping

- If the scanned card includes a food photograph alongside text, the vision model returns a normalized bounding box (`ymin`, `xmin`, `ymax`, `xmax`, 0-1000).
- The app automatically crops and saves only the food photograph as the recipe's cover image.

---

## 5. Continuous Batch Scanning & Direct Auto-Save Flow

- **Direct Auto-Save**: Because AI scan transcription accuracy is high, completed recipe scans are automatically inserted directly into the database without interrupting the user with an intermediate inspection/review screen or navigating out to the recipe page.
- **Continuous Camera Loop**: After a recipe is scanned and saved, the app immediately reopens the camera for the next recipe card (Page 1) so the user can scan stacks of recipe cards in rapid succession.
- **Session Progress & Completion**: The scan interface maintains a session counter (e.g. "X recipes saved") and offers a "Done Scanning" action to cleanly return to the bookshelf when finished.
