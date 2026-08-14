package com.malla.mvp.ui.screen

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.location.LocationManager
import android.location.Location
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.alpha
import com.malla.mvp.ui.settings.ChatSettings
import androidx.compose.ui.graphics.Brush
import com.malla.mvp.ui.components.BubbleShapes
import com.malla.mvp.ui.settings.AccessibilitySettings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.malla.mvp.core.data.MessageData
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.ui.components.GalleryPickerPanel
import com.malla.mvp.ui.components.ComposingBubble
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.network.ConnectivityMonitor
import com.malla.mvp.ui.components.ChatInputBar
import com.malla.mvp.viewmodel.MeshChatViewModel
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.gestures.awaitFirstDown
import com.malla.mvp.ui.components.AudioBubblePlayer
import com.malla.mvp.media.VoiceRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.malla.mvp.R
import android.media.MediaPlayer
import com.malla.mvp.network.NetworkService
import androidx.compose.foundation.border
import com.malla.mvp.ui.theme.LocalColorScheme
import com.malla.mvp.ui.theme.MallaColorScheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.malla.mvp.camera.contract.CameraContract
import android.app.Activity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import com.malla.mvp.ui.settings.BubbleStyle
import com.malla.mvp.ui.settings.ConversationPreferences
import com.malla.mvp.ui.settings.ConversationPrefs
import androidx.compose.ui.graphics.toArgb
import com.malla.mvp.ui.components.ChatCustomizationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    contactName: String,
    isMeshMode: Boolean = false,
    onBack: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
    onVoiceCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: MeshChatViewModel = viewModel()
    val prefsRevision by ConversationPreferences.changes.collectAsState()
    val chatPrefs = remember(conversationId, prefsRevision) { ConversationPreferences.load(context, conversationId) }
    val messages by vm.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showGalleryPanel by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentPanel by remember { mutableStateOf(false) }
    var zumbidoCooldown by remember { mutableStateOf(false) }
    val pendingMediaUris = remember { mutableStateListOf<Uri>() }
    var captionText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(300)
        keyboardController?.show()
    }
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = LocalColorScheme.current
    val shakeOffset = remember { Animatable(0f) }
    var fullScreenImageUri by remember { mutableStateOf<Uri?>(null) }
    var showZumbidoOverlay by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showChatSettings by remember { mutableStateOf(false) }
        var elapsedSeconds by remember { mutableIntStateOf(0) }
    val voiceRecorder = remember { VoiceRecorder(context) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uriString = result.data?.getStringExtra(CameraContract.EXTRA_RESULT_URI)
            if (uriString != null) {
                val uri = Uri.parse(uriString)
                pendingMediaUris.add(uri)
            }
        }
    }

    var typingText by remember { mutableStateOf("") }

    LaunchedEffect(conversationId) {
        vm.loadConversation(conversationId)
    }

    LaunchedEffect(Unit) {
        NetworkService.messages.collect { msg ->
            if (msg.type == "zumbido") {
                MallaEventBus.zumbidoReceived.tryEmit(msg)
            }
        }
    }

    // Receptor de zumbido
    LaunchedEffect(Unit) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        MallaEventBus.zumbidoReceived.collect { msg ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 80, 100, 80, 100),
                    intArrayOf(0, 255, 0, 255, 0, 255),
                    -1
                ))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 100, 80, 100, 80, 100), -1)
            }
            try {
                val mp = MediaPlayer.create(context, R.raw.zumbido)
                mp?.start()
                mp?.setOnCompletionListener { mp2 -> mp2.release() }
            } catch (_: Exception) { }
            showZumbidoOverlay = true
            repeat(4) {
                shakeOffset.animateTo(20f, animationSpec = tween(60))
                shakeOffset.animateTo(-20f, animationSpec = tween(60))
            }
            shakeOffset.animateTo(0f, animationSpec = tween(80))
        }
    }

    LaunchedEffect(zumbidoCooldown) {
        if (zumbidoCooldown) {
            delay(5000)
            zumbidoCooldown = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeOffset.value }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(contactName, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            val isOnline = ConnectivityMonitor.isOnline.collectAsState().value
                            Canvas(modifier = Modifier.size(12.dp)) {
                                drawCircle(
                                    color = if (isOnline) Color(0xFF2ECC71) else Color(0xFFF1C40F),
                                    radius = size.minDimension / 2
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = Color.White)
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showChatMenu = true }) {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = contactName.take(1).uppercase(),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showChatMenu,
                                onDismissRequest = { showChatMenu = false },
                                modifier = Modifier.background(Color(0xFF15202B), RoundedCornerShape(8.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ver perfil", color = Color.White) },
                                    onClick = {
                                        showChatMenu = false
                                        onProfileClicked()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Personalizar chat", color = Color.White) },
                                    onClick = {
                                        showChatMenu = false
                                        showChatSettings = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1B2A))
                )
            },
            containerColor = Color(0xFF0A1118)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Lista de mensajes
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState
                ) {
                    items(messages) { msg ->
                        MessageBubbleV2(
                                msg = msg,
                                animate = vm.isMessageNew(msg.timestamp),
                                onImageClick = { uri -> fullScreenImageUri = uri },
                                ownBubbleColorParam = chatPrefs.ownBubbleColor?.let { Color(it) },
                                otherBubbleColorParam = chatPrefs.otherBubbleColor?.let { Color(it) },
                                fontSizeParam = chatPrefs.fontSize,
                                bubbleOpacityParam = chatPrefs.bubbleOpacity,
                                ownTextColorParam = chatPrefs.ownTextColor?.let { Color(it) },
                                otherTextColorParam = chatPrefs.otherTextColor?.let { Color(it) }
                            )
                    }
                }

                // Auto-scroll al último mensaje
                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }

                // Barra inferior: cambia entre vista previa y composición normal
                                AnimatedVisibility(
                    visible = pendingMediaUris.isNotEmpty(),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(200)),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(150))
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { shadowElevation = 12.dp.toPx() }
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        color = Color(0xFF141E28),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Vista previa",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    "${pendingMediaUris.size} adjunto(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CE6FF)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(pendingMediaUris) { uri ->
                                    val scale = remember { Animatable(0.9f) }
                                    LaunchedEffect(uri) { scale.animateTo(1f, tween(200)) }
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .graphicsLayer {
                                                scaleX = scale.value
                                                scaleY = scale.value
                                            }
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Miniatura",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .clickable { pendingMediaUris.remove(uri) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Eliminar",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                item {
                                    IconButton(
                                        onClick = { showGalleryPanel = true },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(0xFF4CE6FF).copy(alpha = 0.2f),
                                                        Color(0xFF6C63FF).copy(alpha = 0.2f)
                                                    )
                                                )
                                            )
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            "Agregar más",
                                            tint = Color(0xFF4CE6FF),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { showEmojiPicker = !showEmojiPicker }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.InsertEmoticon, "Emoji", tint = Color(0xFF4CE6FF), modifier = Modifier.size(20.dp))
                                        }
                                        BasicTextField(
                                            value = captionText,
                                            onValueChange = { captionText = it },
                                            modifier = Modifier.weight(1f),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                            maxLines = 2,
                                            cursorBrush = SolidColor(Color(0xFF4CE6FF)),
                                            decorationBox = { innerTextField ->
                                                Box {
                                                    if (captionText.isEmpty()) {
                                                        Text("Añade un pie de foto...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF))
                                        .clickable {
                                            coroutineScope.launch {
                                                pendingMediaUris.forEachIndexed { index, uri ->
                                                    val textToSend = if (index == 0 && captionText.isNotBlank()) captionText else ""
                                                    vm.sendMessage(if (textToSend == "Imagen") "" else textToSend, mediaUri = uri.toString())
                                                }
                                                pendingMediaUris.clear()
                                                captionText = ""
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        "Enviar",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (pendingMediaUris.isEmpty()) {
                    AnimatedVisibility(
                        visible = typingText.isNotEmpty(),
                        enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)) + fadeIn(tween(200)),
                        exit = scaleOut(tween(150)) + fadeOut(tween(150))
                    ) {
                        val avatarBitmap = IdentityManager.avatarBitmap.collectAsState().value
                        ComposingBubble(
                            isOwn = false,
                            avatarBitmap = avatarBitmap,
                            userName = contactName.take(1).uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    ChatInputBar(
                        voiceRecorder = voiceRecorder,
                        onSendText = { msg -> vm.sendMessage(msg); typingText = "" },
                        onSendVoice = { file -> vm.sendMessage("", mediaUri = file.absolutePath); typingText = "" },
                        onSendZumbido = { vm.sendZumbido() },
                        onCameraClick = {
                            val intent = CameraContract.createIntent(context, "photo")
                            cameraLauncher.launch(intent)
                        },
                        onAttachmentClick = { showAttachmentPanel = true },
                        onTextChanged = { newText -> typingText = newText }
                    )
                }

                // Panel de emojis
                if (showEmojiPicker) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38))
                    ) {
                        val emojis = listOf("😀","😂","😍","😢","😡","👍","👋","🎉","❤️","🔥","😎","🙏","💪","🤔","😴","🥳")
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                                emojis.take(8).forEach { emoji ->
                                    Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(4.dp).clickable {
                                        text = text + emoji; showEmojiPicker = false
                                    })
                                }
                            }
                            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                                emojis.drop(8).forEach { emoji ->
                                    Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(4.dp).clickable {
                                        text = text + emoji; showEmojiPicker = false
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChatSettings) {
        ChatCustomizationDialog(
            conversationId = conversationId,
            onDismiss = { showChatSettings = false }
        )
    }

    if (showChatSettings) {
        ChatCustomizationDialog(
            conversationId = conversationId,
            onDismiss = { showChatSettings = false }
        )
    }

    if (showZumbidoOverlay) {
        ZumbidoOverlay(onDismiss = { showZumbidoOverlay = false }, colorScheme = colorScheme)
    }


    // ── Panel de adjuntos premium ─────────────────────────────────
    if (showAttachmentPanel) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentPanel = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                Text("Adjuntar archivo", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        AttachmentOptionPremium(icon = Icons.Default.Photo, label = "Galería", color = Color(0xFF4CE6FF), onClick = { showAttachmentPanel = false; showGalleryPanel = true })
                        Spacer(modifier = Modifier.height(16.dp))
                        AttachmentOptionPremium(icon = Icons.Default.InsertDriveFile, label = "Documento", color = Color(0xFF6C63FF), onClick = { /* TODO */ })
                    }
                    Column(modifier = Modifier.weight(1f)) {

                        AttachmentOptionPremium(icon = Icons.Default.LocationOn, label = "Ubicación", color = Color(0xFF4CAF50), onClick = {
                            showAttachmentPanel = false
                            val loc = getBestLocation(context)
                            if (loc != null) {
                                val lat = loc.latitude; val lon = loc.longitude
                                vm.sendMessage("📍 Ubicación actual\nhttps://maps.google.com/maps?q=$lat,$lon")
                            } else {
                                vm.sendMessage("📍 No se pudo obtener la ubicación. Concede permisos.")
                            }
                        })
                    }
                }
            }
        }
    }

    // Paneles externos (no se mueven con el shake)
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enviar multimedia", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { showAttachmentSheet = false; showGalleryPanel = true }) {
                            Icon(Icons.Filled.Photo, "Galería", tint = Color(0xFF4CE6FF))
                        }
                        Text("Galería", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /* TODO: documento */ }) {
                            Icon(Icons.Filled.InsertDriveFile, "Documento", tint = Color(0xFF4CE6FF))
                        }
                        Text("Documento", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            showAttachmentSheet = false
                            val loc = getBestLocation(context)
                            if (loc != null) {
                                val lat = loc.latitude; val lon = loc.longitude
                                vm.sendMessage("📍 Ubicación actual\nhttps://maps.google.com/maps?q=$lat,$lon")
                            } else {
                                vm.sendMessage("📍 Ubicación no disponible. Concede permisos de ubicación.")
                            }
                        }) {
                            Icon(Icons.Filled.LocationOn, "Ubicación", tint = Color(0xFF4CE6FF))
                        }
                        Text("Ubicación", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /* TODO: contacto */ }) {
                            Icon(Icons.Filled.PersonAdd, "Contacto", tint = Color(0xFF4CE6FF))
                        }
                        Text("Contacto", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /* TODO: encuesta */ }) {
                            Icon(Icons.Filled.Poll, "Encuesta", tint = Color(0xFF4CE6FF))
                        }
                        Text("Encuesta", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
    }

    if (showGalleryPanel) {
        GalleryPickerPanel(
            onDismiss = { showGalleryPanel = false },
            onConfirm = { uris ->
                pendingMediaUris.clear()
                pendingMediaUris.addAll(uris)
                showGalleryPanel = false
            },
            initialSelected = pendingMediaUris.toList()
        )
    }
    // Diálogo de imagen a pantalla completa con zoom
    if (fullScreenImageUri != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val scale = remember { Animatable(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val scope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullScreenImageUri = null }  // Cerrar al tocar fondo
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale.value * zoom).coerceIn(0.5f, 5f)
                            scope.launch { scale.snapTo(newScale) }
                            offset = offset + pan
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scope.launch {
                                    if (scale.value > 1f) {
                                        scale.animateTo(1f, tween(200))
                                        offset = Offset.Zero
                                    } else {
                                        scale.animateTo(2f, tween(200))
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullScreenImageUri!!,
                    contentDescription = "Imagen ampliada",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

}

@Composable
fun MessageBubbleV2(
    msg: MessageData,
    animate: Boolean = false,
    onImageClick: (Uri) -> Unit = {},
    ownBubbleColorParam: Color? = null,
    otherBubbleColorParam: Color? = null,
    fontSizeParam: Float? = null,
    bubbleOpacityParam: Float? = null,
    ownTextColorParam: Color? = null,
    otherTextColorParam: Color? = null
) {
    val isOwn = msg.isOwn

    val ownBubbleColor = ownBubbleColorParam ?: AccessibilitySettings.ownBubbleColor.collectAsState().value
    val otherBubbleColor = otherBubbleColorParam ?: AccessibilitySettings.otherBubbleColor.collectAsState().value
    val baseColor = (if (isOwn) ownBubbleColor else otherBubbleColor) ?: if (isOwn) Color(0xFF1A3B4A) else Color(0xFF2A2A2A)
    val ownTextColor = ownTextColorParam ?: ChatSettings.ownTextColor.collectAsState().value
    val otherTextColor = otherTextColorParam ?: ChatSettings.otherTextColor.collectAsState().value
    val textColor = (if (isOwn) ownTextColor else otherTextColor) ?: contrastingTextColor(baseColor)
    val bubbleOpacity = bubbleOpacityParam ?: ChatSettings.bubbleOpacity.collectAsState().value
    val fontSize = fontSizeParam ?: ChatSettings.fontSize.collectAsState().value
    val onlyEmojis = msg.mediaUri == null && msg.content.isOnlyEmojis() && msg.content.codePointCount(0, msg.content.length) <= 4

    val scale = remember { Animatable(1f) }
    val slideY = remember { Animatable(0f) }
    LaunchedEffect(msg.id, animate) {
        if (animate) {
            scale.snapTo(0.5f)
            slideY.snapTo(80f)
            slideY.animateTo(0f, tween(300))
            scale.animateTo(1.15f, spring(dampingRatio = 0.25f, stiffness = 800f))
            scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 900f))
        } else {
            scale.snapTo(1f)
            slideY.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (onlyEmojis) {
            Text(
                text = msg.content,
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationY = slideY.value
                }
            )
        } else {
            val shape = RoundedCornerShape(16.dp)
            Surface(
                shape = shape,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .widthIn(min = 100.dp, max = 280.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationY = slideY.value
                        transformOrigin = if (isOwn) TransformOrigin(1f, 1f) else TransformOrigin(0f, 1f)
                    }
                    .alpha(bubbleOpacity),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(listOf(baseColor.lighten(0.15f), baseColor)),
                            shape = shape
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    BubbleContent(msg, textColor, fontSize, onImageClick)
                }
            }
        }
    }
}

@Composable
private fun BubbleContent(
    msg: MessageData,
    textColor: Color,
    fontSize: Float,
    onImageClick: (Uri) -> Unit
) {
    Column {
        val media = msg.mediaUri
        if (media != null) {
            val uri = Uri.parse(media)
            if (media.endsWith(".3gp") || media.endsWith(".m4a") || media.contains("voice_")) {
                AudioBubblePlayer(filePath = media, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(uri) }
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen enviada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        if (msg.content.isNotBlank() && msg.content != "Imagen") {
            Text(text = msg.content, color = textColor, fontSize = fontSize.sp)
        }
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
            color = textColor.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

fun Color.lighten(factor: Float = 0.1f): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun String.isOnlyEmojis(): Boolean {
    if (this.isBlank()) return false
    val cleaned = this.replace(Regex("[\\s\\u200D\\uFE0F]"), "")
    if (cleaned.isEmpty()) return false
    var i = 0
    while (i < cleaned.length) {
        val cp = cleaned.codePointAt(i)
        val isEmoji = (cp in 0x1F600..0x1F64F) || (cp in 0x1F300..0x1F5FF) || (cp in 0x1F680..0x1F6FF) ||
                      (cp in 0x2600..0x26FF) || (cp in 0x2700..0x27BF) || (cp in 0x1F900..0x1F9FF) ||
                      (cp in 0x1FA00..0x1FA6F) || (cp in 0x1FA70..0x1FAFF)
        if (!isEmoji) return false
        i += Character.charCount(cp)
    }
    return true
}

fun getBestLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return null
    }
    var best: Location? = null
    try { best = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) {}
    if (best == null) try { best = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) {}
    if (best == null) try { best = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) {}
    return best
}


fun formatSeconds(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}

@Composable
fun ZumbidoOverlay(onDismiss: () -> Unit, colorScheme: MallaColorScheme) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(300))
        delay(1200)
        alpha.animateTo(0f, tween(600))
        onDismiss()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .background(colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📳", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Zumbido", color = colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun AttachmentOptionPremium(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onClick() }.scale(scale.value),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring()) }
}