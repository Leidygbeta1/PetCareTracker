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

class MapsRecorridoActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var userId: String
    private lateinit var spinnerRecorridos: Spinner

    private var recorridosMap = mutableMapOf<String, List<Pair<Double,Double>>>()

    private var modo       : String = "general"   // "general" | "mascota"
    private var mascotaId  : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_recorrido)

        spinnerRecorridos = findViewById(R.id.spinnerRecorridos)

        modo      = intent.getStringExtra("modo") ?: "general"
        mascotaId = intent.getStringExtra("mascotaId")

        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId","") ?: ""

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(gMap: GoogleMap) {
        mMap = gMap

        if (modo == "mascota" && mascotaId != null) {
            cargarRecorridosMascota(mascotaId!!)
        } else {
            cargarRecorridosGenerales()
        }
    }

    /* ----------------------------- generales ---------------------------- */
    private fun cargarRecorridosGenerales(){
        FirebaseService.obtenerRecorridosConNombre(userId){ lista ->
            if (lista.isEmpty()){
                Toast.makeText(this,"No hay recorridos guardados",Toast.LENGTH_SHORT).show()
                return@obtenerRecorridosConNombre
            }
            asignarAlSpinner(lista.toMap())
        }
    }

    /* ------------------------------ mascota ----------------------------- */
    private fun cargarRecorridosMascota(mId:String){
        FirebaseService.obtenerRecorridosPorMascota(userId, mId){ lista ->
            if (lista.isEmpty()){
                Toast.makeText(this,"No hay paseos guardados para esta mascota",Toast.LENGTH_SHORT).show()
                return@obtenerRecorridosPorMascota
            }
            asignarAlSpinner(lista.toMap())
        }
    }

    /* --------------------------- dibuja en mapa ------------------------- */
    private fun asignarAlSpinner(info: Map<String, List<Pair<Double,Double>>>) {
        recorridosMap = info.toMutableMap()
        val nombres   = info.keys.toList()

        spinnerRecorridos.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, nombres
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerRecorridos.setOnItemSelectedListener(object: android.widget.AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: android.view.View?, pos:Int, id:Long){
                val nombre = nombres[pos]
                recorrer(recorridosMap[nombre] ?: return)
            }
            override fun onNothingSelected(p0: android.widget.AdapterView<*>?) {}
        })
    }

    private fun recorrer(coords: List<Pair<Double,Double>>){
        val pts = coords.map { LatLng(it.first, it.second) }
        mMap.clear()

        pts.forEachIndexed { idx, p ->
            val titulo = when (idx){
                0 -> "Inicio"
                pts.lastIndex -> "Fin"
                else -> "Parada $idx"
            }
            mMap.addMarker(MarkerOptions().position(p).title(titulo))
        }

        mMap.addPolyline(
            PolylineOptions().addAll(pts).width(10f).color(Color.BLUE)
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pts.first(),15f))
    }
}
