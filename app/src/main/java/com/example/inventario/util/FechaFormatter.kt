package com.example.inventario.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formato humano único para toda la UI y exportaciones.
 * Ejemplo: 24/05/2026 04:45 PM
 */
object FechaFormatter {

    private val zone: ZoneId = ZoneId.systemDefault()

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.getDefault())

    private val formatterSoloFecha: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    fun formatear(valor: Any?, soloFecha: Boolean = false): String {
        if (valor == null) return "—"
        val fmt = if (soloFecha) formatterSoloFecha else formatter
        return when (valor) {
            is Long -> formatearEpoch(valor, fmt)
            is Int -> formatearEpoch(valor.toLong(), fmt)
            is Double -> formatearEpoch(valor.toLong(), fmt)
            is String -> formatearString(valor, fmt)
            is LocalDateTime -> valor.format(fmt)
            else -> valor.toString().takeIf { it.isNotBlank() } ?: "—"
        }
    }

    fun ahora(): String = LocalDateTime.now(zone).format(formatter)

    fun epochAhora(): Long = System.currentTimeMillis()

    private fun formatearString(texto: String, fmt: DateTimeFormatter): String {
        val t = texto.trim()
        if (t.isEmpty()) return "—"
        t.toLongOrNull()?.let { epoch ->
            if (epoch > 1_000_000_000L) return formatearEpoch(epoch, fmt)
        }
        if (t.matches(Regex("^\\d{10,13}$"))) {
            return formatearEpoch(t.toLong(), fmt)
        }
        return t
    }

    private fun formatearEpoch(epoch: Long, fmt: DateTimeFormatter): String {
        val millis = when {
            epoch <= 0L -> return "—"
            epoch < 1_000_000_000_000L -> epoch * 1000
            else -> epoch
        }
        return Instant.ofEpochMilli(millis).atZone(zone).format(fmt)
    }
}
