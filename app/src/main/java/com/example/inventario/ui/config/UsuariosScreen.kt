package com.example.inventario.ui.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.inventario.ui.config.notifications.AppTopBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import coil.compose.rememberAsyncImagePainter

import com.example.inventario.data.administracion.Usuario
import com.example.inventario.navigation.NavRoutes

import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.UsuarioViewModel
import com.example.inventario.ui.components.navigateBackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(

    navController: NavController

) {

    // VALIDAR ADMIN

    if (

        !SessionManager
            .puedeAdministrar()

    ) {

        navController.navigateBackSafely()

        return
    }

    val viewModel:
            UsuarioViewModel =

        viewModel()

    val usuarios by

    viewModel
        .usuarios
        .collectAsState()

    val usuariosActivos = usuarios.filter { !it.isDeleted }

    var usuarioEliminar by remember { mutableStateOf<Usuario?>(null) }

    usuarioEliminar?.let { u ->
        AlertDialog(
            onDismissRequest = { usuarioEliminar = null },
            title = { Text("¿Eliminar usuario?") },
            text = { Text("Se eliminará \"${u.nombre}\" (@${u.username})") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarUsuario(u)
                    usuarioEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { usuarioEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(

        containerColor =

            MaterialTheme
                .colorScheme
                .background,

        topBar = {
            AppTopBar(titulo = "Usuarios", navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(NavRoutes.CREAR_USUARIO) }) {
                Icon(Icons.Default.Add, contentDescription = "Crear usuario")
            }
        }
    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)

        ) {

            items(usuariosActivos, key = { it.id }) { usuario ->
                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =

                        CardDefaults
                            .cardColors(

                                containerColor =

                                    MaterialTheme
                                        .colorScheme
                                        .surface
                            )

                ) {

                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        // FOTO

                        if (

                            usuario
                                .fotoPerfil
                                .isNotEmpty()

                        ) {

                            Image(

                                painter =

                                    rememberAsyncImagePainter(

                                        usuario
                                            .fotoPerfil
                                    ),

                                contentDescription =
                                    null,

                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape),

                                contentScale =
                                    ContentScale.Crop
                            )

                        } else {

                            Card(

                                modifier =
                                    Modifier.size(70.dp),

                                shape =
                                    CircleShape

                            ) {

                                Row(

                                    modifier =
                                        Modifier.fillMaxSize(),

                                    horizontalArrangement =
                                        Arrangement.Center,

                                    verticalAlignment =
                                        Alignment.CenterVertically

                                ) {

                                    Text(

                                        text =

                                            usuario
                                                .nombre
                                                .take(1),

                                        fontSize = 24.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(

                            modifier =
                                Modifier.padding(8.dp)
                        )

                        // INFO

                        Column(

                            modifier =
                                Modifier.weight(1f)

                        ) {

                            Text(

                                text =
                                    usuario.nombre,

                                fontSize = 18.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =

                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                            )

                            Spacer(

                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text =
                                    usuario.username,

                                color =

                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                            )

                            Spacer(

                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text =
                                    usuario.correo,

                                color =

                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                            )

                            Spacer(

                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text =
                                    "Rol: ${usuario.rol}",

                                color =

                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(

                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text =

                                    if (

                                        usuario.activo

                                    ) {

                                        "Activo"

                                    } else {

                                        "Bloqueado"
                                    },

                                color =

                                    if (

                                        usuario.activo

                                    ) {

                                        MaterialTheme
                                            .colorScheme
                                            .primary

                                    } else {

                                        MaterialTheme
                                            .colorScheme
                                            .error
                                    }
                            )
                        }

                        // ACCIONES

                        Column {

                            // EDITAR

                            IconButton(

                                onClick = {

                                    navController.navigate(

                                        "editarUsuario/${usuario.id}"
                                    )
                                }

                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Edit,

                                    contentDescription =
                                        "Editar"
                                )
                            }

                            // BLOQUEAR / ACTIVAR

                            IconButton(

                                onClick = {
                                    if (usuario.activo) {
                                        viewModel.bloquearUsuario(usuario)
                                    } else {
                                        viewModel.activarUsuario(usuario)
                                    }
                                }

                            ) {

                                Icon(

                                    imageVector =

                                        if (

                                            usuario.activo

                                        ) {

                                            Icons.Default.Lock

                                        } else {

                                            Icons.Default.LockOpen
                                        },

                                    contentDescription =
                                        "Estado"
                                )
                            }

                            // ELIMINAR

                            IconButton(onClick = { usuarioEliminar = usuario }) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Delete,

                                    contentDescription =
                                        "Eliminar"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}