package com.vsa.visualsemanticagent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsa.visualsemanticagent.R

/**
 * 模块A：基础框架与UI交互基座 - Loading Overlay Component
 * 
 * 核心特征：
 * 1. 视觉阻断层：使用半透明黑色背景压暗相机预览
 * 2. 手势物理拦截：消费所有触摸事件
 * 3. 动态心理抚慰反馈：阶段性变化的提示文案
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    currentStage: Int = 0 // 0: 扫描, 1: 分析, 2: 生成
) {
    if (!isVisible) return
    
    // 阶段性提示文案
    val stageMessages = listOf(
        stringResource(R.string.scanning_features),
        stringResource(R.string.cloud_analysis),
        stringResource(R.string.generating_strategy)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        OverlayScrim(alpha = 0.5f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .wrapContentSize()
                .padding(24.dp)
        ) {
            // 圆形加载进度条
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 动态阶段提示文案
            Text(
                text = stageMessages.getOrNull(currentStage) ?: "处理中…",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 错误提示UI
 */
@Composable
fun ErrorOverlay(
    isVisible: Boolean,
    errorMessage: String = "发生错误",
    showRetry: Boolean = true,
    retryText: String = "重试",
    onRetry: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (!isVisible) return
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        OverlayScrim(alpha = 0.7f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(12.dp))
                .padding(24.dp)
        ) {
            Text(
                text = errorMessage,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (showRetry) {
                    Button(onClick = onRetry) {
                        Text(retryText)
                    }
                }
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun OverlayScrim(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    )
}
