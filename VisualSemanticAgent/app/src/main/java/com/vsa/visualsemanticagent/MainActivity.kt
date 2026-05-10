package com.vsa.visualsemanticagent

import android.Manifest
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.vsa.visualsemanticagent.camera.CameraManager
import com.vsa.visualsemanticagent.decision.ExecutableIntent
import com.vsa.visualsemanticagent.decision.ExecutionMode
import com.vsa.visualsemanticagent.decision.ExecutionSuggestion
import com.vsa.visualsemanticagent.decision.RiskPolicyEngine
import com.vsa.visualsemanticagent.decision.VisualActionIntentSchema
import com.vsa.visualsemanticagent.input.CampusNoticeInput
import com.vsa.visualsemanticagent.input.NoticeSourceType
import com.vsa.visualsemanticagent.intent.ActivityNotFoundException
import com.vsa.visualsemanticagent.intent.IntentDispatcher
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.network.VLMApiException
import com.vsa.visualsemanticagent.network.VLMNetworkClient
import com.vsa.visualsemanticagent.network.VLMNetworkException
import com.vsa.visualsemanticagent.network.VLMResponseParseException
import com.vsa.visualsemanticagent.storage.AppPreferencesStore
import com.vsa.visualsemanticagent.reminder.ReminderScheduler
import com.vsa.visualsemanticagent.tts.TextToSpeechManager
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.defaultAgendaReminders
import com.vsa.visualsemanticagent.plan.scheduleDate
import com.vsa.visualsemanticagent.ui.ErrorOverlay
import com.vsa.visualsemanticagent.ui.LoadingOverlay
import com.vsa.visualsemanticagent.ui.StartupIntroScreen
import com.vsa.visualsemanticagent.ui.TimelineTransferOverlay
import com.vsa.visualsemanticagent.ui.home.HomeScreenModule
import com.vsa.visualsemanticagent.ui.profile.ProfileScreenModule
import com.vsa.visualsemanticagent.ui.timeline.TimelineScreenModule
import com.vsa.visualsemanticagent.utils.PromptPreset
import com.vsa.visualsemanticagent.utils.PromptPresets
import com.vsa.visualsemanticagent.utils.ResponseInterpreter
import com.vsa.visualsemanticagent.utils.UriImageUtils
import com.vsa.visualsemanticagent.voice.VoiceRecognitionException
import com.vsa.visualsemanticagent.voice.VoiceRecognitionManager
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private enum class RecoveryAction {
        NONE,
        REQUEST_PERMISSIONS,
        RETRY_CAPTURE,
        RETRY_VOICE,
        RETRY_IMPORT
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
    private var notificationPermissionGranted by mutableStateOf(false)
    private var lastError by mutableStateOf("")
    private var showErrorOverlay by mutableStateOf(false)
    private var currentRecoveryAction by mutableStateOf(RecoveryAction.NONE)
    private var pendingExecutableIntent by mutableStateOf<ExecutableIntent?>(null)
    private var pendingExecutionSuggestion by mutableStateOf<ExecutionSuggestion?>(null)
    private var showConfirmationCard by mutableStateOf(false)
    private var agendaItems by mutableStateOf(
        listOf(
            AgendaCardData(
                id = "demo-lecture",
                title = "人工智能前沿讲座",
                summary = "适合展示海报识别后自动生成日程建议与提醒。",
                time = "周五 19:00",
                location = "图书馆报告厅",
                status = "已确认",
                isoDateTime = "2026-05-09T19:00:00",
                sourceLabel = "示例数据",
                action = "create_event",
                reminders = defaultAgendaReminders()
            ),
            AgendaCardData(
                id = "demo-exam",
                title = "数字图像处理考试",
                summary = "适合展示教务通知转提醒卡片与时间抽取。",
                time = "5月10日 08:30",
                location = "A2-304",
                status = "已确认",
                isoDateTime = "2026-05-10T08:30:00",
                sourceLabel = "示例数据",
                action = "create_event",
                reminders = defaultAgendaReminders()
            )
        )
    )
    private var showStartupIntro by mutableStateOf(true)
    private var launchTab by mutableStateOf("home")
    private var reminderPolicyLabel by mutableStateOf("默认提前一天和提前一小时")
    private var reminderStateText by mutableStateOf("本地提醒尚未初始化")
    private var nextReminderText by mutableStateOf("暂无即将触发的提醒")
    private var scheduledReminderCount by mutableIntStateOf(0)
    private var confirmedAgendaCount by mutableIntStateOf(0)
    private var pendingAgendaCount by mutableIntStateOf(0)
    private var todayAgendaCount by mutableIntStateOf(0)
    private var exportFormats by mutableStateOf(listOf("PDF", "JPG", "PNG"))
    private var reminderLeadMinutes by mutableIntStateOf(60)
    private var reminderDayEnabled by mutableStateOf(true)
    private var reminderHourEnabled by mutableStateOf(true)
    private var blockHighRisk by mutableStateOf(true)
    private var muteLowConfidence by mutableStateOf(false)
    private var autoMapLink by mutableStateOf(true)
    private var selectedPlanMode by mutableStateOf("month")
    private var selectedPlanDate by mutableStateOf<LocalDate?>(null)
    private var calendarPreviewMonth by mutableStateOf(YearMonth.now())
    private var importedText by mutableStateOf("")
    private var importedSourceLabel by mutableStateOf("")
    private var importedImageUriText by mutableStateOf("")
    private var planExportStatus by mutableStateOf("等待导出")
    private var appInitialized = false
    private var preferencesSeeded = false

    private lateinit var intentDispatcher: IntentDispatcher
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var vlmNetworkClient: VLMNetworkClient
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var appPreferencesStore: AppPreferencesStore

    private val riskPolicyEngine = RiskPolicyEngine()
    private val presets = PromptPresets.defaults

    private var lastNoticeInput: CampusNoticeInput? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true || hasPermission(Manifest.permission.CAMERA)
        audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] == true || hasPermission(Manifest.permission.RECORD_AUDIO)
        notificationPermissionGranted = hasNotificationPermission()

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

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImageUriImported(uri, NoticeSourceType.ALBUM)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        appPreferencesStore = AppPreferencesStore(this)
        lifecycleScope.launch {
            appPreferencesStore.stateFlow.collect { stored ->
                if (!preferencesSeeded) {
                    if (stored.agendaItems.isNotEmpty()) {
                        agendaItems = stored.agendaItems
                    }
                    reminderLeadMinutes = stored.reminderLeadMinutes
                    reminderDayEnabled = stored.reminderDayEnabled
                    reminderHourEnabled = stored.reminderHourEnabled
                    blockHighRisk = stored.blockHighRisk
                    muteLowConfidence = stored.muteLowConfidence
                    autoMapLink = stored.autoMapLink
                    preferencesSeeded = true
                    if (stored.agendaItems.isEmpty()) {
                        persistAgendaItems()
                        persistAppPreferences()
                    }
                    refreshReminderState()
                }
            }
        }

        refreshPermissionState()
        launchTab = resolveLaunchTab(intent)
        showStartupIntro = !isShareIntent(intent)
        refreshReminderState()

        setContent {
            if (showStartupIntro) {
                com.vsa.visualsemanticagent.ui.LoginScreen(
                    onEnterApp = {
                        refreshReminderState()
                        initializeAppIfNeeded()
                        showStartupIntro = false
                    }
                )
            } else {
                MainScreen(
                    commandText = commandText,
                    onCommandChanged = { commandText = it },
                    onSubmitCommand = { onSubmitCommandClicked() },
                    onCapture = { onCaptureButtonClicked() },
                    onVoice = { onVoiceButtonClicked() },
                    onPickImage = { onPickImageClicked() },
                    onPasteText = { onPasteTextClicked() },
                    onPreset = { onPresetSelected(it) },
                    onConfirmExecution = { onConfirmExecutionClicked() },
                    onCancelExecution = { onCancelExecutionClicked() },
                    bindPreview = { bindPreview(it) },
                    presets = presets,
                    showLivePreview = true,
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening,
                    isCameraAvailable = cameraAvailable,
                    loadingStage = loadingStage,
                    statusText = statusText,
                    resultText = resultText,
                    importedSourceLabel = importedSourceLabel,
                    confirmationIntent = pendingExecutableIntent,
                    confirmationSuggestion = pendingExecutionSuggestion,
                    showConfirmationCard = showConfirmationCard,
                    agendaItems = agendaItems,
                    selectedPlanMode = selectedPlanMode,
                    selectedPlanDate = selectedPlanDate,
                    calendarPreviewMonth = calendarPreviewMonth,
                    reminderPolicyLabel = reminderPolicyLabel,
                    reminderStateText = reminderStateText,
                    nextReminderText = nextReminderText,
                    reminderLeadMinutes = reminderLeadMinutes,
                    reminderDayEnabled = reminderDayEnabled,
                    reminderHourEnabled = reminderHourEnabled,
                    blockHighRisk = blockHighRisk,
                    muteLowConfidence = muteLowConfidence,
                    autoMapLink = autoMapLink,
                    exportFormats = exportFormats,
                    showErrorOverlay = showErrorOverlay,
                    errorText = lastError,
                    showRetry = currentRecoveryAction != RecoveryAction.NONE,
                    retryText = getRetryButtonText(),
                    initialTab = launchTab,
                    onReminderLeadMinutesChange = { updateReminderLeadMinutes(it) },
                    onReminderDayEnabledChange = { updateReminderDayEnabled(it) },
                    onReminderHourEnabledChange = { updateReminderHourEnabled(it) },
                    onBlockHighRiskChange = { updateBlockHighRisk(it) },
                    onMuteLowConfidenceChange = { updateMuteLowConfidence(it) },
                    onAutoMapLinkChange = { updateAutoMapLink(it) },
                    onRetry = { onRetryRequested() },
                    onDismissError = { clearErrorState() },
                    onCancelLoading = {
                        isLoading = false
                        loadingStage = 0
                        statusText = "识别已取消"
                    }
                )
            }
        }

        handleIncomingShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchTab = resolveLaunchTab(intent)
        if (isShareIntent(intent)) {
            showStartupIntro = false
        }
        handleIncomingShareIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
        refreshReminderState()
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
        requestAudio: Boolean = true,
        requestImages: Boolean = false
    ) {
        refreshPermissionState()

        val permissions = buildList {
            if (requestCamera && !cameraPermissionGranted) {
                add(Manifest.permission.CAMERA)
            }
            if (requestImages) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !hasPermission(Manifest.permission.READ_MEDIA_IMAGES) -> {
                        add(Manifest.permission.READ_MEDIA_IMAGES)
                    }

                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                        !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
            if (requestAudio && !audioPermissionGranted) {
                add(Manifest.permission.RECORD_AUDIO)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isEmpty()) {
            initializeAppIfNeeded()
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
        reminderScheduler = ReminderScheduler(this)
        vlmNetworkClient = VLMNetworkClient(
            appId = BuildConfig.VLM_APP_ID,
            apiKey = BuildConfig.VLM_API_KEY,
            modelName = BuildConfig.VLM_MODEL_NAME,
            apiEndpoint = BuildConfig.VLM_API_ENDPOINT,
            ocrEndpoint = BuildConfig.VLM_OCR_ENDPOINT,
            useMockMode = false
        )
        if (audioPermissionGranted) {
            ensureVoiceRecognitionManager().initialize()
        }
        reminderScheduler.ensureNotificationChannel()
        notificationPermissionGranted = reminderScheduler.notificationsGranted()
        refreshReminderState()
        statusText = ""
        resultText = ""
        appInitialized = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
            requestPermissions(requestCamera = false, requestAudio = false)
        }
    }

    private fun bindPreview(previewView: PreviewView) {
        if (!cameraPermissionGranted) return
        CameraManager.bindCamera(this, this, previewView) { available ->
            cameraAvailable = available
            if (!available) {
                statusText = getString(R.string.camera_preview_unavailable_mock_hint)
            }
        }
    }

    private fun onPresetSelected(preset: PromptPreset) {
        commandText = preset.prompt
        statusText = "已选择通知类型：${preset.label}"
    }

    private fun onVoiceButtonClicked() {
        if (isLoading || isVoiceListening) return
        initializeAppIfNeeded()
        if (!audioPermissionGranted) {
            showError(
                message = getString(R.string.microphone_permission_missing),
                recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
            )
            requestPermissions(requestCamera = false, requestAudio = true)
            return
        }

        lifecycleScope.launch {
            try {
                isVoiceListening = true
                clearErrorState()
                statusText = getString(R.string.voice_listening)
                commandText = ensureVoiceRecognitionManager().listenOnce()
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
        lifecycleScope.launch {
            try {
                val base64Image = CameraManager.captureBase64Image()

                val noticeInput = CampusNoticeInput(
                    sourceType = NoticeSourceType.CAMERA,
                    base64Image = base64Image,
                    userInstruction = commandText.ifBlank { getString(R.string.default_command) }
                )
                processNoticeInput(noticeInput)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Camera capture flow failed")
                handleError(e, RecoveryAction.RETRY_CAPTURE)
            }
        }
    }

    private fun onPickImageClicked() {
        if (isLoading || isVoiceListening) return
        pickImageLauncher.launch("image/*")
    }

    private fun onPasteTextClicked() {
        if (isLoading || isVoiceListening) return
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        val text = clip
            ?.takeIf {
                it.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                    it.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
            }
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.trim()
            .orEmpty()

        if (text.isBlank()) {
            statusText = getString(R.string.clipboard_empty)
            return
        }

        onTextImported(text, NoticeSourceType.CLIPBOARD)
    }

    private fun onSubmitCommandClicked() {
        if (isLoading || isVoiceListening) return
        val text = commandText.trim()
        if (text.isBlank()) {
            statusText = getString(R.string.manual_text_empty)
            return
        }
        onTextImported(text, NoticeSourceType.MANUAL_TEXT)
    }

    private fun onImageUriImported(
        uri: Uri,
        sourceType: NoticeSourceType
    ) {
        initializeAppIfNeeded()
        Timber.d("Image imported: sourceType=%s uri=%s", sourceType.value, uri)
        importedText = ""
        importedImageUriText = uri.toString()
        importedSourceLabel = sourceType.label
        commandText = commandText.ifBlank { getString(R.string.default_command) }
        val noticeInput = CampusNoticeInput(
            sourceType = sourceType,
            imageUri = uri,
            userInstruction = commandText.ifBlank { getString(R.string.default_command) }
        )
        processNoticeInput(noticeInput)
    }

    private fun onTextImported(
        text: String,
        sourceType: NoticeSourceType
    ) {
        initializeAppIfNeeded()
        Timber.d(
            "Text imported: sourceType=%s textLength=%s preview=%s",
            sourceType.value,
            text.length,
            text.take(120)
        )
        importedText = text
        importedImageUriText = ""
        importedSourceLabel = sourceType.label
        commandText = commandText.ifBlank { getString(R.string.default_command) }
        val noticeInput = CampusNoticeInput(
            sourceType = sourceType,
            rawText = text,
            userInstruction = commandText.ifBlank { getString(R.string.default_command) }
        )
        processNoticeInput(noticeInput)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        Timber.d("Handle share intent: type=%s extras=%s", intent.type, intent.extras?.keySet()?.joinToString())
        val type = intent.type.orEmpty()
        when {
            type.startsWith("image/") -> {
                val uri = extractSharedImageUri(intent)
                if (uri != null) {
                    showStartupIntro = false
                    onImageUriImported(uri, NoticeSourceType.SHARE_IMAGE)
                }
            }

            type == "text/plain" || type.startsWith("text/") -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty().trim()
                if (text.isNotBlank()) {
                    showStartupIntro = false
                    onTextImported(text, NoticeSourceType.SHARE_TEXT)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun extractSharedImageUri(intent: Intent): Uri? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    private fun processNoticeInput(noticeInput: CampusNoticeInput) {
        if (isLoading || isVoiceListening) return
        initializeAppIfNeeded()
        Timber.d(
            "Process notice input: sourceType=%s hasImage=%s hasText=%s instruction=%s",
            noticeInput.sourceType.value,
            noticeInput.hasImage,
            noticeInput.hasText,
            noticeInput.userInstruction.take(120)
        )

        if (BuildConfig.VLM_API_KEY.isBlank()) {
            showError(
                message = getString(R.string.api_key_missing),
                recoveryAction = RecoveryAction.NONE
            )
            playTextToSpeech(statusText)
            return
        }

        if (noticeInput.hasImage && BuildConfig.VLM_APP_ID.isBlank()) {
            showError(
                message = getString(R.string.app_id_missing),
                recoveryAction = RecoveryAction.NONE
            )
            playTextToSpeech(statusText)
            return
        }

        lastNoticeInput = noticeInput
        isLoading = true
        loadingStage = 0
        resultText = ""
        pendingExecutableIntent = null
        pendingExecutionSuggestion = null
        showConfirmationCard = false
        clearErrorState()

        lifecycleScope.launch {
            try {
                val finalCommand = commandText.ifBlank { noticeInput.userInstruction }
                loadingStage = 1
                Timber.d("Notice processing stage=prepare command=%s", finalCommand.take(120))

                val base64Image = when {
                    !noticeInput.base64Image.isNullOrBlank() -> noticeInput.base64Image
                    noticeInput.imageUri != null -> {
                        withContext(Dispatchers.IO) {
                            UriImageUtils.uriToBase64Jpeg(this@MainActivity, noticeInput.imageUri)
                        }
                    }

                    else -> null
                }
                Timber.d(
                    "Notice processing stage=prepared sourceType=%s imageBase64Length=%s rawTextLength=%s",
                    noticeInput.sourceType.value,
                    base64Image?.length ?: 0,
                    noticeInput.rawText?.length ?: 0
                )

                val rawResponse = sendCampusNoticeToVlm(
                    noticeInput = noticeInput,
                    base64Image = base64Image,
                    userText = finalCommand
                )
                handleVlmResponse(rawResponse)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Campus notice flow failed")
                handleError(
                    throwable = e,
                    recoveryAction = if (noticeInput.sourceType == NoticeSourceType.CAMERA) {
                        RecoveryAction.RETRY_CAPTURE
                    } else {
                        RecoveryAction.RETRY_IMPORT
                    }
                )
                if (lastError.isNotBlank()) {
                    playTextToSpeech(lastError)
                }
            } finally {
                isLoading = false
                loadingStage = 0
            }
        }
    }

    private suspend fun sendCampusNoticeToVlm(
        noticeInput: CampusNoticeInput,
        base64Image: String?,
        userText: String
    ): VLMResponse {
        return vlmNetworkClient.sendCampusNoticeRequest(
            base64Image = base64Image,
            userText = userText,
            rawText = noticeInput.rawText,
            sourceType = noticeInput.sourceType.label
        )
    }

    private fun handleVlmResponse(rawResponse: VLMResponse) {
        val response = ResponseInterpreter.normalize(rawResponse)
        val executableIntent = VisualActionIntentSchema.fromResponse(response)
        Timber.d(
            "VLM parsed: action=%s confidence=%.2f title=%s time=%s location=%s",
            executableIntent.action,
            executableIntent.fusedConfidence,
            executableIntent.title,
            executableIntent.time,
            executableIntent.location
        )
        loadingStage = 2
        handleExecutionSuggestion(executableIntent)
    }

    private fun handleExecutionSuggestion(executableIntent: ExecutableIntent) {
        var suggestion = riskPolicyEngine.evaluate(executableIntent)
        if (blockHighRisk && executableIntent.riskLevel == com.vsa.visualsemanticagent.decision.IntentRiskLevel.HIGH) {
            suggestion = suggestion.copy(
                mode = ExecutionMode.BLOCKED,
                summary = "高风险动作已被织时自动拦截",
                prompt = "当前偏好已开启高风险指令自动拦截。为保证安全，这类动作不会继续执行。"
            )
        } else if (!blockHighRisk && executableIntent.riskLevel == com.vsa.visualsemanticagent.decision.IntentRiskLevel.HIGH) {
            suggestion = suggestion.copy(
                summary = "${suggestion.summary}（当前已关闭高风险动作额外拦截）",
                prompt = "你已关闭高风险动作额外拦截，但织时仍建议谨慎确认。"
            )
        }
        if (!autoMapLink && executableIntent.action == "navigate") {
            suggestion = suggestion.copy(
                mode = ExecutionMode.BLOCKED,
                summary = "已识别出地点，但地图联动当前已关闭",
                prompt = "你可以在“我的”页重新开启地点解析与地图联动，或先将地点作为普通信息保留。"
            )
        }
        if (muteLowConfidence && suggestion.mode == ExecutionMode.REQUIRE_CLARIFICATION) {
            suggestion = suggestion.copy(
                summary = "低置信度结果已被静默降级，建议你补充信息后再试。",
                prompt = "当前结果不够稳定，建议补充时间、地点或重新导入。"
            )
        }
        Timber.d(
            "Execution suggestion: action=%s mode=%s threshold=%.2f summary=%s",
            executableIntent.action,
            suggestion.mode,
            suggestion.threshold,
            suggestion.summary
        )
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
                upsertPendingAgendaItem(
                    executableIntent = executableIntent,
                    summary = suggestion.summary,
                    status = when (executableIntent.action) {
                        "create_event" -> "待确认"
                        "navigate" -> "待导航"
                        else -> "待处理"
                    }
                )
                playTextToSpeech(suggestion.prompt)
            }

            ExecutionMode.REQUIRE_CLARIFICATION -> {
                statusText = suggestion.summary
                resultText = buildResultCardText(executableIntent, suggestion.summary, suggestion)
                showConfirmationCard = false
                if (!muteLowConfidence) {
                    playTextToSpeech(suggestion.prompt)
                }
            }

            ExecutionMode.BLOCKED -> {
                statusText = suggestion.summary
                resultText = buildResultCardText(executableIntent, suggestion.summary, suggestion)
                showConfirmationCard = false
                if (!muteLowConfidence) {
                    playTextToSpeech(suggestion.prompt)
                }
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
            if (executableIntent.action == "create_event") {
                movePendingAgendaToAdded(
                    executableIntent = executableIntent,
                    summary = dispatchResult.summary
                )
            } else {
                removePendingAgendaItem(executableIntent)
            }
            refreshReminderState()
            playTextToSpeech(dispatchResult.summary)
        } catch (e: Exception) {
            Timber.e(e, "Execution failed after confirmation")
            handleError(e, RecoveryAction.RETRY_IMPORT)
        }
    }

    private fun onCancelExecutionClicked() {
        val executableIntent = pendingExecutableIntent
        val suggestion = pendingExecutionSuggestion
        showConfirmationCard = false
        statusText = "已取消执行"
        if (executableIntent != null) {
        agendaItems = agendaItems.map { item ->
            if (item.id == executableIntent.stabilityKey) {
                item.copy(status = "已取消", reminders = currentReminderSet())
            } else {
                item
            }
        }
            removePendingAgendaItem(executableIntent)
        }
        resultText = buildString {
            append("已取消执行")
            if (suggestion != null) {
                append("\n")
                append(suggestion.prompt)
            }
        }
        refreshReminderState()
    }

    private fun buildResultCardText(
        intent: ExecutableIntent,
        summary: String,
        suggestion: ExecutionSuggestion
    ): String {
        val parts = linkedSetOf<String>()
        if (importedSourceLabel.isNotBlank()) {
            parts.add("来源：$importedSourceLabel")
        }
        parts.add(summary)
        parts.add("建议动作：${intent.action}")
        parts.add("识别可信度：${"%.2f".format(intent.fusedConfidence)}")
        parts.add("执行模式：${suggestion.mode}")
        intent.title?.let { parts.add("标题：$it") }
        intent.time?.let { parts.add("时间：$it") }
        intent.location?.let { parts.add("地点：$it") }
        intent.description?.let { parts.add("备注：$it") }
        intent.answer?.let { parts.add("播报：$it") }
        if (suggestion.validation.issues.isNotEmpty()) {
            parts.add("校验问题：${suggestion.validation.issues.joinToString()}")
        }
        parts.add("建议话术：${suggestion.prompt}")
        return parts.joinToString(separator = "\n")
    }

    private fun upsertPendingAgendaItem(
        executableIntent: ExecutableIntent,
        summary: String,
        status: String
    ) {
        val item = buildAgendaCardData(
            executableIntent = executableIntent,
            summary = summary,
            status = status
        )
        agendaItems = listOf(item) + agendaItems.filterNot { it.id == item.id }
        persistAgendaItems()
    }

    private fun movePendingAgendaToAdded(
        executableIntent: ExecutableIntent,
        summary: String
    ) {
        val item = buildAgendaCardData(
            executableIntent = executableIntent,
            summary = summary,
            status = "已加入日历"
        )
        agendaItems = listOf(item) + agendaItems.filterNot { it.id == item.id }
        reminderScheduler.scheduleAgendaReminders(item)
        persistAgendaItems()
        refreshReminderState()
    }

    private fun removePendingAgendaItem(executableIntent: ExecutableIntent) {
        val currentItem = agendaItems.firstOrNull { it.id == executableIntent.stabilityKey }
        reminderScheduler.cancelAgendaReminders(
            agendaId = executableIntent.stabilityKey,
            reminders = currentItem?.reminders ?: defaultAgendaReminders()
        )
        agendaItems = agendaItems.filterNot { it.id == executableIntent.stabilityKey }
        persistAgendaItems()
        refreshReminderState()
    }

    private fun buildAgendaCardData(
        executableIntent: ExecutableIntent,
        summary: String,
        status: String
    ): AgendaCardData {
        return AgendaCardData(
            id = executableIntent.stabilityKey,
            title = executableIntent.title
                ?: commandText.takeIf { it.isNotBlank() }?.take(18)
                ?: "新的校园通知待整理",
            summary = summary,
            time = executableIntent.time ?: "等待提取时间字段",
            location = executableIntent.location ?: importedSourceLabel.ifBlank { "支持海报、截图、文本、分享导入" },
            status = status,
            isoDateTime = executableIntent.time,
            sourceLabel = importedSourceLabel,
            action = executableIntent.action,
            reminders = currentReminderSet()
        )
    }

    private fun playTextToSpeech(text: String) {
        ensureTextToSpeechManager().speak(text)
    }

    private fun ensureVoiceRecognitionManager(): VoiceRecognitionManager {
        if (!::voiceRecognitionManager.isInitialized) {
            voiceRecognitionManager = VoiceRecognitionManager(this)
        }
        return voiceRecognitionManager
    }

    private fun ensureTextToSpeechManager(): TextToSpeechManager {
        if (!::textToSpeechManager.isInitialized) {
            textToSpeechManager = TextToSpeechManager(this)
        }
        return textToSpeechManager
    }

    private fun refreshReminderState() {
        val confirmedItems = agendaItems.filter {
            it.status.contains("已加入日历") || it.status.contains("已确认")
        }
        confirmedAgendaCount = confirmedItems.size
        pendingAgendaCount = agendaItems.count { it.status.contains("待") }
        todayAgendaCount = confirmedItems.count { it.scheduleDate() == LocalDate.now() }
        scheduledReminderCount = confirmedItems.sumOf { it.reminders.size }
        reminderStateText = if (::reminderScheduler.isInitialized && reminderScheduler.notificationsGranted()) {
            "本地通知已启用，正在保护 ${confirmedItems.size} 条已确认日程"
        } else if (::reminderScheduler.isInitialized) {
            "通知权限未开启，本地提醒暂不可见"
        } else if (confirmedItems.isEmpty()) {
            "提醒引擎待激活"
        } else {
            "提醒引擎待激活，已确认日程将在初始化后挂载提醒"
        }
        nextReminderText = if (::reminderScheduler.isInitialized) {
            reminderScheduler.nextReminderSummary(confirmedItems)
        } else if (confirmedItems.isEmpty()) {
            "暂无即将触发的提醒"
        } else {
            "初始化后将生成本地提醒"
        }
        val labels = buildList {
            if (reminderDayEnabled) add("1天")
            if (reminderHourEnabled) add("${reminderLeadMinutes}分钟")
        }
        reminderPolicyLabel = "默认提前 ${labels.joinToString(" / ").ifBlank { "关闭" }}"
    }

    private fun currentReminderSet(): List<com.vsa.visualsemanticagent.plan.AgendaReminderData> {
        return buildList {
            if (reminderDayEnabled) {
                add(com.vsa.visualsemanticagent.plan.AgendaReminderData(label = "提前1天", minutesBefore = 24 * 60))
            }
            if (reminderHourEnabled) {
                val label = when (reminderLeadMinutes) {
                    15 -> "提前15分钟"
                    30 -> "提前30分钟"
                    60 -> "提前1小时"
                    else -> "提前${reminderLeadMinutes}分钟"
                }
                add(com.vsa.visualsemanticagent.plan.AgendaReminderData(label = label, minutesBefore = reminderLeadMinutes))
            }
        }
    }

    private fun persistAgendaItems() {
        if (!::appPreferencesStore.isInitialized || !preferencesSeeded) return
        lifecycleScope.launch {
            appPreferencesStore.saveAgendaItems(agendaItems)
        }
    }

    private fun persistAppPreferences() {
        if (!::appPreferencesStore.isInitialized || !preferencesSeeded) return
        lifecycleScope.launch {
            appPreferencesStore.savePreferences(
                reminderLeadMinutes = reminderLeadMinutes,
                reminderDayEnabled = reminderDayEnabled,
                reminderHourEnabled = reminderHourEnabled,
                blockHighRisk = blockHighRisk,
                muteLowConfidence = muteLowConfidence,
                autoMapLink = autoMapLink
            )
        }
    }

    private fun updateReminderLeadMinutes(minutes: Int) {
        reminderLeadMinutes = minutes
        rebuildAgendaReminderPolicies()
    }

    private fun updateReminderDayEnabled(enabled: Boolean) {
        reminderDayEnabled = enabled
        rebuildAgendaReminderPolicies()
    }

    private fun updateReminderHourEnabled(enabled: Boolean) {
        reminderHourEnabled = enabled
        rebuildAgendaReminderPolicies()
    }

    private fun updateBlockHighRisk(enabled: Boolean) {
        blockHighRisk = enabled
        persistAppPreferences()
    }

    private fun updateMuteLowConfidence(enabled: Boolean) {
        muteLowConfidence = enabled
        persistAppPreferences()
    }

    private fun updateAutoMapLink(enabled: Boolean) {
        autoMapLink = enabled
        persistAppPreferences()
    }

    private fun rebuildAgendaReminderPolicies() {
        val previousItems = agendaItems
        val reminders = currentReminderSet()
        agendaItems = agendaItems.map { item ->
            item.copy(reminders = reminders)
        }
        if (::reminderScheduler.isInitialized) {
            previousItems.forEach { item ->
                if (item.status.contains("已加入日历") || item.status.contains("已确认")) {
                    reminderScheduler.cancelAgendaReminders(item.id, item.reminders)
                }
            }
            agendaItems.forEach { item ->
                if (item.status.contains("已加入日历") || item.status.contains("已确认")) {
                    reminderScheduler.scheduleAgendaReminders(item)
                }
            }
        }
        persistAgendaItems()
        persistAppPreferences()
        refreshReminderState()
    }

    private fun onRetryRequested() {
        val recoveryAction = currentRecoveryAction
        clearErrorState()
        when (recoveryAction) {
            RecoveryAction.REQUEST_PERMISSIONS -> requestPermissions(
                requestCamera = !cameraPermissionGranted,
                requestAudio = !audioPermissionGranted,
                requestImages = !hasImageAccessPermission()
            )

            RecoveryAction.RETRY_CAPTURE -> onCaptureButtonClicked()
            RecoveryAction.RETRY_VOICE -> onVoiceButtonClicked()
            RecoveryAction.RETRY_IMPORT -> lastNoticeInput?.let(::processNoticeInput)
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
            is FileNotFoundException -> getString(R.string.image_import_failed)
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
            else -> message.ifBlank { getString(R.string.error_occurred) }
        }
    }

    private fun mapActivityNotFoundMessage(throwable: ActivityNotFoundException): String {
        val message = throwable.message.orEmpty()
        return when {
            message.contains("Calendar", ignoreCase = true) -> getString(R.string.calendar_app_missing)
            message.contains("Map", ignoreCase = true) -> getString(R.string.map_app_missing)
            else -> getString(R.string.error_occurred)
        }
    }

    private fun getRetryButtonText(): String {
        return when (currentRecoveryAction) {
            RecoveryAction.REQUEST_PERMISSIONS -> getString(R.string.retry_permission)
            RecoveryAction.RETRY_CAPTURE,
            RecoveryAction.RETRY_VOICE,
            RecoveryAction.RETRY_IMPORT -> getString(R.string.retry)

            RecoveryAction.NONE -> getString(R.string.retry)
        }
    }

    private fun refreshPermissionState() {
        cameraPermissionGranted = hasPermission(Manifest.permission.CAMERA)
        audioPermissionGranted = hasPermission(Manifest.permission.RECORD_AUDIO)
        notificationPermissionGranted = hasNotificationPermission()
    }

    private fun hasCaptureAccess(): Boolean {
        return cameraPermissionGranted
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun hasImageAccessPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun resolveLaunchTab(intent: Intent?): String {
        return when (intent?.getStringExtra("open_tab")) {
            "plan", "profile", "home" -> intent.getStringExtra("open_tab").orEmpty()
            else -> "home"
        }.ifBlank { "home" }
    }

    private fun isShareIntent(intent: Intent?): Boolean {
        return intent?.action == Intent.ACTION_SEND
    }
}

@Composable
fun MainScreen(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onSubmitCommand: () -> Unit,
    onCapture: () -> Unit,
    onVoice: () -> Unit,
    onPickImage: () -> Unit,
    onPasteText: () -> Unit,
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
    importedSourceLabel: String,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    agendaItems: List<AgendaCardData>,
    selectedPlanMode: String,
    selectedPlanDate: LocalDate?,
    calendarPreviewMonth: YearMonth,
    reminderPolicyLabel: String,
    reminderStateText: String,
    nextReminderText: String,
    reminderLeadMinutes: Int,
    reminderDayEnabled: Boolean,
    reminderHourEnabled: Boolean,
    blockHighRisk: Boolean,
    muteLowConfidence: Boolean,
    autoMapLink: Boolean,
    exportFormats: List<String>,
    showErrorOverlay: Boolean,
    errorText: String,
    showRetry: Boolean,
    retryText: String,
    initialTab: String,
    onReminderLeadMinutesChange: (Int) -> Unit,
    onReminderDayEnabledChange: (Boolean) -> Unit,
    onReminderHourEnabledChange: (Boolean) -> Unit,
    onBlockHighRiskChange: (Boolean) -> Unit,
    onMuteLowConfidenceChange: (Boolean) -> Unit,
    onAutoMapLinkChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onCancelLoading: (() -> Unit)? = null
) {
    val surfaceColor = if (showLivePreview) {
        Color.Black
    } else {
        Color(0xFFEEF4FF)
    }
    val tabs = listOf(
        MainTabItem("home",    "首页",   Icons.Rounded.Home),
        MainTabItem("plan",    "时间线", Icons.Rounded.Schedule),
        MainTabItem("profile", "我的",   Icons.Rounded.Person)
    )
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var showTimelineTransfer by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val confirmedAgendaItems = androidx.compose.runtime.remember(agendaItems) {
        agendaItems.filter { it.status.contains("已加入日历") || it.status.contains("已确认") }
    }
    val resolvedConfirmedAgendaCount = confirmedAgendaItems.size
    val resolvedPendingAgendaCount = androidx.compose.runtime.remember(agendaItems) {
        agendaItems.count { it.status.contains("待") }
    }
    val resolvedTodayAgendaCount = androidx.compose.runtime.remember(confirmedAgendaItems) {
        confirmedAgendaItems.count { it.scheduleDate() == LocalDate.now() }
    }
    val resolvedScheduledReminderCount = androidx.compose.runtime.remember(confirmedAgendaItems) {
        confirmedAgendaItems.sumOf { it.reminders.size }
    }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    LaunchedEffect(selectedTab, showLivePreview, isCameraAvailable) {
        if (selectedTab != "home" || !showLivePreview || !isCameraAvailable) {
            CameraManager.unbindPreview()
        }
    }

    val confirmWithTimelineTransfer: () -> Unit = {
        scope.launch {
            showTimelineTransfer = true
            onConfirmExecution()
            delay(420)
            selectedTab = "plan"
            delay(360)
            showTimelineTransfer = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = surfaceColor,
                bottomBar = {
                    NavigationBar(
                        containerColor = com.vsa.visualsemanticagent.ui.AppColors.SurfaceContainerLowest,
                        contentColor   = com.vsa.visualsemanticagent.ui.AppColors.Primary
                    ) {
                        tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.id,
                            onClick = { selectedTab = tab.id },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = com.vsa.visualsemanticagent.ui.AppColors.Primary,
                                selectedTextColor   = com.vsa.visualsemanticagent.ui.AppColors.Primary,
                                indicatorColor      = com.vsa.visualsemanticagent.ui.AppColors.PrimaryFixed.copy(alpha = 0.60f),
                                unselectedIconColor = com.vsa.visualsemanticagent.ui.AppColors.OnSurfaceVariant,
                                unselectedTextColor = com.vsa.visualsemanticagent.ui.AppColors.OnSurfaceVariant
                            ),
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                val pageModifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())

                Box(modifier = pageModifier) {
                    when (selectedTab) {
                        "home" -> HomeScreenModule(
                            modifier = Modifier.fillMaxSize(),
                            commandText = commandText,
                            onCommandChanged = onCommandChanged,
                            onSubmitCommandClick = onSubmitCommand,
                            onCaptureClick = onCapture,
                            onVoiceClick = onVoice,
                            onPickImageClick = onPickImage,
                            onPasteTextClick = onPasteText,
                            onPresetClick = onPreset,
                            onConfirmExecution = confirmWithTimelineTransfer,
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
                            resultText = resultText,
                            importedSourceLabel = importedSourceLabel,
                            agendaItems = agendaItems,
                            nextReminderText = nextReminderText,
                            todayAgendaCount = resolvedTodayAgendaCount,
                            onOpenTimeline = { selectedTab = "plan" },
                            onOpenProfile = { selectedTab = "profile" },
                            onOpenRecentResults = { selectedTab = "plan" }
                        )

                        "plan" -> TimelineScreenModule(
                            modifier = Modifier.fillMaxSize(),
                            confirmationIntent = confirmationIntent,
                            confirmationSuggestion = confirmationSuggestion,
                            showConfirmationCard = showConfirmationCard,
                            agendaItems = agendaItems,
                            selectedPlanMode = selectedPlanMode,
                            selectedPlanDate = selectedPlanDate,
                            calendarPreviewMonth = calendarPreviewMonth,
                            nextReminderText = nextReminderText,
                            scheduledReminderCount = resolvedScheduledReminderCount,
                            confirmedAgendaCount = resolvedConfirmedAgendaCount,
                            onConfirmExecution = confirmWithTimelineTransfer,
                            onCancelExecution = onCancelExecution,
                            onGoHome = { selectedTab = "home" }
                        )

                        else -> ProfileScreenModule(
                            modifier = Modifier.fillMaxSize(),
                            scheduledReminderCount = resolvedScheduledReminderCount,
                            confirmedAgendaCount = resolvedConfirmedAgendaCount,
                            pendingAgendaCount = resolvedPendingAgendaCount,
                            todayAgendaCount = resolvedTodayAgendaCount,
                            reminderLeadMinutes = reminderLeadMinutes,
                            reminderDayEnabled = reminderDayEnabled,
                            reminderHourEnabled = reminderHourEnabled,
                            blockHighRisk = blockHighRisk,
                            muteLowConfidence = muteLowConfidence,
                            autoMapLink = autoMapLink,
                            onReminderLeadMinutesChange = onReminderLeadMinutesChange,
                            onReminderDayEnabledChange = onReminderDayEnabledChange,
                            onReminderHourEnabledChange = onReminderHourEnabledChange,
                            onBlockHighRiskChange = onBlockHighRiskChange,
                            onMuteLowConfidenceChange = onMuteLowConfidenceChange,
                            onAutoMapLinkChange = onAutoMapLinkChange,
                            onOpenPlan = { selectedTab = "plan" },
                            onOpenReminderSettings = { selectedTab = "profile" },
                            onOpenExportRecords = { selectedTab = "plan" }
                        )
                    }
                }
            }

            LoadingOverlay(
                isVisible    = isLoading,
                currentStage = loadingStage,
                onCancel     = onCancelLoading
            )

            ErrorOverlay(
                isVisible = showErrorOverlay,
                errorMessage = errorText,
                showRetry = showRetry,
                retryText = retryText,
                onRetry = onRetry,
                onDismiss = onDismissError
            )

            TimelineTransferOverlay(
                isVisible = showTimelineTransfer
            )
        }
    }
}

private data class MainTabItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
