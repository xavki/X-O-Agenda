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
            // 1) Lee alertType y extraInfo del Intent
            val alertType = intent.getStringExtra("alertType")
            val extraInfo = intent.getStringExtra("extraInfo")

            val frag = AlertFragment().apply {
                arguments = Bundle().apply {
                    putString("docId",      intent.getStringExtra("docId"))
                    putString("titol",      intent.getStringExtra("titol"))
                    putString("descripcio", intent.getStringExtra("descripcio"))
                    putString("alertType",  alertType)    // ← Aquí debe llegar no-null
                    putString("extraInfo",  extraInfo)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, frag)
                .addToBackStack(null)
                .commit()

            // 3) Construir mensaje dinámico
            val title = intent.getStringExtra("titol").orEmpty()
            val desc = intent.getStringExtra("descripcio").orEmpty()
            val message = when (alertType) {
                "evento" -> "$title\n$desc\n📅 Inici: ${extraInfo.orEmpty()}"
                "tasca" -> "$title\n$desc\n📌 Estat: ${extraInfo.orEmpty()}"
                else -> title.ifEmpty { desc.ifEmpty { "Tienes un recordatorio" } }
            }

            // 4) Mostrar Snackbar arriba
            val root: View = activity.findViewById(R.id.container_fragment)
            val snack = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE)
            val snackView = snack.view as FrameLayout
            (snackView.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = Gravity.TOP
                snackView.layoutParams = this
            }
            snack.setAction("Cerrar") { snack.dismiss() }
            snack.show()

            // 5) Auto-dismiss en 5s
            Handler(Looper.getMainLooper()).postDelayed({ snack.dismiss() }, 5000)
        }
    }
}
