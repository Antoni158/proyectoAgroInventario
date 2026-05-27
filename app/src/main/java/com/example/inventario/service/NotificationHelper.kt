package com.example.inventario.service

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.inventario.R
import com.example.inventario.data.notificacion.AppNotificacion
import com.example.inventario.data.repos.FirebaseRepository
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.security.AppPreferences
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NotificationHelper {

    private val firebase = FirebaseRepository()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun registrar(
        context: Context,
        titulo: String,
        mensaje: String,
        tipo: String,
        bodegaId: String = "",
        referenciaId: String = "",
        productoCodigo: String = "",
        push: Boolean = true
    ) {
        AppPreferences.init(context)
        if (!AppPreferences.permiteNotificacionTipo(tipo)) return

        val usuario = SessionManager.usernameUsuario()
        val notif = AppNotificacion(
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipo,
            productoCodigo = productoCodigo,
            bodegaId = bodegaId,
            referenciaId = referenciaId,
            usuario = usuario
        )
        val db = appdatabase.getDatabase(context)
        val id = db.appNotificacionDao().insert(notif).toInt()
        val guardada = notif.copy(id = id)
        scope.launch {
            firebase.guardarNotificacion(guardada, usuario)
        }
        if (push) {
            withContext(Dispatchers.Main) {
                mostrarPush(context, titulo, mensaje, tipo)
            }
        }
    }

    fun registrarAsync(
        context: Context,
        titulo: String,
        mensaje: String,
        tipo: String,
        bodegaId: String = "",
        referenciaId: String = "",
        productoCodigo: String = ""
    ) {
        scope.launch {
            registrar(context, titulo, mensaje, tipo, bodegaId, referenciaId, productoCodigo)
        }
    }

    fun canShowNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun mostrarPush(context: Context, titulo: String, mensaje: String, tipo: String) {
        if (!canShowNotifications(context)) {
            android.util.Log.w("NOTIF", "Sin permiso POST_NOTIFICATIONS — no se muestra push")
            return
        }

        AppPreferences.init(context)
        val esAlerta = tipo.uppercase() in setOf("STOCK_BAJO", "CRITICO", "AUDITORIA")
        NotificationChannelManager.ensureChannels(
            context.applicationContext,
            forceRecreate = esAlerta
        )

        val channelId = NotificationChannelManager.channelForTipo(tipo)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !nm.areNotificationsEnabled()) {
            android.util.Log.w("NOTIF", "Notificaciones desactivadas en ajustes del sistema")
            return
        }

        val categoria = when (tipo.uppercase()) {
            "STOCK_BAJO", "CRITICO" -> NotificationCompat.CATEGORY_ALARM
            "AUDITORIA" -> NotificationCompat.CATEGORY_REMINDER
            else -> NotificationCompat.CATEGORY_EVENT
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setAutoCancel(true)
            .setCategory(categoria)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(false)
            .setPriority(
                if (tipo.uppercase() in setOf("STOCK_BAJO", "CRITICO")) {
                    NotificationCompat.PRIORITY_MAX
                } else {
                    NotificationCompat.PRIORITY_HIGH
                }
            )

        val sonidoActivo = AppPreferences.notifSonidos
        val vibracionActiva = AppPreferences.notifVibracion

        if (!sonidoActivo && !vibracionActiva) {
            builder.setSilent(true)
        } else {
            if (!sonidoActivo) {
                builder.setSound(null)
            }
            if (vibracionActiva) {
                builder.setVibrate(NotificationChannelManager.vibrationForTipo(tipo))
            } else {
                builder.setVibrate(null)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && sonidoActivo) {
                builder.setSound(NotificationChannelManager.soundUriForTipo(context, tipo))
            }
        }

        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
        android.util.Log.i("NOTIF", "Push mostrada: $titulo · canal=$channelId")
    }

    /** Prueba manual desde ajustes (no se usa al abrir la app). */
    fun mostrarNotificacionPrueba(context: Context) {
        scope.launch {
            registrar(
                context = context,
                titulo = "Inventario Agrícola",
                mensaje = "Notificaciones activas · sonido de entrada",
                tipo = "ENTRADA",
                push = true
            )
        }
    }
}
