package com.vsa.visualsemanticagent.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    showLivePreview: Boolean = true,
    isLoading: Boolean = false,
    isVoiceListening: Boolean = false,
    isCameraAvailable: Boolean = true,
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
        if (showLivePreview && isCameraAvailable) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PreviewView(context).apply {
                        // Use TextureView-based rendering so Compose controls stay visible above the preview.
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }.also(bindPreview)
                },
                update = bindPreview
            )
        } else if (showLivePreview) {
            CameraUnavailablePlaceholder(
                title = stringResource(R.string.camera_preview_placeholder),
                message = statusText
                    ?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.camera_preview_unavailable_mock_hint)
            )
        } else {
            MockPreviewBackdrop()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xF2172433)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                                    containerColor = Color(0xFF243447),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = commandText,
                        onValueChange = onCommandChanged,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && !isVoiceListening,
                        label = { Text(stringResource(R.string.voice_input)) },
                        placeholder = { Text(stringResource(R.string.command_hint)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF8FA3B8),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFD7E3F0),
                            cursorColor = Color.White,
                            focusedContainerColor = Color(0x221E293B),
                            unfocusedContainerColor = Color(0x221E293B),
                            focusedPlaceholderColor = Color(0xFFD7E3F0),
                            unfocusedPlaceholderColor = Color(0xFFD7E3F0)
                        )
                    )

                    Button(
                        onClick = {
                            if (!isLoading && !isVoiceListening) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCaptureClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && !isVoiceListening,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4DA3FF),
                            contentColor = Color(0xFF0E223D)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.send_command),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

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
    }
}

@Composable
private fun MockPreviewBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEAF3FF),
                        Color(0xFFDCE8FF),
                        Color(0xFFF7FAFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(bottom = 360.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF183153)
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mock_mode_badge),
                        color = Color(0xFFB7D3FF),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.mock_mode_showcase_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.mock_mode_showcase_subtitle),
                        color = Color(0xFFE2ECFF)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.82f)
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mock_mode_steps_title),
                        color = Color(0xFF183153),
                        fontWeight = FontWeight.SemiBold
                    )
                    MockStepRow(
                        index = "1",
                        title = stringResource(R.string.mock_mode_step_pick_title),
                        summary = stringResource(R.string.mock_mode_step_pick_summary)
                    )
                    MockStepRow(
                        index = "2",
                        title = stringResource(R.string.mock_mode_step_prompt_title),
                        summary = stringResource(R.string.mock_mode_step_prompt_summary)
                    )
                    MockStepRow(
                        index = "3",
                        title = stringResource(R.string.mock_mode_step_run_title),
                        summary = stringResource(R.string.mock_mode_step_run_summary)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MockHighlightCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.mock_mode_highlight_offline_title),
                    summary = stringResource(R.string.mock_mode_highlight_offline_summary)
                )
                MockHighlightCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.mock_mode_highlight_actions_title),
                    summary = stringResource(R.string.mock_mode_highlight_actions_summary)
                )
            }
        }
    }
}

@Composable
private fun MockStepRow(
    index: String,
    title: String,
    summary: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F7FF), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Card(
            modifier = Modifier
                .size(36.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF183153)
            ),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF183153),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summary,
                color = Color(0xFF49627F)
            )
        }
    }
}

@Composable
private fun MockHighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    summary: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.72f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF183153),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summary,
                color = Color(0xFF49627F)
            )
        }
    }
}

@Composable
private fun CameraUnavailablePlaceholder(
    title: String,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF4FF))
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFDCEBFF)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color(0xFF183153),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = Color(0xFF36506F),
                    textAlign = TextAlign.Center
                )
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
