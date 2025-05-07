package com.example.petcaretracker.owner

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.petcaretracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.content.pm.PackageManager

class CompartirUbicacionActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnCompartirUbicacion: Button

    // Constante de solicitud de permisos
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compartir_ubicacion)

        // Inicializa el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnCompartirUbicacion = findViewById(R.id.btnCompartirUbicacion)

        // Al dar clic en el botón, compartimos la ubicación
        btnCompartirUbicacion.setOnClickListener {
            // Primero verificamos si el permiso está concedido
            checkLocationPermission()
        }
    }

    // Verificamos los permisos de ubicación
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicitamos el permiso
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            // Si el permiso ya está concedido, obtenemos la ubicación
            obtenerUbicacionYCompartir()
        }
    }

    // Obtiene la ubicación y luego llama a compartirUbicacion
    private fun obtenerUbicacionYCompartir() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar los permisos si no han sido concedidos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Si los permisos están concedidos, obtener la ubicación
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                compartirUbicacion(latLng)
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para compartir la ubicación con un enlace de Google Maps
    private fun compartirUbicacion(ubicacion: LatLng) {
        // Generamos el enlace de Google Maps
        val mensaje = "Mi ubicación actual: https://maps.google.com/?q=${ubicacion.latitude},${ubicacion.longitude}"

        // Creamos el Intent para compartir el enlace
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, mensaje) // El mensaje con el enlace de ubicación
            type = "text/plain"
        }

        // Mostramos el selector de aplicaciones para compartir el enlace
        val shareIntent = Intent.createChooser(sendIntent, "Compartir ubicación con...")
        startActivity(shareIntent)
    }


    // Manejo de permisos en tiempo de ejecución
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener la ubicación
                obtenerUbicacionYCompartir()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
