package com.institutmarianao.xo_agenda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Recordatori"
        val desc  = intent.getStringExtra("desc")  ?: ""
        val channelId = "XO_AGENDA_CHANNEL"

        // 1) Crear canal (solo una vez es suficiente)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                "Alertes de l’Agenda",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Canal per als recordatoris" }
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(chan)
        }

        // 2) Construir i mostrar la notificació
        val notif = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.logoxajo)  // tu icona
            .setContentTitle(title)
            .setContentText(desc)
            .setAutoCancel(true)
            .build()

        ctx.getSystemService(NotificationManager::class.java)
            .notify(title.hashCode(), notif)
    }
}
