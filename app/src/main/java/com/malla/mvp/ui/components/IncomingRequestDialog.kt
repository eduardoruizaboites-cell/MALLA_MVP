package com.malla.mvp.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import com.malla.mvp.core.model.ContactInvitation

@Composable
fun IncomingRequestDialog(
    invitation: ContactInvitation,
    onAccept: (ContactInvitation) -> Unit,
    onReject: (ContactInvitation) -> Unit
) {
    Dialog(onDismissRequest = { onReject(invitation) }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(72.dp).clip(CircleShape),
                    color = Color(android.graphics.Color.HSVToColor(floatArrayOf((invitation.senderAvatarSeed * 27) % 360f, 0.7f, 0.9f)))
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = invitation.senderDisplayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFE6EDF3),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "quiere conectarse contigo",
                    color = Color(0xFF8B949E),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onAccept(invitation) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Aceptar", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onReject(invitation) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE74C3C)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}
