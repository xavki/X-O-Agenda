// PermisosHandler.kt
package com.institutmarianao.xo_agenda.alertas

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.institutmarianao.xo_agenda.MenuActivity.Companion.REQ_NOTIF

object PermisosHandler {
    fun handlePermissionsResult(
        activity: AppCompatActivity,
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_NOTIF) {
            if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    activity,
                    "Sin permiso de notificaciones, los recordatorios no se mostrarán",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

