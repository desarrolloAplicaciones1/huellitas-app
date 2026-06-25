package com.uade.huellitas.domain.model

data class AppSettings(
    val followSystemTheme: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val alertRadiusKm: Int = 15,
    val offlineModeEnabled: Boolean = true
)
