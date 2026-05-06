package com.vsa.visualsemanticagent

import com.vsa.visualsemanticagent.decision.ExecutionMode
import com.vsa.visualsemanticagent.decision.FrameIntentObservation
import com.vsa.visualsemanticagent.decision.VisualActionIntentSchema
import com.vsa.visualsemanticagent.decision.ContinuousVisionCoordinator
import com.vsa.visualsemanticagent.decision.FrameQualitySnapshot
import com.vsa.visualsemanticagent.decision.GuidanceType
import com.vsa.visualsemanticagent.decision.RiskPolicyEngine
import com.vsa.visualsemanticagent.decision.StabilizerStatus
import com.vsa.visualsemanticagent.decision.TemporalIntentStabilizer
import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMPayload
import com.vsa.visualsemanticagent.model.VLMResponse
import com.vsa.visualsemanticagent.network.MockVLMResponseFactory
import com.vsa.visualsemanticagent.utils.JsonCleansingUtils
import com.vsa.visualsemanticagent.utils.ResponseInterpreter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleTests {

    @Test
    fun extractJsonFromDirtyText_returnsInnerJson() {
        val dirty = "Result: {\"action\":\"tts_feedback\",\"answer\":\"test\"}"
        val json = JsonCleansingUtils.extractJsonFromDirtyText(dirty)

        assertEquals("{\"action\":\"tts_feedback\",\"answer\":\"test\"}", json)
    }

    @Test
    fun removeMarkdownWrappers_removesCodeFence() {
        val wrapped = "```json\n{\"action\":\"unknown\"}\n```"
        val json = JsonCleansingUtils.removeMarkdownWrappers(wrapped)

        assertEquals("{\"action\":\"unknown\"}", json)
    }

    @Test
    fun normalizeResponse_mapsUnknownActionToUnknown() {
        val raw = VLMResponse(
            action = "CREATE_MEETING",
            payload = VLMPayload(
                title = "  demo event  ",
                answer = "  ok  ",
                phoneNumber = " 138-0013-8000 "
            )
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals(ModelConstants.ACTION_UNKNOWN, normalized.action)
        assertEquals("demo event", normalized.title)
        assertEquals("ok", normalized.answer)
        assertEquals("13800138000", normalized.phoneNumber)
    }

    @Test
    fun normalizeResponse_normalizesPhoneAndTime() {
        val raw = VLMResponse(
            action = ModelConstants.ACTION_SEND_SMS,
            confidence = 1.2,
            payload = VLMPayload(
                phoneNumber = " +86 138-0013-8000 ",
                time = "2026-05-20 14:30:00"
            )
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals("+8613800138000", normalized.phoneNumber)
        assertEquals("2026-05-20T14:30:00", normalized.time)
        assertEquals(1.0, normalized.confidence ?: 0.0, 0.0001)
    }

    @Test
    fun mockFactory_returnsCalendarActionForEventPrompt() {
        val response = MockVLMResponseFactory.buildResponse("create_event calendar reminder")

        assertEquals(ModelConstants.ACTION_CREATE_EVENT, response.action)
        assertNotNull(response.payload?.title)
    }

    @Test
    fun visualActionIntentSchema_buildsFusedConfidence() {
        val response = VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            confidence = 0.9,
            payload = VLMPayload(
                title = "AI Lecture",
                time = "2026-05-08T09:00:00",
                location = "Library Hall"
            )
        )

        val intent = VisualActionIntentSchema.fromResponse(
            response = response,
            qualityConfidence = 0.8,
            stabilityConfidence = 0.85
        )

        assertEquals("visual_to_tool_os", intent.scene)
        assertTrue(intent.requiresConfirmation)
        assertEquals(ModelConstants.ACTION_CREATE_EVENT, intent.action)
        assertTrue(intent.fusedConfidence > 0.84)
    }

    @Test
    fun riskPolicyEngine_requiresConfirmationForMediumRiskAction() {
        val engine = RiskPolicyEngine()
        val intent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                confidence = 0.88,
                payload = VLMPayload(location = "Information Building A201")
            )
        )

        val suggestion = engine.evaluate(intent)

        assertEquals(ExecutionMode.REQUIRE_CONFIRMATION, suggestion.mode)
    }

    @Test
    fun riskPolicyEngine_triggersClarificationForInvalidPayload() {
        val engine = RiskPolicyEngine()
        val intent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.92,
                payload = VLMPayload(
                    title = "Research Sharing",
                    location = "Innovation Center"
                )
            )
        )

        val suggestion = engine.evaluate(intent)

        assertEquals(ExecutionMode.REQUIRE_CLARIFICATION, suggestion.mode)
        assertTrue(suggestion.validation.issues.any { it.contains("time") })
    }

    @Test
    fun riskPolicyEngine_allowsDirectTtsForLowRiskAction() {
        val engine = RiskPolicyEngine()
        val intent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                confidence = 0.91,
                payload = VLMPayload(answer = "The registration desk is on your right")
            )
        )

        val suggestion = engine.evaluate(intent)

        assertEquals(ExecutionMode.DIRECT_TTS, suggestion.mode)
    }

    @Test
    fun temporalIntentStabilizer_requiresConsistentFramesBeforeReady() {
        val stabilizer = TemporalIntentStabilizer(
            requiredConsistentFrames = 3,
            minConfidence = 0.8,
            maxWindowSize = 5
        )

        val intent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                confidence = 0.88,
                payload = VLMPayload(location = "Library Hall")
            )
        )

        val first = stabilizer.observe(FrameIntentObservation(1L, intent))
        val second = stabilizer.observe(FrameIntentObservation(2L, intent))
        val third = stabilizer.observe(FrameIntentObservation(3L, intent))

        assertEquals(StabilizerStatus.WAITING, first.status)
        assertEquals(StabilizerStatus.WAITING, second.status)
        assertEquals(StabilizerStatus.READY_FOR_CONFIRMATION, third.status)
        assertEquals(3, third.matchedFrames)
        assertEquals("temporal voting passed", third.reason)
    }

    @Test
    fun temporalIntentStabilizer_rejectsLowFusedConfidenceLatestFrame() {
        val stabilizer = TemporalIntentStabilizer(
            requiredConsistentFrames = 2,
            minConfidence = 0.8,
            maxWindowSize = 4
        )

        val strongIntent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                confidence = 0.86,
                payload = VLMPayload(answer = "registration desk is ahead")
            )
        )
        val weakIntent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                confidence = 0.3,
                payload = VLMPayload(answer = "registration desk is ahead")
            )
        )

        stabilizer.observe(FrameIntentObservation(1L, strongIntent))
        val result = stabilizer.observe(FrameIntentObservation(2L, weakIntent))

        assertEquals(StabilizerStatus.WAITING, result.status)
        assertEquals("latest confidence below threshold", result.reason)
    }

    @Test
    fun continuousVisionCoordinator_guidesUserWhenNoReadableText() {
        val coordinator = ContinuousVisionCoordinator()

        val result = coordinator.evaluate(
            frameId = 1L,
            quality = FrameQualitySnapshot(
                hasReadableText = false,
                sharpness = 0.9,
                exposure = 0.8
            ),
            candidateIntent = null
        )

        assertEquals(GuidanceType.SEARCH_TARGET, result.cue.type)
        assertTrue(!result.shouldAutoCapture)
    }

    @Test
    fun continuousVisionCoordinator_autoCapturesAfterStableIntent() {
        val coordinator = ContinuousVisionCoordinator(
            stabilizer = TemporalIntentStabilizer(
                requiredConsistentFrames = 2,
                minConfidence = 0.8,
                maxWindowSize = 4
            )
        )

        val intent = VisualActionIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                confidence = 0.9,
                payload = VLMPayload(
                    title = "Exam Briefing",
                    time = "2026-05-08T09:00:00",
                    location = "Teaching Building B301"
                )
            )
        )

        val quality = FrameQualitySnapshot(
            hasReadableText = true,
            sharpness = 0.82,
            exposure = 0.77,
            targetCenterX = 0.5,
            targetCenterY = 0.5,
            targetAreaRatio = 0.18
        )

        val first = coordinator.evaluate(
            frameId = 1L,
            quality = quality,
            candidateIntent = intent
        )
        val second = coordinator.evaluate(
            frameId = 2L,
            quality = quality,
            candidateIntent = intent
        )

        assertEquals(GuidanceType.READY_TO_PARSE, first.cue.type)
        assertTrue(!first.shouldAutoCapture)
        assertEquals(GuidanceType.AUTO_CAPTURE, second.cue.type)
        assertTrue(second.shouldAutoCapture)
        assertEquals(StabilizerStatus.READY_FOR_CONFIRMATION, second.stableIntentDecision?.status)
    }
}
