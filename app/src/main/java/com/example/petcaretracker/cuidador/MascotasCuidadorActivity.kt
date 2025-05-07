package com.example.petcaretracker.cuidador

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
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

import com.example.petcaretracker.owner.MascotasAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MascotasCuidadorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private val mascotasList = mutableListOf<Mascota>()
    private lateinit var adapter: MascotasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reutiliza el mismo layout de dueños
        setContentView(R.layout.activity_mascotas_veterinario)

        // Firestore & userId
        db = FirebaseFirestore.getInstance()
        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId", "") ?: ""
        if (userId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Vistas
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView       = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewMascotasVeterinario)

        // Toolbar + DrawerToggle
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // RecyclerView & Adapter
        adapter = MascotasAdapter(mascotasList, this) { mascota ->
            // Opcional: mostrar detalles o nada
            Toast.makeText(this, "Mascota: ${mascota.nombre}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter        = adapter

        // Carga datos de header
        FirebaseService.obtenerUsuarioActual(userId) { usuarioData, _ ->
            usuarioData?.let {
                val header = navigationView.getHeaderView(0)
                header.findViewById<TextView>(R.id.tvNombreUsuario).text =
                    it["nombre_completo"].toString()
                header.findViewById<TextView>(R.id.tvCorreoUsuario).text =
                    it["correo_electronico"].toString()
            }
        }

        // Drawer menu
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_perfil   ->
                    startActivity(Intent(this, EditarPerfilActivity::class.java))
                R.id.nav_settings ->
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                R.id.nav_logout   -> {
                    with(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()){
                        remove("userId"); apply()
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Bottom nav
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> startActivity(Intent(this, HomeCuidadorActivity::class.java))
                R.id.nav_medico    -> startActivity(Intent(this, MedicoCuidadroActivity::class.java))
                R.id.nav_ubicacion -> startActivity(Intent(this, UbicacionCuidadorActivity::class.java))
                R.id.nav_mascota   ->
                    Toast.makeText(this, "Ya estás aquí", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Cargar mascotas asignadas
        cargarMascotasAsignadas()
    }

    private fun cargarMascotasAsignadas() {
        // Recorre todos los dueños y filtra las mascotas cuyo cuidador_id sea este userId
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { usuariosSnap ->
                mascotasList.clear()
                val usuarios = usuariosSnap.documents
                var cuentasProcesadas = 0
                if (usuarios.isEmpty()) {
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }
                for (usuarioDoc in usuarios) {
                    val ownerId = usuarioDoc.id
                    db.collection("usuarios")
                        .document(ownerId)
                        .collection("mascotas")
                        .whereEqualTo("cuidador_id", userId)
                        .get()
                        .addOnSuccessListener { mascotasSnap ->
                            for (mascotaDoc in mascotasSnap) {
                                val m = Mascota(
                                    id       = mascotaDoc.id,
                                    nombre   = mascotaDoc.getString("nombre_mascota") ?: "",
                                    raza     = mascotaDoc.getString("raza")            ?: "",
                                    tipo     = mascotaDoc.getString("tipo")            ?: "",
                                    fotoUrl  = mascotaDoc.getString("foto")            ?: ""
                                )
                                mascotasList.add(m)
                            }
                            cuentasProcesadas++
                            if (cuentasProcesadas == usuarios.size) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                            cuentasProcesadas++
                            if (cuentasProcesadas == usuarios.size) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener mascotas", Toast.LENGTH_SHORT).show()
            }
    }
}
