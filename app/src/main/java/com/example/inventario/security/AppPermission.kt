package com.example.inventario.security

/**
 * Acciones y módulos restringibles en UI y navegación.
 */
enum class AppPermission {
    // Módulos principales
    VER_MODULO_BODEGA,
    VER_MODULO_PANEL,
    VER_MODULO_AUDITORIA,
    VER_MODULO_CONFIG,

    // Configuración
    CONFIG_BASICA,
    CONFIG_AVANZADA,
    ADMINISTRAR_USUARIOS,
    VER_LOGS,
    GESTIONAR_NOTIFICACIONES,
    GESTIONAR_TEMAS,
    SINCRONIZAR_NUBE,
    CONFIGURAR_PANEL,

    // Bodegas (global)
    CREAR_BODEGA,
    EDITAR_BODEGA,

    // Operaciones de inventario
    CREAR_PRODUCTO,
    EDITAR_PRODUCTO,
    ELIMINAR_REGISTRO,
    CREAR_ENTRADA,
    EDITAR_ENTRADA,
    CREAR_SALIDA,
    EDITAR_SALIDA,
    CREAR_FACTURA,
    EDITAR_FACTURA,
    CREAR_VALE,
    EDITAR_VALE,
    VER_KARDEX,
    AUDITAR_BODEGA,

    VER_PAPELERA,
    EXPORTAR
}
