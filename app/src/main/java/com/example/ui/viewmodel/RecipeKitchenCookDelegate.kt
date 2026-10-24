package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.LanguageMode
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Delegate responsible for kitchen cook mode, timer, text-to-speech audio guidance,
 * serving multiplier, page sound effects, and cooking journal updates.
 */
class RecipeKitchenCookDelegate(
    private val vm: RecipeViewModel,
    private val app: Application,
    private val repository: RecipeRepository
) {
    private var soundPool: SoundPool? = null
    private var pageTurnSoundId: Int = 0

    private var tts: TextToSpeech? = null
    val isTtsReady = MutableStateFlow(false)
    val isTtsSpeaking = MutableStateFlow(false)
    private var currentSpeakingText: String? = null

    val timerSecondsRemaining = MutableStateFlow(0)
    val timerTotalSeconds = MutableStateFlow(0)
    val isTimerActive = MutableStateFlow(false)
    private var timerJob: Job? = null

    val servingMultiplier = MutableStateFlow(1.0f)

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()
            pageTurnSoundId = soundPool?.load(app, R.raw.page_turn, 1) ?: 0
        } catch (_: Exception) {}

        tts = TextToSpeech(app) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady.value = true
                tts?.language = Locale.ENGLISH
            }
        }
    }

    fun toggleSpeakStep(text: String, isGerman: Boolean, lang: LanguageMode = LanguageMode.ENGLISH) {
        if (!isTtsReady.value) return
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
                val wasSame = (currentSpeakingText == text)
                currentSpeakingText = null
                isTtsSpeaking.value = false
                if (wasSame) {
                    return
                }
            }

            val targetLocale = when {
                isGerman || lang == LanguageMode.GERMAN -> Locale.GERMAN
                lang == LanguageMode.FRENCH -> Locale.FRENCH
                lang == LanguageMode.ITALIAN -> Locale.ITALIAN
                lang == LanguageMode.SPANISH -> Locale("es", "ES")
                lang == LanguageMode.DUTCH -> Locale("nl", "NL")
                else -> Locale.ENGLISH
            }
            tts?.language = targetLocale
            currentSpeakingText = text
            isTtsSpeaking.value = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isTtsSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    isTtsSpeaking.value = false
                    currentSpeakingText = null
                }
                override fun onError(utteranceId: String?) {
                    isTtsSpeaking.value = false
                    currentSpeakingText = null
                }
            })

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "step_tts_${System.currentTimeMillis()}")
        } catch (_: Exception) {
            isTtsSpeaking.value = false
            currentSpeakingText = null
        }
    }

    fun speakStep(text: String, isGerman: Boolean) {
        toggleSpeakStep(text, isGerman, vm.languageMode.value)
    }

    fun stopTts() {
        try {
            tts?.stop()
            isTtsSpeaking.value = false
            currentSpeakingText = null
        } catch (_: Exception) {}
    }

    fun startTimerForStep(minutes: Int) {
        if (minutes <= 0) return
        timerJob?.cancel()
        timerTotalSeconds.value = minutes * 60
        timerSecondsRemaining.value = minutes * 60
        isTimerActive.value = true

        timerJob = vm.viewModelScope.launch {
            while (timerSecondsRemaining.value > 0 && isTimerActive.value) {
                delay(1000)
                timerSecondsRemaining.value -= 1
            }
            if (timerSecondsRemaining.value == 0 && isTimerActive.value) {
                isTimerActive.value = false
                triggerTimerChime()
            }
        }
    }

    fun pauseTimer() {
        isTimerActive.value = false
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (timerSecondsRemaining.value > 0) {
            isTimerActive.value = true
            timerJob = vm.viewModelScope.launch {
                while (timerSecondsRemaining.value > 0 && isTimerActive.value) {
                    delay(1000)
                    timerSecondsRemaining.value -= 1
                }
                if (timerSecondsRemaining.value == 0 && isTimerActive.value) {
                    isTimerActive.value = false
                    triggerTimerChime()
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        isTimerActive.value = false
        timerSecondsRemaining.value = 0
        timerTotalSeconds.value = 0
    }

    private fun triggerTimerChime() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
        } catch (_: Exception) {}
    }

    fun setServingMultiplier(multiplier: Float) {
        servingMultiplier.value = multiplier.coerceIn(0.25f, 6.0f)
    }

    fun incrementServingMultiplier() {
        val current = servingMultiplier.value
        val next = when {
            current < 1.0f -> current + 0.5f
            current < 3.0f -> current + 0.5f
            else -> current + 1.0f
        }
        setServingMultiplier(next)
    }

    fun decrementServingMultiplier() {
        val current = servingMultiplier.value
        val prev = when {
            current <= 0.5f -> 0.5f
            current <= 1.0f -> current - 0.5f
            current <= 3.0f -> current - 0.5f
            else -> current - 1.0f
        }
        setServingMultiplier(prev)
    }

    fun playPageTurnSound() {
        if (!vm.soundEffectsEnabled.value) return
        try {
            if (soundPool != null && pageTurnSoundId != 0) {
                soundPool?.play(pageTurnSoundId, 0.95f, 0.95f, 1, 0, 1.0f)
            } else {
                val toneG = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
                toneG.startTone(ToneGenerator.TONE_PROP_BEEP2, 45)
            }
        } catch (_: Exception) {}
    }

    fun saveRecipeJournal(recipeId: Long, notes: String, rating: Int) {
        val current = vm.selectedRecipe.value ?: return
        if (current.id == recipeId) {
            val updated = current.copy(notes = notes, rating = rating)
            vm.selectedRecipe.value = updated
            vm.viewModelScope.launch {
                repository.updateRecipe(updated)
            }
        }
    }

    fun markRecipeCooked() {
        val current = vm.selectedRecipe.value ?: return
        vm.viewModelScope.launch {
            repository.incrementCooked(current.id)
            vm.selectedRecipe.value = current.copy(timesCooked = current.timesCooked + 1)
        }
    }

    fun onCleared() {
        timerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }
}
