package com.vsa.visualsemanticagent.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.AgendaDayBucket
import com.vsa.visualsemanticagent.plan.agendaDayTitle
import com.vsa.visualsemanticagent.plan.agendaMonthTitle
import com.vsa.visualsemanticagent.plan.agendaShortDayTitle
import com.vsa.visualsemanticagent.plan.displayTimeLabel
import com.vsa.visualsemanticagent.plan.exportTimeLabel
import com.vsa.visualsemanticagent.plan.reminderSummary
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun StartupIntroScreen(
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        TimeWeaverDiffuseBackdrop(dark = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 34.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(26.dp)
            ) {
                GradientBrandText(
                    text = "织时",
                    fontSize = 20.sp,
                    letterSpacing = 3.sp
                )

                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val center = center
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8ED4FF).copy(alpha = 0.85f),
                                    Color(0xFF5E94FF).copy(alpha = 0.46f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension * 0.44f
                            ),
                            radius = size.minDimension * 0.38f,
                            center = center
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Campus Timeline Agent",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        Text(
                            text = "将校园信息碎片\n整合为专属时间线",
                            fontSize = 29.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White,
                                        Color(0xFFD8ECFF),
                                        Color(0xFFFFE0A8)
                                    )
                                ),
                                fontSize = 29.sp,
                                lineHeight = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Text(
                    text = "织时会把海报、群通知、截图与口头指令整理成可确认、可提醒、可回看的校园时间线。",
                    color = Color(0xFFCFDBE8),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "织时 · 让校园生活更有序",
                    color = Color(0xFF3B4A3F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                TimeWeaverButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = "开始使用",
                    onClick = onSkip,
                    background = TimeWeaverBlue,
                    contentColor = Color.White,
                    cornerRadius = 999.dp,
                    minHeight = 56.dp,
                    textSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun MonthCalendarCard(
    month: YearMonth,
    matrix: List<LocalDate?>,
    dayBuckets: List<AgendaDayBucket>,
    selectedDate: LocalDate?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val bucketMap = remember(dayBuckets) { dayBuckets.associateBy { it.date } }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = "月历",
                        style = AppTypography.HeadlineMedium,
                        color = AppColors.OnSurface
                    )
                    Text(
                        text  = agendaMonthTitle(month),
                        style = AppTypography.LabelSmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = null, tint = AppColors.OnSurfaceVariant)
                    }
                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null, tint = AppColors.OnSurfaceVariant)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                    Text(
                        text      = label,
                        style     = AppTypography.LabelSmall,
                        color     = AppColors.Outline,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                matrix.chunked(7).forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { date ->
                            val bucket   = date?.let { bucketMap[it] }
                            val selected = date != null && date == selectedDate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(AppShapes.Medium))
                                    .background(
                                        when {
                                            selected   -> AppColors.PrimaryContainer
                                            bucket != null -> AppColors.PrimaryFixed.copy(alpha = 0.55f)
                                            else       -> AppColors.SurfaceContainerLow
                                        }
                                    )
                                    .clickable(enabled = date != null) { date?.let(onDateSelected) }
                                    .padding(8.dp)
                            ) {
                                Column(
                                    modifier            = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text  = date?.dayOfMonth?.toString().orEmpty(),
                                        style = AppTypography.LabelMedium,
                                        color = when {
                                            selected   -> AppColors.OnPrimary
                                            date != null -> AppColors.OnSurface
                                            else       -> AppColors.Outline
                                        }
                                    )
                                    if (bucket != null) {
                                        Text(
                                            text     = if (bucket.items.size == 1) bucket.items.first().title.take(5) else "${bucket.items.size} 项",
                                            style    = AppTypography.LabelSmall,
                                            color    = if (selected) AppColors.OnPrimary.copy(alpha = 0.80f) else AppColors.Primary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekAgendaStrip(
    weekDates: List<LocalDate>,
    dayBuckets: List<AgendaDayBucket>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val bucketMap = remember(dayBuckets) { dayBuckets.associateBy { it.date } }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = "本周",
                style = AppTypography.HeadlineMedium,
                color = AppColors.OnSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekDates.forEach { date ->
                    val bucket   = bucketMap[date]
                    val selected = date == selectedDate
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(AppShapes.Large))
                            .background(
                                if (selected) AppColors.PrimaryContainer
                                else if (bucket != null) AppColors.PrimaryFixed.copy(alpha = 0.55f)
                                else AppColors.SurfaceContainerLow
                            )
                            .clickable { onDateSelected(date) }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text  = agendaShortDayTitle(date),
                                style = AppTypography.LabelMedium,
                                color = if (selected) AppColors.OnPrimary else AppColors.OnSurface
                            )
                            Text(
                                text  = bucket?.items?.size?.let { "${it} 项" } ?: "空",
                                style = AppTypography.LabelSmall,
                                color = if (selected) AppColors.OnPrimary.copy(alpha = 0.80f) else AppColors.OnSurfaceVariant
                            )
                            if (bucket != null) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 24.dp, height = 4.dp)
                                        .clip(RoundedCornerShape(AppShapes.Full))
                                        .background(if (selected) AppColors.OnPrimary.copy(alpha = 0.60f) else AppColors.Primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaOverviewCard(
    selectedDate: LocalDate?,
    dayItems: List<AgendaCardData>,
    groupedItems: List<AgendaDayBucket>,
    viewMode: String,
    weekDates: List<LocalDate>,
    activeMonth: YearMonth
) {
    var expandedDates by remember { mutableStateOf(setOf<LocalDate>()) }
    var detailItem by remember { mutableStateOf<AgendaCardData?>(null) }
    val totalItems = groupedItems.sumOf { it.items.size }
    val visibleBuckets = when (viewMode) {
        "week" -> groupedItems.filter { it.date in weekDates }
        "month" -> groupedItems.filter { it.date.year == activeMonth.year && it.date.month == activeMonth.month }
        else -> groupedItems.filter { it.date == selectedDate }
    }
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = when (viewMode) {
                    "week"  -> "本周时间线"
                    "month" -> "本月时间线"
                    else    -> selectedDate?.let(::agendaDayTitle) ?: "今日安排"
                },
                style = AppTypography.HeadlineMedium,
                color = AppColors.OnSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgendaStatChip(modifier = Modifier.weight(1f), label = "已保存", value = "$totalItems 条", bgColor = AppColors.PrimaryFixed)
                AgendaStatChip(modifier = Modifier.weight(1f), label = "当前", value = selectedDate?.let(::agendaDayTitle) ?: "今日", bgColor = AppColors.SecondaryFixed)
                AgendaStatChip(modifier = Modifier.weight(1f), label = "浏览", value = if (viewMode == "week") "按周" else if (viewMode == "month") "按月" else "日视图", bgColor = AppColors.SurfaceContainerHigh)
            }

            if (groupedItems.isEmpty()) {
                EmptyTimelineWorkspaceCard()
            }

            when (viewMode) {
                "week" -> visibleBuckets.forEach { bucket ->
                    ExpandableAgendaDayCard(
                        bucket = bucket,
                        expanded = expandedDates.contains(bucket.date),
                        onItemClick = { detailItem = it },
                        onToggle = {
                            expandedDates = if (expandedDates.contains(bucket.date)) {
                                expandedDates - bucket.date
                            } else {
                                expandedDates + bucket.date
                            }
                        }
                    )
                }

                "month" -> visibleBuckets.forEach { bucket ->
                    ExpandableAgendaDayCard(
                        bucket = bucket,
                        expanded = expandedDates.contains(bucket.date),
                        onItemClick = { detailItem = it },
                        onToggle = {
                            expandedDates = if (expandedDates.contains(bucket.date)) {
                                expandedDates - bucket.date
                            } else {
                                expandedDates + bucket.date
                            }
                        }
                    )
                }

                else -> {
                    if (dayItems.isEmpty()) {
                        EmptyAgendaCard(
                            title = "这一天暂无安排",
                            summary = "从首页导入海报、截图或通知文本，确认后的安排会出现在这里。"
                        )
                    } else {
                        TimelineAgendaList(
                            items = dayItems,
                            onItemClick = { detailItem = it }
                        )
                    }
                }
            }
        }
    }

    detailItem?.let { item ->
        AgendaDetailOverlay(
            item = item,
            onDismiss = { detailItem = null }
        )
    }
}

@Composable
private fun EmptyTimelineWorkspaceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShapes.Large))
            .background(AppColors.SurfaceContainerLow)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = "暂无安排",
                style = AppTypography.BodyLarge,
                color = AppColors.OnSurfaceVariant
            )
            Text(
                text      = "从首页导入后，确认的安排会出现在这里",
                style     = AppTypography.LabelSmall,
                color     = AppColors.Outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AgendaStatChip(
    modifier: Modifier,
    label: String,
    value: String,
    bgColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppShapes.Medium))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, style = AppTypography.LabelSmall, color = AppColors.OnSurfaceVariant)
        Text(text = value, style = AppTypography.LabelMedium, color = AppColors.OnSurface, maxLines = 1)
    }
}

@Composable
fun ReminderPolicyCard(
    reminderPolicyLabel: String,
    reminderStateText: String,
    nextReminderText: String,
    scheduledReminderCount: Int,
    reminderLeadMinutes: Int,
    reminderDayEnabled: Boolean,
    reminderHourEnabled: Boolean,
    exportFormats: List<String>,
    agendaItems: List<AgendaCardData>
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = "提醒策略",
                style = AppTypography.HeadlineMedium,
                color = AppColors.OnSurface
            )
            ProfilePreferenceRow(icon = Icons.Rounded.NotificationsActive, title = "默认提醒", value = reminderPolicyLabel)
            ProfilePreferenceRow(
                icon  = Icons.Rounded.NotificationsActive,
                title = "提醒设置",
                value = buildString {
                    append(if (reminderDayEnabled) "日级提醒已开启" else "日级提醒已关闭")
                    append(" · ")
                    append(if (reminderHourEnabled) "分钟级提醒：$reminderLeadMinutes 分钟前" else "分钟级提醒已关闭")
                }
            )
            ProfilePreferenceRow(icon = Icons.Rounded.CalendarMonth, title = "当前状态", value = reminderStateText)
            ProfilePreferenceRow(icon = Icons.Rounded.NotificationsActive, title = "下次提醒", value = nextReminderText)
            ProfilePreferenceRow(icon = Icons.Rounded.Download, title = "导出能力", value = exportFormats.joinToString(" / "))
            ProfilePreferenceRow(
                icon  = Icons.Rounded.Timeline,
                title = "已覆盖安排",
                value = if (agendaItems.isEmpty()) "还没有可提醒的安排"
                        else "当前为 ${agendaItems.size} 项安排保留了 ${scheduledReminderCount} 条提醒"
            )
        }
    }
}

// ── 日视图垂直时间线 ──────────────────────────────────────────────────────────
private val dayTimelineCardPalettes = listOf(
    Pair(AppColors.SurfaceContainerLow,      AppColors.Primary),             // sage tinted
    Pair(AppColors.SurfaceContainerHighest,  AppColors.Primary),             // neutral
    Pair(AppColors.SecondaryContainer,       AppColors.OnSecondaryContainer), // secondary
    Pair(AppColors.PrimaryFixed,             AppColors.OnPrimaryFixed),      // primary fixed
)

@Composable
fun DayVerticalTimelineCard(
    dayItems: List<AgendaCardData>,
    selectedDate: LocalDate?
) {
    var detailItem by remember { mutableStateOf<AgendaCardData?>(null) }
    val sorted = remember(dayItems) { dayItems.sortedBy { it.exportTimeLabel() } }

    if (sorted.isEmpty()) {
        EmptyAgendaCard(
            title = "这一天暂无安排",
            summary = "从首页导入海报、截图或通知文本，确认后的安排会出现在这里。"
        )
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            sorted.forEachIndexed { idx, item ->
                DayTimelineRow(
                    item = item,
                    paletteIndex = idx % dayTimelineCardPalettes.size,
                    isLast = idx == sorted.lastIndex,
                    onItemClick = { detailItem = item }
                )
            }
            // End of day label
            Row(
                modifier = Modifier.padding(start = 72.dp, top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(AppColors.PrimaryFixedDim, CircleShape)
                )
                Text(
                    text  = "今日安排已全部展示",
                    style = AppTypography.LabelSmall,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
    }

    detailItem?.let { item ->
        AgendaDetailOverlay(item = item, onDismiss = { detailItem = null })
    }
}

@Composable
private fun DayTimelineRow(
    item: AgendaCardData,
    paletteIndex: Int,
    isLast: Boolean,
    onItemClick: () -> Unit
) {
    val (cardBg, cardFg) = dayTimelineCardPalettes[paletteIndex]
    val dotColors = listOf(
        AppColors.SecondaryContainer,
        AppColors.PrimaryFixed,
        AppColors.TertiaryFixedDim,
        AppColors.SurfaceContainerHigh
    )
    val dotColor = dotColors[paletteIndex % dotColors.size]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        // 节点 + 连线列 (left side)
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(dotColor, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(AppColors.SurfaceContainerHigh)
                )
            }
        }

        // 事件卡片
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = 20.dp)
        ) {
            // 时间标签
            Text(
                text = item.displayTimeLabel().take(16),
                style = AppTypography.LabelSmall,
                color = AppColors.OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Card(
                modifier  = Modifier.fillMaxWidth().clickable(onClick = onItemClick),
                colors    = CardDefaults.cardColors(containerColor = cardBg),
                shape     = RoundedCornerShape(AppShapes.Large),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column(
                            modifier            = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Source tag chip
                            if (item.sourceLabel.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(AppShapes.Full))
                                        .background(AppColors.SurfaceContainerLowest.copy(alpha = 0.60f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text  = item.sourceLabel.take(8).uppercase(),
                                        style = AppTypography.LabelSmall,
                                        color = cardFg
                                    )
                                }
                            }
                            Text(
                                text  = item.title,
                                style = AppTypography.BodyLarge,
                                color = cardFg,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    if (item.location.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = cardFg.copy(alpha = 0.65f), modifier = Modifier.size(13.dp))
                            Text(text = item.location, style = AppTypography.BodyMedium, color = cardFg.copy(alpha = 0.65f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeroCard(
    eyebrow: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TimeWeaverInk),
        shape = RoundedCornerShape(32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = eyebrow,
                    color = TimeWeaverAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color(0xFFB7E8D6),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            Surface(
                modifier = Modifier.size(84.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TimeWeaverInk,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DoubleInfoRow(
    leftTitle: String,
    leftContent: String,
    rightTitle: String,
    rightContent: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniInfoCard(
            modifier = Modifier.weight(1f),
            title = leftTitle,
            content = leftContent
        )
        MiniInfoCard(
            modifier = Modifier.weight(1f),
            title = rightTitle,
            content = rightContent
        )
    }
}

@Composable
fun MiniInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = TimeWeaverMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (content.any { it.isDigit() }) {
                MetricText(
                    value = content,
                    color = TimeWeaverInk,
                    fontSize = 17.sp
                )
            } else {
                Text(
                    text = content,
                    color = TimeWeaverInk,
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun HighlightPanel(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(16.dp),
                color = TimeWeaverInk
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TimeWeaverAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = TimeWeaverInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = content,
                    color = TimeWeaverMuted,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun TimelineAgendaList(
    items: List<AgendaCardData>,
    onItemClick: (AgendaCardData) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            TimelineAgendaItem(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun TimelineAgendaItem(
    item: AgendaCardData,
    onClick: () -> Unit
) {
    val statusTone = when {
        item.status.contains("已加入") || item.status.contains("已确认") -> TimeWeaverGreen
        item.status.contains("待") -> TimeWeaverOrange
        item.status.contains("取消") -> TimeWeaverDanger
        else -> TimeWeaverBlue
    }
    val actionLabel = when (item.action) {
        "navigate" -> "导航"
        "create_event" -> "日程"
        "tts_feedback" -> "摘要"
        else -> "记录"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(statusTone.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusTone)
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(124.dp)
                    .background(TimeWeaverLine)
            )
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        MetricText(
                            value = item.displayTimeLabel(),
                            color = TimeWeaverBlue,
                            fontSize = 16.sp
                        )
                        Text(
                            text = item.exportTimeLabel(),
                            color = TimeWeaverMuted,
                            fontSize = 11.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.reminders.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "提醒 ${item.reminders.size}",
                                    color = TimeWeaverBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = statusTone.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = item.status,
                                color = statusTone,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    color = TimeWeaverInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = actionLabel,
                            color = TimeWeaverInk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    if (item.sourceLabel.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = "来源：${item.sourceLabel}",
                                color = TimeWeaverMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Text(
                    text = item.location,
                    color = TimeWeaverMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EmptyAgendaCard(
    title: String,
    summary: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = TimeWeaverInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary,
                color = TimeWeaverMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun ExpandableAgendaDayCard(
    bucket: AgendaDayBucket,
    expanded: Boolean,
    onItemClick: (AgendaCardData) -> Unit,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(TimeWeaverBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TimeWeaverBlue)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (expanded) 148.dp else 72.dp)
                        .background(TimeWeaverLine)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = agendaDayTitle(bucket.date),
                            color = TimeWeaverInk,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${bucket.items.size} 项已确认安排",
                            color = TimeWeaverMuted,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = if (expanded) "收起详情" else "展开详情",
                            color = TimeWeaverInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                if (!expanded) {
                    Text(
                        text = bucket.items.take(2).joinToString("  ·  ") { item ->
                            "${item.displayTimeLabel()} ${item.title}"
                        },
                        color = TimeWeaverInk,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
                if (expanded) {
                    bucket.items.forEach { item ->
                        TimelineAgendaItem(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaDetailSheet(
    item: AgendaCardData,
    onDismiss: () -> Unit
) {
    AgendaDetailOverlay(
        item = item,
        onDismiss = onDismiss
    )
}

@Composable
private fun AgendaDetailOverlay(
    item: AgendaCardData,
    onDismiss: () -> Unit
) {
    val statusTone = when (item.action) {
        "create_event" -> TimeWeaverGreen
        "navigate" -> TimeWeaverOrange
        "tts_feedback" -> TimeWeaverBlue
        else -> TimeWeaverDanger
    }
    val revealProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "agenda-detail-overlay-reveal"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
            animationSpec = tween(420, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 4 }
        ),
        exit = fadeOut(animationSpec = tween(160)) + slideOutVertically(
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            targetOffsetY = { it / 4 }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80122136))
                .clickable { onDismiss() }
        ) {
            TimeWeaverDiffuseBackdrop(
                modifier = Modifier.fillMaxSize(),
                dark = false
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 24.dp)
                    .graphicsLayer {
                        alpha = revealProgress
                        scaleX = 0.96f + (0.04f * revealProgress)
                        scaleY = 0.96f + (0.04f * revealProgress)
                    }
                    .clickable(enabled = false) {},
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(TimeWeaverPillRadius),
                        color = Color.White.copy(alpha = 0.82f)
                    ) {
                        Text(
                            text = "日程详情",
                            color = TimeWeaverInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = TimeWeaverInk
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TimeWeaverInk),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(TimeWeaverPillRadius),
                                color = statusTone.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = item.status,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.summary,
                                color = Color(0xFFB7E8D6),
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DetailPill(
                                    modifier = Modifier.weight(1f),
                                    title = "时间",
                                    value = item.displayTimeLabel()
                                )
                                DetailPill(
                                    modifier = Modifier.weight(1f),
                                    title = "提醒",
                                    value = item.reminderSummary()
                                )
                            }
                        }
                    }

                    DetailInfoRow("完整时间", item.exportTimeLabel())
                    DetailInfoRow("地点", item.location)
                    DetailInfoRow("来源", item.sourceLabel.ifBlank { "手动输入或系统默认" })
                    DetailInfoRow(
                        "动作类型",
                        when (item.action) {
                            "create_event" -> "按日程型任务进入时间线，可继续写入系统日历。"
                            "navigate" -> "这是一条地点型结果，可在需要时拉起地图导航。"
                            "tts_feedback" -> "这是一条摘要型结果，以语音反馈为主。"
                            else -> "当前以结构化记录形式保留。"
                        }
                    )
                    if (item.location.isBlank()) {
                        DetailInfoRow("补充提示", "这条安排当前没有明确地点，适合在确认前继续补全。")
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFB7E8D6),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    title: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = TimeWeaverMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = TimeWeaverInk,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun exportPlanSnapshot(
    context: Context,
    format: String,
    mode: String,
    month: YearMonth,
    selectedDate: LocalDate,
    buckets: List<AgendaDayBucket>
) {
    runCatching {
        val targetDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "exports"
        ).apply { mkdirs() }
        val exportBuckets = when (mode) {
            "day" -> buckets.filter { it.date == selectedDate }
            "week" -> buckets.filter { it.date in selectedDate.minusDays(3)..selectedDate.plusDays(3) }
            else -> buckets.filter { it.date.year == month.year && it.date.month == month.month }
        }

        val text = buildString {
            appendLine("织时")
            appendLine("导出视图: $mode")
            appendLine("聚焦日期: $selectedDate")
            appendLine("月份: ${agendaMonthTitle(month)}")
            appendLine()
            exportBuckets.forEach { bucket ->
                appendLine(agendaDayTitle(bucket.date))
                bucket.items.forEach { item ->
                    appendLine("- ${item.exportTimeLabel()} | ${item.title} | ${item.location} | ${item.reminderSummary()}")
                }
                appendLine()
            }
        }

        when (format.uppercase()) {
            "PDF" -> exportPdf(targetDir, text)
            "JPG", "PNG" -> exportBitmap(targetDir, text, format.uppercase())
            else -> exportBitmap(targetDir, text, "PNG")
        }
    }.onSuccess { file ->
        Toast.makeText(context, "已导出到 ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }.onFailure {
        Toast.makeText(context, "导出失败: ${it.message}", Toast.LENGTH_LONG).show()
    }
}

private fun exportPdf(dir: File, content: String): File {
    val file = File(dir, "timeweaver-plan-${System.currentTimeMillis()}.pdf")
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(1240, 1754, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TimeWeaverInk.toArgb()
        textSize = 28f
    }
    var y = 80f
    content.lines().forEach { line ->
        canvas.drawText(line, 60f, y, paint)
        y += 40f
    }
    document.finishPage(page)
    FileOutputStream(file).use(document::writeTo)
    document.close()
    return file
}

private fun exportBitmap(dir: File, content: String, format: String): File {
    val bitmap = Bitmap.createBitmap(1400, 1800, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(TimeWeaverBackground.toArgb())
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TimeWeaverInk.toArgb()
        textSize = 48f
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TimeWeaverMuted.toArgb()
        textSize = 28f
    }
    canvas.drawText("织时", 70f, 110f, titlePaint)
    var y = 180f
    content.lines().forEach { line ->
        canvas.drawText(line, 70f, y, bodyPaint)
        y += 42f
    }
    val ext = if (format == "PNG") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    val file = File(dir, "timeweaver-plan-${System.currentTimeMillis()}.${format.lowercase()}")
    FileOutputStream(file).use { output ->
        bitmap.compress(ext, 95, output)
    }
    return file
}
