package com.malla.mvp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.network.ProximityEngine

@Composable
fun NearbySection(onConnectClick: (NearbyUser) -> Unit) {
    val context = LocalContext.current
    val nearbyUsers by ProximityEngine.nearbyUsers.collectAsState()

    DisposableEffect(Unit) {
        ProximityEngine.start(context)
        onDispose { ProximityEngine.stop() }
    }

    AnimatedVisibility(
        visible = nearbyUsers.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadarAnimation()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cerca de ti",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF0D1117)
                ) {
                    Text("${nearbyUsers.size}")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(nearbyUsers, key = { it.token }) { user ->
                    NearbyUserCard(user = user, onClick = { onConnectClick(user) })
                }
            }
        }
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )
    Canvas(modifier = Modifier.size(24.dp), onDraw = {
        drawCircle(color = Color(0xFF00E5FF).copy(alpha = alpha), radius = size.minDimension / 2)
    })
}

@Composable
fun NearbyUserCard(user: NearbyUser, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp).clip(CircleShape),
                color = Color(android.graphics.Color.HSVToColor(floatArrayOf((user.avatarSeed * 27) % 360f, 0.7f, 0.9f)))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.displayName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.displayName,
                color = Color(0xFFE6EDF3),
                fontSize = 14.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (user.signalStrength) {
                        3 -> "Fuerte"
                        2 -> "Media"
                        else -> "Débil"
                    },
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp
                )
            }
        }
    }
}
