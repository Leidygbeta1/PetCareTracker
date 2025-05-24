package com.example.petcaretracker.cuidador

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R

import com.example.petcaretracker.owner.VacunaAdapter

class CarnetVacunacionCuidadorActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var recyclerVacunas: RecyclerView
    private var cuidadorId: String = ""
    private var mascotasAsignadas = mutableListOf<MascotaCuidador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet_cuidador)

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnBuscar = findViewById(R.id.btnBuscar)
        recyclerVacunas = findViewById(R.id.recyclerVacunas)

        cuidadorId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "") ?: ""
        Log.d("CarnetCuidador", "cuidadorId: $cuidadorId")

        if (cuidadorId.isEmpty()) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarMascotasAsignadas()

        btnBuscar.setOnClickListener {
            val index = spinnerMascotas.selectedItemPosition
            if (index in mascotasAsignadas.indices) {
                val mascota = mascotasAsignadas[index]
                FirebaseService.obtenerIdMascotaPorNombre(mascota.nombre, mascota.duenoId) { mascotaId ->
                    if (mascotaId != null) {
                        cargarVacunas(mascota.duenoId, mascotaId)
                    } else {
                        Toast.makeText(this, "Error al encontrar mascota", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun cargarMascotasAsignadas() {
        FirebaseService.obtenerMascotasAsignadasAlCuidador(cuidadorId) { lista ->
            if (lista.isEmpty()) {
                Toast.makeText(this, "No tienes mascotas asignadas", Toast.LENGTH_SHORT).show()
                return@obtenerMascotasAsignadasAlCuidador
            }

            mascotasAsignadas.clear()
            mascotasAsignadas.addAll(lista)

            val nombres = lista.map { it.nombre }
            spinnerMascotas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        }
    }

    private fun cargarVacunas(duenoId: String, mascotaId: String) {
        FirebaseService.obtenerVacunas(duenoId, mascotaId) { vacunas ->
            if (vacunas != null && vacunas.isNotEmpty()) {
                recyclerVacunas.layoutManager = LinearLayoutManager(this)
                recyclerVacunas.adapter = VacunaAdapter(vacunas)
            } else {
                Toast.makeText(this, "No hay vacunas disponibles", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

