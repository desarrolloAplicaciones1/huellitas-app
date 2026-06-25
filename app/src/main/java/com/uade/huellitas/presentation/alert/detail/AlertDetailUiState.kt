package com.uade.huellitas.presentation.alert.detail

import com.uade.huellitas.domain.model.Alert

sealed class AlertDetailUiState {
    object Loading                               : AlertDetailUiState()
    data class Success(val alert: Alert, val isOwner: Boolean = false, val ownerName: String? = null) : AlertDetailUiState()
    data class Error(val message: String)        : AlertDetailUiState()
    object Deleted                               : AlertDetailUiState()
    object Resolved                              : AlertDetailUiState()
}
