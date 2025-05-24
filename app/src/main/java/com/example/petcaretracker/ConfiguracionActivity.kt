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
import androidx.appcompat.app.AppCompatDelegate
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

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var switchNotificaciones: Switch
    private lateinit var switchTemaOscuro: Switch
    private lateinit var btnEliminarCuenta: Button
    private var isSwitchProgrammaticChange = false


    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de super.onCreate
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val temaOscuro = sharedPreferences.getBoolean("tema_oscuro_activado", false)
        AppCompatDelegate.setDefaultNightMode(
            if (temaOscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

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
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val userId = sharedPreferences.getString("userId", null)

        // Mostrar datos del usuario
        if (userId != null) {
            FirebaseService.obtenerUsuarioActual(userId) { usuarioData, _ ->
                usuarioData?.let {
                    val headerView = navigationView.getHeaderView(0)
                    val nombreTextView = headerView.findViewById<TextView>(R.id.tvNombreUsuario)
                    val correoTextView = headerView.findViewById<TextView>(R.id.tvCorreoUsuario)
                    nombreTextView.text = it["nombre_completo"].toString()
                    correoTextView.text = it["correo_electronico"].toString()
                }
            }
        }

        // Recuperar estado
        switchNotificaciones.isChecked = sharedPreferences.getBoolean("notificaciones_activadas", true)
        switchTemaOscuro.isChecked = temaOscuro

        switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notificaciones_activadas", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
        }

        isSwitchProgrammaticChange = true
        switchTemaOscuro.isChecked = temaOscuro
        isSwitchProgrammaticChange = false

        switchTemaOscuro.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchProgrammaticChange) {
                sharedPreferences.edit().putBoolean("tema_oscuro_activado", isChecked).apply()
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                recreate()
            }
        }



        btnEliminarCuenta.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _: DialogInterface, _: Int ->
                    userId?.let {
                        FirebaseService.eliminarCuenta(it) { exito ->
                            if (exito) {
                                sharedPreferences.edit().remove("userId").apply()
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

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> startActivity(Intent(this, EditarPerfilActivity::class.java))
                R.id.nav_settings -> Toast.makeText(this, "Ya estás en Configuración", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    sharedPreferences.edit().remove("userId").apply()
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

