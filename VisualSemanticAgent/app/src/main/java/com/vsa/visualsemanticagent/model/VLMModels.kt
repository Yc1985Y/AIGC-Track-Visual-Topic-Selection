package com.vsa.visualsemanticagent.model

import com.google.gson.annotations.SerializedName

/**
 * 结构化JSON响应数据模型
 * 所有来自云端大模型的响应都必须被反序列化为该类型
 */
data class VLMResponse(
    @SerializedName("action")
    val action: String? = null, // 意图路由分类标签
    
    @SerializedName("title")
    val title: String? = null, // 提取的事件/信息标题
    
    @SerializedName("time")
    val time: String? = null, // Unix时间戳
    
    @SerializedName("location")
    val location: String? = null, // 地理位置信息
    
    @SerializedName("answer")
    val answer: String? = null, // 自然语言反馈文本
    
    @SerializedName("target_found")
    val targetFound: Boolean = false, // 目标检测状态
    
    @SerializedName("description")
    val description: String? = null, // 物体/场景描述

    @SerializedName("phone_number")
    val phoneNumber: String? = null,
)

/**
 * 模型配置常量
 */
object ModelConstants {
    const val ACTION_CREATE_EVENT = "create_event"
    const val ACTION_NAVIGATE = "navigate"
    const val ACTION_TTS_FEEDBACK = "tts_feedback"
    const val ACTION_SEND_SMS = "send_sms"
    const val ACTION_UNKNOWN = "unknown"
}
