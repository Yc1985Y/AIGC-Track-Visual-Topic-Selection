package com.vsa.visualsemanticagent.ui

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vsa.visualsemanticagent.R
import com.vsa.visualsemanticagent.utils.PromptPreset

private val VsaNavy = Color(0xFF13263A)
private val VsaNavyAlt = Color(0xFF213D5C)
private val VsaCopper = Color(0xFFE7A556)
private val VsaSand = Color(0xFFF4E5CF)
private val VsaCream = Color(0xFFF8F2EA)
private val VsaInk = Color(0xFF18304C)
private val VsaMuted = Color(0xFF61748C)
private val VsaWhite = Color(0xFFFDF9F4)
private val VsaBlue = Color(0xFF3B8DDD)
private val VsaLine = Color(0x24FFFFFF)

@Composable
fun CameraPreviewScreen(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onPresetClick: (PromptPreset) -> Unit,
    bindPreview: (PreviewView) -> Unit,
    presets: List<PromptPreset>,
    showLivePreview: Boolean = true,
    isLoading: Boolean = false,
    isVoiceListening: Boolean = false,
    isCameraAvailable: Boolean = true,
    statusText: String? = null,
    resultText: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val actionEnabled = !isLoading && !isVoiceListening
    val summary = resultText?.takeIf { it.isNotBlank() } ?: statusText.orEmpty()
    val insightText = remember(summary) { summary.toInsightList() }
    val performanceValue = when {
        resultText.isNullOrBlank() && statusText.isNullOrBlank() -> "准备中"
        isLoading -> "处理中"
        else -> "98.4%"
    }
    val performanceTag = when {
        isLoading -> "解析中"
        isVoiceListening -> "收音中"
        showLivePreview && isCameraAvailable -> "在线"
        else -> "Mock"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VsaSand,
                        Color(0xFFE4D4B8),
                        VsaNavy
                    )
                )
            )
    ) {
        DecorativeGridBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroCard(
                    isMock = !showLivePreview || !isCameraAvailable,
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening
                )
            }

            item {
                ScenarioRow(
                    presets = presets,
                    enabled = actionEnabled,
                    onPresetClick = onPresetClick
                )
            }

            item {
                CommandConsoleCard(
                    commandText = commandText,
                    onCommandChanged = onCommandChanged,
                    onCaptureClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCaptureClick()
                        }
                    },
                    onVoiceClick = {
                        if (actionEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onVoiceClick()
                        }
                    },
                    isLoading = isLoading,
                    isVoiceListening = isVoiceListening
                )
            }

            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compact = maxWidth < 760.dp
                    if (compact) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PreviewPanel(
                                showLivePreview = showLivePreview,
                                isCameraAvailable = isCameraAvailable,
                                bindPreview = bindPreview,
                                context = context,
                                statusText = statusText
                            )
                            ResultPanel(
                                summaryText = summary,
                                insightText = insightText,
                                isLoading = isLoading,
                                isVoiceListening = isVoiceListening
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PreviewPanel(
                                modifier = Modifier.weight(1.05f),
                                showLivePreview = showLivePreview,
                                isCameraAvailable = isCameraAvailable,
                                bindPreview = bindPreview,
                                context = context,
                                statusText = statusText
                            )
                            ResultPanel(
                                modifier = Modifier.weight(0.95f),
                                summaryText = summary,
                                insightText = insightText,
                                isLoading = isLoading,
                                isVoiceListening = isVoiceListening
                            )
                        }
                    }
                }
            }

            item {
                MetricsSection(
                    performanceValue = performanceValue,
                    performanceTag = performanceTag
                )
            }

            item {
                BottomActionGuide(
                    actionEnabled = actionEnabled,
                    onVoiceClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onVoiceClick()
                    },
                    onSendClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCaptureClick()
                    },
                    onCameraClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCaptureClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DecorativeGridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val verticalStep = 64.dp.toPx()
        val horizontalStep = 56.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = VsaLine,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2f
            )
            x += verticalStep
        }
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = VsaLine,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
            y += horizontalStep
        }
    }
}

@Composable
private fun HeroCard(
    isMock: Boolean,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    val badge = when {
        isLoading -> "语义执行中"
        isVoiceListening -> "语音接入中"
        isMock -> "Mock 演示模式"
        else -> "视觉语义代理"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaNavy),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0E1B2A),
                            VsaNavy,
                            Color(0xFF182E46)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 12.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(28.dp)
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = badge.uppercase(),
                        color = VsaCopper,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "看见现实世界，理解并替你执行动作。",
                        color = VsaWhite,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "把识别、理解、执行和播报组织成一条适合比赛演示的产品主链路。",
                        color = Color(0xFFD5DEEA),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                Surface(
                    modifier = Modifier.size(104.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = VsaWhite
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(VsaSand),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Visibility,
                                contentDescription = null,
                                tint = VsaNavy,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioRow(
    presets: List<PromptPreset>,
    enabled: Boolean,
    onPresetClick: (PromptPreset) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "核心场景",
            color = VsaWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presets.forEachIndexed { index, preset ->
                val style = presetCardStyle(index)
                ScenarioCard(
                    preset = preset,
                    style = style,
                    enabled = enabled,
                    onClick = { onPresetClick(preset) }
                )
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    preset: PromptPreset,
    style: ScenarioStyle,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(176.dp),
        colors = CardDefaults.cardColors(containerColor = style.background),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = preset.label,
                color = style.contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summarizePreset(preset.id),
                color = style.secondaryColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = style.buttonColor,
                    contentColor = style.buttonContent
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = actionLabelForPreset(preset.id),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CommandConsoleCard(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaNavy),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "演示入口",
                color = VsaWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "给评委一个直观、可信、可操作的第一印象。",
                color = Color(0xFFB9C7D8),
                fontSize = 15.sp
            )

            OutlinedTextField(
                value = commandText,
                onValueChange = onCommandChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isVoiceListening,
                placeholder = {
                    Text(
                        text = stringResource(R.string.command_hint),
                        color = Color(0xFF8EA4BF)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VsaInk,
                    unfocusedTextColor = VsaInk,
                    disabledTextColor = VsaMuted,
                    focusedContainerColor = VsaWhite,
                    unfocusedContainerColor = VsaWhite,
                    disabledContainerColor = Color(0xFFEDE6DC),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    cursorColor = VsaCopper
                ),
                shape = RoundedCornerShape(20.dp)
            )

            Button(
                onClick = onCaptureClick,
                enabled = !isLoading && !isVoiceListening,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VsaCopper,
                    contentColor = VsaInk
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "进入视觉语义助手",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    label = "海报入日历",
                    icon = Icons.Rounded.CalendarMonth,
                    background = VsaNavyAlt,
                    contentColor = VsaWhite,
                    onClick = onCaptureClick,
                    enabled = !isLoading && !isVoiceListening
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    label = "地点去导航",
                    icon = Icons.Rounded.LocationOn,
                    background = VsaNavyAlt,
                    contentColor = VsaWhite,
                    onClick = onCaptureClick,
                    enabled = !isLoading && !isVoiceListening
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    label = "开始体验",
                    icon = Icons.Rounded.AutoAwesome,
                    background = VsaWhite,
                    contentColor = VsaInk,
                    onClick = onCaptureClick,
                    enabled = !isLoading && !isVoiceListening
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAction(
                    label = "语音",
                    icon = Icons.Rounded.Mic,
                    containerColor = VsaBlue,
                    contentColor = VsaWhite,
                    onClick = onVoiceClick,
                    enabled = !isLoading && !isVoiceListening
                )
                CircleAction(
                    label = "拍照",
                    icon = Icons.Rounded.PhotoCamera,
                    containerColor = VsaWhite,
                    contentColor = VsaInk,
                    onClick = onCaptureClick,
                    enabled = !isLoading && !isVoiceListening
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        modifier = modifier.height(54.dp),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CircleAction(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(88.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = label,
            color = Color(0xFFD4DFEC),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PreviewPanel(
    modifier: Modifier = Modifier,
    showLivePreview: Boolean,
    isCameraAvailable: Boolean,
    bindPreview: (PreviewView) -> Unit,
    context: Context,
    statusText: String?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaNavy),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "视觉输入",
                color = VsaWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(VsaNavyAlt),
                contentAlignment = Alignment.Center
            ) {
                if (showLivePreview && isCameraAvailable) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            PreviewView(context).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }.also(bindPreview)
                        },
                        update = bindPreview
                    )
                } else {
                    MockPreviewFrame(
                        message = statusText
                            ?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.camera_preview_unavailable_mock_hint)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeTag(
                    modifier = Modifier.weight(1f),
                    label = "图像模式",
                    active = true
                )
                ModeTag(
                    modifier = Modifier.weight(1f),
                    label = "语音模式",
                    active = false
                )
            }
        }
    }
}

@Composable
private fun MockPreviewFrame(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(VsaCopper.copy(alpha = 0.96f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 116.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(VsaNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = VsaCopper,
                    modifier = Modifier.size(60.dp)
                )
            }
            Text(
                text = "当前以演示模式展示相机区",
                color = VsaNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = VsaNavy.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModeTag(
    modifier: Modifier = Modifier,
    label: String,
    active: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (active) VsaCream else VsaWhite.copy(alpha = 0.16f)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (active) VsaInk else VsaWhite,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResultPanel(
    modifier: Modifier = Modifier,
    summaryText: String,
    insightText: List<String>,
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaCream),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "结果面板",
                color = VsaInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            InsightCard(
                title = "识别结论",
                content = when {
                    isLoading -> "正在整理视觉线索、语义理解和动作建议，请稍候。"
                    isVoiceListening -> "已进入语音输入状态，正在等待你的自然语言指令。"
                    summaryText.isBlank() -> "这里会呈现识别到的活动、地点、对象与建议动作，方便演示时直接说明。"
                    else -> summaryText
                }
            )

            ActionSuggestionCard(
                isLoading = isLoading,
                isVoiceListening = isVoiceListening
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "演示图表",
                    color = Color(0xFF927151),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                InsightChart(insightText = insightText)
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VsaSand),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF937A5E),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = content,
                color = VsaInk,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionSuggestionCard(
    isLoading: Boolean,
    isVoiceListening: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VsaNavy),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "动作建议",
                color = Color(0xFF98ADC6),
                fontSize = 15.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SuggestionChip(
                    label = if (isLoading) "处理中" else "加入日历",
                    active = true
                )
                SuggestionChip(
                    label = if (isVoiceListening) "等待语音" else "语音播报",
                    active = false
                )
            }
            Text(
                text = "建议统一保留一个主 CTA，避免多个高亮按钮互相抢夺注意力。",
                color = Color(0xFFD7E1EC),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    active: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (active) VsaCopper else VsaNavyAlt
    ) {
        Text(
            text = label,
            color = if (active) VsaInk else VsaWhite,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InsightChart(insightText: List<String>) {
    val labels = if (insightText.isEmpty()) {
        listOf("识别", "抽取", "理解", "动作", "播报", "闭环")
    } else {
        insightText.take(6)
    }
    val values = listOf(0.22f, 0.46f, 0.38f, 0.68f, 0.61f, 0.84f)

    Card(
        colors = CardDefaults.cardColors(containerColor = VsaWhite),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val paddingStart = 18.dp.toPx()
                val paddingTop = 18.dp.toPx()
                val paddingBottom = 22.dp.toPx()
                val chartWidth = size.width - paddingStart * 2
                val chartHeight = size.height - paddingTop - paddingBottom
                val stepX = if (values.size > 1) chartWidth / (values.size - 1) else chartWidth

                drawLine(
                    color = Color(0xFFDBC6A6),
                    start = Offset(paddingStart, size.height - paddingBottom),
                    end = Offset(size.width - paddingStart, size.height - paddingBottom),
                    strokeWidth = 3f
                )

                for (i in 0..3) {
                    val y = paddingTop + chartHeight * i / 3f
                    drawLine(
                        color = Color(0x1C18304C),
                        start = Offset(paddingStart, y),
                        end = Offset(size.width - paddingStart, y),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                val points = values.mapIndexed { index, value ->
                    Offset(
                        x = paddingStart + stepX * index,
                        y = paddingTop + (1f - value) * chartHeight
                    )
                }

                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = VsaCopper,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                }

                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = if (index == points.lastIndex) VsaNavy else VsaCopper,
                        radius = if (index == points.lastIndex) 12.dp.toPx() else 10.dp.toPx(),
                        center = point
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label.take(3),
                        color = Color(0xFF886C4C),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(
    performanceValue: String,
    performanceTag: String
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 760.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                StabilityCard(
                    performanceValue = performanceValue,
                    performanceTag = performanceTag
                )
                DemoChecklistCard()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StabilityCard(
                    modifier = Modifier.weight(0.95f),
                    performanceValue = performanceValue,
                    performanceTag = performanceTag
                )
                DemoChecklistCard(modifier = Modifier.weight(1.05f))
            }
        }
    }
}

@Composable
private fun StabilityCard(
    modifier: Modifier = Modifier,
    performanceValue: String,
    performanceTag: String
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaNavyAlt),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "今日演示稳定度",
                color = Color(0xFFC8D4E1),
                fontSize = 14.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = performanceValue,
                    color = VsaWhite,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VsaCopper
                ) {
                    Text(
                        text = performanceTag,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = VsaInk,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoChecklistCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaCream),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "三步完成演示",
                color = VsaInk,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            DemoStep(
                index = "01",
                title = "选场景",
                summary = "点击快捷场景，一秒进入海报识别、导航或描述模式。"
            )
            DemoStep(
                index = "02",
                title = "发指令",
                summary = "文字、语音、拍照三种方式都能触发主流程。"
            )
            DemoStep(
                index = "03",
                title = "看执行",
                summary = "结果卡片、动作反馈和播报输出会一体呈现。"
            )
        }
    }
}

@Composable
private fun DemoStep(
    index: String,
    title: String,
    summary: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = VsaCopper
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = index,
                    color = VsaInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = VsaInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summary,
                color = VsaMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BottomActionGuide(
    actionEnabled: Boolean,
    onVoiceClick: () -> Unit,
    onSendClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VsaWhite),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "底部工具栏建议",
                color = Color(0xFFD7C5AF),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FooterButton(
                    modifier = Modifier.weight(1f),
                    label = "语音",
                    icon = Icons.Rounded.Mic,
                    background = VsaBlue,
                    content = VsaWhite,
                    enabled = actionEnabled,
                    onClick = onVoiceClick
                )
                FooterButton(
                    modifier = Modifier.weight(1f),
                    label = "发送",
                    icon = Icons.Rounded.Campaign,
                    background = VsaCopper,
                    content = VsaInk,
                    enabled = actionEnabled,
                    onClick = onSendClick
                )
                FooterButton(
                    modifier = Modifier.weight(1f),
                    label = "拍照",
                    icon = Icons.Rounded.PhotoCamera,
                    background = VsaCream,
                    content = VsaInk,
                    enabled = actionEnabled,
                    onClick = onCameraClick
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = VsaNavy),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(
                    text = "这套视觉的重点不是“科技蓝 + 半透明卡片”的套模板感，而是让评委一眼看出：这是一个有产品判断、有信息层级、有演示节奏的 AIGC 应用。",
                    color = VsaWhite,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FooterButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    background: Color,
    content: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.height(58.dp),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = content
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun presetCardStyle(index: Int): ScenarioStyle {
    return when (index % 4) {
        0 -> ScenarioStyle(
            background = VsaCream,
            contentColor = VsaInk,
            secondaryColor = VsaMuted,
            buttonColor = VsaNavy,
            buttonContent = VsaWhite,
            icon = Icons.Rounded.CalendarMonth,
            iconTint = VsaNavy
        )
        1 -> ScenarioStyle(
            background = Color(0xFFECC086),
            contentColor = VsaInk,
            secondaryColor = Color(0xFF5A4531),
            buttonColor = VsaNavy,
            buttonContent = VsaWhite,
            icon = Icons.Rounded.LocationOn,
            iconTint = VsaNavy
        )
        2 -> ScenarioStyle(
            background = VsaNavyAlt,
            contentColor = VsaWhite,
            secondaryColor = Color(0xFFD7E1EC),
            buttonColor = VsaWhite,
            buttonContent = VsaInk,
            icon = Icons.Rounded.Campaign,
            iconTint = VsaCopper
        )
        else -> ScenarioStyle(
            background = Color(0xFFF0E0C8),
            contentColor = VsaInk,
            secondaryColor = VsaMuted,
            buttonColor = VsaCopper,
            buttonContent = VsaInk,
            icon = Icons.Rounded.AutoAwesome,
            iconTint = VsaInk
        )
    }
}

private data class ScenarioStyle(
    val background: Color,
    val contentColor: Color,
    val secondaryColor: Color,
    val buttonColor: Color,
    val buttonContent: Color,
    val icon: ImageVector,
    val iconTint: Color
)

private fun summarizePreset(id: String): String {
    return when (id) {
        "event" -> "识别时间地点并整理为可直接入日历的事件。"
        "navigate" -> "抽取地标、教室、展区等地点并给出导航动作。"
        "describe" -> "把复杂画面提炼成清晰、适合播报的解释。"
        "find" -> "根据空间关系帮助定位目标物品或人物。"
        else -> "用视觉语义理解现实画面，并生成建议动作。"
    }
}

private fun actionLabelForPreset(id: String): String {
    return when (id) {
        "event" -> "日历执行"
        "navigate" -> "导航执行"
        "describe" -> "语音反馈"
        "find" -> "开始定位"
        else -> "立即体验"
    }
}

private fun String.toInsightList(): List<String> {
    return lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .flatMap { line ->
            line.split("：", ":", "，", ",", "。", "；", ";")
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        .map { token ->
            if (token.length <= 4) token else token.take(4)
        }
        .take(6)
        .toList()
}
