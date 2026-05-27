package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface KardexDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insert(

        kardex: Kardex

    ): Long

    // ACTUALIZAR

    @Update
    suspend fun update(

        kardex: Kardex

    )

    // ELIMINAR NORMAL

    @Delete
    suspend fun delete(

        kardex: Kardex

    )

    // OBTENER POR ID

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getKardexById(

        id: Int

    ): Kardex?

    // OBTENER TODOS

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getAllKardex():

            Flow<List<Kardex>>

    // OBTENER POR BODEGA

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexByBodega(

        bodegaId: String

    ): Flow<List<Kardex>>

    // OBTENER POR CODIGO

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE codigoProducto = :codigoProducto
        AND bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexByCodigo(

        codigoProducto: String,

        bodegaId: String

    ): Flow<List<Kardex>>

    // BUSCAR

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        AND (

            descripcion LIKE '%' || :query || '%'

            OR codigoProducto LIKE '%' || :query || '%'

            OR categoria LIKE '%' || :query || '%'

            OR tipoMovimiento LIKE '%' || :query || '%'

            OR usuario LIKE '%' || :query || '%'

            OR numeroFactura LIKE '%' || :query || '%'

            OR numeroVale LIKE '%' || :query || '%'

            OR lote LIKE '%' || :query || '%'

            OR status LIKE '%' || :query || '%'

        )
        ORDER BY fechaMovimiento DESC
        """
    )
    fun buscarKardex(

        bodegaId: String,

        query: String

    ): Flow<List<Kardex>>

    // POR TIPO

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE tipoMovimiento = :tipo
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexByTipo(

        tipo: String

    ): Flow<List<Kardex>>

    @Query(
        """
        SELECT *
        FROM kardex
        WHERE tipoMovimiento = :tipo
        AND bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexByTipoYBodega(
        bodegaId: String,
        tipo: String
    ): Flow<List<Kardex>>

    // POR STATUS

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE status = :status
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexByStatus(

        status: String

    ): Flow<List<Kardex>>

    @Query(
        """
        SELECT *
        FROM kardex
        WHERE status = :status
        AND bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexByStatusYBodega(
        bodegaId: String,
        status: String
    ): Flow<List<Kardex>>

    @Query(
        """
        SELECT *
        FROM kardex
        WHERE bodegaId = :bodegaId
        AND fechaMovimiento BETWEEN :inicio AND :fin
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexPorFechaYBodega(
        bodegaId: String,
        inicio: String,
        fin: String
    ): Flow<List<Kardex>>

    // FILTRO FECHAS

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE fechaMovimiento
        BETWEEN :inicio
        AND :fin
        AND isDeleted = 0
        ORDER BY fechaMovimiento DESC
        """
    )
    fun getKardexPorFecha(

        inicio: String,

        fin: String

    ): Flow<List<Kardex>>

    // CONTAR

    @Query(

        """
        SELECT COUNT(*)
        FROM kardex
        WHERE isDeleted = 0
        """
    )
    suspend fun contarMovimientos():

            Int

    // SOFT DELETE

    @Query(

        """
        UPDATE kardex
        SET isDeleted = 1,
        deletionDate = :date
        WHERE id = :id
        """
    )
    suspend fun softDelete(

        id: Int,

        date: Long

    )

    // RESTAURAR

    @Query(

        """
        UPDATE kardex
        SET isDeleted = 0,
        deletionDate = NULL
        WHERE id = :id
        """
    )
    suspend fun restore(

        id: Int

    )

    // PAPELERA

    @Query(

        """
        SELECT *
        FROM kardex
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
        """
    )
    fun getDeletedKardex():

            Flow<List<Kardex>>

    // ELIMINAR PERMANENTE

    @Query(

        """
        DELETE FROM kardex
        WHERE id = :id
        """
    )
    suspend fun deletePermanently(

        id: Int

    )

    // PURGAR

    @Query(

        """
        DELETE FROM kardex
        WHERE isDeleted = 1
        AND deletionDate <= :threshold
        """
    )
    suspend fun permanentPurge(

        threshold: Long

    )

    // ELIMINAR TODO

    @Query(

        """
        DELETE FROM kardex
        """
    )
    suspend fun deleteAll()

    @Query("SELECT * FROM kardex")
    suspend fun obtenerTodasSync(): List<Kardex>
}