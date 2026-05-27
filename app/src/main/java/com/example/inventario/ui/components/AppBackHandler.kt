package com.example.inventario.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.inventario.viewModel.SessionManager

/**
 * Destinos protegidos: no se debe volver atrás hacia login con el botón regresar.
 */
private val protectedPreviousRoutes = setOf("login")

/**
 * Rutas raíz tras login; el back físico no sale de la app en estas pantallas.
 */
private val rootRoutes = setOf("menuP", "login")

@Composable
fun AppBackHandler(
    navController: NavController,
    bodegaId: String? = null,
    enabled: Boolean = true
) {
    if (!enabled) return

    BackHandler {
        navController.navigateBackSafely(bodegaId = bodegaId)
    }
}

@Composable
fun ScreenWithSafeBack(
    navController: NavController,
    bodegaId: String? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    AppBackHandler(
        navController = navController,
        bodegaId = bodegaId,
        enabled = enabled
    )
    content()
}

/**
 * Navegación segura: evita regresar a login y restaura contexto de bodega o menú principal.
 */
fun NavController.navigateBackSafely(
    bodegaId: String? = null,
    fallbackRoute: String = "menuP"
) {
    val currentRoute = currentBackStackEntry?.destination?.route
    val previousRoute = previousBackStackEntry?.destination?.route

    if (
        currentRoute != "login" &&
        previousRoute != null &&
        previousRoute !in protectedPreviousRoutes &&
        popBackStack()
    ) {
        return
    }

    if (currentRoute in rootRoutes) {
        return
    }

    val target = resolveSafeFallback(
        bodegaId = bodegaId ?: SessionManager.obtenerBodegaActual().takeIf { it.isNotBlank() },
        fallbackRoute = fallbackRoute
    )

    if (currentRoute == target) return

    navigate(target) {
        popUpTo("login") { inclusive = false }
        launchSingleTop = true
    }
}

private fun resolveSafeFallback(
    bodegaId: String?,
    fallbackRoute: String
): String {
    if (!SessionManager.haySesion()) {
        return "login"
    }
    if (!bodegaId.isNullOrBlank()) {
        return "menuBodega/$bodegaId"
    }
    return fallbackRoute
}
