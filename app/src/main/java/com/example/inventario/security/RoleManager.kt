package com.example.inventario.security

import com.example.inventario.data.administracion.Usuario

/**
 * Matriz centralizada de permisos por rol oficial del sistema.
 *
 * ADMIN: operación diaria completa en bodega y configuración.
 * AUDITOR: supervisión máxima.
 * VISOR: solo lectura en panel, reportes y consultas.
 */
object RoleManager {

    private val adminPermissions: Set<AppPermission> = setOf(
        AppPermission.VER_MODULO_BODEGA,
        AppPermission.VER_MODULO_PANEL,
        AppPermission.VER_MODULO_CONFIG,
        AppPermission.CONFIG_BASICA,
        AppPermission.GESTIONAR_NOTIFICACIONES,
        AppPermission.GESTIONAR_TEMAS,
        AppPermission.ADMINISTRAR_USUARIOS,
        AppPermission.SINCRONIZAR_NUBE,
        AppPermission.CREAR_PRODUCTO,
        AppPermission.EDITAR_PRODUCTO,
        AppPermission.ELIMINAR_REGISTRO,
        AppPermission.CREAR_ENTRADA,
        AppPermission.EDITAR_ENTRADA,
        AppPermission.CREAR_SALIDA,
        AppPermission.EDITAR_SALIDA,
        AppPermission.CREAR_FACTURA,
        AppPermission.EDITAR_FACTURA,
        AppPermission.CREAR_VALE,
        AppPermission.EDITAR_VALE,
        AppPermission.VER_KARDEX,
        AppPermission.VER_PAPELERA,
        AppPermission.EXPORTAR
    )

    private val auditorPermissions: Set<AppPermission> =
        AppPermission.entries.toSet()

    private val visorPermissions: Set<AppPermission> = setOf(
        AppPermission.VER_MODULO_BODEGA,
        AppPermission.VER_MODULO_PANEL,
        AppPermission.VER_MODULO_CONFIG,
        AppPermission.CONFIG_BASICA,
        AppPermission.GESTIONAR_NOTIFICACIONES,
        AppPermission.GESTIONAR_TEMAS,
        AppPermission.VER_KARDEX,
        AppPermission.EXPORTAR
    )

    private val matrix: Map<UserRole, Set<AppPermission>> = mapOf(
        UserRole.ADMIN to adminPermissions,
        UserRole.AUDITOR to auditorPermissions,
        UserRole.VISOR to visorPermissions,
        UserRole.BODEGA to adminPermissions
    )

    private val bodegaMenuByRole: Map<UserRole, Set<BodegaMenuKey>> = mapOf(
        UserRole.ADMIN to setOf(
            BodegaMenuKey.INVENTARIO,
            BodegaMenuKey.ENTRADAS,
            BodegaMenuKey.SALIDAS,
            BodegaMenuKey.VALES,
            BodegaMenuKey.EXISTENCIAS,
            BodegaMenuKey.KARDEX,
            BodegaMenuKey.CATEGORIAS,
            BodegaMenuKey.FACTURAS,
            BodegaMenuKey.MOVIMIENTOS,
            BodegaMenuKey.ANALISIS
        ),
        UserRole.BODEGA to setOf(
            BodegaMenuKey.INVENTARIO,
            BodegaMenuKey.ENTRADAS,
            BodegaMenuKey.SALIDAS,
            BodegaMenuKey.VALES,
            BodegaMenuKey.EXISTENCIAS,
            BodegaMenuKey.KARDEX,
            BodegaMenuKey.CATEGORIAS,
            BodegaMenuKey.FACTURAS,
            BodegaMenuKey.MOVIMIENTOS,
            BodegaMenuKey.ANALISIS
        ),
        UserRole.AUDITOR to BodegaMenuKey.entries.toSet(),
        UserRole.VISOR to setOf(
            BodegaMenuKey.INVENTARIO,
            BodegaMenuKey.EXISTENCIAS,
            BodegaMenuKey.KARDEX,
            BodegaMenuKey.ANALISIS,
            BodegaMenuKey.MOVIMIENTOS
        )
    )

    fun roleOf(usuario: Usuario?): UserRole = UserRole.fromStorage(usuario?.rol)

    fun roleOf(rol: String?): UserRole = UserRole.fromStorage(rol)

    fun hasPermission(role: UserRole, permission: AppPermission): Boolean =
        matrix[role]?.contains(permission) == true

    fun hasPermission(usuario: Usuario?, permission: AppPermission): Boolean =
        hasPermission(roleOf(usuario), permission)

    fun hasPermission(rol: String?, permission: AppPermission): Boolean =
        hasPermission(roleOf(rol), permission)

    fun isReadOnly(role: UserRole): Boolean = role == UserRole.VISOR

    fun visibleMainModules(role: UserRole): List<MainModule> =
        MainModule.entries.filter { hasPermission(role, it.requiredPermission) }

    fun visibleBodegaMenuKeys(role: UserRole): Set<BodegaMenuKey> =
        bodegaMenuByRole[role].orEmpty()

    fun canNavigateToConfig(role: UserRole): Boolean =
        hasPermission(role, AppPermission.VER_MODULO_CONFIG)

    fun canManageUsers(role: UserRole): Boolean =
        hasPermission(role, AppPermission.ADMINISTRAR_USUARIOS)

    fun canAuditGlobally(role: UserRole): Boolean =
        hasPermission(role, AppPermission.VER_MODULO_AUDITORIA)

    fun canAuditInBodega(role: UserRole): Boolean =
        hasPermission(role, AppPermission.AUDITAR_BODEGA)

    fun canManageBodegas(role: UserRole): Boolean =
        hasPermission(role, AppPermission.CREAR_BODEGA)

    fun canWriteInventory(role: UserRole): Boolean =
        hasPermission(role, AppPermission.CREAR_PRODUCTO) ||
            hasPermission(role, AppPermission.EDITAR_PRODUCTO)

    fun canDelete(role: UserRole): Boolean =
        hasPermission(role, AppPermission.ELIMINAR_REGISTRO)

    fun canExport(role: UserRole): Boolean =
        hasPermission(role, AppPermission.EXPORTAR)

    fun canViewPapelera(role: UserRole): Boolean =
        hasPermission(role, AppPermission.VER_PAPELERA)

    fun canSync(role: UserRole): Boolean =
        hasPermission(role, AppPermission.SINCRONIZAR_NUBE)

    fun displayLabel(role: UserRole): String = when (role) {
        UserRole.ADMIN, UserRole.BODEGA -> "Administrador"
        UserRole.AUDITOR -> "Auditor"
        UserRole.VISOR -> "Visor"
    }

    fun assignableRoles(): List<UserRole> = listOf(
        UserRole.ADMIN,
        UserRole.AUDITOR,
        UserRole.VISOR
    )
}
