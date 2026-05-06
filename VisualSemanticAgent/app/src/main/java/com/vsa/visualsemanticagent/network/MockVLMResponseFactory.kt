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
                "门诊",
                "提醒"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.9,
                payload = VLMPayload(
                    title = "门诊复查提醒",
                    time = "2026-05-20T14:30:00",
                    location = "门诊楼三层影像科",
                    description = "请提前十分钟到达并携带检查单。"
                ),
                fallbackQuery = "我识别到了日程，但如果时间不对，请直接告诉我完整时间。",
                targetFound = true
            )

            containsAny(
                normalized,
                "navigate",
                "导航",
                "地图",
                "去这个地方",
                "去门诊"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                confidence = 0.86,
                payload = VLMPayload(
                    location = "门诊楼三层影像科",
                    description = "从当前位置前往门诊楼三层影像科。"
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
                    description = "您好，我已到医院门诊楼，正在前往影像科。"
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
                    answer = "我已经识别到这是一张医院通知，重点信息是时间、地点和注意事项。",
                    description = "可继续追问我，或者让我帮你创建提醒。"
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
