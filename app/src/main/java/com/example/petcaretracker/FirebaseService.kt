package com.example.petcaretracker

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.petcaretracker.cuidador.MascotaCuidador

import com.example.petcaretracker.veterinario.MascotaVeterinario
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

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
        rol: String,
        nombreClinica: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val userId = db.collection("usuarios").document().id

        // Datos comunes del usuario
        val usuarioData = hashMapOf(
            "nombre_completo" to nombre,
            "nombre_usuario" to usuario,
            "contrasena" to contrasena,
            "correo_electronico" to correo,
            "numero_mascotas" to numMascotas,
            "rol" to rol,
            "fecha_registro" to com.google.firebase.Timestamp.now()
        )

        // Si el rol es veterinario, añadimos la clínica
        if (rol == "Veterinario" && nombreClinica.isNotEmpty()) {
            usuarioData["clinica"] = nombreClinica
        }

        db.collection("usuarios").document(userId)
            .set(usuarioData)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Usuario registrado correctamente.")

                // Si el usuario es 'Owner', guardar mascotas como subcolección
                if (rol == "Owner") {
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
                }

                callback(true, userId)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al registrar usuario", e)
                callback(false, null)
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
        veterinarioId: String,
        veterinarioNombre: String, // ❗️FALTA EN LA LLAMADA
        clinica: String,
        callback: (Boolean) -> Unit
    ) {
        val registroData = hashMapOf(
            "tipo_atencion" to tipoAtencion,
            "descripcion" to descripcion,
            "veterinario_id" to veterinarioId,
            "veterinario_nombre" to veterinarioNombre,
            "clinica" to clinica,
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



    fun iniciarSesion(usuario: String, contrasena: String, callback: (Boolean, String?, String?) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("nombre_usuario", usuario)
            .whereEqualTo("contrasena", contrasena)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val userId = doc.id
                    val rol = doc.getString("rol") ?: "Owner" // por defecto

                    Log.d("FirebaseService", "Inicio de sesión exitoso. userId: $userId - rol: $rol")

                    // Guardar en SharedPreferences
                    val sharedPreferences = MyApp.context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("userId", userId)
                        putString("rol", rol)
                        apply()
                    }

                    callback(true, userId, rol)
                } else {
                    Log.e("FirebaseService", "Usuario o contraseña incorrectos")
                    callback(false, null, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al iniciar sesión", e)
                callback(false, null, null)
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

    fun agregarMascota(
        userId: String,
        nombre: String,
        raza: String,
        tipo: String,
        imageUri: Uri?,
        callback: (Boolean, String?) -> Unit
    ) {
        val mascotaRef = db.collection("usuarios").document(userId).collection("mascotas").document()
        val mascotaId = mascotaRef.id

        val datosMascota = mutableMapOf(
            "nombre_mascota" to nombre,
            "raza" to raza,
            "tipo" to tipo,
            "foto" to "" // Inicialmente vacío
        )

        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("mascotas/$userId/$mascotaId.jpg")

            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        datosMascota["foto"] = uri.toString()

                        mascotaRef.set(datosMascota)
                            .addOnSuccessListener { callback(true, null) }
                            .addOnFailureListener { e -> callback(false, e.message) }
                    }
                }
                .addOnFailureListener { e -> callback(false, "Error al subir la foto: ${e.message}") }
        } else {
            mascotaRef.set(datosMascota)
                .addOnSuccessListener { callback(true, null) }
                .addOnFailureListener { e -> callback(false, e.message) }
        }
    }


    // 🔄 Obtener Vacunas para una mascota específica
    fun obtenerVacunas(userId: String, mascotaId: String, callback: (List<Map<String, Any>>?) -> Unit) {
        db.collection("usuarios").document(userId).collection("mascotas")
            .document(mascotaId).collection("vacunas")
            .get()
            .addOnSuccessListener { snapshot ->
                val vacunas = snapshot.documents.mapNotNull { it.data }
                callback(vacunas)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener vacunas", e)
                callback(null)
            }
    }

    // 🔄 Agregar una nueva vacuna
    fun agregarVacuna(userId: String, mascotaId: String, vacuna: Vacuna, callback: (Boolean) -> Unit) {
        val nuevaVacuna = hashMapOf(
            "nombre" to vacuna.nombre,
            "fechaAplicacion" to vacuna.fechaAplicacion,
            "proximaDosis" to vacuna.proximaDosis,
            "veterinario" to vacuna.veterinario
        )

        db.collection("usuarios").document(userId)
            .collection("mascotas").document(mascotaId)
            .collection("vacunas").add(nuevaVacuna)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al agregar vacuna", e)
                callback(false)
            }
    }

    fun actualizarPerfilUsuario(
        userId: String,
        nombre: String,
        nombreUsuario: String,
        correo: String,
        callback: (Boolean) -> Unit
    ) {
        val data = mapOf(
            "nombre_completo" to nombre,
            "nombre_usuario" to nombreUsuario,
            "correo_electronico" to correo
        )

        Firebase.firestore.collection("usuarios").document(userId)
            .update(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al actualizar perfil", e)
                callback(false)
            }
    }


    fun eliminarCuenta(userId: String, callback: (Boolean) -> Unit) {
        val db = Firebase.firestore
        val userRef = db.collection("usuarios").document(userId)

        // 1. Obtener las mascotas
        userRef.collection("mascotas").get()
            .addOnSuccessListener { mascotasSnapshot ->
                val batch = db.batch()

                val tareas = mutableListOf<Task<Void>>()

                for (mascotaDoc in mascotasSnapshot.documents) {
                    val mascotaRef = mascotaDoc.reference

                    // 1.1 Eliminar registros médicos
                    tareas.add(mascotaRef.collection("registros_medicos").get().continueWithTask { registros ->
                        val eliminarRegistros = registros.result?.documents?.map { it.reference.delete() } ?: emptyList()
                        Tasks.whenAll(eliminarRegistros)
                    })

                    // 1.2 Eliminar vacunas
                    tareas.add(mascotaRef.collection("vacunas").get().continueWithTask { vacunas ->
                        val eliminarVacunas = vacunas.result?.documents?.map { it.reference.delete() } ?: emptyList()
                        Tasks.whenAll(eliminarVacunas)
                    })

                    // 1.3 Eliminar mascota
                    tareas.add(mascotaRef.delete())
                }

                // 2. Eliminar recordatorios
                tareas.add(userRef.collection("recordatorios").get().continueWithTask { recordatorios ->
                    val eliminarRecordatorios = recordatorios.result?.documents?.map { it.reference.delete() } ?: emptyList()
                    Tasks.whenAll(eliminarRecordatorios)
                })

                // 3. Cuando todo lo anterior esté listo, eliminar el usuario
                Tasks.whenAllComplete(tareas).addOnSuccessListener {
                    userRef.delete()
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }
                }.addOnFailureListener { callback(false) }
            }
            .addOnFailureListener { callback(false) }
    }

    fun obtenerClinicasDisponibles(callback: (List<String>?) -> Unit) {
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val clinicas = mutableListOf<String>()
                for (doc in result) {
                    val rol = doc.getString("rol")
                    val clinica = doc.getString("clinica")
                    Log.d("DEBUG_FIREBASE", "Usuario: ${doc.id}, rol: $rol, clinica: $clinica")
                    if (rol == "Veterinario" && clinica != null) {
                        clinicas.add(clinica)
                    }
                }
                val distintas = clinicas.toSet().toList()
                Log.d("DEBUG_FIREBASE", "Clínicas filtradas: $distintas")
                callback(distintas)
            }
            .addOnFailureListener {
                Log.e("DEBUG_FIREBASE", "Error al obtener clínicas", it)
                callback(null)
            }
    }

    fun obtenerVeterinariosPorClinica(clinica: String, callback: (List<Map<String, String>>?) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("rol", "Veterinario")
            .whereEqualTo("clinica", clinica)
            .get()
            .addOnSuccessListener { result ->
                val veterinarios = result.map {
                    mapOf(
                        "id" to it.id,
                        "nombre_completo" to (it.getString("nombre_completo") ?: "Sin Nombre")
                    )
                }
                callback(veterinarios)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun obtenerMascotasPorVeterinario(
        veterinarioId: String,
        callback: (List<MascotaVeterinario>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val resultado = mutableListOf<MascotaVeterinario>()

        db.collection("usuarios").get().addOnSuccessListener { usuariosSnapshot ->
            val usuarios = usuariosSnapshot.documents
            if (usuarios.isEmpty()) {
                callback(emptyList())
                return@addOnSuccessListener
            }

            var usuariosProcesados = 0

            for (usuarioDoc in usuarios) {
                val userId = usuarioDoc.id
                val nombreDueno = usuarioDoc.getString("nombre_completo") ?: "Sin Nombre"

                db.collection("usuarios").document(userId).collection("mascotas").get()
                    .addOnSuccessListener { mascotasSnapshot ->
                        val mascotas = mascotasSnapshot.documents
                        if (mascotas.isEmpty()) {
                            usuariosProcesados++
                            if (usuariosProcesados == usuarios.size) {
                                callback(resultado)
                            }
                            return@addOnSuccessListener
                        }

                        var mascotasProcesadas = 0

                        for (mascotaDoc in mascotas) {
                            val nombreMascota = mascotaDoc.getString("nombre_mascota") ?: "Sin Nombre"
                            val raza = mascotaDoc.getString("raza") ?: "Sin raza"
                            val tipo = mascotaDoc.getString("tipo") ?: "Sin tipo"
                            val foto = mascotaDoc.getString("foto") ?: ""

                            val mascotaId = mascotaDoc.id
                            db.collection("usuarios").document(userId)
                                .collection("mascotas").document(mascotaId)
                                .collection("registros_medicos")
                                .whereEqualTo("veterinario_id", veterinarioId)
                                .get()
                                .addOnSuccessListener { registrosSnapshot ->
                                    if (!registrosSnapshot.isEmpty) {
                                        resultado.add(
                                            MascotaVeterinario(
                                                nombre = nombreMascota,
                                                raza = raza,
                                                tipo = tipo,
                                                foto = foto,
                                                nombreDueno = nombreDueno
                                            )
                                        )
                                    }

                                    mascotasProcesadas++
                                    if (mascotasProcesadas == mascotas.size) {
                                        usuariosProcesados++
                                        if (usuariosProcesados == usuarios.size) {
                                            callback(resultado)
                                        }
                                    }
                                }.addOnFailureListener {
                                    mascotasProcesadas++
                                    if (mascotasProcesadas == mascotas.size) {
                                        usuariosProcesados++
                                        if (usuariosProcesados == usuarios.size) {
                                            callback(resultado)
                                        }
                                    }
                                }
                        }
                    }.addOnFailureListener {
                        usuariosProcesados++
                        if (usuariosProcesados == usuarios.size) {
                            callback(resultado)
                        }
                    }
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }


    // 2) Obtener todos los cuidadores registrados
    fun obtenerCuidadoresDisponibles(
        callback: (List<Map<String, String>>?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .whereEqualTo("rol", "Cuidador")
            .get()
            .addOnSuccessListener { snaps ->
                val cuidadores = snaps.map { doc ->
                    mapOf(
                        "id"               to doc.id,
                        "nombre_completo"  to (doc.getString("nombre_completo") ?: "Sin Nombre")
                    )
                }
                callback(cuidadores)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun obtenerMascotasAsignadasAlCuidador(
        cuidadorId: String,
        callback: (List<MascotaCuidador>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val resultado = mutableListOf<MascotaCuidador>()

        db.collection("usuarios")
            .get()
            .addOnSuccessListener { usuariosSnap ->
                val usuarios = usuariosSnap.documents
                if (usuarios.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                var usuariosProcesados = 0
                for (usuarioDoc in usuarios) {
                    val ownerId = usuarioDoc.id
                    val nombreDueno = usuarioDoc.getString("nombre_completo") ?: "Sin Nombre"

                    db.collection("usuarios")
                        .document(ownerId)
                        .collection("mascotas")
                        .get()
                        .addOnSuccessListener { mascotasSnap ->
                            val mascotas = mascotasSnap.documents
                            if (mascotas.isEmpty()) {
                                usuariosProcesados++
                                if (usuariosProcesados == usuarios.size) callback(resultado)
                                return@addOnSuccessListener
                            }

                            var mascotasProcesadas = 0
                            for (mascotaDoc in mascotas) {
                                // Solo añadimos si su campo cuidador_id coincide
                                val assigned = mascotaDoc.getString("cuidador_id")
                                if (assigned == cuidadorId) {
                                    val nombreM = mascotaDoc.getString("nombre_mascota") ?: "Sin Nombre"
                                    val raza    = mascotaDoc.getString("raza")            ?: "Sin Raza"
                                    val tipo    = mascotaDoc.getString("tipo")            ?: "Sin Tipo"
                                    val foto    = mascotaDoc.getString("foto")            ?: ""

                                    resultado.add(
                                        MascotaCuidador(
                                            nombre      = nombreM,
                                            raza        = raza,
                                            tipo        = tipo,
                                            foto        = foto,
                                            nombreDueno = nombreDueno
                                        )
                                    )
                                }
                                mascotasProcesadas++
                                if (mascotasProcesadas == mascotas.size) {
                                    usuariosProcesados++
                                    if (usuariosProcesados == usuarios.size) {
                                        callback(resultado)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            usuariosProcesados++
                            if (usuariosProcesados == usuarios.size) callback(resultado)
                        }
                }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
    fun getChatsForUser(userId: String, callback: (List<Chat>) -> Unit) {
        db.collection("chats")
            .whereArrayContains("participants", userId)

            .get()
            .addOnSuccessListener { snaps ->
                val chats = snaps.documents.map { doc ->
                    Chat(
                        id          = doc.id,
                        participants= doc.get("participants") as List<String>,
                        petId       = doc.getString("petId"),
                        lastMessage = doc.getString("lastMessage") ?: "",
                        timestamp   = doc.getTimestamp("timestamp")?.toDate()
                    )
                }
                callback(chats)
            }
            .addOnFailureListener { ex ->
                Log.e("FirebaseService", "Error fetching chats", ex)
                callback(emptyList())
            }
    }

    /** 2) Encuentra o crea un chat entre dos usuarios */
    fun getOrCreateChat(
        currentUserId: String,
        otherUserId: String,
        petId: String?,
        callback: (String) -> Unit
    ) {
        val pair = listOf(currentUserId, otherUserId).sorted()
        val chatsRef = db.collection("chats")
        chatsRef
            .whereEqualTo("participants", pair)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    // Creamos uno nuevo
                    val data = mapOf(
                        "participants" to pair,
                        "petId"        to petId,
                        "lastMessage"  to "",
                        "timestamp"    to FieldValue.serverTimestamp()
                    )
                    chatsRef.add(data)
                        .addOnSuccessListener { doc ->
                            // <-- Aquí esperamos medio segundo antes de devolver el id
                            Handler(Looper.getMainLooper()).postDelayed({
                                callback(doc.id)
                            }, 500)
                        }
                } else {
                    // Ya existe, devolvemos directamente
                    callback(snap.documents.first().id)
                }
            }
            .addOnFailureListener {
                // En caso de error, devolvemos vacío
                callback("")
            }
    }


    /** 3) Envía un mensaje y actualiza el último mensaje en el chat */
    fun sendMessage(
        chatId: String,
        senderId: String,
        senderRole: String,
        text: String
    ) {
        val chatRef = db.collection("chats").document(chatId)
        val batch   = db.batch()
        // 3.1) nuevo mensaje
        val msgDoc = chatRef.collection("messages").document()
        batch.set(msgDoc, mapOf(
            "senderId"   to senderId,
            "senderRole" to senderRole,
            "text"       to text,
            "timestamp"  to FieldValue.serverTimestamp()
        ))
        // 3.2) actualizar chat
        batch.update(chatRef, mapOf(
            "lastMessage" to text,
            "timestamp"   to FieldValue.serverTimestamp()
        ))
        batch.commit()
    }

    /** 4) Escucha mensajes en tiempo real */
    fun listenMessages(
        chatId: String,
        onUpdate: (List<Message>) -> Unit
    ): ListenerRegistration {
        return db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, _ ->
                val msgs = snaps?.documents?.map { doc ->
                    Message(
                        id         = doc.id,
                        senderId   = doc.getString("senderId") ?: "",
                        senderRole = doc.getString("senderRole") ?: "",
                        text       = doc.getString("text") ?: "",
                        timestamp  = doc.getTimestamp("timestamp")?.toDate()
                    )
                } ?: emptyList()
                onUpdate(msgs)
            }
    }

    // 1) Lista de veterinarios
    fun getVeterinariosDisponibles(callback: (List<Map<String,String>>) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("rol", "Veterinario")
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.map { doc ->
                    mapOf("id" to doc.id, "nombre_completo" to (doc.getString("nombre_completo") ?: ""))
                }
                callback(list)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // 2) Lista de cuidadores
    fun getCuidadoresDisponibles(callback: (List<Map<String,String>>) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("rol", "Cuidador")
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.map { doc ->
                    mapOf("id" to doc.id, "nombre_completo" to (doc.getString("nombre_completo") ?: ""))
                }
                callback(list)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
    fun getOwnersDisponibles(callback: (List<Map<String,String>>) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("rol", "owner")
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.map { doc ->
                    mapOf(
                        "id"              to doc.id,
                        "nombre_completo" to (doc.getString("nombre_completo") ?: "")
                    )
                }
                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getUsuarioById(
        userId: String,
        callback: (User?) -> Unit
    ) {
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    callback(null)
                    return@addOnSuccessListener
                }
                // doc.data es un Map<String, Any?>
                val dataMap = doc.data ?: emptyMap()

                // Nombre (sigue igual)
                val nombre = dataMap["nombre_completo"] as? String ?: ""

                // Buscamos la entrada cuyo key.trim()=="rol"
                val rolEntry = dataMap.entries
                    .firstOrNull { it.key.trim().equals("rol", ignoreCase = true) }
                val rol = (rolEntry?.value as? String)?.trim() ?: ""

                Log.d("FirebaseService", ">> getUsuarioById($userId): nombre='$nombre', rol='$rol' (clave encontrada='${rolEntry?.key}')")

                callback(User(doc.id, nombre, rol))
            }
            .addOnFailureListener { ex ->
                Log.e("FirebaseService", "Error getUsuarioById($userId)", ex)
                callback(null)
            }
    }


}






