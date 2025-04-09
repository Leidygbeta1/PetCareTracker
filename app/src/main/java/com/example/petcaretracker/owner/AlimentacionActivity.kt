package com.example.petcaretracker.owner

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R

class AlimentacionActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnCalcularAlimento: Button
    private lateinit var tvCantidadAlimento: TextView
    private lateinit var etPesoMascota: EditText
    private lateinit var etEdadMascota: EditText
    private var mascotaIds = mutableListOf<String>()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alimentacion)

        // Inicializar vistas
        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnCalcularAlimento = findViewById(R.id.btnCalcularAlimento)
        tvCantidadAlimento = findViewById(R.id.tvCantidadAlimento)
        etPesoMascota = findViewById(R.id.etPesoMascota)
        etEdadMascota = findViewById(R.id.etEdadMascota)

        // Obtener el userId desde SharedPreferences
        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "") ?: ""

        // Cargar las mascotas del usuario
        cargarMascotas()

        btnCalcularAlimento.setOnClickListener {
            // Obtener el peso y edad de la mascota ingresados por el usuario
            val peso = etPesoMascota.text.toString().toFloatOrNull()
            val edad = etEdadMascota.text.toString().toIntOrNull()

            if (peso != null && peso > 0 && edad != null && edad > 0) {
                // Calcular la cantidad de comida en función del peso y la edad
                val cantidadAlimento = calcularAlimento(peso, edad)
                tvCantidadAlimento.text = "Cantidad de comida recomendada: $cantidadAlimento gramos"
            } else {
                Toast.makeText(this, "Por favor ingresa un peso y edad válidos", Toast.LENGTH_SHORT).show()
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

                // Configurar el Spinner con los nombres de las mascotas
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombresMascotas)
                spinnerMascotas.adapter = adapter
            } else {
                Toast.makeText(this, "Error al cargar mascotas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Calcular la cantidad de comida basada en el peso y la edad de la mascota
    private fun calcularAlimento(peso: Float, edad: Int): Float {
        // Ejemplo: Ajustamos la cantidad de comida según el peso y la edad de la mascota
        var cantidad = peso * 0.05f * 1000 // Convertido a gramos

        // Si la mascota es mayor de 5 años, reducimos la cantidad de comida un 10%
        if (edad > 5) {
            cantidad *= 0.9f
        }

        return cantidad
    }
}
