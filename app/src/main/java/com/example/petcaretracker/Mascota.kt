package com.example.petcaretracker

data class Mascota(
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val tipo: String = "",
    val fotoUrl: String = "",

    val cuidadorId: String? = null,
    val cuidadorNombre: String? = null
)

