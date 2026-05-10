package com.vsa.visualsemanticagent.network

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMPayload
import com.vsa.visualsemanticagent.model.VLMResponse
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.delay

object MockVLMResponseFactory {

    suspend fun createResponse(userText: String): VLMResponse {
        delay(700)
        simulateFailureIfNeeded(userText)
        return buildResponse(userText)
    }

    fun buildResponse(userText: String): VLMResponse {
        val normalized = userText.trim().lowercase(Locale.ROOT)

        return when {
            containsAny(
                normalized,
                "缺时间",
                "时间不清楚",
                "模糊",
                "看不清"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CLARIFICATION,
                confidence = 0.54,
                payload = VLMPayload(
                    title = "就业宣讲会",
                    location = "大学生活动中心",
                    description = "海报中开始时间不清楚。",
                    answer = "我识别到了宣讲会和地点，但开始时间不够清楚。"
                ),
                fallbackQuery = "请补充这个活动的开始时间。",
                targetFound = true
            )

            containsAny(
                normalized,
                "exam",
                "考试",
                "考场",
                "期末",
                "安排"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.88,
                payload = VLMPayload(
                    title = "数据结构期末考试",
                    time = "2026-06-18T09:00:00",
                    location = "A 教 203",
                    description = "来源：考试安排通知；请携带学生证。"
                ),
                fallbackQuery = "",
                targetFound = true
            )

            containsAny(
                normalized,
                "career",
                "宣讲",
                "招聘",
                "就业",
                "企业"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.87,
                payload = VLMPayload(
                    title = "vivo 校园招聘宣讲会",
                    time = "2026-05-25T19:00:00",
                    location = "大学生活动中心报告厅",
                    description = "来源：校园宣讲海报；建议提前报名并携带简历。"
                ),
                fallbackQuery = "",
                targetFound = true
            )

            containsAny(
                normalized,
                "群",
                "截图",
                "班级",
                "实验室群",
                "社团群"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.86,
                payload = VLMPayload(
                    title = "班级主题班会",
                    time = "2026-05-15T15:00:00",
                    location = "教学楼 B302",
                    description = "来源：班级群通知截图；请全体同学准时参加。"
                ),
                fallbackQuery = "",
                targetFound = true
            )

            containsAny(
                normalized,
                "navigate",
                "导航",
                "怎么去",
                "去会场",
                "地点"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                confidence = 0.86,
                payload = VLMPayload(
                    location = "图书馆报告厅",
                    description = "从当前位置前往图书馆报告厅。"
                ),
                fallbackQuery = "我识别到了会场地点，请确认是否开始导航。",
                targetFound = true
            )

            containsAny(
                normalized,
                "lecture",
                "讲座",
                "活动",
                "海报",
                "通知",
                "提醒",
                "日程",
                "日历"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.91,
                payload = VLMPayload(
                    title = "人工智能前沿讲座",
                    time = "2026-05-12T19:00:00",
                    location = "图书馆报告厅",
                    description = "来源：校园讲座海报；主办方：计算机学院；建议提前 30 分钟提醒。"
                ),
                fallbackQuery = "",
                targetFound = true
            )

            else -> VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                confidence = 0.78,
                payload = VLMPayload(
                    answer = "我可以帮你识别校园海报、群通知、考试安排和宣讲会，并生成可确认的日程提醒。",
                    description = "你可以拍照、导入截图，或直接粘贴校园通知文本。"
                ),
                fallbackQuery = "你可以说：帮我把这个讲座加入日程。",
                targetFound = true
            )
        }
    }

    private fun simulateFailureIfNeeded(userText: String) {
        val normalized = userText.trim().lowercase(Locale.ROOT)
        if (containsAny(normalized, "mock_error", "模拟错误")) {
            throw VLMNetworkException(IOException("Mock mode forced error"))
        }
    }

    private fun containsAny(value: String, vararg keywords: String): Boolean {
        return keywords.any { value.contains(it) }
    }
}
