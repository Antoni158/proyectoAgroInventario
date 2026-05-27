package com.example.inventario.ui.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
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
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.viewModel.CostoDetalleLinea
import com.example.inventario.viewModel.OperativoCostosViewModel
import com.example.inventario.viewModel.RangoOperativo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesOperativosScreen(
    navController: NavController,
    bodegaId: String,
    viewModel: OperativoCostosViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var rango by remember { mutableStateOf(RangoOperativo.MES) }
    val context = LocalContext.current
    val header = com.example.inventario.ui.components.rememberBodegaHeader(bodegaId)
    val etiqueta = com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId)
    val periodoLabel = rango.name.lowercase().replaceFirstChar { it.uppercase() }

    LaunchedEffect(bodegaId, rango) {
        viewModel.cargar(bodegaId)
        viewModel.setRango(rango)
    }

    BodegaScrollableListScaffold(
        titulo = "Reportes operativos",
        bodegaId = bodegaId,
        navController = navController
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangoOperativo.entries.forEach { r ->
                    FilterChip(
                        selected = rango == r,
                        onClick = { rango = r },
                        label = { Text(r.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
        item {
            Text(
                "Gasto período: Q ${"%.2f".format(state.gastoMensual)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        exportarCostosOperativosPDF(
                            context,
                            state.detalleLineas,
                            etiqueta,
                            periodoLabel
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Text("PDF")
                }
                Button(
                    onClick = {
                        exportarCostosOperativosExcel(
                            context,
                            state.detalleLineas,
                            etiqueta,
                            periodoLabel
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Text("Excel")
                }
            }
        }
        item { DetalleCostosCard(state.detalleLineas) }
        item { GrupoCard("Por área", state.porArea) }
        item { GrupoCard("Por destino", state.porDestino) }
        item { GrupoCard("Por vehículo / placa", state.porVehiculo) }
        item { GrupoCard("Por campo", state.porCampo) }
        item { GrupoCard("Productos más utilizados", state.productosMasUsados) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun DetalleCostosCard(lineas: List<CostoDetalleLinea>) {
    Text("Detalle de costos operativos", fontWeight = FontWeight.SemiBold)
    Text(
        "Ej.: enero · Tractor 1 · mantenimiento · Q 1,000",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(6.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(12.dp)) {
            if (lineas.isEmpty()) {
                Text("No hay salidas en el período seleccionado.")
                return@Column
            }
            var mesAnterior = ""
            lineas.forEach { linea ->
                if (linea.mesEtiqueta != mesAnterior) {
                    if (mesAnterior.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(linea.mesEtiqueta, fontWeight = FontWeight.Bold)
                    mesAnterior = linea.mesEtiqueta
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(linea.concepto, fontWeight = FontWeight.Medium)
                        Text(
                            buildString {
                                if (linea.vehiculo != "—") append("${linea.vehiculo} · ")
                                if (linea.destino != "—") append("${linea.destino} · ")
                                if (linea.area != "—") append(linea.area)
                            }.trimEnd(' ', '·'),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (linea.fecha.isNotBlank()) {
                            Text(linea.fecha, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        "Q ${"%.2f".format(linea.monto)}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GrupoCard(titulo: String, grupos: List<com.example.inventario.viewModel.CostoGrupo>) {
    if (grupos.isEmpty()) return
    Text(titulo, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            grupos.forEach { g ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(g.clave)
                    Text("Q ${"%.2f".format(g.total)} (${g.movimientos})")
                }
            }
        }
    }
}
