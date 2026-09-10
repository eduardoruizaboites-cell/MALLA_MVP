package com.malla.mvp.core.config

/**
 * Feature flags para controlar qué subsistemas de la app están activos.
 *
 * Objetivo: poder aislar canales durante diagnóstico sin recompilar varias veces
 * ni borrar código. Reactivar un canal = cambiar un `false` por `true` en este archivo.
 *
 * Estado actual (Iteración 7): modo minimalista "BLE + TCP LAN".
 * Los canales de más alto nivel (WebRTC, Wi-Fi Direct) y el sistema legacy
 * (MeshConnector, GlobalTransport, CascadeRouter, DHT) quedan apagados hasta
 * que la base esté estable.
 */
object MeshFlags {
    /** BLE (BleManager + BleTransport + ProximityEngine BLE scan). Núcleo del mesh local. */
    const val enableBle: Boolean = true

    /** TCP en misma red WiFi (NetworkService + DiscoveryService mDNS). */
    const val enableTcpLan: Boolean = true

    /** WebRTC sobre internet (WebRtcDataManager + SignalClient). Escrito pero no cableado aún. */
    const val enableWebRtc: Boolean = false

    /** Wi-Fi Direct de alto ancho de banda. Se enciende bajo demanda cuando se active. */
    const val enableWifiDirect: Boolean = false

    /** DHT (directorio distribuido). Tiene bug conocido de IP pública. */
    const val enableDht: Boolean = false

    /** Sistema legacy: MeshConnector + GlobalTransport + CascadeRouter. */
    const val enableLegacyTransport: Boolean = false
}
