package com.example.inventario.data.administracion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val username: String = "",
    val password: String = "",
    val rol: String = "BODEGA",
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimoAcceso: Long? = null,
    val isDeleted: Boolean = false,
    val deletionDate: Long? = null,
    val fotoPerfil: String = " ",
    val codigoRecuperacion: String? = null,
    val codigoExpiracion: Long? = null
)
