package com.example.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import com.example.data.network.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class ImageGenEngine(val displayName: String, val description: String) {
    GEMINI("Gemini AI (Cloud)", "Uses Google Gemini Flash vision model"),
    COMFY_UI("ComfyUI (Local PC)", "Directly connects to your local or remote ComfyUI server")
}

object ComfyUiClient {
    private const val TAG = "ComfyUiClient"

    private val httpClient = NetworkModule.okHttpClient.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Sanitizes the user configured URL (e.g. "192.168.1.100:8188" -> "http://192.168.1.100:8188")
     */
    fun normalizeBaseUrl(input: String): String {
        var trimmed = input.trim().removeSuffix("/")
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        return trimmed
    }

    /**
     * Fetches the list of installed checkpoint models from ComfyUI.
     */
    suspend fun fetchAvailableCheckpoints(baseUrl: String): List<String> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<String>()
        val base = normalizeBaseUrl(baseUrl)
        
        // Fetch standard Checkpoints
        try {
            val request = Request.Builder().url("$base/object_info/CheckpointLoaderSimple").get().build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val namesArr = json.optJSONObject("CheckpointLoaderSimple")
                        ?.optJSONObject("input")?.optJSONObject("required")
                        ?.optJSONArray("ckpt_name")?.optJSONArray(0)
                    if (namesArr != null) {
                        for (i in 0 until namesArr.length()) {
                            val name = namesArr.getString(i)
                            if (!name.lowercase().contains("audio") && !resultList.contains(name)) {
                                resultList.add(name)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.d(TAG, "Could not fetch CheckpointLoaderSimple models: ${e.message}")
        }

        // Fetch UNET models (e.g. z_image_turbo_bf16.safetensors)
        try {
            val request = Request.Builder().url("$base/object_info/UNETLoader").get().build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val namesArr = json.optJSONObject("UNETLoader")
                        ?.optJSONObject("input")?.optJSONObject("required")
                        ?.optJSONArray("unet_name")?.optJSONArray(0)
                    if (namesArr != null) {
                        for (i in 0 until namesArr.length()) {
                            val name = namesArr.getString(i)
                            if ((name.contains("z_image", ignoreCase = true) || name.contains("turbo", ignoreCase = true)) && !resultList.contains(name)) {
                                resultList.add(name)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.d(TAG, "Could not fetch UNETLoader models: ${e.message}")
        }

        return@withContext resultList
    }

    /**
     * Tests connectivity to the ComfyUI server by checking the /system_stats or /object_info endpoint.
     */
    suspend fun testConnection(baseUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val normalized = normalizeBaseUrl(baseUrl)
            val models = fetchAvailableCheckpoints(normalized)
            val url = "$normalized/system_stats"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    if (models.isNotEmpty()) {
                        val modelSummary = if (models.size <= 3) models.joinToString(", ") else "${models.take(2).joinToString(", ")} (+${models.size - 2} more)"
                        Result.success("Connected to ComfyUI! Available models: $modelSummary")
                    } else {
                        Result.success("Connected to ComfyUI successfully!")
                    }
                } else {
                    // Try root /
                    val rootRequest = Request.Builder().url(normalized).get().build()
                    httpClient.newCall(rootRequest).execute().use { rootResp ->
                        if (rootResp.isSuccessful) {
                            Result.success("Connected to ComfyUI successfully!")
                        } else {
                            Result.failure(Exception("ComfyUI responded with HTTP ${response.code}"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to connect to ComfyUI at $baseUrl: ${e.message}", e)
            Result.failure(Exception("Cannot reach ComfyUI at $baseUrl. Ensure ComfyUI is running with --listen and your phone is on the same network. Error: ${e.localizedMessage}"))
        }
    }

    /**
     * Uploads an image bitmap to ComfyUI's /upload/image endpoint.
     * Returns the uploaded filename string.
     */
    private fun uploadImage(baseUrl: String, bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        val filename = "recipe_ref_${UUID.randomUUID().toString().take(8)}.jpg"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                filename,
                byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .addFormDataPart("overwrite", "true")
            .build()

        val request = Request.Builder()
            .url("${normalizeBaseUrl(baseUrl)}/upload/image")
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to upload reference photo to ComfyUI (HTTP ${response.code})")
            }
            val respStr = response.body?.string() ?: ""
            val json = JSONObject(respStr)
            return json.optString("name", filename)
        }
    }

    /**
     * Injects prompt text, random seed, checkpoint model, and optional reference image into either a user-provided
     * custom API JSON workflow or the standard default API workflow.
     */
            fun buildPromptWorkflow(
        positivePrompt: String,
        negativePrompt: String,
        uploadedImageName: String? = null,
        ckptName: String = "z_image_turbo_bf16.safetensors",
        customWorkflowJson: String? = null,
        width: Int = 768,
        height: Int = 768
    ): JSONObject {
        val clientId = "android-cookbook-" + UUID.randomUUID().toString().take(8)
        val seed = (Math.abs(java.util.Random().nextLong()) % 9000000000000000L) + 1000000000L

        // If custom workflow JSON is provided and valid, inject prompt, seed, and ckpt
        if (!customWorkflowJson.isNullOrBlank()) {
            try {
                val parsed = JSONObject(customWorkflowJson.trim())
                val promptObj = if (parsed.has("prompt") && parsed.optJSONObject("prompt") != null) {
                    parsed.getJSONObject("prompt")
                } else {
                    parsed
                }

                var foundPositiveClip = false
                val keys = promptObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val node = promptObj.optJSONObject(key) ?: continue
                    val classType = node.optString("class_type", "")
                    val inputs = node.optJSONObject("inputs") ?: continue

                    when {
                        classType == "KSampler" || classType == "KSamplerAdvanced" -> {
                            inputs.put("seed", seed)
                            if (inputs.has("noise_seed")) inputs.put("noise_seed", seed)
                        }
                        classType == "CheckpointLoaderSimple" -> {
                            if (ckptName.isNotBlank() && ckptName != "default") {
                                inputs.put("ckpt_name", ckptName.trim())
                            }
                        }
                        classType == "UNETLoader" -> {
                            if (ckptName.isNotBlank() && ckptName != "default") {
                                inputs.put("unet_name", ckptName.trim())
                            }
                        }
                        classType == "CLIPTextEncode" -> {
                            if (!foundPositiveClip) {
                                inputs.put("text", positivePrompt)
                                foundPositiveClip = true
                            } else {
                                val currentText = inputs.optString("text", "")
                                if (currentText.isBlank() || currentText.contains("ugly") || currentText.contains("watermark") || currentText.contains("blur") || currentText.contains("bad")) {
                                    inputs.put("text", negativePrompt)
                                }
                            }
                        }
                        classType == "LoadImage" && uploadedImageName != null -> {
                            inputs.put("image", uploadedImageName)
                        }
                    }
                }

                val req = JSONObject()
                req.put("prompt", promptObj)
                req.put("client_id", clientId)
                return req
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to parse custom ComfyUI workflow JSON, falling back to default: ${e.message}")
            }
        }

        val promptObj = JSONObject()
        val isZImage = ckptName.contains("z_image", ignoreCase = true) || ckptName.contains("z-image", ignoreCase = true) || ckptName.contains("turbo", ignoreCase = true) && !ckptName.contains("sd_xl", ignoreCase = true)

        if (isZImage) {
            // Official Z-Image-Turbo ComfyUI Workflow Template
            val unetLoader = JSONObject().apply {
                put("class_type", "UNETLoader")
                put("inputs", JSONObject().apply {
                    put("unet_name", ckptName)
                    put("weight_dtype", "default")
                })
            }
            val clipLoader = JSONObject().apply {
                put("class_type", "CLIPLoader")
                put("inputs", JSONObject().apply {
                    put("clip_name", "qwen_3_4b_fp8_mixed.safetensors")
                    put("type", "lumina2")
                })
            }
            val vaeLoader = JSONObject().apply {
                put("class_type", "VAELoader")
                put("inputs", JSONObject().put("vae_name", "ae.safetensors"))
            }
            val posClip = JSONObject().apply {
                put("class_type", "CLIPTextEncode")
                put("inputs", JSONObject().apply {
                    put("text", positivePrompt)
                    put("clip", JSONArray().put("2").put(0))
                })
            }
            val zeroOut = JSONObject().apply {
                put("class_type", "ConditioningZeroOut")
                put("inputs", JSONObject().apply {
                    put("conditioning", JSONArray().put("4").put(0))
                })
            }
            val auraFlow = JSONObject().apply {
                put("class_type", "ModelSamplingAuraFlow")
                put("inputs", JSONObject().apply {
                    put("model", JSONArray().put("1").put(0))
                    put("shift", 3.0)
                })
            }

            if (uploadedImageName != null) {
                val loadImg = JSONObject().apply {
                    put("class_type", "LoadImage")
                    put("inputs", JSONObject().put("image", uploadedImageName))
                }
                val vaeEncode = JSONObject().apply {
                    put("class_type", "VAEEncode")
                    put("inputs", JSONObject().apply {
                        put("pixels", JSONArray().put("7").put(0))
                        put("vae", JSONArray().put("3").put(0))
                    })
                }
                val sampler = JSONObject().apply {
                    put("class_type", "KSampler")
                    put("inputs", JSONObject().apply {
                        put("seed", seed)
                        put("steps", 8)
                        put("cfg", 1.0)
                        put("sampler_name", "res_multistep")
                        put("scheduler", "simple")
                        put("denoise", 0.50)
                        put("model", JSONArray().put("6").put(0))
                        put("positive", JSONArray().put("4").put(0))
                        put("negative", JSONArray().put("5").put(0))
                        put("latent_image", JSONArray().put("8").put(0))
                    })
                }
                val vaeDecode = JSONObject().apply {
                    put("class_type", "VAEDecode")
                    put("inputs", JSONObject().apply {
                        put("samples", JSONArray().put("9").put(0))
                        put("vae", JSONArray().put("3").put(0))
                    })
                }
                val saveImage = JSONObject().apply {
                    put("class_type", "SaveImage")
                    put("inputs", JSONObject().apply {
                        put("filename_prefix", "z-image-turbo")
                        put("images", JSONArray().put("10").put(0))
                    })
                }

                promptObj.put("1", unetLoader)
                promptObj.put("2", clipLoader)
                promptObj.put("3", vaeLoader)
                promptObj.put("4", posClip)
                promptObj.put("5", zeroOut)
                promptObj.put("6", auraFlow)
                promptObj.put("7", loadImg)
                promptObj.put("8", vaeEncode)
                promptObj.put("9", sampler)
                promptObj.put("10", vaeDecode)
                promptObj.put("11", saveImage)
            } else {
                val emptyLatent = JSONObject().apply {
                    put("class_type", "EmptySD3LatentImage")
                    put("inputs", JSONObject().apply {
                        put("width", width)
                        put("height", height)
                        put("batch_size", 1)
                    })
                }
                val sampler = JSONObject().apply {
                    put("class_type", "KSampler")
                    put("inputs", JSONObject().apply {
                        put("seed", seed)
                        put("steps", 8)
                        put("cfg", 1.0)
                        put("sampler_name", "res_multistep")
                        put("scheduler", "simple")
                        put("denoise", 1.0)
                        put("model", JSONArray().put("6").put(0))
                        put("positive", JSONArray().put("4").put(0))
                        put("negative", JSONArray().put("5").put(0))
                        put("latent_image", JSONArray().put("7").put(0))
                    })
                }
                val vaeDecode = JSONObject().apply {
                    put("class_type", "VAEDecode")
                    put("inputs", JSONObject().apply {
                        put("samples", JSONArray().put("8").put(0))
                        put("vae", JSONArray().put("3").put(0))
                    })
                }
                val saveImage = JSONObject().apply {
                    put("class_type", "SaveImage")
                    put("inputs", JSONObject().apply {
                        put("filename_prefix", "z-image-turbo")
                        put("images", JSONArray().put("9").put(0))
                    })
                }

                promptObj.put("1", unetLoader)
                promptObj.put("2", clipLoader)
                promptObj.put("3", vaeLoader)
                promptObj.put("4", posClip)
                promptObj.put("5", zeroOut)
                promptObj.put("6", auraFlow)
                promptObj.put("7", emptyLatent)
                promptObj.put("8", sampler)
                promptObj.put("9", vaeDecode)
                promptObj.put("10", saveImage)
            }
        } else {
            // Standard CheckpointLoaderSimple (SDXL Turbo, SD1.5, DreamLay)
            if (uploadedImageName != null) {
                val ckptLoader = JSONObject().apply {
                    put("class_type", "CheckpointLoaderSimple")
                    put("inputs", JSONObject().put("ckpt_name", ckptName))
                }
                val loadImg = JSONObject().apply {
                    put("class_type", "LoadImage")
                    put("inputs", JSONObject().put("image", uploadedImageName))
                }
                val vaeEncode = JSONObject().apply {
                    put("class_type", "VAEEncode")
                    put("inputs", JSONObject().apply {
                        put("pixels", JSONArray().put("2").put(0))
                        put("vae", JSONArray().put("1").put(2))
                    })
                }
                val posClip = JSONObject().apply {
                    put("class_type", "CLIPTextEncode")
                    put("inputs", JSONObject().apply {
                        put("text", positivePrompt)
                        put("clip", JSONArray().put("1").put(1))
                    })
                }
                val negClip = JSONObject().apply {
                    put("class_type", "CLIPTextEncode")
                    put("inputs", JSONObject().apply {
                        put("text", negativePrompt)
                        put("clip", JSONArray().put("1").put(1))
                    })
                }
                val isTurbo = ckptName.contains("turbo", ignoreCase = true)
                val sampler = JSONObject().apply {
                    put("class_type", "KSampler")
                    put("inputs", JSONObject().apply {
                        put("seed", seed)
                        put("steps", if (isTurbo) 6 else 20)
                        put("cfg", if (isTurbo) 2.0 else 7.0)
                        put("sampler_name", "euler")
                        put("scheduler", "normal")
                        put("denoise", if (isTurbo) 0.50 else 0.65)
                        put("model", JSONArray().put("1").put(0))
                        put("positive", JSONArray().put("4").put(0))
                        put("negative", JSONArray().put("5").put(0))
                        put("latent_image", JSONArray().put("3").put(0))
                    })
                }
                val vaeDecode = JSONObject().apply {
                    put("class_type", "VAEDecode")
                    put("inputs", JSONObject().apply {
                        put("samples", JSONArray().put("6").put(0))
                        put("vae", JSONArray().put("1").put(2))
                    })
                }
                val saveImage = JSONObject().apply {
                    put("class_type", "SaveImage")
                    put("inputs", JSONObject().apply {
                        put("filename_prefix", "AndroidCookbook")
                        put("images", JSONArray().put("7").put(0))
                    })
                }

                promptObj.put("1", ckptLoader)
                promptObj.put("2", loadImg)
                promptObj.put("3", vaeEncode)
                promptObj.put("4", posClip)
                promptObj.put("5", negClip)
                promptObj.put("6", sampler)
                promptObj.put("7", vaeDecode)
                promptObj.put("8", saveImage)
            } else {
                val ckptLoader = JSONObject().apply {
                    put("class_type", "CheckpointLoaderSimple")
                    put("inputs", JSONObject().put("ckpt_name", ckptName))
                }
                val emptyLatent = JSONObject().apply {
                    put("class_type", "EmptyLatentImage")
                    put("inputs", JSONObject().apply {
                        put("width", width)
                        put("height", height)
                        put("batch_size", 1)
                    })
                }
                val posClip = JSONObject().apply {
                    put("class_type", "CLIPTextEncode")
                    put("inputs", JSONObject().apply {
                        put("text", positivePrompt)
                        put("clip", JSONArray().put("1").put(1))
                    })
                }
                val negClip = JSONObject().apply {
                    put("class_type", "CLIPTextEncode")
                    put("inputs", JSONObject().apply {
                        put("text", negativePrompt)
                        put("clip", JSONArray().put("1").put(1))
                    })
                }
                val isTurbo = ckptName.contains("turbo", ignoreCase = true)
                val sampler = JSONObject().apply {
                    put("class_type", "KSampler")
                    put("inputs", JSONObject().apply {
                        put("seed", seed)
                        put("steps", if (isTurbo) 6 else 20)
                        put("cfg", if (isTurbo) 2.0 else 7.0)
                        put("sampler_name", "euler")
                        put("scheduler", "normal")
                        put("denoise", 1.0)
                        put("model", JSONArray().put("1").put(0))
                        put("positive", JSONArray().put("3").put(0))
                        put("negative", JSONArray().put("4").put(0))
                        put("latent_image", JSONArray().put("2").put(0))
                    })
                }
                val vaeDecode = JSONObject().apply {
                    put("class_type", "VAEDecode")
                    put("inputs", JSONObject().apply {
                        put("samples", JSONArray().put("5").put(0))
                        put("vae", JSONArray().put("1").put(2))
                    })
                }
                val saveImage = JSONObject().apply {
                    put("class_type", "SaveImage")
                    put("inputs", JSONObject().apply {
                        put("filename_prefix", "AndroidCookbook")
                        put("images", JSONArray().put("6").put(0))
                    })
                }

                promptObj.put("1", ckptLoader)
                promptObj.put("2", emptyLatent)
                promptObj.put("3", posClip)
                promptObj.put("4", negClip)
                promptObj.put("5", sampler)
                promptObj.put("6", vaeDecode)
                promptObj.put("7", saveImage)
            }
        }

        val requestJson = JSONObject()
        requestJson.put("prompt", promptObj)
        requestJson.put("client_id", clientId)
        return requestJson
    }

    /**
     * Executes generation on ComfyUI and polls until the image is returned.
     */
    suspend fun generateRecipeImage(
        baseUrl: String,
        title: String,
        titleGerman: String? = null,
        category: String,
        ingredients: List<String>,
        steps: List<String>,
        notes: String? = null,
        referenceBitmap: Bitmap? = null,
        customCheckPoint: String? = null,
        customWorkflowJson: String? = null,
        customPrompt: String? = null
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val normalizedUrl = normalizeBaseUrl(baseUrl)
            var uploadedImageName: String? = null

            if (referenceBitmap != null) {
                try {
                    uploadedImageName = uploadImage(normalizedUrl, referenceBitmap)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to upload reference photo to ComfyUI, proceeding with text-to-image: ${e.message}")
                }
            }

            val dishName = buildList {
                add(title)
                if (!titleGerman.isNullOrBlank() && !title.equals(titleGerman, ignoreCase = true)) {
                    add(titleGerman)
                }
            }.joinToString(" / ")

            val ingSummary = if (ingredients.isNotEmpty()) {
                "made with " + ingredients.take(6).joinToString(", ")
            } else ""

            val posPrompt = SmartPromptBuilder.buildSmartCulinaryPrompt(
                title = title,
                titleGerman = titleGerman,
                category = category,
                ingredients = ingredients,
                steps = steps,
                notes = notes,
                customPrompt = customPrompt
            )
            val negPrompt = "8k, 4k, text, watermark, logo, badge, stamp, emblem, label, words, letters, typography, signature, banner, frame, border, blurry, cartoon, illustration, 3d render, plastic, fake, distorted, low quality, unappetizing, ugly, oversaturated, deformed, out of frame, pixelated, noisy, lowres"

            // Auto-resolve checkpoint against server if available
            val availableModels = fetchAvailableCheckpoints(normalizedUrl)
            var ckpt = if (!customCheckPoint.isNullOrBlank()) customCheckPoint.trim() else ""

            if (availableModels.isNotEmpty()) {
                if (ckpt.isBlank() || ckpt.equals("default", ignoreCase = true) || !availableModels.contains(ckpt)) {
                    // Try to find a fuzzy match first
                    val fuzzyMatch = if (ckpt.isNotBlank()) availableModels.find { it.contains(ckpt, ignoreCase = true) || ckpt.contains(it, ignoreCase = true) } else null
                    if (fuzzyMatch != null) {
                        ckpt = fuzzyMatch
                    } else {
                        // Pick the best available image checkpoint from the server (preferring sdxl, turbo, lightning, sd, or first non-audio model)
                        val bestModel = availableModels.firstOrNull { it.contains("sd_xl_turbo", ignoreCase = true) }
                            ?: availableModels.firstOrNull { it.contains("turbo", ignoreCase = true) }
                            ?: availableModels.firstOrNull { it.contains("sd_xl", ignoreCase = true) || it.contains("sdxl", ignoreCase = true) }
                            ?: availableModels.firstOrNull { it.contains("v1-5", ignoreCase = true) }
                            ?: availableModels.firstOrNull { !it.lowercase().contains("audio") && !it.lowercase().contains("ltx") && !it.lowercase().contains("qwen") }
                            ?: availableModels.first()

                        AppLogger.i(TAG, "Configured checkpoint '$customCheckPoint' not found on ComfyUI server. Auto-selected available model: '$bestModel' from server models: $availableModels")
                        ckpt = bestModel
                    }
                }
            } else {
                if (ckpt.isBlank()) {
                    ckpt = "v1-5-pruned-emaonly.safetensors"
                }
            }

            val isSdxlOrTurbo = ckpt.contains("xl", ignoreCase = true) || ckpt.contains("turbo", ignoreCase = true) || ckpt.contains("lightning", ignoreCase = true) || ckpt.contains("qwen", ignoreCase = true)
            val imgWidth = if (isSdxlOrTurbo) 1024 else 1024
            val imgHeight = if (isSdxlOrTurbo) 1024 else 1024

            val payload = buildPromptWorkflow(
                positivePrompt = posPrompt,
                negativePrompt = negPrompt,
                uploadedImageName = uploadedImageName,
                ckptName = ckpt,
                customWorkflowJson = customWorkflowJson,
                width = imgWidth,
                height = imgHeight
            )

            // Submit prompt
            val promptRequest = Request.Builder()
                .url("$normalizedUrl/prompt")
                .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val promptId = httpClient.newCall(promptRequest).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    var errorDetail = ""
                    try {
                        val errJson = JSONObject(body)
                        val errorObj = errJson.optJSONObject("error")
                        if (errorObj != null) {
                            val msg = errorObj.optString("message")
                            val details = errorObj.optString("details")
                            val excType = errorObj.optString("type")
                            errorDetail = listOf(excType, msg, details).filter { it.isNotBlank() }.joinToString(" - ")
                        }
                        val nodeErrors = errJson.optJSONObject("node_errors")
                        if (nodeErrors != null && nodeErrors.length() > 0) {
                            val nodeDetailsList = mutableListOf<String>()
                            val nodeKeys = nodeErrors.keys()
                            while (nodeKeys.hasNext()) {
                                val nodeKey = nodeKeys.next()
                                val nodeInfo = nodeErrors.getJSONObject(nodeKey)
                                val nodeClass = nodeInfo.optString("class_type", "Node $nodeKey")
                                val errorsArray = nodeInfo.optJSONArray("errors")
                                val errList = mutableListOf<String>()
                                if (errorsArray != null) {
                                    for (i in 0 until errorsArray.length()) {
                                        val errObj = errorsArray.optJSONObject(i)
                                        if (errObj != null) {
                                            val m = errObj.optString("message", "")
                                            val d = errObj.optString("details", "")
                                            val extra = errObj.optString("extra_info", "")
                                            errList.add(listOf(m, d, extra).filter { it.isNotBlank() }.joinToString(" "))
                                        } else {
                                            errList.add(errorsArray.getString(i))
                                        }
                                    }
                                }
                                nodeDetailsList.add("[$nodeClass (#$nodeKey): ${errList.joinToString("; ")}]")
                            }
                            if (nodeDetailsList.isNotEmpty()) {
                                errorDetail = if (errorDetail.isNotBlank()) "$errorDetail | " + nodeDetailsList.joinToString(" ") else nodeDetailsList.joinToString(" ")
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.d(TAG, "Could not parse JSON error body: ${e.message}")
                    }
                    if (errorDetail.isBlank()) {
                        errorDetail = body.ifBlank { "HTTP ${response.code} ${response.message}" }
                    }
                    throw Exception("ComfyUI server returned HTTP ${response.code}: $errorDetail")
                }
                val respJson = JSONObject(body)
                respJson.getString("prompt_id")
            }

            AppLogger.d(TAG, "Submitted prompt to ComfyUI, prompt_id: $promptId")

            // Poll /history/{prompt_id} until outputs are ready (timeout after 90 seconds)
            val startTime = System.currentTimeMillis()
            var imageFilename: String? = null
            var subfolder: String = ""
            var type: String = "output"

            while (System.currentTimeMillis() - startTime < 90_000) {
                delay(1500)

                val historyRequest = Request.Builder()
                    .url("$normalizedUrl/history/$promptId")
                    .get()
                    .build()

                httpClient.newCall(historyRequest).execute().use { histResp ->
                    if (histResp.isSuccessful) {
                        val histBody = histResp.body?.string() ?: "{}"
                        val histJson = JSONObject(histBody)
                        if (histJson.has(promptId)) {
                            val item = histJson.getJSONObject(promptId)
                            val outputs = item.optJSONObject("outputs")
                            if (outputs != null && outputs.length() > 0) {
                                // Find any node with images array
                                val keys = outputs.keys()
                                while (keys.hasNext()) {
                                    val nodeKey = keys.next()
                                    val nodeOutput = outputs.getJSONObject(nodeKey)
                                    val imagesArray = nodeOutput.optJSONArray("images")
                                    if (imagesArray != null && imagesArray.length() > 0) {
                                        val firstImg = imagesArray.getJSONObject(0)
                                        imageFilename = firstImg.getString("filename")
                                        subfolder = firstImg.optString("subfolder", "")
                                        type = firstImg.optString("type", "output")
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                if (imageFilename != null) break
            }

            if (imageFilename == null) {
                return@withContext Result.failure(Exception("ComfyUI generation timed out after 90 seconds. Check ComfyUI terminal console for queue progress or missing checkpoint models."))
            }

            // Fetch the generated image from /view
            val viewUrl = "$normalizedUrl/view?filename=$imageFilename&subfolder=$subfolder&type=$type"
            val viewRequest = Request.Builder().url(viewUrl).get().build()

            httpClient.newCall(viewRequest).execute().use { viewResp ->
                if (!viewResp.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to download generated image from ComfyUI /view (HTTP ${viewResp.code})"))
                }
                val bytes = viewResp.body?.bytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        Result.success(bitmap)
                    } else {
                        Result.failure(Exception("Could not decode image bytes from ComfyUI."))
                    }
                } else {
                    Result.failure(Exception("Empty image data received from ComfyUI."))
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "ComfyUI generation failed: ${e.message}", e)
            Result.failure(Exception("ComfyUI error: ${e.localizedMessage}"))
        }
    }
}
