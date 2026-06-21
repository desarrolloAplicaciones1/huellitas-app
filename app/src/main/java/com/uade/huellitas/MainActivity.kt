package com.uade.huellitas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.uade.huellitas.domain.model.AppSettings
import com.uade.huellitas.navigation.NavGraph
import com.uade.huellitas.ui.theme.HuellitasTheme
import com.uade.huellitas.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            val appContainer = (application as HuellitasApplication).appContainer
            val settings = appContainer.getAppSettingsUseCase()
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            val resolvedDarkMode = if (settings.value.followSystemTheme) {
                isSystemInDarkTheme()
            } else {
                settings.value.darkModeEnabled
            }
            ThemeState.isDarkMode = resolvedDarkMode

            HuellitasTheme(darkTheme = resolvedDarkMode) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATIONS
            )
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 100
    }
}
