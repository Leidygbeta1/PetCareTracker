package com.example.petcaretracker.owner

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
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

class MapsRecorridoActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var userId: String
    private lateinit var spinnerRecorridos: Spinner
    private var recorridosMap = mutableMapOf<String, List<Pair<Double, Double>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_recorrido)

        spinnerRecorridos = findViewById(R.id.spinnerRecorridos)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        FirebaseService.obtenerRecorridosConNombre(userId) { lista ->
            if (lista.isEmpty()) {
                Toast.makeText(this, "No hay recorridos guardados", Toast.LENGTH_SHORT).show()
                return@obtenerRecorridosConNombre
            }

            recorridosMap = lista.toMap().toMutableMap()
            val nombres = recorridosMap.keys.toList()

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRecorridos.adapter = adapter

            spinnerRecorridos.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                    val nombreSeleccionado = nombres[position]
                    val coords = recorridosMap[nombreSeleccionado] ?: return
                    val latLngs = coords.map { LatLng(it.first, it.second) }

                    mMap.clear()

// Dibujar los marcadores con títulos dinámicos
                    latLngs.forEachIndexed { index, latLng ->
                        val titulo = when (index) {
                            0 -> "Inicio"
                            latLngs.lastIndex -> "Fin"
                            else -> "Parada $index"
                        }
                        mMap.addMarker(MarkerOptions().position(latLng).title(titulo))
                    }

// Dibujar la línea del recorrido
                    mMap.addPolyline(
                        PolylineOptions()
                            .addAll(latLngs)
                            .width(10f)
                            .color(Color.BLUE)
                    )

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.first(), 15f))

                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            })
        }
    }
}
