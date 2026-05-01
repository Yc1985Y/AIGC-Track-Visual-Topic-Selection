package com.vsa.visualsemanticagent.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VoiceRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun initialize() {
        if (speechRecognizer == null && SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    suspend fun listenOnce(languageTag: String = "zh-CN"): String {
        initialize()
        val recognizer = speechRecognizer ?: throw VoiceRecognitionException("Voice recognition unavailable")

        return suspendCancellableCoroutine { continuation ->
            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onError(error: Int) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(VoiceRecognitionException("Speech recognition failed: $error"))
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim().orEmpty()
                    if (text.isBlank()) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(VoiceRecognitionException("No speech recognized"))
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(text)
                        }
                    }
                }
            }

            recognizer.setRecognitionListener(listener)
            recognizer.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
            )

            continuation.invokeOnCancellation {
                recognizer.cancel()
            }
        }
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

class VoiceRecognitionException(message: String) : IllegalStateException(message)
