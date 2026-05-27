package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.ProductoDao
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.util.CodigoGenerator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db =
        appdatabase.getDatabase(application)

    private val dao: ProductoDao =
        db.productoDao()

    private val firebaseRepo =
        FirebaseRepository()

    private val repository =
        InventoryRepository(
            db.bodegaDao(),
            db.productoDao(),
            db.categoriaDao(),
            db.entradaDao(),
            db.salidaDao(),
            db.facturaDao(),
            firebaseRepo
        )

    private var productosListener: ValueEventListener? = null
    private var listeningBodega: String? = null
    private var listeningCodigo: String? = null

    fun iniciarSincronizacion(codigoBodega: String, bodegaId: String) {
        if (listeningBodega == bodegaId) return

        detenerSincronizacion()

        listeningBodega = bodegaId
        listeningCodigo = codigoBodega

        productosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch(Dispatchers.IO) {
                    snapshot.children.mapNotNull { it.getValue(Producto::class.java) }.forEach { prod ->
                        val local = dao.obtenerProductoPorCodigo(prod.codigo, prod.bodegaId)
                        if (local == null) {
                            dao.insertar(prod)
                        } else if ((prod.ultimoMovimiento ?: 0) > (local.ultimoMovimiento ?: 0)) {
                            dao.actualizar(prod)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        firebaseRepo.productosRef(codigoBodega, bodegaId).addValueEventListener(productosListener!!)
    }

    fun detenerSincronizacion() {
        productosListener?.let { listener ->
            val codigo = listeningCodigo
            val id = listeningBodega
            if (codigo != null && id != null) {
                firebaseRepo.productosRef(codigo, id).removeEventListener(listener)
            }
        }
        productosListener = null
        listeningBodega = null
        listeningCodigo = null
    }

    override fun onCleared() {
        super.onCleared()
        detenerSincronizacion()
    }

    suspend fun generarCodigoPorDescripcion(
        descripcion: String,
        bodegaId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (descripcion.isBlank()) return@withContext null
            val existentes = dao.listarCodigoDescripcion(bodegaId)
                .map { it.codigo to it.categoria.ifBlank { it.descripcion } }
            val prefijo = CodigoGenerator.resolverPrefijoDescripcion(descripcion, existentes)
            val siguiente = CodigoGenerator.siguienteCorrelativoPrefijo(
                prefijo,
                existentes.map { it.first }
            )
            CodigoGenerator.construirCodigoAutomatico(prefijo, siguiente)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun previewCodigoPorDescripcion(descripcion: String, bodegaId: String): String =
        withContext(Dispatchers.IO) {
            val existentes = dao.listarCodigoDescripcion(bodegaId)
                .map { it.codigo to it.categoria.ifBlank { it.descripcion } }
            CodigoGenerator.previewCodigoDescripcion(descripcion, existentes)
        }

    /** @deprecated Usar [generarCodigoPorDescripcion] desde entradas */
    suspend fun generarCodigoProducto(categoriaId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                val catDao = db.categoriaDao()
                catDao.incrementarCorrelativo(categoriaId)
                val cat = catDao.obtenerCategoriaPorId(categoriaId) ?: return@withContext null

                firebaseRepo.guardarCategoria(cat)

                val existentes = dao.listarCodigoDescripcion(cat.codigoBodega)
                    .map { it.codigo to it.categoria.ifBlank { it.descripcion } }
                CodigoGenerator.generarCodigoCategoria(cat.nombre, cat.prefijo, existentes)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun obtenerProductos(
        bodegaId: String
    ): Flow<List<Producto>> {

        return dao.obtenerProductos(
            bodegaId
        )
    }

    suspend fun obtenerProductoPorId(
        id: Int
    ): Producto? {

        return dao.obtenerProductoPorId(
            id
        )
    }

    suspend fun obtenerProductoPorCodigo(
        codigo: String,
        bodegaId: String
    ): Producto? {

        return dao.obtenerProductoPorCodigo(
            codigo,
            bodegaId
        )
    }

    fun buscarProductos(
        bodegaId: String,
        query: String
    ): Flow<List<Producto>> {

        return dao.buscarProductos(
            bodegaId,
            query
        )
    }

    suspend fun buscarProductoPorCodigoGlobal(
        codigo: String
    ): Producto? {

        return withContext(
            Dispatchers.IO
        ) {

            dao.buscarProductoPorCodigoGlobal(
                codigo
            )
        }
    }

    suspend fun autocompletarProducto(
        bodegaId: String,
        query: String
    ): List<Producto> {

        return withContext(
            Dispatchers.IO
        ) {

            dao.autocompletarProducto(
                bodegaId,
                query
            )
        }
    }

    fun agregarProducto(
        producto: Producto
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val status =
                calcularStatus(
                    cantidad = producto.cantidad,
                    stockMinimo = producto.stockMinimo
                )

            repository.insertProducto(

                producto.copy(

                    status = status,

                    stockBajo =
                        producto.cantidad <=
                                producto.stockMinimo,

                    presupuesto =
                        producto.cantidad *
                                producto.costo,

                    ultimoMovimiento =
                        System.currentTimeMillis()
                )
            )
        }
    }

    fun actualizarProducto(
        producto: Producto
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val status =
                calcularStatus(
                    cantidad = producto.cantidad,
                    stockMinimo = producto.stockMinimo
                )

            repository.actualizarProducto(

                producto.copy(

                    status = status,

                    stockBajo =
                        producto.cantidad <=
                                producto.stockMinimo,

                    presupuesto =
                        producto.cantidad *
                                producto.costo,

                    ultimoMovimiento =
                        System.currentTimeMillis()
                )
            )
        }
    }

    fun eliminarProducto(
        producto: Producto
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            repository.softDeleteProducto(
                producto.id
            )
        }
    }

    fun restaurarProducto(
        id: Int
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            repository.restoreProducto(
                id
            )
        }
    }

    fun eliminarPermanentemente(
        id: Int
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            repository.deleteProductoPermanently(
                id
            )
        }
    }

    fun obtenerPapelera():
            Flow<List<Producto>> {

        return dao.getDeletedProductos()
    }

    fun obtenerProductosBajoStock():
            Flow<List<Producto>> {

        return dao.obtenerProductosBajoStock()
    }

    fun obtenerSinStock():
            Flow<List<Producto>> {

        return dao.obtenerSinStock()
    }

    fun obtenerVencimientos():
            Flow<List<Producto>> {

        return dao.obtenerVencimientos()
    }

    fun obtenerPorStatus(
        status: String
    ): Flow<List<Producto>> {

        return dao.obtenerPorStatus(
            status
        )
    }

    fun sumarCantidadProducto(
        codigo: String,
        bodegaId: String,
        cantidadEntrada: Int
    ) {

        viewModelScope.launch {

            val productoActual =
                dao.obtenerProductoPorCodigo(
                    codigo,
                    bodegaId
                )

            productoActual?.let {

                val nuevaCantidad =
                    it.cantidad +
                            cantidadEntrada

                val nuevoStatus =
                    calcularStatus(
                        nuevaCantidad,
                        it.stockMinimo
                    )

                dao.actualizar(

                    it.copy(

                        cantidad =
                            nuevaCantidad,

                        status =
                            nuevoStatus,

                        stockBajo =
                            nuevaCantidad <=
                                    it.stockMinimo,

                        presupuesto =
                            nuevaCantidad *
                                    it.costo,

                        ultimoMovimiento =
                            System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun restarCantidadProducto(
        codigo: String,
        bodegaId: String,
        cantidadSalida: Int
    ) {

        viewModelScope.launch {

            val productoActual =
                dao.obtenerProductoPorCodigo(
                    codigo,
                    bodegaId
                )

            productoActual?.let {

                val nuevaCantidad =
                    (it.cantidad -
                            cantidadSalida)
                        .coerceAtLeast(0)

                val nuevoStatus =
                    calcularStatus(
                        nuevaCantidad,
                        it.stockMinimo
                    )

                dao.actualizar(

                    it.copy(

                        cantidad =
                            nuevaCantidad,

                        status =
                            nuevoStatus,

                        stockBajo =
                            nuevaCantidad <=
                                    it.stockMinimo,

                        presupuesto =
                            nuevaCantidad *
                                    it.costo,

                        ultimoMovimiento =
                            System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun calcularStatus(
        cantidad: Int,
        stockMinimo: Int
    ): String {

        return when {

            cantidad <= 0 -> {

                "SIN_STOCK"
            }

            cantidad <= stockMinimo -> {

                "STOCK_BAJO"
            }

            else -> {

                "ACTIVO"
            }
        }
    }

    fun purgarAntiguos() {

        viewModelScope.launch {

            val threshold =
                System.currentTimeMillis() -
                        (90L * 24 * 60 * 60 * 1000)

            dao.permanentPurge(
                threshold
            )
        }
    }

    fun eliminarTodo() {

        viewModelScope.launch {

            dao.eliminarTodo()
        }
    }
}