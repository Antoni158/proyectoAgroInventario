package com.example.inventario.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.ui.components.design.UserAvatar
import com.example.inventario.viewModel.SessionManager

@Composable
fun ProfileTopBarAction(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (!SessionManager.haySesion()) return
    val nombre = SessionManager.nombreUsuario().ifBlank { SessionManager.usernameUsuario() }
    UserAvatar(
        nombre = nombre,
        fotoUri = SessionManager.fotoUsuario(),
        modifier = modifier.padding(end = 8.dp),
        size = 38.dp,
        onClick = { navController.navigate(NavRoutes.PERFIL) }
    )
}

@Composable
fun WelcomeProfileSubtitle(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (!SessionManager.haySesion()) return
    val nombre = SessionManager.nombreUsuario().ifBlank { SessionManager.usernameUsuario() }
    Text(
        text = "Bienvenido $nombre",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onDoubleTap = { navController.navigate(NavRoutes.PERFIL) })
        }
    )
}

@Composable
fun ProfileTopBarTitleBlock(
    titulo: String,
    navController: NavController,
    subtitulo: String? = null,
    welcomeMode: Boolean = false
) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (welcomeMode) {
            WelcomeProfileSubtitle(navController)
        } else if (!subtitulo.isNullOrBlank()) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
