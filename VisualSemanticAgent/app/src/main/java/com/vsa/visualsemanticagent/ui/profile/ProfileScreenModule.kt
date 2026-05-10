package com.vsa.visualsemanticagent.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vsa.visualsemanticagent.ui.AppColors
import com.vsa.visualsemanticagent.ui.AppShapes
import com.vsa.visualsemanticagent.ui.AppSpacing
import com.vsa.visualsemanticagent.ui.AppTypography
import com.vsa.visualsemanticagent.ui.common.WeavingBackground
import com.vsa.visualsemanticagent.ui.common.WeavingGlassCard
import com.vsa.visualsemanticagent.ui.common.WeavingIconBubble
import com.vsa.visualsemanticagent.ui.common.WeavingPrimaryButton
import com.vsa.visualsemanticagent.ui.common.WeavingSectionTitle

@Composable
fun ProfileScreenModule(
    modifier: Modifier = Modifier,
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
    onOpenPlan: () -> Unit,
    onOpenReminderSettings: (() -> Unit)? = null,
    onOpenExportRecords: (() -> Unit)? = null
) {
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
                ProfileHeader()
            }

            item {
                DigitalWellbeingCard(
                    todayAgendaCount = todayAgendaCount,
                    pendingAgendaCount = pendingAgendaCount,
                    scheduledReminderCount = scheduledReminderCount,
                    onNewReminderClick = onOpenReminderSettings
                )
            }

            item {
                AchievementRow(
                    confirmedAgendaCount = confirmedAgendaCount,
                    pendingAgendaCount = pendingAgendaCount,
                    scheduledReminderCount = scheduledReminderCount
                )
            }

            item {
                WeavingSectionTitle(
                    title = "控制中枢",
                    subtitle = "把 AI 的能力边界、提醒策略和兜底联动全部交还给你"
                )
            }

            item {
                PreferenceBoard(
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
                Toolboard(
                    onOpenPlan = onOpenPlan,
                    onOpenReminderSettings = onOpenReminderSettings,
                    onOpenExportRecords = onOpenExportRecords
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader() {
    val context = LocalContext.current
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
                background = AppColors.CoralSoft,
                tint = AppColors.Primary,
                onClick = {
                    Toast.makeText(context, "当前正在查看个人控制中枢", Toast.LENGTH_SHORT).show()
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "我的提醒助手",
                    style = AppTypography.HeadlineLargeMobile,
                    color = AppColors.Primary
                )
                Text(
                    text = "把杂乱通知变成稳定、可控、长期可复用的个人时间资产",
                    style = AppTypography.BodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
        WeavingIconBubble(
            icon = Icons.Rounded.NotificationsActive,
            background = AppColors.GoldSoft,
            tint = AppColors.Primary,
            onClick = {
                Toast.makeText(context, "提醒统计已在下方卡片中展示", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun DigitalWellbeingCard(
    todayAgendaCount: Int,
    pendingAgendaCount: Int,
    scheduledReminderCount: Int,
    onNewReminderClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    WeavingGlassCard(containerColor = AppColors.CoralSoft.copy(alpha = 0.82f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "本周提醒完成率",
                    style = AppTypography.LabelMedium,
                    color = AppColors.OnSurfaceVariant
                )
                Text(
                    text = "85%",
                    style = AppTypography.DisplayLarge,
                    color = AppColors.Primary
                )
            }
            WeavingPrimaryButton(
                text = "新建提醒",
                onClick = onNewReminderClick ?: {
                    Toast.makeText(context, "可在时间线页确认事件后自动生成提醒", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.46f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            MetricPill(
                modifier = Modifier.weight(1f),
                title = "今日安排",
                value = todayAgendaCount.toString(),
                icon = Icons.Rounded.CalendarMonth,
                background = AppColors.GoldSoft
            )
            MetricPill(
                modifier = Modifier.weight(1f),
                title = "待确认",
                value = pendingAgendaCount.toString(),
                icon = Icons.Rounded.Shield,
                background = AppColors.CoralSoft
            )
            MetricPill(
                modifier = Modifier.weight(1f),
                title = "已提醒",
                value = scheduledReminderCount.toString(),
                icon = Icons.Rounded.NotificationsActive,
                background = AppColors.MintAccent
            )
        }
    }
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    background: Color
) {
    WeavingGlassCard(
        modifier = modifier,
        containerColor = background
    ) {
        WeavingIconBubble(icon = icon, background = Color.White.copy(alpha = 0.45f), tint = AppColors.Primary)
        Text(
            text = value,
            style = AppTypography.DisplayLarge,
            color = AppColors.OnSurface
        )
        Text(
            text = title,
            style = AppTypography.BodyLarge,
            color = AppColors.OnSurface
        )
    }
}

@Composable
private fun AchievementRow(
    confirmedAgendaCount: Int,
    pendingAgendaCount: Int,
    scheduledReminderCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "成就勋章",
                style = AppTypography.HeadlineMedium,
                color = AppColors.Primary
            )
            Text(
                text = "查看全部",
                style = AppTypography.LabelMedium,
                color = AppColors.OnSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            BadgeCard(
                modifier = Modifier.weight(1f),
                title = "海报小能手",
                subtitle = "累计沉淀 $confirmedAgendaCount 条安排",
                background = AppColors.GoldSoft
            )
            BadgeCard(
                modifier = Modifier.weight(1f),
                title = "考试周守护者",
                subtitle = "仍有 $pendingAgendaCount 条待确认",
                background = AppColors.MintAccent
            )
            BadgeCard(
                modifier = Modifier.weight(1f),
                title = "低噪规划师",
                subtitle = "已挂载 $scheduledReminderCount 条提醒",
                background = AppColors.CoralSoft
            )
        }
    }
}

@Composable
private fun BadgeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    background: Color
) {
    WeavingGlassCard(
        modifier = modifier,
        containerColor = background
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(Color.White.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoGraph,
                contentDescription = null,
                tint = AppColors.Primary
            )
        }
        Text(
            text = title,
            style = AppTypography.BodyLarge,
            color = AppColors.OnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = AppTypography.BodyMedium,
            color = AppColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun PreferenceBoard(
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
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        WeavingGlassCard {
            PreferenceSwitchRow(
                icon = Icons.Rounded.Shield,
                title = "高风险动作拦截",
                summary = "遇到低确定性或越权操作时先停在你这里",
                checked = blockHighRisk,
                onCheckedChange = onBlockHighRiskChange,
                highlight = true
            )
            PreferenceSwitchRow(
                icon = Icons.Rounded.NotificationsActive,
                title = "低置信度静默处理",
                summary = "置信度不够时避免自动推进，等你来确认",
                checked = muteLowConfidence,
                onCheckedChange = onMuteLowConfidenceChange,
                highlight = true
            )
        }

        WeavingGlassCard {
            PreferenceSwitchRow(
                icon = Icons.Rounded.CalendarMonth,
                title = "默认日级提醒",
                summary = "为重要事件提前一天预热",
                checked = reminderDayEnabled,
                onCheckedChange = onReminderDayEnabledChange
            )
            PreferenceSwitchRow(
                icon = Icons.Rounded.NotificationsActive,
                title = "分钟级提醒",
                summary = "当前默认提前 $reminderLeadMinutes 分钟提醒",
                checked = reminderHourEnabled,
                onCheckedChange = onReminderHourEnabledChange
            )
            PreferenceActionRow(
                icon = Icons.Rounded.Settings,
                title = "提醒提前量",
                summary = "点击可切换为 15 / 30 / 60 分钟",
                actionText = "$reminderLeadMinutes 分钟",
                onClick = {
                    val next = when (reminderLeadMinutes) {
                        15 -> 30
                        30 -> 60
                        else -> 15
                    }
                    onReminderLeadMinutesChange(next)
                }
            )
            PreferenceSwitchRow(
                icon = Icons.Rounded.Timeline,
                title = "地点识别联动",
                summary = "识别到地点后自动补足导航兜底能力",
                checked = autoMapLink,
                onCheckedChange = onAutoMapLinkChange
            )
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (highlight) AppColors.SurfaceContainer else Color.White.copy(alpha = 0.45f),
                shape = RoundedCornerShape(AppShapes.Medium)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeavingIconBubble(
            icon = icon,
            background = if (highlight) AppColors.CoralSoft else AppColors.GoldSoft,
            tint = AppColors.Primary
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = AppTypography.BodyLarge,
                color = AppColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary,
                style = AppTypography.BodyMedium,
                color = AppColors.OnSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.SurfaceContainerLowest,
                checkedTrackColor = AppColors.Primary,
                uncheckedThumbColor = AppColors.SurfaceContainerLowest,
                uncheckedTrackColor = AppColors.OutlineVariant
            )
        )
    }
}

@Composable
private fun PreferenceActionRow(
    icon: ImageVector,
    title: String,
    summary: String,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(AppShapes.Medium))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeavingIconBubble(icon = icon, background = AppColors.MintAccent, tint = AppColors.Primary)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = AppTypography.BodyLarge,
                color = AppColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary,
                style = AppTypography.BodyMedium,
                color = AppColors.OnSurfaceVariant
            )
        }
        Text(
            text = actionText,
            style = AppTypography.LabelMedium,
            color = AppColors.Primary
        )
    }
}

@Composable
private fun Toolboard(
    onOpenPlan: () -> Unit,
    onOpenReminderSettings: (() -> Unit)? = null,
    onOpenExportRecords: (() -> Unit)? = null
) {
    val context = LocalContext.current
    WeavingGlassCard(containerColor = AppColors.SurfaceContainerLowest) {
        ToolRow(Icons.Rounded.Timeline, "时间线中心", "查看最近沉淀的校园安排", onOpenPlan)
        ToolRow(Icons.Rounded.Settings, "提醒设置", "微调默认提醒策略和风险边界") {
            if (onOpenReminderSettings != null) {
                onOpenReminderSettings()
            } else {
                Toast.makeText(context, "已进入提醒设置区", Toast.LENGTH_SHORT).show()
            }
        }
        ToolRow(Icons.Rounded.Download, "导出记录", "为答辩或复盘导出你的时间资产") {
            if (onOpenExportRecords != null) {
                onOpenExportRecords()
            } else {
                Toast.makeText(context, "请到时间线页导出记录", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun ToolRow(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeavingIconBubble(icon = icon, background = AppColors.MintAccent, tint = AppColors.Primary)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = AppTypography.BodyLarge,
                color = AppColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary,
                style = AppTypography.BodyMedium,
                color = AppColors.OnSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = AppColors.OnSurfaceVariant
        )
    }
}
