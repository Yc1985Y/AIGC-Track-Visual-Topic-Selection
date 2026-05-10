package com.vsa.visualsemanticagent.utils

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMPayload
import com.vsa.visualsemanticagent.model.VLMResponse

object ResponseInterpreter {

    private val supportedActions = setOf(
        ModelConstants.ACTION_CREATE_EVENT,
        ModelConstants.ACTION_NAVIGATE,
        ModelConstants.ACTION_TTS_FEEDBACK,
        ModelConstants.ACTION_CLARIFICATION,
        ModelConstants.ACTION_UNKNOWN
    )

    fun normalize(response: VLMResponse): VLMResponse {
        val safeAction = normalizeAction(response.action)
        val payload = response.payload.normalizePayload()

        return response.copy(
            action = safeAction,
            confidence = response.confidence?.coerceIn(0.0, 1.0),
            payload = payload,
            fallbackQuery = response.fallbackQuery.cleanValue(),
            title = payload.title,
            time = payload.time,
            location = payload.location,
            answer = payload.answer,
            description = payload.description,
            phoneNumber = payload.phoneNumber
        )
    }

    fun buildStatusMessage(response: VLMResponse, dispatchSummary: String? = null): String {
        if (!dispatchSummary.isNullOrBlank()) {
            return dispatchSummary
        }

        return when (response.action) {
            ModelConstants.ACTION_CREATE_EVENT -> {
                response.title?.let { "已提取校园日程信息：$it" } ?: "已提取校园日程信息"
            }

            ModelConstants.ACTION_NAVIGATE -> {
                response.location?.let { "已识别校园地点：$it" } ?: "已识别校园地点"
            }

            ModelConstants.ACTION_SEND_SMS -> "当前版本聚焦校园通知转日程，短信不作为主流程。"
            ModelConstants.ACTION_CLARIFICATION -> response.fallbackQuery
                ?: response.answer
                ?: "信息还不完整，需要补充。"

            ModelConstants.ACTION_TTS_FEEDBACK -> response.answer
                ?: response.description
                ?: "已生成语义反馈"

            else -> response.answer
                ?: response.description
                ?: "未识别到可创建日程的校园通知"
        }
    }

    fun buildSpeechText(response: VLMResponse, dispatchSummary: String? = null): String? {
        return response.answer
            ?: response.description
            ?: response.fallbackQuery
            ?: dispatchSummary
            ?: buildStatusMessage(response, dispatchSummary)
    }

    private fun VLMPayload?.normalizePayload(): VLMPayload {
        return VLMPayload(
            title = this?.title.cleanValue(),
            time = this?.time.normalizeIsoTime(),
            location = this?.location.cleanValue(),
            phoneNumber = this?.phoneNumber.normalizePhoneNumber(),
            description = this?.description.cleanValue(),
            answer = this?.answer.cleanValue()
        )
    }

    private fun normalizeAction(action: String?): String {
        return when (action.cleanValue()?.lowercase()) {
            ModelConstants.ACTION_CREATE_EVENT -> ModelConstants.ACTION_CREATE_EVENT
            ModelConstants.ACTION_NAVIGATE -> ModelConstants.ACTION_NAVIGATE
            ModelConstants.ACTION_TTS_FEEDBACK -> ModelConstants.ACTION_TTS_FEEDBACK
            ModelConstants.ACTION_CLARIFICATION -> ModelConstants.ACTION_CLARIFICATION
            ModelConstants.ACTION_SEND_SMS -> ModelConstants.ACTION_UNKNOWN
            ModelConstants.ACTION_UNKNOWN -> ModelConstants.ACTION_UNKNOWN
            else -> ModelConstants.ACTION_UNKNOWN
        }
    }

    private fun String?.cleanValue(): String? {
        val cleaned = this?.trim().orEmpty()
        return cleaned.takeIf { it.isNotBlank() }
    }

    private fun String?.normalizeIsoTime(): String? {
        val cleaned = this.cleanValue() ?: return null
        return cleaned.replace(" ", "T")
    }

    private fun String?.normalizePhoneNumber(): String? {
        val cleaned = this.cleanValue() ?: return null
        val candidate = Regex("""\+?\d[\d\s\-()]{4,}\d""")
            .find(cleaned)
            ?.value
            ?: cleaned
        val hasLeadingPlus = candidate.trim().startsWith("+")
        val digitsOnly = candidate.filter(Char::isDigit)
        if (digitsOnly.length < 5) return null

        val normalizedDigits = when {
            !hasLeadingPlus && digitsOnly.startsWith("0086") && digitsOnly.length > 11 -> {
                digitsOnly.removePrefix("0086")
            }

            !hasLeadingPlus && digitsOnly.startsWith("86") && digitsOnly.length > 11 -> {
                digitsOnly.removePrefix("86")
            }

            else -> digitsOnly
        }

        val normalized = if (hasLeadingPlus) "+$normalizedDigits" else normalizedDigits
        return normalized.takeIf { it.any(Char::isDigit) }
    }
}
