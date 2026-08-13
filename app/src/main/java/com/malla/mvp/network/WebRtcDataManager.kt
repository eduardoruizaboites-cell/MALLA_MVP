package com.malla.mvp.network

import com.malla.mvp.App

import android.util.Log
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.webrtc.*
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor de WebRTC DataChannel para mensajería P2P.
 * Utiliza el cliente de señalización para intercambiar SDP/ICE.
 */
object WebRtcDataManager {
    private const val TAG = "WebRtcDataManager"

    private val _incomingMessages = MutableSharedFlow<MeshMessage>(replay = 10)
    val incomingMessages: SharedFlow<MeshMessage> = _incomingMessages.asSharedFlow()

    private val peers = ConcurrentHashMap<String, PeerConnection>()
    private val dataChannels = ConcurrentHashMap<String, DataChannel>()

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
    )

    private lateinit var factory: PeerConnectionFactory

    fun start() {
        val options = PeerConnectionFactory.InitializationOptions.builder(App.context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        val eglBase = EglBase.create()
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

        SignalClient.setListener { from, payload ->
            handleSignal(from, payload)
        }
        SignalClient.connect()
        LogBuffer.add(TAG, "WebRTC DataChannel iniciado")
    }

    suspend fun sendToContact(contactId: String, message: MeshMessage) {
        if (!dataChannels.containsKey(contactId)) {
            createOffer(contactId)
            // Esperar hasta 10s a que se establezca el canal
            var retries = 0
            while (retries < 20 && !dataChannels.containsKey(contactId)) {
                delay(500)
                retries++
            }
        }
        val channel = dataChannels[contactId]
        if (channel?.state() == DataChannel.State.OPEN) {
            val buffer = ByteBuffer.wrap(message.content.toByteArray(Charsets.UTF_8))
            channel.send(DataChannel.Buffer(buffer, false))
            LogBuffer.add(TAG, "Mensaje enviado a $contactId vía WebRTC")
        } else {
            // Guardar pendiente en NetworkService (fallback)
            NetworkService.sendMessageToContact(contactId, message)
        }
    }

    private fun createOffer(contactId: String) {
        val peer = createPeerConnection(contactId)
        val channel = peer.createDataChannel("messages", DataChannel.Init().apply { ordered = true })
        setupDataChannel(contactId, channel)

        val constraints = MediaConstraints()
        peer.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peer.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            SignalClient.sendSignal(contactId, "sdp:${sdp.type.canonicalForm()}:${sdp.description}")
                        }
                        override fun onSetFailure(p0: String?) {}
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    private fun createPeerConnection(contactId: String): PeerConnection {
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }
        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    SignalClient.sendSignal(contactId, "ice:${it.sdp}:${it.sdpMLineIndex}:${it.sdpMid}")
                }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {
                channel?.let { setupDataChannel(contactId, it) }
            }
            override fun onRenegotiationNeeded() {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
        }
        val peer = factory.createPeerConnection(config, observer)!!
        peers[contactId] = peer
        return peer
    }

    private fun setupDataChannel(contactId: String, channel: DataChannel) {
        dataChannels[contactId] = channel
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {}
            override fun onStateChange() {
                if (channel.state() == DataChannel.State.OPEN) {
                    LogBuffer.add(TAG, "DataChannel abierto con $contactId")
                }
            }
            override fun onMessage(buffer: DataChannel.Buffer?) {
                val bytes = ByteArray(buffer?.data?.remaining() ?: 0)
                buffer?.data?.get(bytes)
                val content = String(bytes, Charsets.UTF_8)
                val msg = MeshMessage(content = content, senderId = contactId, type = "chat")
                MallaEventBus.messageReceived.tryEmit(msg)
                _incomingMessages.tryEmit(msg)
            }
        })
    }

    private fun handleSignal(from: String, payload: String) {
        val parts = payload.split(":", limit = 3)
        when (parts[0]) {
            "sdp" -> {
                val type = if (parts[1] == "offer") SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER
                val sdp = SessionDescription(type, parts[2])
                val peer = peers[from] ?: createPeerConnection(from).also {
                    // Configurar canal si es oferta entrante
                }
                peer.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        if (parts[1] == "offer") {
                            createAnswer(from, peer)
                        }
                    }
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sdp)
            }
            "ice" -> {
                val iceParts = payload.split(":")
                if (iceParts.size >= 4) {
                    val sdpMid = iceParts[3]
                    val sdpMLineIndex = iceParts[2].toIntOrNull() ?: 0
                    val sdp = iceParts[1]
                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                    peers[from]?.addIceCandidate(candidate)
                }
            }
        }
    }

    private fun createAnswer(contactId: String, peer: PeerConnection) {
        val channel = peer.createDataChannel("messages", DataChannel.Init().apply { ordered = true })
        setupDataChannel(contactId, channel)
        val constraints = MediaConstraints()
        peer.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peer.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            SignalClient.sendSignal(contactId, "sdp:${sdp.type.canonicalForm()}:${sdp.description}")
                        }
                        override fun onSetFailure(p0: String?) {}
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    fun stop() {
        SignalClient.disconnect()
        peers.values.forEach { it.close() }
        peers.clear()
        dataChannels.clear()
    }
}
