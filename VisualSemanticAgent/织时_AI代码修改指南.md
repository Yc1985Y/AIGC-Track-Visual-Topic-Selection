# AI 代码修改说明：将 VisualSemanticAgent 改为「织时」

> 本文档交付给编码 AI / IDE Agent 使用。请按本文档逐步修改代码。目标不是重写项目，而是在现有 Android APP 原型基础上，把泛化的「Visual-to-Tool OS」收敛成「多来源校园通知转日程」应用。

---

## 0. 最终目标

把当前项目从：

```text
拍照 / 语音 / 文字输入 → 多模态理解 → 日历 / 导航 / 短信 / 播报等多动作执行
```

收敛为：

```text
校园通知信息 → AI 提取标题、时间、地点、备注 → 生成日程确认卡片 → 用户确认后调用系统日历
```

第一步不要再只写成「拍摄通知」，而要改为「导入校园通知信息」。

原因：校园通知不只来自线下海报，还大量来自班级群、微信群、QQ群、公众号、教务系统、浏览器页面、PDF/图片通知和同学转发截图。

最终产品表达：

```text
织时：拍照、截图、分享或粘贴校园通知，自动生成可确认的日程提醒。
```

作品形态：

```text
Android APP 形态 + 内置校园视觉日程智能体内核
```

---

## 1. 修改原则

1. **不要大范围重构**：保留现有 `MainActivity`、`CameraManager`、`VLMNetworkClient`、`IntentDispatcher`、`RiskPolicyEngine`、Compose UI 主结构。
2. **新增输入通道**：在原有拍照基础上，增加相册导入、系统分享导入、文本粘贴。
3. **主动作只保留日程**：核心 action 为 `create_event`；辅助保留 `navigate`、`clarification`、`tts_feedback`。
4. **弱化短信能力**：`send_sms` 可暂时保留在历史代码中，避免改动过大，但不要出现在 UI、Prompt、Mock 数据和答辩主流程里。
5. **必须用户确认**：模型只能给出建议，任何日历或导航动作都要通过确认卡片。
6. **不读取用户聊天记录**：只处理用户主动拍摄、选择、分享或粘贴的内容。
7. **Debug Mock 必须可跑通**：无真实 API Key 时，也能演示讲座/考试/群通知转日程。

---

## 2. 当前关键代码地图

| 模块 | 文件 | 当前作用 | 修改重点 |
|---|---|---|---|
| 主流程 | `app/src/main/java/com/vsa/visualsemanticagent/MainActivity.kt` | 拍照、语音、VLM 调用、风控、确认、执行 | 增加相册、分享、粘贴输入；统一进入解析流程 |
| UI 主界面 | `app/src/main/java/com/vsa/visualsemanticagent/ui/CameraScreen.kt` | Compose 首页、场景卡、结果卡、确认卡 | 改成「导入校园通知信息」；新增导入按钮 |
| 场景预设 | `app/src/main/java/com/vsa/visualsemanticagent/utils/PromptPresets.kt` | 快捷场景入口 | 替换为讲座、考试、社团、宣讲、群通知 |
| 模型响应结构 | `app/src/main/java/com/vsa/visualsemanticagent/model/VLMModels.kt` | VLM JSON 数据类和 action 常量 | 可增加 `sourceType` 可选字段；限制可见 action |
| 模型请求 | `app/src/main/java/com/vsa/visualsemanticagent/network/VLMNetworkClient.kt` | 构造 VLM 请求和系统 Prompt | 支持图片+文本、纯文本两类请求；替换系统 Prompt |
| Mock 数据 | `app/src/main/java/com/vsa/visualsemanticagent/network/MockVLMResponseFactory.kt` | Debug 演示数据 | 替换成讲座、考试、群截图、文本通知示例 |
| 响应清洗 | `app/src/main/java/com/vsa/visualsemanticagent/utils/ResponseInterpreter.kt` | 归一化 action、时间、手机号等 | 不再推荐 `send_sms`；unknown/短信归一到 clarification 或 unknown |
| 执行意图 | `app/src/main/java/com/vsa/visualsemanticagent/decision/ExecutableIntent.kt` | 统一意图对象、确认话术 | 改为校园日程确认话术 |
| 风控策略 | `app/src/main/java/com/vsa/visualsemanticagent/decision/RiskPolicyEngine.kt` | 决定确认、追问、阻断 | 缺标题/时间/地点时追问；不自动执行 |
| 系统动作 | `app/src/main/java/com/vsa/visualsemanticagent/intent/IntentDispatcher.kt` | 日历、地图、短信 Intent | 保留日历、地图；短信不作为主链路 |
| 文案资源 | `app/src/main/res/values/strings.xml` | App 名、按钮、错误、Mock 文案 | 改成织时相关文案 |
| 清单文件 | `app/src/main/AndroidManifest.xml` | 权限、Activity、Intent Filter | 增加系统分享入口；可设置 `singleTop` |
| 单元测试 | `app/src/test/java/com/vsa/visualsemanticagent/ModuleTests.kt` | 模块测试 | 增加多来源输入和日程链路测试 |

---

## 3. 新交互流程

### 3.1 正常路径：线下海报拍照

1. 用户打开 APP。
2. 选择快捷场景「讲座入日程」。
3. 对准校园海报或公告拍照。
4. VLM 返回结构化 JSON：标题、时间、地点、备注。
5. APP 展示确认卡片。
6. 用户点击「确认添加」。
7. 调用系统日历 `Intent.ACTION_INSERT`。

### 3.2 正常路径：班级群通知截图

1. 用户在微信群/QQ群/班级群看到通知。
2. 用户截图。
3. 用户在 APP 点击「相册导入」，选择截图；或在相册/微信中「分享 → 织时」。
4. APP 将图片交给多模态模型识别。
5. 识别成功后展示日程确认卡片。

### 3.3 正常路径：纯文字通知

1. 用户复制群通知、公众号通知或教务系统文字。
2. 打开 APP，点击「粘贴文本」。
3. APP 读取剪贴板文本，或让用户粘贴到输入框。
4. `VLMNetworkClient` 走纯文本请求。
5. 生成确认卡片。

### 3.4 信息不完整路径

当缺少关键字段时，不要创建日历，返回 `clarification`。

典型情况：

- 缺时间：`我识别到了活动和地点，但没有看清开始时间。请补充完整时间。`
- 缺地点：`我识别到了活动和时间，但地点不清楚。请补充地点。`
- 多个通知：`我识别到多个可能的日程，请先选择要添加哪一个。`

本版本可以先用 `clarification` 提示用户选择，不必强行实现复杂的多候选列表。

---

## 4. 统一输入模型

新增文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/input/CampusNoticeInput.kt
```

建议内容：

```kotlin
package com.vsa.visualsemanticagent.input

import android.net.Uri

enum class NoticeSourceType(val value: String) {
    CAMERA("camera"),
    ALBUM("album"),
    SHARE_IMAGE("share_image"),
    SHARE_TEXT("share_text"),
    CLIPBOARD("clipboard")
}

data class CampusNoticeInput(
    val sourceType: NoticeSourceType,
    val rawText: String? = null,
    val imageUri: Uri? = null,
    val base64Image: String? = null,
    val userInstruction: String = "帮我把这个校园通知加入日程"
) {
    val hasImage: Boolean get() = !base64Image.isNullOrBlank() || imageUri != null
    val hasText: Boolean get() = !rawText.isNullOrBlank()
}
```

用途：

```text
不论输入来自相机、相册、分享还是剪贴板，MainActivity 都先归一化为 CampusNoticeInput，再调用统一解析函数。
```

---

## 5. AndroidManifest 修改

文件：

```text
app/src/main/AndroidManifest.xml
```

### 5.1 MainActivity 建议设置 singleTop

修改：

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:theme="@style/Theme.VisualSemanticAgent">
```

原因：从微信、QQ、相册、浏览器分享内容到 APP 时，如果 APP 已打开，优先复用当前 Activity，并在 `onNewIntent()` 中处理新内容。

### 5.2 增加分享文本入口

在 MainActivity 的 `<intent-filter>` 下新增：

```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="text/plain" />
</intent-filter>
```

### 5.3 增加分享图片入口

继续新增：

```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="image/*" />
</intent-filter>
```

可选：若后续支持多图，可增加 `ACTION_SEND_MULTIPLE`，但初版不建议做多图，避免复杂度上升。

### 5.4 查询能力

当前已有日历和地图相关 queries。短信相关 queries 可以保留，但不再作为主流程展示。

---

## 6. Uri 图片读取工具

新增文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/utils/UriImageUtils.kt
```

建议实现：

```kotlin
package com.vsa.visualsemanticagent.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object UriImageUtils {
    fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open image input stream" }
            return BitmapFactory.decodeStream(input)
                ?: throw IllegalArgumentException("Cannot decode image")
        }
    }

    fun bitmapToBase64Jpeg(bitmap: Bitmap, quality: Int = 85): String {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun uriToBase64Jpeg(context: Context, uri: Uri, quality: Int = 85): String {
        val bitmap = uriToBitmap(context, uri)
        return bitmapToBase64Jpeg(bitmap, quality)
    }
}
```

注意：

- 初版不需要做复杂图片裁剪。
- 若遇到大图 OOM，再补充压缩采样逻辑。
- 不要持久保存用户导入图片，除非用户明确需要。

---

## 7. MainActivity 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/MainActivity.kt
```

### 7.1 新增状态

在现有 state 附近增加：

```kotlin
private var importedText by mutableStateOf("")
private var importedSourceLabel by mutableStateOf("")
```

如果需要在 UI 展示已选图片来源，可增加：

```kotlin
private var importedImageUriText by mutableStateOf("")
```

### 7.2 新增相册选择 Launcher

在 `permissionLauncher` 附近新增：

```kotlin
private val pickImageLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri ->
    if (uri != null) {
        onImageUriImported(uri, "相册图片")
    }
}
```

### 7.3 处理分享 Intent

在 `onCreate()` 初始化后调用：

```kotlin
handleIncomingShareIntent(intent)
```

并新增：

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIncomingShareIntent(intent)
}
```

需要补充 imports：

```kotlin
import android.content.Intent
import android.net.Uri
```

新增函数：

```kotlin
private fun handleIncomingShareIntent(intent: Intent?) {
    if (intent?.action != Intent.ACTION_SEND) return

    val type = intent.type.orEmpty()
    when {
        type.startsWith("image/") -> {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) {
                onImageUriImported(uri, "分享图片")
            }
        }
        type == "text/plain" || type.startsWith("text/") -> {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty().trim()
            if (text.isNotBlank()) {
                onTextImported(text, "分享文本")
            }
        }
    }
}
```

### 7.4 新增导入入口函数

```kotlin
private fun onPickImageClicked() {
    if (isLoading || isVoiceListening) return
    pickImageLauncher.launch("image/*")
}

private fun onPasteTextClicked() {
    if (isLoading || isVoiceListening) return
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val text = clipboard.primaryClip
        ?.getItemAt(0)
        ?.coerceToText(this)
        ?.toString()
        ?.trim()
        .orEmpty()

    if (text.isBlank()) {
        statusText = "剪贴板中没有可识别的通知文本，请先复制群通知或活动通知。"
        return
    }
    onTextImported(text, "剪贴板文本")
}

private fun onImageUriImported(uri: Uri, sourceLabel: String) {
    initializeAppIfNeeded()
    importedSourceLabel = sourceLabel
    importedImageUriText = uri.toString()
    commandText = commandText.ifBlank { getString(R.string.default_command) }
    processImportedImage(uri, sourceLabel)
}

private fun onTextImported(text: String, sourceLabel: String) {
    initializeAppIfNeeded()
    importedText = text
    importedSourceLabel = sourceLabel
    commandText = commandText.ifBlank { getString(R.string.default_command) }
    processImportedText(text, sourceLabel)
}
```

注意：`CLIPBOARD_SERVICE` 可以使用 `Context.CLIPBOARD_SERVICE`，按当前 imports 调整。

### 7.5 抽取统一解析流程

保留原 `onCaptureButtonClicked()`，但让它最终也走统一函数。

新增：

```kotlin
private fun processImportedImage(uri: Uri, sourceLabel: String) {
    if (isLoading || isVoiceListening) return
    if (!BuildConfig.VLM_USE_MOCK && BuildConfig.VLM_API_KEY.isBlank()) {
        showError(getString(R.string.api_key_missing), RecoveryAction.NONE)
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
            loadingStage = 1
            val base64Image = if (BuildConfig.VLM_USE_MOCK) {
                ""
            } else {
                UriImageUtils.uriToBase64Jpeg(this@MainActivity, uri)
            }
            val rawResponse = vlmNetworkClient.sendCampusNoticeRequest(
                base64Image = base64Image,
                userText = finalCommand,
                rawText = null,
                sourceType = sourceLabel
            )
            handleVlmResponse(rawResponse)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Image import flow failed")
            handleError(e, RecoveryAction.RETRY_CAPTURE)
        } finally {
            isLoading = false
            loadingStage = 0
        }
    }
}

private fun processImportedText(text: String, sourceLabel: String) {
    if (isLoading || isVoiceListening) return
    if (!BuildConfig.VLM_USE_MOCK && BuildConfig.VLM_API_KEY.isBlank()) {
        showError(getString(R.string.api_key_missing), RecoveryAction.NONE)
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
            loadingStage = 1
            val rawResponse = vlmNetworkClient.sendCampusNoticeRequest(
                base64Image = null,
                userText = finalCommand,
                rawText = text,
                sourceType = sourceLabel
            )
            handleVlmResponse(rawResponse)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Text import flow failed")
            handleError(e, RecoveryAction.RETRY_CAPTURE)
        } finally {
            isLoading = false
            loadingStage = 0
        }
    }
}

private fun handleVlmResponse(rawResponse: VLMResponse) {
    val response = ResponseInterpreter.normalize(rawResponse)
    val executableIntent = VisualActionIntentSchema.fromResponse(response)
    loadingStage = 2
    handleExecutionSuggestion(executableIntent)
    scheduleDebugSnapshot("mock-result")
}
```

然后把原 `onCaptureButtonClicked()` 中得到 `base64Image` 后的部分改为：

```kotlin
val rawResponse = vlmNetworkClient.sendCampusNoticeRequest(
    base64Image = base64Image,
    userText = finalCommand,
    rawText = null,
    sourceType = "拍照图片"
)
handleVlmResponse(rawResponse)
```

---

## 8. VLMNetworkClient 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/network/VLMNetworkClient.kt
```

### 8.1 新增统一请求函数

保留现有 `sendMultimodalRequest()`，新增：

```kotlin
suspend fun sendCampusNoticeRequest(
    base64Image: String?,
    userText: String,
    rawText: String?,
    sourceType: String
): VLMResponse {
    if (useMockMode) {
        return MockVLMResponseFactory.buildResponse(userText + " " + rawText.orEmpty() + " " + sourceType)
    }
    return sendRequestWithPayload(
        buildCampusNoticePayload(
            base64Image = base64Image,
            userText = userText,
            rawText = rawText,
            sourceType = sourceType
        )
    )
}
```

如果当前代码没有 `sendRequestWithPayload()`，请把 `sendMultimodalRequest()` 内部“执行 OkHttp 请求 + 重试 + parseOpenAIResponse”的部分抽为私有函数，避免复制网络请求逻辑。

### 8.2 支持纯文本，不要在没有图片时塞空 image_url

新增 payload 构造函数：

```kotlin
private fun buildCampusNoticePayload(
    base64Image: String?,
    userText: String,
    rawText: String?,
    sourceType: String
): RequestBody {
    val userContent = mutableListOf<Map<String, Any>>(
        mapOf(
            "type" to "text",
            "text" to buildUserPrompt(userText, rawText, sourceType)
        )
    )

    if (!base64Image.isNullOrBlank()) {
        userContent.add(
            mapOf(
                "type" to "image_url",
                "image_url" to mapOf(
                    "url" to "data:image/jpeg;base64,$base64Image"
                )
            )
        )
    }

    return gson.toJson(
        mutableMapOf<String, Any>(
            "model" to modelName,
            "temperature" to 0.2,
            "stream" to false,
            "max_tokens" to 2048,
            "reasoning_effort" to "minimal",
            "messages" to listOf(
                mapOf("role" to "system", "content" to buildSystemPrompt()),
                mapOf("role" to "user", "content" to userContent)
            )
        ).apply {
            when {
                modelName.contains("qwen", ignoreCase = true) -> put("enable_thinking", false)
                modelName.contains("deepseek", ignoreCase = true) ||
                    modelName.contains("doubao", ignoreCase = true) ||
                    modelName.contains("seed", ignoreCase = true) -> put("thinking", mapOf("type" to "disabled"))
            }
        }
    ).toRequestBody("application/json".toMediaType())
}

private fun buildUserPrompt(userText: String, rawText: String?, sourceType: String): String {
    return buildString {
        appendLine("用户指令：$userText")
        appendLine("输入来源：$sourceType")
        if (!rawText.isNullOrBlank()) {
            appendLine("用户导入的通知原文：")
            appendLine(rawText)
        }
    }
}
```

### 8.3 替换系统 Prompt

替换 `buildSystemPrompt()`：

```kotlin
private fun buildSystemPrompt(): String {
    return """
You are the structured decision engine for a campus schedule assistant on a vivo Android phone.
Your job is to read campus posters, screenshots, group notices, official account posts, exam arrangements, meeting notices, club events, and career talk notices, then convert them into safe, confirmable calendar actions.

Return strict JSON only. Do not output markdown, explanation, comments, or code fences.

Allowed actions:
- create_event: use only when title, time, and location are sufficiently clear.
- navigate: use only when the user clearly wants to go to a campus location.
- clarification: use when important fields are missing, multiple notices are mixed together, or the result is uncertain.
- tts_feedback: use only for short explanatory feedback.
- unknown: use when the input is not a campus notice or cannot be understood.

Do not use send_sms. Do not draft messages. Do not claim that you have inserted the calendar event. The app will ask the user to confirm before executing.

Required schema:
{
  "action": "create_event|navigate|tts_feedback|clarification|unknown",
  "confidence": 0.0,
  "payload": {
    "title": "",
    "time": "",
    "location": "",
    "phone_number": "",
    "description": "",
    "answer": ""
  },
  "fallback_query": "",
  "target_found": true
}

Rules:
1. Always provide confidence between 0.0 and 1.0.
2. For create_event, title, time, and location are required. If any is missing or uncertain, use clarification.
3. time should be ISO-like when possible, for example 2026-05-20T14:30:00.
4. If the notice uses relative time such as "本周五" or "明天下午", preserve the phrase in description if the exact date cannot be inferred.
5. Never invent absent details.
6. If a screenshot contains multiple notices, use clarification and ask the user to select one.
7. Output JSON only.
    """.trimIndent()
}
```

---

## 9. PromptPresets 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/utils/PromptPresets.kt
```

将 `PromptPresets.defaults` 替换为：

```kotlin
object PromptPresets {
    val defaults = listOf(
        PromptPreset(
            id = "lecture",
            label = "讲座入日程",
            prompt = "请识别校园讲座或学术海报中的活动标题、日期、开始时间、地点和备注。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "exam",
            label = "考试安排",
            prompt = "请识别考试通知中的考试名称、考试日期、开始时间、考场地点和注意事项。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "activity",
            label = "社团活动",
            prompt = "请识别校园活动通知中的活动名称、时间、地点、报名或参与要求。如果适合提醒我参加，请返回 create_event。"
        ),
        PromptPreset(
            id = "career",
            label = "宣讲会提醒",
            prompt = "请识别就业宣讲会或招聘海报中的企业名称、宣讲时间、地点和备注。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "group_notice",
            label = "群通知转日程",
            prompt = "请从班级群、社团群或实验室群通知中提取日程信息，包括事项、时间、地点和备注。如果截图中有多个通知，请返回 clarification。"
        )
    )
}
```

---

## 10. CameraScreen UI 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/ui/CameraScreen.kt
```

### 10.1 增加回调参数

在 `CameraPreviewScreen` 参数中增加：

```kotlin
onPickImageClick: () -> Unit,
onPasteTextClick: () -> Unit,
importedSourceLabel: String? = null,
```

在 `MainScreen()` 调用处同步透传。

### 10.2 首页文案改造

把泛化文案改为：

```text
织时
拍照、截图、分享或粘贴校园通知，自动生成可确认的日程提醒。
```

### 10.3 「拍摄通知」改成「导入校园通知信息」

在 `CommandConsoleCard` 或相邻位置增加三个入口：

```text
拍照识别
相册导入
粘贴文本
```

按钮对应：

- `拍照识别` → 原 `onCaptureClick`
- `相册导入` → 新 `onPickImageClick`
- `粘贴文本` → 新 `onPasteTextClick`

如果 UI 空间不足，保留主按钮「拍照识别」，把「相册导入」「粘贴文本」做成小按钮即可。

### 10.4 结果卡显示来源

如果有 `importedSourceLabel`，在结果卡或状态栏显示：

```text
来源：相册图片 / 分享图片 / 分享文本 / 剪贴板文本 / 拍照图片
```

---

## 11. strings.xml 修改

文件：

```text
app/src/main/res/values/strings.xml
```

建议替换或新增：

```xml
<string name="app_name">织时</string>
<string name="camera_capture">拍照识别</string>
<string name="album_import">相册导入</string>
<string name="paste_notice_text">粘贴文本</string>
<string name="command_hint">输入或语音说出指令，例如：帮我把这个讲座加入日程</string>
<string name="default_command">请从这条校园通知中提取标题、时间、地点和备注，并生成日程建议。</string>
<string name="camera_preview_unavailable_mock_hint">当前模拟器没有可用相机，仍可通过 Mock 模式验证校园通知转日程主链路。</string>
<string name="mock_mode_showcase_title">织时已就绪</string>
<string name="mock_mode_showcase_subtitle">支持拍照、截图、分享或粘贴校园通知，生成可确认的日程提醒。</string>
<string name="mock_mode_highlight_actions_summary">重点验证讲座、考试、群通知转日程，以及日历确认和地图导航链路。</string>
```

删除或弱化这些文案中的「短信」表达：

```text
preset_find
phone_number_missing
sms_content_missing
sms_app_missing
mock_mode_highlight_actions_summary 中的“短信”
```

可以暂时不删除资源，但不要在 UI 中引用。

---

## 12. MockVLMResponseFactory 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/network/MockVLMResponseFactory.kt
```

将 Mock 样例替换为校园场景。

建议逻辑：

```kotlin
fun buildResponse(prompt: String): VLMResponse {
    val p = prompt.lowercase()
    return when {
        p.contains("考试") || p.contains("exam") -> examResponse()
        p.contains("宣讲") || p.contains("招聘") || p.contains("career") -> careerTalkResponse()
        p.contains("群") || p.contains("通知") || p.contains("截图") -> groupNoticeResponse()
        p.contains("地点") || p.contains("导航") -> navigationResponse()
        else -> lectureResponse()
    }
}
```

新增示例：

```kotlin
private fun lectureResponse() = VLMResponse(
    action = ModelConstants.ACTION_CREATE_EVENT,
    confidence = 0.91,
    payload = VLMPayload(
        title = "人工智能前沿讲座",
        time = "2026-05-12T19:00:00",
        location = "图书馆报告厅",
        description = "来源：校园讲座海报；主办方：计算机学院；建议提前 30 分钟提醒。"
    ),
    fallbackQuery = "",
    targetFound = true
)

private fun examResponse() = VLMResponse(
    action = ModelConstants.ACTION_CREATE_EVENT,
    confidence = 0.88,
    payload = VLMPayload(
        title = "数据结构期末考试",
        time = "2026-06-18T09:00:00",
        location = "A 教 203",
        description = "来源：考试安排通知；请携带学生证。"
    ),
    fallbackQuery = "",
    targetFound = true
)

private fun groupNoticeResponse() = VLMResponse(
    action = ModelConstants.ACTION_CREATE_EVENT,
    confidence = 0.86,
    payload = VLMPayload(
        title = "班级主题班会",
        time = "2026-05-15T15:00:00",
        location = "教学楼 B302",
        description = "来源：班级群通知截图；请全体同学准时参加。"
    ),
    fallbackQuery = "",
    targetFound = true
)
```

如果要演示信息缺失：

```kotlin
private fun missingTimeResponse() = VLMResponse(
    action = ModelConstants.ACTION_CLARIFICATION,
    confidence = 0.54,
    payload = VLMPayload(
        title = "就业宣讲会",
        location = "大学生活动中心",
        description = "海报中开始时间不清楚。",
        answer = "我识别到了宣讲会和地点，但开始时间不够清楚。"
    ),
    fallbackQuery = "请补充这个活动的开始时间。",
    targetFound = true
)
```

---

## 13. ResponseInterpreter 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/utils/ResponseInterpreter.kt
```

目标：不再把 `send_sms` 作为推荐能力。

如果模型返回 `send_sms`，建议改为 `unknown` 或 `clarification`：

```kotlin
private fun normalizeAction(action: String?): String {
    return when (action?.trim()) {
        ModelConstants.ACTION_CREATE_EVENT -> ModelConstants.ACTION_CREATE_EVENT
        ModelConstants.ACTION_NAVIGATE -> ModelConstants.ACTION_NAVIGATE
        ModelConstants.ACTION_TTS_FEEDBACK -> ModelConstants.ACTION_TTS_FEEDBACK
        ModelConstants.ACTION_CLARIFICATION -> ModelConstants.ACTION_CLARIFICATION
        ModelConstants.ACTION_SEND_SMS -> ModelConstants.ACTION_UNKNOWN
        else -> ModelConstants.ACTION_UNKNOWN
    }
}
```

如果当前结构不是这个函数名，请按相同原则调整。

结果话术改成：

```text
create_event → 已提取校园日程信息
navigate → 已识别校园地点
clarification → 信息还不完整，需要补充
unknown → 未识别到可创建日程的校园通知
```

---

## 14. ExecutableIntent / RiskPolicyEngine 修改

### 14.1 ExecutableIntent.kt

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/decision/ExecutableIntent.kt
```

将 `buildConfirmationPrompt()` 中 create_event 文案改为：

```kotlin
ModelConstants.ACTION_CREATE_EVENT -> {
    val eventTitle = title ?: "新的校园日程"
    val eventTime = time ?: "未确认时间"
    val eventLocation = location ?: "未确认地点"
    "识别到校园日程：$eventTitle，时间 $eventTime，地点 $eventLocation。是否添加到日历？"
}
```

将 `buildSummary()` 中 create_event 文案改为：

```kotlin
ModelConstants.ACTION_CREATE_EVENT -> "建议添加校园日程，可信度 ${confidencePercent}%"
```

可以保留 SMS 分支，但不会被 UI 主流程触发。

### 14.2 RiskPolicyEngine.kt / ActionValidator.kt

目标规则：

```text
create_event 必须具备 title + time + location。
缺任一字段：REQUIRE_CLARIFICATION。
confidence 较低：REQUIRE_CLARIFICATION。
日历和导航：REQUIRE_CONFIRMATION。
TTS：可以 DIRECT_TTS。
unknown/send_sms：BLOCKED 或 REQUIRE_CLARIFICATION。
```

追问话术：

```text
缺时间：我识别到了活动，但没有看清开始时间，请补充完整时间。
缺地点：我识别到了活动时间，但地点不清楚，请补充地点。
缺标题：我还不能确定这是哪个活动，请换个角度重拍或补充活动名称。
```

---

## 15. IntentDispatcher 修改

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/intent/IntentDispatcher.kt
```

保留：

```text
create_event → CalendarContract / Intent.ACTION_INSERT
navigate → Intent.ACTION_VIEW / geo or google.navigation URI
tts_feedback → TTS
```

短信函数可以保留，但不要在主流程中展示。若想更彻底，可以在 `dispatchIntent()` 遇到 `send_sms` 时返回 blocked 结果，而不是打开短信。

---

## 16. README 和项目文档修改

至少更新这些文件中的定位和功能列表：

```text
README.md
VisualSemanticAgent/README.md
VisualSemanticAgent/PROJECT_SUMMARY.md
VisualSemanticAgent/ARCHITECTURE.md
```

替换关键词：

```text
Visual-to-Tool OS → 织时
视觉语义执行代理 → 校园视觉日程智能体
海报入日历 → 校园通知转日程
短信草稿 → 未来扩展 / 暂不主打
```

README 建议一句话：

```text
织时是一款面向大学生的 Android APP，支持通过拍照、相册、系统分享和文本粘贴导入校园通知，利用多模态大模型提取标题、时间、地点和备注，并在用户确认后调用系统日历生成提醒。
```

---

## 17. 测试清单

### 17.1 Debug Mock 测试

- [ ] 启动 APP 显示「织时」。
- [ ] 选择「讲座入日程」后点击拍照识别，返回讲座日程确认卡。
- [ ] 选择「考试安排」后点击拍照识别，返回考试日程确认卡。
- [ ] 选择「群通知转日程」后点击拍照识别，返回班会/通知日程确认卡。
- [ ] 确认后能拉起系统日历。
- [ ] 缺时间 Mock 能返回追问，不拉起日历。
- [ ] UI 中不再出现短信草稿作为主入口。

### 17.2 相册导入测试

- [ ] 点击「相册导入」能打开系统图片选择器。
- [ ] 选择图片后进入解析流程。
- [ ] Mock 模式下即使不真实读图，也能走通主流程。
- [ ] Release 模式下能把 URI 图片转成 base64 并请求模型。

### 17.3 系统分享测试

- [ ] 从相册分享图片到 APP，能进入解析流程。
- [ ] 从浏览器/备忘录分享纯文本到 APP，能进入解析流程。
- [ ] APP 已打开时再次分享，`onNewIntent()` 能处理新内容。

### 17.4 文本粘贴测试

- [ ] 剪贴板为空时提示用户先复制通知文本。
- [ ] 剪贴板有群通知文本时能解析为日程。
- [ ] 纯文本请求不会发送空 `image_url`。

### 17.5 安全测试

- [ ] 模型返回缺时间时不创建日程。
- [ ] 模型返回 `send_sms` 时不打开短信主流程。
- [ ] 模型返回非 JSON 时仍能被清洗或提示解析失败。
- [ ] 所有日历和导航执行都需要用户确认。

---

## 18. 交付验收标准

完成后，项目应满足：

```text
1. 产品名称和主文案已改为「织时」。
2. 首页支持至少三种导入方式：拍照识别、相册导入、粘贴文本。
3. AndroidManifest 支持从外部应用分享 text/plain 和 image/* 到本应用。
4. VLM 请求支持图片+文本和纯文本两种模式。
5. Prompt 只围绕校园通知转日程，不再要求短信草稿。
6. Mock 模式有讲座、考试、群通知三类稳定样例。
7. create_event 缺标题/时间/地点时不会执行，而是追问。
8. 点击确认能拉起系统日历。
9. README / 项目文档已同步新定位。
10. 代码中没有提交真实 API Key。
```

---

## 19. 推荐修改顺序

按以下顺序做，风险最低：

1. 修改 `strings.xml`、`PromptPresets.kt`、`MockVLMResponseFactory.kt`，先完成场景换皮和 Mock 演示。
2. 修改 `VLMNetworkClient.kt` 的系统 Prompt，限制 action 范围。
3. 修改 `ExecutableIntent.kt`、`RiskPolicyEngine.kt`、`ResponseInterpreter.kt`，保证日程确认与追问逻辑。
4. 新增 `CampusNoticeInput.kt`、`UriImageUtils.kt`。
5. 修改 `AndroidManifest.xml`，增加分享入口。
6. 修改 `MainActivity.kt`，接入相册、分享、粘贴三类新输入。
7. 修改 `CameraScreen.kt`，新增导入按钮和来源显示。
8. 更新 README 与项目说明文档。
9. 跑单元测试和手动 Demo 测试。

---

## 20. 不要做的事

- 不要接入微信/QQ聊天记录读取。
- 不要做后台通知监听。
- 不要在用户未确认时写入日历。
- 不要把短信草稿作为主功能继续展示。
- 不要为了多候选通知重构整个 schema；初版用 `clarification` 兜底即可。
- 不要硬编码真实 API Key。
- 不要把项目改成纯插件或快应用；当前主交付仍是 Android APP。
