package com.example.petcaretracker.owner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R

class AgregarMascotaActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextRaza: EditText
    private lateinit var editTextTipo: EditText
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var imageViewMascota: ImageView
    private var imageUri: Uri? = null // Para almacenar la imagen seleccionada
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_mascota)

        // Obtener userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar componentes
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextRaza = findViewById(R.id.editTextRaza)
        editTextTipo = findViewById(R.id.editTextTipo)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        btnGuardar = findViewById(R.id.btnGuardar)
        imageViewMascota = findViewById(R.id.imageViewMascota)

        // Evento para seleccionar foto
        btnSeleccionarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100) // Código de solicitud para imagen
        }

        // Evento para guardar mascota
        btnGuardar.setOnClickListener {
            val nombre = editTextNombre.text.toString().trim()
            val raza = editTextRaza.text.toString().trim()
            val tipo = editTextTipo.text.toString().trim()

            if (nombre.isEmpty() || raza.isEmpty() || tipo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseService.agregarMascota(
                userId,
                nombre,
                raza,
                tipo,
                imageUri
            ) { success, message ->
                if (success) {
                    Toast.makeText(this, "Mascota guardada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    // Manejar selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageViewMascota.setImageURI(imageUri)
        }
    }
}

