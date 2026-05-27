package com.example.inventario.data.repos

import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.BodegaCodigoUtil
import com.example.inventario.data.bodega.BodegaDao
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.EntradaDao
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.FacturaDao
import com.example.inventario.data.bodega.ProductoDao
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.SalidaDao
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.bodega.CategoriaDao
import com.example.inventario.data.bodega.Producto
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlin.collections.forEach

class InventoryRepository(
    private val bodegaDao: BodegaDao,
    private val productoDao: ProductoDao,
    private val categoriaDao: CategoriaDao,
    private val entradaDao: EntradaDao,
    private val salidaDao: SalidaDao,
    private val facturaDao: FacturaDao,
    private val firebaseRepository: FirebaseRepository
) {

    // =========================
    // BODEGAS
    // =========================

    val allBodegas:

            Flow<List<Bodega>> =

        bodegaDao
            .obtenerBodegas()

    suspend fun refreshBodegas() {
        val remoteBodegas = firebaseRepository.obtenerBodegas()
        Log.i("ROOM_BODEGAS", "Persistiendo ${remoteBodegas.size} bodega(s) en Room")
        remoteBodegas.forEach { bodega ->
            val local = bodegaDao.obtenerBodegaPorId(bodega.id)
            if (local?.isDeleted == true) return@forEach
            val codigos = bodegaDao.listarCodigosActivos()
            var final = if (BodegaCodigoUtil.necesitaCodigoLegible(bodega.codigoCorto)) {
                val codigo = BodegaCodigoUtil.generarCodigoCompleto(bodega.nombre, codigos)
                bodega.copy(
                    codigoCorto = codigo,
                    id = bodega.id.ifBlank { codigo }
                )
            } else {
                bodega
            }
            if (local != null) {
                if (final.nombre.isBlank() || BodegaCodigoUtil.esNombreIgualAlCodigo(final.nombre, final.codigoCorto)) {
                    final = final.copy(nombre = local.nombre)
                }
                if (final.descripcion.isBlank() && local.descripcion.isNotBlank()) {
                    final = final.copy(descripcion = local.descripcion)
                }
            }
            bodegaDao.insertar(final)
        }
    }

    suspend fun insertBodega(
        bodega: Bodega
    ) {
        bodegaDao.insertar(bodega)
        firebaseRepository.guardarBodega(bodega)
    }

    suspend fun deleteBodega(bodega: Bodega) {
        val now = System.currentTimeMillis()
        bodegaDao.softDelete(bodega.id, now)
        firebaseRepository.guardarBodega(bodega.copy(isDeleted = true, deletionDate = now))
    }

    suspend fun restoreBodega(bodega: Bodega) {
        bodegaDao.restore(bodega.id)
        firebaseRepository.guardarBodega(bodega.copy(isDeleted = false, deletionDate = null))
    }

    suspend fun updateBodega(bodega: Bodega) {
        bodegaDao.actualizar(bodega)
        firebaseRepository.guardarBodega(bodega)
    }

    suspend fun deleteBodegaPermanently(id: String, codigo: String) {
        bodegaDao.deletePermanently(id)
        firebaseRepository.eliminarBodega(codigo, id)
    }

    // =========================
    // PRODUCTOS
    // =========================

    fun getProductos(
        bodegaId: String
    ): Flow<List<Producto>> =
        productoDao
            .obtenerProductos(
                bodegaId
            )

    suspend fun insertProducto(
        producto: Producto
    ) {
        val existente =
            productoDao
                .obtenerProductoPorCodigo(
                    producto.codigo,
                    producto.bodegaId
                )

        if (
            existente == null
        ) {
            val idGenerado =
                productoDao
                    .insertar(
                        producto
                    )

            val productoConId =
                producto.copy(
                    id =
                        idGenerado.toInt()
                )

            firebaseRepository
                .guardarProducto(
                    productoConId,
                    producto.codigoBodega
                )

        } else {
            val actualizado =
                existente.copy(
                    descripcion =
                        producto.descripcion,
                    categoria =
                        producto.categoria,
                    cantidad =
                        producto.cantidad,
                    presupuesto =
                        producto.presupuesto,
                    proveedor =
                        producto.proveedor,
                    unidad =
                        producto.unidad,
                    ubicacion =
                        producto.ubicacion,
                    costo =
                        producto.costo,
                    stockMinimo =
                        producto.stockMinimo,
                    notas =
                        producto.notas
                )

            productoDao
                .actualizar(
                    actualizado
                )

            firebaseRepository
                .guardarProducto(
                    actualizado,
                    actualizado.codigoBodega
                )
        }
    }

    suspend fun actualizarProducto(
        producto: Producto
    ) {
        productoDao
            .actualizar(
                producto
            )

        firebaseRepository
            .guardarProducto(
                producto,
                producto.codigoBodega
            )
    }

    suspend fun buscarProductoPorCodigo(
        codigo: String,
        bodegaId: String
    ): Producto? {
        return productoDao
            .obtenerProductoPorCodigo(
                codigo,
                bodegaId
            )
    }

    suspend fun softDeleteProducto(

        id: Int

    ) {

        val producto =

            productoDao
                .obtenerProductoPorId(
                    id
                )

        producto?.let {

            val now =
                System.currentTimeMillis()

            productoDao
                .softDelete(

                    id,

                    now
                )

            firebaseRepository
                .guardarProducto(

                    it.copy(

                        isDeleted = true,

                        deletionDate = now
                    ),
                    it.codigoBodega
                )
        }
    }

    suspend fun restoreProducto(

        id: Int

    ) {

        val producto =

            productoDao
                .obtenerProductoPorId(
                    id
                )

        producto?.let {

            productoDao
                .restore(id)

            firebaseRepository
                .guardarProducto(

                    it.copy(

                        isDeleted = false,

                        deletionDate = null
                    ),
                    it.codigoBodega
                )
        }
    }

    fun getDeletedProductos() =

        productoDao
            .getDeletedProductos()

    suspend fun deleteProductoPermanently(

        id: Int

    ) {

        val producto =

            productoDao
                .obtenerProductoPorId(
                    id
                )

        producto?.let {

            productoDao
                .deletePermanently(id)

            firebaseRepository
                .eliminarProducto(

                    it.codigoBodega,

                    it.bodegaId,

                    it.codigo
                )
        }
    }

    suspend fun permanentPurgeProductos(

        threshold: Long

    ) {

        productoDao
            .permanentPurge(
                threshold
            )
    }

    suspend fun sincronizarProductos(

        bodegaId: String,
        codigoBodega: String

    ) {

        val productosFirebase =

            firebaseRepository
                .obtenerProductos(
                    codigoBodega,
                    bodegaId
                )

        productosFirebase.forEach { productoNube ->

            val local =

                productoDao
                    .obtenerProductoPorCodigo(

                        productoNube.codigo,

                        productoNube.bodegaId
                    )

            if (

                local == null

            ) {

                productoDao
                    .insertar(
                        productoNube
                    )

            } else {

                val productoFusionado =

                    local.copy(

                        cantidad =
                            productoNube.cantidad,

                        descripcion =
                            productoNube.descripcion,

                        categoria =
                            productoNube.categoria,

                        unidad =
                            productoNube.unidad,

                        ubicacion =
                            productoNube.ubicacion,

                        proveedor =
                            productoNube.proveedor,

                        costo =
                            productoNube.costo,

                        fechaIngreso =
                            productoNube.fechaIngreso,

                        notas =
                            productoNube.notas,

                        isDeleted =
                            productoNube.isDeleted,

                        deletionDate =
                            productoNube.deletionDate
                    )

                productoDao
                    .actualizar(
                        productoFusionado
                    )
            }
        }
    }

    // =========================
    // CATEGORIAS
    // =========================

    val allCategorias: Flow<List<Categoria>> =
        categoriaDao
            .obtenerCategorias()

    suspend fun insertCategoria(
        categoria: Categoria
    ): Categoria {
        val now = System.currentTimeMillis()
        val toInsert = categoria.copy(ultimaActualizacion = now)
        val rowId = categoriaDao.insertar(toInsert).toInt()
        val guardada = toInsert.copy(
            id = if (toInsert.id == 0) rowId else toInsert.id,
            sincronizado = true
        )
        firebaseRepository.guardarCategoria(guardada)
        return guardada
    }

    suspend fun sincronizarCategorias() {

        val categoriasFirebase =

            firebaseRepository
                .obtenerCategorias()

        categoriasFirebase.forEach { categoria ->

            categoriaDao
                .insertar(
                    categoria
                )
        }
    }

    suspend fun deleteCategoria(categoria: Categoria) {
        val now = System.currentTimeMillis()
        categoriaDao.softDelete(categoria.id, now)
        firebaseRepository.guardarCategoria(categoria.copy(isDeleted = true, deletionDate = now))
    }

    // =========================
    // ENTRADAS
    // =========================

    fun getEntradas(bodegaId: String): Flow<List<Entrada>> =
        entradaDao.getEntradasByBodega(bodegaId)

    suspend fun insertEntrada(entrada: Entrada) {
        val idGenerado = entradaDao.insert(entrada)
        firebaseRepository.guardarEntrada(entrada.copy(id = idGenerado.toInt()))
    }

    suspend fun updateEntrada(entrada: Entrada) {
        entradaDao.update(entrada)
        firebaseRepository.guardarEntrada(entrada)
    }

    suspend fun deleteEntrada(entrada: Entrada) {
        val now = System.currentTimeMillis()
        entradaDao.softDelete(entrada.id, now)
        firebaseRepository.guardarEntrada(entrada.copy(isDeleted = true, deletionDate = now))
    }


    suspend fun sincronizarEntradas(

        bodegaId: String,
        codigoBodega: String

    ) {

        val remote =
            firebaseRepository
                .obtenerEntradas(
                    codigoBodega,
                    bodegaId
                )

        remote.forEach { entrada ->
            val existente = entradaDao.getEntradaById(entrada.id)
            if (existente == null) entradaDao.insert(entrada) else entradaDao.update(entrada)
        }
    }

    // =========================
    // SALIDAS
    // =========================

    fun getSalidas(bodegaId: String): Flow<List<Salida>> =
        salidaDao.getSalidasByBodega(bodegaId)

    suspend fun insertSalida(salida: Salida) {
        val idGenerado = salidaDao.insert(salida)
        firebaseRepository.guardarSalida(salida.copy(id = idGenerado.toInt()))
    }

    suspend fun updateSalida(salida: Salida) {
        salidaDao.update(salida)
        firebaseRepository.guardarSalida(salida)
    }

    suspend fun deleteSalida(salida: Salida) {
        val now = System.currentTimeMillis()
        salidaDao.softDelete(salida.id, now)
        firebaseRepository.guardarSalida(salida.copy(isDeleted = true, deletionDate = now))
    }

    suspend fun sincronizarSalidas(bodegaId: String, codigoBodega: String) {
        firebaseRepository.obtenerSalidas(codigoBodega, bodegaId).forEach { salida ->
            val existente = when {
                salida.id > 0 -> salidaDao.getSalidaById(salida.id)
                salida.codigoSalida.isNotBlank() -> salidaDao.getSalidaByCodigo(salida.codigoSalida, bodegaId)
                else -> null
            }
            if (existente == null) salidaDao.insert(salida) else salidaDao.update(salida.copy(id = existente.id))
        }
    }

    // =========================
    // FACTURAS
    // =========================

    fun getFacturas(bodegaId: String): Flow<List<Factura>> =
        facturaDao.getFacturasByBodega(bodegaId)

    suspend fun insertFactura(factura: Factura) {
        val idGenerado = facturaDao.insert(factura)
        firebaseRepository.guardarFactura(factura.copy(id = idGenerado.toInt()))
    }

    suspend fun updateFactura(factura: Factura) {
        facturaDao.update(factura)
        firebaseRepository.guardarFactura(factura)
    }

    suspend fun deleteFactura(factura: Factura) {
        val now = System.currentTimeMillis()
        facturaDao.softDelete(factura.id, now)
        firebaseRepository.guardarFactura(factura.copy(isDeleted = true, deletionDate = now))
    }

    suspend fun restoreEntrada(id: Int) {
        entradaDao.restore(id)
        val entrada = entradaDao.getEntradaById(id)
        entrada?.let {
            firebaseRepository.guardarEntrada(it.copy(isDeleted = false, deletionDate = null))
        }
    }

    suspend fun deleteEntradaPermanently(id: Int, bodegaId: String, codigoBodega: String) {
        entradaDao.deletePermanently(id)
        firebaseRepository.eliminarEntrada(codigoBodega, bodegaId, id.toString())
    }

    suspend fun restoreSalida(id: Int) {
        salidaDao.restore(id)
        val salida = salidaDao.getSalidaById(id)
        salida?.let {
            firebaseRepository.guardarSalida(it.copy(isDeleted = false, deletionDate = null))
        }
    }

    suspend fun deleteSalidaPermanently(id: Int, bodegaId: String, codigoBodega: String) {
        salidaDao.deletePermanently(id)
        firebaseRepository.eliminarSalida(codigoBodega, bodegaId, id.toString())
    }

    suspend fun restoreFactura(id: Int) {
        facturaDao.restore(id)
        val factura = facturaDao.getFacturaById(id)
        factura?.let {
            firebaseRepository.guardarFactura(it.copy(isDeleted = false, deletionDate = null))
        }
    }

    suspend fun deleteFacturaPermanently(id: Int, bodegaId: String, codigoBodega: String) {
        facturaDao.deletePermanently(id)
        firebaseRepository.eliminarFactura(codigoBodega, bodegaId, id.toString())
    }
}