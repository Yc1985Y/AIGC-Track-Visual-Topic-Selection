package com.vsa.visualsemanticagent.network

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.Locale

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
                "会议",
                "答辩",
                "比赛"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                title = "中国高校计算机大赛 AIGC 赛道项目答辩",
                time = "2026-05-20 14:30",
                location = "信息楼 A201",
                description = "Mock 模式示例：用于验证日历拉起、时间解析和结果卡片展示。"
            )

            containsAny(
                normalized,
                "navigate",
                "导航",
                "地点",
                "地图",
                "前往",
                "怎么走",
                "去这个地方"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                location = "深圳市南山区科技园科苑路 15 号",
                description = "Mock 模式示例：用于验证地图跳转与中文地点兼容。"
            )

            containsAny(
                normalized,
                "send_sms",
                "短信",
                "联系",
                "通知",
                "发消息"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_SEND_SMS,
                phoneNumber = "+86 138-0013-8000",
                answer = "已为联系人准备提醒短信。",
                description = "您好，答辩将在今天下午两点半于信息楼 A201 准时开始。"
            )

            containsAny(
                normalized,
                "find",
                "tts_feedback",
                "寻物",
                "导视",
                "寻找",
                "在哪",
                "描述"
            ) -> VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                targetFound = true,
                answer = "Mock 模式：目标位于画面右侧桌面附近，靠近显示器下方。",
                description = "这是一个室内办公或学习场景，桌面上摆放着常见物品。"
            )

            else -> VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                answer = "Mock 模式：当前主链路已接通，你可以继续测试日历、导航或语音播报流程。",
                description = "如果想测试错误弹层，可在输入框中键入 mock_error 后再点击拍照执行。"
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
