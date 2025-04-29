package com.example.petcaretracker.cuidador

// 1) Data class para el resultado de las mascotas de un cuidador
data class MascotaCuidador(
    val nombre: String,
    val raza: String,
    val tipo: String,
    val foto: String,
    val nombreDueno: String
)
