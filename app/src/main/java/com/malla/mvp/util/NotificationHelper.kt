package com.malla.mvp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.malla.mvp.MainActivity
import com.malla.mvp.R
import com.malla.mvp.core.engine.DiagnosticsLogger

object NotificationHelper {
    private const val CHANNEL_ID = "malla_messages"
    private const val GROUP_KEY = "malla_messages_group"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes entrantes"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            DiagnosticsLogger.log(
                "NotifHelper",
                "Canal $CHANNEL_ID creado. Existe=${existing != null}, importance=${existing?.importance}, enabled=${areNotificationsEnabled(context)}"
            )
        } else {
            DiagnosticsLogger.log("NotifHelper", "Android < O: sin canales. enabled=${areNotificationsEnabled(context)}")
        }
    }

    private fun areNotificationsEnabled(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) nm.areNotificationsEnabled() else true
    }

    private fun channelBlocked(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = nm.getNotificationChannel(CHANNEL_ID) ?: return true
        return channel.importance == NotificationManager.IMPORTANCE_NONE
    }

    fun showMessageNotification(context: Context, conversationId: String, senderName: String, content: String) {
        if (!areNotificationsEnabled(context)) {
            DiagnosticsLogger.log("NotifHelper", "OMITIDA: notificaciones deshabilitadas (POST_NOTIFICATIONS denegado)")
            return
        }
        if (channelBlocked(context)) {
            DiagnosticsLogger.log("NotifHelper", "OMITIDA: canal $CHANNEL_ID bloqueado por el usuario")
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("conversation_id", conversationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, conversationId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Iter 48: color de acento del tema actual del usuario.
        // Conversion manual desde Compose Color (componentes red/green/blue en floats 0..1)
        // para evitar dependencia de androidx.compose.ui.graphics.toArgb().
        val accentColor: Int = try {
            val themeState = com.malla.mvp.viewmodel.AppThemeState.create(context)
            val scheme = themeState.currentTheme.value
            android.graphics.Color.argb(
                255,
                (scheme.primary.red * 255f).toInt().coerceIn(0, 255),
                (scheme.primary.green * 255f).toInt().coerceIn(0, 255),
                (scheme.primary.blue * 255f).toInt().coerceIn(0, 255)
            )
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#4CE6FF")  // fallback cyan MALLA
        }

        // Iter 48: estilo MessagingStyle (look WhatsApp/Telegram, agrupa por contacto)
        val me = androidx.core.app.Person.Builder().setName("Yo").build()
        val sender = androidx.core.app.Person.Builder().setName(senderName).build()
        val messagingStyle = NotificationCompat.MessagingStyle(me)
            .addMessage(content, System.currentTimeMillis(), sender)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setColorized(false)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setGroup(GROUP_KEY)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueId = conversationId.hashCode()
        manager.notify(uniqueId, builder.build())

        // Resumen del grupo
        val summaryBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setContentTitle("MALLA")
            .setContentText("Tienes mensajes nuevos")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        manager.notify(GROUP_KEY.hashCode(), summaryBuilder.build())
    }

    fun cancelNotifications(context: Context, conversationId: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    fun showDiscoveryNotification(context: Context, peerName: String) {
        if (!areNotificationsEnabled(context)) return
        if (channelBlocked(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 9876, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Dispositivo MALLA encontrado")
            .setContentText(peerName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueId = System.currentTimeMillis().toInt()
        manager.notify(uniqueId, builder.build())
    }
}
