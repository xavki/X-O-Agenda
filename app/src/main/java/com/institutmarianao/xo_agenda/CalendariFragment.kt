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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.institutmarianao.xo_agenda.adapters.CalendarItemAdapter
import com.institutmarianao.xo_agenda.models.CalendarItem
import java.util.Locale
import com.institutmarianao.xo_agenda.adapters.OnItemActionListener




class CalendariFragment : Fragment(), OnItemActionListener {

    lateinit var calendarItems: MutableList<CalendarItem>
    lateinit var adapter: CalendarItemAdapter
    lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
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


        //mostrar tareas y eventos
        calendarItems = mutableListOf()
        adapter = CalendarItemAdapter(calendarItems, this)
        recyclerView = view.findViewById(R.id.recyclerViewCalendarItems)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cargareventosytareas(calendarItems, adapter)


        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("'Día' EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(today.time)
        txtDay.text =
            formattedDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Solo permite seleccionar a partir de hoy
            calendarView.minDate = Calendar.getInstance().timeInMillis
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

            val options = arrayOf("Tasca", "Esdeveniment")

            builder.setSingleChoiceItems(options, -1) { dialog, which ->
                dialog.dismiss() // Cierra el primer diálogo

                when (which) {
                    0 -> { // Tasca
                        mostrarDialogAfegirTasca()
                    }

                    1 -> { // Esdeveniments
                        mostrarDialogAfegirEvent()
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
                            textViewDataLimit.text =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                    calendar.time
                                )
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
            datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
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
                            textViewRecordatori.text =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                    calendar.time
                                )
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
                Toast.makeText(
                    requireContext(),
                    "Omple el títol i la data límit",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (recordatoriSeleccionat != null && recordatoriSeleccionat!! > dataLimitSeleccionada!!) {
                Toast.makeText(
                    requireContext(),
                    "El recordatori no pot ser després de la data límit",
                    Toast.LENGTH_SHORT
                ).show()
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
                        Toast.makeText(requireContext(), "Tasca guardada", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialog.show()
    }

    fun mostrarDialogAfegirEvent() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_afegir_esdeveniment, null)

        val editTextTitol = dialogView.findViewById<EditText>(R.id.editTextTitol)
        val editTextDescripcio = dialogView.findViewById<EditText>(R.id.editTextDescripcio)
        val textViewDataLimit = dialogView.findViewById<TextView>(R.id.textViewDataLimit)
        val textViewFinalitzacio = dialogView.findViewById<TextView>(R.id.textviewFinalitzacio)
        val textViewRecordatori = dialogView.findViewById<TextView>(R.id.textviewRecordatori)
        val buttonGuardar = dialogView.findViewById<Button>(R.id.buttonGuardarEsdeveniment)

        val estats = listOf("Pendent", "En_Proces", "Completada")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estats)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        var dataIniciSeleccionada: Timestamp? = null
        var dataFinalSeleccionada: Timestamp? = null
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
                            textViewDataLimit.text =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                    calendar.time
                                )
                            dataIniciSeleccionada = Timestamp(calendar.time)
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
            datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePicker.show()
        }

        // SELECCIO DATA FINALITZACIÓ
        textViewFinalitzacio.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            textViewFinalitzacio.text =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                    calendar.time
                                )
                            dataFinalSeleccionada = Timestamp(calendar.time)
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
            datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
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
                            textViewRecordatori.text =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                                    calendar.time
                                )
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

            if (titol.isEmpty() || dataIniciSeleccionada == null || dataFinalSeleccionada == null) {
                Toast.makeText(
                    requireContext(),
                    "Omple el títol, la data límit i la data de finalització",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (recordatoriSeleccionat != null && recordatoriSeleccionat!! > dataIniciSeleccionada!!) {
                Toast.makeText(
                    requireContext(),
                    "El recordatori no pot ser després de la data límit",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (dataFinalSeleccionada != null && dataFinalSeleccionada!! < dataIniciSeleccionada!!) {
                Toast.makeText(
                    requireContext(),
                    "La data de finalització ha de ser despres de la data d'inici",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (uid != null) {
                val db = FirebaseFirestore.getInstance()
                val tasca = hashMapOf(
                    "titol" to titol,
                    "descripció" to descripcio,
                    "data_inici" to dataIniciSeleccionada,
                    "data_fi" to dataFinalSeleccionada,
                    "recordatori" to recordatoriSeleccionat
                )

                db.collection("usuarios")
                    .document(uid)
                    .collection("esdeveniments")
                    .add(tasca)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Esdeveniment guardat", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialog.show()
    }

    private fun cargareventosytareas(
        calendarItems: MutableList<CalendarItem>,
        adapter: CalendarItemAdapter
    ) {
        val db  = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        calendarItems.clear()
        // formato para todas las fechas
        val fmt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

        fun procesarDocumentos(docs: QuerySnapshot, tipo: String) {
            for (doc in docs) {
                val id          = doc.id
                val title       = doc.getString("titol") ?: ""
                val description = doc.getString("descripció") ?: ""

                if (tipo == "Tasca") {
                    // Para tareas, usamos solo data_limit
                    val ts = doc.getTimestamp("data_limit") ?: continue
                    val fecha = ts.toDate()
                    val dateTime = fmt.format(fecha)

                    calendarItems.add(
                        CalendarItem(id, title, description, dateTime, tipo, fecha)
                    )
                } else {
                    // Para eventos, mostramos inicio - fin
                    val tsStart = doc.getTimestamp("data_inici") ?: continue
                    val tsEnd   = doc.getTimestamp("data_fi")    ?: tsStart

                    val fechaStart = tsStart.toDate()
                    val fechaEnd   = tsEnd.toDate()

                    val dateTime = "${fmt.format(fechaStart)} - ${fmt.format(fechaEnd)}"
                    // Ordenamos por la fecha de inicio
                    calendarItems.add(
                        CalendarItem(id, title, description, dateTime, tipo, fechaStart)
                    )
                }
            }

            // Ordenar por fecha de ordenación y refrescar
            calendarItems.sortBy { it.fechaOrdenacion }
            adapter.notifyDataSetChanged()
        }

        // Cargar tareas primero...
        db.collection("usuarios").document(uid).collection("tasques")
            .get().addOnSuccessListener { procesarDocumentos(it, "Tasca")
                // ...luego eventos
                db.collection("usuarios").document(uid).collection("esdeveniments")
                    .get().addOnSuccessListener { procesarDocumentos(it, "Esdeveniment") }
            }
    }


    // Stub: modifica los campos de la tasca y haz un .update() en Firestore
    private fun mostrarDialogEditarTasca(item: CalendarItem) {
        // TODO: inflar dialog_afegir_tasca,
        //       precargar editTextTitol.setText(item.title), …
        //       y al guardar -> db.collection(".../tasques").document(item.id).update(…)
    }

    // Stub: modifica los campos de l’esdeveniment y haz un .update() en Firestore
    private fun mostrarDialogEditarEvent(item: CalendarItem) {
        // TODO: inflar dialog_afegir_esdeveniment,
        //       precargar, y al guardar -> db.collection(".../esdeveniments").document(item.id).update(…)
    }

    override fun onDelete(item: CalendarItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Borrar ${item.tipo}")
            .setMessage("¿Seguro que deseas borrar “${item.title}”?")
            .setPositiveButton("Sí") { _, _ ->
                val path = if (item.tipo == "Tasca") "tasques" else "esdeveniments"
                FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(FirebaseAuth.getInstance().uid!!)
                    .collection(path)
                    .document(item.id)
                    .delete()
                    .addOnSuccessListener {
                        // Actualiza la lista en memoria y notifica al adapter
                        val idx = calendarItems.indexOf(item)
                        calendarItems.removeAt(idx)
                        recyclerView.adapter?.notifyItemRemoved(idx)
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }


    override fun onEdit(item: CalendarItem) {
        if (item.tipo == "Tasca") {
            mostrarDialogEditarTasca(item)
        } else {
            mostrarDialogEditarEvent(item)
        }
    }

}
