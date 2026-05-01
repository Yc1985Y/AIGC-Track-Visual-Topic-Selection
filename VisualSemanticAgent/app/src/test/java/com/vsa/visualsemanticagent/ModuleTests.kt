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
            answer = "  好的  "
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals(ModelConstants.ACTION_UNKNOWN, normalized.action)
        assertEquals("测试活动", normalized.title)
        assertEquals("好的", normalized.answer)
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
}
