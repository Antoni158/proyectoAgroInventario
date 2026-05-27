package com.example.inventario.data.bodega




import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface TrasladoDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )

    suspend fun insertarTraslado(

        traslado: Traslado
    )

    @Update

    suspend fun actualizarTraslado(

        traslado: Traslado
    )

    @Delete

    suspend fun eliminarTraslado(

        traslado: Traslado
    )

    @Query(
        """
        SELECT *
        FROM traslado
        ORDER BY idTraslado DESC
        """
    )

    fun obtenerTraslados():
            Flow<List<Traslado>>

    @Query(
        """
        SELECT *
        FROM traslado
        WHERE bodegaId = :bodegaId
        ORDER BY idTraslado DESC
        """
    )
    fun obtenerTrasladosPorBodega(
        bodegaId: String
    ): Flow<List<Traslado>>

    @Query(
        """
        SELECT *
        FROM traslado
        WHERE idTraslado = :id
        LIMIT 1
        """
    )

    suspend fun obtenerTrasladoPorId(

        id: Int

    ): Traslado?

    @Query(
        """
        SELECT *
        FROM traslado
        WHERE codigoTraslado = :codigo
        LIMIT 1
        """
    )
    suspend fun obtenerTrasladoPorCodigo(codigo: String): Traslado?

    @Query("SELECT * FROM traslado ORDER BY idTraslado DESC")
    suspend fun obtenerTodosSync(): List<Traslado>

    @Query(
        """
        DELETE FROM traslado
        """
    )

    suspend fun eliminarTodo()

    @Query(
        """
        SELECT COUNT(idTraslado)
        FROM traslado
        """
    )

    suspend fun totalTraslados():
            Int
}