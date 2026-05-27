package com.example.inventario.ui.Vales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.viewModel.TrasladoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrasladoScreen(
    navController: NavController,
    bodegaId: String
) {
    val viewModel: TrasladoViewModel = viewModel()
    val traslados by viewModel
        .obtenerTraslados(bodegaId)
        .collectAsState(initial = emptyList())

    BodegaScrollableListScaffold(
        titulo = "Traslados",
        bodegaId = bodegaId,
        navController = navController,
        detalleExtra = "Origen",
        floatingActionButton = {
            com.example.inventario.ui.components.ReadOnlyGate {
                FloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.crearTraslado(bodegaId)) }
                ) {
                    Text("+")
                }
            }
        }
    ) {
        if (traslados.isEmpty()) {
            item {
                Text(
                    text = "No existen traslados registrados",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(traslados, key = { it.idTraslado }) { traslado ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = traslado.productoDescripcion, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Código: ${traslado.codigoTraslado}")
                        Text("Cantidad: ${traslado.cantidad}")
                        Text("Origen: ${traslado.bodegaOrigen}")
                        Text("Destino: ${traslado.bodegaDestino}")
                        Text("Responsable: ${traslado.responsable}")
                        Text("Fecha: ${traslado.fecha}")
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
