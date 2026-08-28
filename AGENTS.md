# Cookbook AI & Recipe Extraction Guidelines

This document specifies the critical rules, architecture, and extraction contracts for the AI-assisted recipe scanning and parsing engine in the Cookbook application.

---

## 1. Recipe Scanning & Vision Pipeline

### Multimodal Vision Flow
1. **Multi-Page Support**:
   - Recipe cards often consist of a Front (Ingredients / Title / Metadata) and Back (Directions / Steps / Oven instructions) or multiple scrapbook pages.
   - All captured or uploaded pages are downscaled and converted to JPEG Base64 parts within a single Gemini `generateContent` request.
   - The multimodal prompt instructs the model to synthesize both pages into a unified, coherent recipe entity.

2. **Automatic Camera & Multi-Page Sequencing**:
   - When the user captures **Page 1**, the camera flow automatically prompts and opens for **Page 2** (Back of card).
   - Once **2 or more pages** are captured (or if user selects multiple images from the gallery), the app automatically triggers transcription and translation (`onScan(...)`).
   - If the user only has a 1-page recipe and exits the second camera capture, the app automatically transcribes the single captured page.

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
