package com.example.inventario.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.BodegaViewModel
import com.example.inventario.ui.components.navigateBackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearBodegaScreen(
    navController: NavController,
    viewModel: BodegaViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Nueva Bodega", navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Registrar Nueva Bodega",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )

                    Text(
                        text = "Ingrese los detalles de la nueva bodega",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de la Bodega") },
                        placeholder = { Text("Ej: Bodega Taller Zacapa") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Descripción") },
                        placeholder = { Text("Ubicación o notas") },
                        minLines = 2,
                        shape = MaterialTheme.shapes.medium
                    )

                    Button(
                        onClick = {
                            if (nombre.isNotBlank()) {
                                viewModel.crearBodega(nombre, descripcion)
                                navController.navigateBackSafely()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = nombre.isNotBlank()
                    ) {
                        Text("Crear Bodega")
                    }
                }
            }
        }
    }
}
