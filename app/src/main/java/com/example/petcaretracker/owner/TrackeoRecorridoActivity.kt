package com.example.petcaretracker.owner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R

class TrackeoRecorridoActivity : AppCompatActivity() {

    // UI
    private lateinit var spinnerOpciones : Spinner
    private lateinit var spinnerMascotas: Spinner
    private lateinit var btnEjecutar    : Button

    // datos
    private var opcionSeleccionada : String? = null
    private var mascotaIdSeleccion : String? = null

    private lateinit var userId: String
    private val opciones = mutableListOf<String>()

    // mapas auxiliares
    private val direccionesMap = mutableMapOf<String, Pair<String, Pair<Double,Double>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trackeo_recorrido)

        spinnerOpciones  = findViewById(R.id.spinnerOpciones)
        spinnerMascotas  = findViewById(R.id.spinnerMascotas)   // <-- nuevo spinner
        btnEjecutar      = findViewById(R.id.btnEjecutar)

        userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId","") ?: ""

        cargarOpciones()
        cargarMascotas()

        // opción elegida
        spinnerOpciones.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                opcionSeleccionada = opciones[pos]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { opcionSeleccionada = null }
        }

        // mascota elegida
        spinnerMascotas.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                mascotaIdSeleccion = p0?.getItemAtPosition(pos) as? String
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { mascotaIdSeleccion = null }
        }

        btnEjecutar.setOnClickListener { ejecutarOpcion() }
    }

    /* ------------------------------------------------------------------ */
    /*                                ACCIONES                            */
    /* ------------------------------------------------------------------ */

    private fun ejecutarOpcion() {
        when (opcionSeleccionada) {

            "Ver paseos de la mascota 🐶" -> {
                if (mascotaIdSeleccion == null) {
                    Toast.makeText(this,"Selecciona una mascota",Toast.LENGTH_SHORT).show()
                    return
                }

                val i = Intent(this, MapsRecorridoActivity2::class.java).apply {
                    putExtra("modo","mascota")
                    putExtra("mascotaId", mascotaIdSeleccion)
                }
                startActivity(i)
            }

            "Agregar nuevo recorrido personalizado ✍️" -> {
                startActivity(Intent(this, NuevoRecorridoActivity::class.java))
            }

            "Ver recorrido en el mapa dentro de la app 🗺️" -> {
                startActivity(Intent(this, MapsRecorridoActivity::class.java)
                    .putExtra("modo","general"))
            }

            "Agregar nueva dirección personalizada 📌" -> {
                startActivity(Intent(this, NuevaDireccionActivity::class.java))
            }

            /* ---- Ejemplos anteriores que abren Google Maps directamente ---- */
            "Mostrar recorrido del paseo 🐾" -> abrirUrl(
                "https://www.google.com/maps/dir/?api=1" +
                        "&origin=Titan+Plaza,Bogotá" +
                        "&destination=Carrera+69g+%2370-50,Bogotá" +
                        "&waypoints=Homecenter+Calle+80,Bogotá|Centro+Comercial+Metropolis,Bogotá" +
                        "&travelmode=walking"
            )

            "Ver ubicación de la casa 🏠" -> abrirUrl(
                "geo:4.683718,-74.101345?q=Carrera+69g+%2370-50,Bogotá"
            )

            "Buscar veterinarias cercanas 🏥" -> abrirUrl("geo:0,0?q=veterinarias+cercanas")

            "Vista Street View 📷" -> abrirUrl("google.streetview:cbll=4.685960,-74.101050")

            else -> {
                // direcciones personalizadas
                direccionesMap[opcionSeleccionada!!]?.let { (_, coord) ->
                    abrirUrl("geo:${coord.first},${coord.second}?q=${coord.first},${coord.second}")
                }
            }
        }
    }

    private fun abrirUrl(url:String){
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .setPackage("com.google.android.apps.maps"))
    }

    /* ------------------------------------------------------------------ */
    /*                       Carga de combos (spinners)                   */
    /* ------------------------------------------------------------------ */

    private fun cargarOpciones(){
        opciones.apply {
            clear()
            addAll(listOf(
                "Ver paseos de la mascota 🐶",
                "Mostrar recorrido del paseo 🐾",
                "Ver ubicación de la casa 🏠",
                "Buscar veterinarias cercanas 🏥",
                "Vista Street View 📷",
                "Ver recorrido en el mapa dentro de la app 🗺️",
                "Agregar nuevo recorrido personalizado ✍️",
                "Agregar nueva dirección personalizada 📌"
            ))
        }

        // direcciones guardadas
        FirebaseService.obtenerDirecciones(userId){ dirs ->
            dirs.forEach{
                val nombre = it["nombre"]?.toString() ?: return@forEach
                val lat    = (it["latitud"]  as? Number)?.toDouble() ?: return@forEach
                val lng    = (it["longitud"] as? Number)?.toDouble() ?: return@forEach
                val texto  = it["direccion"]?.toString() ?: ""
                direccionesMap[nombre] = texto to (lat to lng)
                opciones += nombre
            }

            spinnerOpciones.adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, opciones)
        }
    }

    private fun cargarMascotas(){
        FirebaseService.obtenerMascotas(userId){ masc ->
            val nombres = masc?.map { it["id"].toString() to it["nombre_mascota"].toString() }
                ?: emptyList()

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                nombres.map { it.second }          // solo el nombre visible
            )
            spinnerMascotas.adapter = adapter

            // Guardamos id-nombre en tag para recuperarlo fácil
            nombres.forEachIndexed { idx, par ->
                (spinnerMascotas.getItemAtPosition(idx) as? TextView)?.tag = par.first
            }

            // set listener para sacar el id
            spinnerMascotas.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p: AdapterView<*>, v: View?, pos:Int, id:Long){
                    mascotaIdSeleccion = nombres[pos].first
                }
                override fun onNothingSelected(p0: AdapterView<*>?) { mascotaIdSeleccion=null }
            }
        }
    }
}


