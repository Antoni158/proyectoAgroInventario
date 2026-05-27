package com.example.inventario.security

/**
 * Roles empresariales del sistema.
 * [BODEGA] y alias legacy se normalizan a [ADMIN] para operación diaria.
 */
enum class UserRole(val storageValue: String) {
    ADMIN("ADMIN"),
    BODEGA("BODEGA"),
    AUDITOR("AUDITOR"),
    VISOR("VISOR");

    companion object {
        fun fromStorage(value: String?): UserRole {
            return when (value?.trim()?.uppercase()) {
                ADMIN.storageValue -> ADMIN
                BODEGA.storageValue -> ADMIN
                AUDITOR.storageValue -> AUDITOR
                VISOR.storageValue -> VISOR
                "OPERADOR", "USUARIO" -> ADMIN
                else -> VISOR
            }
        }

        fun normalizeForStorage(role: UserRole): String = when (role) {
            BODEGA -> ADMIN.storageValue
            else -> role.storageValue
        }
    }
}
