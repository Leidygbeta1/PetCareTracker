package com.example.petcaretracker.veterinario

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.example.petcaretracker.veterinario.HomeVeterinarioActivity
import java.util.*

class ControlFisicoActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var edtPeso: EditText
    private lateinit var edtTalla: EditText
    private lateinit var edtFecha: EditText
    private lateinit var btnGuardar: Button
    private lateinit var userId: String

    private val mascotasMap = mutableMapOf<String, Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_fisico)

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        edtPeso = findViewById(R.id.edtPeso)
        edtTalla = findViewById(R.id.edtTalla)
        edtFecha = findViewById(R.id.edtFecha)
        btnGuardar = findViewById(R.id.btnGuardar)

        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "") ?: ""

        cargarMascotas()
        crearCanalNotificaciones()

        val calendar = Calendar.getInstance()

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

        btnGuardar.setOnClickListener {
            val nombreSeleccionado = spinnerMascotas.selectedItem?.toString() ?: return@setOnClickListener
            val mascota = mascotasMap[nombreSeleccionado] ?: return@setOnClickListener

            val peso = edtPeso.text.toString()
            val talla = edtTalla.text.toString()
            val fecha = edtFecha.text.toString()

            if (peso.isEmpty() || talla.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseService.guardarOActualizarControlFisico(
                userId = mascota["user_id"].toString(),
                mascotaId = mascota["id"].toString(),
                fecha = fecha,
                peso = peso,
                talla = talla
            ) { success ->
                if (success) {
                    mostrarNotificacion(nombreSeleccionado, fecha)
                    Toast.makeText(this, "Control guardado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarMascotas() {
        FirebaseService.obtenerMascotasPorVeterinario2(userId) { lista ->
            if (lista.isNullOrEmpty()) {
                Toast.makeText(this, "No se encontraron mascotas asociadas", Toast.LENGTH_SHORT).show()
                return@obtenerMascotasPorVeterinario2
            }
            val nombres = lista.map { it["nombre_mascota"].toString() }
            lista.forEach { mascotasMap[it["nombre_mascota"].toString()] = it }

            spinnerMascotas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "control_fisico",
                "Registro Físico",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para registrar peso y talla"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacion(nombreMascota: String, fecha: String) {
        val intent = Intent(this, HomeVeterinarioActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "control_fisico")
            .setSmallIcon(R.drawable.ic_pet)
            .setContentTitle("Control actualizado")
            .setContentText("Se registró control físico de $nombreMascota el $fecha")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2002, builder.build())
    }
}
