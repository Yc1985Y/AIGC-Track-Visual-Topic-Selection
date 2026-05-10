package com.vsa.visualsemanticagent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vsa.visualsemanticagent.utils.PromptPreset

private data class ScenarioDef(
    val presetId:   String,
    val title:      String,
    val description: String,
    val icon:       ImageVector,
    val bgColor:    Color,
    val textColor:  Color,
    val iconColor:  Color
)

private val SCENARIO_DEFS = listOf(
    ScenarioDef(
        presetId    = "lecture",
        title       = "讲座",
        description = "学术研讨会、嘉宾演讲通知",
        icon        = Icons.Rounded.School,
        bgColor     = Color(0xFF2AF598),
        textColor   = Color(0xFF002110),
        iconColor   = Color(0xFF006D3F)
    ),
    ScenarioDef(
        presetId    = "exam",
        title       = "考试",
        description = "期中、期末、随堂测验",
        icon        = Icons.Rounded.EditNote,
        bgColor     = Color(0xFFFFDADC),
        textColor   = Color(0xFF6B0020),
        iconColor   = Color(0xFFB32444)
    ),
    ScenarioDef(
        presetId    = "activity",
        title       = "社团活动",
        description = "社团会议、校园文体活动",
        icon        = Icons.Rounded.Groups,
        bgColor     = Color(0xFFFFE17A),
        textColor   = Color(0xFF4A3F00),
        iconColor   = Color(0xFF715C00)
    ),
    ScenarioDef(
        presetId    = "career",
        title       = "宣讲会",
        description = "招聘宣讲、校园求职活动",
        icon        = Icons.Rounded.Campaign,
        bgColor     = Color(0xFFFFB2C1),
        textColor   = Color(0xFF6B0020),
        iconColor   = Color(0xFFB32444)
    ),
    ScenarioDef(
        presetId    = "group_notice",
        title       = "群通知",
        description = "班级群、社团群、实验室群的重要通知",
        icon        = Icons.Rounded.Forum,
        bgColor     = Color(0xFFDBE5DB),
        textColor   = Color(0xFF151E17),
        iconColor   = Color(0xFF3B4A3F)
    )
)

@Composable
fun ScenariosScreen(
    modifier:      Modifier = Modifier,
    presets:       List<PromptPreset>,
    onPresetClick: (PromptPreset) -> Unit
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        modifier              = modifier.background(AppColors.Background),
        contentPadding        = PaddingValues(
            start  = AppSpacing.containerPadding,
            end    = AppSpacing.containerPadding,
            top    = 20.dp,
            bottom = AppSpacing.lg
        ),
        verticalArrangement   = Arrangement.spacedBy(AppSpacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.gutter)
    ) {
        // Header — full width
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.sm)
            ) {
                Text(
                    text  = "常用场景",
                    style = AppTypography.HeadlineMedium,
                    color = AppColors.OnBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "选一个场景，更精准地识别通知内容",
                    style = AppTypography.LabelLarge,
                    color = AppColors.OnSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        // First 4 cards — 2-column grid
        items(SCENARIO_DEFS.take(4), key = { it.presetId }) { def ->
            val preset = presets.find { it.id == def.presetId }
            ScenarioBentoCard(
                def     = def,
                onClick = { preset?.let(onPresetClick) }
            )
        }

        // 5th card — full width
        item(span = { GridItemSpan(2) }) {
            val def    = SCENARIO_DEFS[4]
            val preset = presets.find { it.id == def.presetId }
            ScenarioBentoCardWide(
                def     = def,
                onClick = { preset?.let(onPresetClick) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioBentoCard(def: ScenarioDef, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .height(184.dp),
        shape     = RoundedCornerShape(AppShapes.Large),
        colors    = CardDefaults.cardColors(containerColor = def.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon chip
            Surface(
                modifier = Modifier.size(44.dp),
                shape    = RoundedCornerShape(12.dp),
                color    = def.iconColor.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = def.icon,
                        contentDescription = null,
                        tint               = def.iconColor,
                        modifier           = Modifier.size(24.dp)
                    )
                }
            }

            // Title + description
            Column {
                Text(
                    text       = def.title,
                    style      = AppTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = def.textColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text      = def.description,
                    style     = AppTypography.LabelSmall,
                    color     = def.textColor.copy(alpha = 0.72f),
                    maxLines  = 2
                )
            }

            // Use button
            TextButton(
                onClick  = onClick,
                modifier = Modifier.offset(x = (-10).dp),
                colors   = ButtonDefaults.textButtonColors(contentColor = def.iconColor)
            ) {
                Text(
                    text       = "使用 →",
                    style      = AppTypography.LabelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioBentoCardWide(def: ScenarioDef, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(AppShapes.Large),
        colors    = CardDefaults.cardColors(containerColor = def.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // Icon chip
            Surface(
                modifier = Modifier.size(52.dp),
                shape    = RoundedCornerShape(14.dp),
                color    = def.iconColor.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = def.icon,
                        contentDescription = null,
                        tint               = def.iconColor,
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }

            // Title + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = def.title,
                    style      = AppTypography.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    color      = def.textColor
                )
                Text(
                    text  = def.description,
                    style = AppTypography.LabelLarge,
                    color = def.textColor.copy(alpha = 0.72f)
                )
            }

            // Use button
            TextButton(
                onClick = onClick,
                colors  = ButtonDefaults.textButtonColors(contentColor = def.iconColor)
            ) {
                Text(
                    text       = "使用 →",
                    style      = AppTypography.LabelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
