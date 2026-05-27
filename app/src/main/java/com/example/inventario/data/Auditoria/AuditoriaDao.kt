package com.example.inventario.data.Auditoria


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.inventario.data.Auditoria.Auditoria

import kotlinx.coroutines.flow.Flow

@Dao
interface AuditoriaDao {

    // INSERTAR

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertar(

        auditoria: Auditoria

    ): Long

    // OBTENER TODAS

    @Query(

        """
        SELECT *
        FROM auditorias
        ORDER BY fecha DESC
        """
    )
    fun obtenerTodas():

            Flow<List<Auditoria>>

    // OBTENER POR BODEGA

    @Query(

        """
        SELECT *
        FROM auditorias
        WHERE bodegaId = :bodegaId
        ORDER BY fecha DESC
        """
    )
    fun obtenerPorBodega(

        bodegaId: String

    ): Flow<List<Auditoria>>

    // OBTENER FALTANTES

    @Query(

        """
        SELECT *
        FROM auditorias
        WHERE estado = 'FALTANTE'
        ORDER BY fecha DESC
        """
    )
    fun obtenerFaltantes():

            Flow<List<Auditoria>>

    // OBTENER SOBRANTES

    @Query(

        """
        SELECT *
        FROM auditorias
        WHERE estado = 'SOBRANTE'
        ORDER BY fecha DESC
        """
    )
    fun obtenerSobrantes():

            Flow<List<Auditoria>>

    // OBTENER EXACTOS

    @Query(

        """
        SELECT *
        FROM auditorias
        WHERE estado = 'EXACTO'
        ORDER BY fecha DESC
        """
    )
    fun obtenerExactos():

            Flow<List<Auditoria>>

    // OBTENER POR PRODUCTO

    @Query(

        """
        SELECT *
        FROM auditorias
        WHERE productoId = :productoId
        ORDER BY fecha DESC
        """
    )
    fun obtenerPorProducto(

        productoId: Int

    ): Flow<List<Auditoria>>

    // BUSCAR

    @Query(

        """
        SELECT *
        FROM auditorias
        WHERE
        descripcion LIKE '%' || :query || '%'
        OR codigo LIKE '%' || :query || '%'
        OR categoria LIKE '%' || :query || '%'
        OR auditorNombre LIKE '%' || :query || '%'
        ORDER BY fecha DESC
        """
    )
    fun buscar(

        query: String

    ): Flow<List<Auditoria>>

    // CONTAR

    @Query(

        """
        SELECT COUNT(*)
        FROM auditorias
        """
    )
    suspend fun contar():

            Int

    // AJUSTE APLICADO

    @Query(

        """
        UPDATE auditorias
        SET ajusteAplicado = 1
        WHERE id = :id
        """
    )
    suspend fun marcarAjustado(

        id: Int
    )

    // ELIMINAR TODO

    @Query(

        """
        DELETE FROM auditorias
        """
    )
    suspend fun eliminarTodo()

    // ACTUALIZAR

    @Update
    suspend fun actualizar(

        auditoria: Auditoria
    )

    // ELIMINAR

    @Delete
    suspend fun eliminar(

        auditoria: Auditoria
    )

    @Query("SELECT * FROM auditorias ORDER BY fecha DESC")
    suspend fun obtenerTodasSync(): List<Auditoria>
}