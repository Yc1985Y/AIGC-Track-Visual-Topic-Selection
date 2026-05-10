package com.vsa.visualsemanticagent.ui.home

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.vsa.visualsemanticagent.decision.ExecutableIntent
import com.vsa.visualsemanticagent.decision.ExecutionSuggestion
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.displayTimeLabel
import com.vsa.visualsemanticagent.plan.scheduleDate
import com.vsa.visualsemanticagent.ui.AppColors
import com.vsa.visualsemanticagent.ui.AppShapes
import com.vsa.visualsemanticagent.ui.AppSpacing
import com.vsa.visualsemanticagent.ui.AppTypography
import com.vsa.visualsemanticagent.ui.common.EmptyStateCard
import com.vsa.visualsemanticagent.ui.common.WeavingBackground
import com.vsa.visualsemanticagent.ui.common.WeavingChip
import com.vsa.visualsemanticagent.ui.common.WeavingGlassCard
import com.vsa.visualsemanticagent.ui.common.WeavingIconBubble
import com.vsa.visualsemanticagent.ui.common.WeavingPrimaryButton
import com.vsa.visualsemanticagent.ui.common.WeavingSectionTitle
import com.vsa.visualsemanticagent.utils.PromptPreset
import java.time.LocalDate

@Composable
fun HomeScreenModule(
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
    todayAgendaCount: Int = 0,
    onOpenTimeline: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null,
    onOpenRecentResults: (() -> Unit)? = null
) {
    val todayItems = remember(agendaItems) {
        agendaItems.filter { it.scheduleDate() == LocalDate.now() }
    }
    val pendingTitle = confirmationIntent?.title ?: "待确认安排"
    val pendingTime = confirmationIntent?.time ?: "请先检查时间"
    val pendingLocation = confirmationIntent?.location ?: "请补充地点"
    val confidence = (
        confirmationIntent?.fusedConfidence
            ?: confirmationSuggestion?.threshold
            ?: 0.0
        ) * 100
    val summary = resultText?.takeIf { it.isNotBlank() }
        ?: statusText?.takeIf { it.isNotBlank() }
        ?: "把海报、截图、群通知或一段话交给织时，我们会先整理，再请你确认。"

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
                HomeGreeting(
                    todayAgendaCount = todayAgendaCount,
                    onOpenTimeline = onOpenTimeline,
                    onOpenProfile = onOpenProfile
                )
            }

            item {
                HomeStatRow(
                    todayAgendaCount = todayAgendaCount,
                    pendingCount = agendaItems.count { it.status.contains("待") },
                    nextReminderText = nextReminderText,
                    onOpenTimeline = onOpenTimeline
                )
            }

            item {
                InputEnergyHub(
                    commandText = commandText,
                    onCommandChanged = onCommandChanged,
                    onSubmitCommandClick = onSubmitCommandClick,
                    onCaptureClick = onCaptureClick,
                    onVoiceClick = onVoiceClick,
                    onPickImageClick = onPickImageClick,
                    onPasteTextClick = onPasteTextClick,
                    showLivePreview = showLivePreview,
                    isCameraAvailable = isCameraAvailable,
                    bindPreview = bindPreview,
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening
                )
            }

            item {
                WeavingSectionTitle(
                    title = "最近识别结果",
                    subtitle = "让 AI 先降噪，再决定是否织入时间线",
                    action = "查看全部",
                    onActionClick = onOpenRecentResults ?: onOpenTimeline
                )
            }

            if (showConfirmationCard && confirmationIntent != null && confirmationSuggestion != null) {
                item {
                    PendingReviewCard(
                        title = pendingTitle,
                        time = pendingTime,
                        location = pendingLocation,
                        importedSourceLabel = importedSourceLabel.orEmpty(),
                        confidence = confidence.toInt().coerceIn(0, 100),
                        summary = confirmationSuggestion.prompt,
                        onConfirmExecution = onConfirmExecution,
                        onCancelExecution = onCancelExecution
                    )
                }
            } else {
                item {
                    RecentRecognitionCard(summary = summary)
                }
            }

            item {
                TodayAgendaCard(todayItems = todayItems)
            }

            item {
                ScenarioPresetRow(presets = presets, onPresetClick = onPresetClick)
            }
        }
    }
}

@Composable
private fun HomeGreeting(
    todayAgendaCount: Int,
    onOpenTimeline: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeavingIconBubble(
                icon = Icons.Rounded.Person,
                background = AppColors.SurfaceContainer,
                tint = AppColors.Primary,
                onClick = onOpenProfile
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Hi, Elizabeth",
                    style = AppTypography.HeadlineMedium,
                    color = AppColors.Primary
                )
                Text(
                    text = if (todayAgendaCount > 0) "今天有 $todayAgendaCount 项安排待处理" else "今天想把哪条校园通知整理进时间线？",
                    style = AppTypography.BodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
        WeavingIconBubble(
            icon = Icons.Rounded.NotificationsActive,
            background = AppColors.SurfaceContainer,
            tint = AppColors.Primary,
            onClick = onOpenTimeline
        )
    }
}

@Composable
private fun HomeStatRow(
    todayAgendaCount: Int,
    pendingCount: Int,
    nextReminderText: String,
    onOpenTimeline: (() -> Unit)? = null
) {
    val cards = listOf(
        Triple("今日安排", todayAgendaCount.toString(), AppColors.MintAccent),
        Triple("待确认", pendingCount.toString(), AppColors.CoralSoft),
        Triple("下次提醒", if (nextReminderText.isBlank()) "--" else "34", AppColors.GoldSoft)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        cards.forEachIndexed { index, item ->
            WeavingGlassCard(
                modifier = Modifier.weight(1f),
                containerColor = item.third,
                onClick = onOpenTimeline
            ) {
                val icon = when (index) {
                    0 -> Icons.Rounded.CalendarMonth
                    1 -> Icons.Rounded.CheckCircle
                    else -> Icons.Rounded.Schedule
                }
                WeavingIconBubble(
                    icon = icon,
                    background = Color.White.copy(alpha = 0.55f),
                    tint = AppColors.Primary
                )
                Text(
                    text = item.second,
                    style = AppTypography.DisplayLarge,
                    color = AppColors.OnSurface
                )
                Text(
                    text = item.first,
                    style = AppTypography.BodyLarge,
                    color = AppColors.OnSurface
                )
            }
        }
    }
}

@Composable
private fun InputEnergyHub(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onSubmitCommandClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onPickImageClick: () -> Unit,
    onPasteTextClick: () -> Unit,
    showLivePreview: Boolean,
    isCameraAvailable: Boolean,
    bindPreview: (PreviewView) -> Unit,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    WeavingGlassCard(
        containerColor = AppColors.SurfaceContainerHigh.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "把通知变成清楚的日程",
                    style = AppTypography.HeadlineLargeMobile,
                    color = AppColors.OnSurface
                )
                Text(
                    text = "拍海报、选截图、粘贴群消息或语音输入，都能直接进入智能解析。",
                    style = AppTypography.BodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = AppColors.Primary
                )
            }
        }

        if (showLivePreview && isCameraAvailable) {
            Surface(
                shape = RoundedCornerShape(AppShapes.Large),
                tonalElevation = 0.dp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(
                        width = 2.dp,
                        color = AppColors.Primary.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(AppShapes.Large)
                    )
            ) {
                Box {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            PreviewView(context).also(bindPreview)
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                            .border(
                                width = 1.5.dp,
                                color = AppColors.Primary.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(AppShapes.Large)
                            )
                    )
                    Surface(
                        color = AppColors.SurfaceContainerLowest.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(AppShapes.Full),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 18.dp)
                    ) {
                        Text(
                            text = "为了识别更准，请把通知完整放进取景框",
                            style = AppTypography.LabelMedium,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                    Surface(
                        color = AppColors.Primary.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(AppShapes.Full),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = null,
                                tint = AppColors.OnPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isLoading) "正在整理…" else if (isVoiceListening) "正在倾听…" else "拍照识别",
                                style = AppTypography.LabelMedium,
                                color = AppColors.OnPrimary
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = commandText,
            onValueChange = onCommandChanged,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShapes.Large),
            placeholder = {
                Text(
                    text = "粘贴一段校园通知，我来帮你整理成时间、地点和提醒…",
                    style = AppTypography.BodyLarge,
                    color = AppColors.OnSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.SurfaceContainerLowest.copy(alpha = 0.72f),
                unfocusedContainerColor = AppColors.SurfaceContainerLowest.copy(alpha = 0.72f),
                disabledContainerColor = AppColors.SurfaceContainerLowest.copy(alpha = 0.72f),
                focusedBorderColor = AppColors.Primary.copy(alpha = 0.25f),
                unfocusedBorderColor = AppColors.CardBorder,
                focusedTextColor = AppColors.OnSurface,
                unfocusedTextColor = AppColors.OnSurface,
                cursorColor = AppColors.Primary
            ),
            maxLines = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.PhotoLibrary,
                label = "相册导入",
                background = AppColors.CoralSoft,
                onClick = onPickImageClick
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.ContentPaste,
                label = "粘贴文本",
                background = AppColors.GoldSoft,
                onClick = onPasteTextClick
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Mic,
                label = "语音输入",
                background = AppColors.MintAccent,
                onClick = onVoiceClick
            )
        }

        WeavingPrimaryButton(
            text = if (isLoading) "正在解构通知…" else "开始识别",
            onClick = if (showLivePreview && isCameraAvailable) onCaptureClick else onSubmitCommandClick,
            icon = Icons.Rounded.AutoAwesome,
            enabled = !isLoading && !isVoiceListening,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    background: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppShapes.Large))
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 18.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Primary
            )
        }
        Text(
            text = label,
            style = AppTypography.BodyLarge,
            color = AppColors.OnSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PendingReviewCard(
    title: String,
    time: String,
    location: String,
    importedSourceLabel: String,
    confidence: Int,
    summary: String,
    onConfirmExecution: () -> Unit,
    onCancelExecution: () -> Unit
) {
    WeavingGlassCard(
        containerColor = AppColors.SurfaceContainer
    ) {
        WeavingChip(
            text = if (importedSourceLabel.isBlank()) "待确认" else importedSourceLabel,
            icon = Icons.Rounded.AutoAwesome,
            background = AppColors.CoralSoft,
            contentColor = AppColors.Secondary
        )
        Text(
            text = "我整理出了这些信息",
            style = AppTypography.HeadlineMedium,
            color = AppColors.Primary
        )
        ReviewField("事项", title)
        ReviewField("时间", time)
        ReviewField("地点", location)
        ReviewField("信心指数", "$confidence%")
        Text(
            text = summary,
            style = AppTypography.BodyMedium,
            color = AppColors.OnSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            WeavingGlassCard(
                modifier = Modifier.weight(1f),
                containerColor = AppColors.CoralSoft
            ) {
                Text(
                    text = "信息如果还不清楚，可以先返回补充后再加入。",
                    style = AppTypography.BodyMedium,
                    color = AppColors.Secondary
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            WeavingPrimaryButton(
                text = "确认加入日程",
                onClick = onConfirmExecution,
                icon = Icons.Rounded.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            WeavingPrimaryButton(
                text = "取消",
                onClick = onCancelExecution,
                modifier = Modifier.width(128.dp)
            )
        }
    }
}

@Composable
private fun ReviewField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShapes.Medium))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.LabelMedium,
            color = AppColors.OnSurfaceVariant
        )
        Text(
            text = value,
            style = AppTypography.BodyLarge,
            color = AppColors.OnSurface
        )
    }
}

@Composable
private fun RecentRecognitionCard(summary: String) {
    WeavingGlassCard {
        Text(
            text = "把碎片交给织时",
            style = AppTypography.HeadlineMedium,
            color = AppColors.Primary
        )
        Text(
            text = summary,
            style = AppTypography.BodyMedium,
            color = AppColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun TodayAgendaCard(todayItems: List<AgendaCardData>) {
    if (todayItems.isEmpty()) {
        EmptyStateCard(
            title = "今天暂时没有安排",
            summary = "所有确认后的校园事务都会在这里沉淀成清晰的今日时间线。"
        )
        return
    }

    WeavingGlassCard {
        WeavingSectionTitle(
            title = "今日安排",
            subtitle = "Today-First：先看离你最近的一件事"
        )
        todayItems.take(3).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppShapes.Large))
                    .background(Color.White.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.GoldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timeline,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.title,
                        style = AppTypography.BodyLarge,
                        color = AppColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.displayTimeLabel()} · ${item.location}",
                        style = AppTypography.BodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioPresetRow(
    presets: List<PromptPreset>,
    onPresetClick: (PromptPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        WeavingSectionTitle(
            title = "常用场景",
            subtitle = "把一类校园信息直接送进更适合的解析路径"
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            items(presets, key = { it.id }) { preset ->
                val icon = when (preset.id) {
                    "lecture" -> Icons.Rounded.CalendarMonth
                    "exam" -> Icons.Rounded.Schedule
                    "group_notice" -> Icons.Rounded.ContentPaste
                    else -> Icons.Rounded.AutoAwesome
                }
                Column(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(AppShapes.Large))
                        .background(
                            when (preset.id) {
                                "lecture" -> AppColors.GoldSoft
                                "exam" -> AppColors.CoralSoft
                                "group_notice" -> AppColors.MintAccent
                                else -> AppColors.SurfaceContainer
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPresetClick(preset) }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AppColors.Primary
                        )
                    }
                    Text(
                        text = preset.label,
                        style = AppTypography.BodyLarge,
                        color = AppColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "点击后自动填入这一类校园通知的解析意图。",
                        style = AppTypography.BodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}
