package com.example.inventario.ui.components

import androidx.compose.runtime.Composable
import com.example.inventario.security.AppPermission
import com.example.inventario.viewModel.SessionManager

@Composable
fun PermissionGate(
    permission: AppPermission,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (SessionManager.tienePermiso(permission)) {
        content()
    } else {
        fallback()
    }
}

@Composable
fun RequirePermission(
    permission: AppPermission,
    onDenied: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (SessionManager.tienePermiso(permission)) {
        content()
    } else {
        onDenied()
    }
}

@Composable
fun RoleGate(
    allowed: Boolean,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (allowed) {
        content()
    } else {
        fallback()
    }
}

@Composable
fun ReadOnlyGate(
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (!SessionManager.esSoloLectura()) {
        content()
    } else {
        fallback()
    }
}
