package com.example.inventario.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.inventario.data.repos.appdatabase
import kotlinx.coroutines.flow.Flow
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.Entrada

/**
 * Consultas de reportes/historiales filtrados por bodega.
 */
class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)

    fun historialKardex(bodegaId: String): Flow<List<Kardex>> =
        db.kardexDao().getKardexByBodega(bodegaId)

    fun historialSalidas(bodegaId: String): Flow<List<Salida>> =
        db.salidaDao().getSalidasByBodega(bodegaId)

    fun historialEntradas(bodegaId: String): Flow<List<Entrada>> =
        db.entradaDao().getEntradasByBodega(bodegaId)
}
