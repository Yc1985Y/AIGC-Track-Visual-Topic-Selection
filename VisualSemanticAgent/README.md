# 视觉语义执行代理（Visual Semantic Action Agent）

## 📋 项目概述

本项目是一个基于Android的智能Agent系统，整合了视觉大模型、语音识别、文本转语音和系统Intent调度等功能，实现从物理世界的视觉感知到操作系统数字动作的完整链路。

**核心愿景**：赋予移动设备看懂物理世界的能力，并直接在手机系统中自动执行复杂的数字动作，从而形成完整的智能体（Agent）工作流。

---

## 🏗️ 架构体系

### 三层核心架构

```
┌─────────────────────────────────────────────────────────┐
│              INPUT LAYER (感知层)                       │
│  CameraX单帧截图 + SpeechRecognizer语音识别             │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────▼─────────────┐
        │   Base64编码 + 文本打包   │
        └────────────┬─────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│           PROCESSING LAYER (认知层)                     │
│  视觉大语言模型API (Claude/GPT-4V/BlueLM)               │
│  严格Prompt工程 + 结构化JSON输出                        │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────▼──────────────┐
        │   JSON解析 + 脏数据清洗    │
        └────────────┬──────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│           EXECUTION LAYER (行动层)                      │
│  Android Intent路由 + 系统应用调度                      │
│  TextToSpeech反馈 + UI加载状态管理                      │
└─────────────────────────────────────────────────────────┘
```

### 四个核心模块

#### 📱 模块A：UI交互基座 (`ui/`)

- **文件**：`CameraScreen.kt`, `LoadingOverlay.kt`
- **功能**：
  - Jetpack Compose全屏相机界面
  - 圆形FAB拍摄按钮 + 触觉反馈
  - 全屏Loading Overlay（半透明背景）
  - 手势物理拦截（防止并发请求）
  - 阶段性动态提示文案

**关键特性**：

```kotlin
// 视觉阻断层 + 手势拦截 + 动态心理抚慰
LoadingOverlay(
    isVisible = isLoading,
    currentStage = currentStage,  // 0: 扫描, 1: 分析, 2: 生成
    stageMessages = listOf(
        "正在扫描物理空间特征…",
        "云端语义深度解析中…",
        "正在生成执行策略…"
    )
)
```

#### 📷 模块B：CameraX截帧与Base64转换 (`camera/`, `utils/`)

- **文件**：`CameraManager.kt`, `EncodingUtils.kt`
- **功能**：
  - CameraX单帧拍照（ImageCapture）
  - ImageProxy -> Bitmap内存转换
  - JPEG压缩（质量80）+ Base64编码
  - 纯内存处理（避免磁盘I/O）

**关键实现**：

```kotlin
// 摒弃视频流，采用离散化截图
imageCapture = ImageCapture.Builder()
    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
    .build()

// 内存中直接转换为Base64（无磁盘I/O）
val baos = ByteArrayOutputStream()
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
val base64String = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
```

#### 🌐 模块C：网络通信与结构化输出 (`network/`)

- **文件**：`VLMNetworkClient.kt`
- **功能**：
  - OpenAI兼容API调用
  - 多模态请求载荷构建
  - 三重约束强制JSON输出
  - JSON脏数据清洗

**关键机制**：

1. **系统提示词（三重约束）**：
   - 强制JSON-only输出
   - 角色设定与红线声明
   - JSON Schema约束

2. **API级别结构化输出**：

   ```json
   {
     "action": "create_event|navigate|tts_feedback|send_sms|unknown",
     "title": "string",
     "time": "unix_timestamp",
     "location": "string",
     "answer": "string",
     "target_found": boolean
   }
   ```

3. **脏数据清洗**：
   ```kotlin
   // 自动提取JSON，移除Markdown包装
   val cleanJson = JsonCleansingUtils.extractJsonFromDirtyText(dirtyResponse)
   ```

#### 🚀 模块D：Intent路由引擎 (`intent/`)

- **文件**：`IntentDispatcher.kt`
- **功能**：
  - JSON反序列化
  - Intent构造与分发
  - 意图解析预检（ActivityNotFoundException防护）

**支持的操作**：

```kotlin
when (response.action) {
    "create_event"    -> createCalendarEvent()    // 日历事件
    "navigate"        -> navigateToLocation()     // 地图导航
    "tts_feedback"    -> playTTS()                // 语音反馈
    "send_sms"        -> sendSMS()                // 短信发送
    else              -> handleUnknown()
}
```

#### 🎤 辅助模块：语音与文本处理

- **VoiceRecognitionManager** (`voice/`)：原生SpeechRecognizer
- **TextToSpeechManager** (`tts/`)：原生TextToSpeech引擎

---

## 📂 项目结构

```
VisualSemanticAgent/
├── app/
│   ├── src/main/
│   │   ├── java/com/vsa/visualsemanticagent/
│   │   │   ├── ui/                          # 模块A：UI基座
│   │   │   │   ├── CameraScreen.kt
│   │   │   │   └── LoadingOverlay.kt
│   │   │   ├── camera/                      # 模块B：截帧
│   │   │   │   └── CameraManager.kt
│   │   │   ├── network/                     # 模块C：网络通信
│   │   │   │   └── VLMNetworkClient.kt
│   │   │   ├── intent/                      # 模块D：Intent路由
│   │   │   │   └── IntentDispatcher.kt
│   │   │   ├── model/                       # 数据模型
│   │   │   │   └── VLMModels.kt
│   │   │   ├── voice/                       # 语音识别
│   │   │   │   └── VoiceRecognitionManager.kt
│   │   │   ├── tts/                         # 文本转语音
│   │   │   │   └── TextToSpeechManager.kt
│   │   │   ├── utils/                       # 工具函数
│   │   │   │   └── EncodingUtils.kt
│   │   │   ├── MainActivity.kt              # 应用入口
│   │   │   └── BuildConfig.kt
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle                        # App级配置
│   └── proguard-rules.pro
├── build.gradle                            # 项目级配置
├── settings.gradle
└── README.md
```

---

## 🚀 核心工作流程

### 完整执行链路

```
用户拍摄 + 语音指令
        │
        ▼
    模块B: CameraX截帧
    ├─ 单帧图像捕获
    ├─ ImageProxy -> Bitmap转换
    └─ JPEG压缩 + Base64编码
        │
        ▼
    模块C: 网络通信
    ├─ 构建OpenAI兼容请求
    ├─ 系统提示词约束
    └─ 云端VLM推理
        │
        ▼
    JSON响应处理
    ├─ 脏数据清洗
    ├─ Gson反序列化
    └─ Schema验证
        │
        ▼
    模块D: Intent分发
    ├─ Action路由判断
    ├─ 参数提取
    └─ 系统应用调起
        │
        ▼
    TTS反馈 / 异常处理
```

---

## 🎯 应用场景

### 场景1：物理信息实体化（Physical to OS Action）

- **用户行为**：拍摄海报 + "帮我把这个活动安排进行程"
- **系统流程**：
  1. 视觉识别：活动名称、时间、地点
  2. 构造Intent：`ACTION_INSERT` + CalendarContract
  3. 结果：系统日历应用弹起，日程已填充

### 场景2：复杂物理界面降噪（Physical UI Copilot）

- **用户行为**：对准微波炉面板 + "我只想解冻这块肉，该按哪个键？"
- **系统流程**：
  1. 视觉分析：识别所有图标、按键位置
  2. 语义推理：关联"解冻"与雪花按钮
  3. 结果：TTS播报"请按第三排左数第二个…"

### 场景3：无障碍视觉寻物（Semantic Targeting）

- **用户行为**：对准杂乱房间 + "我的黑色保温杯在哪里？"
- **系统流程**：
  1. 视觉识别：检测保温杯位置
  2. 空间推理：相对位置关系（相对于笔记本电脑右侧）
  3. 结果：TTS播报"在笔记本电脑的右侧，靠近键盘边缘…"

---

## ⚙️ 技术栈

| 组件     | 技术                | 版本   |
| -------- | ------------------- | ------ |
| UI框架   | Jetpack Compose     | 1.6.1  |
| 相机     | CameraX             | 1.3.1  |
| 网络     | OkHttp3             | 4.11.0 |
| JSON解析 | Gson                | 2.10.1 |
| 日志     | Timber              | 5.0.1  |
| 协程     | Kotlin Coroutines   | 1.9.10 |
| 编译目标 | Android 34 (API 34) |        |
| 最低版本 | Android 10 (API 29) |        |

---

## 🔧 配置与依赖

### 权限声明（AndroidManifest.xml）

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
```

### API密钥配置

在 `VLMNetworkClient` 中配置云端API：

```kotlin
val client = VLMNetworkClient(
    apiKey = "your-api-key-here",
    modelName = "claude-3-5-sonnet-20241022",
    apiEndpoint = "https://api.anthropic.com/v1/messages"
)
```

---

## 🛡️ 稳定性与防护策略

### 1. 延迟掩盖（UX心理学）

- ✅ 全屏Loading Overlay + 半透明背景
- ✅ 手势物理拦截（防止并发请求）
- ✅ 阶段性动态提示文案（0-3秒逐段更新）

### 2. 异常隔离（多层防护）

```kotlin
try {
    // 完整流程
    val vlmResponse = sendToVLM(base64Image, userText)
    dispatchIntent(vlmResponse)
} catch (e: SocketTimeoutException) {
    // 网络超时
    playFallbackMessage()
} catch (e: JsonSyntaxException) {
    // JSON解析失败
    playFallbackMessage()
} catch (e: ActivityNotFoundException) {
    // 目标应用不存在
    playFallbackMessage()
}
```

### 3. 优雅的兜底话术

```kotlin
// 任何异常情况下，播放预设的友好消息
playTextToSpeech(
    "抱歉，当前的物理环境特征过于复杂，我未能完全看清，" +
    "能请您稍微靠近一点或者调整一下光线再试一次吗？"
)
```

---

## 📝 使用指南

### 编译和运行

```bash
# 克隆或解压项目
cd VisualSemanticAgent

# 配置gradle（确保Android SDK已安装）
./gradlew clean

# 编译Debug版本
./gradlew assembleDebug

# 编译Release版本
./gradlew assembleRelease

# 直接安装到设备
./gradlew installDebug
```

### 实现自定义业务逻辑

#### 1. 扩展Action类型

编辑 `network/VLMNetworkClient.kt` 的系统提示词：

```kotlin
private fun buildSystemPrompt(): String {
    return """
    ...
    "action": "create_event|navigate|tts_feedback|send_sms|YOUR_NEW_ACTION|unknown"
    ...
    """
}
```

#### 2. 添加新的Intent分发器

编辑 `intent/IntentDispatcher.kt`：

```kotlin
when (response.action) {
    "your_new_action" -> handleYourNewAction(response)
    ...
}
```

#### 3. 调整AI提示词

优化 `VLMNetworkClient.buildSystemPrompt()` 中的JSON Schema和场景指导

---

## 🎓 AI辅助编码指南

### 模块A：UI框架提示词

```
"请扮演资深Android UI架构师，使用Kotlin和Jetpack Compose编写全屏相机界面。
要求：
1. 底部中央圆形FAB按钮
2. 点击触发HapticFeedbackType.LongPress
3. 完整的Composable函数结构
4. 响应式布局设计"
```

### 模块B：CameraX截帧提示词

```
"使用CameraX ImageCapture实现内存中的单帧图像处理。
要求：
1. OnImageCapturedCallback获取ImageProxy
2. ImageProxy.planes提取字节缓冲
3. BitmapFactory.decodeByteArray解析
4. ByteArrayOutputStream压缩为JPEG (质量80)
5. Base64.encodeToString返回字符串
6. 绝对禁止磁盘I/O"
```

### 模块C：网络请求提示词

```
"使用OkHttp3编写VLM网络请求类。
要求：
1. Kotlin Coroutine suspend函数
2. 接收Base64图片和文本
3. 构建OpenAI兼容多模态Payload
4. response_format={"type": "json_object"}
5. 系统提示词包含严格JSON约束
6. 15秒超时设置"
```

### 模块D：Intent路由提示词

```
"使用Kotlin编写Intent分发器。
要求：
1. 接收Gson解析的VLMResponse
2. when语句根据action分支
3. create_event: Intent.ACTION_INSERT + CalendarContract
4. navigate: Intent.ACTION_VIEW + google.navigation:q=
5. tts_feedback: TextToSpeech朗读
6. 每次startActivity前检查resolveActivity"
```

---

## 📚 参考资源

- [Android CameraX官方文档](https://developer.android.com/media/camera/camerax)
- [Jetpack Compose官方文档](https://developer.android.com/develop/ui/compose)
- [Claude API文档](https://docs.anthropic.com/claude/)
- [Android Intent官方文档](https://developer.android.com/guide/components/intents-common)

---

## 📄 许可证

本项目用于教育和研究用途。

---

## ✨ 贡献指南

欢迎提交Issue和Pull Request！

---

**最后更新**：2026年5月1日
**版本**：1.0.0
