package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facturas")
data class Factura(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numeroFactura: String = "",
    val fecha: String = "",
    val proveedor: String = "",
    val codigo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val costo: Double = 0.0,
    val total: Double = 0.0,
    val presupuesto: Double = 0.0,
    val notas: String = "",
    val bodegaId: String = "",
    val codigoBodega: String = "",
    val usuario: String = "",
    val isDeleted: Boolean = false,
    val deletionDate: Long? = null
)
