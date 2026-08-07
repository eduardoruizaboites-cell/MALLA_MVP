package com.malla.mvp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ContactEntity
import com.malla.mvp.data.entity.ContactStatus
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.ui.components.QrCodeDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VerificationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val myUserId = remember { IdentityManager.getUserId(context) ?: "No ID" }
    var inputUserId by remember { mutableStateOf("") }
    var verifyStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Verificación de contactos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tu ID es:", style = MaterialTheme.typography.bodyMedium)
        Text(myUserId, style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CE6FF), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        QrCodeDisplay(content = "malla://contact?id=$myUserId", size = 200)
        Divider()
        Text("Ingresa el ID de tu contacto para verificarlo", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = inputUserId,
            onValueChange = { inputUserId = it },
            label = { Text("ID del contacto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CE6FF),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Button(
            onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(context)
                        val contact = db?.contactDao()?.getContactByUserId(inputUserId.trim())
                        withContext(Dispatchers.Main) {
                            if (contact != null) {
                                verifyStatus = "Contacto encontrado: ${contact.localAlias ?: contact.userId}\nEstado: ${contact.status}"
                                // Opcional: confirmar automáticamente o pedir confirmación
                                if (contact.status == ContactStatus.PENDING) {
                                    scope.launch(Dispatchers.IO) {
                                        db?.contactDao()?.updateContactStatus(contact.pubKeyBase64, ContactStatus.CONFIRMED)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Contacto confirmado", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    }
                                }
                            } else {
                                verifyStatus = "No se encontró ningún contacto con ese ID"
                            }
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verificar", color = Color.Black)
        }
        if (verifyStatus.isNotEmpty()) {
            Text(verifyStatus, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}
