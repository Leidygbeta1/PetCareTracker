package com.example.petcaretracker
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.Vacuna  // Importa tu clase Vacuna, asegúrate de tenerla en el paquete correcto
import com.google.firebase.firestore.FirebaseFirestore  // Si usas Firebase para guardar los datos
import java.util.Calendar


class AgregarVacunaActivity : AppCompatActivity() {

    private lateinit var etNombreVacuna: EditText
    private lateinit var etFechaAplicacion: EditText
    private lateinit var etProximaDosis: EditText
    private lateinit var etVeterinario: EditText
    private lateinit var btnGuardarVacuna: Button
    private lateinit var mascotaId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_vacuna)

        // Inicializar vistas
        etNombreVacuna = findViewById(R.id.etNombreVacuna)
        etFechaAplicacion = findViewById(R.id.etFechaAplicacion)
        etProximaDosis = findViewById(R.id.etProximaDosis)
        etVeterinario = findViewById(R.id.etVeterinario)
        btnGuardarVacuna = findViewById(R.id.btnGuardarVacuna)

        // Obtener datos enviados desde la actividad anterior
        mascotaId = intent.getStringExtra("mascotaId") ?: ""
        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "") ?: ""

        // Configurar el DatePickerDialog para el campo de fecha de aplicación
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val date = "$dayOfMonth/${month + 1}/$year"  // Formato dd/MM/yyyy
            etFechaAplicacion.setText(date)
        }

        etFechaAplicacion.setOnClickListener {
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Configurar el DatePickerDialog para el campo de próxima dosis
        val dateSetListener2 = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val date = "$dayOfMonth/${month + 1}/$year"  // Formato dd/MM/yyyy
            etProximaDosis.setText(date)
        }

        etProximaDosis.setOnClickListener {
            DatePickerDialog(this, dateSetListener2, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnGuardarVacuna.setOnClickListener {
            val nombreVacuna = etNombreVacuna.text.toString()
            val fechaAplicacion = etFechaAplicacion.text.toString()
            val proximaDosis = etProximaDosis.text.toString()
            val veterinario = etVeterinario.text.toString()

            if (nombreVacuna.isNotEmpty() && fechaAplicacion.isNotEmpty() && proximaDosis.isNotEmpty() && veterinario.isNotEmpty()) {
                val nuevaVacuna = Vacuna(nombreVacuna, fechaAplicacion, proximaDosis, veterinario)
                FirebaseService.agregarVacuna(userId, mascotaId, nuevaVacuna) { success ->
                    if (success) {
                        Toast.makeText(this, "Vacuna agregada correctamente.", Toast.LENGTH_SHORT).show()
                        finish() // Cerrar la actividad al agregar la vacuna
                    } else {
                        Toast.makeText(this, "Error al agregar vacuna.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Todos los campos deben ser llenados", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

