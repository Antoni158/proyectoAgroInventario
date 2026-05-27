package com.example.inventario.data.bodega



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "kardex"
)

data class Kardex(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    // CODIGO MOVIMIENTO

    val codigoMovimiento: String = "",

    // PRODUCTO

    val codigoProducto: String = "",

    val descripcion: String = "",

    // BODEGA

    val bodegaId: String = "",
    val codigoBodega: String = "",

    // CATEGORIA

    val categoria: String = "",

    // MOVIMIENTO

    // ENTRADA
    // SALIDA
    // AJUSTE
    // TRASLADO

    val tipoMovimiento: String = "",

    // CANTIDAD

    val cantidad: Int = 0,

    // SALDOS

    val saldoAnterior: Int = 0,

    val saldoNuevo: Int = 0,

    // COSTOS

    val costoUnitario: Double = 0.0,

    val totalMovimiento: Double = 0.0,

    // STATUS

    val status: String = "ACTIVO",

    // LOTE

    val lote: String = "",

    // UBICACION

    val ubicacion: String = "",

    // REFERENCIAS

    val numeroFactura: String = "",

    val numeroVale: String = "",

    // RESPONSABLE

    val usuario: String = "",

    // DESTINO

    val destino: String = "",

    // OBSERVACIONES

    val notas: String = "",

    // FECHA

    val fechaMovimiento: String = "",

    // ACTIVO

    val activo: Boolean = true,

    // PAPELERA

    val isDeleted: Boolean = false,

    val deletionDate: Long? = null
)