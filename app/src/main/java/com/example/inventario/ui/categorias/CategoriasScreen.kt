package com.example.inventario.ui.categorias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.util.CodigoGenerator
import com.example.inventario.viewModel.CategoriaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    navController: NavController,
    bodegaId: String
) {

    val viewModel: CategoriaViewModel =
        viewModel()

    val categorias by
    viewModel.categorias.collectAsState()

    val scope = rememberCoroutineScope()

    var categoriaEditando by remember { mutableStateOf<Categoria?>(null) }
    var nombreEditar by remember { mutableStateOf("") }
    var descripcionEditar by remember { mutableStateOf("") }
    var errorEditar by remember { mutableStateOf("") }

    var categoriaEliminar by remember { mutableStateOf<Categoria?>(null) }
    var errorEliminar by remember { mutableStateOf("") }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "Categorías"
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sistema de categorías", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Consulte y edite categorías. Para crear una nueva use Nuevo Producto o Nueva Entrada.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (categorias.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay categorías")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Créelas desde Inventario → Nuevo producto",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(categorias, key = { it.id }) { categoria ->
                    CategoriaCard(
                        categoria = categoria,
                        onEditar = {
                            categoriaEditando = categoria
                            nombreEditar = categoria.nombre
                            descripcionEditar = categoria.descripcion
                            errorEditar = ""
                        },
                        onEliminar = {
                            categoriaEliminar = categoria
                            errorEliminar = ""
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    categoriaEditando?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoriaEditando = null },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreEditar.isBlank()) {
                            errorEditar = "Ingresa un nombre"
                            return@Button
                        }
                        scope.launch {
                            viewModel.actualizarCategoria(
                                cat.copy(
                                    nombre = nombreEditar.trim(),
                                    descripcion = descripcionEditar.trim()
                                )
                            )
                            categoriaEditando = null
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { categoriaEditando = null }) { Text("Cancelar") }
            },
            title = { Text("Editar categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nombreEditar,
                        onValueChange = { nombreEditar = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcionEditar,
                        onValueChange = { descripcionEditar = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    if (errorEditar.isNotBlank()) {
                        Text(errorEditar, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    categoriaEliminar?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoriaEliminar = null },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (!viewModel.puedeEliminar(cat)) {
                                errorEliminar = "No se puede eliminar: hay productos con esta categoría"
                                return@launch
                            }
                            viewModel.eliminarCategoria(cat)
                            categoriaEliminar = null
                        }
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { categoriaEliminar = null }) { Text("Cancelar") }
            },
            title = { Text("¿Desea eliminar esta categoría?") },
            text = {
                Column {
                    Text("Se eliminará \"${cat.nombre}\".")
                    if (errorEliminar.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorEliminar, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}

@Composable
private fun CategoriaCard(
    categoria: Categoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    categoria.nombre,
                    fontWeight = FontWeight.Bold
                )
                val slug = categoria.prefijo.ifBlank {
                    CodigoGenerator.prefijoDesdeCategoria(categoria.nombre)
                }.uppercase()
                Text(
                    "Códigos: ${slug}0001…",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                if (categoria.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        categoria.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}