package com.example.inventario.ui.login

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.inventario.data.repos.CloudSyncManager
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.security.AppPreferences
import com.example.inventario.ui.branding.AgriculturalBackground
import com.example.inventario.ui.branding.BrandLogo
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        AppPreferences.init(context)
        progress.animateTo(1f, animationSpec = tween(1400, easing = FastOutSlowInEasing))
        
        launch(Dispatchers.IO) {
            try {
                CloudSyncManager(context).sincronizarBidireccional()
            } catch (e: Exception) {
                android.util.Log.e("SPLASH", "Sync fail: ${e.message}")
            }
        }
        delay(800)

        val destino = if (AppPreferences.rememberSession && AppPreferences.savedUsername.isNotBlank()) {
            val user = appdatabase.getDatabase(context).usuarioDao()
                .existeUsername(AppPreferences.savedUsername)
            if (user != null && user.activo) {
                SessionManager.login(user)
                NavRoutes.MENU_PRINCIPAL
            } else NavRoutes.LOGIN
        } else NavRoutes.LOGIN

        navController.navigate(destino) {
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
        }
    }

    AgriculturalBackground(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BrandLogo(
                size = 220.dp,
                showTitle = false,
                titleColor = Color.White,
                animate = true,
                animationProgress = progress.value
            )
        }
    }
}
