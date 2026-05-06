package com.vsa.visualsemanticagent.utils

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMPayload
import com.vsa.visualsemanticagent.model.VLMResponse

object ResponseInterpreter {

    private val supportedActions = setOf(
        ModelConstants.ACTION_CREATE_EVENT,
        ModelConstants.ACTION_NAVIGATE,
        ModelConstants.ACTION_TTS_FEEDBACK,
        ModelConstants.ACTION_SEND_SMS,
        ModelConstants.ACTION_CLARIFICATION,
        ModelConstants.ACTION_UNKNOWN
    )

    fun normalize(response: VLMResponse): VLMResponse {
        val normalizedAction = response.action.cleanValue()?.lowercase().orEmpty()
        val safeAction = normalizedAction.takeIf { it in supportedActions } ?: ModelConstants.ACTION_UNKNOWN
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
                response.title?.let { "已识别到可创建日程：$it" } ?: "已识别到日程信息"
            }

            ModelConstants.ACTION_NAVIGATE -> {
                response.location?.let { "已识别到地点：$it" } ?: "已识别到导航地点"
            }

            ModelConstants.ACTION_SEND_SMS -> "已整理出短信草稿"
            ModelConstants.ACTION_CLARIFICATION -> response.fallbackQuery
                ?: response.answer
                ?: "当前结果还不够确定，请补充一点信息。"

            ModelConstants.ACTION_TTS_FEEDBACK -> response.answer
                ?: response.description
                ?: "已生成语义反馈"

            else -> response.answer
                ?: response.description
                ?: "暂时无法确定最合适的动作"
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
