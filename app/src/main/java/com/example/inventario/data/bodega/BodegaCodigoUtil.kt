package com.example.inventario.data.bodega

import com.example.inventario.util.CodigoGenerator

object BodegaCodigoUtil {

    /** Código legible: zacapa000001, bodega000002 */
    fun generarCodigoCompleto(nombre: String, existentes: List<String>): String =
        CodigoGenerator.generarCodigoBodega(nombre, existentes)

    fun necesitaCodigoLegible(codigo: String): Boolean =
        codigo.isBlank() || CodigoGenerator.esCodigoLegacy(codigo)

    /** Nombre humano para UI — nunca muestra solo el código automático. */
    fun nombreParaMostrar(bodega: Bodega?): String {
        if (bodega == null) return "Bodega"
        val nombre = bodega.nombre.trim()
        if (nombre.isNotBlank() && !esNombreIgualAlCodigo(nombre, bodega.codigoCorto)) {
            return nombre
        }
        val base = CodigoGenerator.extraerBase(bodega.codigoCorto).orEmpty()
        if (base.isNotBlank()) {
            return base.replaceFirstChar { c -> c.titlecase() }
        }
        return nombre.ifBlank { "Bodega" }
    }

    fun esNombreIgualAlCodigo(nombre: String, codigo: String): Boolean {
        if (codigo.isBlank()) return false
        return nombre.equals(codigo, ignoreCase = true) ||
            CodigoGenerator.esCodigoNuevo(nombre.trim().lowercase())
    }

    fun tituloBienvenida(bodega: Bodega?): String {
        val nombre = nombreParaMostrar(bodega)
        return if (nombre.isNotBlank() && nombre != "Bodega") "Bienvenido a $nombre" else "Bienvenido al inventario"
    }

    fun subtituloVisible(bodega: Bodega?): String {
        if (bodega == null) return ""
        return buildString {
            val desc = bodega.descripcion.trim()
            if (desc.isNotBlank()) append(desc)
            if (bodega.codigoCorto.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(bodega.codigoCorto)
            }
        }
    }
}
