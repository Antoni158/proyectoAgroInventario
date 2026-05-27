package com.example.inventario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Encabezado fijo (bodega + título) y contenido en un único [LazyColumn] desplazable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodegaScrollableListScaffold(
    titulo: String,
    bodegaId: String,
    navController: NavController,
    detalleExtra: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    floatingActionButton: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    Scaffold(
        containerColor = containerColor,
        topBar = {
            BodegaAppTopBar(
                titulo = titulo,
                bodegaId = bodegaId,
                navController = navController,
                detalleExtra = detalleExtra,
                scrollBehavior = null
            )
        },
        floatingActionButton = floatingActionButton
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}
