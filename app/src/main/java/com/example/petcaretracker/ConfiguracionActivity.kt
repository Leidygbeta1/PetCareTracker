package com.example.petcaretracker

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var switchNotificaciones: Switch
    private lateinit var switchTemaOscuro: Switch
    private lateinit var btnEliminarCuenta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        toolbar = findViewById(R.id.toolbar)

        switchNotificaciones = findViewById(R.id.switchNotificaciones)
        switchTemaOscuro = findViewById(R.id.switchTemaOscuro)
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // SharedPreferences para guardar cambios
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

        // Cambios de switches (ejemplo)
        switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
        }

        switchTemaOscuro.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Tema oscuro activado" else "Tema claro activado", Toast.LENGTH_SHORT).show()
        }

        btnEliminarCuenta.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _: DialogInterface, _: Int ->
                    if (userId != null) {
                        FirebaseService.eliminarCuenta(userId) { exito ->
                            if (exito) {
                                with(sharedPreferences.edit()) {
                                    remove("userId")
                                    apply()
                                }
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {
                    startActivity(Intent(this, EditarPerfilActivity::class.java))
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Ya estás en Configuración", Toast.LENGTH_SHORT).show()
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

        // Menú inferior
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                R.id.nav_medico -> {
                    startActivity(Intent(this, MedicoActivity::class.java))
                    finish()
                }
                R.id.nav_ubicacion -> {
                    startActivity(Intent(this, UbicacionActivity::class.java))
                    finish()
                }
                R.id.nav_mascota -> {
                    startActivity(Intent(this, MascotasActivity::class.java))
                    finish()
                }
            }
            true
        }
    }
}
