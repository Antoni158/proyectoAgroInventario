package com.example.inventario.data.bodega




import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface ValeDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )

    suspend fun insertarVale(

        vale: Vale
    ): Long

    // ACTUALIZAR

    @Update

    suspend fun actualizarVale(

        vale: Vale
    )

    // ELIMINAR

    @Delete

    suspend fun eliminarVale(

        vale: Vale
    )

    // OBTENER TODOS

    @Query(
        """
        SELECT *
        FROM vale
        ORDER BY idVale DESC
        """
    )

    fun obtenerVales():
            Flow<List<Vale>>

    @Query(
        """
        SELECT *
        FROM vale
        WHERE bodegaId = :bodegaId
        ORDER BY idVale DESC
        """
    )
    fun obtenerValesPorBodega(
        bodegaId: String
    ): Flow<List<Vale>>

    // OBTENER POR ID

    @Query(
        """
        SELECT *
        FROM vale
        WHERE idVale = :id
        LIMIT 1
        """
    )

    suspend fun obtenerValePorId(

        id: Int

    ): Vale?

    @Query(
        """
        SELECT *
        FROM vale
        WHERE codigoVale = :codigoVale
        AND bodegaId = :bodegaId
        LIMIT 1
        """
    )
    suspend fun obtenerValePorCodigo(
        codigoVale: String,
        bodegaId: String
    ): Vale?

    @Query(
        """
        SELECT *
        FROM vale
        WHERE bodegaId = :bodegaId
        AND (
            codigoVale LIKE '%' || :query || '%'
            OR responsable LIKE '%' || :query || '%'
            OR destino LIKE '%' || :query || '%'
            OR observacion LIKE '%' || :query || '%'
        )
        ORDER BY idVale DESC
        """
    )
    fun buscarValesPorBodega(
        bodegaId: String,
        query: String
    ): Flow<List<Vale>>

    @Query(
        """
        SELECT codigoVale FROM vale
        WHERE bodegaId = :bodegaId AND codigoVale != ''
        """
    )
    suspend fun listarCodigosVale(bodegaId: String): List<String>

    // TOTAL VALES

    @Query(
        """
        SELECT COUNT(idVale)
        FROM vale
        """
    )

    suspend fun totalVales():
            Int

    @Query(
        """
        SELECT COUNT(idVale)
        FROM vale
        WHERE bodegaId = :bodegaId
        """
    )
    suspend fun totalValesPorBodega(
        bodegaId: String
    ): Int

    // ELIMINAR TODO

    @Query(
        """
        DELETE FROM vale
        """
    )

    suspend fun eliminarTodo()

    @Query("SELECT * FROM vale")
    suspend fun obtenerTodosSync(): List<Vale>
}