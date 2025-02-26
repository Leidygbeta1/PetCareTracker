package com.example.petcaretracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNombre: EditText = findViewById(R.id.etNombre)
        val etUsuario: EditText = findViewById(R.id.etUsuario)
        val etContrasena: EditText = findViewById(R.id.etContrasena)
        val etCorreo: EditText = findViewById(R.id.etCorreo)
        val etNumMascotas: EditText = findViewById(R.id.etNumMascotas)
        val contenedorMascotas: LinearLayout = findViewById(R.id.contenedorMascotas)
        val btnRegistrar: Button = findViewById(R.id.btnRegistrar)

        // Diccionario de razas según el tipo de mascota
        val razasMascotas = mapOf(
            "Perro" to listOf("Labrador", "Bulldog", "Pug", "Golden Retriever"),
            "Gato" to listOf("Siames", "Persa", "Sphynx", "Maine Coon"),
            "Ave" to listOf("Loro", "Canario", "Cacatúa", "Periquito"),
            "Otro" to listOf("Desconocido") // Default
        )

        // Detecta cambios en el número de mascotas y genera campos dinámicos
        etNumMascotas.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                contenedorMascotas.removeAllViews() // Limpia campos previos
                val numMascotas = s.toString().toIntOrNull() ?: 0

                for (i in 1..numMascotas) {
                    // Título para cada mascota
                    val textMascota = TextView(this@RegisterActivity)
                    textMascota.text = "Mascota $i"
                    textMascota.textSize = 18f
                    textMascota.setPadding(0, 20, 0, 10) // 🔹 Añade espacio superior e inferior
                    contenedorMascotas.addView(textMascota)

                    // Campo para el nombre de la mascota
                    val etMascota = EditText(this@RegisterActivity)
                    etMascota.hint = "Nombre de Mascota $i"
                    val paramsMascota = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsMascota.setMargins(0, 10, 0, 10) // 🔹 Espaciado superior e inferior
                    etMascota.layoutParams = paramsMascota
                    contenedorMascotas.addView(etMascota)

                    // Spinner para el tipo de mascota
                    val spinnerTipoMascota = Spinner(this@RegisterActivity)
                    val tiposMascotas = listOf("Perro", "Gato", "Ave", "Otro")
                    val adapterTipo = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, tiposMascotas)
                    adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTipoMascota.adapter = adapterTipo
                    val paramsTipo = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsTipo.setMargins(0, 10, 0, 10) // 🔹 Espaciado superior e inferior
                    spinnerTipoMascota.layoutParams = paramsTipo
                    contenedorMascotas.addView(spinnerTipoMascota)

                    // Spinner para la raza de la mascota
                    val spinnerRaza = Spinner(this@RegisterActivity)
                    val adapterRaza = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, listOf("Selecciona un tipo primero"))
                    adapterRaza.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerRaza.adapter = adapterRaza
                    val paramsRaza = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsRaza.setMargins(0, 10, 0, 20) // 🔹 Más espacio inferior
                    spinnerRaza.layoutParams = paramsRaza
                    contenedorMascotas.addView(spinnerRaza)

                    // Manejo de cambio de selección del tipo de mascota para actualizar razas
                    spinnerTipoMascota.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            val tipoSeleccionado = tiposMascotas[position]
                            val razasMascotas = mapOf(
                                "Perro" to listOf("Labrador", "Bulldog", "Pug", "Golden Retriever"),
                                "Gato" to listOf("Siames", "Persa", "Sphynx", "Maine Coon"),
                                "Ave" to listOf("Loro", "Canario", "Cacatúa", "Periquito"),
                                "Otro" to listOf("Desconocido")
                            )
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

        // Acción al presionar el botón de registro
        // Acción al presionar el botón de registro
        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val usuario = etUsuario.text.toString()
            val contrasena = etContrasena.text.toString()
            val correo = etCorreo.text.toString()
            val numMascotas = etNumMascotas.text.toString().toIntOrNull() ?: 0
            val datosMascotas = mutableListOf<String>()

            // Recoger los datos de cada mascota
            var index = 0
            for (i in 0 until contenedorMascotas.childCount) {
                val view = contenedorMascotas.getChildAt(i)

                if (view is EditText) {
                    val nombreMascota = view.text.toString()
                    val tipoMascota = (contenedorMascotas.getChildAt(i + 1) as Spinner).selectedItem.toString()
                    val razaMascota = (contenedorMascotas.getChildAt(i + 2) as Spinner).selectedItem.toString()
                    datosMascotas.add("Mascota ${index + 1}: Nombre: $nombreMascota, Tipo: $tipoMascota, Raza: $razaMascota")
                    index++
                }
            }

            // Validar que los campos no estén vacíos
            if (nombre.isEmpty() || usuario.isEmpty() || contrasena.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar mensaje de éxito
            Toast.makeText(this, "Registro exitoso. Bienvenido, $nombre!", Toast.LENGTH_LONG).show()

            // 🔹 Navegar a la pantalla HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Cierra RegisterActivity para evitar que el usuario vuelva atrás
        }

    }
}
