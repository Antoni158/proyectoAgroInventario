package com.example.inventario.data.bodega


import androidx.room.Embedded
import androidx.room.Relation

data class FacturaConDetalles(

    @Embedded
    val factura: Factura,

    @Relation(
        parentColumn = "id",
        entityColumn = "facturaId"
    )

    val detalles: List<DetalleFactura>
)