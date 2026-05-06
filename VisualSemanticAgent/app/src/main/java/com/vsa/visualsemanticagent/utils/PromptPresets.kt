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
            label = "海报入日历",
            prompt = "请识别图片中的活动标题、时间和地点，并在适合时返回 create_event。"
        ),
        PromptPreset(
            id = "navigate",
            label = "地点去导航",
            prompt = "请识别图片中的楼栋、教室、会场或地点信息。如果适合前往该地点，请返回 navigate。"
        ),
        PromptPreset(
            id = "describe",
            label = "长文本播报",
            prompt = "请提炼当前画面中的关键信息，适合语音播报，并返回 tts_feedback。"
        ),
        PromptPreset(
            id = "find",
            label = "短信草稿",
            prompt = "请识别当前画面中的联系电话和沟通要点，并在适合时返回 send_sms。"
        )
    )
}
