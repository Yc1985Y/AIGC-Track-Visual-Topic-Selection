# Visual Semantic Action Agent

> Recommended current design references: `进度说明.md`, `VISUAL_TO_TOOL_OS_DESIGN.md`, `REFERENCE_MATERIALS.md`, and `INITIAL_PRESENTATION_BRIEF.md`

一个面向比赛演示场景的 Android 视觉语义执行代理原型。  
当前版本已重构为“Visual-to-Tool OS / Agent Middleware”叙事，目标是把“拍照 / 语音 / 文字输入”转成“结构化语义理解 + 风控判定 + Android 系统动作执行”，重点支持海报入日历、地点去导航、短信草稿和语音播报等跨应用视觉任务。

## 当前状态

- 已具备主流程：相机预览、拍照采集、语音输入、云端多模态请求、结果解析、系统动作分发、TTS 播报。
- 已具备中间件执行链：`Extract -> Suggest -> Confirm -> Execute`
- `debug` 构建默认启用 `Mock` 模式，方便在没有正式模型额度时先验证界面和交互。
- 主界面已重构为更适合比赛展示的产品化布局：
  - 首屏英雄区
  - 视觉动作场景快捷入口
  - 主命令输入与发送区
  - 视觉输入区与结果面板
  - 演示图表与演示清单
  - 确认执行卡片

## 模型接口

当前项目对接 vivo 比赛接口：

- 接口地址：`https://api-ai.vivo.com.cn/v1/chat/completions`
- 默认模型：`Volc-DeepSeek-V3.2`
- 鉴权方式：`Authorization: Bearer AppKey`
- 协议形式：OpenAI 兼容 `chat/completions`

## Mock 模式

为了方便 Android Studio 本地验证，工程已内置 `Mock` 演示模式：

- `debug` 默认：`VLM_USE_MOCK = true`
- `release` 默认：`VLM_USE_MOCK = false`
- `Mock` 模式下不会访问真实 vivo 接口，也不依赖 `VLM_API_KEY`

可直接验证：

- 输入框与发送流程
- 快捷场景切换
- 结果卡片展示
- 日历 / 地图 / 短信 / TTS 链路
- 错误弹层和状态提示

## 本地构建

### 重要说明

在你当前这台 Windows 机器上，完整 `assembleDebug` 构建需要使用 **JDK 17**。  
如果直接用 Android Studio 自带的 **JBR 21**，会触发 `androidJdkImage / jlink / core-for-system-modules.jar` 相关失败。

当前已经验证通过的 JDK 路径：

`E:\AIGC\tools\jdk17\jdk-17.0.19+10`

另外，由于仓库目录本身包含中文路径，项目内已经在 `gradle.properties` 中启用：

`android.overridePathCheck=true`

这样可以避免 Windows 下 AGP 因非 ASCII 路径直接拦截构建。

### 命令行构建

在 PowerShell 中先切换到 JDK 17，再执行构建：

```powershell
$env:JAVA_HOME='E:\AIGC\tools\jdk17\jdk-17.0.19+10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

当前这条命令已经验证可成功完成构建。

### Android Studio 设置

建议在 Android Studio 中把 **Gradle JDK** 指向 JDK 17，而不是 Embedded JBR 21。

推荐值：

- Gradle JDK：`E:\AIGC\tools\jdk17\jdk-17.0.19+10`

如果只做界面调试和 Mock 验证，优先使用：

- 工程目录：`E:\AIGC\VisualSemanticAgent`
- 模拟器：`VSA_API34_GOOGLE`

## 主要文件

- 主入口：[MainActivity.kt](app/src/main/java/com/vsa/visualsemanticagent/MainActivity.kt)
- 主界面：[CameraScreen.kt](app/src/main/java/com/vsa/visualsemanticagent/ui/CameraScreen.kt)
- 相机管理：[CameraManager.kt](app/src/main/java/com/vsa/visualsemanticagent/camera/CameraManager.kt)
- 接口调用：[VLMNetworkClient.kt](app/src/main/java/com/vsa/visualsemanticagent/network/VLMNetworkClient.kt)
- 动作分发：[IntentDispatcher.kt](app/src/main/java/com/vsa/visualsemanticagent/intent/IntentDispatcher.kt)

## 当前支持的动作

模型输出中的 `action` 当前支持：

- `create_event`
- `navigate`
- `tts_feedback`
- `send_sms`
- `clarification`
- `unknown`

## 验证建议

优先按下面顺序验证：

1. 使用 JDK 17 完成 `assembleDebug`
2. 启动模拟器或真机
3. 先在 `Mock` 模式下验证首屏、发送、结果区和按钮交互
4. 再验证海报入日历、地点去导航两条主展示链路
5. 有正式接口额度后，再关闭 `Mock` 模式验证真实模型返回

## 后续建议

- 继续补齐真实设备端联调
- 为结果面板补更贴近比赛场景的真实图表示例
- 增加关键链路自动化测试与错误重试策略
