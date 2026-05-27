package com.example.inventario.ui.config

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.repos.BackupManager
import com.example.inventario.data.repos.CloudSyncManager
import com.example.inventario.data.repos.ExcelImportManager
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.security.AppPermission
import com.example.inventario.security.AppPreferences
import com.example.inventario.security.PasswordHasher
import com.example.inventario.ui.branding.AgriculturalBackground
import com.example.inventario.ui.components.PermissionGate
import com.example.inventario.ui.components.design.GlassCard
import com.example.inventario.ui.components.design.ModernSectionHeader
import com.example.inventario.ui.components.design.ModernStatChip
import com.example.inventario.ui.components.design.ModernTextField
import com.example.inventario.ui.components.design.UserAvatar
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.components.rememberBodegaHeader
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.service.NotificationChannelManager
import com.example.inventario.viewModel.AppThemeState
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.UsuarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    navController: NavController,
    viewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppPreferences.init(context)
    AppThemeState.initFromPreferences()

    val usuario = SessionManager.obtenerUsuario()
    val bodegaId = SessionManager.obtenerBodegaActual()
    val header = rememberBodegaHeader(bodegaId.ifBlank { "global" })

    var nombre by remember { mutableStateOf(usuario?.nombre.orEmpty()) }
    var correo by remember { mutableStateOf(usuario?.correo.orEmpty()) }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf(usuario?.fotoPerfil.orEmpty()) }
    var guardando by remember { mutableStateOf(false) }

    var notifGlobal by remember { mutableStateOf(AppPreferences.notificacionesActivas) }
    var notifEntradas by remember { mutableStateOf(AppPreferences.notifEntradas) }
    var notifSalidas by remember { mutableStateOf(AppPreferences.notifSalidas) }
    var notifStock by remember { mutableStateOf(AppPreferences.notifStockBajo) }
    var notifAuditoria by remember { mutableStateOf(AppPreferences.notifAuditoria) }
    var notifCriticos by remember { mutableStateOf(AppPreferences.notifCriticos) }
    var notifSonidos by remember { mutableStateOf(AppPreferences.notifSonidos) }
    var notifVibracion by remember { mutableStateOf(AppPreferences.notifVibracion) }
    var darkMode by remember { mutableStateOf(AppPreferences.darkModeEnabled) }
    var panelCompacto by remember { mutableStateOf(AppPreferences.panelCompacto) }

    var expandNotif by remember { mutableStateOf(false) }
    var expandSeguridad by remember { mutableStateOf(false) }
    var expandBackups by remember { mutableStateOf(false) }

    val darkThemeFlow by AppThemeState.darkMode.collectAsState()

    val fotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fotoUri = it.toString() }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                val r = BackupManager(context).importarJson(it)
                Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importProductosLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                val r = ExcelImportManager(context).importarProductos(it, bodegaId, header.codigo)
                val msg = if (r.errores.isNotEmpty()) "${r.mensaje}\n${r.errores.take(3).joinToString("\n")}" else r.mensaje
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importCategoriasLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                val r = ExcelImportManager(context).importarCategorias(it)
                Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importSqliteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                val r = BackupManager(context).restaurarSqlite(it)
                Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Mi perfil", navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AgriculturalBackground(Modifier.fillMaxSize().padding(padding)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(
                            nombre = nombre.ifBlank { usuario?.username.orEmpty() },
                            fotoUri = fotoUri,
                            size = 96.dp,
                            onClick = { fotoLauncher.launch("image/*") }
                        )
                        TextButton(onClick = { fotoLauncher.launch("image/*") }) {
                            Text("Cambiar foto", color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            nombre.ifBlank { usuario?.username.orEmpty() },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "@${usuario?.username.orEmpty()}",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModernStatChip("Rol", SessionManager.etiquetaRol())
                            if (bodegaId.isNotBlank()) {
                                ModernStatChip(
                                    "Bodega",
                                    header.nombre.ifBlank { header.codigo }.take(12)
                                )
                            }
                        }
                        Text(
                            correo.ifBlank { "Sin correo" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernSectionHeader("Editar perfil", "Actualiza tus datos personales")
                        ModernTextField(nombre, { nombre = it }, "Nombre completo", leadingIcon = Icons.Default.Person)
                        ModernTextField(correo, { correo = it }, "Correo", leadingIcon = Icons.Default.Email)
                        ModernTextField(
                            passwordNueva, { passwordNueva = it }, "Nueva contraseña",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        ModernTextField(
                            passwordConfirm, { passwordConfirm = it }, "Confirmar contraseña",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(
                            onClick = {
                                if (nombre.isBlank() || correo.isBlank()) {
                                    Toast.makeText(context, "Complete nombre y correo", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (passwordNueva.isNotBlank() && passwordNueva != passwordConfirm) {
                                    Toast.makeText(context, "Contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch {
                                    guardando = true
                                    val pass = if (passwordNueva.isNotBlank()) {
                                        PasswordHasher.hash(passwordNueva)
                                    } else usuario?.password.orEmpty()
                                    val ok = viewModel.actualizarPerfil(
                                        nombre.trim(), correo.trim(), pass, fotoUri
                                    )
                                    guardando = false
                                    Toast.makeText(
                                        context,
                                        if (ok) "Perfil actualizado" else "Error al guardar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !guardando,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Guardar cambios", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Configuración", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                PerfilConfigSection(
                    titulo = "Notificaciones",
                    subtitulo = "Entradas, salidas, stock y auditoría",
                    icono = Icons.Default.Notifications,
                    expanded = expandNotif,
                    onToggle = { expandNotif = !expandNotif }
                ) {
                    PerfilToggle("Activar notificaciones", "Canal general del sistema", notifGlobal) {
                        notifGlobal = it; AppPreferences.notificacionesActivas = it
                    }
                    PerfilToggle("✓ Entrada registrada", "Aviso al registrar entradas", notifEntradas) {
                        notifEntradas = it; AppPreferences.notifEntradas = it
                    }
                    PerfilToggle("↘ Salida registrada", "Aviso al registrar salidas", notifSalidas) {
                        notifSalidas = it; AppPreferences.notifSalidas = it
                    }
                    PerfilToggle("⚠ Stock bajo", "Alerta de stock mínimo", notifStock) {
                        notifStock = it; AppPreferences.notifStockBajo = it
                    }
                    PerfilToggle("📋 Auditoría pendiente", "Diferencias en conteos", notifAuditoria) {
                        notifAuditoria = it; AppPreferences.notifAuditoria = it
                    }
                    PerfilToggle("🚨 Producto crítico", "Stock agotado o crítico", notifCriticos) {
                        notifCriticos = it; AppPreferences.notifCriticos = it
                    }
                    PerfilToggle("🔊 Sonidos", "Tono distinto por tipo de alerta", notifSonidos) {
                        notifSonidos = it
                        AppPreferences.notifSonidos = it
                        NotificationChannelManager.ensureChannels(context, forceRecreate = true)
                    }
                    PerfilToggle("📳 Vibración", "Patrón de vibración por alerta", notifVibracion) {
                        notifVibracion = it
                        AppPreferences.notifVibracion = it
                        NotificationChannelManager.ensureChannels(context, forceRecreate = true)
                    }
                    OutlinedButton(
                        onClick = { navController.navigate(NavRoutes.NOTIFICACIONES) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver historial de notificaciones")
                    }
                }

                PerfilConfigSection(
                    titulo = "Seguridad",
                    subtitulo = "Contraseña y sesión",
                    icono = Icons.Default.Security,
                    expanded = expandSeguridad,
                    onToggle = { expandSeguridad = !expandSeguridad }
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(NavRoutes.RECUPERAR_PASSWORD) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Recuperar contraseña / usuario")
                    }
                    Text(
                        "Use el formulario superior para cambiar contraseña.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    PerfilActionRow(
                        "Temas visuales",
                        "Verde, azul, morado, naranja y oscuro premium",
                        Icons.Default.Palette
                    ) {
                        navController.navigate(NavRoutes.TEMAS)
                    }
                }

                PermissionGate(permission = AppPermission.ADMINISTRAR_USUARIOS) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column {
                            PerfilActionRow(
                                "Gestión de usuarios",
                                "Crear, editar, eliminar y activar cuentas",
                                Icons.Default.Person
                            ) {
                                navController.navigate(NavRoutes.USUARIOS)
                            }
                            OutlinedButton(
                                onClick = { navController.navigate(NavRoutes.CREAR_USUARIO) },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Crear nuevo usuario")
                            }
                        }
                    }
                }

                PermissionGate(permission = AppPermission.VER_PAPELERA) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        PerfilActionRow(
                            "Papelera",
                            "Productos, usuarios y registros eliminados",
                            Icons.Default.Delete
                        ) {
                            navController.navigate(NavRoutes.PAPELERA)
                        }
                    }
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text("Tema oscuro", fontWeight = FontWeight.Bold)
                            Text("Modo premium dark", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = darkMode || darkThemeFlow,
                            onCheckedChange = {
                                darkMode = it
                                AppThemeState.setDarkMode(it)
                            }
                        )
                    }
                }

                PerfilToggleRow("Panel compacto", "Vista resumida en panel ejecutivo", panelCompacto) {
                    panelCompacto = it; AppPreferences.panelCompacto = it
                }

                PermissionGate(permission = AppPermission.SINCRONIZAR_NUBE) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        PerfilActionRow(
                            "Sincronización Firebase",
                            "Room ↔ Firebase bidireccional",
                            Icons.Default.CloudSync
                        ) {
                            scope.launch {
                                val r = CloudSyncManager(context).sincronizarBidireccional()
                                Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                PermissionGate(permission = AppPermission.CONFIG_AVANZADA) {
                    PerfilConfigSection(
                        titulo = "Backups",
                        subtitulo = "JSON y Excel",
                        icono = Icons.Default.Backup,
                        expanded = expandBackups,
                        onToggle = { expandBackups = !expandBackups }
                    ) {
                        PerfilActionRow("Exportar backup JSON", "Compartir respaldo completo", Icons.Default.SaveAlt) {
                            scope.launch {
                                val r = BackupManager(context).exportarJson()
                                Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
                                r.file?.let { BackupManager(context).compartirBackup(it) }
                            }
                        }
                        PerfilActionRow("Exportar backup SQLite", "Copia completa de la base", Icons.Default.SaveAlt) {
                            scope.launch {
                                val r = BackupManager(context).exportarSqlite()
                                Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
                                r.file?.let { BackupManager(context).compartirSqlite(it) }
                            }
                        }
                        PerfilActionRow("Restaurar backup JSON", "Importar desde archivo", Icons.Default.Backup) {
                            importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                        }
                        PerfilActionRow("Restaurar backup SQLite", "Reemplazar base local", Icons.Default.Backup) {
                            importSqliteLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream", "*/*"))
                        }
                        PerfilActionRow("Importar categorías Excel", "Catálogo de categorías", Icons.Default.SaveAlt) {
                            importCategoriasLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel"
                                )
                            )
                        }
                        if (bodegaId.isNotBlank()) {
                            PerfilActionRow("Importar productos Excel", header.nombre, Icons.Default.SaveAlt) {
                                importProductosLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel"
                                    )
                                )
                            }
                        }
                    }
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Información de la app", fontWeight = FontWeight.Bold)
                            Text("Inventario Agrícola v1.0", style = MaterialTheme.typography.bodySmall)
                            Text("Kotlin · Compose · Room · Firebase", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        SessionManager.cerrarSesionCompleto(context)
                        navController.navigate(NavRoutes.LOGIN) { popUpTo(0) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Cerrar sesión")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PerfilConfigSection(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column {
            PerfilActionRow(titulo, subtitulo, icono, onClick = onToggle)
            AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(
                    Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun PerfilActionRow(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

@Composable
private fun PerfilToggle(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    PerfilToggleRow(titulo, subtitulo, checked, onCheckedChange)
}

@Composable
private fun PerfilToggleRow(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Medium)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
