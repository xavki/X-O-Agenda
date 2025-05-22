package com.institutmarianao.xo_agenda.alertas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.institutmarianao.xo_agenda.AlertFragment
import com.institutmarianao.xo_agenda.R

object NavigationIntentHandler {

    fun handleNavigationIntent(activity: AppCompatActivity, intent: Intent) {
        // 1) Si viene de la notificación genérica de alertas, muestro el fragment sin extras:
        if (intent.getStringExtra("navigateTo") == "alerts") {
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, AlertFragment())
                .addToBackStack(null)
                .commit()
            return
        }
    }

        /*
        // 2) En los demás casos, recogemos los extras de alerta individual:
        val docId     = intent.getStringExtra("docId")
        val title     = intent.getStringExtra("titol").orEmpty()
        val desc      = intent.getStringExtra("descripció").orEmpty()
        val alertType = intent.getStringExtra("alertType")
        val extraInfo = intent.getStringExtra("extraInfo")

        // 3) Creamos el fragment con esos argumentos
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

        // 4) Construimos el mensaje según el tipo de alerta
        val message = when (alertType) {
            "evento" -> "$title\n$desc\n📅 Inici: ${extraInfo.orEmpty()}"
            "tasca"  -> "$title\n$desc\n📌 Estat: ${extraInfo.orEmpty()}"
            else     -> title.ifEmpty { desc.ifEmpty { "Tienes un recordatorio" } }
        }

        // 5) Mostramos un Snackbar arriba con el detalle
        val root = activity.findViewById<View>(R.id.container_fragment)
        val snack = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE)
        (snack.view.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.TOP
            snack.view.layoutParams = this
        }
        snack.setAction("Cerrar") { snack.dismiss() }
        snack.show()
        Handler(Looper.getMainLooper()).postDelayed({ snack.dismiss() }, 5000)
    }*/
}
