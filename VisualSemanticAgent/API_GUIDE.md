# API 开发指南

## 目录

1. [模块API参考](#1-模块api参考)
2. [集成示例](#2-集成示例)
3. [最佳实践](#3-最佳实践)
4. [常见问题](#4-常见问题)

---

## 1. 模块API参考

### 1.1 相机管理模块 (CameraManager)

**功能**: 管理设备摄像头，提供实时视频流

#### 初始化

```kotlin
val cameraManager = CameraManager()

// 在Activity/Fragment中初始化
cameraManager.initCamera(
    context = this,
    lifecycleOwner = this,
    onError = { error ->
        Log.e("Camera", "Camera error: ${error.message}")
    }
)
```

#### 获取视频流

```kotlin
// 返回 Flow<Bitmap> 实时帧
val frameFlow = cameraManager.getFrameFlow()

lifecycleScope.launch {
    frameFlow.collect { bitmap ->
        // 处理每一帧
        processFrame(bitmap)
    }
}
```

#### 捕获单帧

```kotlin
val currentFrame: Bitmap? = cameraManager.captureFrame()
currentFrame?.let {
    // 处理捕获的帧
    analyzeImage(it)
}
```

#### 控制摄像头

```kotlin
// 切换前后摄像头
cameraManager.switchCamera()

// 控制闪光灯
cameraManager.toggleFlash(true)  // 打开
cameraManager.toggleFlash(false) // 关闭

// 调整对焦
cameraManager.setFocusMode(FocusMode.AUTO)
cameraManager.setFocusMode(FocusMode.MACRO)

// 调整缩放
cameraManager.setZoom(1.5f)  // 1.0f = 无缩放
```

#### 生命周期管理

```kotlin
// 恢复摄像头（Activity/Fragment onResume）
cameraManager.resume()

// 暂停摄像头（Activity/Fragment onPause）
cameraManager.pause()

// 释放资源（Activity/Fragment onDestroy）
cameraManager.release()
```

#### 完整示例

```kotlin
class CameraActivity : AppCompatActivity() {
    private val cameraManager = CameraManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化相机
        cameraManager.initCamera(this, this)
    }

    override fun onResume() {
        super.onResume()
        cameraManager.resume()

        // 开始处理帧
        lifecycleScope.launch {
            cameraManager.getFrameFlow().collect { bitmap ->
                onFrameAvailable(bitmap)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cameraManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.release()
    }
}
```

---

### 1.2 VLM 网络客户端 (VLMNetworkClient)

**功能**: 与视觉语言模型通信，进行图像分析

#### 初始化

```kotlin
val vlmClient = VLMNetworkClient(
    context = this,
    apiKey = "your-api-key",
    modelName = "gpt-4-vision"
)
```

#### 图像分析

```kotlin
// 单图分析
val analysisResult = vlmClient.analyzeImage(
    bitmap = imageBitmap,
    prompt = "请分析这张图中的物体"
)

// 分析结果包含
data class AnalysisResult(
    val objects: List<DetectedObject>,      // 检测到的对象
    val caption: String,                     // 图像描述
    val embeddings: FloatArray,              // 特征向量
    val confidence: Float,                   // 置信度
    val timestamp: Long                      // 时间戳
)
```

#### 对象检测

```kotlin
// 检测图像中的所有对象
val detectedObjects = vlmClient.detectObjects(bitmap)

// 检测结果
detectedObjects.forEach { obj ->
    Log.d("Detection", """
        Label: ${obj.label}
        Confidence: ${obj.confidence}
        BoundingBox: ${obj.boundingBox}
    """.trimIndent())
}
```

#### 生成描述

```kotlin
// 生成自然语言描述
val caption = vlmClient.generateCaption(bitmap)
Log.d("Caption", caption)
```

#### 异步操作

```kotlin
// 使用 suspend 函数配合 Coroutines
lifecycleScope.launch {
    try {
        val result = vlmClient.analyzeImage(bitmap)
        updateUI(result)
    } catch (e: Exception) {
        handleError(e)
    }
}
```

#### 批量分析

```kotlin
// 分析多张图片
val bitmaps = listOf(bitmap1, bitmap2, bitmap3)
val results = vlmClient.analyzeBatch(bitmaps)

results.forEachIndexed { index, result ->
    Log.d("Batch Analysis", "Image $index: ${result.caption}")
}
```

#### 配置

```kotlin
// 配置分析参数
vlmClient.setConfig(
    temperature = 0.7f,      // 模型温度（0-1）
    maxTokens = 500,         // 最大输出令牌数
    timeout = 30000L         // 请求超时（毫秒）
)

// 设置重试策略
vlmClient.setRetryPolicy(
    maxRetries = 3,
    backoffMultiplier = 2.0f
)
```

#### 完整示例

```kotlin
class ImageAnalysisActivity : AppCompatActivity() {
    private lateinit var vlmClient: VLMNetworkClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vlmClient = VLMNetworkClient(this, "api-key", "gpt-4-vision")
    }

    private fun analyzeImage(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val result = vlmClient.analyzeImage(
                    bitmap = bitmap,
                    prompt = "描述这张图中的内容"
                )

                Log.d("Analysis", result.caption)
                result.objects.forEach { obj ->
                    Log.d("Object", "${obj.label}: ${obj.confidence}")
                }
            } catch (e: Exception) {
                Log.e("VLM", "分析失败: ${e.message}")
            }
        }
    }
}
```

---

### 1.3 意图分派器 (IntentDispatcher)

**功能**: 解析用户意图，协调模块执行

#### 初始化

```kotlin
val intentDispatcher = IntentDispatcher(
    context = this,
    cameraManager = cameraManager,
    vlmClient = vlmClient,
    voiceManager = voiceManager,
    ttsManager = ttsManager
)
```

#### 解析意图

> 说明：以下 `parseIntent/executeIntent` 示例属于早期概念设计接口，不是当前 Android 原型中的真实公开 API。
> 当前工程以 `MainActivity -> VLMNetworkClient -> ResponseInterpreter -> IntentDispatcher` 这条主链路为准。

```kotlin
// 从自然语言解析意图
val intent = intentDispatcher.parseIntent(
    userInput = "这是什么?",
    context = analysisResult
)

// Intent 对象
data class Intent(
    val id: String,
    val type: String,           // "query", "control", "interact"
    val action: String,
    val parameters: Map<String, Any>,
    val confidence: Float
)
```

#### 执行意图

```kotlin
// 执行意图并获取流式结果
val resultFlow = intentDispatcher.executeIntent(intent)

lifecycleScope.launch {
    resultFlow.collect { result ->
        when {
            result.isLoading -> showLoadingUI()
            result.isSuccess -> updateResultUI(result.data)
            result.isError -> showErrorUI(result.error)
        }
    }
}
```

#### 意图类型

```kotlin
// 查询意图 - 分析、识别、描述
val queryIntent = Intent(
    type = "query",
    action = "describe",
    parameters = mapOf("detail_level" to "high")
)

// 控制意图 - 执行动作
val controlIntent = Intent(
    type = "control",
    action = "capture",
    parameters = mapOf("quality" to "high")
)

// 交互意图 - 对话、反馈
val interactIntent = Intent(
    type = "interact",
    action = "confirm",
    parameters = mapOf("timeout" to 5000)
)
```

#### 取消执行

```kotlin
// 取消正在执行的意图
intentDispatcher.cancelIntent(intentId)

// 取消所有意图
intentDispatcher.cancelAllIntents()
```

#### 监听执行状态

```kotlin
intentDispatcher.observeExecutionState().collect { state ->
    when (state) {
        ExecutionState.IDLE -> Log.d("Intent", "就绪")
        ExecutionState.PROCESSING -> Log.d("Intent", "处理中")
        ExecutionState.EXECUTING -> Log.d("Intent", "执行中")
        ExecutionState.COMPLETED -> Log.d("Intent", "完成")
        ExecutionState.FAILED -> Log.d("Intent", "失败")
    }
}
```

#### 完整示例

```kotlin
class IntentActivity : AppCompatActivity() {
    private lateinit var intentDispatcher: IntentDispatcher

    private fun handleUserInput(userInput: String) {
        lifecycleScope.launch {
            // 获取当前分析结果
            val context = getCurrentAnalysisResult()

            // 解析意图
            val intent = intentDispatcher.parseIntent(userInput, context)

            Log.d("Intent", """
                Type: ${intent.type}
                Action: ${intent.action}
                Confidence: ${intent.confidence}
            """.trimIndent())

            // 执行意图
            intentDispatcher.executeIntent(intent).collect { result ->
                when {
                    result.isSuccess -> {
                        val output = result.output
                        showResult(output)
                        ttsManager.speak(output)
                    }
                    result.isError -> {
                        val error = result.error?.message ?: "未知错误"
                        showError(error)
                    }
                }
            }
        }
    }
}
```

---

### 1.4 语音识别管理器 (VoiceRecognitionManager)

**功能**: 将语音转换为文本

#### 初始化

```kotlin
val voiceManager = VoiceRecognitionManager(context = this)

// 配置
voiceManager.setLanguage("zh-CN")  // 中文
voiceManager.setSpeechRate(1.0f)   // 语速
```

#### 开始监听

```kotlin
lifecycleScope.launch {
    try {
        val recognizedText = voiceManager.startListening(
            timeout = 10000L,  // 10秒超时
            onPartialResult = { partial ->
                // 实时返回识别中的文本
                Log.d("Speech", "Partial: $partial")
            }
        )

        Log.d("Speech", "Final: $recognizedText")
        processUserInput(recognizedText)
    } catch (e: Exception) {
        Log.e("Speech", "识别失败: ${e.message}")
    }
}
```

#### 停止监听

```kotlin
voiceManager.stopListening()
```

#### 处理结果

```kotlin
// 设置识别结果回调
voiceManager.setOnResultListener { results ->
    val bestResult = results.firstOrNull() ?: return@setOnResultListener
    Log.d("Speech", "识别结果: $bestResult")
}

// 设置错误回调
voiceManager.setOnErrorListener { error ->
    Log.e("Speech", "错误: $error")
}
```

#### 完整示例

```kotlin
class VoiceActivity : AppCompatActivity() {
    private lateinit var voiceManager: VoiceRecognitionManager

    fun startVoiceInput() {
        lifecycleScope.launch {
            try {
                val text = voiceManager.startListening(timeout = 15000)
                handleUserInput(text)
            } catch (e: Exception) {
                showError("语音识别失败")
            }
        }
    }
}
```

---

### 1.5 文本转语音管理器 (TextToSpeechManager)

**功能**: 将文本转换为语音并播放

#### 初始化

```kotlin
val ttsManager = TextToSpeechManager(context = this)

// 配置
ttsManager.setLanguage("zh-CN")    // 中文
ttsManager.setPitch(1.0f)          // 音高
ttsManager.setSpeechRate(1.0f)     // 语速
ttsManager.setVolume(1.0f)         // 音量
```

#### 播放文本

```kotlin
// 同步播放
ttsManager.speak("这是一个测试句子")

// 异步播放
ttsManager.speakAsync(
    text = "这是一个测试句子",
    onStart = {
        Log.d("TTS", "开始播放")
    },
    onDone = {
        Log.d("TTS", "播放完成")
    },
    onError = { error ->
        Log.e("TTS", "播放错误: $error")
    }
)
```

#### 停止播放

```kotlin
ttsManager.stop()
```

#### 队列管理

```kotlin
// 添加到队列
ttsManager.enqueue("第一句话")
ttsManager.enqueue("第二句话")
ttsManager.enqueue("第三句话")

// 清空队列
ttsManager.clearQueue()

// 暂停/继续
ttsManager.pause()
ttsManager.resume()
```

#### 完整示例

```kotlin
class TTSActivity : AppCompatActivity() {
    private lateinit var ttsManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsManager = TextToSpeechManager(this)

        ttsManager.setLanguage("zh-CN")
        ttsManager.setSpeechRate(1.2f)
    }

    fun announceResult(result: String) {
        ttsManager.speakAsync(
            text = result,
            onStart = { showPlayingIndicator() },
            onDone = { hidePlayingIndicator() }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
```

---

### 1.6 编码工具 (EncodingUtils)

**功能**: 图像编码、解码、格式转换

#### 图像编码

```kotlin
// Bitmap 转 Base64
val base64String = EncodingUtils.bitmapToBase64(bitmap)

// Bitmap 转 ByteArray
val byteArray = EncodingUtils.bitmapToByteArray(bitmap, format = "JPEG")

// Bitmap 压缩
val compressedBitmap = EncodingUtils.compressBitmap(
    bitmap = bitmap,
    quality = 85,
    targetWidth = 1920,
    targetHeight = 1080
)
```

#### 图像解码

```kotlin
// Base64 转 Bitmap
val bitmap = EncodingUtils.base64ToBitmap(base64String)

// ByteArray 转 Bitmap
val bitmap = EncodingUtils.byteArrayToBitmap(byteArray)
```

#### 图像格式转换

```kotlin
// 调整分辨率
val resized = EncodingUtils.resizeBitmap(bitmap, width = 640, height = 480)

// 旋转
val rotated = EncodingUtils.rotateBitmap(bitmap, degrees = 90)

// 裁剪
val cropped = EncodingUtils.cropBitmap(bitmap, x = 0, y = 0, width = 100, height = 100)

// 灰度化
val gray = EncodingUtils.toGrayscale(bitmap)
```

#### 完整示例

```kotlin
// 编码流程
val bitmap = cameraManager.captureFrame()
val compressed = EncodingUtils.compressBitmap(bitmap, quality = 85)
val base64 = EncodingUtils.bitmapToBase64(compressed)

// 发送到VLM
vlmClient.analyzeImageBase64(base64)
```

---

## 2. 集成示例

### 2.1 完整应用流程

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var vlmClient: VLMNetworkClient
    private lateinit var voiceManager: VoiceRecognitionManager
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var intentDispatcher: IntentDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化所有模块
        initializeModules()

        // 启动相机
        startCamera()

        // 设置语音命令监听
        setupVoiceCommandListener()
    }

    private fun initializeModules() {
        cameraManager = CameraManager()
        vlmClient = VLMNetworkClient(this, "api-key", "gpt-4-vision")
        voiceManager = VoiceRecognitionManager(this)
        ttsManager = TextToSpeechManager(this)

        intentDispatcher = IntentDispatcher(
            context = this,
            cameraManager = cameraManager,
            vlmClient = vlmClient,
            voiceManager = voiceManager,
            ttsManager = ttsManager
        )
    }

    private fun startCamera() {
        cameraManager.initCamera(this, this)

        lifecycleScope.launch {
            cameraManager.getFrameFlow().collect { bitmap ->
                // 定期分析帧
                analyzeFrame(bitmap)
            }
        }
    }

    private fun setupVoiceCommandListener() {
        // 当用户说话时
        lifecycleScope.launch {
            try {
                val command = voiceManager.startListening(timeout = 10000)
                processCommand(command)
            } catch (e: Exception) {
                Log.e("App", "语音输入失败")
            }
        }
    }

    private suspend fun analyzeFrame(bitmap: Bitmap) {
        val result = vlmClient.analyzeImage(bitmap)
        updateAnalysisUI(result)
    }

    private suspend fun processCommand(userInput: String) {
        val context = getCurrentAnalysisResult()
        val intent = intentDispatcher.parseIntent(userInput, context)

        intentDispatcher.executeIntent(intent).collect { executionResult ->
            when {
                executionResult.success -> {
                    ttsManager.speak(executionResult.output)
                    showResult(executionResult)
                }
                executionResult.error != null -> {
                    val errorMsg = executionResult.error.message ?: "未知错误"
                    ttsManager.speak("操作失败: $errorMsg")
                }
            }
        }
    }
}
```

---

## 3. 最佳实践

### 3.1 错误处理

```kotlin
// ✅ 好的实践
lifecycleScope.launch {
    try {
        val result = vlmClient.analyzeImage(bitmap)
    } catch (e: IOException) {
        Log.e("Network", "网络错误", e)
        showNetworkError()
    } catch (e: TimeoutException) {
        Log.e("Network", "超时", e)
        retry()
    } catch (e: Exception) {
        Log.e("App", "未预期的错误", e)
        showGenericError()
    }
}
```

### 3.2 生命周期管理

```kotlin
// ✅ 好的实践 - 在合适的时机初始化和释放
override fun onResume() {
    super.onResume()
    cameraManager.resume()
    voiceManager.resume()
}

override fun onPause() {
    super.onPause()
    cameraManager.pause()
    voiceManager.pause()
}

override fun onDestroy() {
    super.onDestroy()
    cameraManager.release()
    voiceManager.release()
    ttsManager.shutdown()
}
```

### 3.3 内存管理

```kotlin
// ✅ 好的实践 - 及时释放大型对象
val bitmap = cameraManager.captureFrame()
try {
    val result = vlmClient.analyzeImage(bitmap)
    // 处理结果
} finally {
    bitmap.recycle()
}
```

### 3.4 线程安全

```kotlin
// ✅ 好的实践 - 使用 Coroutines
lifecycleScope.launch(Dispatchers.Main) {
    val result = withContext(Dispatchers.Default) {
        // CPU密集操作
        vlmClient.analyzeImage(bitmap)
    }
    // 更新UI
    updateUI(result)
}
```

---

## 4. 常见问题

### Q: 如何处理网络超时?

A: 使用 `VLMNetworkClient.setRetryPolicy()` 设置重试策略，或在 catch 块中手动重试。

### Q: 相机权限如何处理?

A: 系统会自动处理，确保在 `AndroidManifest.xml` 中声明权限。

### Q: 如何优化内存使用?

A: 使用 `EncodingUtils.compressBitmap()` 压缩图像，及时回收 Bitmap 对象。

### Q: 支持离线模式吗?

A: 目前需要网络连接。考虑在未来版本添加本地模型支持。

### Q: 如何自定义意图处理?

A: 创建 `IntentDispatcher` 的子类，覆盖 `executeIntent()` 方法。
