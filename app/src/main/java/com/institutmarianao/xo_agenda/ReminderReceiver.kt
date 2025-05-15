package com.institutmarianao.xo_agenda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "XO_AGENDA_CHANNEL"
        private const val ACTION_SNOOZE = "posponer"
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        // Crear el canal si hace falta
        createNotificationChannel(ctx)

        // Extraer datos
        val title = intent.getStringExtra("title") ?: ctx.getString(R.string.default_title)
        val desc  = intent.getStringExtra("desc")  ?: ""

        // Intent al pulsar la notificación (detalle)
        val detailIntent = Intent(ctx, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val detailPending = PendingIntent.getActivity(
            ctx,
            title.hashCode(),
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para “posponer”
        val snoozeIntent = Intent(ctx, MyBroadcastReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(NotificationManager.EXTRA_NOTIFICATION_ID, title.hashCode())
        }
        val snoozePending = PendingIntent.getBroadcast(
            ctx,
            title.hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.logoxajo)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(detailPending)
            .addAction(R.drawable.ic_snooze, ctx.getString(R.string.snooze), snoozePending)
            .setAutoCancel(true)
            .build()

        // 6) Mostrarla
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(title.hashCode(), notif)
    }

    private fun createNotificationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name        = ctx.getString(R.string.channel_name)
            val description = ctx.getString(R.string.channel_description)
            val importance  = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
