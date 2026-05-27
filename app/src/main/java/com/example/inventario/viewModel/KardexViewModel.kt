package com.example.inventario.viewModel




import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.bodega.Kardex

import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.appdatabase

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

import kotlinx.coroutines.launch

import java.text.SimpleDateFormat

import java.util.Calendar
import java.util.Locale

class KardexViewModel(

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

    private val kardexDao =

        database.kardexDao()

    // BUSQUEDA

    private val _searchQuery =

        MutableStateFlow("")

    val searchQuery:

            StateFlow<String> =

        _searchQuery

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

    // TEXTO

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

    // OBTENER TODOS

    fun obtenerKardex(

        bodegaId: String

    ) =

        kardexDao
            .getKardexByBodega(
                bodegaId
            )

    // FILTRADOS

    @OptIn(
        ExperimentalCoroutinesApi::class
    )

    fun obtenerKardexFiltrado(

        bodegaId: String

    ): Flow<List<Kardex>> {

        return combine(

            _searchQuery,

            _filtroPeriodo,

            _fechaReferencia

        ) {

                query,
                periodo,
                fecha ->

            Triple(

                query,

                periodo,

                fecha
            )

        }.flatMapLatest {

                (
                    query,
                    periodo,
                    fecha
                ) ->

            val flow =

                if (

                    query.isEmpty()

                ) {

                    kardexDao
                        .getKardexByBodega(
                            bodegaId
                        )

                } else {

                    kardexDao
                        .buscarKardex(

                            bodegaId,

                            query
                        )
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

    // FILTRAR

    private fun filtrarPorPeriodo(

        lista: List<Kardex>,

        periodo: String,

        calRef: Calendar

    ): List<Kardex> {

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

                movimiento ->

            try {

                val fechaMovimiento =

                    sdf.parse(

                        movimiento
                            .fechaMovimiento
                    )

                        ?: return@filter false

                val calMovimiento =

                    Calendar
                        .getInstance()
                        .apply {

                            time =
                                fechaMovimiento
                        }

                when (periodo) {

                    "Dia" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calMovimiento.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.DAY_OF_YEAR
                                ) ==

                                calMovimiento.get(
                                    Calendar.DAY_OF_YEAR
                                )

                    "Semana" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calMovimiento.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.WEEK_OF_YEAR
                                ) ==

                                calMovimiento.get(
                                    Calendar.WEEK_OF_YEAR
                                )

                    "Mes" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calMovimiento.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.MONTH
                                ) ==

                                calMovimiento.get(
                                    Calendar.MONTH
                                )

                    "Año" ->

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calMovimiento.get(
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

    // AGREGAR

    fun agregarMovimiento(

        kardex: Kardex

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            kardexDao.insert(
                kardex
            )
        }
    }

    // ACTUALIZAR

    fun actualizarMovimiento(

        kardex: Kardex

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            kardexDao.update(
                kardex
            )
        }
    }

    // ELIMINAR

    fun eliminarMovimiento(

        kardex: Kardex

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            kardexDao.delete(
                kardex
            )
        }
    }

    // POR ID

    suspend fun obtenerPorId(

        id: Int

    ): Kardex? {

        return kardexDao
            .getKardexById(id)
    }

    // POR TIPO

    fun obtenerPorTipo(

        tipo: String

    ): Flow<List<Kardex>> {

        return kardexDao
            .getKardexByTipo(
                tipo
            )
    }

    // POR STATUS

    fun obtenerPorStatus(

        status: String

    ): Flow<List<Kardex>> {

        return kardexDao
            .getKardexByStatus(
                status
            )
    }

    // FILTRO FECHA

    fun obtenerPorFecha(

        inicio: String,

        fin: String

    ): Flow<List<Kardex>> {

        return kardexDao
            .getKardexPorFecha(

                inicio,

                fin
            )
    }

    // PAPELERA

    fun obtenerPapelera() =

        kardexDao
            .getDeletedKardex()

    // RESTAURAR

    fun restaurarMovimiento(

        kardex: Kardex

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            kardexDao.restore(
                kardex.id
            )
        }
    }

    // ELIMINAR PERMANENTE

    fun eliminarPermanente(

        kardex: Kardex

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            kardexDao
                .deletePermanently(
                    kardex.id
                )
        }
    }

    // KPI

    fun calcularTotalMovimientos(

        lista: List<Kardex>

    ): Int {

        return lista.size
    }

    fun calcularCantidadTotal(

        lista: List<Kardex>

    ): Int {

        return lista.sumOf {

            it.cantidad
        }
    }

    fun calcularValorTotal(

        lista: List<Kardex>

    ): Double {

        return lista.sumOf {

            it.totalMovimiento
        }
    }

    // CODIGO

    fun generarCodigoMovimiento():

            String {

        return "KDX-" +

                System.currentTimeMillis()
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

            kardexDao
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

            kardexDao.deleteAll()
        }
    }
}