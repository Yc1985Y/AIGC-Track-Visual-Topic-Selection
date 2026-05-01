package com.vsa.visualsemanticagent

import com.vsa.visualsemanticagent.utils.JsonCleansingUtils
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
}
