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
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        createNotificationChannel(ctx)
        val pendingResult = goAsync()

        // 1) Extraemos docId y collection (evento/tasca)
        val docId = intent.getStringExtra("docId")
        val collection = intent.getStringExtra("type")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (docId == null || collection == null || uid == null) {
            pendingResult.finish()
            return
        }

        // 2) Leemos el documento Firestore
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection(collection)
            .document(docId)
            .get()
            .addOnSuccessListener { doc ->
                // 3) Título “crudo” (sin fallback)
                val rawTitle = doc.getString("titol")
                if (rawTitle == null) {
                    pendingResult.finish()
                    return@addOnSuccessListener
                }
                // 4) Descripción
                val rawDesc = doc.getString("descripció") ?: ""
                // 5) ExtraInfo (por ejemplo fecha de inicio)
                val rawExtra = doc.getDate("data_inici")?.toString() ?: ""

                // 6) Decidimos tipo de alerta según la colección
                val alertType = when (collection) {
                    "esdeveniments" -> "evento"
                    "tasques"        -> "tasca"
                    else             -> "otro"
                }

                // 7) Construimos y guardamos la alerta real
                // Así encaja con tu data class
                val item = AlertItem(
                    id       = docId,
                    title    = rawTitle,
                    desc     = rawDesc,            // coincide con “val desc: String”
                    isRead   = false,              // por defecto no leído
                    type     = alertType,          // tu “evento” o “tasca”
                    extraInfo= rawExtra            // la fecha u otra info
                )

                AlertRepository.addAlert(ctx, item)

                // 8) Preparamos los datos para la notificación
                val title     = item.title
                val desc      = item.desc
                val extraInfo = item.extraInfo

                val notifText = when (alertType) {
                    "evento" -> "Descripcion: $desc"
                    "tasca"  -> "Descripcion: $desc"
                    else     -> desc
                }


                // 9) Intent genérico: solo navigatingTo=alerts
                val detailIntent = Intent(ctx, MenuActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigateTo", "alerts")
                }
                val detailPI = PendingIntent.getActivity(
                    ctx,
                    docId.hashCode(),
                    detailIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 10) Construcción y envío de la notificación
                val notifBuilder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoxajo)
                    .setContentTitle(title)
                    .setContentText(notifText)
                    .setContentIntent(detailPI)
                    .setAutoCancel(true)

                (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(docId.hashCode(), notifBuilder.build())

                pendingResult.finish()
            }
            .addOnFailureListener {
                pendingResult.finish()
            }
    }

    private fun createNotificationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name        = ctx.getString(R.string.channel_name)
            val description = ctx.getString(R.string.channel_description)
            val importance  = NotificationManager.IMPORTANCE_HIGH
            val channel     = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
