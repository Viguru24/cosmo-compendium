# Cosmo Compendium AI & Formulation / Recipe Extraction Guidelines

This document specifies the critical rules, architecture, and extraction contracts for the AI-assisted recipe and craft formula scanning and parsing engine in the Cosmo Compendium application.

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

## 3. Universal Multi-Language Support & Global Translation

- Supports handwritten and printed recipe cards in **ANY language worldwide** (e.g., German, French, Italian, Spanish, Portuguese, Polish, Russian, Ukrainian, Dutch, Swedish, Danish, Japanese, Chinese, Arabic, Hindi, Greek, etc., including vintage cursive scripts like Kurrentschrift and Sütterlin).
- Translates titles, ingredient names, preparation steps, and notes into natural, clean, culinary English while preserving the original source text in the original language fields for authentic heritage preservation.
- International fractions, measurements, and spoken colloquial units (e.g. "1/4 / 1/2 spoon", "ein Esslöffel", "une pincée", "una presa") are standardized into clean display formats.

---

## 4. Food Photo Auto-Cropping

- If the scanned card includes a food photograph alongside text, the vision model returns a normalized bounding box (`ymin`, `xmin`, `ymax`, `xmax`, 0-1000).
- The app automatically crops and saves only the food photograph as the recipe's cover image.

---

## 5. Continuous Batch Scanning & Direct Auto-Save Flow

- **Direct Auto-Save**: Because AI scan transcription accuracy is high, completed recipe scans are automatically inserted directly into the database without interrupting the user with an intermediate inspection/review screen or navigating out to the recipe page.
- **Continuous Camera Loop**: After a recipe is scanned and saved, the app immediately reopens the camera for the next recipe card (Page 1) so the user can scan stacks of recipe cards in rapid succession.
- **Session Progress & Completion**: The scan interface maintains a session counter (e.g. "X recipes saved") and offers a "Done Scanning" action to cleanly return to the bookshelf when finished.

---

## 6. Strict Google Gemini Model Architecture & Self-Healing Discovery

> **CRITICAL ARCHITECTURAL DIRECTIVE**: Never rely on a single static model identifier without dynamic fallback. Always follow `GEMINI_MODEL_ARCHITECTURE.md`.

- **Single Source of Truth**: All model identifiers and fallback chains must reference `GeminiModelConfig` (`com.example.ai.GeminiModelConfig`).
- **Dynamic Model Discovery & Self-Healing**:
  - The app dynamically discovers live models from `v1beta/models?key=...` on device.
  - If Google deprecates or changes a model (returning `404 Not Found` or `"is no longer available"`), the system **automatically blacklists and evicts** that model, hot-swapping to the next live model instantly without user intervention.
- **Active Verified Fallback Chain**:
  - `GeminiModelConfig.PRIMARY_MODEL`: `"gemini-2.5-flash"` (Ultra-fast multimodal vision & extraction).
  - `GeminiModelConfig.FALLBACK_MODEL`: `"gemini-3.5-flash"` (High-speed resilient backup).
  - Additional verified active models: `"gemini-3.7-flash"`, `"gemini-3.6-flash"`, `"gemini-flash-latest"`.
- **Graceful Offline Fallback**: If Gemini API returns an error or is unavailable, the system must immediately and seamlessly fall back to `OfflineRecipeParser` without stalling, blocking, or hanging the UI.

