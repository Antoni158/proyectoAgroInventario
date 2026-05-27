package com.example.inventario.data.bodega



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "traslado"
)

data class Traslado(

    @PrimaryKey(autoGenerate = true)

    val idTraslado: Int = 0,

    val codigoTraslado: String = "",

    val productoCodigo: String = "",

    val productoDescripcion: String = "",

    val categoria: String = "",

    val cantidad: Int = 0,

    val bodegaOrigen: String = "",

    val bodegaDestino: String = "",

    val responsable: String = "",

    val fecha: String = "",

    val observacion: String = "",

    val bodegaId: String = "",

    val codigoBodega: String = ""
)