# 织时系统架构设计

## 1. 系统概述

### 1.1 核心理念

织时是一个面向校园通知场景的多模态 Android 应用，能够通过拍照、相册、分享和文本导入识别校园通知，并在用户确认后生成日程提醒。

### 1.2 系统特点

- **多来源导入**：整合拍照、相册、分享、粘贴四类入口
- **结构化解析**：提取标题、时间、地点、备注
- **确认执行**：在系统级动作执行前保留人工确认
- **校园场景聚焦**：优先服务讲座、考试、宣讲和群通知

---

## 2. 架构层次

```
┌─────────────────────────────────────────────────────┐
│         用户交互层 (UI/UX Layer)                    │
│  ┌──────────────┬──────────────┬──────────────┐    │
│  │ 相机界面      │ 动画加载      │ 结果显示      │    │
│  └──────────────┴──────────────┴──────────────┘    │
└─────────────────────────────────────────────────────┘
                        ↑ ↓
┌─────────────────────────────────────────────────────┐
│      核心业务逻辑层 (Business Logic Layer)          │
│  ┌─────────────────────────────────────────────┐   │
│  │        意图调度器 (Intent Dispatcher)        │   │
│  │  协调各模块完成用户需求                      │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
         ↓           ↓           ↓           ↓
┌──────────────┬──────────────┬──────────────┬────────────┐
│  感知模块    │  理解模块    │  推理模块    │  执行模块  │
├──────────────┼──────────────┼──────────────┼────────────┤
│  • 摄像头    │  • VLM网络   │  • 意图推理  │  • 动作执行 │
│  • 数据采集  │  • 特征抽取  │  • 计划生成  │  • 结果反馈 │
│  • 预处理    │  • 语义编码  │  • 优化      │  • 交互管理 │
└──────────────┴──────────────┴──────────────┴────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│         基础服务层 (Infrastructure Layer)           │
│  ┌──────────────┬──────────────┬──────────────┐    │
│  │ 网络通信      │ 本地存储      │ 多模态处理    │    │
│  └──────────────┴──────────────┴──────────────┘    │
└─────────────────────────────────────────────────────┘
```

---

## 3. 核心模块详解

### 3.1 感知模块 (Perception Module)

**文件位置**: `camera/`

**主要功能**:

- 实时摄像头控制和帧数据采集
- 相机权限管理
- 图像缓冲和优化

**关键类**:

```
CameraManager
├── 初始化相机
├── 预览管理
├── 帧捕获
└── 释放资源
```

**关键API**:

```kotlin
fun initCamera(context: Context, lifecycleOwner: LifecycleOwner): Flow<Bitmap>
fun captureFrame(): Bitmap?
fun releaseCamera()
fun toggleFlash(enable: Boolean)
```

---

### 3.2 理解模块 (Understanding Module)

**文件位置**: `network/`, `model/`

**主要功能**:

- 与视觉语言模型（VLM）通信
- 图像编码和特征提取
- 语义理解和标记

**关键类**:

```
VLMNetworkClient
├── 请求管理
├── 响应解析
└── 错误处理

VLMModels
├── 模型配置
├── API端点管理
└── 参数优化
```

**工作流程**:

```
图像帧 → 编码 → VLM请求 → 语义标签 → 特征向量
```

**关键API**:

```kotlin
suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult
suspend fun detectObjects(bitmap: Bitmap): List<DetectedObject>
suspend fun generateCaption(bitmap: Bitmap): String
```

---

### 3.3 意图分派模块 (Intent Dispatcher)

**文件位置**: `intent/`

**主要功能**:

- 解析用户意图
- 分配任务给相应模块
- 协调模块间通信
- 管理执行流程

**关键类**:

```
IntentDispatcher
├── 意图识别
├── 任务分解
├── 流程编排
└── 状态管理
```

**意图类型**:

1. **查询意图** - 分析、识别、描述
2. **控制意图** - 执行动作、调整参数
3. **交互意图** - 对话、反馈、学习

> 说明：以下 `parseIntent/executeIntent/cancelIntent` 为较早阶段的架构草案接口。
> 当前 Android 原型实际实现并未暴露该组 API，当前以 `VLMResponse.action` + `IntentDispatcher.dispatchIntent()` 为主。

**关键API**:

```kotlin
fun parseIntent(input: String, context: AnalysisResult): Intent
fun executeIntent(intent: Intent): Flow<ExecutionResult>
fun cancelIntent(intentId: String)
```

---

### 3.4 语音模块 (Voice Module)

**文件位置**: `voice/`, `tts/`

**子模块**:

#### 3.4.1 语音识别 (Voice Recognition)

```
VoiceRecognitionManager
├── 录音管理
├── 语音-文本转换
├── 结果处理
└── 错误恢复
```

#### 3.4.2 文本转语音 (Text-to-Speech)

```
TextToSpeechManager
├── 文本处理
├── 音频合成
├── 播放控制
└── 音质优化
```

**关键API**:

```kotlin
// 语音识别
suspend fun startListening(): String
fun stopListening()

// 文本转语音
fun speak(text: String, language: String = "zh-CN")
fun stop()
```

---

### 3.5 用户界面模块 (UI Module)

**文件位置**: `ui/`

**关键界面**:

#### 3.5.1 摄像头界面 (CameraScreen)

- 实时视频预览
- 帧捕获控制
- 闪光灯切换
- 边界指示器

#### 3.5.2 加载动画 (LoadingOverlay)

- 处理进度显示
- 动画反馈
- 取消操作按钮
- 状态提示

**关键API**:

```kotlin
@Composable
fun CameraScreen(onCapture: (Bitmap) -> Unit)

@Composable
fun LoadingOverlay(progress: Float, message: String)
```

---

### 3.6 工具模块 (Utilities Module)

**文件位置**: `utils/`

**功能**:

- 图像编码/解码
- 数据格式转换
- 性能监控
- 日志管理

**关键类**:

```
EncodingUtils
├── 图像编码
├── Base64处理
├── 数据压缩
└── 格式转换
```

---

## 4. 数据流设计

### 4.1 单次任务执行流程

```
用户语音/文本输入
    ↓
[语音识别/文本处理]
    ↓
[摄像头捕获]
    ↓
[图像预处理和编码]
    ↓
[VLM分析]
    ↓
[意图推理]
    ↓
[任务执行规划]
    ↓
[执行动作/反馈]
    ↓
[语音/文本/视觉输出]
```

### 4.2 数据结构

**核心数据类**:

```kotlin
// 图像分析结果
data class AnalysisResult(
    val objects: List<DetectedObject>,
    val caption: String,
    val embeddings: FloatArray,
    val confidence: Float,
    val timestamp: Long
)

// 检测到的对象
data class DetectedObject(
    val id: String,
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox,
    val attributes: Map<String, String>
)

// 用户意图
data class Intent(
    val id: String,
    val type: String,  // "query", "control", "interact"
    val action: String,
    val parameters: Map<String, Any>,
    val confidence: Float
)

// 执行结果
data class ExecutionResult(
    val success: Boolean,
    val output: String,
    val data: Any? = null,
    val error: Exception? = null,
    val executionTime: Long
)
```

---

## 5. 关键设计模式

### 5.1 观察者模式

- UI订阅模块状态变化
- 实时更新反馈

### 5.2 命令模式

- 意图作为命令对象
- 支持撤销/重做

### 5.3 工厂模式

- VLM客户端创建
- 不同模型适配

### 5.4 责任链模式

- 意图分发处理
- 错误恢复流程

---

## 6. 工作流程详解

### 6.1 初始化流程

```
应用启动 (MainActivity)
    ↓
权限检查（摄像头、麦克风、存储）
    ↓
初始化相机管理器
    ↓
初始化VLM网络客户端
    ↓
初始化语音管理器
    ↓
UI准备就绪，等待用户交互
```

### 6.2 实时执行循环

```
1. [监听] - 等待用户语音/点击
2. [捕获] - 获取当前帧和用户输入
3. [处理] - 编码图像，转换语音
4. [推理] - VLM分析，意图识别
5. [决策] - 生成执行计划
6. [执行] - 执行必要动作
7. [反馈] - 返回结果到用户
8. 返回步骤1
```

---

## 7. 集成技术栈

| 层级     | 技术              | 版本          |
| -------- | ----------------- | ------------- |
| 语言     | Kotlin            | 1.9.0+        |
| UI框架   | Jetpack Compose   | 1.6.1         |
| 相机     | CameraX           | 1.3.1         |
| 网络     | OkHttp            | 4.11.0        |
| JSON     | Moshi/GSON        | 1.15.0/2.10.1 |
| 异步     | Kotlin Coroutines | 1.7.0+        |
| 数据库   | Room              | 2.5.2+        |
| 依赖注入 | Hilt              | 2.47+         |

---

## 8. 扩展性设计

### 8.1 模型替换

- 支持多种VLM（GPT-4V、Claude Vision等）
- 通过配置切换模型

### 8.2 功能扩展

- 添加新的意图类型
- 集成新的传感器（IMU、GPS等）
- 扩展执行动作库

### 8.3 性能优化

- 图像压缩算法选择
- 批处理请求
- 本地缓存策略

---

## 9. 安全性考虑

### 9.1 数据保护

- 本地数据加密
- API密钥管理
- 敏感信息脱敏

### 9.2 权限管理

- 最小权限原则
- 动态权限申请
- 用户隐私保护

### 9.3 通信安全

- HTTPS加密传输
- 请求签名验证
- 速率限制

---

## 10. 错误处理和恢复

### 10.1 错误分类

- **网络错误** - 重试、离线模式
- **权限错误** - 请求授权、降级功能
- **模型错误** - 重新请求、使用备用模型
- **UI错误** - 友好提示、日志记录

### 10.2 恢复策略

```
检测错误 → 分类 → 选择恢复策略 → 重试/降级/降级 → 通知用户
```

---

## 11. 性能目标

- **首帧延迟**: < 500ms
- **推理延迟**: < 2s
- **内存占用**: < 200MB
- **功耗**: 优化模式 < 5W
- **帧率**: 30 FPS @ 720p

---

## 12. 开发检查清单

- [ ] 各模块单元测试覆盖率 > 80%
- [ ] 集成测试通过率 100%
- [ ] 代码静态分析无高危问题
- [ ] 性能基准测试通过
- [ ] 权限测试完整
- [ ] 文档更新完整
- [ ] Code Review通过
- [ ] 用户验收测试通过
