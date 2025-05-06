package com.institutmarianao.xo_agenda

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class CalendariFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Botón para abrir el menú lateral
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)
        val anadir = view.findViewById<ImageButton>(R.id.btnanadir)

        btnOpenMenu.setOnClickListener {
            // Llama al método público de la actividad para abrir el drawer
            (activity as? MenuActivity)?.openDrawer()
        }
        val calendarView = view.findViewById<CalendarView>(R.id.calendar)
        val txtDay = view.findViewById<TextView>(R.id.txtDay)

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("'Día' EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(today.time)
        txtDay.text =
            formattedDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Calendar usa 0-based months, por eso sumamos 1
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            val dateFormat =
                SimpleDateFormat("'Dia' EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            val formattedDate = dateFormat.format(calendar.time)

            txtDay.text =
                formattedDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        }

        anadir.setOnClickListener {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona una opció")

        val options = arrayOf("Tasca", "Esdeveniments")

        builder.setSingleChoiceItems(options, -1) { dialog, which ->
            dialog.dismiss() // Cierra el primer diálogo

            when (which) {
                0 -> { // Tasca
                    val tascaDialog = AlertDialog.Builder(requireContext())
                        .setTitle("Nova Tasca")
                        .setMessage("Aquí pots afegir una nova tasca.")
                        .setPositiveButton("OK") { dialog2, _ ->
                            dialog2.dismiss()
                        }
                        .create()
                    tascaDialog.show()
                }

                1 -> { // Esdeveniments
                    val eventDialog = AlertDialog.Builder(requireContext())
                        .setTitle("Nou Esdeveniment")
                        .setMessage("Aquí pots afegir un nou esdeveniment.")
                        .setPositiveButton("OK") { dialog2, _ ->
                            dialog2.dismiss()
                        }
                        .create()
                    eventDialog.show()
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
        return view
    }
}
