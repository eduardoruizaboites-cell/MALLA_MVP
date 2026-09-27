package com.malla.mvp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ContactEntity
import com.malla.mvp.ui.components.ContactOptionsSheet
import com.malla.mvp.ui.components.ContactRow
import kotlinx.coroutines.launch

/**
 * Iter 53: pantalla Contactos — solo contactos MALLA (no agenda del celular).
 * Permite ver, silenciar, marcar favorito y eliminar con borrado en cascada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    val contacts by (db?.contactDao()?.observeAllSorted()
        ?: kotlinx.coroutines.flow.flowOf(emptyList<ContactEntity>()))
        .collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf(ContactFilter.Todos) }
    var optionsTarget by remember { mutableStateOf<ContactEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ContactEntity?>(null) }

    val filtered = remember(contacts, selectedFilter) {
        when (selectedFilter) {
            ContactFilter.Todos -> contacts
            ContactFilter.Favoritos -> contacts.filter { it.isFavorite }
            ContactFilter.Silenciados -> contacts.filter { it.isMuted }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1B2A))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Text(
                text = "Contactos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "(${contacts.size})",
                color = Color(0xFF8B949E),
                fontSize = 15.sp
            )
        }

        // Chips de filtro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContactFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CE6FF).copy(alpha = 0.15f),
                        selectedLabelColor = Color(0xFF4CE6FF),
                        labelColor = Color(0xFF8B949E)
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Lista o empty state
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CE6FF).copy(alpha = 0.08f))
                            .border(2.dp, Color(0xFF4CE6FF).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color(0xFF4CE6FF).copy(alpha = 0.6f),
                            fontSize = 40.sp
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Aún no tenés contactos",
                        color = Color(0xFFE6EDF3),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Buscá personas cerca o compartí tu código para agregar contactos",
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.contactUserId }) { contact ->
                    ContactRow(
                        contact = contact,
                        onClick = { /* TODO: abrir chat */ },
                        onMoreOptions = { optionsTarget = contact }
                    )
                }
            }
        }
    }

    // Bottom sheet de opciones
    optionsTarget?.let { target ->
        ContactOptionsSheet(
            contact = target,
            onDismiss = { optionsTarget = null },
            onToggleFavorite = {
                scope.launch { db?.contactDao()?.setFavorite(target.contactUserId, !target.isFavorite) }
            },
            onToggleMute = {
                scope.launch { db?.contactDao()?.setMuted(target.contactUserId, !target.isMuted) }
            },
            onDelete = { deleteTarget = target }
        )
    }

    // Diálogo de confirmación de eliminación
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color(0xFF141B22),
            title = { Text("Eliminar contacto", color = Color(0xFFE6EDF3)) },
            text = {
                Text(
                    "Se eliminará ${target.displayName} junto con la conversación y todos sus mensajes. Esta acción no se puede deshacer.",
                    color = Color(0xFF8B949E)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = target.contactUserId
                    scope.launch {
                        db?.contactDao()?.deleteCascade(id)
                        deleteTarget = null
                    }
                }) {
                    Text("Eliminar", color = Color(0xFFF87171), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = Color(0xFF4CE6FF))
                }
            }
        )
    }
}

enum class ContactFilter(val label: String) {
    Todos("Todos"),
    Favoritos("Favoritos"),
    Silenciados("Silenciados")
}
