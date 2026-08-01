package com.malla.mvp.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.identity.IdentityManager

@Composable
fun ComposingBubble(
    isOwn: Boolean,
    avatarBitmap: Bitmap?,
    userName: String,
    modifier: Modifier = Modifier
) {
    val alignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = Color.Transparent

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (avatarBitmap != null) {
                Image(
                    bitmap = avatarBitmap.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF4CE6FF), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = Color(0xFF4CE6FF).copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(userName, style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CE6FF))
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            // Tres puntos saltarines
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    val infiniteTransition = rememberInfiniteTransition(label = "compose_dot_$i")
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, delayMillis = i * 150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha_$i"
                    )
                    val dotOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, delayMillis = i * 150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_offset_$i"
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = (-dotOffset).dp)
                            .size(11.dp)
                            .alpha(dotAlpha)
                            .background(Color.White, CircleShape)
                    )
                    if (i < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}
