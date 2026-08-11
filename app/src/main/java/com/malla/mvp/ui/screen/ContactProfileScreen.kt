package com.malla.mvp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Shield
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
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContactProfileScreen(
    contactName: String,
    contactId: String = "",
    onBack: () -> Unit,
    onChatClicked: (String, String) -> Unit = { _, _ -> },
    onVoiceCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var contact by remember { mutableStateOf<ContactEntity?>(null) }
    var showFingerprintDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contactId) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            contact = db?.contactDao()?.getById(contactId)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Barra superior
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Volver", tint = Color(0xFFE6EDF3))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Perfil de contacto", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6EDF3))
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Avatar y nombre
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.size(100.dp).clip(CircleShape),
                color = Color(0xFF1976D2).copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(contactName.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE6EDF3))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(contactName, style = MaterialTheme.typography.headlineSmall, color = Color(0xFFE6EDF3))
            if (contact != null) {
                Text(contact!!.contactUserId, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B949E))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones de acción
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onChatClicked(contactId, contactName) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Icon(Icons.Filled.Chat, "Chat", tint = Color(0xFF0D1117))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Chat", color = Color(0xFF0D1117))
            }
            Button(
                onClick = onVoiceCallClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
            ) {
                Icon(Icons.Filled.Call, "Voz", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Voz", color = Color.White)
            }
            Button(
                onClick = onVideoCallClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
            ) {
                Icon(Icons.Filled.Videocam, "Video", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Video", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verificación de seguridad
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Seguridad", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, "Cifrado", tint = Color(0xFF2ECC71), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cifrado de extremo a extremo", color = Color(0xFFE6EDF3), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (contact != null) {
                    Text("Fingerprint: ${contact!!.publicKey.take(16)}...", color = Color(0xFF8B949E), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showFingerprintDialog = true }) {
                    Text("Verificar clave de seguridad", color = Color(0xFF00E5FF))
                }
            }
        }
    }

    // Diálogo de verificación
    if (showFingerprintDialog) {
        AlertDialog(
            onDismissRequest = { showFingerprintDialog = false },
            title = { Text("Clave de seguridad") },
            text = {
                Text(
                    "Compara esta clave con la que ve tu contacto para asegurar que no hay suplantación:\n\n" +
                    (contact?.publicKey ?: "No disponible"),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showFingerprintDialog = false }) { Text("Hecho") }
            },
            containerColor = Color(0xFF161B22),
            titleContentColor = Color(0xFFE6EDF3),
            textContentColor = Color(0xFF8B949E)
        )
    }
}
