package com.malla.mvp.network

import com.malla.mvp.events.MallaEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Protección anti-replay para mensajes mesh.
 * Ventana temporal de 300 segundos.
 * FIFO de 500 pares (messageId, timestamp).
 *
 * Referencia: Arquitectura V3.0 — Sección "Anti-Replay"
 */
object ReplayProtection {

    private const val WINDOW_MS = 300_000L  // 300 segundos
    private const val MAX_ENTRIES = 500
    private val window = ArrayDeque<Pair<String, Long>>(MAX_ENTRIES)
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Valida un mensaje entrante.
     * @return true si el mensaje es válido (no duplicado y dentro de la ventana temporal)
     */
    // Iter 55: @Synchronized para hacer check-y-add atómico. Antes, dos llamadas
    // concurrentes desde Dispatchers.IO podían pasar el `window.any` antes de que
    // la primera hiciera `addLast`, y ambas retornaban true → ambas insertaban en DB.
    // Causa raíz real de B2 (duplicados), no el messageId.
    @Synchronized
    fun validate(messageId: String, timestamp: Long): Boolean {
        val now = System.currentTimeMillis()

        // 1. Fuera de ventana temporal
        if (kotlin.math.abs(now - timestamp) > WINDOW_MS) {
            return false
        }

        // 2. ¿Ya existe en la ventana? (replay detectado)
        if (window.any { it.first == messageId }) {
            scope.launch {
                MallaEventBus.replayAttackDetected.emit(messageId)
            }
            return false
        }

        // 3. Agregar a la ventana (FIFO)
        if (window.size >= MAX_ENTRIES) {
            window.removeFirst()
        }
        window.addLast(Pair(messageId, timestamp))
        return true
    }

    /** Vacía la ventana de protección */
    @Synchronized
    fun clear() {
        window.clear()
    }
}
