package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.PresupuestoBodega
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MesFinanciero(
    val etiqueta: String,
    val ingresos: Double,
    val gastos: Double,
    val presupuestoMeta: Double = 0.0
)

data class ResumenFinancieroState(
    val presupuestoInicial: Double = 0.0,
    val presupuestoActual: Double = 0.0,
    val presupuestoFinal: Double = 0.0,
    val presupuestoMetaPeriodo: Double = 0.0,
    val tipoPeriodoActivo: String = "MENSUAL",
    val anioActivo: Int = Calendar.getInstance().get(Calendar.YEAR),
    val indicePeriodoActivo: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val ingresosTotales: Double = 0.0,
    val egresosTotales: Double = 0.0,
    val utilidad: Double = 0.0,
    val consumoOperativo: Double = 0.0,
    @Deprecated("Usar consumoOperativo", ReplaceWith("consumoOperativo"))
    val ventas: Double = 0.0,
    val gastosCompras: Double = 0.0,
    val balance: Double = 0.0,
    val progresoPct: Float = 0f,
    val meses: List<MesFinanciero> = emptyList(),
    val presupuestosGuardados: List<PresupuestoBodega> = emptyList()
)

enum class TipoPeriodoPresupuesto(val etiqueta: String) {
    MENSUAL("Mensual"),
    TRIMESTRAL("Trimestral"),
    SEMESTRAL("Semestral"),
    ANUAL("Anual")
}

@OptIn(ExperimentalCoroutinesApi::class)
class PresupuestoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val presupuestoDao = db.presupuestoBodegaDao()
    private val bodegaDao = db.bodegaDao()
    private val firebase = FirebaseRepository()
    private val _bodegaId = MutableStateFlow("")
    private val _tipoPeriodo = MutableStateFlow(TipoPeriodoPresupuesto.MENSUAL.name)
    private val _anio = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _indicePeriodo = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)

    val tipoPeriodoActivo = _tipoPeriodo.stateIn(viewModelScope, SharingStarted.Eagerly, TipoPeriodoPresupuesto.MENSUAL.name)
    val anioActivo = _anio.stateIn(viewModelScope, SharingStarted.Eagerly, Calendar.getInstance().get(Calendar.YEAR))
    val indicePeriodoActivo = _indicePeriodo.stateIn(viewModelScope, SharingStarted.Eagerly, Calendar.getInstance().get(Calendar.MONTH) + 1)

    private val productos = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else db.productoDao().obtenerProductos(id)
    }
    private val entradas = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else db.entradaDao().getEntradasByBodega(id)
    }
    private val salidas = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else db.salidaDao().getSalidasByBodega(id)
    }
    private val facturas = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else db.facturaDao().getFacturasByBodega(id)
    }
    private val presupuestosMeta = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else presupuestoDao.observarPorBodega(id)
    }

    val resumen = combine(
        combine(productos, entradas, salidas, facturas, presupuestosMeta) { prods, ents, sals, facts, metas ->
            listOf(prods, ents, sals, facts, metas)
        },
        combine(_tipoPeriodo, _anio, _indicePeriodo) { tipo, anio, indice ->
            Triple(tipo, anio, indice)
        }
    ) { datos, periodo ->
        @Suppress("UNCHECKED_CAST")
        val prods = datos[0] as List<com.example.inventario.data.bodega.Producto>
        @Suppress("UNCHECKED_CAST")
        val ents = datos[1] as List<com.example.inventario.data.bodega.Entrada>
        @Suppress("UNCHECKED_CAST")
        val sals = datos[2] as List<com.example.inventario.data.bodega.Salida>
        @Suppress("UNCHECKED_CAST")
        val facts = datos[3] as List<com.example.inventario.data.bodega.Factura>
        @Suppress("UNCHECKED_CAST")
        val metas = datos[4] as List<PresupuestoBodega>
        val (tipo, anio, indice) = periodo
        val presupuestoInventario = prods.sumOf { it.cantidad * it.costo }
        val presupuestoMetaPeriodo = metas.find {
            it.tipoPeriodo == tipo && it.anio == anio && it.indicePeriodo == indice
        }?.monto ?: 0.0

        val presupuestoInicial = presupuestoMetaPeriodo.takeIf { it > 0 }
            ?: prods.sumOf { it.presupuesto }.takeIf { it > 0 }
            ?: presupuestoInventario

        val gastosEntradas = ents.sumOf { it.costoEntrada * it.cantidad }
        val gastosFacturas = facts.sumOf { it.total }
        val egresos = gastosEntradas + gastosFacturas

        val consumoOperativo = sals.sumOf { montoSalida(it) }
        val ingresos = consumoOperativo

        val utilidad = ingresos - egresos
        val presupuestoFinal = presupuestoInicial + utilidad
        val balance = presupuestoFinal - presupuestoInicial
        val metaProgreso = presupuestoInicial.coerceAtLeast(1.0)
        val progreso = ((egresos + consumoOperativo) / metaProgreso * 100).toFloat().coerceIn(0f, 100f)

        val meses = buildMesesComparativos(ents, sals, metas, anio)

        ResumenFinancieroState(
            presupuestoInicial = presupuestoInicial,
            presupuestoActual = presupuestoInventario,
            presupuestoFinal = presupuestoFinal,
            presupuestoMetaPeriodo = presupuestoMetaPeriodo,
            tipoPeriodoActivo = tipo,
            anioActivo = anio,
            indicePeriodoActivo = indice,
            ingresosTotales = ingresos,
            egresosTotales = egresos,
            utilidad = utilidad,
            consumoOperativo = consumoOperativo,
            ventas = consumoOperativo,
            gastosCompras = egresos,
            balance = balance,
            progresoPct = progreso,
            meses = meses,
            presupuestosGuardados = metas
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenFinancieroState())

    fun cargar(bodegaId: String) {
        _bodegaId.value = bodegaId
    }

    fun sincronizarDesdeFirebase(bodegaId: String) {
        if (bodegaId.isBlank()) return
        viewModelScope.launch {
            val bodega = bodegaDao.obtenerBodegaPorId(bodegaId) ?: return@launch
            firebase.obtenerPresupuestos(bodega.codigoCorto, bodegaId).forEach { remoto ->
                presupuestoDao.guardar(remoto)
            }
        }
    }

    fun setPeriodoActivo(tipo: String, anio: Int, indice: Int) {
        _tipoPeriodo.value = tipo
        _anio.value = anio
        _indicePeriodo.value = indice
    }

    fun guardarPresupuesto(bodegaId: String, monto: Double, notas: String = "") {
        if (bodegaId.isBlank() || monto <= 0) return
        viewModelScope.launch {
            val existente = presupuestoDao.obtener(
                bodegaId,
                _tipoPeriodo.value,
                _anio.value,
                _indicePeriodo.value
            )
            val guardado = PresupuestoBodega(
                id = existente?.id ?: 0,
                bodegaId = bodegaId,
                tipoPeriodo = _tipoPeriodo.value,
                anio = _anio.value,
                indicePeriodo = _indicePeriodo.value,
                monto = monto,
                notas = notas,
                fechaCreacion = existente?.fechaCreacion ?: System.currentTimeMillis(),
                ultimaActualizacion = System.currentTimeMillis()
            )
            presupuestoDao.guardar(guardado)
            bodegaDao.obtenerBodegaPorId(bodegaId)?.let { bodega ->
                firebase.guardarPresupuesto(guardado, bodega.codigoCorto)
            }
        }
    }

    private fun montoSalida(s: com.example.inventario.data.bodega.Salida): Double =
        s.total.takeIf { it > 0 } ?: s.costoUnitario * s.cantidad

    private fun buildMesesComparativos(
        entradas: List<com.example.inventario.data.bodega.Entrada>,
        salidas: List<com.example.inventario.data.bodega.Salida>,
        metas: List<PresupuestoBodega>,
        anio: Int
    ): List<MesFinanciero> {
        val labels = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val gastosPorMes = MutableList(12) { 0.0 }
        val ingPorMes = MutableList(12) { 0.0 }
        val metaPorMes = MutableList(12) { 0.0 }

        entradas.forEach { e ->
            val idx = monthIndex(e.fechaIngreso, anio)
            if (idx in 0..11) gastosPorMes[idx] += e.costoEntrada * e.cantidad
        }
        salidas.forEach { s ->
            val idx = monthIndex(s.fechaSalida, anio)
            if (idx in 0..11) ingPorMes[idx] += montoSalida(s)
        }
        metas.filter { it.tipoPeriodo == TipoPeriodoPresupuesto.MENSUAL.name && it.anio == anio }
            .forEach { meta ->
                val idx = meta.indicePeriodo - 1
                if (idx in 0..11) metaPorMes[idx] = meta.monto
            }

        return labels.mapIndexed { i, label ->
            MesFinanciero(label, ingPorMes[i], gastosPorMes[i], metaPorMes[i])
        }
    }

    private fun monthIndex(fecha: String, anioObjetivo: Int): Int {
        val cal = parseFecha(fecha) ?: return -1
        if (cal.get(Calendar.YEAR) != anioObjetivo) return -1
        return cal.get(Calendar.MONTH)
    }

    private fun parseFecha(fecha: String): Calendar? {
        if (fecha.isBlank()) return null
        val formatos = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("d/M/yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        )
        for (fmt in formatos) {
            try {
                val date = fmt.parse(fecha.trim().split(" ").first()) ?: continue
                return Calendar.getInstance().apply { time = date }
            } catch (_: Exception) {
            }
        }
        return null
    }
}
