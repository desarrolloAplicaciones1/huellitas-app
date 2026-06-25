package com.uade.huellitas.presentation.alert.create

import android.net.Uri
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType

sealed class CreateAlertUiState {
    object Idle                           : CreateAlertUiState()
    object Loading                        : CreateAlertUiState()
    object UploadingPhoto                 : CreateAlertUiState()
    object Success                        : CreateAlertUiState()
    data class Error(val message: String) : CreateAlertUiState()
}

data class CreateAlertFormState(
    val alertType: AlertType = AlertType.LOST,
    val petName: String = "",
    val petType: PetType = PetType.DOG,
    val breed: String = "",
    val color: String = "",
    val size: String = "Chico",
    val hasCollar: Boolean = false,
    val isCastrated: Boolean = false,
    val description: String = "",
    val contactPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val street: String = "",
    val selectedPhotoUri: Uri? = null
)
