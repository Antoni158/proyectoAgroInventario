package com.example.inventario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.inventario.viewModel.SessionManager

@Composable
fun BindBodegaContext(bodegaId: String) {
    LaunchedEffect(bodegaId) {
        if (bodegaId.isNotBlank()) {
            SessionManager.seleccionarBodega(bodegaId)
        }
    }
}
