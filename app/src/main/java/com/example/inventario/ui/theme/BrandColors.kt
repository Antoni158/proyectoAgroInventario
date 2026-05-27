package com.example.inventario.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Paleta oficial Inventario Agrícola. */
object BrandColors {
    val VerdeOscuro = Color(0xFF1B5E20)
    val VerdePrincipal = Color(0xFF2E7D32)
    val VerdeMedio = Color(0xFF43A047)
    val VerdeClaro = Color(0xFF66BB6A)
    val VerdeFondo = Color(0xFFF1F8E9)
    val VerdeSurface = Color(0xFFE8F5E9)
    val Blanco = Color(0xFFFFFFFF)
    val TextoOscuro = Color(0xFF1B3A1F)
    val Sombra = Color(0x1A1B5E20)

    val gradienteAgricola = Brush.verticalGradient(
        colors = listOf(VerdeMedio, VerdePrincipal, VerdeOscuro)
    )

    val gradienteSuave = Brush.verticalGradient(
        colors = listOf(VerdeFondo, VerdeSurface, Blanco)
    )

    val gradienteLogin = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1B5E20),
            Color(0xFF2E7D32),
            Color(0xFF388E3C),
            Color(0xFF1B5E20)
        )
    )
}
