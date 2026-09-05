package com.malla.mvp.core.engine

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticsLogger {
    private const val FILENAME = "malla_diagnostics.txt"
    private var logFile: File? = null

    fun init(context: Context) {
        if (logFile == null) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dir = if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                downloadsDir
            } else {
                context.getExternalFilesDir(null) ?: context.filesDir
            }
            logFile = File(dir, FILENAME)
        }
    }

    fun log(tag: String, message: String) {
        val file = logFile ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val line = "[$timestamp] [$tag] $message\n"
        try {
            file.appendText(line)
        } catch (_: Exception) {}
    }

    fun getLogFilePath(): String? = logFile?.absolutePath
}
