package com.example.inventario.ui.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Bodega
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.security.AppPreferences
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.dashboard.DashboardCards
import com.example.inventario.ui.dashboard.DashboardKPI
import com.example.inventario.ui.dashboard.DashboardStatus
import com.example.inventario.ui.presupuesto.ResumenFinanciero
import com.example.inventario.viewModel.BodegaViewModel
import com.example.inventario.viewModel.DashboardViewModel
import com.example.inventario.viewModel.PresupuestoViewModel
import com.example.inventario.viewModel.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelPrincipalScreen(
    navController: NavController,
    bodegaViewModel: BodegaViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    presupuestoViewModel: PresupuestoViewModel = viewModel()
) {
    val bodegas by bodegaViewModel.bodegas.collectAsState(initial = emptyList())
    var bodegaSeleccionada by remember { mutableStateOf<Bodega?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(bodegas) {
        if (bodegaSeleccionada == null && bodegas.isNotEmpty()) {
            bodegaSeleccionada = bodegas.first()
        }
    }

    val bodegaId = bodegaSeleccionada?.id.orEmpty()

    LaunchedEffect(bodegaId) {
        if (bodegaId.isNotEmpty()) {
            dashboardViewModel.cargarDashboard(bodegaId)
            presupuestoViewModel.cargar(bodegaId)
        }
    }

    val resumenFin by presupuestoViewModel.resumen.collectAsState()

    val totalProductos by dashboardViewModel.totalProductos.collectAsState()
    val existencias by dashboardViewModel.existencias.collectAsState()
    val stockBajo by dashboardViewModel.stockBajo.collectAsState()
    val valorInventario by dashboardViewModel.valorInventario.collectAsState()
    val totalFacturas by dashboardViewModel.totalFacturas.collectAsState()
    val totalVales by dashboardViewModel.totalVales.collectAsState()
    val productoTop by dashboardViewModel.productoMasMovido.collectAsState()
    val productoCritico by dashboardViewModel.productoCritico.collectAsState()
    val promedioSalidas by dashboardViewModel.promedioSalidas.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Panel ejecutivo",
                subtitulo = "KPIs · Dashboard · Analytics",
                navController = navController,
                welcomeMode = true
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Vista consolidada del inventario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (bodegas.isEmpty()) {
                Text(
                    "No hay bodegas registradas. Un auditor puede crear bodegas desde Configuración.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = bodegaSeleccionada?.nombre ?: "Seleccionar bodega",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bodega activa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        bodegas.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.nombre) },
                                onClick = {
                                    bodegaSeleccionada = b
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelFeatureChip(
                        icon = Icons.Default.Insights,
                        label = "KPIs",
                        modifier = Modifier.weight(1f)
                    )
                    PanelFeatureChip(
                        icon = Icons.Default.ShowChart,
                        label = "Gráficas",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelFeatureChip(
                        icon = Icons.Default.Analytics,
                        label = "Analytics",
                        modifier = Modifier.weight(1f)
                    )
                    PanelFeatureChip(
                        icon = Icons.Default.Timeline,
                        label = "Actividad",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (bodegaId.isNotEmpty()) {
                    DashboardStatus(
                        totalProductos = totalProductos,
                        stockBajo = stockBajo,
                        existencias = existencias
                    )
                    DashboardKPI(
                        productoTop = productoTop,
                        productoCritico = productoCritico,
                        valorInventario = valorInventario,
                        promedioSalidas = promedioSalidas
                    )
                    DashboardCards(
                        productos = totalProductos,
                        stockBajo = stockBajo,
                        existencias = existencias,
                        presupuesto = valorInventario,
                        totalVales = totalVales
                    )

                    ResumenFinanciero(state = resumenFin)

                    Button(
                        onClick = {
                            SessionManager.seleccionarBodega(bodegaId)
                            navController.navigate(NavRoutes.panelBodega(bodegaId))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir dashboard completo")
                    }

                    Button(
                        onClick = {
                            SessionManager.seleccionarBodega(bodegaId)
                            navController.navigate(NavRoutes.presupuesto(bodegaId))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver presupuesto y finanzas")
                    }
                }
            }

            if (AppPreferences.panelCompacto) {
                Text(
                    "Vista compacta activada (Configuración → Panel)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun PanelFeatureChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        }
    }
}
