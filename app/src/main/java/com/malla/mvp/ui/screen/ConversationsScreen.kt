package com.malla.mvp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.network.DhtWrapper
import com.malla.mvp.data.entity.ConversationEntity
import com.malla.mvp.data.entity.StoryEntity
import com.malla.mvp.ui.components.ConversationCard
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID
import com.malla.mvp.ui.components.NearbySection
import com.malla.mvp.ui.screen.ContactsScreen
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.core.model.ContactInvitation
import com.malla.mvp.ui.components.NearbyPanel
import com.malla.mvp.ui.components.IncomingRequestDialog
import com.malla.mvp.data.entity.ContactEntity
import com.malla.mvp.util.BiometricAuthHelper
import com.malla.mvp.network.ProximityEngine
import com.malla.mvp.network.InvitationManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malla.mvp.viewmodel.ConversationsViewModel

@Composable
fun ConversationsScreen(
    onChatClicked: (String, String) -> Unit,
    onProfileClicked: (String) -> Unit,
    onNavigateToQrScanner: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val conversationDao = remember { db?.conversationDao() }
    val storyDao = remember { db?.storyDao() }
    val vm: ConversationsViewModel = viewModel()
    val conversations by vm.conversations.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var showStoryViewer by remember { mutableStateOf(false) }
    var currentStoryUri by remember { mutableStateOf("") }
    var customTabs by remember { mutableStateOf(listOf<String>()) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }
    var selectedNearbyUser by remember { mutableStateOf<NearbyUser?>(null) }
    var showContacts by remember { mutableStateOf(false) }
    var acceptanceMessage by remember { mutableStateOf<String?>(null) }
    var incomingInvitation by remember { mutableStateOf<ContactInvitation?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        InvitationManager.incomingInvitation.collect { inv ->
            incomingInvitation = inv
        }
    }
    LaunchedEffect(Unit) {
        InvitationManager.acceptanceReceived.collect { (name, seed) ->
            acceptanceMessage = "${name} aceptó tu solicitud"
        }
    }

    var stories by remember { mutableStateOf<List<StoryEntity>>(emptyList()) }
    LaunchedEffect(storyDao) {
        storyDao?.getAllStories()?.collect { stories = it }
    }

    val allTabs = listOf("Todos", "No leídos", "Favoritos") + customTabs

    val filtered = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val tabFiltered = remember(filtered, selectedTab) {
        when (selectedTab) {
            0 -> filtered
            1 -> filtered.filter { it.unreadCount > 0 }
            2 -> filtered.filter { it.id.startsWith("sim") }
            3 -> filtered.filter { it.isGroup }
            else -> {
                val customLabel = allTabs.getOrElse(selectedTab) { "" }
                filtered.filter { it.title.contains(customLabel, ignoreCase = true) }
            }
        }
    }

    if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A1B2A), Color(0xFF0A1118)))
        ), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sin conversaciones", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    } else {
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0A1B2A), Color(0xFF0A1118)))
    )) {
        if (showStoryViewer) {
            StoryViewerScreen(imageUri = currentStoryUri, onFinished = { showStoryViewer = false })
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Barra de búsqueda mejorada
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, "Buscar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Buscar conversaciones...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Clear, "Limpiar", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Historias
            Text(
                "Historias",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(60.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            onClick = {
                                scope.launch {
                                    storyDao?.insertStory(StoryEntity(id = UUID.randomUUID().toString(), userId = "self", imageUri = "#FFFF00", timestamp = System.currentTimeMillis()))
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, "Nueva historia", tint = MaterialTheme.colorScheme.primary) }
                        }
                        Text("Nueva", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                items(stories) { story ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val haloColor = if (!story.seen) MaterialTheme.colorScheme.primary else Color.Transparent
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).border(2.dp, haloColor, CircleShape)) {
                            Surface(
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                onClick = { showStoryViewer = true; currentStoryUri = story.imageUri }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(story.userId.take(1).uppercase(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                        Text(story.userId.take(8), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Etiquetas
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allTabs.size) { index ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(allTabs[index]) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                item {
                    IconButton(onClick = {
                        val newLabel = "Nuevo ${customTabs.size + 1}"
                        customTabs = customTabs + newLabel
                        selectedTab = allTabs.size - 1
                    }) {
                        Icon(Icons.Filled.Add, "Añadir etiqueta", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tabFiltered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ChatBubbleOutline, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay conversaciones", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                item { NearbySection(onConnectClick = { user -> selectedNearbyUser = user }) }
                    items(tabFiltered, key = { it.id }) { conversation ->
                        val avatarBitmap: Bitmap? = if (conversation.id == "sim_alicia") IdentityManager.loadAvatar(context) else null
                        ConversationCard(
                            conversation = conversation,
                            onClick = { onChatClicked(conversation.id, conversation.title) },
                            avatarBitmap = avatarBitmap,
                            onProfile = { onProfileClicked(conversation.title) },
                            onStories = { Toast.makeText(context, "Historias de ${conversation.title}", Toast.LENGTH_SHORT).show() },
                            onHide = { scope.launch { conversationDao?.hideConversation(conversation.id) } },
                            onDelete = { scope.launch { conversationDao?.deleteConversation(conversation) } },
                            onArchive = { scope.launch { conversationDao?.hideConversation(conversation.id) } }
                        )
                    }
                }
            }
        }

        // FAB
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            FloatingActionButton(
                onClick = { showFabMenu = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Filled.Add, "Nuevo") }
            DropdownMenu(expanded = showFabMenu, onDismissRequest = { showFabMenu = false }) {
                DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Agregar usuario") } }, onClick = { showFabMenu = false; showAddContactDialog = true })
                DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.AddCircle, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Nueva historia") } }, onClick = { showFabMenu = false; scope.launch { storyDao?.insertStory(StoryEntity(id = UUID.randomUUID().toString(), userId = "self", imageUri = "#FF00FF", timestamp = System.currentTimeMillis())) } })
                DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Group, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Nuevo grupo") } }, onClick = {
                    showFabMenu = false
                    scope.launch {
                        val groupId = UUID.randomUUID().toString()
                        val group = ConversationEntity(id = groupId, title = "Grupo nuevo", isGroup = true, timestamp = System.currentTimeMillis())
                        conversationDao?.insertConversation(group)
                        onChatClicked(groupId, "Grupo nuevo")
                    }
                })
            }
        }

        // ── Diálogo Agregar usuario (Premium) ─────────────────
        if (showAddContactDialog) {
            AlertDialog(
                onDismissRequest = { showAddContactDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PersonAdd, null, tint = Color(0xFF4CE6FF), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Agregar usuario", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { showAddContactDialog = false; showCodeDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A2C3B)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Tag, null, tint = Color(0xFF4CE6FF), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Código de invitación", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    Text("Ingresa el código de 12 dígitos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { showAddContactDialog = false; onNavigateToQrScanner() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A2C3B)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.QrCodeScanner, null, tint = Color(0xFF4CE6FF), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Escanear QR", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    Text("Apuntar la cámara al código", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddContactDialog = false }) { Text("Cancelar", color = Color.Gray) }
                }
            )
        }

        // ── Diálogo Ingresar código de invitación (12 dígitos) ──
        if (showCodeDialog) {
            var inviteCode by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCodeDialog = false },
                title = { Text("Código de invitación") },
                text = {
                    Column {
                        Text("Ingresa el código de 12 dígitos que te compartió tu contacto")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inviteCode,
                            onValueChange = { inviteCode = it },
                            label = { Text("Código de 12 dígitos") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (inviteCode.isNotBlank() && inviteCode.length >= 12) {
                            scope.launch {
                                val convId = "invite_" + inviteCode.trim().take(12)
                                val conv = ConversationEntity(
                                    id = convId,
                                    title = "Invitación " + inviteCode.trim().take(12),
                                    timestamp = System.currentTimeMillis()
                                )
                                conversationDao?.insertConversation(conv)
                                showCodeDialog = false
                                // Intentar conexión directa (IP encriptada en el código)
                                try {
                                    val extra = inviteCode.trim().substring(8)
                                    val myUserId = IdentityManager.getIdentityId() ?: ""
                                    val ip = DhtWrapper.decryptIp(extra, myUserId)
                                    if (ip.isNotBlank()) {
                                        com.malla.mvp.network.NetworkService.connectToPeer(ip)
                                        // Reintentar si falla
                                        var retries = 0
                                        while (retries < 5 && com.malla.mvp.network.NetworkService.connectedClientsCount.value == 0) {
                                            delay(1000)
                                            retries++
                                            try { com.malla.mvp.network.NetworkService.connectToPeer(ip) } catch (_: Exception) {}
                                        }
                                        if (com.malla.mvp.network.NetworkService.connectedClientsCount.value > 0) {
                                            Toast.makeText(context, "Conectado a $ip", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No se pudo conectar. Verifica que el otro dispositivo esté en la misma red.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al conectar. Código inválido o dispositivo no disponible.", Toast.LENGTH_LONG).show()
                                }
                                onChatClicked(convId, "Invitación " + inviteCode.trim().take(12))
                                Toast.makeText(context, "Código aceptado", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Código inválido (debe tener al menos 12 caracteres)", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { showCodeDialog = false }) { Text("Cancelar") } }
            )
        }
    }
    } // cierre del if/else
    if (selectedNearbyUser != null) {
        NearbyPanel(
            user = selectedNearbyUser!!,
            onDismiss = { selectedNearbyUser = null },
            onSendRequest = { user ->
                scope.launch {
                    InvitationManager.sendInvitation(context, user)
                }
                selectedNearbyUser = null
            },
            onHide = { user ->
                ProximityEngine.hideUser(user.token)
                selectedNearbyUser = null
            },
            onBlock = { user ->
                ProximityEngine.blockUser(user.token)
                selectedNearbyUser = null
            }
        )
    }

    // Diálogo de invitación entrante
    if (acceptanceMessage != null) {
        AlertDialog(
            onDismissRequest = { acceptanceMessage = null },
            title = { Text("Solicitud aceptada") },
            text = { Text(acceptanceMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { acceptanceMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (incomingInvitation != null) {
        IncomingRequestDialog(
            invitation = incomingInvitation!!,
            onAccept = { inv ->
                scope.launch {
                        BiometricAuthHelper.authenticate(context,
                            onSuccess = {
                                scope.launch {
                                    try {
                                        // Guardar contacto en Room
                                        val contact = ContactEntity(
                                            contactUserId = inv.senderUserId,
                                            displayName = inv.senderDisplayName,
                                            avatarSeed = inv.senderAvatarSeed,
                                            publicKey = inv.senderPublicKey,
                                            addedAt = System.currentTimeMillis()
                                        )
                                        val db = AppDatabase.getInstance(context)
                                        db?.contactDao()?.insert(contact)
                                        val conversation = ConversationEntity(
                                            id = inv.senderUserId,
                                            title = inv.senderDisplayName,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        db?.conversationDao()?.insertConversation(conversation)
                                        Toast.makeText(context, "Solicitud de ${inv.senderDisplayName} aceptada", Toast.LENGTH_SHORT).show()
                                        // TODO: Enviar notificación de aceptación al emisor
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al guardar contacto", Toast.LENGTH_SHORT).show()
                                    }
                                    incomingInvitation = null
                                }
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                incomingInvitation = null
                            }
                        )
                    }
            },
            onReject = { inv ->
                incomingInvitation = null
            }
        )
    }
}