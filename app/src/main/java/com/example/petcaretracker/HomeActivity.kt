package com.example.petcaretracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = FuncionesAdapter(getFunciones())

        // 🔄 Obtener userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        Log.d("HomeActivity", "userId: $userId")

        if (userId != null) {
            // 🔄 Consultar datos del usuario desde Firebase
            FirebaseService.obtenerUsuarioActual(userId) { usuarioData, mascotas ->
                if (usuarioData != null) {
                    val headerView = navigationView.getHeaderView(0)
                    val nombreTextView = headerView.findViewById<TextView>(R.id.tvNombreUsuario)
                    val correoTextView = headerView.findViewById<TextView>(R.id.tvCorreoUsuario)

                    nombreTextView.text = usuarioData["nombre_completo"].toString()
                    correoTextView.text = usuarioData["correo_electronico"].toString()

                    // 🔄 Mostrar datos de mascotas en el Logcat (ejemplo)
                    if (mascotas != null) {
                        for (mascota in mascotas) {
                            Log.d("HomeActivity", "Mascota: ${mascota["nombre"]} - ${mascota["tipo"]} - ${mascota["raza"]}")
                        }
                    }
                } else {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

        // 🔄 Configurar el menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                    // 🔄 Limpiar SharedPreferences al cerrar sesión
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

        // 🔄 Configurar el menú inferior
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show()
                R.id.nav_medico -> {
                    startActivity(Intent(this, MedicoActivity::class.java))
                    finish()
                }
                R.id.nav_ubicacion -> Toast.makeText(this, "Ubicación", Toast.LENGTH_SHORT).show()
                R.id.nav_mascota -> Toast.makeText(this, "Mascotas", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    // 🔄 Manejar apertura y cierre del drawer con el botón de menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers()
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFunciones(): List<String> {
        return listOf("Trackeo de Recorridos", "Alimentacion Proporcional a la mascota", "Mejores Estéticas y Veterinarias Certificadas", "Compartir ubicación")
    }
}

