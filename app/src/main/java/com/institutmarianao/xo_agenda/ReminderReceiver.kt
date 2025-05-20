package com.institutmarianao.xo_agenda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.institutmarianao.xo_agenda.alertas.AlertRepository
import com.institutmarianao.xo_agenda.models.AlertItem

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "XO_AGENDA_CHANNEL"
        const val ACTION_SNOOZE = "posponer"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        createNotificationChannel(ctx)
        val pendingResult = goAsync()

        val docId = intent.getStringExtra("docId")
            ?: return pendingResult.finish()
        val collection = intent.getStringExtra("type")
            ?: return pendingResult.finish()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return pendingResult.finish()

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection(collection)
            .document(docId)
            .get()
            .addOnSuccessListener { doc ->
                val title = doc.getString("titol")
                    ?: ctx.getString(R.string.default_title)
                val desc = doc.getString("descripció") ?: ""

                // ———> 1) Guardar la alerta en el repositorio (isRead=false)
                AlertRepository.addAlert(
                    ctx,
                    AlertItem(
                        id = docId,
                        title = title,
                        desc = desc,
                        isRead = false
                    )
                )

                val detailIntent = Intent(ctx, MenuActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigateTo", "alerts")
                    putExtra("docId", docId)
                    putExtra("titol", title)
                    putExtra("descripcio", desc)
                }
                val detailPI = PendingIntent.getActivity(
                    ctx,
                    docId.hashCode(),
                    detailIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoxajo)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(detailPI)
                    .setAutoCancel(true)
                    .build()

                (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(docId.hashCode(), notif)

                pendingResult.finish()
            }
            .addOnFailureListener {
                pendingResult.finish()
            }
    }

    private fun createNotificationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ctx.getString(R.string.channel_name)
            val description = ctx.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}