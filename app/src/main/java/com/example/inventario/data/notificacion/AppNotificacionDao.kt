package com.example.inventario.data.notificacion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(n: AppNotificacion): Long

    @Query("SELECT * FROM app_notificaciones ORDER BY fecha DESC LIMIT 100")
    fun observar(): Flow<List<AppNotificacion>>

    @Query("SELECT * FROM app_notificaciones WHERE bodegaId = :bodegaId OR bodegaId = '' ORDER BY fecha DESC LIMIT 50")
    fun observarPorBodega(bodegaId: String): Flow<List<AppNotificacion>>

    @Query("UPDATE app_notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarLeida(id: Int)

    @Query("SELECT COUNT(*) FROM app_notificaciones WHERE leida = 0")
    fun contarNoLeidas(): Flow<Int>

    @Query("SELECT * FROM app_notificaciones ORDER BY fecha DESC")
    suspend fun obtenerTodosSync(): List<AppNotificacion>
}
