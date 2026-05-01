package com.vsa.visualsemanticagent

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.vsa.visualsemanticagent.camera.CameraManager
import com.vsa.visualsemanticagent.intent.IntentDispatcher
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.network.VLMNetworkClient
import com.vsa.visualsemanticagent.tts.TextToSpeechManager
import com.vsa.visualsemanticagent.ui.CameraPreviewScreen
import com.vsa.visualsemanticagent.ui.ErrorOverlay
import com.vsa.visualsemanticagent.ui.LoadingOverlay
import com.vsa.visualsemanticagent.utils.PromptPreset
import com.vsa.visualsemanticagent.utils.PromptPresets
import com.vsa.visualsemanticagent.utils.ResponseInterpreter
import com.vsa.visualsemanticagent.voice.VoiceRecognitionManager
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private var isLoading by mutableStateOf(false)
    private var loadingStage by mutableStateOf(0)
    private var commandText by mutableStateOf("")
    private var statusText by mutableStateOf("")
    private var resultText by mutableStateOf("")
    private var permissionsGranted by mutableStateOf(false)
    private var lastError by mutableStateOf("")
    private var showErrorOverlay by mutableStateOf(false)

    private lateinit var intentDispatcher: IntentDispatcher
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var vlmNetworkClient: VLMNetworkClient

    private val presets = PromptPresets.defaults

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions[Manifest.permission.CAMERA] == true &&
            permissions[Manifest.permission.RECORD_AUDIO] == true

        if (permissionsGranted) {
            initializeApp()
        } else {
            statusText = getString(R.string.permissions_missing)
            lastError = statusText
            showErrorOverlay = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        requestPermissions()

        setContent {
            MainScreen(
                commandText = commandText,
                onCommandChanged = { commandText = it },
                onCapture = { onCaptureButtonClicked() },
                onVoice = { onVoiceButtonClicked() },
                onPreset = { onPresetSelected(it) },
                bindPreview = { bindPreview(it) },
                presets = presets,
                isLoading = isLoading,
                loadingStage = loadingStage,
                statusText = statusText,
                resultText = resultText,
                showErrorOverlay = showErrorOverlay,
                errorText = lastError,
                onRetry = {
                    showErrorOverlay = false
                    onCaptureButtonClicked()
                },
                onDismissError = { showErrorOverlay = false }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraManager.shutdown()
        if (::voiceRecognitionManager.isInitialized) {
            voiceRecognitionManager.release()
        }
        if (::textToSpeechManager.isInitialized) {
            textToSpeechManager.release()
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun initializeApp() {
        intentDispatcher = IntentDispatcher(this)
        voiceRecognitionManager = VoiceRecognitionManager(this)
        textToSpeechManager = TextToSpeechManager(this)
        vlmNetworkClient = VLMNetworkClient(
            apiKey = BuildConfig.VLM_API_KEY,
            modelName = BuildConfig.VLM_MODEL_NAME,
            apiEndpoint = BuildConfig.VLM_API_ENDPOINT
        )
        voiceRecognitionManager.initialize()
        statusText = ""
        resultText = ""
    }

    private fun bindPreview(previewView: PreviewView) {
        if (!permissionsGranted) return
        CameraManager.bindCamera(this, this, previewView)
    }

    private fun onPresetSelected(preset: PromptPreset) {
        commandText = preset.prompt
        statusText = "已选择场景：${preset.label}"
    }

    private fun onVoiceButtonClicked() {
        if (!permissionsGranted || !::voiceRecognitionManager.isInitialized) return

        lifecycleScope.launch {
            try {
                showErrorOverlay = false
                statusText = getString(R.string.voice_listening)
                commandText = voiceRecognitionManager.listenOnce()
                statusText = "已识别语音指令"
            } catch (e: Exception) {
                Timber.e(e, "Voice capture failed")
                lastError = getString(R.string.voice_not_supported)
                statusText = lastError
                showErrorOverlay = true
            }
        }
    }

    private fun onCaptureButtonClicked() {
        if (!permissionsGranted) {
            statusText = getString(R.string.permissions_missing)
            lastError = statusText
            showErrorOverlay = true
            return
        }
        if (BuildConfig.VLM_API_KEY.isBlank()) {
            statusText = getString(R.string.api_key_missing)
            lastError = statusText
            showErrorOverlay = true
            playTextToSpeech(statusText)
            return
        }

        isLoading = true
        loadingStage = 0
        showErrorOverlay = false

        lifecycleScope.launch {
            try {
                loadingStage = 0
                val base64Image = CameraManager.captureBase64Image()
                val finalCommand = commandText.ifBlank { getString(R.string.default_command) }

                loadingStage = 1
                val rawResponse = sendToVLM(base64Image, finalCommand)
                val response = ResponseInterpreter.normalize(rawResponse)

                loadingStage = 2
                val dispatchResult = dispatchAction(response)
                val status = ResponseInterpreter.buildStatusMessage(response, dispatchResult.summary)
                val speechText = ResponseInterpreter.buildSpeechText(response, dispatchResult.summary)

                statusText = status
                resultText = buildResultCardText(response, dispatchResult.summary)
                if (!speechText.isNullOrBlank()) {
                    playTextToSpeech(speechText)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during capture flow")
                lastError = e.message ?: getString(R.string.error_occurred)
                statusText = lastError
                showErrorOverlay = true
                playFallbackMessage()
            } finally {
                isLoading = false
                loadingStage = 0
            }
        }
    }

    private suspend fun sendToVLM(base64Image: String, userText: String): VLMResponse {
        return vlmNetworkClient.sendMultimodalRequest(base64Image, userText)
    }

    private fun dispatchAction(response: VLMResponse): IntentDispatcher.DispatchResult {
        return intentDispatcher.dispatchIntent(response)
    }

    private fun buildResultCardText(response: VLMResponse, summary: String): String {
        val parts = buildList {
            add(summary)
            response.title?.let { add("标题：$it") }
            response.time?.let { add("时间：$it") }
            response.location?.let { add("地点：$it") }
            response.description?.let { add("描述：$it") }
            response.answer?.let { add("回复：$it") }
        }
        return parts.joinToString(separator = "\n")
    }

    private fun playTextToSpeech(text: String) {
        textToSpeechManager.speak(text)
    }

    private fun playFallbackMessage() {
        playTextToSpeech(getString(R.string.fallback_message))
    }
}

@Composable
fun MainScreen(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onCapture: () -> Unit,
    onVoice: () -> Unit,
    onPreset: (PromptPreset) -> Unit,
    bindPreview: (PreviewView) -> Unit,
    presets: List<PromptPreset>,
    isLoading: Boolean,
    loadingStage: Int,
    statusText: String,
    resultText: String,
    showErrorOverlay: Boolean,
    errorText: String,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreviewScreen(
                commandText = commandText,
                onCommandChanged = onCommandChanged,
                onCaptureClick = onCapture,
                onVoiceClick = onVoice,
                onPresetClick = onPreset,
                bindPreview = bindPreview,
                presets = presets,
                isLoading = isLoading,
                statusText = statusText,
                resultText = resultText
            )

            LoadingOverlay(
                isVisible = isLoading,
                currentStage = loadingStage
            )

            ErrorOverlay(
                isVisible = showErrorOverlay,
                errorMessage = errorText,
                onRetry = onRetry,
                onDismiss = onDismissError
            )
        }
    }
}
