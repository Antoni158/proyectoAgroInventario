package com.example.inventario.viewModel

import com.example.inventario.data.administracion.Usuario
import com.example.inventario.security.AppPermission
import com.example.inventario.security.MainModule
import com.example.inventario.security.RoleManager
import com.example.inventario.security.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    private val _bodegaActual = MutableStateFlow<String?>(null)

    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()
    val bodegaActual: StateFlow<String?> = _bodegaActual.asStateFlow()

    fun login(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun logout() {
        _usuarioActual.value = null
        _bodegaActual.value = null
    }

    fun cerrarSesion() = logout()

    fun cerrarSesionCompleto(context: android.content.Context) {
        com.example.inventario.security.AppPreferences.init(context)
        com.example.inventario.security.AppPreferences.limpiarSesion()
        logout()
    }

    fun seleccionarBodega(bodegaId: String) {
        _bodegaActual.value = bodegaId
    }

    fun limpiarBodega() {
        _bodegaActual.value = null
    }

    fun obtenerBodegaActual(): String = _bodegaActual.value.orEmpty()
    fun obtenerUsuario(): Usuario? = _usuarioActual.value
    fun obtenerIdUsuario(): Int = _usuarioActual.value?.id ?: 0
    fun nombreUsuario(): String = _usuarioActual.value?.nombre.orEmpty()
    fun usernameUsuario(): String = _usuarioActual.value?.username.orEmpty()
    fun correoUsuario(): String = _usuarioActual.value?.correo.orEmpty()
    fun fotoUsuario(): String = _usuarioActual.value?.fotoPerfil.orEmpty()
    fun rolUsuario(): String = _usuarioActual.value?.rol.orEmpty()
    fun rolActual(): UserRole = RoleManager.roleOf(_usuarioActual.value)
    fun haySesion(): Boolean = _usuarioActual.value != null
    fun tienePermiso(permission: AppPermission): Boolean = RoleManager.hasPermission(_usuarioActual.value, permission)
    fun modulosPrincipales(): List<MainModule> = RoleManager.visibleMainModules(rolActual())
    fun esSoloLectura(): Boolean = RoleManager.isReadOnly(rolActual())
    fun esAdmin(): Boolean = !esSoloLectura()
    fun esAuditor(): Boolean = rolActual() == UserRole.AUDITOR
    fun esVisor(): Boolean = rolActual() == UserRole.VISOR
    fun esUsuario(): Boolean = esAdmin()
    fun puedeAuditar(): Boolean = RoleManager.canAuditGlobally(rolActual())
    fun puedeAuditarEnBodega(): Boolean = RoleManager.canAuditInBodega(rolActual())
    fun puedeAdministrar(): Boolean = RoleManager.canManageUsers(rolActual())
    fun puedeGestionarBodegas(): Boolean = RoleManager.canManageBodegas(rolActual())
    fun puedeEscribirInventario(): Boolean = RoleManager.canWriteInventory(rolActual())
    fun puedeEliminar(): Boolean = RoleManager.canDelete(rolActual())
    fun puedeExportar(): Boolean = RoleManager.canExport(rolActual())
    fun puedeVerPapelera(): Boolean = RoleManager.canViewPapelera(rolActual())
    fun puedeSincronizar(): Boolean = RoleManager.canSync(rolActual())
    fun puedeGestionarConfig(): Boolean = RoleManager.canNavigateToConfig(rolActual())
    fun etiquetaRol(): String = RoleManager.displayLabel(rolActual())
}
