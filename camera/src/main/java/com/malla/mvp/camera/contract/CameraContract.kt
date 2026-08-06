package com.malla.mvp.camera.contract

import android.content.Context
import android.content.Intent
import com.malla.mvp.camera.CameraActivity

object CameraContract {
    const val EXTRA_MODE = "camera_mode"
    const val EXTRA_RESULT_URI = "result_uri"
    
    fun createIntent(context: Context, mode: String = "photo"): Intent {
        return Intent(context, CameraActivity::class.java).apply {
            putExtra(EXTRA_MODE, mode)
        }
    }
}
