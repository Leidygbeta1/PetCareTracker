package com.example.petcaretracker.cuidador

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapsRecorridoActivity : AppCompatActivity(), OnMapReadyCallback {

    // ---------------- GPS / MAPA ----------------
    private lateinit var mMap: GoogleMap
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var currentPolyline: Polyline? = null
    private val recorrido = mutableListOf<LatLng>()

    // ---------------- Extras (mascotas) ---------
    private lateinit var nombresMascotas: List<String>
    private lateinit var duenosMascotas : List<String>

    // ---------------- UI ------------------------
    private lateinit var edtNombre: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_recorrido)     // mismo XML que usas para el dueño

        // ► extras que llegan desde ProgramarPaseoActivity
        nombresMascotas = intent.getStringArrayListExtra("nombresMascotas") ?: emptyList()
        duenosMascotas  = intent.getStringArrayListExtra("duenosMascotas")  ?: emptyList()

        // ► UI
        edtNombre  = findViewById(R.id.edtNombreRecorrido)
        btnGuardar = findViewById(R.id.btnGuardarRecorrido)

        // ► Mapa
        (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment)
            .getMapAsync(this)

        fused = LocationServices.getFusedLocationProviderClient(this)
        setupLocationUpdates()

        btnGuardar.setOnClickListener { guardarPaseo() }
    }

    /*----------------  GPS ----------------*/
    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval         = 5_000L          // 5 s
            fastestInterval  = 3_000L          // 3 s
            priority         = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    val p = LatLng(loc.latitude, loc.longitude)
                    recorrido += p
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p, 17f))
                    drawPolyline()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.uiSettings.isZoomControlsEnabled = true

        // ► permitir que el cuidador añada puntos manualmente
        mMap.setOnMapClickListener { p ->
            recorrido += p
            // Dibujamos marcador
            mMap.addMarker(
                MarkerOptions().position(p).title("Punto ${recorrido.size}")
            )
            drawPolyline()
        }

        startUpdates()
    }

    private fun startUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fused.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

    private fun drawPolyline() {
        currentPolyline?.remove()
        currentPolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(recorrido)
                .color(Color.BLUE)
                .width(10f)
        )
    }

    /*-------------  GUARDADO  ---------------*/
    private fun guardarPaseo() {
        if (recorrido.size < 2) {
            Toast.makeText(this, "Traza al menos dos puntos", Toast.LENGTH_SHORT).show()
            return
        }
        if (nombresMascotas.isEmpty()) {
            Toast.makeText(this, "No llegaron mascotas para registrar", Toast.LENGTH_SHORT).show()
            return
        }

        val nombrePaseo = edtNombre.text.toString()
            .ifBlank { "Paseo ${System.currentTimeMillis()}" }

        val coords = recorrido.map { it.latitude to it.longitude }

        // Guarda el recorrido para cada mascota seleccionada
        nombresMascotas.forEachIndexed { idx, nombreM ->
            val duenoId = duenosMascotas[idx]

            FirebaseService.obtenerIdMascotaPorNombre(nombreM, duenoId) { mId ->
                if (mId != null) {
                    FirebaseService.guardarPaseoProgramado(
                        duenoId,           // id del dueño
                        mId,               // id de la mascota
                        nombrePaseo,
                        coords
                    ) { ok ->
                        if (!ok) {
                            Toast.makeText(
                                this,
                                "Error guardando paseo de $nombreM",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        Toast.makeText(this, "Paseo registrado", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onPause() {
        super.onPause()
        fused.removeLocationUpdates(locationCallback)
    }
}
