package com.institutmarianao.xo_agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment

@Suppress("UNREACHABLE_CODE")
class AlertFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alerts, container, false)

        // Botón para abrir el menú lateral
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)

        btnOpenMenu.setOnClickListener {
            // Llama al método público de la actividad para abrir el drawer
            (activity as? MenuActivity)?.openDrawer()
        }
        return view

        // 1) Recupera el ListView
        val listView = view.findViewById<ListView>(R.id.listViewAlerts)

        // 2) Lee los datos de la notificación (pasados en los extras del Intent)
        //    Aquí pedimos los argumentos que metiste en el PendingIntent:
        val title = arguments?.getString("titol") ?: "Sin título"
        val desc  = arguments?.getString("descripcio") ?: ""

        // 3) Crea la lista de strings que vas a mostrar
        //    Si quisieras varios avisos, aquí podrías cargar un array de Firestore
        val items = mutableListOf<String>()
        items.add("$title\n$desc")

        // 4) Pon un ArrayAdapter sobre el ListView
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            items
        )
        listView.adapter = adapter

        // 5) (Opcional) Si quisieras reaccionar a clicks:
        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(requireContext(),
                "Pulsaste: ${items[position]}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}
