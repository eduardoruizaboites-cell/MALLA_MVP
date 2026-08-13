package com.malla.mvp.network

import android.util.Log
import com.malla.mvp.App
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.crypto.CryptoEngine
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.NetworkInterface
import java.security.PublicKey
import javax.crypto.SecretKey

object NetworkService {
    private const val TAG = "NetworkService"
    const val DEFAULT_PORT = 8888

    // Flujos internos (opcional, para compatibilidad)
    private val _messages = MutableSharedFlow<MeshMessage>(replay = 10)
    val messages: SharedFlow<MeshMessage> = _messages.asSharedFlow()

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    private var isServerRunning = false
    private val serverJob = Job()
    private val serverScope = CoroutineScope(Dispatchers.IO + serverJob)

    // Mapa de clientes: contactId -> ClientHandler
    private val clients = mutableMapOf<String, ClientHandler>()
    private val clientsBySocket = mutableMapOf<Socket, ClientHandler>()
    private val pendingMessages = mutableMapOf<String, MutableList<MeshMessage>>()

    // Clave efímera local para ECDH (se regenera en cada arranque)
    private val localKeyPair = CryptoEngine.generateKeyPair()
    private val localPublicKeyBase64 = CryptoEngine.publicKeyToBase64(localKeyPair.public)

    // Datos de identidad local (se obtienen al iniciar)
    private fun getLocalUserId(): String = IdentityManager.getIdentityId()
    private fun getLocalDisplayName(): String = IdentityManager.getUserName(App.context)

    fun startServer() {
        if (isServerRunning) return
        isServerRunning = true
        serverScope.launch {
            try {
                val serverSocket = ServerSocket(DEFAULT_PORT)
                Log.d(TAG, "[NS:TCP] Servidor iniciado en puerto $DEFAULT_PORT")
                while (isActive) {
                    val clientSocket = serverSocket.accept()
                    val handler = ClientHandler(
                        socket = clientSocket,
                        expectedContactId = null,
                        expectedPublicKeyBase64 = null,
                        localUserId = getLocalUserId(),
                        localDisplayName = getLocalDisplayName()
                    )
                    handler.start()
                    Log.d(TAG, "[NS:TCP] Nueva conexión entrante")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[NS:ERR] Error en servidor: ${e.message}", e)
                isServerRunning = false
            }
        }
    }

    fun stopServer() {
        Log.d(TAG, "[NS:TCP] Deteniendo servidor (${clients.size} clientes)")
        serverJob.cancel()
        clients.values.forEach { it.disconnect() }
        clients.clear()
        clientsBySocket.clear()
        _connectedClientsCount.value = 0
    }

    fun connectToPeer(address: String, expectedContactId: String? = null, expectedPublicKeyBase64: String? = null) {
        Log.d(TAG, "[NS:TCP] Intentando conectar a $address:$DEFAULT_PORT")
        serverScope.launch {
            try {
                val socket = Socket(address, DEFAULT_PORT)
                val handler = ClientHandler(
                    socket = socket,
                    expectedContactId = expectedContactId,
                    expectedPublicKeyBase64 = expectedPublicKeyBase64,
                    localUserId = getLocalUserId(),
                    localDisplayName = getLocalDisplayName()
                )
                handler.start()
                Log.d(TAG, "[NS:TCP] Conectado a $address")
            } catch (e: Exception) {
                Log.e(TAG, "[NS:ERR] Error conectando a $address: ${e.message}", e)
            }
        }
    }

    suspend fun sendMessageToContact(contactId: String?, message: MeshMessage) {
        if (contactId == null) {
            // Broadcast
            clients.values.forEach { it.send(message) }
        } else {
            val handler = clients[contactId]
            if (handler != null) {
                handler.send(message)
            } else {
                // Encolar mensaje pendiente
                val queue = pendingMessages.getOrPut(contactId) { mutableListOf() }
                queue.add(message)
                Log.w(TAG, "[NS:MSG] Contacto $contactId no conectado. Mensaje encolado (${queue.size} pendientes)")
            }
        }
    }

    // Llamar al registrar un nuevo cliente para enviar pendientes
    private fun flushPendingMessages(contactId: String, handler: ClientHandler) {
        val queue = pendingMessages.remove(contactId) ?: return
        for (msg in queue) {
            serverScope.launch { handler.send(msg) }
        }
        Log.d(TAG, "[NS:MSG] Enviados ${queue.size} mensajes pendientes a $contactId")
    }

    // Compatibilidad con llamadas anteriores (se puede eliminar después)
    suspend fun sendMessage(message: MeshMessage) {
        sendMessageToContact(null, message)
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.contains(":") == false) {
                        return address.hostAddress ?: "Desconocida"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[NS:ERR] Error obteniendo IP", e)
        }
        return "Desconocida"
    }

    class ClientHandler(
        private val socket: Socket,
        private val expectedContactId: String?,
        private val expectedPublicKeyBase64: String?,
        private val localUserId: String,
        private val localDisplayName: String
    ) {
        private var input: DataInputStream? = null
        private var output: DataOutputStream? = null
        private var secretKey: SecretKey? = null
        private var running = false
        var contactId: String? = null
        var displayName: String? = null
        var publicKeyBase64: String? = null

        fun start() {
            running = true
            val handler = this  // Capturar instancia para usar dentro de la corrutina
            serverScope.launch {
                try {
                    input = DataInputStream(socket.getInputStream())
                    output = DataOutputStream(socket.getOutputStream())

                    // 1. Enviar nuestra clave pública y datos de identidad
                    val identityPayload = "$localPublicKeyBase64|$localUserId|$localDisplayName"
                    output?.writeUTF(identityPayload)
                    output?.flush()

                    // 2. Recibir datos del peer
                    val peerPayload = input?.readUTF() ?: throw Exception("No se recibió identidad")
                    val parts = peerPayload.split("|")
                    if (parts.size < 3) throw Exception("Payload de identidad incompleto")
                    val peerPubKeyBase64 = parts[0]
                    val peerUserId = parts[1]
                    val peerDisplayName = parts[2]

                    // 3. Verificar clave pública si se esperaba una concreta
                    if (expectedPublicKeyBase64 != null && peerPubKeyBase64 != expectedPublicKeyBase64) {
                        Log.e(TAG, "[NS:HS] Clave pública no coincide para $peerUserId. Desconectando.")
                        socket.close()
                        return@launch
                    }

                    // 4. Verificar contactId esperado (si se especificó)
                    if (expectedContactId != null && peerUserId != expectedContactId) {
                        Log.e(TAG, "[NS:HS] UserId no coincide con el esperado ($peerUserId != $expectedContactId). Desconectando.")
                        socket.close()
                        return@launch
                    }

                    // 5. Derivar secreto compartido
                    val peerPublicKey = CryptoEngine.base64ToPublicKey(peerPubKeyBase64)
                    secretKey = CryptoEngine.deriveSharedSecret(localKeyPair.private, peerPublicKey)

                    // 6. Guardar datos del peer
                    handler.contactId = peerUserId
                    handler.displayName = peerDisplayName
                    handler.publicKeyBase64 = peerPubKeyBase64

                    // 7. Registrar cliente en el mapa global
                    clients[peerUserId] = handler
                    clientsBySocket[socket] = handler
                    flushPendingMessages(peerUserId, handler)
                    _connectedClientsCount.value = clients.size

                    Log.d(TAG, "[NS:HS] Handshake completado con $peerUserId ($peerDisplayName)")
                    LogBuffer.add("NS", "Handshake ECDH OK: $peerUserId")

                    // 8. Escuchar mensajes entrantes
                    listenForMessages()
                } catch (e: Exception) {
                    Log.e(TAG, "[NS:ERR] Handshake fallido: ${e.message}", e)
                    disconnect()
                }
            }
        }

        private suspend fun listenForMessages() {
            try {
                while (running) {
                    val length = input?.readInt() ?: break
                    val encrypted = ByteArray(length)
                    input?.readFully(encrypted)
                    val decrypted = CryptoEngine.decrypt(encrypted, secretKey!!)
                    val parts = decrypted.split("|", limit = 4)
                    val type = parts.getOrElse(0) { "chat" }
                    val quoteId = parts.getOrElse(1) { "" }.ifBlank { null }
                    val quoteContent = parts.getOrElse(2) { "" }.ifBlank { null }
                    val text = parts.getOrElse(3) { decrypted }
                    val message = MeshMessage(
                        content = text,
                        senderId = contactId ?: "unknown",
                        type = type,
                        quotedMessageId = quoteId,
                        quotedMessageContent = quoteContent
                    )
                    Log.d(TAG, "[NS:MSG] Mensaje recibido de $contactId (tipo=$type, ${encrypted.size} bytes)")
                    LogBuffer.add("NS", "Mensaje recibido: tipo=${type} de $contactId")
                    // Emitir al bus global para que el ViewModel lo procese
                    // También al flujo local para compatibilidad
                    _messages.emit(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[NS:ERR] Error recibiendo mensaje de $contactId: ${e.message}", e)
            } finally {
                disconnect()
            }
        }

        suspend fun send(message: MeshMessage) {
            try {
                val wire = "${message.type}|${message.quotedMessageId ?: ""}|${message.quotedMessageContent ?: ""}|${message.content}"
                val encrypted = CryptoEngine.encrypt(wire, secretKey!!)
                output?.writeInt(encrypted.size)
                output?.write(encrypted)
                output?.flush()
                Log.d(TAG, "[NS:MSG] Mensaje enviado a $contactId (${encrypted.size} bytes cifrados)")
            } catch (e: Exception) {
                Log.e(TAG, "[NS:ERR] Error enviando mensaje a $contactId: ${e.message}", e)
            }
        }

        fun disconnect() {
            running = false
            try { socket.close() } catch (_: Exception) {}
            val id = contactId
            if (id != null) {
                clients.remove(id)
                clientsBySocket.remove(socket)
            }
            _connectedClientsCount.value = clients.size
            Log.d(TAG, "[NS:TCP] Cliente desconectado: ${id ?: "desconocido"} (total: ${clients.size})")
        }
    }
}

data class MeshMessage(
    val content: String,
    val senderId: String = "self",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "chat",
    val quotedMessageId: String? = null,
    val quotedMessageContent: String? = null
)
