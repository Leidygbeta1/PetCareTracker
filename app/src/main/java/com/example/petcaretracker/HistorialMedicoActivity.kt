package com.example.petcaretracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HistorialMedicoActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var btnAtras: Button
    private lateinit var listaRegistros: LinearLayout
    private var userId: String = ""
    private var mascotaIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_medico)

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnAtras = findViewById(R.id.btnAtras)
        listaRegistros = findViewById(R.id.listaRegistros)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""
        Log.d("historialMedicoActivity", "userId recibido: $userId")

        if (userId.isEmpty()) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarMascotas()

        btnBuscar.setOnClickListener {
            val mascotaSeleccionada = spinnerMascotas.selectedItemPosition
            if (mascotaSeleccionada >= 0 && mascotaSeleccionada < mascotaIds.size) {
                val mascotaId = mascotaIds[mascotaSeleccionada]
                cargarRegistrosMedicos(mascotaId)
            }
        }

        btnAtras.setOnClickListener {
            finish()
        }
    }

    private fun cargarMascotas() {
        FirebaseService.obtenerMascotas(userId) { mascotas ->
            if (mascotas != null) {
                val nombresMascotas = mascotas.map {
                    mascotaIds.add(it["id"].toString())
                    it["nombre_mascota"].toString()
                }
                spinnerMascotas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombresMascotas)
            } else {
                Toast.makeText(this, "Error al cargar mascotas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarRegistrosMedicos(mascotaId: String) {
        listaRegistros.removeAllViews()
        FirebaseService.obtenerRegistrosMedicos(userId, mascotaId) { registros ->
            if (registros != null && registros.isNotEmpty()) {
                registros.forEach { registro ->
                    val registroView = TextView(this)
                    val tipoAtencion = registro["tipo_atencion"].toString()
                    val descripcion = registro["descripcion"].toString()
                    val timestamp = registro["fecha_registro"] as? com.google.firebase.Timestamp
                    val fecha = if (timestamp != null) {
                        val date = timestamp.toDate()
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
                        dateFormat.format(date)
                    } else {
                        "Fecha no disponible"
                    }
                    registroView.text = "Tipo: $tipoAtencion\nDescripción: $descripcion\nFecha: $fecha\n"
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
    }
}
