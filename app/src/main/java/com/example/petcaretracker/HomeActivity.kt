package com.example.petcaretracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat

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

        // 🔹 Configurar Toolbar para que funcione con el DrawerLayout
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar el RecyclerView con 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = FuncionesAdapter(getFunciones())

        // Configurar el menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
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

    // Manejar apertura y cierre del drawer con el botón de menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) { // ✅ Corrección aquí
                drawerLayout.closeDrawers()
            } else {
                drawerLayout.openDrawer(GravityCompat.START) // ✅ Corrección aquí
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFunciones(): List<String> {
        return listOf("Funcionalidad 1", "Funcionalidad 2", "Funcionalidad 3", "Funcionalidad 4")
    }
}
