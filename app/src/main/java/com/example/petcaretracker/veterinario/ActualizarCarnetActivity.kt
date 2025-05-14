package com.example.petcaretracker.veterinario

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
import com.example.petcaretracker.Vacuna

import com.example.petcaretracker.owner.AgregarVacunaActivity
import com.example.petcaretracker.owner.VacunaAdapter

class ActualizarCarnetActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var btnAgregarVacuna: Button
    private lateinit var recyclerVacunas: RecyclerView
    private var veterinarioId: String = ""
    private var veterinarioNombre: String = ""
    private var mascotaIds = mutableListOf<String>()
    private var dueñoIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet_vacunacion)

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnAgregarVacuna = findViewById(R.id.btnAgregarVacuna)
        recyclerVacunas = findViewById(R.id.recyclerVacunas)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        veterinarioId = prefs.getString("userId", "") ?: ""

        if (veterinarioId.isEmpty()) {
            Toast.makeText(this, "Veterinario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseService.obtenerVeterinarioYClinica(veterinarioId) { datos ->
            if (datos != null) {
                veterinarioNombre = datos["veterinario_nombre"] as String
            }
        }

        cargarMascotasDelVeterinario()

        btnBuscar.setOnClickListener {
            val index = spinnerMascotas.selectedItemPosition
            if (index >= 0 && index < mascotaIds.size) {
                val mascotaId = mascotaIds[index]
                val dueñoId = dueñoIds[index]
                cargarVacunas(dueñoId, mascotaId)
            }
        }

        btnAgregarVacuna.setOnClickListener {
            val index = spinnerMascotas.selectedItemPosition
            if (index >= 0 && index < mascotaIds.size) {
                val mascotaId = mascotaIds[index]
                val dueñoId = dueñoIds[index]

                val intent = Intent(this, AgregarVacunaActivity2::class.java)
                intent.putExtra("mascotaId", mascotaId)
                intent.putExtra("duenioId", dueñoId)
                startActivity(intent)
            }
        }
    }

    private fun cargarMascotasDelVeterinario() {
        FirebaseService.obtenerMascotasPorVeterinario2(veterinarioId) { mascotas ->
            if (mascotas != null && mascotas.isNotEmpty()) {
                val nombresMascotas = mascotas.map {
                    mascotaIds.add(it["id"].toString())
                    dueñoIds.add(it["user_id"].toString())
                    it["nombre_mascota"].toString()
                }
                spinnerMascotas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombresMascotas)
            } else {
                Toast.makeText(this, "No se encontraron mascotas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarVacunas(duenioId: String, mascotaId: String) {
        FirebaseService.obtenerVacunas(duenioId, mascotaId) { vacunas ->
            if (vacunas != null && vacunas.isNotEmpty()) {
                val adapter = VacunaAdapter(vacunas)
                recyclerVacunas.layoutManager = LinearLayoutManager(this)
                recyclerVacunas.adapter = adapter
            } else {
                Toast.makeText(this, "No hay vacunas disponibles.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

