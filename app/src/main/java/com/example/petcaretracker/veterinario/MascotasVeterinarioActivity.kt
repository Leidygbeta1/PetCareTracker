package com.example.petcaretracker.veterinario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.*

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MascotasVeterinarioActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewMascotas: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userId: String
    private val mascotasList = mutableListOf<MascotaVeterinario>()
    private lateinit var adapter: MascotasVeterinarioAdapter
    private lateinit var mensajeVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascotas_veterinario)


        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance()

        // Configurar navegación
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        recyclerViewMascotas = findViewById(R.id.recyclerViewMascotasVeterinario)
        mensajeVacio = findViewById(R.id.tvMensajeVacio)


        // Configurar Toolbar
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Obtener userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "") ?: ""

        if (userId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Configurar RecyclerView
        recyclerViewMascotas.layoutManager = LinearLayoutManager(this)
        adapter = MascotasVeterinarioAdapter(mascotasList)
        recyclerViewMascotas.adapter = adapter

        // Cargar mascotas asociadas al veterinario
        cargarMascotasPorVeterinario()

        // Cargar datos en la barra lateral
        FirebaseService.obtenerUsuarioActual(userId) { usuarioData, _ ->
            if (usuarioData != null) {
                val headerView = navigationView.getHeaderView(0)
                val nombreTextView = headerView.findViewById<TextView>(R.id.tvNombreUsuario)
                val correoTextView = headerView.findViewById<TextView>(R.id.tvCorreoUsuario)

                nombreTextView.text = usuarioData["nombre_completo"].toString()
                correoTextView.text = usuarioData["correo_electronico"].toString()
            }
        }

        // Configurar menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> startActivity(Intent(this, EditarPerfilActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, ConfiguracionActivity::class.java))
                R.id.nav_logout -> {
                    with(sharedPreferences.edit()) {
                        remove("userId")
                        apply()
                    }
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Configurar menú inferior
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeVeterinarioActivity::class.java))
                R.id.nav_medico -> {
                    startActivity(Intent(this, MedicoVeterinarioActivity::class.java))
                    finish()
                }
                R.id.nav_ubicacion -> {
                    startActivity(Intent(this, UbicacionActivity::class.java))
                    finish()
                }
                R.id.nav_mascota -> Toast.makeText(this, "Ya estás en Mascotas", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun cargarMascotasPorVeterinario() {
        FirebaseService.obtenerMascotasPorVeterinario(userId) { mascotasConDueños ->
            mascotasList.clear()
            if (mascotasConDueños != null && mascotasConDueños.isNotEmpty()) {
                mascotasList.addAll(mascotasConDueños)
                mensajeVacio.text = ""
            } else {
                mensajeVacio.text = "No se encontraron mascotas relacionadas."
            }
            adapter.notifyDataSetChanged()
        }
    }
}