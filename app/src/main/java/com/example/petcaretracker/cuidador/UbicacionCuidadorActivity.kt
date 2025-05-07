package com.example.petcaretracker.cuidador

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import com.example.petcaretracker.ConfiguracionActivity
import com.example.petcaretracker.EditarPerfilActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.LoginActivity
import com.example.petcaretracker.R
import com.google.android.gms.common.api.ResolvableApiException

class UbicacionCuidadorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var tvLocationInfo: TextView

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsClient: SettingsClient
    private lateinit var locationCallback: LocationCallback


    private val REQUEST_LOCATION_PERMISSION = 1
    private val REQUEST_CHECK_SETTINGS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion)

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawerLayout)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        tvLocationInfo = findViewById(R.id.tvLocationInfo)

        // Toolbar y Drawer
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Mostrar nombre y correo del usuario logueado
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

        // Configurar menú lateral
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

        // Menú inferior
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeCuidadorActivity::class.java))
                    finish()
                }
                R.id.nav_medico -> {
                    startActivity(Intent(this, MedicoCuidadroActivity::class.java))
                    finish()
                }
                R.id.nav_ubicacion -> Toast.makeText(this, "Ya estás en Ubicación", Toast.LENGTH_SHORT).show()
                R.id.nav_mascota -> {
                    startActivity(Intent(this, MascotasCuidadorActivity::class.java))
                    finish()
                }
            }
            true
        }

        // Inicializar mapa y ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSettingsClient = LocationServices.getSettingsClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermissionAndSettings()
    }

    private fun checkLocationPermissionAndSettings() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        // Crear solicitud de ubicación
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        locationSettingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                habilitarUbicacion()
            }
            .addOnFailureListener {
                // Abrir configuración para activar ubicación
                try {
                    val resolvable = it as ResolvableApiException
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (e: Exception) {
                    Toast.makeText(this, "Activa la ubicación desde ajustes", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun habilitarUbicacion() {
        try {
            mMap.isMyLocationEnabled = true

            // Crear solicitud de ubicación más precisa
            locationRequest = LocationRequest.create().apply {
                interval = 10000 // cada 10 segundos (opcional)
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
                        tvLocationInfo.text = "Lat: ${location.latitude}, Long: ${location.longitude}"
                        // Parar después de obtener una ubicación
                        fusedLocationClient.removeLocationUpdates(this)
                    } else {
                        tvLocationInfo.text = "Ubicación no disponible"
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)

        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al acceder a la ubicación", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissionAndSettings()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            checkLocationPermissionAndSettings()
        }
    }
}
