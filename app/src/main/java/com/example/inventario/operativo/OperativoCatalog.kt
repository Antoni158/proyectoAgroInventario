package com.example.inventario.operativo

/**
 * Catálogo operativo agrícola: áreas, destinos, vehículos, placas, campos.
 */
object OperativoCatalog {

    val AREAS = listOf(
        "mantenimiento",
        "campo",
        "taller",
        "bodega",
        "administración",
        "riego",
        "fumigación",
        "cosecha"
    )

    val TRACTORES = listOf(
        "tractor JD-01",
        "tractor JD-02",
        "tractor CASE-01",
        "tractor CASE-02",
        "tractor NH-01"
    )

    val VEHICULOS = listOf(
        "camión P123ABC",
        "camión P456XYZ",
        "camión P999AAA",
        "pickup P555DEF",
        "motocicleta M001"
    )

    val CAMPOS = listOf(
        "campo norte",
        "campo sur",
        "campo este",
        "lote 12",
        "sistema riego central",
        "invernadero A"
    )

    val DESTINOS = TRACTORES + VEHICULOS + CAMPOS + listOf(
        "fumigadora F-01",
        "bodega central",
        "taller mecánico",
        "oficina administrativa"
    )

    val RESPONSABLES = listOf(
        "Juan Pérez",
        "María López",
        "Carlos Méndez",
        "Operador Campo",
        "Mecánico Taller"
    )

    fun placasFromVehiculos(): List<String> =
        VEHICULOS.mapNotNull { v ->
            Regex("P[A-Z0-9]+").find(v.uppercase())?.value
        }.distinct()

    fun sugerencias(query: String, base: List<String>, limit: Int = 12): List<String> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return base.take(limit)
        return base.filter { it.lowercase().contains(q) }.take(limit)
    }

    fun mergeHistorico(query: String, historico: List<String>, base: List<String>): List<String> {
        val q = query.trim().lowercase()
        val fromHist = if (q.isEmpty()) historico else historico.filter { it.lowercase().contains(q) }
        val fromBase = sugerencias(query, base, 20)
        return (fromHist + fromBase).distinct().take(15)
    }
}
