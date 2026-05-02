package com.vsa.visualsemanticagent

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.utils.JsonCleansingUtils
import com.vsa.visualsemanticagent.utils.ResponseInterpreter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonCleansingUtilsTest {

    @Test
    fun extractJsonFromDirtyText_returnsInnerJson() {
        val dirty = "好的，结果如下：{\"action\":\"tts_feedback\",\"answer\":\"测试\"}"
        val json = JsonCleansingUtils.extractJsonFromDirtyText(dirty)
        assertEquals("{\"action\":\"tts_feedback\",\"answer\":\"测试\"}", json)
    }

    @Test
    fun removeMarkdownWrappers_removesCodeFence() {
        val wrapped = "```json\n{\"action\":\"unknown\"}\n```"
        val json = JsonCleansingUtils.removeMarkdownWrappers(wrapped)
        assertEquals("{\"action\":\"unknown\"}", json)
    }

    @Test
    fun removeMarkdownWrappers_removesPlainCodeFence() {
        val wrapped = "```\n{\"action\":\"unknown\"}\n```"
        val json = JsonCleansingUtils.removeMarkdownWrappers(wrapped)
        assertEquals("{\"action\":\"unknown\"}", json)
    }

    @Test
    fun extractJsonFromDirtyText_keepsBraces() {
        val dirty = "prefix {\"action\":\"navigate\",\"location\":\"北京\"} suffix"
        val json = JsonCleansingUtils.extractJsonFromDirtyText(dirty)
        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
    }

    @Test
    fun normalizeResponse_mapsUnknownActionToUnknown() {
        val raw = VLMResponse(
            action = "CREATE_MEETING",
            title = "  测试活动  ",
            answer = "  好的  ",
            phoneNumber = " 138-0013-8000 "
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals(ModelConstants.ACTION_UNKNOWN, normalized.action)
        assertEquals("测试活动", normalized.title)
        assertEquals("好的", normalized.answer)
        assertEquals("13800138000", normalized.phoneNumber)
    }

    @Test
    fun buildStatusMessage_prefersDispatchSummary() {
        val response = VLMResponse(
            action = ModelConstants.ACTION_NAVIGATE,
            location = "上海"
        )

        val message = ResponseInterpreter.buildStatusMessage(response, "正在打开地图导航。")

        assertEquals("正在打开地图导航。", message)
    }

    @Test
    fun buildStatusMessage_usesFallbackForUnknownAction() {
        val response = VLMResponse(action = ModelConstants.ACTION_UNKNOWN)

        val message = ResponseInterpreter.buildStatusMessage(response)

        assertEquals("暂时无法确定最合适的动作。", message)
    }

    @Test
    fun normalizeResponse_defaultsMissingActionToUnknown() {
        val raw = VLMResponse(
            action = null,
            description = "测试描述"
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals(ModelConstants.ACTION_UNKNOWN, normalized.action)
        assertEquals("测试描述", normalized.description)
    }

    @Test
    fun buildSpeechText_fallsBackToDescription() {
        val response = VLMResponse(
            action = ModelConstants.ACTION_TTS_FEEDBACK,
            description = "这里是一间教室"
        )

        val speech = ResponseInterpreter.buildSpeechText(response)

        assertEquals("这里是一间教室", speech)
    }

    @Test
    fun normalizeResponse_trimsLocationAndDescription() {
        val raw = VLMResponse(
            action = ModelConstants.ACTION_NAVIGATE,
            location = "  第一教学楼 A 座  ",
            description = "  活动在一层大厅  "
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals("第一教学楼 A 座", normalized.location)
        assertEquals("活动在一层大厅", normalized.description)
    }

    @Test
    fun buildStatusMessage_usesDescriptionForSmsFallback() {
        val response = VLMResponse(
            action = ModelConstants.ACTION_SEND_SMS,
            description = "给小王发一条提醒短信"
        )

        val message = ResponseInterpreter.buildStatusMessage(response)

        assertEquals("已准备短信内容。", message)
    }
}
