package com.example.inventario.data.notificacion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notificaciones")
data class AppNotificacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "INFO",
    val productoCodigo: String = "",
    val bodegaId: String = "",
    val referenciaId: String = "",
    val usuario: String = "",
    val leida: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
