# 织时

面向高校场景的 `校园通知 -> 结构化时间线 -> 系统提醒 / 日历执行` Android 智能体原型。

**织时：将校园信息碎片整合为专属时间线的智能助手**

## 当前定位

织时不是传统的手填日历，也不是只做识别的 Demo，而是把校园海报、群通知、截图、网页公告和语音指令，收束为一条可确认、可提醒、可回看的个人时间线。

当前 App 已经稳定为三栏结构：

- `首页`：Today-First 焦点卡、意图输入舱、输入源预览、动态工作台、确认写入卡
- `时间线`：日 / 周 / 月聚合、时间轴列表、详情底部卡、提醒策略、导出能力
- `我的`：状态总览、数字福祉、快捷入口、智能体偏好、成就徽章

## 核心能力

- 多入口导入：拍照、相册、系统分享、粘贴文本、语音输入
- vivo AI 双阶段链路
  - 文本输入：直接调用 vivo `chat/completions`
  - 图片输入：先调用 vivo OCR，再把 OCR 结果送入 vivo LLM 做结构化理解
- 结构化输出：`create_event / navigate / clarification / tts_feedback`
- 安全执行：先建议、再确认，保留人在回路
- 本地提醒：已接入真实通知调度，支持提前一天、提前若干分钟提醒
- 时间线沉淀：已接入 DataStore，本地保存日程、提醒偏好和风险控制偏好
- 计划导出：支持 `PDF / JPG / PNG`

## vivo AI 接入

项目从根目录 `local.properties` 读取：

```properties
VLM_APP_ID=你的AppID
VLM_API_KEY=你的AppKEY
```

当前默认在线模型配置：

- `VLM_MODEL_NAME=Volc-DeepSeek-V3.2`
- `VLM_API_ENDPOINT=https://api-ai.vivo.com.cn/v1/chat/completions`
- `VLM_OCR_ENDPOINT=https://api-ai.vivo.com.cn/ocr/general_recognition`

## 构建

推荐使用本机 JDK 17：

```powershell
$env:JAVA_HOME='E:\AIGC\tools\jdk17\jdk-17.0.19+10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"

C:\Users\yc\.gradle\wrapper\dists\gradle-8.2-bin\bbg7u40eoinfdyxsxr3z4i7ta\gradle-8.2\bin\gradle.bat :app:assembleDebug --no-daemon
```

当前工程为了适配本机稳定工具链，已临时收敛为：

- `compileSdk 34`
- `targetSdk 34`
- AGP `8.2.1`

## 当前验证状态

- 已通过：`:app:compileDebugKotlin`
- 已通过：`:app:assembleDebug`
- 最新 APK：
  - `E:\AIGC\vivo\vivo\VisualSemanticAgent\app\build\outputs\apk\debug\app-debug.apk`
- 已安装到 Google Android Emulator
- 已完成冷启动前台验证，主 Activity 正常恢复

## 本轮新增优化

- 首页相机预览改为按需绑定，减少预览在切页和重组时的额外负担
- 首页动态工作台做减法，空态与有结果态更清晰，减少信息堆叠
- 时间线详情页改为“摘要头卡 + 关键字段”的展示结构，更适合答辩演示
- “我的”页合并重复说明块，保留状态、偏好和成果三类核心信息
- 清理未使用的调试截图逻辑，降低维护噪音
- 视觉表现继续升级为决赛展示版：
  - 启动页、首页和“我的”页接入弥散光背景
  - 品牌字与关键标题加入渐变排印
  - 置信度和统计数字改为更强的等宽数字表达
  - 输入舱加入轻微呼吸感
  - 加载层改为骨架化微光状态
  - 时间线空状态加入抽象线团式图形表达

## 注意事项

- `local.properties` 仅本地使用，不应提交仓库
- `adb file://...` 方式不等价于真实系统分享，正式演示请优先使用相册或支持分享的 App 发送 `content://` URI
