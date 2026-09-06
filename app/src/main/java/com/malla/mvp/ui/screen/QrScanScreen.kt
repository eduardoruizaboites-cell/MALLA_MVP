package com.malla.mvp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.malla.mvp.core.engine.DiagnosticsLogger
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun QrScanScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            DiagnosticsLogger.log("QrScan", "Resultado permiso cámara: $granted")
            if (!granted) {
                // Mostrar mensaje o volver atrás
            }
        }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasPermission = granted
        if (!granted) {
            DiagnosticsLogger.log("QrScan", "Solicitando permiso de cámara")
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            DiagnosticsLogger.log("QrScan", "Permiso de cámara ya concedido")
        }
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso de cámara")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack) {
            Text("Cancelar")
        }
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            DiagnosticsLogger.log("QrScan", "CameraProvider obtenido")
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val mediaImage = imageProxy.image ?: run { imageProxy.close(); return@setAnalyzer }
                                val buffer = mediaImage.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                val source = PlanarYUVLuminanceSource(
                                    bytes,
                                    mediaImage.width,
                                    mediaImage.height,
                                    0, 0,
                                    mediaImage.width,
                                    mediaImage.height,
                                    false
                                )
                                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                                val reader = QRCodeReader()
                                try {
                                    val result = reader.decode(binaryBitmap)
                                    if (result != null) {
                                        imageProxy.close()
                                        scope.launch { onQrScanned(result.text) }
                                        return@setAnalyzer
                                    }
                                } catch (e: NotFoundException) {
                                    // No QR found
                                } catch (e: Exception) {
                                    DiagnosticsLogger.log("QrScan", "Error decodificando QR: ${e.message}")
                                }
                                imageProxy.close()
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            DiagnosticsLogger.log("QrScan", "Cámara vinculada al lifecycle")
                        } catch (e: Exception) {
                            DiagnosticsLogger.log("QrScan", "Error inicializando cámara: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
