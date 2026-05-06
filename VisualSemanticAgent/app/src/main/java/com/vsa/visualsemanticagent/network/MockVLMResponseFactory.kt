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
                "create_event",
                "日历",
                "活动",
                "海报",
                "讲座",
                "通知",
                "提醒"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.9,
                payload = VLMPayload(
                    title = "AI 创新讲座",
                    time = "2026-05-20T14:30:00",
                    location = "图书馆报告厅",
                    description = "建议提前十分钟到场，并携带校园卡签到。"
                ),
                fallbackQuery = "我识别到了一个活动，但如果时间不对，请直接告诉我完整时间。",
                targetFound = true
            )

            containsAny(
                normalized,
                "navigate",
                "导航",
                "地图",
                "去这个地方",
                "去会场",
                "去教学楼"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                confidence = 0.86,
                payload = VLMPayload(
                    location = "信息楼 A 座 201",
                    description = "从当前位置前往信息楼 A 座 201 教室。"
                ),
                fallbackQuery = "我识别到了一个可能的地点，请再对准地点信息或者直接说出楼名。",
                targetFound = true
            )

            containsAny(
                normalized,
                "send_sms",
                "短信",
                "通知联系人",
                "发消息"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_SEND_SMS,
                confidence = 0.81,
                payload = VLMPayload(
                    phoneNumber = "13800138000",
                    description = "老师您好，我已经到达信息楼，预计五分钟后进入会场。"
                ),
                fallbackQuery = "我整理出了短信草稿，但号码或内容还需要你再确认一次。",
                targetFound = true
            )

            containsAny(
                normalized,
                "clarify",
                "不确定",
                "模糊",
                "看不清"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CLARIFICATION,
                confidence = 0.45,
                payload = VLMPayload(
                    answer = "我看到了一张通知，但时间和地点还不够清晰。"
                ),
                fallbackQuery = "请把手机再靠近一点，或者告诉我是上午还是下午。",
                targetFound = true
            )

            else -> VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                confidence = 0.93,
                payload = VLMPayload(
                    answer = "我已经识别到这是一张校园通知，重点信息是活动时间、地点和参与要求。",
                    description = "你可以继续追问我，或者让我帮你创建提醒、开始导航。"
                ),
                fallbackQuery = "如果你希望我执行动作，可以继续说创建提醒或开始导航。",
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
