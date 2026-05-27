package com.example.inventario.data.repos

import android.content.Context
import android.net.Uri
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.Producto
import com.example.inventario.util.CodigoGenerator
import com.example.inventario.util.FechaFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExcelImportManager(private val context: Context) {

    private val db = appdatabase.getDatabase(context)
    private val firebase = FirebaseRepository()
    private val repository = InventoryRepository(
        db.bodegaDao(),
        db.productoDao(),
        db.categoriaDao(),
        db.entradaDao(),
        db.salidaDao(),
        db.facturaDao(),
        firebase
    )
    private val movimientoService = MovimientoInventarioService(db, firebase)
    private val formatter = DataFormatter()

    data class ImportResult(
        val ok: Boolean,
        val mensaje: String,
        val creados: Int = 0,
        val actualizados: Int = 0,
        val omitidos: Int = 0,
        val errores: List<String> = emptyList()
    ) {
        val total: Int get() = creados + actualizados
    }

    suspend fun importarProductos(uri: Uri, bodegaId: String, codigoBodega: String): ImportResult =
        withContext(Dispatchers.IO) {
            if (bodegaId.isBlank()) {
                return@withContext ImportResult(false, "Seleccione una bodega activa")
            }
            val errores = mutableListOf<String>()
            var creados = 0
            var actualizados = 0
            var omitidos = 0
            try {
                val rows = leerFilas(uri)
                if (rows.isEmpty()) return@withContext ImportResult(false, "Archivo vacío")

                val headerIdx = detectarEncabezado(
                    rows,
                    "codigo", "código", "descripcion", "descripción", "producto", "nombre"
                )
                if (headerIdx < 0) {
                    return@withContext ImportResult(false, "No se encontraron columnas reconocibles (código, descripción, producto…)")
                }

                val headers = rows[headerIdx].map { it.lowercase().trim() }
                val codigosEnArchivo = mutableSetOf<String>()
                val categorias = db.categoriaDao().obtenerTodosSync()
                val productosBodega = db.productoDao().obtenerTodosSync().filter { it.bodegaId == bodegaId }

                rows.drop(headerIdx + 1).forEachIndexed { idx, fila ->
                    val filaNum = headerIdx + idx + 2
                    if (fila.all { it.isBlank() }) return@forEachIndexed

                    val map = headers.mapIndexed { i, h -> h to fila.getOrElse(i) { "" } }.toMap()
                    var descripcion = map.valorDescripcion().trim()
                    var codigo = map.valorCodigo().trim().uppercase()

                    if (descripcion.isBlank() && codigo.isBlank()) {
                        omitidos++
                        return@forEachIndexed
                    }

                    val existentePorCodigo = codigo.takeIf { it.isNotBlank() }
                        ?.let { db.productoDao().obtenerProductoPorCodigo(it, bodegaId) }
                    val existentePorDesc = if (descripcion.isNotBlank()) {
                        db.productoDao().obtenerPorDescripcionExacta(bodegaId, descripcion)
                    } else null

                    val existente = existentePorCodigo ?: existentePorDesc

                    if (codigo.isBlank()) {
                        if (existente != null) {
                            codigo = existente.codigo
                        } else if (descripcion.isNotBlank()) {
                            val pares = productosBodega.map {
                                it.codigo to it.categoria.ifBlank { it.descripcion }
                            }
                            codigo = CodigoGenerator.previewCodigoDescripcion(descripcion, pares)
                                .takeIf { it != "Escriba descripción…" } ?: "PRD0001"
                        } else {
                            errores.add("Fila $filaNum: falta código o descripción")
                            omitidos++
                            return@forEachIndexed
                        }
                    }

                    if (!codigosEnArchivo.add(codigo)) {
                        errores.add("Fila $filaNum: código duplicado '$codigo' en el archivo")
                        omitidos++
                        return@forEachIndexed
                    }

                    if (descripcion.isBlank()) descripcion = existente?.descripcion.orEmpty()
                    if (descripcion.isBlank()) {
                        errores.add("Fila $filaNum: descripción requerida para '$codigo'")
                        omitidos++
                        return@forEachIndexed
                    }

                    val cantidadNueva = map.valor("cantidad", "stock", "existencia", "qty").toIntOrNull()
                    val costo = map.valor("costo", "costo compra", "precio", "precio unitario").toDoubleOrNull()
                    val precioVenta = map.valor("precio venta", "precio_venta", "venta", "pvp").toDoubleOrNull()
                    val stockMin = map.valor("stock minimo", "stock mínimo", "stockminimo", "minimo").toIntOrNull() ?: 0
                    val categoriaNom = map.valor("categoria", "categoría", "familia", "grupo")
                    val cat = resolverCategoria(categoriaNom, categorias)

                    if (existente != null) {
                        val incremento = cantidadNueva?.coerceAtLeast(0) ?: 0
                        val cantidadFinal = when {
                            cantidadNueva != null -> existente.cantidad + incremento
                            else -> existente.cantidad
                        }
                        val actualizado = existente.copy(
                            descripcion = descripcion,
                            categoria = categoriaNom.ifBlank { existente.categoria },
                            prefijoCategoria = cat?.prefijo ?: existente.prefijoCategoria,
                            cantidad = cantidadFinal,
                            stockMinimo = if (stockMin > 0) stockMin else existente.stockMinimo,
                            costo = costo?.takeIf { it >= 0 } ?: existente.costo,
                            precioVenta = precioVenta?.takeIf { it >= 0 } ?: existente.precioVenta,
                            unidad = map.valor("unidad", "um", "medida").ifBlank { existente.unidad },
                            ubicacion = map.valor("ubicacion", "ubicación", "bodega", "estante").ifBlank { existente.ubicacion },
                            proveedor = map.valor("proveedor", "vendor").ifBlank { existente.proveedor },
                            presupuesto = map.valor("presupuesto").toDoubleOrNull()?.takeIf { it >= 0 } ?: existente.presupuesto,
                            lote = map.valor("lote", "batch").ifBlank { existente.lote },
                            notas = map.valor("notas", "observaciones", "comentario").ifBlank { existente.notas },
                            stockBajo = cantidadFinal <= (if (stockMin > 0) stockMin else existente.stockMinimo) && (if (stockMin > 0) stockMin else existente.stockMinimo) > 0
                        )
                        db.productoDao().actualizar(actualizado)
                        firebase.guardarProducto(actualizado, codigoBodega.ifBlank { actualizado.codigoBodega })
                        actualizados++
                    } else {
                        val cantidadFinal = cantidadNueva?.coerceAtLeast(0) ?: 0
                        val nuevo = Producto(
                            bodegaId = bodegaId,
                            codigoBodega = codigoBodega,
                            codigo = codigo,
                            descripcion = descripcion,
                            categoria = categoriaNom.ifBlank { cat?.nombre.orEmpty() },
                            prefijoCategoria = cat?.prefijo.orEmpty(),
                            cantidad = cantidadFinal,
                            stockMinimo = stockMin,
                            costo = costo ?: 0.0,
                            precioVenta = precioVenta ?: 0.0,
                            unidad = map.valor("unidad", "um").ifBlank { "Unidad" },
                            ubicacion = map.valor("ubicacion", "ubicación"),
                            proveedor = map.valor("proveedor"),
                            presupuesto = map.valor("presupuesto").toDoubleOrNull() ?: 0.0,
                            lote = map.valor("lote"),
                            notas = map.valor("notas", "observaciones"),
                            fechaIngreso = map.valor("fecha ingreso", "fecha").ifBlank { FechaFormatter.ahora() },
                            stockBajo = cantidadFinal <= stockMin && stockMin > 0
                        )
                        db.productoDao().insertar(nuevo)
                        firebase.guardarProducto(nuevo, codigoBodega)
                        creados++
                    }
                }

                resumenImport(creados, actualizados, omitidos, errores)
            } catch (e: Exception) {
                ImportResult(false, e.message ?: "Error importando productos", errores = listOf(e.message.orEmpty()))
            }
        }

    suspend fun importarEntradas(uri: Uri, bodegaId: String, codigoBodega: String): ImportResult =
        withContext(Dispatchers.IO) {
            if (bodegaId.isBlank()) return@withContext ImportResult(false, "Seleccione una bodega")
            val errores = mutableListOf<String>()
            var creados = 0
            var omitidos = 0
            try {
                val rows = leerFilas(uri)
                val headerIdx = detectarEncabezado(
                    rows, "codigo", "código", "fecha", "descripcion", "descripción", "producto", "cantidad"
                )
                if (headerIdx < 0) return@withContext ImportResult(false, "Encabezados no reconocidos en entradas")

                val headers = rows[headerIdx].map { it.lowercase().trim() }
                val codigoBod = codigoBodega.ifBlank {
                    db.bodegaDao().obtenerBodegaPorId(bodegaId)?.codigoCorto.orEmpty()
                }

                rows.drop(headerIdx + 1).forEachIndexed { idx, fila ->
                    val filaNum = headerIdx + idx + 2
                    if (fila.all { it.isBlank() }) return@forEachIndexed
                    val map = headers.mapIndexed { i, h -> h to fila.getOrElse(i) { "" } }.toMap()

                    val descripcion = map.valorDescripcion().trim()
                    var codigoProducto = map.valorCodigo().trim().uppercase()
                    val cantidad = map.valor("cantidad", "qty", "unidades").toIntOrNull() ?: 0

                    if (cantidad <= 0) {
                        omitidos++
                        return@forEachIndexed
                    }

                    if (codigoProducto.isBlank() && descripcion.isNotBlank()) {
                        codigoProducto = db.productoDao()
                            .obtenerPorDescripcionExacta(bodegaId, descripcion)?.codigo.orEmpty()
                    }
                    if (codigoProducto.isBlank()) {
                        errores.add("Fila $filaNum: indique código o descripción de producto")
                        omitidos++
                        return@forEachIndexed
                    }

                    val producto = db.productoDao().obtenerProductoPorCodigo(codigoProducto, bodegaId)
                    val costo = map.valor("costo", "costo entrada", "precio unitario").toDoubleOrNull()
                        ?: producto?.costo ?: 0.0
                    val precioVenta = map.valor("precio venta", "precio_venta", "venta", "pvp").toDoubleOrNull()
                        ?: producto?.precioVenta ?: 0.0

                    val entrada = Entrada(
                        fechaIngreso = map.valor("fecha", "fecha ingreso", "fecha entrada").ifBlank { FechaFormatter.ahora() },
                        codigoEntrada = map.valor("codigo entrada", "id entrada").ifBlank { "IMP-${System.currentTimeMillis()}-$filaNum" },
                        codigoProducto = codigoProducto,
                        descripcion = descripcion.ifBlank { producto?.descripcion.orEmpty() },
                        categoria = map.valor("categoria", "categoría").ifBlank { producto?.categoria.orEmpty() },
                        cantidad = cantidad,
                        unidad = map.valor("unidad").ifBlank { producto?.unidad.orEmpty() },
                        ubicacion = map.valor("ubicacion", "ubicación").ifBlank { producto?.ubicacion.orEmpty() },
                        proveedor = map.valor("proveedor", "vendor"),
                        costoEntrada = costo,
                        precioVenta = precioVenta,
                        stockMinimo = map.valor("stock minimo", "stock mínimo").toIntOrNull() ?: producto?.stockMinimo ?: 0,
                        numeroFactura = map.valor("factura", "numero factura", "nº factura"),
                        notas = map.valor("notas", "observaciones"),
                        bodegaId = bodegaId,
                        codigoBodega = codigoBod
                    )

                    val productoNuevo = if (producto == null && descripcion.isNotBlank()) {
                        val pares = db.productoDao().obtenerTodosSync()
                            .filter { it.bodegaId == bodegaId }
                            .map { it.codigo to it.categoria.ifBlank { it.descripcion } }
                        val codGen = if (CodigoGenerator.esCodigoValido(codigoProducto)) {
                            codigoProducto
                        } else {
                            CodigoGenerator.previewCodigoDescripcion(descripcion, pares)
                        }
                        Producto(
                            bodegaId = bodegaId,
                            codigoBodega = codigoBod,
                            codigo = codGen,
                            descripcion = descripcion,
                            categoria = entrada.categoria,
                            cantidad = 0,
                            costo = costo,
                            precioVenta = precioVenta,
                            unidad = entrada.unidad.ifBlank { "Unidad" }
                        )
                    } else null

                    when (val r = movimientoService.registrarEntrada(entrada.copy(codigoProducto = productoNuevo?.codigo ?: codigoProducto), productoNuevo)) {
                        is MovimientoInventarioService.ResultadoMovimiento.EntradaOk -> creados++
                        is MovimientoInventarioService.ResultadoMovimiento.Error -> {
                            errores.add("Fila $filaNum: ${r.mensaje}")
                            omitidos++
                        }
                        else -> omitidos++
                    }
                }

                resumenImport(creados, 0, omitidos, errores)
            } catch (e: Exception) {
                ImportResult(false, e.message ?: "Error importando entradas", errores = listOf(e.message.orEmpty()))
            }
        }

    suspend fun importarFacturas(uri: Uri, bodegaId: String, codigoBodega: String): ImportResult =
        withContext(Dispatchers.IO) {
            if (bodegaId.isBlank()) return@withContext ImportResult(false, "Seleccione una bodega")
            val errores = mutableListOf<String>()
            var creados = 0
            var actualizados = 0
            var omitidos = 0
            try {
                val rows = leerFilas(uri)
                val headerIdx = detectarEncabezado(
                    rows, "factura", "numero", "nº", "proveedor", "total", "fecha"
                )
                if (headerIdx < 0) return@withContext ImportResult(false, "Encabezados no reconocidos en facturas")

                val headers = rows[headerIdx].map { it.lowercase().trim() }
                val codigoBod = codigoBodega.ifBlank {
                    db.bodegaDao().obtenerBodegaPorId(bodegaId)?.codigoCorto.orEmpty()
                }

                rows.drop(headerIdx + 1).forEachIndexed { idx, fila ->
                    val filaNum = headerIdx + idx + 2
                    if (fila.all { it.isBlank() }) return@forEachIndexed
                    val map = headers.mapIndexed { i, h -> h to fila.getOrElse(i) { "" } }.toMap()

                    val numero = map.valor("factura", "numero factura", "nº factura", "numero", "nº", "no factura").trim()
                    if (numero.isBlank()) {
                        omitidos++
                        return@forEachIndexed
                    }

                    val cantidad = map.valor("cantidad", "qty").toIntOrNull() ?: 0
                    val precio = map.valor("precio", "precio unitario", "costo").toDoubleOrNull() ?: 0.0
                    val total = map.valor("total", "monto", "importe").toDoubleOrNull()
                        ?: (cantidad * precio).takeIf { it > 0 } ?: precio

                    val descripcion = map.valorDescripcion()
                    val codigoProd = map.valorCodigo()

                    val facturaExistente = db.facturaDao().obtenerTodasSync()
                        .firstOrNull { it.bodegaId == bodegaId && it.numeroFactura.equals(numero, true) }

                    val factura = Factura(
                        id = facturaExistente?.id ?: 0,
                        fecha = map.valor("fecha", "fecha factura").ifBlank { facturaExistente?.fecha ?: FechaFormatter.ahora() },
                        numeroFactura = numero,
                        proveedor = map.valor("proveedor", "vendor", "suplidor").ifBlank { facturaExistente?.proveedor.orEmpty() },
                        codigo = codigoProd.ifBlank { facturaExistente?.codigo.orEmpty() },
                        descripcion = descripcion.ifBlank { facturaExistente?.descripcion.orEmpty() },
                        categoria = map.valor("categoria", "categoría").ifBlank { facturaExistente?.categoria.orEmpty() },
                        cantidad = cantidad.takeIf { it > 0 } ?: facturaExistente?.cantidad ?: 0,
                        precioUnitario = precio.takeIf { it > 0 } ?: facturaExistente?.precioUnitario ?: 0.0,
                        costo = precio,
                        total = total,
                        presupuesto = total,
                        usuario = map.valor("usuario", "responsable"),
                        notas = map.valor("notas", "observaciones"),
                        bodegaId = bodegaId,
                        codigoBodega = codigoBod
                    )

                    if (facturaExistente != null) {
                        repository.updateFactura(factura)
                        actualizados++
                    } else {
                        repository.insertFactura(factura)
                        creados++
                    }
                }

                resumenImport(creados, actualizados, omitidos, errores)
            } catch (e: Exception) {
                ImportResult(false, e.message ?: "Error importando facturas", errores = listOf(e.message.orEmpty()))
            }
        }

    suspend fun importarCategorias(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val errores = mutableListOf<String>()
        var creados = 0
        var actualizados = 0
        try {
            val rows = leerFilas(uri)
            val headerIdx = detectarEncabezado(rows, "nombre", "prefijo", "categoria", "categoría")
            if (headerIdx < 0) return@withContext ImportResult(false, "No se encontró columna Nombre o Prefijo")

            val headers = rows[headerIdx].map { it.lowercase().trim() }
            val prefijosEnArchivo = mutableSetOf<String>()

            rows.drop(headerIdx + 1).forEachIndexed { idx, fila ->
                val filaNum = headerIdx + idx + 2
                if (fila.all { it.isBlank() }) return@forEachIndexed
                val map = headers.mapIndexed { i, h -> h to fila.getOrElse(i) { "" } }.toMap()
                val nombre = map.valor("nombre", "categoria", "categoría").trim()
                if (nombre.isBlank()) return@forEachIndexed

                val prefijo = map.valor("prefijo", "codigo", "código").uppercase().take(3).ifBlank {
                    nombre.take(3).uppercase()
                }
                if (!prefijosEnArchivo.add(prefijo)) {
                    errores.add("Fila $filaNum: prefijo duplicado '$prefijo'")
                    return@forEachIndexed
                }

                val existente = db.categoriaDao().buscarPorPrefijo(prefijo)
                    ?: db.categoriaDao().buscarCategoria(nombre)

                val categoria = Categoria(
                    id = existente?.id ?: 0,
                    nombre = nombre,
                    prefijo = prefijo,
                    descripcion = map.valor("descripcion", "descripción").ifBlank { existente?.descripcion.orEmpty() },
                    area = map.valor("area", "área").ifBlank { existente?.area.orEmpty() },
                    colorHex = map.valor("color", "colorhex").ifBlank { existente?.colorHex ?: "#2E7D32" },
                    icono = map.valor("icono").ifBlank { existente?.icono ?: "category" },
                    correlativoActual = map.valor("correlativo").toIntOrNull() ?: existente?.correlativoActual ?: 0
                )
                repository.insertCategoria(categoria)
                if (existente != null) actualizados++ else creados++
            }

            resumenImport(creados, actualizados, 0, errores)
        } catch (e: Exception) {
            ImportResult(false, e.message ?: "Error importando categorías")
        }
    }

    private suspend fun resumenImport(
        creados: Int,
        actualizados: Int,
        omitidos: Int,
        errores: List<String>
    ): ImportResult {
        if (creados + actualizados > 0) {
            CloudSyncManager(context).subirDatosLocalesANube()
        }
        val msg = buildString {
            append("$creados nuevos, $actualizados actualizados")
            if (omitidos > 0) append(", $omitidos omitidos")
            if (errores.isNotEmpty()) append(" · ${errores.size} advertencias")
        }
        return ImportResult(
            ok = creados + actualizados > 0 || errores.isEmpty(),
            mensaje = if (creados + actualizados > 0) msg else "Sin cambios: ${errores.firstOrNull() ?: "archivo vacío"}",
            creados = creados,
            actualizados = actualizados,
            omitidos = omitidos,
            errores = errores.take(20)
        )
    }

    private fun detectarEncabezado(filas: List<List<String>>, vararg claves: String): Int =
        filas.indexOfFirst { fila ->
            val joined = fila.joinToString(" ").lowercase()
            if (joined.contains("inventario agrícola") || joined.contains("inventario agricola")) return@indexOfFirst false
            fila.any { celda ->
                val c = celda.lowercase().trim()
                claves.any { clave -> c.contains(clave, ignoreCase = true) }
            }
        }

    private fun resolverCategoria(nombre: String, categorias: List<Categoria>): Categoria? {
        if (nombre.isBlank()) return null
        return categorias.firstOrNull { it.nombre.equals(nombre, true) }
            ?: categorias.firstOrNull { it.prefijo.equals(nombre, true) }
    }

    private fun leerFilas(uri: Uri): List<List<String>> {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val workbook = XSSFWorkbook(input)
            val sheet = workbook.getSheetAt(0)
            val filas = mutableListOf<List<String>>()
            for (row in sheet) {
                val celdas = (0 until row.lastCellNum.coerceAtLeast(0)).map { i ->
                    formatter.formatCellValue(row.getCell(i)).trim()
                }
                if (celdas.any { it.isNotBlank() }) filas.add(celdas)
            }
            workbook.close()
            return filas
        }
        return emptyList()
    }

    private fun Map<String, String>.valorDescripcion(): String = valor(
        "descripcion", "descripción", "producto", "nombre", "nombre producto",
        "articulo", "artículo", "item", "material", "aproducto", "a producto", "nombre del producto"
    )

    private fun Map<String, String>.valorCodigo(): String = valor(
        "codigo", "código", "cod producto", "cod. producto", "sku", "codigo producto", "código producto", "id producto"
    )

    private fun Map<String, String>.valor(vararg keys: String): String {
        keys.forEach { key ->
            entries.firstOrNull { (h, v) ->
                h.contains(key, ignoreCase = true) && v.isNotBlank()
            }?.value?.let { return it }
        }
        return ""
    }
}
