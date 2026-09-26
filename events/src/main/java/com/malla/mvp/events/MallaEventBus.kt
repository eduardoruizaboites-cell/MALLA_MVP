package com.malla.mvp.events

import com.malla.mvp.data.entity.MeshMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bus de eventos centralizado para MALLA.
 * Único canal de comunicación entre módulos (transporte, crypto, UI).
 *
 * Referencia: Arquitectura V3.0 — Sección "Bus de Eventos"
 */
object MallaEventBus {

    // ── Conectividad ──────────────────────────────────────────────
    /** Nivel mesh activo (1-10) según DecisionEngine */
    val meshLevelChanged = MutableSharedFlow<Int>(replay = 1)
    /** Se restauró la conexión a internet */
    val internetRestored = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Lista de nodos cercanos detectados (direcciones/IPs) */
    val nearbyNodesUpdated = MutableSharedFlow<List<String>>(replay = 1)
    /** IP de un peer descubierto por mDNS en la misma LAN (iter 46) */
    val peerMdnsResolved = MutableSharedFlow<String>(extraBufferCapacity = 5)

    // ── Mensajería ────────────────────────────────────────────────
    /** Mensaje recibido desde la red mesh (ya validado y guardado) */
    val messageReceived = MutableSharedFlow<MeshMessage>(extraBufferCapacity = 10)
    /** Se detectó un intento de replay (mensaje duplicado) */
    val replayAttackDetected = MutableSharedFlow<String>(extraBufferCapacity = 5)

    // ── Hardware ──────────────────────────────────────────────────
    /** Nivel de batería actual (0-100) */
    val batteryLevelChanged = MutableStateFlow(100)
    /** Nuevo contacto agregado vía NFC (publicKey base64) */
    val nfcContactAdded = MutableSharedFlow<String>(extraBufferCapacity = 2)

    // ── UI ────────────────────────────────────────────────────────
    /** Solicitud de mostrar un Toast desde cualquier módulo */
    val showToast = MutableSharedFlow<String>(extraBufferCapacity = 3)
    /** Usuario remoto está escribiendo */
    val typingReceived = MutableSharedFlow<Pair<String, Boolean>>(extraBufferCapacity = 5)
    /** Zumbido recibido */
    val zumbidoReceived = MutableSharedFlow<MeshMessage>(extraBufferCapacity = 10)
    /** El usuario abrió una conversación (peerId) → MessageReceiver enviará ACK=2 (read) al peer */
    val conversationOpened = MutableSharedFlow<String>(extraBufferCapacity = 5)
    /** Un peer aceptó nuestra invitación (iter 47). Par: (acceptorUserId, acceptorName, acceptorAvatarSeed) */
    val acceptanceReceived = MutableSharedFlow<Triple<String, String, Int>>(extraBufferCapacity = 5)
}
