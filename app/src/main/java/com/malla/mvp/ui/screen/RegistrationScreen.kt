package com.malla.mvp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.util.BiometricAuthHelper
import kotlinx.coroutines.delay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed

private enum class RegStep {
    PERMISOS,
    BIOMETRIA_EXPLANATION,
    BIOMETRIA,
    ANIMACION_ID,
    NOMBRE,
    CONFIRMACION
}

@Composable
fun RegistrationScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(RegStep.PERMISOS) }
    var username by remember { mutableStateOf("") }
    val myId = remember { IdentityManager.getIdentityId() }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            (slideInHorizontally { width -> width } + fadeIn(tween(400))) togetherWith
                (slideOutHorizontally { width -> -width } + fadeOut(tween(400)))
        },
        label = "reg_step"
    ) { step ->
        when (step) {
            RegStep.PERMISOS -> PermissionsExplanation(onContinue = { currentStep = RegStep.BIOMETRIA_EXPLANATION })
            RegStep.BIOMETRIA_EXPLANATION -> BiometricExplanation(onContinue = { currentStep = RegStep.BIOMETRIA })
            RegStep.BIOMETRIA -> BiometricStep(onSuccess = { currentStep = RegStep.ANIMACION_ID }, onError = {})
            RegStep.ANIMACION_ID -> IdAnimationStep(onFinished = { currentStep = RegStep.NOMBRE })
            RegStep.NOMBRE -> NameStep(
                username = username,
                myId = myId,
                onContinue = { name ->
                    username = name
                    IdentityManager.setUserName(context, name)
                    currentStep = RegStep.CONFIRMACION
                }
            )
            RegStep.CONFIRMACION -> ConfirmationStep(
                username = username,
                myId = myId,
                onComplete = {
                    IdentityManager.setRegistrationComplete(context, true)
                    onComplete()
                }
            )
        }
    }
}

@Composable
fun PermissionsExplanation(onContinue: () -> Unit) {
    val context = LocalContext.current
    val essentialPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // No verificamos aquí, lo hará el LaunchedEffect
    }

    // Verificar continuamente si los permisos ya fueron concedidos
    LaunchedEffect(Unit) {
        while (true) {
            val allGranted = essentialPermissions.all {
                androidx.core.content.ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                onContinue()
                break
            }
            delay(500)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117)), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22).copy(alpha = 0.9f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Permisos necesarios", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFE6EDF3), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Para proteger tu identidad y conectar con otros de forma segura, MALLA necesita acceso a:\n\n" +
                    "• Ubicación: para generar tu ID único basado en tu posición.\n" +
                    "• Cámara y micrófono: para enviar fotos y notas de voz.\n\n" +
                    "Puedes gestionar estos permisos más tarde en Ajustes.",
                    color = Color(0xFF8B949E),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { launcher.launch(essentialPermissions) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar", color = Color(0xFF0D1117), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BiometricExplanation(onContinue: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117)), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22).copy(alpha = 0.9f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔐", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Verificación biométrica", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFE6EDF3), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Usaremos tu huella o rostro para:\n\n" +
                    "• Crear tu identidad única y enlazarla a tu dispositivo.\n" +
                    "• Asegurar que solo tú puedes leer tus mensajes.\n" +
                    "• Evitar suplantaciones de identidad.\n\n" +
                    "Tus datos biométricos nunca salen de tu teléfono.",
                    color = Color(0xFF8B949E),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar", color = Color(0xFF0D1117), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BiometricStep(onSuccess: () -> Unit, onError: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        BiometricAuthHelper.authenticate(
            context,
            onSuccess = onSuccess,
            onError = { onError() }
        )
    }
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF00E5FF))
    }
}

@Composable
fun IdAnimationStep(onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "idGen")
    val ringProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "ring"
    )
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "icons"
    )

    LaunchedEffect(Unit) {
        delay(3000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentAlignment = Alignment.Center
    ) {
        // Anillos expansivos
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = size.minDimension * 0.4f
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.1f * (1 - ringProgress)),
                radius = maxRadius * ringProgress,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.05f * (1 - ringProgress)),
                radius = maxRadius * ringProgress * 0.7f,
                center = Offset(centerX, centerY)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Iconos profesionales con animación de opacidad
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.alpha(iconAlpha)
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = "Huella",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(48.dp)
                )
                Icon(
                    Icons.Default.GpsFixed,
                    contentDescription = "GPS",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = Color(0xFF00E5FF),
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "CREANDO IDENTIDAD SEGURA",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "huella digital + ubicación GPS",
                color = Color(0xFF8B949E),
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun NameStep(username: String, myId: String, onContinue: (String) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(username) }
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D1117), Color(0xFF161B22)))), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22).copy(alpha = 0.8f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tu ID único", color = Color(0xFF8B949E))
                Text(myId, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Elige un nombre de usuario *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF30363D)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "El nombre de usuario es obligatorio", Toast.LENGTH_SHORT).show()
                        } else {
                            onContinue(name.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar", color = Color(0xFF0D1117), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConfirmationStep(username: String, myId: String, onComplete: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D1117), Color(0xFF161B22)))), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22).copy(alpha = 0.8f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Así te verán en la red", color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(8.dp))
                Text(username, color = Color(0xFFE6EDF3), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(myId, color = Color(0xFF00E5FF), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Tu identidad es única y está protegida con biometría. Nadie puede suplantarte. Comparte tu ID de forma segura para conectarte.",
                    color = Color(0xFF8B949E),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Empezar", color = Color(0xFF0D1117), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
