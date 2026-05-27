package com.example.inventario.data.bodega

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

 @Insert(
  onConflict = OnConflictStrategy.REPLACE
 )
 suspend fun insertar(
  producto: Producto
 ): Long

 @Update
 suspend fun actualizar(
  producto: Producto
 )

 @Delete
 suspend fun eliminar(
  producto: Producto
 )

 @Query(
  """
        SELECT *
        FROM productos
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY descripcion ASC
        """
 )
 fun obtenerProductos(
  bodegaId: String
 ): Flow<List<Producto>>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE codigo = :codigo
        AND bodegaId = :bodegaId
        AND isDeleted = 0
        LIMIT 1
        """
 )
 suspend fun obtenerProductoPorCodigo(
  codigo: String,
  bodegaId: String
 ): Producto?

 @Query("""
        SELECT * FROM productos
        WHERE bodegaId = :bodegaId AND LOWER(TRIM(descripcion)) = LOWER(TRIM(:descripcion))
        AND isDeleted = 0 LIMIT 1
        """)
 suspend fun obtenerPorDescripcionExacta(bodegaId: String, descripcion: String): Producto?

 @Query("SELECT COUNT(*) FROM productos WHERE categoria = :nombre AND isDeleted = 0")
 suspend fun contarPorCategoria(nombre: String): Int

 @Query(
  """
        SELECT *
        FROM productos
        WHERE codigo = :codigo
        AND isDeleted = 0
        LIMIT 1
        """
 )
 suspend fun buscarProductoPorCodigoGlobal(
  codigo: String
 ): Producto?

 @Query(
  """
        SELECT *
        FROM productos
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        AND (
            codigo LIKE '%' || :query || '%'
            OR descripcion LIKE '%' || :query || '%'
        )
        ORDER BY descripcion ASC
        LIMIT 10
        """
 )
 suspend fun autocompletarProducto(
  bodegaId: String,
  query: String
 ): List<Producto>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE id = :id
        LIMIT 1
        """
 )
 suspend fun obtenerProductoPorId(
  id: Int
 ): Producto?

 @Query(
  """
        SELECT *
        FROM productos
        WHERE bodegaId = :bodegaId
        AND isDeleted = 0
        AND (
            descripcion LIKE '%' || :query || '%'
            OR codigo LIKE '%' || :query || '%'
            OR categoria LIKE '%' || :query || '%'
            OR proveedor LIKE '%' || :query || '%'
            OR ubicacion LIKE '%' || :query || '%'
            OR lote LIKE '%' || :query || '%'
            OR status LIKE '%' || :query || '%'
        )
        ORDER BY descripcion ASC
        """
 )
 fun buscarProductos(
  bodegaId: String,
  query: String
 ): Flow<List<Producto>>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE cantidad <= stockMinimo
        AND cantidad > 0
        AND isDeleted = 0
        ORDER BY cantidad ASC
        """
 )
 fun obtenerProductosBajoStock():
         Flow<List<Producto>>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE cantidad <= 0
        AND isDeleted = 0
        ORDER BY descripcion ASC
        """
 )
 fun obtenerSinStock():
         Flow<List<Producto>>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE fechaVencimiento != ''
        AND isDeleted = 0
        ORDER BY fechaVencimiento ASC
        """
 )
 fun obtenerVencimientos():
         Flow<List<Producto>>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE status = :status
        AND isDeleted = 0
        ORDER BY descripcion ASC
        """
 )
 fun obtenerPorStatus(
  status: String
 ): Flow<List<Producto>>

 @Query(
  """
        SELECT *
        FROM productos
        WHERE presupuesto > 0
        AND isDeleted = 0
        ORDER BY presupuesto DESC
        """
 )
 fun obtenerProductosConPresupuesto():
         Flow<List<Producto>>

 @Query(
  """
        SELECT codigo
        FROM productos
        WHERE prefijoCategoria = :prefijo
        AND bodegaId = :bodegaId
        AND isDeleted = 0
        ORDER BY id DESC
        LIMIT 1
        """
 )
 suspend fun obtenerUltimoCodigoPorPrefijo(
  prefijo: String,
  bodegaId: String
 ): String?

 @Query(
  """
        SELECT codigo, descripcion, categoria FROM productos
        WHERE bodegaId = :bodegaId AND isDeleted = 0
        """
 )
 suspend fun listarCodigoDescripcion(bodegaId: String): List<ProductoCodigoDesc>

 @Query(
  """
        SELECT codigo FROM productos
        WHERE bodegaId = :bodegaId AND isDeleted = 0
        """
 )
 suspend fun listarCodigos(bodegaId: String): List<String>

 @Query(
  """
        UPDATE productos
        SET status = :status
        WHERE id = :id
        """
 )
 suspend fun actualizarStatus(
  id: Int,
  status: String
 )

 @Query(
  """
        UPDATE productos
        SET cantidad = :cantidad,
        ultimoMovimiento = :fecha
        WHERE id = :id
        """
 )
 suspend fun actualizarCantidad(
  id: Int,
  cantidad: Int,
  fecha: Long
 )

 @Query(
  """
        UPDATE productos
        SET isDeleted = 1,
        deletionDate = :date
        WHERE id = :id
        """
 )
 suspend fun softDelete(
  id: Int,
  date: Long
 )

 @Query(
  """
        UPDATE productos
        SET isDeleted = 0,
        deletionDate = NULL
        WHERE id = :id
        """
 )
 suspend fun restore(
  id: Int
 )

 @Query(
  """
        SELECT *
        FROM productos
        WHERE isDeleted = 1
        ORDER BY deletionDate DESC
        """
 )
 fun getDeletedProductos():
         Flow<List<Producto>>

 @Query(
  """
        DELETE FROM productos
        WHERE id = :id
        """
 )
 suspend fun deletePermanently(
  id: Int
 )

 @Query(
  """
        DELETE FROM productos
        WHERE isDeleted = 1
        AND deletionDate <= :threshold
        """
 )
 suspend fun permanentPurge(
  threshold: Long
 )

 @Query(
  """
        DELETE FROM productos
        """
 )
 suspend fun eliminarTodo()

 @Query(
  """
        SELECT *
        FROM productos
        WHERE isDeleted = 0
        """
 )
 suspend fun obtenerTodosSync():
         List<Producto>
}