package com.example.petcaretracker.cuidador

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
import com.example.petcaretracker.*

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class HomeCuidadorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)   // ¡Reutilizamos el mismo layout!

        drawerLayout  = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = FuncionesAdapter(getFuncionesCuidador())

        // --- Cargar datos del usuario (igual a veterinario) ---
        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)
        Log.d("HomeCuidador", "userId: $userId")

        if (userId != null) {
            FirebaseService.obtenerUsuarioActual(userId) { data, _ ->
                if (data != null) {
                    val header = navigationView.getHeaderView(0)
                    header.findViewById<TextView>(R.id.tvNombreUsuario).text =
                        data["nombre_completo"].toString()
                    header.findViewById<TextView>(R.id.tvCorreoUsuario).text =
                        data["correo_electronico"].toString()
                } else {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // --- Drawer lateral ---
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_perfil   -> startActivity(Intent(this, EditarPerfilActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, ConfiguracionActivity::class.java))
                R.id.nav_logout   -> {
                    Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                    with(prefs.edit()) { clear(); apply() }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // --- Bottom navigation (puedes afinar destinos) ---
        bottomNavigation.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.nav_home      -> {} // Ya estamos aquí
                R.id.nav_medico    -> startActivity(Intent(this, MedicoCuidadorActivity::class.java))
                R.id.nav_ubicacion -> startActivity(Intent(this, UbicacionCuidadorActivity::class.java))
                R.id.nav_mascota   -> startActivity(Intent(this, MascotasCuidadorActivity::class.java))
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawers()
            else
                drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFuncionesCuidador(): List<String> = listOf(
        "Mensajes de dueños",
        "Paseos Programados",
        "Compartir Ubicación en Tiempo Real",
        "Historial de Servicios"
    )
}
