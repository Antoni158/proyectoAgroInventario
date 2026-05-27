package com.example.inventario.data.bodega

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "bodegas"
)

data class Bodega(

    @PrimaryKey
    val id: String = "",

    val nombre: String = "",

    /** Código visible: ZAC-0001, GUA-0001 */
    val codigoCorto: String = "",

    val descripcion: String = "",

    val isDeleted: Boolean = false,

    val deletionDate: Long? = null
)