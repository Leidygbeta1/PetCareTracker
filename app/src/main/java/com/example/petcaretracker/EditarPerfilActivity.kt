package com.example.petcaretracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.petcaretracker.cuidador.HomeCuidadorActivity
import com.example.petcaretracker.cuidador.MascotasCuidadorActivity
import com.example.petcaretracker.cuidador.MedicoCuidadorActivity
import com.example.petcaretracker.cuidador.UbicacionCuidadorActivity
import com.example.petcaretracker.owner.HomeActivity
import com.example.petcaretracker.owner.MascotasActivity
import com.example.petcaretracker.owner.MedicoActivity
import com.example.petcaretracker.owner.UbicacionActivity
import com.example.petcaretracker.veterinario.HomeVeterinarioActivity
import com.example.petcaretracker.veterinario.MascotasVeterinarioActivity
import com.example.petcaretracker.veterinario.MedicoVeterinarioActivity
import com.example.petcaretracker.veterinario.UbicacionVeterinarioActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var etNombre: EditText
    private lateinit var etUsuario: EditText
    private lateinit var etCorreo: EditText
    private lateinit var btnGuardar: Button

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        toolbar = findViewById(R.id.toolbar)

        etNombre = findViewById(R.id.etNombre)
        etUsuario = findViewById(R.id.etUsuario)
        etCorreo = findViewById(R.id.etCorreo)
        btnGuardar = findViewById(R.id.btnGuardar)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            FirebaseService.obtenerUsuarioActual(userId) { usuarioData, _ ->
                if (usuarioData != null) {
                    val headerView = navigationView.getHeaderView(0)
                    val nombreTextView = headerView.findViewById<TextView>(R.id.tvNombreUsuario)
                    val correoTextView = headerView.findViewById<TextView>(R.id.tvCorreoUsuario)
                    nombreTextView.text = usuarioData["nombre_completo"].toString()
                    correoTextView.text = usuarioData["correo_electronico"].toString()
                } else {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            }
        }


        if (userId != null) {
            FirebaseService.obtenerUsuarioActual(userId) { usuarioData, _ ->
                usuarioData?.let {
                    etNombre.setText(it["nombre_completo"].toString())
                    etUsuario.setText(it["nombre_usuario"].toString())
                    etCorreo.setText(it["correo_electronico"].toString())
                }
            }
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val usuario = etUsuario.text.toString().trim()
            val correo = etCorreo.text.toString().trim()

            val userId = sharedPreferences.getString("userId", null) ?: return@setOnClickListener

            FirebaseService.actualizarPerfilUsuario(userId, nombre, usuario, correo) { exito ->
                if (exito) {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // Menús igual que en otras actividades
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> Toast.makeText(this, "Ya estás en Perfil", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> startActivity(Intent(this, ConfiguracionActivity::class.java))
                R.id.nav_logout -> {
                    with(sharedPreferences.edit()) {
                        remove("userId")
                        apply()
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val rol = sharedPreferences.getString("rol", "Owner") ?: "Owner"

        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (rol == "Veterinario") {
                        startActivity(Intent(this, HomeVeterinarioActivity::class.java))
                    } else if (rol == "Cuidador") {
                        startActivity(Intent(this, HomeCuidadorActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()
                }

                R.id.nav_medico -> {
                    if (rol == "Veterinario") {
                        startActivity(Intent(this, MedicoVeterinarioActivity::class.java))
                    }else if (rol == "Cuidador") {
                        startActivity(Intent(this, MedicoCuidadorActivity::class.java))
                    } else {
                        startActivity(Intent(this, MedicoActivity::class.java))
                    }
                    finish()
                }

                R.id.nav_ubicacion -> {
                    if (rol == "Veterinario") {
                        startActivity(Intent(this, UbicacionVeterinarioActivity::class.java))
                    }else if (rol == "Cuidador") {
                        startActivity(Intent(this, UbicacionCuidadorActivity::class.java))
                    } else {
                        startActivity(Intent(this, UbicacionActivity::class.java))
                    }
                    finish()
                }

                R.id.nav_mascota -> {
                    if (rol == "Veterinario") {
                        startActivity(Intent(this, MascotasVeterinarioActivity::class.java))
                    }else if (rol == "Cuidador") {
                        startActivity(Intent(this, MascotasCuidadorActivity::class.java))
                    } else {
                        startActivity(Intent(this, MascotasActivity::class.java))
                    }
                    finish()
                }
            }
            true
        }

    }
}
