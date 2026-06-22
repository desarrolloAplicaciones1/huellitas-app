package com.uade.huellitas.presentation.map

import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.domain.model.ReferenceLocationSource

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(
        val alerts: List<MapAlert>,
        val selectedRadiusKm: Int,
        val centerLabel: String,
        val center: Location,
        val centerSource: ReferenceLocationSource
    ) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

data class MapAlert(
    val id: String,
    val name: String,
    val typeLabel: String,
    val petType: PetType,
    val distanceLabel: String,
    val distanceMeters: Int?,
    val colorLabel: String,
    val description: String,
    val photoUrl: String?,
    val address: String?,
    val offsetX: Float,
    val offsetY: Float,
    val source: Alert,
    val hasPreciseLocation: Boolean
)

data class MapCenter(
    val label: String,
    val location: Location
)
