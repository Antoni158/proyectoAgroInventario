package com.example.inventario.ui.Vales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.security.AppPermission
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.ui.components.PermissionGate
import com.example.inventario.viewModel.ValeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValeSalidaScreen(
    navController: NavController,
    bodegaId: String
) {
    val viewModel: ValeViewModel = viewModel()
    var busquedaLocal by remember { mutableStateOf("") }

    LaunchedEffect(bodegaId) {
        viewModel.bindBodega(bodegaId)
    }

    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val valesRaw by viewModel.valesConDetalles.collectAsState(initial = emptyList())
    val vales = viewModel.valesFiltradosPorEstado(valesRaw)

    BodegaScrollableListScaffold(
        titulo = "Historial de vales",
        bodegaId = bodegaId,
        navController = navController,
        detalleExtra = "${vales.size} vales",
        floatingActionButton = {
            PermissionGate(permission = AppPermission.CREAR_VALE) {
                FloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.crearVale(bodegaId)) }
                ) {
                    Text("+")
                }
            }
        }
    ) {
        item {
            OutlinedTextField(
                value = busquedaLocal,
                onValueChange = {
                    busquedaLocal = it
                    viewModel.setSearchQuery(it)
                },
                label = { Text("Buscar vale, responsable o destino") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("TODOS", "CONFIRMADO", "BORRADOR", "ANULADO").forEach { estado ->
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick = { viewModel.setFiltroEstado(estado) },
                        label = { Text(estado) }
                    )
                }
            }
        }

        item {
            androidx.compose.material3.Button(
                onClick = { navController.navigate(NavRoutes.traslados(bodegaId)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Traslados")
            }
        }

        if (vales.isEmpty()) {
            item {
                Text(
                    "No hay vales registrados para esta bodega",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(vales, key = { it.vale.idVale }) { item ->
                val vale = item.vale
                val totalCant = item.detalles.sumOf { it.cantidad }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(NavRoutes.detalleVale(vale.idVale, bodegaId))
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(vale.codigoVale, fontWeight = FontWeight.Bold)
                        Text("${item.detalles.size} producto(s) · $totalCant unidades")
                        Text("Destino: ${vale.destino}")
                        Text("Responsable: ${vale.responsable}")
                        Text("Fecha: ${vale.fecha} · ${vale.estado}")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
