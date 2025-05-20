package com.institutmarianao.xo_agenda.alertas

import android.content.Context
import android.content.SharedPreferences
import com.institutmarianao.xo_agenda.models.AlertItem

object AlertRepository {
    private const val PREFS_NAME      = "alerts_prefs"
    private const val KEY_ALERT_IDS   = "all_alert_ids"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Añade una alerta al repositorio */
    fun addAlert(ctx: Context, alert: AlertItem) {
        val p = prefs(ctx)
        val ids = p.getStringSet(KEY_ALERT_IDS, emptySet())!!.toMutableSet()
        ids.add(alert.id)
        p.edit()
            .putStringSet(KEY_ALERT_IDS, ids)
            .putString("alert_${alert.id}_title", alert.title)
            .putString("alert_${alert.id}_desc", alert.desc)
            .apply()
    }

    /** Elimina una alerta (por ejemplo al marcarla leída) */
    fun removeAlert(ctx: Context, id: String) {
        val p = prefs(ctx)
        val ids = p.getStringSet(KEY_ALERT_IDS, emptySet())!!
        p.edit()
            .putStringSet(KEY_ALERT_IDS, ids - id)
            .remove("alert_${id}_title")
            .remove("alert_${id}_desc")
            .apply()
    }

    /** Devuelve todas las alertas pendientes (sin filtrar leído/no leído) */
    fun getAllPendingAlerts(ctx: Context): MutableList<AlertItem> {
        val p   = prefs(ctx)
        val ids = p.getStringSet(KEY_ALERT_IDS, emptySet()) ?: emptySet()
        val out = mutableListOf<AlertItem>()
        for (id in ids) {
            val title = p.getString("alert_${id}_title", "") ?: ""
            val desc  = p.getString("alert_${id}_desc", "") ?: ""
            out += AlertItem(id, title, desc, isRead = false)
        }
        return out
    }
}
