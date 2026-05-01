package com.vsa.visualsemanticagent.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse
import timber.log.Timber

/**
 * 模块D：Android Intent 操作系统路由引擎
 * 
 * 核心目标：安全、稳定地反序列化大模型返回的JSON结果，
 * 并将其转化为具体的系统动作，分发至Android操作系统的各项内置应用程序。
 */
class IntentDispatcher(private val context: Context) {
    
    /**
     * 根据VLMResponse分发意图
     * @param response 解析好的VLM响应
     */
    fun dispatchIntent(response: VLMResponse) {
        try {
            when (response.action) {
                ModelConstants.ACTION_CREATE_EVENT -> createCalendarEvent(response)
                ModelConstants.ACTION_NAVIGATE -> navigateToLocation(response)
                ModelConstants.ACTION_TTS_FEEDBACK -> {
                    Timber.d("TTS feedback action: ${response.answer}")
                }
                ModelConstants.ACTION_SEND_SMS -> sendSMS(response)
                else -> {
                    Timber.w("Unknown action: ${response.action}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to dispatch intent")
            throw e
        }
    }
    
    /**
     * 创建日历事件
     */
    private fun createCalendarEvent(response: VLMResponse) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = Events.CONTENT_URI
            
            // 提取并设置事件详情
            response.time?.let { timeStr ->
                try {
                    val timeMillis = timeStr.toLong()
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeMillis)
                } catch (e: NumberFormatException) {
                    Timber.e("Invalid time format: $timeStr")
                }
            }
            
            response.title?.let { 
                putExtra(Events.TITLE, it)
            }
            
            response.location?.let {
                putExtra(Events.EVENT_LOCATION, it)
            }
        }
        
        // 安全检查：验证意图的可解析性
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Timber.d("Calendar event created: ${response.title}")
        } else {
            Timber.e("No calendar app found to handle intent")
            throw ActivityNotFoundException("Calendar app not found")
        }
    }
    
    /**
     * 导航到指定位置
     */
    private fun navigateToLocation(response: VLMResponse) {
        val location = response.location ?: throw IllegalArgumentException("Missing location")
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("google.navigation:q=$location")
            setPackage("com.google.android.apps.maps")
        }
        
        // 如果Google Maps不可用，尝试通用的地图Intent
        val resolveActivity = intent.resolveActivity(context.packageManager)
        val finalIntent = if (resolveActivity != null) {
            intent
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=$location")
            }
        }
        
        if (finalIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(finalIntent)
            Timber.d("Navigation intent sent for: $location")
        } else {
            Timber.e("No map app found to handle navigation")
            throw ActivityNotFoundException("Map app not found")
        }
    }
    
    /**
     * 发送短信
     */
    private fun sendSMS(response: VLMResponse) {
        val phoneNumber = response.phoneNumber ?: throw IllegalArgumentException("Missing phone number")
        val message = response.answer ?: return
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Timber.d("SMS sent to: $phoneNumber")
        } else {
            Timber.e("No SMS app found")
            throw ActivityNotFoundException("SMS app not found")
        }
    }
}

/**
 * 自定义异常：找不到对应的应用
 */
class ActivityNotFoundException(message: String) : Exception(message)
