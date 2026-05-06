package com.vsa.visualsemanticagent.utils

data class PromptPreset(
    val id: String,
    val label: String,
    val prompt: String
)

object PromptPresets {
    val defaults = listOf(
        PromptPreset(
            id = "event",
            label = "就诊单建提醒",
            prompt = "请识别图片中的就诊事项、时间和地点，并在适合时返回 create_event。"
        ),
        PromptPreset(
            id = "navigate",
            label = "科室去导航",
            prompt = "请识别图片中的科室、楼栋或医院地点信息。如果用户适合前往该地点，请返回 navigate。"
        ),
        PromptPreset(
            id = "describe",
            label = "通知读给我听",
            prompt = "请提炼当前画面中的关键信息，适合语音播报给视障用户，并返回 tts_feedback。"
        ),
        PromptPreset(
            id = "find",
            label = "大厅导视辅助",
            prompt = "请根据画面帮助我定位服务台、科室牌或目标物体，并用清晰的空间描述返回 tts_feedback。"
        )
    )
}
