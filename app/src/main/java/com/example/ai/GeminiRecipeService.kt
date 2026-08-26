package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response DTOs ---
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
    val temperature: Float = 0.2f,
    val topP: Float = 0.95f,
    val topK: Int = 40,
    val responseMimeType: String = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

// --- Parsed Recipe Schema ---
@JsonClass(generateAdapter = true)
data class ParsedRecipeDto(
    val titleGerman: String? = null,
    val titleEnglish: String? = null,
    val category: String? = null,
    val servings: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val difficulty: String? = null,
    val ingredients: List<ParsedIngredientDto>? = null,
    val steps: List<ParsedStepDto>? = null,
    val notesGerman: String? = null,
    val notesEnglish: String? = null,
    val detectedSourceLanguage: String? = null // "de" or "en"
)

@JsonClass(generateAdapter = true)
data class ParsedIngredientDto(
    val nameGerman: String? = null,
    val nameEnglish: String? = null,
    val amount: String? = null,
    val unit: String? = null,
    val isOptional: Boolean? = false,
    val group: String? = null
)

@JsonClass(generateAdapter = true)
data class ParsedStepDto(
    val stepNumber: Int? = null,
    val instructionGerman: String? = null,
    val instructionEnglish: String? = null,
    val timerMinutes: Int? = null,
    val tip: String? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun parseRecipeWithAi(
        imageBitmap: Bitmap?,
        recipeText: String?
    ): Result<ParsedRecipeDto> {
        val bitmaps = if (imageBitmap != null) listOf(imageBitmap) else emptyList()
        return parseRecipeWithAi(bitmaps, recipeText)
    }

    suspend fun parseRecipeWithAi(
        imageBitmaps: List<Bitmap>,
        recipeText: String?
    ): Result<ParsedRecipeDto> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback to intelligent offline parser
            return@withContext Result.success(OfflineRecipeParser.parse(recipeText ?: ""))
        }

        try {
            val systemPrompt = """
                You are an expert master chef, archivist, and culinary linguist specializing in German, Austrian, and European heirloom recipes and handwritten vintage cookbooks.

                TASK:
                Analyze the provided recipe card photo(s) (which may span across multiple pages/images) and/or text (such as handwritten German or vintage heirloom recipes).
                Intelligently transcribe and translate the entire recipe directly and completely into clear, beautiful, 100% ENGLISH.

                CRITICAL MULTI-PAGE & TRANSLATION INSTRUCTIONS:
                1. TRANSLATION TO ENGLISH:
                   - If the source recipe is in German (or any other language), translate all titles, ingredients, instructions, tips, and notes into natural, fluent English.
                   - Ensure no German words remain untranslated in the English fields (e.g. translate 'Mehl' to 'All-Purpose Flour', 'Zucker' to 'Granulated Sugar', 'Butter' to 'Butter', 'Eier' to 'Eggs', 'Sahne' to 'Heavy Cream').

                2. MULTI-PAGE SYNTHESIS:
                   - If multiple images or pages are provided (Page 1, Page 2, Page 3, Card front/back), synthesize and combine ALL pages into ONE single, cohesive recipe.
                   - Ensure nothing is lost between pages.

                3. 100% INGREDIENT & MEASUREMENT FIDELITY:
                   - Scrutinize all handwritten notes, lists, and margin scribbles.
                   - Transcribe ALL ingredients with fractional amounts, ranges, and additions (e.g. '1/4 - 1/2 tsp sugar', 'a pinch of salt', '1/2 tsp vanilla').
                   - For each ingredient, provide:
                     * nameEnglish (e.g. "Granulated Sugar", "All-Purpose Flour")
                     * amount (e.g. "1/4 - 1/2", "250", "1", "1/2")
                     * unit (e.g. "spoon", "tsp", "tbsp", "g", "ml", "cup", "pinch")
                     * group (e.g. "Main Ingredients", "Dough", "Filling", "Topping")

                4. NUMBERED STEPS & DIRECTIONS:
                   - Clearly transcribe and preserve all numbered steps: 1, 2, 3, 4, etc.
                   - Never merge distinct steps into one wall of text.
                   - Provide complete, clear English instructions for every step.
                   - Estimate timerMinutes if a duration is specified (e.g., 35 minutes bake time = 35).

                OUTPUT FORMAT: Return ONLY valid JSON adhering strictly to this schema:
                {
                  "titleEnglish": "Grandma's Traditional Apple Cake",
                  "category": "Baking & Desserts",
                  "servings": "4-6 servings",
                  "prepTimeMinutes": 20,
                  "cookTimeMinutes": 35,
                  "difficulty": "Medium",
                  "ingredients": [
                    {
                      "nameEnglish": "Granulated Sugar",
                      "amount": "1/4 - 1/2",
                      "unit": "spoon",
                      "isOptional": false,
                      "group": "Dough"
                    }
                  ],
                  "steps": [
                    {
                      "stepNumber": 1,
                      "instructionEnglish": "Cream butter with sugar and eggs until light and fluffy.",
                      "timerMinutes": 5,
                      "tip": "Room temperature ingredients blend smoothest."
                    }
                  ],
                  "notesEnglish": "Transcribed from Grandma's handwritten recipe notebook.",
                  "detectedSourceLanguage": "de"
                }
            """.trimIndent()

            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = systemPrompt))

            if (!recipeText.isNullOrBlank()) {
                parts.add(GeminiPart(text = "Recipe Text Content:\n$recipeText"))
            }

            imageBitmaps.forEachIndexed { index, bitmap ->
                val base64Image = bitmapToBase64(bitmap)
                if (base64Image.isNotBlank()) {
                    parts.add(GeminiPart(text = "Page ${index + 1} Image:"))
                    parts.add(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = base64Image
                            )
                        )
                    )
                }
            }

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GeminiGenerationConfig()
            )

            val response = api.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (jsonText.isNullOrBlank()) {
                return@withContext Result.success(OfflineRecipeParser.parse(recipeText ?: ""))
            }

            // Clean json response if wrapped in ```json ... ```
            val cleanedJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val adapter = moshi.adapter(ParsedRecipeDto::class.java)
            val parsed = adapter.fromJson(cleanedJson) ?: OfflineRecipeParser.parse(recipeText ?: "")
            Result.success(parsed)
        } catch (e: Throwable) {
            Log.e("GeminiRecipeService", "Error parsing with Gemini API: ${e.message}", e)
            Result.success(OfflineRecipeParser.parse(recipeText ?: ""))
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        return try {
            val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE || !bitmap.isMutable) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
            val stream = ByteArrayOutputStream()
            safeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (t: Throwable) {
            Log.e("GeminiRecipeService", "Error encoding bitmap to Base64", t)
            ""
        }
    }
}
