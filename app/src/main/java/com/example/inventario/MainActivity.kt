package com.example.inventario

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface

import androidx.compose.ui.Modifier

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.inventario.navigation.NavGraph
import com.example.inventario.security.AppPreferences
import com.example.inventario.service.NotificationChannelManager
import com.example.inventario.service.OfflineSyncObserver
import com.example.inventario.ui.theme.InventarioTheme
import com.example.inventario.viewModel.AppThemeState

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                NotificationChannelManager.ensureChannels(this, forceRecreate = true)
            }
        }

    override fun onCreate(

        savedInstanceState: Bundle?

    ) {

        setTheme(com.example.inventario.R.style.Theme_Inventario)

        super.onCreate(
            savedInstanceState
        )

        enableEdgeToEdge()
        AppPreferences.init(this)
        AppThemeState.initFromPreferences()
        NotificationChannelManager.ensureChannels(this, forceRecreate = true)
        solicitarPermisoNotificaciones()
        OfflineSyncObserver.registrar(this, lifecycleScope)

        setContent {
            val accent by AppThemeState.tema.collectAsState()
            val darkPref by AppThemeState.darkMode.collectAsState()
            val darkTheme = darkPref || accent == "oscuro"

            InventarioTheme(darkTheme = darkTheme) {

                Surface(

                    modifier =
                        Modifier.fillMaxSize(),

                    color =
                        MaterialTheme
                            .colorScheme
                            .background

                ) {

                    Scaffold(

                        modifier =
                            Modifier.fillMaxSize(),

                        containerColor =

                            MaterialTheme
                                .colorScheme
                                .background

                    ) { padding ->

                        Box(

                            modifier = Modifier
                                .padding(padding)

                        ) {

                            NavGraph()
                        }
                    }
                }
            }
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationChannelManager.ensureChannels(this, forceRecreate = true)
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}