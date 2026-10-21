package com.example.data.web

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.ai.GeminiClient
import com.example.ai.OfflineRecipeParser
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import com.example.data.network.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import java.util.regex.Matcher

object UrlRecipeExtractor {
    private const val TAG = "UrlRecipeExtractor"

    private const val SOCIAL_BOT_USER_AGENT =
        "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"

    private const val BROWSER_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    private val httpClient = NetworkModule.okHttpClient.newBuilder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Extracts a full RecipeEntity from any web URL, including Instagram, TikTok, YouTube,
     * Facebook, Pinterest, and traditional food blogs/recipe websites.
     */
    suspend fun extractRecipeFromUrl(
        context: Context,
        urlStr: String,
        targetProfile: String = "Wife"
    ): Result<RecipeEntity> = withContext(Dispatchers.IO) {
        val timedResult = withTimeoutOrNull(25000) {
            try {
                val cleanUrl = cleanUrlString(urlStr)
                AppLogger.i(TAG, "Starting URL recipe extraction for: $cleanUrl (Target Profile: $targetProfile)")
                if (cleanUrl.isBlank()) {
                    AppLogger.w(TAG, "Invalid or empty URL string provided.")
                    return@withTimeoutOrNull Result.failure<RecipeEntity>(IllegalArgumentException("Invalid or empty URL provided."))
                }

                val isSocial = isSocialPlatform(cleanUrl)
                val isYouTube = isYouTubeUrl(cleanUrl)
                val ytVideoId = if (isYouTube) extractYouTubeVideoId(cleanUrl) else null
                val ytOEmbed = if (isYouTube) fetchYouTubeOEmbed(cleanUrl) else null

                val primaryUA = if (isSocial) SOCIAL_BOT_USER_AGENT else BROWSER_USER_AGENT

                var html = fetchHtml(cleanUrl, primaryUA)
                AppLogger.i(TAG, "Primary HTML fetch returned ${html.length} chars (isSocial=$isSocial, isYouTube=$isYouTube)")
                
                // If primary fetch returned empty or insufficient content, try fallback UA
                if (html.length < 500) {
                    val fallbackUA = if (isSocial) BROWSER_USER_AGENT else SOCIAL_BOT_USER_AGENT
                    AppLogger.i(TAG, "Primary fetch small, retrying with fallback User-Agent...")
                    val fallbackHtml = fetchHtml(cleanUrl, fallbackUA)
                    if (fallbackHtml.length > html.length) {
                        html = fallbackHtml
                    }
                }

                // If Instagram post/reel, try embed/captioned endpoint fallback
                if (cleanUrl.contains("instagram.com") || cleanUrl.contains("instagr.am")) {
                    if (html.length < 500 || !html.contains("og:description")) {
                        val embedUrl = getInstagramEmbedUrl(cleanUrl)
                        if (embedUrl != null) {
                            AppLogger.i(TAG, "Attempting Instagram embed fetch: $embedUrl")
                            val embedHtml = fetchHtml(embedUrl, BROWSER_USER_AGENT)
                            if (embedHtml.length > html.length) {
                                html = embedHtml
                            }
                        }
                    }
                }

                if (html.isBlank()) {
                    if (isYouTube && ytOEmbed != null && !ytOEmbed.title.isNullOrBlank()) {
                        AppLogger.i(TAG, "YouTube HTML fetch was empty, but oEmbed succeeded for: ${ytOEmbed.title}")
                    } else {
                        val msg = "Webpage content could not be loaded. If Instagram requires login, copy & paste the recipe caption text directly into the chat!"
                        AppLogger.e(TAG, msg)
                        return@withTimeoutOrNull Result.failure<RecipeEntity>(Exception(msg))
                    }
                }

                // --- Tier 1: Try Schema.org JSON-LD ---
                val jsonLdRecipe = extractFromSchemaJsonLd(context, html, cleanUrl, targetProfile)
                if (jsonLdRecipe != null && jsonLdRecipe.ingredients.isNotEmpty()) {
                    AppLogger.i(TAG, "Successfully extracted recipe via Schema.org JSON-LD: ${jsonLdRecipe.title}")
                    return@withTimeoutOrNull Result.success(jsonLdRecipe)
                }

                // --- Tier 2: OpenGraph / Meta Tag & Gemini AI Extraction ---
                AppLogger.i(TAG, "Parsing OpenGraph metadata & page text for Gemini AI extraction...")
                val meta = extractPageMetadata(html)
                val compositeText = buildCompositeRecipeText(html, meta)

                if (compositeText.isBlank() || compositeText.length < 25) {
                    if (!isYouTube || ytOEmbed == null || ytOEmbed.title.isNullOrBlank()) {
                        val msg = "No readable recipe caption found. If this Instagram post is restricted, copy & paste the caption text directly into the chat!"
                        AppLogger.e(TAG, msg)
                        return@withTimeoutOrNull Result.failure<RecipeEntity>(Exception(msg))
                    }
                }

                var workingDto = if (compositeText.isNotBlank() && compositeText.length >= 25) {
                    AppLogger.i(TAG, "Sending ${compositeText.length} chars of recipe text to Gemini AI parser...")
                    val parsedDtoResult = GeminiClient.parseRecipeWithAi(emptyList(), compositeText)
                    if (parsedDtoResult.isSuccess) {
                        val r = parsedDtoResult.getOrThrow()
                        AppLogger.i(TAG, "Gemini parsed recipe: '${r.titleEnglish ?: r.title}' (${r.ingredients?.size ?: 0} ingredients)")
                        r
                    } else {
                        AppLogger.w(TAG, "Gemini AI failed: ${parsedDtoResult.exceptionOrNull()?.message}, falling back to OfflineRecipeParser...", parsedDtoResult.exceptionOrNull())
                        OfflineRecipeParser.parse(compositeText)
                    }
                } else {
                    com.example.ai.ParsedRecipeDto()
                }

                var title = workingDto.titleEnglish ?: workingDto.title ?: workingDto.name
                if (title.isNullOrBlank() || title.contains("Imported Web Recipe", ignoreCase = true) || (title.contains("Recipe", ignoreCase = true) && title.length < 10)) {
                    val cleanedSocial = cleanSocialTitle(ytOEmbed?.title ?: meta.ogTitle ?: meta.twitterTitle ?: meta.pageTitle)
                    if (!cleanedSocial.isNullOrBlank()) {
                        title = cleanedSocial
                    }
                }
                var finalTitle = if (!title.isNullOrBlank()) title else (cleanSocialTitle(ytOEmbed?.title) ?: "Imported Recipe")

                // --- Tier 3: Zero Ingredients Fallback ---
                // If the parsed recipe has 0 ingredients (e.g. YouTube video where description only had general info,
                // or recipe card didn't extract ingredients), formulate a complete recipe using the dish/video title!
                if (workingDto.ingredients.isNullOrEmpty()) {
                    AppLogger.w(TAG, "Parsed recipe has 0 ingredients. Attempting AI recipe formulation fallback for '$finalTitle'...")
                    val formulatePrompt = if (!compositeText.isBlank() && compositeText.length > 30) {
                        "Recipe title: $finalTitle\nContext / Video summary:\n${compositeText.take(1500)}"
                    } else {
                        finalTitle
                    }
                    val formulationResult = GeminiClient.formulateRecipeWithAi(formulatePrompt)
                    if (formulationResult.isSuccess) {
                        val formulated = formulationResult.getOrThrow()
                        if (!formulated.ingredients.isNullOrEmpty()) {
                            AppLogger.i(TAG, "Successfully formulated recipe fallback: '${formulated.titleEnglish ?: finalTitle}' with ${formulated.ingredients.size} ingredients!")
                            workingDto = formulated
                            if (!formulated.titleEnglish.isNullOrBlank()) {
                                finalTitle = formulated.titleEnglish
                            }
                        }
                    }
                }

                // High-resolution cover photo selection
                val imageUrl = meta.ogImage ?: meta.twitterImage ?: ytOEmbed?.thumbnail_url ?: (if (ytVideoId != null) "https://i.ytimg.com/vi/$ytVideoId/hqdefault.jpg" else null)
                val downloadedImage = if (!imageUrl.isNullOrBlank()) {
                    val cleanImageUrl = decodeHtmlEntities(imageUrl).replace("&amp;", "&")
                    downloadAndSavePhoto(context, cleanImageUrl)
                } else null

                val ingredientsList = (workingDto.ingredients ?: emptyList()).map { ingDto ->
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

                val stepsList = (workingDto.steps ?: emptyList()).mapIndexed { idx, stepDto ->
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
                    title = finalTitle,
                    titleGerman = workingDto.titleGerman ?: finalTitle,
                    titleEnglish = finalTitle,
                    category = workingDto.category ?: "Web Imports",
                    servings = workingDto.servings ?: "4 servings",
                    prepTimeMinutes = workingDto.prepTimeMinutes ?: 15,
                    cookTimeMinutes = workingDto.cookTimeMinutes ?: 30,
                    difficulty = workingDto.difficulty ?: "Medium",
                    ingredients = ingredientsList,
                    steps = stepsList,
                    notes = if (!workingDto.notesEnglish.isNullOrBlank()) workingDto.notesEnglish else "Imported from $cleanUrl",
                    notesGerman = workingDto.notesGerman ?: "",
                    sourceLanguage = workingDto.detectedSourceLanguage ?: "en",
                    coverTheme = "VINTAGE_LEATHER",
                    imageUri = downloadedImage?.first,
                    coverPhotoName = downloadedImage?.second,
                    profileName = targetProfile,
                    isFavorite = false,
                    rating = 5,
                    timesCooked = 0,
                    originStory = if (ytOEmbed != null && !ytOEmbed.author_name.isNullOrBlank()) "Recipe from YouTube: '${ytOEmbed.title ?: finalTitle}' by ${ytOEmbed.author_name} ($cleanUrl)" else "Recipe imported from $cleanUrl",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false,
                    syncStatus = "PENDING"
                )
                AppLogger.i(TAG, "Recipe successfully imported: '${recipe.title}' with ${recipe.ingredients.size} ingredients and ${recipe.steps.size} steps.")
                Result.success(recipe)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error importing recipe from URL: ${e.message}", e)
                Result.failure(e)
            }
        }

        timedResult ?: Result.failure(Exception("Extraction timed out. Tip: Copy and paste the recipe caption text directly into the chat!"))
    }

    private fun getInstagramEmbedUrl(url: String): String? {
        val pattern = Pattern.compile("https?://(?:www\\.)?instagram\\.com/(?:p|reel|tv)/([A-Za-z0-9_-]+)")
        val matcher = pattern.matcher(url)
        return if (matcher.find()) {
            val code = matcher.group(1)
            "https://www.instagram.com/p/$code/embed/captioned/"
        } else null
    }

    private fun fetchHtml(url: String, userAgent: String): String {
        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", userAgent)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("Accept-Language", "en-US,en;q=0.9,de;q=0.8")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                AppLogger.i(TAG, "HTTP ${response.code} OK for $url (Length: ${body.length} chars)")
                body
            } else {
                AppLogger.w(TAG, "HTTP ${response.code} error fetching $url (UA: ${userAgent.take(30)}...)")
                ""
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Network exception fetching $url: ${e.message}", e)
            ""
        }
    }

    fun isSocialPlatform(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("instagram.com") || lower.contains("instagr.am") ||
               lower.contains("tiktok.com") ||
               lower.contains("facebook.com") || lower.contains("fb.watch") ||
               lower.contains("threads.net") ||
               lower.contains("pinterest.com") || lower.contains("pin.it") ||
               lower.contains("youtube.com") || lower.contains("youtu.be") ||
               lower.contains("twitter.com") || lower.contains("x.com") ||
               lower.contains("reddit.com")
    }

    data class PageMetadata(
        val ogTitle: String? = null,
        val ogDescription: String? = null,
        val ogImage: String? = null,
        val twitterTitle: String? = null,
        val twitterDescription: String? = null,
        val twitterImage: String? = null,
        val metaDescription: String? = null,
        val metaKeywords: String? = null,
        val pageTitle: String? = null,
        val youTubeDescription: String? = null
    )

    fun extractPageMetadata(html: String): PageMetadata {
        val searchChunk = html.take(100000)
        val ogTitle = extractMetaAttribute(searchChunk, "property", "og:title")
            ?: extractMetaAttribute(searchChunk, "name", "og:title")
        val ogDesc = extractMetaAttribute(searchChunk, "property", "og:description")
            ?: extractMetaAttribute(searchChunk, "name", "og:description")
        val ogImg = extractMetaAttribute(searchChunk, "property", "og:image")
            ?: extractMetaAttribute(searchChunk, "property", "og:image:secure_url")
            ?: extractMetaAttribute(searchChunk, "name", "og:image")

        val twTitle = extractMetaAttribute(searchChunk, "name", "twitter:title")
            ?: extractMetaAttribute(searchChunk, "property", "twitter:title")
        val twDesc = extractMetaAttribute(searchChunk, "name", "twitter:description")
            ?: extractMetaAttribute(searchChunk, "property", "twitter:description")
        val twImg = extractMetaAttribute(searchChunk, "name", "twitter:image")
            ?: extractMetaAttribute(searchChunk, "property", "twitter:image")

        val metaDesc = extractMetaAttribute(searchChunk, "name", "description")
        val metaKw = extractMetaAttribute(searchChunk, "name", "keywords")

        val titlePattern = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE)
        val titleMatcher = titlePattern.matcher(searchChunk)
        val pTitle = if (titleMatcher.find()) decodeHtmlEntities(titleMatcher.group(1)?.trim() ?: "") else null

        val ytDesc = extractYouTubeDescription(html)

        return PageMetadata(
            ogTitle = ogTitle?.let { decodeHtmlEntities(it) },
            ogDescription = ogDesc?.let { decodeHtmlEntities(it) },
            ogImage = ogImg,
            twitterTitle = twTitle?.let { decodeHtmlEntities(it) },
            twitterDescription = twDesc?.let { decodeHtmlEntities(it) },
            twitterImage = twImg,
            metaDescription = metaDesc?.let { decodeHtmlEntities(it) },
            metaKeywords = metaKw?.let { decodeHtmlEntities(it) },
            pageTitle = pTitle,
            youTubeDescription = ytDesc
        )
    }

    private fun extractMetaAttribute(htmlChunk: String, attrName: String, attrValue: String): String? {
        val p1 = Pattern.compile("<meta[^>]+(?:property|name)=[\"']${Pattern.quote(attrValue)}[\"'][^>]+content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
        val m1 = p1.matcher(htmlChunk)
        if (m1.find()) return m1.group(1)

        val p2 = Pattern.compile("<meta[^>]+content=[\"']([^\"']*)[\"'][^>]+(?:property|name)=[\"']${Pattern.quote(attrValue)}[\"']", Pattern.CASE_INSENSITIVE)
        val m2 = p2.matcher(htmlChunk)
        if (m2.find()) return m2.group(1)

        return null
    }

    fun buildCompositeRecipeText(html: String, meta: PageMetadata): String {
        val sb = StringBuilder()

        val bestTitle = meta.ogTitle ?: meta.twitterTitle ?: meta.pageTitle
        if (!bestTitle.isNullOrBlank()) {
            sb.append("POST TITLE / HEADER:\n").append(bestTitle).append("\n\n")
        }

        // Check for full YouTube description first if present
        if (!meta.youTubeDescription.isNullOrBlank()) {
            sb.append("RECIPE POST CAPTION / DESCRIPTION (YOUTUBE):\n").append(meta.youTubeDescription).append("\n\n")
        } else {
            val bestDesc = meta.ogDescription ?: meta.twitterDescription ?: meta.metaDescription
            if (!bestDesc.isNullOrBlank()) {
                sb.append("RECIPE POST CAPTION / DESCRIPTION:\n").append(bestDesc).append("\n\n")
            }
        }

        if (!meta.metaKeywords.isNullOrBlank()) {
            sb.append("TAGS & KEYWORDS:\n").append(meta.metaKeywords).append("\n\n")
        }

        val bodyText = sanitizeHtmlToText(html)
        if (bodyText.isNotBlank()) {
            sb.append("PAGE BODY CONTENT:\n").append(bodyText)
        }

        return sb.toString().trim()
    }

    fun cleanSocialTitle(rawTitle: String?): String? {
        if (rawTitle.isNullOrBlank()) return null
        var title = rawTitle.trim()

        val igPrefixPattern = Pattern.compile("^[\\w.-]+\\s+on\\s+Instagram:\\s*[\"']?(.*?)[\"']?$", Pattern.CASE_INSENSITIVE)
        val igMatcher = igPrefixPattern.matcher(title)
        if (igMatcher.find()) {
            val inner = igMatcher.group(1)?.trim()
            if (!inner.isNullOrBlank()) title = inner
        }

        val reelSuffixPattern = Pattern.compile("^(.*?)\\s*\\([@\\w.-]+\\)\\s*[•\\u2022\\-\\|]\\s*Instagram.*$", Pattern.CASE_INSENSITIVE)
        val reelMatcher = reelSuffixPattern.matcher(title)
        if (reelMatcher.find()) {
            val inner = reelMatcher.group(1)?.trim()
            if (!inner.isNullOrBlank()) title = inner
        }

        // YouTube title suffix cleaning (e.g. "How to Make Oat Milk - YouTube" or "... | YouTube")
        val ytSuffixPattern = Pattern.compile("^(.*?)\\s*[-|•\\u2022]\\s*YouTube$", Pattern.CASE_INSENSITIVE)
        val ytMatcher = ytSuffixPattern.matcher(title)
        if (ytMatcher.find()) {
            val inner = ytMatcher.group(1)?.trim()
            if (!inner.isNullOrBlank()) title = inner
        }

        if (title.contains("\n")) {
            title = title.substringBefore("\n").trim()
        }
        if (title.contains("#")) {
            title = title.substringBefore("#").trim()
        }

        return title.trim().trim('"', '\'', ' ', ':', '-')
    }

    fun decodeHtmlEntities(input: String): String {
        if (input.isBlank()) return ""
        var s = input
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&deg;", "°")
            .replace("&nbsp;", " ")
            .replace("&#064;", "@")
            .replace("&#64;", "@")
            .replace("&#x2022;", "•")
            .replace("&#8226;", "•")

        val hexPattern = Pattern.compile("&#x([0-9a-fA-F]+);")
        var hexMatcher = hexPattern.matcher(s)
        val sbHex = StringBuffer()
        while (hexMatcher.find()) {
            try {
                val codePoint = hexMatcher.group(1)!!.toInt(16)
                hexMatcher.appendReplacement(sbHex, MatcherQuote(String(Character.toChars(codePoint))))
            } catch (e: Exception) {
                hexMatcher.appendReplacement(sbHex, "")
            }
        }
        hexMatcher.appendTail(sbHex)
        s = sbHex.toString()

        val decPattern = Pattern.compile("&#([0-9]+);")
        var decMatcher = decPattern.matcher(s)
        val sbDec = StringBuffer()
        while (decMatcher.find()) {
            try {
                val codePoint = decMatcher.group(1)!!.toInt(10)
                decMatcher.appendReplacement(sbDec, MatcherQuote(String(Character.toChars(codePoint))))
            } catch (e: Exception) {
                decMatcher.appendReplacement(sbDec, "")
            }
        }
        decMatcher.appendTail(sbDec)
        s = sbDec.toString()

        return s
    }

    private fun MatcherQuote(replacement: String): String {
        return java.util.regex.Matcher.quoteReplacement(replacement)
    }

    private fun extractFromSchemaJsonLd(
        context: Context,
        html: String,
        sourceUrl: String,
        targetProfile: String
    ): RecipeEntity? {
        val pattern = Pattern.compile("<script[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(.*?)</script>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)

        while (matcher.find()) {
            val jsonText = matcher.group(1)?.trim() ?: continue
            try {
                if (jsonText.startsWith("[")) {
                    val array = JSONArray(jsonText)
                    for (i in 0 until array.length()) {
                        val item = array.optJSONObject(i) ?: continue
                        val parsed = parseRecipeJsonObject(context, item, sourceUrl, targetProfile)
                        if (parsed != null) return parsed
                    }
                } else if (jsonText.startsWith("{")) {
                    val obj = JSONObject(jsonText)
                    if (obj.has("@graph")) {
                        val graph = obj.optJSONArray("@graph")
                        if (graph != null) {
                            for (i in 0 until graph.length()) {
                                val item = graph.optJSONObject(i) ?: continue
                                val parsed = parseRecipeJsonObject(context, item, sourceUrl, targetProfile)
                                if (parsed != null) return parsed
                            }
                        }
                    } else {
                        val parsed = parseRecipeJsonObject(context, obj, sourceUrl, targetProfile)
                        if (parsed != null) return parsed
                    }
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to parse JSON-LD block: ${e.message}")
            }
        }
        return null
    }

    private fun parseRecipeJsonObject(
        context: Context,
        obj: JSONObject,
        sourceUrl: String,
        targetProfile: String
    ): RecipeEntity? {
        val type = obj.optString("@type", "")
        val typesArr = obj.optJSONArray("@type")
        val isRecipe = type.contains("Recipe", ignoreCase = true) || 
            (typesArr != null && (0 until typesArr.length()).any { typesArr.optString(it).contains("Recipe", ignoreCase = true) })

        if (!isRecipe) return null

        val title = obj.optString("name").ifBlank { obj.optString("headline", "Imported Recipe") }
        val description = obj.optString("description", "")
        val category = obj.optString("recipeCategory", "Web Imports")
        val yield = obj.optString("recipeYield", "4 servings")

        val prepTimeIso = obj.optString("prepTime", "")
        val cookTimeIso = obj.optString("cookTime", "")
        val prepMins = parseIsoDurationMinutes(prepTimeIso, 15)
        val cookMins = parseIsoDurationMinutes(cookTimeIso, 30)

        val ingredients = mutableListOf<RecipeIngredient>()
        val ingArr = obj.optJSONArray("recipeIngredient")
        if (ingArr != null) {
            for (i in 0 until ingArr.length()) {
                val rawLine = ingArr.optString(i, "").trim()
                if (rawLine.isNotBlank()) {
                    ingredients.add(parseIngredientLine(rawLine))
                }
            }
        }

        val steps = mutableListOf<RecipeStep>()
        val instructionsObj = obj.opt("recipeInstructions")
        when (instructionsObj) {
            is JSONArray -> {
                var stepNum = 1
                for (i in 0 until instructionsObj.length()) {
                    val item = instructionsObj.get(i)
                    if (item is JSONObject) {
                        val text = item.optString("text", item.optString("name", "")).trim()
                        if (text.isNotBlank()) {
                            steps.add(RecipeStep(stepNumber = stepNum++, instructionEnglish = text, instructionGerman = text))
                        }
                    } else if (item is String && item.isNotBlank()) {
                        steps.add(RecipeStep(stepNumber = stepNum++, instructionEnglish = item.trim(), instructionGerman = item.trim()))
                    }
                }
            }
            is String -> {
                if (instructionsObj.isNotBlank()) {
                    val splitLines = instructionsObj.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    splitLines.forEachIndexed { index, line ->
                        steps.add(RecipeStep(stepNumber = index + 1, instructionEnglish = line, instructionGerman = line))
                    }
                }
            }
        }

        var imageUrl: String? = null
        val imgObj = obj.opt("image")
        when (imgObj) {
            is String -> imageUrl = imgObj
            is JSONArray -> if (imgObj.length() > 0) imageUrl = imgObj.optString(0)
            is JSONObject -> imageUrl = imgObj.optString("url", imgObj.optString("contentUrl"))
        }

        val downloadedImage = if (!imageUrl.isNullOrBlank()) {
            downloadAndSavePhoto(context, imageUrl)
        } else null

        return RecipeEntity(
            id = 0,
            title = title,
            titleGerman = title,
            titleEnglish = title,
            category = if (category.isNotBlank()) category else "Web Imports",
            servings = if (yield.isNotBlank()) yield else "4 servings",
            prepTimeMinutes = prepMins,
            cookTimeMinutes = cookMins,
            difficulty = "Medium",
            ingredients = ingredients,
            steps = steps,
            notes = if (description.isNotBlank()) "$description\n\nSource: $sourceUrl" else "Source: $sourceUrl",
            notesGerman = "",
            sourceLanguage = "en",
            coverTheme = "VINTAGE_LEATHER",
            imageUri = downloadedImage?.first,
            coverPhotoName = downloadedImage?.second,
            profileName = targetProfile,
            isFavorite = false,
            rating = 5,
            timesCooked = 0,
            originStory = "Imported from $sourceUrl",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = "PENDING"
        )
    }

    private fun parseIngredientLine(line: String): RecipeIngredient {
        val regex = Pattern.compile("^([0-9/Â½â…“â…”Â¼Â¾â…›â…œâ… â…ž\\s.-]+)\\s*([a-zA-Z]+)?\\s*(.*)$")
        val matcher = regex.matcher(line.trim())
        return if (matcher.find()) {
            val amt = matcher.group(1)?.trim() ?: ""
            val unit = matcher.group(2)?.trim() ?: ""
            val name = matcher.group(3)?.trim() ?: line
            RecipeIngredient(
                name = if (name.isNotBlank()) name else line,
                amount = amt,
                unit = unit,
                nameEnglish = if (name.isNotBlank()) name else line
            )
        } else {
            RecipeIngredient(name = line, amount = "", unit = "", nameEnglish = line)
        }
    }

    private fun parseIsoDurationMinutes(isoStr: String, defaultMins: Int): Int {
        if (isoStr.isBlank()) return defaultMins
        try {
            var total = 0
            val hourMatcher = Pattern.compile("(\\d+)H", Pattern.CASE_INSENSITIVE).matcher(isoStr)
            if (hourMatcher.find()) {
                total += (hourMatcher.group(1)?.toIntOrNull() ?: 0) * 60
            }
            val minMatcher = Pattern.compile("(\\d+)M", Pattern.CASE_INSENSITIVE).matcher(isoStr)
            if (minMatcher.find()) {
                total += minMatcher.group(1)?.toIntOrNull() ?: 0
            }
            return if (total > 0) total else defaultMins
        } catch (e: Exception) {
            return defaultMins
        }
    }

    private fun downloadAndSavePhoto(context: Context, imageUrl: String): Pair<String, String>? {
        return try {
            val req = Request.Builder()
                .url(imageUrl)
                .addHeader("User-Agent", BROWSER_USER_AGENT)
                .build()
            val resp = httpClient.newCall(req).execute()
            if (!resp.isSuccessful) return null

            val bytes = resp.body?.bytes() ?: return null
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

            val photosDir = File(context.filesDir, "recipes_photos")
            photosDir.mkdirs()
            val filename = "web_recipe_${UUID.randomUUID().toString().take(8)}.jpg"
            val file = File(photosDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Pair(file.absolutePath, filename)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to download image from $imageUrl: ${e.message}")
            null
        }
    }

    private fun sanitizeHtmlToText(html: String): String {
        val chunk = html.take(40000)
        val sb = StringBuilder()
        var insideTag = false
        var i = 0
        while (i < chunk.length) {
            val c = chunk[i]
            if (c == '<') {
                insideTag = true
                if (chunk.startsWith("<script", i, ignoreCase = true)) {
                    val endIdx = chunk.indexOf("</script>", i, ignoreCase = true)
                    if (endIdx != -1) {
                        i = endIdx + 9
                        insideTag = false
                        continue
                    }
                } else if (chunk.startsWith("<style", i, ignoreCase = true)) {
                    val endIdx = chunk.indexOf("</style>", i, ignoreCase = true)
                    if (endIdx != -1) {
                        i = endIdx + 8
                        insideTag = false
                        continue
                    }
                }
            } else if (c == '>') {
                insideTag = false
                sb.append(' ')
            } else if (!insideTag) {
                sb.append(c)
            }
            i++
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim().take(4000)
    }

    fun extractYouTubeDescription(html: String): String? {
        if (!html.contains("youtube.com") && !html.contains("youtu.be") && !html.contains("shortDescription")) {
            return null
        }

        try {
            // Check for "shortDescription":"..." in initial data / player response
            val pattern = Pattern.compile("\"shortDescription\"\\s*:\\s*\"(.*?)(?<!\\\\)\"", Pattern.DOTALL)
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                val raw = matcher.group(1)
                if (!raw.isNullOrBlank()) {
                    val unescaped = unescapeJsonString(raw).trim()
                    if (unescaped.isNotBlank()) {
                        AppLogger.i(TAG, "Extracted full YouTube shortDescription (${unescaped.length} chars)")
                        return unescaped
                    }
                }
            }

            // Fallback: Check for "description":{"simpleText":"..."}
            val simplePattern = Pattern.compile("\"description\"\\s*:\\s*\\{\\s*\"simpleText\"\\s*:\\s*\"(.*?)(?<!\\\\)\"", Pattern.DOTALL)
            val simpleMatcher = simplePattern.matcher(html)
            if (simpleMatcher.find()) {
                val raw = simpleMatcher.group(1)
                if (!raw.isNullOrBlank()) {
                    val unescaped = unescapeJsonString(raw).trim()
                    if (unescaped.isNotBlank()) {
                        AppLogger.i(TAG, "Extracted YouTube simpleText description (${unescaped.length} chars)")
                        return unescaped
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed extracting YouTube description: ${e.message}")
        }
        return null
    }

    private fun unescapeJsonString(input: String): String {
        var s = input
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\/", "/")

        val unicodePattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})")
        val matcher = unicodePattern.matcher(s)
        val sb = StringBuffer()
        while (matcher.find()) {
            try {
                val code = matcher.group(1)!!.toInt(16)
                matcher.appendReplacement(sb, MatcherQuote(String(Character.toChars(code))))
            } catch (_: Exception) {
                matcher.appendReplacement(sb, "")
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    data class YouTubeOEmbed(
        val title: String?,
        val author_name: String?,
        val thumbnail_url: String?
    )

    fun isYouTubeUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }

    fun extractYouTubeVideoId(url: String): String? {
        val pattern = Pattern.compile("(?i)(?:youtube\\.com\\/(?:[^\\s/]+\\/\\S+\\/|(?:v|e(?:mbed)?|shorts)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})")
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    fun fetchYouTubeOEmbed(url: String): YouTubeOEmbed? {
        val videoId = extractYouTubeVideoId(url) ?: return null
        val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
        return try {
            val req = Request.Builder()
                .url(oembedUrl)
                .addHeader("User-Agent", BROWSER_USER_AGENT)
                .build()
            val resp = httpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                val jsonStr = resp.body?.string() ?: ""
                if (jsonStr.isNotBlank()) {
                    val obj = JSONObject(jsonStr)
                    YouTubeOEmbed(
                        title = obj.optString("title").takeIf { it.isNotBlank() },
                        author_name = obj.optString("author_name").takeIf { it.isNotBlank() },
                        thumbnail_url = obj.optString("thumbnail_url").takeIf { it.isNotBlank() }
                    )
                } else null
            } else null
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed fetching YouTube oEmbed: ${e.message}")
            null
        }
    }

    fun cleanUrlString(input: String): String {
        var s = input.trim()
        val pattern = Pattern.compile("(?i)(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+)")
        val matcher = pattern.matcher(s)
        s = if (matcher.find()) {
            matcher.group(1) ?: s
        } else {
            val schemelessPattern = Pattern.compile("(?i)((?:www\\.|(?:(?:m\\.)?youtube\\.com|youtu\\.be|instagram\\.com|instagr\\.am|tiktok\\.com|pinterest\\.com|pin\\.it|facebook\\.com|fb\\.watch|allrecipes\\.com|chefkoch\\.de)/)[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)")
            val sm = schemelessPattern.matcher(s)
            if (sm.find()) sm.group(1) ?: s else s
        }

        while (s.isNotEmpty() && (s.endsWith(".") || s.endsWith(",") || s.endsWith(";") || 
               s.endsWith("!") || s.endsWith("?") || s.endsWith(")") || s.endsWith(">") || 
               s.endsWith("\"") || s.endsWith("'"))) {
            s = s.substring(0, s.length - 1)
        }

        if (!s.startsWith("http://", ignoreCase = true) && !s.startsWith("https://", ignoreCase = true)) {
            if (s.contains(".") && !s.contains(" ")) {
                s = "https://$s"
            }
        }
        return s
    }
}