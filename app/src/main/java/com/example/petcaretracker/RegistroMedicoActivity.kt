package com.example.petcaretracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.content.Context

class RegistroMedicoActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: Spinner
    private lateinit var spinnerTipoAtencion: Spinner
    private lateinit var contenedorOpciones: LinearLayout
    private lateinit var btnGuardarRegistro: Button
    private lateinit var btnAtras: Button
    private var userId: String = "" // 🔹 ID del usuario logueado
    private var mascotaIds = mutableListOf<String>() // 🔹 IDs de las mascotas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_medico)

        Log.d("RegistroMedicoActivity", "Iniciando RegistroMedicoActivity...")

        // 🔹 Obtener userId del intent
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""
        Log.d("RegistroMedicoActivity", "userId recibido: $userId")




        // Inicializar elementos
        spinnerMascota = findViewById(R.id.spinnerMascota)
        spinnerTipoAtencion = findViewById(R.id.spinnerTipoAtencion)
        contenedorOpciones = findViewById(R.id.contenedorOpciones)
        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro)
        btnAtras = findViewById(R.id.btnAtras)



        // 🔹 Cargar mascotas del usuario
        cargarMascotas()

        // Lista de tipos de atención
        val tiposAtencion = listOf("Tratamiento", "Rutina", "Vacunación", "Esterilización")
        spinnerTipoAtencion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposAtencion)

        // Listener para tipos de atención
        spinnerTipoAtencion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarOpciones(tiposAtencion[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Guardar registro médico
        btnGuardarRegistro.setOnClickListener {
            val mascotaSeleccionada = spinnerMascota.selectedItemPosition
            val mascotaId = mascotaIds[mascotaSeleccionada]
            val tipoAtencion = spinnerTipoAtencion.selectedItem.toString()
            val detalles = obtenerDetalles()

            FirebaseService.guardarRegistroMedico(userId, mascotaId, tipoAtencion, detalles) { success ->
                if (success) {
                    Toast.makeText(this, "Registro guardado exitosamente.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar el registro.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Acción para regresar
        btnAtras.setOnClickListener {
            startActivity(Intent(this, MedicoActivity::class.java))
            finish()
        }
    }

    // 🔹 Cargar mascotas del usuario desde Firebase
    private fun cargarMascotas() {
        if (userId.isEmpty()) {
            Log.e("RegistroMedicoActivity", "userId está vacío, redirigiendo a LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        FirebaseService.obtenerMascotas(userId) { mascotas ->
            if (mascotas != null) {
                val nombresMascotas = mascotas.map {
                    mascotaIds.add(it["id"].toString())
                    it["nombre_mascota"].toString()
                }
                spinnerMascota.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombresMascotas)
            } else {
                Toast.makeText(this, "Error al cargar mascotas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Actualizar opciones según el tipo de atención
    private fun actualizarOpciones(tipo: String) {
        contenedorOpciones.removeAllViews()
        when (tipo) {
            "Tratamiento" -> {
                val editText = EditText(this)
                editText.hint = "Describe el tratamiento"
                contenedorOpciones.addView(editText)
            }
            "Rutina" -> {
                val opciones = listOf("Baño", "Desparasitación", "Revisión General")
                opciones.forEach {
                    val checkBox = CheckBox(this)
                    checkBox.text = it
                    contenedorOpciones.addView(checkBox)
                }
            }
            "Vacunación" -> {
                val vacunas = listOf("Rabia", "Moquillo", "Parvovirus")
                val spinner = Spinner(this)
                spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vacunas)
                contenedorOpciones.addView(spinner)
            }
            "Esterilización" -> {
                val textView = TextView(this)
                textView.text = "No hay opciones adicionales."
                contenedorOpciones.addView(textView)
            }
        }
    }

    // Obtener detalles seleccionados
    private fun obtenerDetalles(): String {
        val detalles = StringBuilder()
        for (i in 0 until contenedorOpciones.childCount) {
            val view = contenedorOpciones.getChildAt(i)
            when (view) {
                is EditText -> detalles.append(view.text.toString())
                is CheckBox -> if (view.isChecked) detalles.append("${view.text}, ")
                is Spinner -> detalles.append(view.selectedItem.toString())
            }
        }
        return detalles.toString()
    }
}


