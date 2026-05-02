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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.vsa.visualsemanticagent.camera.CameraManager
import com.vsa.visualsemanticagent.intent.ActivityNotFoundException
import com.vsa.visualsemanticagent.intent.IntentDispatcher
import com.vsa.visualsemanticagent.model.VLMResponse
import androidx.camera.core.ImageCaptureException
import com.vsa.visualsemanticagent.network.VLMApiException
import com.vsa.visualsemanticagent.network.VLMNetworkClient
import com.vsa.visualsemanticagent.network.VLMNetworkException
import com.vsa.visualsemanticagent.network.VLMResponseParseException
import com.vsa.visualsemanticagent.tts.TextToSpeechManager
import com.vsa.visualsemanticagent.ui.CameraPreviewScreen
import com.vsa.visualsemanticagent.ui.ErrorOverlay
import com.vsa.visualsemanticagent.ui.LoadingOverlay
import com.vsa.visualsemanticagent.utils.PromptPreset
import com.vsa.visualsemanticagent.utils.PromptPresets
import com.vsa.visualsemanticagent.utils.ResponseInterpreter
import com.vsa.visualsemanticagent.voice.VoiceRecognitionException
import com.vsa.visualsemanticagent.voice.VoiceRecognitionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private enum class RecoveryAction {
        NONE,
        REQUEST_PERMISSIONS,
        RETRY_CAPTURE,
        RETRY_VOICE
    }

    private var isLoading by mutableStateOf(false)
    private var isVoiceListening by mutableStateOf(false)
    private var loadingStage by mutableStateOf(0)
    private var commandText by mutableStateOf("")
    private var statusText by mutableStateOf("")
    private var resultText by mutableStateOf("")
    private var cameraPermissionGranted by mutableStateOf(false)
    private var audioPermissionGranted by mutableStateOf(false)
    private var lastError by mutableStateOf("")
    private var showErrorOverlay by mutableStateOf(false)
    private var currentRecoveryAction by mutableStateOf(RecoveryAction.NONE)
    private var appInitialized = false

    private lateinit var intentDispatcher: IntentDispatcher
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var vlmNetworkClient: VLMNetworkClient

    private val presets = PromptPresets.defaults

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true || hasPermission(Manifest.permission.CAMERA)
        audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] == true || hasPermission(Manifest.permission.RECORD_AUDIO)

        if (cameraPermissionGranted) {
            initializeAppIfNeeded()
            if (!audioPermissionGranted) {
                statusText = getString(R.string.voice_permission_tip)
            } else if (statusText == getString(R.string.permissions_missing)) {
                statusText = ""
            }
            clearErrorState()
        } else {
            showError(
                message = getString(R.string.camera_permission_missing),
                recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        refreshPermissionState()
        requestPermissions(requestCamera = true, requestAudio = false)

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
                isVoiceListening = isVoiceListening,
                loadingStage = loadingStage,
                statusText = statusText,
                resultText = resultText,
                showErrorOverlay = showErrorOverlay,
                errorText = lastError,
                showRetry = currentRecoveryAction != RecoveryAction.NONE,
                retryText = getRetryButtonText(),
                onRetry = { onRetryRequested() },
                onDismissError = { clearErrorState() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
        if (cameraPermissionGranted) {
            initializeAppIfNeeded()
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

    private fun requestPermissions(
        requestCamera: Boolean = true,
        requestAudio: Boolean = true
    ) {
        refreshPermissionState()

        val permissions = buildList {
            if (requestCamera && !cameraPermissionGranted) {
                add(Manifest.permission.CAMERA)
            }
            if (requestAudio && !audioPermissionGranted) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }

        if (permissions.isEmpty()) {
            if (cameraPermissionGranted) {
                initializeAppIfNeeded()
            }
            return
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun initializeAppIfNeeded() {
        if (appInitialized) {
            if (::voiceRecognitionManager.isInitialized && audioPermissionGranted) {
                voiceRecognitionManager.initialize()
            }
            return
        }

        intentDispatcher = IntentDispatcher(this)
        voiceRecognitionManager = VoiceRecognitionManager(this)
        textToSpeechManager = TextToSpeechManager(this)
        vlmNetworkClient = VLMNetworkClient(
            apiKey = BuildConfig.VLM_API_KEY,
            modelName = BuildConfig.VLM_MODEL_NAME,
            apiEndpoint = BuildConfig.VLM_API_ENDPOINT
        )
        if (audioPermissionGranted) {
            voiceRecognitionManager.initialize()
        }
        statusText = ""
        resultText = ""
        appInitialized = true
    }

    private fun bindPreview(previewView: PreviewView) {
        if (!cameraPermissionGranted) return
        CameraManager.bindCamera(this, this, previewView)
    }

    private fun onPresetSelected(preset: PromptPreset) {
        commandText = preset.prompt
        statusText = "已选择场景：${preset.label}"
    }

    private fun onVoiceButtonClicked() {
        if (isVoiceListening) return
        if (!cameraPermissionGranted) {
            showError(
                message = getString(R.string.camera_permission_missing),
                recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
            )
            requestPermissions(requestCamera = true, requestAudio = false)
            return
        }
        if (!audioPermissionGranted) {
            showError(
                message = getString(R.string.microphone_permission_missing),
                recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
            )
            requestPermissions(requestCamera = false, requestAudio = true)
            return
        }
        if (!::voiceRecognitionManager.isInitialized) {
            initializeAppIfNeeded()
        }

        lifecycleScope.launch {
            try {
                isVoiceListening = true
                clearErrorState()
                statusText = getString(R.string.voice_listening)
                commandText = voiceRecognitionManager.listenOnce()
                statusText = "已识别语音指令"
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                Timber.e(e, "Voice capture failed")
                handleError(e, RecoveryAction.RETRY_VOICE)
            } finally {
                isVoiceListening = false
            }
        }
    }

    private fun onCaptureButtonClicked() {
        if (!cameraPermissionGranted) {
            showError(
                message = getString(R.string.camera_permission_missing),
                recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
            )
            requestPermissions(requestCamera = true, requestAudio = false)
            return
        }
        initializeAppIfNeeded()
        if (BuildConfig.VLM_API_KEY.isBlank()) {
            showError(
                message = getString(R.string.api_key_missing),
                recoveryAction = RecoveryAction.NONE
            )
            playTextToSpeech(statusText)
            return
        }

        isLoading = true
        loadingStage = 0
        resultText = ""
        clearErrorState()

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
                if (e is CancellationException) {
                    throw e
                }
                Timber.e(e, "Error during capture flow")
                handleError(e, RecoveryAction.RETRY_CAPTURE)
                if (lastError.isNotBlank()) {
                    playTextToSpeech(lastError)
                }
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
        val parts = linkedSetOf<String>()
        parts.add(summary)
        response.title?.let { parts.add("标题：$it") }
        response.time?.let { parts.add("时间：$it") }
        response.location?.let { parts.add("地点：$it") }
        response.description?.let { parts.add("描述：$it") }
        response.answer?.let { parts.add("回复：$it") }
        return parts.joinToString(separator = "\n")
    }

    private fun playTextToSpeech(text: String) {
        textToSpeechManager.speak(text)
    }

    private fun onRetryRequested() {
        val recoveryAction = currentRecoveryAction
        clearErrorState()
        when (recoveryAction) {
            RecoveryAction.REQUEST_PERMISSIONS -> requestPermissions(
                requestCamera = !cameraPermissionGranted,
                requestAudio = !audioPermissionGranted
            )
            RecoveryAction.RETRY_CAPTURE -> onCaptureButtonClicked()
            RecoveryAction.RETRY_VOICE -> onVoiceButtonClicked()
            RecoveryAction.NONE -> Unit
        }
    }

    private fun handleError(
        throwable: Throwable,
        recoveryAction: RecoveryAction
    ) {
        showError(
            message = resolveErrorMessage(throwable),
            recoveryAction = recoveryAction
        )
    }

    private fun showError(
        message: String,
        recoveryAction: RecoveryAction
    ) {
        statusText = message
        lastError = message
        currentRecoveryAction = recoveryAction
        showErrorOverlay = true
    }

    private fun clearErrorState() {
        lastError = ""
        currentRecoveryAction = RecoveryAction.NONE
        showErrorOverlay = false
    }

    private fun resolveErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is VLMNetworkException -> getString(R.string.network_error)
            is VLMApiException -> mapApiErrorMessage(throwable)
            is VLMResponseParseException -> getString(R.string.parse_error)
            is ActivityNotFoundException -> mapActivityNotFoundMessage(throwable)
            is ImageCaptureException -> getString(R.string.camera_capture_failed)
            is VoiceRecognitionException -> mapVoiceRecognitionMessage(throwable)
            is IllegalStateException -> mapIllegalStateMessage(throwable)
            is IllegalArgumentException -> mapIllegalArgumentMessage(throwable)
            else -> throwable.message ?: getString(R.string.error_occurred)
        }
    }

    private fun mapApiErrorMessage(throwable: VLMApiException): String {
        val responseBody = throwable.responseBody.lowercase()
        return when {
            throwable.code == 429 || responseBody.contains("rate limit") || responseBody.contains("429") -> {
                getString(R.string.model_rate_limited)
            }
            responseBody.contains("no model access permission") || responseBody.contains("permission expires") -> {
                getString(R.string.model_permission_denied)
            }
            responseBody.contains("today usage limit") -> {
                getString(R.string.model_daily_quota_exceeded)
            }
            else -> getString(R.string.model_error)
        }
    }

    private fun mapIllegalArgumentMessage(throwable: IllegalArgumentException): String {
        val message = throwable.message.orEmpty()
        return when {
            message.contains("Missing location", ignoreCase = true) -> getString(R.string.location_missing)
            message.contains("Missing phone number", ignoreCase = true) -> getString(R.string.phone_number_missing)
            message.contains("Missing sms content", ignoreCase = true) -> getString(R.string.sms_content_missing)
            else -> message.ifBlank { getString(R.string.error_occurred) }
        }
    }

    private fun mapVoiceRecognitionMessage(throwable: VoiceRecognitionException): String {
        val message = throwable.message.orEmpty()
        val errorCode = message.substringAfterLast(": ", "").toIntOrNull()
        return when {
            message.contains("unavailable", ignoreCase = true) -> getString(R.string.voice_not_supported)
            message.contains("no speech recognized", ignoreCase = true) -> getString(R.string.voice_capture_failed)
            errorCode == VoiceRecognitionManager.ERROR_RECOGNIZER_BUSY -> getString(R.string.voice_busy)
            errorCode == VoiceRecognitionManager.ERROR_AUDIO ||
                errorCode == VoiceRecognitionManager.ERROR_SERVER ||
                errorCode == VoiceRecognitionManager.ERROR_NETWORK ||
                errorCode == VoiceRecognitionManager.ERROR_NETWORK_TIMEOUT -> getString(R.string.voice_service_error)
            errorCode == VoiceRecognitionManager.ERROR_NO_MATCH ||
                errorCode == VoiceRecognitionManager.ERROR_SPEECH_TIMEOUT -> getString(R.string.voice_capture_failed)
            else -> getString(R.string.voice_capture_failed)
        }
    }

    private fun mapIllegalStateMessage(throwable: IllegalStateException): String {
        val message = throwable.message.orEmpty()
        return when {
            message.contains("Camera is not ready", ignoreCase = true) -> getString(R.string.camera_not_ready)
            message.contains("Voice recognition unavailable", ignoreCase = true) -> getString(R.string.voice_not_supported)
            message.contains("No speech recognized", ignoreCase = true) -> getString(R.string.voice_capture_failed)
            message.contains("Missing location", ignoreCase = true) -> getString(R.string.location_missing)
            message.contains("Missing phone number", ignoreCase = true) -> getString(R.string.phone_number_missing)
            message.contains("Missing sms content", ignoreCase = true) -> getString(R.string.sms_content_missing)
            else -> message.ifBlank { getString(R.string.error_occurred) }
        }
    }

    private fun mapActivityNotFoundMessage(throwable: ActivityNotFoundException): String {
        val message = throwable.message.orEmpty()
        return when {
            message.contains("Calendar", ignoreCase = true) -> getString(R.string.calendar_app_missing)
            message.contains("Map", ignoreCase = true) -> getString(R.string.map_app_missing)
            message.contains("SMS", ignoreCase = true) -> getString(R.string.sms_app_missing)
            else -> getString(R.string.error_occurred)
        }
    }

    private fun getRetryButtonText(): String {
        return when (currentRecoveryAction) {
            RecoveryAction.REQUEST_PERMISSIONS -> getString(R.string.retry_permission)
            RecoveryAction.RETRY_CAPTURE,
            RecoveryAction.RETRY_VOICE -> getString(R.string.retry)
            RecoveryAction.NONE -> getString(R.string.retry)
        }
    }

    private fun refreshPermissionState() {
        cameraPermissionGranted = hasPermission(Manifest.permission.CAMERA)
        audioPermissionGranted = hasPermission(Manifest.permission.RECORD_AUDIO)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
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
    isVoiceListening: Boolean,
    loadingStage: Int,
    statusText: String,
    resultText: String,
    showErrorOverlay: Boolean,
    errorText: String,
    showRetry: Boolean,
    retryText: String,
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
                isVoiceListening = isVoiceListening,
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
                showRetry = showRetry,
                retryText = retryText,
                onRetry = onRetry,
                onDismiss = onDismissError
            )
        }
    }
}
