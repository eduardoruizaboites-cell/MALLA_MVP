package com.malla.mvp.util

import android.content.Context
import com.malla.mvp.core.engine.DiagnosticsLogger

/**
 * Iter 49: handler global de excepciones no capturadas.
 *
 * Cuando Android detecta una excepción no capturada, mata el proceso sin dejar
 * rastro en malla_diagnostics.txt. Sin cable USB ni adb logcat, no hay forma
 * de diagnosticar el crash. Este handler intercepta la excepción ANTES de que
 * Android la mate, escribe el stack trace completo al log persistente, y re-lanza
 * para que Android siga con el comportamiento normal (matar el proceso).
 *
 * El log queda en el archivo de siempre — se puede exportar desde la app sin USB.
 */
object CrashCapture {
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = java.io.StringWriter()
                throwable.printStackTrace(java.io.PrintWriter(sw))
                val stack = sw.toString()
                val header = "=== CRASH EN THREAD '${thread.name}' ==="
                DiagnosticsLogger.log("CRASH", header)
                // Partir el stack en lineas para que no se trunque
                stack.lines().forEach { line ->
                    DiagnosticsLogger.log("CRASH", line)
                }
                DiagnosticsLogger.log("CRASH", "=== FIN CRASH ===")
            } catch (_: Exception) {}
            // Re-lanzar para que Android mate el proceso normalmente
            previous?.uncaughtException(thread, throwable)
        }
    }
}
