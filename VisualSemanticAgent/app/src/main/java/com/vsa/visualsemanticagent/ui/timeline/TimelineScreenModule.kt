package com.vsa.visualsemanticagent.ui.timeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vsa.visualsemanticagent.decision.ExecutableIntent
import com.vsa.visualsemanticagent.decision.ExecutionSuggestion
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.AgendaDayBucket
import com.vsa.visualsemanticagent.plan.agendaMonthMatrix
import com.vsa.visualsemanticagent.plan.agendaMonthTitle
import com.vsa.visualsemanticagent.plan.agendaWeekWindow
import com.vsa.visualsemanticagent.plan.displayTimeLabel
import com.vsa.visualsemanticagent.plan.exportTimeLabel
import com.vsa.visualsemanticagent.plan.groupAgendasByDay
import com.vsa.visualsemanticagent.plan.reminderSummary
import com.vsa.visualsemanticagent.plan.scheduleDateTime
import com.vsa.visualsemanticagent.ui.AppColors
import com.vsa.visualsemanticagent.ui.AppShapes
import com.vsa.visualsemanticagent.ui.AppSpacing
import com.vsa.visualsemanticagent.ui.AppTypography
import com.vsa.visualsemanticagent.ui.common.EmptyStateCard
import com.vsa.visualsemanticagent.ui.common.WeavingBackground
import com.vsa.visualsemanticagent.ui.common.WeavingChip
import com.vsa.visualsemanticagent.ui.common.WeavingGlassCard
import com.vsa.visualsemanticagent.ui.common.WeavingPrimaryButton
import com.vsa.visualsemanticagent.ui.common.WeavingSectionTitle
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun TimelineScreenModule(
    modifier: Modifier = Modifier,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    agendaItems: List<AgendaCardData>,
    selectedPlanMode: String,
    selectedPlanDate: LocalDate?,
    calendarPreviewMonth: YearMonth,
    nextReminderText: String,
    scheduledReminderCount: Int,
    confirmedAgendaCount: Int,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val groupedItems = remember(agendaItems) { groupAgendasByDay(agendaItems) }
    var activeMode by remember(selectedPlanMode) { mutableStateOf(selectedPlanMode.ifBlank { "week" }) }
    var activeDate by remember(selectedPlanDate, agendaItems) {
        mutableStateOf(selectedPlanDate ?: groupedItems.firstOrNull()?.date ?: LocalDate.now())
    }
    var activeMonth by remember(calendarPreviewMonth) { mutableStateOf(calendarPreviewMonth) }
    var detailItem by remember { mutableStateOf<AgendaCardData?>(null) }
    val weekDates = remember(activeDate) { agendaWeekWindow(activeDate) }
    val visibleBuckets = remember(groupedItems, activeMode, activeDate, weekDates, activeMonth) {
        when (activeMode) {
            "day" -> groupedItems.filter { it.date == activeDate }
            "month" -> groupedItems.filter { it.date.year == activeMonth.year && it.date.month == activeMonth.month }
            else -> groupedItems.filter { it.date in weekDates }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        WeavingBackground()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(
                start = AppSpacing.lg,
                end = AppSpacing.lg,
                top = AppSpacing.lg,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter)
        ) {
            item {
                WeavingSectionTitle(
                    title = "Timeline",
                    subtitle = "把杂乱校园信息沉淀成一条真正可回看的专属时间线",
                    action = "导出",
                    onActionClick = { exportPlanSnapshot(context, activeMode, visibleBuckets, "PDF") }
                )
            }

            item {
                HeroTimelineCard(
                    confirmedAgendaCount = confirmedAgendaCount,
                    scheduledReminderCount = scheduledReminderCount,
                    nextReminderText = nextReminderText
                )
            }

            item {
                ModeSwitchBar(
                    activeMode = activeMode,
                    onModeSelected = { activeMode = it },
                    onExport = { exportPlanSnapshot(context, activeMode, visibleBuckets, it) }
                )
            }

            if (activeMode == "month") {
                item {
                    MonthCard(
                        month = activeMonth,
                        selectedDate = activeDate,
                        buckets = groupedItems,
                        onPrev = { activeMonth = activeMonth.minusMonths(1) },
                        onNext = { activeMonth = activeMonth.plusMonths(1) },
                        onDateSelected = {
                            activeDate = it
                            activeMode = "day"
                        }
                    )
                }
            }

            if (showConfirmationCard && confirmationIntent != null && confirmationSuggestion != null) {
                item {
                    WeavingGlassCard(containerColor = AppColors.SurfaceContainer) {
                        Text(
                            text = "还有一条待你织入时间线的安排",
                            style = AppTypography.HeadlineMedium,
                            color = AppColors.Primary
                        )
                        Text(
                            text = confirmationSuggestion.prompt,
                            style = AppTypography.BodyMedium,
                            color = AppColors.OnSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                            WeavingPrimaryButton(
                                text = "确认写入",
                                onClick = onConfirmExecution,
                                modifier = Modifier.weight(1f)
                            )
                            WeavingPrimaryButton(
                                text = "返回首页",
                                onClick = onGoHome,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (visibleBuckets.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "这段时间还没有沉淀安排",
                        summary = "从首页导入海报、截图、群通知或语音后，确认过的事件会按时间顺序织入这里。"
                    )
                }
            } else {
                items(visibleBuckets, key = { it.date.toString() }) { bucket ->
                    TimelineDayGroup(
                        bucket = bucket,
                        onItemClick = { detailItem = it }
                    )
                }
            }
        }
    }

    detailItem?.let {
        TimelineDetailCard(
            item = it,
            onDismiss = { detailItem = null }
        )
    }
}

@Composable
private fun HeroTimelineCard(
    confirmedAgendaCount: Int,
    scheduledReminderCount: Int,
    nextReminderText: String
) {
    WeavingGlassCard(containerColor = AppColors.SurfaceContainerHigh.copy(alpha = 0.92f)) {
        Text(
            text = "本周已将 $confirmedAgendaCount 个碎片织入生活",
            style = AppTypography.HeadlineMedium,
            color = AppColors.Primary
        )
        Text(
            text = if (nextReminderText.isBlank()) "已关联 $scheduledReminderCount 条提醒策略，随时准备接住下一条校园通知。" else nextReminderText,
            style = AppTypography.BodyMedium,
            color = AppColors.OnSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    color = Color.White.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(AppShapes.Full)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(14.dp)
                    .background(
                        color = AppColors.Primary,
                        shape = RoundedCornerShape(AppShapes.Full)
                    )
            )
        }
    }
}

@Composable
private fun ModeSwitchBar(
    activeMode: String,
    onModeSelected: (String) -> Unit,
    onExport: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = AppColors.SurfaceContainerLowest,
                    shape = RoundedCornerShape(AppShapes.Full)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("day" to "Day", "week" to "Week", "month" to "Month").forEach { (key, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (activeMode == key) AppColors.Primary else Color.Transparent,
                            shape = RoundedCornerShape(AppShapes.Full)
                        )
                        .clickable { onModeSelected(key) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = AppTypography.BodyLarge,
                        color = if (activeMode == key) AppColors.OnPrimary else AppColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .background(AppColors.CoralSoft, RoundedCornerShape(AppShapes.Full))
                .clickable { onExport("PDF") }
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = AppColors.Primary
                )
                Text(
                    text = "Export",
                    style = AppTypography.LabelMedium,
                    color = AppColors.Primary
                )
            }
        }
    }
}

@Composable
private fun MonthCard(
    month: YearMonth,
    selectedDate: LocalDate,
    buckets: List<AgendaDayBucket>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val matrix = remember(month) { agendaMonthMatrix(month) }
    WeavingGlassCard(containerColor = AppColors.SurfaceContainerLowest) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = agendaMonthTitle(month),
                style = AppTypography.HeadlineLargeMobile,
                color = AppColors.Primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .background(AppColors.SurfaceContainer, RoundedCornerShape(AppShapes.Full))
                        .clickable { onPrev() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) { Text("←", style = AppTypography.BodyLarge, color = AppColors.Primary) }
                Box(
                    modifier = Modifier
                        .background(AppColors.SurfaceContainer, RoundedCornerShape(AppShapes.Full))
                        .clickable { onNext() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) { Text("→", style = AppTypography.BodyLarge, color = AppColors.Primary) }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    text = it,
                    style = AppTypography.LabelMedium,
                    color = AppColors.OnSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        matrix.chunked(7).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { date ->
                    val isSelected = date == selectedDate
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = if (isSelected) AppColors.Primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = date != null) { date?.let(onDateSelected) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString().orEmpty(),
                            style = AppTypography.BodyLarge,
                            color = if (isSelected) AppColors.OnPrimary else AppColors.OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
private fun TimelineDayGroup(
    bucket: AgendaDayBucket,
    onItemClick: (AgendaCardData) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            text = bucket.date.toString(),
            style = AppTypography.LabelMedium,
            color = AppColors.OnSurfaceVariant
        )
        bucket.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.displayTimeLabel(),
                        style = AppTypography.LabelMedium,
                        color = AppColors.Primary
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(if (index == bucket.items.lastIndex) 32.dp else 120.dp)
                            .background(AppColors.SurfaceContainerHighest, RoundedCornerShape(AppShapes.Full))
                    )
                }
                WeavingGlassCard(
                    modifier = Modifier.weight(1f),
                    containerColor = when (index % 3) {
                        0 -> AppColors.SurfaceContainer
                        1 -> AppColors.CoralSoft
                        else -> AppColors.GoldSoft
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeavingChip(
                            text = item.sourceLabel.ifBlank { "Campus" },
                            background = Color.White.copy(alpha = 0.45f)
                        )
                        Text(
                            text = "···",
                            style = AppTypography.BodyLarge,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                    Text(
                        text = item.title,
                        style = AppTypography.HeadlineMedium,
                        color = AppColors.OnSurface
                    )
                    Text(
                        text = item.summary.ifBlank { item.location },
                        style = AppTypography.BodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                    if (item.reminders.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(AppShapes.Full)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .height(10.dp)
                                    .background(
                                        color = AppColors.SecondaryContainer,
                                        shape = RoundedCornerShape(AppShapes.Full)
                                    )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.location,
                            style = AppTypography.BodyMedium,
                            color = AppColors.OnSurfaceVariant
                        )
                        Text(
                            text = "查看详情",
                            style = AppTypography.LabelMedium,
                            color = AppColors.Primary,
                            modifier = Modifier.clickable { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineDetailCard(
    item: AgendaCardData,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable { onDismiss() }
            .padding(AppSpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        WeavingGlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = AppColors.SurfaceContainer
        ) {
            Text(
                text = item.title,
                style = AppTypography.DisplayLarge,
                color = AppColors.Primary
            )
            Text(
                text = item.summary,
                style = AppTypography.BodyLarge,
                color = AppColors.OnSurfaceVariant
            )
            DetailRow("时间", item.exportTimeLabel(), Icons.Rounded.CalendarMonth)
            DetailRow("地点", item.location, Icons.Rounded.LocationOn)
            DetailRow("提醒", item.reminderSummary(), Icons.Rounded.NotificationsActive)
            DetailRow("来源", item.sourceLabel.ifBlank { "校园通知导入" }, Icons.Rounded.Timeline)
            WeavingPrimaryButton(
                text = "关闭详情",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(AppShapes.Medium))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(AppColors.GoldSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AppColors.Primary)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = AppTypography.LabelMedium, color = AppColors.OnSurfaceVariant)
            Text(value, style = AppTypography.BodyLarge, color = AppColors.OnSurface)
        }
    }
}

private fun exportPlanSnapshot(
    context: Context,
    mode: String,
    buckets: List<AgendaDayBucket>,
    format: String
) {
    runCatching {
        val targetDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "exports"
        ).apply { mkdirs() }

        val text = buildString {
            appendLine("织时时间线导出")
            appendLine("模式: $mode")
            appendLine()
            buckets.forEach { bucket ->
                appendLine(bucket.date.toString())
                bucket.items.forEach { item ->
                    appendLine("- ${item.exportTimeLabel()} | ${item.title} | ${item.location} | ${item.reminderSummary()}")
                }
                appendLine()
            }
        }

        val file = when (format.uppercase()) {
            "PDF" -> exportPdf(targetDir, text)
            "JPG", "PNG" -> exportBitmap(targetDir, text, format.uppercase())
            else -> exportBitmap(targetDir, text, "PNG")
        }
        Toast.makeText(context, "已导出到 ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }.onFailure {
        Toast.makeText(context, "导出失败: ${it.message}", Toast.LENGTH_LONG).show()
    }
}

private fun exportPdf(dir: File, content: String): File {
    val file = File(dir, "timeline-${System.currentTimeMillis()}.pdf")
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(1240, 1754, 1).create()
    val page = document.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint().apply {
        color = AppColors.OnSurface.toArgb()
        textSize = 34f
        isAntiAlias = true
    }
    var y = 100f
    content.lines().forEach { line ->
        canvas.drawText(line, 80f, y, paint)
        y += 48f
    }
    document.finishPage(page)
    FileOutputStream(file).use(document::writeTo)
    document.close()
    return file
}

private fun exportBitmap(dir: File, content: String, format: String): File {
    val bitmap = Bitmap.createBitmap(1600, 2200, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AppColors.Background.toArgb())
    val paint = Paint().apply {
        color = AppColors.OnSurface.toArgb()
        textSize = 38f
        isAntiAlias = true
    }
    var y = 120f
    content.lines().forEach { line ->
        canvas.drawText(line, 90f, y, paint)
        y += 54f
    }
    val file = File(dir, "timeline-${System.currentTimeMillis()}.${format.lowercase()}")
    FileOutputStream(file).use { stream ->
        bitmap.compress(
            if (format == "JPG") Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG,
            96,
            stream
        )
    }
    return file
}
