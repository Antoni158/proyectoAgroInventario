package com.example.inventario.security

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.inventario.navigation.NavRoutes

// módulos menú principal
enum class MainModule(
    val title: String,
    val subtitle: String,
    val route: String,
    val icon: ImageVector,
    val requiredPermission: AppPermission
) {
    BODEGA(
        title = "Bodega",
        subtitle = "Inventario y operaciones",
        route = NavRoutes.MENU_BODEGAS,
        icon = Icons.Default.Apartment,
        requiredPermission = AppPermission.VER_MODULO_BODEGA
    ),
    PANEL(
        title = "Panel",
        subtitle = "KPIs, gráficas y analytics",
        route = NavRoutes.PANEL,
        icon = Icons.Default.Assessment,
        requiredPermission = AppPermission.VER_MODULO_PANEL
    ),
    AUDITORIA(
        title = "Auditoría",
        subtitle = "Supervisión y conteos",
        route = NavRoutes.AUDITORIA_HUB,
        icon = Icons.Default.Security,
        requiredPermission = AppPermission.VER_MODULO_AUDITORIA
    )
}
