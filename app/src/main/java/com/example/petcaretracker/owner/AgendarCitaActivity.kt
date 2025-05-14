package com.example.petcaretracker.owner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import java.util.*

class AgendarCitaActivity : AppCompatActivity() {

    private lateinit var spinnerVeterinarios: Spinner
    private lateinit var edtFecha: EditText
    private lateinit var edtHora: EditText
    private lateinit var edtMotivo: EditText
    private lateinit var btnAgendar: Button

    private val veterinarioIds = mutableListOf<String>()
    private lateinit var duenioId: String
    private lateinit var duenioNombre: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita)

        // Inicializar vistas
        spinnerVeterinarios = findViewById(R.id.spinnerVeterinarios)
        edtFecha = findViewById(R.id.edtFecha)
        edtHora = findViewById(R.id.edtHora)
        edtMotivo = findViewById(R.id.edtMotivo)
        btnAgendar = findViewById(R.id.btnAgendar)

        // Obtener datos del dueño desde SharedPreferences
        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        duenioId = prefs.getString("userId", "") ?: ""
        duenioNombre = prefs.getString("nombreUsuario", "") ?: "Dueño"

        // Cargar veterinarios disponibles
        cargarVeterinarios()

        val calendar = Calendar.getInstance()

        // Selección de fecha
        edtFecha.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val fecha = String.format("%02d/%02d/%04d", day, month + 1, year)
                    edtFecha.setText(fecha)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Selección de hora
        edtHora.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    val hora = String.format("%02d:%02d", hour, minute)
                    edtHora.setText(hora)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Botón Agendar
        btnAgendar.setOnClickListener {
            val index = spinnerVeterinarios.selectedItemPosition
            val vetId = veterinarioIds.getOrNull(index)
            val vetNombre = spinnerVeterinarios.selectedItem?.toString() ?: ""
            val fecha = edtFecha.text.toString()
            val hora = edtHora.text.toString()
            val motivo = edtMotivo.text.toString()

            if (fecha.isEmpty() || hora.isEmpty() || vetId == null || motivo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (Opcional) Validar horario (por ejemplo 08:00 a 18:00)
            val horaSplit = hora.split(":")
            val horaInt = horaSplit[0].toIntOrNull()
            if (horaInt != null && (horaInt < 8 || horaInt > 18)) {
                Toast.makeText(this, "Horario fuera de atención (08:00 a 18:00)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Verificar disponibilidad antes de guardar
            FirebaseService.verificarDisponibilidadCita(vetId, fecha, hora) { disponible ->
                if (disponible) {
                    FirebaseService.guardarCita(
                        duenioId,
                        duenioNombre,
                        vetId,
                        vetNombre,
                        fecha,
                        hora,
                        motivo
                    ) { success ->
                        if (success) {
                            Toast.makeText(this, "Cita agendada correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error al guardar la cita", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "El veterinario ya tiene una cita en ese horario", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cargarVeterinarios() {
        FirebaseService.obtenerVeterinarios { veterinarios ->
            if (veterinarios != null && veterinarios.isNotEmpty()) {
                val nombres = veterinarios.map {
                    veterinarioIds.add(it["id"].toString())
                    it["nombre_completo"].toString()
                }
                spinnerVeterinarios.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombres
                )
            } else {
                Toast.makeText(this, "No se encontraron veterinarios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
