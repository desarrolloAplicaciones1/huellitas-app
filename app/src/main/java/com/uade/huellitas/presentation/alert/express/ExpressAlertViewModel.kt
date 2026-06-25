package com.uade.huellitas.presentation.alert.express

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ExpressAlertViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as HuellitasApplication).appContainer
    private val createAlertUseCase = appContainer.createAlertUseCase
    private val getCurrentUserIdUseCase = appContainer.getCurrentUserIdUseCase
    private val geocodeAddressUseCase = appContainer.geocodeAddressUseCase
    private val uploadAlertPhotoUseCase = appContainer.uploadAlertPhotoUseCase

    private val _uiState = MutableStateFlow<ExpressAlertUiState>(ExpressAlertUiState.Idle)
    val uiState: StateFlow<ExpressAlertUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ExpressAlertFormState())
    val formState: StateFlow<ExpressAlertFormState> = _formState.asStateFlow()

    fun onAlertTypeChange(type: com.uade.huellitas.domain.model.AlertType) {
        _formState.value = _formState.value.copy(alertType = type)
    }

    fun onPetNameChange(value: String) {
        _formState.value = _formState.value.copy(petName = value)
    }

    fun onPetTypeChange(type: com.uade.huellitas.domain.model.PetType) {
        _formState.value = _formState.value.copy(petType = type)
    }

    fun onSizeChange(value: String) {
        _formState.value = _formState.value.copy(size = value)
    }

    fun onDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun onAddressChange(value: String) {
        _formState.value = _formState.value.copy(address = value)
    }

    fun onPhotoSelected(uri: Uri) {
        _formState.value = _formState.value.copy(selectedPhotoUri = uri)
    }

    fun clearPhoto() {
        _formState.value = _formState.value.copy(selectedPhotoUri = null)
    }

    fun publishAlert() {
        val ownerId = getCurrentUserIdUseCase() ?: run {
            _uiState.value = ExpressAlertUiState.Error("No hay sesion activa")
            return
        }
        val form = _formState.value

        viewModelScope.launch {
            _uiState.value = ExpressAlertUiState.Loading
            try {
                val now = System.currentTimeMillis()
                val resolvedLocation = geocodeAddressUseCase(form.address)
                    ?: Location(0.0, 0.0, form.address.ifBlank { null })

                val expressDescription = form.description.ifBlank {
                    "Reporte express en ${form.address}"
                }

                val uploadedPhotoUrl = form.selectedPhotoUri?.let { uri ->
                    runCatching { uploadAlertPhotoUseCase(ownerId, uri.toString()) }.getOrNull()
                }

                createAlertUseCase(
                    Alert(
                        id = UUID.randomUUID().toString(),
                        ownerId = ownerId,
                        type = form.alertType,
                        status = AlertStatus.ACTIVE,
                        petName = form.petName.ifBlank { "Sin nombre" },
                        petType = form.petType,
                        size = form.size,
                        description = expressDescription,
                        photoUrls = listOfNotNull(uploadedPhotoUrl),
                        location = resolvedLocation,
                        contactPhone = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                _uiState.value = ExpressAlertUiState.Success
            } catch (e: Exception) {
                _uiState.value = ExpressAlertUiState.Error(e.message ?: "Error al publicar alerta")
            }
        }
    }

    fun resetState() {
        _uiState.value = ExpressAlertUiState.Idle
    }
}
