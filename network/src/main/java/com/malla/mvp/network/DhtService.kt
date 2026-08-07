package com.malla.mvp.network

import com.malla.mvp.core.engine.LogBuffer
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object DhtService {
    const val DHT_PORT = 8887
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: DatagramSocket? = null
    private var running = false
    private val routingTable = ConcurrentHashMap<String, InetSocketAddress>()
    private val storage = ConcurrentHashMap<String, String>()
    private val nodeId: String = generateNodeId()

    // Nodos semilla (reemplazar por IPs reales)
    private val seedNodes = listOf(
        "dht.malla.network:8887",
        "seed1.malla.org:8887"
    )

    private fun generateNodeId(): String = (1..20).map { Random.nextInt(16).toString(16) }.joinToString("")

    fun start() {
        if (running) return
        running = true
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
        val address = "$ip:$port"
        storage[userId] = address
    }

    fun find(userId: String): String? {
        return storage[userId]
    }

    fun getLocalAddress(): String? {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.contains(":") == false) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {}
        return null
    }

    private fun joinNetwork() {
        seedNodes.forEach { seed ->
            try {
                val parts = seed.split(":")
                val host = parts[0]
                val port = parts[1].toInt()
                sendPing(InetSocketAddress(host, port))
            } catch (_: Exception) {}
        }
    }

    private fun sendPing(target: InetSocketAddress) {
        try {
            val message = "PING|$nodeId"
            val data = message.toByteArray()
            val packet = DatagramPacket(data, data.size, target.address, target.port)
            socket?.send(packet)
        } catch (_: Exception) {}
    }

    private fun handlePacket(packet: DatagramPacket) {
        val message = String(packet.data, 0, packet.length)
        val sender = InetSocketAddress(packet.address, packet.port)
        val parts = message.split("|")
        when (parts[0]) {
            "PING" -> {
                val remoteNodeId = parts[1]
                routingTable[remoteNodeId] = sender
                val response = "PONG|$nodeId"
                val data = response.toByteArray()
                val reply = DatagramPacket(data, data.size, sender.address, sender.port)
                try { socket?.send(reply) } catch (_: Exception) {}
            }
            "PONG" -> {
                val remoteNodeId = parts[1]
                routingTable[remoteNodeId] = sender
            }
            "FIND" -> {
                val key = parts[1]
                val value = storage[key]
                if (value != null) {
                    val response = "FOUND|$key|$value"
                    val data = response.toByteArray()
                    val reply = DatagramPacket(data, data.size, sender.address, sender.port)
                    try { socket?.send(reply) } catch (_: Exception) {}
                }
            }
            "STORE" -> {
                if (parts.size == 3) {
                    val key = parts[1]
                    val value = parts[2]
                    storage[key] = value
                }
            }
        }
    }
}
