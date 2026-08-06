package com.malla.mvp.camera.ui

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malla.mvp.camera.viewmodel.CameraMode
import com.malla.mvp.camera.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
    mode: String,
    lifecycleOwner: LifecycleOwner,
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val vm: CameraViewModel = viewModel(
        factory = CameraViewModel.Factory(context, mode)
    )
    val currentMode by vm.currentMode.collectAsState()
    val showModes by vm.showModes.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    vm.startCamera(previewView, lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color(0x80000000), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.toggleFlash() }) {
                Icon(Icons.Default.FlashOn, "Flash", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0x40000000)) {
                Text(
                    "${currentMode.icon} ${currentMode.label}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            IconButton(onClick = { vm.toggleCamera() }) {
                Icon(Icons.Default.FlipCameraAndroid, "Cambiar cámara", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        // Selector de modos
        AnimatedVisibility(
            visible = showModes,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCC1A1A1A)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Modos de cámara",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(CameraMode.values().toList()) { modeItem ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (modeItem == currentMode) Color(0xFF4CE6FF).copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .clickable { vm.setMode(modeItem) }
                                    .padding(12.dp)
                            ) {
                                Text(modeItem.icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    modeItem.label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (modeItem == currentMode) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Barra inferior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .clickable { vm.toggleModePanel() },
                contentAlignment = Alignment.Center
            ) {
                Text("⋮", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { vm.capturePhoto { uri -> onImageCaptured(uri) } },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { vm.capturePhoto { uri -> onImageCaptured(uri) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, "Capturar", tint = Color.Black, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}
