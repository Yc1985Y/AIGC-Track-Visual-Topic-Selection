package com.vsa.visualsemanticagent.utils

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse

object ResponseInterpreter {

    private val supportedActions = setOf(
        ModelConstants.ACTION_CREATE_EVENT,
        ModelConstants.ACTION_NAVIGATE,
        ModelConstants.ACTION_TTS_FEEDBACK,
        ModelConstants.ACTION_SEND_SMS,
        ModelConstants.ACTION_UNKNOWN
    )

    fun normalize(response: VLMResponse): VLMResponse {
        val normalizedAction = response.action.cleanValue()?.lowercase().orEmpty()
        val safeAction = normalizedAction.takeIf { it in supportedActions } ?: ModelConstants.ACTION_UNKNOWN

        return response.copy(
            action = safeAction,
            title = response.title.cleanValue(),
            time = response.time.cleanValue(),
            location = response.location.cleanValue(),
            answer = response.answer.cleanValue(),
            description = response.description.cleanValue(),
            phoneNumber = response.phoneNumber.normalizePhoneNumber()
        )
    }

    fun buildStatusMessage(response: VLMResponse, dispatchSummary: String? = null): String {
        if (!dispatchSummary.isNullOrBlank()) {
            return dispatchSummary
        }

        return when (response.action) {
            ModelConstants.ACTION_CREATE_EVENT -> {
                when {
                    !response.title.isNullOrBlank() -> "已识别活动：${response.title}"
                    else -> "已识别活动信息。"
                }
            }
            ModelConstants.ACTION_NAVIGATE -> {
                when {
                    !response.location.isNullOrBlank() -> "已识别地点：${response.location}"
                    else -> "已识别导航地点。"
                }
            }
            ModelConstants.ACTION_SEND_SMS -> "已准备短信内容。"
            ModelConstants.ACTION_TTS_FEEDBACK -> response.answer
                ?: response.description
                ?: "已生成语义反馈。"
            else -> response.answer
                ?: response.description
                ?: "暂时无法确定最合适的动作。"
        }
    }

    fun buildSpeechText(response: VLMResponse, dispatchSummary: String? = null): String? {
        return response.answer
            ?: response.description
            ?: dispatchSummary
            ?: buildStatusMessage(response, dispatchSummary)
    }

    private fun String?.cleanValue(): String? {
        val cleaned = this?.trim().orEmpty()
        return cleaned.takeIf { it.isNotBlank() }
    }

    private fun String?.normalizePhoneNumber(): String? {
        val cleaned = this.cleanValue() ?: return null
        val candidate = Regex("""\+?\d[\d\s\-()（）]{4,}\d""")
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
