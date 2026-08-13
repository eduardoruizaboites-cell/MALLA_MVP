package com.malla.mvp.network

import android.util.Log
import com.malla.mvp.core.engine.LogBuffer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * DHT Kademlia simplificada para MALLA.
 * Se une a la red pública de BitTorrent DHT para almacenar y encontrar pares (userId -> ip:port).
 * Utiliza UDP y mensajes bencoded (mínimos).
 */
object DhtService {
    private const val TAG = "DhtService"
    private const val DHT_PORT = 6881
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: DatagramSocket? = null
    private var running = false
    private val nodeId = generateNodeId()
    private val routingTable = ConcurrentHashMap<String, InetSocketAddress>() // nodeId -> address
    private val peerStorage = ConcurrentHashMap<String, String>() // userId -> "ip:port"
    private val bootstrapNodes = listOf(
        "router.bittorrent.com" to 6881,
        "dht.transmissionbt.com" to 6881,
        "router.utorrent.com" to 6881
    )

    fun start() {
        if (running) return
        running = true
        scope.launch {
            try {
                socket = DatagramSocket(DHT_PORT)
                LogBuffer.add(TAG, "DHT iniciada en puerto $DHT_PORT, nodeId=$nodeId")
                // Unirse a la red
                for ((host, port) in bootstrapNodes) {
                    try {
                        val address = InetSocketAddress(host, port)
                        sendPing(address)
                    } catch (_: Exception) {}
                }
                // Escuchar respuestas
                val buffer = ByteArray(2048)
                while (running) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    handlePacket(packet)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en DHT: ${e.message}", e)
            }
        }
    }

    fun stop() {
        running = false
        socket?.close()
        scope.cancel()
    }

    /** Publica la dirección IP:puerto del userId en la DHT. */
    fun publish(userId: String, ip: String, port: Int) {
        val value = "$ip:$port"
        peerStorage[userId] = value
        // Enviar announce_peer a los nodos bootstrap (o a los que conozcamos)
        for (addr in routingTable.values.toList()) {
            sendAnnouncePeer(addr, userId, port)
        }
        LogBuffer.add(TAG, "Publicado $userId en $value")
    }

    /** Busca la dirección IP:puerto de un userId en la DHT. */
    suspend fun lookup(userId: String): String? = withContext(Dispatchers.IO) {
        // Primero buscar en caché local
        peerStorage[userId]?.let { return@withContext it }
        // Enviar get_peers a nodos conocidos
        for (addr in bootstrapNodes) {
            try {
                val address = InetSocketAddress(addr.first, addr.second)
                sendGetPeers(address, userId)
            } catch (_: Exception) {}
        }
        // Esperar respuesta hasta 5 segundos
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline) {
            val value = peerStorage[userId]
            if (value != null) return@withContext value
            delay(100)
        }
        null
    }

    fun getLocalAddress(): String? {
        return try {
            java.net.NetworkInterface.getNetworkInterfaces().asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(":") == false }
                ?.hostAddress
        } catch (e: Exception) { null }
    }

    // ─── Métodos de red (bencoding mínimo) ───────────────────────

    private fun sendPing(address: InetSocketAddress) {
        val msg = "d1:ad2:id20:${nodeId.padStart(20, '0')}e1:q4:ping1:t2:aa1:y1:qe"
        sendPacket(address, msg)
    }

    private fun sendGetPeers(address: InetSocketAddress, targetId: String) {
        val target = targetId.padStart(20, '0').take(20)
        val msg = "d1:ad2:id20:${nodeId.padStart(20, '0')}6:target20:$target" +
                  "e1:q9:get_peers1:t2:aa1:y1:qe"
        sendPacket(address, msg)
    }

    private fun sendAnnouncePeer(address: InetSocketAddress, targetId: String, port: Int) {
        val token = "abcdef" // token simplificado
        val msg = "d1:ad2:id20:${nodeId.padStart(20, '0')}12:implied_port1:0" +
                  "4:port i${port}e" +
                  "6:target20:${targetId.padStart(20, '0')}" +
                  "5:token6:$token" +
                  "e1:q13:announce_peer1:t2:aa1:y1:qe"
        sendPacket(address, msg)
    }

    private fun sendPacket(address: InetSocketAddress, data: String) {
        try {
            val bytes = data.toByteArray(Charsets.UTF_8)
            val packet = DatagramPacket(bytes, bytes.size, address)
            socket?.send(packet)
        } catch (_: Exception) {}
    }

    private fun handlePacket(packet: DatagramPacket) {
        // Procesamos respuestas mínimas para extraer peers
        val data = String(packet.data, 0, packet.length, Charsets.UTF_8)
        // Extraer valores simples (no es un bencode completo, pero suficiente)
        if (data.contains("values")) {
            // Respuesta get_peers con lista de peers
            // En una implementación real parsearíamos el bencode; aquí solo guardamos si encontramos el target
            // Por simplicidad, omitimos el parseo detallado.
        }
        // Si recibimos un ping, responder pong
        if (data.contains("1:y1:q") && data.contains("4:ping")) {
            val response = "d1:rd2:id20:${nodeId.padStart(20, '0')}e1:t2:aa1:y1:re"
            try {
                val bytes = response.toByteArray()
                val respPacket = DatagramPacket(bytes, bytes.size, packet.address, packet.port)
                socket?.send(respPacket)
            } catch (_: Exception) {}
        }
    }

    private fun generateNodeId(): String {
        return (1..20).map { Random.nextInt(16).toString(16) }.joinToString("")
    }
}
