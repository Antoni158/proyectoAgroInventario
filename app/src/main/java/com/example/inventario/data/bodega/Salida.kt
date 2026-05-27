package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "salidas"
)

data class Salida(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    // CODIGO SALIDA

    val codigoSalida: String = "",

    // CODIGO PRODUCTO

    val codigoProducto: String = "",

    // DESCRIPCION

    val descripcion: String = "",

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

    // RESPONSABLE

    val responsable: String = "",

    // DESTINO

    val destino: String = "",

    // VEHICULO

    val vehiculo: String = "",

    // COSTOS

    val costoUnitario: Double = 0.0,

    val precioVenta: Double = 0.0,

    // TOTAL

    val total: Double = 0.0,

    // VALE

    val numeroVale: String = "",

    // FACTURA

    val numeroFactura: String = "",

    // TIPO SALIDA

    // VENTA
    // TRASLADO
    // CONSUMO
    // AJUSTE

    val tipoSalida: String = "CONSUMO",

    val area: String = "",

    val placa: String = "",

    val campoAgricola: String = "",

    val centroCosto: String = "",

    // FECHAS

    val fechaSalida: String = "",

    val fechaVencimiento: String = "",

    // USUARIO

    val usuario: String = "",

    // BODEGA

    val bodegaId: String = "",

    val codigoBodega: String = "",

    // OBSERVACIONES

    val notas: String = "",

    // ACTIVO

    val activo: Boolean = true,

    // PAPELERA

    val isDeleted: Boolean = false,

    val deletionDate: Long? = null
)