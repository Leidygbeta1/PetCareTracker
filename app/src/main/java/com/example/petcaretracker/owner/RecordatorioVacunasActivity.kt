package com.example.petcaretracker.owner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.R

class RecordatorioVacunasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordatorios1)

        // Botón para ver el calendario
        val btnVerCalendario: Button = findViewById(R.id.btnVerCalendario)
        btnVerCalendario.setOnClickListener {
            // Llamar a la actividad de ver calendario
            val intent = Intent(this, VerCalendarioActivity::class.java)
            startActivity(intent)
        }

        // Botón para agregar un recordatorio
        val btnAgregarRecordatorio: Button = findViewById(R.id.btnAgregarRecordatorio)
        btnAgregarRecordatorio.setOnClickListener {
            // Llamar a la actividad de agregar recordatorio
            val intent = Intent(this, AgregarRecordatorioActivity::class.java)
            startActivity(intent)
        }

        // Botón para regresar a la pantalla principal
        val btnAtras: Button = findViewById(R.id.btnAtras)
        btnAtras.setOnClickListener {
            // Regresar a la pantalla principal
            finish()
        }
    }
}
