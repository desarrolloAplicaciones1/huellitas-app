package com.uade.huellitas.presentation.alert.express

import android.net.Uri
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType

sealed class ExpressAlertUiState {
    object Idle : ExpressAlertUiState()
    object Loading : ExpressAlertUiState()
    object Success : ExpressAlertUiState()
    data class Error(val message: String) : ExpressAlertUiState()
}

data class ExpressAlertFormState(
    val alertType: AlertType = AlertType.LOST,
    val petName: String = "",
    val petType: PetType = PetType.DOG,
    val size: String = "Chico",
    val description: String = "",
    val address: String = "",
    val selectedPhotoUri: Uri? = null
)
