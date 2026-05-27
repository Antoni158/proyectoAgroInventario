package com.example.inventario.data.administracion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "logs"
)
data class Log(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    // USUARIO

    val usuarioId: Int = 0,

    val username: String = "",

    val rol: String = "",

    // MODULO

    // USUARIOS
    // PRODUCTOS
    // ENTRADAS
    // SALIDAS
    // FACTURAS
    // AUDITORIA
    // BODEGAS

    val modulo: String = "",

    // ACCION

    // CREAR
    // EDITAR
    // ELIMINAR
    // RESTAURAR
    // LOGIN
    // LOGOUT
    // BLOQUEAR

    val accion: String = "",

    // DESCRIPCION

    val descripcion: String = "",

    // FECHA

    val fecha: Long =
        System.currentTimeMillis()
)