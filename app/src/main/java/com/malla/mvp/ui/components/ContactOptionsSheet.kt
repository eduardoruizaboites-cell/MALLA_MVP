package com.malla.mvp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.data.entity.ContactEntity

/**
 * Iter 53: bottom sheet de opciones por contacto.
 * Silenciar / Favorito / Eliminar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactOptionsSheet(
    contact: ContactEntity,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141B22),
        contentColor = Color(0xFFE6EDF3)
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = contact.displayName,
                color = Color(0xFFE6EDF3),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            HorizontalDivider(color = Color(0xFF22303C))

            SheetItem(
                icon = if (contact.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                iconTint = Color(0xFFFFD166),
                text = if (contact.isFavorite) "Quitar de favoritos" else "Marcar como favorito",
                onClick = { onDismiss(); onToggleFavorite() }
            )
            SheetItem(
                icon = if (contact.isMuted) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                iconTint = Color(0xFF4CE6FF),
                text = if (contact.isMuted) "Reactivar notificaciones" else "Silenciar notificaciones",
                onClick = { onDismiss(); onToggleMute() }
            )

            HorizontalDivider(color = Color(0xFF22303C), modifier = Modifier.padding(vertical = 6.dp))

            SheetItem(
                icon = Icons.Filled.Delete,
                iconTint = Color(0xFFF87171),
                text = "Eliminar contacto",
                textColor = Color(0xFFF87171),
                onClick = { onDismiss(); onDelete() }
            )
        }
    }
}

@Composable
private fun SheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    text: String,
    textColor: Color = Color(0xFFE6EDF3),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = textColor, fontSize = 15.sp)
    }
}
