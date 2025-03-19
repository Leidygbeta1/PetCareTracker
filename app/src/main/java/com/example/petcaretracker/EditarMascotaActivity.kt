package com.example.petcaretracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


class EditarMascotaActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextRaza: EditText
    private lateinit var editTextTipo: EditText
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var imageViewMascota: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private var imageUri: Uri? = null
    private var mascotaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_mascota)

        db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""

        mascotaId = intent.getStringExtra("mascotaId") ?: ""

        editTextNombre = findViewById(R.id.editTextNombre)
        editTextRaza = findViewById(R.id.editTextRaza)
        editTextTipo = findViewById(R.id.editTextTipo)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        btnGuardar = findViewById(R.id.btnGuardar)

        imageViewMascota = findViewById(R.id.imageViewMascota)

        btnGuardar.setOnClickListener { actualizarDatosMascota() }

    }

    private fun actualizarDatosMascota() {
        val datosMascota = mapOf(
            "nombre_mascota" to editTextNombre.text.toString(),
            "raza" to editTextRaza.text.toString(),
            "tipo" to editTextTipo.text.toString()
        )
        db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
            .update(datosMascota)
            .addOnSuccessListener {
                Toast.makeText(this, "Mascota actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
    }


}
