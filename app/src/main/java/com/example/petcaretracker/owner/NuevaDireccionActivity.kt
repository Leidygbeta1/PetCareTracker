package com.example.petcaretracker.owner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class NuevaDireccionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var edtNombre: EditText
    private lateinit var edtDescripcion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var layoutFormulario: LinearLayout
    private lateinit var userId: String

    private var puntoSeleccionado: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_direccion)

        edtNombre = findViewById(R.id.edtNombre)
        edtDescripcion = findViewById(R.id.edtDescripcion)
        btnGuardar = findViewById(R.id.btnGuardar)
        layoutFormulario = findViewById(R.id.formularioLayout)

        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId", "") ?: ""

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnGuardar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val descripcion = edtDescripcion.text.toString().trim()
            val punto = puntoSeleccionado

            if (nombre.isEmpty() || descripcion.isEmpty() || punto == null) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseService.guardarDireccion(
                userId, nombre, descripcion, punto.latitude, punto.longitude
            ) { success ->
                if (success) {
                    Toast.makeText(this, "Dirección guardada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        // Obtener ubicación actual
        val fused = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true
        fused.lastLocation.addOnSuccessListener {
            val ubicacion = LatLng(it.latitude, it.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16f))
        }

        mMap.setOnMapClickListener { latLng ->
            puntoSeleccionado = latLng
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            layoutFormulario.visibility = LinearLayout.VISIBLE
        }
    }
}

