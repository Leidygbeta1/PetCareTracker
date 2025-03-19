package com.example.petcaretracker


import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AgregarRecordatorioActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: Spinner
    private lateinit var datePicker: DatePicker
    private lateinit var editTextDescripcion: EditText
    private lateinit var btnGuardarRecordatorio: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var mascotasList: MutableList<String> // Lista de mascotas del usuario
    private lateinit var mascotaIds: MutableList<String> // Lista de IDs de las mascotas
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_recordatorio)

        // Inicializamos los componentes de la vista
        spinnerMascota = findViewById(R.id.spinnerMascota)
        datePicker = findViewById(R.id.datePicker)
        editTextDescripcion = findViewById(R.id.editTextDescripcion)
        btnGuardarRecordatorio = findViewById(R.id.btnGuardarRecordatorio)

        // Inicializamos Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""
        Log.d("AgregarRecordatorioActivity", "userId recibido: $userId")

        // Si el userId está vacío, redirigir a la pantalla de login
        if (userId.isEmpty()) {
            Log.e("AgregarRecordatorioActivity", "userId está vacío, redirigiendo a LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Cargar las mascotas del usuario logueado
        cargarMascotas()

        // Evento para guardar el recordatorio
        btnGuardarRecordatorio.setOnClickListener {
            guardarRecordatorio()
        }
    }

    // Función para cargar las mascotas del usuario logueado
    private fun cargarMascotas() {
        // Utilizamos el userId para cargar las mascotas desde Firestore
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                mascotasList = mutableListOf()
                mascotaIds = mutableListOf()
                for (document in result) {
                    val nombreMascota = document.getString("nombre_mascota")
                    if (nombreMascota != null) {
                        mascotasList.add(nombreMascota)
                        mascotaIds.add(document.id)  // Almacenar ID de la mascota
                    }
                }
                // Configurar el adaptador para el Spinner
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mascotasList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMascota.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las mascotas", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para guardar el recordatorio en Firebase
    private fun guardarRecordatorio() {
        val mascotaSeleccionada = spinnerMascota.selectedItem.toString()
        val descripcion = editTextDescripcion.text.toString()
        val fecha = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"

        // Verificamos que la descripción no esté vacía
        if (descripcion.isNotEmpty()) {
            // Crear un recordatorio
            val recordatorio = hashMapOf(
                "nombre_mascota" to mascotaSeleccionada,
                "descripcion" to descripcion,
                "fecha" to fecha
            )

            // Guardar el recordatorio en la subcolección de "recordatorios" del usuario
            db.collection("usuarios")
                .document(userId)
                .collection("recordatorios")
                .add(recordatorio)
                .addOnSuccessListener {
                    Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                    finish() // Volver a la pantalla anterior
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar recordatorio", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Por favor, ingresa una descripción", Toast.LENGTH_SHORT).show()
        }
    }
}


