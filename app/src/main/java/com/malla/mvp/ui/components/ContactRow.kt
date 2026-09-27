package com.malla.mvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.data.entity.ContactEntity

/**
 * Iter 53: fila de contacto para ContactsScreen.
 * Avatar circular con color derivado de avatarSeed, nombre, estado y iconos.
 */
@Composable
fun ContactRow(
    contact: ContactEntity,
    onClick: () -> Unit,
    onMoreOptions: () -> Unit
) {
    val (bgColor, initial) = contactColorAndInitial(contact.displayName, contact.avatarSeed)
    val rowAlpha = if (contact.isMuted) 0.6f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con glow
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bgColor.copy(alpha = 0.25f))
                .border(1.5.dp, bgColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.displayName,
                    color = Color(0xFFE6EDF3).copy(alpha = rowAlpha),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                if (contact.isFavorite) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Favorito",
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (contact.isMuted) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.NotificationsOff,
                        contentDescription = "Silenciado",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (contact.isMuted) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Silenciado",
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp
                )
            }
        }

        IconButton(onClick = onMoreOptions) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Más opciones",
                tint = Color(0xFF8B949E)
            )
        }
    }
}

private fun contactColorAndInitial(name: String, seed: Int): Pair<Color, String> {
    val hue = ((seed * 27) % 360).toFloat()
    val color = Color.hsv(hue, 0.7f, 0.9f)
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    return color to initial
}
