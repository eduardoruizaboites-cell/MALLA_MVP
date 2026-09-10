package com.malla.mvp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.malla.mvp.MainActivity
import com.malla.mvp.R
import com.malla.mvp.network.NetworkService
import com.malla.mvp.network.MessageReceiver
import com.malla.mvp.network.ProximityEngine
import com.malla.mvp.network.BleTransport
import com.malla.mvp.network.TransportManager
import com.malla.mvp.network.InvitationManager
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.malla.mvp.core.config.MeshFlags

class MeshChatService : Service() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MALLA activa")
            .setContentText("La comunicación mesh está en segundo plano")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        startForeground(1, notification)

        // Iniciar componentes de red
        IdentityManager.init(this)
        NetworkService.startServer()
        MessageReceiver.start(this)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            TransportManager.start(this@MeshChatService)
            ProximityEngine.start(this@MeshChatService)
            if (MeshFlags.enableLegacyTransport) {
                com.malla.mvp.network.MeshConnector.start()
            }
            InvitationManager.start(this@MeshChatService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        MessageReceiver.stop()
        ProximityEngine.stop()
        BleTransport.stop()
        InvitationManager.stop()
        NetworkService.stopServer()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio MALLA",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "malla_foreground_service"
    }
}
