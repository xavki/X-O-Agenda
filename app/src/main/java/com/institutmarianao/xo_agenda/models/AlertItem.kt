package com.institutmarianao.xo_agenda.models

data class AlertItem(
    val id: String,
    val title: String,
    val desc: String,
    var isRead: Boolean = false,
    val type: String? = null,
    val extraInfo: String? = null

)