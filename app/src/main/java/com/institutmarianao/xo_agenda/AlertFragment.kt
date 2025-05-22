package com.institutmarianao.xo_agenda

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.institutmarianao.xo_agenda.adapters.AlertsAdapter
import com.institutmarianao.xo_agenda.alertas.AlertRepository
import com.institutmarianao.xo_agenda.models.AlertItem

class AlertFragment : Fragment() {

    private var snack: Snackbar? = null

    private val prefs by lazy {
        requireContext().getSharedPreferences("alerts_prefs", Context.MODE_PRIVATE)
    }

    private fun isRead(id: String) = prefs.getBoolean(id, false)
    private fun markReadPrefs(id: String) {
        prefs.edit().putBoolean(id, true).apply()
    }

    // Estas propiedades contendrán los valores del Bundle
    private lateinit var docId: String
    private lateinit var title: String
    private lateinit var desc: String

    private lateinit var adapter: AlertsAdapter
    private lateinit var alerts: MutableList<AlertItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_alerts, container, false)

        // Abrir drawer
        view.findViewById<ImageView>(R.id.btnOpenMenu).setOnClickListener {
            (activity as? MenuActivity)?.openDrawer()
        }
        // 2) Cargar lista de alertas:
        //    - Si vengo de notificación, uso sólo esa alerta
        //    - Si vengo del menú, cargo todas las pendientes
        arguments?.let { args ->
            val id = args.getString("docId")!!
            val title = args.getString("titol")!!
            val desc = args.getString("descripcio")!!
            val type = args.getString("alertType")    // puede ser "evento" o "tasca"
            val extraInfo = args.getString("extraInfo")    // fechaInici o estat

            // Creamos la alerta **con** type y extraInfo
            alerts = mutableListOf(
                AlertItem(
                    id = id,
                    title = title,
                    desc = desc,
                    isRead = isRead(id),
                    type = type,
                    extraInfo = extraInfo
                )
            )
        } ?: run {
            alerts = AlertRepository
                .getAllPendingAlerts(requireContext())
                .filter { !isRead(it.id) }        // sólo sin leer
                .map { it.apply { isRead = false } }
                .toMutableList()
            adapter = AlertsAdapter(requireContext(), alerts)
        }

        // 3) Preparo el adapter
        adapter = AlertsAdapter(requireContext(), alerts)

        // 4) Configuro ListView y click individual
        view.findViewById<ListView>(R.id.listViewAlerts).apply {
            adapter = this@AlertFragment.adapter
            setOnItemClickListener { _, _, pos, _ ->
                val id = alerts[pos].id
                // 1) Persiste en prefs
                prefs.edit().putBoolean(id, true).apply()
                // 2) Quita del repositorio
                AlertRepository.removeAlert(requireContext(), id)
                // 3) Elimina el ítem de la lista en memoria
                alerts.removeAt(pos)
                // 4) Notifica al adapter para refrescar
                (adapter as AlertsAdapter).notifyDataSetChanged()
            }
        }


        // 5) Botón “Marcar todas como leídas”
        view.findViewById<ImageView>(R.id.btnMarkAllRead).setOnClickListener {
            alerts.forEach {
                prefs.edit().putBoolean(it.id, true).apply()
                AlertRepository.removeAlert(requireContext(), it.id)
            }
            alerts.clear()
            adapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        // 6) Si quedó alguna sin leer, muestro SnackBar indefinido
        val root = view ?: return
        alerts.firstOrNull { !it.isRead }?.let { item ->
            snack?.dismiss()
            val builder = StringBuilder()
            builder.append(item.title).append("\n")
            builder.append(item.desc)
            item.extraInfo?.let { info ->
                when (item.type) {
                    "evento" -> builder.append("\n\n📅 Inici: ").append(info)
                    "tasca" -> builder.append("\n\n📌 Estat: ").append(info)
                    else -> {}
                }
            }
            val msg = builder.toString()
            snack = Snackbar
                .make(root, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Marcar leído") {
                    // marco la primera pendiente
                    markReadPrefs(item.id)
                    adapter.markRead(alerts.indexOf(item))
                    AlertRepository.removeAlert(requireContext(), item.id)
                    snack?.dismiss()
                }
            snack?.show()
        }
    }

    override fun onPause() {
        super.onPause()
        snack?.dismiss()
    }
}





