package com.example.inventario.data.firebase

import android.content.Context
import android.util.Log
import com.example.inventario.data.repos.CloudSyncManager
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SyncQueueManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val db = appdatabase.getDatabase(context)
    private val firebase = FirebaseRepository()

    fun enqueue(op: SyncOperation) {
        val queue = loadQueue()
        queue.removeAll { it.type == op.type && it.entityKey == op.entityKey }
        queue.add(op)
        saveQueue(queue)
    }

    suspend fun processPending(): Int = mutex.withLock {
        withContext(Dispatchers.IO) {
            val queue = loadQueue().toMutableList()
            if (queue.isEmpty()) return@withContext 0
            var ok = 0
            val failed = mutableListOf<SyncOperation>()
            queue.forEach { op ->
                val success = try {
                    execute(op)
                } catch (e: Exception) {
                    Log.e(TAG, "Sync falló ${op.type}: ${e.message}")
                    false
                }
                if (success) ok++ else failed.add(op.copy(retries = op.retries + 1))
            }
            saveQueue(failed.filter { it.retries < MAX_RETRIES })
            ok
        }
    }

    fun pendingCount(): Int = loadQueue().size

    private suspend fun execute(op: SyncOperation): Boolean = when (op.type) {
        SyncEntityType.USUARIO -> {
            val u = db.usuarioDao().obtenerUsuarioPorUuid(op.entityKey)
                ?: db.usuarioDao().obtenerUsuarioPorUsername(op.entityKey)
            u?.let { firebase.guardarUsuario(it) } ?: false
        }
        SyncEntityType.BODEGA -> db.bodegaDao().obtenerBodegaPorId(op.entityKey)?.let { firebase.guardarBodega(it) } ?: false
        SyncEntityType.CATEGORIA -> db.categoriaDao().obtenerCategoriaPorId(op.entityKey.toIntOrNull() ?: 0)
            ?.let { firebase.guardarCategoria(it) } ?: false
        SyncEntityType.PRODUCTO -> {
            val p = db.productoDao().obtenerProductoPorCodigo(op.entityKey, op.bodegaId)
            p?.let { firebase.guardarProducto(it, op.codigoBodega) } ?: false
        }
        SyncEntityType.ENTRADA -> db.entradaDao().getEntradaById(op.entityKey.toIntOrNull() ?: 0)
            ?.let { firebase.guardarEntrada(it) } ?: false
        SyncEntityType.SALIDA -> db.salidaDao().getSalidaById(op.entityKey.toIntOrNull() ?: 0)
            ?.let { firebase.guardarSalida(it) } ?: false
        SyncEntityType.FACTURA -> db.facturaDao().getFacturaById(op.entityKey.toIntOrNull() ?: 0)
            ?.let { firebase.guardarFactura(it) } ?: false
        SyncEntityType.KARDEX -> db.kardexDao().getKardexById(op.entityKey.toIntOrNull() ?: 0)
            ?.let { firebase.guardarKardex(it) } ?: false
        SyncEntityType.AUDITORIA, SyncEntityType.NOTIFICACION,
        SyncEntityType.DETALLE_FACTURA, SyncEntityType.VALE, SyncEntityType.TRASLADO -> true
        else -> true
    }

    private fun loadQueue(): MutableList<SyncOperation> {
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    SyncOperation(
                        type = SyncEntityType.valueOf(o.getString("type")),
                        entityKey = o.getString("entityKey"),
                        bodegaId = o.optString("bodegaId"),
                        codigoBodega = o.optString("codigoBodega"),
                        timestamp = o.optLong("timestamp"),
                        retries = o.optInt("retries")
                    )
                )
            }
        }.toMutableList()
    }

    private fun saveQueue(queue: List<SyncOperation>) {
        val arr = JSONArray()
        queue.forEach { op ->
            arr.put(
                JSONObject().apply {
                    put("type", op.type.name)
                    put("entityKey", op.entityKey)
                    put("bodegaId", op.bodegaId)
                    put("codigoBodega", op.codigoBodega)
                    put("timestamp", op.timestamp)
                    put("retries", op.retries)
                }
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    suspend fun syncAllPendingAndBidirectional(): CloudSyncManager.SyncResult {
        processPending()
        return CloudSyncManager(context).sincronizarBidireccional()
    }

    companion object {
        private const val PREFS = "sync_queue_prefs"
        private const val KEY = "queue"
        private const val MAX_RETRIES = 5
        private const val TAG = "SyncQueue"
    }
}
