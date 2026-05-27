package com.example.inventario.ui.dashboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.viewModel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    bodegaId: String,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    LaunchedEffect(bodegaId) {
        dashboardViewModel.cargarDashboard(bodegaId)
    }

    val totalProductos by dashboardViewModel.totalProductos.collectAsState()
    val existencias by dashboardViewModel.existencias.collectAsState()
    val stockBajo by dashboardViewModel.stockBajo.collectAsState()
    val valorInventario by dashboardViewModel.valorInventario.collectAsState()
    val productoTop by dashboardViewModel.productoMasMovido.collectAsState()
    val productoCritico by dashboardViewModel.productoCritico.collectAsState()
    val promedioSalidas by dashboardViewModel.promedioSalidas.collectAsState()
    val productosBajoStock by dashboardViewModel.productosBajoStock.collectAsState()
    val prediccionesStock by dashboardViewModel.prediccionesStock.collectAsState()

    val statsEntradas by dashboardViewModel.statsEntradas.collectAsState()
    val statsSalidas by dashboardViewModel.statsSalidas.collectAsState()
    val statsFacturas by dashboardViewModel.statsFacturas.collectAsState()

    val areaEntradas by dashboardViewModel.areaEntradas.collectAsState()
    val areaSalidas by dashboardViewModel.areaSalidas.collectAsState()
    val areaFacturas by dashboardViewModel.areaFacturas.collectAsState()
    val areaStock by dashboardViewModel.areaStock.collectAsState()
    val totalVales by dashboardViewModel.totalVales.collectAsState()

    BodegaScrollableListScaffold(
        titulo = "Panel de análisis",
        bodegaId = bodegaId,
        navController = navController,
        detalleExtra = "Dashboard"
    ) {
        item {
            DashboardStatus(
                totalProductos = totalProductos,
                stockBajo = stockBajo,
                existencias = existencias
            )
        }
        item {
            DashboardKPI(
                productoTop = productoTop,
                productoCritico = productoCritico,
                valorInventario = valorInventario,
                promedioSalidas = promedioSalidas
            )
        }
        item {
            DashboardCards(
                productos = totalProductos,
                stockBajo = stockBajo,
                existencias = existencias,
                presupuesto = valorInventario,
                totalVales = totalVales
            )
        }
        item {
            DashboardCharts(
                entradas = statsEntradas,
                salidas = statsSalidas,
                facturas = statsFacturas,
                stockCritico = stockBajo
            )
        }
        item {
            DashboardAreaChart(
                entradas = areaEntradas,
                salidas = areaSalidas,
                stock = areaStock,
                facturas = areaFacturas
            )
        }
        item {
            DashboardDonutChart(
                normales = totalProductos - stockBajo,
                bajos = stockBajo,
                criticos = productosBajoStock.count { it.cantidad <= it.stockMinimo / 2 }
            )
        }
        item {
            DashboardPrediction(predicciones = prediccionesStock)
        }
        item {
            DashboardAlerts(productos = productosBajoStock)
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
