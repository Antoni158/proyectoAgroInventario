package com.example.inventario.data.administracion

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertar(

        log: Log

    ): Long

    // OBTENER TODOS

    @Query(

        """
        SELECT *
        FROM logs
        ORDER BY fecha DESC
        """
    )
    fun obtenerTodos():

            Flow<List<Log>>

    // OBTENER POR MODULO

    @Query(

        """
        SELECT *
        FROM logs
        WHERE modulo = :modulo
        ORDER BY fecha DESC
        """
    )
    fun obtenerPorModulo(

        modulo: String

    ): Flow<List<Log>>

    // OBTENER POR ACCION

    @Query(

        """
        SELECT *
        FROM logs
        WHERE accion = :accion
        ORDER BY fecha DESC
        """
    )
    fun obtenerPorAccion(

        accion: String

    ): Flow<List<Log>>

    // OBTENER POR USUARIO

    @Query(

        """
        SELECT *
        FROM logs
        WHERE usuarioId = :usuarioId
        ORDER BY fecha DESC
        """
    )
    fun obtenerPorUsuario(

        usuarioId: Int

    ): Flow<List<Log>>

    // BUSCAR LOGS

    @Query(

        """
        SELECT *
        FROM logs
        WHERE
        username LIKE '%' || :query || '%'
        OR modulo LIKE '%' || :query || '%'
        OR accion LIKE '%' || :query || '%'
        OR descripcion LIKE '%' || :query || '%'
        ORDER BY fecha DESC
        """
    )
    fun buscarLogs(

        query: String

    ): Flow<List<Log>>

    // CONTAR LOGS

    @Query(

        """
        SELECT COUNT(*)
        FROM logs
        """
    )
    suspend fun contarLogs():

            Int

    // ELIMINAR TODOS

    @Query(

        """
        DELETE FROM logs
        """
    )
    suspend fun eliminarTodos()

    // ELIMINAR POR ID

    @Query(

        """
        DELETE FROM logs
        WHERE id = :id
        """
    )
    suspend fun eliminarPorId(

        id: Int
    )

    // ACTUALIZAR

    @Update
    suspend fun actualizar(

        log: Log
    )

    // ELIMINAR

    @Delete
    suspend fun eliminar(

        log: Log
    )
}