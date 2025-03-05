package com.example.petcaretracker

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseService {

    private val db: FirebaseFirestore = Firebase.firestore

    // 🔄 Registrar Usuario y Guardar Mascotas como Subcolección
    fun registrarUsuario(
        nombre: String,
        usuario: String,
        contrasena: String,
        correo: String,
        numMascotas: Int,
        datosMascotas: List<Map<String, String>>,
        callback: (Boolean, String?) -> Unit  // 🔄 Modificado para devolver userId
    ) {
        val userId = db.collection("usuarios").document().id

        val usuarioData = hashMapOf(
            "nombre_completo" to nombre,
            "nombre_usuario" to usuario,
            "contrasena" to contrasena,
            "correo_electronico" to correo,
            "numero_mascotas" to numMascotas,
            "fecha_registro" to com.google.firebase.Timestamp.now()
        )

        db.collection("usuarios").document(userId)
            .set(usuarioData)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Usuario registrado correctamente.")

                // Guardar cada mascota como subcolección
                datosMascotas.forEach { mascota ->
                    db.collection("usuarios").document(userId).collection("mascotas")
                        .add(mascota)
                        .addOnSuccessListener {
                            Log.d("FirebaseService", "Mascota guardada correctamente.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseService", "Error al guardar mascota", e)
                        }
                }

                callback(true, userId)  // 🔄 Devolver true y el userId
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al registrar usuario", e)
                callback(false, null)  // 🔄 Devolver false y null en caso de error
            }
    }


    // 🔄 Obtener Datos del Usuario y sus Mascotas
    fun obtenerUsuarioActual(userId: String, callback: (Map<String, Any>?, List<Map<String, Any>>?) -> Unit) {
        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val usuarioData = document.data

                    // 🔄 Obtener Subcolección "mascotas"
                    db.collection("usuarios").document(userId).collection("mascotas").get()
                        .addOnSuccessListener { mascotasSnapshot ->
                            val mascotas = mascotasSnapshot.documents.mapNotNull { doc ->
                                doc.data?.plus("id" to doc.id)
                            }
                            callback(usuarioData, mascotas)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseService", "Error al obtener mascotas", e)
                            callback(usuarioData, null)
                        }
                } else {
                    callback(null, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener usuario", e)
                callback(null, null)
            }
    }

    // 🔄 Obtener Mascotas del Usuario
    fun obtenerMascotas(userId: String, callback: (List<Map<String, Any>>?) -> Unit) {
        if (userId.isEmpty()) {
            Log.e("FirebaseService", "userId está vacío en obtenerMascotas")
            callback(null)
            return
        }

        db.collection("usuarios").document(userId).collection("mascotas")
            .get()
            .addOnSuccessListener { snapshot ->
                val mascotas = snapshot.documents.map { doc ->
                    val data = doc.data ?: emptyMap()
                    data + ("id" to doc.id)
                }
                callback(mascotas)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener mascotas", e)
                callback(null)
            }
    }


    // 🔄 Guardar registro médico para una mascota específica
    fun guardarRegistroMedico(
        userId: String,
        mascotaId: String,
        tipoAtencion: String,
        descripcion: String,
        callback: (Boolean) -> Unit
    ) {
        val registroData = hashMapOf(
            "tipo_atencion" to tipoAtencion,
            "descripcion" to descripcion,
            "fecha_registro" to com.google.firebase.Timestamp.now()
        )

        db.collection("usuarios").document(userId).collection("mascotas")
            .document(mascotaId).collection("registros_medicos")
            .add(registroData)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Registro médico guardado correctamente.")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al guardar registro médico", e)
                callback(false)
            }
    }

    fun iniciarSesion(usuario: String, contrasena: String, callback: (Boolean, String?) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("nombre_usuario", usuario)
            .whereEqualTo("contrasena", contrasena)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.documents[0].id
                    Log.d("FirebaseService", "Inicio de sesión exitoso. userId: $userId")

                    // Guardar el userId en SharedPreferences
                    val sharedPreferences = MyApp.context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("userId", userId)
                        apply()
                    }

                    callback(true, userId)
                } else {
                    Log.e("FirebaseService", "Usuario o contraseña incorrectos")
                    callback(false, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al iniciar sesión", e)
                callback(false, null)
            }
    }



    // 🔄 Obtener registros médicos para una mascota específica
    fun obtenerRegistrosMedicos(
        userId: String,
        mascotaId: String,
        callback: (List<Map<String, Any>>?) -> Unit
    ) {
        db.collection("usuarios").document(userId).collection("mascotas")
            .document(mascotaId).collection("registros_medicos")
            .get()
            .addOnSuccessListener { registrosSnapshot ->
                val registros = registrosSnapshot.documents.mapNotNull { it.data }
                callback(registros)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener registros médicos", e)
                callback(null)
            }
    }
}



