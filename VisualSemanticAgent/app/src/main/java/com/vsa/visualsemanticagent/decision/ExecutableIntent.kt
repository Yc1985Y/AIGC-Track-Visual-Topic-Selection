package com.vsa.visualsemanticagent.decision

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse
import kotlin.math.roundToInt

enum class IntentRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class ExecutablePayload(
    val title: String? = null,
    val time: String? = null,
    val location: String? = null,
    val phoneNumber: String? = null,
    val description: String? = null,
    val answer: String? = null,
)

data class ExecutableIntent(
    val scene: String,
    val action: String,
    val payload: ExecutablePayload,
    val modelConfidence: Double,
    val fusedConfidence: Double,
    val fallbackQuery: String?,
    val requiresConfirmation: Boolean,
    val riskLevel: IntentRiskLevel,
) {
    val title: String?
        get() = payload.title

    val time: String?
        get() = payload.time

    val location: String?
        get() = payload.location

    val answer: String?
        get() = payload.answer

    val description: String?
        get() = payload.description

    val phoneNumber: String?
        get() = payload.phoneNumber

    val stabilityKey: String
        get() = listOf(
            scene,
            action,
            title.normalizeSlot(),
            time.normalizeSlot(),
            location.normalizeSlot(),
            phoneNumber.normalizeSlot()
        ).joinToString(separator = "|")

    fun buildConfirmationPrompt(): String {
        return when (action) {
            ModelConstants.ACTION_CREATE_EVENT -> {
                val eventTitle = title ?: "新的提醒"
                val eventTime = time ?: "未确认时间"
                val eventLocation = location ?: "未确认地点"
                "识别到日程信息：$eventTitle，时间 $eventTime，地点 $eventLocation。是否创建提醒？"
            }

            ModelConstants.ACTION_NAVIGATE -> {
                val target = location ?: "目标地点"
                "识别到导航目标：$target。是否开始导航？"
            }

            ModelConstants.ACTION_SEND_SMS -> {
                val number = phoneNumber ?: "联系人"
                "已整理出一条短信草稿，接收方为 $number。是否打开短信草稿？"
            }

            ModelConstants.ACTION_TTS_FEEDBACK -> {
                answer ?: description ?: "已生成语音反馈。是否继续播报？"
            }

            ModelConstants.ACTION_CLARIFICATION -> {
                fallbackQuery ?: "当前结果还不够确定。请再补充一点信息。"
            }

            else -> "当前结果还不够稳定。是否重试？"
        }
    }

    fun buildSummary(): String {
        val confidencePercent = (fusedConfidence * 100).roundToInt()
        return when (action) {
            ModelConstants.ACTION_CREATE_EVENT -> {
                "建议创建日程，可信度 ${confidencePercent}%"
            }

            ModelConstants.ACTION_NAVIGATE -> {
                "建议发起导航，可信度 ${confidencePercent}%"
            }

            ModelConstants.ACTION_SEND_SMS -> {
                "建议生成短信草稿，可信度 ${confidencePercent}%"
            }

            ModelConstants.ACTION_TTS_FEEDBACK -> {
                "建议直接语音播报，可信度 ${confidencePercent}%"
            }

            ModelConstants.ACTION_CLARIFICATION -> {
                "信息还不够完整，建议追问修复"
            }

            else -> {
                "当前结果暂不适合直接执行"
            }
        }
    }

    private fun String?.normalizeSlot(): String {
        return this
            ?.trim()
            ?.lowercase()
            ?.replace("\\s+".toRegex(), "")
            .orEmpty()
    }
}

object HospitalIntentSchema {
    const val SCENE_HOSPITAL_ASSIST = "hospital_outpatient_assist"

    fun fromResponse(
        response: VLMResponse,
        qualityConfidence: Double = 1.0,
        stabilityConfidence: Double = 1.0,
    ): ExecutableIntent {
        val safeAction = response.action?.trim().orEmpty().ifBlank {
            ModelConstants.ACTION_UNKNOWN
        }
        val payload = ExecutablePayload(
            title = response.payload?.title?.trim().orEmpty().ifBlank { response.title?.trim() },
            time = response.payload?.time?.trim().orEmpty().ifBlank { response.time?.trim() },
            location = response.payload?.location?.trim().orEmpty().ifBlank { response.location?.trim() },
            phoneNumber = response.payload?.phoneNumber?.trim().orEmpty().ifBlank { response.phoneNumber?.trim() },
            description = response.payload?.description?.trim().orEmpty().ifBlank { response.description?.trim() },
            answer = response.payload?.answer?.trim().orEmpty().ifBlank { response.answer?.trim() },
        )

        val modelConfidence = (response.confidence ?: defaultConfidenceFor(safeAction)).coerceIn(0.0, 1.0)
        val completenessConfidence = computeCompletenessConfidence(safeAction, payload)
        val fusedConfidence = (
            modelConfidence * 0.5 +
                qualityConfidence.coerceIn(0.0, 1.0) * 0.2 +
                stabilityConfidence.coerceIn(0.0, 1.0) * 0.15 +
                completenessConfidence * 0.15
            ).coerceIn(0.0, 1.0)
        val riskLevel = riskLevelFor(safeAction)

        return ExecutableIntent(
            scene = SCENE_HOSPITAL_ASSIST,
            action = safeAction,
            payload = payload,
            modelConfidence = modelConfidence,
            fusedConfidence = fusedConfidence,
            fallbackQuery = response.fallbackQuery?.trim(),
            requiresConfirmation = requiresConfirmation(safeAction, riskLevel),
            riskLevel = riskLevel
        )
    }

    private fun defaultConfidenceFor(action: String): Double {
        return when (action) {
            ModelConstants.ACTION_SEND_SMS -> 0.76
            ModelConstants.ACTION_CREATE_EVENT,
            ModelConstants.ACTION_NAVIGATE -> 0.82
            ModelConstants.ACTION_TTS_FEEDBACK -> 0.88
            ModelConstants.ACTION_CLARIFICATION -> 0.55
            else -> 0.45
        }
    }

    private fun computeCompletenessConfidence(
        action: String,
        payload: ExecutablePayload
    ): Double {
        val checks = when (action) {
            ModelConstants.ACTION_CREATE_EVENT -> listOf(
                !payload.title.isNullOrBlank(),
                !payload.time.isNullOrBlank(),
                !payload.location.isNullOrBlank()
            )

            ModelConstants.ACTION_NAVIGATE -> listOf(
                !payload.location.isNullOrBlank()
            )

            ModelConstants.ACTION_SEND_SMS -> listOf(
                !payload.phoneNumber.isNullOrBlank(),
                !payload.description.isNullOrBlank() || !payload.answer.isNullOrBlank()
            )

            ModelConstants.ACTION_TTS_FEEDBACK -> listOf(
                !payload.answer.isNullOrBlank() || !payload.description.isNullOrBlank()
            )

            ModelConstants.ACTION_CLARIFICATION -> listOf(
                !payload.answer.isNullOrBlank() || !payload.description.isNullOrBlank()
            )

            else -> listOf(false)
        }

        return checks.count { it }.toDouble() / checks.size.coerceAtLeast(1)
    }

    private fun requiresConfirmation(
        action: String,
        riskLevel: IntentRiskLevel
    ): Boolean {
        return when (action) {
            ModelConstants.ACTION_TTS_FEEDBACK -> false
            ModelConstants.ACTION_CLARIFICATION -> false
            else -> riskLevel != IntentRiskLevel.LOW
        }
    }

    private fun riskLevelFor(action: String): IntentRiskLevel {
        return when (action) {
            ModelConstants.ACTION_SEND_SMS -> IntentRiskLevel.HIGH
            ModelConstants.ACTION_CREATE_EVENT,
            ModelConstants.ACTION_NAVIGATE -> IntentRiskLevel.MEDIUM
            ModelConstants.ACTION_TTS_FEEDBACK -> IntentRiskLevel.LOW
            else -> IntentRiskLevel.MEDIUM
        }
    }
}
