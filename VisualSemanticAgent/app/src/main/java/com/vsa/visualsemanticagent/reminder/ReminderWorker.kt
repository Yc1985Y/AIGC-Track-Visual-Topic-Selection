package com.vsa.visualsemanticagent.reminder

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vsa.visualsemanticagent.MainActivity
import com.vsa.visualsemanticagent.R

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val scheduler = ReminderScheduler(applicationContext)
        scheduler.ensureNotificationChannel()
        if (!scheduler.notificationsGranted()) {
            return Result.success()
        }

        val title = inputData.getString(KEY_TITLE).orEmpty().ifBlank { "新的校园安排" }
        val location = inputData.getString(KEY_LOCATION).orEmpty()
        val summary = inputData.getString(KEY_SUMMARY).orEmpty()
        val eventTime = inputData.getString(KEY_EVENT_TIME).orEmpty()
        val reminderLabel = inputData.getString(KEY_REMINDER_LABEL).orEmpty()
        val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 0)

        val content = buildString {
            append(reminderLabel.ifBlank { "即将开始" })
            if (eventTime.isNotBlank()) {
                append(" · ")
                append(eventTime)
            }
            if (location.isNotBlank()) {
                append(" · ")
                append(location)
            }
            if (summary.isNotBlank()) {
                append("\n")
                append(summary)
            }
        }

        val pendingIntent = androidx.core.app.TaskStackBuilder.create(applicationContext)
            .addNextIntentWithParentStack(
                Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_tab", "plan")
                }
            )
            .getPendingIntent(
                minutesBefore,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content.lineSequence().firstOrNull().orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify((title + minutesBefore + location).hashCode(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_AGENDA_ID = "agenda_id"
        const val KEY_TITLE = "title"
        const val KEY_LOCATION = "location"
        const val KEY_SUMMARY = "summary"
        const val KEY_EVENT_TIME = "event_time"
        const val KEY_REMINDER_LABEL = "reminder_label"
        const val KEY_MINUTES_BEFORE = "minutes_before"
    }
}
