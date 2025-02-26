package com.example.petcaretracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegistroMedicoActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: Spinner
    private lateinit var spinnerTipoAtencion: Spinner
    private lateinit var contenedorOpciones: LinearLayout
    private lateinit var btnGuardarRegistro: Button
    private lateinit var btnAtras: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_medico)

        // Inicializar elementos
        spinnerMascota = findViewById(R.id.spinnerMascota)
        spinnerTipoAtencion = findViewById(R.id.spinnerTipoAtencion)
        contenedorOpciones = findViewById(R.id.contenedorOpciones)
        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro)
        btnAtras = findViewById(R.id.btnAtras) // 🔹 Nuevo botón para regresar

        // Lista de mascotas de ejemplo
        val mascotas = listOf("Mia", "Max", "Rocky", "Luna")
        spinnerMascota.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mascotas)

        // Lista de tipos de atención
        val tiposAtencion = listOf("Tratamiento", "Rutina", "Vacunación", "Esterilización")
        spinnerTipoAtencion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposAtencion)

        // Listener para cambiar dinámicamente las opciones
        spinnerTipoAtencion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarOpciones(tiposAtencion[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Acción al presionar Guardar Registro
        btnGuardarRegistro.setOnClickListener {
            val mascotaSeleccionada = spinnerMascota.selectedItem.toString()
            val tipoAtencion = spinnerTipoAtencion.selectedItem.toString()
            val detalles = obtenerDetalles()

            Toast.makeText(this, "Registro guardado para $mascotaSeleccionada\n$tipoAtencion: $detalles", Toast.LENGTH_LONG).show()

            // 🔹 Volver a la pantalla principal después de guardar
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Acción del botón "Atrás" para regresar a `MedicoActivity`
        btnAtras.setOnClickListener {
            val intent = Intent(this, MedicoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Función para actualizar opciones según la selección
    private fun actualizarOpciones(tipo: String) {
        contenedorOpciones.removeAllViews()

        when (tipo) {
            "Tratamiento" -> {
                val editText = EditText(this)
                editText.hint = "Describe el tratamiento"
                contenedorOpciones.addView(editText)
            }
            "Rutina" -> {
                val opcionesRutina = listOf("Baño", "Desparasitación", "Revisión General")
                for (opcion in opcionesRutina) {
                    val checkBox = CheckBox(this)
                    checkBox.text = opcion
                    contenedorOpciones.addView(checkBox)
                }
            }
            "Vacunación" -> {
                val vacunas = listOf("Rabia", "Moquillo", "Parvovirus", "Hepatitis", "Leptospirosis")
                val spinnerVacuna = Spinner(this)
                val adapterVacuna = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vacunas)
                spinnerVacuna.adapter = adapterVacuna
                contenedorOpciones.addView(spinnerVacuna)
            }
            "Esterilización" -> {
                val textView = TextView(this)
                textView.text = "No hay opciones adicionales para esta atención."
                contenedorOpciones.addView(textView)
            }
        }
    }

    // Función para obtener detalles de la selección
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

