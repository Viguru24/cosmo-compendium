package com.example.ai

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.util.AppLogger
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.data.network.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.json.JSONArray
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

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

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private var customApiKey: String? = null
    private var customModel: String? = null

    fun sanitizeApiKey(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var key = raw.trim()
        // Strip zero-width & invisible whitespace characters
        key = key.replace("\u200B", "")
            .replace("\u200C", "")
            .replace("\u200D", "")
            .replace("\uFEFF", "")
            .replace("\u00A0", " ")
            .trim()

        // Strip common script/export prefixes if pasted from terminal or environment
        val prefixes = listOf(
            "export GEMINI_API_KEY=",
            "export GOOGLE_API_KEY=",
            "GEMINI_API_KEY=",
            "GOOGLE_API_KEY=",
            "API_KEY=",
            "key=",
            "Bearer ",
            "token="
        )
        for (p in prefixes) {
            if (key.startsWith(p, ignoreCase = true)) {
                key = key.substring(p.length).trim()
            }
        }

        // Loop to strip outer quotes, backticks, or escaped quotes
        var prev = ""
        while (prev != key) {
            prev = key
            key = key.trim('\'', '"', '`', ';', ',', ' ', '\t', '\n', '\r')
            if (key.startsWith("\\\"") && key.endsWith("\\\"") && key.length >= 4) {
                key = key.substring(2, key.length - 2).trim()
            }
            if (key.startsWith("\\'") && key.endsWith("\\'") && key.length >= 4) {
                key = key.substring(2, key.length - 2).trim()
            }
        }
        return key
    }

    fun setCustomApiKey(key: String?) {
        val cleaned = sanitizeApiKey(key)
        customApiKey = cleaned.takeIf { it.isNotBlank() }
    }

    fun setCustomModel(model: String?) {
        customModel = model?.trim()?.takeIf { it.isNotBlank() && it != "auto" }
    }

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank()) return custom
        val buildKey = sanitizeApiKey(BuildConfig.GEMINI_API_KEY)
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") return buildKey
        return ""
    }

    private var discoveredLiveModels: List<String> = emptyList()
    private var lastDiscoveryTimestamp: Long = 0L
    private const val DISCOVERY_TTL_MS = 12 * 60 * 60 * 1000L // 12 hours
    val retiredModels: MutableSet<String> = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    suspend fun fetchLiveModels(apiKey: String, forceRefresh: Boolean = false): List<String> {
        val now = System.currentTimeMillis()
        if (!forceRefresh && discoveredLiveModels.isNotEmpty() && (now - lastDiscoveryTimestamp < DISCOVERY_TTL_MS)) {
            return discoveredLiveModels.filterNot { retiredModels.contains(it) }
        }
        return try {
            val response = api.listModels(apiKey, apiKey)
            val list = response.models?.mapNotNull { item ->
                val name = item.name?.removePrefix("models/") ?: return@mapNotNull null
                val methods = item.supportedGenerationMethods ?: emptyList()
                if (methods.contains("generateContent")) name else null
            } ?: emptyList()

            // Prioritize flash models, then version sorted
            val sorted = list.filterNot { retiredModels.contains(it) }
                .sortedWith(
                    compareByDescending<String> { it.contains("flash", ignoreCase = true) }
                        .thenByDescending { it.contains("latest", ignoreCase = true) }
                        .thenByDescending { it }
                )
            if (sorted.isNotEmpty()) {
                discoveredLiveModels = sorted
                lastDiscoveryTimestamp = now
                AppLogger.i("GeminiRecipeService", "Dynamically discovered ${sorted.size} live Gemini models: ${sorted.take(4)}")
            }
            sorted
        } catch (e: Throwable) {
            AppLogger.w("GeminiRecipeService", "Live model discovery query failed: ${e.message}, using fallback pool.")
            emptyList()
        }
    }

    fun markModelRetired(modelName: String) {
        retiredModels.add(modelName)
        discoveredLiveModels = discoveredLiveModels.filterNot { it.equals(modelName, ignoreCase = true) }
        AppLogger.w("GeminiRecipeService", "Auto-blacklisted retired model: $modelName. Active pool: ${getEffectiveModels().take(3)}")
    }

    fun getEffectiveModels(): List<String> {
        val specific = customModel?.takeIf { !retiredModels.contains(it) }
        val live = discoveredLiveModels.filterNot { retiredModels.contains(it) }
        val staticActive = GeminiModelConfig.ACTIVE_MODELS.filterNot { retiredModels.contains(it) }

        val combined = buildList {
            if (!specific.isNullOrBlank()) add(specific)
            addAll(live)
            addAll(staticActive)
            add(GeminiModelConfig.PRIMARY_MODEL)
            add(GeminiModelConfig.FALLBACK_MODEL)
            add(GeminiModelConfig.LEGACY_FALLBACK_MODEL)
        }.distinct().filterNot { retiredModels.contains(it) }

        return combined.ifEmpty { listOf("gemini-2.5-flash", "gemini-3.5-flash", "gemini-flash-latest") }
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = NetworkModule.okHttpClient.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
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
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            if (!recipeText.isNullOrBlank()) {
                return@withContext Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                return@withContext Result.failure(
                    Exception("Google Gemini API key is missing. Please enter your Gemini API key in App Settings -> AI Engine.")
                )
            }
        }

        try {
            val systemPrompt = """
                You are an expert master chef, archivist, and formulation specialist specializing in recipe cards, craft formulas (such as soap making, balms, cosmetics, and household preparations), printed recipes, and handwritten compendiums.

                TASK:
                Analyze the provided recipe or formula card photo(s) and/or text. Accurately transcribe and translate the entire entry into clear, structured JSON.

                CRITICAL INSTRUCTIONS:
                1. STRICT FIDELITY & COMPLETE INGREDIENT EXTRACTION:
                   - Extract EVERY SINGLE INGREDIENT or component mentioned, listed, or implied.
                   - Do NOT skip any ingredient (extract all flours, sugars, oils, lye, scents, eggs, butter, spices, extracts, fruits, nuts, leavening agents, liquids, etc.).
                   - For each ingredient, capture:
                     * "amount": exact quantity/fraction (e.g. "2 1/4", "1/2", "3/4", "3", "1", "6")
                     * "unit": unit of measure (e.g. "cups", "tablespoons", "teaspoons", "oz", "g", "ml", "package", "cloves", "pinch", or "" if none)
                     * "nameEnglish": clear descriptive ingredient name (e.g. "All-purpose Flour", "Olive Oil", "Lye / Sodium Hydroxide", "Lavender Essential Oil")
                     * "group": sub-recipe group if present (e.g. "Dough", "Filling", "Oil Phase", "Lye Solution", "Crust", "Glaze", "Topping"), otherwise null
                     * "isOptional": boolean indicating if ingredient is marked optional/variations
                   - When ingredients include brand names or specific descriptors, keep them intact (e.g. "6-ounce package Ocean Spray Craisins").
                   - DUAL MEASUREMENTS & CLEAN INGREDIENT NAMES:
                     * When recipe cards list dual units (e.g. "600g / 1.3lbs Chicken Thighs", "50g / 1/3 cup Plain Flour", "120ml / 1/2 cup Veg Oil", "200g / 7oz Cashew Nuts", or "1 tbsp + 2 tsp Soy Sauce"), capture the primary measurement in "amount" and "unit" (e.g. amount: "600", unit: "g").
                     * NEVER include secondary measurements, slashes, or plus signs in "nameEnglish" or "nameGerman" (e.g. write "boneless skinless Chicken Thighs, diced into bite-sized pieces", "Plain Flour", "Light Soy Sauce", NOT "/ 1.3lbs Chicken Thighs" or "+ 2 tsp Soy Sauce").

                2. UNIVERSAL MULTI-LANGUAGE SUPPORT & GLOBAL TRANSLATION:
                   - The recipe card or text may be in ANY language worldwide (e.g., German, French, Italian, Spanish, Portuguese, Polish, Russian, Ukrainian, Dutch, Swedish, Danish, Japanese, Chinese, Arabic, Hindi, Greek, etc., or English), including vintage cursive or handwritten notes (such as German Kurrent/Sütterlin).
                   - Accurately preserve and transcribe the original language names into the original fields: "nameGerman" (stores original ingredient name), "titleGerman" (stores original title), "instructionGerman" (stores original instruction step), "notesGerman" (stores original notes).
                   - Accurately translate everything into natural, idiomatic, clear culinary English in "nameEnglish", "titleEnglish", "instructionEnglish", "notesEnglish".
                   - If the recipe is already in English:
                     * Populate the English and original fields identically with the English text.

                3. STEP TRANSCRIPTION:
                   - Provide clear, numbered steps in sequential order.
                   - If steps contain baking or boiling times, extract the duration in minutes into "timerMinutes" (e.g. "bake for 30 minutes" -> 30).
                   - If a step contains a chef tip or special note, include it in "tip".

                4. METADATA:
                   - Estimate "prepTimeMinutes" and "cookTimeMinutes" if not explicitly stated.
                   - Categorize into one of: "Baking & Desserts", "Main Dishes", "Salads & Starters", "Soups & Stews", "Sauces & Condiments", "Beverages", "Snacks & Appetizers".
                   - Set "difficulty" to "Easy", "Medium", or "Hard".
                   - Set "detectedSourceLanguage" to the 2-letter ISO 639-1 language code (e.g. "en", "de", "fr", "it", "es", "pl", "ru", "uk", "sv", "nl", "ja", "zh", "el", etc.).

                5. DISH PHOTOGRAPH DETECTION (CRITICAL):
                   - Check if any provided photo contains an actual picture/photo of the cooked food/dish (e.g. baked pie, cake, roasted meat, cookies, sauce jar).
                   - A recipe card containing only text, handwriting, paper texture, or drawings of spoons has NO food photo ("hasFoodPhoto": false).
                   - ONLY set "hasFoodPhoto": true if there is an actual photograph of the prepared food. If present, set "foodPhotoBox" with coordinates [ymin, xmin, ymax, xmax] in 0..1000 scale and "pageIndex" (0 for page 1, 1 for page 2).

                6. ORIENTATION & UPRIGHT REALIGNMENT:
                   - Check the natural reading orientation of the recipe text in the image.
                   - If the card was photographed sideways or upside down (common when shooting top-down on a counter), determine how many degrees CLOCKWISE the image must be rotated to make the text upright: 0 (already upright), 90, 180, or 270.
                   - Set "cardRotationNeededDegrees": 0 | 90 | 180 | 270.

                OUTPUT FORMAT: Return ONLY valid JSON adhering strictly to this schema:
                {
                  "titleEnglish": "Cranberry Almond Biscotti",
                  "category": "Baking & Desserts",
                  "servings": "Makes about 2 1/2 dozen",
                  "prepTimeMinutes": 20,
                  "cookTimeMinutes": 50,
                  "difficulty": "Medium",
                  "ingredients": [
                    {
                      "nameEnglish": "Flour",
                      "amount": "2 1/4",
                      "unit": "cups",
                      "isOptional": false,
                      "group": "Dry Ingredients"
                    }
                  ],
                  "steps": [
                    {
                      "stepNumber": 1,
                      "instructionEnglish": "Preheat oven to 325°F (165°C). Combine dry ingredients in a medium mixing bowl...",
                      "timerMinutes": 30,
                      "tip": null
                    }
                  ],
                  "notesEnglish": "Recipe card transcription.",
                  "detectedSourceLanguage": "en",
                  "hasFoodPhoto": false,
                  "foodPhotoBox": null,
                  "cardRotationNeededDegrees": 0
                }
            """.trimIndent()

            val parts = mutableListOf<GeminiPart>()
            val promptContent = if (!recipeText.isNullOrBlank()) {
                "$systemPrompt\n\nRecipe Text:\n$recipeText"
            } else {
                systemPrompt
            }
            parts.add(GeminiPart(text = promptContent))

            for (bitmap in imageBitmaps) {
                val b64 = bitmapToBase64(bitmap)
                if (b64.isNotBlank()) {
                    parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = b64)))
                }
            }

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    topP = 0.95f,
                    topK = 40,
                    responseMimeType = "application/json"
                )
            )

            // Fetch live active models dynamically if cache is empty
            if (discoveredLiveModels.isEmpty()) {
                try {
                    fetchLiveModels(apiKey, forceRefresh = false)
                } catch (_: Throwable) {}
            }

            val modelsToTry = getEffectiveModels()
            var lastException: Throwable? = null
            var response: GeminiResponse? = null

            for (modelName in modelsToTry) {
                try {
                    AppLogger.i("GeminiRecipeService", "Invoking Gemini model: $modelName (API Key length: ${apiKey.length})")
                    response = api.generateContent(
                        model = modelName,
                        apiKeyHeader = apiKey,
                        apiKey = apiKey,
                        request = request
                    )
                    if (response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text != null) {
                        AppLogger.i("GeminiRecipeService", "Successfully received recipe generation from $modelName")
                        break
                    }
                } catch (err: Throwable) {
                    lastException = err
                    val errMsg = err.message ?: ""
                    val isRetiredOr404 = (err is retrofit2.HttpException && err.code() == 404) ||
                            errMsg.contains("no longer available", ignoreCase = true) ||
                            errMsg.contains("404", ignoreCase = true)

                    if (isRetiredOr404) {
                        markModelRetired(modelName)
                        AppLogger.w("GeminiRecipeService", "Model $modelName is retired or 404 on Google API. Auto-evicted and trying next live model.")
                    } else {
                        AppLogger.w("GeminiRecipeService", "Model $modelName failed: ${err.message}", err)
                    }
                }
            }

            if (response == null) {
                if (!recipeText.isNullOrBlank()) {
                    AppLogger.i("GeminiRecipeService", "Gemini API unavailable (${lastException?.message}), falling back to OfflineRecipeParser for recipe text.")
                    return@withContext Result.success(OfflineRecipeParser.parse(recipeText))
                }
                val errorMsg = lastException?.message ?: "AI scanning service unavailable."
                AppLogger.e("GeminiRecipeService", "All Gemini models failed: $errorMsg", lastException)
                if (errorMsg.contains("503") || errorMsg.contains("high demand", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("Google AI is currently under high load. Please tap 'Retry Scan'."))
                }
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (jsonText.isNullOrBlank()) {
                if (!recipeText.isNullOrBlank()) {
                    return@withContext Result.success(OfflineRecipeParser.parse(recipeText))
                } else {
                    return@withContext Result.failure(Exception("AI did not return recipe content for the image."))
                }
            }

            // Clean json response if wrapped in ```json ... ```
            val cleanedJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val adapter = moshi.adapter(ParsedRecipeDto::class.java)
            val parsedFromMoshi = try {
                adapter.fromJson(cleanedJson)
            } catch (t: Throwable) {
                AppLogger.w("GeminiRecipeService", "Moshi failed: ${t.message}, trying lenient parser")
                null
            }

            val parsed = if (parsedFromMoshi != null && !parsedFromMoshi.ingredients.isNullOrEmpty()) {
                parsedFromMoshi
            } else {
                parseLenientJson(cleanedJson) ?: parsedFromMoshi
            }

            val sanitized = parsed?.copy(
                ingredients = parsed.ingredients?.map { ing ->
                    val cleanEn = ing.nameEnglish?.let { com.example.data.model.RecipeIngredient.cleanIngredientName(it) }
                    val cleanDe = ing.nameGerman?.let { com.example.data.model.RecipeIngredient.cleanIngredientName(it) }
                    val cleanName = ing.name?.let { com.example.data.model.RecipeIngredient.cleanIngredientName(it) }
                    ing.copy(
                        nameEnglish = cleanEn ?: cleanName,
                        nameGerman = cleanDe ?: cleanName,
                        name = cleanName ?: cleanEn ?: ""
                    )
                }
            )

            if (sanitized != null) {
                Result.success(sanitized)
            } else if (!recipeText.isNullOrBlank()) {
                Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                Result.failure(Exception("Could not parse AI response JSON."))
            }
        } catch (e: Throwable) {
            val errorMsg = if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                AppLogger.e("GeminiRecipeService", "Gemini HTTP error ${e.code()}: $errorBody", e)
                "Gemini AI error (${e.code()}): ${errorBody ?: e.message()}"
            } else {
                AppLogger.e("GeminiRecipeService", "Error parsing with Gemini API: ${e.message}", e)
                e.localizedMessage ?: "Failed to process recipe image with AI"
            }
            if (!recipeText.isNullOrBlank()) {
                Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                Result.failure(Exception(errorMsg))
            }
        }
    }

    fun parseLenientJson(jsonStr: String): ParsedRecipeDto? {
        return try {
            val root = org.json.JSONObject(jsonStr)
            val titleEnglish = root.optString("titleEnglish", "").ifBlank {
                root.optString("title", "").ifBlank {
                    root.optString("name", "")
                }
            }.takeIf { it.isNotBlank() }

            val titleGerman = root.optString("titleGerman", "").takeIf { it.isNotBlank() }
            val category = root.optString("category", "").takeIf { it.isNotBlank() }
            val servings = root.optString("servings", "").takeIf { it.isNotBlank() }
            val prepTimeMinutes = if (root.has("prepTimeMinutes")) root.optInt("prepTimeMinutes") else null
            val cookTimeMinutes = if (root.has("cookTimeMinutes")) root.optInt("cookTimeMinutes") else null
            val difficulty = root.optString("difficulty", "").takeIf { it.isNotBlank() }
            val notesEnglish = root.optString("notesEnglish", "").ifBlank { root.optString("notes", "") }.takeIf { it.isNotBlank() }
            val notesGerman = root.optString("notesGerman", "").takeIf { it.isNotBlank() }
            val detectedSourceLanguage = root.optString("detectedSourceLanguage", "en")
            val hasFoodPhoto = root.optBoolean("hasFoodPhoto", false)

            var foodPhotoBox: FoodPhotoBoxDto? = null
            if (root.has("foodPhotoBox") && !root.isNull("foodPhotoBox")) {
                val boxObj = root.optJSONObject("foodPhotoBox")
                if (boxObj != null) {
                    foodPhotoBox = FoodPhotoBoxDto(
                        ymin = boxObj.optInt("ymin"),
                        xmin = boxObj.optInt("xmin"),
                        ymax = boxObj.optInt("ymax"),
                        xmax = boxObj.optInt("xmax"),
                        pageIndex = boxObj.optInt("pageIndex", 0)
                    )
                }
            }

            val ingredientsList = mutableListOf<ParsedIngredientDto>()
            val ingArray = root.optJSONArray("ingredients")
            if (ingArray != null) {
                for (i in 0 until ingArray.length()) {
                    val item = ingArray.opt(i)
                    if (item is org.json.JSONObject) {
                        val nameEn = item.optString("nameEnglish", "").ifBlank {
                            item.optString("name", "").ifBlank {
                                item.optString("ingredient", "").ifBlank {
                                    item.optString("item", "")
                                }
                            }
                        }
                        val nameDe = item.optString("nameGerman", "")
                        val amount = item.optString("amount", "").ifBlank {
                            item.optString("quantity", "").ifBlank {
                                item.optString("qty", "")
                            }
                        }
                        val unit = item.optString("unit", "").ifBlank {
                            item.optString("measurement", "").ifBlank {
                                item.optString("measure", "")
                            }
                        }
                        val group = item.optString("group", "").takeIf { it.isNotBlank() }
                        val isOptional = item.optBoolean("isOptional", false)

                        if (nameEn.isNotBlank() || amount.isNotBlank() || unit.isNotBlank()) {
                            ingredientsList.add(
                                ParsedIngredientDto(
                                    nameGerman = nameDe.takeIf { it.isNotBlank() } ?: nameEn,
                                    nameEnglish = nameEn.takeIf { it.isNotBlank() } ?: nameDe,
                                    name = nameEn.takeIf { it.isNotBlank() } ?: nameDe,
                                    amount = amount,
                                    unit = unit,
                                    group = group,
                                    isOptional = isOptional
                                )
                            )
                        }
                    } else if (item is String && item.isNotBlank()) {
                        ingredientsList.add(OfflineRecipeParser.parseIngredientLine(item, isSourceGerman = detectedSourceLanguage == "de"))
                    }
                }
            }

            val stepsList = mutableListOf<ParsedStepDto>()
            val stepsArray = root.optJSONArray("steps") ?: root.optJSONArray("instructions") ?: root.optJSONArray("directions")
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    val item = stepsArray.opt(i)
                    if (item is org.json.JSONObject) {
                        val instEn = item.optString("instructionEnglish", "").ifBlank {
                            item.optString("instruction", "").ifBlank {
                                item.optString("step", "").ifBlank {
                                    item.optString("text", "").ifBlank {
                                        item.optString("description", "")
                                    }
                                }
                            }
                        }
                        val instDe = item.optString("instructionGerman", "")
                        val stepNum = item.optInt("stepNumber", i + 1)
                        val timerMin = item.optInt("timerMinutes", 0)
                        val tip = item.optString("tip", "").takeIf { it.isNotBlank() }

                        if (instEn.isNotBlank() || instDe.isNotBlank()) {
                            stepsList.add(
                                ParsedStepDto(
                                    stepNumber = stepNum,
                                    instructionGerman = instDe.takeIf { it.isNotBlank() } ?: instEn,
                                    instructionEnglish = instEn.takeIf { it.isNotBlank() } ?: instDe,
                                    timerMinutes = timerMin,
                                    tip = tip
                                )
                            )
                        }
                    } else if (item is String && item.isNotBlank()) {
                        stepsList.add(
                            ParsedStepDto(
                                stepNumber = i + 1,
                                instructionGerman = item,
                                instructionEnglish = item
                            )
                        )
                    }
                }
            }

            ParsedRecipeDto(
                titleGerman = titleGerman,
                titleEnglish = titleEnglish,
                category = category,
                servings = servings,
                prepTimeMinutes = prepTimeMinutes,
                cookTimeMinutes = cookTimeMinutes,
                difficulty = difficulty,
                ingredients = ingredientsList,
                steps = stepsList,
                notesGerman = notesGerman,
                notesEnglish = notesEnglish,
                detectedSourceLanguage = detectedSourceLanguage,
                hasFoodPhoto = hasFoodPhoto,
                foodPhotoBox = foodPhotoBox
            )
        } catch (t: Throwable) {
            AppLogger.w("GeminiRecipeService", "Lenient JSON parse failed: ${t.message}")
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        return try {
            val maxDim = 1280
            val width = bitmap.width
            val height = bitmap.height
            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val scale = maxDim.toFloat() / maxOf(width, height)
                Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                bitmap
            }
            val safeBitmap = if (scaledBitmap.config == Bitmap.Config.HARDWARE || !scaledBitmap.isMutable) {
                scaledBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: scaledBitmap
            } else {
                scaledBitmap
            }
            val stream = ByteArrayOutputStream()
            safeBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val byteArray = stream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (t: Throwable) {
            AppLogger.e("GeminiRecipeService", "Error encoding bitmap to Base64", t)
            ""
        }
    }

    suspend fun generateRecipeCoverImage(
        title: String,
        titleGerman: String? = null,
        category: String = "Main Dish",
        ingredients: List<String> = emptyList(),
        steps: List<String> = emptyList(),
        notes: String? = null,
        referenceBitmap: Bitmap? = null,
        customPrompt: String? = null
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is required to generate AI dish photos. Please configure GEMINI_API_KEY in the Secrets panel."))
        }

        try {
            val names = buildList {
                add(title)
                if (!titleGerman.isNullOrBlank() && !titleGerman.equals(title, ignoreCase = true)) {
                    add(titleGerman)
                }
            }.joinToString(" / ")

            val ingSummary = if (ingredients.isNotEmpty()) {
                "Key ingredients include: " + ingredients.take(8).joinToString(", ") + "."
            } else ""

            val stepsSummary = if (steps.isNotEmpty()) {
                "Preparation technique: " + steps.take(3).joinToString(" ") + "."
            } else ""

            val promptText = buildString {
                append("Create a stunning, mouth-watering, gourmet food photography portrait of the completed dish: \"$names\" ($category). ")
                if (ingSummary.isNotBlank()) append("$ingSummary ")
                if (stepsSummary.isNotBlank()) append("$stepsSummary ")
                if (!notes.isNullOrBlank()) append("Culinary style: $notes. ")
                if (referenceBitmap != null) {
                    append("Inspect the attached reference image carefully. Accurately capture the subject's visual characteristics, colors, textures, crust, garnishes, and style, elevating it into a clean, professionally lit compendium cover photograph.")
                } else {
                    append("Render the dish served fresh and beautifully plated in a warm, rustic kitchen setting with soft natural window lighting, gentle steam, appetizing texture, shallow depth of field, 4k culinary studio detail. No watermarks or overlaid text.")
                }
            }

            // Try Google Imagen 3 models in sequence
            val imagenModels = listOf(
                "imagen-3.0-generate-002",
                "imagen-3.0-fast-generate-001",
                "imagen-3.0-generate-001"
            )

            val imagenPayload = JSONObject().apply {
                put("instances", JSONArray().apply {
                    put(JSONObject().put("prompt", promptText))
                })
                put("parameters", JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", "1:1")
                    put("outputMimeType", "image/jpeg")
                })
            }

            var generatedBitmap: Bitmap? = null
            var lastImagenError = ""

            for (modelName in imagenModels) {
                try {
                    val imagenRequest = okhttp3.Request.Builder()
                        .url("${BASE_URL}v1beta/models/$modelName:predict?key=$apiKey")
                        .post(imagenPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()

                    okHttpClient.newCall(imagenRequest).execute().use { resp ->
                        val bodyStr = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(bodyStr)
                            val preds = json.optJSONArray("predictions")
                            if (preds != null && preds.length() > 0) {
                                val pred = preds.getJSONObject(0)
                                val b64 = pred.optString("bytesBase64Encoded")
                                if (b64.isNotBlank()) {
                                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                                    generatedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    if (generatedBitmap != null) {
                                        AppLogger.i("GeminiRecipeService", "Successfully generated image with $modelName")
                                        return@withContext Result.success(generatedBitmap!!)
                                    }
                                }
                            }
                        } else {
                            AppLogger.w("GeminiRecipeService", "$modelName returned HTTP ${resp.code}: $bodyStr")
                            lastImagenError = "Google Imagen ($modelName) error (${resp.code}): ${bodyStr.ifBlank { resp.message }}"
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.w("GeminiRecipeService", "Failed request to $modelName: ${e.message}")
                    if (lastImagenError.isBlank()) {
                        lastImagenError = e.localizedMessage ?: "Failed to connect to Google Imagen endpoint"
                    }
                }
            }

            if (generatedBitmap != null) {
                Result.success(generatedBitmap!!)
            } else {
                val finalErr = if (lastImagenError.isNotBlank()) lastImagenError else "Google Imagen 3 did not return image data."
                Result.failure(Exception(finalErr))
            }
        } catch (t: Throwable) {
            AppLogger.e("GeminiRecipeService", "Error in generateRecipeCoverImage: ${t.message}", t)
            Result.failure(Exception(t.localizedMessage ?: "Failed to generate recipe cover image"))
        }
    }
    suspend fun askSousChefAssistant(question: String): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            AppLogger.w("GeminiClient", "No effective Gemini API key available for Sous-Chef.")
            return@withContext ""
        }
        try {
            val systemPrompt = """
                You are the warm, highly skilled, and friendly culinary & crafting assistant in the Cosmo Compendium app.
                Give a concise, helpful, and clear answer (2 to 4 sentences maximum) to the user's cooking, baking, crafting (such as soap making or balms), substitution, or project question.
                Be practical, encouraging, and accurate with weights, measurements, ratios, and temperatures.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = "$systemPrompt\n\nUser Question: $question")
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.5f)
            )

            for (modelName in getEffectiveModels()) {
                try {
                    val response = api.generateContent(
                        model = modelName,
                        apiKeyHeader = apiKey,
                        apiKey = apiKey,
                        request = request
                    )
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        return@withContext text.trim()
                    }
                } catch (err: Throwable) {
                    AppLogger.w("GeminiRecipeService", "SousChef model $modelName failed: ${err.message}, trying next fallback...")
                }
            }
            return@withContext ""
        } catch (e: Exception) {
            AppLogger.e("GeminiClient", "Error asking SousChef assistant: ${e.message}", e)
            return@withContext ""
        }
    }

    suspend fun testApiKeyDetailed(apiKeyToTest: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val rawKey = apiKeyToTest ?: getEffectiveApiKey()
        val key = sanitizeApiKey(rawKey)
        if (key.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is blank. Please paste your Google AI Studio API key."))
        }
        try {
            // Force refresh live models on manual test connection
            try {
                fetchLiveModels(key, forceRefresh = true)
            } catch (_: Throwable) {}

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = "Hello! Please reply with 'OK' to verify API connection.")
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.1f)
            )

            var lastError: Throwable? = null
            var successModel: String? = null
            var successText: String? = null

            for (modelName in getEffectiveModels()) {
                try {
                    val response = api.generateContent(
                        model = modelName,
                        apiKeyHeader = key,
                        apiKey = key,
                        request = request
                    )
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        successModel = modelName
                        successText = text.trim()
                        break
                    }
                } catch (err: Throwable) {
                    lastError = err
                    val errMsg = err.message ?: ""
                    val isRetiredOr404 = (err is retrofit2.HttpException && err.code() == 404) ||
                            errMsg.contains("no longer available", ignoreCase = true) ||
                            errMsg.contains("404", ignoreCase = true)

                    if (isRetiredOr404) {
                        markModelRetired(modelName)
                    }
                    AppLogger.w("GeminiRecipeService", "Test probe with $modelName failed: ${err.message}")
                }
            }

            if (successModel != null) {
                Result.success("✓ Connected ($successModel)")
            } else {
                val errorMsg = if (lastError is retrofit2.HttpException) {
                    val errorBody = lastError.response()?.errorBody()?.string()
                    AppLogger.e("GeminiRecipeService", "Gemini HTTP error ${lastError.code()}: $errorBody", lastError)
                    "HTTP ${lastError.code()}: ${errorBody ?: lastError.message()}"
                } else {
                    AppLogger.e("GeminiRecipeService", "Gemini connection error: ${lastError?.message}", lastError)
                    lastError?.localizedMessage ?: "Failed to connect to Google Gemini"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to connect to Google Gemini"))
        }
    }
}

typealias GeminiRecipeService = GeminiClient
