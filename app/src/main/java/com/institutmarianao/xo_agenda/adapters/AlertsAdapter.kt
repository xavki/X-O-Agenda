package com.institutmarianao.xo_agenda.adapters

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.institutmarianao.xo_agenda.R
import com.institutmarianao.xo_agenda.models.AlertItem

class AlertsAdapter(
    context: Context,
    private val items: MutableList<AlertItem>,
    prefsName: String = "alerts_prefs"
) : ArrayAdapter<AlertItem>(context, 0, items) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    init {
        // Inicializa el estado desde SharedPreferences
        items.forEach {
            it.isRead = prefs.getBoolean(it.id, false)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = items[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_alert, parent, false)

        val tvTitle = view.findViewById<TextView>(R.id.tvAlertTitle)
        val tvDesc = view.findViewById<TextView>(R.id.tvAlertDesc)
        val ivStatus = view.findViewById<ImageView>(R.id.ivStatus)

        // Siempre oculto, sólo título y descripción
        view.findViewById<TextView>(R.id.tvAlertExtra)?.visibility = View.GONE



        // Texto en negrita si no leído, normal si leído
        if (item.isRead) {
            // leído: normal + gris + check
            tvTitle.setTypeface(null, Typeface.NORMAL)
            tvTitle.setTextColor(Color.GRAY)
            ivStatus.setImageResource(R.drawable.ic_check)
        } else {
            // pendiente: negrita + negro + punto
            tvTitle.setTypeface(null, Typeface.BOLD)
            tvTitle.setTextColor(Color.BLACK)
            ivStatus.setImageResource(R.drawable.ic_unread)
        }

        tvTitle.text = item.title
        tvDesc.text = item.desc



        return view
    }


    fun markRead(position: Int) {
        // 1) Validamos posición
        if (position == RecyclerView.NO_POSITION || position < 0 || position >= items.size) return

        // 2) Actualizamos estado
        val item = items[position]
        if (!item.isRead) {
            item.isRead = true
            prefs.edit().putBoolean(item.id, true).apply()
            notifyDataSetChanged()
        }
    }

    fun markAllRead() {
        items.forEach {
            if (!it.isRead) {
                it.isRead = true
                prefs.edit().putBoolean(it.id, true).apply()
            }
        }
        notifyDataSetChanged()
    }
}
