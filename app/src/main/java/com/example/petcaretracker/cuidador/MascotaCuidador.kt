package com.example.petcaretracker.cuidador

data class MascotaCuidador(
    val nombre: String,
    val raza: String,
    val tipo: String,
    val foto: String,
    val nombreDueno: String,
    val duenoId: String // ✅ Agregado para obtener el ID del dueño
)

