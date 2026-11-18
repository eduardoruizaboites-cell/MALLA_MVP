package com.malla.mvp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.malla.mvp.App
import com.malla.mvp.network.WebRtcManager
import com.malla.mvp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Iter 70C: ViewModel que expone WebRtcManager a la UI de llamadas.
 * El estado de la llamada (Idle/Outgoing/Incoming/Connected/Ended) viene directamente
 * del StateFlow interno de WebRtcManager. Adicionalmente expone callerName y callType
 * para que CallScreen muestre el nombre real del contacto y el ícono de voz/video.
 *
 * Inicializa WebRtcManager (idempotente, guard sincronizado) en init.
 * Al limpiarse, si hay llamada activa, la cierra para no dejar peer connection huérfano.
 */
class CallViewModel : ViewModel() {

    val callState: StateFlow<WebRtcManager.CallState> = WebRtcManager.callState

    private val _callerName = MutableStateFlow<String?>(null)
    val callerName: StateFlow<String?> = _callerName.asStateFlow()

    private val _callType = MutableStateFlow("voice")
    val callType: StateFlow<String> = _callType.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        WebRtcManager.start(App.context)
        // Iter 70c: hidratar callerName cuando llega una llamada entrante
        scope.launch {
            WebRtcManager.callState.collect { state ->
                if (state is WebRtcManager.CallState.Incoming && _callerName.value == null) {
                    val resolved = withContext(Dispatchers.IO) {
                        try {
                            AppDatabase.getInstance(App.context)?.contactDao()
                                ?.getById(state.contactId)?.displayName
                        } catch (e: Exception) { null }
                    }
                    _callerName.value = resolved ?: state.contactId.take(8)
                    _callType.value = state.callType
                }
            }
        }
    }

    fun startVoiceCall(contactId: String, name: String) {
        _callerName.value = name
        _callType.value = "voice"
        WebRtcManager.startVoiceCall(contactId)
    }

    fun startVideoCall(contactId: String, name: String) {
        _callerName.value = name
        _callType.value = "video"
        WebRtcManager.startVideoCall(contactId)
    }

    fun acceptCall() {
        val incoming = WebRtcManager.callState.value as? WebRtcManager.CallState.Incoming ?: return
        WebRtcManager.acceptCall(incoming.contactId, incoming.callType)
    }

    fun endCall() {
        WebRtcManager.endCall()
        _callerName.value = null
    }

    fun toggleMute(muted: Boolean) {
        WebRtcManager.setMicrophoneMute(muted)
    }

    fun toggleSpeaker(on: Boolean) {
        WebRtcManager.setSpeakerphoneOn(on)
    }

    override fun onCleared() {
        super.onCleared()
        if (WebRtcManager.isInCall()) WebRtcManager.endCall()
    }
}
