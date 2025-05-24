package com.example.petcaretracker.owner

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class NuevoRecorridoActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnGuardar: Button
    private lateinit var edtNombre: EditText
    private lateinit var userId: String
    private val puntos = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_recorrido)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""

        btnGuardar = findViewById(R.id.btnGuardarRecorrido)
        edtNombre = findViewById(R.id.edtNombreRecorrido)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnGuardar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            if (puntos.size < 2 || nombre.isEmpty()) {
                Toast.makeText(this, "Agrega al menos 2 puntos y un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val coords = puntos.map { it.latitude to it.longitude }

            FirebaseService.guardarRecorrido(userId, nombre, coords) { success ->
                if (success) {
                    Toast.makeText(this, "Recorrido guardado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar recorrido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnMapClickListener { latLng ->
            puntos.add(latLng)
            mMap.addMarker(MarkerOptions().position(latLng).title("Punto ${puntos.size}"))

            // Limpiar polilíneas anteriores antes de redibujar
            mMap.clear()

            // Redibujar marcadores
            puntos.forEachIndexed { index, punto ->
                mMap.addMarker(MarkerOptions().position(punto).title("Punto ${index + 1}"))
            }

            // Redibujar polilínea
            if (puntos.size > 1) {
                mMap.addPolyline(PolylineOptions().addAll(puntos).width(6f).color(Color.BLUE))
            }
        }

        val centro = LatLng(4.685960, -74.101050)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, 15f))
    }

}

