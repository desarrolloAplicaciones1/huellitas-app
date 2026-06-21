package com.uade.huellitas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.uade.huellitas.di.AppContainer

class HuellitasApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Alertas de mascotas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de avisos cercanos"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val ALERTS_CHANNEL_ID = "ALERTS_CHANNEL"
    }
}
