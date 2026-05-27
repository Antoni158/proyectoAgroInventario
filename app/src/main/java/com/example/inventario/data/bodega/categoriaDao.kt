package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertar(
        categoria: Categoria
    ): Long

    @Update
    suspend fun actualizar(
        categoria: Categoria
    )

    @Query("""
        SELECT * FROM categorias
        WHERE isDeleted = 0
        ORDER BY nombre ASC
    """)
    fun obtenerCategorias():
            Flow<List<Categoria>>

    @Query("""
        SELECT * FROM categorias
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun obtenerCategoriaPorId(
        id: Int
    ): Categoria?

    @Query("""
        SELECT * FROM categorias
        WHERE nombre = :nombre
        LIMIT 1
    """)
    suspend fun buscarCategoria(
        nombre: String
    ): Categoria?

    @Query("""
        SELECT * FROM categorias
        WHERE prefijo = :prefijo
        LIMIT 1
    """)
    suspend fun buscarPorPrefijo(
        prefijo: String
    ): Categoria?

    @Query("""
        SELECT * FROM categorias
        WHERE prefijo LIKE '%' || :texto || '%'
        AND isDeleted = 0
        ORDER BY nombre ASC
    """)
    suspend fun buscarPorPrefijoLike(
        texto: String
    ): List<Categoria>

    @Query("""
        SELECT * FROM categorias
        WHERE nombre LIKE '%' || :texto || '%'
        AND isDeleted = 0
        ORDER BY nombre ASC
    """)
    suspend fun buscarPorNombreLike(
        texto: String
    ): List<Categoria>

    @Query("""
        UPDATE categorias
        SET correlativoActual =
        correlativoActual + 1
        WHERE id = :categoriaId
    """)
    suspend fun incrementarCorrelativo(
        categoriaId: Int
    )

    @Query("""
        UPDATE categorias
        SET isDeleted = 1,
        deletionDate = :fecha
        WHERE id = :id
    """)
    suspend fun softDelete(
        id: Int,
        fecha: Long
    )

    @Query("""
        UPDATE categorias
        SET isDeleted = 0,
        deletionDate = NULL
        WHERE id = :id
    """)
    suspend fun restore(
        id: Int
    )

    @Query("""
        DELETE FROM categorias
        WHERE id = :id
    """)
    suspend fun deletePermanently(
        id: Int
    )

    @Query("""
        DELETE FROM categorias
        WHERE isDeleted = 1
        AND deletionDate < :threshold
    """)
    suspend fun permanentPurge(
        threshold: Long
    )

    @Query("""
        SELECT * FROM categorias
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
    """)
    fun getDeletedCategorias():
            Flow<List<Categoria>>

    @Query("""
        SELECT * FROM categorias
        WHERE isDeleted = 0
    """)
    suspend fun obtenerTodosSync():
            List<Categoria>

    @Query("""
        SELECT COUNT(*) FROM categorias
        WHERE prefijo = :prefijo
        AND isDeleted = 0
    """)
    suspend fun existePrefijo(
        prefijo: String
    ): Int

    @Query("""
        SELECT * FROM categorias
        WHERE codigoBodega = :codigoBodega
        AND isDeleted = 0
        ORDER BY nombre ASC
    """)
    fun obtenerCategoriasPorBodega(
        codigoBodega: String
    ): Flow<List<Categoria>>

    @Query("""
        SELECT * FROM categorias
        WHERE codigoBodega = :codigoBodega
        AND isDeleted = 0
    """)
    suspend fun obtenerCategoriasSyncPorBodega(
        codigoBodega: String
    ): List<Categoria>

    @Query("""
        UPDATE categorias
        SET ultimaActualizacion = :fecha
        WHERE id = :id
    """)
    suspend fun actualizarFecha(
        id: Int,
        fecha: Long
    )

    @Query("""
        SELECT MAX(correlativoActual)
        FROM categorias
        WHERE prefijo = :prefijo
    """)
    suspend fun obtenerUltimoCorrelativo(
        prefijo: String
    ): Int?

    @Query("""
        DELETE FROM categorias
    """)
    suspend fun limpiarTodo()

    @Query("""
        SELECT * FROM categorias
        ORDER BY fechaCreacion DESC
    """)
    suspend fun obtenerTodasIncluidasEliminadas():
            List<Categoria>

    @Query("""
        SELECT * FROM categorias
        WHERE activa = 1
        AND isDeleted = 0
        ORDER BY nombre ASC
    """)
    fun obtenerActivas():
            Flow<List<Categoria>>

    @Query("""
        UPDATE categorias
        SET activa = :estado
        WHERE id = :id
    """)
    suspend fun cambiarEstado(
        id: Int,
        estado: Boolean
    )

    @Query("""
        SELECT * FROM categorias
        WHERE sincronizado = 0
    """)
    suspend fun obtenerPendientesSync():
            List<Categoria>

    @Query("""
        UPDATE categorias
        SET sincronizado = 1
        WHERE id = :id
    """)
    suspend fun marcarSincronizado(
        id: Int
    )
}