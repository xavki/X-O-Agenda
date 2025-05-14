package com.institutmarianao.xo_agenda.models

import java.util.Date

data class CalendarItem(
    val id: String,
    val title: String,
    val description: String,
    val dateTimeText: String,
    val tipo: String,
    val fechaOrdenacion: Date
)

