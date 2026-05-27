package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FacturaDao {

    // insertar

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        factura: Factura
    ): Long

    // actualizar

    @Update
    suspend fun update(
        factura: Factura
    )

    // eliminar

    @Delete
    suspend fun delete(
        factura: Factura
    )

    // obtener todas

    @Query(
        """
        SELECT * FROM facturas
        WHERE isDeleted = 0
        ORDER BY id DESC
        """
    )
    fun getFacturas(): Flow<List<Factura>>

    // obtener por bodega

    @Query(
        """
        SELECT * FROM facturas
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY id DESC
        """
    )
    fun getFacturasByBodega(
        bodegaId: String
    ): Flow<List<Factura>>

    // buscar facturas

    @Query(
        """
        SELECT * FROM facturas
        WHERE bodegaId = :bodegaId
        AND (
            numeroFactura LIKE '%' || :query || '%'
            OR proveedor LIKE '%' || :query || '%'
        )
        AND isDeleted = 0
        ORDER BY id DESC
        """
    )
    fun buscarFacturas(
        bodegaId: String,
        query: String
    ): Flow<List<Factura>>

    // obtener por id

    @Query(
        """
        SELECT * FROM facturas
        WHERE id = :id
        """
    )
    suspend fun getFacturaById(
        id: Int
    ): Factura?

    // obtener eliminadas

    @Query(
        """
        SELECT * FROM facturas
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
        """
    )
    fun getDeletedFacturas(): Flow<List<Factura>>

    // papelera

    @Query(
        """
        UPDATE facturas
        SET isDeleted = 1,
        deletionDate = :fecha
        WHERE id = :id
        """
    )
    suspend fun softDelete(
        id: Int,
        fecha: Long
    )

    // restaurar

    @Query(
        """
        UPDATE facturas
        SET isDeleted = 0,
        deletionDate = NULL
        WHERE id = :id
        """
    )
    suspend fun restore(
        id: Int
    )

    // eliminar permanente

    @Query(
        """
        DELETE FROM facturas
        WHERE id = :id
        """
    )
    suspend fun deletePermanently(
        id: Int
    )

    // limpieza papelera

    @Query(
        """
        DELETE FROM facturas
        WHERE isDeleted = 1
        AND deletionDate < :threshold
        """
    )
    suspend fun permanentPurge(
        threshold: Long
    )

    // total general

    @Query(
        """
        SELECT SUM(total)
        FROM facturas
        WHERE isDeleted = 0
        """
    )
    suspend fun obtenerTotalGeneral(): Double?

    // factura con detalles

    @Transaction
    @Query(
        """
        SELECT * FROM facturas
        WHERE id = :facturaId
        """
    )
    suspend fun obtenerFacturaConDetalles(
        facturaId: Int
    ): FacturaConDetalles

    // actualizar total

    @Query(
        """
        UPDATE facturas
        SET total = :nuevoTotal
        WHERE id = :facturaId
        """
    )
    suspend fun actualizarTotalFactura(
        facturaId: Int,
        nuevoTotal: Double
    )

    // sync firebase

    @Query(
        """
        SELECT * FROM facturas
        WHERE isDeleted = 0
        """
    )
    suspend fun obtenerTodasSync(): List<Factura>

    // buscar por numero factura

    @Query(
        """
        SELECT * FROM facturas
        WHERE numeroFactura = :numeroFactura
        LIMIT 1
        """
    )
    suspend fun buscarPorNumeroFactura(
        numeroFactura: String
    ): Factura?

    // contar facturas

    @Query(
        """
        SELECT COUNT(*)
        FROM facturas
        WHERE isDeleted = 0
        """
    )
    suspend fun contarFacturas(): Int

    // total por bodega

    @Query(
        """
        SELECT SUM(total)
        FROM facturas
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        """
    )
    suspend fun obtenerTotalPorBodega(
        bodegaId: String
    ): Double?

    // ultimas facturas

    @Query(
        """
        SELECT * FROM facturas
        WHERE isDeleted = 0
        ORDER BY id DESC
        LIMIT :limite
        """
    )
    suspend fun obtenerUltimasFacturas(
        limite: Int
    ): List<Factura>
}