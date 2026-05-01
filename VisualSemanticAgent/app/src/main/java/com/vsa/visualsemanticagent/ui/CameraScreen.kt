package com.vsa.visualsemanticagent.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vsa.visualsemanticagent.R
import com.vsa.visualsemanticagent.utils.PromptPreset

@Composable
fun CameraPreviewScreen(
    commandText: String,
    onCommandChanged: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onPresetClick: (PromptPreset) -> Unit,
    bindPreview: (PreviewView) -> Unit,
    presets: List<PromptPreset>,
    isLoading: Boolean = false,
    isVoiceListening: Boolean = false,
    statusText: String? = null,
    resultText: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PreviewView(context).also(bindPreview)
            },
            update = bindPreview
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!resultText.isNullOrBlank()) {
                ResultCard(resultText = resultText)
            }

            statusText?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, color = Color.White)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    AssistChip(
                        onClick = { onPresetClick(preset) },
                        enabled = !isLoading && !isVoiceListening,
                        label = { Text(preset.label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            labelColor = Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = commandText,
                onValueChange = onCommandChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.voice_input)) },
                placeholder = { Text(stringResource(R.string.command_hint)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isLoading && !isVoiceListening) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onVoiceClick()
                        }
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = stringResource(R.string.voice_input))
                }

                FloatingActionButton(
                    onClick = {
                        if (!isLoading && !isVoiceListening) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCaptureClick()
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    Icon(
                        Icons.Rounded.PhotoCamera,
                        contentDescription = stringResource(R.string.camera_capture)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(resultText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.62f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.result_title),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = resultText,
                color = Color.White
            )
        }
    }
}
