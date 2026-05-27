package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias"
)
data class Categoria(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    val nombre: String = "",

    val prefijo: String = "",

    val correlativoActual: Int = 0,

    val codigoBodega: String = "",

    val descripcion: String = "",

    val area: String = "",

    val colorHex: String = "#2E7D32",

    val icono: String = "category",

    val activa: Boolean = true,

    val sincronizado: Boolean = false,

    val fechaCreacion: Long =
        System.currentTimeMillis(),

    val ultimaActualizacion: Long =
        System.currentTimeMillis(),

    val isDeleted: Boolean = false,

    val deletionDate: Long? = null
)