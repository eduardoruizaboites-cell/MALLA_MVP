package com.malla.mvp.network

import com.malla.mvp.core.engine.LogBuffer
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.random.Random

object DhtService {
    const val DHT_PORT = 8887
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: DatagramSocket? = null
    private var running = false
    private val nodeId: String = generateNodeId()
    private var seedNodes = mutableListOf<InetSocketAddress>()

    fun start(seeds: List<String> = listOf("127.0.0.1:8887")) {
        if (running) return
        running = true
        seedNodes = seeds.map {
            val parts = it.split(":")
            InetSocketAddress(parts[0], parts[1].toInt())
        }.toMutableList()
        scope.launch {
            try {
                socket = DatagramSocket(DHT_PORT)
                LogBuffer.add("DHT", "Iniciada en puerto $DHT_PORT, nodeId=$nodeId")
                joinNetwork()
                val buffer = ByteArray(2048)
                while (running) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    handlePacket(packet)
                }
            } catch (e: Exception) {
                LogBuffer.add("DHT", "Error: ${e.message}")
            }
        }
    }

    fun stop() {
        running = false
        socket?.close()
        scope.cancel()
    }

    fun publish(userId: String, ip: String, port: Int) {
        val message = "PUBLISH|$userId|$ip|$port"
        sendToAllSeeds(message)
    }

    /**
     * Busca un userId en la DHT. Envía FIND a todas las semillas y espera la primera respuesta positiva.
     * Retorna la dirección "ip:port" o null si no se encuentra o hay timeout.
     */
    suspend fun find(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = "FIND|$userId"
            val data = request.toByteArray()
            // Enviar a cada semilla
            for (seed in seedNodes) {
                try {
                    val packet = DatagramPacket(data, data.size, seed.address, seed.port)
                    socket?.send(packet)
                } catch (_: Exception) {}
            }

            // Esperar respuestas durante 5 segundos
            val responseBuffer = ByteArray(1024)
            val deadline = System.currentTimeMillis() + 5000L
            while (System.currentTimeMillis() < deadline) {
                try {
                    val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                    socket?.soTimeout = (deadline - System.currentTimeMillis()).coerceAtLeast(1).toInt()
                    socket?.receive(responsePacket)
                    val response = String(responsePacket.data, 0, responsePacket.length)
                    if (response.startsWith("FOUND|")) {
                        val parts = response.split("|")
                        if (parts.size >= 3 && parts[1] == userId) {
                            return@withContext parts[2] // "ip:port"
                        }
                    } else if (response.startsWith("NOTFOUND")) {
                        // continuar escuchando
                    }
                } catch (e: SocketTimeoutException) {
                    break
                }
            }
        } catch (e: Exception) {
            LogBuffer.add("DHT", "Error en find: ${e.message}")
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

    private fun joinNetwork() {
        sendToAllSeeds("PING|$nodeId")
    }

    private fun sendToAllSeeds(message: String) {
        val data = message.toByteArray()
        for (seed in seedNodes) {
            try {
                val packet = DatagramPacket(data, data.size, seed.address, seed.port)
                socket?.send(packet)
            } catch (_: Exception) {}
        }
    }

    private fun handlePacket(packet: DatagramPacket) {
        // Procesa respuestas PONG y otras (no críticas para el funcionamiento básico)
    }

    private fun generateNodeId(): String = (1..20).map { Random.nextInt(16).toString(16) }.joinToString("")
}
