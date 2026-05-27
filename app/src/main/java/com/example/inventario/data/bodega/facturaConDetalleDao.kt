package com.example.inventario.data.bodega



import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

import kotlinx.coroutines.flow.Flow

@Dao
interface FacturaConDetallesDao {

    // OBTENER FACTURA CON DETALLES

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

    // OBTENER TODAS LAS FACTURAS CON DETALLES

    @Transaction

    @Query(
        """
        SELECT * FROM facturas
        WHERE isDeleted = 0
        ORDER BY id DESC
        """
    )

    fun obtenerFacturasConDetalles():

            Flow<List<FacturaConDetalles>>

    // OBTENER POR BODEGA

    @Transaction

    @Query(
        """
        SELECT * FROM facturas
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY id DESC
        """
    )

    fun obtenerFacturasConDetallesPorBodega(

        bodegaId: String

    ): Flow<List<FacturaConDetalles>>

    // BUSCAR FACTURAS

    @Transaction

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

    fun buscarFacturasConDetalles(

        bodegaId: String,

        query: String

    ): Flow<List<FacturaConDetalles>>

    // FACTURAS ELIMINADAS

    @Transaction

    @Query(
        """
        SELECT * FROM facturas
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
        """
    )

    fun obtenerFacturasEliminadas():

            Flow<List<FacturaConDetalles>>

    // TOTAL GENERAL FACTURAS

    @Query(
        """
        SELECT SUM(total)
        FROM facturas
        WHERE isDeleted = 0
        """
    )

    suspend fun obtenerTotalGeneral():

            Double?

    // TOTAL FACTURAS

    @Query(
        """
        SELECT COUNT(id)
        FROM facturas
        WHERE isDeleted = 0
        """
    )

    suspend fun contarFacturas():

            Int

    // TOTAL PRODUCTOS FACTURADOS

    @Query(
        """
        SELECT SUM(cantidad)
        FROM detalle_factura
        """
    )

    suspend fun totalProductosFacturados():

            Int?

    // PROMEDIO FACTURAS

    @Query(
        """
        SELECT AVG(total)
        FROM facturas
        WHERE isDeleted = 0
        """
    )

    suspend fun promedioFacturas():

            Double?

    // TOTAL POR PROVEEDOR

    @Query(
        """
        SELECT SUM(total)
        FROM facturas
        WHERE proveedor = :proveedor
        AND isDeleted = 0
        """
    )

    suspend fun totalPorProveedor(

        proveedor: String

    ): Double?

    // FACTURAS POR FECHA

    @Transaction

    @Query(
        """
        SELECT * FROM facturas
        WHERE fecha = :fecha
        AND isDeleted = 0
        ORDER BY id DESC
        """
    )

    fun obtenerFacturasPorFecha(

        fecha: String

    ): Flow<List<FacturaConDetalles>>
}