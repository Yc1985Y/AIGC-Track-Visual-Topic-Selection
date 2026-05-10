package com.vsa.visualsemanticagent.decision

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>
)

object ActionValidator {

    fun validate(intent: ExecutableIntent): ValidationResult {
        val issues = mutableListOf<String>()

        when (intent.action) {
            com.vsa.visualsemanticagent.model.ModelConstants.ACTION_CREATE_EVENT -> {
                if (intent.title.isNullOrBlank()) issues += "missing title"
                if (intent.time.isNullOrBlank()) issues += "missing time"
                if (intent.location.isNullOrBlank()) issues += "missing location"
                val parsed = parseDateTime(intent.time)
                if (intent.time != null && parsed != null && parsed.isBefore(LocalDateTime.now().minusMinutes(30))) {
                    issues += "time is already expired"
                }
            }

            com.vsa.visualsemanticagent.model.ModelConstants.ACTION_NAVIGATE -> {
                if (intent.location.isNullOrBlank()) issues += "missing location"
            }

            com.vsa.visualsemanticagent.model.ModelConstants.ACTION_SEND_SMS -> {
                if (intent.phoneNumber.isNullOrBlank()) issues += "missing phone number"
                if (intent.description.isNullOrBlank() && intent.answer.isNullOrBlank()) {
                    issues += "missing sms body"
                }
            }

            com.vsa.visualsemanticagent.model.ModelConstants.ACTION_TTS_FEEDBACK -> {
                if (intent.answer.isNullOrBlank() && intent.description.isNullOrBlank()) {
                    issues += "missing speech text"
                }
            }
        }

        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues
        )
    }

    private fun isIsoLikeDateTime(value: String?): Boolean {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return false

        return try {
            OffsetDateTime.parse(normalized)
            true
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(normalized)
                true
            } catch (_: DateTimeParseException) {
                false
            }
        }
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return null
        return try {
            OffsetDateTime.parse(normalized).toLocalDateTime()
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(normalized)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}
