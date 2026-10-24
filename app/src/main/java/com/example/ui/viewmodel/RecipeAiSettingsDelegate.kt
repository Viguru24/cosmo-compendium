package com.example.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.example.ai.AiProvider
import com.example.ai.ComfyUiClient
import com.example.ai.GeminiClient
import com.example.ai.GeminiModelConfig
import com.example.ai.ImageGenEngine
import com.example.ai.UniversalAiRecipeService
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Delegate responsible for AI engine configurations (Gemini, Universal AI, ComfyUI)
 * and Cloud Sync settings with the self-hosted VPS.
 */
class RecipeAiSettingsDelegate(
    private val vm: RecipeViewModel,
    private val repository: RecipeRepository,
    private val prefs: SharedPreferences
) {
    val aiProvider = MutableStateFlow(
        try {
            AiProvider.valueOf(
                prefs.getString("pref_ai_provider", AiProvider.GOOGLE_GEMINI.name)
                    ?: AiProvider.GOOGLE_GEMINI.name
            )
        } catch (e: Exception) {
            AiProvider.GOOGLE_GEMINI
        }
    )

    val geminiApiKey = MutableStateFlow(prefs.getString("pref_gemini_api_key", "") ?: "")
    val geminiModel = MutableStateFlow(
        prefs.getString("pref_gemini_model", GeminiModelConfig.PRIMARY_MODEL)
            ?: GeminiModelConfig.PRIMARY_MODEL
    )
    val geminiApiTestStatus = MutableStateFlow<String?>(null)
    val isTestingGeminiApi = MutableStateFlow(false)

    fun getAiApiKey(provider: AiProvider): String {
        return prefs.getString(
            "pref_ai_key_${provider.id}",
            if (provider == AiProvider.GOOGLE_GEMINI) geminiApiKey.value else ""
        ) ?: ""
    }

    fun getAiModel(provider: AiProvider): String {
        return prefs.getString(
            "pref_ai_model_${provider.id}",
            if (provider == AiProvider.GOOGLE_GEMINI) geminiModel.value else provider.defaultModel
        ) ?: provider.defaultModel
    }

    fun getAiBaseUrl(provider: AiProvider): String {
        return prefs.getString("pref_ai_base_url_${provider.id}", provider.defaultBaseUrl)
            ?: provider.defaultBaseUrl
    }

    fun setAiProvider(provider: AiProvider) {
        aiProvider.value = provider
        prefs.edit().putString("pref_ai_provider", provider.name).commit()
        UniversalAiRecipeService.setProvider(provider)
    }

    fun setAiApiKey(provider: AiProvider, key: String) {
        val sanitized = if (provider == AiProvider.GOOGLE_GEMINI) GeminiClient.sanitizeApiKey(key) else key.trim()
        prefs.edit().putString("pref_ai_key_${provider.id}", sanitized).commit()
        if (provider == AiProvider.GOOGLE_GEMINI) {
            geminiApiKey.value = sanitized
            prefs.edit().putString("pref_gemini_api_key", sanitized).commit()
            GeminiClient.setCustomApiKey(sanitized)
        }
        UniversalAiRecipeService.setApiKey(provider, sanitized)
    }

    fun setAiModel(provider: AiProvider, model: String) {
        val trimmed = model.trim()
        prefs.edit().putString("pref_ai_model_${provider.id}", trimmed).commit()
        if (provider == AiProvider.GOOGLE_GEMINI) {
            geminiModel.value = trimmed
            prefs.edit().putString("pref_gemini_model", trimmed).commit()
            GeminiClient.setCustomModel(trimmed)
        }
        UniversalAiRecipeService.setModel(provider, trimmed)
    }

    fun setAiBaseUrl(provider: AiProvider, url: String) {
        val trimmed = url.trim()
        prefs.edit().putString("pref_ai_base_url_${provider.id}", trimmed).commit()
        UniversalAiRecipeService.setCustomBaseUrl(provider, trimmed)
    }

    fun setGeminiApiKey(key: String) {
        setAiApiKey(AiProvider.GOOGLE_GEMINI, key)
    }

    fun setGeminiModel(model: String) {
        setAiModel(AiProvider.GOOGLE_GEMINI, model)
    }

    fun testGeminiApiConnection() {
        testAiConnection(aiProvider.value)
    }

    fun testAiConnection(provider: AiProvider = aiProvider.value) {
        vm.viewModelScope.launch {
            isTestingGeminiApi.value = true
            geminiApiTestStatus.value = "Testing ${provider.displayName} API connection..."
            try {
                val key = getAiApiKey(provider)
                val model = getAiModel(provider)
                val baseUrl = getAiBaseUrl(provider)
                UniversalAiRecipeService.setProvider(provider)
                UniversalAiRecipeService.setApiKey(provider, key)
                UniversalAiRecipeService.setModel(provider, model)
                UniversalAiRecipeService.setCustomBaseUrl(provider, baseUrl)

                val res = UniversalAiRecipeService.testConnection(provider)
                if (res.isSuccess) {
                    val statusText = res.getOrNull() ?: "✓ Connected to ${provider.displayName} successfully!"
                    geminiApiTestStatus.value = statusText
                } else {
                    val err = res.exceptionOrNull()?.localizedMessage ?: "Connection failed"
                    geminiApiTestStatus.value = "✗ $err"
                }
            } catch (e: Exception) {
                geminiApiTestStatus.value = "✗ Error: ${e.localizedMessage ?: "Invalid API connection"}"
            }
            isTestingGeminiApi.value = false
        }
    }

    private fun getSanitizedComfyCheckpoint(): String {
        val stored = prefs.getString("pref_comfy_ui_ckpt", "qwen_image_fp8_e4m3fn.safetensors")
            ?: "qwen_image_fp8_e4m3fn.safetensors"
        if (stored.contains("z_image", ignoreCase = true) ||
            stored.contains("z-image", ignoreCase = true) ||
            stored.contains("dr34m", ignoreCase = true) ||
            stored.contains("ltx", ignoreCase = true)
        ) {
            prefs.edit().putString("pref_comfy_ui_ckpt", "qwen_image_fp8_e4m3fn.safetensors").commit()
            return "qwen_image_fp8_e4m3fn.safetensors"
        }
        return stored
    }

    val imageGenEngine = MutableStateFlow(
        try {
            ImageGenEngine.valueOf(
                prefs.getString("pref_image_gen_engine", ImageGenEngine.COMFY_UI.name)
                    ?: ImageGenEngine.COMFY_UI.name
            )
        } catch (e: Exception) {
            ImageGenEngine.COMFY_UI
        }
    )

    val comfyUiUrl = MutableStateFlow(
        prefs.getString("pref_comfy_ui_url", null)?.let { saved ->
            if (saved == "http://192.168.1.54:8188" || saved.isBlank()) "https://api.cosmowhisper.com/comfyui" else saved
        } ?: "https://api.cosmowhisper.com/comfyui"
    )
    val comfyUiCheckpoint = MutableStateFlow(getSanitizedComfyCheckpoint())
    val comfyUiCustomWorkflow = MutableStateFlow(prefs.getString("pref_comfy_ui_custom_workflow", "") ?: "")
    val comfyUiTestStatus = MutableStateFlow<String?>(null)
    val isTestingComfyConnection = MutableStateFlow(false)
    val availableComfyCheckpoints = MutableStateFlow<List<String>>(emptyList())

    val syncConfig = repository.syncConfig
    val syncManager = repository.syncManager

    val isCloudSyncEnabled = MutableStateFlow(syncConfig?.isSyncEnabled ?: false)
    val syncServerUrl = MutableStateFlow(syncConfig?.serverUrl ?: "")
    val syncSecretToken = MutableStateFlow(syncConfig?.syncToken ?: "")
    val isAutoSyncWifi = MutableStateFlow(syncConfig?.autoSyncWifi ?: false)
    val isTestingSyncConnection = MutableStateFlow(false)
    val syncConnectionTestResult = MutableStateFlow<Pair<Boolean, String>?>(null)

    val isSyncing: StateFlow<Boolean> = syncManager?.isSyncing ?: MutableStateFlow(false)
    val lastSyncStatus: StateFlow<String?> = syncManager?.lastSyncStatus ?: MutableStateFlow(null)
    val lastSyncTimestamp: StateFlow<Long> = syncManager?.lastSyncTimestamp ?: MutableStateFlow(0L)

    fun setCloudSyncEnabled(enabled: Boolean) {
        isCloudSyncEnabled.value = enabled
        syncConfig?.isSyncEnabled = enabled
        if (enabled && syncManager != null && syncConfig?.serverUrl?.isNotBlank() == true) {
            triggerSyncNow()
        }
    }

    fun setSyncServerUrl(url: String) {
        syncServerUrl.value = url.trim()
        syncConfig?.serverUrl = url.trim()
        syncConnectionTestResult.value = null
    }

    fun setSyncSecretToken(token: String) {
        syncSecretToken.value = token.trim()
        syncConfig?.syncToken = token.trim()
        syncConnectionTestResult.value = null
    }

    fun setAutoSyncWifi(enabled: Boolean) {
        isAutoSyncWifi.value = enabled
        syncConfig?.autoSyncWifi = enabled
    }

    fun testSyncConnection() {
        val manager = syncManager ?: return
        vm.viewModelScope.launch {
            isTestingSyncConnection.value = true
            syncConnectionTestResult.value = null
            val result = manager.testConnection()
            if (result.isSuccess) {
                syncConnectionTestResult.value = Pair(true, result.getOrNull() ?: "Connected successfully (Server Online)")
            } else {
                syncConnectionTestResult.value = Pair(false, result.exceptionOrNull()?.message ?: "Connection Failed")
            }
            isTestingSyncConnection.value = false
        }
    }

    fun triggerSyncNow() {
        val manager = syncManager ?: return
        vm.viewModelScope.launch {
            manager.performSync()
        }
    }

    fun onAppForegroundResume() {
        syncManager?.triggerImmediateSync()
    }

    fun setImageGenEngine(engine: ImageGenEngine) {
        imageGenEngine.value = engine
        prefs.edit().putString("pref_image_gen_engine", engine.name).apply()
    }

    fun setComfyUiUrl(url: String) {
        comfyUiUrl.value = url.trim()
        prefs.edit().putString("pref_comfy_ui_url", url.trim()).apply()
    }

    fun setComfyUiCheckpoint(ckpt: String) {
        comfyUiCheckpoint.value = ckpt.trim()
        prefs.edit().putString("pref_comfy_ui_ckpt", ckpt.trim()).apply()
    }

    fun setComfyUiCustomWorkflow(workflowJson: String) {
        comfyUiCustomWorkflow.value = workflowJson.trim()
        prefs.edit().putString("pref_comfy_ui_custom_workflow", workflowJson.trim()).apply()
    }

    fun testComfyUiConnection() {
        vm.viewModelScope.launch {
            isTestingComfyConnection.value = true
            comfyUiTestStatus.value = "Testing connection to ${comfyUiUrl.value}..."
            val models = ComfyUiClient.fetchAvailableCheckpoints(comfyUiUrl.value)
            if (models.isNotEmpty()) {
                availableComfyCheckpoints.value = models
                if (!models.contains(comfyUiCheckpoint.value)) {
                    val best = models.firstOrNull {
                        it.contains("turbo", ignoreCase = true) ||
                        it.contains("lightning", ignoreCase = true) ||
                        it.contains("xl", ignoreCase = true) ||
                        it.contains("qwen", ignoreCase = true) ||
                        it.contains("sd", ignoreCase = true) ||
                        it.contains("v1", ignoreCase = true) ||
                        it.contains("dr34m", ignoreCase = true)
                    } ?: models.firstOrNull { !it.contains("audio", ignoreCase = true) } ?: models.first()
                    setComfyUiCheckpoint(best)
                }
            }
            val res = ComfyUiClient.testConnection(comfyUiUrl.value)
            if (res.isSuccess) {
                val msg = res.getOrNull() ?: "Connected to ComfyUI successfully!"
                comfyUiTestStatus.value = "✓ $msg"
                setImageGenEngine(ImageGenEngine.COMFY_UI)
            } else {
                comfyUiTestStatus.value = "✗ ${res.exceptionOrNull()?.localizedMessage ?: "Failed to connect"}"
            }
            isTestingComfyConnection.value = false
        }
    }
}
