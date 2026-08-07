package com.malla.mvp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegistrationScreen(onRegistrationComplete: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(0) } // 0 = Generar ID, 1 = Apodo
    var generatedId by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager?

    // ── Función que realmente genera el ID ─────────────────────
    fun performGeneration() {
        if (isProcessing) return
        isProcessing = true
        statusText = "Obteniendo ubicación..."
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    var location: android.location.Location? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                    withContext(Dispatchers.Main) { statusText = "Generando tu identidad única..." }
                    val id = IdentityManager.generateUniqueId(location)
                    IdentityManager.saveUserId(context, id)
                    withContext(Dispatchers.Main) {
                        generatedId = id
                        isProcessing = false
                        statusText = ""
                        step = 1 // pasar al apodo
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusText = "Error: ${e.message}"
                        isProcessing = false
                    }
                }
            }
        }
    }

    // ── Solicitar permisos de ubicación ────────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            performGeneration()
        } else {
            statusText = "Se necesita permiso de ubicación para generar tu ID único."
        }
    }

    // ── Biometría opcional ─────────────────────────────────────
    val biometricExecutor = remember { ContextCompat.getMainExecutor(context) }
    val biometricPrompt = remember {
        BiometricPrompt(
            context as FragmentActivity,
            biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    performGeneration()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    performGeneration()
                }
                override fun onAuthenticationFailed() {
                    performGeneration()
                }
            }
        )
    }

    // ── Acción del botón "Generar mi ID" ───────────────────────
    val onGenerateClick: () -> Unit = {
        // Verificar permisos de ubicación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            // Intentar biometría si está disponible
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

    // ── Interfaz principal ─────────────────────────────────
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A1118)  // fondo oscuro premium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo / título
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CE6FF)
                )
                Text(
                    if (step == 0) "Creando tu identidad Malla" else "Elige un apodo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Paso 0: Generar ID
                AnimatedVisibility(visible = step == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A3A))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Para garantizar tu privacidad, generamos un ID único basado en tu dispositivo y ubicación. Opcionalmente puedes usar tu huella para mayor seguridad.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color(0xFF4CE6FF))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(statusText, color = Color.White)
                            } else {
                                Button(
                                    onClick = onGenerateClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
                                ) {
                                    Text("Generar mi ID", color = Color.Black)
                                }
                                if (statusText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(statusText, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Paso 1: Elegir apodo
                AnimatedVisibility(visible = step == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A3A))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "Tu ID es:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                generatedId,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF4CE6FF),
                                fontWeight = FontWeight.Bold
                            )
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
                                        if (nickname.isNotBlank()) {
                                            IdentityManager.setUserNickname(context, nickname)
                                        }
                                        onRegistrationComplete(generatedId)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
                            ) {
                                Text("Continuar", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
