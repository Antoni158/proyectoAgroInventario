package com.example.inventario.data.bodega




import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "vale"
)

data class Vale(

    @PrimaryKey(autoGenerate = true)

    val idVale: Int = 0,

    val codigoVale: String = "",

    val responsable: String = "",

    val destino: String = "",

    val fecha: String = "",

    val observacion: String = "",

    val totalProductos: Int = 0,

    val bodegaId: String = "",

    val codigoBodega: String = "",

    /** CONFIRMADO | BORRADOR | ANULADO */
    val estado: String = "CONFIRMADO",

    val usuario: String = ""
)