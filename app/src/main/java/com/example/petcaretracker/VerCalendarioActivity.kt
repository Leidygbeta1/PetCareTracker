package com.example.petcaretracker

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import android.widget.CalendarView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class VerCalendarioActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var listViewRecordatorios: ListView
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_calendario)

        // Inicializar componentes
        calendarView = findViewById(R.id.calendarView)
        listViewRecordatorios = findViewById(R.id.listViewRecordatorios)

        // Inicializamos Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""
        Log.d("VerCalendarioActivity", "userId recibido: $userId")

        // Si el userId está vacío, redirigir a la pantalla de login
        if (userId.isEmpty()) {
            Log.e("VerCalendarioActivity", "userId está vacío, redirigiendo a LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Configurar evento para cuando se seleccione una fecha en el calendario
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Formatear la fecha seleccionada
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            // Cargar los recordatorios para esa fecha
            cargarRecordatorios(selectedDate)
        }
    }

    // Función para cargar los recordatorios desde Firebase
    private fun cargarRecordatorios(fechaSeleccionada: String) {
        db.collection("usuarios")
            .document(userId)
            .collection("recordatorios")
            .whereEqualTo("fecha", fechaSeleccionada)
            .get()
            .addOnSuccessListener { result ->
                val recordatorios = mutableListOf<String>()
                for (document in result) {
                    val descripcion = document.getString("descripcion")
                    if (descripcion != null) {
                        recordatorios.add(descripcion)
                    }
                }

                // Si hay recordatorios, los mostramos en el ListView
                if (recordatorios.isNotEmpty()) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recordatorios)
                    listViewRecordatorios.adapter = adapter

                } else {
                    Toast.makeText(this, "No hay recordatorios para esta fecha.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar los recordatorios.", Toast.LENGTH_SHORT).show()
            }
    }
}

