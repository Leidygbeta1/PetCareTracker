// --- AgendaCitasActivity.kt ---
package com.example.petcaretracker.veterinario

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.example.petcaretracker.model.Cita
import java.text.SimpleDateFormat
import java.util.*

class AgendaCitasActivity : AppCompatActivity() {

    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var btnSeleccionarFecha: Button
    private lateinit var recyclerCitas: RecyclerView

    private lateinit var veterinarioId: String
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_citas)

        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        recyclerCitas = findViewById(R.id.recyclerCitas)
        recyclerCitas.layoutManager = LinearLayoutManager(this)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        veterinarioId = prefs.getString("userId", "") ?: ""

        actualizarFechaActual()
        cargarCitasDelDia()

        btnSeleccionarFecha.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                actualizarFechaActual()
                cargarCitasDelDia()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun actualizarFechaActual() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvFechaSeleccionada.text = "Citas para: ${sdf.format(calendar.time)}"
    }

    private fun cargarCitasDelDia() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = sdf.format(calendar.time)

        FirebaseService.obtenerCitasDelDia(veterinarioId, fecha) { citas ->
            if (citas != null) {
                recyclerCitas.adapter = CitaAdapter(citas) { cita ->
                    FirebaseService.marcarCitaComoRealizada(cita.id) { success ->
                        if (success) {
                            Toast.makeText(this, "Cita marcada como realizada", Toast.LENGTH_SHORT).show()
                            cargarCitasDelDia()
                        } else {
                            Toast.makeText(this, "Error al actualizar cita", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error al cargar citas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
