# Google Gemini Model Lifecycle & Self-Healing Architecture

## 1. Executive Summary & Core Principle

Google periodically introduces new Gemini model generations (e.g. Gemini 1.5 -> 2.0 -> 2.5 -> 3.5 -> 3.6 -> 3.7) and permanently sunsets older versions. When a model version is retired, Google's API returns **HTTP 404 (Not Found)** with messages such as:
`json
{
  error: {
    code: 404,
    message: This model models/gemini-2.0-flash is no longer available. Please update your code to use models/gemini-3.6-flash...,
    status: NOT_FOUND
  }
}
`

Hardcoding static model identifiers without dynamic discovery guarantees eventual failure. Therefore, the Cookbook codebase implements an **autonomous, self-healing model discovery & eviction engine** that makes model obsolescence impossible to break the app.

---

## 2. Dynamic Model Discovery Architecture

### Flow Diagram
`
User Initiates Scan / App Launches
       |
       v
[Check Discovered Models Cache]
  |-- Valid (Within 12h TTL) ---> Use Cached Live Models
  +-- Stale or Empty -----------> Call v1beta/models?key=...
                                       |
                                       v
                              [Filter & Prioritize]
                                1. Supported generation: generateContent
                                2. Flash multimodal models (newest first: 3.7, 3.6, 3.5, 2.5, flash-latest)
                                3. Pro multimodal models (3.1-pro, 2.5-pro, pro-latest)
                                4. Evict any model in retiredModels blacklist
                                       |
                                       v
                              [Execute generateContent]
                                |-- HTTP 200 OK ----------> Return Parsed Recipe (Success)
                                |-- HTTP 404 / Retired ---> Auto-blacklist model, evict from cache, 
                                |                          and immediately try next live model!
                                +-- All APIs Offline -----> Seamless fallback to OfflineRecipeParser
`

---

## 3. The 4 Layers of Resilience

1. **Layer 1: Live Model Discovery API (1beta/models)**
   - Queries Google's live catalog on device using the user's active API key.
   - Automatically detects what models Google currently exposes for that specific API tier and region.

2. **Layer 2: Automatic 404 Auto-Eviction & Immediate Hot-Switching**
   - If any model returns HTTP 404 or a no longer available error payload:
     - The engine immediately records that model in the etiredModels blacklist in memory and SharedPreferences.
     - The model is purged from the active rotation without prompting or interrupting the user.
     - The engine instantly retries the request with the next model in the priority chain.

3. **Layer 3: Verified Multi-Version Fallback Chain**
   - If the device is temporarily unable to query the models catalog (e.g. initial connection jitter), it falls back to the verified multi-generation fallback list:
     - gemini-2.5-flash
     - gemini-3.5-flash
     - gemini-3.7-flash
     - gemini-3.6-flash
     - gemini-flash-latest (Google's dynamic alias)

4. **Layer 4: 100% Offline Parser (OfflineRecipeParser)**
   - If all network requests fail or the user has zero connectivity, the app never crashes or stalls. It falls back directly to the local offline regex & OCR parsing engine.

---

## 4. Strict Code-Level Contracts

1. **Never Hardcode a Single Model without Fallbacks**:
   - GeminiRecipeService.parseRecipeWithAi must ALWAYS iterate through getEffectiveModels().
2. **Never Treat Model Lists as Immutable**:
   - GeminiClient maintains mutable, dynamically updated collections of verified active models.
3. **Single Source of Truth**:
   - All default lists, aliases, and ranking logic are centralized in com.example.ai.GeminiModelConfig.
