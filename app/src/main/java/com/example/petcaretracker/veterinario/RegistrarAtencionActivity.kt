package com.example.petcaretracker.veterinario

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R

class RegistrarAtencionActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: Spinner
    private lateinit var spinnerTipoAtencion: Spinner
    private lateinit var contenedorOpciones: LinearLayout
    private lateinit var btnGuardarRegistro: Button
    private lateinit var btnAtras: Button
    private var veterinarioId: String = ""
    private var mascotaIds = mutableListOf<String>()
    private var veterinarioNombre: String = ""
    private var clinica: String = ""
    private var dueñoIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_atencion)

        // Inicializar vistas
        spinnerMascota = findViewById(R.id.spinnerMascota)
        spinnerTipoAtencion = findViewById(R.id.spinnerTipoAtencion)
        contenedorOpciones = findViewById(R.id.contenedorOpciones)
        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro)
        btnAtras = findViewById(R.id.btnAtras)

        // Obtener veterinarioId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        veterinarioId = sharedPreferences.getString("userId", "") ?: ""

        if (veterinarioId.isEmpty()) {
            Toast.makeText(this, "Veterinario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar las mascotas del veterinario
        cargarMascotasPorVeterinario()

        // Obtener el nombre del veterinario y la clínica
        obtenerVeterinarioYClinica()

        // Tipos de atención
        val tiposAtencion = listOf("Tratamiento", "Rutina", "Vacunación", "Esterilización")
        spinnerTipoAtencion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposAtencion)
        spinnerTipoAtencion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                actualizarOpciones(tiposAtencion[position])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Guardar el registro de atención médica
        btnGuardarRegistro.setOnClickListener {
            val mascotaSeleccionada = spinnerMascota.selectedItemPosition
            val mascotaId = mascotaIds[mascotaSeleccionada]
            val dueñoId = dueñoIds[mascotaSeleccionada] // <- el ID del dueño claramente
            val tipoAtencion = spinnerTipoAtencion.selectedItem.toString()
            val detalles = obtenerDetalles()

            FirebaseService.guardarAtencionMedica(
                dueñoId, // Aquí envías claramente el dueño
                mascotaId,
                tipoAtencion,
                detalles,
                veterinarioId,
                veterinarioNombre,
                clinica
            ) { success ->
                if (success) {
                    Toast.makeText(this, "Atención registrada exitosamente.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al registrar la atención.", Toast.LENGTH_SHORT).show()
                }
            }
        }


        btnAtras.setOnClickListener {
            finish() // Regresar a la actividad anterior
        }
    }

    private fun cargarMascotasPorVeterinario() {
        FirebaseService.obtenerMascotasPorVeterinario2(veterinarioId) { mascotas ->
            if (mascotas != null && mascotas.isNotEmpty()) {
                dueñoIds.clear()
                mascotaIds.clear()

                val nombresMascotas = mutableListOf<String>()

                mascotas.forEach { mascota ->
                    val mascotaId = mascota["id"].toString()
                    val dueñoId = mascota["user_id"].toString()
                    val nombreMascota = mascota["nombre_mascota"].toString()

                    mascotaIds.add(mascotaId)
                    dueñoIds.add(dueñoId)
                    nombresMascotas.add(nombreMascota)

                    // Logs para verificar los datos
                    Log.d("CargarMascotas", "Mascota ID: $mascotaId")
                    Log.d("CargarMascotas", "Dueño ID: $dueñoId")
                    Log.d("CargarMascotas", "Nombre mascota: $nombreMascota")
                }

                spinnerMascota.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombresMascotas
                )
            } else {
                Toast.makeText(this, "Error al cargar mascotas del veterinario.", Toast.LENGTH_SHORT).show()
                Log.e("CargarMascotas", "Lista de mascotas vacía o nula.")
            }
        }
    }





    private fun obtenerVeterinarioYClinica() {
        FirebaseService.obtenerVeterinarioYClinica(veterinarioId) { data ->
            if (data != null) {
                veterinarioNombre = data["veterinario_nombre"] as String
                clinica = data["clinica"] as String
            } else {
                Toast.makeText(this, "Error al obtener datos del veterinario.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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



