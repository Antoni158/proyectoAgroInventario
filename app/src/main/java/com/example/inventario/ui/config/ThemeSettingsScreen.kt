package com.example.inventario.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.AppThemeState

private data class ThemeOption(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val color1: Color,
    val color2: Color,
    val activaDark: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(navController: NavController) {
    val temaActual by AppThemeState.tema.collectAsState()
    val darkMode by AppThemeState.darkMode.collectAsState()

    val opciones = listOf(
        ThemeOption("verde", "Verde agrícola", "Corporativo · natural", Color(0xFF2E7D32), Color(0xFF66BB6A)),
        ThemeOption("azul", "Azul dinámico", "Moderno · fintech", Color(0xFF1565C0), Color(0xFF42A5F5)),
        ThemeOption("morado", "Morado premium", "Elegante · ejecutivo", Color(0xFF7B2FF7), Color(0xFFB06CFF)),
        ThemeOption("naranja", "Naranja ejecutivo", "Industrial · energía", Color(0xFFE65100), Color(0xFFFF9800)),
        ThemeOption("oscuro", "Oscuro premium", "Glass dark · AMOLED", Color(0xFF0A0E14), Color(0xFF1E2430), activaDark = true)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(titulo = "Temas visuales", navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Personaliza la apariencia global del sistema",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo oscuro", fontWeight = FontWeight.Bold)
                        Text(
                            "Activa dark mode premium independiente del tema",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = darkMode || temaActual == "oscuro",
                        onCheckedChange = { AppThemeState.setDarkMode(it) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            opciones.forEach { opcion ->
                ThemeOptionCard(
                    opcion = opcion,
                    seleccionado = temaActual == opcion.id,
                    onClick = { AppThemeState.cambiarTema(opcion.id) }
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Los cambios se aplican al instante en login, dashboard, bodega, auditoría y configuración. Se guardan al reiniciar la app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    opcion: ThemeOption,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (seleccionado) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(24.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(opcion.color1, opcion.color2)))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(opcion.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(4.dp))
                Text(opcion.subtitulo, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
            if (seleccionado) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
