package com.example.ai

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// --- Gemini Request / Response DTOs ---
@JsonClass(generateAdapter = true)
data class GeminiImageConfig(
    val aspectRatio: String = "1:1",
    val imageSize: String? = "1K"
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null,
    val responseModalities: List<String>? = null,
    val imageConfig: GeminiImageConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class FoodPhotoBoxDto(
    val ymin: Int? = null,
    val xmin: Int? = null,
    val ymax: Int? = null,
    val xmax: Int? = null,
    val pageIndex: Int? = 0
)

// --- Parsed Recipe Schema ---
@JsonClass(generateAdapter = true)
data class ParsedRecipeDto(
    val title: String? = null,
    val name: String? = null,
    val titleGerman: String? = null,
    val titleEnglish: String? = null,
    val category: String? = null,
    val servings: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val difficulty: String? = null,
    val ingredients: List<ParsedIngredientDto>? = null,
    val steps: List<ParsedStepDto>? = null,
    val notes: String? = null,
    val notesGerman: String? = null,
    val notesEnglish: String? = null,
    val detectedSourceLanguage: String? = null, // "de" or "en"
    val hasFoodPhoto: Boolean? = false,
    val foodPhotoBox: FoodPhotoBoxDto? = null,
    val cardRotationNeededDegrees: Int? = 0
)

@JsonClass(generateAdapter = true)
data class ParsedIngredientDto(
    val nameGerman: String? = null,
    val nameEnglish: String? = null,
    val name: String? = null,
    val ingredient: String? = null,
    val item: String? = null,
    val raw: String? = null,
    val amount: String? = null,
    val quantity: String? = null,
    val qty: String? = null,
    val unit: String? = null,
    val measurement: String? = null,
    val measure: String? = null,
    val isOptional: Boolean? = false,
    val group: String? = null
)

@JsonClass(generateAdapter = true)
data class ParsedStepDto(
    val stepNumber: Int? = null,
    val instructionGerman: String? = null,
    val instructionEnglish: String? = null,
    val instruction: String? = null,
    val step: String? = null,
    val text: String? = null,
    val description: String? = null,
    val timerMinutes: Int? = null,
    val tip: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiModelItemDto(
    val name: String? = null,
    val supportedGenerationMethods: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiModelListResponseDto(
    val models: List<GeminiModelItemDto>? = null
)

interface GeminiApi {
    @GET("v1beta/models")
    suspend fun listModels(
        @Header("x-goog-api-key") apiKeyHeader: String,
        @Query("key") apiKey: String
    ): GeminiModelListResponseDto

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKeyHeader: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
