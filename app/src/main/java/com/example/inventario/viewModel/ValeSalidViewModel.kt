package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.bodega.ValeSalida
import com.example.inventario.data.bodega.ValeSalidaDao
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ValeSalidaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: ValeSalidaDao = appdatabase.getDatabase(application).ValeSalidaDao()

    fun insertarVale(valeSalida: ValeSalida) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.insertarVale(valeSalida)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarVale(valeSalida: ValeSalida) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.actualizarVale(valeSalida)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarVale(valeSalida: ValeSalida) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.eliminarVale(valeSalida)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun obtenerVales(bodegaId: String): Flow<List<ValeSalida>> =
        if (bodegaId.isBlank()) dao.obtenerVales()
        else dao.obtenerValesPorBodega(bodegaId)

    suspend fun obtenerValePorId(id: Int): ValeSalida? =
        dao.obtenerValePorId(id)

    suspend fun totalVales(bodegaId: String): Int =
        if (bodegaId.isBlank()) dao.totalVales()
        else dao.totalValesPorBodega(bodegaId)

    fun eliminarTodo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.eliminarTodo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
