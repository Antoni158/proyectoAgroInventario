package com.example.inventario.ui.Kardex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.util.FechaFormatter
import com.example.inventario.viewModel.KardexViewModel
import com.example.inventario.viewModel.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KardexScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val viewModel: KardexViewModel = viewModel()
    var busqueda by remember { mutableStateOf("") }

    val movimientos by viewModel
        .obtenerKardexFiltrado(bodegaId)
        .collectAsState(initial = emptyList())

    val filtrados = movimientos.filter {
        busqueda.isBlank() ||
            it.descripcion.contains(busqueda, ignoreCase = true) ||
            it.codigoProducto.contains(busqueda, ignoreCase = true) ||
            it.tipoMovimiento.contains(busqueda, ignoreCase = true)
    }

    val header = com.example.inventario.ui.components.rememberBodegaHeader(bodegaId)

    BodegaScrollableListScaffold(
        titulo = "Kardex",
        bodegaId = bodegaId,
        navController = navController
    ) {
        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar movimiento") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        exportarKardexPDF(
                            context = context,
                            movimientos = filtrados,
                            etiquetaBodega = com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Text("PDF")
                }
                Button(
                    onClick = {
                        exportarKardexExcel(
                            context = context,
                            movimientos = filtrados,
                            etiquetaBodega = com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId),
                            usuario = SessionManager.nombreUsuario()
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Text("Excel")
                }
            }
        }

        if (filtrados.isEmpty()) {
            item { Text("Sin movimientos para esta bodega") }
        } else {
            items(filtrados, key = { it.id }) { mov ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(mov.descripcion, fontWeight = FontWeight.Bold)
                        Text("Código: ${mov.codigoProducto}")
                        Text("Tipo: ${mov.tipoMovimiento}")
                        Text("Cantidad: ${mov.cantidad}")
                        Text("Saldo: ${mov.saldoNuevo}")
                        Text("Fecha: ${FechaFormatter.formatear(mov.fechaMovimiento)}")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
