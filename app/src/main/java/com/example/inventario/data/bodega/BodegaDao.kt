package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BodegaDao {

    // insertar

    @Insert(
        onConflict =
            OnConflictStrategy.Companion.REPLACE
    )
    suspend fun insertar(

        bodega: Bodega

    ): Long

    // actualizar

    @Update
    suspend fun actualizar(

        bodega: Bodega

    )

    // obtener activas

    @Query(

        """
        SELECT *
        FROM bodegas
        WHERE isDeleted = 0
        ORDER BY nombre ASC
        """
    )
    fun obtenerBodegas():

            Flow<List<Bodega>>

    // obtener por id

    @Query(

        """
        SELECT *
        FROM bodegas
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun obtenerBodegaPorId(

        id: String

    ): Bodega?

    // soft delete

    @Query(

        """
        UPDATE bodegas
        SET isDeleted = 1,
        deletionDate = :date
        WHERE id = :id
        """
    )
    suspend fun softDelete(

        id: String,

        date: Long

    )

    // restaurar

    @Query(

        """
        UPDATE bodegas
        SET isDeleted = 0,
        deletionDate = NULL
        WHERE id = :id
        """
    )
    suspend fun restore(

        id: String

    )

    // papelera

    @Query(

        """
        SELECT *
        FROM bodegas
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
        """
    )
    fun getDeletedBodegas():

            Flow<List<Bodega>>

    // purgar automatico

    @Query(

        """
        DELETE FROM bodegas
        WHERE isDeleted = 1
        AND deletionDate <= :threshold
        """
    )
    suspend fun permanentPurge(

        threshold: Long

    )

    // eliminar permanente

    @Query(

        """
        DELETE FROM bodegas
        WHERE id = :id
        """
    )
    suspend fun deletePermanently(

        id: String

    )

    // eliminar normal

    @Delete
    suspend fun eliminar(

        bodega: Bodega

    )

    @Query("SELECT codigoCorto FROM bodegas WHERE isDeleted = 0")
    suspend fun listarCodigosActivos(): List<String>

    @Query("SELECT * FROM bodegas WHERE isDeleted = 0")
    suspend fun listarActivasSync(): List<Bodega>
}