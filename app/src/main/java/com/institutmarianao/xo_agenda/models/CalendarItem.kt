package com.institutmarianao.xo_agenda.models

data class CalendarItem(
    val title: String,
    val description: String,
    val dateTime: String,
    val type: String,
    val fechaOrdenacion: java.util.Date? = null // ← usada para ordenar

)
