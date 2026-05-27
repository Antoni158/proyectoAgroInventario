package com.example.inventario.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.ui.branding.AgriculturalBackground
import com.example.inventario.ui.components.design.GlassCard
import com.example.inventario.ui.components.design.ModernSectionHeader
import com.example.inventario.ui.components.design.ModernTextField
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.theme.BrandColors
import com.example.inventario.viewModel.UsuarioViewModel
import kotlinx.coroutines.launch

private enum class ModoRecuperacion { CONTRASENA, USUARIO }

@Composable
fun RecuperarContrasenaScreen(
    navController: NavController,
    viewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var modo by remember { mutableStateOf(ModoRecuperacion.CONTRASENA) }
    var step by remember { mutableStateOf(1) }
    var identificador by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var nuevaPass by remember { mutableStateOf("") }
    var usernameRecuperado by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(false) }

    AgriculturalBackground(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            GlassCard(Modifier.padding(24.dp).fillMaxWidth()) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ModernSectionHeader(
                        titulo = "Recuperar acceso",
                        subtitulo = "Control Agrícola · verificación segura"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = modo == ModoRecuperacion.CONTRASENA,
                            onClick = { modo = ModoRecuperacion.CONTRASENA; step = 1 },
                            label = { Text("Contraseña") }
                        )
                        FilterChip(
                            selected = modo == ModoRecuperacion.USUARIO,
                            onClick = { modo = ModoRecuperacion.USUARIO; step = 1 },
                            label = { Text("Usuario") }
                        )
                    }

                    when (modo) {
                        ModoRecuperacion.CONTRASENA -> when (step) {
                            1 -> {
                                Text("Ingrese usuario y correo registrados")
                                ModernTextField(
                                    value = identificador,
                                    onValueChange = { identificador = it },
                                    label = "Usuario",
                                    leadingIcon = Icons.Default.Person
                                )
                                ModernTextField(
                                    value = correo,
                                    onValueChange = { correo = it },
                                    label = "Correo",
                                    leadingIcon = Icons.Default.Email
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            cargando = true
                                            if (viewModel.generarYEnviarOTP(identificador.trim())) {
                                                step = 2
                                            } else {
                                                Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                                            }
                                            cargando = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !cargando,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.VerdePrincipal)
                                ) { Text("Enviar código") }
                            }
                            2 -> {
                                Text("Código de verificación (6 dígitos)")
                                ModernTextField(value = otp, onValueChange = { otp = it }, label = "Código OTP")
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val u = viewModel.verificarOTP(identificador.trim(), otp.trim())
                                            if (u != null) {
                                                userId = u.id
                                                step = 3
                                            } else {
                                                Toast.makeText(context, "Código inválido o expirado", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.VerdePrincipal)
                                ) { Text("Validar") }
                            }
                            else -> {
                                ModernTextField(
                                    value = nuevaPass,
                                    onValueChange = { nuevaPass = it },
                                    label = "Nueva contraseña",
                                    leadingIcon = Icons.Default.Lock,
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (viewModel.cambiarPassword(userId, nuevaPass)) {
                                                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_LONG).show()
                                                navController.navigateBackSafely()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.VerdePrincipal)
                                ) { Text("Guardar contraseña") }
                            }
                        }

                        ModoRecuperacion.USUARIO -> when (step) {
                            1 -> {
                                Text("Ingrese su correo para recuperar el nombre de usuario")
                                ModernTextField(
                                    value = correo,
                                    onValueChange = { correo = it },
                                    label = "Correo electrónico",
                                    leadingIcon = Icons.Default.Email
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            cargando = true
                                            val otpGen = viewModel.generarOtpRecuperarUsuario(correo.trim())
                                            if (otpGen != null) {
                                                Toast.makeText(context, "Código enviado (ver Logcat OTP)", Toast.LENGTH_LONG).show()
                                                step = 2
                                            } else {
                                                Toast.makeText(context, "Correo no registrado", Toast.LENGTH_SHORT).show()
                                            }
                                            cargando = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !cargando,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.VerdePrincipal)
                                ) { Text("Enviar código") }
                            }
                            2 -> {
                                ModernTextField(value = otp, onValueChange = { otp = it }, label = "Código OTP")
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val u = viewModel.verificarOTP(correo.trim(), otp.trim())
                                            if (u != null) {
                                                usernameRecuperado = u.username
                                                step = 3
                                            } else {
                                                Toast.makeText(context, "Código inválido", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.VerdePrincipal)
                                ) { Text("Verificar") }
                            }
                            else -> {
                                Text(
                                    "Su usuario es:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    usernameRecuperado.orEmpty(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = BrandColors.VerdePrincipal,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { navController.navigateBackSafely() },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Ir al login") }
                            }
                        }
                    }

                    TextButton(onClick = { navController.navigateBackSafely() }) {
                        Text("Volver al login")
                    }
                }
            }
        }
    }
}
