package com.example.petcaretracker.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.LoginActivity
import com.example.petcaretracker.R

class RegistroMedicoActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: Spinner
    private lateinit var spinnerTipoAtencion: Spinner
    private lateinit var spinnerClinica: Spinner
    private lateinit var spinnerVeterinario: Spinner
    private lateinit var contenedorOpciones: LinearLayout
    private lateinit var btnGuardarRegistro: Button
    private lateinit var btnAtras: Button
    private var userId: String = ""
    private var mascotaIds = mutableListOf<String>()
    private var listaVeterinariosId = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_medico)

        // Obtener userId de SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""

        // Inicializar vistas
        spinnerMascota = findViewById(R.id.spinnerMascota)
        spinnerTipoAtencion = findViewById(R.id.spinnerTipoAtencion)
        spinnerClinica = findViewById(R.id.spinnerClinica)
        spinnerVeterinario = findViewById(R.id.spinnerVeterinario)
        contenedorOpciones = findViewById(R.id.contenedorOpciones)
        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro)
        btnAtras = findViewById(R.id.btnAtras)



        // Cargar mascotas
        cargarMascotas()

        // Tipos de atención
        val tiposAtencion = listOf("Tratamiento", "Rutina", "Vacunación", "Esterilización")
        spinnerTipoAtencion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposAtencion)
        spinnerTipoAtencion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                actualizarOpciones(tiposAtencion[position])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Cargar clínicas disponibles
        cargarClinicas()

        // Guardar registro médico
        btnGuardarRegistro.setOnClickListener {
            val mascotaSeleccionada = spinnerMascota.selectedItemPosition
            val mascotaId = mascotaIds[mascotaSeleccionada]
            val tipoAtencion = spinnerTipoAtencion.selectedItem.toString()
            val detalles = obtenerDetalles()
            val clinica = spinnerClinica.selectedItem?.toString() ?: ""
            val veterinarioId = listaVeterinariosId.getOrNull(spinnerVeterinario.selectedItemPosition) ?: ""
            val veterinarioNombre = spinnerVeterinario.selectedItem?.toString() ?: ""

            FirebaseService.guardarRegistroMedico(
                userId,
                mascotaId,
                tipoAtencion,
                detalles,
                veterinarioId,
                veterinarioNombre,
                clinica
            ) { success ->
                if (success) {
                    Toast.makeText(this, "Registro guardado exitosamente.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar el registro.", Toast.LENGTH_SHORT).show()
                }
            }
        }


        btnAtras.setOnClickListener {
            startActivity(Intent(this, MedicoActivity::class.java))
            finish()
        }
    }

    private fun cargarMascotas() {
        if (userId.isEmpty()) {
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

    private fun cargarClinicas() {
        FirebaseService.obtenerClinicasDisponibles { clinicas ->
            if (clinicas != null) {
                spinnerClinica.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, clinicas)
                spinnerClinica.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val clinicaSeleccionada = clinicas[position]
                        cargarVeterinariosPorClinica(clinicaSeleccionada)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            } else {
                Toast.makeText(this, "No hay clínicas disponibles", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarVeterinariosPorClinica(clinica: String) {
        FirebaseService.obtenerVeterinariosPorClinica(clinica) { veterinarios ->
            if (veterinarios != null) {
                listaVeterinariosId.clear()
                val nombres = veterinarios.map {
                    listaVeterinariosId.add(it["id"].toString())
                    it["nombre_completo"].toString()
                }
                spinnerVeterinario.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
            } else {
                Toast.makeText(this, "No hay veterinarios para esta clínica", Toast.LENGTH_SHORT).show()
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



