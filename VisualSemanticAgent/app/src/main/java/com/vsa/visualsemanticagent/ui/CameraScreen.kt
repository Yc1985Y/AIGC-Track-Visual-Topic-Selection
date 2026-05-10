package com.vsa.visualsemanticagent.ui

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vsa.visualsemanticagent.R
import com.vsa.visualsemanticagent.camera.CameraManager
import com.vsa.visualsemanticagent.decision.ExecutableIntent
import com.vsa.visualsemanticagent.decision.ExecutionMode
import com.vsa.visualsemanticagent.decision.ExecutionSuggestion
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.agendaMonthMatrix
import com.vsa.visualsemanticagent.plan.agendaWeekWindow
import com.vsa.visualsemanticagent.plan.displayDateLabel
import com.vsa.visualsemanticagent.plan.displayTimeLabel
import com.vsa.visualsemanticagent.plan.groupAgendasByDay
import com.vsa.visualsemanticagent.plan.scheduleDate
import com.vsa.visualsemanticagent.plan.scheduleDateTime
import com.vsa.visualsemanticagent.utils.PromptPreset
import kotlin.math.roundToInt

// ── Zhishi Premium palette (mapped from AppColors for legacy usages) ──────────
internal val TimeWeaverBackground     = AppColors.Background               // #F8FAF9
internal val TimeWeaverBackgroundWarm = AppColors.SurfaceContainerLow      // #F3F4F3
internal val TimeWeaverBackgroundDeep = AppColors.InverseSurface           // #2E3131
internal val TimeWeaverSurface        = AppColors.SurfaceContainerLowest   // #FFFFFF
internal val TimeWeaverSurfaceAlt     = AppColors.SurfaceContainer         // #EDEEED
internal val TimeWeaverTint           = AppColors.SurfaceContainerHighest  // #E1E3E2
internal val TimeWeaverInk            = AppColors.OnBackground             // #191C1C
internal val TimeWeaverMuted          = AppColors.OnSurfaceVariant         // #444842
internal val TimeWeaverAccent         = AppColors.PrimaryFixed             // #D7E7D2
internal val TimeWeaverBlue           = AppColors.Primary                  // #51604F
internal val TimeWeaverPrimaryEnd     = AppColors.PrimaryFixedDim          // #BBCBB7
internal val TimeWeaverAuroraCyan     = AppColors.SecondaryFixed           // #DCE5D7
internal val TimeWeaverAuroraPurple   = AppColors.TertiaryFixed            // #DFE4DD
internal val TimeWeaverGreen          = AppColors.PrimaryContainer         // #6A7967
internal val TimeWeaverOrange         = AppColors.TertiaryFixed            // #DFE4DD (desaturated warm)
internal val TimeWeaverDanger         = AppColors.Error                    // #BA1A1A
internal val TimeWeaverLine           = AppColors.OutlineVariant.copy(alpha = 0.35f)
internal val TimeWeaverCardRadius     = AppShapes.ExtraLarge               // 24dp
internal val TimeWeaverNestedRadius   = AppShapes.Large                    // 16dp
internal val TimeWeaverPillRadius     = AppShapes.Full                     // 999dp
internal val TimeWeaverGlassAlpha     = 0.80f
internal val TimeWeaverGlassBlur      = 12.dp

internal fun weavingPrimaryBrush(): Brush = Brush.linearGradient(
    listOf(
        TimeWeaverBlue,
        TimeWeaverPrimaryEnd
    )
)

@Composable
internal fun TimeWeaverDiffuseBackdrop(
    modifier: Modifier = Modifier,
    dark: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (dark) {
                        listOf(
                            Color(0xFF071C10),
                            Color(0xFF0A2818),
                            Color(0xFF0F3A20),
                            Color(0xFFF2FCF1)
                        )
                    } else {
                        listOf(
                            Color(0xFFF2FCF1),
                            Color(0xFFFFFFFF),
                            Color(0xFFECF6EC)
                        )
                    }
                )
            )
    ) {
        DiffuseOrb(
            modifier = Modifier
                .size(if (dark) 300.dp else 260.dp)
                .offset(x = (-40).dp, y = 18.dp),
            colors = listOf(
                TimeWeaverBlue.copy(alpha = if (dark) 0.24f else 0.14f),
                Color.Transparent
            )
        )
        DiffuseOrb(
            modifier = Modifier
                .size(if (dark) 260.dp else 220.dp)
                .offset(x = 186.dp, y = 90.dp),
            colors = listOf(
                TimeWeaverPrimaryEnd.copy(alpha = if (dark) 0.18f else 0.12f),
                Color.Transparent
            )
        )
        DiffuseOrb(
            modifier = Modifier
                .size(if (dark) 240.dp else 210.dp)
                .offset(x = 92.dp, y = 420.dp),
            colors = listOf(
                TimeWeaverAuroraCyan.copy(alpha = if (dark) 0.16f else 0.12f),
                Color.Transparent
            )
        )
        DiffuseOrb(
            modifier = Modifier
                .size(if (dark) 220.dp else 180.dp)
                .offset(x = 240.dp, y = 360.dp),
            colors = listOf(
                TimeWeaverAuroraPurple.copy(alpha = if (dark) 0.12f else 0.1f),
                Color.Transparent
            )
        )
    }
}

@Composable
private fun DiffuseOrb(
    modifier: Modifier,
    colors: List<Color>
) {
    Box(
        modifier = modifier
            .blur(76.dp)
            .background(
                brush = Brush.radialGradient(colors = colors),
                shape = CircleShape
            )
    )
}

@Composable
internal fun GradientBrandText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 1.2.sp
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            brush = Brush.linearGradient(
                listOf(
                    TimeWeaverBlue,
                    TimeWeaverPrimaryEnd,
                    TimeWeaverAuroraCyan
                )
            ),
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            fontFamily = FontFamily.SansSerif
        )
    )
}

@Composable
internal fun MetricText(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = TimeWeaverInk,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    textAlign: TextAlign? = null
) {
    Text(
        text = value,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.4.sp,
        textAlign = textAlign
    )
}

@Composable
internal fun TimeWeaverCard(
    modifier: Modifier = Modifier,
    containerColor: Color = TimeWeaverSurface,
    showGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassColor = if (containerColor.alpha >= 1f && containerColor.luminance() > 0.55f) {
        containerColor.copy(alpha = TimeWeaverGlassAlpha)
    } else {
        containerColor
    }
    val borderColor = if (glassColor.luminance() < 0.32f) {
        Color.White.copy(alpha = 0.24f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }
    val cardShape = RoundedCornerShape(TimeWeaverCardRadius)
    Box(modifier = modifier.fillMaxWidth()) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .blur(38.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                TimeWeaverBlue.copy(alpha = 0.1f),
                                TimeWeaverPrimaryEnd.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = cardShape
                    )
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = glassColor),
            shape = cardShape,
            border = BorderStroke(0.8.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
internal fun TimeWeaverGridBackdrop(
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val verticalStep = 72.dp.toPx()
                val horizontalStep = 64.dp.toPx()
                val lineColor = TimeWeaverLine.copy(alpha = alpha)
                val verticalOffsets = buildList {
                    var offsetX = 0f
                    while (offsetX < canvasWidth) {
                        add(offsetX)
                        offsetX += verticalStep
                    }
                }
                val horizontalOffsets = buildList {
                    var offsetY = 0f
                    while (offsetY < canvasHeight) {
                        add(offsetY)
                        offsetY += horizontalStep
                    }
                }
                onDrawBehind {
                    verticalOffsets.forEach { x ->
                        drawLine(
                            color = lineColor,
                            start = Offset(x, 0f),
                            end = Offset(x, canvasHeight),
                            strokeWidth = 1.5f
                        )
                    }
                    horizontalOffsets.forEach { y ->
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.5f
                        )
                    }
                }
            }
    )
}

@Composable
fun PlanScreen(
    modifier: Modifier = Modifier,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    agendaItems: List<AgendaCardData>,
    selectedPlanMode: String,
    selectedPlanDate: java.time.LocalDate?,
    calendarPreviewMonth: java.time.YearMonth,
    reminderPolicyLabel: String,
    reminderStateText: String,
    nextReminderText: String,
    scheduledReminderCount: Int,
    confirmedAgendaCount: Int,
    todayAgendaCount: Int,
    reminderLeadMinutes: Int,
    reminderDayEnabled: Boolean,
    reminderHourEnabled: Boolean,
    exportFormats: List<String>,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val groupedItems = remember(agendaItems) { groupAgendasByDay(agendaItems) }
    val confirmationConfidence = confirmationIntent?.fusedConfidence?.toFloat()?.coerceIn(0f, 1f)
        ?: confirmationSuggestion?.threshold?.toFloat()?.coerceIn(0f, 1f)
        ?: 0.5f
    val confirmationRequiresReview = confirmationConfidence < 0.9f
    var activeMode by remember(selectedPlanMode) {
        mutableStateOf(selectedPlanMode.ifBlank { "month" })
    }
    val currentModeScope = when (activeMode) {
        "day" -> "当日"
        "week" -> "本周"
        else -> "本月"
    }
    var activeDate by remember(selectedPlanDate, agendaItems) {
        mutableStateOf(selectedPlanDate ?: groupedItems.firstOrNull()?.date ?: java.time.LocalDate.now())
    }
    var activeMonth by remember(calendarPreviewMonth) {
        mutableStateOf(calendarPreviewMonth)
    }
    val dayItems = remember(activeDate, groupedItems, agendaItems) {
        groupedItems.firstOrNull { it.date == activeDate }?.items
            ?: agendaItems.filter { it.scheduleDate() == activeDate }
    }
    val weekDates = remember(activeDate) { agendaWeekWindow(activeDate) }
    val monthMatrix = remember(activeMonth) { agendaMonthMatrix(activeMonth) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter)
        ) {
            // 1. 页面标题
            item {
                TimelinePageHeader()
            }

            // 2. 日/周/月 标签页 + 导出胶囊（同一行）
            item {
                TimelineControlsRow(
                    activeMode = activeMode,
                    onModeSelected = { activeMode = it },
                    onExport = { format ->
                        exportPlanSnapshot(
                            context = context,
                            format = format,
                            mode = activeMode,
                            month = activeMonth,
                            selectedDate = activeDate,
                            buckets = groupedItems
                        )
                    }
                )
            }

            // 3. 待确认（有时显示）
            if (showConfirmationCard && confirmationIntent != null && confirmationSuggestion != null) {
                item {
                    ConfirmationCard(
                        intent = confirmationIntent,
                        suggestion = confirmationSuggestion,
                        confidence = confirmationConfidence,
                        requiresReview = confirmationRequiresReview,
                        onConfirmExecution = onConfirmExecution,
                        onCancelExecution = onCancelExecution
                    )
                }
            }

            // 4. 周视图：日期横条
            if (activeMode != "month") {
                item {
                    WeekAgendaStrip(
                        weekDates = weekDates,
                        dayBuckets = groupedItems,
                        selectedDate = activeDate,
                        onDateSelected = {
                            activeDate = it
                            activeMode = "day"
                        }
                    )
                }
            }

            // 5. 月视图：日历格
            if (activeMode == "month") {
                item {
                    MonthCalendarCard(
                        month = activeMonth,
                        matrix = monthMatrix,
                        dayBuckets = groupedItems,
                        selectedDate = activeDate,
                        onPreviousMonth = { activeMonth = activeMonth.minusMonths(1) },
                        onNextMonth = { activeMonth = activeMonth.plusMonths(1) },
                        onDateSelected = {
                            activeDate = it
                            activeMode = "day"
                        }
                    )
                }
            }

            // 6. 日视图：新垂直时间线 | 周/月视图：保留原卡片
            if (activeMode == "day") {
                item {
                    DayVerticalTimelineCard(
                        dayItems = dayItems,
                        selectedDate = activeDate
                    )
                }
            } else {
                item {
                    AgendaOverviewCard(
                        selectedDate = activeDate,
                        dayItems = dayItems,
                        groupedItems = groupedItems,
                        viewMode = activeMode,
                        weekDates = weekDates,
                        activeMonth = activeMonth
                    )
                }
            }

            // 7. 提醒策略
            item {
                ReminderPolicyCard(
                    reminderPolicyLabel = reminderPolicyLabel,
                    reminderStateText = reminderStateText,
                    nextReminderText = nextReminderText,
                    scheduledReminderCount = scheduledReminderCount,
                    reminderLeadMinutes = reminderLeadMinutes,
                    reminderDayEnabled = reminderDayEnabled,
                    reminderHourEnabled = reminderHourEnabled,
                    exportFormats = exportFormats,
                    agendaItems = agendaItems
                )
            }

            // 8. 回首页
            item {
                Button(
                    onClick  = onGoHome,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(AppShapes.Full),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor   = AppColors.OnPrimary
                    )
                ) {
                    Icon(Icons.Rounded.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("回到首页继续导入", style = AppTypography.LabelMedium)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    showLivePreview: Boolean,
    isCameraAvailable: Boolean,
    statusText: String,
    importedSourceLabel: String,
    reminderStateText: String,
    nextReminderText: String,
    scheduledReminderCount: Int,
    confirmedAgendaCount: Int,
    pendingAgendaCount: Int,
    todayAgendaCount: Int,
    reminderLeadMinutes: Int,
    reminderDayEnabled: Boolean,
    reminderHourEnabled: Boolean,
    blockHighRisk: Boolean,
    muteLowConfidence: Boolean,
    autoMapLink: Boolean,
    onReminderLeadMinutesChange: (Int) -> Unit,
    onReminderDayEnabledChange: (Boolean) -> Unit,
    onReminderHourEnabledChange: (Boolean) -> Unit,
    onBlockHighRiskChange: (Boolean) -> Unit,
    onMuteLowConfidenceChange: (Boolean) -> Unit,
    onAutoMapLinkChange: (Boolean) -> Unit,
    onOpenPlan: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Ambient blobs
        Box(modifier = Modifier.size(300.dp).offset(x = (-50).dp, y = (-50).dp)
            .blur(80.dp).background(AppColors.SecondaryFixed.copy(alpha = 0.40f), androidx.compose.foundation.shape.CircleShape))
        Box(modifier = Modifier.size(360.dp).align(Alignment.BottomEnd).offset(x = 60.dp, y = 60.dp)
            .blur(90.dp).background(AppColors.PrimaryFixed.copy(alpha = 0.30f), androidx.compose.foundation.shape.CircleShape))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter)
        ) {
            item {
                ProfileHeroCard(
                    confirmedAgendaCount    = confirmedAgendaCount,
                    pendingAgendaCount      = pendingAgendaCount,
                    scheduledReminderCount  = scheduledReminderCount
                )
            }

            item {
                ProfileStatusOverviewCard(
                    importedSourceLabel = importedSourceLabel,
                    showLivePreview = showLivePreview,
                    isCameraAvailable = isCameraAvailable,
                    reminderStateText = reminderStateText,
                    nextReminderText = nextReminderText,
                    statusText = statusText
                )
            }

            item {
                DigitalWellbeingCard(
                    confirmedAgendaCount = confirmedAgendaCount,
                    pendingAgendaCount = pendingAgendaCount
                )
            }

            item {
                ProfileQuickActionsCard(
                    reminderCount = scheduledReminderCount,
                    onOpenPlan = onOpenPlan
                )
            }

            item {
                PreferencesBoard(
                    showLivePreview = showLivePreview,
                    isCameraAvailable = isCameraAvailable,
                    reminderLeadMinutes = reminderLeadMinutes,
                    reminderDayEnabled = reminderDayEnabled,
                    reminderHourEnabled = reminderHourEnabled,
                    blockHighRisk = blockHighRisk,
                    muteLowConfidence = muteLowConfidence,
                    autoMapLink = autoMapLink,
                    onReminderLeadMinutesChange = onReminderLeadMinutesChange,
                    onReminderDayEnabledChange = onReminderDayEnabledChange,
                    onReminderHourEnabledChange = onReminderHourEnabledChange,
                    onBlockHighRiskChange = onBlockHighRiskChange,
                    onMuteLowConfidenceChange = onMuteLowConfidenceChange,
                    onAutoMapLinkChange = onAutoMapLinkChange
                )
            }

            item {
                BadgeWallCard()
            }
        }
    }
}

@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onSubmitCommandClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onPickImageClick: () -> Unit,
    onPasteTextClick: () -> Unit,
    onPresetClick: (PromptPreset) -> Unit,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    bindPreview: (PreviewView) -> Unit,
    presets: List<PromptPreset>,
    showLivePreview: Boolean = true,
    isLoading: Boolean = false,
    isVoiceListening: Boolean = false,
    isCameraAvailable: Boolean = true,
    statusText: String? = null,
    resultText: String? = null,
    importedSourceLabel: String? = null,
    agendaItems: List<AgendaCardData> = emptyList(),
    nextReminderText: String = "",
    confirmedAgendaCount: Int = 0,
    todayAgendaCount: Int = 0
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val actionEnabled = !isLoading && !isVoiceListening
    var previewArmed by rememberSaveable(showLivePreview, isCameraAvailable) {
        mutableStateOf(!showLivePreview || !isCameraAvailable)
    }
    val nextAgenda = remember(agendaItems) {
        agendaItems
            .filter { it.status.contains("已加入") || it.status.contains("已确认") }
            .sortedBy { it.scheduleDateTime() }
            .firstOrNull { agenda ->
                agenda.scheduleDateTime()?.isAfter(java.time.LocalDateTime.now()) == true
            }
    }
    val summary = resultText?.takeIf { it.isNotBlank() }
        ?: statusText?.takeIf { it.isNotBlank() }
        ?: "把一张海报、一张截图或一句话交给织时，它会先理解，再给你确认后的时间线结果。"
    val todayItems = remember(agendaItems) {
        val today = java.time.LocalDate.now()
        agendaItems.filter { it.scheduleDate() == today }
    }
    val hasActiveWorkspace = importedSourceLabel?.isNotBlank() == true ||
        confirmationIntent != null || isLoading || isVoiceListening

    val pendingAgendaCount = remember(agendaItems) { agendaItems.count { it.status.contains("待") } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter)
        ) {
            // 1. 问候标题
            item {
                HomeGreetingSection(todayAgendaCount = todayAgendaCount)
            }

            // 2. 统计三格
            item {
                HomeStatsRow(
                    todayAgendaCount   = todayAgendaCount,
                    pendingAgendaCount = pendingAgendaCount,
                    nextReminderText   = nextReminderText
                )
            }

            // 3. 主行动卡（含文字输入 + 四快捷入口）
            item {
                QuickActionMainCard(
                    commandText = commandText,
                    onCommandChanged = onCommandChanged,
                    onSubmitCommandClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSubmitCommandClick()
                        }
                    },
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening,
                    onCaptureClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (showLivePreview && isCameraAvailable && !previewArmed) {
                                previewArmed = true
                            } else {
                                onCaptureClick()
                            }
                        }
                    },
                    onPickImageClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPickImageClick()
                        }
                    },
                    onPasteTextClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPasteTextClick()
                        }
                    },
                    onVoiceClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onVoiceClick()
                        }
                    }
                )
            }

            // 4. 今日安排
            item {
                TodayScheduleInfoCard(
                    todayItems = todayItems,
                    todayAgendaCount = todayAgendaCount
                )
            }

            // 5. 待确认（有确认事项时显示）
            if (showConfirmationCard && confirmationIntent != null && confirmationSuggestion != null) {
                item {
                    PendingConfirmationInfoCard(
                        intent = confirmationIntent,
                        suggestion = confirmationSuggestion,
                        onConfirmExecution = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirmExecution()
                        },
                        onCancelExecution = onCancelExecution
                    )
                }
            }

            // 6. 下次提醒
            item {
                NextReminderInfoCard(
                    nextAgenda = nextAgenda,
                    nextReminderText = nextReminderText
                )
            }

            // 7. 工作台（有活跃素材或相机已激活时显示）
            if (hasActiveWorkspace || previewArmed) {
                item {
                    WorkbenchBentoSection(
                        context = context,
                        showLivePreview = showLivePreview,
                        previewArmed = previewArmed,
                        onArmPreview = { previewArmed = true },
                        isCameraAvailable = isCameraAvailable,
                        bindPreview = bindPreview,
                        summary = summary,
                        importedSourceLabel = importedSourceLabel,
                        confirmationIntent = confirmationIntent,
                        confirmationSuggestion = confirmationSuggestion,
                        showConfirmationCard = showConfirmationCard,
                        isLoading = isLoading,
                        isVoiceListening = isVoiceListening,
                        onConfirmExecution = onConfirmExecution,
                        onCancelExecution = onCancelExecution
                    )
                }
            }

            // 8. 快速模板
            item {
                PresetCapsuleRow(
                    presets = presets,
                    enabled = actionEnabled,
                    onPresetClick = onPresetClick
                )
            }
        }
    }
}

// ── 时间线页标题 ──────────────────────────────────────────────────────────────
@Composable
private fun TimelinePageHeader() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = "时间线",
            style = AppTypography.DisplayLarge,
            color = AppColors.Primary
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(AppColors.SurfaceContainerLow)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = {}
                )
        ) {
            Icon(
                imageVector        = Icons.Rounded.Share,
                contentDescription = null,
                tint               = AppColors.Primary,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ── 时间线控制行（日/周/月 + 导出胶囊）────────────────────────────────────────
@Composable
private fun TimelineControlsRow(
    activeMode: String,
    onModeSelected: (String) -> Unit,
    onExport: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 日 / 周 / 月 胶囊标签
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AppShapes.Full))
                .background(AppColors.SurfaceContainerLowest)
                .border(1.dp, AppColors.SurfaceContainerLow, RoundedCornerShape(AppShapes.Full))
                .padding(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                listOf("day" to "日", "week" to "周", "month" to "月").forEach { (key, label) ->
                    val selected = key == activeMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShapes.Full))
                            .background(if (selected) AppColors.Primary else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onModeSelected(key) }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = label,
                            style = AppTypography.LabelSmall,
                            color = if (selected) AppColors.OnPrimary else AppColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
        // 导出胶囊
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TimelineExportChip("PDF", AppColors.PrimaryFixed,   AppColors.OnPrimaryFixed)   { onExport("pdf") }
            TimelineExportChip("JPG", AppColors.SecondaryFixed, AppColors.OnSecondaryFixed) { onExport("jpg") }
            TimelineExportChip("PNG", AppColors.TertiaryFixed,  AppColors.OnTertiaryFixed)  { onExport("png") }
        }
    }
}

@Composable
private fun TimelineExportChip(label: String, bg: Color, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppShapes.Full))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = AppTypography.LabelSmall, color = tint)
    }
}

// ── 首页问候标题 ──────────────────────────────────────────────────────────────
@Composable
private fun HomeGreetingSection(todayAgendaCount: Int) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AppColors.SurfaceContainerHigh)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Person,
                    contentDescription = null,
                    tint               = AppColors.Primary,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text  = "Hi，同学",
                    style = AppTypography.HeadlineMedium,
                    color = AppColors.Primary
                )
                Text(
                    text  = "今天有什么安排？",
                    style = AppTypography.BodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
        // Notification bell
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(AppColors.SurfaceContainerLow)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = {}
                )
        ) {
            Icon(
                imageVector        = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint               = AppColors.Primary,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ── 首页统计三格 ───────────────────────────────────────────────────────────────
@Composable
private fun HomeStatsRow(
    todayAgendaCount:   Int,
    pendingAgendaCount: Int,
    nextReminderText:   String
) {
    data class StatCard(val icon: ImageVector, val value: String, val label: String, val bg: Color, val iconBg: Color, val iconTint: Color)
    val cards = listOf(
        StatCard(Icons.Rounded.CalendarMonth, "$todayAgendaCount", "今日安排",
            AppColors.PrimaryFixed.copy(alpha = 0.30f), AppColors.PrimaryFixed, AppColors.Primary),
        StatCard(Icons.Rounded.NotificationsActive, "$pendingAgendaCount", "待确认",
            AppColors.SecondaryFixed.copy(alpha = 0.30f), AppColors.SecondaryFixed, AppColors.Secondary),
        StatCard(Icons.Rounded.Schedule, if (nextReminderText.length > 4) "•" else "–", "下次提醒",
            AppColors.SurfaceContainerHighest.copy(alpha = 0.50f), AppColors.SurfaceContainerHighest, AppColors.OnSurface)
    )
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        cards.forEach { card ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppShapes.Large))
                    .background(card.bg)
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(card.iconBg)
                    ) {
                        Icon(
                            imageVector        = card.icon,
                            contentDescription = null,
                            tint               = card.iconTint,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text  = card.value,
                        style = AppTypography.HeadlineMedium,
                        color = AppColors.OnSurface
                    )
                    Text(
                        text      = card.label,
                        style     = AppTypography.LabelSmall,
                        color     = AppColors.OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── 主行动卡（文字输入 + 四快捷入口合一） ─────────────────────────────────────────
@Composable
private fun QuickActionMainCard(
    commandText:         String,
    onCommandChanged:    (String) -> Unit,
    onSubmitCommandClick: () -> Unit,
    isLoading:           Boolean,
    isVoiceListening:    Boolean,
    onCaptureClick:      () -> Unit,
    onPickImageClick:    () -> Unit,
    onPasteTextClick:    () -> Unit,
    onVoiceClick:        () -> Unit
) {
    val enabled = !isLoading && !isVoiceListening
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            // Decorative blob
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .blur(40.dp)
                    .background(AppColors.SecondaryContainer.copy(alpha = 0.20f), androidx.compose.foundation.shape.CircleShape)
            )
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text  = "把通知变成清楚的日程",
                            style = AppTypography.HeadlineMedium,
                            color = AppColors.OnSurface
                        )
                        Text(
                            text  = "智能提取，轻松管理",
                            style = AppTypography.BodyMedium,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                    // State chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShapes.Full))
                            .background(AppColors.Primary.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint               = AppColors.Primary,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }

                // Text input
                OutlinedTextField(
                    value         = commandText,
                    onValueChange = onCommandChanged,
                    modifier      = Modifier.fillMaxWidth(),
                    enabled       = enabled,
                    placeholder   = {
                        Text(
                            text  = "扔一段文字给我，例如：周五晚七点图书馆报告厅有讲座",
                            style = AppTypography.BodyMedium,
                            color = AppColors.Outline
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor        = AppColors.OnSurface,
                        unfocusedTextColor      = AppColors.OnSurface,
                        focusedContainerColor   = AppColors.InputBackground,
                        unfocusedContainerColor = AppColors.InputBackground,
                        disabledContainerColor  = AppColors.SurfaceContainerHigh,
                        focusedBorderColor      = AppColors.Primary.copy(alpha = 0.60f),
                        unfocusedBorderColor    = Color.Transparent,
                        disabledBorderColor     = Color.Transparent,
                        cursorColor             = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(AppShapes.Large)
                )

                // Submit button
                Button(
                    onClick  = onSubmitCommandClick,
                    enabled  = enabled,
                    modifier = Modifier.align(Alignment.End).height(44.dp),
                    shape    = RoundedCornerShape(AppShapes.Full),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = AppColors.Primary,
                        contentColor           = AppColors.OnPrimary,
                        disabledContainerColor = AppColors.SurfaceContainerHigh,
                        disabledContentColor   = AppColors.Outline
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("开始解析", style = AppTypography.LabelMedium)
                }

                // 4 quick action buttons in a row
                data class ActionItem(val label: String, val icon: ImageVector, val bg: Color, val tint: Color, val onClick: () -> Unit)
                val actions = listOf(
                    ActionItem("拍海报",  Icons.Rounded.PhotoCamera,  AppColors.SecondaryContainer, AppColors.OnSecondaryContainer, onCaptureClick),
                    ActionItem("选截图",  Icons.Rounded.PhotoLibrary,  AppColors.PrimaryFixed,       AppColors.OnPrimaryFixed,       onPickImageClick),
                    ActionItem("粘贴通知", Icons.Rounded.ContentPaste, AppColors.SurfaceDim,         AppColors.OnSurface,            onPasteTextClick),
                    ActionItem("语音输入", Icons.Rounded.Mic,          AppColors.TertiaryContainer,  AppColors.OnTertiaryContainer,  onVoiceClick)
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { action ->
                        Column(
                            modifier              = Modifier
                                .weight(1f)
                                .clickable(enabled = enabled, onClick = action.onClick),
                            horizontalAlignment   = Alignment.CenterHorizontally,
                            verticalArrangement   = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(action.bg)
                            ) {
                                Icon(
                                    imageVector        = action.icon,
                                    contentDescription = null,
                                    tint               = action.tint,
                                    modifier           = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text      = action.label,
                                style     = AppTypography.LabelSmall,
                                color     = AppColors.OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 快速录入英雄卡 ─────────────────────────────────────────────────────────────
@Composable
private fun QuickImportHeroCard(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onSubmitCommandClick: () -> Unit,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    val dockPulse = rememberInfiniteTransition(label = "hero-dock")
    val glow by dockPulse.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero-glow"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF006D3F), Color(0xFF004D2A), Color(0xFF003820))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "快速录入",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "无论是一张海报还是长篇通知，\n轻松转化为清晰的日程安排。",
                        color = Color(0xFFB7E8D6),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = when {
                            isLoading -> "正在解析"
                            isVoiceListening -> "正在倾听"
                            else -> "待机"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            OutlinedTextField(
                value = commandText,
                onValueChange = onCommandChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isVoiceListening,
                placeholder = {
                    Text(
                        text = "扔一段文字给我，例如：周五晚七点图书馆报告厅有讲座",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.1f * glow),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                    disabledContainerColor = Color.White.copy(alpha = 0.06f),
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                    disabledBorderColor = Color.Transparent,
                    cursorColor = TimeWeaverAccent
                ),
                shape = RoundedCornerShape(TimeWeaverCardRadius)
            )

            if (isLoading) {
                InlineShimmerWorkspace(
                    title = "正在整理通知",
                    lines = listOf("正在整理通知…", "正在识别事项、时间和地点…", "很快就好…")
                )
            }

            Button(
                onClick = onSubmitCommandClick,
                enabled = !isLoading && !isVoiceListening,
                modifier = Modifier
                    .align(Alignment.End)
                    .height(44.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = TimeWeaverBlue,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(text = "开始解析", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── 四宫格快捷入口 ─────────────────────────────────────────────────────────────
@Composable
private fun QuickActionBentoGrid(
    onCaptureClick: () -> Unit,
    onPickImageClick: () -> Unit,
    onPasteTextClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    val enabled = !isLoading && !isVoiceListening
    data class Tile(
        val label: String,
        val icon: ImageVector,
        val iconBg: Color,
        val iconTint: Color,
        val borderColor: Color,
        val borderWidth: androidx.compose.ui.unit.Dp,
        val onClick: () -> Unit
    )
    val tiles = listOf(
        Tile("拍海报",  Icons.Rounded.PhotoCamera,   Color(0xFFFFDADC), Color(0xFFB32444), Color(0xFF57FFA6), 2.dp,  onCaptureClick),
        Tile("选截图",  Icons.Rounded.PhotoLibrary,  Color(0xFFFFE17A), Color(0xFF715C00), Color(0xFFBACBBC), 0.8.dp, onPickImageClick),
        Tile("粘贴通知", Icons.Rounded.ContentPaste, Color(0xFF57FFA6), Color(0xFF006D3F), Color(0xFFBACBBC), 0.8.dp, onPasteTextClick),
        Tile("语音输入", Icons.Rounded.Mic,          Color(0xFFDBE5DB), Color(0xFF3B4A3F), Color(0xFFBACBBC), 0.8.dp, onVoiceClick)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { tile ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = enabled, onClick = tile.onClick),
                        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurface),
                        shape = RoundedCornerShape(TimeWeaverCardRadius),
                        border = BorderStroke(tile.borderWidth, tile.borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape,
                                color = tile.iconBg
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = tile.icon,
                                        contentDescription = null,
                                        tint = tile.iconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = tile.label,
                                color = TimeWeaverInk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 今日安排信息卡 ─────────────────────────────────────────────────────────────
@Composable
private fun TodayScheduleInfoCard(
    todayItems: List<AgendaCardData>,
    todayAgendaCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("最近识别结果", style = AppTypography.BodyLarge, color = AppColors.OnSurface)
            Text(
                text  = "查看全部",
                style = AppTypography.LabelMedium,
                color = AppColors.Primary
            )
        }
        if (todayItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppShapes.Large))
                    .background(AppColors.Surface)
                    .border(1.dp, AppColors.OutlineVariant.copy(alpha = 0.30f), RoundedCornerShape(AppShapes.Large))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("今天暂无安排", style = AppTypography.BodyMedium, color = AppColors.Outline)
                    Text("导入通知后自动出现在这里", style = AppTypography.LabelSmall, color = AppColors.Outline)
                }
            }
        } else {
            todayItems.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppShapes.Large))
                        .background(AppColors.Surface)
                        .border(1.dp, AppColors.OutlineVariant.copy(alpha = 0.30f), RoundedCornerShape(AppShapes.Large))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(AppShapes.Medium))
                            .background(AppColors.SurfaceContainerHighest)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.EventNote,
                            contentDescription = null,
                            tint               = AppColors.Primary,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                    Column(
                        modifier            = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = item.title, style = AppTypography.BodyLarge, color = AppColors.OnSurface)
                        Text(text = item.displayTimeLabel(), style = AppTypography.BodyMedium, color = AppColors.OnSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShapes.Full))
                            .background(AppColors.Primary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("添加", style = AppTypography.LabelMedium, color = AppColors.OnPrimary)
                    }
                }
            }
        }
    }
}

// ── 待确认信息卡 ───────────────────────────────────────────────────────────────
@Composable
private fun PendingConfirmationInfoCard(
    intent: ExecutableIntent,
    suggestion: ExecutionSuggestion,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit
) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = AppColors.SecondaryContainer.copy(alpha = 0.45f)),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        border    = BorderStroke(1.dp, AppColors.SecondaryFixed.copy(alpha = 0.60f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint               = AppColors.Primary,
                        modifier           = Modifier.size(20.dp)
                    )
                    Text("待确认", style = AppTypography.BodyLarge, color = AppColors.OnSurface)
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(AppColors.Primary, CircleShape)
                )
            }
            Card(
                colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
                shape     = RoundedCornerShape(AppShapes.Large),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text  = "\"${suggestion.prompt}\"",
                        style = AppTypography.BodyMedium,
                        color = AppColors.OnSurface
                    )
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intent.time?.takeIf { it.isNotBlank() }?.let {
                            FieldPill(modifier = Modifier.weight(1f), title = "时间", value = it)
                        }
                        intent.location?.takeIf { it.isNotBlank() }?.let {
                            FieldPill(modifier = Modifier.weight(1f), title = "地点", value = it)
                        }
                    }
                    Button(
                        onClick  = onConfirmExecution,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape    = RoundedCornerShape(AppShapes.Full),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            contentColor   = AppColors.OnPrimary
                        )
                    ) {
                        Text("确认加入日程", style = AppTypography.LabelMedium)
                    }
                }
            }
        }
    }
}

// ── 下次提醒信息卡 ─────────────────────────────────────────────────────────────
@Composable
private fun NextReminderInfoCard(
    nextAgenda: AgendaCardData?,
    nextReminderText: String
) {
    TimeWeaverCard(containerColor = TimeWeaverSurface.copy(alpha = 0.82f)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = TimeWeaverOrange,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "下次提醒",
                color = TimeWeaverInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = TimeWeaverOrange.copy(alpha = 0.10f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.8.dp, TimeWeaverOrange.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = TimeWeaverSurface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoGraph,
                            contentDescription = null,
                            tint = TimeWeaverOrange,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                if (nextAgenda != null) {
                    Text(
                        text = nextAgenda.title,
                        color = TimeWeaverInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = nextReminderText.ifBlank {
                            "${nextAgenda.displayDateLabel()} · ${nextAgenda.displayTimeLabel()}"
                        },
                        color = TimeWeaverOrange,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = nextReminderText.ifBlank { "暂无即将到来的提醒" },
                        color = TimeWeaverMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCapsuleRow(
    presets: List<PromptPreset>,
    enabled: Boolean,
    onPresetClick: (PromptPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "快速模板",
            color = TimeWeaverInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "用高频校园模板快速进入识别链路，减少手动组织通知的时间。",
            color = TimeWeaverMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        presets.chunked(2).forEachIndexed { rowIndex, rowPresets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPresets.forEachIndexed { columnIndex, preset ->
                    val index = rowIndex * 2 + columnIndex
                    val accent = when (index % 4) {
                        0 -> TimeWeaverAccent
                        1 -> TimeWeaverBlue
                        2 -> TimeWeaverGreen
                        else -> TimeWeaverOrange
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = accent.copy(alpha = 0.18f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = presetIconFor(index),
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = preset.label,
                                color = TimeWeaverInk,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = preset.prompt.take(38),
                                color = TimeWeaverMuted,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                            TimeWeaverButton(
                                modifier = Modifier.fillMaxWidth(),
                                label = "使用",
                                onClick = { onPresetClick(preset) },
                                enabled = enabled,
                                background = TimeWeaverInk,
                                contentColor = Color.White,
                                minHeight = 40.dp,
                                cornerRadius = 18.dp,
                                textSize = 12.sp
                            )
                        }
                    }
                }
                if (rowPresets.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WorkbenchBentoSection(
    context: Context,
    showLivePreview: Boolean,
    previewArmed: Boolean,
    onArmPreview: () -> Unit,
    isCameraAvailable: Boolean,
    bindPreview: (PreviewView) -> Unit,
    summary: String,
    importedSourceLabel: String?,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    isLoading: Boolean,
    isVoiceListening: Boolean,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 760.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewBentoCard(
                    showLivePreview = showLivePreview,
                    previewArmed = previewArmed,
                    onArmPreview = onArmPreview,
                    isCameraAvailable = isCameraAvailable,
                    bindPreview = bindPreview,
                    context = context
                )
                ResultBentoCard(
                    summary = summary,
                    importedSourceLabel = importedSourceLabel,
                    confirmationIntent = confirmationIntent,
                    confirmationSuggestion = confirmationSuggestion,
                    showConfirmationCard = showConfirmationCard,
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening,
                    onConfirmExecution = onConfirmExecution,
                    onCancelExecution = onCancelExecution
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreviewBentoCard(
                    modifier = Modifier.weight(1f),
                    showLivePreview = showLivePreview,
                    previewArmed = previewArmed,
                    onArmPreview = onArmPreview,
                    isCameraAvailable = isCameraAvailable,
                    bindPreview = bindPreview,
                    context = context
                )
                ResultBentoCard(
                    modifier = Modifier.weight(1f),
                    summary = summary,
                    importedSourceLabel = importedSourceLabel,
                    confirmationIntent = confirmationIntent,
                    confirmationSuggestion = confirmationSuggestion,
                    showConfirmationCard = showConfirmationCard,
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening,
                    onConfirmExecution = onConfirmExecution,
                    onCancelExecution = onCancelExecution
                )
            }
        }
    }
}

@Composable
private fun PreviewBentoCard(
    modifier: Modifier = Modifier,
    showLivePreview: Boolean,
    previewArmed: Boolean,
    onArmPreview: () -> Unit,
    isCameraAvailable: Boolean,
    bindPreview: (PreviewView) -> Unit,
    context: Context
) {
    TimeWeaverCard(
        modifier = modifier,
        containerColor = Color.White.copy(alpha = 0.96f)
    ) {
        Text(
            text = "输入源画面",
            color = TimeWeaverInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (previewArmed) 278.dp else 220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TimeWeaverInk),
            contentAlignment = Alignment.Center
        ) {
            if (showLivePreview && isCameraAvailable && previewArmed) {
                val previewView = remember(context) {
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                }
                LaunchedEffect(previewView) {
                    bindPreview(previewView)
                }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewView }
                )
                DisposableEffect(previewView) {
                    onDispose {
                        CameraManager.unbindPreview()
                    }
                }
            } else if (showLivePreview && isCameraAvailable) {
                MockLensPlaceholder(
                    title = "相机已收纳到待机状态",
                    description = "需要拍海报时再点击打开，相机不会在首页空转，切页会更轻。",
                    actionLabel = "打开相机",
                    onAction = onArmPreview
                )
            } else {
                MockLensPlaceholder()
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InputChip("相机")
            InputChip(
                when {
                    showLivePreview && isCameraAvailable && previewArmed -> "在线预览"
                    showLivePreview && isCameraAvailable -> "待机"
                    else -> "静态导入"
                }
            )
        }
    }
}

@Composable
private fun MockLensPlaceholder(
    title: String = "这里会展示当前拍摄或导入的画面",
    description: String = "你也可以直接通过截图、分享或粘贴进入同一条识别链路。",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(82.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            color = Color(0xFFD4E0EC),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            TimeWeaverButton(
                label = actionLabel,
                onClick = onAction,
                background = TimeWeaverSurface,
                contentColor = TimeWeaverInk,
                minHeight = 42.dp,
                cornerRadius = 999.dp,
                textSize = 13.sp
            )
        }
    }
}

@Composable
private fun ResultBentoCard(
    modifier: Modifier = Modifier,
    summary: String,
    importedSourceLabel: String?,
    confirmationIntent: ExecutableIntent?,
    confirmationSuggestion: ExecutionSuggestion?,
    showConfirmationCard: Boolean,
    isLoading: Boolean,
    isVoiceListening: Boolean,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit
) {
    val confidence = resolveConfidence(confirmationIntent, confirmationSuggestion, isLoading, isVoiceListening)
    val tone = confidenceTone(confidence)
    val requiresReview = confidence < 0.9f
    val hasActiveWorkspace = importedSourceLabel?.isNotBlank() == true ||
        confirmationIntent != null ||
        isLoading ||
        isVoiceListening

    TimeWeaverCard(
        modifier = modifier,
        containerColor = Color.White.copy(alpha = 0.98f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "识别结果",
                    color = TimeWeaverInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "系统只做提取与建议，最终是否写入由你确认。",
                    color = TimeWeaverMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = tone.copy(alpha = 0.14f)
            ) {
                Text(
                    text = if (showConfirmationCard) "待确认" else "已收束",
                    color = tone,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        if (!hasActiveWorkspace) {
            WorkspaceEmptyCard(importedSourceLabel = importedSourceLabel)
        } else {
            BentoInfoCard(
                modifier = Modifier.fillMaxWidth(),
                title = importedSourceLabel?.ifBlank { "来源" } ?: "来源",
                content = summary,
                accent = tone
            )

            WorkbenchFlowCard(
                importedSourceLabel = importedSourceLabel,
                showConfirmationCard = showConfirmationCard,
                hasStructuredResult = confirmationIntent != null,
                isLoading = isLoading,
                isVoiceListening = isVoiceListening
            )

            ConfidenceCard(confidence = confidence, tone = tone)

            if (confirmationIntent != null) {
                ExtractedFieldsCard(
                    intent = confirmationIntent,
                    suggestion = confirmationSuggestion
                )
            } else {
                WorkspaceEmptyCard(importedSourceLabel = importedSourceLabel)
            }
        }

        if (showConfirmationCard && confirmationIntent != null && confirmationSuggestion != null) {
            ConfirmationCard(
                intent = confirmationIntent,
                suggestion = confirmationSuggestion,
                confidence = confidence,
                requiresReview = requiresReview,
                onConfirmExecution = onConfirmExecution,
                onCancelExecution = onCancelExecution
            )
        } else if (confirmationIntent != null && confirmationSuggestion != null) {
            PassiveResultFooter(
                action = confirmationIntent.action,
                confidence = confidence,
                summary = when (confirmationIntent.action) {
                    "create_event" -> "结果已具备写入时间线的结构，但当前不需要再次确认。"
                    "navigate" -> "地点结果已经稳定，可在需要时进入导航。"
                    else -> "当前结果已进入系统建议阶段。"
                }
            )
        }
    }
}

@Composable
private fun WorkspaceEmptyCard(
    importedSourceLabel: String?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = TimeWeaverInk
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = TimeWeaverAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = if (importedSourceLabel.isNullOrBlank()) "等待新的校园信息进入工作台" else "等待结构化结果",
                color = TimeWeaverInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (importedSourceLabel.isNullOrBlank()) {
                    "先从上方导入一条通知，系统会在这里收束成标题、时间、地点和确认动作。"
                } else {
                    "素材已经进入系统，识别完成后这里会自动生成标题、时间、地点和执行建议。"
                },
                color = TimeWeaverMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun PassiveResultFooter(
    action: String,
    confidence: Float,
    summary: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FD)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "系统建议",
                color = TimeWeaverInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary,
                color = TimeWeaverMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            Text(
                text = "动作：$action · 置信度 ${(confidence * 100).roundToInt()}%",
                color = TimeWeaverMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun WorkbenchFlowCard(
    importedSourceLabel: String?,
    showConfirmationCard: Boolean,
    hasStructuredResult: Boolean,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F7FC)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "本次工作流",
                color = TimeWeaverInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WorkbenchStagePill(
                    modifier = Modifier.weight(1f),
                    title = "导入",
                    summary = importedSourceLabel?.ifBlank { "等待素材" } ?: "等待素材",
                    active = importedSourceLabel?.isNotBlank() == true || isLoading || isVoiceListening
                )
                WorkbenchStagePill(
                    modifier = Modifier.weight(1f),
                    title = "抽取",
                    summary = if (hasStructuredResult || showConfirmationCard) "字段已生成" else "待结构化",
                    active = hasStructuredResult || showConfirmationCard || isLoading
                )
                WorkbenchStagePill(
                    modifier = Modifier.weight(1f),
                    title = "确认",
                    summary = if (showConfirmationCard) "待你确认" else "等待建议",
                    active = showConfirmationCard
                )
            }
        }
    }
}

@Composable
private fun WorkbenchStagePill(
    modifier: Modifier = Modifier,
    title: String,
    summary: String,
    active: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (active) Color.White else TimeWeaverSurfaceAlt
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) TimeWeaverAccent else Color(0xFFD6DFEA))
            )
            Text(
                text = title,
                color = TimeWeaverInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summary,
                color = TimeWeaverMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ConfidenceCard(
    confidence: Float,
    tone: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(22.dp)
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
                Text(
                    text = "信心指数",
                    color = TimeWeaverInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                MetricText(
                    value = "${(confidence * 100).roundToInt()}%",
                    color = tone,
                    fontSize = 22.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(TimeWeaverTint)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(confidence.coerceIn(0.06f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(tone)
                )
            }
            Text(
                text = when {
                    confidence >= 0.9f -> "高置信度，可直接进入确认写入。"
                    confidence >= 0.6f -> "存在少量模糊字段，建议你快速核对后再写入。"
                    else -> "识别仍不稳定，建议补充文本或重新导入。"
                },
                color = TimeWeaverMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ExtractedFieldsCard(
    intent: ExecutableIntent,
    suggestion: ExecutionSuggestion?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "我整理出了这些信息",
                color = TimeWeaverInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "织时不会只给一句自然语言总结，而是把画面压缩成可执行字段，供时间线、提醒和系统动作复用。",
                color = TimeWeaverMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FieldPill(
                    modifier = Modifier.weight(1f),
                    title = "标题",
                    value = intent.title ?: "待确认"
                )
                FieldPill(
                    modifier = Modifier.weight(1f),
                    title = "时间",
                    value = intent.time ?: "待确认"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FieldPill(
                    modifier = Modifier.weight(1f),
                    title = "地点",
                    value = intent.location ?: "待确认"
                )
                FieldPill(
                    modifier = Modifier.weight(1f),
                    title = "动作",
                    value = when (intent.action) {
                        "create_event" -> "加入时间线"
                        "navigate" -> "生成导航"
                        "clarification" -> "继续追问"
                        "tts_feedback" -> "语音摘要"
                        else -> suggestion?.mode?.name ?: intent.action
                    }
                )
            }
            intent.description?.takeIf { it.isNotBlank() }?.let { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "补充说明",
                            color = TimeWeaverMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = note,
                            color = TimeWeaverInk,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationCard(
    intent: ExecutableIntent,
    suggestion: ExecutionSuggestion,
    confidence: Float,
    requiresReview: Boolean,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit
) {
    val confirmEnabled = suggestion.mode == ExecutionMode.REQUIRE_CONFIRMATION && confidence >= 0.6f

    Card(
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLow),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        border    = BorderStroke(1.dp, AppColors.OutlineVariant.copy(alpha = 0.40f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            // Decorative blob
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .blur(40.dp)
                    .background(AppColors.SecondaryFixed.copy(alpha = 0.50f), androidx.compose.foundation.shape.CircleShape)
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Fields
                ConfirmationFieldRow(
                    icon  = Icons.Rounded.EventNote,
                    label = "事项",
                    value = intent.title ?: "待确认",
                    tint  = AppColors.Secondary,
                    showDivider = true
                )
                ConfirmationFieldRow(
                    icon  = Icons.Rounded.Schedule,
                    label = "时间",
                    value = intent.time ?: "待确认",
                    tint  = AppColors.PrimaryFixedDim,
                    showDivider = true
                )
                ConfirmationFieldRow(
                    icon  = Icons.Rounded.LocationOn,
                    label = "地点",
                    value = intent.location ?: "待确认",
                    tint  = AppColors.SurfaceDim,
                    showDivider = true
                )
                ConfirmationFieldRow(
                    icon  = Icons.Rounded.Notes,
                    label = "备注",
                    value = intent.description ?: "无",
                    tint  = AppColors.Outline,
                    showDivider = true
                )
                // Reminder chips row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text  = "提醒",
                        style = AppTypography.LabelSmall,
                        color = AppColors.Outline
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReminderChip("提前 30 分钟")
                        ReminderChip("提前 1 天")
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Info box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppShapes.Medium))
                        .background(AppColors.ErrorContainer.copy(alpha = 0.50f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Info,
                        contentDescription = null,
                        tint               = AppColors.Error,
                        modifier           = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Text(
                        text  = when {
                            !requiresReview -> "信息如果不太清楚，可以先修改补充后再加入日程哦。"
                            confidence >= 0.6f -> "当前结果需要你重点核对时间与地点，再决定是否写入。"
                            else -> "当前结果置信度偏低，建议重新导入更清晰的通知素材。"
                        },
                        style = AppTypography.BodyMedium,
                        color = AppColors.OnErrorContainer
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "修改信息" — ghost secondary button
                    androidx.compose.material3.OutlinedButton(
                        onClick   = onCancelExecution,
                        modifier  = Modifier.weight(1f).height(52.dp),
                        shape     = RoundedCornerShape(AppShapes.Full),
                        border    = BorderStroke(0.dp, Color.Transparent),
                        colors    = ButtonDefaults.outlinedButtonColors(
                            containerColor = AppColors.SurfaceContainerHigh,
                            contentColor   = AppColors.Primary
                        )
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("修改信息", style = AppTypography.LabelMedium)
                    }
                    // "确认加入日程" — filled primary button
                    Button(
                        onClick  = onConfirmExecution,
                        enabled  = confirmEnabled,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(AppShapes.Full),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = AppColors.Primary,
                            contentColor           = AppColors.OnPrimary,
                            disabledContainerColor = AppColors.SurfaceContainerHighest,
                            disabledContentColor   = AppColors.Outline
                        )
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("确认加入日程", style = AppTypography.LabelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationFieldRow(
    icon:        ImageVector,
    label:       String,
    value:       String,
    tint:        Color,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = label, style = AppTypography.LabelSmall, color = AppColors.Outline)
                Text(text = value, style = AppTypography.BodyLarge, color = AppColors.OnSurface)
            }
        }
        if (showDivider) {
            Divider(color = AppColors.OutlineVariant.copy(alpha = 0.40f), thickness = 1.dp)
        }
    }
}

@Composable
private fun ReminderChip(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppShapes.Full))
            .background(AppColors.PrimaryFixed.copy(alpha = 0.30f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.Rounded.NotificationsActive,
            contentDescription = null,
            tint               = AppColors.PrimaryContainer,
            modifier           = Modifier.size(14.dp)
        )
        Text(text = label, style = AppTypography.LabelSmall, color = AppColors.PrimaryContainer)
    }
}

@Composable
internal fun ProfilePreferenceRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(14.dp),
            color = TimeWeaverInk
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TimeWeaverAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = TimeWeaverInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = TimeWeaverMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun ProfileHeroCard(
    confirmedAgendaCount:   Int,
    pendingAgendaCount:     Int,
    scheduledReminderCount: Int
) {
    // Banner
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SecondaryFixed.copy(alpha = 0.40f)),
        border    = BorderStroke(1.dp, AppColors.OutlineVariant.copy(alpha = 0.30f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            Box(
                modifier = Modifier.size(120.dp).align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp).blur(40.dp)
                    .background(AppColors.Secondary.copy(alpha = 0.20f), androidx.compose.foundation.shape.CircleShape)
            )
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("本周提醒完成率", style = AppTypography.LabelSmall, color = AppColors.OnSurfaceVariant)
                    Text(
                        text  = if (confirmedAgendaCount > 0) "${(confirmedAgendaCount * 85 / (confirmedAgendaCount + pendingAgendaCount.coerceAtLeast(1)))}%" else "–",
                        style = AppTypography.DisplayLarge,
                        color = AppColors.Primary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShapes.Full))
                        .background(AppColors.Primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = {}
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.EditCalendar, contentDescription = null, tint = AppColors.OnPrimary, modifier = Modifier.size(16.dp))
                        Text("新建提醒", style = AppTypography.BodyLarge, color = AppColors.OnPrimary)
                    }
                }
            }
        }
    }
    // Mini stats row
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        listOf(
            Triple(Icons.Rounded.CheckCircle,         "$confirmedAgendaCount",         "已保存"),
            Triple(Icons.Rounded.NotificationsActive,  "$pendingAgendaCount",           "待确认"),
            Triple(Icons.Rounded.Schedule,             "$scheduledReminderCount",       "已提醒")
        ).forEachIndexed { i, (icon, value, label) ->
            val bg = when(i) {
                0 -> AppColors.SurfaceContainerHigh
                1 -> AppColors.SurfaceContainerHighest
                else -> AppColors.PrimaryFixed.copy(alpha = 0.50f)
            }
            val iconBg = when(i) {
                0 -> AppColors.PrimaryFixed
                1 -> AppColors.SecondaryFixed
                else -> AppColors.SurfaceContainerLowest
            }
            val iconTint = when(i) {
                0 -> AppColors.Primary; 1 -> AppColors.Secondary; else -> AppColors.Primary
            }
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(AppShapes.Large)).background(bg).padding(vertical = 14.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                    Text(value, style = AppTypography.HeadlineMedium, color = AppColors.OnSurface)
                    Text(label, style = AppTypography.LabelSmall, color = AppColors.OnSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun DigitalWellbeingCard(
    confirmedAgendaCount: Int,
    pendingAgendaCount: Int
) {
    TimeWeaverCard {
        Text(
            text = "我的统计",
            color = TimeWeaverInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoInfoCard(
                modifier = Modifier.weight(1f),
                title = "已降噪提取",
                content = "${confirmedAgendaCount + pendingAgendaCount} 条通知",
                accent = TimeWeaverGreen
            )
            BentoInfoCard(
                modifier = Modifier.weight(1f),
                title = "减少遗忘风险",
                content = "${(confirmedAgendaCount * 0.7f).roundToInt()} 次",
                accent = TimeWeaverAccent
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoInfoCard(
                modifier = Modifier.weight(1f),
                title = "已加入日程",
                content = "$confirmedAgendaCount 条正式安排",
                accent = TimeWeaverBlue
            )
            BentoInfoCard(
                modifier = Modifier.weight(1f),
                title = "待你处理",
                content = if (pendingAgendaCount == 0) "当前没有悬而未决的建议" else "$pendingAgendaCount 条待确认建议",
                accent = TimeWeaverDanger
            )
        }
    }
}

@Composable
private fun ProfileQuickActionsCard(
    reminderCount: Int,
    onOpenPlan: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.ExtraLarge),
        colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
        border    = BorderStroke(1.dp, AppColors.OutlineVariant.copy(alpha = 0.20f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            ProfileMenuRow(
                icon     = Icons.Rounded.Timeline,
                iconBg   = AppColors.PrimaryFixed.copy(alpha = 0.30f),
                iconTint = AppColors.Primary,
                title    = "时间线中心",
                onClick  = onOpenPlan
            )
            Divider(color = AppColors.OutlineVariant.copy(alpha = 0.20f), modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuRow(
                icon     = Icons.Rounded.NotificationsActive,
                iconBg   = AppColors.SecondaryFixed.copy(alpha = 0.30f),
                iconTint = AppColors.Secondary,
                title    = "提醒设置（$reminderCount 条）",
                onClick  = {}
            )
            Divider(color = AppColors.OutlineVariant.copy(alpha = 0.20f), modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuRow(
                icon     = Icons.Rounded.Share,
                iconBg   = AppColors.SurfaceDim.copy(alpha = 0.30f),
                iconTint = AppColors.OnSurface,
                title    = "导出记录",
                onClick  = {}
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon:    ImageVector,
    iconBg:  Color,
    iconTint: Color,
    title:   String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(iconBg)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(title, style = AppTypography.BodyLarge, color = AppColors.OnSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Timeline, contentDescription = null, tint = AppColors.OnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PreferencesBoard(
    showLivePreview: Boolean,
    isCameraAvailable: Boolean,
    reminderLeadMinutes: Int,
    reminderDayEnabled: Boolean,
    reminderHourEnabled: Boolean,
    blockHighRisk: Boolean,
    muteLowConfidence: Boolean,
    autoMapLink: Boolean,
    onReminderLeadMinutesChange: (Int) -> Unit,
    onReminderDayEnabledChange: (Boolean) -> Unit,
    onReminderHourEnabledChange: (Boolean) -> Unit,
    onBlockHighRiskChange: (Boolean) -> Unit,
    onMuteLowConfidenceChange: (Boolean) -> Unit,
    onAutoMapLinkChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("偏好设置", style = AppTypography.BodyLarge, color = AppColors.OnSurface, modifier = Modifier.padding(horizontal = 4.dp))
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(AppShapes.ExtraLarge),
            colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
            border    = BorderStroke(1.dp, AppColors.OutlineVariant.copy(alpha = 0.20f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) { Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        ProfilePreferenceRow(
            icon = Icons.Rounded.CalendarMonth,
            title = "默认提醒策略",
            value = buildString {
                append(if (reminderDayEnabled) "提前 1 天" else "关闭日级提醒")
                append(" + ")
                append(if (reminderHourEnabled) "提前 ${reminderLeadMinutes} 分钟" else "关闭分钟提醒")
            }
        )
        LeadMinuteSelector(
            selectedMinutes = reminderLeadMinutes,
            onSelected = onReminderLeadMinutesChange
        )
        SwitchPreferenceRow(
            icon = Icons.Rounded.NotificationsActive,
            title = "日级提醒",
            value = "是否保留提前一天的本地提醒",
            checked = reminderDayEnabled,
            onCheckedChange = onReminderDayEnabledChange
        )
        SwitchPreferenceRow(
            icon = Icons.Rounded.NotificationsActive,
            title = "分钟级提醒",
            value = "是否在开始前再次提醒",
            checked = reminderHourEnabled,
            onCheckedChange = onReminderHourEnabledChange
        )
        ProfilePreferenceRow(
            icon = Icons.Rounded.EditCalendar,
            title = "系统日历写入",
            value = "识别成功后先确认，再打开系统日历"
        )
        SwitchPreferenceRow(
            icon = Icons.Rounded.Shield,
            title = "自动拦截高风险指令",
            value = "保持人在回路，避免误执行高风险动作",
            checked = blockHighRisk,
            onCheckedChange = onBlockHighRiskChange
        )
        SwitchPreferenceRow(
            icon = Icons.Rounded.AutoGraph,
            title = "信息不清楚时优先提示",
            value = "低置信度时优先提示补充，而不是强推执行",
            checked = muteLowConfidence,
            onCheckedChange = onMuteLowConfidenceChange
        )
        SwitchPreferenceRow(
            icon = Icons.Rounded.LocationOn,
            title = "地点解析与地图联动",
            value = "识别出地点时生成导航入口",
            checked = autoMapLink,
            onCheckedChange = onAutoMapLinkChange
        )
        ProfilePreferenceRow(
            icon = Icons.Rounded.Share,
            title = "导入入口",
            value = if (showLivePreview && isCameraAvailable) "拍照 + 分享 + 粘贴 + 语音全入口可用" else "以相册、分享和粘贴入口为主"
        )
        } } // Card + Column end
    } // outer Column end
}

@Composable
private fun ProfileStatusOverviewCard(
    importedSourceLabel: String,
    showLivePreview: Boolean,
    isCameraAvailable: Boolean,
    reminderStateText: String,
    nextReminderText: String,
    statusText: String
) {
    TimeWeaverCard(containerColor = TimeWeaverSurfaceAlt) {
        Text(
            text = "当前状态",
            color = TimeWeaverInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoInfoCard(
                modifier = Modifier.weight(1f),
                title = "最近导入",
                content = importedSourceLabel.ifBlank { "还没有新的碎片被织入" },
                accent = TimeWeaverBlue
            )
            BentoInfoCard(
                modifier = Modifier.weight(1f),
                title = "输入通道",
                content = if (showLivePreview && isCameraAvailable) "相机待机，可随时拍摄" else "静态导入模式",
                accent = TimeWeaverGreen
            )
        }
        BentoInfoCard(
            title = "提醒引擎",
            content = reminderStateText,
            accent = TimeWeaverAccent
        )
        BentoInfoCard(
            title = "下次提醒",
            content = nextReminderText,
            accent = TimeWeaverBlue
        )
        if (statusText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Text(
                    text = statusText,
                    color = TimeWeaverMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun LeadMinuteSelector(
    selectedMinutes: Int,
    onSelected: (Int) -> Unit
) {
    val options = listOf(15, 30, 60)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            val selected = value == selectedMinutes
            Surface(
                modifier = Modifier.clickable { onSelected(value) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) TimeWeaverInk else TimeWeaverSurfaceAlt
            ) {
                Text(
                    text = when (value) {
                        60 -> "提前 1 小时"
                        else -> "提前 $value 分钟"
                    },
                    color = if (selected) Color.White else TimeWeaverInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SwitchPreferenceRow(
    icon: ImageVector,
    title: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(14.dp),
            color = TimeWeaverInk
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TimeWeaverAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = TimeWeaverInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = TimeWeaverMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TimeWeaverBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCAD7E4)
            )
        )
    }
}

@Composable
private fun BadgeWallCard() {
    TimeWeaverCard {
        Text(
            text = "成就徽章",
            color = TimeWeaverInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "把长期坚持管理通知与日程的过程变成可见的成果墙，提升“真实 App”气质，也更方便在汇报中表达用户成长感。",
            color = TimeWeaverMuted,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                "海报捕手" to "连续整理 3 张海报",
                "期末生存者" to "考试周保持提醒不断档",
                "低噪规划师" to "连续 7 天保持时间线整洁",
                "群通知驯服者" to "成功整理 10 条群通知"
            ).forEachIndexed { index, pair ->
                Card(
                    modifier = Modifier.width(168.dp),
                    colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = when (index % 3) {
                                0 -> TimeWeaverAccent.copy(alpha = 0.24f)
                                1 -> TimeWeaverBlue.copy(alpha = 0.18f)
                                else -> TimeWeaverGreen.copy(alpha = 0.2f)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.WorkspacePremium,
                                    contentDescription = null,
                                    tint = TimeWeaverInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = pair.first,
                            color = TimeWeaverInk,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pair.second,
                            color = TimeWeaverMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    modifier: Modifier = Modifier,
    title: String,
    summary: String,
    icon: ImageVector,
    accent: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(text = title, color = TimeWeaverInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = summary, color = TimeWeaverMuted, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
internal fun TimeWeaverButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    background: Color = TimeWeaverInk,
    contentColor: Color = Color.White,
    minHeight: Dp = 54.dp,
    cornerRadius: Dp = 22.dp,
    textSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    withHaptic: Boolean = false
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "timeweaver-button-scale"
    )
    val brush = when (background) {
        TimeWeaverInk, TimeWeaverBlue -> weavingPrimaryBrush()
        TimeWeaverAccent -> Brush.linearGradient(
            listOf(
                TimeWeaverAccent,
                Color(0xFF57FFA6)
            )
        )
        TimeWeaverSurface, TimeWeaverSurfaceAlt -> Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.96f),
                TimeWeaverSurfaceAlt.copy(alpha = 0.96f)
            )
        )
        else -> Brush.linearGradient(listOf(background, background))
    }
    val resolvedContentColor = if (background == TimeWeaverInk || background == TimeWeaverBlue) {
        Color.White
    } else {
        contentColor
    }
    val borderColor = if (background == TimeWeaverSurface || background == TimeWeaverSurfaceAlt) {
        TimeWeaverLine.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .height(minHeight)
            .scale(scale)
            .clip(shape)
            .background(brush = brush, shape = shape)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                }
            )
    ) {
        Button(
            modifier = Modifier.fillMaxSize(),
            onClick = {
                if (enabled && withHaptic) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            },
            enabled = enabled,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = resolvedContentColor.copy(alpha = 0.52f)
            ),
            shape = shape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = label, fontSize = textSize, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun TimelineTransferOverlay(
    isVisible: Boolean,
    label: String = "已加入日程"
) {
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 720,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "timeline-transfer-progress"
    )
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "timeline-transfer-alpha"
    )

    if (!isVisible && alpha <= 0.01f) return

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val startX = width * 0.42f
        val endX = width * 0.67f
        val startY = height * 0.68f
        val endY = height * 0.9f
        val currentX = startX + (endX - startX) * progress
        val currentY = startY + (endY - startY) * progress
        val scale = 1f - (0.32f * progress)

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = currentX.roundToInt(),
                            y = currentY.roundToInt()
                        )
                    }
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(TimeWeaverPillRadius))
                    .background(
                        brush = weavingPrimaryBrush(),
                        shape = RoundedCornerShape(TimeWeaverPillRadius)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timeline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
private fun BentoInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent)
            )
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
                    fontSize = 18.sp
                )
            } else {
                Text(
                    text = content,
                    color = TimeWeaverInk,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FieldPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurface),
        shape = RoundedCornerShape(TimeWeaverNestedRadius)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, color = TimeWeaverMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = TimeWeaverInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun InputChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = TimeWeaverSurfaceAlt
    ) {
        Text(
            text = label,
            color = TimeWeaverInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PrimaryActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    background: Color,
    contentColor: Color,
    enabled: Boolean,
    withHaptic: Boolean = false,
    onClick: () -> Unit
) {
    TimeWeaverButton(
        modifier = modifier,
        label = label,
        onClick = onClick,
        enabled = enabled,
        icon = icon,
        background = background,
        contentColor = contentColor,
        withHaptic = withHaptic
    )
}

@Composable
private fun InlineShimmerWorkspace(
    title: String,
    lines: List<String>
) {
    val shimmer = rememberInfiniteTransition(label = "inline-workspace-shimmer")
    val shift = shimmer.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "workspace-shimmer-shift"
    ).value

    Card(
        colors = CardDefaults.cardColors(containerColor = TimeWeaverSurfaceAlt),
        shape = RoundedCornerShape(TimeWeaverCardRadius)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TimeWeaverInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            lines.forEachIndexed { index, line ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = line,
                        color = TimeWeaverMuted,
                        fontSize = 12.sp
                    )
                    ShimmerLine(
                        widthFraction = when (index) {
                            0 -> 0.94f
                            1 -> 0.82f
                            else -> 0.64f
                        },
                        shift = shift
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerLine(
    widthFraction: Float,
    shift: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(12.dp)
            .clip(RoundedCornerShape(TimeWeaverPillRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color(0xFFDBF5EC).copy(alpha = 0.64f),
                        Color(0xFFE1EBE0).copy(alpha = 0.48f),
                        Color.White.copy(alpha = 0.18f)
                    ),
                    start = Offset(600f * shift - 240f, 0f),
                    end = Offset(600f * shift, 0f)
                )
            )
    )
}

@Composable
private fun QuickPillAction(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = TimeWeaverSurfaceAlt,
            contentColor = TimeWeaverInk
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun resolveConfidence(
    intent: ExecutableIntent?,
    suggestion: ExecutionSuggestion?,
    isLoading: Boolean,
    isVoiceListening: Boolean
): Float {
    return when {
        isLoading -> 0.62f
        isVoiceListening -> 0.54f
        intent != null -> intent.fusedConfidence.toFloat()
        suggestion != null -> suggestion.threshold.toFloat()
        else -> 0.38f
    }.coerceIn(0f, 1f)
}

private fun confidenceTone(confidence: Float): Color {
    return when {
        confidence >= 0.9f -> TimeWeaverGreen
        confidence >= 0.6f -> TimeWeaverOrange
        else -> TimeWeaverDanger
    }
}

private fun presetIconFor(index: Int): ImageVector {
    return when (index % 5) {
        0 -> Icons.Rounded.Event
        1 -> Icons.Rounded.CalendarMonth
        2 -> Icons.Rounded.Campaign
        3 -> Icons.Rounded.LocationOn
        else -> Icons.Rounded.FolderShared
    }
}
