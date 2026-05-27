package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.notificacion.AppNotificacion
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificacionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = appdatabase.getDatabase(application).appNotificacionDao()

    val notificaciones = dao.observar().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun marcarLeida(id: Int) {
        viewModelScope.launch { dao.marcarLeida(id) }
    }
}
