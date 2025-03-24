package com.institutmarianao.xo_agenda.models




// Clase para representar una tasca
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // o tipo Timestamp
    val done: Boolean = false,
    val userId: String = ""
)
