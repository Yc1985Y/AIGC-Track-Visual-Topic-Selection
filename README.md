# AIGC-Track-Visual-Topic-Selection

中国高校计算机大赛 AIGC 赛道项目仓库。当前实现重点是一个 Android 端“视觉语义执行代理”原型：把拍照、语音或文字输入，转成结构化理解结果，并触发日历、地图导航、TTS 等系统动作。

## 当前状态

- 当前阶段：MVP 原型补全中
- 当前主工程：`VisualSemanticAgent/`
- 当前优先场景：
  - 海报/活动信息识别 -> 打开日历
  - 地点识别 -> 打开地图导航
  - 场景描述/导视 -> TTS 语义反馈

## 仓库结构

- `方案.md`
  - 比赛方案与整体思路
- `进度说明.md`
  - 当前进展、待办和后续约定
- `VisualSemanticAgent/`
  - Android 原型工程
- `视觉语义执行代理方案设计.docx`
  - 方案备份文档

## Android 原型能力

`VisualSemanticAgent` 当前已经补上这些主链路能力：

- CameraX 预览与拍照
- 图片 Base64 编码
- 单次语音识别
- vivo 比赛多模态接口接入
- 模型返回 JSON 清洗与解析
- Android Intent 分发
- TTS 播报反馈
- 场景预设快捷入口
- 结果卡片回显
- 错误弹层与有限重试

这轮又补了一批更偏稳定性的收口项：

- Gradle / 依赖配置纠偏，降低 Android Studio Sync 失败风险
- 导航地点编码兼容，减少中文地点跳转失败
- 模型接口更细的错误映射，便于定位限流/权限/额度问题
- 针对不同 vivo 模型的思考参数兼容处理
- 更稳妥的协程取消处理，减少中断时误报错误

更详细的工程说明见：

- [VisualSemanticAgent/README.md](./VisualSemanticAgent/README.md)

## 当前模型接入

当前默认接入比赛可用的 vivo 云端接口：

- Endpoint: `https://api-ai.vivo.com.cn/v1/chat/completions`
- Model: `Volc-DeepSeek-V3.2`
- Auth: `Authorization: Bearer AppKey`

这样更适合当前比赛 demo 阶段，因为它直接支持图片理解，并且 Android 侧接入成本更低。

## 使用约定

- 后续继续本项目时，先读取 `进度说明.md`
- 代码主迭代目录是 `VisualSemanticAgent/`
- 当前优先目标不是继续铺大功能，而是尽快完成真机闭环验证

## 下一步

- 在 Android Studio 中先完成 Gradle Sync 与 Debug 构建
- 配置比赛 `AppKey`
- 验证“活动入日历”和“地点导航”两个主演示场景
- 根据真实接口表现继续补充容错与测试
