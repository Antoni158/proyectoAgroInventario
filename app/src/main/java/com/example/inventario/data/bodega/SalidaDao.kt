package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalidaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(salida: Salida): Long

    @Update
    suspend fun update(salida: Salida)

    @Delete
    suspend fun delete(salida: Salida)

    @Query("SELECT * FROM salidas WHERE id = :id LIMIT 1")
    suspend fun getSalidaById(id: Int): Salida?

    @Query(
        """
        SELECT * FROM salidas
        WHERE codigoSalida = :codigoSalida AND bodegaId = :bodegaId AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getSalidaByCodigo(codigoSalida: String, bodegaId: String): Salida?

    @Query(
        """
        SELECT codigoSalida FROM salidas
        WHERE bodegaId = :bodegaId AND codigoSalida != '' AND isDeleted = 0
        """
    )
    suspend fun listarCodigosSalida(bodegaId: String): List<String>

    @Query("SELECT * FROM salidas WHERE bodegaId = :bodegaId AND isDeleted = 0 ORDER BY fechaSalida DESC")
    fun getSalidasByBodega(bodegaId: String): Flow<List<Salida>>

    @Query("SELECT * FROM salidas WHERE codigoProducto = :codigoProducto AND bodegaId = :bodegaId AND isDeleted = 0 ORDER BY fechaSalida DESC")
    fun getSalidasByCodigo(codigoProducto: String, bodegaId: String): Flow<List<Salida>>

    @Query("""
        SELECT * FROM salidas 
        WHERE bodegaId = :bodegaId AND isDeleted = 0 
        AND (
            descripcion LIKE '%' || :query || '%' OR codigoProducto LIKE '%' || :query || '%'
            OR destino LIKE '%' || :query || '%' OR area LIKE '%' || :query || '%'
            OR vehiculo LIKE '%' || :query || '%' OR placa LIKE '%' || :query || '%'
            OR responsable LIKE '%' || :query || '%' OR notas LIKE '%' || :query || '%'
            OR campoAgricola LIKE '%' || :query || '%' OR centroCosto LIKE '%' || :query || '%'
        )
        ORDER BY fechaSalida DESC
    """)
    fun buscarSalidas(bodegaId: String, query: String): Flow<List<Salida>>

    @Query("""
        SELECT * FROM salidas WHERE bodegaId = :bodegaId AND isDeleted = 0
        AND descripcion LIKE '%' || :query || '%'
        ORDER BY fechaSalida DESC
    """)
    fun buscarPorDescripcion(bodegaId: String, query: String): Flow<List<Salida>>

    @Query("""
        SELECT * FROM salidas WHERE bodegaId = :bodegaId AND isDeleted = 0
        AND destino LIKE '%' || :query || '%'
        ORDER BY fechaSalida DESC
    """)
    fun buscarPorDestino(bodegaId: String, query: String): Flow<List<Salida>>

    @Query("""
        SELECT * FROM salidas WHERE bodegaId = :bodegaId AND isDeleted = 0
        AND area LIKE '%' || :query || '%'
        ORDER BY fechaSalida DESC
    """)
    fun buscarPorArea(bodegaId: String, query: String): Flow<List<Salida>>

    @Query("""
        SELECT * FROM salidas WHERE bodegaId = :bodegaId AND isDeleted = 0
        AND (vehiculo LIKE '%' || :query || '%' OR placa LIKE '%' || :query || '%')
        ORDER BY fechaSalida DESC
    """)
    fun buscarPorVehiculo(bodegaId: String, query: String): Flow<List<Salida>>

    @Query("SELECT * FROM salidas WHERE cantidad <= stockMinimo AND stockMinimo > 0 AND isDeleted = 0 ORDER BY cantidad ASC")
    fun getSalidasStock(): Flow<List<Salida>>

    @Query("SELECT * FROM salidas WHERE status = :status AND isDeleted = 0 ORDER BY fechaSalida DESC")
    fun getSalidasByStatus(status: String): Flow<List<Salida>>

    @Query("SELECT * FROM salidas WHERE tipoSalida = :tipoSalida AND isDeleted = 0 ORDER BY fechaSalida DESC")
    fun getSalidasByTipo(tipoSalida: String): Flow<List<Salida>>

    @Query("SELECT * FROM salidas WHERE fechaVencimiento != '' AND isDeleted = 0 ORDER BY fechaVencimiento ASC")
    fun getSalidasVencimiento(): Flow<List<Salida>>

    @Query("SELECT COUNT(*) FROM salidas WHERE isDeleted = 0")
    suspend fun contarSalidas(): Int

    @Query("UPDATE salidas SET status = :status WHERE id = :id")
    suspend fun actualizarStatus(id: Int, status: String)

    @Query("UPDATE salidas SET isDeleted = 1, deletionDate = :date WHERE id = :id")
    suspend fun softDelete(id: Int, date: Long)

    @Query("UPDATE salidas SET isDeleted = 0, deletionDate = NULL WHERE id = :id")
    suspend fun restore(id: Int)

    @Query("SELECT * FROM salidas WHERE isDeleted = 1 ORDER BY deletionDate DESC")
    fun getDeletedSalidas(): Flow<List<Salida>>

    @Query("DELETE FROM salidas WHERE id = :id")
    suspend fun deletePermanently(id: Int)

    @Query("DELETE FROM salidas WHERE isDeleted = 1 AND deletionDate <= :threshold")
    suspend fun permanentPurge(threshold: Long)

    @Query("DELETE FROM salidas")
    suspend fun deleteAll()

    @Query("SELECT * FROM salidas WHERE fechaSalida BETWEEN :inicio AND :fin AND isDeleted = 0 ORDER BY fechaSalida DESC")
    fun getSalidasPorFecha(inicio: String, fin: String): Flow<List<Salida>>

    @Query("SELECT * FROM salidas WHERE isDeleted = 0")
    suspend fun obtenerTodasSync(): List<Salida>
}