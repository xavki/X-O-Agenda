package com.institutmarianao.xo_agenda

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.institutmarianao.xo_agenda.adapters.CalendarItemAdapter
import com.institutmarianao.xo_agenda.adapters.OnItemActionListener
import com.institutmarianao.xo_agenda.models.CalendarItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.Date
import java.util.Locale

class CalendariFragment : Fragment(), OnItemActionListener {

    lateinit var calendarItems: MutableList<CalendarItem>
    lateinit var adapter: CalendarItemAdapter
    lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormat =
        SimpleDateFormat(
            "'Día' EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES")
        )


    private lateinit var txtDay: TextView
    private val allEventDays = mutableSetOf<CalendarDay>()
    private lateinit var calendarView: MaterialCalendarView
    private val calendarInici = Calendar.getInstance()
    private val calendarFi = Calendar.getInstance()
    private val calendarFinal = Calendar.getInstance()
    private val calendarRecord = Calendar.getInstance()
    private var dataIniciSeleccionada: Timestamp? = null
    private var dataFinalSeleccionada: Timestamp? = null
    private var recordatoriSeleccionat: Timestamp? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializa calendarItems aquí, dentro del cuerpo:
        calendarItems = mutableListOf()

        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Botón menú
        view.findViewById<ImageView>(R.id.btnOpenMenu).apply {
            setOnClickListener { (activity as? MenuActivity)?.openDrawer() }
        }
        // Botón añadir
        view.findViewById<ImageButton>(R.id.btnanadir).setOnClickListener {
            mostrarOpcionesAgregar()
        }

        // Referencias principales
        calendarView = view.findViewById(R.id.calendar)
        txtDay = view.findViewById(R.id.txtDay)
        recyclerView = view.findViewById(R.id.recyclerViewCalendarItems)

        // RecyclerView
        adapter = CalendarItemAdapter(calendarItems, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 1) Carga global de fechas para decorar
        loadEventDays()

        // 2) Listener DE LISTADO (no toca decoradores)
        calendarView.setOnDateChangedListener { _, date, _ ->
            val cal = Calendar.getInstance().apply {
                time = date.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            txtDay.text = dateFormat.format(cal.time)
            cargareventosytareas(cal)
        }

        // 3) Carga inicial de hoy
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        txtDay.text = dateFormat.format(today.time)
        cargareventosytareas(selectedDate)

        return view
    }


    private fun cargareventosytareas(filterDate: Calendar) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Rango día
        val startOfDay = filterDate.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0); startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0); startOfDay.set(Calendar.MILLISECOND, 0)
        val endOfDay = filterDate.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23); endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59); endOfDay.set(Calendar.MILLISECOND, 999)

        calendarItems.clear()

        fun procesar(docs: QuerySnapshot, tipo: String) {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
            for (doc in docs) {
                val ts = if (tipo == "Tasca")
                    doc.getTimestamp("data_limit")
                else
                    doc.getTimestamp("data_inici")
                ts?.toDate()?.takeIf { it in startOfDay.time..endOfDay.time }?.let { itemDate ->
                    val fechaText = if (tipo == "Tasca") {
                        fmt.format(itemDate)
                    } else {
                        val tsEnd = doc.getTimestamp("data_fi")?.toDate() ?: itemDate
                        "${fmt.format(itemDate)} - ${fmt.format(tsEnd)}"
                    }
                    calendarItems.add(
                        CalendarItem(
                            doc.id,
                            doc.getString("titol") ?: "",
                            doc.getString("descripció") ?: "",
                            fechaText,
                            tipo,
                            itemDate
                        )
                    )
                }
            }
            calendarItems.sortBy { it.fechaOrdenacion }
            adapter.notifyDataSetChanged()
        }

        db.collection("usuarios").document(uid).collection("tasques")
            .whereGreaterThanOrEqualTo("data_limit", startOfDay.time)
            .whereLessThanOrEqualTo("data_limit", endOfDay.time)
            .get().addOnSuccessListener { procesar(it, "Tasca") }

        db.collection("usuarios").document(uid).collection("esdeveniments")
            .whereGreaterThanOrEqualTo("data_inici", startOfDay.time)
            .whereLessThanOrEqualTo("data_inici", endOfDay.time)
            .get().addOnSuccessListener { procesar(it, "Esdeveniment") }
    }

    private fun loadEventDays() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1) Tareas
        db.collection("usuarios").document(uid).collection("tasques")
            .get().addOnSuccessListener { snap ->
                snap.documents.forEach { doc ->
                    doc.getTimestamp("data_limit")?.toDate()?.let { date ->
                        val cal = Calendar.getInstance().apply {
                            time = date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        allEventDays += CalendarDay.from(cal.time)
                    }
                }
                // tras la primera carga
                calendarView.addDecorator(EventDecorator(Color.RED, allEventDays.toList()))
                calendarView.invalidateDecorators()
            }

        // 2) Eventos
        db.collection("usuarios").document(uid).collection("esdeveniments")
            .get().addOnSuccessListener { snap ->
                snap.documents.forEach { doc ->
                    doc.getTimestamp("data_inici")?.toDate()?.let { date ->
                        val cal = Calendar.getInstance().apply {
                            time = date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        allEventDays += CalendarDay.from(cal.time)
                    }
                }
                // refrescamos con el set completo
                calendarView.removeDecorators()
                calendarView.addDecorator(EventDecorator(Color.RED, allEventDays.toList()))
                calendarView.invalidateDecorators()
            }
    }

    // --------------------------------------------------
    private fun mostrarOpcionesAgregar() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona una opción")
        val options = arrayOf("Tasca", "Esdeveniment")
        builder.setSingleChoiceItems(options, -1) { dialog, which ->
            dialog.dismiss()
            if (which == 0) mostrarDialogAfegirTasca() else mostrarDialogAfegirEvent()
        }
        builder.setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
        builder.show()
    }

    @SuppressLint("MissingInflatedId")
    fun mostrarDialogAfegirTasca() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_afegir_tasca, null)

        val editTextTitol = dialogView.findViewById<EditText>(R.id.editTextTitol)
        val editTextDescripcio = dialogView.findViewById<EditText>(R.id.editTextDescripcio)
        val textViewDataLimit = dialogView.findViewById<TextView>(R.id.textViewDataLimit)
        val textViewRecordatori = dialogView.findViewById<TextView>(R.id.textviewRecordatori)
        val buttonGuardar = dialogView.findViewById<Button>(R.id.buttonGuardarTasca)

        val spinnerEstat = dialogView.findViewById<Spinner>(R.id.spinnerEstat)
        spinnerEstat.visibility = View.GONE
        val labelEstat = dialogView.findViewById<TextView>(R.id.textviewEstado)
        labelEstat.visibility = View.GONE


        var dataLimitSeleccionada: Timestamp? = null
        var recordatoriSeleccionat: Timestamp? = null
        val calendar = Calendar.getInstance()

        // Selección de data límit
        textViewDataLimit.setOnClickListener {
            requireContext().showDateTimePicker(
                minDate = System.currentTimeMillis()
            ) { date ->
                textViewDataLimit.text = dateFormat.format(date)
                dataLimitSeleccionada = Timestamp(date)
            }
        }

        // Selección de recordatori
        textViewRecordatori.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendar,
                minDate = System.currentTimeMillis()
            ) { date ->
                calendar.time = date
                textViewRecordatori.text = dateFormat.format(date)
                recordatoriSeleccionat = Timestamp(date)
            }
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
                    .addOnSuccessListener { docRef ->
                        mostrarToast("Tasca guardada")
                        dialog.dismiss()
                        cargareventosytareas(selectedDate)

                        recordatoriSeleccionat?.let { ts ->
                            rescheduleAlarm(
                                docId = docRef.id,
                                type = "tasques",
                                title = titol,
                                desc = descripcio,
                                triggerAt = ts.toDate().time
                            )
                        }
                    }
                    .addOnFailureListener {
                        mostrarToast("Error al guardar")
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
        val textDataLimit = dialogView.findViewById<TextView>(R.id.textViewDataLimit)
        val textFinalitzacio = dialogView.findViewById<TextView>(R.id.textviewFinalitzacio)
        val textRecordatori = dialogView.findViewById<TextView>(R.id.textviewRecordatori)
        val buttonGuardar = dialogView.findViewById<Button>(R.id.buttonGuardarEsdeveniment)


        var dataIniciSeleccionada: Timestamp? = null
        var dataFinalSeleccionada: Timestamp? = null
        var recordatoriSeleccionat: Timestamp? = null
        val calendar = Calendar.getInstance()

        // Selección de data límit
        textDataLimit.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendarInici,
                minDate = Calendar.getInstance().timeInMillis
            ) { date ->
                calendarInici.time = date
                textDataLimit.text = dateFormat.format(date)
                dataIniciSeleccionada = Timestamp(date)
            }
        }

        // Selección de data finalització
        textFinalitzacio.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendarFi,
                // opcional: que no sea anterior a la data d'inici
                minDate = dataIniciSeleccionada?.toDate()?.time
                    ?: Calendar.getInstance().timeInMillis
            ) { date ->
                calendarFi.time = date
                textFinalitzacio.text = dateFormat.format(date)
                dataFinalSeleccionada = Timestamp(date)
            }
        }

        // Selección de recordatori
        textRecordatori.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendar
            ) { date ->
                calendar.time = date
                textRecordatori.text = dateFormat.format(date)
                recordatoriSeleccionat = Timestamp(date)
            }
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
                    .addOnSuccessListener { docRef ->
                        mostrarToast("Esdeveniment guardat")
                        dialog.dismiss()
                        cargareventosytareas(selectedDate)

                        recordatoriSeleccionat?.let { ts ->
                            // Llama a tu helper y ya se encarga de cancelar/la alarma
                            rescheduleAlarm(
                                docId = docRef.id,
                                type = "esdeveniments",
                                title = titol,
                                desc = descripcio,
                                triggerAt = ts.toDate().time
                            )
                        }
                    }
                    .addOnFailureListener {
                        mostrarToast("Error al guardar")
                    }
            }
        }

        dialog.show()
    }

    override fun onEdit(item: CalendarItem) {
        if (item.tipo == "Tasca") {
            mostrarDialogEditarTasca(item)
        } else {
            mostrarDialogEditarEvent(item)
        }
    }

    private fun mostrarDialogEditarTasca(item: CalendarItem) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_afegir_tasca, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        // vistas
        val edtTitol = dialogView.findViewById<EditText>(R.id.editTextTitol)
        val edtDesc = dialogView.findViewById<EditText>(R.id.editTextDescripcio)
        val txtDataLimit = dialogView.findViewById<TextView>(R.id.textViewDataLimit)
        val txtRecord = dialogView.findViewById<TextView>(R.id.textviewRecordatori)
        val spinnerEstat = dialogView.findViewById<Spinner>(R.id.spinnerEstat)
        val btnGuardar = dialogView.findViewById<Button>(R.id.buttonGuardarTasca)

        var dataLimitSeleccionada: Timestamp? = null
        var recordatoriSeleccionat: Timestamp? = null
        val calendar = Calendar.getInstance()

        // Spinner
        val estados = listOf("En Proces", "Completada", "Pendiente")
        spinnerEstat.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            estados
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        //cargamos el documento
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val docRef = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection("tasques")
            .document(item.id)

        docRef.get().addOnSuccessListener { doc ->
            edtTitol.setText(doc.getString("titol"))
            edtDesc.setText(doc.getString("descripció"))

            val fmt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            doc.getTimestamp("data_limit")?.also {
                dataLimitSeleccionada = it
                txtDataLimit.text = fmt.format(it.toDate())
            }
            doc.getTimestamp("recordatori")?.also {
                recordatoriSeleccionat = it
                txtRecord.text = fmt.format(it.toDate())
            }

            val actual = doc.getString("estat") ?: "En Proces"
            spinnerEstat.setSelection(estados.indexOf(actual))
        }

        // Selección de data límit
        txtDataLimit.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendar,
                minDate = System.currentTimeMillis()
            ) { date ->
                calendar.time = date
                txtDataLimit.text = dateFormat.format(date)
                dataLimitSeleccionada = Timestamp(date)
            }
        }

        // Selección de recordatori
        txtRecord.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendar,
                minDate = dataLimitSeleccionada?.toDate()?.time ?: System.currentTimeMillis()
            ) { date ->
                calendar.time = date
                txtRecord.text = dateFormat.format(date)
                recordatoriSeleccionat = Timestamp(date)
            }
        }

        // Guardar cambios
        btnGuardar.setOnClickListener {
            val newTitol = edtTitol.text.toString().trim()
            val newDesc = edtDesc.text.toString().trim()

            if (newTitol.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "El títol no pot estar buit",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }

            if (recordatoriSeleccionat != null &&
                recordatoriSeleccionat!!.toDate().after(dataLimitSeleccionada!!.toDate())
            ) {
                Toast.makeText(
                    requireContext(),
                    "El recordatori no pot ser després de la data límit",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val updates = hashMapOf<String, Any>(
                "titol" to newTitol,
                "descripció" to edtDesc.text.toString().trim(),
                "estat" to spinnerEstat.selectedItem as String,
                "data_limit" to dataLimitSeleccionada!!
                //"recordatori" to recordatoriSeleccionat!!

            )
            recordatoriSeleccionat?.let { updates["recordatori"] = it }

            docRef.update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Tasca actualitzada", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                    cargareventosytareas(selectedDate)

                    recordatoriSeleccionat?.let { ts ->
                        rescheduleAlarm(
                            docId = item.id,
                            type = "tasques",
                            title = newTitol,
                            desc = newDesc,
                            triggerAt = ts.toDate().time
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error actualitzant", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        dialog.show()
    }

    private fun mostrarDialogEditarEvent(item: CalendarItem) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_afegir_esdeveniment, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val edtTitol = dialogView.findViewById<EditText>(R.id.editTextTitol)
        val edtDesc = dialogView.findViewById<EditText>(R.id.editTextDescripcio)
        val txtInici = dialogView.findViewById<TextView>(R.id.textViewDataLimit)
        val txtFinal = dialogView.findViewById<TextView>(R.id.textviewFinalitzacio)
        val txtRecord = dialogView.findViewById<TextView>(R.id.textviewRecordatori)
        val btnGuardar = dialogView.findViewById<Button>(R.id.buttonGuardarEsdeveniment)

        var dataFinalSeleccionada: Timestamp? = null
        var recordatoriSeleccionat: Timestamp? = null
        val calendar = Calendar.getInstance()

        // Cargar doc para obtener los Timestamps originales
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val docRef = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection("esdeveniments")
            .document(item.id)

        docRef.get().addOnSuccessListener { doc ->
            edtTitol.setText(doc.getString("titol"))
            edtDesc.setText(doc.getString("descripció"))

            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            doc.getTimestamp("data_fi")?.also {
                dataFinalSeleccionada = it
                txtFinal.text = fmt.format(it.toDate())
            }
            doc.getTimestamp("recordatori")?.also {
                recordatoriSeleccionat = it
                txtRecord.text = fmt.format(it.toDate())
            }
            edtTitol.setText(doc.getString("titol"))
            edtDesc.setText(doc.getString("descripció"))
        }

        // fecha de inicio desabilitada
        txtInici.isClickable = false
        txtInici.isFocusable = false
        txtInici.alpha = 0.6f

        // SELECCIÓ DATA FINALITZACIÓ
        txtFinal.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendarFinal,
                // No deixar que la final sigui abans de la inici
                minDate = dataIniciSeleccionada
                    ?.toDate()
                    ?.time
                    ?: System.currentTimeMillis()
            ) { date ->
                calendarFinal.time = date
                txtFinal.text = dateFormat.format(date)
                dataFinalSeleccionada = Timestamp(date)
            }
        }

        // Selección de recordatori
        txtRecord.setOnClickListener {
            requireContext().showDateTimePicker(
                initial = calendarRecord,
                // No deixar que el recordatori sigui abans de la inici
                minDate = dataIniciSeleccionada
                    ?.toDate()
                    ?.time
                    ?: System.currentTimeMillis()
            ) { date ->
                calendarRecord.time = date
                txtRecord.text = dateFormat.format(date)
                recordatoriSeleccionat = Timestamp(date)
            }
        }

        // Guardar cambios
        btnGuardar.setOnClickListener {
            val newTitle = edtTitol.text.toString().trim()
            val newDesc = edtDesc.text.toString().trim()
            if (newTitle.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "El títol no pot estar buit",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            if (dataFinalSeleccionada == null) {
                Toast.makeText(
                    requireContext(),
                    "Cal indicar la data de finalització", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (recordatoriSeleccionat != null &&
                recordatoriSeleccionat!!.toDate().after(dataFinalSeleccionada!!.toDate())
            ) {
                Toast.makeText(
                    requireContext(),
                    "El recordatori no pot ser després de la data de finalització",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val updates = hashMapOf<String, Any>(
                "titol" to newTitle,
                "descripció" to edtDesc.text.toString().trim(),
                //"data_inici" to
                "data_fi" to dataFinalSeleccionada!!,
                "recordatori" to recordatoriSeleccionat!!
            )

            docRef.update(updates)
                .addOnSuccessListener { docRef ->
                    Toast.makeText(requireContext(), "… actualizado", LENGTH_SHORT).show()
                    dialog.dismiss()
                    cargareventosytareas(selectedDate)
                    recordatoriSeleccionat?.let { ts ->
                        rescheduleAlarm(
                            docId = item.id,
                            type = "esdeveniments",           // o "tasques"
                            title = newTitle,
                            desc = newDesc,
                            triggerAt = ts.toDate().time
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error actualitzant", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        dialog.show()
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    fun Context.showDateTimePicker(
        initial: Calendar = Calendar.getInstance(),
        minDate: Long? = null,
        onDateTimeSelected: (Date) -> Unit
    ) {
        DatePickerDialog(
            this,
            { _, y, m, d ->
                TimePickerDialog(this, { _, h, min ->
                    Calendar.getInstance().apply {
                        set(y, m, d, h, min)
                    }.time.let(onDateTimeSelected)
                }, initial.get(Calendar.HOUR_OF_DAY), initial.get(Calendar.MINUTE), true).show()
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate?.let { datePicker.minDate = it }
        }.show()
    }

    private fun rescheduleAlarm(
        docId: String,
        type: String,        // "tasques" o "esdeveniments"
        title: String,
        desc: String,
        triggerAt: Long
    ) {
        val context = requireContext()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancela la antigua (si existía)
        Intent(context, ReminderReceiver::class.java).let { oldIntent ->
            PendingIntent.getBroadcast(
                context,
                docId.hashCode(),
                oldIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )?.let { am.cancel(it) }
        }

        // Crea la nueva
        val reminderIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("docId", docId)
            putExtra("type", type)
            putExtra("titol", title)
            putExtra("descripcio", desc)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            docId.hashCode(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Comprueba permisos en Android S+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            return
        }

        // Programa
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
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
}