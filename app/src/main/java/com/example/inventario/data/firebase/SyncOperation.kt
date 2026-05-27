package com.example.inventario.data.firebase

enum class SyncEntityType {
    USUARIO, BODEGA, CATEGORIA, PRODUCTO, ENTRADA, SALIDA,
    FACTURA, DETALLE_FACTURA, KARDEX, AUDITORIA, VALE, TRASLADO, NOTIFICACION
}

data class SyncOperation(
    val type: SyncEntityType,
    val entityKey: String,
    val bodegaId: String = "",
    val codigoBodega: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val retries: Int = 0
)
