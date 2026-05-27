package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.DetalleVale
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.Vale
import com.example.inventario.data.bodega.ValeConDetalles
import com.example.inventario.data.repos.ValeSalidaIntegracion
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import com.example.inventario.util.CodigoGenerator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ValeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val valeDao = db.ValeDao()
    private val detalleValeDao = db.DetalleValeDao()
    private val valeConDetallesDao = db.ValeConDetallesDao()

    private val _bodegaActiva = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filtroEstado = MutableStateFlow("TODOS")
    val filtroEstado: StateFlow<String> = _filtroEstado.asStateFlow()

    val valesConDetalles: Flow<List<ValeConDetalles>> = combine(
        _bodegaActiva,
        _searchQuery
    ) { bodegaId, query -> bodegaId to query }
        .flatMapLatest { (bodegaId, query) ->
            if (bodegaId.isBlank()) {
                flowOf(emptyList())
            } else if (query.isBlank()) {
                valeConDetallesDao.obtenerValesConDetallesPorBodega(bodegaId)
            } else {
                valeConDetallesDao.buscarValesConDetallesPorBodega(bodegaId, query)
            }
        }

    fun bindBodega(bodegaId: String) {
        _bodegaActiva.value = bodegaId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFiltroEstado(estado: String) {
        _filtroEstado.value = estado
    }

    fun valesFiltradosPorEstado(lista: List<ValeConDetalles>): List<ValeConDetalles> {
        val estado = _filtroEstado.value
        if (estado == "TODOS") return lista
        return lista.filter { it.vale.estado == estado }
    }

    fun crearValeConDetalles(
        vale: Vale,
        detalles: List<DetalleVale>,
        onSuccess: (Int) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val valeId = valeDao.insertarVale(
                    vale.copy(totalProductos = detalles.size)
                ).toInt()
                detalles.forEach { detalle ->
                    detalleValeDao.insertarDetalle(
                        detalle.copy(
                            valeId = valeId,
                            bodegaId = vale.bodegaId.ifBlank { detalle.bodegaId }
                        )
                    )
                }
                withContext(Dispatchers.Main) { onSuccess(valeId) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun procesarSalidaEnVale(salida: Salida): String =
        ValeSalidaIntegracion.procesarSalidaEnVale(db, salida)

    suspend fun obtenerValeCompleto(valeId: Int): ValeConDetalles? =
        valeConDetallesDao.obtenerValeConDetalles(valeId)

    fun eliminarVale(vale: Vale) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                detalleValeDao.eliminarDetallesVale(vale.idVale)
                valeDao.eliminarVale(vale)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun totalVales(bodegaId: String): Int =
        if (bodegaId.isBlank()) valeDao.totalVales()
        else valeDao.totalValesPorBodega(bodegaId)

    fun generarCodigoVale(bodegaId: String): String = runBlocking(Dispatchers.IO) {
        val codigos = valeDao.listarCodigosVale(bodegaId)
        CodigoGenerator.generarCodigoTipo("vale", codigos)
    }
}
