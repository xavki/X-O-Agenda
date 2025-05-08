package com.institutmarianao.xo_agenda

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                    mostrarDialogAfegirTasca()
                    /* val tascaDialog = AlertDialog.Builder(requireContext())
                         .setTitle("Nova Tasca")
                         .setMessage("Aquí pots afegir una nova tasca.")
                         .setPositiveButton("OK") { dialog2, _ ->
                             dialog2.dismiss()
                         }
                         .create()
                     tascaDialog.show()*/
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

    fun mostrarDialogAfegirTasca() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_afegir_tasca, null)

        val editTextTitol = dialogView.findViewById<EditText>(R.id.editTextTitol)
        val editTextDescripcio = dialogView.findViewById<EditText>(R.id.editTextDescripcio)
        val textViewDataLimit = dialogView.findViewById<TextView>(R.id.textViewDataLimit)
        val textViewRecordatori = dialogView.findViewById<TextView>(R.id.textviewRecordatori)
        val buttonGuardar = dialogView.findViewById<Button>(R.id.buttonGuardarTasca)

        val estats = listOf("Pendent", "En_Proces", "Completada")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estats)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        var dataLimitSeleccionada: Timestamp? = null
        var recordatoriSeleccionat: Timestamp? = null
        val calendar = Calendar.getInstance()

        // Selección de data límit
        textViewDataLimit.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            textViewDataLimit.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                            dataLimitSeleccionada = Timestamp(calendar.time)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePicker.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Selección de recordatori
        textViewRecordatori.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            textViewRecordatori.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                            recordatoriSeleccionat = Timestamp(calendar.time)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePicker.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val dialog = builder.setView(dialogView)
            .setTitle("Nova Tasca")
            .create()

        buttonGuardar.setOnClickListener {
            val titol = editTextTitol.text.toString().trim()
            val descripcio = editTextDescripcio.text.toString().trim()
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (titol.isEmpty() || dataLimitSeleccionada == null) {
                Toast.makeText(requireContext(), "Omple el títol i la data límit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (recordatoriSeleccionat != null && recordatoriSeleccionat!! > dataLimitSeleccionada!!) {
                Toast.makeText(requireContext(), "El recordatori no pot ser després de la data límit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uid != null) {
                val db = FirebaseFirestore.getInstance()
                val tasca = hashMapOf(
                    "titol" to titol,
                    "descripció" to descripcio,
                    "data_limit" to dataLimitSeleccionada,
                    "recordatori" to recordatoriSeleccionat
                )

                db.collection("usuarios")
                    .document(uid)
                    .collection("tasques")
                    .add(tasca)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tasca guardada", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()
    }


}
