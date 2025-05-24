package com.example.petcaretracker.cuidador

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R

class ProgramarPaseoActivity : AppCompatActivity() {

    // ---------- UI ----------
    private lateinit var edtCantidad      : EditText
    private lateinit var btnCargarMascotas: Button
    private lateinit var btnIniciarPaseo  : Button
    private lateinit var layoutMascotas   : LinearLayout

    // ---------- Datos ----------
    private lateinit var cuidadorId: String
    private val mascotasAsignadas = mutableListOf<Pair<String,String>>() // (nombre, dueñoId)

    // límite de seleccionados que el usuario ingresó
    private var maxSeleccion = 0
    private var seleccionados = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programar_paseo)

        // UI
        edtCantidad       = findViewById(R.id.edtCantidadMascotas)
        btnCargarMascotas = findViewById(R.id.btnSeleccionar)
        btnIniciarPaseo   = findViewById(R.id.btnIniciarPaseo)
        layoutMascotas    = findViewById(R.id.layoutCheckBoxes)

        cuidadorId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId", "") ?: ""

        // Cargar listado completo ↓
        btnCargarMascotas.setOnClickListener { prepararListado() }

        btnIniciarPaseo.setOnClickListener { iniciarPaseo() }
    }

    /* --------------------------------------------------------------------- */
    /*                        CARGA Y PRESENTA MASCOTAS                       */
    /* --------------------------------------------------------------------- */
    private fun prepararListado() {
        maxSeleccion = edtCantidad.text.toString().toIntOrNull() ?: 0
        if (maxSeleccion <= 0) {
            Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }
        // Obtener TODAS las mascotas asignadas
        FirebaseService.obtenerMascotasAsignadasAlCuidador(cuidadorId) { lista ->
            if (lista.isEmpty()) {
                Toast.makeText(this, "No tienes mascotas asignadas", Toast.LENGTH_SHORT).show()
                return@obtenerMascotasAsignadasAlCuidador
            }

            layoutMascotas.removeAllViews()
            mascotasAsignadas.clear()
            seleccionados = 0                                   // reset

            lista.forEach { mascota ->
                mascotasAsignadas += mascota.nombre to mascota.duenoId

                // Creamos un CheckBox por mascota
                val cb = CheckBox(this).apply {
                    text = mascota.nombre
                }

                // Escuchamos cambios para respetar el límite
                cb.setOnCheckedChangeListener { _, isChecked ->
                    seleccionados += if (isChecked) 1 else -1

                    // Si se alcanzó el máximo, desactivamos los que no estén marcados
                    if (seleccionados >= maxSeleccion) {
                        bloquearDesbloquear(false)
                    } else {
                        bloquearDesbloquear(true)
                    }
                }

                layoutMascotas.addView(cb)
            }
        }
    }

    /** Bloquea o libera los CheckBox NO seleccionados */
    private fun bloquearDesbloquear(habilitarNoMarcados: Boolean) {
        for (i in 0 until layoutMascotas.childCount) {
            val cb = layoutMascotas.getChildAt(i) as CheckBox
            // solo cambiamos los que no están marcados
            if (!cb.isChecked) cb.isEnabled = habilitarNoMarcados
        }
    }

    /* --------------------------------------------------------------------- */
    /*                       ENVÍA AL MAPA PARA RASTREAR                      */
    /* --------------------------------------------------------------------- */
    private fun iniciarPaseo() {
        val nombres = arrayListOf<String>()
        val duenos  = arrayListOf<String>()

        for (i in 0 until layoutMascotas.childCount) {
            val cb = layoutMascotas.getChildAt(i) as CheckBox
            if (cb.isChecked) {
                nombres += mascotasAsignadas[i].first
                duenos  += mascotasAsignadas[i].second
            }
        }

        if (nombres.size != maxSeleccion) {
            Toast.makeText(
                this,
                "Debes marcar exactamente $maxSeleccion mascota(s)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Abrir el mapa para trazar el recorrido
        startActivity(
            Intent(this, MapsRecorridoActivity::class.java).apply {
                putStringArrayListExtra("nombresMascotas", nombres)
                putStringArrayListExtra("duenosMascotas" , duenos)
            }
        )
    }
}

