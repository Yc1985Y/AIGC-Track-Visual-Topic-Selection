package com.vsa.visualsemanticagent.decision

data class FrameIntentObservation(
    val frameId: Long,
    val intent: ExecutableIntent
)

enum class StabilizerStatus {
    WAITING,
    READY_FOR_CONFIRMATION
}

data class StableIntentDecision(
    val status: StabilizerStatus,
    val stableIntent: ExecutableIntent?,
    val matchedFrames: Int,
    val totalFrames: Int,
    val reason: String
)

class TemporalIntentStabilizer(
    private val requiredConsistentFrames: Int = 3,
    private val minConfidence: Double = 0.78,
    private val maxWindowSize: Int = 5
) {
    private val history = ArrayDeque<FrameIntentObservation>()

    fun observe(observation: FrameIntentObservation): StableIntentDecision {
        history.addLast(observation)
        trimHistory()

        val latestIntent = observation.intent
        if (latestIntent.fusedConfidence < minConfidence) {
            return StableIntentDecision(
                status = StabilizerStatus.WAITING,
                stableIntent = null,
                matchedFrames = 0,
                totalFrames = history.size,
                reason = "latest confidence below threshold"
            )
        }

        val consecutiveMatches = history
            .reversed()
            .takeWhile { item ->
                item.intent.fusedConfidence >= minConfidence &&
                    item.intent.stabilityKey == latestIntent.stabilityKey
            }
            .count()

        return if (consecutiveMatches >= requiredConsistentFrames) {
            StableIntentDecision(
                status = StabilizerStatus.READY_FOR_CONFIRMATION,
                stableIntent = latestIntent,
                matchedFrames = consecutiveMatches,
                totalFrames = history.size,
                reason = "temporal voting passed"
            )
        } else {
            StableIntentDecision(
                status = StabilizerStatus.WAITING,
                stableIntent = null,
                matchedFrames = consecutiveMatches,
                totalFrames = history.size,
                reason = "waiting for more consistent frames"
            )
        }
    }

    fun reset() {
        history.clear()
    }

    private fun trimHistory() {
        while (history.size > maxWindowSize) {
            history.removeFirst()
        }
    }
}
