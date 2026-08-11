package com.malla.mvp.network

import android.content.Context
import android.widget.Toast
import com.malla.mvp.App
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.MessageEntity
import com.malla.mvp.di.Injector
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.*
import java.util.UUID

object CascadeRouter {
    private const val TAG = "CascadeRouter"
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
        mainHandler.post {
            Toast.makeText(App.context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
        }
    })
    private val phoneMap = mutableMapOf<String, String>()

    fun sendMessage(contactId: String, content: String, type: String = "chat") {
        scope.launch {
            try {
                // 1. Internet
                if (ConnectivityMonitor.isOnline.value) {
                    try {
                        Injector.networkService.sendMeshMessage(
                            com.malla.mvp.core.network.MeshMessage(
                                senderId = IdentityManager.getIdentityId(),
                                content = content,
                                type = if (type == "zumbido") 4 else 0
                            )
                        )
                        LogBuffer.add(TAG, "Enviado por Internet a $contactId")
                        return@launch
                    } catch (e: Exception) {
                        LogBuffer.add(TAG, "Internet falló: ${e.message}")
                    }
                }

                // 2. TCP directo (esperar hasta 3s si no hay clientes)
                if (NetworkService.connectedClientsCount.value > 0) {
                    try {
                        NetworkService.sendMessage(
                            MeshMessage(content = content, senderId = "self", type = type)
                        )
                        LogBuffer.add(TAG, "Enviado por TCP a $contactId")
                        return@launch
                    } catch (e: Exception) {
                        LogBuffer.add(TAG, "TCP falló: ${e.message}")
                    }
                } else {
                    // Reintentar hasta 3 segundos
                    var retries = 0
                    while (retries < 6 && NetworkService.connectedClientsCount.value == 0) {
                        delay(500)
                        retries++
                    }
                    if (NetworkService.connectedClientsCount.value > 0) {
                        try {
                            NetworkService.sendMessage(
                                MeshMessage(content = content, senderId = "self", type = type)
                            )
                            LogBuffer.add(TAG, "Enviado por TCP (tras espera) a $contactId")
                            return@launch
                        } catch (e: Exception) {
                            LogBuffer.add(TAG, "TCP falló tras espera: ${e.message}")
                        }
                    }
                }

                // 3. Mesh (DHT routing)
                if (!ConnectivityMonitor.isOnline.value) {
                    try {
                        val sent = DhtService.routeMessage(contactId, content)
                        if (sent) {
                            LogBuffer.add(TAG, "Enviado por Mesh a $contactId")
                            return@launch
                        }
                    } catch (e: Exception) {
                        LogBuffer.add(TAG, "Mesh falló: ${e.message}")
                    }
                }

                // 4. SMS
                val phone = getContactPhone(contactId)
                if (phone.isNotBlank()) {
                    try {
                        Injector.smsTransport.sendSms(phone, content)
                        LogBuffer.add(TAG, "Enviado por SMS a $contactId ($phone)")
                        return@launch
                    } catch (e: Exception) {
                        LogBuffer.add(TAG, "SMS falló: ${e.message}")
                    }
                }

                // 4. Mesh (pendiente por ahora)
                LogBuffer.add(TAG, "Mesh no disponible, guardando pendiente")

                // 5. Guardar pendiente
                savePendingMessage(contactId, content)
            } catch (e: Exception) {
                LogBuffer.add(TAG, "Error en cascada: ${e.message}")
                try {
                    mainHandler.post {
                        Toast.makeText(App.context, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun getContactPhone(contactId: String): String {
        return try {
            phoneMap[contactId]?.let { return it }
            val prefs = App.context.getSharedPreferences("malla_contacts", Context.MODE_PRIVATE)
            val saved = prefs.getString(contactId, "")
            if (!saved.isNullOrBlank()) {
                phoneMap[contactId] = saved
                return saved
            }
            if (contactId.all { it.isDigit() || it == '+' }) {
                phoneMap[contactId] = contactId
                prefs.edit().putString(contactId, contactId).apply()
                return contactId
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun savePendingMessage(contactId: String, content: String) {
        try {
            val db = AppDatabase.getInstance(App.context) ?: return
            val msg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = contactId,
                content = content,
                timestamp = System.currentTimeMillis(),
                isOwn = true,
                status = 0
            )
            db.messageDao().insertMessage(msg)
        } catch (e: Exception) {
            LogBuffer.add(TAG, "Error guardando pendiente: ${e.message}")
        }
    }

    fun retryPendingMessages() {
        scope.launch {
            try {
                val db = AppDatabase.getInstance(App.context) ?: return@launch
                val allMessages = db.messageDao().observeAllMessages()
                allMessages.collect { messages ->
                    messages.filter { it.status == 0 }.forEach { msg ->
                        sendMessage(msg.conversationId, msg.content)
                        db.messageDao().updateStatus(msg.id, 1)
                    }
                }
            } catch (e: Exception) {
                LogBuffer.add(TAG, "Error reintentando pendientes: ${e.message}")
            }
        }
    }
}
