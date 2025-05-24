package com.example.petcaretracker.owner

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import java.util.*

class AgendarCitaActivity : AppCompatActivity() {

    private lateinit var spinnerVeterinarios: Spinner
    private lateinit var edtFecha: EditText
    private lateinit var spinnerHorasDisponibles: Spinner
    private lateinit var edtMotivo: EditText
    private lateinit var btnAgendar: Button

    private val veterinarioIds = mutableListOf<String>()
    private lateinit var duenioId: String
    private lateinit var duenioNombre: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita)

        spinnerVeterinarios = findViewById(R.id.spinnerVeterinarios)
        edtFecha = findViewById(R.id.edtFecha)
        spinnerHorasDisponibles = findViewById(R.id.spinnerHorasDisponibles)
        edtMotivo = findViewById(R.id.edtMotivo)
        btnAgendar = findViewById(R.id.btnAgendar)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        duenioId = prefs.getString("userId", "") ?: ""
        duenioNombre = prefs.getString("nombreUsuario", "") ?: "Dueño"

        crearCanalNotificaciones()
        cargarVeterinarios()

        val calendar = Calendar.getInstance()

        edtFecha.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val fecha = String.format("%02d/%02d/%04d", day, month + 1, year)
                    edtFecha.setText(fecha)

                    val index = spinnerVeterinarios.selectedItemPosition
                    val vetId = veterinarioIds.getOrNull(index) ?: return@DatePickerDialog
                    cargarHorasDisponibles(vetId, fecha)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnAgendar.setOnClickListener {
            val index = spinnerVeterinarios.selectedItemPosition
            val vetId = veterinarioIds.getOrNull(index)
            val vetNombre = spinnerVeterinarios.selectedItem?.toString() ?: ""
            val fecha = edtFecha.text.toString()
            val hora = spinnerHorasDisponibles.selectedItem?.toString() ?: ""
            val motivo = edtMotivo.text.toString()

            if (fecha.isEmpty() || hora.isEmpty() || vetId == null || motivo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                            mostrarNotificacion(vetNombre, fecha, hora)
                            Toast.makeText(this, "Cita agendada correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error al guardar la cita", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "El veterinario ya tiene una cita o no está disponible en ese horario", Toast.LENGTH_LONG).show()
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

    private fun cargarHorasDisponibles(vetId: String, fecha: String) {
        FirebaseService.obtenerHorariosNoDisponibles(vetId) { horariosND ->
            FirebaseService.obtenerCitasDelVeterinarioSimple(vetId) { citas ->
                val ocupadas = mutableSetOf<Pair<Int, Int>>()  // ejemplo: (8, 0) representa 08:00

                // Citas ocupadas (exactas)
                citas.filter { it["fecha"] == fecha }.forEach { cita ->
                    val horaStr = cita["hora"].toString()
                    val parts = horaStr.split(":")
                    val h = parts[0].toIntOrNull()
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    if (h != null) {
                        ocupadas.add(Pair(h, m))
                    }
                }

                // Horarios no disponibles
                horariosND.filter { it["fecha"] == fecha }.forEach { horario ->
                    val inicioStr = (horario["hora_inicio"] as String).split(":")
                    val finStr = (horario["hora_fin"] as String).split(":")
                    val hInicio = inicioStr[0].toIntOrNull()
                    val hFin = finStr[0].toIntOrNull()
                    val mInicio = inicioStr.getOrNull(1)?.toIntOrNull() ?: 0
                    val mFin = finStr.getOrNull(1)?.toIntOrNull() ?: 0

                    if (hInicio != null && hFin != null) {
                        var h = hInicio
                        var m = mInicio
                        while (h < hFin || (h == hFin && m < mFin)) {
                            ocupadas.add(Pair(h, m))
                            m += 30
                            if (m >= 60) {
                                m = 0
                                h++
                            }
                        }
                    }
                }

                // Crear todos los intervalos de 30 minutos entre 08:00 y 18:00
                val disponibles = mutableListOf<String>()
                var h = 8
                var m = 0
                while (h < 18 || (h == 18 && m == 0)) {
                    val inicio = Pair(h, m)
                    val fin = if (m == 0) Pair(h, 30) else Pair(h + 1, 0)

                    if (!ocupadas.contains(inicio)) {
                        val label = "%02d:%02d - %02d:%02d".format(inicio.first, inicio.second, fin.first, fin.second)
                        disponibles.add(label)
                    }

                    m += 30
                    if (m >= 60) {
                        m = 0
                        h++
                    }
                }

                if (disponibles.isEmpty()) {
                    Toast.makeText(this, "No hay horas disponibles para esta fecha", Toast.LENGTH_LONG).show()
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, disponibles)
                spinnerHorasDisponibles.adapter = adapter
            }
        }
    }


    private fun crearCanalNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "citas_channel",
                "Recordatorio de Citas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificaciones de citas"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacion(vetNombre: String, fecha: String, hora: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "citas_channel")
            .setSmallIcon(R.drawable.ic_pet)
            .setContentTitle("Cita Agendada")
            .setContentText("Con $vetNombre el $fecha a las $hora")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, builder.build())
    }
}

