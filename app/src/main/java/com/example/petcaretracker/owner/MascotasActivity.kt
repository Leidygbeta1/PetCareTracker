package com.example.petcaretracker.owner

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
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

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MascotasActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewMascotas: RecyclerView
    private lateinit var btnAgregarMascota: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private val mascotasList = mutableListOf<Mascota>()
    private lateinit var adapter: MascotasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascotas)

        // Firestore & userId
        db = FirebaseFirestore.getInstance()
        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId", "") ?: ""
        if (userId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Views
        drawerLayout         = findViewById(R.id.drawerLayout)
        val navigationView  : NavigationView       = findViewById(R.id.navigationView)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val toolbar         : androidx.appcompat.widget.Toolbar =
            findViewById(R.id.toolbar)
        recyclerViewMascotas = findViewById(R.id.recyclerViewMascotas)
        btnAgregarMascota    = findViewById(R.id.btnAgregarMascota)

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
            mostrarOpciones(mascota)
        }
        recyclerViewMascotas.layoutManager = LinearLayoutManager(this)
        recyclerViewMascotas.adapter        = adapter

        // Botón Agregar
        btnAgregarMascota.setOnClickListener {
            startActivity(Intent(this, AgregarMascotaActivity::class.java))
        }

        // Header data
        FirebaseService.obtenerUsuarioActual(userId) { usuarioData, _ ->
            if (usuarioData != null) {
                val header = navigationView.getHeaderView(0)
                header.findViewById<TextView>(R.id.tvNombreUsuario).text =
                    usuarioData["nombre_completo"].toString()
                header.findViewById<TextView>(R.id.tvCorreoUsuario).text =
                    usuarioData["correo_electronico"].toString()
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
                    with(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()) {
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
                R.id.nav_home      -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_medico    -> startActivity(Intent(this, MedicoActivity::class.java))
                R.id.nav_ubicacion -> startActivity(Intent(this, UbicacionActivity::class.java))
                R.id.nav_mascota   ->
                    Toast.makeText(this, "Ya estás en Mascotas", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Carga inicial
        cargarMascotas()
    }

    private fun cargarMascotas() {
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                mascotasList.clear()
                for (doc in result) {
                    val nombre      = doc.getString("nombre_mascota") ?: "Sin Nombre"
                    val raza        = doc.getString("raza")            ?: "Desconocida"
                    val tipo        = doc.getString("tipo")            ?: "Desconocido"
                    val fotoUrl     = doc.getString("foto")            ?: ""
                    val cuidadorId  = doc.getString("cuidador_id")
                    val cuidadorNom = doc.getString("cuidador_nombre")
                    mascotasList.add(
                        Mascota(
                            id              = doc.id,
                            nombre          = nombre,
                            raza            = raza,
                            tipo            = tipo,
                            fotoUrl         = fotoUrl,
                            cuidadorId      = cuidadorId,
                            cuidadorNombre = cuidadorNom
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarOpciones(m: Mascota) {
        val opts = if (m.cuidadorId.isNullOrEmpty()) {
            arrayOf("Editar", "Eliminar", "Agregar cuidador")
        } else {
            arrayOf("Editar", "Eliminar", "Quitar cuidador (${m.cuidadorNombre})")
        }

        AlertDialog.Builder(this)
            .setTitle(m.nombre)
            .setItems(opts) { _, which ->
                when (opts[which]) {
                    "Editar"           -> startActivity(
                        Intent(this, EditarMascotaActivity::class.java)
                            .putExtra("mascotaId", m.id)
                    )
                    "Eliminar"         -> eliminarMascota(m.id)
                    "Agregar cuidador" -> mostrarDialogoCuidadores(m)
                    else               -> actualizarCampoCuidador(m, null, null)
                }
            }
            .show()
    }

    // Aquí se buscan todos los cuidadores y se muestra el diálogo
    private fun mostrarDialogoCuidadores(m: Mascota) {
        obtenerCuidadoresDisponibles { cuidadores ->
            if (cuidadores.isNullOrEmpty()) {
                Toast.makeText(this, "No hay cuidadores disponibles", Toast.LENGTH_SHORT).show()
                return@obtenerCuidadoresDisponibles
            }
            val nombres = cuidadores.map { it["nombre_completo"]!! }
            val ids     = cuidadores.map { it["id"]!! }

            AlertDialog.Builder(this)
                .setTitle("Selecciona cuidador")
                .setItems(nombres.toTypedArray()) { _, idx ->
                    actualizarCampoCuidador(m, ids[idx], nombres[idx])
                }
                .show()
        }
    }

    // Helper que recupera todos los documentos con rol = "Cuidador"
    private fun obtenerCuidadoresDisponibles(
        callback: (List<Map<String, String>>) -> Unit
    ) {
        db.collection("usuarios")
            .whereEqualTo("rol", "Cuidador")
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.map { doc ->
                    mapOf(
                        "id"              to doc.id,
                        "nombre_completo" to (doc.getString("nombre_completo") ?: "Sin Nombre")
                    )
                }
                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    private fun actualizarCampoCuidador(m: Mascota, nuevoId: String?, nuevoNombre: String?) {
        val updates = mapOf(
            "cuidador_id"     to nuevoId,
            "cuidador_nombre" to nuevoNombre
        )
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(m.id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    if (nuevoId == null) "Cuidador quitado" else "Cuidador asignado",
                    Toast.LENGTH_SHORT
                ).show()
                cargarMascotas()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarMascota(mascotaId: String) {
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(mascotaId)
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



