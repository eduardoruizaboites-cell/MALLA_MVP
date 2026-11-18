package com.malla.mvp.ui.screen

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Iter 70d — CallScreen premium performance-first.
 *
 * Diseño:
 *  - Fondo gradiente radial (estático, 1 draw call)
 *  - Avatar 180dp con borde cyan + inicial
 *  - Glow pulsante (solo alpha anima, 1 animation clock)
 *  - Tipografía: nombre headlineLarge blanco + timer displayMedium cyan
 *  - Botonera custom (círculos semi-transparentes, sin Material FAB)
 *  - Solo 2 animaciones totales: glow pulse + transición de estado
 *
 * Performance:
 *  - 1 animation clock (glow)
 *  - Timer actualiza cada 1s (no por frame)
 *  - Material Icons (cacheados) en vez de SVG custom
 *  - Sin blur real, sin sweepGradient, sin partículas
 *  - Presupuesto estimado: <3ms/frame en Redmi Note 9S
 */
@Composable
fun CallScreen(
    contactName: String = "Contacto",
    callType: String = "voice",
    isIncoming: Boolean = false,
    isConnected: Boolean = false,
    onAcceptCall: () -> Unit = {},
    onEndCall: () -> Unit = {},
    onToggleMute: (Boolean) -> Unit = {},
    onToggleSpeaker: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var callDuration by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var ringtonePlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Paleta
    val cyan = Color(0xFF4CE6FF)
    val bgInner = Color(0xFF0F2436)
    val bgOuter = Color(0xFF050A10)
    val danger = Color(0xFFFF1744)
    val acceptGreen = Color(0xFF00C853)
    val textSecondary = Color(0xFF8899AA)

    // Fondo gradiente radial estático (1 draw call)
    val bgBrush = remember {
        Brush.radialGradient(
            colors = listOf(bgInner, bgOuter),
            radius = 1200f
        )
    }

    // Glow pulse: solo el alpha anima (0.10 → 0.25 → 0.10, 2s loop)
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.10f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Ringtone: solo en incoming no conectado
    LaunchedEffect(isIncoming, isConnected) {
        if (isIncoming && !isConnected) {
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val player = MediaPlayer.create(context, ringtoneUri)
            if (player != null) {
                player.isLooping = true
                player.start()
                ringtonePlayer = player
            }
        } else {
            ringtonePlayer?.stop()
            ringtonePlayer?.release()
            ringtonePlayer = null
        }
    }

    // Timer: solo cuando está conectado
    LaunchedEffect(isConnected) {
        if (isConnected) {
            callDuration = 0
            while (true) {
                delay(1000)
                callDuration++
            }
        } else {
            callDuration = 0
        }
    }

    // Speakerphone
    LaunchedEffect(isSpeakerOn) {
        try { audioManager.isSpeakerphoneOn = isSpeakerOn } catch (_: Exception) {}
    }

    // Mute
    LaunchedEffect(isMuted) {
        try { audioManager.setMicrophoneMute(isMuted) } catch (_: Exception) {}
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            ringtonePlayer?.stop()
            ringtonePlayer?.release()
            try {
                audioManager.isSpeakerphoneOn = false
                audioManager.setMicrophoneMute(false)
            } catch (_: Exception) {}
        }
    }

    // Inicial del avatar
    val initial = contactName.take(1).uppercase().ifBlank { "?" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Espacio superior flexible
            Spacer(modifier = Modifier.weight(0.15f))

            // ── Avatar + Glow ────────────────────────────────
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow pulsante detrás (solo alpha anima)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .alpha(glowAlpha)
                        .clip(CircleShape)
                        .background(cyan)
                )
                // Avatar con borde cyan
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF15202B))
                        .border(2.dp, cyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 80.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Nombre del contacto ──────────────────────────
            Text(
                text = contactName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Estado / Timer (transición animada 200ms) ────
            AnimatedContent(
                targetState = when {
                    isIncoming && !isConnected -> "incoming"
                    isConnected -> "connected"
                    else -> "outgoing"
                },
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)))
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "stateTransition"
            ) { state ->
                when (state) {
                    "connected" -> Text(
                        text = formatDuration(callDuration),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light,
                        color = cyan
                    )
                    "incoming" -> Text(
                        text = "Llamada entrante",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                    else -> Text(
                        text = "Llamando...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            }

            // Espacio medio flexible
            Spacer(modifier = Modifier.weight(0.25f))

            // ── Botonera condicional ─────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (isIncoming && !isConnected) {
                    // Incoming: [Reject] [Accept]
                    Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                        CircleActionButton(
                            size = 72,
                            bgColor = danger,
                            icon = { Icon(Icons.Filled.CallEnd, "Rechazar", tint = Color.White, modifier = Modifier.size(32.dp)) },
                            onClick = onEndCall
                        )
                        CircleActionButton(
                            size = 72,
                            bgColor = acceptGreen,
                            icon = { Icon(Icons.Filled.Call, "Aceptar", tint = Color.White, modifier = Modifier.size(32.dp)) },
                            onClick = onAcceptCall
                        )
                    }
                } else if (isConnected) {
                    // Connected: [Mute] [End] [Speaker]
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircleActionButton(
                            size = 72,
                            bgColor = Color.White.copy(alpha = if (isMuted) 0.25f else 0.10f),
                            borderColor = Color.White.copy(alpha = 0.20f),
                            icon = {
                                Icon(
                                    if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                    "Micrófono",
                                    tint = if (isMuted) cyan else Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            onClick = { isMuted = !isMuted; onToggleMute(isMuted) }
                        )
                        CircleActionButton(
                            size = 88,
                            bgColor = danger,
                            icon = { Icon(Icons.Filled.CallEnd, "Colgar", tint = Color.White, modifier = Modifier.size(36.dp)) },
                            onClick = onEndCall
                        )
                        CircleActionButton(
                            size = 72,
                            bgColor = Color.White.copy(alpha = if (isSpeakerOn) 0.25f else 0.10f),
                            borderColor = Color.White.copy(alpha = 0.20f),
                            icon = {
                                Icon(
                                    if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                    "Altavoz",
                                    tint = if (isSpeakerOn) cyan else Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            onClick = { isSpeakerOn = !isSpeakerOn; onToggleSpeaker(isSpeakerOn) }
                        )
                    }
                } else {
                    // Outgoing: solo [End]
                    CircleActionButton(
                        size = 88,
                        bgColor = danger,
                        icon = { Icon(Icons.Filled.CallEnd, "Cancelar", tint = Color.White, modifier = Modifier.size(36.dp)) },
                        onClick = onEndCall
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Botón circular custom (no Material FAB — más liviano, sin elevation, sin ripple pesado).
 * Tamaño en dp como Int para evitar allocations por frame.
 */
@Composable
private fun CircleActionButton(
    size: Int,
    bgColor: Color,
    borderColor: Color? = null,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val dpSize = size.dp
    Box(
        modifier = Modifier
            .size(dpSize)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, CircleShape)
                } else Modifier
            )
            .clickableCircle(onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/**
 * Clickable circular sin ripple pesado (indication = null).
 * Feedback táctil viene del cambio de color del botón (mute/speaker on/off),
 * no del ripple Material. Ahorra allocations por frame.
 */
@Composable
private fun Modifier.clickableCircle(onClick: () -> Unit): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.then(
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    )
}

private fun formatDuration(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}
