package com.example.ai

/**
 * Universal Multi-Provider AI Configuration
 * Supports Google Gemini, OpenAI (ChatGPT), xAI (Grok), Anthropic (Claude), OpenRouter, and Local OpenAI-compatible APIs (Ollama).
 */
enum class AiProvider(
    val id: String,
    val displayName: String,
    val emoji: String,
    val keyUrl: String,
    val defaultModel: String,
    val defaultBaseUrl: String,
    val description: String,
    val popularModels: List<String>
) {
    GOOGLE_GEMINI(
        id = "google",
        displayName = "Google Gemini",
        emoji = "✨",
        keyUrl = "https://aistudio.google.com/app/apikey",
        defaultModel = "gemini-2.5-flash",
        defaultBaseUrl = "https://generativelanguage.googleapis.com",
        description = "Official Google Gemini with ultra-fast multimodal vision & OCR (Free tier available).",
        popularModels = listOf("gemini-2.5-flash", "gemini-3.5-flash", "gemini-3.7-flash", "gemini-flash-latest")
    ),
    OPENAI_CHATGPT(
        id = "openai",
        displayName = "OpenAI (ChatGPT)",
        emoji = "🤖",
        keyUrl = "https://platform.openai.com/api-keys",
        defaultModel = "gpt-4o",
        defaultBaseUrl = "https://api.openai.com/v1",
        description = "OpenAI GPT-4o & GPT-4o-mini with high precision recipe parsing.",
        popularModels = listOf("gpt-4o", "gpt-4o-mini", "o3-mini", "gpt-4-turbo")
    ),
    XAI_GROK(
        id = "grok",
        displayName = "xAI (Grok)",
        emoji = "⚡",
        keyUrl = "https://console.x.ai/",
        defaultModel = "grok-2-vision-1212",
        defaultBaseUrl = "https://api.x.ai/v1",
        description = "xAI Grok multimodal vision and conversational models.",
        popularModels = listOf("grok-2-vision-1212", "grok-2-1212", "grok-beta")
    ),
    ANTHROPIC_CLAUDE(
        id = "anthropic",
        displayName = "Anthropic (Claude)",
        emoji = "🧠",
        keyUrl = "https://console.anthropic.com/",
        defaultModel = "claude-3-5-sonnet-20241022",
        defaultBaseUrl = "https://api.anthropic.com/v1",
        description = "Anthropic Claude 3.5 Sonnet & Haiku multimodal vision.",
        popularModels = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022")
    ),
    OPENROUTER(
        id = "openrouter",
        displayName = "OpenRouter",
        emoji = "🌐",
        keyUrl = "https://openrouter.ai/keys",
        defaultModel = "google/gemini-2.0-flash",
        defaultBaseUrl = "https://openrouter.ai/api/v1",
        description = "Access 200+ models with one API key (Gemini, Claude, GPT, Llama, Grok).",
        popularModels = listOf("google/gemini-2.0-flash", "openai/gpt-4o", "anthropic/claude-3.5-sonnet", "x-ai/grok-2-vision-1212")
    ),
    CUSTOM_OPENAI(
        id = "custom",
        displayName = "Custom / Local AI (Ollama)",
        emoji = "💻",
        keyUrl = "https://ollama.com",
        defaultModel = "llama3.2-vision",
        defaultBaseUrl = "http://192.168.1.50:11434/v1",
        description = "Local or self-hosted OpenAI-compatible server (Ollama, LM Studio, vLLM).",
        popularModels = listOf("llama3.2-vision", "llava", "qwen2.5-coder", "mistral")
    );

    companion object {
        fun fromId(id: String): AiProvider {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GOOGLE_GEMINI
        }
    }
}
