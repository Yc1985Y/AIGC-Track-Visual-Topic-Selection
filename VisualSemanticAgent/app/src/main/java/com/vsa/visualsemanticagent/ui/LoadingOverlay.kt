package com.vsa.visualsemanticagent.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsa.visualsemanticagent.R

private data class LoadingStep(
    val label:    String,
    val icon:     ImageVector,
    val doneIcon: ImageVector = Icons.Rounded.CheckCircle
)

private val LOADING_STEPS = listOf(
    LoadingStep("正在识别事项", Icons.Rounded.Search),
    LoadingStep("时间、地点...", Icons.Rounded.Schedule),
    LoadingStep("很快就好",      Icons.Rounded.HourglassEmpty),
)

/**
 * 识别中覆盖层
 *
 * @param isVisible     是否显示
 * @param currentStage  0/1/2 对应三步进度
 * @param onCancel      取消回调，传 null 则不显示取消按钮
 */
@Composable
fun LoadingOverlay(
    isVisible:    Boolean,
    currentStage: Int = 0,
    onCancel:     (() -> Unit)? = null
) {
    if (!isVisible) return

    val transition = rememberInfiniteTransition(label = "loading")

    val pulseScale by transition.animateFloat(
        initialValue  = 0.88f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val spinAngle by transition.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "spin"
    )
    val shimmerShift by transition.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Dot grid ────────────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val color   = AppColors.PrimaryFixedDim.copy(alpha = 0.55f)
            val spacing = 20.dp.toPx()
            var cx = spacing
            while (cx < size.width) {
                var cy = spacing
                while (cy < size.height) {
                    drawCircle(color = color, radius = 1.5f, center = Offset(cx, cy))
                    cy += spacing
                }
                cx += spacing
            }
        }

        // ── Ambient blobs ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = (-30).dp)
                .blur(60.dp)
                .background(AppColors.PrimaryFixed.copy(alpha = 0.50f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .blur(60.dp)
                .background(AppColors.SecondaryFixed.copy(alpha = 0.55f), CircleShape)
        )

        // ── Main column ──────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
        ) {
            // Glass card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(AppShapes.Large),
                colors    = CardDefaults.cardColors(
                    containerColor = AppColors.SurfaceContainerLowest.copy(alpha = 0.85f)
                ),
                border    = BorderStroke(1.dp, AppColors.SurfaceContainerHighest.copy(alpha = 0.50f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    // ── Animated icon ────────────────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(176.dp)
                    ) {
                        // Pulsing outer ring
                        Box(
                            modifier = Modifier
                                .size(176.dp)
                                .scale(pulseScale)
                                .background(AppColors.PrimaryFixed.copy(alpha = 0.22f), CircleShape)
                        )
                        // Inner icon circle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(112.dp)
                                .background(AppColors.PrimaryContainer, CircleShape)
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.DocumentScanner,
                                contentDescription = null,
                                tint               = AppColors.OnPrimary,
                                modifier           = Modifier.size(44.dp)
                            )
                        }
                        // Floating chip — schedule (top-right)
                        LoadingFloatingChip(
                            icon      = Icons.Rounded.Schedule,
                            bgColor   = AppColors.SecondaryContainer,
                            iconTint  = AppColors.OnSecondaryContainer,
                            chipSize  = 32,
                            iconSize  = 16,
                            cornerDp  = AppShapes.Full,
                            modifier  = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 12.dp)
                        )
                        // Floating chip — location (bottom-left)
                        LoadingFloatingChip(
                            icon      = Icons.Rounded.LocationOn,
                            bgColor   = AppColors.SurfaceDim,
                            iconTint  = AppColors.OnSurface,
                            chipSize  = 38,
                            iconSize  = 20,
                            cornerDp  = AppShapes.Medium,
                            modifier  = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 4.dp, y = (-12).dp)
                                .rotate(12f)
                        )
                        // Floating chip — check (mid-left)
                        LoadingFloatingChip(
                            icon      = Icons.Rounded.Check,
                            bgColor   = AppColors.PrimaryFixed,
                            iconTint  = AppColors.PrimaryContainer,
                            chipSize  = 24,
                            iconSize  = 12,
                            cornerDp  = AppShapes.Small,
                            modifier  = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = 6.dp, y = (-24).dp)
                                .rotate(-12f)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Title
                    Text(
                        text      = "正在整理通知...",
                        style     = AppTypography.HeadlineLargeMobile,
                        color     = AppColors.Primary,
                        textAlign = TextAlign.Start,
                        modifier  = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // Steps
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier            = Modifier.fillMaxWidth()
                    ) {
                        LOADING_STEPS.forEachIndexed { i, step ->
                            LoadingStepRow(
                                step      = step,
                                isDone    = i < currentStage,
                                isActive  = i == currentStage,
                                spinAngle = spinAngle
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Progress bar
                    LoadingProgressBar(currentStage = currentStage, shimmerShift = shimmerShift)
                }
            }

            // Cancel button (outside card)
            if (onCancel != null) {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick   = onCancel,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape     = RoundedCornerShape(AppShapes.Full),
                    colors    = ButtonDefaults.outlinedButtonColors(
                        containerColor = AppColors.SurfaceContainerHighest,
                        contentColor   = AppColors.OnSurfaceVariant
                    ),
                    border    = BorderStroke(0.dp, Color.Transparent)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text          = "取消",
                        style         = AppTypography.LabelMedium,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingStepRow(
    step:      LoadingStep,
    isDone:    Boolean,
    isActive:  Boolean,
    spinAngle: Float
) {
    val bgAlpha   = if (isActive) 0.55f else 0.30f
    val textColor = if (!isDone && !isActive) AppColors.Outline else AppColors.OnSurfaceVariant
    val iconTint  = when {
        isDone   -> AppColors.Primary
        isActive -> AppColors.PrimaryContainer
        else     -> AppColors.Outline
    }
    val displayIcon = if (isDone) step.doneIcon else step.icon

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShapes.Medium))
            .background(AppColors.SurfaceContainer.copy(alpha = bgAlpha))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = displayIcon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text     = step.label,
            style    = if (isActive) AppTypography.BodyLarge else AppTypography.BodyMedium,
            color    = textColor,
            modifier = Modifier.weight(1f)
        )
        if (isActive) {
            Icon(
                imageVector        = Icons.Rounded.Autorenew,
                contentDescription = null,
                tint               = AppColors.Primary,
                modifier           = Modifier
                    .size(18.dp)
                    .rotate(spinAngle)
            )
        }
    }
}

@Composable
private fun LoadingProgressBar(currentStage: Int, shimmerShift: Float) {
    val fillFraction = when (currentStage) {
        0    -> 0.28f
        1    -> 0.65f
        else -> 0.92f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(AppShapes.Full))
            .background(AppColors.SurfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fillFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(AppShapes.Full))
                .background(AppColors.PrimaryContainer)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(AppShapes.Full))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.38f),
                            Color.Transparent
                        ),
                        start = Offset((500f * shimmerShift - 180f), 0f),
                        end   = Offset((500f * shimmerShift), 0f)
                    )
                )
        )
    }
}

@Composable
private fun LoadingFloatingChip(
    icon:     ImageVector,
    bgColor:  Color,
    iconTint: Color,
    chipSize: Int,
    iconSize: Int,
    cornerDp: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(chipSize.dp)
            .clip(RoundedCornerShape(cornerDp))
            .background(bgColor)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(iconSize.dp)
        )
    }
}

// ── Error overlay ─────────────────────────────────────────────────────────────

@Composable
fun ErrorOverlay(
    isVisible:    Boolean,
    errorMessage: String  = "发生错误",
    showRetry:    Boolean = true,
    retryText:    String  = "重试",
    onRetry:      () -> Unit = {},
    onDismiss:    () -> Unit = {}
) {
    if (!isVisible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.padding(horizontal = 24.dp),
            shape     = RoundedCornerShape(AppShapes.Large),
            colors    = CardDefaults.cardColors(containerColor = AppColors.SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(24.dp)
            ) {
                Text(
                    text      = errorMessage,
                    style     = AppTypography.BodyMedium,
                    color     = AppColors.OnSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (showRetry) {
                        OutlinedButton(
                            onClick = onRetry,
                            shape   = RoundedCornerShape(AppShapes.Full),
                            border  = BorderStroke(1.dp, AppColors.Outline)
                        ) {
                            Text(retryText, style = AppTypography.LabelMedium, color = AppColors.OnSurface)
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        shape   = RoundedCornerShape(AppShapes.Full),
                        colors  = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text(
                            stringResource(R.string.close),
                            style = AppTypography.LabelMedium,
                            color = AppColors.OnPrimary
                        )
                    }
                }
            }
        }
    }
}
