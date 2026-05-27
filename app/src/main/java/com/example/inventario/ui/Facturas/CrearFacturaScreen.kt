package com.example.inventario.ui.Facturas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Factura
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.FacturaViewModel
import com.example.inventario.viewModel.SessionManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearFacturaScreen(
    navController: NavController,
    bodegaId: String,
    viewModel: FacturaViewModel = viewModel()
) {
    var proveedor by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }

    val cal = Calendar.getInstance()
    val fecha = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Nueva factura", navController = navController, bodegaId = bodegaId)
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(proveedor, { proveedor = it }, label = { Text("Proveedor") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(codigo, { codigo = it }, label = { Text("Código producto") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(cantidad, { cantidad = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(costo, { costo = it }, label = { Text("Costo unitario") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val cant = cantidad.toIntOrNull() ?: 0
                    val c = costo.toDoubleOrNull() ?: 0.0
                    viewModel.agregarFactura(
                        Factura(
                            numeroFactura = "FAC-MAN-${System.currentTimeMillis()}",
                            fecha = fecha,
                            proveedor = proveedor,
                            codigo = codigo,
                            descripcion = descripcion,
                            cantidad = cant,
                            precioUnitario = c,
                            costo = c,
                            total = cant * c,
                            bodegaId = bodegaId,
                            usuario = SessionManager.usernameUsuario()
                        )
                    )
                    navController.navigateBackSafely(bodegaId = bodegaId)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar factura") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarFacturaScreen(
    navController: NavController,
    facturaId: Int,
    viewModel: FacturaViewModel = viewModel()
) {
    var factura by remember { mutableStateOf<Factura?>(null) }
    var proveedor by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(facturaId) {
        val f = viewModel.obtenerFacturaPorId(facturaId)
        factura = f
        proveedor = f?.proveedor.orEmpty()
        codigo = f?.codigo.orEmpty()
        descripcion = f?.descripcion.orEmpty()
        cantidad = f?.cantidad?.toString().orEmpty()
        costo = f?.costo?.toString().orEmpty()
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Editar factura", navController = navController)
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = factura?.numeroFactura.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("N° Factura") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(proveedor, { proveedor = it }, label = { Text("Proveedor") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(codigo, { codigo = it }, label = { Text("Código producto") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(cantidad, { cantidad = it.filter { c -> c.isDigit() } }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(costo, { costo = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Costo unitario") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val cant = cantidad.toIntOrNull() ?: 0
                    val c = costo.toDoubleOrNull() ?: 0.0
                    factura?.let {
                        viewModel.actualizarFactura(
                            it.copy(
                                proveedor = proveedor.trim(),
                                codigo = codigo.trim(),
                                descripcion = descripcion.trim(),
                                cantidad = cant,
                                precioUnitario = c,
                                costo = c,
                                total = cant * c
                            )
                        )
                    }
                    navController.navigateBackSafely()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Actualizar") }
        }
    }
}
