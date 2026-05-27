package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import com.example.inventario.ui.dashboard.StockPedidoUtil
import com.example.inventario.ui.dashboard.DashboardChartUtil

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val productoDao = db.productoDao()
    private val entradaDao = db.entradaDao()
    private val salidaDao = db.salidaDao()
    private val facturaDao = db.facturaDao()
    private val kardexDao = db.kardexDao()
    private val valeConDetallesDao = db.ValeConDetallesDao()

    private val _bodegaId = MutableStateFlow("")

    val productos = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else productoDao.obtenerProductos(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entradas = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else entradaDao.getEntradasByBodega(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salidas = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else salidaDao.getSalidasByBodega(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facturas = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else facturaDao.getFacturasByBodega(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kardex = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else kardexDao.getKardexByBodega(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vales = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else valeConDetallesDao.obtenerValesConDetallesPorBodega(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalVales = vales.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun cargarDashboard(bodegaId: String) {
        _bodegaId.value = bodegaId
    }

    // Cálculos para Dashboard

    val totalProductos = productos.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val existencias = productos.map { it.sumOf { p -> p.cantidad } }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val stockBajo = productos.map { list ->
        list.count { StockPedidoUtil.esStockBajo(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val valorInventario = productos.map { it.sumOf { p -> p.cantidad * p.costo } }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val productoMasMovido = kardex.map { list ->
        list.groupBy { it.codigoProducto }
            .maxByOrNull { it.value.size }?.value?.firstOrNull()?.descripcion ?: "N/A"
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "N/A")

    val productoCritico = productos.map { list ->
        list.minByOrNull { it.cantidad }?.descripcion ?: "N/A"
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "N/A")

    val promedioSalidas = salidas.map { list ->
        if (list.isEmpty()) 0 else list.sumOf { it.cantidad } / list.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val totalFacturas = facturas.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Alertas y Predicciones
    val productosBajoStock = productos.map { list ->
        list.filter { StockPedidoUtil.esStockBajo(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val prediccionesStock = combine(productos, salidas) { prods, sals ->
        StockPedidoUtil.prediccionesDesde(prods, sals)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Datos para gráficas (ejemplo simplificado, se puede expandir por fecha)
    val statsEntradas = entradas.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val statsSalidas = salidas.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val statsFacturas = facturas.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    val areaEntradas = entradas.map { list ->
        DashboardChartUtil.serieReciente(list) { it.cantidad.toFloat() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val areaSalidas = salidas.map { list ->
        DashboardChartUtil.serieReciente(list) { it.cantidad.toFloat() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val areaFacturas = facturas.map { list ->
        DashboardChartUtil.serieReciente(list) { it.cantidad.toFloat() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val areaStock = kardex.map { list ->
        DashboardChartUtil.serieReciente(list) { it.saldoNuevo.toFloat() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
