package com.example.inventario.data.repos

import android.content.Context
import android.util.Log
import com.example.inventario.data.firebase.ConflictResolver
import com.example.inventario.data.firebase.OfflineManager
import com.example.inventario.data.firebase.SyncQueueManager
import com.example.inventario.data.bodega.BodegaCodigoUtil
import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.DetalleVale
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.Vale
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CloudSyncManager(context: Context) {

    private val appContext = context.applicationContext
    private val db = appdatabase.getDatabase(appContext)
    private val firebase = FirebaseRepository()
    private val queue = SyncQueueManager(appContext)

    suspend fun sincronizarBidireccional(): SyncResult = withContext(Dispatchers.IO) {
        queue.processPending()
        val down = sincronizarCompletoDesdeNube()
        val up = subirDatosLocalesANube()
        queue.processPending()
        SyncResult(
            ok = down.ok && up.ok,
            mensaje = "↓ ${down.mensaje} | ↑ ${up.mensaje}"
        )
    }

    suspend fun sincronizarCompletoDesdeNube(): SyncResult = withContext(Dispatchers.IO) {
        var ok = true
        val detalle = StringBuilder()
        try {
            firebase.obtenerUsuarios().forEach { remote ->
                val local = db.usuarioDao().obtenerUsuarioPorUsername(remote.username)
                    ?: remote.uuid.takeIf { it.isNotBlank() }?.let { db.usuarioDao().obtenerUsuarioPorUuid(it) }
                val merged = when {
                    local == null -> remote.ensureUuid()
                    else -> ConflictResolver.mergeUsuario(local, remote.ensureUuid())
                }
                db.usuarioDao().insertar(merged)
            }
            detalle.append("Usuarios OK. ")

            val bodegas = firebase.obtenerBodegas()
            bodegas.forEach { bodega ->
                val local = db.bodegaDao().obtenerBodegaPorId(bodega.id)
                if (local?.isDeleted == true) return@forEach
                val codigos = db.bodegaDao().listarCodigosActivos()
                val final = if (BodegaCodigoUtil.necesitaCodigoLegible(bodega.codigoCorto)) {
                    val codigo = BodegaCodigoUtil.generarCodigoCompleto(bodega.nombre, codigos)
                    bodega.copy(
                        codigoCorto = codigo,
                        id = bodega.id.ifBlank { codigo }
                    )
                } else bodega
                db.bodegaDao().insertar(final)
            }
            detalle.append("Bodegas: ${bodegas.size}. ")

            firebase.obtenerCategorias().forEach { db.categoriaDao().insertar(it) }
            detalle.append("Categorías OK. ")

            firebase.obtenerTodosProductos().forEach { remote ->
                val local = db.productoDao().obtenerProductoPorCodigo(remote.codigo, remote.bodegaId)
                if (local == null) {
                    db.productoDao().insertar(remote)
                } else if ((remote.ultimoMovimiento ?: 0L) >= (local.ultimoMovimiento ?: 0L)) {
                    db.productoDao().insertar(remote)
                }
            }
            detalle.append("Productos OK. ")

            firebase.obtenerTodasEntradas().forEach { upsertEntrada(it) }
            firebase.obtenerTodasSalidas().forEach { upsertSalida(it) }
            firebase.obtenerTodasFacturas().forEach { upsertFactura(it) }
            firebase.obtenerTodosDetallesFacturas().forEach { upsertDetalleFactura(it) }
            firebase.obtenerTodasAuditorias().forEach { db.auditoriaDao().insertar(it) }
            firebase.obtenerTodosVales().forEach { upsertVale(it) }
            firebase.obtenerTodosDetallesVales().forEach { upsertDetalleVale(it) }
            firebase.obtenerTodosKardex().forEach { upsertKardex(it) }
            firebase.obtenerTodosTraslados().forEach { db.TrasladoDao().insertarTraslado(it) }
            firebase.obtenerTodosPresupuestos().forEach { db.presupuestoBodegaDao().guardar(it) }
            firebase.obtenerTodasNotificaciones().forEach { db.appNotificacionDao().insert(it) }
            limpiarDuplicadosSalidasYVales()
            detalle.append("Resto OK.")

            Log.i("SYNC_OK", detalle.toString())
        } catch (e: Exception) {
            ok = false
            detalle.append("Error: ${e.message}")
            Log.e("SYNC_ERROR", e.message ?: "Error", e)
        }
        SyncResult(ok, detalle.toString())
    }

    private suspend fun upsertEntrada(entrada: Entrada) {
        val dao = db.entradaDao()
        val local = when {
            entrada.id > 0 -> dao.getEntradaById(entrada.id)
            entrada.codigoEntrada.isNotBlank() -> dao.getEntradaByCodigo(entrada.codigoEntrada, entrada.bodegaId)
            else -> null
        }
        if (local == null) dao.insert(entrada) else dao.update(entrada.copy(id = local.id))
    }

    private suspend fun upsertSalida(salida: Salida) {
        val dao = db.salidaDao()
        val local = when {
            salida.id > 0 -> dao.getSalidaById(salida.id)
            salida.codigoSalida.isNotBlank() -> dao.getSalidaByCodigo(salida.codigoSalida, salida.bodegaId)
            else -> null
        }
        if (local == null) dao.insert(salida) else dao.update(salida.copy(id = local.id))
    }

    private suspend fun upsertFactura(factura: Factura) {
        val dao = db.facturaDao()
        val local = if (factura.id > 0) dao.getFacturaById(factura.id) else null
        if (local == null) dao.insert(factura) else dao.update(factura.copy(id = local.id))
    }

    private suspend fun upsertDetalleFactura(detalle: DetalleFactura) {
        val dao = db.detalleFacturaDao()
        val local = if (detalle.idDetalle > 0) dao.obtenerDetallePorId(detalle.idDetalle) else null
        if (local == null) dao.insertarDetalle(detalle) else dao.actualizarDetalle(detalle.copy(idDetalle = local.idDetalle))
    }

    private suspend fun upsertVale(vale: Vale) {
        val dao = db.ValeDao()
        val local = when {
            vale.idVale > 0 -> dao.obtenerValePorId(vale.idVale)
            vale.codigoVale.isNotBlank() -> dao.obtenerValePorCodigo(vale.codigoVale, vale.bodegaId)
            else -> null
        }
        if (local == null) {
            dao.insertarVale(vale)
        } else {
            dao.actualizarVale(vale.copy(idVale = local.idVale))
        }
    }

    private suspend fun upsertDetalleVale(detalle: DetalleVale) {
        val dao = db.DetalleValeDao()
        val local = when {
            detalle.idDetalle > 0 -> dao.obtenerDetallePorId(detalle.idDetalle)
            detalle.valeId > 0 && detalle.productoCodigo.isNotBlank() ->
                dao.buscarDetalleExistente(detalle.valeId, detalle.productoCodigo, detalle.codigoSalida)
            else -> null
        }
        if (local == null) {
            dao.insertarDetalle(detalle)
        } else {
            dao.actualizarDetalle(detalle.copy(idDetalle = local.idDetalle, valeId = local.valeId))
        }
    }

    private suspend fun upsertKardex(kardex: Kardex) {
        val dao = db.kardexDao()
        val local = if (kardex.id > 0) dao.getKardexById(kardex.id) else null
        if (local == null) dao.insert(kardex) else dao.update(kardex.copy(id = local.id))
    }

    /** Elimina duplicados históricos (mismo código de salida o misma línea de vale). */
    private suspend fun limpiarDuplicadosSalidasYVales() {
        val now = System.currentTimeMillis()
        db.salidaDao().obtenerTodasSync()
            .filter { it.codigoSalida.isNotBlank() && !it.isDeleted }
            .groupBy { "${it.bodegaId}|${it.codigoSalida}" }
            .filter { it.value.size > 1 }
            .forEach { (_, grupo) ->
                val mantener = grupo.maxByOrNull { it.id } ?: return@forEach
                grupo.filter { it.id != mantener.id }.forEach { dup ->
                    db.salidaDao().softDelete(dup.id, now)
                }
            }

        db.DetalleValeDao().obtenerTodosSync()
            .groupBy { "${it.valeId}|${it.productoCodigo}|${it.codigoSalida}" }
            .filter { it.value.size > 1 }
            .forEach { (_, grupo) ->
                grupo.drop(1).forEach { dup ->
                    db.DetalleValeDao().eliminarDetalle(dup)
                }
            }

        db.ValeDao().obtenerTodosSync()
            .filter { it.codigoVale.isNotBlank() }
            .groupBy { "${it.bodegaId}|${it.codigoVale}" }
            .filter { it.value.size > 1 }
            .forEach { (_, grupo) ->
                val mantener = grupo.maxByOrNull { it.idVale } ?: return@forEach
                grupo.filter { it.idVale != mantener.idVale }.forEach { dup ->
                    db.DetalleValeDao().eliminarDetallesVale(dup.idVale)
                    db.ValeDao().eliminarVale(dup)
                }
                val total = db.DetalleValeDao().totalProductosVale(mantener.idVale)
                db.ValeDao().actualizarVale(mantener.copy(totalProductos = total))
            }
    }

    suspend fun subirDatosLocalesANube(): SyncResult = withContext(Dispatchers.IO) {
        if (!OfflineManager.isOnline(appContext)) {
            return@withContext SyncResult(true, "Sin conexión — pendientes en cola (${queue.pendingCount()})")
        }
        try {
            db.usuarioDao().obtenerTodosSync().forEach { u ->
                val ready = u.ensureUuid()
                if (ready.uuid != u.uuid) db.usuarioDao().actualizar(ready)
                firebase.guardarUsuario(ready)
            }
            db.bodegaDao().listarActivasSync().forEach { firebase.guardarBodega(it) }
            db.categoriaDao().obtenerTodosSync().forEach { firebase.guardarCategoria(it) }
            db.productoDao().obtenerTodosSync().forEach { firebase.guardarProducto(it, it.codigoBodega) }
            db.entradaDao().obtenerTodasSync().forEach { firebase.guardarEntrada(it) }
            db.salidaDao().obtenerTodasSync().forEach { firebase.guardarSalida(it) }
            db.facturaDao().obtenerTodasSync().forEach { firebase.guardarFactura(it) }
            db.detalleFacturaDao().obtenerTodosSync().forEach { detalle ->
                db.facturaDao().getFacturaById(detalle.facturaId)?.let { factura ->
                    firebase.guardarDetalleFactura(detalle, factura.codigoBodega, factura.bodegaId)
                }
            }
            db.auditoriaDao().obtenerTodasSync().forEach { firebase.guardarAuditoria(it, it.codigoBodega) }
            db.ValeDao().obtenerTodosSync().forEach { firebase.guardarVale(it) }
            db.DetalleValeDao().obtenerTodosSync().forEach { firebase.guardarDetalleVale(it, it.codigoBodega) }
            db.kardexDao().obtenerTodasSync().forEach { firebase.guardarKardex(it) }
            db.TrasladoDao().obtenerTodosSync().forEach { traslado ->
                firebase.guardarTraslado(traslado, traslado.codigoBodega)
            }
            db.presupuestoBodegaDao().obtenerTodosSync().forEach { presupuesto ->
                db.bodegaDao().obtenerBodegaPorId(presupuesto.bodegaId)?.let { bodega ->
                    firebase.guardarPresupuesto(presupuesto, bodega.codigoCorto)
                }
            }
            db.appNotificacionDao().obtenerTodosSync().forEach { n ->
                firebase.guardarNotificacion(n, n.usuario.ifBlank { SessionManager.usernameUsuario() })
            }
            SyncResult(true, "Datos enviados a Firebase")
        } catch (e: Exception) {
            Log.e("SYNC_UPLOAD", e.message ?: "Error", e)
            SyncResult(false, e.message ?: "Error sincronizando")
        }
    }

    data class SyncResult(val ok: Boolean, val mensaje: String)
}

private fun com.example.inventario.data.administracion.Usuario.ensureUuid() =
    if (uuid.isBlank()) copy(uuid = UUID.randomUUID().toString()) else this
