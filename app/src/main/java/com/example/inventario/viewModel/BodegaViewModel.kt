package com.example.inventario.viewModel


import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.BodegaCodigoUtil
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.InventoryRepository
import com.example.inventario.data.repos.appdatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BodegaViewModel(

    application: Application

) : AndroidViewModel(application) {

    private val db = appdatabase.getDatabase(application)
    private val dao = db.bodegaDao()

    private val firebaseRepo = FirebaseRepository()
    private val repository = InventoryRepository(
        db.bodegaDao(),
        db.productoDao(),
        db.categoriaDao(),
        db.entradaDao(),
        db.salidaDao(),
        db.facturaDao(),
        firebaseRepo
    )

    // obtener bodegas

    val bodegas:

            Flow<List<Bodega>> =

        dao.obtenerBodegas()

    init {
        firebaseRepo.bodegasRef().keepSynced(true)
        escucharBodegasFirebase()
        sincronizarDesdeFirebase()
    }

    private fun escucharBodegasFirebase() {
        firebaseRepo.bodegasRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        repository.refreshBodegas()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FIREBASE_BODEGAS", error.message)
            }
        })
    }

    fun sincronizarDesdeFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.refreshBodegas()
                normalizarCodigosExistentes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // actualizar nube

    fun actualizarTodoDesdeNube() {

        sincronizarDesdeFirebase()
    }

    // crear bodega

    fun crearBodega(
        nombre: String,
        descripcion: String = ""
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val nombreTrim =
                nombre.trim()

            if (
                nombreTrim.isEmpty()
            ) {

                return@launch
            }

            val codigos =
                dao.listarCodigosActivos()

            val codigo =
                BodegaCodigoUtil
                    .generarCodigoCompleto(
                        nombreTrim,
                        codigos
                    )

            val nuevaBodega =
                Bodega(

                    id = codigo,

                    nombre =
                        nombreTrim,

                    codigoCorto =
                        codigo,

                    descripcion =
                        descripcion.trim()
                )

            repository.insertBodega(
                nuevaBodega
            )
        }
    }

    private suspend fun asegurarCodigo(
        bodega: Bodega
    ): Bodega {

        if (
            bodega.codigoCorto
                .isNotBlank()
        ) {

            return bodega
        }

        val codigos =
            dao.listarCodigosActivos()

        return bodega.copy(
            codigoCorto =
                BodegaCodigoUtil
                    .generarCodigoCompleto(
                        bodega.nombre,
                        codigos
                    )
        )
    }

    private suspend fun normalizarCodigosExistentes() {

        val activas =
            dao.listarActivasSync()

        val codigos =
            activas
                .map {
                    it.codigoCorto
                }
                .filter {
                    it.isNotBlank()
                }
                .toMutableList()

        activas
            .filter {
                BodegaCodigoUtil.necesitaCodigoLegible(it.codigoCorto)
            }
            .forEach { b ->

                val codigo =
                    BodegaCodigoUtil
                        .generarCodigoCompleto(
                            b.nombre,
                            codigos
                        )

                codigos.add(
                    codigo
                )

                val actualizada =
                    b.copy(
                        id = if (b.id.contains("-") && b.id.length > 20) b.id else codigo,
                        codigoCorto = codigo
                    )

                repository.updateBodega(
                    actualizada
                )
            }
    }

    // editar bodega

    fun editarBodega(

        bodega: Bodega

    ) {

        viewModelScope.launch(Dispatchers.IO) {

            repository.updateBodega(
                bodega
            )
        }
    }

    // eliminar bodega

    fun eliminarBodega(

        bodega: Bodega

    ) {

        viewModelScope.launch(Dispatchers.IO) {

            repository.deleteBodega(
                bodega
            )
        }
    }

    // restaurar bodega

    fun restaurarBodega(

        bodega: Bodega

    ) {

        viewModelScope.launch(Dispatchers.IO) {

            repository.restoreBodega(
                bodega
            )
        }
    }

    // obtener papelera

    fun obtenerPapelera():

            Flow<List<Bodega>> =

        dao.getDeletedBodegas()

    // purgar automatico

    fun purgarAntiguos() {

        viewModelScope.launch {

            val threshold =

                System.currentTimeMillis() -

                        (
                                90L * 24 * 60 * 60 * 1000
                                )

            dao.permanentPurge(
                threshold
            )
        }
    }

    // eliminar permanente

    fun eliminarPermanente(

        bodega: Bodega

    ) {

        viewModelScope.launch(Dispatchers.IO) {

            repository.deleteBodegaPermanently(
                bodega.id,
                bodega.codigoCorto
            )
        }
    }

    // obtener bodega por id

    suspend fun obtenerBodega(

        id: String

    ): Bodega? {

        return dao
            .obtenerBodegaPorId(
                id
            )
    }
}