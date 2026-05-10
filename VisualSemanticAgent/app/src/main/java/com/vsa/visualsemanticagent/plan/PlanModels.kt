package com.vsa.visualsemanticagent.plan

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class AgendaReminderData(
    val label: String,
    val minutesBefore: Int
)

data class AgendaCardData(
    val id: String,
    val title: String,
    val summary: String,
    val time: String,
    val location: String,
    val status: String,
    val isoDateTime: String? = null,
    val sourceLabel: String = "",
    val action: String = "create_event",
    val reminders: List<AgendaReminderData> = defaultAgendaReminders()
)

enum class PlanViewMode {
    DAY,
    WEEK,
    MONTH
}

fun defaultAgendaReminders(): List<AgendaReminderData> {
    return listOf(
        AgendaReminderData(label = "提前1天", minutesBefore = 24 * 60),
        AgendaReminderData(label = "提前1小时", minutesBefore = 60)
    )
}

fun AgendaCardData.scheduleDateTime(): LocalDateTime? {
    val candidate = isoDateTime?.trim().orEmpty()
    if (candidate.isNotBlank()) {
        runCatching { return LocalDateTime.parse(candidate) }
    }

    return parseFallbackDateTime(time)
}

fun AgendaCardData.scheduleDate(): LocalDate? = scheduleDateTime()?.toLocalDate()

fun AgendaCardData.reminderSummary(): String {
    return reminders.joinToString(separator = " / ") { it.label }
}

fun AgendaCardData.displayDateLabel(): String {
    val parsed = scheduleDateTime() ?: return time
    return parsed.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
}

fun AgendaCardData.displayTimeLabel(): String {
    val parsed = scheduleDateTime() ?: return time
    return parsed.format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
}

fun AgendaCardData.exportTimeLabel(): String {
    val parsed = scheduleDateTime() ?: return time
    return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA))
}

fun agendaMonthMatrix(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val firstOffset = (firstDay.dayOfWeek.value + 6) % 7
    val totalDays = month.lengthOfMonth()
    val cells = mutableListOf<LocalDate?>()

    repeat(firstOffset) { cells += null }
    for (day in 1..totalDays) {
        cells += month.atDay(day)
    }
    while (cells.size % 7 != 0) {
        cells += null
    }
    return cells
}

data class AgendaDayBucket(
    val date: LocalDate,
    val items: List<AgendaCardData>
)

fun groupAgendasByDay(items: List<AgendaCardData>): List<AgendaDayBucket> {
    return items
        .mapNotNull { item -> item.scheduleDate()?.let { it to item } }
        .groupBy({ it.first }, { it.second })
        .toSortedMap()
        .map { (date, dayItems) ->
            AgendaDayBucket(
                date = date,
                items = dayItems.sortedBy { it.scheduleDateTime() }
            )
        }
}

fun agendaWeekWindow(anchor: LocalDate = LocalDate.now()): List<LocalDate> {
    val start = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
    return (0..6).map { start.plusDays(it.toLong()) }
}

fun agendaMonthTitle(month: YearMonth): String {
    return month.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA))
}

fun agendaDayTitle(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
}

fun agendaShortDayTitle(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))
}

private fun parseFallbackDateTime(raw: String): LocalDateTime? {
    val normalized = raw.trim()
    if (normalized.isBlank()) return null

    val patterns = listOf(
        "yyyy-MM-dd HH:mm",
        "yyyy/M/d HH:mm",
        "M月d日 HH:mm",
        "M月d日 H:mm",
        "M月d日 HH点mm分",
        "M月d日 H点m分"
    )

    for (pattern in patterns) {
        try {
            return when {
                pattern.startsWith("yyyy") -> {
                    LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
                }

                else -> {
                    LocalDateTime.parse(
                        "${LocalDate.now().year}年$normalized".replace("/", "-"),
                        DateTimeFormatter.ofPattern("yyyy年$pattern", Locale.CHINA)
                    )
                }
            }
        } catch (_: Exception) {
            // Keep trying other patterns.
        }
    }

    return tryParseMonthDayFallback(normalized)
}

private fun tryParseMonthDayFallback(raw: String): LocalDateTime? {
    val monthDayPattern = Regex("""(\d{1,2})月(\d{1,2})日(?:\s*(\d{1,2})[:点时](\d{1,2}))?""")
    val match = monthDayPattern.find(raw) ?: return null
    val month = match.groupValues[1].toIntOrNull() ?: return null
    val day = match.groupValues[2].toIntOrNull() ?: return null
    val hour = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 9
    val minute = match.groupValues.getOrNull(4)?.toIntOrNull() ?: 0

    return try {
        LocalDateTime.of(LocalDate.of(LocalDate.now().year, month, day), LocalTime.of(hour, minute))
    } catch (_: DateTimeParseException) {
        null
    } catch (_: Exception) {
        null
    }
}
