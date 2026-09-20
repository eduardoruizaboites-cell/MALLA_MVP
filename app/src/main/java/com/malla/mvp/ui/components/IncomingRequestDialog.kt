package com.malla.mvp.ui.components

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.malla.mvp.core.model.ContactInvitation
import com.malla.mvp.util.BiometricAuthHelper

@Composable
fun IncomingRequestDialog(
    invitation: ContactInvitation,
    onAccept: (ContactInvitation) -> Unit,
    onReject: (ContactInvitation) -> Unit
) {
    var authenticating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.20f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    var pressed by remember { mutableStateOf(false) }
    val acceptScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )

    val hasBiometric = remember {
        try {
            BiometricManager.from(context).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) { false }
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).widthIn(max = 360.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141B22)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CE6FF).copy(alpha = glowAlpha))
                    )
                    Surface(
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                        color = Color(android.graphics.Color.HSVToColor(
                            floatArrayOf(
                                (invitation.senderAvatarSeed * 27) % 360f,
                                0.7f,
                                0.9f
                            )
                        ))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = invitation.senderDisplayName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = invitation.senderDisplayName,
                    color = Color(0xFFE6EDF3),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "quiere agregarte a sus contactos",
                    color = Color(0xFF8B949E),
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(20.dp))

                Surface(
                    color = Color(0xFF4CE6FF).copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFF4CE6FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (hasBiometric) "Confirma con tu huella" else "Toca para confirmar",
                            color = Color(0xFF4CE6FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!authenticating) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            authenticating = true
                            BiometricAuthHelper.authenticate(
                                context = context,
                                onSuccess = {
                                    authenticating = false
                                    onAccept(invitation)
                                },
                                onError = { err ->
                                    authenticating = false
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).scale(acceptScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CE6FF),
                        contentColor = Color(0xFF0A1B2A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (authenticating) "Verificando..." else "Aceptar",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = { onReject(invitation) },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(
                        text = "Rechazar",
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
