package com.example.inventario.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Legacy — usar InventarioTheme en AppColores.kt
private val LegacyDark = darkColorScheme(
    primary = VerdePrincipal,
    secondary = VerdeSuave,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onBackground = Color(0xFFE8F5E9),
    onSurface = Color(0xFFE8F5E9)
)

private val LegacyLight = lightColorScheme(
    primary = VerdePrincipal,
    secondary = VerdeSuave,
    background = VerdeFondo,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1B3A1F),
    onSurface = Color(0xFF1B3A1F)
)

@Deprecated("Usar InventarioTheme de AppColores.kt")
@Composable
fun LegacyInventarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LegacyDark
        else -> LegacyLight
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
