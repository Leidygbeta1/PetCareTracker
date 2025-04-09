package com.example.petcaretracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.cuidador.HomeCuidadorActivity
import com.example.petcaretracker.owner.HomeActivity
import com.example.petcaretracker.owner.RegisterActivity
import com.example.petcaretracker.veterinario.HomeVeterinarioActivity

class LoginActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val etUsuario: EditText = findViewById(R.id.etUsuario)
        val etContrasena: EditText = findViewById(R.id.etContrasena)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val tvRegistro: TextView = findViewById(R.id.tvRegistro)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val contrasena = etContrasena.text.toString()

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔄 Llamamos a iniciarSesion con retorno de rol
            FirebaseService.iniciarSesion(usuario, contrasena) { success, userId, rol ->
                if (success && userId != null && rol != null) {
                    Toast.makeText(this, "Bienvenido $usuario", Toast.LENGTH_SHORT).show()

                    // Guardar en SharedPreferences
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("userId", userId)
                        putString("rol", rol)
                        apply()
                    }

                    // Redirección según el rol
                    when (rol) {
                        "Veterinario" -> startActivity(Intent(this, HomeVeterinarioActivity::class.java))
                        "Cuidador" -> startActivity(Intent(this, HomeCuidadorActivity::class.java))
                        else -> startActivity(Intent(this, HomeActivity::class.java)) // Owner
                    }

                    finish()
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}

