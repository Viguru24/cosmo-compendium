package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object VideoRecipeExtractor {
    private const val TAG = "VideoRecipeExtractor"

    suspend fun extractRecipeFromVideoUri(
        context: Context,
        videoUri: Uri,
        targetProfile: String = "Wife"
    ): Result<RecipeEntity> = withContext(Dispatchers.IO) {
        val timedResult = withTimeoutOrNull(45000) {
            val retriever = MediaMetadataRetriever()
            try {
                AppLogger.i(TAG, "Starting video recipe extraction from URI:  (Target profile: )")
                retriever.setDataSource(context, videoUri)

                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 15000L
                val durationSec = durationMs / 1000
                AppLogger.i(TAG, "Video duration: s ( ms)")

                // Sample 6 to 8 keyframe timestamps across video
                val samplePoints = listOf(0.08f, 0.22f, 0.36f, 0.50f, 0.65f, 0.80f, 0.94f)
                val keyframes = mutableListOf<Bitmap>()

                for (point in samplePoints) {
                    val timeUs = (durationMs * point * 1000).toLong()
                    try {
                        val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                        if (frame != null) {
                            val scaled = downscaleFrame(frame)
                            keyframes.add(scaled)
                        }
                    } catch (fe: Throwable) {
                        AppLogger.w(TAG, "Failed extracting frame at %: ")
                    }
                }

                if (keyframes.isEmpty()) {
                    AppLogger.e(TAG, "Could not extract any video frames from ")
                    return@withTimeoutOrNull Result.failure<RecipeEntity>(Exception("Could not read frames from this video. Ensure it is a valid MP4 or recording."))
                }

                AppLogger.i(TAG, "Successfully extracted  keyframes from video. Sending to Gemini Multimodal Vision...")

                val videoPrompt = """
                    You are an expert chef analyzing keyframe photographs extracted in chronological order from a cooking video (Instagram Reel / TikTok / cooking tutorial).
                    
                    INSTRUCTIONS:
                    1. Study all frames carefully: identify the raw ingredients on the counter, prep techniques, cooking processes in the pan/pot/oven, and the final plated dish.
                    2. Extract EVERY single ingredient shown or used with realistic culinary quantities and units.
                    3. Write clear, detailed, numbered step-by-step instructions based on the cooking actions shown in the video.
                    4. Identify the recipe title, category, estimated prep/cook times, and difficulty.
                    5. Return complete structured recipe JSON.
                """.trimIndent()

                val parsedDtoResult = UniversalAiRecipeService.parseRecipe(keyframes, videoPrompt)
                val dto = if (parsedDtoResult.isSuccess) {
                    val res = parsedDtoResult.getOrThrow()
                    AppLogger.i(TAG, "AI parsed video recipe: '${res.titleEnglish ?: res.title}'")
                    res
                } else {
                    val err = parsedDtoResult.exceptionOrNull()
                    AppLogger.e(TAG, "AI video parsing failed: ${err?.message}", err)
                    return@withTimeoutOrNull Result.failure<RecipeEntity>(Exception("AI could not extract recipe from video frames: ${err?.localizedMessage}"))
                }

                val title = dto.titleEnglish ?: dto.title ?: dto.name ?: "Video Recipe"

                // Save last keyframe (plated finished dish) as recipe cover photo
                val bestCoverFrame = keyframes.lastOrNull() ?: keyframes.first()
                val coverPhoto = saveBitmapToPhotos(context, bestCoverFrame)

                val ingredientsList = (dto.ingredients ?: emptyList()).map { ingDto ->
                    RecipeIngredient(
                        name = ingDto.nameEnglish ?: ingDto.name ?: "Ingredient",
                        amount = ingDto.amount ?: "",
                        unit = ingDto.unit ?: "",
                        nameGerman = ingDto.nameGerman,
                        nameEnglish = ingDto.nameEnglish,
                        isOptional = ingDto.isOptional ?: false,
                        group = ingDto.group
                    )
                }

                val stepsList = (dto.steps ?: emptyList()).mapIndexed { idx, stepDto ->
                    RecipeStep(
                        stepNumber = stepDto.stepNumber ?: (idx + 1),
                        instructionEnglish = stepDto.instructionEnglish ?: stepDto.instruction ?: "",
                        instructionGerman = stepDto.instructionGerman ?: stepDto.instruction ?: "",
                        timerMinutes = stepDto.timerMinutes ?: 0,
                        tip = stepDto.tip
                    )
                }

                val recipe = RecipeEntity(
                    id = 0,
                    title = title,
                    titleGerman = dto.titleGerman ?: title,
                    titleEnglish = title,
                    category = dto.category ?: "Video Recipes",
                    servings = dto.servings ?: "4 servings",
                    prepTimeMinutes = dto.prepTimeMinutes ?: 15,
                    cookTimeMinutes = dto.cookTimeMinutes ?: 25,
                    difficulty = dto.difficulty ?: "Medium",
                    ingredients = ingredientsList,
                    steps = stepsList,
                    notes = if (!dto.notesEnglish.isNullOrBlank()) dto.notesEnglish else "Extracted from video clip (s)",
                    notesGerman = dto.notesGerman ?: "",
                    sourceLanguage = dto.detectedSourceLanguage ?: "en",
                    coverTheme = "VINTAGE_LEATHER",
                    imageUri = coverPhoto.first,
                    coverPhotoName = coverPhoto.second,
                    profileName = targetProfile,
                    isFavorite = false,
                    rating = 5,
                    timesCooked = 0,
                    originStory = "Recipe extracted with AI from video clip",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false,
                    syncStatus = "PENDING"
                )

                AppLogger.i(TAG, "Video recipe extraction complete: '' with  ingredients.")
                Result.success(recipe)
            } catch (t: Throwable) {
                AppLogger.e(TAG, "Error extracting recipe from video URI: ", t)
                Result.failure(t)
            } finally {
                try {
                    retriever.release()
                } catch (ignored: Throwable) {}
            }
        }

        timedResult ?: Result.failure(Exception("Video recipe extraction timed out. Please try a shorter video clip (under 2 minutes)."))
    }

    private fun downscaleFrame(bitmap: Bitmap): Bitmap {
        val maxDim = 1024
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val scale = maxDim.toFloat() / maxOf(w, h)
        val newW = (w * scale).toInt()
        val newH = (h * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    private fun saveBitmapToPhotos(context: Context, bitmap: Bitmap): Pair<String, String> {
        val photosDir = File(context.filesDir, "recipes_photos")
        photosDir.mkdirs()
        val filename = "video_cover_.jpg"
        val file = File(photosDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return Pair(file.absolutePath, filename)
    }
}
