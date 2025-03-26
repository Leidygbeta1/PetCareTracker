package com.example.petcaretracker

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.*

object PostgreSQLHelper {
    // 📌 Configuración de PostgreSQL
    private const val IP = "192.168.0.116"  // Cambia esto por la IP de tu servidor PostgreSQL
    private const val PORT = "5432"  // Puerto por defecto de PostgreSQL
    private const val DATABASE = "petcare"
    private const val USER = "tu_usuario"
    private const val PASSWORD = "tu_contraseña"

    private var connection: Connection? = null

    // 🔹 Método para conectar a PostgreSQL
    suspend fun connect(): Connection? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "jdbc:postgresql://$IP:$PORT/$DATABASE"

                // Cargar el driver de PostgreSQL
                Class.forName("org.postgresql.Driver")

                // Establecer la conexión
                connection = DriverManager.getConnection(url, USER, PASSWORD)
                Log.d("PostgreSQLHelper", "✅ Conexión exitosa a PostgreSQL")

                connection
            } catch (e: ClassNotFoundException) {
                Log.e("PostgreSQLHelper", "❌ Error: Driver PostgreSQL no encontrado: ${e.message}")
                null
            } catch (e: SQLException) {
                Log.e("PostgreSQLHelper", "❌ Error al conectar a PostgreSQL: ${e.message}")
                null
            }
        }
    }

    // 🔹 Iniciar sesión
    suspend fun iniciarSesion(usuario: String, contrasena: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            val conn = connect()
            if (conn == null) return@withContext Pair(false, null)

            val sql = "SELECT id FROM usuarios WHERE nombre_usuario = ? AND contrasena = crypt(?, contrasena)"

            try {
                val preparedStatement: PreparedStatement = conn.prepareStatement(sql)
                preparedStatement.setString(1, usuario)
                preparedStatement.setString(2, contrasena)

                val resultSet: ResultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    val userId = resultSet.getString("id")
                    return@withContext Pair(true, userId) // Inicio de sesión exitoso
                }
            } catch (e: SQLException) {
                Log.e("PostgreSQLHelper", "Error en inicio de sesión: ${e.message}")
            } finally {
                conn.close()
            }

            Pair(false, null) // Credenciales incorrectas o error
        }
    }

    // 🔹 Obtener datos del usuario actual
    suspend fun obtenerUsuarioActual(userId: String): Pair<Map<String, String>?, List<Map<String, String>>?> {
        return withContext(Dispatchers.IO) {
            val conn = connect()
            if (conn == null) return@withContext Pair(null, null)

            val sqlUsuario = "SELECT nombre_completo, correo_electronico FROM usuarios WHERE id = ?"
            val sqlMascotas = "SELECT nombre, tipo, raza FROM mascotas WHERE usuario_id = ?"

            try {
                val preparedStatementUsuario: PreparedStatement = conn.prepareStatement(sqlUsuario)
                preparedStatementUsuario.setString(1, userId)
                val resultSetUsuario: ResultSet = preparedStatementUsuario.executeQuery()

                val usuarioData = if (resultSetUsuario.next()) {
                    mapOf(
                        "nombre_completo" to resultSetUsuario.getString("nombre_completo"),
                        "correo_electronico" to resultSetUsuario.getString("correo_electronico")
                    )
                } else null

                val mascotasList = mutableListOf<Map<String, String>>()
                val preparedStatementMascotas: PreparedStatement = conn.prepareStatement(sqlMascotas)
                preparedStatementMascotas.setString(1, userId)
                val resultSetMascotas: ResultSet = preparedStatementMascotas.executeQuery()

                while (resultSetMascotas.next()) {
                    mascotasList.add(
                        mapOf(
                            "nombre" to resultSetMascotas.getString("nombre"),
                            "tipo" to resultSetMascotas.getString("tipo"),
                            "raza" to resultSetMascotas.getString("raza")
                        )
                    )
                }

                Pair(usuarioData, mascotasList)

            } catch (e: SQLException) {
                Log.e("PostgreSQLHelper", "Error al obtener usuario: ${e.message}")
                Pair(null, null)
            }
        }
    }


    // 🔹 Registrar usuario con mascotas
    suspend fun registrarUsuario(
        nombre: String,
        usuario: String,
        contrasena: String,
        correo: String,
        datosMascotas: List<Map<String, String>>
    ): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            val conn = connect()
            if (conn == null) return@withContext Pair(false, null)

            try {
                conn.autoCommit = false // 🔹 Usamos transacción para asegurar la integridad

                // Insertar usuario
                val sqlUsuario = "INSERT INTO usuarios (nombre_completo, nombre_usuario, contrasena, correo_electronico) VALUES (?, ?, crypt(?, gen_salt('bf')), ?)"
                val stmtUsuario: PreparedStatement = conn.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)
                stmtUsuario.setString(1, nombre)
                stmtUsuario.setString(2, usuario)
                stmtUsuario.setString(3, contrasena)
                stmtUsuario.setString(4, correo)
                stmtUsuario.executeUpdate()

                // Obtener el ID del usuario recién insertado
                val generatedKeys: ResultSet = stmtUsuario.generatedKeys
                var userId: Int? = null
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1)
                }
                generatedKeys.close()
                stmtUsuario.close()

                if (userId == null) {
                    conn.rollback()
                    return@withContext Pair(false, null)
                }

                // Insertar mascotas asociadas al usuario
                for (mascota in datosMascotas) {
                    val sqlMascota = "INSERT INTO mascotas (nombre_mascota, tipo, raza, usuario_id) VALUES (?, ?, ?, ?)"
                    val stmtMascota: PreparedStatement = conn.prepareStatement(sqlMascota)
                    stmtMascota.setString(1, mascota["nombre_mascota"])
                    stmtMascota.setString(2, mascota["tipo"])
                    stmtMascota.setString(3, mascota["raza"])
                    stmtMascota.setInt(4, userId)
                    stmtMascota.executeUpdate()
                    stmtMascota.close()
                }

                conn.commit() // 🔹 Confirmamos la transacción
                Pair(true, userId.toString())

            } catch (e: SQLException) {
                conn.rollback() // 🔹 En caso de error, deshacemos la transacción
                Log.e("PostgreSQLHelper", "Error en el registro: ${e.message}")
                Pair(false, null)
            } finally {
                conn.autoCommit = true
                conn.close()
            }
        }
    }
}
