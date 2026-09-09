package com.malla.mvp
import androidx.fragment.app.FragmentActivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import android.bluetooth.BluetoothAdapter
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.core.transport.FlashlightTransport
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ConversationEntity
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.core.crypto.IdentityQrPayload
import com.malla.mvp.core.model.ContactInvitation
import com.malla.mvp.data.entity.ContactEntity
import com.malla.mvp.network.ConnectivityMonitor
import com.malla.mvp.network.MeshConnector
import com.malla.mvp.network.BleTransport
import com.malla.mvp.util.RadioManager
import com.malla.mvp.service.MeshChatService
import com.malla.mvp.network.ProximityEngine
import com.malla.mvp.network.BleManager
import com.malla.mvp.util.NotificationHelper
import com.malla.mvp.service.CacheCleanerWorker
import com.malla.mvp.core.engine.DeviceStateMonitor
import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.network.DhtWrapper
import com.malla.mvp.network.NetworkService
import com.malla.mvp.network.TransportManager
import com.malla.mvp.network.InvitationManager
import com.malla.mvp.ui.components.MainTopBar
import com.malla.mvp.ui.components.StickerPickerDialog
import com.malla.mvp.ui.components.StickerFullScreenDialog
import com.malla.mvp.ui.components.SplashScreen
import com.malla.mvp.ui.screen.RegistrationScreen
import com.malla.mvp.ui.components.StickerState
import com.malla.mvp.ui.components.ConnectivityStatusBar
import com.malla.mvp.ui.components.TutorialOverlay
import com.malla.mvp.ui.screen.*
import com.malla.mvp.ui.settings.AccessibilitySettings
import com.malla.mvp.ui.theme.MallaColorScheme
import com.malla.mvp.R
import com.malla.mvp.ui.theme.MallaTheme
import com.malla.mvp.viewmodel.AppThemeState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.compose.runtime.mutableStateOf

enum class AppState { Splash, Main }

class MainActivity : FragmentActivity() {
    companion object {
        @Volatile var appForeground = false
            private set
        @Volatile var instanceForeground = false
            private set
    }

    @Volatile private var isForeground = false
        private set

    private val notifiedNodes = mutableSetOf<String>()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val showPermissionExplanationState = mutableStateOf(true)

    override fun onResume() {
        super.onResume()
        isForeground = true
        MainActivity.appForeground = true
        MainActivity.instanceForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
        MainActivity.appForeground = false
        MainActivity.instanceForeground = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar identidad antes de cualquier operación criptográfica
        IdentityManager.init(this)
        DiagnosticsLogger.init(this)
        DiagnosticsLogger.logDeviceInfo(this)

        // Iniciar componentes base
        ConnectivityMonitor.start(application)
        DeviceStateMonitor.start(this)
        CacheCleanerWorker.schedule(this)
        NotificationHelper.createChannel(this)
        DhtWrapper.init(this)
        insertSampleStories()

        // Iniciar servidor TCP siempre (para comunicación directa)
        NetworkService.startServer()
        LogBuffer.add("MAIN", "NetworkService iniciado")

        // Iniciar descubrimiento y conexión mesh si los permisos ya están concedidos
        if (hasRequiredPermissions()) {
            // La comunicación mesh la inicia MeshChatService, no aquí
            MainScope().launch(Dispatchers.Default) {
                enableRadio()
            }

            // Observar descubrimiento de nodos para notificación
            MainScope().launch(Dispatchers.IO) {
                ProximityEngine.nearbyUsers.collect { users ->
                    users.forEach { user ->
                        if (user.token !in notifiedNodes) {
                            notifiedNodes.add(user.token)
                            if (!isForeground && (user.bluetoothDevice != null || user.token.startsWith("mdns_"))) {
                                NotificationHelper.showDiscoveryNotification(this@MainActivity, user.displayName)
                            }
                        }
                    }
                }
            }
        }
        val permissionPrefs = getSharedPreferences("malla_prefs", Context.MODE_PRIVATE)
        showPermissionExplanationState.value = !permissionPrefs.getBoolean("permission_explanation_shown", false)

        // Configurar lanzador de permisos
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            val allGranted = grants.values.all { it }
            if (allGranted) {
                LogBuffer.add("MAIN", "Permisos concedidos, habilitando radio")
                enableRadio()
                ProximityEngine.ensureAdvertising(this@MainActivity)
                Toast.makeText(this, "Comunicación mesh activa", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Algunos permisos fueron denegados. La app puede funcionar con limitaciones.", Toast.LENGTH_LONG).show()
            }
            showPermissionExplanationState.value = false
            try {
                getSharedPreferences("malla_prefs", Context.MODE_PRIVATE).edit().putBoolean("permission_explanation_shown", true).commit()
            } catch (_: Exception) {}
        }

        // Iniciar servicio foreground para mantener la comunicación viva
        val serviceIntent = Intent(this, MeshChatService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Forzar solicitud de permisos BLE si faltan en Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blePerms = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            if (blePerms.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
                // Evitar mostrar pantalla de explicación al mismo tiempo
                showPermissionExplanationState.value = false
                requestPermissions()
            }
        }

        val appThemeState = AppThemeState.create(this)

        val prefs = try {
            getSharedPreferences("malla_prefs", Context.MODE_PRIVATE)
        } catch (e: Exception) { null }
        val permissionExplanationShown = try {
            prefs?.getBoolean("permission_explanation_shown", false) ?: false
        } catch (e: Exception) { false }
        
        val isFirstLaunch = try {
            prefs?.getBoolean("first_launch", true) ?: true
        } catch (e: Exception) { true }

        val database = AppDatabase.getInstance(application)
        val conversationIdFromNotification = intent?.getStringExtra("conversation_id")

        setContent {
            val context = LocalContext.current
            var appState by remember { mutableStateOf(AppState.Splash) }
            var showQrScanner by remember { mutableStateOf(false) }
            var showRegistration by remember { mutableStateOf(!IdentityManager.isRegistrationComplete(context)) }
            var currentConversationId by remember { mutableStateOf(conversationIdFromNotification) }
            var selectedContact by remember { mutableStateOf<String?>(null) }
            var showSettings by remember { mutableStateOf(false) }
            var showChatSettings by remember { mutableStateOf(false) }
            var showCall by remember { mutableStateOf(false) }
            var callContact by remember { mutableStateOf("") }
            var callType by remember { mutableStateOf("voice") }
            var showTutorial by remember { mutableStateOf(false) }
            var showPermissionExplanation by showPermissionExplanationState
            val flashlight = remember { FlashlightTransport(context) }
            var incomingInvitation by remember { mutableStateOf<ContactInvitation?>(null) }
            var showInvitationDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                InvitationManager.incomingInvitation.collect { invitation ->
                    incomingInvitation = invitation
                    showInvitationDialog = true
                }
            }

            if (showInvitationDialog && incomingInvitation != null) {
                val invitation = incomingInvitation!!
                AlertDialog(
                    onDismissRequest = { showInvitationDialog = false; incomingInvitation = null },
                    title = { Text("Solicitud de contacto") },
                    text = { Text("${invitation.senderDisplayName} (${invitation.senderUserId}) quiere agregarte a sus contactos.") },
                    confirmButton = {
                        TextButton(onClick = {
                            // Autenticación biométrica antes de aceptar
                            val biometricManager = BiometricManager.from(context)
                            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                                val executor = ContextCompat.getMainExecutor(context)
                                val biometricPrompt = BiometricPrompt(
                                    context as FragmentActivity,
                                    executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            MainScope().launch(Dispatchers.IO) {
                                                InvitationManager.sendAcceptance(context, invitation)
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
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
                                    .setSubtitle("Confirma tu identidad para aceptar la solicitud")
                                    .setNegativeButtonText("Cancelar")
                                    .build()
                                biometricPrompt.authenticate(promptInfo)
                            } else {
                                MainScope().launch(Dispatchers.IO) {
                                    InvitationManager.sendAcceptance(context, invitation)
                                }
                            }
                            showInvitationDialog = false
                            incomingInvitation = null
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showInvitationDialog = false; incomingInvitation = null }) { Text("Rechazar") }
                    }
                )
            }

            val effectiveScheme by appThemeState.currentTheme.collectAsState()
            val isOnline by ConnectivityMonitor.isOnline.collectAsState()
            val meshToastShown = remember { mutableStateOf(false) }

            LaunchedEffect(isOnline) {
                if (!isOnline) {
                    if (!meshToastShown.value) {
                        android.widget.Toast.makeText(
                            context,
                            "Modo Mesh activado – comunicaciones locales",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        meshToastShown.value = true
                    }
                    LogBuffer.add("MAIN", "Sin internet – usando comunicaciones locales")
                    // Publicar presencia en DHT
                    val myUserId = IdentityManager.getIdentityId() ?: ""
                    val myIp = DhtWrapper.getLocalAddress() ?: "127.0.0.1"
                    DhtWrapper.publish(myUserId, myIp, NetworkService.DEFAULT_PORT)
                }
            }

            LaunchedEffect(appState) {
                if (appState == AppState.Main && !isFirstLaunch) {
                    val tutorialPrefs = try {
                        getSharedPreferences("tutorial", Context.MODE_PRIVATE)
                    } catch (e: Exception) { null }
                    val tutorialShown = try {
                        tutorialPrefs?.getBoolean("shown", false) ?: false
                    } catch (e: Exception) { false }
                    if (!tutorialShown && !isFirstLaunch) {
                        showTutorial = true
                    }
                }
            }

            MallaTheme(colorScheme = effectiveScheme, fontScale = AccessibilitySettings.fontScale.value) {
                AnimatedContent(
                    targetState = appState,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut(tween(300)))
                    },
                    label = "app_state_transition"
                ) { state ->
                    when (state) {
                        AppState.Splash -> SplashScreen {
                            appState = AppState.Main
                        }
                        AppState.Main -> {
                            if (showPermissionExplanation) {
                                PermissionExplanationScreen(
                                    onContinue = {
                                        showPermissionExplanation = false
                                        requestPermissions()
                                    }
                                )
                            } else if (showRegistration) {
                                RegistrationScreen(onComplete = { showRegistration = false })
                            } else if (showTutorial) {
                                TutorialOverlay(
                                    onDismiss = {
                                        showTutorial = false
                                        try {
                                            getSharedPreferences("tutorial", Context.MODE_PRIVATE)
                                                ?.edit()?.putBoolean("shown", true)?.apply()
                                        } catch (_: Exception) {}
                                    }
                                )
                            } else if (showQrScanner) {
                                BackHandler { showQrScanner = false }
                                QrScanScreen(
                                    onQrScanned = { payload ->
                                        showQrScanner = false
                                        MainScope().launch(Dispatchers.IO) {
                                            val parsed = IdentityQrPayload.parseAndVerify(payload)
                                            if (parsed != null) {
                                                val userId = parsed.pubKeyBase64  // en MVP, pubKeyBase64 contiene userId real
                                                val displayName = parsed.displayName ?: "Usuario MALLA"
                                                val contact = ContactEntity(
                                                    contactUserId = userId,
                                                    displayName = displayName,
                                                    avatarSeed = userId.hashCode(),
                                                    publicKey = "",
                                                    addedAt = System.currentTimeMillis()
                                                )
                                                database?.contactDao()?.insert(contact)
                                                // Crear conversación con el userId real del QR, nunca con IP
                                                val conv = ConversationEntity(
                                                    id = userId,
                                                    title = displayName,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                database?.conversationDao()?.insertConversation(conv)
                                                withContext(Dispatchers.Main) {
                                                    currentConversationId = userId
                                                    Toast.makeText(this@MainActivity, "Contacto agregado por QR", Toast.LENGTH_SHORT).show()
                                                }
                                                // Opcional: intentar conexión TCP en segundo plano si IP existe
                                                val ip = parsed.localIp
                                                if (ip != null && ip.isNotBlank()) {
                                                    try { NetworkService.connectToPeer(ip) } catch (_: Exception) {}
                                                }
                                            } else {
                                                // Intentar compatibilidad con formato anterior userId|displayName|publicKey
                                                val parts = payload.split("|")
                                                if (parts.size >= 3) {
                                                    val userId = parts[0]
                                                    val displayName = parts[1]
                                                    val publicKey = parts[2]
                                                    val contact = ContactEntity(
                                                        contactUserId = userId,
                                                        displayName = displayName,
                                                        avatarSeed = publicKey.hashCode(),
                                                        publicKey = publicKey,
                                                        addedAt = System.currentTimeMillis()
                                                    )
                                                    database?.contactDao()?.insert(contact)
                                                    val conv = ConversationEntity(
                                                        id = userId,
                                                        title = displayName,
                                                        timestamp = System.currentTimeMillis()
                                                    )
                                                    database?.conversationDao()?.insertConversation(conv)
                                                    withContext(Dispatchers.Main) {
                                                        currentConversationId = userId
                                                        Toast.makeText(this@MainActivity, "Contacto agregado", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    connectToPeerAndCreateConversation(payload) { convId ->
                                                        currentConversationId = convId
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onBack = { showQrScanner = false }
                                )
                            } else if (showChatSettings) {
                                BackHandler { showChatSettings = false }
                                ChatSettingsScreen(onBack = { showChatSettings = false })
                            } else if (showSettings) {
                                BackHandler { showSettings = false }
                                SettingsScreenWrapper(
                                    currentScheme = effectiveScheme,
                                    onSchemeSelected = { scheme -> appThemeState.selectScheme(scheme) },
                                    onBack = { showSettings = false }
                                )
                            } else if (selectedContact != null) {
                                BackHandler { selectedContact = null }
                                ContactProfileScreen(
                                    contactName = selectedContact!!,
                                    onBack = { selectedContact = null }
                                )
                            } else {
                                MainApp(
                                    isMeshMode = !isOnline,
                                    currentConversationId = currentConversationId,
                                    onConversationChanged = { convId -> currentConversationId = convId },
                                    onSettingsClick = { showSettings = true },
                                    onChatSettingsClick = { showChatSettings = true },
                                    onProfileClicked = { contactName -> selectedContact = contactName },
                                    onNavigateToQrScanner = { showQrScanner = true },
                                    onConnectToPeer = { ip ->
                                        connectToPeerAndCreateConversation(ip) { convId ->
                                            currentConversationId = convId
                                        }
                                    },
                                    onVoiceCallClick = {
                                        showCall = true
                                        callContact = "Contacto"
                                        callType = "voice"
                                    },
                                    onVideoCallClick = {
                                        showCall = true
                                        callContact = "Contacto"
                                        callType = "video"
                                    },
                                    db = database
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enableRadio() {
        try {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (btAdapter != null && !btAdapter.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        btAdapter.enable()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    btAdapter.enable()
                }
            }
        } catch (e: Exception) {
            LogBuffer.add("MAIN", "No se pudo habilitar Bluetooth: ${e.message}")
        }

        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // En Android 10+ no se puede habilitar WiFi programáticamente; abrir panel rápido
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                } else {
                    @Suppress("DEPRECATION")
                    wifiManager.isWifiEnabled = true
                }
            }
        } catch (e: Exception) {
            LogBuffer.add("MAIN", "No se pudo habilitar WiFi: ${e.message}")
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(requiredPermissions.toTypedArray())
    }

    private fun insertSampleStories() {
        MainScope().launch {
            val db = AppDatabase.getInstance(application) ?: return@launch
            val storyDao = db.storyDao()
            storyDao.insertStory(
                com.malla.mvp.data.entity.StoryEntity(
                    id = "story1",
                    userId = "sim_alicia",
                    imageUri = "#FF5733",
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )
            storyDao.insertStory(
                com.malla.mvp.data.entity.StoryEntity(
                    id = "story2",
                    userId = "sim_carlos",
                    imageUri = "#33FF57",
                    timestamp = System.currentTimeMillis() - 7200000
                )
            )
        }
    }

    private fun connectToPeerAndCreateConversation(ip: String, onCreated: (String) -> Unit) {
        try {
            NetworkService.connectToPeer(ip)
        } catch (_: Exception) {}
        val db = AppDatabase.getInstance(application)
        val conversationId = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = conversationId,
            title = "Peer ${ip.take(8)}",
            timestamp = System.currentTimeMillis()
        )
        MainScope().launch {
            try {
                db?.conversationDao()?.insertConversation(conv)
                Toast.makeText(this@MainActivity, "Conectado a $ip", Toast.LENGTH_SHORT).show()
                onCreated(conversationId)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al crear conversación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return requiredPermissions.all { perm ->
            checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        RadioManager.restoreStates(this)
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenWrapper(
    currentScheme: MallaColorScheme,
    onSchemeSelected: (MallaColorScheme) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsScreen(
                currentScheme = currentScheme,
                onSchemeSelected = onSchemeSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    onVerifyClick: () -> Unit = {},
    onVoiceCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {},
    isMeshMode: Boolean,
    currentConversationId: String?,
    onConversationChanged: (String?) -> Unit,
    onSettingsClick: () -> Unit,
    onChatSettingsClick: () -> Unit = {},
    onProfileClicked: (String) -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onConnectToPeer: (String) -> Unit,
    db: AppDatabase?
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentContactName by remember { mutableStateOf("Chat") }
    var lastBackPressTime by remember { mutableStateOf(0L) }
    val backContext = LocalContext.current

    BackHandler {
        if (currentConversationId != null) {
            onConversationChanged(null)
            return@BackHandler
        }
        if (selectedTab != 0) {
            selectedTab = 0
            return@BackHandler
        }
        if (lastBackPressTime + 2000 > System.currentTimeMillis()) {
            (backContext as? android.app.Activity)?.finish()
        } else {
            Toast.makeText(backContext, "Presiona de nuevo para salir", Toast.LENGTH_SHORT).show()
        }
        lastBackPressTime = System.currentTimeMillis()
    }

    if (currentConversationId != null) {
        ChatScreen(
            conversationId = currentConversationId,
            contactName = currentContactName,
            onBack = { onConversationChanged(null) },
            isMeshMode = isMeshMode,
            onVoiceCallClick = onVoiceCallClick,
            onVideoCallClick = onVideoCallClick
        )
        return
    }
    val onProfileClick = { selectedTab = 2 }
    Scaffold(
        topBar = {
            MainTopBar(
                onSettingsClick = onSettingsClick,
                onChatSettingsClick = onChatSettingsClick,
                onProfileClick = onProfileClick,
                isOnline = !isMeshMode,
                showEncryption = currentConversationId != null
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(56.dp),
                containerColor = Color(0xFF0A1B2A)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, "Chats") },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CE6FF),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF4CE6FF),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.WifiTethering, "Pulso") },
                    label = { Text("Pulso") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CE6FF),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF4CE6FF),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Person, "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CE6FF),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF4CE6FF),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        ConnectivityStatusBar()
        Spacer(modifier = Modifier.height(1.dp))
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ConversationsScreen(
                    onChatClicked = { convId, name ->
                        currentContactName = name
                        onConversationChanged(convId)
                    },
                    onProfileClicked = onProfileClicked,
                    onNavigateToQrScanner = onNavigateToQrScanner
                )
                1 -> PulsoScreen(
                    onNavigateToQrScanner = onNavigateToQrScanner,
                    onConnectToPeer = onConnectToPeer
                )
                2 -> PerfilScreen(onVerifyClick = onVerifyClick)
            }
        }
    }
    StickerPickerDialog()
    StickerFullScreenDialog()
}

@Composable
fun PremiumNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            scale.animateTo(1.2f, animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f))
            scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f))
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp).clip(RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color(0xFF4CE6FF) else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CE6FF))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = if (selected) Color(0xFF4CE6FF) else Color.White.copy(alpha = 0.4f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionExplanationScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val permissionList = listOf(
        "Ubicación y Bluetooth" to "Para descubrir dispositivos cercanos y crear la red mesh.",
        "Cámara y micrófono" to "Para llamadas de voz/video y escanear códigos QR.",
        "Contactos y notificaciones" to "Para mostrar notificaciones y facilitar la comunicación.",
        "Almacenamiento" to "Para enviar y recibir imágenes y archivos."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bienvenido a MALLA") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Permisos necesarios",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "MALLA necesita los siguientes permisos para funcionar correctamente. " +
                "Tus datos están cifrados y nunca se comparten con terceros.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            permissionList.forEach { (title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar")
            }
        }
    }
}