package com.example.inventario.viewModel



import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.bodega.Traslado
import com.example.inventario.data.bodega.TrasladoDao

import com.example.inventario.data.repos.MovimientoInventarioService
import com.example.inventario.data.repos.appdatabase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TrasladoViewModel(

    application: Application

) : AndroidViewModel(application) {

    // DATABASE

    private val db =

        appdatabase
            .getDatabase(application)

    // DAO

    private val dao:
            TrasladoDao =

        db.TrasladoDao()

    // TRASLADO CON INVENTARIO

    suspend fun registrarTrasladoCompleto(
        traslado: Traslado,
        usuario: String
    ): MovimientoInventarioService.ResultadoMovimiento =
        MovimientoInventarioService(db).registrarTraslado(traslado, usuario)

    // INSERTAR LEGACY

    fun insertarTraslado(

        traslado: Traslado

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                dao.insertarTraslado(
                    traslado
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // ACTUALIZAR

    fun actualizarTraslado(

        traslado: Traslado

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                dao.actualizarTraslado(
                    traslado
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // ELIMINAR

    fun eliminarTraslado(

        traslado: Traslado

    ) {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                dao.eliminarTraslado(
                    traslado
                )

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }

    // OBTENER TODOS

    fun obtenerTraslados(bodegaId: String = ""): Flow<List<Traslado>> =
        if (bodegaId.isBlank()) dao.obtenerTraslados()
        else dao.obtenerTrasladosPorBodega(bodegaId)

    // OBTENER POR ID

    suspend fun obtenerTrasladoPorId(

        id: Int

    ): Traslado? {

        return dao
            .obtenerTrasladoPorId(
                id
            )
    }

    // TOTAL TRASLADOS

    suspend fun totalTraslados(): Int = dao.totalTraslados()

    // ELIMINAR TODO

    fun eliminarTodo() {

        viewModelScope.launch(

            Dispatchers.IO

        ) {

            try {

                dao.eliminarTodo()

            } catch (

                e: Exception

            ) {

                e.printStackTrace()
            }
        }
    }
}