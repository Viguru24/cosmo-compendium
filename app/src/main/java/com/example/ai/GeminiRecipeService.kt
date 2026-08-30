package com.example.ai

import android.graphics.BitmapFactory
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
    val foodPhotoBox: FoodPhotoBoxDto? = null
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

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
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
            if (!recipeText.isNullOrBlank()) {
                return@withContext Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                return@withContext Result.failure(
                    Exception("Gemini API key is required to scan images. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel.")
                )
            }
        }

        try {
            val systemPrompt = """
                You are an expert master chef, archivist, and culinary linguist specializing in vintage recipe cards, printed recipes, and handwritten heirloom cookbooks.

                TASK:
                Analyze the provided recipe card photo(s) (which may span across multiple pages/images) and/or text.
                Accurately transcribe and translate the entire recipe directly and completely into clear, structured JSON.

                CRITICAL INSTRUCTIONS:
                1. STRICT FIDELITY & COMPLETE INGREDIENT EXTRACTION (MANDATORY):
                   - Extract EVERY SINGLE INGREDIENT mentioned, listed, or implied in the recipe card or text.
                   - Do NOT skip any ingredient (extract all flours, sugars, eggs, butter, spices, extracts, fruits, nuts, leavening agents, liquids, etc.).
                   - For each ingredient, capture:
                     * "amount": exact quantity/fraction (e.g. "2 1/4", "1/2", "3/4", "3", "1", "6")
                     * "unit": unit of measure (e.g. "cups", "tablespoons", "teaspoons", "oz", "package", "cloves", "pinch", or "" if none)
                     * "nameEnglish": clear descriptive ingredient name (e.g. "All-purpose Flour", "Craisins Dried Cranberries", "Eggs", "Almond Extract")
                     * "nameGerman": German name if source is German or German card
                     * "group": section header if grouped (e.g. "Dough", "Filling", "Glaze", "Topping", "Dry Ingredients") or null
                   - Transcribe all brand names, package sizes, and prep notes faithfully (e.g., '6-ounce package Craisins', '3/4 cup sliced almonds', '3 large eggs, beaten').

                2. UNLIMITED MULTI-PAGE SYNTHESIS & TRANSLATION:
                   - If source is in German or another language, translate titles, ingredients, and steps into natural English.
                   - If multiple card photos or scrapbook/notebook pages are provided (Page 1, Page 2, Page 3, Page 4, etc.), synthesize all pages comprehensively into ONE single unified recipe.
                   - Ingredients and directions distributed across multiple pages/sides must all be combined seamlessly in proper chronological order without dropping any items.

                3. ACCURATE COOK TIME, PREP TIME, YIELD & DIFFICULTY:
                   - Look for prep time and cook time. If multiple baking/cooking periods are specified (e.g. 'bake for 30 minutes', then 'bake for an additional 20 minutes'), sum them up (50 min total cook time).
                   - Look for yield / portions (e.g., 'Makes about 2 1/2 dozen', '4 servings').
                   - Determine difficulty ('Easy', 'Medium', 'Advanced').

                4. NUMBERED STEPS & DIRECTIONS:
                   - Transcribe all steps in clean sequence (Step 1, Step 2, Step 3...).
                   - Set timerMinutes if a time is specified for that step.

                5. DISH PHOTOGRAPH DETECTION (CRITICAL):
                   - Check if any provided photo contains an actual picture/photo of the cooked food/dish (e.g. baked pie, cake, roasted meat, cookies, sauce jar).
                   - A recipe card containing only text, handwriting, paper texture, or drawings of spoons has NO food photo ("hasFoodPhoto": false).
                   - ONLY set "hasFoodPhoto": true if there is an actual photograph of the prepared food. If present, set "foodPhotoBox" with coordinates [ymin, xmin, ymax, xmax] in 0..1000 scale and "pageIndex" (0 for page 1, 1 for page 2).

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
                  "foodPhotoBox": null
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
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    topP = 0.95f,
                    topK = 40,
                    responseMimeType = "application/json"
                )
            )

            val response = try {
                api.generateContent(model = "gemini-2.5-flash", apiKey = apiKey, request = request)
            } catch (firstErr: Throwable) {
                Log.w("GeminiRecipeService", "Primary model gemini-2.5-flash failed, trying gemini-flash-latest: ${firstErr.message}")
                try {
                    api.generateContent(model = "gemini-flash-latest", apiKey = apiKey, request = request)
                } catch (secondErr: Throwable) {
                    Log.w("GeminiRecipeService", "Secondary model gemini-flash-latest failed, trying gemini-3.1-flash-lite-preview: ${secondErr.message}")
                    api.generateContent(model = "gemini-3.1-flash-lite-preview", apiKey = apiKey, request = request)
                }
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
                Log.w("GeminiRecipeService", "Moshi failed: ${t.message}, trying lenient parser")
                null
            }

            val parsed = if (parsedFromMoshi != null && !parsedFromMoshi.ingredients.isNullOrEmpty()) {
                parsedFromMoshi
            } else {
                parseLenientJson(cleanedJson) ?: parsedFromMoshi
            }

            if (parsed != null) {
                Result.success(parsed)
            } else if (!recipeText.isNullOrBlank()) {
                Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                Result.failure(Exception("Could not parse AI response JSON."))
            }
        } catch (e: Throwable) {
            val errorMsg = if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("GeminiRecipeService", "Gemini HTTP error ${e.code()}: $errorBody", e)
                "Gemini AI error (${e.code()}): ${errorBody ?: e.message()}"
            } else {
                Log.e("GeminiRecipeService", "Error parsing with Gemini API: ${e.message}", e)
                e.localizedMessage ?: "Failed to process recipe image with AI"
            }
            if (!recipeText.isNullOrBlank()) {
                Result.success(OfflineRecipeParser.parse(recipeText))
            } else {
                Result.failure(Exception(errorMsg))
            }
        }
    }

    private fun parseLenientJson(jsonStr: String): ParsedRecipeDto? {
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
            Log.w("GeminiRecipeService", "Lenient JSON parse failed: ${t.message}")
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
            Log.e("GeminiRecipeService", "Error encoding bitmap to Base64", t)
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
        referenceBitmap: Bitmap? = null
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
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
                    append("Inspect the attached reference image carefully. Accurately capture the dish's visual characteristics, colors, textures, crust, garnishes, and plating style, elevating it into a clean, professionally lit heirloom cookbook cover photograph.")
                } else {
                    append("Render the dish served fresh and beautifully plated in a warm, rustic kitchen setting with soft natural window lighting, gentle steam, appetizing texture, shallow depth of field, 4k culinary studio detail. No watermarks or overlaid text.")
                }
            }

            // First try Imagen 3 endpoint via raw OkHttpClient
            val imagenPayload = JSONObject().apply {
                put("instances", JSONArray().apply {
                    put(JSONObject().put("prompt", promptText))
                })
                put("parameters", JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", "1:1")
                })
            }

            val imagenRequest = okhttp3.Request.Builder()
                .url("${BASE_URL}v1beta/models/imagen-3.0-generate-002:predict?key=$apiKey")
                .post(imagenPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            var generatedBitmap: Bitmap? = null

            try {
                okHttpClient.newCall(imagenRequest).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        val json = JSONObject(bodyStr)
                        val preds = json.optJSONArray("predictions")
                        if (preds != null && preds.length() > 0) {
                            val pred = preds.getJSONObject(0)
                            val b64 = pred.optString("bytesBase64Encoded")
                            if (b64.isNotBlank()) {
                                val bytes = Base64.decode(b64, Base64.DEFAULT)
                                generatedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("GeminiRecipeService", "Imagen 3 generation call failed: ${e.message}")
            }

            if (generatedBitmap != null) {
                return@withContext Result.success(generatedBitmap!!)
            }

            // Fallback: Gemini multimodal generateContent with responseModalities
            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = promptText))

            if (referenceBitmap != null) {
                val b64 = bitmapToBase64(referenceBitmap)
                if (b64.isNotBlank()) {
                    parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = b64)))
                }
            }

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GeminiGenerationConfig(
                    responseModalities = listOf("IMAGE", "TEXT"),
                    imageConfig = GeminiImageConfig(aspectRatio = "1:1", imageSize = "1K")
                )
            )

            // Try supported image generation models
            val response = try {
                api.generateContent(model = "gemini-2.0-flash-exp", apiKey = apiKey, request = request)
            } catch (err1: Throwable) {
                Log.w("GeminiRecipeService", "gemini-2.0-flash-exp failed: ${err1.message}, trying gemini-2.0-flash")
                api.generateContent(model = "gemini-2.0-flash", apiKey = apiKey, request = request)
            }

            val candidates = response.candidates
            val allParts = candidates?.flatMap { it.content?.parts ?: emptyList() } ?: emptyList()
            val imagePart = allParts.firstOrNull { it.inlineData != null && it.inlineData.data.isNotBlank() }

            val base64Data = imagePart?.inlineData?.data

            if (!base64Data.isNullOrBlank()) {
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    Result.success(bitmap)
                } else {
                    Result.failure(Exception("Failed to decode generated image."))
                }
            } else {
                val returnedText = allParts.mapNotNull { it.text }.joinToString(" ")
                val err = if (returnedText.isNotBlank()) {
                    "Model response: $returnedText"
                } else {
                    "No image data returned by AI model."
                }
                Result.failure(Exception(err))
            }
        } catch (t: Throwable) {
            val errorMsg = if (t is retrofit2.HttpException) {
                val errorBody = t.response()?.errorBody()?.string()
                Log.e("GeminiRecipeService", "Gemini HTTP error ${t.code()}: $errorBody", t)
                "Gemini AI error (${t.code()}): ${errorBody ?: t.message()}"
            } else {
                Log.e("GeminiRecipeService", "Error generating recipe cover image: ${t.message}", t)
                t.localizedMessage ?: "Failed to generate recipe cover image"
            }
            Result.failure(Exception(errorMsg))
        }
    }
}
