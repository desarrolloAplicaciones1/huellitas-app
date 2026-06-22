package com.uade.huellitas.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.User
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.ThemeState
import com.uade.huellitas.ui.theme.Urbanist

private val CardBg = Color(0xFFF5F9F9)
private val radiusOptions = listOf(1, 3, 5, 10, 20)

private enum class ReportFilter {
    ACTIVE,
    RESOLVED,
    ALL
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit,
    onNavigateToAlertDetail: (alertId: String) -> Unit = {},
    onNavigateToMyAlerts: () -> Unit = {},
    onNavigateToMyPets: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDark = if (settings.followSystemTheme) systemDark else settings.darkModeEnabled
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    var tempRadius by remember { mutableIntStateOf(settings.alertRadiusKm) }
    var reportFilter by remember { mutableStateOf(ReportFilter.ACTIVE) }

    LaunchedEffect(isDark) {
        ThemeState.isDarkMode = isDark
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión", fontFamily = Urbanist, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que querés cerrar sesión?", fontFamily = Urbanist) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() }) {
                    Text("Cerrar sesión", color = Color.Red, fontFamily = Urbanist)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", fontFamily = Urbanist)
                }
            }
        )
    }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = { Text("Radio de alertas", fontFamily = Urbanist, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "${tempRadius} km",
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HuellitasTeal,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = radiusOptions.indexOf(tempRadius).toFloat(),
                        onValueChange = { tempRadius = radiusOptions[it.toInt()] },
                        valueRange = 0f..(radiusOptions.size - 1).toFloat(),
                        steps = radiusOptions.size - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = HuellitasTeal,
                            activeTrackColor = HuellitasTeal,
                            inactiveTrackColor = Color(0xFFDDDDDD)
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        radiusOptions.forEach { km ->
                            Text(
                                "${km}km",
                                fontFamily = Urbanist,
                                fontSize = 12.sp,
                                color = if (tempRadius == km) HuellitasTeal else Color(0xFF888888),
                                fontWeight = if (tempRadius == km) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAlertRadius(tempRadius)
                        showRadiusDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
                ) {
                    Text("Guardar", fontFamily = Urbanist, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRadiusDialog = false }) {
                    Text("Cancelar", fontFamily = Urbanist, color = Color.Gray)
                }
            }
        )
    }

    val visibleAlerts = when (val state = uiState) {
        is ProfileUiState.Success -> state.userAlerts.filter { alert ->
            when (reportFilter) {
                ReportFilter.ACTIVE -> alert.status == AlertStatus.ACTIVE
                ReportFilter.RESOLVED -> alert.status == AlertStatus.RESOLVED
                ReportFilter.ALL -> true
            }
        }
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF1C1C1C) else Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F7F6)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = HuellitasTeal, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Perfil", fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = if (isDark) Color.White else Color(0xFF1C1C1C))
        }

        when (val state = uiState) {
            is ProfileUiState.Success -> UserSection(user = state.user, darkMode = isDark)
            is ProfileUiState.Error -> ProfileStatusSection(
                title = "Perfil no disponible",
                subtitle = state.message,
                darkMode = isDark
            )
            ProfileUiState.Loading -> ProfileStatusSection(
                title = "Cargando perfil",
                subtitle = "Estamos recuperando tus datos.",
                darkMode = isDark
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("MIS REPORTES", darkMode = isDark)
        Spacer(modifier = Modifier.height(8.dp))

        val cardBgDynamic = if (isDark) Color(0xFF2A2A2A) else CardBg

        when (val state = uiState) {
            is ProfileUiState.Success -> {
                ReportFilterRow(current = reportFilter, darkMode = isDark, onChange = { reportFilter = it })
                Spacer(modifier = Modifier.height(8.dp))
                if (visibleAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBgDynamic)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (reportFilter) {
                                ReportFilter.ACTIVE -> "No tenés reportes activos ahora mismo"
                                ReportFilter.RESOLVED -> "Todavía no resolviste ningún aviso"
                                ReportFilter.ALL -> "Todavía no publicaste ningún aviso"
                            },
                            fontFamily = Urbanist,
                            fontSize = 14.sp,
                            color = Color(0xFF888888)
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(12.dp), color = cardBgDynamic, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column {
                            visibleAlerts.forEachIndexed { index, alert ->
                                ReporteItem(alert = alert, darkMode = isDark, onClick = { onNavigateToAlertDetail(alert.id) })
                                if (index < visibleAlerts.lastIndex) {
                                    HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                    }
                }
            }
            is ProfileUiState.Error -> {
                ProfileMessageCard(
                    message = state.message,
                    darkMode = isDark,
                    backgroundColor = cardBgDynamic
                )
            }
            ProfileUiState.Loading -> {
                ProfileMessageCard(
                    message = "Cargando tus reportes...",
                    darkMode = isDark,
                    backgroundColor = cardBgDynamic
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("GESTION", darkMode = isDark)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(shape = RoundedCornerShape(12.dp), color = cardBgDynamic, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                CuentaItem("Mis avisos", darkMode = isDark, onClick = onNavigateToMyAlerts)
                HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 12.dp))
                CuentaItem("Mis mascotas", darkMode = isDark, onClick = onNavigateToMyPets)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("CONFIGURACIÓN", darkMode = isDark)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(shape = RoundedCornerShape(12.dp), color = cardBgDynamic, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                Surface(onClick = {
                    tempRadius = settings.alertRadiusKm
                    showRadiusDialog = true
                }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF3D3D3D), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Radio de alertas", fontFamily = Urbanist, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF1C1C1C), modifier = Modifier.weight(1f))
                        Text("${settings.alertRadiusKm} km", fontFamily = Urbanist, fontSize = 14.sp, color = HuellitasTeal, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 12.dp))

                SettingsSwitchRow(
                    title = "Seguir tema del sistema",
                    subtitle = "Usa el modo claro u oscuro de tu dispositivo",
                    checked = settings.followSystemTheme,
                    onCheckedChange = viewModel::setFollowSystemTheme,
                    enabled = true,
                    darkMode = isDark,
                    icon = { tint -> Icon(Icons.Default.BrightnessAuto, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 12.dp))

                SettingsSwitchRow(
                    title = "Tema oscuro",
                    subtitle = if (settings.followSystemTheme) "Desactivá la opción anterior para elegirlo manualmente" else "Aplicar tema oscuro en la app",
                    checked = isDark,
                    onCheckedChange = viewModel::setDarkMode,
                    enabled = !settings.followSystemTheme,
                    darkMode = isDark,
                    icon = { tint -> Icon(Icons.Default.DarkMode, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 12.dp))

                SettingsSwitchRow(
                    title = "Modo offline",
                    subtitle = "Permite trabajar con los datos cacheados localmente",
                    checked = settings.offlineModeEnabled,
                    onCheckedChange = viewModel::setOfflineMode,
                    enabled = true,
                    darkMode = isDark,
                    icon = { tint -> Icon(Icons.Default.WifiOff, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("CUENTA", darkMode = isDark)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(shape = RoundedCornerShape(12.dp), color = cardBgDynamic, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                if (uiState is ProfileUiState.Success) {
                    CuentaItem("Editar perfil", darkMode = isDark, onClick = onNavigateToEditProfile)
                    HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 12.dp))
                }
                CuentaItem("Cerrar sesión", isDestructive = true, darkMode = isDark, onClick = { showLogoutDialog = true })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun UserSection(user: User, darkMode: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!user.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "Foto de perfil de ${user.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(HuellitasTeal), contentAlignment = Alignment.Center) {
                Text(user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U", fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(user.name, fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkMode) Color.White else Color(0xFF1C1C1C))
            Text(user.email, fontFamily = Urbanist, fontSize = 14.sp, color = Color(0xFF888888))
            if (!user.phone.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = HuellitasTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(user.phone, fontFamily = Urbanist, fontSize = 13.sp, color = Color(0xFF888888))
                }
            }
            if (!user.location.isNullOrBlank()) {
                Text(user.location, fontFamily = Urbanist, fontSize = 13.sp, color = Color(0xFF888888))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportFilterRow(
    current: ReportFilter,
    darkMode: Boolean,
    onChange: (ReportFilter) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportFilter.entries.forEach { filter ->
            val selected = filter == current
            Surface(
                onClick = { onChange(filter) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) HuellitasTeal else if (darkMode) Color(0xFF232F2F) else Color(0xFFF0F0F0)
            ) {
                Text(
                    text = when (filter) {
                        ReportFilter.ACTIVE -> "Activos"
                        ReportFilter.RESOLVED -> "Resueltos"
                        ReportFilter.ALL -> "Todos"
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontFamily = Urbanist,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (selected) Color.White else if (darkMode) Color.White else Color(0xFF3D3D3D)
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    darkMode: Boolean,
    icon: @Composable (Color) -> Unit
) {
    val iconTint = if (enabled) {
        if (darkMode) Color.White else Color(0xFF3D3D3D)
    } else {
        Color(0xFF888888)
    }
    val titleColor = if (enabled) {
        if (darkMode) Color.White else Color(0xFF1C1C1C)
    } else {
        Color(0xFF888888)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon(iconTint)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Urbanist, fontSize = 15.sp, color = titleColor)
            Text(subtitle, fontFamily = Urbanist, fontSize = 12.sp, color = Color(0xFF888888))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = HuellitasTeal,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD7D0DD),
                uncheckedBorderColor = Color(0xFFB7AFC0),
                disabledUncheckedTrackColor = Color(0xFFE7E3EA),
                disabledUncheckedBorderColor = Color(0xFFD1CAD8)
            )
        )
    }
}

@Composable
private fun ProfileStatusSection(title: String, subtitle: String, darkMode: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title,
            fontFamily = Urbanist,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (darkMode) Color.White else Color(0xFF1C1C1C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontFamily = Urbanist,
            fontSize = 14.sp,
            color = Color(0xFF888888)
        )
    }
}

@Composable
private fun SectionTitle(text: String, darkMode: Boolean) {
    Text(
        text = text,
        fontFamily = Urbanist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = if (darkMode) Color.White else HuellitasTeal,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ReporteItem(alert: Alert, darkMode: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFDDEEEE)), contentAlignment = Alignment.Center) {
                Text(alert.petName.take(1).uppercase(), fontSize = 12.sp, fontFamily = Urbanist, fontWeight = FontWeight.Bold, color = HuellitasTeal)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(alert.petName, fontFamily = Urbanist, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = if (darkMode) Color.White else Color(0xFF1C1C1C))
                Text("${if (alert.type == AlertType.LOST) "Perdido" else "Encontrado"} - ${timeAgo(alert.createdAt)}", fontFamily = Urbanist, fontSize = 13.sp, color = Color(0xFF888888))
                if (alert.status == AlertStatus.RESOLVED) {
                    Surface(shape = RoundedCornerShape(999.dp), color = HuellitasTeal.copy(alpha = 0.12f)) {
                        Text(
                            text = "Resuelto",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontFamily = Urbanist,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = HuellitasTeal
                        )
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ProfileMessageCard(message: String, darkMode: Boolean, backgroundColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontFamily = Urbanist,
                fontSize = 14.sp,
                color = if (darkMode) Color(0xFFBDBDBD) else Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun CuentaItem(texto: String, isDestructive: Boolean = false, darkMode: Boolean = false, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(texto, fontFamily = Urbanist, fontSize = 15.sp, color = if (isDestructive) Color.Red else if (darkMode) Color.White else Color(0xFF1C1C1C), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
        }
    }
}

private fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        mins < 1 -> "Recién publicado"
        mins < 60 -> "Hace ${mins}min"
        hours < 24 -> "Hace ${hours}h"
        else -> "Hace ${days}d"
    }
}

