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
                mode = if (intent.action == ModelConstants.ACTION_TTS_FEEDBACK) {
                    ExecutionMode.REQUIRE_CLARIFICATION
                } else {
                    ExecutionMode.REQUIRE_CLARIFICATION
                },
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
            return ExecutionSuggestion(
                mode = ExecutionMode.REQUIRE_CONFIRMATION,
                summary = summary,
                prompt = intent.buildConfirmationPrompt(),
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
            validation.issues.any { it.contains("time", ignoreCase = true) } -> {
                "我识别到了活动，但时间还不够确定。请告诉我是上午还是下午，或者直接说完整时间。"
            }

            validation.issues.any { it.contains("location", ignoreCase = true) } -> {
                "我还不能稳定确定地点。请把目标再对准一些，或者直接说出地点名称。"
            }

            validation.issues.any { it.contains("phone", ignoreCase = true) } -> {
                "我还没有拿到稳定的联系电话。请再靠近一点，或者直接口述号码。"
            }

            else -> {
                "当前信息还不够完整。请调整角度或补充一句说明。"
            }
        }
    }

    private fun buildLowConfidencePrompt(intent: ExecutableIntent): String {
        return when (intent.action) {
            ModelConstants.ACTION_CREATE_EVENT -> {
                "我识别到了一个可能的日程，但还不够确定。请再靠近海报，或者补充时间和地点。"
            }

            ModelConstants.ACTION_NAVIGATE -> {
                "我识别到了一个可能的地点，但还不够稳定。请再对准地点信息。"
            }

            ModelConstants.ACTION_SEND_SMS -> {
                "我整理出了短信草稿，但号码或内容还不够确定。请再确认一次。"
            }

            else -> {
                "当前结果还不够稳定。请稍微调整一下再试。"
            }
        }
    }
}
