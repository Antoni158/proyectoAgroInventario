package com.example.inventario.data.Auditoria

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auditorias")
data class Auditoria(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int = 0,
    val codigo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val bodegaId: String = "",
    val codigoBodega: String = "",
    val nombreBodega: String = "",
    val stockSistema: Double = 0.0,
    val stockFisico: Double = 0.0,
    val diferencia: Double = 0.0,
    val estado: String = "EXACTO",
    val observacion: String = "",
    val auditorId: Int = 0,
    val auditorNombre: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val ajusteAplicado: Boolean = false
)
