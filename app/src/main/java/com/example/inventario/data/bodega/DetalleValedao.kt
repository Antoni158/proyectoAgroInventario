package com.example.inventario.data.bodega



import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleValeDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )

    suspend fun insertarDetalle(

        detalleVale: DetalleVale
    )

    // ACTUALIZAR

    @Update

    suspend fun actualizarDetalle(

        detalleVale: DetalleVale
    )

    // ELIMINAR

    @Delete

    suspend fun eliminarDetalle(

        detalleVale: DetalleVale
    )

    // OBTENER DETALLES

    @Query(
        """
        SELECT *
        FROM detalle_vale
        WHERE valeId = :valeId
        ORDER BY idDetalle DESC
        """
    )

    fun obtenerDetallesVale(

        valeId: Int

    ): Flow<List<DetalleVale>>

    // DIRECTO

    @Query(
        """
        SELECT *
        FROM detalle_vale
        WHERE valeId = :valeId
        ORDER BY idDetalle DESC
        """
    )

    suspend fun obtenerDetallesDirecto(

        valeId: Int

    ): List<DetalleVale>

    // POR ID

    @Query(
        """
        SELECT *
        FROM detalle_vale
        WHERE idDetalle = :id
        LIMIT 1
        """
    )

    suspend fun obtenerDetallePorId(

        id: Int

    ): DetalleVale?

    @Query(
        """
        SELECT * FROM detalle_vale
        WHERE valeId = :valeId
        AND productoCodigo = :productoCodigo
        AND codigoSalida = :codigoSalida
        LIMIT 1
        """
    )
    suspend fun buscarDetalleExistente(
        valeId: Int,
        productoCodigo: String,
        codigoSalida: String
    ): DetalleVale?

    // BUSCAR

    @Query(
        """
        SELECT *
        FROM detalle_vale
        WHERE valeId = :valeId
        AND (
            productoCodigo LIKE '%' || :query || '%'
            OR productoDescripcion LIKE '%' || :query || '%'
            OR categoria LIKE '%' || :query || '%'
        )
        ORDER BY idDetalle DESC
        """
    )

    fun buscarDetalles(

        valeId: Int,

        query: String

    ): Flow<List<DetalleVale>>

    // TOTAL PRODUCTOS

    @Query(
        """
        SELECT COUNT(idDetalle)
        FROM detalle_vale
        WHERE valeId = :valeId
        """
    )

    suspend fun totalProductosVale(

        valeId: Int

    ): Int

    // TOTAL CANTIDADES

    @Query(
        """
        SELECT SUM(cantidad)
        FROM detalle_vale
        WHERE valeId = :valeId
        """
    )

    suspend fun totalCantidades(

        valeId: Int

    ): Int?

    // ELIMINAR DETALLES

    @Query(
        """
        DELETE FROM detalle_vale
        WHERE valeId = :valeId
        """
    )

    suspend fun eliminarDetallesVale(

        valeId: Int
    )

    // ELIMINAR TODO

    @Query(
        """
        DELETE FROM detalle_vale
        """
    )
    suspend fun eliminarTodo()

    @Query("SELECT * FROM detalle_vale")
    suspend fun obtenerTodosSync(): List<DetalleVale>

    @Query(
        """
        SELECT *
        FROM detalle_vale
        WHERE bodegaId = :bodegaId
        ORDER BY idDetalle DESC
        """
    )
    fun obtenerDetallesPorBodega(
        bodegaId: String
    ): Flow<List<DetalleVale>>
}