package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.administracion.Log
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = appdatabase.getDatabase(application).logDao()

    val logs: Flow<List<Log>> = dao.obtenerTodos()

    fun buscarLogs(query: String): Flow<List<Log>> {
        return dao.buscarLogs(query)
    }

    fun insertarLog(log: Log) {
        viewModelScope.launch {
            dao.insertar(log)
        }
    }

    fun eliminarTodo() {
        viewModelScope.launch {
            dao.eliminarTodos()
        }
    }
}
