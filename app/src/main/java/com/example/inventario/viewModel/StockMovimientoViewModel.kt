package com.example.inventario.viewModel



import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.repos.appdatabase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StockMovimientoViewModel(

    application: Application

) : AndroidViewModel(application) {

    private val db =

        appdatabase
            .getDatabase(application)

    private val productoDao =

        db.productoDao()

    // =========================
    // RESTAR STOCK
    // =========================

    fun descontarStock(

        codigoProducto: String,

        bodegaId: String,

        cantidad: Int

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                val producto =

                    productoDao
                        .obtenerProductoPorCodigo(

                            codigoProducto,

                            bodegaId
                        )

                producto?.let {

                    val nuevoStock =

                        (
                                it.cantidad -
                                        cantidad
                                )

                            .coerceAtLeast(0)

                    productoDao
                        .actualizar(

                            it.copy(

                                cantidad =
                                    nuevoStock
                            )
                        )
                }

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // =========================
    // AUMENTAR STOCK
    // =========================

    fun aumentarStock(

        codigoProducto: String,

        bodegaId: String,

        cantidad: Int

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                val producto =

                    productoDao
                        .obtenerProductoPorCodigo(

                            codigoProducto,

                            bodegaId
                        )

                producto?.let {

                    val nuevoStock =

                        it.cantidad +
                                cantidad

                    productoDao
                        .actualizar(

                            it.copy(

                                cantidad =
                                    nuevoStock
                            )
                        )
                }

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // =========================
    // VALIDAR STOCK
    // =========================

    suspend fun validarStock(

        codigoProducto: String,

        bodegaId: String,

        cantidad: Int

    ): Boolean {

        return try {

            val producto =

                productoDao
                    .obtenerProductoPorCodigo(

                        codigoProducto,

                        bodegaId
                    )

            producto != null &&

                    producto.cantidad >=
                    cantidad

        } catch (

            e: Exception

        ) {

            false
        }
    }
}
