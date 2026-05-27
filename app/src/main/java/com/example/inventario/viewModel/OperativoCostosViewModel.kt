package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CostoGrupo(
    val clave: String,
    val total: Double,
    val movimientos: Int
)

data class CostoDetalleLinea(
    val mesEtiqueta: String,
    val concepto: String,
    val destino: String,
    val area: String,
    val vehiculo: String,
    val monto: Double,
    val fecha: String
)

data class OperativoCostosState(
    val gastoMensual: Double = 0.0,
    val porArea: List<CostoGrupo> = emptyList(),
    val porDestino: List<CostoGrupo> = emptyList(),
    val porVehiculo: List<CostoGrupo> = emptyList(),
    val porCampo: List<CostoGrupo> = emptyList(),
    val productosMasUsados: List<CostoGrupo> = emptyList(),
    val detalleLineas: List<CostoDetalleLinea> = emptyList()
)

enum class RangoOperativo { DIA, SEMANA, MES, ANIO }

@OptIn(ExperimentalCoroutinesApi::class)
class OperativoCostosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val _bodegaId = MutableStateFlow("")
    private val _rango = MutableStateFlow(RangoOperativo.MES)

    private val salidasFlow = _bodegaId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else db.salidaDao().getSalidasByBodega(id)
    }

    val state = combine(salidasFlow, _rango) { salidas, rango ->
        buildState(filtrarPorRango(salidas, rango))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OperativoCostosState())

    fun cargar(bodegaId: String) {
        _bodegaId.value = bodegaId
    }

    fun setRango(rango: RangoOperativo) {
        _rango.value = rango
    }

    private fun monto(s: Salida): Double =
        s.total.takeIf { it > 0 } ?: s.costoUnitario * s.cantidad

    private fun buildState(salidas: List<Salida>): OperativoCostosState {
        fun agrupar(selector: (Salida) -> String): List<CostoGrupo> =
            salidas
                .groupBy { selector(it).ifBlank { "Sin asignar" } }
                .map { (k, v) -> CostoGrupo(k, v.sumOf { monto(it) }, v.size) }
                .sortedByDescending { it.total }
                .take(8)

        val detalle = salidas
            .sortedByDescending { parseFecha(it.fechaSalida) }
            .map { s ->
                CostoDetalleLinea(
                    mesEtiqueta = mesEtiqueta(s.fechaSalida),
                    concepto = s.descripcion.ifBlank { s.codigoProducto },
                    destino = s.destino.ifBlank { "—" },
                    area = s.area.ifBlank { "—" },
                    vehiculo = s.vehiculo.ifBlank { s.placa.ifBlank { "—" } },
                    monto = monto(s),
                    fecha = s.fechaSalida
                )
            }

        return OperativoCostosState(
            gastoMensual = salidas.sumOf { monto(it) },
            porArea = agrupar { it.area },
            porDestino = agrupar { it.destino },
            porVehiculo = agrupar { it.vehiculo.ifBlank { it.placa } },
            porCampo = agrupar { it.campoAgricola },
            productosMasUsados = agrupar { it.descripcion.ifBlank { it.codigoProducto } },
            detalleLineas = detalle
        )
    }

    private fun mesEtiqueta(fecha: String): String {
        val cal = parseFechaCalendar(fecha) ?: return "Sin fecha"
        val meses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return "${meses[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
    }

    private fun filtrarPorRango(salidas: List<Salida>, rango: RangoOperativo): List<Salida> {
        val now = System.currentTimeMillis()
        val ms = when (rango) {
            RangoOperativo.DIA -> 86_400_000L
            RangoOperativo.SEMANA -> 7 * 86_400_000L
            RangoOperativo.MES -> 30L * 86_400_000L
            RangoOperativo.ANIO -> 365L * 86_400_000L
        }
        val desde = now - ms
        return salidas.filter { parseFecha(it.fechaSalida) >= desde }
    }

    private fun parseFecha(fecha: String): Long =
        parseFechaCalendar(fecha)?.timeInMillis ?: 0L

    private fun parseFechaCalendar(fecha: String): Calendar? {
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
