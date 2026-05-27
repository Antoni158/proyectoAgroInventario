package com.example.inventario.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.security.AppPermission
import com.example.inventario.viewModel.SessionManager

/**
 * Protege rutas por permiso: redirige al menú principal si el rol no tiene acceso.
 */
@Composable
fun ModuleRouteGuard(
    permission: AppPermission,
    navController: NavController,
    content: @Composable () -> Unit
) {
    val allowed = SessionManager.tienePermiso(permission)
    LaunchedEffect(allowed) {
        if (!allowed) {
            navController.navigate(NavRoutes.MENU_PRINCIPAL) {
                popUpTo("menuP") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    if (allowed) {
        content()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No tiene permiso para este módulo",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
