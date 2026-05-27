package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FacturaViewModel(

    application: Application

) : AndroidViewModel(application) {

    // DATABASE

    private val db =
        appdatabase.getDatabase(application)

    // DAO

    private val dao =
        db.facturaDao()

    // FIREBASE

    private val firebaseRepo =
        FirebaseRepository()

    // REPOSITORY

    private val repository =

        InventoryRepository(

            bodegaDao =
                db.bodegaDao(),

            productoDao =
                db.productoDao(),

            categoriaDao =
                db.categoriaDao(),

            entradaDao =
                db.entradaDao(),

            salidaDao =
                db.salidaDao(),

            facturaDao =
                db.facturaDao(),

            firebaseRepository =
                firebaseRepo
        )

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

                                "dd/MM/yyyy",

                                Locale.getDefault()

                            ).format(cal.time)

                "Semana" ->

                    "Semana " +

                            cal.get(
                                Calendar.WEEK_OF_YEAR
                            )

                "Mes" ->

                    SimpleDateFormat(

                        "MMMM yyyy",

                        Locale.getDefault()

                    ).format(cal.time)

                "Año" ->

                    cal.get(
                        Calendar.YEAR
                    ).toString()

                else ->

                    "Todo"
            }

        }.stateIn(

            viewModelScope,

            SharingStarted
                .WhileSubscribed(5000),

            ""
        )

    // INIT

    init {
        val bId = SessionManager.obtenerBodegaActual()
        if (bId.isNotEmpty()) {
            viewModelScope.launch {
                val bodega = appdatabase.getDatabase(getApplication()).bodegaDao().obtenerBodegaPorId(bId)
                bodega?.let {
                    sincronizarDesdeFirebase(it.codigoCorto, it.id)
                }
            }
        }
    }

    // BUSQUEDA

    fun setSearchQuery(
        query: String
    ) {

        _searchQuery.value =
            query
    }

    // FILTRO

    fun setFiltroPeriodo(
        periodo: String
    ) {

        _filtroPeriodo.value =
            periodo
    }

    // FECHA

    fun setFechaReferencia(
        calendar: Calendar
    ) {

        _fechaReferencia.value =
            calendar
    }

    // FACTURAS FILTRADAS

    @OptIn(
        ExperimentalCoroutinesApi::class
    )

    fun obtenerFacturasFiltradas(

        bodegaId: String

    ): Flow<List<Factura>> {

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

                    dao.getFacturasByBodega(
                        bodegaId
                    )

                } else {

                    dao.buscarFacturas(
                        bodegaId,
                        query
                    )
                }

            flow.map { lista ->

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

        lista: List<Factura>,

        periodo: String,

        calRef: Calendar

    ): List<Factura> {

        if (
            periodo == "Todo"
        ) return lista

        val sdf =

            SimpleDateFormat(

                "d/M/yyyy",

                Locale.getDefault()
            )

        return lista.filter { factura ->

            try {

                val fechaFactura =
                    sdf.parse(
                        factura.fecha
                    )

                        ?: return@filter false

                val calFactura =
                    Calendar.getInstance()

                calFactura.time =
                    fechaFactura

                when (periodo) {

                    "Dia" -> {

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calFactura.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.DAY_OF_YEAR
                                ) ==

                                calFactura.get(
                                    Calendar.DAY_OF_YEAR
                                )
                    }

                    "Semana" -> {

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calFactura.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.WEEK_OF_YEAR
                                ) ==

                                calFactura.get(
                                    Calendar.WEEK_OF_YEAR
                                )
                    }

                    "Mes" -> {

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calFactura.get(
                                    Calendar.YEAR
                                )

                                &&

                                calRef.get(
                                    Calendar.MONTH
                                ) ==

                                calFactura.get(
                                    Calendar.MONTH
                                )
                    }

                    "Año" -> {

                        calRef.get(
                            Calendar.YEAR
                        ) ==

                                calFactura.get(
                                    Calendar.YEAR
                                )
                    }

                    else -> true
                }

            } catch (

                e: Exception

            ) {

                false
            }
        }
    }

    // FIREBASE

    fun sincronizarDesdeFirebase(
        codigoBodega: String,
        bodegaId: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            try {
                val facturasNube =
                    firebaseRepo
                        .obtenerFacturas(
                            codigoBodega,
                            bodegaId
                        )

                facturasNube.forEach {
                        factura ->
                    dao.insert(factura)
                }
            } catch (
                e: Exception
            ) {
                e.printStackTrace()
            }
        }
    }

    // AGREGAR

    fun agregarFactura(

        factura: Factura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            repository.insertFactura(
                factura
            )
        }
    }

    // OBTENER POR ID

    suspend fun obtenerFacturaPorId(id: Int): Factura? {
        return dao.getFacturaById(id)
    }

    // ACTUALIZAR

    fun actualizarFactura(

        factura: Factura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            repository.updateFactura(
                factura
            )
        }
    }

    // ELIMINAR

    fun eliminarFactura(

        factura: Factura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            repository.deleteFactura(
                factura
            )
        }
    }

    // TOTAL FACTURAS

    fun calcularTotalFacturas(

        lista: List<Factura>

    ): Double {

        return lista.sumOf {
            it.total
        }
    }

    // TOTAL PRODUCTOS

    fun calcularTotalProductos(

        lista: List<Factura>

    ): Int {

        return lista.sumOf {
            it.cantidad
        }
    }

    // TOTAL REGISTROS

    fun calcularCantidadFacturas(lista: List<Factura>): Int = lista.size

    fun obtenerPapelera(): Flow<List<Factura>> = dao.getDeletedFacturas()

    fun restaurarFactura(factura: Factura) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreFactura(factura.id)
        }
    }

    fun eliminarPermanente(factura: Factura) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFacturaPermanently(factura.id, factura.bodegaId, factura.codigoBodega)
        }
    }

    fun purgarAntiguos() {
        viewModelScope.launch(Dispatchers.IO) {
            val threshold = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
            dao.permanentPurge(threshold)
        }
    }
}