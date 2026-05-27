package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoBodegaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(presupuesto: PresupuestoBodega): Long

    @Query("""
        SELECT * FROM presupuestos_bodega
        WHERE bodegaId = :bodegaId
        ORDER BY anio DESC, indicePeriodo ASC
    """)
    fun observarPorBodega(bodegaId: String): Flow<List<PresupuestoBodega>>

    @Query("""
        SELECT * FROM presupuestos_bodega
        WHERE bodegaId = :bodegaId
        AND tipoPeriodo = :tipo
        AND anio = :anio
        AND indicePeriodo = :indice
        LIMIT 1
    """)
    suspend fun obtener(bodegaId: String, tipo: String, anio: Int, indice: Int): PresupuestoBodega?

    @Query("SELECT * FROM presupuestos_bodega WHERE bodegaId = :bodegaId")
    suspend fun listarSync(bodegaId: String): List<PresupuestoBodega>

    @Query("SELECT * FROM presupuestos_bodega")
    suspend fun obtenerTodosSync(): List<PresupuestoBodega>
}
