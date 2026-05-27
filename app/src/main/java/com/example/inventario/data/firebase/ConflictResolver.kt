package com.example.inventario.data.firebase

import com.example.inventario.data.administracion.Usuario

object ConflictResolver {

    fun mergeUsuario(local: Usuario, remote: Usuario): Usuario {
        val localTs = local.ultimoAcceso ?: local.fechaCreacion
        val remoteTs = remote.ultimoAcceso ?: remote.fechaCreacion
        val winner = if (localTs >= remoteTs) local else remote
        val loser = if (localTs >= remoteTs) remote else local
        return winner.copy(
            uuid = winner.uuid.ifBlank { loser.uuid },
            nombre = winner.nombre.ifBlank { loser.nombre },
            correo = winner.correo.ifBlank { loser.correo },
            fotoPerfil = winner.fotoPerfil.ifBlank { loser.fotoPerfil },
            activo = winner.activo || loser.activo
        )
    }

    fun <T> pickNewer(local: T, remote: T, localTs: Long, remoteTs: Long): T =
        if (localTs >= remoteTs) local else remote
}
