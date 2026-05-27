package com.example.inventario.data.repos

import android.util.Log
import com.example.inventario.data.Auditoria.Auditoria
import com.example.inventario.data.administracion.Usuario
import com.example.inventario.data.bodega.*
import com.example.inventario.data.notificacion.AppNotificacion
import com.example.inventario.util.CodigoGenerator
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {

    private val db = FirebaseDatabase.getInstance("https://inventarioagr-default-rtdb.firebaseio.com/")
    private val rootBodegas = db.getReference("bodegas")
    private val usuariosRef = db.getReference("usuarios")

    init {
        // Persistencia habilitada en InventarioApp.kt
        Log.i("FIREBASE_CONNECTED", "Firebase Repository initialized")
    }

    private fun bRef(codigo: String, id: String) = rootBodegas.child(codigo).child(id)

    private val bodegaDataKeys = setOf(
        "productos", "entradas", "salidas", "facturas", "kardex", "vales",
        "detalles_vales", "detalles_facturas", "auditorias", "traslados", "presupuestos"
    )

    private data class BodegaFirebaseRef(val codigoCorto: String, val bodegaId: String)

    private fun normalizarBodega(
        bodega: Bodega,
        codigoFallback: String,
        idFallback: String?
    ): Bodega {
        val codigo = bodega.codigoCorto.ifBlank { codigoFallback }
        val id = bodega.id.ifBlank { idFallback.orEmpty().ifBlank { codigo } }
        return bodega.copy(
            id = id,
            codigoCorto = codigo,
            isDeleted = bodega.isDeleted
        )
    }

    private fun parseBodegaSnapshot(
        snap: com.google.firebase.database.DataSnapshot,
        codigoFallback: String,
        idFallback: String?
    ): Bodega? {
        if (!snap.exists()) return null
        snap.getValue(Bodega::class.java)?.let {
            return normalizarBodega(it, codigoFallback, idFallback)
        }
        val nombre = snap.child("nombre").getValue(String::class.java)
            ?: snap.child("name").getValue(String::class.java)
            ?: ""
        if (nombre.isBlank() && idFallback.isNullOrBlank() && codigoFallback.isBlank()) return null
        val parsed = Bodega(
            id = snap.child("id").getValue(String::class.java).orEmpty(),
            nombre = nombre.ifBlank {
                CodigoGenerator.extraerBase(codigoFallback)?.replaceFirstChar { it.titlecase() }.orEmpty()
            },
            codigoCorto = snap.child("codigoCorto").getValue(String::class.java).orEmpty(),
            descripcion = snap.child("descripcion").getValue(String::class.java).orEmpty(),
            isDeleted = snap.child("isDeleted").getValue(Boolean::class.java)
                ?: snap.child("deleted").getValue(Boolean::class.java)
                ?: false,
            deletionDate = snap.child("deletionDate").getValue(Long::class.java)
        )
        return normalizarBodega(parsed, codigoFallback, idFallback)
    }

    private fun parseBodegasDesdeSnapshot(
        root: com.google.firebase.database.DataSnapshot
    ): List<Bodega> {
        val result = linkedMapOf<String, Bodega>()
        root.children.forEach { codigoNode ->
            val codigoKey = codigoNode.key.orEmpty()
            if (codigoKey.isBlank()) return@forEach

            parseBodegaSnapshot(codigoNode.child("info"), codigoKey, codigoKey)?.let {
                result[it.id] = it
            }
            parseBodegaSnapshot(codigoNode, codigoKey, codigoKey)?.let {
                result[it.id] = it
            }

            codigoNode.children.forEach { child ->
                val childKey = child.key.orEmpty()
                if (childKey.isBlank() || childKey in bodegaDataKeys) return@forEach
                parseBodegaSnapshot(child.child("info"), codigoKey, childKey)?.let {
                    result[it.id] = it
                }
                parseBodegaSnapshot(child, codigoKey, childKey)?.let {
                    result[it.id] = it
                }
                if (child.hasChild("productos") || child.hasChild("entradas") || child.hasChild("salidas")) {
                    val sintetica = Bodega(
                        id = childKey,
                        nombre = child.child("info").child("nombre").getValue(String::class.java)
                            ?: "Bodega $codigoKey",
                        codigoCorto = codigoKey,
                        descripcion = child.child("info").child("descripcion").getValue(String::class.java).orEmpty()
                    )
                    result[sintetica.id] = sintetica
                }
            }

            if (result.values.none { it.codigoCorto == codigoKey }) {
                val idHijo = codigoNode.children
                    .mapNotNull { it.key }
                    .firstOrNull { it !in bodegaDataKeys && it != "info" }
                val idFinal = idHijo ?: codigoKey
                result[idFinal] = Bodega(
                    id = idFinal,
                    nombre = codigoNode.child("nombre").getValue(String::class.java)
                        ?: codigoNode.child("info").child("nombre").getValue(String::class.java)
                        ?: CodigoGenerator.extraerBase(codigoKey)?.replaceFirstChar { it.titlecase() }.orEmpty(),
                    codigoCorto = codigoKey,
                    descripcion = codigoNode.child("descripcion").getValue(String::class.java)
                        ?: codigoNode.child("info").child("descripcion").getValue(String::class.java).orEmpty()
                )
            }
        }
        return result.values.filter { !it.isDeleted && it.id.isNotBlank() }.toList()
    }

    private suspend fun listarRefsBodega(): List<BodegaFirebaseRef> {
        val snap = snapshotServidor(rootBodegas)
        val refs = linkedSetOf<BodegaFirebaseRef>()
        parseBodegasDesdeSnapshot(snap).forEach { b ->
            refs.add(BodegaFirebaseRef(b.codigoCorto, b.id))
        }
        snap.children.forEach { codigoNode ->
            val codigo = codigoNode.key.orEmpty()
            codigoNode.children.forEach { child ->
                val key = child.key.orEmpty()
                when {
                    key in bodegaDataKeys -> refs.add(BodegaFirebaseRef(codigo, codigo))
                    key == "info" -> Unit
                    child.hasChild("productos") || child.hasChild("entradas") || child.hasChild("info") ->
                        refs.add(BodegaFirebaseRef(codigo, key))
                }
            }
        }
        return refs.toList()
    }

    fun bodegasRef() = rootBodegas

    private suspend fun snapshotServidor(ref: com.google.firebase.database.DatabaseReference): com.google.firebase.database.DataSnapshot {
        ref.database.goOnline()
        return ref.get().await()
    }

    fun parseCategoriaSnapshot(snap: com.google.firebase.database.DataSnapshot): Categoria? {
        if (!snap.exists()) return null
        val id = snap.key?.toIntOrNull()
            ?: snap.child("id").getValue(Int::class.java)
            ?: return null
        snap.getValue(Categoria::class.java)?.let { cat ->
            return cat.copy(
                id = if (cat.id == 0) id else cat.id,
                isDeleted = cat.isDeleted || snap.child("deleted").getValue(Boolean::class.java) == true
            )
        }
        val nombre = snap.child("nombre").getValue(String::class.java).orEmpty()
        val prefijo = snap.child("prefijo").getValue(String::class.java).orEmpty()
        return Categoria(
            id = id,
            nombre = nombre.ifBlank { prefijo.ifBlank { "Categoría $id" } },
            prefijo = prefijo,
            correlativoActual = snap.child("correlativoActual").getValue(Int::class.java) ?: 0,
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty(),
            descripcion = snap.child("descripcion").getValue(String::class.java).orEmpty(),
            area = snap.child("area").getValue(String::class.java).orEmpty(),
            colorHex = snap.child("colorHex").getValue(String::class.java) ?: "#2E7D32",
            icono = snap.child("icono").getValue(String::class.java) ?: "category",
            activa = snap.child("activa").getValue(Boolean::class.java) ?: true,
            sincronizado = true,
            fechaCreacion = snap.child("fechaCreacion").getValue(Long::class.java) ?: System.currentTimeMillis(),
            ultimaActualizacion = snap.child("ultimaActualizacion").getValue(Long::class.java)
                ?: snap.child("fechaCreacion").getValue(Long::class.java)
                ?: System.currentTimeMillis(),
            isDeleted = snap.child("isDeleted").getValue(Boolean::class.java)
                ?: snap.child("deleted").getValue(Boolean::class.java)
                ?: false,
            deletionDate = snap.child("deletionDate").getValue(Long::class.java)
        )
    }

    private fun parseProductoSnapshot(snap: com.google.firebase.database.DataSnapshot): Producto? {
        if (!snap.exists()) return null
        snap.getValue(Producto::class.java)?.let { return it }
        return Producto(
            id = snap.child("id").getValue(Int::class.java) ?: 0,
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty(),
            codigo = snap.child("codigo").getValue(String::class.java) ?: snap.key.orEmpty(),
            descripcion = snap.child("descripcion").getValue(String::class.java).orEmpty(),
            categoria = snap.child("categoria").getValue(String::class.java).orEmpty(),
            prefijoCategoria = snap.child("prefijoCategoria").getValue(String::class.java).orEmpty(),
            cantidad = snap.child("cantidad").getValue(Int::class.java) ?: 0,
            stockMinimo = snap.child("stockMinimo").getValue(Int::class.java) ?: 0,
            status = snap.child("status").getValue(String::class.java) ?: "ACTIVO",
            presupuesto = snap.child("presupuesto").getValue(Double::class.java) ?: 0.0,
            unidad = snap.child("unidad").getValue(String::class.java).orEmpty(),
            costo = snap.child("costo").getValue(Double::class.java) ?: 0.0,
            ultimoMovimiento = snap.child("ultimoMovimiento").getValue(Long::class.java),
            isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false
        )
    }

    private fun parseEntradaSnapshot(snap: com.google.firebase.database.DataSnapshot): Entrada? {
        if (!snap.exists()) return null
        snap.getValue(Entrada::class.java)?.let { return it.copy(id = snap.key?.toIntOrNull() ?: it.id) }
        return Entrada(
            id = snap.key?.toIntOrNull() ?: 0,
            codigoEntrada = snap.child("codigoEntrada").getValue(String::class.java).orEmpty(),
            codigoProducto = snap.child("codigoProducto").getValue(String::class.java).orEmpty(),
            descripcion = snap.child("descripcion").getValue(String::class.java).orEmpty(),
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty(),
            cantidad = snap.child("cantidad").getValue(Int::class.java) ?: 0,
            fechaIngreso = snap.child("fechaIngreso").getValue(String::class.java).orEmpty(),
            isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false
        )
    }

    private fun parseSalidaSnapshot(snap: com.google.firebase.database.DataSnapshot): Salida? {
        if (!snap.exists()) return null
        snap.getValue(Salida::class.java)?.let { return it.copy(id = snap.key?.toIntOrNull() ?: it.id) }
        return Salida(
            id = snap.key?.toIntOrNull() ?: 0,
            codigoSalida = snap.child("codigoSalida").getValue(String::class.java).orEmpty(),
            codigoProducto = snap.child("codigoProducto").getValue(String::class.java).orEmpty(),
            descripcion = snap.child("descripcion").getValue(String::class.java).orEmpty(),
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty(),
            cantidad = snap.child("cantidad").getValue(Int::class.java) ?: 0,
            fechaSalida = snap.child("fechaSalida").getValue(String::class.java).orEmpty(),
            isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false
        )
    }

    private fun parseFacturaSnapshot(snap: com.google.firebase.database.DataSnapshot): Factura? {
        if (!snap.exists()) return null
        snap.getValue(Factura::class.java)?.let { return it.copy(id = snap.key?.toIntOrNull() ?: it.id) }
        return Factura(
            id = snap.key?.toIntOrNull() ?: 0,
            numeroFactura = snap.child("numeroFactura").getValue(String::class.java).orEmpty(),
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseDetalleFacturaSnapshot(snap: com.google.firebase.database.DataSnapshot): DetalleFactura? {
        if (!snap.exists()) return null
        snap.getValue(DetalleFactura::class.java)?.let { return it.copy(idDetalle = snap.key?.toIntOrNull() ?: it.idDetalle) }
        return DetalleFactura(
            idDetalle = snap.key?.toIntOrNull() ?: 0,
            facturaId = snap.child("facturaId").getValue(Int::class.java) ?: 0,
            codigoProducto = snap.child("codigoProducto").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseValeSnapshot(snap: com.google.firebase.database.DataSnapshot): Vale? {
        if (!snap.exists()) return null
        snap.getValue(Vale::class.java)?.let { return it.copy(idVale = snap.key?.toIntOrNull() ?: it.idVale) }
        return Vale(
            idVale = snap.key?.toIntOrNull() ?: 0,
            codigoVale = snap.child("codigoVale").getValue(String::class.java).orEmpty(),
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseDetalleValeSnapshot(snap: com.google.firebase.database.DataSnapshot): DetalleVale? {
        if (!snap.exists()) return null
        snap.getValue(DetalleVale::class.java)?.let { return it.copy(idDetalle = snap.key?.toIntOrNull() ?: it.idDetalle) }
        return DetalleVale(
            idDetalle = snap.key?.toIntOrNull() ?: 0,
            valeId = snap.child("valeId").getValue(Int::class.java) ?: 0,
            productoCodigo = snap.child("productoCodigo").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseKardexSnapshot(snap: com.google.firebase.database.DataSnapshot): Kardex? {
        if (!snap.exists()) return null
        snap.getValue(Kardex::class.java)?.let { return it.copy(id = snap.key?.toIntOrNull() ?: it.id) }
        return Kardex(
            id = snap.key?.toIntOrNull() ?: 0,
            codigoProducto = snap.child("codigoProducto").getValue(String::class.java).orEmpty(),
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseAuditoriaSnapshot(snap: com.google.firebase.database.DataSnapshot): Auditoria? {
        if (!snap.exists()) return null
        snap.getValue(Auditoria::class.java)?.let { return it.copy(id = snap.key?.toIntOrNull() ?: it.id) }
        return Auditoria(
            id = snap.key?.toIntOrNull() ?: 0,
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseTrasladoSnapshot(snap: com.google.firebase.database.DataSnapshot): Traslado? {
        if (!snap.exists()) return null
        snap.getValue(Traslado::class.java)?.let { return it.copy(idTraslado = snap.key?.toIntOrNull() ?: it.idTraslado) }
        return Traslado(
            idTraslado = snap.key?.toIntOrNull() ?: 0,
            bodegaId = snap.child("bodegaId").getValue(String::class.java).orEmpty(),
            codigoBodega = snap.child("codigoBodega").getValue(String::class.java).orEmpty()
        )
    }

    private fun parsePresupuestoSnapshot(snap: com.google.firebase.database.DataSnapshot): PresupuestoBodega? {
        if (!snap.exists()) return null
        return snap.getValue(PresupuestoBodega::class.java)
    }

    private fun parseNotificacionSnapshot(snap: com.google.firebase.database.DataSnapshot): AppNotificacion? {
        if (!snap.exists()) return null
        snap.getValue(AppNotificacion::class.java)?.let { return it.copy(id = snap.key?.toIntOrNull() ?: it.id) }
        return null
    }

    /** Clave en Firebase: usuarios/{username} */
    private fun usuarioFirebaseKey(u: Usuario): String =
        u.username.trim().ifBlank { u.uuid.ifBlank { UUID.randomUUID().toString() } }

    private fun usuarioPayload(u: Usuario): Map<String, Any?> = mapOf(
        "id" to u.id,
        "uuid" to u.uuid.ifBlank { usuarioFirebaseKey(u) },
        "username" to u.username,
        "nombre" to u.nombre,
        "correo" to u.correo,
        "passwordHash" to u.password,
        "password" to u.password,
        "rol" to u.rol,
        "activo" to u.activo,
        "fotoPerfil" to u.fotoPerfil,
        "fechaCreacion" to u.fechaCreacion,
        "ultimoAcceso" to u.ultimoAcceso,
        "isDeleted" to u.isDeleted
    )

    private fun parseUsuario(snapshot: com.google.firebase.database.DataSnapshot): Usuario? {
        val uuid = snapshot.child("uuid").getValue(String::class.java)
            ?: snapshot.key?.takeIf { it.contains("-") }
            ?: snapshot.key.orEmpty()
        val username = snapshot.child("username").getValue(String::class.java)
            ?: snapshot.key.orEmpty()
        if (username.isBlank() && uuid.isBlank()) return null
        val pass = snapshot.child("passwordHash").getValue(String::class.java)
            ?: snapshot.child("password").getValue(String::class.java).orEmpty()
        return Usuario(
            id = snapshot.child("id").getValue(Int::class.java) ?: 0,
            uuid = uuid,
            username = username,
            nombre = snapshot.child("nombre").getValue(String::class.java).orEmpty(),
            correo = snapshot.child("correo").getValue(String::class.java).orEmpty(),
            password = pass,
            rol = snapshot.child("rol").getValue(String::class.java) ?: "BODEGA",
            activo = snapshot.child("activo").getValue(Boolean::class.java) ?: true,
            fotoPerfil = snapshot.child("fotoPerfil").getValue(String::class.java).orEmpty(),
            fechaCreacion = snapshot.child("fechaCreacion").getValue(Long::class.java)
                ?: System.currentTimeMillis(),
            ultimoAcceso = snapshot.child("ultimoAcceso").getValue(Long::class.java)
        )
    }

    // USUARIOS — estructura: usuarios/{username}
    suspend fun guardarUsuario(u: Usuario): Boolean = try {
        val withUuid = if (u.uuid.isBlank()) u.copy(uuid = UUID.randomUUID().toString()) else u
        val key = usuarioFirebaseKey(withUuid)
        usuariosRef.child(key).setValue(usuarioPayload(withUuid)).await()
        Log.i("FB_SAVE_USER", "Usuario guardado en usuarios/$key")
        true
    } catch (e: Exception) {
        Log.e("FB_SAVE_USER", e.message ?: "")
        false
    }

    suspend fun obtenerUsuarios(): List<Usuario> = try {
        val list = mutableListOf<Usuario>()
        usuariosRef.get().await().children.forEach { snap ->
            parseUsuario(snap)?.let { list.add(it) }
        }
        list.distinctBy { it.username.lowercase() }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerUsuario(username: String): Usuario? {
        return try {
            val key = username.trim()
            if (key.isBlank()) return null
            parseUsuario(usuariosRef.child(key).get().await())
                ?: usuariosRef.get().await().children.mapNotNull { parseUsuario(it) }
                    .find { it.username.equals(key, true) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario? = try {
        obtenerUsuarios().find { it.correo.equals(correo, true) }
    } catch (e: Exception) {
        null
    }

    // BODEGAS — estructura: bodegas/{codigo}/{id}/info (+ productos, entradas…)
    suspend fun guardarBodega(b: Bodega) = try {
        bRef(b.codigoCorto, b.id).child("info").setValue(b).await()
        Log.i("FB_SAVE_BODEGA", "Bodega ${b.codigoCorto}/${b.id} → ${b.nombre}")
        true
    } catch (e: Exception) {
        Log.e("FB_SAVE_BODEGA", e.message ?: "")
        false
    }

    suspend fun obtenerBodegas(): List<Bodega> = try {
        db.goOnline()
        val snap = snapshotServidor(rootBodegas)
        val parsed = parseBodegasDesdeSnapshot(snap)
        Log.i("FB_BODEGAS", "Descargadas ${parsed.size} bodega(s) · nodos=${snap.childrenCount}")
        parsed
    } catch (e: Exception) {
        Log.e("FB_BODEGAS", e.message ?: "", e)
        emptyList()
    }
    suspend fun eliminarBodega(codigo: String, id: String) = try { bRef(codigo, id).removeValue().await(); true } catch (e: Exception) { false }

    // PRODUCTOS
    suspend fun guardarProducto(p: Producto, codigoBodega: String) = try { bRef(codigoBodega, p.bodegaId).child("productos").child(p.codigo).setValue(p).await(); true } catch (e: Exception) { false }
    suspend fun obtenerProductos(codigoBodega: String, bId: String): List<Producto> = try {
        bRef(codigoBodega, bId).child("productos").get().await().children.mapNotNull {
            parseProductoSnapshot(it)
        }
    } catch (e: Exception) { emptyList() }
    suspend fun eliminarProducto(codigoBodega: String, bId: String, codigo: String) = try { bRef(codigoBodega, bId).child("productos").child(codigo).removeValue().await(); true } catch (e: Exception) { false }

    // ENTRADAS / SALIDAS
    suspend fun guardarEntrada(e: Entrada) = try { bRef(e.codigoBodega, e.bodegaId).child("entradas").child(e.id.toString()).setValue(e).await(); true } catch (e: Exception) { false }
    suspend fun obtenerEntradas(codigoBodega: String, bId: String): List<Entrada> = try {
        bRef(codigoBodega, bId).child("entradas").get().await().children.mapNotNull { child ->
            parseEntradaSnapshot(child)
        }
    } catch (e: Exception) { emptyList() }
    suspend fun eliminarEntrada(codigoBodega: String, bId: String, id: String) = try { bRef(codigoBodega, bId).child("entradas").child(id).removeValue().await(); true } catch (e: Exception) { false }

    suspend fun guardarSalida(s: Salida) = try { bRef(s.codigoBodega, s.bodegaId).child("salidas").child(s.id.toString()).setValue(s).await(); true } catch (e: Exception) { false }
    suspend fun obtenerSalidas(codigoBodega: String, bId: String): List<Salida> = try {
        bRef(codigoBodega, bId).child("salidas").get().await().children.mapNotNull { child ->
            parseSalidaSnapshot(child)
        }
    } catch (e: Exception) { emptyList() }
    suspend fun eliminarSalida(codigoBodega: String, bId: String, id: String) = try { bRef(codigoBodega, bId).child("salidas").child(id).removeValue().await(); true } catch (e: Exception) { false }

    // CATEGORIAS
    suspend fun guardarCategoria(c: Categoria) = try {
        val payload = c.copy(
            ultimaActualizacion = System.currentTimeMillis(),
            sincronizado = true
        )
        db.getReference("categorias").child(payload.id.toString()).setValue(payload).await()
        Log.i("FIREBASE_CATEGORIA", "OK id=${payload.id} nombre=${payload.nombre}")
        true
    } catch (e: Exception) {
        Log.e("FIREBASE_CATEGORIA", "Error: ${e.message}", e)
        false
    }
    suspend fun obtenerCategorias(): List<Categoria> = try {
        db.goOnline()
        val snap = snapshotServidor(db.getReference("categorias"))
        snap.children.mapNotNull { parseCategoriaSnapshot(it) }
            .filter { !it.isDeleted }
            .also { Log.i("FB_CATEGORIAS", "Descargadas ${it.size} categoría(s)") }
    } catch (e: Exception) {
        Log.e("FB_CATEGORIAS", e.message ?: "", e)
        emptyList()
    }
    suspend fun eliminarCategoria(id: Int) = try { db.getReference("categorias").child(id.toString()).removeValue().await(); true } catch (e: Exception) { false }

    fun categoriasRef() = db.getReference("categorias")
    fun productosRef(codigoBodega: String, bId: String) = bRef(codigoBodega, bId).child("productos")

    // VALES
    suspend fun guardarVale(v: Vale) = try { bRef(v.codigoBodega, v.bodegaId).child("vales").child(v.idVale.toString()).setValue(v).await(); true } catch (e: Exception) { false }
    suspend fun obtenerVales(codigoBodega: String, bId: String): List<Vale> = try {
        bRef(codigoBodega, bId).child("vales").get().await().children.mapNotNull { child ->
            parseValeSnapshot(child)
        }
    } catch (e: Exception) { emptyList() }

    suspend fun guardarDetalleVale(dv: DetalleVale, codigoBodega: String) = try { bRef(codigoBodega, dv.bodegaId).child("detalles_vales").child(dv.idDetalle.toString()).setValue(dv).await(); true } catch (e: Exception) { false }
    suspend fun obtenerDetallesVales(codigoBodega: String, bId: String): List<DetalleVale> = try {
        bRef(codigoBodega, bId).child("detalles_vales").get().await().children.mapNotNull { child ->
            parseDetalleValeSnapshot(child)
        }
    } catch (e: Exception) { emptyList() }

    // FACTURAS
    suspend fun guardarFactura(f: Factura) = try { bRef(f.codigoBodega, f.bodegaId).child("facturas").child(f.id.toString()).setValue(f).await(); true } catch (e: Exception) { false }
    suspend fun obtenerFacturas(codigoBodega: String, bId: String): List<Factura> = try { bRef(codigoBodega, bId).child("facturas").get().await().children.mapNotNull { parseFacturaSnapshot(it) } } catch (e: Exception) { emptyList() }
    suspend fun eliminarFactura(codigoBodega: String, bId: String, id: String) = try { bRef(codigoBodega, bId).child("facturas").child(id).removeValue().await(); true } catch (e: Exception) { false }

    suspend fun guardarDetalleFactura(df: DetalleFactura, codigoBodega: String, bId: String) = try { bRef(codigoBodega, bId).child("detalles_facturas").child(df.idDetalle.toString()).setValue(df).await(); true } catch (e: Exception) { false }
    suspend fun obtenerDetallesFacturas(codigoBodega: String, bId: String): List<DetalleFactura> = try { bRef(codigoBodega, bId).child("detalles_facturas").get().await().children.mapNotNull { parseDetalleFacturaSnapshot(it) } } catch (e: Exception) { emptyList() }

    // AUDITORIA
    suspend fun guardarAuditoria(a: Auditoria, codigoBodega: String) = try { bRef(codigoBodega, a.bodegaId).child("auditorias").child(a.id.toString()).setValue(a).await(); true } catch (e: Exception) { false }
    suspend fun obtenerAuditorias(codigoBodega: String, bId: String): List<Auditoria> = try { bRef(codigoBodega, bId).child("auditorias").get().await().children.mapNotNull { parseAuditoriaSnapshot(it) } } catch (e: Exception) { emptyList() }
    suspend fun obtenerTodasAuditorias(): List<Auditoria> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerAuditorias(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    // TRASLADOS
    suspend fun guardarTraslado(t: Traslado, codigoBodega: String) = try { bRef(codigoBodega, t.bodegaId).child("traslados").child(t.idTraslado.toString()).setValue(t).await(); true } catch (e: Exception) { false }
    suspend fun obtenerTraslados(codigoBodega: String, bId: String): List<Traslado> = try { bRef(codigoBodega, bId).child("traslados").get().await().children.mapNotNull { parseTrasladoSnapshot(it) } } catch (e: Exception) { emptyList() }
    suspend fun obtenerTodosTraslados(): List<Traslado> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerTraslados(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    // KARDEX
    suspend fun guardarKardex(k: Kardex) = try { bRef(k.codigoBodega, k.bodegaId).child("kardex").child(k.id.toString()).setValue(k).await(); true } catch (e: Exception) { false }
    suspend fun obtenerKardex(codigoBodega: String, bId: String): List<Kardex> = try { bRef(codigoBodega, bId).child("kardex").get().await().children.mapNotNull { parseKardexSnapshot(it) } } catch (e: Exception) { emptyList() }


    suspend fun obtenerTodosProductos(): List<Producto> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerProductos(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodasEntradas(): List<Entrada> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerEntradas(ref.codigoCorto, ref.bodegaId)
        }.distinctBy { e ->
            if (e.id > 0) "id:${e.id}" else "cod:${e.bodegaId}|${e.codigoEntrada}"
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodasSalidas(): List<Salida> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerSalidas(ref.codigoCorto, ref.bodegaId)
        }.distinctBy { s ->
            if (s.id > 0) "id:${s.id}" else "cod:${s.bodegaId}|${s.codigoSalida}"
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodasFacturas(): List<Factura> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerFacturas(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodosKardex(): List<Kardex> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerKardex(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodosVales(): List<Vale> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerVales(ref.codigoCorto, ref.bodegaId)
        }.distinctBy { v ->
            if (v.idVale > 0) "id:${v.idVale}" else "cod:${v.bodegaId}|${v.codigoVale}"
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodosDetallesVales(): List<DetalleVale> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerDetallesVales(ref.codigoCorto, ref.bodegaId)
        }.distinctBy { d ->
            if (d.idDetalle > 0) "id:${d.idDetalle}"
            else "line:${d.bodegaId}|${d.valeId}|${d.productoCodigo}|${d.codigoSalida}"
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodosDetallesFacturas(): List<DetalleFactura> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerDetallesFacturas(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    // PRESUPUESTOS
    private fun presupuestoFirebaseKey(p: PresupuestoBodega) =
        "${p.tipoPeriodo}_${p.anio}_${p.indicePeriodo}"

    suspend fun guardarPresupuesto(p: PresupuestoBodega, codigoBodega: String) = try {
        bRef(codigoBodega, p.bodegaId)
            .child("presupuestos")
            .child(presupuestoFirebaseKey(p))
            .setValue(p)
            .await()
        true
    } catch (e: Exception) {
        false
    }

    suspend fun obtenerPresupuestos(codigoBodega: String, bId: String): List<PresupuestoBodega> = try {
        bRef(codigoBodega, bId).child("presupuestos").get().await().children.mapNotNull {
            parsePresupuestoSnapshot(it)
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun obtenerTodosPresupuestos(): List<PresupuestoBodega> = try {
        listarRefsBodega().flatMap { ref ->
            obtenerPresupuestos(ref.codigoCorto, ref.bodegaId)
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun guardarNotificacion(n: AppNotificacion, username: String): Boolean = try {
        if (username.isBlank()) {
            false
        } else {
            usuariosRef.child(username.trim()).child("notificaciones").child(n.id.toString()).setValue(n).await()
            true
        }
    } catch (e: Exception) { false }

    suspend fun obtenerNotificacionesUsuario(username: String): List<AppNotificacion> = try {
        usuariosRef.child(username.trim()).child("notificaciones").get().await()
            .children.mapNotNull { parseNotificacionSnapshot(it) }
    } catch (e: Exception) { emptyList() }

    suspend fun obtenerTodasNotificaciones(): List<AppNotificacion> = try {
        usuariosRef.get().await().children.flatMap { userNode ->
            userNode.child("notificaciones").children.mapNotNull {
                parseNotificacionSnapshot(it)
            }
        }
    } catch (e: Exception) { emptyList() }
}
