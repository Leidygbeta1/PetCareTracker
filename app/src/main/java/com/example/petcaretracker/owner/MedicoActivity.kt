package com.example.petcaretracker.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.ConfiguracionActivity
import com.example.petcaretracker.EditarPerfilActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.FuncionesAdapter
import com.example.petcaretracker.LoginActivity
import com.example.petcaretracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MedicoActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medico)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMedico)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        // Configurar Toolbar con DrawerLayout
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔹 Obtener userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            // 🔹 Consultar datos del usuario desde Firebase
            FirebaseService.obtenerUsuarioActual(userId) { usuarioData, mascotas ->
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
        } else {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

        // Configurar el RecyclerView con 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = FuncionesAdapter(getFuncionesMedicas())

        // Configurar el menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {
                    startActivity(Intent(this, EditarPerfilActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
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

        // Configurar el menú inferior
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                R.id.nav_medico -> Toast.makeText(this, "Ya estás en Médico", Toast.LENGTH_SHORT).show()
                R.id.nav_ubicacion -> {
                    startActivity(Intent(this, UbicacionActivity::class.java))
                    finish()
                }
                R.id.nav_mascota -> {
                    val intent = Intent(this, MascotasActivity::class.java)
                    startActivity(intent)
                    finish() // Finaliza la actividad actual para evitar que se acumule en el historial
                }

            }
            true
        }
    }

    private fun getFuncionesMedicas(): List<String> {
        return listOf("Historial Médico", "Recordatorio Vacunas", "Registro Médico", "Carnet de Vacunacion")
    }
}

