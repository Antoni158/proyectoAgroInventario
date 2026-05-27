package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos"
)
data class Producto(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    val bodegaId: String = "",

    val codigoBodega: String = "",

    val codigo: String = "",

    val descripcion: String = "",

    val categoria: String = "",

    val prefijoCategoria: String = "",

    val cantidad: Int = 0,

    val stockMinimo: Int = 0,

    val status: String = "ACTIVO",

    val presupuesto: Double = 0.0,

    val stockBajo: Boolean = false,

    val unidad: String = "",

    val ubicacion: String = "",

    val proveedor: String = "",

    val costo: Double = 0.0,

    val centroCosto: String = "",

    val areaOperativa: String = "",

    val usoOperativo: String = "",

    val precioVenta: Double = 0.0,

    val lote: String = "",

    val fechaIngreso: String = "",

    val fechaVencimiento: String = "",

    val ultimoMovimiento: Long? = null,

    val notas: String = "",

    val activo: Boolean = true,

    val isDeleted: Boolean = false,

    val deletionDate: Long? = null
)

/** Proyección Room para generación de códigos automáticos */
data class ProductoCodigoDesc(
    val codigo: String,
    val descripcion: String,
    val categoria: String = ""
)