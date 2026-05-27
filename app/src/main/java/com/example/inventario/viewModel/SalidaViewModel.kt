package com.example.inventario.viewModel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.bodega.Salida

import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.service.NotificationHelper
import com.example.inventario.data.repos.MovimientoInventarioService
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.ui.dashboard.StockPedidoUtil

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

enum class TipoBusquedaSalida(val etiqueta: String) {
    TODO("Todo"),
    DESCRIPCION("Descripción"),
    DESTINO("Destino"),
    AREA("Área"),
    VEHICULO("Vehículo")
}

class SalidaViewModel(

    application: Application

) : AndroidViewModel(application) {

    private val database =

        appdatabase
            .getDatabase(
                application
            )

    private val repository =

        InventoryRepository(

            bodegaDao =
                database.bodegaDao(),

            productoDao =
                database.productoDao(),

            categoriaDao =
                database.categoriaDao(),

            entradaDao =
                database.entradaDao(),

            salidaDao =
                database.salidaDao(),

            facturaDao =
                database.facturaDao(),

            firebaseRepository =
                FirebaseRepository()
        )

    private val salidaDao =

        database.salidaDao()

    // BUSQUEDA

    private val _searchQuery =

        MutableStateFlow("")

    val searchQuery:

            StateFlow<String> =

        _searchQuery

    private val _tipoBusqueda = MutableStateFlow(TipoBusquedaSalida.TODO)

    val tipoBusqueda: StateFlow<TipoBusquedaSalida> = _tipoBusqueda

    // FILTRO

    private val _filtroPeriodo =

        MutableStateFlow("Dia")

    val filtroPeriodo:

            StateFlow<String> =

        _filtroPeriodo

    // FECHA

    private val _fechaReferencia =

        MutableStateFlow(

            Calendar.getInstance()
        )

    val fechaReferencia:

            StateFlow<Calendar> =

        _fechaReferencia

    // TEXTO PERIODO

    val periodoTexto:

            StateFlow<String> =

        combine(

            _filtroPeriodo,

            _fechaReferencia

        ) {

                periodo,
                cal ->

            when (periodo) {

                "Dia" ->

                    "Día: " +

                            SimpleDateFormat(

                                "dd 'de' MMMM yyyy",

                                Locale(
                                    "es",
                                    "ES"
                                )

                            ).format(
                                cal.time
                            )

                "Semana" ->

                    "Semana " +

                            cal.get(

                                Calendar.WEEK_OF_MONTH
                            ) +

                            " de " +

                            SimpleDateFormat(

                                "MMMM yyyy",

                                Locale(
                                    "es",
                                    "ES"
                                )

                            ).format(
                                cal.time
                            )

                "Mes" ->

                    "Mes de " +

                            SimpleDateFormat(

                                "MMMM yyyy",

                                Locale(
                                    "es",
                                    "ES"
                                )

                            ).format(
                                cal.time
                            )

                "Año" ->

                    "Año " +

                            cal.get(
                                Calendar.YEAR
                            )

                else ->

                    "Todo Historial"
            }

        }.stateIn(

            viewModelScope,

            SharingStarted
                .WhileSubscribed(5000),

            "Cargando..."
        )

    // SETTERS

    fun setSearchQuery(

        query: String

    ) {

        _searchQuery.value =
            query
    }

    fun setTipoBusqueda(tipo: TipoBusquedaSalida) {
        _tipoBusqueda.value = tipo
    }

    fun setFiltroPeriodo(

        periodo: String

    ) {

        _filtroPeriodo.value =
            periodo
    }

    fun setFechaReferencia(

        calendar: Calendar

    ) {

        _fechaReferencia.value =
            calendar
    }

    // OBTENER SALIDAS

    fun obtenerSalidas(

        bodegaId: String

    ) =

        salidaDao
            .getSalidasByBodega(
                bodegaId
            )

    // FILTRADAS

    @OptIn(
        ExperimentalCoroutinesApi::class
    )

    fun obtenerSalidasFiltradas(

        bodegaId: String

    ): Flow<List<Salida>> {

        return combine(

            _searchQuery,

            _tipoBusqueda,

            _filtroPeriodo,

            _fechaReferencia

        ) {

                query,
                tipo,
                periodo,
                fecha ->

            Triple(

                query to tipo,

                periodo,

                fecha
            )

        }.flatMapLatest {

                (
                    busqueda,
                    periodo,
                    fecha
                ) ->

            val (query, tipo) = busqueda

            val flow =

                if (

                    query.isEmpty()

                ) {

                    salidaDao
                        .getSalidasByBodega(
                            bodegaId
                        )

                } else {

                    when (tipo) {
                        TipoBusquedaSalida.DESCRIPCION ->
                            salidaDao.buscarPorDescripcion(bodegaId, query)
                        TipoBusquedaSalida.DESTINO ->
                            salidaDao.buscarPorDestino(bodegaId, query)
                        TipoBusquedaSalida.AREA ->
                            salidaDao.buscarPorArea(bodegaId, query)
                        TipoBusquedaSalida.VEHICULO ->
                            salidaDao.buscarPorVehiculo(bodegaId, query)
                        TipoBusquedaSalida.TODO ->
                            salidaDao.buscarSalidas(bodegaId, query)
                    }
                }

            flow.map {

                    lista ->

                filtrarPorPeriodo(

                    lista,

                    periodo,

                    fecha
                )
            }
        }
    }

    // FILTRO FECHAS

    private fun filtrarPorPeriodo(

        lista: List<Salida>,

        periodo: String,

        calRef: Calendar

    ): List<Salida> {

        if (

            periodo == "Todo"

        ) {

            return lista
        }

        val sdf =

            SimpleDateFormat(

                "d/M/yyyy",

                Locale.getDefault()
            )

        return lista.filter {

                salida ->

            try {

                val fechaSalida =

                    sdf.parse(

                        salida.fechaSalida
                    ) ?: return@filter false

                val calSalida =

                    Calendar
                        .getInstance()
                        .apply {

                            time =
                                fechaSalida
                        }

                when (periodo) {

                    "Dia" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calSalida.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.DAY_OF_YEAR
                                ) ==

                                calSalida.get(
                                    Calendar.DAY_OF_YEAR
                                )

                    "Semana" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calSalida.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.WEEK_OF_YEAR
                                ) ==

                                calSalida.get(
                                    Calendar.WEEK_OF_YEAR
                                )

                    "Mes" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calSalida.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.MONTH
                                ) ==

                                calSalida.get(
                                    Calendar.MONTH
                                )

                    "Año" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calSalida.get(
                                    Calendar.YEAR
                                )

                    else -> true
                }

            } catch (

                e: Exception

            ) {

                false
            }
        }
    }

    // OBTENER POR BODEGA

    fun obtenerSalidasPorBodega(

        bodegaId: String

    ): Flow<List<Salida>> {

        return salidaDao
            .getSalidasByBodega(
                bodegaId
            )
    }

    // BUSCAR

    fun buscarSalidas(

        bodegaId: String,

        query: String

    ): Flow<List<Salida>> {

        return salidaDao
            .buscarSalidas(

                bodegaId,

                query
            )
    }

    // OBTENER POR ID

    suspend fun obtenerSalidaPorId(

        id: Int

    ): Salida? {

        return salidaDao
            .getSalidaById(
                id
            )
    }

    // AGREGAR

    fun agregarSalida(salida: Salida) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                registrarSalidaCompleta(salida)
            } catch (_: Exception) { }
        }
    }

    private val movimientoService = MovimientoInventarioService(database)

    /** Registra salida con stock, kardex, Firebase y notificación. */
    suspend fun registrarSalidaCompleta(salida: Salida, notificar: Boolean = true): Salida {
        val guardada = when (val r = movimientoService.registrarSalida(salida)) {
            is MovimientoInventarioService.ResultadoMovimiento.SalidaOk -> r.salida
            is MovimientoInventarioService.ResultadoMovimiento.Error ->
                throw IllegalStateException(r.mensaje)
            else -> throw IllegalStateException("Resultado inesperado")
        }
        if (notificar) {
            notificarSalidaRegistrada(guardada)
        }
        return guardada
    }

    /** Registra varias líneas de un mismo vale: descuenta stock una vez por línea y notifica al final. */
    suspend fun registrarSalidasEnVale(salidas: List<Salida>): List<Salida> {
        if (salidas.isEmpty()) return emptyList()
        val guardadas = salidas.map { registrarSalidaCompleta(it, notificar = false) }
        val ctx = getApplication<Application>().applicationContext
        val totalUnidades = guardadas.sumOf { it.cantidad }
        val vale = guardadas.first().numeroVale
        NotificationHelper.registrar(
            ctx,
            "Salida registrada",
            "${guardadas.size} producto(s) · $totalUnidades uds · Vale $vale",
            "SALIDA",
            guardadas.first().bodegaId,
            guardadas.first().id.toString(),
            guardadas.joinToString { it.codigoProducto }
        )
        guardadas.forEach { notificarStockProducto(it) }
        return guardadas
    }

    private suspend fun notificarSalidaRegistrada(guardada: Salida) {
        val ctx = getApplication<Application>().applicationContext
        NotificationHelper.registrar(
            ctx,
            "Salida registrada",
            "${guardada.descripcion} · ${guardada.cantidad} uds → ${guardada.destino}",
            "SALIDA",
            guardada.bodegaId,
            guardada.id.toString(),
            guardada.codigoProducto
        )
        notificarStockProducto(guardada)
    }

    private suspend fun notificarStockProducto(guardada: Salida) {
        val ctx = getApplication<Application>().applicationContext
        val prod = database.productoDao()
            .obtenerProductoPorCodigo(guardada.codigoProducto, guardada.bodegaId)
        if (prod != null && prod.cantidad <= 0) {
            NotificationHelper.registrar(
                ctx,
                "Sin existencias",
                "${prod.descripcion} (${prod.codigo}) agotado",
                "CRITICO",
                guardada.bodegaId,
                prod.id.toString(),
                prod.codigo
            )
        } else if (prod != null && StockPedidoUtil.esStockBajo(prod)) {
            NotificationHelper.registrar(
                ctx,
                "Stock bajo",
                "${prod.descripcion} · quedan ${prod.cantidad} uds (mín. ${prod.stockMinimo})",
                "STOCK_BAJO",
                guardada.bodegaId,
                prod.id.toString(),
                prod.codigo
            )
        } else if (prod != null && prod.cantidad <= 5 && prod.stockMinimo <= 0) {
            NotificationHelper.registrar(
                ctx,
                "Stock bajo",
                "${prod.descripcion} · quedan ${prod.cantidad} uds",
                "STOCK_BAJO",
                guardada.bodegaId,
                prod.id.toString(),
                prod.codigo
            )
        }
    }

    // ACTUALIZAR

    fun actualizarSalida(

        salida: Salida

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            val total =

                salida.cantidad *
                        salida.costoUnitario

            val status =

                calcularStatus(

                    salida.cantidad,

                    salida.stockMinimo
                )

            repository.updateSalida(

                salida.copy(

                    total =
                        total,

                    status =
                        status
                )
            )
        }
    }

    // ELIMINAR

    fun eliminarSalida(
        salida: Salida
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            repository.deleteSalida(salida)
            
            // Sincronizar eliminación física o soft-delete en Firebase usando bRef jerárquico
            FirebaseRepository().eliminarSalida(
                salida.codigoBodega,
                salida.bodegaId,
                salida.id.toString()
            )
        }
    }

    // PAPELERA

    fun obtenerPapelera() =

        salidaDao
            .getDeletedSalidas()

    // RESTAURAR

    fun restaurarSalida(

        salida: Salida

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            repository.restoreSalida(
                salida.id
            )
        }
    }

    // ELIMINAR PERMANENTE

    fun eliminarPermanente(

        salida: Salida

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            repository.deleteSalidaPermanently(
                salida.id,
                salida.bodegaId,
                salida.codigoBodega
            )
        }
    }

    // STOCK BAJO

    fun obtenerStockBajo():

            Flow<List<Salida>> {

        return salidaDao
            .getSalidasStock()
    }

    // STATUS

    fun obtenerPorStatus(

        status: String

    ): Flow<List<Salida>> {

        return salidaDao
            .getSalidasByStatus(
                status
            )
    }

    // TIPO

    fun obtenerPorTipo(

        tipo: String

    ): Flow<List<Salida>> {

        return salidaDao
            .getSalidasByTipo(
                tipo
            )
    }

    // VENCIMIENTOS

    fun obtenerVencimientos():

            Flow<List<Salida>> {

        return salidaDao
            .getSalidasVencimiento()
    }

    // KPI TOTAL

    fun calcularTotalSalidas(

        lista: List<Salida>

    ): Int {

        return lista.sumOf {

            it.cantidad
        }
    }

    // KPI COSTO

    fun calcularCostoTotal(

        lista: List<Salida>

    ): Double {

        return lista.sumOf {

            it.total
        }
    }

    // KPI PRODUCTOS

    fun calcularProductosUnicos(

        lista: List<Salida>

    ): Int {

        return lista
            .map {

                it.codigoProducto
            }
            .distinct()
            .count()
    }

    // CODIGO

    fun generarCodigoSalida(bodegaId: String = ""): String = runBlocking(Dispatchers.IO) {
        val codigos = if (bodegaId.isBlank()) emptyList()
        else salidaDao.listarCodigosSalida(bodegaId)
        CodigoGenerator.generarCodigoTipo("salida", codigos)
    }

    suspend fun generarCodigosSalida(bodegaId: String, cantidad: Int): List<String> {
        val acumulado = salidaDao.listarCodigosSalida(bodegaId).toMutableList()
        return List(cantidad) {
            CodigoGenerator.generarSiguiente("salida", acumulado).also(acumulado::add)
        }
    }

    // STATUS

    private fun calcularStatus(

        cantidad: Int,

        stockMinimo: Int

    ): String {

        return when {

            cantidad <= 0 ->

                "SIN_STOCK"

            cantidad <= stockMinimo ->

                "STOCK_BAJO"

            else ->

                "ACTIVO"
        }
    }

    // PURGAR

    fun purgarAntiguos() {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            val threshold =

                System.currentTimeMillis() -

                        (
                                90L *
                                        24 *
                                        60 *
                                        60 *
                                        1000
                                )

            salidaDao
                .permanentPurge(
                    threshold
                )
        }
    }

    // ELIMINAR TODO

    fun eliminarTodo() {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            salidaDao.deleteAll()
        }
    }
}