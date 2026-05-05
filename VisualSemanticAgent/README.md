# Visual Semantic Action Agent

基于 Android 的视觉语义执行代理原型。项目目标是把“拍照/语音/文字输入”转成“结构化语义理解 + Android 系统动作执行”，优先服务比赛演示场景，例如活动海报入日历、地点识别后导航、复杂物理界面导视和 TTS 反馈。

## 当前状态

- 当前阶段：MVP 原型补全中
- 已打通主链路：
  - CameraX 预览与拍照
  - 图片 Base64 编码
  - 语音单次识别
  - 云端多模态请求
  - JSON 清洗与解析
  - Intent 分发
  - TTS 反馈
- 当前优先验证场景：
  - 海报/活动信息 -> 日历
  - 地点识别 -> 地图导航

## 当前模型接入

项目现已默认接入比赛可用的 vivo 大模型接口，而不是本地 BlueLM-7B 部署方案。

- 接口地址：`https://api-ai.vivo.com.cn/v1/chat/completions`
- 默认模型：`Volc-DeepSeek-V3.2`
- 鉴权方式：`Authorization: Bearer AppKey`
- 请求协议：OpenAI 兼容 `chat/completions`
- 图像输入：`messages[].content` 中混合 `text` 与 `image_url`

这样更适合当前项目，因为它直接支持：

- 云端图片理解
- OpenAI 风格消息格式
- Android 端低成本接入
- 比赛演示所需的快速闭环

相比之下，BlueLM-7B 更适合作为后续离线化或自部署扩展，不适合当前先把 demo 跑通的阶段。

## Mock 模式

为了方便 Android Studio 本地验证，工程当前已内置 `Mock` 演示模式：

- `debug` 构建默认开启 `VLM_USE_MOCK = true`
- `release` 构建默认关闭 `VLM_USE_MOCK = false`
- Mock 模式下不会访问真实 vivo 接口，也不依赖 `VLM_API_KEY`

Mock 模式可直接验证：

- CameraX 拍照链路
- 加载态与结果卡片
- 日历创建 Intent
- 地图导航 Intent
- TTS 播报
- 错误弹层与重试

可通过输入文字快速触发不同动作：

- `活动` / `日历` / `讲座`：返回 `create_event`
- `导航` / `地点` / `地图`：返回 `navigate`
- `短信` / `通知`：返回 `send_sms`
- 其他描述类输入：返回 `tts_feedback`
- `mock_error`：主动模拟错误，用于验证错误弹层

### 已验证的本地调试路径

当前这套工程在 Windows + Android Studio 环境下，已验证通过的本地调试方式如下：

- 活跃构建目录：`E:\AIGC\VisualSemanticAgent`
- 推荐模拟器：`VSA_API34_GOOGLE`
- 对应 ADB 设备名可能显示为：`emulator-5554` 或 `emulator-5556`
- 命令行构建推荐使用 JDK 17：`E:\AIGC\tools\jdk17\jdk-17.0.19+10`

如果直接在命令行运行 `gradlew`，请先临时切换到 JDK 17：

```powershell
$env:JAVA_HOME='E:\AIGC\tools\jdk17\jdk-17.0.19+10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:assembleDebug
```

说明：

- Android Studio 自带的 JBR 21 在当前机器上会触发 `androidJdkImage` / `jlink` 相关构建错误
- 使用上面的 JDK 17 可以稳定完成 `assembleDebug`
- 模拟器验证时，不建议再使用早前的 `VSA_API34_ATD`

### Mock 首页验收方式

这次已经确认，单看桌面窗口很容易把 mock 首页误判成“白屏”，因此后续建议统一用下面两种方式验收：

1. 用 `adb shell screencap` 抓设备内真实截图。
2. 对照应用自动写出的调试快照：
   - `files/debug_snapshots/mock-home.png`
   - `files/debug_snapshots/mock-result.png`

当前 mock 首屏已经调整为更明显的“欢迎页 + 使用步骤 + 主能力区”布局，避免因为上半部分留白被误判为空白页。

## 项目结构

```text
VisualSemanticAgent/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── androidTest/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/vsa/visualsemanticagent/
│       │   │   ├── camera/
│       │   │   ├── intent/
│       │   │   ├── model/
│       │   │   ├── network/
│       │   │   ├── tts/
│       │   │   ├── ui/
│       │   │   ├── utils/
│       │   │   ├── voice/
│       │   │   └── MainActivity.kt
│       │   └── res/
│       └── test/
├── build.gradle
├── settings.gradle
├── QUICKSTART.md
├── ARCHITECTURE.md
├── API_GUIDE.md
└── DEVELOPMENT_GUIDE.md
```

## 核心链路

```text
用户拍照/输入文字或语音
    ->
CameraX 截图
    ->
Base64 编码
    ->
vivo 多模态模型接口
    ->
严格 JSON 输出
    ->
VLMResponse 解析
    ->
Intent / TTS 执行
```

## 关键模块

- `MainActivity.kt`
  - 串联 UI、拍照、语音、网络、Intent 和 TTS
- `camera/CameraManager.kt`
  - CameraX 绑定、拍照、图像转换
- `network/VLMNetworkClient.kt`
  - 对接 vivo `chat/completions`
  - 自动添加 `request_id`
  - 构建多模态请求
  - 清洗并解析模型输出
- `intent/IntentDispatcher.kt`
  - 根据 `action` 调起日历、地图、短信等系统能力
- `voice/VoiceRecognitionManager.kt`
  - 使用原生 `SpeechRecognizer`
- `tts/TextToSpeechManager.kt`
  - 使用原生 `TextToSpeech`

## 配置方式

在 `app/build.gradle` 的 `defaultConfig` 中填写比赛提供的 AppKey：

```gradle
buildConfigField "String", "VLM_API_KEY", "\"你的AppKey\""
buildConfigField "String", "VLM_MODEL_NAME", "\"Volc-DeepSeek-V3.2\""
buildConfigField "String", "VLM_API_ENDPOINT", "\"https://api-ai.vivo.com.cn/v1/chat/completions\""
```

当前默认参数策略：

- `stream = false`
- `max_tokens = 2048`
- `temperature = 0.2`
- `reasoning_effort = "minimal"`
- `thinking.type = "disabled"`

这套参数更适合移动端 demo：优先保证速度、稳定性和结构化输出。

## 当前支持的 action

模型输出 JSON 中的 `action` 目前支持：

- `create_event`
- `navigate`
- `tts_feedback`
- `send_sms`
- `unknown`

对应数据模型位于：

- `app/src/main/java/com/vsa/visualsemanticagent/model/VLMModels.kt`

## 运行建议

当前最重要的不是继续扩功能，而是先做真实验证：

1. 在 Android Studio 打开工程，优先使用 `E:\AIGC\VisualSemanticAgent` 作为构建副本。
2. 确认 Gradle JDK 指向 JDK 17，而不是失效的 `JAVA_HOME` 或不兼容的 JBR 21。
3. 如果只是本地演示，可直接使用默认 `debug` Mock 模式。
4. 启动 `VSA_API34_GOOGLE` 模拟器，或连接真机。
5. 如果要验证真实接口，再填入 `VLM_API_KEY` 并将 `VLM_USE_MOCK` 关闭。
6. 先验证两个主演示场景：
   - 海报识别并拉起日历
   - 地点识别并拉起地图

## 当前已知限制

- 还没有完成 Android Studio 下的真实编译验证。
- 当前测试覆盖仍偏最小化，主要保证主链路代码先成型。
- `response_format` 没有继续强绑定在请求体中，当前主要依赖 prompt 严格约束 JSON 输出。
- README、设计文档和项目说明仍可能存在部分“成熟度高于实现度”的旧表述，后续还要继续统一。

## 本轮补充

本轮重点没有继续扩大功能面，而是先补了几类更影响真机验证效率的稳定性问题：

- 修正 Gradle 仓库声明冲突与 `ui-graphics` 版本缺失，降低 Android Studio Sync 风险
- 移除当前未实际使用的 Moshi / `kapt` 依赖，减少构建噪音
- 增强导航地点 URL 编码兼容，降低中文地点跳转失败风险
- 增强模型接口错误映射，便于区分限流、权限不足、当日额度耗尽
- 根据模型名兼容 `thinking.type` / `enable_thinking` 参数，减少不同 vivo 模型切换时的请求风险
- 在协程取消场景下避免误弹错误提示，减少回前台、销毁页面时的噪音
- 修正覆盖层拦截方式，避免错误弹层把自身“关闭/重试”按钮也一起拦截掉
- 增强短信号码清洗与 `smsto:` 编码，提升 `+86`、空格、短横线号码的兼容性
- 补上 TTS 初始化失败后的兜底策略，避免待播报内容持续堆积
- 收紧 JSON 脏文本提取逻辑，降低尾随说明文本干扰结构化解析的风险
- 重做 Mock 首屏视觉层次，增加欢迎信息、体验路径和能力提示，减少“看起来像白屏”的误判
- 确认 `adb` 设备内截图和应用内部调试快照都能正确显示新的 Mock 首屏

## 后续建议

- 优先完成真机闭环验证
- 先在 Android Studio 完成 Gradle Sync 与 Debug 构建
- 给网络异常、限流和 JSON 解析失败增加重试与降级策略
- 增加更真实的单元测试
- 收敛文档表述，让方案、README、代码状态一致

## 参考

- vivo 比赛模型接口：`https://api-ai.vivo.com.cn/v1/chat/completions`
- Android CameraX
- Android SpeechRecognizer
- Android TextToSpeech
- Android Intent / Calendar / Maps
