package com.malla.mvp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(onBack: () -> Unit, onChatClicked: (contactId: String, contactName: String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<ContactEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        db?.contactDao()?.observeAllVisible()?.collect { list ->
            contacts = list
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFFE6EDF3))
            }
            Text(
                "Contactos",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFE6EDF3),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes contactos aún", color = Color(0xFF8B949E))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(contacts, key = { it.contactUserId }) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp).clip(CircleShape),
                                color = Color(android.graphics.Color.HSVToColor(floatArrayOf((contact.avatarSeed * 27) % 360f, 0.7f, 0.9f)))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contact.displayName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.displayName,
                                    color = Color(0xFFE6EDF3),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = contact.contactUserId.take(12),
                                    color = Color(0xFF8B949E),
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                onChatClicked(contact.contactUserId, contact.displayName)
                            }) {
                                Icon(Icons.Default.Chat, contentDescription = "Abrir chat", tint = Color(0xFF00E5FF))
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    val db = AppDatabase.getInstance(context)
                                    db?.contactDao()?.delete(contact)
                                    Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE74C3C))
                            }
                        }
                    }
                }
            }
        }
    }
}
