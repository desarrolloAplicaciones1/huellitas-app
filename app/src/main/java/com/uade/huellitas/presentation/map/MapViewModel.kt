package com.uade.huellitas.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as HuellitasApplication).appContainer
    private val getActiveAlertsUseCase = appContainer.getActiveAlertsUseCase
    private val getAppSettingsUseCase = appContainer.getAppSettingsUseCase
    private val setAlertRadiusUseCase = appContainer.setAlertRadiusUseCase
    private val getCurrentUserUseCase = appContainer.getCurrentUserUseCase
    private val resolveReferenceLocationUseCase = appContainer.resolveReferenceLocationUseCase
    private val calculateDistanceMetersUseCase = appContainer.calculateDistanceMetersUseCase
    private val filterAlertsByRadiusUseCase = appContainer.filterAlertsByRadiusUseCase
    private val locationRefreshTrigger = MutableStateFlow(0)

    private val _isResolvingLocation = MutableStateFlow(false)
    val isResolvingLocation: StateFlow<Boolean> = _isResolvingLocation.asStateFlow()

    val uiState: StateFlow<MapUiState> = combine(
        getActiveAlertsUseCase(),
        getAppSettingsUseCase(),
        getCurrentUserUseCase(),
        locationRefreshTrigger
    ) { alerts, settings, user, _ ->
        Triple(alerts, settings, user)
    }
        .mapLatest { (alerts, settings, user) ->
            val referenceLocation = resolveReferenceLocationUseCase(user?.location)
            val filteredAlerts = filterAlertsByRadiusUseCase(
                alerts = alerts,
                center = referenceLocation.location,
                radiusKm = settings.alertRadiusKm
            )

            val mapAlerts = filteredAlerts.map { alert ->
                alert.toMapAlert(referenceLocation.location)
            }

            _isResolvingLocation.value = false
            MapUiState.Success(
                alerts = mapAlerts,
                selectedRadiusKm = settings.alertRadiusKm,
                centerLabel = referenceLocation.label,
                center = referenceLocation.location,
                centerSource = referenceLocation.source
            ) as MapUiState
        }
        .catch { e ->
            _isResolvingLocation.value = false
            emit(MapUiState.Error(e.message ?: "Error al cargar el mapa"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState.Loading
        )

    fun updateRadius(radiusKm: Int) {
        viewModelScope.launch {
            setAlertRadiusUseCase(radiusKm)
        }
    }

    fun refreshReferenceLocation() {
        locationRefreshTrigger.update { current -> current + 1 }
    }

    @SuppressLint("MissingPermission")
    fun loadUserLocation() {
        val hasPermission =
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        _isResolvingLocation.value = hasPermission
        refreshReferenceLocation()
    }

    private fun Alert.toMapAlert(center: Location): MapAlert {
        val distanceMeters = calculateDistanceMetersUseCase(center, location)
        val preciseLocation = distanceMeters != null
        val (offsetX, offsetY) = calculateOffsets(center, preciseLocation)

        return MapAlert(
            id = id,
            name = petName,
            typeLabel = if (type.name == "LOST") "PERDIDO" else "ENCONTRADO",
            petType = petType,
            distanceLabel = distanceMeters?.let(::formatDistance) ?: (location.address ?: "Ubicacion sin precision"),
            distanceMeters = distanceMeters,
            colorLabel = color ?: breed ?: size ?: "Sin datos",
            description = description,
            photoUrl = photoUrls.firstOrNull(),
            address = location.address,
            offsetX = offsetX,
            offsetY = offsetY,
            source = this,
            hasPreciseLocation = preciseLocation
        )
    }

    private fun Alert.calculateOffsets(center: Location, preciseLocation: Boolean): Pair<Float, Float> {
        if (!preciseLocation) {
            val hash = id.hashCode()
            val x = 0.18f + ((hash ushr 8) and 0xFF) / 255f * 0.64f
            val y = 0.20f + (hash and 0xFF) / 255f * 0.58f
            return x to y
        }

        val lngDelta = (location.longitude - center.longitude).toFloat()
        val latDelta = (location.latitude - center.latitude).toFloat()
        val x = (0.5f + lngDelta * 45f).coerceIn(0.12f, 0.88f)
        val y = (0.5f - latDelta * 60f).coerceIn(0.16f, 0.84f)
        return x to y
    }

    private fun formatDistance(distanceMeters: Int): String {
        return if (distanceMeters < 1000) {
            "a ${distanceMeters}m"
        } else {
            val distanceKm = distanceMeters / 1000.0
            "a ${"%.1f".format(distanceKm)}km"
        }
    }
}
