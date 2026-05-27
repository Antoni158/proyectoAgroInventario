package com.example.inventario.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.inventario.data.repos.CloudSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes

import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.BodegaCodigoUtil
import com.example.inventario.viewModel.BodegaViewModel
import com.example.inventario.viewModel.SessionManager

import com.example.inventario.ui.config.notifications.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val bodegaViewModel: BodegaViewModel = viewModel()
    val bodegas by bodegaViewModel.bodegas.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        bodegaViewModel.sincronizarDesdeFirebase()
        withContext(Dispatchers.IO) {
            CloudSyncManager(context).sincronizarCompletoDesdeNube()
        }
    }

    var bodegaParaEditar by remember { mutableStateOf<Bodega?>(null) }
    var bodegaParaEliminar by remember { mutableStateOf<Bodega?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaDescripcion by remember { mutableStateOf("") }

    LaunchedEffect(bodegaParaEditar) {
        nuevoNombre = bodegaParaEditar?.nombre.orEmpty()
        nuevaDescripcion = bodegaParaEditar?.descripcion.orEmpty()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = "Bodegas",
                subtitulo = "Seleccione una unidad agrícola",
                navController = navController
            )
        }
    ) { padding ->

        LazyVerticalGrid(

            columns =
                GridCells.Fixed(2),

            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),

            contentPadding =
                PaddingValues(12.dp),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)

        ) {

            // bodegas

            items(bodegas) { bodega ->

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable {

                            navController.navigate(

                                "menuBodega/${bodega.id}"
                            )
                        },

                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),

                    elevation =
                        CardDefaults.cardElevation(

                            defaultElevation = 4.dp
                        )

                ) {

                    Box(

                        modifier =
                            Modifier.fillMaxSize()

                    ) {

                        if (SessionManager.puedeGestionarBodegas()) {

                            // editar

                            IconButton(

                                onClick = {

                                    bodegaParaEditar =
                                        bodega
                                },

                                modifier =
                                    Modifier.align(
                                        Alignment.TopStart
                                    )

                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Edit,

                                    contentDescription =
                                        "Editar",

                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }

                            // eliminar

                            IconButton(

                                onClick = {

                                    bodegaParaEliminar =
                                        bodega
                                },

                                modifier =
                                    Modifier.align(
                                        Alignment.TopEnd
                                    )

                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Delete,

                                    contentDescription =
                                        "Eliminar",

                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                            }
                        }

                        Column(

                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),

                            horizontalAlignment =
                                Alignment.CenterHorizontally,

                            verticalArrangement =
                                Arrangement.Center

                        ) {

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(45.dp)
                                )
                            }

                            Spacer(

                                modifier =
                                    Modifier.height(16.dp)
                            )

                            Text(
                                text = BodegaCodigoUtil.nombreParaMostrar(bodega),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = bodega.descripcion.trim().ifBlank { "Sin descripción" },
                                fontSize = 14.sp,
                                color = if (bodega.descripcion.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                fontStyle = if (bodega.descripcion.isBlank()) {
                                    androidx.compose.ui.text.font.FontStyle.Italic
                                } else {
                                    androidx.compose.ui.text.font.FontStyle.Normal
                                }
                            )

                            if (bodega.codigoCorto.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bodega.codigoCorto,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Abrir bodega",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // crear bodega

            if (SessionManager.puedeGestionarBodegas()) {
                item {
                    CardCrearBodega(
                        onClick = { navController.navigate(NavRoutes.CREAR_BODEGA) }
                    )
                }
            }
        }

        // editar

        if (bodegaParaEditar != null) {

            AlertDialog(

                onDismissRequest = {

                    bodegaParaEditar = null
                },

                title = {

                    Text(
                        "Editar Bodega"
                    )
                },

                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nuevaDescripcion,
                            onValueChange = { nuevaDescripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                },

                confirmButton = {

                    Button(

                        onClick = {

                            bodegaParaEditar?.let {
                                bodegaViewModel.editarBodega(
                                    it.copy(
                                        nombre = nuevoNombre.trim(),
                                        descripcion = nuevaDescripcion.trim()
                                    )
                                )
                            }

                            bodegaParaEditar = null
                            nuevoNombre = ""
                            nuevaDescripcion = ""
                        }

                    ) {

                        Text("Guardar")
                    }
                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            bodegaParaEditar =
                                null
                        }

                    ) {

                        Text("Cancelar")
                    }
                }
            )
        }

        // eliminar

        if (bodegaParaEliminar != null) {

            AlertDialog(

                onDismissRequest = {

                    bodegaParaEliminar = null
                },

                title = {

                    Text(
                        "Eliminar Bodega"
                    )
                },

                text = {

                    Text(

                        "¿Seguro que desea eliminar esta bodega?"
                    )
                },

                confirmButton = {

                    Button(

                        onClick = {

                            bodegaParaEliminar?.let {

                                bodegaViewModel
                                    .eliminarBodega(it)
                            }

                            bodegaParaEliminar =
                                null
                        },

                        colors =
                            ButtonDefaults.buttonColors(

                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )

                    ) {

                        Text("Eliminar")
                    }
                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            bodegaParaEliminar =
                                null
                        }

                    ) {

                        Text("Cancelar")
                    }
                }
            )
        }
    }
}