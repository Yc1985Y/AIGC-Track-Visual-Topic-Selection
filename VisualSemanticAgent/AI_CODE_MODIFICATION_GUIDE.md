# AI 代码修改说明：将 VisualSemanticAgent 收敛为「织时」

> 本文档交付给编码 AI / IDE Agent 使用。请按本文档进行代码修改，不要大范围重构。目标不是重写项目，而是在现有 Android APP 原型基础上，把泛化的「Visual-to-Tool OS」收敛成更适合 vivo 应用赛道答辩和 Demo 的「织时」校园时间线智能体。

---

## 1. 最终目标

把当前项目从：

> 拍照 / 语音 / 文字输入 → 多模态理解 → 日历 / 导航 / 短信 / 播报等多动作执行

收敛为：

> 校园通知图片 → 识别活动标题、时间、地点、备注 → 生成日程确认卡片 → 用户确认后调用系统日历；如有地点，可辅助跳转地图导航。

最终产品表达：

> **织时：将校园信息碎片整合为专属时间线的智能助手。**

作品形态：

> **Android APP 形态 + 内置视觉语义智能体内核。**

---

## 2. 修改原则

1. **保留现有架构**：不要重写 MainActivity、CameraManager、IntentDispatcher、Compose UI 主结构。
2. **缩小功能边界**：主打 `create_event`，辅助保留 `navigate`、`clarification`、`tts_feedback`。
3. **弱化或隐藏短信能力**：不要再在 UI、Prompt、Mock 示例中出现「短信草稿」作为核心能力。
4. **所有系统执行必须用户确认**：日历和导航都要走确认卡片，不允许模型直接执行。
5. **优先保证 Demo 稳定**：Debug Mock 模式要能完整跑通校园日程场景。
6. **不要提交真实 API Key**：真实模型 Key 只能通过本地配置或 BuildConfig 注入，不能写死在仓库中。

---

## 3. 当前关键代码地图

| 模块 | 文件 | 当前作用 | 修改重点 |
|---|---|---|---|
| 主流程 | `app/src/main/java/com/vsa/visualsemanticagent/MainActivity.kt` | 拍照、语音、VLM 调用、风控、确认、执行 | 基本保留，只调整默认文案和结果话术 |
| UI 主界面 | `app/src/main/java/com/vsa/visualsemanticagent/ui/CameraScreen.kt` | Compose 首页、场景卡、结果卡、确认卡 | 改成校园通知 / 日程助手表达 |
| 场景预设 | `app/src/main/java/com/vsa/visualsemanticagent/utils/PromptPresets.kt` | 快捷场景入口 | 替换为讲座、考试、社团、宣讲 |
| 模型响应结构 | `app/src/main/java/com/vsa/visualsemanticagent/model/VLMModels.kt` | VLM JSON 数据类和 action 常量 | 可保留字段，重点限制可见 action |
| 模型请求 | `app/src/main/java/com/vsa/visualsemanticagent/network/VLMNetworkClient.kt` | 构造 VLM 请求和系统 Prompt | 改成校园日程专用 Prompt |
| Mock 数据 | `app/src/main/java/com/vsa/visualsemanticagent/network/MockVLMResponseFactory.kt` | Debug 演示数据 | 替换成校园讲座 / 考试 / 活动示例 |
| 响应清洗 | `app/src/main/java/com/vsa/visualsemanticagent/utils/ResponseInterpreter.kt` | 归一化 action、时间、手机号等 | 不再把 `send_sms` 作为推荐能力 |
| 执行意图 | `app/src/main/java/com/vsa/visualsemanticagent/decision/ExecutableIntent.kt` | 统一意图对象、置信度、确认话术 | 改为校园日程确认话术 |
| 风控策略 | `app/src/main/java/com/vsa/visualsemanticagent/decision/RiskPolicyEngine.kt` | 决定是否确认、追问、执行 | 追问话术改成时间 / 地点 / 标题 |
| 系统动作 | `app/src/main/java/com/vsa/visualsemanticagent/intent/IntentDispatcher.kt` | 日历、地图、短信 Intent | 保留日历、地图；短信不作为主链路 |
| 文案资源 | `app/src/main/res/values/strings.xml` | App 名、错误、加载、Mock 文案 | 改成织时 |
| 单元测试 | `app/src/test/java/com/vsa/visualsemanticagent/ModuleTests.kt` | 模块测试 | 增加校园日程主链路测试 |

---

## 4. 目标交互流程

### 4.1 正常路径

1. 用户打开 APP。
2. 选择快捷场景，例如「讲座入日程」。
3. 对准校园海报 / 通知拍照，或在 Mock 模式直接点击「拍照执行」。
4. VLM 返回结构化 JSON：标题、时间、地点、备注。
5. APP 生成确认卡片：
   - 标题
   - 时间
   - 地点
   - 备注
   - 置信度
   - 「确认添加」按钮
6. 用户确认后调用系统日历 `Intent.ACTION_INSERT`。
7. 地点明确时，后续可通过 `navigate` 场景打开地图。

### 4.2 信息不完整路径

当标题、时间、地点中有关键字段缺失时，不要创建日历，而是返回 `clarification`：

- 缺时间：提示用户补充完整时间。
- 缺地点：提示用户靠近海报或口述地点。
- 缺标题：提示用户换角度重拍。

### 4.3 不再主打的路径

以下功能不要作为当前版本核心入口展示：

- 短信草稿
- 复杂面板指导
- 通用视觉问答
- 全局 OS Agent
- 寻物

这些最多可以在 README 或答辩材料中写成「未来扩展」。

---

## 5. 模型输出契约

### 5.1 允许的 action

当前版本对外只允许以下动作：

```text
create_event | navigate | clarification | tts_feedback | unknown
```

`send_sms` 可以暂时保留在历史代码中，避免改动过大，但不要再出现在 Prompt、Mock、UI 入口和答辩主流程里。

### 5.2 推荐 JSON Schema

模型必须只输出严格 JSON，不允许输出 Markdown 或解释文字。

```json
{
  "action": "create_event",
  "confidence": 0.88,
  "payload": {
    "title": "人工智能前沿讲座",
    "time": "2026-05-20T14:30:00",
    "location": "图书馆报告厅",
    "description": "主办方：计算机学院；建议提前十分钟到场。",
    "answer": ""
  },
  "fallback_query": "",
  "target_found": true
}
```

缺时间示例：

```json
{
  "action": "clarification",
  "confidence": 0.56,
  "payload": {
    "title": "就业宣讲会",
    "time": "",
    "location": "大学生活动中心",
    "description": "海报中时间区域不清晰。",
    "answer": "我识别到了宣讲会和地点，但开始时间不够清楚。"
  },
  "fallback_query": "我没有看清开始时间，请补充完整时间。",
  "target_found": true
}
```

### 5.3 字段规则

- `title`：活动名称、考试名称、会议名称，不能虚构。
- `time`：尽量输出 ISO-like 格式，例如 `2026-05-20T14:30:00`。
- `location`：教室、报告厅、楼栋、校区地点。
- `description`：主办方、报名要求、注意事项等补充信息。
- `confidence`：0.0 到 1.0。
- 任何关键字段不确定时，返回 `clarification`。

---

## 6. 逐文件修改说明

### 6.1 修改 `PromptPresets.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/utils/PromptPresets.kt
```

将 `PromptPresets.defaults` 替换为校园日程场景：

```kotlin
object PromptPresets {
    val defaults = listOf(
        PromptPreset(
            id = "lecture",
            label = "讲座入日程",
            prompt = "请识别这张校园讲座或学术海报中的活动标题、日期、开始时间、地点和备注。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "exam",
            label = "考试安排",
            prompt = "请识别图片中的考试名称、考试日期、开始时间、考场地点和注意事项。如果信息完整，请返回 create_event。"
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
        )
    )
}
```

注意：这里不再放「短信草稿」「长文本播报」作为核心场景入口。

---

### 6.2 修改 `VLMNetworkClient.kt` 的系统 Prompt

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/network/VLMNetworkClient.kt
```

替换 `buildSystemPrompt()` 的返回文本。新的 Prompt 要专注校园通知转日程：

```kotlin
private fun buildSystemPrompt(): String {
    return """
You are the structured decision engine for a campus visual schedule assistant on a vivo phone.
Your job is to read campus posters, notices, exam arrangements, meeting notices, club events, and career talk posters from the image, then convert them into a safe, confirmable mobile action.

Return strict JSON only. Do not output markdown, explanation, comments, or code fences.

Allowed actions:
- create_event: use when title, time, and location are sufficiently clear.
- navigate: use only when the user explicitly asks to go to a recognized campus location.
- clarification: use when any critical field is missing or uncertain.
- tts_feedback: use for low-risk summary or explanation only.
- unknown: use when the image is not a campus notice or no useful action can be inferred.

Required schema:
{
  "action": "create_event|navigate|clarification|tts_feedback|unknown",
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
2. For create_event, title, time, and location are critical fields.
3. time should be ISO-like whenever possible, for example 2026-05-20T14:30:00.
4. Never invent absent date, time, location, speaker, organizer, or registration details.
5. If time is relative, such as 本周五晚 7 点, infer only when the date context is clear; otherwise use clarification.
6. If the image contains multiple events, choose the most prominent one and summarize the rest in description.
7. navigate requires a concrete location and explicit user intent to navigate.
8. Do not choose send_sms in this version.
9. For uncertain OCR or unclear poster text, use clarification and explain what is missing in fallback_query.
10. Output JSON only.
    """.trimIndent()
}
```

不要改网络请求协议本身，继续使用现有 `chat/completions` 兼容格式即可。

---

### 6.3 修改 `MockVLMResponseFactory.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/network/MockVLMResponseFactory.kt
```

目标：Debug 模式下所有示例都围绕校园日程。建议把 `buildResponse()` 中的分支改成以下逻辑。

```kotlin
fun buildResponse(userText: String): VLMResponse {
    val normalized = userText.trim().lowercase(Locale.ROOT)

    return when {
        containsAny(
            normalized,
            "exam",
            "考试",
            "考场",
            "期末",
            "安排"
        ) -> VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            confidence = 0.91,
            payload = VLMPayload(
                title = "数据结构期末考试",
                time = "2026-06-18T09:00:00",
                location = "教学楼 B301",
                description = "请携带学生证和黑色签字笔，建议提前二十分钟到达考场。"
            ),
            fallbackQuery = "我识别到了考试安排，如果时间或考场不对，请直接补充。",
            targetFound = true
        )

        containsAny(
            normalized,
            "career",
            "宣讲",
            "招聘",
            "就业",
            "企业"
        ) -> VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            confidence = 0.9,
            payload = VLMPayload(
                title = "vivo 校园招聘宣讲会",
                time = "2026-05-25T19:00:00",
                location = "大学生活动中心报告厅",
                description = "建议提前报名并携带简历。"
            ),
            fallbackQuery = "我识别到了宣讲会信息，如果需要也可以继续让我导航到会场。",
            targetFound = true
        )

        containsAny(
            normalized,
            "navigate",
            "导航",
            "怎么去",
            "去会场",
            "地点"
        ) -> VLMResponse(
            action = ModelConstants.ACTION_NAVIGATE,
            confidence = 0.86,
            payload = VLMPayload(
                location = "图书馆报告厅",
                description = "从当前位置前往图书馆报告厅。"
            ),
            fallbackQuery = "我识别到了会场地点，请确认是否开始导航。",
            targetFound = true
        )

        containsAny(
            normalized,
            "clarify",
            "不确定",
            "模糊",
            "看不清"
        ) -> VLMResponse(
            action = ModelConstants.ACTION_CLARIFICATION,
            confidence = 0.48,
            payload = VLMPayload(
                title = "校园活动通知",
                location = "大学生活动中心",
                answer = "我识别到了活动和地点，但开始时间不够清楚。"
            ),
            fallbackQuery = "请把手机再靠近时间区域，或者直接告诉我活动开始时间。",
            targetFound = true
        )

        containsAny(
            normalized,
            "lecture",
            "讲座",
            "活动",
            "海报",
            "通知",
            "提醒",
            "日程",
            "日历"
        ) -> VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            confidence = 0.92,
            payload = VLMPayload(
                title = "人工智能前沿讲座",
                time = "2026-05-20T14:30:00",
                location = "图书馆报告厅",
                description = "主办方：计算机学院。建议提前十分钟到场，并携带校园卡签到。"
            ),
            fallbackQuery = "我识别到了讲座日程，如果有误可以手动修改。",
            targetFound = true
        )

        else -> VLMResponse(
            action = ModelConstants.ACTION_TTS_FEEDBACK,
            confidence = 0.78,
            payload = VLMPayload(
                answer = "我可以帮你识别校园海报、考试安排、社团活动和宣讲会，并生成日程提醒。",
                description = "请对准通知中的时间和地点，或选择上方快捷场景。"
            ),
            fallbackQuery = "你可以说：帮我把这个讲座加入日程。",
            targetFound = true
        )
    }
}
```

删除或注释原来的短信 Mock 分支，避免 Demo 时误触发短信能力。

---

### 6.4 修改 `ResponseInterpreter.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/utils/ResponseInterpreter.kt
```

目标：不再将 `send_sms` 当作当前版本支持能力。

把 `supportedActions` 改为：

```kotlin
private val supportedActions = setOf(
    ModelConstants.ACTION_CREATE_EVENT,
    ModelConstants.ACTION_NAVIGATE,
    ModelConstants.ACTION_TTS_FEEDBACK,
    ModelConstants.ACTION_CLARIFICATION,
    ModelConstants.ACTION_UNKNOWN
)
```

在 `buildStatusMessage()` 中删除或弱化 `ACTION_SEND_SMS` 分支。推荐处理方式：

```kotlin
ModelConstants.ACTION_SEND_SMS -> "当前版本聚焦校园日程提醒，暂不执行短信草稿。"
```

不要删除手机号归一化工具函数，保留兼容历史响应即可。

---

### 6.5 修改 `ExecutableIntent.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/decision/ExecutableIntent.kt
```

#### 6.5.1 修改场景常量

将：

```kotlin
const val SCENE_VISUAL_TO_TOOL_OS = "visual_to_tool_os"
```

改为：

```kotlin
const val SCENE_CAMPUS_SCHEDULE_AGENT = "campus_schedule_agent"
```

并在 `fromResponse()` 中使用新常量：

```kotlin
scene = SCENE_CAMPUS_SCHEDULE_AGENT
```

#### 6.5.2 修改确认话术

在 `buildConfirmationPrompt()` 中将 `create_event` 分支改为：

```kotlin
ModelConstants.ACTION_CREATE_EVENT -> {
    val eventTitle = title ?: "新的校园日程"
    val eventTime = time ?: "未确认时间"
    val eventLocation = location ?: "未确认地点"
    "识别到校园日程：$eventTitle，时间 $eventTime，地点 $eventLocation。是否添加到系统日历？"
}
```

将 `navigate` 分支改为：

```kotlin
ModelConstants.ACTION_NAVIGATE -> {
    val target = location ?: "校园地点"
    "识别到校园地点：$target。是否打开地图导航？"
}
```

#### 6.5.3 修改摘要话术

在 `buildSummary()` 中改成更贴近校园场景：

```kotlin
ModelConstants.ACTION_CREATE_EVENT -> "建议添加校园日程，可信度 ${confidencePercent}%"
ModelConstants.ACTION_NAVIGATE -> "建议打开校园地点导航，可信度 ${confidencePercent}%"
ModelConstants.ACTION_TTS_FEEDBACK -> "建议语音说明识别结果，可信度 ${confidencePercent}%"
ModelConstants.ACTION_CLARIFICATION -> "通知信息不完整，建议补充确认"
else -> "当前画面暂不适合生成日程"
```

`ACTION_SEND_SMS` 可以保留原逻辑，也可以改成非主流程文案，但不要在 UI 中触发它。

---

### 6.6 修改 `RiskPolicyEngine.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/decision/RiskPolicyEngine.kt
```

目标：校园日程必须校验标题、时间、地点；缺字段要追问。

保持阈值大致不变即可：

```kotlin
private val mediumRiskThreshold: Double = 0.7
```

修改追问话术：

```kotlin
validation.issues.any { it.contains("time", ignoreCase = true) } -> {
    "我识别到了校园通知，但时间还不够确定。请靠近海报时间区域，或直接告诉我完整日期和开始时间。"
}

validation.issues.any { it.contains("location", ignoreCase = true) } -> {
    "我识别到了校园通知，但地点还不够清楚。请对准地点区域，或直接说出教室 / 报告厅名称。"
}

else -> {
    "当前通知信息还不够完整。请调整拍摄角度，确保标题、时间和地点都在画面中。"
}
```

修改低置信度话术：

```kotlin
ModelConstants.ACTION_CREATE_EVENT -> {
    "我识别到了一个可能的校园日程，但还不够确定。请再靠近海报，或补充活动时间和地点。"
}

ModelConstants.ACTION_NAVIGATE -> {
    "我识别到了一个可能的校园地点，但还不够稳定。请对准地点文字再试。"
}

else -> {
    "当前画面还不够清晰，暂时无法生成稳定日程。请调整角度后重试。"
}
```

---

### 6.7 修改 `IntentDispatcher.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/intent/IntentDispatcher.kt
```

当前日历和地图 Intent 可以保留。建议只做小幅文案修改：

```kotlin
summary = "已打开日历确认页面：$title"
```

导航分支改为：

```kotlin
summary = "已打开地图，准备导航到：$location"
```

`sendSmsDraft()` 不需要删除，避免连带改动 Manifest 和测试，但当前 UI / Prompt / Mock 不应再调用它。

---

### 6.8 修改 `strings.xml`

文件：

```text
app/src/main/res/values/strings.xml
```

重点替换以下资源：

```xml
<string name="app_name">织时</string>
<string name="command_hint">输入或语音说出指令，例如：帮我把这个讲座加入日程</string>
<string name="default_command">请识别这张校园通知中的标题、时间和地点，并生成日程建议。</string>
<string name="mock_mode_showcase_title">织时已就绪</string>
<string name="mock_mode_showcase_subtitle">拍摄讲座海报、考试安排、社团活动或宣讲会通知，自动生成可确认的日程提醒。</string>
<string name="mock_mode_steps_title">快速体验路径</string>
<string name="mock_mode_step_pick_title">选择通知类型</string>
<string name="mock_mode_step_pick_summary">先选择讲座、考试、活动或宣讲会，快速进入校园日程识别流程。</string>
<string name="mock_mode_step_prompt_title">输入或说出需求</string>
<string name="mock_mode_step_prompt_summary">可以输入“帮我加入日程”，也可以使用语音触发识别。</string>
<string name="mock_mode_step_run_title">确认加入日历</string>
<string name="mock_mode_step_run_summary">系统会生成日程卡片，确认后再打开系统日历。</string>
<string name="mock_mode_highlight_actions_summary">重点验证校园通知识别、日程卡片、用户确认、日历和导航链路。</string>
```

保留通用错误文案，例如网络失败、模型限流、解析失败等。

---

### 6.9 修改 `CameraScreen.kt` UI 文案

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/ui/CameraScreen.kt
```

不要重构 UI，只替换文案和场景说明。

#### 6.9.1 `HeroCard()`

将标题文案从泛化 Agent 改为校园日程：

```kotlin
Text(
    text = "拍一下校园通知，自动生成日程提醒。",
    ...
)
```

副标题改为：

```kotlin
Text(
    text = "识别讲座、考试、社团活动和宣讲会中的标题、时间、地点，经确认后写入系统日历。",
    ...
)
```

Badge 建议：

```kotlin
val badge = when {
    isLoading -> "日程识别中"
    isVoiceListening -> "语音指令接入中"
    isMock -> "Mock 演示模式"
    else -> "Campus Schedule Agent"
}
```

#### 6.9.2 `ScenarioRow()`

标题从：

```kotlin
"核心场景"
```

改为：

```kotlin
"校园通知类型"
```

#### 6.9.3 `ResultPanel()`

默认空态改为：

```kotlin
summaryText.isBlank() -> "这里会显示识别到的校园日程信息，包括标题、时间、地点和建议动作。"
```

图表标题如果保留，可改为：

```kotlin
"识别字段"
```

#### 6.9.4 `ActionSuggestionCard()`

建议 chips 改为：

```kotlin
SuggestionChip(
    label = if (isLoading) "识别中" else "加入日历",
    active = true
)
SuggestionChip(
    label = if (isVoiceListening) "等待语音" else "地点导航",
    active = false
)
```

说明文字改为：

```kotlin
"日历和导航都需要用户确认后再执行，避免模型误识别造成错误操作。"
```

#### 6.9.5 `DemoChecklistCard()`

替换三步演示：

```kotlin
Text(text = "三步完成日程创建", ...)

DemoStep(
    index = "01",
    title = "拍通知",
    summary = "对准讲座海报、考试安排、社团活动或宣讲会通知。"
)
DemoStep(
    index = "02",
    title = "看卡片",
    summary = "AI 提取标题、时间、地点和备注，生成可确认日程卡片。"
)
DemoStep(
    index = "03",
    title = "加日历",
    summary = "用户确认后再打开系统日历，保留人工确认闭环。"
)
```

#### 6.9.6 `summarizePreset()` 和 `actionLabelForPreset()`

替换为：

```kotlin
private fun summarizePreset(id: String): String {
    return when (id) {
        "lecture" -> "识别讲座标题、时间、地点，并生成日程提醒。"
        "exam" -> "提取考试名称、考场、时间和注意事项。"
        "activity" -> "整理社团活动通知，避免错过报名和开始时间。"
        "career" -> "识别宣讲企业、时间地点和携带材料。"
        else -> "识别校园通知，生成可确认的日程建议。"
    }
}

private fun actionLabelForPreset(id: String): String {
    return when (id) {
        "lecture", "exam", "activity", "career" -> "生成日程"
        else -> "立即体验"
    }
}
```

---

### 6.10 修改 `MainActivity.kt`

文件：

```text
app/src/main/java/com/vsa/visualsemanticagent/MainActivity.kt
```

主流程不用大改。重点检查以下行为：

1. `onCaptureButtonClicked()` 仍然按以下顺序执行：
   - 获取命令
   - 捕获图片或 Mock 空图片
   - 调用 `sendToVLM()`
   - `ResponseInterpreter.normalize()`
   - `VisualActionIntentSchema.fromResponse()`
   - `riskPolicyEngine.evaluate()`
   - 显示确认卡片或追问
2. `onConfirmExecutionClicked()` 仍然只在用户点击确认后调用 `IntentDispatcher.dispatchIntent()`。
3. `buildResultCardText()` 中可以保留调试信息，但建议将「动作」「融合置信度」「执行模式」保留，便于答辩解释。
4. 如果想让结果卡更像日程卡，可把字段顺序调整为：标题 → 时间 → 地点 → 说明 → 动作 → 置信度 → 建议话术。

推荐小改 `buildResultCardText()` 的字段标题：

```kotlin
parts.add("建议动作：${intent.action}")
parts.add("识别可信度：${"%.2f".format(intent.fusedConfidence)}")
```

---

### 6.11 修改测试 `ModuleTests.kt`

文件：

```text
app/src/test/java/com/vsa/visualsemanticagent/ModuleTests.kt
```

新增或替换以下测试，确保收敛后的主链路稳定。

```kotlin
@Test
fun mockFactory_returnsCampusLectureEventForLecturePrompt() {
    val response = MockVLMResponseFactory.buildResponse("帮我把这个讲座加入日程")

    assertEquals(ModelConstants.ACTION_CREATE_EVENT, response.action)
    assertTrue(response.payload?.title?.contains("讲座") == true)
    assertNotNull(response.payload?.time)
    assertNotNull(response.payload?.location)
}

@Test
fun riskPolicyEngine_requiresClarificationWhenCampusEventTimeMissing() {
    val engine = RiskPolicyEngine()
    val intent = VisualActionIntentSchema.fromResponse(
        VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            confidence = 0.92,
            payload = VLMPayload(
                title = "就业宣讲会",
                location = "大学生活动中心"
            )
        )
    )

    val suggestion = engine.evaluate(intent)

    assertEquals(ExecutionMode.REQUIRE_CLARIFICATION, suggestion.mode)
    assertTrue(suggestion.validation.issues.any { it.contains("time") })
}

@Test
fun riskPolicyEngine_requiresConfirmationForValidCampusEvent() {
    val engine = RiskPolicyEngine()
    val intent = VisualActionIntentSchema.fromResponse(
        VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            confidence = 0.9,
            payload = VLMPayload(
                title = "人工智能前沿讲座",
                time = "2026-05-20T14:30:00",
                location = "图书馆报告厅"
            )
        )
    )

    val suggestion = engine.evaluate(intent)

    assertEquals(ExecutionMode.REQUIRE_CONFIRMATION, suggestion.mode)
}
```

如果旧测试仍然断言 `visual_to_tool_os`，需要改成新场景常量 `campus_schedule_agent`。

---

### 6.12 可选：API Key 本地化配置

文件：

```text
app/build.gradle
```

当前 `VLM_API_KEY` 是空字符串。建议不要把真实 Key 写进代码。可以在 Gradle 中读取 `local.properties`：

```gradle
def localProperties = new Properties()
def localPropertiesFile = rootProject.file('local.properties')
if (localPropertiesFile.exists()) {
    localPropertiesFile.withInputStream { stream ->
        localProperties.load(stream)
    }
}
def vlmApiKey = localProperties.getProperty("VLM_API_KEY", "")
```

然后将：

```gradle
buildConfigField "String", "VLM_API_KEY", "\"\""
```

改成：

```gradle
buildConfigField "String", "VLM_API_KEY", "\"${vlmApiKey}\""
```

本地 `local.properties` 示例：

```properties
VLM_API_KEY=你的vivo比赛AppKey
```

注意：`local.properties` 不应提交到仓库。

---

## 7. 验收标准

完成修改后，至少满足以下标准：

### 7.1 UI 验收

- App 名称显示为「织时」。
- 首页主标题围绕「拍通知 → 生成日程」。
- 快捷场景只包含校园通知相关入口：讲座、考试、社团活动、宣讲会。
- 页面不再主打「短信草稿」。
- 结果面板能展示标题、时间、地点、说明、置信度。
- 日历和导航都会显示确认卡片。

### 7.2 Mock 验收

在 Debug Mock 模式下：

1. 点击「讲座入日程」→ 点击「拍照执行」→ 返回 `create_event`。
2. 页面展示「人工智能前沿讲座」「2026-05-20T14:30:00」「图书馆报告厅」。
3. 出现确认卡片。
4. 点击确认后打开系统日历插入页。
5. 输入「导航到会场」或选择相关场景后，应返回 `navigate` 并确认后打开地图。
6. 输入「看不清」应返回 `clarification`，不打开日历。

### 7.3 单元测试验收

执行：

```bash
./gradlew testDebugUnitTest
```

或 Windows：

```powershell
.\gradlew.bat testDebugUnitTest
```

如果项目没有该 task，可执行：

```bash
./gradlew test
```

### 7.4 构建验收

确保 JDK 17 环境下构建通过：

```bash
./gradlew assembleDebug
```

Windows PowerShell 参考：

```powershell
$env:JAVA_HOME='E:\AIGC\tools\jdk17\jdk-17.0.19+10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

---

## 8. 不要做的事情

编码 AI 修改时请避免：

1. 不要把项目改成纯聊天机器人。
2. 不要删除相机、TTS、日历、地图等现有能力。
3. 不要新增复杂后端服务。
4. 不要直接调用 Calendar Provider 写入日程，当前版本保留系统日历确认页更安全。
5. 不要让模型输出自然语言再由客户端猜测动作，必须保持结构化 JSON。
6. 不要把真实 API Key 写入 Git 仓库。
7. 不要继续扩展短信、寻物、面板识别等支线。
8. 不要重命名包名，除非明确需要上架或提交。

---

## 9. 推荐实施顺序

建议编码 AI 按以下顺序提交修改，便于排查问题：

1. 修改 `strings.xml` 和 `PromptPresets.kt`，先完成产品文案收敛。
2. 修改 `CameraScreen.kt`，让 UI 变成校园日程助手。
3. 修改 `VLMNetworkClient.kt` 的系统 Prompt。
4. 修改 `MockVLMResponseFactory.kt`，确保 Debug 演示稳定。
5. 修改 `ResponseInterpreter.kt`，移除 `send_sms` 的对外支持。
6. 修改 `ExecutableIntent.kt` 和 `RiskPolicyEngine.kt` 的话术。
7. 补充或修正 `ModuleTests.kt`。
8. 运行单元测试和 `assembleDebug`。
9. 最后再接真实 vivo 模型接口。

---

## 10. 给编码 AI 的直接执行提示词

可以把下面这段直接发给代码修改 AI：

```text
请在现有 Android Kotlin 项目 VisualSemanticAgent 上做小范围修改，不要重写架构。目标是把项目从泛化的 Visual-to-Tool OS 收敛为「织时」。主场景是：拍摄校园讲座海报、考试安排、社团活动通知或就业宣讲会海报，识别标题、时间、地点和备注，生成可确认的日程卡片，用户确认后通过系统日历 Intent 创建日程；地点导航作为辅助能力。请隐藏短信草稿和通用视觉 Agent 表达。

请按 AI_CODE_MODIFICATION_GUIDE.md 修改：
1. 替换 PromptPresets 为讲座、考试、社团活动、宣讲会四个场景。
2. 将 VLMNetworkClient 的 system prompt 改成校园通知转日程专用，只允许 create_event、navigate、clarification、tts_feedback、unknown，不再选择 send_sms。
3. 将 MockVLMResponseFactory 的示例改成校园讲座、考试、宣讲会、导航、看不清这几类。
4. UI 文案改为「织时」「拍一下校园通知，自动生成日程提醒」。
5. ResponseInterpreter 不再把 send_sms 作为当前支持 action。
6. 日历和导航必须经过确认卡片后执行。
7. 补充校园日程相关单元测试，并保证 assembleDebug 通过。
```

---

## 11. 最终答辩表达

修改完成后，项目可以这样介绍：

> 本项目选择 APP 形态交付，但内部采用视觉语义智能体架构。用户只需用 vivo 手机拍摄校园讲座、考试安排、社团活动或宣讲会通知，系统即可调用多模态大模型提取标题、时间、地点和备注，生成结构化日程卡片。用户确认后，APP 再调用系统日历或地图完成执行。我们保留了「识别、结构化、校验、确认、执行」的人机协同闭环，避免模型误识别直接操作手机系统。
