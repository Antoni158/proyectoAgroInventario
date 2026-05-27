package com.example.inventario.data.firebase

import android.content.Context
import com.example.inventario.data.administracion.Usuario
import com.example.inventario.data.bodega.Producto
import java.util.UUID

object SyncHelper {

    fun enqueueUsuario(context: Context, usuario: Usuario) {
        val key = usuario.uuid.ifBlank { usuario.username }
        OfflineManager.enqueueSync(
            context,
            SyncOperation(type = SyncEntityType.USUARIO, entityKey = key)
        )
    }

    fun enqueueProducto(context: Context, producto: Producto) {
        OfflineManager.enqueueSync(
            context,
            SyncOperation(
                type = SyncEntityType.PRODUCTO,
                entityKey = producto.codigo,
                bodegaId = producto.bodegaId,
                codigoBodega = producto.codigoBodega
            )
        )
    }

    fun newUuid(): String = UUID.randomUUID().toString()
}
