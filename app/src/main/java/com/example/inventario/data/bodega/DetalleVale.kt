package com.example.inventario.data.bodega



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_vale"
)

data class DetalleVale(

    @PrimaryKey(autoGenerate = true)

    val idDetalle: Int = 0,

    val valeId: Int = 0,

    val productoCodigo: String = "",

    val productoDescripcion: String = "",

    val categoria: String = "",

    val cantidad: Int = 0,

    val bodegaId: String = "",

    val codigoBodega: String = "",

    val codigoSalida: String = ""
)