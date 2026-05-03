package com.vsa.visualsemanticagent.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import timber.log.Timber
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var initialized = false
    private var initializationFailed = false
    private var pendingSpeech: PendingSpeech? = null

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
        if (!initialized) {
            initializationFailed = true
            pendingSpeech = null
            Timber.e("TextToSpeech initialization failed: $status")
            return
        }

        initializationFailed = false
        val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Timber.w("Simplified Chinese TTS is unavailable, falling back to default locale")
        }
        tts.setSpeechRate(1.0f)
        pendingSpeech?.let { speakNow(it.text, it.flush) }
        pendingSpeech = null
    }

    fun speak(text: String, flush: Boolean = true) {
        val normalizedText = text.trim()
        if (normalizedText.isEmpty()) return

        if (!initialized) {
            if (initializationFailed) {
                Timber.w("TTS is unavailable, dropping utterance")
                return
            }
            pendingSpeech = PendingSpeech(normalizedText, flush)
            return
        }
        speakNow(normalizedText, flush)
    }

    private fun speakNow(text: String, flush: Boolean) {
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts.speak(text, queueMode, Bundle(), "vsa-${System.currentTimeMillis()}")
    }

    fun stop() {
        if (initialized) {
            tts.stop()
        }
    }

    fun release() {
        tts.stop()
        tts.shutdown()
    }

    private data class PendingSpeech(
        val text: String,
        val flush: Boolean
    )
}
