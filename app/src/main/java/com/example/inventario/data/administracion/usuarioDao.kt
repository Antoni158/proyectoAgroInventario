package com.example.inventario.data.administracion

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )

    suspend fun insertar(

        usuario: Usuario

    ): Long

    // ACTUALIZAR

    @Update

    suspend fun actualizar(

        usuario: Usuario
    )

    // ELIMINAR

    @Delete

    suspend fun eliminar(

        usuario: Usuario
    )

    // LOGIN

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE username = :username
        AND password = :password
        AND isDeleted = 0
        LIMIT 1
        """
    )

    suspend fun login(

        username: String,

        password: String

    ): Usuario?

    // OBTENER TODOS

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE isDeleted = 0
        ORDER BY nombre ASC
        """
    )

    fun obtenerTodos():
            Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE isDeleted = 0")
    suspend fun obtenerTodosSync(): List<Usuario>

    // OBTENER POR ID

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE id = :id
        LIMIT 1
        """
    )

    suspend fun obtenerUsuarioPorId(

        id: Int

    ): Usuario?

    // OBTENER POR USERNAME

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE username = :username
        AND isDeleted = 0
        LIMIT 1
        """
    )

    suspend fun obtenerUsuarioPorUsername(

        username: String

    ): Usuario?

    @Query("SELECT * FROM usuarios WHERE uuid = :uuid AND isDeleted = 0 LIMIT 1")
    suspend fun obtenerUsuarioPorUuid(uuid: String): Usuario?

    // EXISTE USERNAME

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE username = :username
        LIMIT 1
        """
    )

    suspend fun existeUsername(

        username: String

    ): Usuario?

    // EXISTE CORREO

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE correo = :correo
        LIMIT 1
        """
    )

    suspend fun existeCorreo(

        correo: String

    ): Usuario?

    // BUSCAR USUARIOS

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE isDeleted = 0
        AND (
            nombre LIKE '%' || :query || '%'
            OR username LIKE '%' || :query || '%'
            OR rol LIKE '%' || :query || '%'
            OR correo LIKE '%' || :query || '%'
        )
        ORDER BY nombre ASC
        """
    )

    fun buscarUsuarios(

        query: String

    ): Flow<List<Usuario>>

    // ACTUALIZAR ULTIMO ACCESO

    @Query(
        """
        UPDATE usuarios
        SET ultimoAcceso = :fecha
        WHERE id = :id
        """
    )

    suspend fun actualizarUltimoAcceso(

        id: Int,

        fecha: Long
    )
// BLOQUEAR USUARIO

    @Query(
        """
    UPDATE usuarios
    SET activo = 0
    WHERE id = :id
    """
    )

    suspend fun bloquearUsuario(

        id: Int
    )


// ACTIVAR USUARIO

    @Query(
        """
    UPDATE usuarios
    SET activo = 1
    WHERE id = :id
    """
    )

    suspend fun activarUsuario(

        id: Int
    )

    // RESTAURAR

    @Query(
        """
        UPDATE usuarios
        SET isDeleted = 0,
        deletionDate = NULL
        WHERE id = :id
        """
    )

    suspend fun restore(

        id: Int
    )

    // PAPELERA

    @Query(
        """
        SELECT *
        FROM usuarios
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
        """
    )

    fun obtenerPapelera():

            Flow<List<Usuario>>

    // ELIMINAR DEFINITIVO

    @Query(
        """
        DELETE FROM usuarios
        WHERE id = :id
        """
    )

    suspend fun eliminarPermanente(

        id: Int
    )

    // PURGAR ANTIGUOS

    @Query(
        """
        DELETE FROM usuarios
        WHERE isDeleted = 1
        AND deletionDate < :limite
        """
    )

    suspend fun purgarAntiguos(

        limite: Long
    )

    // ACTUALIZAR PASSWORD

    @Query(
        """
        UPDATE usuarios
        SET password = :nuevaPassword
        WHERE correo = :correo
        """
    )

    suspend fun actualizarPassword(

        correo: String,

        nuevaPassword: String
    )

    // ELIMINAR TODO

    @Query(
        """
        DELETE FROM usuarios
        """
    )

    suspend fun deleteAll()

    // PAPELERA ANTIGUA COMPATIBILIDAD

    fun getDeletedUsuarios():

            Flow<List<Usuario>> {

        return obtenerPapelera()
    }
// SOFT DELETE

    @Query(
        """
    UPDATE usuarios
    SET isDeleted = 1,
    deletionDate = :date
    WHERE id = :id
    """
    )

    suspend fun softDelete(

        id: Int,

        date: Long
    )
    // ELIMINAR DEFINITIVO COMPATIBILIDAD

    suspend fun deletePermanently(

        id: Int

    ) {

        eliminarPermanente(id)
    }

    // ACTUALIZAR LOGIN COMPATIBILIDAD

    suspend fun actualizarLastLogin(

        id: Int,

        fecha: Long

    ) {

        actualizarUltimoAcceso(
            id,
            fecha
        )
    }
}