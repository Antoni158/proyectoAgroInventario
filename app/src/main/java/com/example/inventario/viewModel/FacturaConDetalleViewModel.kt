package com.example.inventario.viewModel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.FacturaConDetalles

import com.example.inventario.data.repos.appdatabase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FacturaConDetalleViewModel(

    application: Application

) : AndroidViewModel(application) {

    // DATABASE

    private val db =

        appdatabase
            .getDatabase(application)

    // DAO DETALLE

    private val detalleDao =

        db.detalleFacturaDao()

    // DAO RELACION

    private val facturaConDetallesDao =

        db.facturaConDetallesDao()

    // INSERTAR DETALLE

    fun insertarDetalle(

        detalleFactura: DetalleFactura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                detalleDao.insertarDetalle(
                    detalleFactura
                )

                recalcularTotalFactura(
                    detalleFactura.facturaId
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // ACTUALIZAR DETALLE

    fun actualizarDetalle(

        detalleFactura: DetalleFactura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                detalleDao.actualizarDetalle(
                    detalleFactura
                )

                recalcularTotalFactura(
                    detalleFactura.facturaId
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // ELIMINAR DETALLE

    fun eliminarDetalle(

        detalleFactura: DetalleFactura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                detalleDao.eliminarDetalle(
                    detalleFactura
                )

                recalcularTotalFactura(
                    detalleFactura.facturaId
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // OBTENER DETALLES FACTURA

    fun obtenerDetallesFactura(

        facturaId: Int

    ): Flow<List<DetalleFactura>> {

        return detalleDao
            .obtenerDetallesFactura(
                facturaId
            )
    }

    // OBTENER DETALLE POR ID

    suspend fun obtenerDetallePorId(

        id: Int

    ): DetalleFactura? {

        return detalleDao
            .obtenerDetallePorId(
                id
            )
    }

    // ELIMINAR TODOS LOS DETALLES

    fun eliminarDetallesFactura(

        facturaId: Int

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                detalleDao
                    .eliminarDetallesFactura(
                        facturaId
                    )

                recalcularTotalFactura(
                    facturaId
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // CALCULAR TOTAL FACTURA

    suspend fun calcularTotalFactura(

        facturaId: Int

    ): Double {

        return detalleDao
            .calcularTotalFactura(
                facturaId
            ) ?: 0.0
    }

    // RECALCULAR TOTAL

    private suspend fun recalcularTotalFactura(

        facturaId: Int

    ) {

        val total =

            detalleDao
                .calcularTotalFactura(
                    facturaId
                ) ?: 0.0

        db.facturaDao()
            .actualizarTotalFactura(

                facturaId,

                total
            )
    }

    // OBTENER FACTURA CON DETALLES

    suspend fun obtenerFacturaConDetalles(

        facturaId: Int

    ): FacturaConDetalles {

        return facturaConDetallesDao
            .obtenerFacturaConDetalles(
                facturaId
            )
    }

    // TOTAL GENERAL

    suspend fun obtenerTotalGeneral():

            Double {

        return facturaConDetallesDao
            .obtenerTotalGeneral()
            ?: 0.0
    }

    // CONTAR FACTURAS

    suspend fun contarFacturas():

            Int {

        return facturaConDetallesDao
            .contarFacturas()
    }

    // TOTAL PRODUCTOS

    suspend fun totalProductosFacturados():

            Int {

        return facturaConDetallesDao
            .totalProductosFacturados()
            ?: 0
    }

    // PROMEDIO FACTURAS

    suspend fun promedioFacturas():

            Double {

        return facturaConDetallesDao
            .promedioFacturas()
            ?: 0.0
    }
}