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
import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.network.VLMNetworkClient
import com.vsa.visualsemanticagent.tts.TextToSpeechManager
import com.vsa.visualsemanticagent.ui.CameraPreviewScreen
import com.vsa.visualsemanticagent.ui.LoadingOverlay
import com.vsa.visualsemanticagent.voice.VoiceRecognitionManager
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private var isLoading by mutableStateOf(false)
    private var loadingStage by mutableStateOf(0)
    private var commandText by mutableStateOf("")
    private var statusText by mutableStateOf("")
    private var permissionsGranted by mutableStateOf(false)

    private lateinit var intentDispatcher: IntentDispatcher
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var vlmNetworkClient: VLMNetworkClient

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions[Manifest.permission.CAMERA] == true &&
            permissions[Manifest.permission.RECORD_AUDIO] == true

        if (permissionsGranted) {
            initializeApp()
        } else {
            statusText = getString(R.string.permissions_missing)
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
                bindPreview = { bindPreview(it) },
                isLoading = isLoading,
                loadingStage = loadingStage,
                statusText = statusText
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
    }

    private fun bindPreview(previewView: PreviewView) {
        if (!permissionsGranted) return
        CameraManager.bindCamera(this, this, previewView)
    }

    private fun onVoiceButtonClicked() {
        if (!permissionsGranted || !::voiceRecognitionManager.isInitialized) return

        lifecycleScope.launch {
            try {
                statusText = getString(R.string.voice_listening)
                commandText = voiceRecognitionManager.listenOnce()
                statusText = commandText
            } catch (e: Exception) {
                Timber.e(e, "Voice capture failed")
                statusText = getString(R.string.voice_not_supported)
            }
        }
    }

    private fun onCaptureButtonClicked() {
        if (!permissionsGranted) {
            statusText = getString(R.string.permissions_missing)
            return
        }
        if (BuildConfig.VLM_API_KEY.isBlank()) {
            statusText = getString(R.string.api_key_missing)
            playTextToSpeech(statusText)
            return
        }

        isLoading = true
        loadingStage = 0

        lifecycleScope.launch {
            try {
                loadingStage = 0
                val base64Image = CameraManager.captureBase64Image()
                val finalCommand = commandText.ifBlank { getString(R.string.default_command) }

                loadingStage = 1
                val response = sendToVLM(base64Image, finalCommand)

                loadingStage = 2
                dispatchAction(response)
                handleResponseFeedback(response)
            } catch (e: Exception) {
                Timber.e(e, "Error during capture flow")
                statusText = e.message ?: getString(R.string.error_occurred)
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

    private fun dispatchAction(response: VLMResponse) {
        intentDispatcher.dispatchIntent(response)
    }

    private fun handleResponseFeedback(response: VLMResponse) {
        statusText = buildStatusMessage(response)
        if (response.action == ModelConstants.ACTION_TTS_FEEDBACK || !response.answer.isNullOrBlank()) {
            playTextToSpeech(response.answer ?: response.description ?: statusText)
        }
    }

    private fun buildStatusMessage(response: VLMResponse): String {
        return when (response.action) {
            ModelConstants.ACTION_CREATE_EVENT -> "已识别活动信息，正在打开日历。"
            ModelConstants.ACTION_NAVIGATE -> "已识别地点，正在打开地图导航。"
            ModelConstants.ACTION_SEND_SMS -> "已准备短信内容。"
            ModelConstants.ACTION_TTS_FEEDBACK -> response.answer ?: response.description ?: "已完成语义反馈。"
            else -> response.answer ?: response.description ?: "无法确定动作，已返回说明。"
        }
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
    bindPreview: (PreviewView) -> Unit,
    isLoading: Boolean,
    loadingStage: Int,
    statusText: String
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
                bindPreview = bindPreview,
                isLoading = isLoading,
                statusText = statusText
            )

            LoadingOverlay(
                isVisible = isLoading,
                currentStage = loadingStage
            )
        }
    }
}
