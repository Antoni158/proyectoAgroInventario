package com.example.inventario.ui.logs

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.administracion.Log
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.log.exportarLogsExcel
import com.example.inventario.ui.log.exportarLogsTexto
import com.example.inventario.viewModel.LogViewModel
import com.example.inventario.util.FechaFormatter
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(navController: NavController) {
    val viewModel: LogViewModel = viewModel()
    val context = LocalContext.current
    var busqueda by remember { mutableStateOf("") }
    var filtroModulo by remember { mutableStateOf<String?>(null) }

    val logsFlow = if (busqueda.isBlank()) viewModel.logs else viewModel.buscarLogs(busqueda)
    val logs by logsFlow.collectAsState(initial = emptyList<com.example.inventario.data.administracion.Log>())

    val logsFiltrados = remember(logs, filtroModulo) {
        if (filtroModulo == null) logs
        else logs.filter { it.modulo.equals(filtroModulo, ignoreCase = true) }
    }

    val modulosCriticos = listOf("ELIMINAR", "LOGIN", "AUDITORIA", "USUARIOS")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = "Auditoría empresarial",
                subtitulo = "Logs · historial de cambios",
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
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar usuario, módulo o acción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filtroModulo == null,
                    onClick = { filtroModulo = null },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = filtroModulo == "ELIMINAR",
                    onClick = { filtroModulo = if (filtroModulo == "ELIMINAR") null else "ELIMINAR" },
                    label = { Text("Críticos") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { exportarLogsExcel(context, logsFiltrados) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Text("Excel", modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = {
                        val file = exportarLogsTexto(context, logsFiltrados)
                        file?.let {
                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", it
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Exportar logs"))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exportar TXT")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (logsFiltrados.isEmpty()) {
                Text(
                    "Sin registros para mostrar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logsFiltrados, key = { it.id }) { log ->
                        LogCard(log, esCritico = log.accion in modulosCriticos || log.modulo in modulosCriticos)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: Log, esCritico: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = if (esCritico) {
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            )
        } else {
            androidx.compose.material3.CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(log.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(log.rol, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Módulo: ${log.modulo}")
            Text("Acción: ${log.accion}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(log.descripcion)
            Spacer(modifier = Modifier.height(8.dp))
            val fecha = FechaFormatter.formatear(log.fecha)
            Text(fecha, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
