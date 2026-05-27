package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface FacturaDetalleDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )

    suspend fun insertarDetalle(

        detalleFactura: DetalleFactura
    )

    // ACTUALIZAR

    @Update

    suspend fun actualizarDetalle(

        detalleFactura: DetalleFactura
    )

    // ELIMINAR

    @Delete

    suspend fun eliminarDetalle(

        detalleFactura: DetalleFactura
    )

    // OBTENER DETALLES FACTURA

    @Query(
        """
        SELECT *
        FROM detalle_factura
        WHERE facturaId = :facturaId
        ORDER BY idDetalle DESC
        """
    )

    fun obtenerDetallesFactura(

        facturaId: Int

    ): Flow<List<DetalleFactura>>

    // OBTENER DETALLES DIRECTO

    @Query(
        """
        SELECT *
        FROM detalle_factura
        WHERE facturaId = :facturaId
        ORDER BY idDetalle DESC
        """
    )

    suspend fun obtenerDetallesDirecto(

        facturaId: Int

    ): List<DetalleFactura>

    // OBTENER DETALLE POR ID

    @Query(
        """
        SELECT *
        FROM detalle_factura
        WHERE idDetalle = :id
        LIMIT 1
        """
    )

    suspend fun obtenerDetallePorId(

        id: Int

    ): DetalleFactura?

    // BUSCAR DETALLES

    @Query(
        """
        SELECT *
        FROM detalle_factura
        WHERE facturaId = :facturaId
        AND (
            codigoProducto LIKE '%' || :query || '%'
            OR descripcion LIKE '%' || :query || '%'
            OR categoria LIKE '%' || :query || '%'
        )
        ORDER BY idDetalle DESC
        """
    )

    fun buscarDetalles(

        facturaId: Int,

        query: String

    ): Flow<List<DetalleFactura>>

    // ELIMINAR DETALLES FACTURA

    @Query(
        """
        DELETE FROM detalle_factura
        WHERE facturaId = :facturaId
        """
    )

    suspend fun eliminarDetallesFactura(

        facturaId: Int
    )

    // TOTAL FACTURA

    @Query(
        """
        SELECT SUM(subtotal)
        FROM detalle_factura
        WHERE facturaId = :facturaId
        """
    )

    suspend fun calcularTotalFactura(

        facturaId: Int

    ): Double?

    // CONTAR PRODUCTOS

    @Query(
        """
        SELECT COUNT(idDetalle)
        FROM detalle_factura
        WHERE facturaId = :facturaId
        """
    )

    suspend fun contarProductosFactura(

        facturaId: Int

    ): Int

    // TOTAL CANTIDAD PRODUCTOS

    @Query(
        """
        SELECT SUM(cantidad)
        FROM detalle_factura
        WHERE facturaId = :facturaId
        """
    )

    suspend fun totalCantidadProductos(

        facturaId: Int

    ): Int?

    // ELIMINAR TODO

    @Query(
        """
        DELETE FROM detalle_factura
        """
    )
    suspend fun eliminarTodo()

    @Query("SELECT * FROM detalle_factura")
    suspend fun obtenerTodosSync(): List<DetalleFactura>
}
