package com.vsa.visualsemanticagent.decision

import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse

enum class IntentRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class ExecutableIntent(
    val scene: String,
    val action: String,
    val title: String? = null,
    val time: String? = null,
    val location: String? = null,
    val answer: String? = null,
    val description: String? = null,
    val phoneNumber: String? = null,
    val confidence: Double,
    val requiresConfirmation: Boolean,
    val riskLevel: IntentRiskLevel
) {
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
                "检测到就诊信息：$eventTitle，时间 $eventTime，地点 $eventLocation。是否创建提醒？"
            }
            ModelConstants.ACTION_NAVIGATE -> {
                val target = location ?: "目标科室"
                "检测到目标地点：$target。是否开始导航？"
            }
            ModelConstants.ACTION_SEND_SMS -> {
                val number = phoneNumber ?: "联系人"
                "已整理出一条待发送消息，接收对象为 $number。是否继续？"
            }
            ModelConstants.ACTION_TTS_FEEDBACK -> {
                answer ?: description ?: "已识别到画面信息，是否继续播报？"
            }
            else -> "当前结果不够稳定，是否重试？"
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
        confidence: Double
    ): ExecutableIntent {
        val safeAction = response.action?.trim().orEmpty().ifBlank {
            ModelConstants.ACTION_UNKNOWN
        }

        return ExecutableIntent(
            scene = SCENE_HOSPITAL_ASSIST,
            action = safeAction,
            title = response.title?.trim(),
            time = response.time?.trim(),
            location = response.location?.trim(),
            answer = response.answer?.trim(),
            description = response.description?.trim(),
            phoneNumber = response.phoneNumber?.trim(),
            confidence = confidence.coerceIn(0.0, 1.0),
            requiresConfirmation = safeAction != ModelConstants.ACTION_UNKNOWN,
            riskLevel = riskLevelFor(safeAction)
        )
    }

    private fun riskLevelFor(action: String): IntentRiskLevel {
        return when (action) {
            ModelConstants.ACTION_CREATE_EVENT,
            ModelConstants.ACTION_NAVIGATE,
            ModelConstants.ACTION_SEND_SMS -> IntentRiskLevel.HIGH
            ModelConstants.ACTION_TTS_FEEDBACK -> IntentRiskLevel.LOW
            else -> IntentRiskLevel.MEDIUM
        }
    }
}
