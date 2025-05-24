package com.example.petcaretracker.veterinario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import java.util.*

class HorariosVeterinarioActivity : AppCompatActivity() {

    private lateinit var edtFecha: EditText
    private lateinit var edtHoraInicio: EditText
    private lateinit var edtHoraFin: EditText
    private lateinit var btnGuardar: Button
    private lateinit var listaHorarios: ListView
    private lateinit var listaAdapter: ArrayAdapter<String>

    private lateinit var userId: String
    private val horariosMostrados = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horario_no_disponible)

        edtFecha = findViewById(R.id.edtFechaNoDisponible)
        edtHoraInicio = findViewById(R.id.edtHoraInicio)
        edtHoraFin = findViewById(R.id.edtHoraFin)
        btnGuardar = findViewById(R.id.btnGuardarHorario)
        listaHorarios = findViewById(R.id.listaHorarios)

        listaAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, horariosMostrados)
        listaHorarios.adapter = listaAdapter

        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "") ?: ""

        val cal = Calendar.getInstance()

        edtFecha.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                edtFecha.setText("%02d/%02d/%04d".format(d, m + 1, y))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        edtHoraInicio.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                edtHoraInicio.setText("%02d:%02d".format(h, m))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        edtHoraFin.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                edtHoraFin.setText("%02d:%02d".format(h, m))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnGuardar.setOnClickListener {
            val fecha = edtFecha.text.toString()
            val inicio = edtHoraInicio.text.toString()
            val fin = edtHoraFin.text.toString()

            if (fecha.isEmpty() || inicio.isEmpty() || fin.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseService.guardarHorarioNoDisponible(userId, fecha, inicio, fin) { success ->
                if (success) {
                    Toast.makeText(this, "Horario guardado", Toast.LENGTH_SHORT).show()
                    cargarHorariosNoDisponibles() // Refrescar lista
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cargarHorariosNoDisponibles()
    }

    private fun cargarHorariosNoDisponibles() {
        horariosMostrados.clear()

        // Primero obtenemos horarios bloqueados por el veterinario
        FirebaseService.obtenerHorariosNoDisponibles(userId) { horariosBloqueados ->
            horariosBloqueados.forEach {
                val texto = "${it["fecha"]} de ${it["hora_inicio"]} a ${it["hora_fin"]}"
                horariosMostrados.add("Bloqueado: $texto")
            }

            // Luego obtenemos citas ya agendadas
            FirebaseService.obtenerCitasDelVeterinario(userId) { citas ->
                citas?.forEach {
                    val texto = "${it.fecha} de ${it.hora}"
                    horariosMostrados.add("Cita: $texto")
                }

                // Finalmente actualizamos la lista
                listaAdapter.notifyDataSetChanged()
            }
        }
    }



}
