package com.example.inventario.ui.auditoria

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.Auditoria.Auditoria
import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.auditoria.export.exportarAuditoriaExcel
import com.example.inventario.ui.auditoria.export.exportarAuditoriaPDF
import com.example.inventario.ui.components.BodegaAppTopBar
import com.example.inventario.ui.components.rememberBodegaHeader
import com.example.inventario.util.FechaFormatter
import com.example.inventario.viewModel.AuditoriaViewModel
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditoriaScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: AuditoriaViewModel = viewModel()
    val header = rememberBodegaHeader(bodegaId)

    LaunchedEffect(bodegaId) { viewModel.cargarBodega(bodegaId) }

    val productos by viewModel.productos.collectAsState(initial = emptyList())
    val auditorias by viewModel.auditorias.collectAsState(initial = emptyList())
    val resumen by viewModel.resumen.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val porCategoria by viewModel.porCategoria.collectAsState()
    val historial = viewModel.auditoriasFiltradas(auditorias)

    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf<String?>(null) }
    var aplicarAjusteAuto by remember { mutableStateOf(true) }
    val stockFisicoMap = remember { mutableStateMapOf<Int, String>() }
    val observacionMap = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(busqueda) { viewModel.setBusqueda(busqueda) }
    LaunchedEffect(filtro) { viewModel.setFiltroEstado(filtro) }

    val promedioSistema = historial.takeIf { it.isNotEmpty() }
        ?.map { it.stockSistema }?.average()?.toFloat() ?: 0f
    val promedioFisico = historial.takeIf { it.isNotEmpty() }
        ?.map { it.stockFisico }?.average()?.toFloat() ?: 0f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BodegaAppTopBar(
                titulo = "Centro de Auditoría",
                bodegaId = bodegaId,
                navController = navController,
                detalleExtra = header.nombre.ifBlank { header.codigo }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(header.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard("Exactitud", "${resumen.exactitudPct.toInt()}%", Modifier.width(110.dp))
                    MetricCard("Exactos", resumen.exactos.toString(), Modifier.width(90.dp))
                    MetricCard("Faltantes", resumen.faltantes.toString(), Modifier.width(100.dp), MaterialTheme.colorScheme.error)
                    MetricCard("Sobrantes", resumen.sobrantes.toString(), Modifier.width(100.dp), MaterialTheme.colorScheme.tertiary)
                    MetricCard("Dif. total", historial.sumOf { kotlin.math.abs(it.diferencia) }.toInt().toString(), Modifier.width(100.dp))
                }
            }

            item {
                AuditoriaResumenCharts(chartData, porCategoria)
            }

            if (historial.isNotEmpty()) {
                item {
                    AuditoriaComparativaChart(promedioSistema, promedioFisico)
                }
            }

            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar producto o auditor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(null to "Todos", "EXACTO" to "Exacto", "FALTANTE" to "Faltante", "SOBRANTE" to "Sobrante")
                        .forEach { (estado, label) ->
                            FilterChip(
                                selected = filtro == estado,
                                onClick = { filtro = estado },
                                label = { Text(label) }
                            )
                        }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ajuste automático", fontWeight = FontWeight.SemiBold)
                        Text("Sincroniza diferencias al kardex", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = aplicarAjusteAuto, onCheckedChange = { aplicarAjusteAuto = it })
                }
            }

            item {
                Text("Conteo físico por producto", fontWeight = FontWeight.Bold)
            }

            items(productos, key = { it.id }) { producto ->
                ProductoAuditoriaCard(
                    producto = producto,
                    stockFisico = stockFisicoMap[producto.id].orEmpty(),
                    onStockChange = { stockFisicoMap[producto.id] = it },
                    observacion = observacionMap[producto.id].orEmpty(),
                    onObsChange = { observacionMap[producto.id] = it },
                    onRegistrar = {
                        val fisico = stockFisicoMap[producto.id]?.toDoubleOrNull()
                        if (fisico == null) {
                            Toast.makeText(context, "Ingrese stock físico", Toast.LENGTH_SHORT).show()
                            return@ProductoAuditoriaCard
                        }
                        viewModel.registrarAuditoriaProductoAsync(
                            producto = producto,
                            nombreBodega = header.nombre,
                            stockFisico = fisico,
                            observacion = observacionMap[producto.id].orEmpty(),
                            auditorId = SessionManager.obtenerIdUsuario(),
                            auditorNombre = SessionManager.nombreUsuario(),
                            aplicarAjuste = aplicarAjusteAuto
                        ) { error ->
                            if (error == null) {
                                Toast.makeText(context, "Auditoría registrada", Toast.LENGTH_SHORT).show()
                                stockFisicoMap.remove(producto.id)
                            } else {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Historial de auditoría", fontWeight = FontWeight.Bold)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { exportarAuditoriaPDF(context, historial, header.nombre) }) {
                        Text("PDF")
                    }
                    Button(onClick = { exportarAuditoriaExcel(context, historial, header.nombre) }) {
                        Text("Excel")
                    }
                }
            }

            items(historial, key = { it.id }) { a ->
                HistorialAuditoriaCard(
                    auditoria = a,
                    onAplicarAjuste = {
                        scope.launch {
                            val error = viewModel.aplicarAjustePendiente(a)
                            Toast.makeText(
                                context,
                                error ?: "Ajuste aplicado y registrado en kardex",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MetricCard(titulo: String, valor: String, modifier: Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text(valor, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ProductoAuditoriaCard(
    producto: Producto,
    stockFisico: String,
    onStockChange: (String) -> Unit,
    observacion: String,
    onObsChange: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${producto.codigo} · ${producto.descripcion}", fontWeight = FontWeight.SemiBold)
            Text("Categoría: ${producto.categoria} · Ubicación: ${producto.ubicacion}")
            Text("Stock sistema: ${producto.cantidad}")
            OutlinedTextField(
                value = stockFisico,
                onValueChange = onStockChange,
                label = { Text("Stock físico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = observacion,
                onValueChange = onObsChange,
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth()
            )
            stockFisico.toDoubleOrNull()?.let { f ->
                val d = f - producto.cantidad
                val preview = when {
                    d > 0 -> "Vista previa: SOBRANTE (+$d)"
                    d < 0 -> "Vista previa: FALTANTE ($d)"
                    else -> "Vista previa: EXACTO"
                }
                Text(preview, color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = onRegistrar, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar conteo")
            }
        }
    }
}

@Composable
private fun HistorialAuditoriaCard(
    auditoria: Auditoria,
    onAplicarAjuste: () -> Unit
) {
    val estadoColor = when (auditoria.estado) {
        "FALTANTE" -> MaterialTheme.colorScheme.error
        "SOBRANTE" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${auditoria.codigo} · ${auditoria.descripcion}", fontWeight = FontWeight.SemiBold)
            Text("Sistema: ${auditoria.stockSistema} → Físico: ${auditoria.stockFisico}", style = MaterialTheme.typography.bodySmall)
            Text("Estado: ${auditoria.estado} · Diferencia: ${auditoria.diferencia}", color = estadoColor, fontWeight = FontWeight.Medium)
            Text(FechaFormatter.formatear(auditoria.fecha), style = MaterialTheme.typography.bodySmall)
            if (auditoria.observacion.isNotBlank()) Text(auditoria.observacion, style = MaterialTheme.typography.bodySmall)
            Text("Auditor: ${auditoria.auditorNombre}", style = MaterialTheme.typography.labelSmall)
            Text(
                if (auditoria.ajusteAplicado) "Ajuste: aplicado en kardex" else "Ajuste: pendiente",
                style = MaterialTheme.typography.labelSmall,
                color = if (auditoria.ajusteAplicado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            if (!auditoria.ajusteAplicado && auditoria.estado != "EXACTO") {
                OutlinedButton(onClick = onAplicarAjuste, modifier = Modifier.fillMaxWidth()) {
                    Text("Aplicar ajuste a inventario")
                }
            }
        }
    }
}
