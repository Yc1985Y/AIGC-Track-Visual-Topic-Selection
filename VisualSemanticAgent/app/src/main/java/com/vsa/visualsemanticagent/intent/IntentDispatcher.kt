package com.vsa.visualsemanticagent.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import com.vsa.visualsemanticagent.model.ModelConstants
import com.vsa.visualsemanticagent.model.VLMResponse
import timber.log.Timber
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.net.URLEncoder

/**
 * 模块D：Android Intent 操作系统路由引擎
 * 
 * 核心目标：安全、稳定地反序列化大模型返回的JSON结果，
 * 并将其转化为具体的系统动作，分发至Android操作系统的各项内置应用程序。
 */
class IntentDispatcher(private val context: Context) {

    data class DispatchResult(
        val launchedIntent: Boolean,
        val summary: String
    )
    
    /**
     * 根据VLMResponse分发意图
     * @param response 解析好的VLM响应
     */
    fun dispatchIntent(response: VLMResponse): DispatchResult {
        try {
            return when (response.action) {
                ModelConstants.ACTION_CREATE_EVENT -> createCalendarEvent(response)
                ModelConstants.ACTION_NAVIGATE -> navigateToLocation(response)
                ModelConstants.ACTION_TTS_FEEDBACK -> {
                    Timber.d("TTS feedback action: ${response.answer}")
                    DispatchResult(
                        launchedIntent = false,
                        summary = response.answer ?: response.description ?: "已生成语义反馈。"
                    )
                }
                ModelConstants.ACTION_SEND_SMS -> sendSMS(response)
                else -> {
                    Timber.w("Unknown action: ${response.action}")
                    DispatchResult(
                        launchedIntent = false,
                        summary = response.answer ?: response.description ?: "模型未返回明确动作。"
                    )
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
    private fun createCalendarEvent(response: VLMResponse): DispatchResult {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = Events.CONTENT_URI
            
            // 提取并设置事件详情
            response.time?.let { timeStr ->
                parseEventTimeMillis(timeStr)?.let { timeMillis ->
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeMillis)
                }
            }
            
            response.title?.let { 
                putExtra(Events.TITLE, it)
            }
            
            response.location?.let {
                putExtra(Events.EVENT_LOCATION, it)
            }

            response.description?.let {
                putExtra(Events.DESCRIPTION, it)
            }
        }
        
        // 安全检查：验证意图的可解析性
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Timber.d("Calendar event created: ${response.title}")
            return DispatchResult(
                launchedIntent = true,
                summary = response.title?.let { "已识别活动：$it，正在打开日历。" } ?: "正在打开日历。"
            )
        } else {
            Timber.e("No calendar app found to handle intent")
            throw ActivityNotFoundException("Calendar app not found")
        }
    }
    
    /**
     * 导航到指定位置
     */
    private fun navigateToLocation(response: VLMResponse): DispatchResult {
        val location = response.location ?: throw IllegalArgumentException("Missing location")
        val encodedLocation = URLEncoder.encode(location, Charsets.UTF_8.name())
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("google.navigation:q=$encodedLocation")
            setPackage("com.google.android.apps.maps")
        }
        
        // 如果Google Maps不可用，尝试通用的地图Intent
        val resolveActivity = intent.resolveActivity(context.packageManager)
        val finalIntent = if (resolveActivity != null) {
            intent
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=$encodedLocation")
            }
        }
        
        if (finalIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(finalIntent)
            Timber.d("Navigation intent sent for: $location")
            return DispatchResult(
                launchedIntent = true,
                summary = "已识别地点：$location，正在打开地图导航。"
            )
        } else {
            Timber.e("No map app found to handle navigation")
            throw ActivityNotFoundException("Map app not found")
        }
    }
    
    /**
     * 发送短信
     */
    private fun sendSMS(response: VLMResponse): DispatchResult {
        val phoneNumber = response.phoneNumber ?: throw IllegalArgumentException("Missing phone number")
        val message = (response.answer ?: response.description)
            ?: throw IllegalArgumentException("Missing sms content")
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            putExtra("sms_body", message)
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Timber.d("SMS sent to: $phoneNumber")
            return DispatchResult(
                launchedIntent = true,
                summary = "已准备给 $phoneNumber 的短信。"
            )
        } else {
            Timber.e("No SMS app found")
            throw ActivityNotFoundException("SMS app not found")
        }
    }

    private fun parseEventTimeMillis(timeStr: String): Long? {
        val trimmed = timeStr.trim()
        if (trimmed.isEmpty()) return null

        trimmed.toLongOrNull()?.let { value ->
            return if (value < 1_000_000_000_000L) value * 1000 else value
        }

        parseIsoDateTime(trimmed)?.let { return it }

        val patterns = listOf(
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm",
            "yyyy.MM.dd HH:mm",
            "yyyy年M月d日 HH:mm",
            "yyyy年M月d日 H:mm",
            "M月d日 H:mm",
            "M月d日 HH:mm",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "yyyy年M月d日",
            "yyyy-MM-dd"
        )

        patterns.forEach { pattern ->
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern, Locale.CHINA)
                val localDateTime = if (pattern.contains("HH") || pattern.contains("H:mm")) {
                    if (pattern.startsWith("M月")) {
                        val currentYear = LocalDateTime.now().year
                        val normalizedTime = "${currentYear}年$trimmed"
                        val normalizedPattern = if (pattern == "M月d日 H:mm") {
                            "yyyy年M月d日 H:mm"
                        } else {
                            "yyyy年M月d日 HH:mm"
                        }
                        LocalDateTime.parse(normalizedTime, DateTimeFormatter.ofPattern(normalizedPattern, Locale.CHINA))
                    } else {
                        LocalDateTime.parse(trimmed, formatter)
                    }
                } else {
                    return parseDateOnly(trimmed, pattern)
                }
                return localDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } catch (_: DateTimeParseException) {
                Unit
            }
        }

        Timber.w("Invalid time format: $timeStr")
        return null
    }

    private fun parseDateOnly(value: String, pattern: String): Long {
        val localDate = LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
        return localDate
            .atTime(9, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun parseIsoDateTime(value: String): Long? {
        return try {
            OffsetDateTime.parse(value)
                .toInstant()
                .toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}

/**
 * 自定义异常：找不到对应的应用
 */
class ActivityNotFoundException(message: String) : Exception(message)
