package com.example.inventario.ui.login

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.data.repos.CloudSyncManager
import com.example.inventario.security.AppPreferences
import com.example.inventario.ui.branding.AgriculturalBackground
import com.example.inventario.ui.branding.BrandLogo
import com.example.inventario.ui.components.design.GlassCard
import com.example.inventario.ui.components.design.ModernTextField
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import com.example.inventario.ui.theme.BrandColors
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.UsuarioViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: UsuarioViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val cardAlpha = remember { Animatable(0f) }
    val cardOffset = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        cardAlpha.animateTo(1f, tween(800))
        cardOffset.animateTo(0f, tween(800))
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        AgriculturalBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrandLogo(
                        size = 140.dp,
                        showTitle = false,
                        animate = true,
                        animationProgress = cardAlpha.value
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Centro de Inventario",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Iniciar sesión",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.TextoOscuro
                            )
                            Text(
                                text = "Acceso seguro · Inventario Agrícola",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandColors.VerdePrincipal
                            )

                            ModernTextField(
                                value = usuario,
                                onValueChange = { usuario = it },
                                label = "Usuario",
                                leadingIcon = Icons.Default.Person,
                                isError = usuario.isBlank() && password.isNotBlank(),
                                errorMessage = if (usuario.isBlank() && password.isNotBlank()) "Ingrese usuario" else null
                            )

                            ModernTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Contraseña",
                                leadingIcon = Icons.Default.Lock,
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    val image = if (passwordVisible) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }
                                    androidx.compose.material3.IconButton(
                                        onClick = { passwordVisible = !passwordVisible }
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = image,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )

                            Button(
                                onClick = {
                                    if (usuario.isEmpty() || password.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Complete todos los campos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        // Primero intentamos login local por si ya hay datos
                                        var user = viewModel.login(
                                            usuario.trim(),
                                            password.trim()
                                        )

                                        // Si no está local, intentamos sincronizar usuarios de Firebase y re-intentar login
                                        if (user == null) {
                                            android.util.Log.i("LOGIN", "Usuario no encontrado localmente, intentando sincronizar con Firebase...")
                                            val syncManager = CloudSyncManager(context)
                                            val syncRes = syncManager.sincronizarBidireccional()
                                            if (syncRes.ok) {
                                                user = viewModel.login(usuario.trim(), password.trim())
                                            }
                                        }

                                        if (user != null) {
                                            SessionManager.login(user)
                                            AppPreferences.init(context)
                                            AppPreferences.guardarSesion(user.username)
                                            
                                            // Aseguramos que el resto de datos estén sincronizados
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                CloudSyncManager(context).sincronizarBidireccional()
                                            }

                                            Toast.makeText(
                                                context,
                                                "Bienvenido ${user.username}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate(NavRoutes.MENU_PRINCIPAL) {
                                                popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Usuario o contraseña incorrectos",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandColors.VerdePrincipal
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Iniciar sesión",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            TextButton(
                                onClick = {
                                    navController.navigate(NavRoutes.RECUPERAR_PASSWORD)
                                }
                            ) {
                                Text(
                                    text = "Recuperar contraseña",
                                    color = BrandColors.VerdePrincipal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
