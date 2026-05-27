package com.example.inventario.data.bodega


import androidx.room.Embedded
import androidx.room.Relation

data class ValeConDetalles(

    @Embedded

    val vale: Vale,

    @Relation(

        parentColumn = "idVale",

        entityColumn = "valeId"
    )

    val detalles:
    List<DetalleVale>
)