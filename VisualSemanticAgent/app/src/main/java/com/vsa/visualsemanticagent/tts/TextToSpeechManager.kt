package com.vsa.visualsemanticagent.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import timber.log.Timber
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var initialized = false
    private val pendingTexts = mutableListOf<String>()

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
        if (!initialized) {
            Timber.e("TextToSpeech initialization failed: $status")
            return
        }

        val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Timber.w("Simplified Chinese TTS is unavailable, falling back to default locale")
        }
        tts.setSpeechRate(1.0f)
        pendingTexts.forEach { speak(it) }
        pendingTexts.clear()
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!initialized) {
            pendingTexts.add(text)
            return
        }
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
}
