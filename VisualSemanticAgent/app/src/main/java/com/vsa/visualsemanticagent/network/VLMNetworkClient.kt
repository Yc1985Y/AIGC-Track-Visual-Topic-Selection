package com.vsa.visualsemanticagent.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.utils.JsonCleansingUtils
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber

class VLMNetworkClient(
    private val apiKey: String,
    private val modelName: String,
    private val apiEndpoint: String,
    private val useMockMode: Boolean = false
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
        if (useMockMode) {
            Timber.d("Using mock VLM response for userText=%s", userText)
            return@withContext MockVLMResponseFactory.createResponse(userText)
        }

        var lastError: Exception? = null

        repeat(2) { attempt ->
            val requestId = UUID.randomUUID().toString()
            try {
                val requestBody = buildRequestPayload(base64Image, userText)
                val request = Request.Builder()
                    .url(
                        apiEndpoint.toHttpUrl().newBuilder()
                            .addQueryParameter("request_id", requestId)
                            .build()
                    )
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    ensureSuccessfulResponse(response, requestId)
                    val responseBody = response.body?.string().orEmpty()
                    return@withContext parseOpenAIResponse(responseBody)
                }
            } catch (e: VLMResponseParseException) {
                lastError = e
                Timber.w(e, "VLM response parsing failed on attempt %s", attempt + 1)
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: VLMApiException) {
                lastError = e
                Timber.w(e, "VLM API rejected request on attempt %s", attempt + 1)
                if (!e.isRetryable || attempt == 1) {
                    throw e
                }
                delay(1200)
            } catch (e: IOException) {
                lastError = VLMNetworkException(e)
                Timber.w(e, "VLM network failed on attempt %s", attempt + 1)
                if (attempt == 0) {
                    delay(1200)
                }
            } catch (e: Exception) {
                lastError = e
                Timber.w(e, "VLM request failed on attempt %s", attempt + 1)
                if (attempt == 0) {
                    delay(1200)
                }
            }
        }

        throw lastError ?: IllegalStateException("VLM request failed after retry")
    }

    private fun ensureSuccessfulResponse(
        response: Response,
        requestId: String
    ) {
        if (response.isSuccessful) return

        val errorBody = response.body?.string().orEmpty()
        throw VLMApiException(
            code = response.code,
            requestId = requestId,
            responseBody = errorBody
        )
    }

    private fun buildRequestPayload(
        base64Image: String,
        userText: String
    ) = gson.toJson(
        mutableMapOf<String, Any>(
            "model" to modelName,
            "temperature" to 0.2,
            "stream" to false,
            "max_tokens" to 2048,
            "reasoning_effort" to "minimal",
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
        ).apply {
            when {
                modelName.contains("qwen", ignoreCase = true) -> {
                    put("enable_thinking", false)
                }

                modelName.contains("deepseek", ignoreCase = true) ||
                    modelName.contains("doubao", ignoreCase = true) ||
                    modelName.contains("seed", ignoreCase = true) -> {
                    put("thinking", mapOf("type" to "disabled"))
                }
            }
        }
    ).toRequestBody("application/json".toMediaType())

    private fun buildSystemPrompt(): String {
        return """
You are the structured decision engine for a mobile visual-to-tool middleware.
Return strict JSON only. Do not output markdown, explanation, or code fences.

Required schema:
{
  "action": "create_event|navigate|tts_feedback|send_sms|clarification|unknown",
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
2. If a critical field is missing or uncertain, use clarification and fill fallback_query.
3. create_event should include title, ISO-like time, and location whenever possible.
4. navigate should include a concrete location.
5. send_sms must be conservative and should only be chosen when the user intent is explicit.
6. tts_feedback should summarize or answer clearly for voice playback.
7. Never invent absent details.
8. Output JSON only.
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
                ?: throw VLMResponseParseException("Empty parsed response", responseBody)
        } catch (e: VLMResponseParseException) {
            throw e
        } catch (e: JsonParseException) {
            Timber.e(e, "Failed to parse VLM response JSON: %s", responseBody)
            throw VLMResponseParseException("Failed to parse VLM response JSON", responseBody, e)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse VLM response: %s", responseBody)
            throw VLMResponseParseException("Failed to parse VLM response", responseBody, e)
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
