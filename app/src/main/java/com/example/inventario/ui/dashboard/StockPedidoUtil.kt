package com.example.inventario.ui.dashboard

import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.Salida

data class PrediccionStock(
    val codigo: String,
    val nombre: String,
    val diasRestantes: Int,
    val stockActual: Int,
    val stockMinimo: Int
)

object StockPedidoUtil {

    fun esStockBajo(producto: Producto): Boolean =
        producto.stockMinimo > 0 && producto.cantidad <= producto.stockMinimo

    /** Cantidad sugerida para llevar stock al doble del mínimo (o al menos 1 unidad). */
    fun cantidadSugeridaPedido(producto: Producto, multiplicador: Double = 2.0): Int {
        val min = producto.stockMinimo.coerceAtLeast(1)
        val objetivo = (min * multiplicador).toInt().coerceAtLeast(min)
        return (objetivo - producto.cantidad).coerceAtLeast(1)
    }

    fun textoEstadoStock(producto: Producto): String {
        val deficit = (producto.stockMinimo - producto.cantidad).coerceAtLeast(0)
        val sugerido = cantidadSugeridaPedido(producto)
        return if (deficit > 0) {
            "Actual: ${producto.cantidad} · Mín: ${producto.stockMinimo} · Faltan $deficit uds"
        } else {
            "Actual: ${producto.cantidad} · Mín: ${producto.stockMinimo} · En límite · Pedir $sugerido uds"
        }
    }

    fun calcularDiasRestantes(producto: Producto, salidas: List<Salida>): Int {
        val movimientos = salidas.filter { it.codigoProducto == producto.codigo }
        val promedioDiario = if (movimientos.isEmpty()) {
            1
        } else {
            val total = movimientos.sumOf { it.cantidad }
            (total / movimientos.size.coerceAtLeast(1)).coerceAtLeast(1)
        }
        return (producto.cantidad / promedioDiario).coerceAtLeast(1)
    }

    fun prediccionesDesde(
        productos: List<Producto>,
        salidas: List<Salida>
    ): List<PrediccionStock> =
        productos
            .filter { esStockBajo(it) }
            .sortedBy { it.cantidad }
            .map { p ->
                PrediccionStock(
                    codigo = p.codigo,
                    nombre = p.descripcion,
                    diasRestantes = calcularDiasRestantes(p, salidas),
                    stockActual = p.cantidad,
                    stockMinimo = p.stockMinimo
                )
            }
}
