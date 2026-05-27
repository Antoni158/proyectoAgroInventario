package com.example.inventario.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventario.ui.components.design.GlassCard
import com.example.inventario.ui.components.design.UserAvatar
import com.example.inventario.viewModel.SessionManager

@Composable
fun WelcomeProfileCard(
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "welcomeScale")

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        pressed = true
                        onOpenProfile()
                    },
                    onPress = {
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            }
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserAvatar(
                nombre = SessionManager.nombreUsuario().ifBlank { SessionManager.usernameUsuario() },
                fotoUri = SessionManager.fotoUsuario(),
                size = 64.dp
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Bienvenido ${SessionManager.nombreUsuario().ifBlank { SessionManager.usernameUsuario() }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Rol: ${SessionManager.etiquetaRol()}",
                    color = MaterialTheme.colorScheme.primary
                )
                if (SessionManager.esSoloLectura()) {
                    Text(
                        "Modo solo lectura",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Doble clic para abrir perfil",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
