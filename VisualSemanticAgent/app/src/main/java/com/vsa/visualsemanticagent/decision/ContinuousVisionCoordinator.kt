package com.vsa.visualsemanticagent.decision

data class FrameQualitySnapshot(
    val hasReadableText: Boolean,
    val sharpness: Double,
    val exposure: Double,
    val targetCenterX: Double? = null,
    val targetCenterY: Double? = null,
    val targetAreaRatio: Double = 0.0
)

enum class GuidanceType {
    SEARCH_TARGET,
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    MOVE_CLOSER,
    HOLD_STEADY,
    BRIGHTER_PLACE,
    READY_TO_PARSE,
    AUTO_CAPTURE
}

data class GuidanceCue(
    val type: GuidanceType,
    val spokenPrompt: String,
    val shouldVibrate: Boolean = false
)

data class ContinuousCaptureDecision(
    val cue: GuidanceCue,
    val shouldAutoCapture: Boolean,
    val stableIntentDecision: StableIntentDecision? = null
)

class ContinuousVisionCoordinator(
    private val stabilizer: TemporalIntentStabilizer = TemporalIntentStabilizer(),
    private val minSharpness: Double = 0.55,
    private val minExposure: Double = 0.35,
    private val minTargetAreaRatio: Double = 0.08
) {

    fun evaluate(
        frameId: Long,
        quality: FrameQualitySnapshot,
        candidateIntent: ExecutableIntent?
    ): ContinuousCaptureDecision {
        if (!quality.hasReadableText) {
            stabilizer.reset()
            return waitingDecision(
                GuidanceType.SEARCH_TARGET,
                "No readable text detected yet. Slowly pan the phone to search for the notice."
            )
        }

        if (quality.exposure < minExposure) {
            stabilizer.reset()
            return waitingDecision(
                GuidanceType.BRIGHTER_PLACE,
                "The scene is too dark. Please move to a brighter position."
            )
        }

        if (quality.sharpness < minSharpness) {
            stabilizer.reset()
            return waitingDecision(
                GuidanceType.HOLD_STEADY,
                "The image is blurred. Please hold the phone steady for a moment."
            )
        }

        directionalCue(quality)?.let { cue ->
            stabilizer.reset()
            return waitingDecision(cue.type, cue.spokenPrompt)
        }

        if (quality.targetAreaRatio in 0.0..<minTargetAreaRatio) {
            stabilizer.reset()
            return waitingDecision(
                GuidanceType.MOVE_CLOSER,
                "The target is still too small. Please move a little closer."
            )
        }

        if (candidateIntent == null) {
            stabilizer.reset()
            return waitingDecision(
                GuidanceType.READY_TO_PARSE,
                "Text detected. Keep the phone still while I read it."
            )
        }

        val stableDecision = stabilizer.observe(
            FrameIntentObservation(
                frameId = frameId,
                intent = candidateIntent
            )
        )

        return if (stableDecision.status == StabilizerStatus.READY_FOR_CONFIRMATION) {
            ContinuousCaptureDecision(
                cue = GuidanceCue(
                    type = GuidanceType.AUTO_CAPTURE,
                    spokenPrompt = "Stable result detected. Capturing and parsing now.",
                    shouldVibrate = true
                ),
                shouldAutoCapture = true,
                stableIntentDecision = stableDecision
            )
        } else {
            ContinuousCaptureDecision(
                cue = GuidanceCue(
                    type = GuidanceType.READY_TO_PARSE,
                    spokenPrompt = "Text detected. Keep holding still while I verify the result."
                ),
                shouldAutoCapture = false,
                stableIntentDecision = stableDecision
            )
        }
    }

    private fun directionalCue(
        quality: FrameQualitySnapshot
    ): GuidanceCue? {
        val centerX = quality.targetCenterX
        val centerY = quality.targetCenterY

        if (centerX != null) {
            if (centerX < 0.35) {
                return GuidanceCue(
                    type = GuidanceType.MOVE_LEFT,
                    spokenPrompt = "The target is on the left. Move the phone slightly left."
                )
            }
            if (centerX > 0.65) {
                return GuidanceCue(
                    type = GuidanceType.MOVE_RIGHT,
                    spokenPrompt = "The target is on the right. Move the phone slightly right."
                )
            }
        }

        if (centerY != null) {
            if (centerY < 0.30) {
                return GuidanceCue(
                    type = GuidanceType.MOVE_UP,
                    spokenPrompt = "The target is too high in the frame. Raise the phone slightly."
                )
            }
            if (centerY > 0.70) {
                return GuidanceCue(
                    type = GuidanceType.MOVE_DOWN,
                    spokenPrompt = "The target is too low in the frame. Lower the phone slightly."
                )
            }
        }

        return null
    }

    private fun waitingDecision(
        type: GuidanceType,
        prompt: String
    ): ContinuousCaptureDecision {
        return ContinuousCaptureDecision(
            cue = GuidanceCue(
                type = type,
                spokenPrompt = prompt
            ),
            shouldAutoCapture = false
        )
    }
}
