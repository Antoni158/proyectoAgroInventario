package com.example.inventario.data.repos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.Auditoria.Auditoria
import com.example.inventario.data.administracion.Usuario
import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.Salida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class BackupManager(private val context: Context) {

    private val db = appdatabase.getDatabase(context)

    data class BackupResult(val ok: Boolean, val mensaje: String, val file: File? = null)

    suspend fun exportarJson(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 30)
            root.put("exportedAt", System.currentTimeMillis())
            root.put("usuarios", JSONArray(db.usuarioDao().obtenerTodosSync().map { usuarioJson(it) }))
            root.put("bodegas", JSONArray(db.bodegaDao().listarActivasSync().map { bodegaJson(it) }))
            root.put("categorias", JSONArray(db.categoriaDao().obtenerTodosSync().map { categoriaJson(it) }))
            root.put("productos", JSONArray(db.productoDao().obtenerTodosSync().map { productoJson(it) }))
            root.put("entradas", JSONArray(db.entradaDao().obtenerTodasSync().map { entradaJson(it) }))
            root.put("salidas", JSONArray(db.salidaDao().obtenerTodasSync().map { salidaJson(it) }))
            root.put("facturas", JSONArray(db.facturaDao().obtenerTodasSync().map { facturaJson(it) }))
            root.put("detallesFactura", JSONArray(db.detalleFacturaDao().obtenerTodosSync().map { detalleFacturaJson(it) }))
            root.put("kardex", JSONArray(db.kardexDao().obtenerTodasSync().map { kardexJson(it) }))
            root.put("auditorias", JSONArray(db.auditoriaDao().obtenerTodasSync().map { auditoriaJson(it) }))

            val file = File(context.cacheDir, "backup_inventario_${System.currentTimeMillis()}.json")
            file.writeText(root.toString(2))
            BackupResult(true, "Backup exportado (${file.length() / 1024} KB)", file)
        } catch (e: Exception) {
            BackupResult(false, e.message ?: "Error exportando backup")
        }
    }

    suspend fun importarJson(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            } ?: return@withContext BackupResult(false, "No se pudo leer el archivo")

            val root = JSONObject(json)
            if (!root.has("version")) return@withContext BackupResult(false, "Formato de backup inválido")

            root.optJSONArray("usuarios")?.let { arr ->
                for (i in 0 until arr.length()) db.usuarioDao().insertar(parseUsuario(arr.getJSONObject(i)))
            }
            root.optJSONArray("bodegas")?.let { arr ->
                for (i in 0 until arr.length()) db.bodegaDao().insertar(parseBodega(arr.getJSONObject(i)))
            }
            root.optJSONArray("categorias")?.let { arr ->
                for (i in 0 until arr.length()) db.categoriaDao().insertar(parseCategoria(arr.getJSONObject(i)))
            }
            root.optJSONArray("productos")?.let { arr ->
                for (i in 0 until arr.length()) db.productoDao().insertar(parseProducto(arr.getJSONObject(i)))
            }
            root.optJSONArray("entradas")?.let { arr ->
                for (i in 0 until arr.length()) db.entradaDao().insert(parseEntrada(arr.getJSONObject(i)))
            }
            root.optJSONArray("salidas")?.let { arr ->
                for (i in 0 until arr.length()) db.salidaDao().insert(parseSalida(arr.getJSONObject(i)))
            }
            root.optJSONArray("facturas")?.let { arr ->
                for (i in 0 until arr.length()) db.facturaDao().insert(parseFactura(arr.getJSONObject(i)))
            }
            root.optJSONArray("detallesFactura")?.let { arr ->
                for (i in 0 until arr.length()) db.detalleFacturaDao().insertarDetalle(parseDetalleFactura(arr.getJSONObject(i)))
            }
            root.optJSONArray("kardex")?.let { arr ->
                for (i in 0 until arr.length()) db.kardexDao().insert(parseKardex(arr.getJSONObject(i)))
            }
            root.optJSONArray("auditorias")?.let { arr ->
                for (i in 0 until arr.length()) db.auditoriaDao().insertar(parseAuditoria(arr.getJSONObject(i)))
            }

            CloudSyncManager(context).subirDatosLocalesANube()
            BackupResult(true, "Backup restaurado y enviado a Firebase")
        } catch (e: Exception) {
            BackupResult(false, e.message ?: "Error importando backup")
        }
    }

    suspend fun exportarSqlite(): BackupResult = withContext(Dispatchers.IO) {
        try {
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            val dbPath = context.getDatabasePath("inventario_db")
            if (!dbPath.exists()) return@withContext BackupResult(false, "Base de datos no encontrada")
            val ts = System.currentTimeMillis()
            val backupFile = File(context.cacheDir, "inventario_db_$ts.db")
            dbPath.copyTo(backupFile, overwrite = true)
            listOf("-wal", "-shm").forEach { ext ->
                val extra = File(dbPath.parent, "${dbPath.name}$ext")
                if (extra.exists()) extra.copyTo(File(context.cacheDir, "inventario_db_$ts$ext"), overwrite = true)
            }
            BackupResult(true, "Backup SQLite (${backupFile.length() / 1024} KB)", backupFile)
        } catch (e: Exception) {
            BackupResult(false, e.message ?: "Error exportando SQLite")
        }
    }

    suspend fun restaurarSqlite(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val dest = context.getDatabasePath("inventario_db")
            dest.parentFile?.mkdirs()
            appdatabase.closeDatabase()
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return@withContext BackupResult(false, "No se pudo leer el archivo SQLite")
            listOf("-wal", "-shm").forEach { ext ->
                File(dest.parent, "${dest.name}$ext").delete()
            }
            appdatabase.getDatabase(context)
            CloudSyncManager(context).subirDatosLocalesANube()
            BackupResult(true, "Base SQLite restaurada y sincronizada")
        } catch (e: Exception) {
            appdatabase.getDatabase(context)
            BackupResult(false, e.message ?: "Error restaurando SQLite")
        }
    }

    fun compartirSqlite(file: File) {
        compartirArchivo(file, "application/x-sqlite3", "Compartir backup SQLite")
    }

    private fun compartirArchivo(file: File, mimeType: String, titulo: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, titulo))
    }

    fun compartirBackup(file: File) {
        compartirArchivo(file, "application/json", "Compartir backup")
    }

    fun mostrarToast(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun usuarioJson(u: Usuario) = JSONObject().apply {
        put("id", u.id); put("uuid", u.uuid); put("username", u.username); put("nombre", u.nombre)
        put("correo", u.correo); put("password", u.password); put("rol", u.rol)
        put("fotoPerfil", u.fotoPerfil); put("activo", u.activo)
    }

    private fun parseUsuario(o: JSONObject) = Usuario(
        id = o.optInt("id"),
        uuid = o.optString("uuid"),
        username = o.optString("username"),
        nombre = o.optString("nombre"),
        correo = o.optString("correo"),
        password = o.optString("password"),
        rol = o.optString("rol"),
        fotoPerfil = o.optString("fotoPerfil"),
        activo = o.optBoolean("activo", true)
    )

    private fun bodegaJson(b: Bodega) = JSONObject().apply {
        put("id", b.id); put("nombre", b.nombre); put("codigoCorto", b.codigoCorto)
        put("descripcion", b.descripcion); put("isDeleted", b.isDeleted)
    }

    private fun parseBodega(o: JSONObject) = Bodega(
        id = o.optString("id"),
        nombre = o.optString("nombre"),
        codigoCorto = o.optString("codigoCorto"),
        descripcion = o.optString("descripcion"),
        isDeleted = o.optBoolean("isDeleted")
    )

    private fun categoriaJson(c: Categoria) = JSONObject().apply {
        put("id", c.id); put("nombre", c.nombre); put("prefijo", c.prefijo)
        put("descripcion", c.descripcion); put("area", c.area)
        put("colorHex", c.colorHex); put("icono", c.icono)
        put("correlativoActual", c.correlativoActual)
    }

    private fun parseCategoria(o: JSONObject) = Categoria(
        id = o.optInt("id"),
        nombre = o.optString("nombre"),
        prefijo = o.optString("prefijo"),
        descripcion = o.optString("descripcion"),
        area = o.optString("area"),
        colorHex = o.optString("colorHex"),
        icono = o.optString("icono"),
        correlativoActual = o.optInt("correlativoActual")
    )

    private fun productoJson(p: Producto) = JSONObject().apply {
        put("id", p.id); put("bodegaId", p.bodegaId); put("codigoBodega", p.codigoBodega)
        put("codigo", p.codigo); put("descripcion", p.descripcion); put("categoria", p.categoria)
        put("prefijoCategoria", p.prefijoCategoria); put("cantidad", p.cantidad)
        put("stockMinimo", p.stockMinimo); put("costo", p.costo); put("presupuesto", p.presupuesto)
        put("unidad", p.unidad); put("ubicacion", p.ubicacion); put("proveedor", p.proveedor)
        put("status", p.status); put("stockBajo", p.stockBajo); put("lote", p.lote)
        put("notas", p.notas); put("fechaIngreso", p.fechaIngreso); put("fechaVencimiento", p.fechaVencimiento)
        put("centroCosto", p.centroCosto); put("activo", p.activo)
    }

    private fun parseProducto(o: JSONObject) = Producto(
        id = o.optInt("id"),
        bodegaId = o.optString("bodegaId"),
        codigoBodega = o.optString("codigoBodega"),
        codigo = o.optString("codigo"),
        descripcion = o.optString("descripcion"),
        categoria = o.optString("categoria"),
        prefijoCategoria = o.optString("prefijoCategoria"),
        cantidad = o.optInt("cantidad"),
        stockMinimo = o.optInt("stockMinimo"),
        costo = o.optDouble("costo"),
        presupuesto = o.optDouble("presupuesto"),
        unidad = o.optString("unidad"),
        ubicacion = o.optString("ubicacion"),
        proveedor = o.optString("proveedor"),
        status = o.optString("status", "ACTIVO"),
        stockBajo = o.optBoolean("stockBajo"),
        lote = o.optString("lote"),
        notas = o.optString("notas"),
        fechaIngreso = o.optString("fechaIngreso"),
        fechaVencimiento = o.optString("fechaVencimiento"),
        centroCosto = o.optString("centroCosto"),
        activo = o.optBoolean("activo", true)
    )

    private fun entradaJson(e: Entrada) = JSONObject().apply {
        put("id", e.id); put("codigoProducto", e.codigoProducto); put("bodegaId", e.bodegaId)
        put("cantidad", e.cantidad); put("costoEntrada", e.costoEntrada); put("proveedor", e.proveedor)
    }

    private fun parseEntrada(o: JSONObject) = Entrada(
        id = o.optInt("id"),
        codigoProducto = o.optString("codigoProducto"),
        bodegaId = o.optString("bodegaId"),
        cantidad = o.optInt("cantidad"),
        costoEntrada = o.optDouble("costoEntrada"),
        proveedor = o.optString("proveedor")
    )

    private fun salidaJson(s: Salida) = JSONObject().apply {
        put("id", s.id); put("codigoProducto", s.codigoProducto); put("bodegaId", s.bodegaId)
        put("cantidad", s.cantidad); put("costoUnitario", s.costoUnitario)
    }

    private fun parseSalida(o: JSONObject) = Salida(
        id = o.optInt("id"),
        codigoProducto = o.optString("codigoProducto"),
        bodegaId = o.optString("bodegaId"),
        cantidad = o.optInt("cantidad"),
        costoUnitario = o.optDouble("costoUnitario")
    )

    private fun facturaJson(f: Factura) = JSONObject().apply {
        put("id", f.id); put("numeroFactura", f.numeroFactura); put("bodegaId", f.bodegaId)
        put("codigo", f.codigo); put("cantidad", f.cantidad); put("total", f.total)
    }

    private fun parseFactura(o: JSONObject) = Factura(
        id = o.optInt("id"),
        numeroFactura = o.optString("numeroFactura"),
        bodegaId = o.optString("bodegaId"),
        codigo = o.optString("codigo"),
        cantidad = o.optInt("cantidad"),
        total = o.optDouble("total")
    )

    private fun detalleFacturaJson(d: DetalleFactura) = JSONObject().apply {
        put("idDetalle", d.idDetalle); put("facturaId", d.facturaId)
        put("codigoProducto", d.codigoProducto); put("cantidad", d.cantidad)
        put("subtotal", d.subtotal)
    }

    private fun parseDetalleFactura(o: JSONObject) = DetalleFactura(
        idDetalle = o.optInt("idDetalle"),
        facturaId = o.optInt("facturaId"),
        codigoProducto = o.optString("codigoProducto"),
        cantidad = o.optInt("cantidad"),
        subtotal = o.optDouble("subtotal")
    )

    private fun kardexJson(k: Kardex) = JSONObject().apply {
        put("id", k.id); put("codigoProducto", k.codigoProducto); put("descripcion", k.descripcion)
        put("bodegaId", k.bodegaId); put("codigoBodega", k.codigoBodega)
        put("tipoMovimiento", k.tipoMovimiento); put("cantidad", k.cantidad)
        put("saldoAnterior", k.saldoAnterior); put("saldoNuevo", k.saldoNuevo)
        put("costoUnitario", k.costoUnitario); put("totalMovimiento", k.totalMovimiento)
        put("fechaMovimiento", k.fechaMovimiento); put("usuario", k.usuario)
        put("numeroFactura", k.numeroFactura); put("numeroVale", k.numeroVale)
        put("destino", k.destino); put("notas", k.notas)
    }

    private fun parseKardex(o: JSONObject) = Kardex(
        id = o.optInt("id"),
        codigoProducto = o.optString("codigoProducto"),
        descripcion = o.optString("descripcion"),
        bodegaId = o.optString("bodegaId"),
        codigoBodega = o.optString("codigoBodega"),
        tipoMovimiento = o.optString("tipoMovimiento"),
        cantidad = o.optInt("cantidad"),
        saldoAnterior = o.optInt("saldoAnterior"),
        saldoNuevo = o.optInt("saldoNuevo"),
        costoUnitario = o.optDouble("costoUnitario"),
        totalMovimiento = o.optDouble("totalMovimiento"),
        fechaMovimiento = o.optString("fechaMovimiento"),
        usuario = o.optString("usuario"),
        numeroFactura = o.optString("numeroFactura"),
        numeroVale = o.optString("numeroVale"),
        destino = o.optString("destino"),
        notas = o.optString("notas")
    )

    private fun auditoriaJson(a: Auditoria) = JSONObject().apply {
        put("id", a.id); put("productoId", a.productoId); put("codigo", a.codigo)
        put("bodegaId", a.bodegaId); put("stockSistema", a.stockSistema)
        put("stockFisico", a.stockFisico); put("diferencia", a.diferencia)
        put("estado", a.estado); put("ajusteAplicado", a.ajusteAplicado)
    }

    private fun parseAuditoria(o: JSONObject) = Auditoria(
        id = o.optInt("id"),
        productoId = o.optInt("productoId"),
        codigo = o.optString("codigo"),
        bodegaId = o.optString("bodegaId"),
        stockSistema = o.optDouble("stockSistema"),
        stockFisico = o.optDouble("stockFisico"),
        diferencia = o.optDouble("diferencia"),
        estado = o.optString("estado"),
        ajusteAplicado = o.optBoolean("ajusteAplicado")
    )
}
