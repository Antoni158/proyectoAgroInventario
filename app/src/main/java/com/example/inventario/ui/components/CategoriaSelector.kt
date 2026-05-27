package com.example.inventario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.viewModel.CategoriaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaSelector(
    categorias: List<Categoria>,
    categoriaSeleccionada: Categoria?,
    onCategoriaSelected: (Categoria?) -> Unit,
    categoriaViewModel: CategoriaViewModel,
    bodegaId: String,
    modifier: Modifier = Modifier,
    label: String = "Categoría",
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var mostrarNueva by remember { mutableStateOf(false) }
    var nombreNueva by remember { mutableStateOf("") }
    var errorNueva by remember { mutableStateOf("") }
    var creando by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = categoriaSeleccionada?.nombre.orEmpty(),
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                label = { Text(label) },
                placeholder = { Text("Seleccione categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false }
            ) {
                if (categorias.isEmpty()) {
                    Text(
                        "Sin categorías — cree una abajo",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(cat.nombre, fontWeight = FontWeight.Medium)
                                    if (cat.prefijo.isNotBlank()) {
                                        Text(
                                            "Prefijo: ${cat.prefijo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onCategoriaSelected(cat)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                nombreNueva = ""
                errorNueva = ""
                mostrarNueva = true
            },
            enabled = enabled && !creando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Nueva categoría", modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (mostrarNueva) {
        AlertDialog(
            onDismissRequest = { if (!creando) mostrarNueva = false },
            title = { Text("Nueva categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreNueva,
                        onValueChange = { nombreNueva = it; errorNueva = "" },
                        label = { Text("Nombre (ej. Tornillos)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        "Se guarda en Room y Firebase. Los códigos serán T0001, T0002… según la categoría.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (errorNueva.isNotBlank()) {
                        Text(errorNueva, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreNueva.isBlank()) {
                            errorNueva = "Ingrese el nombre"
                            return@Button
                        }
                        creando = true
                        scope.launch {
                            try {
                                val creada = categoriaViewModel.crearCategoriaDesdeProducto(
                                    nombreNueva.trim(),
                                    bodegaId
                                )
                                if (creada != null) {
                                    onCategoriaSelected(creada)
                                    mostrarNueva = false
                                } else {
                                    errorNueva = "No se pudo crear la categoría"
                                }
                            } finally {
                                creando = false
                            }
                        }
                    },
                    enabled = !creando
                ) {
                    Text(if (creando) "Guardando…" else "Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarNueva = false }, enabled = !creando) {
                    Text("Cancelar")
                }
            }
        )
    }
}
