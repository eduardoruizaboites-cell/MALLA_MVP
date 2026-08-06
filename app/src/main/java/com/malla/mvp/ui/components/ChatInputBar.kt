package com.malla.mvp.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.malla.mvp.media.VoiceRecorder
import com.malla.mvp.ui.theme.LocalColorScheme
import com.malla.mvp.emoji.ui.EmojiPicker
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatInputBar(
    voiceRecorder: VoiceRecorder,
    onSendText: (String) -> Unit,
    onSendVoice: (File) -> Unit,
    onSendZumbido: () -> Unit,
    onCameraClick: () -> Unit = {},
    onAttachmentClick: () -> Unit = {},
    onTextChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val colorScheme = LocalColorScheme.current
    var text by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Cuando se cierra el panel de emojis, solicitar foco al campo de texto
    LaunchedEffect(showEmojiPicker) {
        if (!showEmojiPicker) {
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = !isRecording,
            enter = fadeIn(tween(200)) + scaleIn(),
            exit = fadeOut(tween(200)) + scaleOut()
        ) {
            IconButton(onClick = onSendZumbido) {
                Icon(Icons.Filled.Vibration, "Zumbido", tint = colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        val neonActive = text.isNotEmpty() || isRecording
        Box(
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
                .clip(RoundedCornerShape(28.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .then(
                    if (neonActive) Modifier.drawBehind {
                        val borderWidth = 2.dp.toPx()
                        val gradient = Brush.linearGradient(
                            listOf(colorScheme.primary, colorScheme.secondary, colorScheme.primary),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                        drawRoundRect(
                            brush = gradient,
                            size = size,
                            cornerRadius = CornerRadius(28.dp.toPx()),
                            style = Stroke(width = borderWidth)
                        )
                    } else Modifier
                )
        ) {
            if (!isRecording) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .animateContentSize()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Mensaje", color = colorScheme.onSurface.copy(alpha = 0.5f)) },
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = colorScheme.primary
                    ),
                    leadingIcon = {
                        IconButton(onClick = {
                            showEmojiPicker = !showEmojiPicker
                            if (showEmojiPicker) {
                                focusManager.clearFocus()
                            }
                        }) {
                            Icon(
                                if (showEmojiPicker) Icons.Default.Keyboard else Icons.Default.InsertEmoticon,
                                if (showEmojiPicker) "Teclado" else "Emoji",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = onAttachmentClick) {
                                Icon(
                                    Icons.Filled.AttachFile,
                                    "Adjuntar",
                                    tint = colorScheme.primary
                                )
                            }
                            IconButton(onClick = onCameraClick) {
                                Icon(
                                    Icons.Filled.CameraAlt,
                                    "Cámara",
                                    tint = colorScheme.primary
                                )
                            }
                        }
                    }
                )
            } else {
                EqualizerBarsIndicator(
                    amplitudeFlow = voiceRecorder.amplitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    primaryColor = colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        if (text.isBlank() || isRecording) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.1f))
                    .pointerInput(isRecording) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue
                                if (change.pressed && !change.previousPressed && !isRecording) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        val file = voiceRecorder.startRecording()
                                        if (file != null) {
                                            isRecording = true
                                        } else {
                                            Toast.makeText(context, "Error al iniciar grabación", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Permiso de micrófono requerido", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                if (!change.pressed && change.previousPressed && isRecording) {
                                    val file = try { voiceRecorder.stopRecording() } catch (e: Exception) { null }
                                    isRecording = false
                                    if (file != null && file.length() > 0) {
                                        onSendVoice(file)
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    val pulse = rememberInfiniteTransition(label = "micPulse")
                    val micScale by pulse.animateFloat(1f, 1.15f, infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "s")
                    Icon(
                        Icons.Filled.Mic,
                        "Grabando",
                        tint = Color(0xFFFF4C4C),
                        modifier = Modifier.size(24.dp).scale(micScale)
                    )
                } else {
                    Icon(
                        Icons.Filled.Mic,
                        "Grabar",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            val sendScale = remember { Animatable(1f) }
            LaunchedEffect(text) {
                if (text.isNotEmpty()) {
                    sendScale.animateTo(1.1f, tween(100))
                    sendScale.animateTo(1f, tween(100))
                }
            }
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSendText(text)
                        text = ""
                    }
                },
                modifier = Modifier.scale(sendScale.value)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    "Enviar",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = showEmojiPicker,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(200)),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200))
    ) {
        EmojiPicker(
            onEmojiSelected = { emoji -> text = text + emoji },
            onDismissKeyboard = {
                showEmojiPicker = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .background(colorScheme.surface)
        )
    }
}

@Composable
private fun EqualizerBarsIndicator(
    amplitudeFlow: kotlinx.coroutines.flow.StateFlow<Int>,
    modifier: Modifier = Modifier,
    barCount: Int = 20,
    primaryColor: Color = Color(0xFF4CE6FF)
) {
    val amp by amplitudeFlow.collectAsState()

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / (barCount * 2f)
        val maxBarHeight = canvasHeight * 0.8f
        val normalizedAmp = (amp / 32767f).coerceIn(0.05f, 1f)

        for (i in 0 until barCount) {
            val fraction = i.toFloat() / barCount
            val barHeight = maxBarHeight * normalizedAmp * (0.4f + 0.6f * kotlin.math.sin(fraction * Math.PI.toFloat()).toFloat())
            val x = i * (barWidth * 2f)
            val y = canvasHeight - barHeight
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}
