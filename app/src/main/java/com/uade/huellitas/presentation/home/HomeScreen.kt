package com.uade.huellitas.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.presentation.location.RequestLocationPermissionEffect
import com.uade.huellitas.presentation.shared.AlertCard as SharedAlertCard
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.HuellitasTealSurface
import com.uade.huellitas.ui.theme.StatusFound
import com.uade.huellitas.ui.theme.StatusLost
import com.uade.huellitas.ui.theme.ThemeState
import com.uade.huellitas.ui.theme.Urbanist

private val FooterColor = Color(0xFFE8F7F6)
private val radiusOptions = listOf(1, 3, 5, 10, 20)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (alertId: String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToCreateAlert: () -> Unit = {},
    onNavigateToExpressAlert: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val isOnline    by viewModel.isOnline.collectAsStateWithLifecycle()
    val currentUserName = (uiState as? HomeUiState.Success)?.currentUserName
    val hasActiveAdvancedFilters = filterState.petType != null ||
        filterState.alertType != null ||
        filterState.radiusKm != HomeFilterState().radiusKm
    var showFilterDialog by remember { mutableStateOf(false) }
    var showReportSheet  by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    RequestLocationPermissionEffect(
        onPermissionGranted = viewModel::refreshReferenceLocation
    )

    if (showFilterDialog) {
        FilterDialog(
            currentPetType   = filterState.petType,
            currentAlertType = filterState.alertType,
            currentRadiusKm  = filterState.radiusKm,
            onApply = { petType, alertType, radiusKm ->
                viewModel.setFilter(petType = petType, alertType = alertType, radiusKm = radiusKm)
                showFilterDialog = false
            },
            onClearFilter = { viewModel.clearAdvancedFilters(); showFilterDialog = false },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showReportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReportSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text("¿Qué querés reportar?", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                    fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = { showReportSheet = false; onNavigateToCreateAlert() },
                        modifier = Modifier.weight(1f).height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE8F7F6),
                        border = BorderStroke(1.5.dp, HuellitasTeal)
                    ) {
                        Column(modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null,
                                tint = HuellitasTeal, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Me perdí / Encontré", fontFamily = Urbanist,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = Color(0xFF1C1C1C), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Para reportes detallados", fontFamily = Urbanist,
                                fontSize = 12.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
                        }
                    }
                    Surface(
                        onClick = { showReportSheet = false; onNavigateToExpressAlert() },
                        modifier = Modifier.weight(1f).height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE8F7F6),
                        border = BorderStroke(1.5.dp, HuellitasTeal)
                    ) {
                        Column(modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.FlashOn, contentDescription = null,
                                tint = HuellitasTeal, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Vi algo rápido", fontFamily = Urbanist,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = Color(0xFF1C1C1C), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sin foto, solo zona", fontFamily = Urbanist,
                                fontSize = 12.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 72.dp)) {
            HomeHeader(
                userName = currentUserName,
                onFilterClick = { showFilterDialog = true }
            )
            SearchSection(
                query = filterState.query,
                onQueryChange = viewModel::updateSearchQuery,
                onClearQuery = { viewModel.updateSearchQuery("") }
            )
            CreateAlertBanner(onClick = { showReportSheet = true })
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OfflineBanner()
            }
            FeedSummarySection(
                filterState = filterState,
                alertsCount = (uiState as? HomeUiState.Success)?.alerts?.size ?: 0,
                hasActiveAdvancedFilters = hasActiveAdvancedFilters,
                onOpenFilters = { showFilterDialog = true },
                onClearAdvancedFilters = viewModel::clearAdvancedFilters
            )

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HuellitasTeal)
                    }
                }
                is HomeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is HomeUiState.Empty -> {
                    EmptyAlertsState(
                        query = filterState.query,
                        hasActiveFilters = hasActiveAdvancedFilters,
                        modifier = Modifier.fillMaxSize(),
                        onClearFilters = viewModel::clearAllFilters
                    )
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.alerts, key = { it.id }) { alert ->
                            SharedAlertCard(alert = alert, onClick = { onNavigateToDetail(alert.id) })
                        }
                    }
                }
            }
        }

        HomeBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHome = {},
            onAdd = { showReportSheet = true },
            onMap = onNavigateToMap,
            onProfile = onNavigateToProfile
        )
    }
}

@Composable
private fun HomeHeader(userName: String?, onFilterClick: () -> Unit) {
    val greeting = userName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { "Hola, $it!" }
        ?: "Hola!"

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(greeting, fontFamily = Urbanist, fontWeight = FontWeight.Normal,
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("¿Todo listo para ayudar?", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterList, contentDescription = "Filtrar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape)
            .background(FooterColor), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones",
                tint = Color.Black, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = {
            Text(
                "Buscar por nombre de mascota",
                fontFamily = Urbanist,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HuellitasTeal,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedSummarySection(
    filterState: HomeFilterState,
    alertsCount: Int,
    hasActiveAdvancedFilters: Boolean,
    onOpenFilters: () -> Unit,
    onClearAdvancedFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (alertsCount == 1) "1 aviso visible" else "$alertsCount avisos visibles",
                fontFamily = Urbanist,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onOpenFilters) {
                Text("Ajustar filtros", fontFamily = Urbanist, color = HuellitasTeal)
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filterState.query.isNotBlank()) {
                ActiveFilterChip("Nombre: ${filterState.query}")
            }
            if (filterState.alertType != null) {
                ActiveFilterChip(
                    if (filterState.alertType == AlertType.LOST) "Perdidos" else "Encontrados"
                )
            }
            if (filterState.petType != null) {
                ActiveFilterChip(
                    if (filterState.petType == PetType.DOG) "Perros" else "Gatos"
                )
            }
            if (filterState.radiusKm != HomeFilterState().radiusKm) {
                ActiveFilterChip("Radio: ${filterState.radiusKm} km")
            }
        }

        if (hasActiveAdvancedFilters) {
            TextButton(
                onClick = onClearAdvancedFilters,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Limpiar filtros avanzados", fontFamily = Urbanist, color = HuellitasTeal)
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = HuellitasTeal.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontFamily = Urbanist,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = HuellitasTeal
        )
    }
}

@Composable
private fun FilterDialog(
    currentPetType: PetType?,
    currentAlertType: AlertType?,
    currentRadiusKm: Int,
    onApply: (PetType?, AlertType?, Int) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPetType   by remember { mutableStateOf(currentPetType) }
    var selectedAlertType by remember { mutableStateOf(currentAlertType) }
    var selectedRadius    by remember { mutableIntStateOf(currentRadiusKm) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Filtrar avisos", fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tipo de aviso", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipOption("Perdidos", selectedAlertType == AlertType.LOST) {
                        selectedAlertType = if (selectedAlertType == AlertType.LOST) null else AlertType.LOST
                    }
                    FilterChipOption("Encontrados", selectedAlertType == AlertType.FOUND) {
                        selectedAlertType = if (selectedAlertType == AlertType.FOUND) null else AlertType.FOUND
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Especie", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipOption("Perros", selectedPetType == PetType.DOG) {
                        selectedPetType = if (selectedPetType == PetType.DOG) null else PetType.DOG
                    }
                    FilterChipOption("Gatos", selectedPetType == PetType.CAT) {
                        selectedPetType = if (selectedPetType == PetType.CAT) null else PetType.CAT
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rango de cercanía", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                val sliderIndex = radiusOptions.indexOf(selectedRadius).takeIf { it >= 0 } ?: 2
                Slider(
                    value = sliderIndex.toFloat(),
                    onValueChange = { selectedRadius = radiusOptions[it.toInt()] },
                    valueRange = 0f..(radiusOptions.size - 1).toFloat(),
                    steps = radiusOptions.size - 2,
                    colors = SliderDefaults.colors(thumbColor = HuellitasTeal,
                        activeTrackColor = HuellitasTeal, inactiveTrackColor = Color(0xFFDDDDDD)),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    radiusOptions.forEach { km ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (selectedRadius == km) HuellitasTeal else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("${km}km", fontFamily = Urbanist, fontSize = 12.sp,
                                fontWeight = if (selectedRadius == km) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedRadius == km) Color.White else Color(0xFF888888))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onClearFilter, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)) {
                        Text("Limpiar", fontFamily = Urbanist, color = Color.Gray)
                    }
                    Button(onClick = { onApply(selectedPetType, selectedAlertType, selectedRadius) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)) {
                        Text("Aplicar", fontFamily = Urbanist, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
        .background(if (selected) HuellitasTeal else Color(0xFFF0F0F0))
        .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, fontFamily = Urbanist, fontWeight = FontWeight.Medium, fontSize = 13.sp,
            color = if (selected) Color.White else Color(0xFF3D3D3D))
    }
}

@Composable
private fun CreateAlertBanner(onClick: () -> Unit) {
    val isDark = ThemeState.isDarkMode
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) HuellitasTeal else HuellitasTealSurface,
        contentColor = if (isDark) Color.White else HuellitasTeal
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.2f) else HuellitasTeal),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FlashOn, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Reportar mascota", fontFamily = Urbanist,
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null,
                modifier = Modifier.size(18.dp))
        }
    }
}
@Composable
private fun OfflineBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Sin conexión · Mostrando datos guardados",
                fontFamily = Urbanist,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyAlertsState(
    query: String,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier,
    onClearFilters: () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("Sin resultados", fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    query.isNotBlank() -> "No encontramos avisos para \"$query\"."
                    hasActiveFilters -> "No encontramos avisos con los filtros actuales."
                    else -> "Todavía no hay avisos cercanos para mostrar."
                },
                fontFamily = Urbanist,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Probá ampliar el radio, cambiar filtros o publicar un nuevo reporte.",
                fontFamily = Urbanist,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (query.isNotBlank() || hasActiveFilters) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
                ) {
                    Text("Limpiar filtros", fontFamily = Urbanist, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    modifier: Modifier = Modifier,
    onHome: () -> Unit,
    onAdd: () -> Unit,
    onMap: () -> Unit,
    onProfile: () -> Unit
) {
    val isDark = ThemeState.isDarkMode
    val bgColor = if (isDark) Color(0xFF1C2626) else Color(0xFFE8F7F6)
    val iconColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp)
            .background(bgColor)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Default.Home, contentDescription = "Inicio",
                    tint = iconColor, modifier = Modifier.size(26.dp))
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Publicar",
                    tint = iconColor, modifier = Modifier.size(26.dp))
            }
            IconButton(onClick = onMap) {
                Icon(Icons.Default.Map, contentDescription = "Mapa",
                    tint = iconColor, modifier = Modifier.size(26.dp))
            }
            IconButton(onClick = onProfile) {
                Icon(Icons.Default.Person, contentDescription = "Perfil",
                    tint = iconColor, modifier = Modifier.size(26.dp))
            }
        }
    }
}
