package com.vsa.visualsemanticagent.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsa.visualsemanticagent.ui.AppColors
import com.vsa.visualsemanticagent.ui.AppShapes
import com.vsa.visualsemanticagent.ui.AppSpacing
import com.vsa.visualsemanticagent.ui.AppTypography

@Composable
fun WeavingBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.Background,
                        AppColors.SurfaceBright,
                        AppColors.Background
                    )
                )
            )
    ) {
        DiffuseGlow(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopStart),
            colors = listOf(AppColors.MintAccent.copy(alpha = 0.55f), Color.Transparent)
        )
        DiffuseGlow(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .padding(top = 80.dp),
            colors = listOf(AppColors.GoldSoft.copy(alpha = 0.5f), Color.Transparent)
        )
        DiffuseGlow(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd),
            colors = listOf(AppColors.CoralSoft.copy(alpha = 0.45f), Color.Transparent)
        )
    }
}

@Composable
private fun DiffuseGlow(
    modifier: Modifier,
    colors: List<Color>
) {
    Box(
        modifier = modifier
            .blur(72.dp)
            .background(
                brush = Brush.radialGradient(colors),
                shape = CircleShape
            )
    )
}

@Composable
fun WeavingGlassCard(
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.GlassSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) {
            modifier.clickable { onClick() }
        } else {
            modifier
        },
        shape = RoundedCornerShape(AppShapes.Large),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.GlassBorder.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            content = content
        )
    }
}

@Composable
fun WeavingSectionTitle(
    title: String,
    subtitle: String? = null,
    action: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = AppTypography.HeadlineLargeMobile,
                color = AppColors.Primary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = AppTypography.BodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
        if (!action.isNullOrBlank()) {
            Text(
                text = action,
                style = AppTypography.LabelMedium,
                color = AppColors.Primary,
                modifier = if (onActionClick != null) {
                    Modifier.clickable { onActionClick() }
                } else {
                    Modifier
                }
            )
        }
    }
}

@Composable
fun WeavingChip(
    text: String,
    icon: ImageVector? = null,
    background: Color = AppColors.SurfaceContainer,
    contentColor: Color = AppColors.Primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppShapes.Full))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            style = AppTypography.LabelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WeavingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 58.dp),
        enabled = enabled,
        shape = RoundedCornerShape(AppShapes.Full),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.OnPrimary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.45f),
            disabledContentColor = AppColors.OnPrimary.copy(alpha = 0.75f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = AppTypography.BodyLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeavingSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(AppShapes.Full),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.SurfaceContainer,
            contentColor = AppColors.Primary
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            style = AppTypography.BodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeavingIconBubble(
    icon: ImageVector,
    background: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = if (onClick != null) {
                Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            } else {
                Modifier.matchParentSize()
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    summary: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    WeavingGlassCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = AppColors.SurfaceContainer
    ) {
        Text(
            text = title,
            style = AppTypography.HeadlineMedium,
            color = AppColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = summary,
            style = AppTypography.BodyMedium,
            color = AppColors.OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            WeavingPrimaryButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
