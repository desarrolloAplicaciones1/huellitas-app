package com.uade.huellitas.presentation.profile.pets

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.Pet
import com.uade.huellitas.domain.model.PetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CreatePetViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as HuellitasApplication).appContainer
    private val savePetUseCase = appContainer.savePetUseCase
    private val getCurrentUserIdUseCase = appContainer.getCurrentUserIdUseCase
    private val uploadPetPhotoUseCase = appContainer.uploadPetPhotoUseCase

    private val _uiState = MutableStateFlow<CreatePetUiState>(CreatePetUiState.Idle)
    val uiState: StateFlow<CreatePetUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CreatePetFormState())
    val formState: StateFlow<CreatePetFormState> = _formState.asStateFlow()

    fun onNameChange(value: String) { _formState.value = _formState.value.copy(name = value) }
    fun onPetTypeChange(type: PetType) { _formState.value = _formState.value.copy(petType = type) }
    fun onBreedChange(value: String) { _formState.value = _formState.value.copy(breed = value) }
    fun onColorChange(value: String) { _formState.value = _formState.value.copy(color = value) }
    fun onDescriptionChange(value: String) { _formState.value = _formState.value.copy(description = value) }
    fun onMicrochipChange(value: String) { _formState.value = _formState.value.copy(microchipId = value) }
    fun onPhotoSelected(uri: Uri) { _formState.value = _formState.value.copy(selectedPhotoUri = uri) }

    fun savePet() {
        val form = _formState.value
        if (form.name.isBlank()) {
            _uiState.value = CreatePetUiState.Error("El nombre de la mascota es obligatorio.")
            return
        }
        val ownerId = getCurrentUserIdUseCase() ?: run {
            _uiState.value = CreatePetUiState.Error("No hay sesión activa.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreatePetUiState.Loading
            try {
                val uploadedPhotoUrl = form.selectedPhotoUri?.let { uri ->
                    runCatching { uploadPetPhotoUseCase(ownerId, uri.toString()) }
                        .getOrElse { throw IllegalStateException("No se pudo subir la foto. Intentá sin imagen.") }
                }
                val pet = Pet(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    name = form.name.trim(),
                    petType = form.petType,
                    breed = form.breed.ifBlank { null },
                    color = form.color.ifBlank { null },
                    description = form.description.ifBlank { null },
                    photoUrls = if (uploadedPhotoUrl != null) listOf(uploadedPhotoUrl) else emptyList(),
                    microchipId = form.microchipId.ifBlank { null },
                    createdAt = System.currentTimeMillis()
                )
                savePetUseCase(pet)
                _uiState.value = CreatePetUiState.Success
            } catch (e: Exception) {
                _uiState.value = CreatePetUiState.Error(e.message ?: "Error al guardar la mascota.")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreatePetUiState.Idle
    }
}
