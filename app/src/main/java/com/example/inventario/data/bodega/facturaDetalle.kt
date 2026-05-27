package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_factura"
)

data class DetalleFactura(

    @PrimaryKey(autoGenerate = true)

    val idDetalle: Int = 0,

    val facturaId: Int = 0,

    val codigoProducto: String = "",

    val descripcion: String = "",

    val categoria: String = "",

    val cantidad: Int = 0,

    val precioUnitario: Double = 0.0,

    val subtotal: Double = 0.0
)