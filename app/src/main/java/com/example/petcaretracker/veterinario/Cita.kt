package com.example.petcaretracker.model

data class Cita(
    val id: String = "",
    val duenioId: String = "",
    val duenioNombre: String = "",
    val veterinarioId: String = "",
    val veterinarioNombre: String = "",
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = "",
    val estado: String = "Pendiente"
)
