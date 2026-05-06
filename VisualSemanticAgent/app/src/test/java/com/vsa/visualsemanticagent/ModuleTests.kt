package com.vsa.visualsemanticagent

import com.vsa.visualsemanticagent.decision.FrameIntentObservation
import com.vsa.visualsemanticagent.decision.HospitalIntentSchema
import com.vsa.visualsemanticagent.decision.ContinuousVisionCoordinator
import com.vsa.visualsemanticagent.decision.FrameQualitySnapshot
import com.vsa.visualsemanticagent.decision.GuidanceType
import com.vsa.visualsemanticagent.decision.StabilizerStatus
import com.vsa.visualsemanticagent.decision.TemporalIntentStabilizer
import com.vsa.visualsemanticagent.model.ModelConstants
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
            title = "  demo event  ",
            answer = "  ok  ",
            phoneNumber = " 138-0013-8000 "
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals(ModelConstants.ACTION_UNKNOWN, normalized.action)
        assertEquals("demo event", normalized.title)
        assertEquals("ok", normalized.answer)
        assertEquals("13800138000", normalized.phoneNumber)
    }

    @Test
    fun normalizeResponse_normalizesInternationalPhoneNumber() {
        val raw = VLMResponse(
            action = ModelConstants.ACTION_SEND_SMS,
            phoneNumber = " +86 138-0013-8000 "
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals("+8613800138000", normalized.phoneNumber)
    }

    @Test
    fun normalizeResponse_defaultsMissingActionToUnknown() {
        val raw = VLMResponse(
            action = null,
            description = "test description"
        )

        val normalized = ResponseInterpreter.normalize(raw)

        assertEquals(ModelConstants.ACTION_UNKNOWN, normalized.action)
        assertEquals("test description", normalized.description)
    }

    @Test
    fun buildSpeechText_fallsBackToDescription() {
        val response = VLMResponse(
            action = ModelConstants.ACTION_TTS_FEEDBACK,
            description = "desk in front of the user"
        )

        val speech = ResponseInterpreter.buildSpeechText(response)

        assertEquals("desk in front of the user", speech)
    }

    @Test
    fun mockFactory_returnsCalendarActionForEventPrompt() {
        val response = MockVLMResponseFactory.buildResponse("create_event calendar reminder")

        assertEquals(ModelConstants.ACTION_CREATE_EVENT, response.action)
        assertNotNull(response.title)
    }

    @Test
    fun mockFactory_returnsNavigateActionForNavigationPrompt() {
        val response = MockVLMResponseFactory.buildResponse("please navigate there")

        assertEquals(ModelConstants.ACTION_NAVIGATE, response.action)
        assertNotNull(response.location)
    }

    @Test
    fun mockFactory_returnsSmsActionForSmsPrompt() {
        val response = MockVLMResponseFactory.buildResponse("send_sms reminder")

        assertEquals(ModelConstants.ACTION_SEND_SMS, response.action)
        assertNotNull(response.phoneNumber)
    }

    @Test
    fun hospitalIntentSchema_marksHighRiskActionsAsConfirmationRequired() {
        val response = VLMResponse(
            action = ModelConstants.ACTION_CREATE_EVENT,
            title = "Outpatient follow-up",
            time = "2026-05-08 09:00",
            location = "Building 3 Internal Medicine"
        )

        val intent = HospitalIntentSchema.fromResponse(response, confidence = 0.91)

        assertEquals("hospital_outpatient_assist", intent.scene)
        assertTrue(intent.requiresConfirmation)
        assertEquals(ModelConstants.ACTION_CREATE_EVENT, intent.action)
        assertEquals(0.91, intent.confidence, 0.0001)
    }

    @Test
    fun temporalIntentStabilizer_requiresConsistentFramesBeforeReady() {
        val stabilizer = TemporalIntentStabilizer(
            requiredConsistentFrames = 3,
            minConfidence = 0.8,
            maxWindowSize = 5
        )

        val intent = HospitalIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_NAVIGATE,
                location = "Building 3 Internal Medicine"
            ),
            confidence = 0.88
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
    fun temporalIntentStabilizer_rejectsLowConfidenceLatestFrame() {
        val stabilizer = TemporalIntentStabilizer(
            requiredConsistentFrames = 2,
            minConfidence = 0.8,
            maxWindowSize = 4
        )

        val strongIntent = HospitalIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                answer = "registration desk is ahead"
            ),
            confidence = 0.86
        )
        val weakIntent = HospitalIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_TTS_FEEDBACK,
                answer = "registration desk is ahead"
            ),
            confidence = 0.62
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

        val intent = HospitalIntentSchema.fromResponse(
            VLMResponse(
                action = ModelConstants.ACTION_CREATE_EVENT,
                title = "Medical examination",
                time = "2026-05-08 09:00",
                location = "Imaging Department"
            ),
            confidence = 0.9
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
