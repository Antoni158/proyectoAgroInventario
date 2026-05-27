package com.example.inventario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.inventario.viewModel.AppThemeState

private val VerdeLight = lightColorScheme(
    primary = BrandColors.VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = BrandColors.VerdeSurface,
    onPrimaryContainer = BrandColors.TextoOscuro,
    secondary = BrandColors.VerdeMedio,
    onSecondary = Color.White,
    secondaryContainer = BrandColors.VerdeClaro,
    onSecondaryContainer = BrandColors.TextoOscuro,
    tertiary = BrandColors.VerdeOscuro,
    onTertiary = Color.White,
    background = BrandColors.VerdeFondo,
    onBackground = BrandColors.TextoOscuro,
    surface = Color.White,
    onSurface = BrandColors.TextoOscuro,
    surfaceVariant = BrandColors.VerdeSurface,
    onSurfaceVariant = BrandColors.VerdeOscuro,
    outline = BrandColors.VerdeClaro,
    error = Color(0xFFB00020),
    onError = Color.White
)

private val VerdeDark = darkColorScheme(
    primary = BrandColors.VerdeClaro,
    onPrimary = BrandColors.TextoOscuro,
    primaryContainer = BrandColors.VerdeOscuro,
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = BrandColors.VerdeMedio,
    onSecondary = Color.White,
    background = Color(0xFF0D1F12),
    onBackground = Color(0xFFE8F5E9),
    surface = Color(0xFF1A2E1E),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF243528),
    onSurfaceVariant = Color(0xFFB9D4BC),
    outline = BrandColors.VerdeMedio,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val AzulLight = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF42A5F5),
    onSecondary = Color(0xFF0D47A1),
    background = Color(0xFFE3F2FD),
    onBackground = Color(0xFF0D1B2A),
    surface = Color.White,
    onSurface = Color(0xFF0D1B2A),
    surfaceVariant = Color(0xFFBBDEFB),
    onSurfaceVariant = Color(0xFF1565C0)
)

private val MoradoLight = lightColorScheme(
    primary = Color(0xFF7B2FF7),
    onPrimary = Color.White,
    secondary = Color(0xFFB06CFF),
    onSecondary = Color(0xFF4A148C),
    background = Color(0xFFF3E5F5),
    onBackground = Color(0xFF1A1025),
    surface = Color.White,
    onSurface = Color(0xFF1A1025),
    surfaceVariant = Color(0xFFE1BEE7),
    onSurfaceVariant = Color(0xFF6A1B9A)
)

private val NaranjaLight = lightColorScheme(
    primary = Color(0xFFE65100),
    onPrimary = Color.White,
    secondary = Color(0xFFFF9800),
    onSecondary = Color(0xFF3E2723),
    background = Color(0xFFFFF3E0),
    onBackground = Color(0xFF2E1500),
    surface = Color.White,
    onSurface = Color(0xFF2E1500),
    surfaceVariant = Color(0xFFFFE0B2),
    onSurfaceVariant = Color(0xFFBF360C)
)

private val NaranjaDark = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF2E1500),
    secondary = Color(0xFFFF9800),
    background = Color(0xFF1A1008),
    onBackground = Color(0xFFFFE0B2),
    surface = Color(0xFF261508),
    onSurface = Color(0xFFFFE0B2),
    surfaceVariant = Color(0xFF3E2720),
    onSurfaceVariant = Color(0xFFFFCC80)
)

private val AzulDark = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D1B2A),
    background = Color(0xFF0A1628),
    onBackground = Color(0xFFE3F2FD),
    surface = Color(0xFF132238),
    onSurface = Color(0xFFE3F2FD),
    surfaceVariant = Color(0xFF1E3A5F),
    onSurfaceVariant = Color(0xFFBBDEFB)
)

private val MoradoDark = darkColorScheme(
    primary = Color(0xFFCE93D8),
    onPrimary = Color(0xFF1A1025),
    background = Color(0xFF120818),
    onBackground = Color(0xFFF3E5F5),
    surface = Color(0xFF1E1028),
    onSurface = Color(0xFFF3E5F5),
    surfaceVariant = Color(0xFF2D1B3D),
    onSurfaceVariant = Color(0xFFE1BEE7)
)

private val OscuroDark = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0A0E14),
    secondary = Color(0xFF78909C),
    onSecondary = Color.White,
    background = Color(0xFF0A0E14),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF141820),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF1E2430),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF37474F)
)

private val RojoDark = darkColorScheme(
    primary = Color(0xFFEF9A9A),
    onPrimary = Color(0xFF2B0A0A),
    background = Color(0xFF1A0808),
    onBackground = Color(0xFFFFEBEE),
    surface = Color(0xFF261010),
    onSurface = Color(0xFFFFEBEE),
    surfaceVariant = Color(0xFF3E1515),
    onSurfaceVariant = Color(0xFFFFCDD2)
)

private val RojoLight = lightColorScheme(
    primary = Color(0xFFD32F2F),
    onPrimary = Color.White,
    secondary = Color(0xFFFF5252),
    onSecondary = Color(0xFF7F0000),
    background = Color(0xFFFFEBEE),
    onBackground = Color(0xFF2B0A0A),
    surface = Color.White,
    onSurface = Color(0xFF2B0A0A),
    surfaceVariant = Color(0xFFFFCDD2),
    onSurfaceVariant = Color(0xFFB71C1C)
)

@Composable
fun InventarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val temaActual by AppThemeState.tema.collectAsState()

    val colorScheme = when {
        darkTheme || temaActual == "oscuro" -> when (temaActual) {
            "azul" -> AzulDark
            "morado" -> MoradoDark
            "naranja" -> NaranjaDark
            "rojo" -> RojoDark
            "oscuro" -> OscuroDark
            else -> VerdeDark
        }
        temaActual == "azul" -> AzulLight
        temaActual == "morado" -> MoradoLight
        temaActual == "rojo" -> RojoLight
        temaActual == "naranja" -> NaranjaLight
        else -> VerdeLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
