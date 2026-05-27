package com.example.inventario.ui.auditoria

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.BodegaViewModel
import com.example.inventario.viewModel.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditoriaHubScreen(
    navController: NavController,
    bodegaViewModel: BodegaViewModel = viewModel()
) {
    val bodegas by bodegaViewModel.bodegas.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Centro de Auditoría",
                subtitulo = "Supervisión global · ${SessionManager.etiquetaRol()}",
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Seleccione la bodega a auditar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(bodegas) { bodega ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SessionManager.seleccionarBodega(bodega.id)
                                navController.navigate(NavRoutes.auditoria(bodega.id))
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Text(bodega.nombre, fontWeight = FontWeight.Bold)
                            Text(
                                "ID: ${bodega.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
