package com.malla.mvp.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.network.ProximityEngine
import com.malla.mvp.network.DhtWrapper
import com.malla.mvp.ui.components.QrCodeDisplay
import com.malla.mvp.core.crypto.InviteCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PerfilScreen(onVerifyClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val avatarBitmap by IdentityManager.avatarBitmap.collectAsState()
    val displayName = remember { IdentityManager.getUserName(context) ?: IdentityManager.getUserName(context) }
    var userName by remember { mutableStateOf(displayName) }
    var userStatus by remember { mutableStateOf(IdentityManager.getUserStatus(context)) }
    val userId = remember { IdentityManager.getIdentityId() ?: "Sin ID" }
    var bannerBitmap by remember { mutableStateOf(IdentityManager.loadBanner(context)) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    val prefs = remember { context.getSharedPreferences("perfil_prefs", android.content.Context.MODE_PRIVATE) }
    var nameVisible by remember { mutableStateOf(prefs.getBoolean("name_visible", true)) }
    var statusVisible by remember { mutableStateOf(prefs.getBoolean("status_visible", true)) }
    var lastSeenVisible by remember { mutableStateOf(prefs.getBoolean("last_seen_visible", true)) }
    var meshVisible by remember { mutableStateOf(prefs.getBoolean("mesh_visible", true)) }
    var readReceipts by remember { mutableStateOf(prefs.getBoolean("read_receipts", true)) }
    var messagePermission by remember { mutableStateOf(prefs.getString("message_permission", "todos") ?: "todos") }

    var qrPayload by remember { mutableStateOf<String?>(null) }
    var inviteCode by remember { mutableStateOf<InviteCodeGenerator.InviteCode?>(null) }
    var qrExpired by remember { mutableStateOf(false) }

    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { IdentityManager.saveAvatar(context, it) }
    }
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { bannerBitmap = IdentityManager.saveBanner(context, it) }
    }

    fun performAfterBiometricAuth(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        Toast.makeText(context, "Autenticación cancelada", Toast.LENGTH_SHORT).show()
                    }
                    override fun onAuthenticationFailed() {
                        Toast.makeText(context, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verificación biométrica")
                .setSubtitle("Confirma tu identidad para continuar")
                .setNegativeButtonText("Cancelar")
                .build()
            biometricPrompt.authenticate(promptInfo)
        } else {
            onSuccess()
        }
    }

    fun generateQrAfterAuth() {
        performAfterBiometricAuth {
            try {
                val userId = IdentityManager.getIdentityId() ?: "Sin ID"
                val ip = DhtWrapper.getLocalAddress() ?: "127.0.0.1"
                qrPayload = "malla://connect?ip=$ip&userId=$userId"
                qrExpired = false
                Toast.makeText(context, "QR generado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun generateCodeAfterAuth() {
        performAfterBiometricAuth {
            val myUserId = IdentityManager.getIdentityId() ?: "unknown"
                                val myIp = DhtWrapper.getLocalAddress() ?: "127.0.0.1"
                                val encryptedIp = DhtWrapper.encryptIp(myIp, myUserId)
                                inviteCode = InviteCodeGenerator.generate(extra = encryptedIp)
        }
    }

    // Refrescar código (botón adicional)
    fun refreshCode() {
        inviteCode = null  // invalidar el anterior
        generateCodeAfterAuth()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1B2A), Color(0xFF0A1118))))
            .padding(bottom = 32.dp)
    ) {
        // ── Banner ───────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            if (bannerBitmap != null) {
                Image(bitmap = bannerBitmap!!.asImageBitmap(), contentDescription = "Banner", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {}
            }
            IconButton(onClick = { bannerLauncher.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                Icon(Icons.Filled.Edit, "Cambiar banner", tint = Color.White)
            }
        }

        // ── Avatar ───────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.offset(y = (-60).dp).size(120.dp)) {
                Surface(modifier = Modifier.size(120.dp).clip(CircleShape).border(3.dp, Color(0xFF4CE6FF), CircleShape), color = Color(0xFF1A2A3A), shape = CircleShape) {
                    if (avatarBitmap != null) Image(bitmap = avatarBitmap!!.asImageBitmap(), contentDescription = "Avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Box(contentAlignment = Alignment.Center) { Text(text = userName.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = Color(0xFF4CE6FF), fontWeight = FontWeight.Bold) }
                }
                IconButton(onClick = { avatarLauncher.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).offset(x = (-2).dp, y = (-2).dp).background(Color(0xFF4CE6FF), CircleShape)) {
                    Icon(Icons.Filled.CameraAlt, "Cambiar avatar", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Nombre y ID ─────────────────────────────────────
        Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text(text = userName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
            IconButton(onClick = { editedName = userName; showEditNameDialog = true }) { Icon(Icons.Filled.Edit, "Editar nombre", tint = Color(0xFF4CE6FF)) }
        }
        Text(text = "@$userId", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CE6FF), modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = userStatus, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(20.dp))

        // ── Verificar contactos ──────────────────────────────
        Button(onClick = onVerifyClick, modifier = Modifier.align(Alignment.CenterHorizontally), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF)), shape = RoundedCornerShape(20.dp)) {
            Icon(Icons.Default.QrCode, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verificar contactos", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── PRIVACIDAD ──────────────────────────────────────
        Text("Privacidad", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CE6FF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2C3B))) {
            Column(modifier = Modifier.padding(16.dp)) {
                SwitchRow("Mostrar nombre", nameVisible) { nameVisible = it; prefs.edit().putBoolean("name_visible", it).apply() }
                SwitchRow("Mostrar estado", statusVisible) { statusVisible = it; prefs.edit().putBoolean("status_visible", it).apply() }
                SwitchRow("Última conexión", lastSeenVisible) { lastSeenVisible = it; prefs.edit().putBoolean("last_seen_visible", it).apply() }
                SwitchRow("Confirmación de lectura", readReceipts) { readReceipts = it; prefs.edit().putBoolean("read_receipts", it).apply() }
                SwitchRow("Visible en Mesh Cercano", meshVisible) { meshVisible = it; prefs.edit().putBoolean("mesh_visible", it).apply(); if (it) ProximityEngine.startAdvertising(IdentityManager.getUserName(context), 0) else ProximityEngine.stopAdvertising() }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quién puede enviarme mensajes", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos" to "todos", "Contactos" to "contactos", "Nadie" to "nadie").forEach { (label, value) ->
                        FilterChip(
                            selected = messagePermission == value,
                            onClick = { messagePermission = value; prefs.edit().putString("message_permission", value).apply() },
                            label = { Text(label, color = if (messagePermission == value) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CE6FF), selectedLabelColor = Color.Black)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── COMPARTIR IDENTIDAD ──────────────────────────────
        Text("Compartir identidad", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CE6FF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top  // alinear arriba para que sean simétricas
        ) {
            // Tarjeta QR
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2C3B))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Icon(Icons.Filled.QrCode, "QR", tint = Color(0xFF4CE6FF), modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("QR efímero", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("60 s", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8899AA))
                    if (qrPayload != null && !qrExpired) {
                        Spacer(Modifier.height(8.dp))
                        QrCodeDisplay(content = qrPayload!!, size = 100)
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { generateQrAfterAuth() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF)), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("Generar", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Tarjeta Código 24h
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2C3B))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Icon(Icons.Filled.Tag, "Código", tint = Color(0xFF4CE6FF), modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Código 24h", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    if (inviteCode != null && InviteCodeGenerator.isValid(inviteCode)) {
                        Spacer(Modifier.height(8.dp))
                        Text(inviteCode!!.fullCode.chunked(4).joinToString("-"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF4CE6FF), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "¡Hola! Hablemos por Malla. Mi código de invitación es: ${inviteCode!!.fullCode}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir código"))
                            }) { Icon(Icons.Filled.Share, "Compartir", tint = Color(0xFF4CE6FF)) }
                            IconButton(onClick = { refreshCode() }) { Icon(Icons.Filled.Refresh, "Refrescar código", tint = Color(0xFF4CE6FF)) }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { generateCodeAfterAuth() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF)), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("Generar", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Diálogo para editar nombre ────────────────────────────
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Editar nombre") },
            text = { OutlinedTextField(value = editedName, onValueChange = { editedName = it }, label = { Text("Nombre o apodo") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { if (editedName.isNotBlank()) { userName = editedName; IdentityManager.setUserName(context, editedName); IdentityManager.setUserName(context, editedName) }; showEditNameDialog = false }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { showEditNameDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CE6FF)))
    }
}
