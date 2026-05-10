package com.vsa.visualsemanticagent.utils

data class PromptPreset(
    val id: String,
    val label: String,
    val prompt: String
)

object PromptPresets {
    val defaults = listOf(
        PromptPreset(
            id = "lecture",
            label = "讲座入日程",
            prompt = "请识别这张校园讲座海报中的活动标题、日期、开始时间、地点和备注。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "exam",
            label = "考试安排",
            prompt = "请识别图片中的考试名称、考试日期、开始时间、考场地点和注意事项。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "activity",
            label = "社团活动",
            prompt = "请识别校园活动通知中的活动名称、时间、地点、报名或参与要求。如果适合提醒我参加，请返回 create_event。"
        ),
        PromptPreset(
            id = "career",
            label = "宣讲会提醒",
            prompt = "请识别就业宣讲会或招聘海报中的企业名称、宣讲时间、地点和备注。如果信息完整，请返回 create_event。"
        ),
        PromptPreset(
            id = "group_notice",
            label = "群通知转日程",
            prompt = "请从班级群、社团群或实验室群通知中提取日程信息，包括事项、时间、地点和备注。如果截图中有多个通知，请返回 clarification。"
        )
    )
}
