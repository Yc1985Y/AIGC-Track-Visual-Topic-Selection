package com.vsa.visualsemanticagent.model

import com.google.gson.annotations.SerializedName

data class VLMResponse(
    @SerializedName("action")
    val action: String? = null,
    @SerializedName("confidence")
    val confidence: Double? = null,
    @SerializedName("payload")
    val payload: VLMPayload? = null,
    @SerializedName("fallback_query")
    val fallbackQuery: String? = null,
    @SerializedName("target_found")
    val targetFound: Boolean? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("answer")
    val answer: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
)

data class VLMPayload(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("answer")
    val answer: String? = null,
)

object ModelConstants {
    const val ACTION_CREATE_EVENT = "create_event"
    const val ACTION_NAVIGATE = "navigate"
    const val ACTION_TTS_FEEDBACK = "tts_feedback"
    const val ACTION_SEND_SMS = "send_sms"
    const val ACTION_CLARIFICATION = "clarification"
    const val ACTION_UNKNOWN = "unknown"
}
