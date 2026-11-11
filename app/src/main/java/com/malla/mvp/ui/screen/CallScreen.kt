package com.malla.mvp.ui.screen

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Iter 70D-1: pantalla de llamada con estado externo.
 * El estado (Incoming/Outgoing/Connected) lo maneja WebRtcManager y lo expone CallViewModel.
 * Esta pantalla solo renderiza según el estado y emite eventos (accept/end/mute/speaker).
 *
 * - Ringtone solo si isIncoming && !isConnected
 * - Timer solo si isConnected, empieza en 0 al conectar
 * - Botonera condicional según estado
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

    // Timer: solo cuando está conectado, se resetea a 0 al conectar
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

    // Control de altavoz
    LaunchedEffect(isSpeakerOn) {
        try {
            audioManager.isSpeakerphoneOn = isSpeakerOn
        } catch (_: Exception) {}
    }

    // Control de mute
    LaunchedEffect(isMuted) {
        try {
            audioManager.setMicrophoneMute(isMuted)
        } catch (_: Exception) {}
    }

    // Limpieza al salir
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

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A1B2A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(contactName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    isIncoming && !isConnected -> "Llamada entrante..."
                    isConnected -> if (callType == "video") "Videollamada" else "Llamada de voz"
                    else -> "Llamando..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8899AA)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isConnected) {
                Text(
                    text = formatDuration(callDuration),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(48.dp))

            // Botonera condicional
            when {
                isIncoming && !isConnected -> {
                    // Llamada entrante: Reject (rojo) + Accept (verde)
                    Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                        FloatingActionButton(
                            onClick = onEndCall,
                            containerColor = Color(0xFFFF1744)
                        ) {
                            Icon(Icons.Filled.CallEnd, contentDescription = "Rechazar", tint = Color.White)
                        }
                        FloatingActionButton(
                            onClick = onAcceptCall,
                            containerColor = Color(0xFF00C853)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Aceptar", tint = Color.White)
                        }
                    }
                }
                isConnected -> {
                    // Conectado: Mute + End + Speaker
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        FloatingActionButton(
                            onClick = {
                                isMuted = !isMuted
                                onToggleMute(isMuted)
                            },
                            containerColor = Color.White.copy(alpha = if (isMuted) 0.8f else 0.2f)
                        ) {
                            Icon(
                                if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Micrófono",
                                tint = if (isMuted) Color.Red else Color.White
                            )
                        }
                        FloatingActionButton(
                            onClick = onEndCall,
                            containerColor = Color(0xFFFF1744)
                        ) {
                            Icon(Icons.Filled.CallEnd, contentDescription = "Colgar", tint = Color.White)
                        }
                        FloatingActionButton(
                            onClick = {
                                isSpeakerOn = !isSpeakerOn
                                onToggleSpeaker(isSpeakerOn)
                            },
                            containerColor = Color.White.copy(alpha = if (isSpeakerOn) 0.8f else 0.2f)
                        ) {
                            Icon(
                                if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                contentDescription = "Altavoz",
                                tint = Color.White
                            )
                        }
                    }
                }
                else -> {
                    // Outgoing: Cancel + "Llamando..."
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = Color(0xFFFF1744)
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Cancelar", tint = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}
