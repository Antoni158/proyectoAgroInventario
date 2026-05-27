package com.example.inventario.data.bodega


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "vale_salida"
)

data class ValeSalida(

    @PrimaryKey(autoGenerate = true)

    val idVale: Int = 0,

    val codigoVale: String = "",

    val productoCodigo: String = "",

    val productoDescripcion: String = "",

    val categoria: String = "",

    val cantidad: Int = 0,

    val responsable: String = "",

    val destino: String = "",

    val fecha: String = "",

    val observacion: String = "",

    val bodegaId: String = "",

    val codigoBodega: String = ""
)