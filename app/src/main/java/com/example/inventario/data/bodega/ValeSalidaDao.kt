package com.example.inventario.data.bodega




import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface ValeSalidaDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )

    suspend fun insertarVale(

        valeSalida: ValeSalida
    )

    @Update

    suspend fun actualizarVale(

        valeSalida: ValeSalida
    )

    @Delete

    suspend fun eliminarVale(

        valeSalida: ValeSalida
    )

    @Query(
        """
        SELECT *
        FROM vale_salida
        ORDER BY idVale DESC
        """
    )

    fun obtenerVales():
            Flow<List<ValeSalida>>

    @Query(
        """
        SELECT *
        FROM vale_salida
        WHERE bodegaId = :bodegaId
        ORDER BY idVale DESC
        """
    )
    fun obtenerValesPorBodega(
        bodegaId: String
    ): Flow<List<ValeSalida>>

    @Query(
        """
        SELECT *
        FROM vale_salida
        WHERE idVale = :id
        LIMIT 1
        """
    )

    suspend fun obtenerValePorId(

        id: Int

    ): ValeSalida?

    @Query(
        """
        DELETE FROM vale_salida
        """
    )

    suspend fun eliminarTodo()

    @Query(
        """
        SELECT COUNT(idVale)
        FROM vale_salida
        """
    )

    suspend fun totalVales():
            Int

    @Query(
        """
        SELECT COUNT(idVale)
        FROM vale_salida
        WHERE bodegaId = :bodegaId
        """
    )
    suspend fun totalValesPorBodega(
        bodegaId: String
    ): Int
}