package com.malla.mvp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegistrationScreen(onRegistrationComplete: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(0) } // 0=inicio, 1=procesando, 2=apodo
    var generatedId by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var processingPhase by remember { mutableIntStateOf(0) } // 0=ubicación, 1=generando
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager?

    // Animaciones de los iconos
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val locationScale by pulseAnim.animateFloat(1f, 1.3f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "loc")
    val fingerprintScale by pulseAnim.animateFloat(1f, 1.2f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "fp")

    fun performGeneration() {
        if (isProcessing) return
        isProcessing = true
        step = 1 // pasar a modo procesamiento visible
        processingPhase = 0
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Fase 1: obtener ubicación
                    var location: android.location.Location? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                    currentLocation = location
                    // Pausa para que se vea la animación de ubicación
                    delay(1000)

                    // Fase 2: generar ID
                    withContext(Dispatchers.Main) { processingPhase = 1 }
                    val id = IdentityManager.generateUniqueId(location)
                    IdentityManager.saveUserId(context, id)
                    generatedId = id

                    // Pausa para que se vea la animación de generación
                    delay(1200)

                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        step = 2 // ir a apodo
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        step = 0
                    }
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) performGeneration()
    }

    val biometricExecutor = remember { ContextCompat.getMainExecutor(context) }
    val biometricPrompt = remember {
        BiometricPrompt(
            context as FragmentActivity,
            biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { performGeneration() }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { performGeneration() }
                override fun onAuthenticationFailed() { performGeneration() }
            }
        )
    }

    val onGenerateClick: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verificación biométrica")
                    .setSubtitle("Confirma tu identidad para mayor seguridad")
                    .setNegativeButtonText("Omitir")
                    .build()
                biometricPrompt.authenticate(promptInfo)
            } else {
                performGeneration()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A1118)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Título
                Icon(
                    if (step == 1 && processingPhase == 0) Icons.Default.LocationOn else Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).scale(
                        if (step == 1 && processingPhase == 0) locationScale
                        else if (step == 1 && processingPhase == 1) fingerprintScale
                        else 1f
                    ),
                    tint = Color(0xFF4CE6FF)
                )
                Text(
                    when {
                        step == 0 -> "Creando tu identidad Malla"
                        step == 1 && processingPhase == 0 -> "Obteniendo ubicación..."
                        step == 1 && processingPhase == 1 -> "Generando tu identidad única..."
                        step == 2 -> "Elige un apodo"
                        else -> ""
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Contenido según el paso
                when (step) {
                    0 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A3A))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Para garantizar tu privacidad, generamos un ID único basado en tu dispositivo y ubicación. Opcionalmente puedes usar tu huella para mayor seguridad.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onGenerateClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
                                ) {
                                    Text("Generar mi ID", color = Color.Black)
                                }
                            }
                        }
                    }
                    1 -> {
                        // Pantalla de procesamiento
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A3A))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                // Icono animado ya está arriba (fuera del Card), aquí podemos poner detalles adicionales
                                if (currentLocation != null && processingPhase == 1) {
                                    Text(
                                        "📍 ${currentLocation!!.latitude}, ${currentLocation!!.longitude}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFB0BEC5)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                CircularProgressIndicator(color = Color(0xFF4CE6FF))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (processingPhase == 0) "Usando tu ubicación para mejorar la seguridad" else "Creando claves criptográficas...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    2 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A3A))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Tu ID es:", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                                Text(generatedId, style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CE6FF), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = nickname,
                                    onValueChange = { nickname = it },
                                    label = { Text("Apodo (opcional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF4CE6FF),
                                        unfocusedBorderColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (nickname.isNotBlank()) IdentityManager.setUserNickname(context, nickname)
                                            onRegistrationComplete(generatedId)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
                                ) { Text("Continuar", color = Color.Black) }
                            }
                        }
                    }
                }
            }
        }
    }
}
