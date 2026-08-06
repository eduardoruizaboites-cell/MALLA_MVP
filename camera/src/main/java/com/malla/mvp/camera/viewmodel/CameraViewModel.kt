package com.malla.mvp.camera.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class CameraMode(val label: String, val icon: String) {
    PHOTO("Foto", "📷"),
    NIGHT("Noche", "🌙"),
    PANORAMA("Panorámica", "🌄"),
    DOCUMENT("Documento", "📄"),
    PORTRAIT("Retrato", "🧑"),
    PRO("Pro", "⚙️")
}

class CameraViewModel(
    private val context: Context,
    initialMode: String
) : ViewModel() {

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var previewView: PreviewView? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null

    private lateinit var cameraExecutor: ExecutorService

    private val _currentMode = MutableStateFlow(CameraMode.PHOTO)
    val currentMode: StateFlow<CameraMode> = _currentMode

    private val _showModes = MutableStateFlow(false)
    val showModes: StateFlow<Boolean> = _showModes

    init {
        _currentMode.value = when (initialMode) {
            "night" -> CameraMode.NIGHT
            "panorama" -> CameraMode.PANORAMA
            "document" -> CameraMode.DOCUMENT
            "portrait" -> CameraMode.PORTRAIT
            "pro" -> CameraMode.PRO
            else -> CameraMode.PHOTO
        }
    }

    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        this.previewView = previewView
        this.lifecycleOwner = lifecycleOwner
        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al iniciar cámara: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        val pv = previewView ?: return
        val owner = lifecycleOwner ?: return

        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(pv.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                owner,
                CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Error al vincular cámara: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun capturePhoto(onImageCaptured: (Uri) -> Unit) {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            context.externalMediaDirs.firstOrNull()?.let { dir ->
                File(dir, "MALLA").also { it.mkdirs() }
            } ?: context.filesDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Error al capturar: ${exception.message}", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
            }
        )
    }

    fun toggleFlash() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        bindCameraUseCases()
    }

    fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases()
    }

    fun setMode(mode: CameraMode) {
        _currentMode.value = mode
    }

    fun toggleModePanel() {
        _showModes.value = !_showModes.value
    }

    override fun onCleared() {
        super.onCleared()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    class Factory(
        private val context: Context,
        private val mode: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                return CameraViewModel(context, mode) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
