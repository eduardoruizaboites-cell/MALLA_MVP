package com.malla.mvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.core.model.NearbyUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPanel(
    user: NearbyUser,
    onDismiss: () -> Unit,
    onSendRequest: (NearbyUser) -> Unit,
    onHide: (NearbyUser) -> Unit,
    onBlock: (NearbyUser) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        contentColor = Color(0xFFE6EDF3)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar grande
            Surface(
                modifier = Modifier.size(80.dp).clip(CircleShape),
                color = Color(android.graphics.Color.HSVToColor(floatArrayOf((user.avatarSeed * 27) % 360f, 0.7f, 0.9f)))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.displayName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFE6EDF3),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Señal: ${when (user.signalStrength) { 3 -> "Fuerte" 2 -> "Media" else -> "Débil" }}",
                color = Color(0xFF8B949E),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            Button(
                onClick = { onSendRequest(user) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF0D1117))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar solicitud", color = Color(0xFF0D1117), fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onHide(user) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B949E)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.VisibilityOff, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ocultar")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onBlock(user) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE74C3C)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Block, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bloquear")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
