package com.example.petcaretracker.owner

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.R
import com.example.petcaretracker.cuidador.HomeCuidadorActivity
import com.example.petcaretracker.veterinario.HomeVeterinarioActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNombre: EditText = findViewById(R.id.etNombre)
        val etUsuario: EditText = findViewById(R.id.etUsuario)
        val etContrasena: EditText = findViewById(R.id.etContrasena)
        val etCorreo: EditText = findViewById(R.id.etCorreo)
        val etNumMascotas: EditText = findViewById(R.id.etNumMascotas)
        val etClinica: EditText = findViewById(R.id.etClinica)
        val spinnerRol: Spinner = findViewById(R.id.spinnerRol)
        val contenedorMascotas: LinearLayout = findViewById(R.id.contenedorMascotas)
        val btnRegistrar: Button = findViewById(R.id.btnRegistrar)

        // Configurar Spinner de roles
        val roles = listOf("Owner", "Cuidador", "Veterinario")
        val adapterRoles = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapterRoles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapterRoles

        // Mostrar u ocultar campos según el rol
        spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                when (roles[position]) {
                    "Owner" -> {
                        etNumMascotas.visibility = android.view.View.VISIBLE
                        contenedorMascotas.visibility = android.view.View.VISIBLE
                        etClinica.visibility = android.view.View.GONE
                    }
                    "Cuidador" -> {
                        etNumMascotas.visibility = android.view.View.GONE
                        contenedorMascotas.visibility = android.view.View.GONE
                        etClinica.visibility = android.view.View.GONE
                    }
                    "Veterinario" -> {
                        etNumMascotas.visibility = android.view.View.GONE
                        contenedorMascotas.visibility = android.view.View.GONE
                        etClinica.visibility = android.view.View.VISIBLE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Diccionario de razas según el tipo de mascota
        val razasMascotas = mapOf(
            "Perro" to listOf("Labrador", "Bulldog", "Pug", "Golden Retriever"),
            "Gato" to listOf("Siames", "Persa", "Sphynx", "Maine Coon"),
            "Ave" to listOf("Loro", "Canario", "Cacatúa", "Periquito"),
            "Otro" to listOf("Desconocido")
        )

        // Detecta cambios en el número de mascotas y genera campos dinámicos
        etNumMascotas.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                contenedorMascotas.removeAllViews()
                val numMascotas = s.toString().toIntOrNull() ?: 0

                for (i in 1..numMascotas) {
                    val textMascota = TextView(this@RegisterActivity)
                    textMascota.text = "Mascota $i"
                    textMascota.textSize = 18f
                    textMascota.setPadding(0, 20, 0, 10)
                    contenedorMascotas.addView(textMascota)

                    val etMascota = EditText(this@RegisterActivity)
                    etMascota.hint = "Nombre de Mascota $i"
                    val paramsMascota = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsMascota.setMargins(0, 10, 0, 10)
                    etMascota.layoutParams = paramsMascota
                    contenedorMascotas.addView(etMascota)

                    val spinnerTipoMascota = Spinner(this@RegisterActivity)
                    val tiposMascotas = listOf("Perro", "Gato", "Ave", "Otro")
                    val adapterTipo = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, tiposMascotas)
                    adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTipoMascota.adapter = adapterTipo
                    val paramsTipo = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsTipo.setMargins(0, 10, 0, 10)
                    spinnerTipoMascota.layoutParams = paramsTipo
                    contenedorMascotas.addView(spinnerTipoMascota)

                    val spinnerRaza = Spinner(this@RegisterActivity)
                    val adapterRaza = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, listOf("Selecciona un tipo primero"))
                    adapterRaza.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerRaza.adapter = adapterRaza
                    val paramsRaza = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsRaza.setMargins(0, 10, 0, 20)
                    spinnerRaza.layoutParams = paramsRaza
                    contenedorMascotas.addView(spinnerRaza)

                    spinnerTipoMascota.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            val tipoSeleccionado = tiposMascotas[position]
                            val razas = razasMascotas[tipoSeleccionado] ?: listOf("Desconocido")
                            val adapterNuevaRaza = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, razas)
                            adapterNuevaRaza.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerRaza.adapter = adapterNuevaRaza
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val usuario = etUsuario.text.toString()
            val contrasena = etContrasena.text.toString()
            val correo = etCorreo.text.toString()
            val rolSeleccionado = spinnerRol.selectedItem.toString()
            val nombreClinica = etClinica.text.toString()
            val numMascotas = etNumMascotas.text.toString().toIntOrNull() ?: 0

            if (nombre.isEmpty() || usuario.isEmpty() || contrasena.isEmpty() || correo.isEmpty() || rolSeleccionado.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datosMascotas = mutableListOf<Map<String, String>>()
            if (rolSeleccionado == "Owner") {
                for (i in 0 until contenedorMascotas.childCount step 4) {
                    val nombreMascota = (contenedorMascotas.getChildAt(i + 1) as EditText).text.toString()
                    val tipoMascota = (contenedorMascotas.getChildAt(i + 2) as Spinner).selectedItem.toString()
                    val razaMascota = (contenedorMascotas.getChildAt(i + 3) as Spinner).selectedItem.toString()

                    if (nombreMascota.isNotEmpty()) {
                        val mascotaData = mapOf(
                            "nombre_mascota" to nombreMascota,
                            "tipo" to tipoMascota,
                            "raza" to razaMascota
                        )
                        datosMascotas.add(mascotaData)
                    }
                }
            }

            FirebaseService.registrarUsuario(
                nombre,
                usuario,
                contrasena,
                correo,
                numMascotas,
                datosMascotas,
                rolSeleccionado,
                nombreClinica
            ) { success, userId ->
                if (success && userId != null) {
                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("userId", userId)
                        putString("rol", rolSeleccionado)
                        apply()
                    }

                    Toast.makeText(
                        this,
                        "Registro exitoso. Bienvenido, $nombre!",
                        Toast.LENGTH_LONG
                    ).show()

                    if (rolSeleccionado == "Veterinario") {
                        startActivity(Intent(this, HomeVeterinarioActivity::class.java))
                    } else if (rolSeleccionado == "Cuidador") {
                        startActivity(Intent(this, HomeCuidadorActivity::class.java))
                    } else {
                        // Owner
                        startActivity(Intent(this, HomeActivity::class.java))
                    }


                    finish()
                } else {
                    Toast.makeText(this, "Error al registrar. Intenta de nuevo.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}



