package com.malla.mvp.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.malla.mvp.camera.contract.CameraContract
import com.malla.mvp.camera.ui.CameraScreen

class CameraActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) recreate() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val mode = intent.getStringExtra(CameraContract.EXTRA_MODE) ?: "photo"
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        CameraScreen(
                            mode = mode,
                            lifecycleOwner = this@CameraActivity,
                            onImageCaptured = { uri ->
                                val resultIntent = Intent().apply {
                                    putExtra(CameraContract.EXTRA_RESULT_URI, uri.toString())
                                }
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}
