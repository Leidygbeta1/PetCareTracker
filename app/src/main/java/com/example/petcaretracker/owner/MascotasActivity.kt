package com.example.petcaretracker.owner

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
import com.example.petcaretracker.ConfiguracionActivity
import com.example.petcaretracker.EditarPerfilActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.LoginActivity
import com.example.petcaretracker.Mascota
import com.example.petcaretracker.R
import com.example.petcaretracker.UbicacionActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotasActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewMascotas: RecyclerView
    private lateinit var btnAgregarMascota: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private val mascotasList = mutableListOf<Mascota>()
    private lateinit var adapter: MascotasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascotas)

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configurar navegación
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        recyclerViewMascotas = findViewById(R.id.recyclerViewMascotas)
        btnAgregarMascota = findViewById(R.id.btnAgregarMascota)

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
        adapter = MascotasAdapter(mascotasList, this) { mascotaId -> mostrarOpciones(mascotaId) }
        recyclerViewMascotas.adapter = adapter

        // Cargar mascotas desde Firebase
        cargarMascotas()

        // Botón para agregar nueva mascota
        btnAgregarMascota.setOnClickListener {
            startActivity(Intent(this, AgregarMascotaActivity::class.java))
        }

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

        // Configurar navegación lateral
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

        // Configurar menú inferior
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_medico -> startActivity(Intent(this, MedicoActivity::class.java))
                R.id.nav_ubicacion -> {
                    startActivity(Intent(this, UbicacionActivity::class.java))
                    finish()
                }
                R.id.nav_mascota -> Toast.makeText(this, "Ya estás en Mascotas", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun cargarMascotas() {
        db.collection("usuarios").document(userId).collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                mascotasList.clear()
                for (document in result) {
                    val nombre = document.getString("nombre_mascota") ?: "Sin Nombre"
                    val raza = document.getString("raza") ?: "Desconocida"
                    val tipo = document.getString("tipo") ?: "Desconocido"  // Nuevo campo agregado
                    val fotoUrl = document.getString("foto") ?: ""

                    mascotasList.add(Mascota(document.id, nombre, raza, tipo, fotoUrl))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show()
            }
    }


    private fun mostrarOpciones(mascotaId: String) {
        val options = arrayOf("Editar", "Eliminar")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val intent = Intent(this, EditarMascotaActivity::class.java)
                    intent.putExtra("mascotaId", mascotaId)
                    startActivity(intent)
                }
                1 -> eliminarMascota(mascotaId)
            }
        }
        builder.show()
    }

    private fun eliminarMascota(mascotaId: String) {
        db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                cargarMascotas()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }
}


