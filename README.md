# 《织时》AIGC 参赛工程仓库

当前仓库的主工程为：

- [VisualSemanticAgent](D:/桌面/本科/课题all/中国高校计算机大赛AIGC赛道/VisualSemanticAgent)

作品名称：

- `织时：将校园信息碎片整合为专属时间线的智能助手`

手机桌面名称：

- `织时`

## 当前状态

截至 `2026-05-10`，当前工程已经完成以下关键工作：

- 首页、时间线、我的三栏主界面已稳定成型
- 旧的演示模式主链路已移除
- vivo 真实大模型与 OCR 已接入 Android 工程
- 文本分享与图片分享两条主链路已在模拟器中完成实际验证
- 结果会进入现有的风险判断、确认卡与时间线沉淀流程

也就是说，这个仓库当前保存的不是单纯 UI 原型，而是一份已经接通真实 AI 能力链路的《织时》完整 Android 工程。

## 推荐阅读顺序

建议后续接手时按这个顺序阅读：

1. [进度说明.md](D:/桌面/本科/课题all/中国高校计算机大赛AIGC赛道/进度说明.md)
2. [VisualSemanticAgent/配置说明.md](D:/桌面/本科/课题all/中国高校计算机大赛AIGC赛道/VisualSemanticAgent/配置说明.md)
3. [VisualSemanticAgent/作品进度.md](D:/桌面/本科/课题all/中国高校计算机大赛AIGC赛道/VisualSemanticAgent/作品进度.md)
4. [VisualSemanticAgent/README.md](D:/桌面/本科/课题all/中国高校计算机大赛AIGC赛道/VisualSemanticAgent/README.md)

## 当前重点能力

- 多模态导入：拍照、相册、系统分享、剪贴板、文本、语音
- AI 结构化理解：标题、时间、地点、动作、置信度
- 风险控制：人在回路确认、高风险拦截、低置信度澄清
- 时间线沉淀：确认后的安排进入时间线并可挂载提醒

## 说明

- `VisualSemanticAgent/local.properties` 为本地敏感配置文件，不纳入远程版本控制。
- 仓库中存在历史参考材料、答辩文档和设计资源，但继续开发时应以 `VisualSemanticAgent/` 当前工程为准。
