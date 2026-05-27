package com.example.inventario.ui.Vales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.inventario.data.bodega.ValeConDetalles
import com.example.inventario.security.AppPermission
import com.example.inventario.ui.components.PermissionGate
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.ValeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleValeScreen(
    navController: NavController,
    valeId: Int,
    bodegaId: String
) {
    val context = LocalContext.current
    val viewModel: ValeViewModel = viewModel()
    var valeCompleto by remember { mutableStateOf<ValeConDetalles?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(valeId) {
        cargando = true
        valeCompleto = viewModel.obtenerValeCompleto(valeId)
        cargando = false
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Detalle de vale",
                subtitulo = valeCompleto?.vale?.codigoVale ?: "Bodega: $bodegaId",
                navController = navController,
                bodegaId = bodegaId
            )
        }
    ) { padding ->
        when {
            cargando -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            valeCompleto == null -> {
                Text(
                    "Vale no encontrado",
                    modifier = Modifier.padding(padding).padding(16.dp)
                )
            }
            else -> {
                val data = valeCompleto!!
                val vale = data.vale
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(vale.codigoVale, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Responsable: ${vale.responsable}")
                            Text("Destino: ${vale.destino}")
                            Text("Fecha: ${vale.fecha}")
                            Text("Estado: ${vale.estado}")
                            Text("Productos: ${data.detalles.size} líneas")
                        }
                    }

                    PermissionGate(permission = AppPermission.EXPORTAR) {
                        Button(
                            onClick = { exportarValeProfesionalPDF(context, data, bodegaId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Text("Exportar PDF profesional")
                        }
                    }

                    Text("Detalle de productos", fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(data.detalles, key = { it.idDetalle }) { linea ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(linea.productoDescripcion, fontWeight = FontWeight.SemiBold)
                                    Text("Código: ${linea.productoCodigo}")
                                    Text("Categoría: ${linea.categoria}")
                                    Text("Cantidad: ${linea.cantidad}")
                                    if (linea.codigoSalida.isNotBlank()) {
                                        Text("Ref. salida: ${linea.codigoSalida}")
                                    }
                                }
                            }
                        }
                    }

                    PermissionGate(permission = AppPermission.ELIMINAR_REGISTRO) {
                        Button(
                            onClick = {
                                viewModel.eliminarVale(vale)
                                navController.navigateBackSafely(bodegaId = bodegaId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Eliminar vale")
                        }
                    }
                }
            }
        }
    }
}
