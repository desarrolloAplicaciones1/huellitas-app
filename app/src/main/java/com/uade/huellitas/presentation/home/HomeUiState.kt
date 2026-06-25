package com.uade.huellitas.presentation.home

import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty   : HomeUiState()
    data class Success(
        val alerts: List<Alert>,
        val currentUserName: String? = null
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class HomeFilterState(
    val query: String = "",
    val petType: PetType? = null,
    val alertType: AlertType? = null,
    val radiusKm: Int = 15
)
