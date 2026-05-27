package com.example.inventario.data.repos

import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.Traslado
import com.example.inventario.data.bodega.Bodega

class MovimientoInventarioService(
    private val db: appdatabase,
    private val firebase: FirebaseRepository = FirebaseRepository()
) {

    sealed class ResultadoMovimiento {
        data class EntradaOk(val entrada: Entrada, val factura: Factura) : ResultadoMovimiento()
        data class SalidaOk(val salida: Salida) : ResultadoMovimiento()
        data class TrasladoOk(val traslado: Traslado) : ResultadoMovimiento()
        data class AjusteOk(val auditoriaId: Int, val kardex: Kardex) : ResultadoMovimiento()
        data class Error(val mensaje: String) : ResultadoMovimiento()
    }

    suspend fun registrarEntrada(
        entrada: Entrada,
        productoNuevo: Producto? = null
    ): ResultadoMovimiento {
        var producto = db.productoDao().obtenerProductoPorCodigo(entrada.codigoProducto, entrada.bodegaId)

        if (producto == null) {
            val nuevo = productoNuevo ?: return ResultadoMovimiento.Error(
                "Producto ${entrada.codigoProducto.ifBlank { "(sin código)" }} no encontrado"
            )
            if (nuevo.descripcion.isBlank() || nuevo.categoria.isBlank() || nuevo.codigo.isBlank()) {
                return ResultadoMovimiento.Error("Datos incompletos para crear producto")
            }
            val id = db.productoDao().insertar(nuevo.copy(cantidad = 0, status = "SIN_STOCK")).toInt()
            producto = nuevo.copy(id = id, cantidad = 0, status = "SIN_STOCK")
            firebase.guardarProducto(producto, producto.codigoBodega)
        }

        val stockAnterior = producto.cantidad
        val stockNuevo = stockAnterior + entrada.cantidad

        // Costo Promedio Automático: (stockActual * costoActual + nuevaCantidad * nuevoCosto) / (stockActual + nuevaCantidad)
        val costoActual = producto.costo
        val nuevoCostoPromedio = if (stockNuevo > 0) {
            ((stockAnterior * costoActual) + (entrada.cantidad * entrada.costoEntrada)) / stockNuevo
        } else {
            entrada.costoEntrada
        }

        val entradaPrep = entrada.copy(
            status = calcularStatus(stockNuevo, producto.stockMinimo),
            presupuesto = entrada.cantidad * entrada.costoEntrada
        )

        db.productoDao().actualizar(productoActualizado(producto, stockNuevo, nuevoCostoPromedio))

        val idEntrada = db.entradaDao().insert(entradaPrep).toInt()
        val numeroFactura = entradaPrep.numeroFactura.trim().ifBlank { "FAC-ENT-$idEntrada" }
        val guardada = entradaPrep.copy(id = idEntrada, numeroFactura = numeroFactura)
        db.entradaDao().update(guardada)

        val factura = Factura(
            numeroFactura = numeroFactura,
            fecha = guardada.fechaIngreso,
            proveedor = guardada.proveedor,
            codigo = guardada.codigoProducto,
            descripcion = guardada.descripcion,
            categoria = guardada.categoria,
            cantidad = guardada.cantidad,
            precioUnitario = guardada.costoEntrada,
            costo = guardada.costoEntrada,
            total = guardada.cantidad * guardada.costoEntrada,
            presupuesto = guardada.presupuesto,
            bodegaId = guardada.bodegaId,
            codigoBodega = producto.codigoBodega,
            usuario = guardada.usuario,
            notas = "Auto desde entrada #$idEntrada"
        ).let { f ->
            f.copy(id = db.facturaDao().insert(f).toInt())
        }

        val detalle = DetalleFactura(
            facturaId = factura.id,
            codigoProducto = guardada.codigoProducto,
            descripcion = guardada.descripcion,
            categoria = guardada.categoria,
            cantidad = guardada.cantidad,
            precioUnitario = guardada.costoEntrada,
            subtotal = guardada.cantidad * guardada.costoEntrada
        )
        db.detalleFacturaDao().insertarDetalle(detalle)
        val detalleGuardado = db.detalleFacturaDao().obtenerDetallesDirecto(factura.id).firstOrNull() ?: detalle

        val kardex = Kardex(
            codigoMovimiento = "KAR-ENT-$idEntrada",
            codigoProducto = guardada.codigoProducto,
            descripcion = guardada.descripcion,
            bodegaId = guardada.bodegaId,
            codigoBodega = producto.codigoBodega,
            categoria = guardada.categoria,
            tipoMovimiento = "ENTRADA",
            cantidad = guardada.cantidad,
            saldoAnterior = stockAnterior,
            saldoNuevo = stockNuevo,
            costoUnitario = guardada.costoEntrada,
            totalMovimiento = guardada.cantidad * guardada.costoEntrada,
            numeroFactura = numeroFactura,
            usuario = guardada.usuario,
            lote = guardada.lote,
            fechaMovimiento = guardada.fechaIngreso,
            notas = guardada.notas
        ).let { k ->
            k.copy(id = db.kardexDao().insert(k).toInt())
        }

        firebase.guardarEntrada(guardada)
        firebase.guardarFactura(factura)
        firebase.guardarDetalleFactura(detalleGuardado, producto.codigoBodega, guardada.bodegaId)
        firebase.guardarKardex(kardex)
        db.productoDao().obtenerProductoPorCodigo(guardada.codigoProducto, guardada.bodegaId)?.let {
            firebase.guardarProducto(it, it.codigoBodega)
        }

        return ResultadoMovimiento.EntradaOk(guardada, factura)
    }

    suspend fun registrarSalida(salida: Salida): ResultadoMovimiento {
        val producto = db.productoDao().obtenerProductoPorCodigo(salida.codigoProducto, salida.bodegaId)
            ?: return ResultadoMovimiento.Error("Producto ${salida.codigoProducto} no encontrado")

        if (salida.cantidad > producto.cantidad) {
            return ResultadoMovimiento.Error(
                "Stock insuficiente. Disponible: ${producto.cantidad}, solicitado: ${salida.cantidad}"
            )
        }

        val stockAnterior = producto.cantidad
        val stockNuevo = stockAnterior - salida.cantidad
        val total = salida.total.takeIf { it > 0 } ?: salida.cantidad * salida.costoUnitario
        val salidaPrep = salida.copy(
            total = total,
            status = calcularStatus(stockNuevo, producto.stockMinimo)
        )

        db.productoDao().actualizar(productoActualizado(producto, stockNuevo))

        val id = db.salidaDao().insert(salidaPrep).toInt()
        val guardada = salidaPrep.copy(
            id = id,
            codigoBodega = producto.codigoBodega.ifBlank { salida.codigoBodega }
        )

        val kardex = Kardex(
            codigoMovimiento = "KAR-SAL-$id",
            codigoProducto = guardada.codigoProducto,
            descripcion = guardada.descripcion,
            bodegaId = guardada.bodegaId,
            codigoBodega = producto.codigoBodega,
            categoria = guardada.categoria,
            tipoMovimiento = "SALIDA",
            cantidad = guardada.cantidad,
            saldoAnterior = stockAnterior,
            saldoNuevo = stockNuevo,
            costoUnitario = guardada.costoUnitario,
            totalMovimiento = total,
            numeroVale = guardada.numeroVale,
            usuario = guardada.usuario,
            destino = guardada.destino,
            lote = guardada.lote,
            fechaMovimiento = guardada.fechaSalida,
            notas = guardada.notas
        ).let { k ->
            k.copy(id = db.kardexDao().insert(k).toInt())
        }

        firebase.guardarSalida(guardada)
        firebase.guardarKardex(kardex)
        db.productoDao().obtenerProductoPorCodigo(guardada.codigoProducto, guardada.bodegaId)?.let {
            firebase.guardarProducto(it, it.codigoBodega)
        }

        return ResultadoMovimiento.SalidaOk(guardada)
    }

    suspend fun registrarTraslado(traslado: Traslado, usuario: String = ""): ResultadoMovimiento {
        if (traslado.cantidad <= 0) return ResultadoMovimiento.Error("Cantidad inválida")

        val origenId = resolverBodega(traslado.bodegaOrigen.ifBlank { traslado.bodegaId })
            ?: return ResultadoMovimiento.Error("Bodega origen no encontrada")
        val destinoId = resolverBodega(traslado.bodegaDestino)
            ?: return ResultadoMovimiento.Error("Bodega destino no encontrada")
        if (origenId == destinoId) return ResultadoMovimiento.Error("Origen y destino deben ser distintos")

        val bodegaOrigen = db.bodegaDao().obtenerBodegaPorId(origenId)
            ?: return ResultadoMovimiento.Error("Bodega origen no existe")
        val bodegaDestino = db.bodegaDao().obtenerBodegaPorId(destinoId)
            ?: return ResultadoMovimiento.Error("Bodega destino no existe")

        val productoOrigen = db.productoDao().obtenerProductoPorCodigo(traslado.productoCodigo, origenId)
            ?: return ResultadoMovimiento.Error("Producto no encontrado en bodega origen")
        if (traslado.cantidad > productoOrigen.cantidad) {
            return ResultadoMovimiento.Error(
                "Stock insuficiente. Disponible: ${productoOrigen.cantidad}"
            )
        }

        val stockOrigenAnterior = productoOrigen.cantidad
        val stockOrigenNuevo = stockOrigenAnterior - traslado.cantidad
        db.productoDao().actualizar(productoActualizado(productoOrigen, stockOrigenNuevo))

        var productoDestino = db.productoDao().obtenerProductoPorCodigo(traslado.productoCodigo, destinoId)
        if (productoDestino == null) {
            val nuevo = productoOrigen.copy(
                id = 0,
                bodegaId = destinoId,
                codigoBodega = bodegaDestino.codigoCorto,
                cantidad = 0,
                presupuesto = 0.0,
                stockBajo = true,
                status = "ACTIVO"
            )
            val id = db.productoDao().insertar(nuevo).toInt()
            productoDestino = nuevo.copy(id = id)
        }

        val stockDestinoAnterior = productoDestino.cantidad
        val stockDestinoNuevo = stockDestinoAnterior + traslado.cantidad
        db.productoDao().actualizar(productoActualizado(productoDestino, stockDestinoNuevo))

        val codigoTraslado = traslado.codigoTraslado.trim().ifBlank { "TRA-${System.currentTimeMillis()}" }
        val guardado = traslado.copy(
            codigoTraslado = codigoTraslado,
            bodegaId = origenId,
            codigoBodega = bodegaOrigen.codigoCorto,
            bodegaOrigen = etiquetaBodega(bodegaOrigen),
            bodegaDestino = etiquetaBodega(bodegaDestino),
            productoDescripcion = traslado.productoDescripcion.ifBlank { productoOrigen.descripcion },
            categoria = traslado.categoria.ifBlank { productoOrigen.categoria }
        )
        db.TrasladoDao().insertarTraslado(guardado)
        val trasladoFinal = db.TrasladoDao().obtenerTrasladoPorCodigo(codigoTraslado) ?: guardado

        val kardexOrigen = Kardex(
            codigoMovimiento = "KAR-TRA-OUT-${trasladoFinal.idTraslado}",
            codigoProducto = traslado.productoCodigo,
            descripcion = trasladoFinal.productoDescripcion,
            bodegaId = origenId,
            codigoBodega = bodegaOrigen.codigoCorto,
            categoria = trasladoFinal.categoria,
            tipoMovimiento = "TRASLADO_SALIDA",
            cantidad = traslado.cantidad,
            saldoAnterior = stockOrigenAnterior,
            saldoNuevo = stockOrigenNuevo,
            costoUnitario = productoOrigen.costo,
            totalMovimiento = traslado.cantidad * productoOrigen.costo,
            usuario = usuario.ifBlank { traslado.responsable },
            destino = etiquetaBodega(bodegaDestino),
            fechaMovimiento = traslado.fecha,
            notas = traslado.observacion
        ).let { k -> k.copy(id = db.kardexDao().insert(k).toInt()) }

        val kardexDestino = Kardex(
            codigoMovimiento = "KAR-TRA-IN-${trasladoFinal.idTraslado}",
            codigoProducto = traslado.productoCodigo,
            descripcion = trasladoFinal.productoDescripcion,
            bodegaId = destinoId,
            codigoBodega = bodegaDestino.codigoCorto,
            categoria = trasladoFinal.categoria,
            tipoMovimiento = "TRASLADO_ENTRADA",
            cantidad = traslado.cantidad,
            saldoAnterior = stockDestinoAnterior,
            saldoNuevo = stockDestinoNuevo,
            costoUnitario = productoOrigen.costo,
            totalMovimiento = traslado.cantidad * productoOrigen.costo,
            usuario = usuario.ifBlank { traslado.responsable },
            destino = etiquetaBodega(bodegaOrigen),
            fechaMovimiento = traslado.fecha,
            notas = traslado.observacion
        ).let { k -> k.copy(id = db.kardexDao().insert(k).toInt()) }

        firebase.guardarTraslado(trasladoFinal, bodegaOrigen.codigoCorto)
        firebase.guardarKardex(kardexOrigen)
        firebase.guardarKardex(kardexDestino)
        db.productoDao().obtenerProductoPorCodigo(traslado.productoCodigo, origenId)?.let {
            firebase.guardarProducto(it, it.codigoBodega)
        }
        db.productoDao().obtenerProductoPorCodigo(traslado.productoCodigo, destinoId)?.let {
            firebase.guardarProducto(it, it.codigoBodega)
        }

        return ResultadoMovimiento.TrasladoOk(trasladoFinal)
    }

    suspend fun registrarAjusteAuditoria(
        producto: Producto,
        stockFisico: Double,
        auditoriaId: Int,
        usuario: String,
        observacion: String = ""
    ): ResultadoMovimiento {
        val stockAnterior = producto.cantidad
        val stockNuevo = stockFisico.toInt().coerceAtLeast(0)
        if (stockNuevo == stockAnterior) {
            db.auditoriaDao().marcarAjustado(auditoriaId)
            return ResultadoMovimiento.Error("Sin diferencia de stock")
        }

        db.productoDao().actualizar(productoActualizado(producto, stockNuevo))

        val diferencia = kotlin.math.abs(stockNuevo - stockAnterior)
        val tipo = if (stockNuevo > stockAnterior) "AJUSTE_ENTRADA" else "AJUSTE_SALIDA"
        val kardex = Kardex(
            codigoMovimiento = "KAR-AJU-$auditoriaId",
            codigoProducto = producto.codigo,
            descripcion = producto.descripcion,
            bodegaId = producto.bodegaId,
            codigoBodega = producto.codigoBodega,
            categoria = producto.categoria,
            tipoMovimiento = tipo,
            cantidad = diferencia,
            saldoAnterior = stockAnterior,
            saldoNuevo = stockNuevo,
            costoUnitario = producto.costo,
            totalMovimiento = diferencia * producto.costo,
            usuario = usuario,
            fechaMovimiento = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(java.util.Date()),
            notas = "Ajuste auditoría #$auditoriaId${if (observacion.isNotBlank()) ": $observacion" else ""}"
        ).let { k -> k.copy(id = db.kardexDao().insert(k).toInt()) }

        db.auditoriaDao().marcarAjustado(auditoriaId)
        firebase.guardarKardex(kardex)
        db.productoDao().obtenerProductoPorCodigo(producto.codigo, producto.bodegaId)?.let {
            firebase.guardarProducto(it, it.codigoBodega)
        }

        return ResultadoMovimiento.AjusteOk(auditoriaId, kardex)
    }

    private suspend fun resolverBodega(valor: String): String? {
        if (valor.isBlank()) return null
        db.bodegaDao().obtenerBodegaPorId(valor)?.let { return it.id }
        return db.bodegaDao().listarActivasSync().firstOrNull { b ->
            b.id.equals(valor, ignoreCase = true) ||
                b.nombre.equals(valor, ignoreCase = true) ||
                b.codigoCorto.equals(valor, ignoreCase = true)
        }?.id
    }

    private fun etiquetaBodega(bodega: Bodega): String =
        bodega.nombre.ifBlank { bodega.codigoCorto }.ifBlank { bodega.id }

    private fun productoActualizado(producto: Producto, nuevaCantidad: Int, nuevoCosto: Double? = null): Producto {
        val status = calcularStatus(nuevaCantidad, producto.stockMinimo)
        val costoFinal = nuevoCosto ?: producto.costo
        return producto.copy(
            cantidad = nuevaCantidad,
            costo = costoFinal,
            status = status,
            stockBajo = nuevaCantidad <= producto.stockMinimo,
            presupuesto = nuevaCantidad * costoFinal,
            ultimoMovimiento = System.currentTimeMillis()
        )
    }

    private fun calcularStatus(cantidad: Int, stockMinimo: Int): String = when {
        cantidad <= 0 -> "SIN_STOCK"
        cantidad <= stockMinimo -> "STOCK_BAJO"
        else -> "ACTIVO"
    }
}
