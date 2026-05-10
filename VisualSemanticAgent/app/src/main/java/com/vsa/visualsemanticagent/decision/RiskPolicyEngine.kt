package com.vsa.visualsemanticagent.decision

import com.vsa.visualsemanticagent.model.ModelConstants

enum class ExecutionMode {
    DIRECT_TTS,
    REQUIRE_CONFIRMATION,
    REQUIRE_CLARIFICATION,
    BLOCKED
}

data class ExecutionSuggestion(
    val mode: ExecutionMode,
    val summary: String,
    val prompt: String,
    val threshold: Double,
    val validation: ValidationResult,
)

class RiskPolicyEngine(
    private val highRiskThreshold: Double = 0.9,
    private val mediumRiskThreshold: Double = 0.7,
    private val lowRiskThreshold: Double = 0.5,
) {

    fun evaluate(intent: ExecutableIntent): ExecutionSuggestion {
        val validation = ActionValidator.validate(intent)
        val threshold = thresholdFor(intent)
        val summary = intent.buildSummary()

        if (!validation.isValid) {
            return ExecutionSuggestion(
                mode = ExecutionMode.REQUIRE_CLARIFICATION,
                summary = summary,
                prompt = buildClarificationPrompt(intent, validation),
                threshold = threshold,
                validation = validation
            )
        }

        if (intent.action == ModelConstants.ACTION_CLARIFICATION) {
            return ExecutionSuggestion(
                mode = ExecutionMode.REQUIRE_CLARIFICATION,
                summary = summary,
                prompt = intent.fallbackQuery ?: "我还不够确定，请再补充一点信息。",
                threshold = threshold,
                validation = validation
            )
        }

        if (intent.fusedConfidence < threshold) {
            return ExecutionSuggestion(
                mode = ExecutionMode.REQUIRE_CLARIFICATION,
                summary = summary,
                prompt = intent.fallbackQuery ?: buildLowConfidencePrompt(intent),
                threshold = threshold,
                validation = validation
            )
        }

        if (intent.action == ModelConstants.ACTION_TTS_FEEDBACK && !intent.requiresConfirmation) {
            return ExecutionSuggestion(
                mode = ExecutionMode.DIRECT_TTS,
                summary = summary,
                prompt = intent.answer ?: intent.description ?: summary,
                threshold = threshold,
                validation = validation
            )
        }

        if (intent.requiresConfirmation) {
            val finalPrompt = when {
                intent.riskLevel == IntentRiskLevel.HIGH && intent.action == com.vsa.visualsemanticagent.model.ModelConstants.ACTION_SEND_SMS ->
                    "高风险动作已被拦截，当前版本只保留校园通知转日程主流程。"
                else -> intent.buildConfirmationPrompt()
            }
            return ExecutionSuggestion(
                mode = ExecutionMode.REQUIRE_CONFIRMATION,
                summary = summary,
                prompt = finalPrompt,
                threshold = threshold,
                validation = validation
            )
        }

        return ExecutionSuggestion(
            mode = ExecutionMode.BLOCKED,
            summary = summary,
            prompt = "当前结果暂不适合执行，请重试。",
            threshold = threshold,
            validation = validation
        )
    }

    private fun thresholdFor(intent: ExecutableIntent): Double {
        return when (intent.riskLevel) {
            IntentRiskLevel.HIGH -> highRiskThreshold
            IntentRiskLevel.MEDIUM -> mediumRiskThreshold
            IntentRiskLevel.LOW -> lowRiskThreshold
        }
    }

    private fun buildClarificationPrompt(
        intent: ExecutableIntent,
        validation: ValidationResult
    ): String {
        if (!intent.fallbackQuery.isNullOrBlank()) {
            return intent.fallbackQuery
        }

        return when {
            validation.issues.any { it.contains("title", ignoreCase = true) } -> {
                "我还不能确定这是哪个活动，请换个角度重拍，或直接补充活动名称。"
            }

            validation.issues.any { it.contains("time", ignoreCase = true) } -> {
                "我识别到了活动，但没有看清开始时间，请补充完整时间。"
            }

            validation.issues.any { it.contains("location", ignoreCase = true) } -> {
                "我识别到了活动时间，但地点不清楚，请补充地点。"
            }

            else -> {
                "当前通知信息还不够完整。请调整角度，确保标题、时间和地点都在内容中。"
            }
        }
    }

    private fun buildLowConfidencePrompt(intent: ExecutableIntent): String {
        return when (intent.action) {
            ModelConstants.ACTION_CREATE_EVENT -> {
                "我识别到了一个可能的校园日程，但还不够确定。请再靠近海报，或补充活动时间和地点。"
            }

            ModelConstants.ACTION_NAVIGATE -> {
                "我识别到了一个可能的校园地点，但还不够稳定。请对准地点文字再试。"
            }

            else -> {
                "当前画面还不够清晰，暂时无法生成稳定日程。请调整角度后重试。"
            }
        }
    }
}
