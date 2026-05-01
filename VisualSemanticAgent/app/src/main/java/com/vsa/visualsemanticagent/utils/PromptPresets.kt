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
            label = "活动入日历",
            prompt = "请识别图片中的活动主题、时间和地点，并在适合时返回 create_event。"
        ),
        PromptPreset(
            id = "navigate",
            label = "地点去导航",
            prompt = "请识别图片中的地点信息。如果用户适合前往该地点，请返回 navigate。"
        ),
        PromptPreset(
            id = "describe",
            label = "场景描述",
            prompt = "请详细描述当前画面中的关键信息，并返回 tts_feedback。"
        ),
        PromptPreset(
            id = "find",
            label = "寻物导视",
            prompt = "请根据画面帮助我定位目标物体，并用清晰的空间描述返回 tts_feedback。"
        )
    )
}
