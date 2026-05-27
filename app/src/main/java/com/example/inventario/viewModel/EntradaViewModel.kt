package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.EntradaDao
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.MovimientoInventarioService
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.inventario.util.CodigoGenerator
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Gestiona la lógica de negocio para las entradas de inventario.
 */
class EntradaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val entradaDao: EntradaDao = db.entradaDao()
    private val firebaseRepo = FirebaseRepository()
    private val repository = InventoryRepository(
        db.bodegaDao(),
        db.productoDao(),
        db.categoriaDao(),
        db.entradaDao(),
        db.salidaDao(),
        db.facturaDao(),
        firebaseRepo
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filtroPeriodo = MutableStateFlow("Dia")
    val filtroPeriodo: StateFlow<String> = _filtroPeriodo

    private val _fechaReferencia = MutableStateFlow(Calendar.getInstance())
    val fechaReferencia: StateFlow<Calendar> = _fechaReferencia

    val periodoTexto: StateFlow<String> = combine(_filtroPeriodo, _fechaReferencia) { periodo, cal ->
        when (periodo) {
            "Dia" -> "Día: " + SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "ES")).format(cal.time)
            "Semana" -> "Semana ${cal.get(Calendar.WEEK_OF_MONTH)} de " +
                SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(cal.time)
            "Mes" -> "Mes de " + SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(cal.time)
            "Año" -> "Año ${cal.get(Calendar.YEAR)}"
            else -> "Todo Historial"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFiltroPeriodo(periodo: String) { _filtroPeriodo.value = periodo }
    fun setFechaReferencia(calendar: Calendar) { _fechaReferencia.value = calendar }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun obtenerEntradasFiltradas(bodegaId: String): Flow<List<Entrada>> {
        return combine(_searchQuery, _filtroPeriodo, _fechaReferencia) { query, periodo, fecha ->
            Triple(query, periodo, fecha)
        }.flatMapLatest { (query, periodo, fecha) ->
            val flow = if (query.isEmpty()) {
                entradaDao.getEntradasByBodega(bodegaId)
            } else {
                entradaDao.buscarEntradas(bodegaId, query)
            }
            flow.map { lista -> filtrarPorPeriodo(lista, periodo, fecha) }
        }
    }

    private fun filtrarPorPeriodo(lista: List<Entrada>, periodo: String, calRef: Calendar): List<Entrada> {
        if (periodo == "Todo") return lista
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        return lista.filter { entrada ->
            try {
                val fechaEntrada = sdf.parse(entrada.fechaIngreso) ?: return@filter false
                val calEntrada = Calendar.getInstance().apply { time = fechaEntrada }
                when (periodo) {
                    "Dia" -> calRef.get(Calendar.YEAR) == calEntrada.get(Calendar.YEAR) &&
                        calRef.get(Calendar.DAY_OF_YEAR) == calEntrada.get(Calendar.DAY_OF_YEAR)
                    "Semana" -> calRef.get(Calendar.YEAR) == calEntrada.get(Calendar.YEAR) &&
                        calRef.get(Calendar.WEEK_OF_YEAR) == calEntrada.get(Calendar.WEEK_OF_YEAR)
                    "Mes" -> calRef.get(Calendar.YEAR) == calEntrada.get(Calendar.YEAR) &&
                        calRef.get(Calendar.MONTH) == calEntrada.get(Calendar.MONTH)
                    "Año" -> calRef.get(Calendar.YEAR) == calEntrada.get(Calendar.YEAR)
                    else -> true
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    fun obtenerEntradas(bodegaId: String): Flow<List<Entrada>> {
        return entradaDao.getEntradasByBodega(bodegaId)
    }

    suspend fun obtenerEntradaPorId(id: Int): Entrada? {
        return entradaDao.getEntradaById(id)
    }

    private val movimientoService = MovimientoInventarioService(db)

    suspend fun registrarEntradaCompleta(
        entrada: Entrada,
        productoNuevo: Producto? = null
    ): MovimientoInventarioService.ResultadoMovimiento {
        val result = movimientoService.registrarEntrada(entrada, productoNuevo)
        if (result is MovimientoInventarioService.ResultadoMovimiento.EntradaOk) {
            val ctx = getApplication<Application>().applicationContext
            NotificationHelper.registrar(
                ctx,
                "Entrada registrada",
                "${result.entrada.descripcion} · ${result.entrada.cantidad} uds · ${result.entrada.codigoProducto}",
                "ENTRADA",
                result.entrada.bodegaId,
                result.entrada.id.toString(),
                result.entrada.codigoProducto
            )
        }
        return result
    }

    fun agregarEntrada(entrada: Entrada, productoNuevo: Producto? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            registrarEntradaCompleta(entrada, productoNuevo)
        }
    }

    fun actualizarEntrada(entrada: Entrada) {
        viewModelScope.launch(Dispatchers.IO) {
            val status = calcularStatus(
                cantidad = entrada.cantidad,
                stockMinimo = entrada.stockMinimo
            )
            val actualizada = entrada.copy(
                status = status,
                presupuesto = entrada.cantidad * entrada.costoEntrada
            )
            repository.updateEntrada(actualizada)
        }
    }

    fun eliminarEntrada(entrada: Entrada) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntrada(entrada)
        }
    }

    fun restaurarEntrada(entrada: Entrada) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreEntrada(entrada.id)
        }
    }

    fun eliminarPermanentemente(entrada: Entrada) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntradaPermanently(
                entrada.id,
                entrada.bodegaId,
                entrada.codigoBodega
            )
        }
    }

    fun buscarEntradas(bodegaId: String, query: String): Flow<List<Entrada>> {
        return entradaDao.buscarEntradas(bodegaId, query)
    }

    fun obtenerEntradasCodigo(codigoProducto: String, bodegaId: String): Flow<List<Entrada>> {
        return entradaDao.getEntradasByCodigo(codigoProducto, bodegaId)
    }

    fun obtenerEntradasStock(): Flow<List<Entrada>> {
        return entradaDao.getEntradasStock()
    }

    fun obtenerEntradasStatus(status: String): Flow<List<Entrada>> {
        return entradaDao.getEntradasByStatus(status)
    }

    fun obtenerEntradasTipo(tipoEntrada: String): Flow<List<Entrada>> {
        return entradaDao.getEntradasByTipo(tipoEntrada)
    }

    fun obtenerEntradasVencimiento(): Flow<List<Entrada>> {
        return entradaDao.getEntradasVencimiento()
    }

    fun obtenerPapelera(): Flow<List<Entrada>> {
        return entradaDao.getDeletedEntradas()
    }

    fun actualizarStatus(id: Int, status: String) {
        viewModelScope.launch {
            entradaDao.actualizarStatus(id, status)
        }
    }

    fun generarCodigoEntrada(bodegaId: String = ""): String = runBlocking(Dispatchers.IO) {
        val codigos = if (bodegaId.isBlank()) emptyList()
        else entradaDao.listarCodigosEntrada(bodegaId)
        CodigoGenerator.generarCodigoTipo("entrada", codigos)
    }

    private fun calcularStatus(cantidad: Int, stockMinimo: Int): String {
        return when {
            cantidad <= 0 -> "SIN_STOCK"
            cantidad <= stockMinimo -> "STOCK_BAJO"
            else -> "ACTIVO"
        }
    }

    fun calcularTotalEntradas(entradas: List<Entrada>): Int {
        return entradas.sumOf { it.cantidad }
    }

    fun calcularTotalCosto(entradas: List<Entrada>): Double {
        return entradas.sumOf { it.presupuesto }
    }

    fun calcularProductosUnicos(entradas: List<Entrada>): Int {
        return entradas.map { it.codigoProducto }.distinct().count()
    }

    fun purgarAntiguos() {
        viewModelScope.launch {
            val threshold = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
            entradaDao.permanentPurge(threshold)
        }
    }

    fun eliminarTodo() {
        viewModelScope.launch {
            entradaDao.deleteAll()
        }
    }
}