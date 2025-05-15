package com.institutmarianao.xo_agenda

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.provider.Settings

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
                        cargareventosytareas(calendarItems, adapter)
                        recordatoriSeleccionat?.let { ts ->
                            val am = requireContext()
                                .getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            val intent = Intent(requireContext(), ReminderReceiver::class.java)
                                .apply {
                                    putExtra("title", titol)
                                    putExtra("desc", descripcio)
                                }

                            val requestCode = it.id.hashCode()
                            val pi = PendingIntent.getBroadcast(
                                requireContext(),
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            am.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                ts.toDate().time,
                                pi
                            )
                        }
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
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            textDataLimit.text =
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
        textFinalitzacio.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            textFinalitzacio.text =
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
        textRecordatori.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            textRecordatori.text =
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
                        cargareventosytareas(calendarItems, adapter)
                        // Programar alarma si hay recordatori
                        recordatoriSeleccionat?.let { ts ->
                            val am =
                                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            val intent =
                                Intent(requireContext(), ReminderReceiver::class.java).apply {
                                    putExtra("title", titol)
                                    putExtra("desc", descripcio)
                                }
                            val requestCode = it.id.hashCode()
                            val pi = PendingIntent.getBroadcast(
                                requireContext(),
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            am.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                ts.toDate().time,
                                pi
                            )
                        }
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
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        calendarItems.clear()
        // formato para todas las fechas
        val fmt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

        fun procesarDocumentos(docs: QuerySnapshot, tipo: String) {
            for (doc in docs) {
                val id = doc.id
                val title = doc.getString("titol") ?: ""
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
                    val tsEnd = doc.getTimestamp("data_fi") ?: tsStart

                    val fechaStart = tsStart.toDate()
                    val fechaEnd = tsEnd.toDate()

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
            .get().addOnSuccessListener {
                procesarDocumentos(it, "Tasca")
                // ...luego eventos
                db.collection("usuarios").document(uid).collection("esdeveniments")
                    .get().addOnSuccessListener { procesarDocumentos(it, "Esdeveniment") }
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

        // 5) DatePicker / TimePicker para volver a seleccionar límites y recordatorio

        // Selección de data límit
        txtDataLimit.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            txtDataLimit.text =
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
        txtRecord.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            txtRecord.text =
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


        // Guardar cambios
        btnGuardar.setOnClickListener {
            val nuevoTitol = edtTitol.text.toString().trim()
            val nuevaDesc = edtDesc.text.toString().trim()

            if (nuevoTitol.isEmpty()) {
                Toast.makeText(requireContext(), "El títol no pot estar buit", Toast.LENGTH_SHORT)
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
                "titol" to nuevoTitol,
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
                    cargareventosytareas(calendarItems, adapter)
                    // Primero, cancela la alarma anterior (si existía)
                    val am =
                        requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val oldIntent = Intent(requireContext(), ReminderReceiver::class.java)
                    val oldPi = PendingIntent.getBroadcast(
                        requireContext(),
                        item.id.hashCode(),
                        oldIntent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (oldPi != null) am.cancel(oldPi)

                    // Luego, programa la nueva
                    recordatoriSeleccionat?.let { ts ->
                        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
                            putExtra("title", nuevoTitol)
                            putExtra("desc", nuevaDesc)
                        }
                        val requestCode = item.id.hashCode()
                        val pi = PendingIntent.getBroadcast(
                            requireContext(),
                            requestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (!am.canScheduleExactAlarms()) {
                                // Lanza la pantalla de ajustes para activar exact alarms
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    // Opcional: abrir directamente los ajustes de tu app
                                    // setPackage(requireContext().packageName)
                                }
                                startActivity(intent)
                                // Aquí podrías retornar o mostrar un mensaje para que el usuario vuelva
                                return@addOnSuccessListener
                            }
                        }
                        am.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            ts.toDate().time,
                            pi
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


        // SELECCIO DATA FINALITZACIÓ
        txtFinal.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            txtFinal.text =
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
        txtRecord.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val timePicker = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, day, hourOfDay, minute)
                            txtRecord.text =
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

        // 3) Guardar cambios
        btnGuardar.setOnClickListener {
            val newTitle = edtTitol.text.toString().trim()
            val newDesc = edtDesc.text.toString().trim()
            if (newTitle.isEmpty()) {
                Toast.makeText(requireContext(), "El títol no pot estar buit", Toast.LENGTH_SHORT)
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
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Esdeveniment actualitzat", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                    cargareventosytareas(calendarItems, adapter)
                    // Primero, cancela la alarma anterior (si existía)
                    val am =
                        requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val oldIntent = Intent(requireContext(), ReminderReceiver::class.java)
                    val oldPi = PendingIntent.getBroadcast(
                        requireContext(),
                        item.id.hashCode(),
                        oldIntent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (oldPi != null) am.cancel(oldPi)

                    // Luego, programa la nueva
                    recordatoriSeleccionat?.let { ts ->
                        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
                            putExtra("title", newTitle)
                            putExtra("desc", newDesc)
                        }
                        val requestCode = item.id.hashCode()
                        val pi = PendingIntent.getBroadcast(
                            requireContext(),
                            requestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (!am.canScheduleExactAlarms()) {
                                // Lanza la pantalla de ajustes para activar exact alarms
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    // Opcional: abrir directamente los ajustes de tu app
                                    // setPackage(requireContext().packageName)
                                }
                                startActivity(intent)
                                // Aquí podrías retornar o mostrar un mensaje para que el usuario vuelva
                                return@addOnSuccessListener
                            }
                        }
                        am.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            ts.toDate().time,
                            pi
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
