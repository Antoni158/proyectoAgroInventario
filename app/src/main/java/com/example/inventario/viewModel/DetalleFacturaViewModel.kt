package com.example.inventario.viewModel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.FacturaDetalleDao

import com.example.inventario.data.repos.appdatabase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DetalleFacturaViewModel(

    application: Application

) : AndroidViewModel(application) {

    // DATABASE

    private val db =

        appdatabase
            .getDatabase(application)

    // DAO

    private val dao:
            FacturaDetalleDao =

        db.detalleFacturaDao()

    // INSERTAR DETALLE

    fun insertarDetalle(

        detalleFactura: DetalleFactura

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                dao.insertarDetalle(
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

                dao.actualizarDetalle(
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

                dao.eliminarDetalle(
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

        return dao
            .obtenerDetallesFactura(
                facturaId
            )
    }

    // OBTENER DETALLE POR ID

    suspend fun obtenerDetallePorId(

        id: Int

    ): DetalleFactura? {

        return dao
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

                dao.eliminarDetallesFactura(
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

        return dao
            .calcularTotalFactura(
                facturaId
            ) ?: 0.0
    }

    // RECALCULAR TOTAL

    private suspend fun recalcularTotalFactura(

        facturaId: Int

    ) {

        val total =

            dao.calcularTotalFactura(
                facturaId
            ) ?: 0.0

        db.facturaDao()
            .actualizarTotalFactura(

                facturaId,

                total
            )
    }

    // TOTAL PRODUCTOS

    suspend fun totalProductosFactura(

        facturaId: Int

    ): Int {

        val lista =

            dao.obtenerDetallesDirecto(
                facturaId
            )

        return lista.sumOf {

            it.cantidad
        }
    }

    // TOTAL SUBTOTALES

    suspend fun totalSubtotales(

        facturaId: Int

    ): Double {

        val lista =

            dao.obtenerDetallesDirecto(
                facturaId
            )

        return lista.sumOf {

            it.subtotal
        }
    }

    // BUSCAR DETALLES

    fun buscarDetalles(

        facturaId: Int,

        query: String

    ): Flow<List<DetalleFactura>> {

        return dao
            .buscarDetalles(

                facturaId,

                query
            )
    }
}