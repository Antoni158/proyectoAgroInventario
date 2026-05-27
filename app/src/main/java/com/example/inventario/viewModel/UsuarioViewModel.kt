package com.example.inventario.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.administracion.Usuario
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.data.firebase.OfflineManager
import com.example.inventario.data.firebase.SyncHelper
import com.example.inventario.security.PasswordHasher
import com.example.inventario.security.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val dao = db.usuarioDao()
    private val firebaseRepo = FirebaseRepository()
    private val appContext get() = getApplication<Application>().applicationContext

    private suspend fun syncUsuario(u: Usuario) {
        val withUuid = if (u.uuid.isBlank()) u.copy(uuid = SyncHelper.newUuid()) else u
        if (withUuid.uuid != u.uuid) dao.actualizar(withUuid)
        if (OfflineManager.isOnline(appContext)) {
            firebaseRepo.guardarUsuario(withUuid)
        } else {
            SyncHelper.enqueueUsuario(appContext, withUuid)
        }
    }

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    init {
        cargarUsuarios()
        crearUsuariosSemilla()
        migrarRolesLegacy()
    }

    private fun migrarRolesLegacy() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val legacy = listOf("OPERADOR", "USUARIO", "BODEGA")
                dao.obtenerTodosSync().filter { it.rol.uppercase() in legacy }.forEach { u ->
                    val normalizado = u.copy(rol = UserRole.ADMIN.storageValue)
                    dao.actualizar(normalizado)
                    syncUsuario(normalizado)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private data class UsuarioSemilla(
        val username: String,
        val password: String,
        val correo: String,
        val rol: String,
        val nombre: String
    )

    private val usuariosSistema = setOf("visor", "admin", "auditor")

    private fun crearUsuariosSemilla() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val semillas = listOf(
                    UsuarioSemilla(
                        username = "visor",
                        password = "12345",
                        correo = "josecab512@gmail.com",
                        rol = UserRole.VISOR.storageValue,
                        nombre = "Visor"
                    ),
                    UsuarioSemilla(
                        username = "admin",
                        password = "1234",
                        correo = "josedepaz313@gmail.com",
                        rol = UserRole.ADMIN.storageValue,
                        nombre = "Administrador"
                    ),
                    UsuarioSemilla(
                        username = "auditor",
                        password = "23456",
                        correo = "jcabrerad7@miumg.edu.gt",
                        rol = UserRole.AUDITOR.storageValue,
                        nombre = "Auditor"
                    )
                )
                // Eliminar cuentas demo antiguas (*_erp) para evitar duplicados
                dao.obtenerTodosSync()
                    .filter { u ->
                        !u.isDeleted && (
                            u.username.endsWith("_erp", ignoreCase = true) ||
                            (u.username !in usuariosSistema && u.correo.endsWith("@agro.com"))
                        )
                    }
                    .forEach { legacy ->
                        dao.eliminarPermanente(legacy.id)
                    }

                semillas.forEach { semilla ->
                    val hashed = PasswordHasher.hash(semilla.password)
                    val existente = dao.existeUsername(semilla.username)
                    val usuario = if (existente == null) {
                        Usuario(
                            uuid = SyncHelper.newUuid(),
                            nombre = semilla.nombre,
                            correo = semilla.correo,
                            username = semilla.username,
                            password = hashed,
                            rol = semilla.rol,
                            activo = true
                        )
                    } else {
                        existente.copy(
                            nombre = semilla.nombre,
                            correo = semilla.correo,
                            password = hashed,
                            rol = semilla.rol,
                            activo = true,
                            uuid = existente.uuid.ifBlank { SyncHelper.newUuid() },
                            isDeleted = false,
                            deletionDate = null
                        )
                    }
                    dao.insertar(usuario)
                    syncUsuario(usuario)
                    Log.i("USUARIO_SEMILLA", "Sincronizado ${semilla.username} → usuarios/${semilla.username}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarUsuarios() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.obtenerTodos().collect { lista -> _usuarios.value = lista }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun login(username: String, password: String): Usuario? {
        return try {
            val pass = password.trim()
            var user = dao.obtenerUsuarioPorUsername(username.trim())
                ?: dao.existeCorreo(username.trim())

            if (user != null && !PasswordHasher.verify(pass, user.password)) {
                user = null
            }

            if (user == null) {
                val remoto = firebaseRepo.obtenerUsuario(username.trim())
                    ?: firebaseRepo.obtenerUsuarioPorCorreo(username.trim())

                if (remoto != null && PasswordHasher.verify(pass, remoto.password)) {
                    user = remoto
                    if (PasswordHasher.needsRehash(remoto.password)) {
                        user = remoto.copy(password = PasswordHasher.hash(pass))
                    }
                    dao.insertar(user)
                }
            } else if (PasswordHasher.needsRehash(user.password)) {
                user = user.copy(password = PasswordHasher.hash(pass))
                dao.actualizar(user)
                syncUsuario(user)
            }

            if (user != null && user.activo) {
                dao.actualizarUltimoAcceso(user.id, System.currentTimeMillis())
                return user
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generarYEnviarOTP(identificador: String): Boolean {
        return try {
            val user = dao.obtenerUsuarioPorUsername(identificador) 
                ?: dao.existeCorreo(identificador)
                ?: firebaseRepo.obtenerUsuario(identificador)
                ?: firebaseRepo.obtenerUsuarioPorCorreo(identificador)

            if (user != null) {
                val otp = (100000 + Random().nextInt(900000)).toString()
                val expiracion = System.currentTimeMillis() + (5 * 60 * 1000)
                val userActualizado = user.copy(codigoRecuperacion = otp, codigoExpiracion = expiracion)
                
                dao.insertar(userActualizado)
                syncUsuario(userActualizado)
                Log.i("OTP_SENT", "Código OTP para ${user.username}: $otp")
                true
            } else false
        } catch (e: Exception) { false }
    }

    suspend fun verificarOTP(identificador: String, otp: String): Usuario? {
        val user = dao.obtenerUsuarioPorUsername(identificador) 
            ?: dao.existeCorreo(identificador)
            ?: firebaseRepo.obtenerUsuario(identificador)
            ?: firebaseRepo.obtenerUsuarioPorCorreo(identificador)
            
        return if (user != null && user.codigoRecuperacion == otp && (user.codigoExpiracion ?: 0) > System.currentTimeMillis()) {
            Log.i("OTP_VERIFIED", "OTP correcto para ${user.username}")
            user
        } else null
    }

    suspend fun cambiarPassword(usuarioId: Int, nuevaPass: String): Boolean {
        return try {
            val user = dao.obtenerUsuarioPorId(usuarioId)
            if (user != null) {
                val actualizado = user.copy(
                    password = PasswordHasher.hash(nuevaPass),
                    codigoRecuperacion = null,
                    codigoExpiracion = null
                )
                dao.actualizar(actualizado)
                syncUsuario(actualizado)
                Log.i("PASSWORD_CHANGED", "Password cambiada para ${user.username}")
                true
            } else false
        } catch (e: Exception) { false }
    }

    data class CrearUsuarioResult(val ok: Boolean, val mensaje: String)

    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        username: String,
        password: String,
        fotoPerfil: String,
        rol: String
    ): CrearUsuarioResult = withContext(Dispatchers.IO) {
        try {
            if (dao.existeUsername(username) != null) return@withContext CrearUsuarioResult(false, "El username ya existe")
            if (dao.existeCorreo(correo) != null) return@withContext CrearUsuarioResult(false, "El correo ya existe")

            val nuevo = Usuario(
                uuid = SyncHelper.newUuid(),
                nombre = nombre,
                correo = correo,
                username = username,
                password = PasswordHasher.hash(password),
                fotoPerfil = fotoPerfil,
                rol = rol
            )
            dao.insertar(nuevo)
            syncUsuario(nuevo)
            CrearUsuarioResult(true, "Usuario creado exitosamente")
        } catch (e: Exception) {
            CrearUsuarioResult(false, "Error: ${e.message}")
        }
    }

    suspend fun registrar(username: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (dao.existeUsername(username) != null) return@withContext false
            val nuevo = Usuario(
                uuid = SyncHelper.newUuid(),
                nombre = username,
                correo = "$username@agro.com",
                username = username,
                password = PasswordHasher.hash(pass),
                rol = UserRole.ADMIN.storageValue
            )
            dao.insertar(nuevo)
            syncUsuario(nuevo)
            true
        } catch (e: Exception) { false }
    }

    // Resto de métodos CRUD...
    fun editarUsuario(u: Usuario) { viewModelScope.launch(Dispatchers.IO) { dao.actualizar(u); syncUsuario(u) } }
    
    fun bloquearUsuario(u: Usuario) {
        val inactivo = u.copy(activo = false)
        viewModelScope.launch(Dispatchers.IO) {
            dao.actualizar(inactivo)
            syncUsuario(inactivo)
        }
    }

    fun activarUsuario(u: Usuario) {
        val activo = u.copy(activo = true)
        viewModelScope.launch(Dispatchers.IO) {
            dao.actualizar(activo)
            syncUsuario(activo)
        }
    }

    fun eliminarUsuario(u: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            val ts = System.currentTimeMillis()
            dao.softDelete(u.id, ts)
            syncUsuario(u.copy(isDeleted = true, deletionDate = ts))
        }
    }
    fun eliminarPermanente(id: Int) { viewModelScope.launch(Dispatchers.IO) { dao.eliminarPermanente(id) } }
    fun restaurarUsuario(id: Int) { viewModelScope.launch(Dispatchers.IO) { dao.restore(id) } }

    suspend fun actualizarPerfil(
        nombre: String,
        correo: String,
        password: String,
        fotoPerfil: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val actual = SessionManager.obtenerUsuario() ?: return@withContext false
            val actualizado = actual.copy(
                nombre = nombre,
                correo = correo,
                password = password,
                fotoPerfil = fotoPerfil.ifBlank { actual.fotoPerfil }
            )
            dao.actualizar(actualizado)
            syncUsuario(actualizado)
            SessionManager.login(actualizado)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recuperarUsernamePorCorreo(correo: String): String? = withContext(Dispatchers.IO) {
        val user = dao.existeCorreo(correo.trim())
            ?: firebaseRepo.obtenerUsuarioPorCorreo(correo.trim())
        user?.username
    }

    suspend fun generarOtpRecuperarUsuario(correo: String): String? = withContext(Dispatchers.IO) {
        val user = dao.existeCorreo(correo.trim()) ?: return@withContext null
        val otp = (100000 + Random().nextInt(900000)).toString()
        val exp = System.currentTimeMillis() + 5 * 60_000
        val updated = user.copy(codigoRecuperacion = otp, codigoExpiracion = exp)
        dao.actualizar(updated)
        syncUsuario(updated)
        otp
    }
    fun purgarAntiguos() {
        viewModelScope.launch(Dispatchers.IO) {
            val limite = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 días
            dao.purgarAntiguos(limite)
        }
    }
    fun obtenerPapelera(): Flow<List<Usuario>> = dao.obtenerPapelera()
}
