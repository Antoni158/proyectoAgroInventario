package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

import kotlinx.coroutines.flow.Flow

@Dao
interface ValeConDetallesDao {

    // TODOS LOS VALES

    @Transaction

    @Query(
        """
        SELECT *
        FROM vale
        ORDER BY idVale DESC
        """
    )

    fun obtenerValesConDetalles():
            Flow<List<ValeConDetalles>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM vale
        WHERE bodegaId = :bodegaId
        ORDER BY idVale DESC
        """
    )
    fun obtenerValesConDetallesPorBodega(
        bodegaId: String
    ): Flow<List<ValeConDetalles>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM vale
        WHERE bodegaId = :bodegaId
        AND (
            codigoVale LIKE '%' || :query || '%'
            OR responsable LIKE '%' || :query || '%'
            OR destino LIKE '%' || :query || '%'
        )
        ORDER BY idVale DESC
        """
    )
    fun buscarValesConDetallesPorBodega(
        bodegaId: String,
        query: String
    ): Flow<List<ValeConDetalles>>

    // POR ID

    @Transaction

    @Query(
        """
        SELECT *
        FROM vale
        WHERE idVale = :valeId
        LIMIT 1
        """
    )

    suspend fun obtenerValeConDetalles(

        valeId: Int

    ): ValeConDetalles?
}