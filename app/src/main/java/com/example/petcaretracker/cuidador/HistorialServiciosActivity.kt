package com.example.petcaretracker.cuidador

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import java.util.*

class HistorialServiciosActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnBuscarHistorial: Button
    private lateinit var recyclerHistorial: RecyclerView
    private lateinit var btnAgregarServicio: Button
    private lateinit var cuidadorId: String

    private val mascotasAsignadas = mutableListOf<Pair<String, String>>() // Pair<nombre, dueñoId>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_servicios)

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        btnBuscarHistorial = findViewById(R.id.btnBuscar)
        recyclerHistorial = findViewById(R.id.recyclerServicios)
        btnAgregarServicio = findViewById(R.id.btnAgregar)

        cuidadorId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "") ?: ""

        recyclerHistorial.layoutManager = LinearLayoutManager(this)

        cargarMascotasAsignadas()

        btnBuscarHistorial.setOnClickListener {
            val index = spinnerMascotas.selectedItemPosition
            if (index in mascotasAsignadas.indices) {
                val (nombreMascota, duenioId) = mascotasAsignadas[index]
                FirebaseService.obtenerIdMascotaPorNombre(nombreMascota, duenioId) { mascotaId ->
                    if (mascotaId != null) {
                        cargarHistorial(duenioId, mascotaId)
                    } else {
                        Toast.makeText(this, "Error al obtener la mascota", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnAgregarServicio.setOnClickListener {
            val index = spinnerMascotas.selectedItemPosition
            if (index in mascotasAsignadas.indices) {
                val (nombreMascota, duenioId) = mascotasAsignadas[index]
                FirebaseService.obtenerIdMascotaPorNombre(nombreMascota, duenioId) { mascotaId ->
                    if (mascotaId != null) {
                        mostrarDialogoAgregarServicio(duenioId, mascotaId, nombreMascota)
                    } else {
                        Toast.makeText(this, "No se pudo obtener la mascota", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        crearCanalNotificacion()

    }

    private fun cargarMascotasAsignadas() {
        FirebaseService.obtenerMascotasAsignadasAlCuidador(cuidadorId) { lista ->
            if (lista.isEmpty()) {
                Toast.makeText(this, "No tienes mascotas asignadas", Toast.LENGTH_SHORT).show()
                return@obtenerMascotasAsignadasAlCuidador
            }
            mascotasAsignadas.clear()
            val nombres = lista.map {
                mascotasAsignadas.add(it.nombre to it.duenoId)
                it.nombre
            }
            spinnerMascotas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        }
    }

    private fun cargarHistorial(duenoId: String, mascotaId: String) {
        FirebaseService.obtenerHistorialCuidador(duenoId, mascotaId) { historial ->
            if (historial.isNotEmpty()) {
                val adapter = ServicioAdapter(historial)
                recyclerHistorial.adapter = adapter
            } else {
                Toast.makeText(this, "Sin historial para esta mascota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoAgregarServicio(duenoId: String, mascotaId: String, nombreMascota: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registrar servicio para $nombreMascota")

        val view = layoutInflater.inflate(R.layout.dialogo_agregar_servicio, null)
        val spinnerTipo = view.findViewById<Spinner>(R.id.spinnerTipoServicio)
        val edtComentario = view.findViewById<EditText>(R.id.edtComentarioServicio)
        val edtFecha = view.findViewById<EditText>(R.id.edtFechaServicio)

        spinnerTipo.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Paseo", "Baño", "Alimentación", "Juego", "Otro")
        )

        // ✅ CORREGIDO: Calendario que se abre correctamente
        edtFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val fechaFormateada = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                edtFecha.setText(fechaFormateada)
            }, year, month, day)

            datePicker.show()
        }

        builder.setView(view)

        builder.setPositiveButton("Guardar") { _, _ ->
            val tipo = spinnerTipo.selectedItem.toString()
            val comentario = edtComentario.text.toString()
            val fecha = edtFecha.text.toString()

            FirebaseService.guardarServicioCuidador(
                duenoId,
                mascotaId,
                cuidadorId,
                tipo,
                comentario,
                fecha
            ) { exito ->
                if (exito) {
                    Toast.makeText(this, "Servicio guardado", Toast.LENGTH_SHORT).show()
                    mostrarNotificacionServicio(tipo, fecha) // <-- aquí

                    cargarHistorial(duenoId, mascotaId)
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
    private fun crearCanalNotificacion() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "servicios_channel",
                "Registro de Servicios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificaciones de servicios registrados por el cuidador"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }
    private fun mostrarNotificacionServicio(tipo: String, fecha: String) {
        val builder = NotificationCompat.Builder(this, "servicios_channel")
            .setSmallIcon(R.drawable.ic_pet) // Usa un ícono válido de tu proyecto
            .setContentTitle("Servicio Registrado")
            .setContentText("Se registró el servicio '$tipo' para la fecha $fecha")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }


}
