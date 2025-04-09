package com.example.petcaretracker.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.example.petcaretracker.Vacuna

class CarnetVacunacionActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var btnAgregarVacuna: Button
    private lateinit var recyclerVacunas: RecyclerView // Cambié LinearLayout por RecyclerView
    private var userId: String = ""
    private var mascotaIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet_vacunacion)

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnAgregarVacuna = findViewById(R.id.btnAgregarVacuna)
        recyclerVacunas = findViewById(R.id.recyclerVacunas) // Cambié LinearLayout por RecyclerView

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""
        Log.d("carnetVacunacionActivity", "userId recibido: $userId")

        if (userId.isEmpty()) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar las mascotas del usuario
        cargarMascotas()

        // Configurar el botón de buscar para cargar las vacunas de la mascota seleccionada
        btnBuscar.setOnClickListener {
            val mascotaSeleccionada = spinnerMascotas.selectedItemPosition
            if (mascotaSeleccionada >= 0 && mascotaSeleccionada < mascotaIds.size) {
                val mascotaId = mascotaIds[mascotaSeleccionada]
                cargarVacunas(mascotaId)
            }
        }

        // Configurar el botón de agregar vacuna
        btnAgregarVacuna.setOnClickListener {
            val mascotaSeleccionada = spinnerMascotas.selectedItemPosition
            if (mascotaSeleccionada >= 0 && mascotaSeleccionada < mascotaIds.size) {
                val mascotaId = mascotaIds[mascotaSeleccionada]

                // Abrir la actividad para agregar vacuna
                val intent = Intent(this, AgregarVacunaActivity::class.java)
                intent.putExtra("mascotaId", mascotaId) // Pasar el ID de la mascota
                startActivity(intent)
            }
        }

    }

    // Cargar las mascotas del usuario
    private fun cargarMascotas() {
        FirebaseService.obtenerMascotas(userId) { mascotas ->
            if (mascotas != null) {
                val nombresMascotas = mascotas.map {
                    mascotaIds.add(it["id"].toString())
                    it["nombre_mascota"].toString()
                }
                spinnerMascotas.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombresMascotas
                )
            } else {
                Toast.makeText(this, "Error al cargar mascotas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cargar las vacunas de la mascota seleccionada
    private fun cargarVacunas(mascotaId: String) {
        FirebaseService.obtenerVacunas(userId, mascotaId) { vacunas ->
            if (vacunas != null && vacunas.isNotEmpty()) {
                // Configurar el RecyclerView con el adaptador
                val adapter = VacunaAdapter(vacunas)
                recyclerVacunas.layoutManager = LinearLayoutManager(this)
                recyclerVacunas.adapter = adapter
            } else {
                Toast.makeText(this, "No hay vacunas disponibles.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Agregar una nueva vacuna
    private fun agregarVacuna(mascotaId: String, vacuna: Vacuna) {
        FirebaseService.agregarVacuna(userId, mascotaId, vacuna) { success ->
            if (success) {
                Toast.makeText(this, "Vacuna agregada correctamente.", Toast.LENGTH_SHORT).show()
                cargarVacunas(mascotaId) // Actualizar la lista de vacunas después de agregar
            } else {
                Toast.makeText(this, "Error al agregar vacuna.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



