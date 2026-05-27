package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.util.CodigoGenerator
import kotlinx.coroutines.Dispatchers
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db =
        appdatabase.getDatabase(application)

    private val dao =
        db.categoriaDao()

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

    private val _categorias =
        MutableStateFlow<List<Categoria>>(
            emptyList()
        )

    val categorias:
            StateFlow<List<Categoria>> =
        _categorias

    init {
        cargarCategoriasLocales()
        escucharCategoriasFirebase()
    }

    private fun escucharCategoriasFirebase() {
        firebaseRepo.categoriasRef().keepSynced(true)
        firebaseRepo.categoriasRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch(Dispatchers.IO) {
                    snapshot.children.mapNotNull { child ->
                        firebaseRepo.parseCategoriaSnapshot(child)
                    }.forEach { cat ->
                        if (cat.id <= 0) return@forEach
                        val local = dao.obtenerCategoriaPorId(cat.id)
                        if (local == null) {
                            dao.insertar(cat)
                        } else if (cat.ultimaActualizacion >= local.ultimaActualizacion) {
                            dao.actualizar(cat.copy(sincronizado = true))
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FIREBASE_CATEGORIAS", error.message)
            }
        })
    }

    private fun cargarCategoriasLocales() {
        viewModelScope.launch {
            dao.obtenerCategorias().collect {
                _categorias.value = it
            }
        }
    }

    fun insertarCategoria(
        categoria: Categoria
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategoria(prepararCategoriaNueva(categoria))
        }
    }

    private suspend fun resolverCodigoBodega(bodegaId: String): String {
        val bodega = db.bodegaDao().obtenerBodegaPorId(bodegaId)
        return bodega?.codigoCorto?.takeIf { it.isNotBlank() } ?: bodegaId
    }

    suspend fun crearCategoriaDesdeProducto(
        nombre: String,
        bodegaId: String
    ): Categoria? = withContext(Dispatchers.IO) {
        val nombreTrim = nombre.trim()
        if (nombreTrim.isBlank()) return@withContext null

        dao.buscarCategoria(nombreTrim)?.let { return@withContext it }

        val productos = productosParaCodigos(
            db.productoDao().listarCodigoDescripcion(bodegaId)
        )
        val prefijo = CodigoGenerator.prefijoDesdeCategoria(
            nombreTrim,
            "",
            productos,
            otrasCategorias()
        )
        val codigoBodegaLegible = resolverCodigoBodega(bodegaId)

        repository.insertCategoria(
            Categoria(
                nombre = nombreTrim,
                prefijo = prefijo,
                codigoBodega = codigoBodegaLegible,
                correlativoActual = 0,
                ultimaActualizacion = System.currentTimeMillis()
            )
        )
    }

    private suspend fun prepararCategoriaNueva(categoria: Categoria): Categoria {
        val productos = productosParaCodigos(
            db.productoDao().listarCodigoDescripcion(categoria.codigoBodega)
        )
        val prefijo = CodigoGenerator.prefijoDesdeCategoria(
            categoria.nombre,
            categoria.prefijo,
            productos,
            otrasCategorias(categoria.id)
        )
        val codigoBodegaLegible = if (categoria.codigoBodega.contains("-") && categoria.codigoBodega.length > 20) {
            resolverCodigoBodega(categoria.codigoBodega)
        } else {
            categoria.codigoBodega
        }
        return categoria.copy(
            prefijo = prefijo,
            codigoBodega = codigoBodegaLegible,
            correlativoActual = categoria.correlativoActual.coerceAtLeast(0),
            ultimaActualizacion = System.currentTimeMillis()
        )
    }

    private suspend fun productosBodega(bodegaId: String) =
        productosParaCodigos(db.productoDao().listarCodigoDescripcion(bodegaId))

    private fun productosParaCodigos(list: List<com.example.inventario.data.bodega.ProductoCodigoDesc>) =
        list.map { it.codigo to it.categoria.ifBlank { it.descripcion } }

    private suspend fun otrasCategorias(excluirId: Int = 0): List<Pair<String, String>> =
        dao.obtenerTodosSync()
            .filter { !it.isDeleted && it.id != excluirId }
            .map { it.prefijo to it.nombre }
            .filter { it.first.isNotBlank() || it.second.isNotBlank() }


    fun actualizarCategoria(categoria: Categoria) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefijoFinal = CodigoGenerator.prefijoDesdeCategoria(
                categoria.nombre,
                categoria.prefijo,
                productosParaCodigos(
                    db.productoDao().listarCodigoDescripcion(categoria.codigoBodega)
                ),
                otrasCategorias(categoria.id)
            )
            val codigoBodegaLegible = if (categoria.codigoBodega.contains("-") && categoria.codigoBodega.length > 20) {
                resolverCodigoBodega(categoria.codigoBodega)
            } else {
                categoria.codigoBodega
            }
            val actualizada = categoria.copy(
                prefijo = prefijoFinal,
                codigoBodega = codigoBodegaLegible
            )
            dao.actualizar(actualizada)
            firebaseRepo.guardarCategoria(actualizada)
        }
    }

    suspend fun contarProductos(categoriaNombre: String): Int =
        db.productoDao().contarPorCategoria(categoriaNombre)

    suspend fun puedeEliminar(categoria: Categoria): Boolean =
        contarProductos(categoria.nombre) == 0

    fun eliminarCategoria(
        categoria: Categoria
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            repository.deleteCategoria(
                categoria
            )
        }
    }

    fun restaurarCategoria(
        categoria: Categoria
    ) {

        viewModelScope.launch {

            dao.restore(
                categoria.id
            )

            firebaseRepo.guardarCategoria(

                categoria.copy(
                    isDeleted = false,
                    deletionDate = null
                )
            )
        }
    }

    fun obtenerPapelera():
            Flow<List<Categoria>> {

        return dao.getDeletedCategorias()
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

    fun eliminarPermanente(
        categoria: Categoria
    ) {

        viewModelScope.launch {

            dao.deletePermanently(
                categoria.id
            )

            firebaseRepo.eliminarCategoria(
                categoria.id
            )
        }
    }

    suspend fun buscarCategoria(
        nombre: String
    ): Categoria? {

        return dao.buscarCategoria(
            nombre
        )
    }

    suspend fun buscarPorPrefijo(
        prefijo: String
    ): Categoria? {

        return dao.buscarPorPrefijo(
            prefijo
        )
    }

    suspend fun obtenerCategoriaPorId(
        id: Int
    ): Categoria? {

        return dao.obtenerCategoriaPorId(
            id
        )
    }

    suspend fun buscarParaAutocomplete(
        query: String
    ): List<Categoria> {

        if (query.isBlank()) {

            return emptyList()
        }

        val texto =
            query
                .trim()
                .uppercase()

        return withContext(
            Dispatchers.IO
        ) {

            val porPrefijo =
                dao.buscarPorPrefijoLike(
                    texto
                )

            val porNombre =
                dao.buscarPorNombreLike(
                    texto
                )

            (porPrefijo + porNombre)
                .distinctBy {
                    it.id
                }
        }
    }

    suspend fun generarCodigoProducto(
        categoriaId: Int,
        bodegaId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val categoria = dao.obtenerCategoriaPorId(categoriaId) ?: return@withContext null
            val codigo = CodigoGenerator.generarCodigoCategoria(
                categoria.nombre,
                categoria.prefijo,
                productosBodega(bodegaId),
                otrasCategorias(categoria.id)
            )
            val prefijo = CodigoGenerator.extraerPrefijoProducto(codigo).orEmpty()
            val correlativo = CodigoGenerator.extraerCorrelativoProducto(codigo) ?: 1
            val actualizada = categoria.copy(
                prefijo = prefijo.ifBlank { categoria.prefijo },
                correlativoActual = correlativo,
                ultimaActualizacion = System.currentTimeMillis(),
                sincronizado = true
            )
            dao.actualizar(actualizada)
            firebaseRepo.guardarCategoria(actualizada)
            codigo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun previewSiguienteCodigo(
        categoriaId: Int,
        bodegaId: String
    ): String = withContext(Dispatchers.IO) {
        val categoria = dao.obtenerCategoriaPorId(categoriaId)
            ?: return@withContext "Seleccione categoría…"
        CodigoGenerator.previewCodigoCategoria(
            categoria.nombre,
            categoria.prefijo,
            productosBodega(bodegaId),
            otrasCategorias(categoria.id)
        )
    }
}