package com.vsa.visualsemanticagent.network

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.utils.JsonCleansingUtils
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber

class VLMNetworkClient(
    private val appId: String,
    private val apiKey: String,
    private val modelName: String,
    private val apiEndpoint: String,
    private val ocrEndpoint: String,
    private val useMockMode: Boolean = false
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun sendCampusNoticeRequest(
        base64Image: String?,
        userText: String,
        rawText: String?,
        sourceType: String
    ): VLMResponse = withContext(Dispatchers.IO) {
        if (useMockMode) {
            return@withContext MockVLMResponseFactory.buildResponse(
                "$userText ${rawText.orEmpty()} $sourceType"
            )
        }

        var lastError: Exception? = null
        repeat(2) { attempt ->
            val requestId = UUID.randomUUID().toString()
            try {
                val ocrText = if (!base64Image.isNullOrBlank()) {
                    requestOcrText(base64Image, requestId)
                } else {
                    null
                }
                val requestBody = buildCampusNoticePayload(
                    userText = userText,
                    rawText = rawText,
                    sourceType = sourceType,
                    ocrText = ocrText
                )
                return@withContext executeRequest(requestId, requestBody)
            } catch (e: VLMResponseParseException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: VLMApiException) {
                lastError = e
                if (!e.isRetryable || attempt == 1) throw e
                delay(1200)
            } catch (e: IOException) {
                lastError = VLMNetworkException(e)
                if (attempt == 0) delay(1200)
            } catch (e: Exception) {
                lastError = e
                if (attempt == 0) delay(1200)
            }
        }
        throw lastError ?: IllegalStateException("Campus notice request failed after retry")
    }

    suspend fun sendMultimodalRequest(
        base64Image: String,
        userText: String
    ): VLMResponse = sendCampusNoticeRequest(
        base64Image = base64Image,
        userText = userText,
        rawText = null,
        sourceType = "摄像头输入"
    )

    private fun executeRequest(
        requestId: String,
        requestBody: okhttp3.RequestBody
    ): VLMResponse {
        val request = Request.Builder()
            .url(
                apiEndpoint.toHttpUrl().newBuilder()
                    .addQueryParameter("requestId", requestId)
                    .build()
            )
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            ensureSuccessfulResponse(response, requestId)
            val responseBody = response.body?.string().orEmpty()
            return parseChatResponse(responseBody, requestId)
        }
    }

    private fun ensureSuccessfulResponse(
        response: Response,
        requestId: String
    ) {
        if (response.isSuccessful) return
        throw VLMApiException(
            code = response.code,
            requestId = requestId,
            responseBody = response.body?.string().orEmpty()
        )
    }

    private fun buildCampusNoticePayload(
        userText: String,
        rawText: String?,
        sourceType: String,
        ocrText: String?
    ): okhttp3.RequestBody {
        val payload = linkedMapOf<String, Any>(
            "model" to modelName,
            "temperature" to 0.2,
            "stream" to false,
            "max_tokens" to 2048,
            "reasoning_effort" to "minimal",
            "messages" to listOf(
                mapOf("role" to "system", "content" to buildSystemPrompt()),
                mapOf(
                    "role" to "user",
                    "content" to buildUserPrompt(
                        userText = userText,
                        rawText = rawText,
                        sourceType = sourceType,
                        ocrText = ocrText
                    )
                )
            )
        )

        if (modelName.contains("qwen", ignoreCase = true)) {
            payload["enable_thinking"] = false
        }
        if (modelName.contains("deepseek", ignoreCase = true) ||
            modelName.contains("doubao", ignoreCase = true) ||
            modelName.contains("seed", ignoreCase = true)
        ) {
            payload["thinking"] = mapOf("type" to "disabled")
        }

        return gson.toJson(payload).toRequestBody("application/json".toMediaType())
    }

    private fun buildUserPrompt(
        userText: String,
        rawText: String?,
        sourceType: String,
        ocrText: String?
    ): String {
        val nowInShanghai = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
        val nowText = nowInShanghai.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val todayText = nowInShanghai.toLocalDate().toString()
        return buildString {
            appendLine("当前设备时区：Asia/Shanghai")
            appendLine("当前参考时间：$nowText")
            appendLine("当前参考日期：$todayText")
            appendLine("用户指令：$userText")
            appendLine("输入来源：$sourceType")
            if (!ocrText.isNullOrBlank()) {
                appendLine("图片 OCR 提取文本：")
                appendLine(ocrText)
            }
            if (!rawText.isNullOrBlank()) {
                appendLine("用户导入的通知原文：")
                appendLine(rawText)
            }
        }
    }

    private fun buildSystemPrompt(): String {
        return """
You are the structured decision engine for a campus schedule assistant on a vivo Android phone.
Return strict JSON only.

Allowed actions:
- create_event
- navigate
- clarification
- tts_feedback
- unknown

Required schema:
{
  "action": "create_event|navigate|tts_feedback|clarification|unknown",
  "confidence": 0.0,
  "payload": {
    "title": "",
    "time": "",
    "location": "",
    "phone_number": "",
    "description": "",
    "answer": ""
  },
  "fallback_query": "",
  "target_found": true
}

Rules:
1. Always provide confidence between 0.0 and 1.0.
2. For create_event, title, time, and location are required. If any is missing or uncertain, use clarification.
3. time should be ISO-like when possible, for example 2026-05-12T15:00:00.
4. Use the current reference time from the user message to resolve relative expressions such as "今天", "明天", "后天", "本周二", "下周一晚上七点".
5. If the exact date still cannot be inferred, keep the original relative phrase in payload.time and explain uncertainty in payload.description, then use clarification.
6. Never invent absent details.
7. If a screenshot contains multiple notices, use clarification and ask the user to select one.
8. Output JSON only.
        """.trimIndent()
    }

    private fun parseChatResponse(responseBody: String, requestId: String): VLMResponse {
        try {
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val code = json.get("code")?.asInt
            if (code != null && code != 0) {
                throw VLMApiException(code, requestId, responseBody)
            }
            val errorCode = json.get("error_code")?.asInt
            if (errorCode != null && errorCode != 0) {
                throw VLMApiException(errorCode, requestId, responseBody)
            }

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
                ?: throw VLMResponseParseException("Empty parsed response", responseBody)
        } catch (e: VLMResponseParseException) {
            throw e
        } catch (e: VLMApiException) {
            throw e
        } catch (e: JsonParseException) {
            throw VLMResponseParseException("Failed to parse VLM response JSON", responseBody, e)
        } catch (e: Exception) {
            throw VLMResponseParseException("Failed to parse VLM response", responseBody, e)
        }
    }

    private fun requestOcrText(
        base64Image: String,
        requestId: String
    ): String? {
        val request = Request.Builder()
            .url(
                ocrEndpoint.toHttpUrl().newBuilder()
                    .addQueryParameter("requestId", requestId)
                    .build()
            )
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(
                FormBody.Builder()
                    .add("image", base64Image)
                    .add("pos", "2")
                    .add("businessid", "aigc$appId")
                    .add("sessid", UUID.randomUUID().toString())
                    .build()
            )
            .build()

        httpClient.newCall(request).execute().use { response ->
            ensureSuccessfulResponse(response, requestId)
            val responseBody = response.body?.string().orEmpty()
            return parseOcrResponse(responseBody)
        }
    }

    private fun parseOcrResponse(responseBody: String): String? {
        val json = gson.fromJson(responseBody, JsonObject::class.java)
        val errorCode = json.get("error_code")?.asInt ?: -1
        if (errorCode != 0) {
            val message = json.get("error_msg")?.asString.orEmpty()
            throw VLMResponseParseException("OCR failed: $message", responseBody)
        }
        val result = json.getAsJsonObject("result") ?: return null
        val words = mutableListOf<String>()
        result.getAsJsonArray("words")?.collectWords(words)
        result.getAsJsonArray("OCR")?.collectWords(words)
        return words
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(separator = "\n")
            .takeIf { it.isNotBlank() }
    }

    private fun JsonArray.collectWords(target: MutableList<String>) {
        forEach { element ->
            if (element.isJsonObject) {
                element.asJsonObject.get("words")?.asString?.let(target::add)
            }
        }
    }
}

class VLMNetworkException(cause: Throwable) : IOException(cause)

class VLMApiException(
    val code: Int,
    val requestId: String,
    val responseBody: String
) : IllegalStateException("API request failed with code: $code, request_id=$requestId, body=$responseBody") {
    val isRetryable: Boolean
        get() = code == 429 || code in 500..599
}

class VLMResponseParseException(
    message: String,
    val rawResponse: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
