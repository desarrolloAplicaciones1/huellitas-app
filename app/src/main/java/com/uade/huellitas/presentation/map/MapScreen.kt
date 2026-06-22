package com.uade.huellitas.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.domain.model.ReferenceLocationSource
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.Urbanist

private val radiusOptions = listOf(1, 3, 5, 10, 20)
private val buenosAires = LatLng(-34.6037, -58.3816)

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (alertId: String) -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isResolvingLocation by viewModel.isResolvingLocation.collectAsStateWithLifecycle()
    var showRadiusSheet by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(true) }
    var selectedAlert by remember { mutableStateOf<MapAlert?>(null) }
    var tempRadius by remember { mutableStateOf(radiusOptions[1]) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.loadUserLocation()
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState) {
        val success = uiState as? MapUiState.Success ?: return@LaunchedEffect
        tempRadius = success.selectedRadiusKm
        selectedAlert = selectedAlert?.let { current -> success.alerts.firstOrNull { it.id == current.id } }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MapUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HuellitasTeal)
                }
            }

            is MapUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = HuellitasTeal, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error, fontFamily = Urbanist)
                    }
                }
            }

            is MapUiState.Success -> {
                GoogleMapView(
                    state = state,
                    onAlertTap = { selectedAlert = it }
                )

                BackButton(onBack = onBack)
                RadiusBadge(
                    selectedRadius = state.selectedRadiusKm,
                    onClick = {
                        tempRadius = state.selectedRadiusKm
                        showRadiusSheet = true
                    }
                )

                if (isResolvingLocation) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = HuellitasTeal,
                        trackColor = HuellitasTeal.copy(alpha = 0.2f)
                    )
                } else if (state.centerSource != ReferenceLocationSource.CURRENT_DEVICE) {
                    Text(
                        mapFallbackMessage(state.centerSource, state.centerLabel),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = Urbanist,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                selectedAlert?.let { alert ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable { selectedAlert = null },
                        contentAlignment = Alignment.Center
                    ) {
                        AlertPopupCard(
                            alert = alert,
                            onDismiss = { selectedAlert = null },
                            onViewDetail = {
                                selectedAlert = null
                                onNavigateToDetail(alert.id)
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showBottomSheet && selectedAlert == null,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    if (dragAmount > 30f) showBottomSheet = false
                                }
                            },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 16.dp
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFDDDDDD))
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "${state.alerts.size} mascotas en tu zona",
                                        fontFamily = Urbanist,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        "Centro: ${state.centerLabel} · Radio: ${state.selectedRadiusKm}km",
                                        fontFamily = Urbanist,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = {
                                    tempRadius = state.selectedRadiusKm
                                    showRadiusSheet = true
                                }) {
                                    Text(
                                        "Cambiar radio",
                                        fontFamily = Urbanist,
                                        fontSize = 13.sp,
                                        color = HuellitasTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            if (state.alerts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "No hay alertas en ${state.selectedRadiusKm}km",
                                            fontFamily = Urbanist,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.alerts, key = { it.id }) { alert ->
                                        PetMapCard(alert = alert, onClick = { selectedAlert = alert })
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                if (!showBottomSheet && selectedAlert == null) {
                    ExtendedFloatingActionButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .navigationBarsPadding(),
                        containerColor = HuellitasTeal,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${state.alerts.size} mascotas", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (showRadiusSheet) {
                    RadiusSheet(
                        selectedRadius = tempRadius,
                        alertCount = state.alerts.size,
                        onRadiusChange = { tempRadius = it },
                        onDismiss = { showRadiusSheet = false },
                        onSave = {
                            viewModel.updateRadius(tempRadius)
                            showRadiusSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleMapView(
    state: MapUiState.Success,
    onAlertTap: (MapAlert) -> Unit
) {
    val center = remember(state.center) {
        if (state.center.latitude != 0.0 || state.center.longitude != 0.0) {
            LatLng(state.center.latitude, state.center.longitude)
        } else {
            buenosAires
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 13f)
    }

    LaunchedEffect(center) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(center, 13f)
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {
        Circle(
            center = center,
            radius = state.selectedRadiusKm * 1000.0,
            fillColor = HuellitasTeal.copy(alpha = 0.07f),
            strokeColor = HuellitasTeal.copy(alpha = 0.55f),
            strokeWidth = 3f
        )

        state.alerts.filter { it.hasPreciseLocation }.forEach { alert ->
            Marker(
                state = MarkerState(
                    position = LatLng(
                        alert.source.location.latitude,
                        alert.source.location.longitude
                    )
                ),
                title = alert.name,
                snippet = "${alert.typeLabel} · ${alert.distanceLabel}",
                onClick = {
                    onAlertTap(alert)
                    true
                }
            )
        }
    }
}

private fun mapFallbackMessage(source: ReferenceLocationSource, centerLabel: String): String =
    when (source) {
        ReferenceLocationSource.USER_PROFILE -> "Usando $centerLabel como referencia"
        ReferenceLocationSource.DEFAULT -> "Activa la ubicacion para ver alertas de tu zona"
        ReferenceLocationSource.CURRENT_DEVICE -> centerLabel
    }

@Composable
private fun BackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .statusBarsPadding()
            .size(40.dp)
            .clip(CircleShape)
            .background(HuellitasTeal)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun BoxScope.RadiusBadge(selectedRadius: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.MyLocation, contentDescription = null, tint = HuellitasTeal, modifier = Modifier.size(16.dp))
            Text("${selectedRadius}km", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1C1C1C))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun BoxScope.RadiusSheet(
    selectedRadius: Int,
    alertCount: Int,
    onRadiusChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() }
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Radio de busqueda", fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("${selectedRadius}KM", fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HuellitasTeal)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${alertCount} mascotas en este radio", fontFamily = Urbanist, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = radiusOptions.indexOf(selectedRadius).toFloat(),
                onValueChange = { onRadiusChange(radiusOptions[it.toInt()]) },
                valueRange = 0f..(radiusOptions.size - 1).toFloat(),
                steps = radiusOptions.size - 2,
                colors = SliderDefaults.colors(
                    thumbColor = HuellitasTeal,
                    activeTrackColor = HuellitasTeal,
                    inactiveTrackColor = Color(0xFFDDDDDD)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                radiusOptions.forEach { km ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedRadius == km) HuellitasTeal else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${km}km",
                            fontFamily = Urbanist,
                            fontSize = 12.sp,
                            fontWeight = if (selectedRadius == km) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRadius == km) Color.White else Color(0xFF888888)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
            ) {
                Text("Guardar", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun AlertPopupCard(
    alert: MapAlert,
    onDismiss: () -> Unit,
    onViewDetail: () -> Unit
) {
    val badgeColor = if (alert.typeLabel == "PERDIDO") Color(0xFFF43F47) else Color(0xFF43A047)

    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clickable(onClick = {}),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = alert.photoUrl ?: "https://images.dog.ceo/breeds/retriever-golden/n02099601_3004.jpg",
                    contentDescription = alert.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(onClick = onDismiss)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(alert.typeLabel, fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Text(alert.distanceLabel, fontFamily = Urbanist, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = Color(0xFF1C1C1C))
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(alert.name, fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (alert.petType == PetType.CAT) Color(0xFF12A99F) else Color(0xFF1E5955))
                    )
                }
                Text(alert.colorLabel, fontFamily = Urbanist, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                alert.address?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, fontFamily = Urbanist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(alert.description, fontFamily = Urbanist, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onViewDetail,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
                ) {
                    Text("Ver detalles", fontFamily = Urbanist, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PetMapCard(alert: MapAlert, onClick: () -> Unit) {
    val badgeColor = if (alert.typeLabel == "PERDIDO") Color(0xFFF43F47) else Color(0xFF43A047)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.width(150.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = alert.photoUrl ?: "https://images.dog.ceo/breeds/retriever-golden/n02099601_3004.jpg",
                    contentDescription = alert.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(badgeColor)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(alert.typeLabel, fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(alert.name, fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = HuellitasTeal, modifier = Modifier.size(10.dp))
                    Text(alert.distanceLabel, fontFamily = Urbanist, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


