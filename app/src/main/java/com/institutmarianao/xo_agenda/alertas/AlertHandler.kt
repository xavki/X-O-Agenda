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
            // 1) Reemplazar el fragment
            val frag = AlertFragment().apply {
                arguments = Bundle().apply {
                    putString("docId", intent.getStringExtra("docId"))
                    putString("titol", intent.getStringExtra("titol"))
                    putString("descripcio", intent.getStringExtra("descripcio"))
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, frag)
                .addToBackStack(null)
                .commit()

            // 2) Obtener el mensaje
            val message = intent.getStringExtra("titol")
                ?: intent.getStringExtra("descripcio")
                ?: "Tienes un recordatorio"

            // 3) Mostrar la alerta con Snackbar
            val root: View = activity.findViewById(R.id.container_fragment)
            val snack = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE)

            // Ponerlo arriba
            val snackView = snack.view
            val params = snackView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackView.layoutParams = params

            // Botón “Cerrar”
            snack.setAction("Cerrar") { snack.dismiss() }

            // Mostrar + auto-dismiss en 5s
            snack.show()
            Handler(Looper.getMainLooper()).postDelayed({
                snack.dismiss()
            }, 5000)
        }
    }


}
