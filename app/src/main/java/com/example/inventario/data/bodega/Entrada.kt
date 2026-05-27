package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "entradas"
)

data class Entrada(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    // CODIGO ENTRADA

    val codigoEntrada: String = "",

    // PRODUCTO

    val codigoProducto: String = "",

    val descripcion: String = "",

    // BODEGA

    val bodegaId: String = "",

    val codigoBodega: String = "",

    // CATEGORIA

    val categoria: String = "",

    // CANTIDAD

    val cantidad: Int = 0,

    // STOCK MINIMO

    val stockMinimo: Int = 0,

    // UNIDAD

    val unidad: String = "",

    // STATUS

    val status: String = "ACTIVO",

    // LOTE

    val lote: String = "",

    // UBICACION

    val ubicacion: String = "",

    // COSTOS

    val costoEntrada: Double = 0.0,

    val precioVenta: Double = 0.0,

    // PRESUPUESTO

    val presupuesto: Double = 0.0,

    // PROVEEDOR

    val proveedor: String = "",

    // FACTURA

    val numeroFactura: String = "",

    // TIPO ENTRADA

    // COMPRA
    // AJUSTE
    // DEVOLUCION
    // TRASLADO

    val tipoEntrada: String = "COMPRA",

    // FECHAS

    val fechaIngreso: String = "",

    /** @deprecated */
    val fechaVencimiento: String = "",

    // USUARIO

    val usuario: String = "",

    // OBSERVACIONES

    val notas: String = "",

    // ESTADO

    val activo: Boolean = true,

    // PAPELERA

    val isDeleted: Boolean = false,

    val deletionDate: Long? = null
)