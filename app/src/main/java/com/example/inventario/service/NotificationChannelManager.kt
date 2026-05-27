package com.example.inventario.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import com.example.inventario.R
import com.example.inventario.security.AppPreferences

object NotificationChannelManager {

    const val ENTRADAS = "entradas_channel"
    const val SALIDAS = "salidas_channel"
    const val STOCK = "stock_channel"
    const val AUDITORIA = "auditoria_channel"

    private data class ChannelSpec(
        val id: String,
        val name: String,
        val description: String,
        val soundRes: Int,
        val vibrationPattern: LongArray,
        val bypassDnd: Boolean = false
    )

    private val channels = listOf(
        ChannelSpec(
            id = ENTRADAS,
            name = "Entradas",
            description = "Entradas de inventario registradas",
            soundRes = R.raw.notif_entrada,
            vibrationPattern = longArrayOf(0, 120, 80, 120)
        ),
        ChannelSpec(
            id = SALIDAS,
            name = "Salidas",
            description = "Salidas y movimientos de inventario",
            soundRes = R.raw.notif_salida,
            vibrationPattern = longArrayOf(0, 90, 70, 90, 70, 90)
        ),
        ChannelSpec(
            id = STOCK,
            name = "Stock bajo",
            description = "Alertas de stock mínimo y productos críticos",
            soundRes = R.raw.notif_alerta,
            vibrationPattern = longArrayOf(0, 280, 120, 280, 120, 400),
            bypassDnd = true
        ),
        ChannelSpec(
            id = AUDITORIA,
            name = "Auditoría",
            description = "Diferencias y auditorías pendientes",
            soundRes = R.raw.notif_auditoria,
            vibrationPattern = longArrayOf(0, 160, 100, 160)
        )
    )

    fun ensureChannels(context: Context, forceRecreate: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        AppPreferences.init(context)
        val sonidos = AppPreferences.notifSonidos
        val vibracion = AppPreferences.notifVibracion
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (!forceRecreate && channels.all { nm.getNotificationChannel(it.id) != null }) {
            return
        }
        if (forceRecreate) {
            channels.forEach { spec ->
                nm.deleteNotificationChannel(spec.id)
            }
        }
        channels.forEach { spec ->
            if (!forceRecreate && nm.getNotificationChannel(spec.id) != null) return@forEach
            val soundUri = soundUri(context, spec.soundRes)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(
                    if (spec.id == STOCK) {
                        AudioAttributes.USAGE_ALARM
                    } else {
                        AudioAttributes.USAGE_NOTIFICATION
                    }
                )
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                spec.id,
                spec.name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = spec.description
                enableLights(true)
                enableVibration(vibracion)
                if (vibracion) {
                    vibrationPattern = spec.vibrationPattern
                }
                if (sonidos) {
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                if (spec.bypassDnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setBypassDnd(true)
                }
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun channelForTipo(tipo: String): String = when (tipo.uppercase()) {
        "ENTRADA" -> ENTRADAS
        "SALIDA" -> SALIDAS
        "STOCK_BAJO", "CRITICO" -> STOCK
        "AUDITORIA" -> AUDITORIA
        else -> ENTRADAS
    }

    fun soundUriForTipo(context: Context, tipo: String): Uri {
        val res = when (tipo.uppercase()) {
            "ENTRADA" -> R.raw.notif_entrada
            "SALIDA" -> R.raw.notif_salida
            "STOCK_BAJO", "CRITICO" -> R.raw.notif_alerta
            "AUDITORIA" -> R.raw.notif_auditoria
            else -> R.raw.notif_entrada
        }
        return soundUri(context, res)
    }

    fun vibrationForTipo(tipo: String): LongArray = when (tipo.uppercase()) {
        "ENTRADA" -> longArrayOf(0, 120, 80, 120)
        "SALIDA" -> longArrayOf(0, 90, 70, 90, 70, 90)
        "STOCK_BAJO", "CRITICO" -> longArrayOf(0, 280, 120, 280, 120, 400)
        "AUDITORIA" -> longArrayOf(0, 160, 100, 160)
        else -> longArrayOf(0, 120)
    }

    private fun soundUri(context: Context, resId: Int): Uri =
        "android.resource://${context.packageName}/$resId".toUri()
}
