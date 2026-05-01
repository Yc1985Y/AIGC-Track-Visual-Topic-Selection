# 开发规范与项目指南

## 目录

1. [编码规范](#1-编码规范)
2. [代码组织](#2-代码组织)
3. [提交规范](#3-提交规范)
4. [测试规范](#4-测试规范)
5. [文档规范](#5-文档规范)
6. [性能优化指南](#6-性能优化指南)
7. [安全实践](#7-安全实践)
8. [调试和日志](#8-调试和日志)

---

## 1. 编码规范

### 1.1 Kotlin 编码规范

#### 命名约定

```kotlin
// ✅ 正确
class CameraManager
fun captureFrame()
val cameraInstance
private val MAX_RETRIES = 3

// ❌ 错误
class camera_manager
fun CaptureFrame
val _cameraInstance
private val maxRetries = 3  // 常量应为大写
```

#### 函数

```kotlin
// ✅ 好的实践
suspend fun analyzeImage(
    bitmap: Bitmap,
    modelName: String = "default"
): AnalysisResult {
    return withContext(Dispatchers.Default) {
        // 实现
    }
}

// ❌ 应避免
fun analyzeImage(bitmap: Bitmap, modelName: String): AnalysisResult? {
    // 可能返回null，不够明确
}
```

#### 空安全

```kotlin
// ✅ 好的实践
val result = bitmap?.let {
    analyzeImage(it)
} ?: run {
    Log.w("Analysis", "Bitmap is null")
    null
}

// ❌ 应避免
val result = analyzeImage(bitmap!!)  // 可能导致crash
```

#### 集合操作

```kotlin
// ✅ 好的实践
val labels = detectedObjects
    .filter { it.confidence > 0.5f }
    .map { it.label }
    .distinct()

// ❌ 应避免
val labels = mutableListOf<String>()
for (obj in detectedObjects) {
    if (obj.confidence > 0.5f) {
        if (!labels.contains(obj.label)) {
            labels.add(obj.label)
        }
    }
}
```

### 1.2 类设计规范

```kotlin
// ✅ 好的实践 - 清晰的职责分离
class VLMNetworkClient(
    private val context: Context,
    private val apiKey: String
) {
    private val httpClient: OkHttpClient = createHttpClient()
    private val gson: Gson = Gson()

    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult {
        val encoded = encodeImage(bitmap)
        return makeRequest(encoded)
    }

    private suspend fun makeRequest(encodedImage: String): AnalysisResult {
        // 实现
    }
}

// ❌ 应避免 - 职责过多
class VLMClient {
    fun doEverything(bitmap: Bitmap): String {
        // 混合了网络、图像处理、UI更新等多个职责
    }
}
```

---

## 2. 代码组织

### 2.1 包结构

```
com/vsa/visualsemanticagent/
├── camera/               # 摄像头相关
│   └── CameraManager.kt
├── model/                # 数据模型
│   └── VLMModels.kt
├── network/              # 网络通信
│   └── VLMNetworkClient.kt
├── intent/               # 意图分派
│   └── IntentDispatcher.kt
├── voice/                # 语音识别
│   └── VoiceRecognitionManager.kt
├── tts/                  # 文本转语音
│   └── TextToSpeechManager.kt
├── ui/                   # 用户界面
│   ├── CameraScreen.kt
│   └── LoadingOverlay.kt
├── utils/                # 工具类
│   └── EncodingUtils.kt
└── MainActivity.kt       # 主入口
```

### 2.2 类的内部组织

```kotlin
class VLMNetworkClient(
    private val context: Context,
    private val apiKey: String
) {

    // ========== 常量 ==========
    companion object {
        private const val API_ENDPOINT = "https://api.example.com"
        private const val TIMEOUT_MS = 30000L
    }

    // ========== 属性 ==========
    private val httpClient = createHttpClient()
    private val gson = Gson()
    private var currentRequest: Call? = null

    // ========== 初始化块 ==========
    init {
        Log.d("VLMClient", "Client initialized")
    }

    // ========== 公共方法 ==========
    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult {
        return withContext(Dispatchers.IO) {
            val encoded = encodeImage(bitmap)
            makeRequest(encoded)
        }
    }

    // ========== 私有方法 ==========
    private suspend fun makeRequest(encoded: String): AnalysisResult {
        // 实现
    }

    private fun encodeImage(bitmap: Bitmap): String {
        // 实现
    }

    // ========== 回调和监听器 ==========
    private fun onRequestSuccess(response: String) {
        // 处理成功响应
    }

    // ========== 清理方法 ==========
    fun release() {
        currentRequest?.cancel()
        httpClient.dispatcher.executorService.shutdown()
    }
}
```

---

## 3. 提交规范

### 3.1 Commit Message 格式

遵循 Conventional Commits 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### 类型 (Type)

- `feat`: 新功能
- `fix`: 修复问题
- `docs`: 文档更新
- `style`: 代码风格（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试
- `chore`: 依赖更新等

#### 示例

```
feat(camera): add manual focus control

Added support for manual focus adjustment with pinch gesture.
Implements TAP_TO_FOCUS mode and MANUAL_FOCUS mode.

- Pinch to adjust focus distance
- Double-tap to reset focus
- Visual feedback with focus ring

Closes #123
```

### 3.2 分支命名

```
main                    # 主分支
├── develop            # 开发分支
├── feature/camera-api # 功能分支
├── fix/network-error  # 修复分支
└── release/1.0.0      # 发布分支
```

---

## 4. 测试规范

### 4.1 测试类型和覆盖率

```
单元测试 (Unit Tests)
├── Camera Manager: 90%+
├── VLM Client: 85%+
├── Intent Dispatcher: 88%+
├── Voice Manager: 80%+
└── Utilities: 95%+

集成测试 (Integration Tests)
├── Camera to VLM: ✓
├── Voice to Intent: ✓
├── Intent to Action: ✓
└── End-to-End: ✓

性能测试 (Performance Tests)
├── Frame Rate: 30+ FPS
├── Latency: < 2s
├── Memory: < 200MB
└── Battery: < 5W
```

### 4.2 测试编写示例

```kotlin
// ✅ 好的实践 - 清晰的测试结构
class VLMNetworkClientTest {

    private lateinit var vlmClient: VLMNetworkClient

    @Before
    fun setUp() {
        vlmClient = VLMNetworkClient(mockContext, "test-key")
    }

    @Test
    fun analyzeImage_WithValidBitmap_ReturnsValidResult() {
        // Arrange
        val testBitmap = createTestBitmap()
        val expectedConfidence = 0.95f

        // Act
        val result = vlmClient.analyzeImage(testBitmap)

        // Assert
        assertNotNull(result)
        assertTrue(result.confidence >= expectedConfidence)
        assertFalse(result.objects.isEmpty())
    }

    @Test(expected = IOException::class)
    fun analyzeImage_WithNetworkError_ThrowsIOException() {
        // Test exception handling
    }
}

// ❌ 应避免 - 测试不清晰
@Test
fun test() {
    val client = VLMNetworkClient(context, "key")
    val bitmap = Bitmap.createBitmap(100, 100, ARGB_8888)
    val result = client.analyzeImage(bitmap)
    assert(result != null)
}
```

---

## 5. 文档规范

### 5.1 类文档

````kotlin
/**
 * 视觉语言模型网络客户端
 *
 * 管理与VLM服务的通信，包括图像上传、结果解析和错误处理。
 * 支持自动重试、请求超时和批量分析。
 *
 * 使用示例：
 * ```
 * val client = VLMNetworkClient(context, apiKey, "gpt-4-vision")
 * val result = client.analyzeImage(bitmap)
 * ```
 *
 * @param context Android上下文
 * @param apiKey API密钥
 * @param modelName 模型名称
 * @throws IOException 网络错误
 * @throws TimeoutException 请求超时
 *
 * @see AnalysisResult
 * @see DetectedObject
 */
class VLMNetworkClient(
    private val context: Context,
    private val apiKey: String,
    private val modelName: String = "gpt-4-vision"
)
````

### 5.2 函数文档

```kotlin
/**
 * 分析图像并返回识别结果
 *
 * 将图像上传到VLM服务进行分析，返回检测到的对象、
 * 图像描述和特征向量。该方法是挂起函数，应在协程中调用。
 *
 * @param bitmap 要分析的图像
 * @param prompt 分析提示词（可选）
 * @return 分析结果，包含检测对象、描述等信息
 * @throws IOException 网络连接失败
 * @throws TimeoutException 请求超时（>30s）
 * @throws JsonSyntaxException 响应解析失败
 *
 * @sample analyzeImageSample
 */
suspend fun analyzeImage(
    bitmap: Bitmap,
    prompt: String? = null
): AnalysisResult
```

### 5.3 参数和返回值文档

```kotlin
/**
 * @param bitmap
 *   图像Bitmap对象
 *   - 最小分辨率: 100x100
 *   - 最大分辨率: 4096x4096
 *   - 支持格式: ARGB_8888, RGB_565
 *
 * @return
 *   分析结果对象，包含：
 *   - objects: 检测到的对象列表（最多100个）
 *   - caption: 图像的自然语言描述
 *   - confidence: 整体置信度 (0.0-1.0)
 *   - embeddings: 512维特征向量
 */
```

---

## 6. 性能优化指南

### 6.1 图像处理优化

```kotlin
// ✅ 好的实践 - 及时释放资源
val bitmap = captureFrame()
try {
    val compressed = EncodingUtils.compressBitmap(bitmap, quality = 85)
    val result = analyzeImage(compressed)
    return result
} finally {
    bitmap.recycle()  // 释放原始图像
    compressed?.recycle()
}

// ✅ 好的实践 - 异步处理
lifecycleScope.launch(Dispatchers.IO) {
    val result = vlmClient.analyzeImage(bitmap)
    withContext(Dispatchers.Main) {
        updateUI(result)
    }
}

// ❌ 应避免 - 阻塞主线程
val result = vlmClient.analyzeImage(bitmap)  // 可能导致ANR
updateUI(result)
```

### 6.2 网络优化

```kotlin
// ✅ 好的实践 - 配置合理的超时和重试
vlmClient.setConfig(
    connectTimeout = 10000L,     // 10秒连接超时
    readTimeout = 30000L,        // 30秒读取超时
    writeTimeout = 10000L        // 10秒写入超时
)

vlmClient.setRetryPolicy(
    maxRetries = 3,
    backoffMultiplier = 2.0f,    // 指数退避
    initialDelayMs = 1000L
)

// ✅ 好的实践 - 使用连接池
private val httpClient = OkHttpClient.Builder()
    .connectionPool(ConnectionPool(maxIdleConnections = 5, keepAliveDuration = 60, SECONDS))
    .build()
```

### 6.3 内存优化

```kotlin
// ✅ 好的实践 - 及时清理集合
private val frameCache = LruCache<Int, Bitmap>(maxSize = 5)

fun cacheFrame(id: Int, bitmap: Bitmap) {
    frameCache.put(id, bitmap)
    // LruCache 自动移除最少使用的项
}

// ✅ 好的实践 - 避免内存泄漏
val lifecycleObserver = object : DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) {
        cameraManager.release()  // 清理资源
    }
}
lifecycle.addObserver(lifecycleObserver)
```

---

## 7. 安全实践

### 7.1 API 密钥管理

```kotlin
// ❌ 不要硬编码API密钥
const val API_KEY = "sk-1234567890"

// ✅ 使用本地配置或环境变量
object ApiConfig {
    val apiKey: String get() = readFromSecureStorage("api_key")
}

// ✅ 在 build.gradle 中使用
buildTypes {
    release {
        buildConfigField("String", "API_KEY", "\"${System.getenv("API_KEY")}\"")
    }
}
```

### 7.2 权限管理

```kotlin
// ✅ 好的实践 - 动态权限请求
val cameraPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        initCamera()
    } else {
        showPermissionDeniedMessage()
    }
}

// 请求权限
cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

// ✅ 好的实践 - 权限检查
if (ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.CAMERA
) == PackageManager.PERMISSION_GRANTED) {
    initCamera()
}
```

### 7.3 数据加密

```kotlin
// ✅ 好的实践 - 敏感数据加密
private fun encryptApiKey(apiKey: String): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey())
    return Base64.getEncoder().encodeToString(cipher.doFinal(apiKey.toByteArray()))
}

// ✅ 好的实践 - 使用 EncryptedSharedPreferences
val encryptedPreferences = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
encryptedPreferences.edit().putString("api_key", apiKey).apply()
```

---

## 8. 调试和日志

### 8.1 日志级别

```kotlin
// DEBUG - 开发调试信息
Log.d("CameraManager", "Frame captured: ${bitmap.width}x${bitmap.height}")

// INFO - 一般信息
Log.i("VLMClient", "Analysis started")

// WARN - 警告
Log.w("VoiceManager", "Audio input level is low")

// ERROR - 错误
Log.e("IntentDispatcher", "Failed to parse intent", exception)

// VERBOSE - 详细信息（仅开发）
Log.v("NetworkClient", "Request headers: $headers")
```

### 8.2 使用 Timber

```kotlin
// ✅ 推荐使用 Timber 库
Timber.d("Camera initialized")
Timber.e(exception, "Analysis failed")

// 在 Application 中初始化
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}
```

### 8.3 性能监控

```kotlin
// ✅ 使用 Profiler 测量执行时间
val startTime = System.currentTimeMillis()
val result = vlmClient.analyzeImage(bitmap)
val duration = System.currentTimeMillis() - startTime
Log.d("Performance", "Analysis took ${duration}ms")

// ✅ 使用 Android Profiler
// 在 Android Studio: Run > Profile 'app'
```

---

## 检查清单

完成每个功能前，请确保：

- [ ] 代码遵循编码规范
- [ ] 编写了单元测试（覆盖率 > 80%）
- [ ] 编写了集成测试
- [ ] 添加了必要的文档注释
- [ ] 没有 lint 错误或警告
- [ ] 性能测试通过（延迟 < 2s）
- [ ] 内存测试通过（< 200MB）
- [ ] 进行了 Code Review
- [ ] 更新了相关文档
- [ ] 提交信息遵循规范

---

## 常见问题解答

**Q: 测试覆盖率要求是多少?**
A: 核心模块应达到 80% 以上，关键路径应达到 95%+。

**Q: 如何处理第三方库的版本更新?**
A: 在 `build.gradle` 中使用版本管理，定期检查更新，运行完整的集成测试。

**Q: 性能目标无法达到怎么办?**
A: 使用 Android Profiler 进行分析，识别瓶颈，考虑缓存、批处理或异步处理等优化方案。

**Q: 如何调试网络问题?**
A: 使用 Interceptor 记录请求/响应，或使用 Charles 代理工具进行网络监控。
