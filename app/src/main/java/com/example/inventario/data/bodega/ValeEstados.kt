package com.example.inventario.data.bodega

object ValeEstados {
    const val CONFIRMADO = "CONFIRMADO"
    const val PENDIENTE = "PENDIENTE"
    const val TRASLADO = "TRASLADO"
    const val CANCELADO = "CANCELADO"
    const val ANULADO = "ANULADO"

    val TODOS = listOf(CONFIRMADO, PENDIENTE, TRASLADO, CANCELADO, ANULADO)

    fun etiqueta(estado: String): String = when (estado.uppercase()) {
        CONFIRMADO -> "Confirmado"
        PENDIENTE -> "Pendiente"
        TRASLADO -> "Traslado"
        CANCELADO -> "Cancelado"
        ANULADO -> "Anulado"
        else -> estado
    }

    fun colorArgb(estado: String): Long = when (estado.uppercase()) {
        CONFIRMADO -> 0xFF2E7D32
        PENDIENTE -> 0xFFF9A825
        TRASLADO -> 0xFF1565C0
        CANCELADO -> 0xFF757575
        ANULADO -> 0xFFC62828
        else -> 0xFF616161
    }
}
