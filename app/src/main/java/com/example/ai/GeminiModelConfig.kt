package com.example.ai

/**
 * Single source of truth for verified Google Gemini API model identifiers.
 * DO NOT change or invent model identifiers here without verifying with official Google AI docs.
 */
data class GeminiModelOption(
    val id: String,
    val displayName: String,
    val tag: String,
    val description: String
)

object GeminiModelConfig {
    /** Ultra-fast, low-latency, multimodal model for recipe vision and extraction */
    const val PRIMARY_MODEL = "gemini-2.5-flash"

    /** Resilient fallback model */
    const val FALLBACK_MODEL = "gemini-3.5-flash"
    const val LEGACY_FALLBACK_MODEL = "gemini-flash-latest"

    /** Verified, strictly valid active Google Gemini models */
    val ACTIVE_MODELS = listOf(
        "gemini-2.5-flash",
        "gemini-3.5-flash",
        "gemini-3.7-flash",
        "gemini-3.6-flash",
        "gemini-flash-latest"
    )

    val MODEL_OPTIONS = listOf(
        GeminiModelOption("gemini-2.5-flash", "Gemini 2.5 Flash", "Default / Fastest", "Ultra-fast, lowest latency multimodal vision & OCR"),
        GeminiModelOption("gemini-3.5-flash", "Gemini 3.5 Flash", "High Speed", "Rapid multimodal extraction and recipe parsing"),
        GeminiModelOption("gemini-3.7-flash", "Gemini 3.7 Flash", "Advanced", "Deep multimodal reasoning for complex handwriting"),
        GeminiModelOption("gemini-flash-latest", "Gemini Flash (Auto)", "Latest", "Always routes to the newest official production Flash model")
    )
}
