package com.malla.mvp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AcceptanceReceivedCard(
    acceptorName: String,
    acceptorAvatarSeed: Int,
    onOpenChat: () -> Unit,
    onDismiss: () -> Unit,
    autoDismissMs: Long = 8000L
) {
    val haptic = LocalHapticFeedback.current

    val transition = rememberInfiniteTransition(label = "acceptance_glow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(autoDismissMs)
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(horizontal = 8.dp, vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141B22)),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CE6FF).copy(alpha = glowAlpha))
                    )
                    Surface(
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        color = Color(
                            android.graphics.Color.HSVToColor(
                                floatArrayOf(
                                    (acceptorAvatarSeed * 27) % 360f,
                                    0.7f,
                                    0.9f
                                )
                            )
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = acceptorName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$acceptorName te agregó",
                        color = Color(0xFFE6EDF3),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Presiona para abrir el chat",
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = Color(0xFF4CE6FF),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { onOpenChat() }
                    ) {
                        Text(
                            text = "Abrir",
                            color = Color(0xFF0A1B2A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    TextButton(onClick = { onDismiss() }) {
                        Text(
                            text = "Cerrar",
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
