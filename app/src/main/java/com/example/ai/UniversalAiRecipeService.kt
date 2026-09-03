package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.network.NetworkModule
import com.example.util.AppLogger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Universal AI Recipe Service supporting:
 * - Google Gemini (Official Multimodal API)
 * - OpenAI / ChatGPT (GPT-4o, GPT-4o-mini, o3-mini)
 * - xAI / Grok (Grok-2-vision, Grok-2, Grok-beta)
 * - Anthropic / Claude (Claude 3.5 Sonnet, Claude 3.5 Haiku)
 * - OpenRouter (Universal router)
 * - Custom / Local (Ollama, LM Studio, vLLM)
 */
object UniversalAiRecipeService {

    private var activeProvider: AiProvider = AiProvider.GOOGLE_GEMINI
    private var providerApiKeys = mutableMapOf<String, String>()
    private var providerModels = mutableMapOf<String, String>()
    private var customBaseUrls = mutableMapOf<String, String>()

    fun setProvider(provider: AiProvider) {
        activeProvider = provider
    }

    fun getActiveProvider(): AiProvider = activeProvider

    fun setApiKey(provider: AiProvider, key: String) {
        val clean = key.trim()
        if (clean.isNotBlank()) {
            providerApiKeys[provider.id] = clean
        } else {
            providerApiKeys.remove(provider.id)
        }
        if (provider == AiProvider.GOOGLE_GEMINI) {
            GeminiRecipeService.setCustomApiKey(clean)
        }
    }

    fun getApiKey(provider: AiProvider): String {
        return providerApiKeys[provider.id] ?: if (provider == AiProvider.GOOGLE_GEMINI) GeminiRecipeService.getEffectiveApiKey() else ""
    }

    fun setModel(provider: AiProvider, model: String) {
        val clean = model.trim()
        if (clean.isNotBlank()) {
            providerModels[provider.id] = clean
        } else {
            providerModels.remove(provider.id)
        }
        if (provider == AiProvider.GOOGLE_GEMINI) {
            GeminiRecipeService.setCustomModel(clean)
        }
    }

    fun getModel(provider: AiProvider): String {
        return providerModels[provider.id] ?: provider.defaultModel
    }

    fun setCustomBaseUrl(provider: AiProvider, url: String) {
        val clean = url.trim().removeSuffix("/")
        if (clean.isNotBlank()) {
            customBaseUrls[provider.id] = clean
        } else {
            customBaseUrls.remove(provider.id)
        }
    }

    fun getBaseUrl(provider: AiProvider): String {
        return customBaseUrls[provider.id] ?: provider.defaultBaseUrl
    }

    private val httpClient: OkHttpClient by lazy {
        NetworkModule.okHttpClient.newBuilder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Universal recipe parsing across any configured provider
     */
    suspend fun parseRecipe(
        imageBitmaps: List<Bitmap>,
        recipeText: String?
    ): Result<ParsedRecipeDto> = withContext(Dispatchers.IO) {
        val provider = activeProvider
        val apiKey = getApiKey(provider)

        // Gemini delegates to official GeminiRecipeService
        if (provider == AiProvider.GOOGLE_GEMINI) {
            return@withContext GeminiRecipeService.parseRecipeWithAi(imageBitmaps, recipeText)
        }

        // Check if API key is provided
        if (apiKey.isBlank() && provider != AiProvider.CUSTOM_OPENAI) {
            if (!recipeText.isNullOrBlank()) {
                return@withContext Result.success(OfflineRecipeParser.parse(recipeText))
            }
            return@withContext Result.failure(
                Exception("${provider.displayName} API Key is missing. Please enter your API key in Settings -> AI Engine or tap 'Get Free API Key'.")
            )
        }

        try {
            when (provider) {
                AiProvider.OPENAI_CHATGPT,
                AiProvider.XAI_GROK,
                AiProvider.OPENROUTER,
                AiProvider.CUSTOM_OPENAI -> {
                    parseOpenAiCompatible(provider, apiKey, imageBitmaps, recipeText)
                }
                AiProvider.ANTHROPIC_CLAUDE -> {
                    parseAnthropic(apiKey, imageBitmaps, recipeText)
                }
                else -> {
                    GeminiRecipeService.parseRecipeWithAi(imageBitmaps, recipeText)
                }
            }
        } catch (e: Throwable) {
            AppLogger.e("UniversalAiRecipeService", "Extraction failed on ${provider.displayName}: ${e.message}", e)
            if (!recipeText.isNullOrBlank()) {
                AppLogger.w("UniversalAiRecipeService", "Falling back to OfflineRecipeParser")
                Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * OpenAI-Compatible Endpoint Handler (OpenAI GPT-4o, xAI Grok, OpenRouter, Ollama)
     */
    private suspend fun parseOpenAiCompatible(
        provider: AiProvider,
        apiKey: String,
        bitmaps: List<Bitmap>,
        text: String?
    ): Result<ParsedRecipeDto> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(provider)
        val model = getModel(provider)
        val endpoint = "$baseUrl/chat/completions"

        val prompt = SmartPromptBuilder.buildRecipeExtractionPrompt(text)

        val messagesArray = JSONArray()
        val userContentArray = JSONArray()

        // Text prompt part
        val textObj = JSONObject().apply {
            put("type", "text")
            put("text", prompt)
        }
        userContentArray.put(textObj)

        // Vision image parts
        bitmaps.forEach { bitmap ->
            val base64 = encodeBitmapToBase64(bitmap)
            val imgObj = JSONObject().apply {
                put("type", "image_url")
                put("image_url", JSONObject().apply {
                    put("url", "data:image/jpeg;base64,$base64")
                    put("detail", "high")
                })
            }
            userContentArray.put(imgObj)
        }

        val userMessage = JSONObject().apply {
            put("role", "user")
            put("content", userContentArray)
        }
        messagesArray.put(userMessage)

        val requestBodyJson = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("temperature", 0.1)
            if (provider == AiProvider.OPENAI_CHATGPT || provider == AiProvider.XAI_GROK) {
                put("response_format", JSONObject().apply { put("type", "json_object") })
            }
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .addHeader("Content-Type", "application/json")

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }
        if (provider == AiProvider.OPENROUTER) {
            requestBuilder.addHeader("HTTP-Referer", "https://github.com/Viguru24/cosmo-compendium")
            requestBuilder.addHeader("X-Title", "Cosmo Compendium")
        }

        val response = httpClient.newCall(requestBuilder.build()).execute()
        val bodyString = response.body?.string() ?: throw Exception("Empty response from ${provider.displayName}")

        if (!response.isSuccessful) {
            throw Exception("${provider.displayName} API error (${response.code}): $bodyString")
        }

        val rootJson = JSONObject(bodyString)
        val choices = rootJson.optJSONArray("choices")
        val content = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
            ?: throw Exception("No message content returned from ${provider.displayName}")

        val parsed = GeminiClient.parseLenientJson(content)
        if (parsed != null) {
            Result.success(parsed)
        } else {
            Result.failure(Exception("Failed to parse recipe JSON from ${provider.displayName}"))
        }
    }

    /**
     * Anthropic Claude /v1/messages API Handler
     */
    private suspend fun parseAnthropic(
        apiKey: String,
        bitmaps: List<Bitmap>,
        text: String?
    ): Result<ParsedRecipeDto> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(AiProvider.ANTHROPIC_CLAUDE)
        val model = getModel(AiProvider.ANTHROPIC_CLAUDE)
        val endpoint = "$baseUrl/messages"

        val prompt = SmartPromptBuilder.buildRecipeExtractionPrompt(text)

        val contentArray = JSONArray()

        bitmaps.forEach { bitmap ->
            val base64 = encodeBitmapToBase64(bitmap)
            val imgObj = JSONObject().apply {
                put("type", "image")
                put("source", JSONObject().apply {
                    put("type", "base64")
                    put("media_type", "image/jpeg")
                    put("data", base64)
                })
            }
            contentArray.put(imgObj)
        }

        val textObj = JSONObject().apply {
            put("type", "text")
            put("text", "$prompt\n\nCRITICAL: Output ONLY a single, valid JSON object following the schema without markdown formatting or introductory text.")
        }
        contentArray.put(textObj)

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", contentArray)
            })
        }

        val requestBodyJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", 4096)
            put("messages", messagesArray)
            put("temperature", 0.1)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyString = response.body?.string() ?: throw Exception("Empty response from Anthropic")

        if (!response.isSuccessful) {
            throw Exception("Anthropic API error (${response.code}): $bodyString")
        }

        val rootJson = JSONObject(bodyString)
        val contents = rootJson.optJSONArray("content")
        val textBlock = contents?.optJSONObject(0)?.optString("text")
            ?: throw Exception("No text block in Anthropic response")

        val parsed = GeminiClient.parseLenientJson(textBlock)
        if (parsed != null) {
            Result.success(parsed)
        } else {
            Result.failure(Exception("Failed to parse recipe JSON from Anthropic"))
        }
    }

    /**
     * Test connection for any selected provider
     */
    suspend fun testConnection(provider: AiProvider): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(provider)
        if (apiKey.isBlank() && provider != AiProvider.CUSTOM_OPENAI) {
            return@withContext Result.failure(Exception("Please enter an API key for ${provider.displayName}"))
        }

        try {
            when (provider) {
                AiProvider.GOOGLE_GEMINI -> {
                    GeminiClient.testApiKeyDetailed()
                }
                AiProvider.OPENAI_CHATGPT,
                AiProvider.XAI_GROK,
                AiProvider.OPENROUTER,
                AiProvider.CUSTOM_OPENAI -> {
                    testOpenAiConnection(provider, apiKey)
                }
                AiProvider.ANTHROPIC_CLAUDE -> {
                    testAnthropicConnection(apiKey)
                }
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun testOpenAiConnection(provider: AiProvider, apiKey: String): Result<String> {
        val baseUrl = getBaseUrl(provider)
        val model = getModel(provider)
        val endpoint = "$baseUrl/chat/completions"

        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Respond with 'OK'")
                })
            })
            put("max_tokens", 10)
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }
        if (provider == AiProvider.OPENROUTER) {
            requestBuilder.addHeader("HTTP-Referer", "https://github.com/Viguru24/cosmo-compendium")
            requestBuilder.addHeader("X-Title", "Cosmo Compendium")
        }

        val response = httpClient.newCall(requestBuilder.build()).execute()
        val text = response.body?.string() ?: ""
        if (response.isSuccessful) {
            return Result.success("Connected to ${provider.displayName} ($model) successfully!")
        } else {
            return Result.failure(Exception("${provider.displayName} error ${response.code}: $text"))
        }
    }

    private fun testAnthropicConnection(apiKey: String): Result<String> {
        val baseUrl = getBaseUrl(AiProvider.ANTHROPIC_CLAUDE)
        val model = getModel(AiProvider.ANTHROPIC_CLAUDE)
        val endpoint = "$baseUrl/messages"

        val body = JSONObject().apply {
            put("model", model)
            put("max_tokens", 10)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Respond with 'OK'")
                })
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()

        val response = httpClient.newCall(request).execute()
        val text = response.body?.string() ?: ""
        if (response.isSuccessful) {
            return Result.success("Connected to Anthropic ($model) successfully!")
        } else {
            return Result.failure(Exception("Anthropic error ${response.code}: $text"))
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
