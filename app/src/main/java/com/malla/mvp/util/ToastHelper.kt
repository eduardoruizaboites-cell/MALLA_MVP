package com.malla.mvp.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * Iter 49: wrapper seguro de Toast.
 *
 * Toast.makeText() internamente crea un Handler, que requiere un Looper en
 * el thread actual. Si se llama desde un thread sin Looper (por ejemplo una
 * corrutina en Dispatchers.IO, o una corrutina cancelada a mitad de un
 * withContext(Dispatchers.Main)), Android lanza:
 *   RuntimeException: Can't toast on a thread that has not called Looper.prepare()
 *
 * Este helper postea el Toast al message queue del main looper, garantizando
 * que siempre se ejecute en el thread correcto sin importar desde donde se llame.
 */
object ToastHelper {
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            try {
                Toast.makeText(context.applicationContext, message, duration).show()
            } catch (_: Exception) {}
        }
    }
}
