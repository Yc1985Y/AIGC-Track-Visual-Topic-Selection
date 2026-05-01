package com.vsa.visualsemanticagent.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.utils.JsonCleansingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit

class VLMNetworkClient(
    private val apiKey: String,
    private val modelName: String,
    private val apiEndpoint: String
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun sendMultimodalRequest(
        base64Image: String,
        userText: String
    ): VLMResponse = withContext(Dispatchers.IO) {
        val requestBody = buildRequestPayload(base64Image, userText)
        val request = Request.Builder()
            .url(apiEndpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("API request failed with code: ${response.code}")
        }

        val responseBody = response.body?.string().orEmpty()
        parseOpenAIResponse(responseBody)
    }

    private fun buildRequestPayload(
        base64Image: String,
        userText: String
    ) = gson.toJson(
        mapOf(
            "model" to modelName,
            "temperature" to 0.2,
            "response_format" to mapOf("type" to "json_object"),
            "messages" to listOf(
                mapOf("role" to "system", "content" to buildSystemPrompt()),
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf(
                            "type" to "text",
                            "text" to "用户指令：$userText"
                        ),
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf(
                                "url" to "data:image/jpeg;base64,$base64Image"
                            )
                        )
                    )
                )
            )
        )
    ).toRequestBody("application/json".toMediaType())

    private fun buildSystemPrompt(): String {
        return """
你是视觉语义执行代理的结构化决策引擎。
你必须严格输出一个合法 JSON 对象，不能输出任何解释、Markdown、前后缀。

可选 action 只有：
- create_event：从海报、通知、名片等图像中提取活动信息并建议写入日历
- navigate：从图像或文字中提取地点并发起导航
- tts_feedback：返回描述、问答、导视或寻物反馈
- send_sms：当用户明确要求发短信时返回
- unknown：无法判断时返回

输出 JSON Schema：
{
  "action": "create_event|navigate|tts_feedback|send_sms|unknown",
  "title": "string",
  "time": "string",
  "location": "string",
  "answer": "string",
  "target_found": true,
  "description": "string",
  "phone_number": "string"
}

要求：
1. 如果信息缺失，不要臆造，字段可省略。
2. 如果用户是描述/问答/导视类需求，优先返回 tts_feedback。
3. 如果检测到活动主题、时间、地点且用户有安排意图，返回 create_event。
4. 如果检测到明确地点且用户有前往意图，返回 navigate。
        """.trimIndent()
    }

    private fun parseOpenAIResponse(responseBody: String): VLMResponse {
        try {
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val choices = json.getAsJsonArray("choices")
            val first = choices?.firstOrNull()?.asJsonObject
                ?: throw IllegalStateException("Missing choices")
            val message = first.getAsJsonObject("message")
            val content = message.get("content")?.asString.orEmpty()
            val cleanedJson = try {
                JsonCleansingUtils.extractJsonFromDirtyText(content)
            } catch (_: Exception) {
                JsonCleansingUtils.removeMarkdownWrappers(content)
            }
            return gson.fromJson(cleanedJson, VLMResponse::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse VLM response: $responseBody")
            throw IllegalStateException("Failed to parse VLM response", e)
        }
    }
}
