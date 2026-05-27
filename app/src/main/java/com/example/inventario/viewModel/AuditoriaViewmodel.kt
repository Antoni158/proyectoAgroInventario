package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.Auditoria.Auditoria
import com.example.inventario.data.Auditoria.AuditoriaDao
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.ProductoDao
import com.example.inventario.service.NotificationHelper
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.MovimientoInventarioService
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.ui.auditoria.AuditoriaChartData
import com.example.inventario.ui.auditoria.CategoriaAuditoriaResumen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuditoriaResumen(
    val total: Int = 0,
    val exactos: Int = 0,
    val faltantes: Int = 0,
    val sobrantes: Int = 0,
    val exactitudPct: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
class AuditoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val dao: AuditoriaDao = db.auditoriaDao()
    private val productoDao: ProductoDao = db.productoDao()
    private val firebase = FirebaseRepository()
    private val movimiento = MovimientoInventarioService(db)

    private val _bodegaId = MutableStateFlow<String?>(null)
    private val _filtroEstado = MutableStateFlow<String?>(null)
    private val _busqueda = MutableStateFlow("")

    val auditorias: Flow<List<Auditoria>> = _bodegaId.flatMapLatest { bodegaId ->
        if (bodegaId.isNullOrBlank()) dao.obtenerTodas() else dao.obtenerPorBodega(bodegaId)
    }

    val productos: Flow<List<Producto>> = _bodegaId.flatMapLatest { bodegaId ->
        if (bodegaId.isNullOrBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            productoDao.obtenerProductos(bodegaId)
        }
    }

    private val _resumen = MutableStateFlow(AuditoriaResumen())
    val resumen: StateFlow<AuditoriaResumen> = _resumen.asStateFlow()

    private val _chartData = MutableStateFlow(AuditoriaChartData())
    val chartData: StateFlow<AuditoriaChartData> = _chartData.asStateFlow()

    private val _porCategoria = MutableStateFlow<List<CategoriaAuditoriaResumen>>(emptyList())
    val porCategoria: StateFlow<List<CategoriaAuditoriaResumen>> = _porCategoria.asStateFlow()

    init {
        viewModelScope.launch {
            auditorias.collect { lista ->
                val filtrada = aplicarFiltrosLocal(lista)
                val exactos = filtrada.count { it.estado == "EXACTO" }
                val faltantes = filtrada.count { it.estado == "FALTANTE" }
                val sobrantes = filtrada.count { it.estado == "SOBRANTE" }
                val total = filtrada.size.coerceAtLeast(1)
                _resumen.value = AuditoriaResumen(
                    total = filtrada.size,
                    exactos = exactos,
                    faltantes = faltantes,
                    sobrantes = sobrantes,
                    exactitudPct = (exactos.toDouble() / total) * 100.0
                )
                _chartData.value = AuditoriaChartData(
                    exactos = exactos,
                    faltantes = faltantes,
                    sobrantes = sobrantes,
                    exactitudPct = _resumen.value.exactitudPct.toInt()
                )
                _porCategoria.value = filtrada
                    .groupBy { it.categoria.ifBlank { "Sin categoría" } }
                    .map { (cat, items) ->
                        CategoriaAuditoriaResumen(
                            categoria = cat,
                            exactos = items.count { it.estado == "EXACTO" },
                            faltantes = items.count { it.estado == "FALTANTE" },
                            sobrantes = items.count { it.estado == "SOBRANTE" }
                        )
                    }
                    .sortedByDescending { it.faltantes + it.sobrantes }
            }
        }
    }

    fun cargarBodega(bodegaId: String) {
        _bodegaId.value = bodegaId
    }

    fun setFiltroEstado(estado: String?) {
        _filtroEstado.value = estado
    }

    fun setBusqueda(q: String) {
        _busqueda.value = q
    }

    fun auditoriasFiltradas(lista: List<Auditoria>): List<Auditoria> = aplicarFiltrosLocal(lista)

    private fun aplicarFiltrosLocal(lista: List<Auditoria>): List<Auditoria> {
        var r = lista
        _filtroEstado.value?.let { e -> r = r.filter { it.estado == e } }
        val q = _busqueda.value.trim()
        if (q.isNotEmpty()) {
            r = r.filter {
                it.codigo.contains(q, true) ||
                    it.descripcion.contains(q, true) ||
                    it.auditorNombre.contains(q, true) ||
                    it.categoria.contains(q, true)
            }
        }
        return r
    }

    suspend fun registrarAuditoriaProducto(
        producto: Producto,
        nombreBodega: String,
        stockFisico: Double,
        observacion: String,
        auditorId: Int,
        auditorNombre: String,
        aplicarAjuste: Boolean
    ): String? = withContext(Dispatchers.IO) {
        try {
            val stockSistema = producto.cantidad.toDouble()
            val diferencia = stockFisico - stockSistema
            val estado = when {
                diferencia > 0 -> "SOBRANTE"
                diferencia < 0 -> "FALTANTE"
                else -> "EXACTO"
            }
            val codigoBdg = producto.codigoBodega.ifBlank {
                db.bodegaDao().obtenerBodegaPorId(producto.bodegaId)?.codigoCorto.orEmpty()
            }.ifBlank { producto.bodegaId }

            val auditoria = Auditoria(
                productoId = producto.id,
                codigo = producto.codigo,
                descripcion = producto.descripcion,
                categoria = producto.categoria,
                bodegaId = producto.bodegaId,
                codigoBodega = codigoBdg,
                nombreBodega = nombreBodega,
                stockSistema = stockSistema,
                stockFisico = stockFisico,
                diferencia = diferencia,
                estado = estado,
                observacion = observacion,
                auditorId = auditorId,
                auditorNombre = auditorNombre,
                fecha = System.currentTimeMillis(),
                ajusteAplicado = estado == "EXACTO"
            )
            val id = dao.insertar(auditoria).toInt()
            val guardada = auditoria.copy(id = id)
            firebase.guardarAuditoria(guardada, codigoBdg)

            if (estado != "EXACTO") {
                NotificationHelper.registrar(
                    getApplication(),
                    "📋 Auditoría pendiente",
                    "Producto: ${producto.codigo} · $estado",
                    "AUDITORIA",
                    producto.bodegaId,
                    id.toString(),
                    producto.codigo
                )
            }

            if (aplicarAjuste && estado != "EXACTO") {
                when (val r = movimiento.registrarAjusteAuditoria(
                    producto = productoDao.obtenerProductoPorId(producto.id) ?: producto,
                    stockFisico = stockFisico,
                    auditoriaId = id,
                    usuario = auditorNombre,
                    observacion = observacion
                )) {
                    is MovimientoInventarioService.ResultadoMovimiento.AjusteOk -> {
                        firebase.guardarAuditoria(guardada.copy(ajusteAplicado = true), codigoBdg)
                        null
                    }
                    is MovimientoInventarioService.ResultadoMovimiento.Error -> r.mensaje
                    else -> "Ajuste no aplicado"
                }
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al guardar auditoría: ${e.message ?: "desconocido"}"
        }
    }

    suspend fun aplicarAjustePendiente(auditoria: Auditoria): String? = withContext(Dispatchers.IO) {
        try {
            if (auditoria.ajusteAplicado) return@withContext "Ajuste ya aplicado"
            if (auditoria.estado == "EXACTO") return@withContext "Conteo exacto, sin ajuste"
            val producto = productoDao.obtenerProductoPorId(auditoria.productoId)
                ?: productoDao.obtenerProductoPorCodigo(auditoria.codigo, auditoria.bodegaId)
                ?: return@withContext "Producto no encontrado"
            val codigoBdg = auditoria.codigoBodega.ifBlank {
                db.bodegaDao().obtenerBodegaPorId(auditoria.bodegaId)?.codigoCorto.orEmpty()
            }.ifBlank { auditoria.bodegaId }
            when (val r = movimiento.registrarAjusteAuditoria(
                producto = producto,
                stockFisico = auditoria.stockFisico,
                auditoriaId = auditoria.id,
                usuario = auditoria.auditorNombre,
                observacion = auditoria.observacion
            )) {
                is MovimientoInventarioService.ResultadoMovimiento.AjusteOk -> {
                    firebase.guardarAuditoria(auditoria.copy(ajusteAplicado = true), codigoBdg)
                    null
                }
                is MovimientoInventarioService.ResultadoMovimiento.Error -> r.mensaje
                else -> "Error al aplicar ajuste"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al aplicar ajuste: ${e.message ?: "desconocido"}"
        }
    }

    fun registrarAuditoriaProductoAsync(
        producto: Producto,
        nombreBodega: String,
        stockFisico: Double,
        observacion: String,
        auditorId: Int,
        auditorNombre: String,
        aplicarAjuste: Boolean,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                registrarAuditoriaProducto(
                    producto, nombreBodega, stockFisico, observacion,
                    auditorId, auditorNombre, aplicarAjuste
                )
            )
        }
    }

    fun aplicarAjusteAsync(auditoria: Auditoria, onResult: (String?) -> Unit) {
        viewModelScope.launch { onResult(aplicarAjustePendiente(auditoria)) }
    }
}
