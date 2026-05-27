package com.example.inventario.ui.config.notifications

import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import coil.compose.rememberAsyncImagePainter

import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.inventario.data.administracion.Usuario
import com.example.inventario.security.PasswordHasher
import com.example.inventario.security.RoleManager
import com.example.inventario.security.UserRole
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.UsuarioViewModel
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUsuarioScreen(

    navController: NavController,

    id: Int,

    viewModel: UsuarioViewModel = viewModel()

) {

    if (!SessionManager.puedeAdministrar()) {
        navController.navigateBackSafely()
        return
    }

    // USUARIOS

    val usuarios by

    viewModel
        .usuarios
        .collectAsState()

    // USUARIO

    var usuarioActual by remember {

        mutableStateOf<Usuario?>(null)
    }

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

    var fotoPerfil by remember {

        mutableStateOf("")
    }

    var rol by remember { mutableStateOf(UserRole.ADMIN.storageValue) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val roles = RoleManager.assignableRoles()

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

    // CARGAR DATOS

    LaunchedEffect(Unit) {

        val encontrado =

            usuarios.find {

                it.id == id
            }

        usuarioActual =
            encontrado

        encontrado?.let {

            nombre =
                it.nombre

            correo =
                it.correo

            username =
                it.username

            password =
                it.password

            fotoPerfil =
                it.fotoPerfil

            rol =
                UserRole.normalizeForStorage(UserRole.fromStorage(it.rol))
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Editar Usuario",
                navController = navController
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(
                    rememberScrollState()
                ),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)

        ) {

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

            } else {

                Card(

                    modifier =
                        Modifier.size(120.dp),

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
                                nombre.take(1),

                            fontSize = 32.sp
                        )
                    }
                }
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

                    Text("Nombre")
                },

                modifier =
                    Modifier.fillMaxWidth()
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
                    Modifier.fillMaxWidth()
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
                    Modifier.fillMaxWidth()
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
                    Modifier.fillMaxWidth()
            )

            Spacer(

                modifier =
                    Modifier.height(10.dp)
            )

            Text("Rol")

            roles.forEach { role ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = rol == role.storageValue,
                        onClick = { rol = role.storageValue }
                    )
                    Text(RoleManager.displayLabel(role))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val base = usuarioActual ?: return@Button
                    if (nombre.isBlank() || correo.isBlank() || username.isBlank()) {
                        Toast.makeText(context, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        val otroUser = usuarios.find {
                            it.username.equals(username, true) && it.id != base.id
                        }
                        val otroCorreo = usuarios.find {
                            it.correo.equals(correo, true) && it.id != base.id
                        }
                        if (otroUser != null) {
                            Toast.makeText(context, "El username ya existe", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        if (otroCorreo != null) {
                            Toast.makeText(context, "El correo ya existe", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val passFinal = when {
                            password.isBlank() -> base.password
                            password == base.password -> base.password
                            else -> PasswordHasher.hash(password)
                        }
                        viewModel.editarUsuario(
                            base.copy(
                                nombre = nombre.trim(),
                                correo = correo.trim(),
                                username = username.trim(),
                                password = passFinal,
                                fotoPerfil = fotoPerfil,
                                rol = UserRole.normalizeForStorage(UserRole.fromStorage(rol))
                            )
                        )
                        Toast.makeText(context, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                        navController.navigateBackSafely()
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text("Guardar")
            }
        }
    }
}