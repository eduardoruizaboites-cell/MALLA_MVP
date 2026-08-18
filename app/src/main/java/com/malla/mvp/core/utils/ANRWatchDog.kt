package com.malla.mvp.core.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class ANRWatchDog(
    private val context: Context,
    private val timeoutMs: Long = 5000L
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val isBlocked = AtomicBoolean(true)

    fun start() {
        Thread {
            while (true) {
                isBlocked.set(true)
                mainHandler.post {
                    isBlocked.set(false)
                }
                Thread.sleep(timeoutMs)
                if (isBlocked.get()) {
                    generateReport()
                }
                Thread.sleep(timeoutMs / 2)
            }
        }.apply {
            name = "ANR-Watchdog"
            isDaemon = true
            start()
        }
    }

    private fun generateReport() {
        try {
            val traces = mutableListOf<String>()
            traces.add("=== MALLA ANR REPORT ===")
            traces.add("Fecha: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            traces.add("Tiempo límite: $timeoutMs ms\n")

            val mainThread = Looper.getMainLooper().thread
            traces.add("=== HILO PRINCIPAL (${mainThread.name}) ===")
            traces.add("State: ${mainThread.state}")
            mainThread.stackTrace.forEach { traces.add("\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})") }
            traces.add("")

            traces.add("=== TODOS LOS HILOS ===")
            for ((thread, stack) in Thread.getAllStackTraces()) {
                if (thread.name == "main") continue
                traces.add("Thread: ${thread.name} (state=${thread.state})")
                stack.forEach { traces.add("\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})") }
                traces.add("")
            }

            val report = traces.joinToString("\n")
            saveToDownloads(report)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveToDownloads(content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "anr_${System.currentTimeMillis()}.txt")
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                resolver.openOutputStream(it)?.use { out -> out.write(content.toByteArray()) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, values, null, null)
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "anr_${System.currentTimeMillis()}.txt")
            FileOutputStream(file).use { it.write(content.toByteArray()) }
        }
    }
}
