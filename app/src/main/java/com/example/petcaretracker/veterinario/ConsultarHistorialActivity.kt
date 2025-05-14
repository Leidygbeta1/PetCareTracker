package com.example.petcaretracker.veterinario

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ConsultarHistorialActivity: AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var btnAtras: Button
    private lateinit var listaRegistros: LinearLayout
    private var veterinarioId: String = ""
    private var mascotaIds = mutableListOf<String>()
    private var selectedMascotaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_medico)

        // Inicializamos las vistas
        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnAtras = findViewById(R.id.btnAtras)
        listaRegistros = findViewById(R.id.listaRegistros)

        // Obtener el ID del veterinario logueado desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        veterinarioId = sharedPreferences.getString("userId", "") ?: ""
        Log.d("HistorialMedicoVeterinario", "veterinarioId recibido: $veterinarioId")

        if (veterinarioId.isEmpty()) {
            Toast.makeText(this, "Veterinario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar las mascotas asociadas al veterinario
        cargarMascotas()

        // Acción del botón "Buscar"
        btnBuscar.setOnClickListener {
            val mascotaSeleccionada = spinnerMascotas.selectedItemPosition
            if (mascotaSeleccionada >= 0 && mascotaSeleccionada < mascotaIds.size) {
                val mascotaId = mascotaIds[mascotaSeleccionada]
                cargarRegistrosMedicos(mascotaId)
            }
        }


        // Acción del botón "Atrás"
        btnAtras.setOnClickListener {
            finish()
        }
    }

    // Función para cargar las mascotas asociadas al veterinario
    private fun cargarMascotas() {
        FirebaseService.obtenerMascotasPorVeterinario2(veterinarioId) { mascotas ->
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



    // Función para cargar los registros médicos de la mascota seleccionada
    private fun cargarRegistrosMedicos(mascotaId: String) {
        listaRegistros.removeAllViews()

        if (mascotaId.isNotEmpty()) {
            FirebaseService.obtenerRegistrosMedicosPorVeterinario(veterinarioId, mascotaId) { registros ->
                if (registros != null && registros.isNotEmpty()) {
                    registros.forEach { registro ->
                        val registroView = TextView(this)
                        val tipoAtencion = registro["tipo_atencion"].toString()
                        val descripcion = registro["descripcion"].toString()
                        val fecha = registro["fecha"].toString()

                        registroView.text = """
                        🐾 Tipo: $tipoAtencion
                        📝 Descripción: $descripcion
                        📅 Fecha: $fecha
                    """.trimIndent()

                        registroView.setPadding(10, 10, 10, 10)
                        registroView.setBackgroundResource(R.drawable.border_item)
                        listaRegistros.addView(registroView)
                    }
                } else {
                    val noDataView = TextView(this)
                    noDataView.text = "No hay registros médicos disponibles."
                    listaRegistros.addView(noDataView)
                }
            }
        } else {
            Toast.makeText(this, "No se seleccionó una mascota válida.", Toast.LENGTH_SHORT).show()
        }
    }

}




