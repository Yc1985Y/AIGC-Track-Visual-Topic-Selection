package com.vsa.visualsemanticagent

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCaptureException
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
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import com.vsa.visualsemanticagent.camera.CameraManager
import com.vsa.visualsemanticagent.decision.ExecutableIntent
import com.vsa.visualsemanticagent.decision.ExecutionMode
import com.vsa.visualsemanticagent.decision.ExecutionSuggestion
import com.vsa.visualsemanticagent.decision.VisualActionIntentSchema
import com.vsa.visualsemanticagent.decision.RiskPolicyEngine
import com.vsa.visualsemanticagent.intent.ActivityNotFoundException
import com.vsa.visualsemanticagent.intent.IntentDispatcher
import com.vsa.visualsemanticagent.model.VLMResponse
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
import java.io.File
import java.io.FileOutputStream
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
    private var cameraAvailable by mutableStateOf(true)
    private var lastError by mutableStateOf("")
    private var showErrorOverlay by mutableStateOf(false)
    private var currentRecoveryAction by mutableStateOf(RecoveryAction.NONE)
    private var pendingExecutableIntent by mutableStateOf<ExecutableIntent?>(null)
    private var pendingExecutionSuggestion by mutableStateOf<ExecutionSuggestion?>(null)
    private var showConfirmationCard by mutableStateOf(false)
    private var appInitialized = false

    private lateinit var intentDispatcher: IntentDispatcher
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var vlmNetworkClient: VLMNetworkClient

    private val riskPolicyEngine = RiskPolicyEngine()
    private val presets = PromptPresets.defaults

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true || hasPermission(Manifest.permission.CAMERA)
        audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] == true || hasPermission(Manifest.permission.RECORD_AUDIO)

        if (hasCaptureAccess()) {
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
        if (BuildConfig.VLM_USE_MOCK) {
            initializeAppIfNeeded()
        } else {
            requestPermissions(requestCamera = true, requestAudio = false)
        }

        setContent {
            MainScreen(
                commandText = commandText,
                onCommandChanged = { commandText = it },
                onCapture = { onCaptureButtonClicked() },
                onVoice = { onVoiceButtonClicked() },
                onPreset = { onPresetSelected(it) },
                onConfirmExecution = { onConfirmExecutionClicked() },
                onCancelExecution = { onCancelExecutionClicked() },
                bindPreview = { bindPreview(it) },
                presets = presets,
                showLivePreview = !BuildConfig.VLM_USE_MOCK,
                isLoading = isLoading,
                isVoiceListening = isVoiceListening,
                isCameraAvailable = cameraAvailable,
                loadingStage = loadingStage,
                statusText = statusText,
                resultText = resultText,
                confirmationIntent = pendingExecutableIntent,
                confirmationSuggestion = pendingExecutionSuggestion,
                showConfirmationCard = showConfirmationCard,
                showErrorOverlay = showErrorOverlay,
                errorText = lastError,
                showRetry = currentRecoveryAction != RecoveryAction.NONE,
                retryText = getRetryButtonText(),
                onRetry = { onRetryRequested() },
                onDismissError = { clearErrorState() }
            )
        }

        if (BuildConfig.VLM_USE_MOCK) {
            window.decorView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            scheduleDebugSnapshot("mock-home")
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
        if (hasCaptureAccess()) {
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
            if (requestCamera && !BuildConfig.VLM_USE_MOCK && !cameraPermissionGranted) {
                add(Manifest.permission.CAMERA)
            }
            if (requestAudio && !audioPermissionGranted) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }

        if (permissions.isEmpty()) {
            if (hasCaptureAccess()) {
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
            apiEndpoint = BuildConfig.VLM_API_ENDPOINT,
            useMockMode = BuildConfig.VLM_USE_MOCK
        )
        if (audioPermissionGranted) {
            voiceRecognitionManager.initialize()
        }
        statusText = if (BuildConfig.VLM_USE_MOCK) {
            getString(R.string.mock_mode_ready)
        } else {
            ""
        }
        resultText = ""
        appInitialized = true
    }

    private fun bindPreview(previewView: PreviewView) {
        if (BuildConfig.VLM_USE_MOCK || !cameraPermissionGranted) return
        CameraManager.bindCamera(this, this, previewView) { available ->
            cameraAvailable = available
            if (!available && BuildConfig.VLM_USE_MOCK) {
                statusText = getString(R.string.camera_preview_unavailable_mock_hint)
            }
        }
    }

    private fun onPresetSelected(preset: PromptPreset) {
        commandText = preset.prompt
        statusText = "已选择场景：${preset.label}"
    }

    private fun onVoiceButtonClicked() {
        if (isLoading || isVoiceListening) return
        if (!hasCaptureAccess()) {
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
                if (e is CancellationException) throw e
                Timber.e(e, "Voice capture failed")
                handleError(e, RecoveryAction.RETRY_VOICE)
            } finally {
                isVoiceListening = false
            }
        }
    }

    private fun onCaptureButtonClicked() {
        if (isLoading || isVoiceListening) return
        if (!hasCaptureAccess()) {
            showError(
                message = getString(R.string.camera_permission_missing),
                recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
            )
            requestPermissions(requestCamera = true, requestAudio = false)
            return
        }
        initializeAppIfNeeded()
        if (!BuildConfig.VLM_USE_MOCK && BuildConfig.VLM_API_KEY.isBlank()) {
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
        pendingExecutableIntent = null
        pendingExecutionSuggestion = null
        showConfirmationCard = false
        clearErrorState()

        lifecycleScope.launch {
            try {
                val finalCommand = commandText.ifBlank { getString(R.string.default_command) }
                val base64Image = if (BuildConfig.VLM_USE_MOCK && !CameraManager.isCaptureReady()) {
                    ""
                } else {
                    CameraManager.captureBase64Image()
                }

                loadingStage = 1
                val rawResponse = sendToVLM(base64Image, finalCommand)
                val response = ResponseInterpreter.normalize(rawResponse)
                val executableIntent = VisualActionIntentSchema.fromResponse(response)

                loadingStage = 2
                handleExecutionSuggestion(executableIntent)
                scheduleDebugSnapshot("mock-result")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
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

    private fun handleExecutionSuggestion(executableIntent: ExecutableIntent) {
        val suggestion = riskPolicyEngine.evaluate(executableIntent)
        pendingExecutableIntent = executableIntent
        pendingExecutionSuggestion = suggestion

        when (suggestion.mode) {
            ExecutionMode.DIRECT_TTS -> {
                statusText = suggestion.summary
                resultText = buildResultCardText(executableIntent, suggestion.summary, suggestion)
                showConfirmationCard = false
                playTextToSpeech(suggestion.prompt)
            }

            ExecutionMode.REQUIRE_CONFIRMATION -> {
                statusText = suggestion.summary
                resultText = buildResultCardText(executableIntent, suggestion.summary, suggestion)
                showConfirmationCard = true
                playTextToSpeech(suggestion.prompt)
            }

            ExecutionMode.REQUIRE_CLARIFICATION -> {
                statusText = suggestion.summary
                resultText = buildResultCardText(executableIntent, suggestion.summary, suggestion)
                showConfirmationCard = false
                playTextToSpeech(suggestion.prompt)
            }

            ExecutionMode.BLOCKED -> {
                statusText = suggestion.summary
                resultText = buildResultCardText(executableIntent, suggestion.summary, suggestion)
                showConfirmationCard = false
                playTextToSpeech(suggestion.prompt)
            }
        }
    }

    private fun onConfirmExecutionClicked() {
        val executableIntent = pendingExecutableIntent ?: return
        val suggestion = pendingExecutionSuggestion ?: return

        try {
            val dispatchResult = intentDispatcher.dispatchIntent(executableIntent)
            statusText = dispatchResult.summary
            resultText = buildResultCardText(executableIntent, dispatchResult.summary, suggestion)
            showConfirmationCard = false
            playTextToSpeech(dispatchResult.summary)
        } catch (e: Exception) {
            Timber.e(e, "Execution failed after confirmation")
            handleError(e, RecoveryAction.RETRY_CAPTURE)
        }
    }

    private fun onCancelExecutionClicked() {
        val suggestion = pendingExecutionSuggestion
        showConfirmationCard = false
        statusText = "已取消执行"
        resultText = buildString {
            append("已取消执行")
            if (suggestion != null) {
                append("\n")
                append(suggestion.prompt)
            }
        }
    }

    private fun buildResultCardText(
        intent: ExecutableIntent,
        summary: String,
        suggestion: ExecutionSuggestion
    ): String {
        val parts = linkedSetOf<String>()
        if (BuildConfig.VLM_USE_MOCK) {
            parts.add(getString(R.string.mock_mode_result_tag))
        }
        parts.add(summary)
        parts.add("动作：${intent.action}")
        parts.add("融合置信度：${"%.2f".format(intent.fusedConfidence)}")
        parts.add("执行模式：${suggestion.mode}")
        intent.title?.let { parts.add("标题：$it") }
        intent.time?.let { parts.add("时间：$it") }
        intent.location?.let { parts.add("地点：$it") }
        intent.phoneNumber?.let { parts.add("号码：$it") }
        intent.description?.let { parts.add("说明：$it") }
        intent.answer?.let { parts.add("播报：$it") }
        if (suggestion.validation.issues.isNotEmpty()) {
            parts.add("校验问题：${suggestion.validation.issues.joinToString()}")
        }
        parts.add("建议话术：${suggestion.prompt}")
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
        val errorCode = throwable.errorCode ?: message.substringAfterLast(": ", "").toIntOrNull()
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
            message.contains("No available camera can be found", ignoreCase = true) ->
                getString(R.string.camera_preview_unavailable_mock_hint)

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

    private fun hasCaptureAccess(): Boolean {
        return BuildConfig.VLM_USE_MOCK || cameraPermissionGranted
    }

    private fun scheduleDebugSnapshot(name: String) {
        if (!BuildConfig.DEBUG) return
        window.decorView.rootView.doOnLayout { root ->
            root.postDelayed(
                { captureDebugSnapshot(name) },
                1800L
            )
        }
    }

    private fun captureDebugSnapshot(name: String) {
        if (!BuildConfig.DEBUG) return
        runCatching {
            val bitmap = window.decorView.rootView.drawToBitmap(Bitmap.Config.ARGB_8888)
            val directory = File(filesDir, "debug_snapshots").apply { mkdirs() }
            FileOutputStream(File(directory, "$name.png")).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            bitmap.recycle()
        }.onFailure { error ->
            Timber.w(error, "Failed to capture debug snapshot: %s", name)
        }
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
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit,
    bindPreview: (PreviewView) -> Unit,
    presets: List<PromptPreset>,
    showLivePreview: Boolean,
    isLoading: Boolean,
    isVoiceListening: Boolean,
    isCameraAvailable: Boolean,
    loadingStage: Int,
    statusText: String,
    resultText: String,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    showErrorOverlay: Boolean,
    errorText: String,
    showRetry: Boolean,
    retryText: String,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    val surfaceColor = if (showLivePreview) {
        Color.Black
    } else {
        Color(0xFFEEF4FF)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreviewScreen(
                commandText = commandText,
                onCommandChanged = onCommandChanged,
                onCaptureClick = onCapture,
                onVoiceClick = onVoice,
                onPresetClick = onPreset,
                onConfirmExecution = onConfirmExecution,
                onCancelExecution = onCancelExecution,
                confirmationIntent = confirmationIntent,
                confirmationSuggestion = confirmationSuggestion,
                showConfirmationCard = showConfirmationCard,
                bindPreview = bindPreview,
                presets = presets,
                showLivePreview = showLivePreview,
                isLoading = isLoading,
                isVoiceListening = isVoiceListening,
                isCameraAvailable = isCameraAvailable,
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
