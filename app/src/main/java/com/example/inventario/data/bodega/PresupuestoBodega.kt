package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Presupuesto meta ingresado por bodega y período (mensual, trimestral, etc.) */
@Entity(
    tableName = "presupuestos_bodega",
    indices = [Index(value = ["bodegaId", "tipoPeriodo", "anio", "indicePeriodo"], unique = true)]
)
data class PresupuestoBodega(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bodegaId: String = "",
    /** MENSUAL | TRIMESTRAL | SEMESTRAL | ANUAL */
    val tipoPeriodo: String = "MENSUAL",
    val anio: Int = 0,
    /** Mes 1-12, trimestre 1-4, semestre 1-2, anual 1 */
    val indicePeriodo: Int = 1,
    val monto: Double = 0.0,
    val notas: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
