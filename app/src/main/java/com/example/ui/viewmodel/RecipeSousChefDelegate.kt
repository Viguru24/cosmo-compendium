package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiClient
import com.example.ai.OfflineRecipeParser
import com.example.ai.SousChefActionEngine
import com.example.ai.SousChefIntent
import com.example.ai.VideoRecipeExtractor
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.data.repository.RecipeRepository
import com.example.data.web.UrlRecipeExtractor
import com.example.ui.components.GuideTopic
import com.example.ui.util.FuzzySearchHelper
import com.example.ui.util.getDisplayTitle
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Delegate responsible for Sous-Chef AI copilot actions, interactive guide tour,
 * video/link/text recipe imports, and conversational intent execution.
 */
class RecipeSousChefDelegate(
    private val vm: RecipeViewModel,
    private val app: Application,
    private val repository: RecipeRepository,
    private val prefs: SharedPreferences
) {
    val isSousChefOpen = MutableStateFlow(false)
    val sousChefMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val isSousChefProcessing = MutableStateFlow(false)
    val triggerScanFromSousChef = MutableStateFlow(false)
    val showErrorLogDialog = MutableStateFlow(false)

    private val hasSeenOnboarding: Boolean = prefs.getBoolean("pref_has_seen_onboard", false)
    val isGuideOpen = MutableStateFlow(!hasSeenOnboarding)
    val guideInitialTopic = MutableStateFlow(GuideTopic.CARD_SCANNING)

    val isVideoAnalyzing = MutableStateFlow(false)

    fun openSousChef() {
        isSousChefOpen.value = true
    }

    fun closeSousChef() {
        isSousChefOpen.value = false
    }

    fun openErrorLogDialog() {
        showErrorLogDialog.value = true
    }

    fun closeErrorLogDialog() {
        showErrorLogDialog.value = false
    }

    fun openGuide(topic: GuideTopic = GuideTopic.CARD_SCANNING) {
        guideInitialTopic.value = topic
        isGuideOpen.value = true
    }

    fun closeGuide() {
        prefs.edit().putBoolean("pref_has_seen_onboard", true).apply()
        isGuideOpen.value = false
    }

    fun clearTriggerScanFromSousChef() {
        triggerScanFromSousChef.value = false
    }

    fun cancelSousChefProcessing() {
        isSousChefProcessing.value = false
        isVideoAnalyzing.value = false
    }

    fun importRecipeFromVideo(context: Context, videoUri: Uri, targetProfile: String = vm.activeProfile.value) {
        vm.viewModelScope.launch {
            try {
                isVideoAnalyzing.value = true
                isSousChefProcessing.value = true
                val profile = if (targetProfile == "All Family" || targetProfile == "All") "Annette" else targetProfile
                AppLogger.i("RecipeViewModel", "Starting AI video extraction for profile '$profile' from URI: $videoUri")

                val statusMsg = ChatMessage(
                    isUser = false,
                    text = "📹 Analyzing video frames with Gemini 2.0 Multimodal Vision to extract ingredients and cooking steps..."
                )
                sousChefMessages.value = sousChefMessages.value + statusMsg

                val result = VideoRecipeExtractor.extractRecipeFromVideoUri(context, videoUri, profile)
                if (result.isSuccess) {
                    val recipe = result.getOrThrow()
                    repository.insertRecipe(recipe)
                    AppLogger.i("RecipeViewModel", "Saved video recipe #${recipe.id} '${recipe.title}'")

                    if (vm.isCloudSyncEnabled.value) {
                        vm.viewModelScope.launch(Dispatchers.IO) { vm.syncManager?.performSync() }
                    }

                    val successMsg = ChatMessage(
                        isUser = false,
                        text = "🎉 Successfully extracted '${recipe.title}' from video!\n\n• ${recipe.ingredients.size} ingredients\n• ${recipe.steps.size} steps\n• Plated cover photo captured\n\nSaved to $profile's Cookbook.",
                        matchingRecipes = listOf(recipe)
                    )
                    sousChefMessages.value = sousChefMessages.value + successMsg
                    Toast.makeText(context, "🎉 Extracted '${recipe.title}'!", Toast.LENGTH_LONG).show()
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Unknown video extraction failure."
                    AppLogger.e("RecipeViewModel", "Video extraction failed: $err", result.exceptionOrNull())
                    val errorMsg = ChatMessage(
                        isUser = false,
                        text = "⚠️ Video recipe extraction could not complete:\n$err\n\n💡 Tip: You can also use a screen recording of the cooking clip or physical recipe photos."
                    )
                    sousChefMessages.value = sousChefMessages.value + errorMsg
                    Toast.makeText(context, "Video extraction failed: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Throwable) {
                AppLogger.e("RecipeViewModel", "Exception during importRecipeFromVideo: ${e.message}", e)
                val errorMsg = ChatMessage(
                    isUser = false,
                    text = "⚠️ Error reading video: ${e.message}"
                )
                sousChefMessages.value = sousChefMessages.value + errorMsg
            } finally {
                isVideoAnalyzing.value = false
                isSousChefProcessing.value = false
            }
        }
    }

    fun handleSousChefInput(prompt: String) {
        if (prompt.isBlank()) return
        AppLogger.i("SousChef", "User message: \"$prompt\"")
        val userMsg = ChatMessage(isUser = true, text = prompt)
        sousChefMessages.value = sousChefMessages.value + userMsg

        vm.viewModelScope.launch {
            try {
                isSousChefProcessing.value = true
                val intent = SousChefActionEngine.parseIntent(prompt, vm.activeProfile.value, vm.familyProfiles.value)
                AppLogger.i("SousChef", "Detected intent: ${intent.javaClass.simpleName}")

                when (intent) {
                    is SousChefIntent.ScanCamera -> {
                        val targetProfile = intent.targetProfile ?: vm.activeProfile.value
                        if (targetProfile != vm.activeProfile.value && targetProfile != "All Family") {
                            vm.setActiveProfile(targetProfile)
                        }
                        val aiMsg = ChatMessage(
                            isUser = false,
                            text = "Opening camera immediately to scan recipe cards for $targetProfile's cookbook! 📸",
                            intent = intent
                        )
                        sousChefMessages.value = sousChefMessages.value + aiMsg
                        triggerScanFromSousChef.value = true
                    }
                    is SousChefIntent.ImportUrl -> {
                        val targetProfile = intent.targetProfile ?: vm.activeProfile.value
                        val initialAiMsg = ChatMessage(
                            isUser = false,
                            text = "Fetching and extracting recipe from ${intent.url} for $targetProfile's cookbook... 🌐"
                        )
                        sousChefMessages.value = sousChefMessages.value + initialAiMsg

                        val result = UrlRecipeExtractor.extractRecipeFromUrl(
                            context = app,
                            urlStr = intent.url,
                            targetProfile = if (targetProfile == "All Family") "Annette" else targetProfile
                        )

                        if (result.isSuccess) {
                            val newRecipe = result.getOrThrow()
                            repository.insertRecipe(newRecipe)
                            AppLogger.i("SousChef", "Imported recipe #${newRecipe.id} '${newRecipe.title}' saved to $targetProfile's cookbook.")

                            if (vm.isCloudSyncEnabled.value) {
                                vm.viewModelScope.launch(Dispatchers.IO) {
                                    vm.syncManager?.performSync()
                                }
                            }

                            val successMsg = ChatMessage(
                                isUser = false,
                                text = "🎉 Successfully imported '${newRecipe.title}' (${newRecipe.ingredients.size} ingredients, ${newRecipe.steps.size} steps) and saved into $targetProfile's cookbook!"
                            )
                            sousChefMessages.value = sousChefMessages.value + successMsg
                            vm.navigateToRecipeEvent.value = newRecipe
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "Unknown error"
                            AppLogger.e("SousChef", "URL recipe extraction error: $err", result.exceptionOrNull())
                            val errorMsg = ChatMessage(
                                isUser = false,
                                text = "⚠️ Could not extract recipe from link:\n$err\n\n💡 Tip: You can tap the 📋 Log icon in the top header or paste the recipe text/caption directly into the chat!"
                            )
                            sousChefMessages.value = sousChefMessages.value + errorMsg
                        }
                    }
                    is SousChefIntent.ImportTextRecipe -> {
                        val targetProfile = intent.targetProfile ?: vm.activeProfile.value
                        val resolvedProfile = if (targetProfile == "All Family" || targetProfile == "All") "Annette" else targetProfile
                        val initialAiMsg = ChatMessage(
                            isUser = false,
                            text = "Parsing and extracting recipe text for $resolvedProfile's cookbook... 📝"
                        )
                        sousChefMessages.value = sousChefMessages.value + initialAiMsg

                        val parsedResult = GeminiClient.parseRecipeWithAi(emptyList(), intent.text)
                        val dto = if (parsedResult.isSuccess) {
                            parsedResult.getOrThrow()
                        } else {
                            AppLogger.w("SousChef", "Gemini parsing text recipe failed, falling back to OfflineRecipeParser: ${parsedResult.exceptionOrNull()?.message}")
                            OfflineRecipeParser.parse(intent.text)
                        }

                        val title = dto.titleEnglish?.takeIf { it.isNotBlank() }
                            ?: dto.title?.takeIf { it.isNotBlank() }
                            ?: dto.name?.takeIf { it.isNotBlank() }
                            ?: "Imported Recipe"

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

                        val newRecipe = RecipeEntity(
                            id = 0,
                            title = title,
                            titleGerman = dto.titleGerman ?: title,
                            titleEnglish = title,
                            category = dto.category ?: "Family Classics",
                            servings = dto.servings ?: "4 servings",
                            prepTimeMinutes = dto.prepTimeMinutes ?: 15,
                            cookTimeMinutes = dto.cookTimeMinutes ?: 30,
                            difficulty = dto.difficulty ?: "Medium",
                            ingredients = ingredientsList,
                            steps = stepsList,
                            notes = dto.notesEnglish ?: "Imported recipe text.",
                            notesGerman = dto.notesGerman ?: "",
                            sourceLanguage = dto.detectedSourceLanguage ?: "en",
                            coverTheme = "VINTAGE_LEATHER",
                            profileName = resolvedProfile,
                            isFavorite = false,
                            rating = 5,
                            timesCooked = 0,
                            originStory = "Recipe imported from text via Sous-Chef.",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            isDeleted = false,
                            syncStatus = "PENDING"
                        )

                        repository.insertRecipe(newRecipe)
                        AppLogger.i("SousChef", "Imported text recipe #${newRecipe.id} '$title' saved to $resolvedProfile's cookbook.")

                        if (vm.isCloudSyncEnabled.value) {
                            vm.viewModelScope.launch(Dispatchers.IO) { vm.syncManager?.performSync() }
                        }

                        val successMsg = ChatMessage(
                            isUser = false,
                            text = "🎉 Successfully imported '$title' (${newRecipe.ingredients.size} ingredients, ${newRecipe.steps.size} steps) and saved into $resolvedProfile's cookbook!",
                            matchingRecipes = listOf(newRecipe)
                        )
                        sousChefMessages.value = sousChefMessages.value + successMsg
                        vm.navigateToRecipeEvent.value = newRecipe
                    }
                    is SousChefIntent.CreateRecipe -> {
                        val targetProfile = intent.targetProfile ?: vm.activeProfile.value
                        val resolvedProfile = if (targetProfile == "All Family" || targetProfile == "All") "Annette" else targetProfile
                        val dish = intent.dishQuery
                        val initialAiMsg = ChatMessage(
                            isUser = false,
                            text = "👨‍🍳 Putting together a complete culinary recipe for '$dish' for $resolvedProfile's cookbook..."
                        )
                        sousChefMessages.value = sousChefMessages.value + initialAiMsg

                        val result = GeminiClient.formulateRecipeWithAi(dish)
                        if (result.isSuccess) {
                            val dto = result.getOrThrow()
                            val title = dto.titleEnglish?.takeIf { it.isNotBlank() }
                                ?: dto.title?.takeIf { it.isNotBlank() }
                                ?: dish.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

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

                            val newRecipe = RecipeEntity(
                                id = 0,
                                title = title,
                                titleGerman = dto.titleGerman ?: title,
                                titleEnglish = title,
                                category = dto.category ?: "Family Classics",
                                servings = dto.servings ?: "4 servings",
                                prepTimeMinutes = dto.prepTimeMinutes ?: 15,
                                cookTimeMinutes = dto.cookTimeMinutes ?: 25,
                                difficulty = dto.difficulty ?: "Easy",
                                ingredients = ingredientsList,
                                steps = stepsList,
                                notes = dto.notesEnglish ?: "Formulated by Sous-Chef AI.",
                                notesGerman = dto.notesGerman ?: "",
                                sourceLanguage = dto.detectedSourceLanguage ?: "en",
                                coverTheme = "VINTAGE_LEATHER",
                                profileName = resolvedProfile,
                                isFavorite = false,
                                rating = 5,
                                timesCooked = 0,
                                originStory = "Culinary creation formulated by Sous-Chef AI.",
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                isDeleted = false,
                                syncStatus = "PENDING"
                            )

                            repository.insertRecipe(newRecipe)
                            AppLogger.i("SousChef", "Formulated recipe #${newRecipe.id} '$title' saved to $resolvedProfile's cookbook.")

                            if (vm.isCloudSyncEnabled.value) {
                                vm.viewModelScope.launch(Dispatchers.IO) { vm.syncManager?.performSync() }
                            }

                            val successMsg = ChatMessage(
                                isUser = false,
                                text = "🎉 Here is your complete recipe for '$title'!\n\n• ${newRecipe.ingredients.size} ingredients\n• ${newRecipe.steps.size} steps\n• Prep: ${newRecipe.prepTimeMinutes}m | Cook: ${newRecipe.cookTimeMinutes}m\n\nSaved directly to $resolvedProfile's Cookbook! Tap below to open:",
                                matchingRecipes = listOf(newRecipe)
                            )
                            sousChefMessages.value = sousChefMessages.value + successMsg
                            vm.navigateToRecipeEvent.value = newRecipe
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "AI formulation unavailable."
                            AppLogger.e("SousChef", "Recipe creation failed: $err", result.exceptionOrNull())
                            val errorMsg = ChatMessage(
                                isUser = false,
                                text = "⚠️ Could not formulate recipe for '$dish':\n$err"
                            )
                            sousChefMessages.value = sousChefMessages.value + errorMsg
                        }
                    }
                    is SousChefIntent.SwitchProfile -> {
                        vm.setActiveProfile(intent.targetProfile)
                        val aiMsg = ChatMessage(
                            isUser = false,
                            text = "Switched active cookbook to ${intent.targetProfile}! 📖",
                            intent = intent
                        )
                        sousChefMessages.value = sousChefMessages.value + aiMsg
                    }
                    is SousChefIntent.SearchRecipes -> {
                        val q = intent.query.trim()
                        vm.searchQuery.value = q
                        val all = repository.getAllRecipesDirect()
                        val matching = all
                            .map { it to FuzzySearchHelper.score(it, q) }
                            .filter { it.second > 0 }
                            .sortedByDescending { it.second }
                            .map { it.first }

                        val aiMsg = if (matching.isNotEmpty()) {
                            val countText = if (matching.size == 1) "1 recipe" else "${matching.size} recipes"
                            ChatMessage(
                                isUser = false,
                                text = "📖 Found $countText for '$q' in your cookbook library! Tap below to open:",
                                intent = intent,
                                matchingRecipes = matching
                            )
                        } else {
                            ChatMessage(
                                isUser = false,
                                text = "🔍 I couldn't find any saved recipes for '$q' in your cookbook library yet.\n\n💡 You can:\n• 📸 Scan a recipe card with the camera\n• 🌐 Import a recipe from any web link\n• 💬 Ask me for culinary advice or substitutions!",
                                intent = intent
                            )
                        }
                        sousChefMessages.value = sousChefMessages.value + aiMsg
                    }
                    is SousChefIntent.MoveRecipes -> {
                        val target = intent.targetProfile
                        if (intent.isAll || intent.sourceQuery.isNullOrBlank()) {
                            val all = repository.getAllRecipesDirect()
                            val toMove = if (vm.activeProfile.value == "All Family" || vm.activeProfile.value == "All") {
                                all
                            } else {
                                val filtered = all.filter { it.profileName.equals(vm.activeProfile.value, ignoreCase = true) }
                                if (filtered.isNotEmpty()) filtered else all
                            }

                            val count = toMove.size
                            toMove.forEach { repository.updateRecipeProfile(it.id, target) }
                            vm.setActiveProfile(target)

                            if (vm.isCloudSyncEnabled.value) {
                                vm.viewModelScope.launch(Dispatchers.IO) { vm.syncManager?.performSync() }
                            }

                            val aiMsg = ChatMessage(
                                isUser = false,
                                text = "🎉 Done! Successfully moved all $count recipe(s) to $target's Cookbook! I've also switched your active bookshelf to $target's recipes.",
                                intent = intent
                            )
                            sousChefMessages.value = sousChefMessages.value + aiMsg
                        } else {
                            val query = intent.sourceQuery.trim()
                            val all = repository.getAllRecipesDirect()
                            val matching = all.filter { FuzzySearchHelper.matches(it, query) }

                            if (matching.isNotEmpty()) {
                                matching.forEach { repository.updateRecipeProfile(it.id, target) }
                                if (vm.isCloudSyncEnabled.value) {
                                    vm.viewModelScope.launch(Dispatchers.IO) { vm.syncManager?.performSync() }
                                }
                                val titles = matching.joinToString(", ") { "'${it.getDisplayTitle()}'" }
                                val aiMsg = ChatMessage(
                                    isUser = false,
                                    text = "✅ Moved $titles to $target's Cookbook!",
                                    intent = intent,
                                    matchingRecipes = matching
                                )
                                sousChefMessages.value = sousChefMessages.value + aiMsg
                            } else {
                                val aiMsg = ChatMessage(
                                    isUser = false,
                                    text = "⚠️ Couldn't find a recipe matching '$query' to move to $target.",
                                    intent = intent
                                )
                                sousChefMessages.value = sousChefMessages.value + aiMsg
                            }
                        }
                    }
                    is SousChefIntent.KitchenAdvice -> {
                        val advice = SousChefActionEngine.getKitchenAdvice(intent.query)
                        val aiMsg = ChatMessage(isUser = false, text = advice, intent = intent)
                        sousChefMessages.value = sousChefMessages.value + aiMsg
                    }
                }
            } catch (e: Throwable) {
                AppLogger.e("RecipeViewModel", "Error in handleSousChefInput: ${e.message}", e)
                sousChefMessages.value = sousChefMessages.value + ChatMessage(
                    isUser = false,
                    text = "⚠️ Error processing request: ${e.message ?: "Unknown error"}"
                )
            } finally {
                isSousChefProcessing.value = false
            }
        }
    }
}
