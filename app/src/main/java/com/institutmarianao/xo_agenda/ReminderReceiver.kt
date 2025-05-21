package com.institutmarianao.xo_agenda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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

        // 1) Null-checks sin return sobre una expresión
        val docId = intent.getStringExtra("docId")
        if (docId == null) {
            pendingResult.finish()
            return
        }
        val collection = intent.getStringExtra("type")
        if (collection == null) {
            pendingResult.finish()
            return
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            pendingResult.finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection(collection)
            .document(docId)
            .get()
            .addOnSuccessListener { doc ->
                // Usa la misma 'collection' que ya comprobaste arriba
                val title = doc.getString("titol")
                    ?: ctx.getString(R.string.default_title)
                val desc = doc.getString("descripció") ?: ""

                // Si por algún motivo aquí quisieras volver a salir:
                // if (collection.isBlank()) { pendingResult.finish(); return@addOnSuccessListener }

                val typeKey = collection.lowercase()
                val (alertType, extraInfo) = when (typeKey) {
                    "tasques" -> "tasca" to (doc.getString("estat") ?: "Sense estat")
                    "esdeveniments" -> "evento" to (doc.getString("dataInici")
                        ?: "Data desconeguda")

                    else -> "desconegut" to null
                }

                AlertRepository.addAlert(
                    ctx,
                    AlertItem(
                        id = docId,
                        title = title,
                        desc = desc,
                        isRead = false,
                        type = alertType,
                        extraInfo = extraInfo
                    )
                )

                val detailIntent = Intent(ctx, MenuActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigateTo", "alerts")
                    putExtra("docId", docId)
                    putExtra("titol", title)
                    putExtra("descripcio", desc)
                    putExtra("alertType", alertType)
                    putExtra("extraInfo", extraInfo)
                }
                val detailPI = PendingIntent.getActivity(
                    ctx,
                    docId.hashCode(),
                    detailIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val notifText = when (alertType) {
                    "evento" -> "Inici: $extraInfo"
                    "tasca" -> "Estat: $extraInfo"
                    else -> desc
                }

                val bigText = when (alertType) {
                    "evento" -> "$desc\n\n📅 Inici: $extraInfo"
                    "tasca" -> "$desc\n\n📌 Estat: $extraInfo"
                    else -> desc
                }

                val notifBuilder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoxajo)
                    .setContentTitle(title)
                    .setContentText(notifText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                    .setContentIntent(detailPI)
                    .setAutoCancel(true)

                val notif = notifBuilder.build()

                (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(docId.hashCode(), notif)

                /*val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoxajo)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(detailPI)
                    .setAutoCancel(true)
                    .build()

                (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(docId.hashCode(), notif)*/

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