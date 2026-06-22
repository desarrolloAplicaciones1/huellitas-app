package com.uade.huellitas.presentation.profile.pets

import android.net.Uri
import com.uade.huellitas.domain.model.PetType

sealed class CreatePetUiState {
    object Idle : CreatePetUiState()
    object Loading : CreatePetUiState()
    object Success : CreatePetUiState()
    data class Error(val message: String) : CreatePetUiState()
}

data class CreatePetFormState(
    val name: String = "",
    val petType: PetType = PetType.DOG,
    val breed: String = "",
    val color: String = "",
    val description: String = "",
    val microchipId: String = "",
    val selectedPhotoUri: Uri? = null
)
