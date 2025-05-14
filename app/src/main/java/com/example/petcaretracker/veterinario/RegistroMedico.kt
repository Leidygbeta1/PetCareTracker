package com.example.petcaretracker.veterinario

data class RegistroMedico(
    val tipoAtencion: String = "",
    val descripcion: String = "",
    val veterinarioNombre: String = "",
    val clinica: String = "",
    val fechaRegistro: String = "",  // Podrías usar Timestamp si lo necesitas
    val nombreMascota: String,
    val raza: String,
    val tipo: String,
    val nombreDueno: String,
    val fecha: String
)

