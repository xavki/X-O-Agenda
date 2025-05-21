package com.institutmarianao.xo_agenda.alertas

import android.content.Intent
import android.os.Bundle
import com.institutmarianao.xo_agenda.AlertFragment
import com.institutmarianao.xo_agenda.R
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity


object NavigationIntentHandler {


    fun handleNavigationIntent(activity: AppCompatActivity, intent: Intent) {
        if (intent.getStringExtra("navigateTo") == "alerts") {
            // 1) Lee los extras que pusiste en ReminderReceiver
            val docId = intent.getStringExtra("docId")
            val title = intent.getStringExtra("titol").orEmpty()
            val desc = intent.getStringExtra("descripcio").orEmpty()
            val alertType = intent.getStringExtra("alertType")
            val extraInfo = intent.getStringExtra("extraInfo")

            // 2) Pásalos al fragment en el Bundle
            val frag = AlertFragment().apply {
                arguments = Bundle().apply {
                    putString("docId", docId)
                    putString("titol", title)
                    putString("descripcio", desc)
                    putString("alertType", alertType)
                    putString("extraInfo", extraInfo)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, frag)
                .addToBackStack(null)
                .commit()

            // 3) Construye el mensaje según el tipo
            val message = when (alertType) {
                "evento" -> "$title\n$desc\n📅 Inici: ${extraInfo.orEmpty()}"
                "tasca" -> "$title\n$desc\n📌 Estat: ${extraInfo.orEmpty()}"
                else -> title.ifEmpty { desc.ifEmpty { "Tienes un recordatorio" } }
            }

            // 4) Muestra el Snackbar arriba
            val root = activity.findViewById<View>(R.id.container_fragment)
            val snack = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE)
            (snack.view.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = Gravity.TOP
                snack.view.layoutParams = this
            }
            snack.setAction("Cerrar") { snack.dismiss() }
            snack.show()
            Handler(Looper.getMainLooper()).postDelayed({ snack.dismiss() }, 5000)
        }
    }
}
