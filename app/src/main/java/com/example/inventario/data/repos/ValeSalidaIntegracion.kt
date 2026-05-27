package com.example.inventario.data.repos

import com.example.inventario.data.bodega.DetalleVale
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.Vale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.inventario.util.CodigoGenerator

object ValeSalidaIntegracion {

    data class LineaVale(
        val productoCodigo: String,
        val productoDescripcion: String,
        val categoria: String,
        val cantidad: Int,
        val codigoSalida: String = "",
        val costoUnitario: Double = 0.0
    )

    suspend fun procesarSalidaEnVale(database: appdatabase, salida: Salida): String =
        withContext(Dispatchers.IO) {
            val codigo = procesarLineasEnValeInterno(
                database = database,
                bodegaId = salida.bodegaId,
                codigoVale = salida.numeroVale,
                responsable = salida.responsable,
                destino = salida.destino,
                fecha = salida.fechaSalida,
                observacion = salida.notas,
                usuario = salida.usuario,
                lineas = listOf(
                    LineaVale(
                        productoCodigo = salida.codigoProducto,
                        productoDescripcion = salida.descripcion,
                        categoria = salida.categoria,
                        cantidad = salida.cantidad,
                        codigoSalida = salida.codigoSalida,
                        costoUnitario = salida.costoUnitario
                    )
                )
            )
            if (salida.numeroVale.isBlank() && salida.id > 0) {
                database.salidaDao().update(salida.copy(numeroVale = codigo))
            }
            codigo
        }

    suspend fun procesarLineasEnVale(
        database: appdatabase,
        bodegaId: String,
        codigoVale: String,
        responsable: String,
        destino: String,
        fecha: String,
        observacion: String,
        usuario: String,
        lineas: List<LineaVale>
    ): String = withContext(Dispatchers.IO) {
        procesarLineasEnValeInterno(
            database, bodegaId, codigoVale, responsable, destino, fecha, observacion, usuario, lineas
        )
    }

    private suspend fun procesarLineasEnValeInterno(
        database: appdatabase,
        bodegaId: String,
        codigoVale: String,
        responsable: String,
        destino: String,
        fecha: String,
        observacion: String,
        usuario: String,
        lineas: List<LineaVale>
    ): String {
        if (lineas.isEmpty()) return codigoVale

        val valeDao = database.ValeDao()
        val detalleDao = database.DetalleValeDao()
        val firebase = FirebaseRepository()
        val bodega = database.bodegaDao().obtenerBodegaPorId(bodegaId)
        val codigoBodega = bodega?.codigoCorto.orEmpty()

        val codigoFinal = codigoVale.trim().ifBlank {
            val existentes = valeDao.listarCodigosVale(bodegaId)
            CodigoGenerator.generarCodigoTipo("vale", existentes)
        }

        var vale = valeDao.obtenerValePorCodigo(codigoFinal, bodegaId)
        if (vale == null) {
            val id = valeDao.insertarVale(
                Vale(
                    codigoVale = codigoFinal,
                    responsable = responsable,
                    destino = destino,
                    fecha = fecha,
                    observacion = observacion,
                    totalProductos = lineas.size,
                    bodegaId = bodegaId,
                    codigoBodega = codigoBodega,
                    estado = "CONFIRMADO",
                    usuario = usuario
                )
            ).toInt()
            vale = valeDao.obtenerValePorId(id)
        } else {
            valeDao.actualizarVale(
                vale.copy(
                    responsable = responsable.ifBlank { vale.responsable },
                    destino = destino.ifBlank { vale.destino },
                    observacion = observacion.ifBlank { vale.observacion },
                    codigoBodega = codigoBodega.ifBlank { vale.codigoBodega }
                )
            )
        }

        val valeId = vale?.idVale ?: return codigoFinal
        vale?.let { v ->
            firebase.guardarVale(v.copy(idVale = valeId, codigoBodega = codigoBodega))
        }

        lineas.forEach { linea ->
            val codigoSalidaLinea = linea.codigoSalida.trim()
            val duplicado = if (codigoSalidaLinea.isNotBlank()) {
                detalleDao.buscarDetalleExistente(valeId, linea.productoCodigo, codigoSalidaLinea)
            } else {
                detalleDao.obtenerDetallesDirecto(valeId).firstOrNull {
                    it.productoCodigo == linea.productoCodigo && it.codigoSalida.isBlank()
                }
            }
            if (duplicado != null) return@forEach

            val detalle = DetalleVale(
                valeId = valeId,
                productoCodigo = linea.productoCodigo,
                productoDescripcion = linea.productoDescripcion,
                categoria = linea.categoria,
                cantidad = linea.cantidad,
                bodegaId = bodegaId,
                codigoBodega = codigoBodega,
                codigoSalida = codigoSalidaLinea
            )
            detalleDao.insertarDetalle(detalle)
            val guardado = detalleDao.obtenerDetallesDirecto(valeId)
                .firstOrNull {
                    it.productoCodigo == linea.productoCodigo &&
                        it.codigoSalida == codigoSalidaLinea
                } ?: detalle
            firebase.guardarDetalleVale(guardado, codigoBodega)
        }

        val totalReal = detalleDao.totalProductosVale(valeId)
        valeDao.actualizarVale(
            (vale ?: return codigoFinal).copy(totalProductos = totalReal)
        )

        return codigoFinal
    }
}
