package com.vsa.visualsemanticagent.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.AgendaReminderData
import com.vsa.visualsemanticagent.plan.scheduleDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun notificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        }
    }

    fun scheduleAgendaReminders(agenda: AgendaCardData): Int {
        ensureNotificationChannel()
        val eventTime = agenda.scheduleDateTime() ?: return 0
        var scheduledCount = 0
        agenda.reminders.forEach { reminder ->
            if (scheduleReminder(agenda, reminder, eventTime)) {
                scheduledCount += 1
            }
        }
        return scheduledCount
    }

    fun cancelAgendaReminders(agendaId: String, reminders: List<AgendaReminderData> = emptyList()) {
        val reminderMinutes = if (reminders.isEmpty()) DEFAULT_REMINDER_MINUTES else reminders.map { it.minutesBefore }
        reminderMinutes.forEach { minutes ->
            workManager.cancelUniqueWork(uniqueWorkName(agendaId, minutes))
        }
    }

    fun pendingReminderCount(agendas: List<AgendaCardData>, now: LocalDateTime = LocalDateTime.now()): Int {
        return agendas.sumOf { agenda ->
            val eventTime = agenda.scheduleDateTime() ?: return@sumOf 0
            agenda.reminders.count { reminder ->
                eventTime.minusMinutes(reminder.minutesBefore.toLong()).isAfter(now)
            }
        }
    }

    fun nextReminderSummary(agendas: List<AgendaCardData>, now: LocalDateTime = LocalDateTime.now()): String {
        val next = agendas
            .flatMap { agenda ->
                val eventTime = agenda.scheduleDateTime() ?: return@flatMap emptyList()
                agenda.reminders.mapNotNull { reminder ->
                    val triggerAt = eventTime.minusMinutes(reminder.minutesBefore.toLong())
                    if (triggerAt.isAfter(now)) {
                        ReminderPreview(agenda = agenda, reminder = reminder, triggerAt = triggerAt)
                    } else {
                        null
                    }
                }
            }
            .minByOrNull { it.triggerAt }
            ?: return "暂无即将触发的本地提醒"

        val minutes = Duration.between(now, next.triggerAt).toMinutes().coerceAtLeast(0)
        return when {
            minutes < 60 -> "${minutes} 分钟后提醒 ${next.agenda.title}"
            minutes < 24 * 60 -> "${minutes / 60} 小时后提醒 ${next.agenda.title}"
            else -> "${minutes / (24 * 60)} 天后提醒 ${next.agenda.title}"
        }
    }

    private fun scheduleReminder(
        agenda: AgendaCardData,
        reminder: AgendaReminderData,
        eventTime: LocalDateTime
    ): Boolean {
        val triggerAt = eventTime.minusMinutes(reminder.minutesBefore.toLong())
        val delay = Duration.between(LocalDateTime.now(), triggerAt)
        if (delay.isNegative || delay.isZero) return false

        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_AGENDA_ID, agenda.id)
            .putString(ReminderWorker.KEY_TITLE, agenda.title)
            .putString(ReminderWorker.KEY_LOCATION, agenda.location)
            .putString(ReminderWorker.KEY_SUMMARY, agenda.summary)
            .putString(ReminderWorker.KEY_EVENT_TIME, agenda.time)
            .putString(ReminderWorker.KEY_REMINDER_LABEL, reminder.label)
            .putInt(ReminderWorker.KEY_MINUTES_BEFORE, reminder.minutesBefore)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(TAG_REMINDER)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(agenda.id, reminder.minutesBefore),
            ExistingWorkPolicy.REPLACE,
            request
        )
        return true
    }

    private fun uniqueWorkName(agendaId: String, minutesBefore: Int): String {
        return "agenda-reminder-$agendaId-$minutesBefore"
    }

    data class ReminderPreview(
        val agenda: AgendaCardData,
        val reminder: AgendaReminderData,
        val triggerAt: LocalDateTime
    )

    companion object {
        const val CHANNEL_ID = "campus_schedule_reminders"
        private const val CHANNEL_NAME = "织时校园提醒"
        private const val CHANNEL_DESCRIPTION = "织时为讲座、考试、班会等安排生成的本地提醒"
        private const val TAG_REMINDER = "campus_schedule_reminder"
        private val DEFAULT_REMINDER_MINUTES = listOf(24 * 60, 60)
    }
}
