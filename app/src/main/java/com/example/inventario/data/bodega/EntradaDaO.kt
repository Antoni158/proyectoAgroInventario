package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntradaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entrada: Entrada): Long

    @Update
    suspend fun update(entrada: Entrada)

    @Delete
    suspend fun delete(entrada: Entrada)

    @Query("SELECT * FROM entradas WHERE id = :id LIMIT 1")
    suspend fun getEntradaById(id: Int): Entrada?

    @Query(
        """
        SELECT * FROM entradas
        WHERE codigoEntrada = :codigoEntrada AND bodegaId = :bodegaId AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getEntradaByCodigo(codigoEntrada: String, bodegaId: String): Entrada?

    @Query(
        """
        SELECT codigoEntrada FROM entradas
        WHERE bodegaId = :bodegaId AND codigoEntrada != '' AND isDeleted = 0
        """
    )
    suspend fun listarCodigosEntrada(bodegaId: String): List<String>

    @Query("SELECT * FROM entradas WHERE bodegaId = :bodegaId AND isDeleted = 0 ORDER BY fechaIngreso DESC")
    fun getEntradasByBodega(bodegaId: String): Flow<List<Entrada>>

    @Query("""
        SELECT * FROM entradas 
        WHERE bodegaId = :bodegaId AND isDeleted = 0 
        AND (descripcion LIKE '%' || :query || '%' OR codigoProducto LIKE '%' || :query || '%' OR proveedor LIKE '%' || :query || '%')
        ORDER BY fechaIngreso DESC
    """)
    fun buscarEntradas(bodegaId: String, query: String): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas WHERE codigoProducto = :codigoProducto AND bodegaId = :bodegaId AND isDeleted = 0 ORDER BY fechaIngreso DESC")
    fun getEntradasByCodigo(codigoProducto: String, bodegaId: String): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas WHERE cantidad <= stockMinimo AND stockMinimo > 0 AND isDeleted = 0 ORDER BY cantidad ASC")
    fun getEntradasStock(): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas WHERE status = :status AND isDeleted = 0 ORDER BY fechaIngreso DESC")
    fun getEntradasByStatus(status: String): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas WHERE tipoEntrada = :tipoEntrada AND isDeleted = 0 ORDER BY fechaIngreso DESC")
    fun getEntradasByTipo(tipoEntrada: String): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas WHERE fechaVencimiento != '' AND isDeleted = 0 ORDER BY fechaVencimiento ASC")
    fun getEntradasVencimiento(): Flow<List<Entrada>>

    @Query("UPDATE entradas SET status = :status WHERE id = :id")
    suspend fun actualizarStatus(id: Int, status: String)

    @Query("UPDATE entradas SET isDeleted = 1, deletionDate = :date WHERE id = :id")
    suspend fun softDelete(id: Int, date: Long)

    @Query("UPDATE entradas SET isDeleted = 0, deletionDate = NULL WHERE id = :id")
    suspend fun restore(id: Int)

    @Query("SELECT * FROM entradas WHERE isDeleted = 1 ORDER BY deletionDate DESC")
    fun getDeletedEntradas(): Flow<List<Entrada>>

    @Query("DELETE FROM entradas WHERE id = :id")
    suspend fun deletePermanently(id: Int)

    @Query("DELETE FROM entradas WHERE isDeleted = 1 AND deletionDate <= :threshold")
    suspend fun permanentPurge(threshold: Long)

    @Query("DELETE FROM entradas")
    suspend fun deleteAll()

    @Query("SELECT * FROM entradas WHERE isDeleted = 0")
    suspend fun obtenerTodasSync(): List<Entrada>
}