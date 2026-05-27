package com.example.inventario.ui.config

import android.net.Uri
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import coil.compose.rememberAsyncImagePainter

import com.example.inventario.security.RoleManager
import com.example.inventario.security.UserRole
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar

import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.UsuarioViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioScreen(

    navController: NavController,

    viewModel: UsuarioViewModel = viewModel()

) {

    // VALIDAR ADMIN

    if (

        !SessionManager
            .puedeAdministrar()

    ) {

        navController.navigateBackSafely()

        return
    }

    // CONTEXT

    val context =
        LocalContext.current

    val scope = rememberCoroutineScope()

    // STATES

    var nombre by remember {

        mutableStateOf("")
    }

    var correo by remember {

        mutableStateOf("")
    }

    var username by remember {

        mutableStateOf("")
    }

    var password by remember {

        mutableStateOf("")
    }

    var rol by remember {

        mutableStateOf(UserRole.ADMIN.storageValue)
    }

    var fotoPerfil by remember {

        mutableStateOf("")
    }

    // GALERIA

    val launcher =

        rememberLauncherForActivityResult(

            contract =

                ActivityResultContracts
                    .GetContent()

        ) { uri: Uri? ->

            uri?.let {

                fotoPerfil =
                    it.toString()
            }
        }

    // DROPDOWN

    var expanded by remember {

        mutableStateOf(false)
    }

    val roles = RoleManager.assignableRoles().map { it.storageValue }

    Scaffold(

        containerColor =

            MaterialTheme
                .colorScheme
                .background,

        topBar = {

            AppTopBar(

                titulo =
                    "Crear Usuario",

                navController =
                    navController
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)

        ) {

            // FOTO

            if (

                fotoPerfil.isNotEmpty()

            ) {

                Image(

                    painter =

                        rememberAsyncImagePainter(
                            fotoPerfil
                        ),

                    contentDescription =
                        null,

                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),

                    contentScale =
                        ContentScale.Crop
                )

                Spacer(

                    modifier =
                        Modifier.height(10.dp)
                )
            }

            // BOTON GALERIA

            Button(

                onClick = {

                    launcher.launch(
                        "image/*"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(

                    text =
                        "Seleccionar Imagen"
                )
            }

            // NOMBRE

            OutlinedTextField(

                value =
                    nombre,

                onValueChange = {

                    nombre = it
                },

                label = {

                    Text("Nombre Completo")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            // CORREO

            OutlinedTextField(

                value =
                    correo,

                onValueChange = {

                    correo = it
                },

                label = {

                    Text("Correo")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            // USERNAME

            OutlinedTextField(

                value =
                    username,

                onValueChange = {

                    username = it
                },

                label = {

                    Text("Usuario")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            // PASSWORD

            OutlinedTextField(

                value =
                    password,

                onValueChange = {

                    password = it
                },

                label = {

                    Text("Contraseña")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            // ROL

            ExposedDropdownMenuBox(

                expanded =
                    expanded,

                onExpandedChange = {

                    expanded =
                        !expanded
                }

            ) {

                OutlinedTextField(

                    value =
                        rol,

                    onValueChange = {},

                    readOnly = true,

                    label = {

                        Text("Rol")
                    },

                    trailingIcon = {

                        ExposedDropdownMenuDefaults
                            .TrailingIcon(

                                expanded =
                                    expanded
                            )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(

                    expanded =
                        expanded,

                    onDismissRequest = {

                        expanded =
                            false
                    }

                ) {

                    roles.forEach {

                            item ->

                        DropdownMenuItem(

                            text = {

                                Text(item)
                            },

                            onClick = {

                                rol = item

                                expanded =
                                    false
                            }
                        )
                    }
                }
            }

            Spacer(

                modifier =
                    Modifier.height(10.dp)
            )

            // BOTON

            Button(

                onClick = {

                    if (

                        nombre.isEmpty()
                        ||
                        correo.isEmpty()
                        ||
                        username.isEmpty()
                        ||
                        password.isEmpty()

                    ) {

                        Toast.makeText(

                            context,

                            "Complete todos los campos",

                            Toast.LENGTH_SHORT

                        ).show()

                        return@Button
                    }

                    scope.launch {
                        val result = viewModel.crearUsuario(
                            nombre = nombre,
                            correo = correo,
                            username = username,
                            password = password,
                            fotoPerfil = fotoPerfil,
                            rol = UserRole.normalizeForStorage(UserRole.fromStorage(rol))
                        )
                        Toast.makeText(
                            context,
                            result.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                        if (result.ok) navController.navigateBackSafely()
                    }
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =

                            MaterialTheme
                                .colorScheme
                                .primary
                    )

            ) {

                Text(

                    text =
                        "Guardar Usuario",

                    color =

                        MaterialTheme
                            .colorScheme
                            .onPrimary
                )
            }
        }
    }
}