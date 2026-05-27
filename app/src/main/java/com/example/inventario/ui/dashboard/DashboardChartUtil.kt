package com.example.inventario.ui.dashboard

object DashboardChartUtil {

    private const val PUNTOS = 5

    /** Últimos N movimientos en orden cronológico (izq → der). */
    fun <T> serieReciente(lista: List<T>, valor: (T) -> Float): List<Float> {
        if (lista.isEmpty()) return List(PUNTOS) { 0f }
        val recientes = lista.take(PUNTOS).map(valor).reversed()
        return rellenarSerie(recientes)
    }

    fun rellenarSerie(datos: List<Float>, puntos: Int = PUNTOS): List<Float> {
        if (datos.size >= puntos) return datos.takeLast(puntos)
        return List(puntos - datos.size) { 0f } + datos
    }

    /** Escala cada serie al rango 0..1 para que todas se vean en el gráfico. */
    fun normalizarSerie(datos: List<Float>): List<Float> {
        if (datos.isEmpty()) return datos
        val max = datos.maxOrNull() ?: 0f
        val min = datos.minOrNull() ?: 0f
        val rango = (max - min).coerceAtLeast(1f)
        return datos.map { ((it - min) / rango).coerceIn(0.05f, 1f) }
    }
}
