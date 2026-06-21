package com.uade.huellitas.presentation.alert.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as HuellitasApplication).appContainer
    private val getAlertByIdUseCase = appContainer.getAlertByIdUseCase
    private val getCurrentUserIdUseCase = appContainer.getCurrentUserIdUseCase
    private val updateAlertUseCase = appContainer.updateAlertUseCase
    private val resolveAlertUseCase = appContainer.resolveAlertUseCase
    private val deleteAlertUseCase = appContainer.deleteAlertUseCase
    private val geocodeAddressUseCase = appContainer.geocodeAddressUseCase

    val isOnline: StateFlow<Boolean> = appContainer.networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    private val _uiState = MutableStateFlow<AlertDetailUiState>(AlertDetailUiState.Loading)
    val uiState: StateFlow<AlertDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun loadAlert(alertId: String) {
        viewModelScope.launch {
            _uiState.value = AlertDetailUiState.Loading
            try {
                val alert = getAlertByIdUseCase(alertId)
                if (alert != null) {
                    val isOwner = getCurrentUserIdUseCase() == alert.ownerId
                    _uiState.value = AlertDetailUiState.Success(alert, isOwner)
                } else {
                    _uiState.value = AlertDetailUiState.Error("Aviso no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = AlertDetailUiState.Error(e.message ?: "Error al cargar aviso")
            }
        }
    }

    fun resolveAlert() {
        val currentState = (_uiState.value as? AlertDetailUiState.Success) ?: return
        if (!ensureOwner(currentState)) return
        if (!isOnline.value) {
            _snackbarMessage.value = "Sin conexión. Esta acción requiere internet."
            return
        }

        viewModelScope.launch {
            try {
                resolveAlertUseCase(currentState.alert)
                val resolved = currentState.alert.copy(
                    status = AlertStatus.RESOLVED,
                    updatedAt = System.currentTimeMillis()
                )
                _uiState.value = AlertDetailUiState.Success(resolved, currentState.isOwner)
                _snackbarMessage.value = "Aviso marcado como resuelto"
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Error al resolver aviso"
            }
        }
    }

    fun updateAlert(
        petName: String,
        breed: String,
        color: String,
        size: String,
        address: String,
        contactPhone: String,
        description: String,
        hasCollar: Boolean?
    ) {
        val currentState = (_uiState.value as? AlertDetailUiState.Success) ?: return
        if (!ensureOwner(currentState)) return
        if (!isOnline.value) {
            _snackbarMessage.value = "Sin conexión. Los cambios no se pueden guardar sin internet."
            return
        }

        viewModelScope.launch {
            try {
                val resolvedLocation = resolveLocation(
                    currentLocation = currentState.alert.location,
                    editedAddress = address
                )
                val updated = currentState.alert.copy(
                    petName = petName.trim(),
                    breed = breed.trim().ifBlank { null },
                    description = description.trim(),
                    color = color.trim().ifBlank { null },
                    size = size.trim().ifBlank { null },
                    location = resolvedLocation,
                    contactPhone = contactPhone.trim().ifBlank { null },
                    hasCollar = hasCollar,
                    updatedAt = System.currentTimeMillis()
                )
                updateAlertUseCase(updated)
                _uiState.value = AlertDetailUiState.Success(updated, currentState.isOwner)
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Error al actualizar aviso"
            }
        }
    }

    fun saveNameEdit(newName: String) {
        val currentState = (_uiState.value as? AlertDetailUiState.Success) ?: return
        if (!ensureOwner(currentState)) return
        if (!isOnline.value) {
            _snackbarMessage.value = "Sin conexión. Los cambios no se pueden guardar sin internet."
            return
        }

        viewModelScope.launch {
            try {
                val updated = currentState.alert.copy(
                    petName = newName.trim(),
                    updatedAt = System.currentTimeMillis()
                )
                updateAlertUseCase(updated)
                _uiState.value = AlertDetailUiState.Success(updated, currentState.isOwner)
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Error al guardar nombre"
            }
        }
    }

    fun saveDescriptionEdit(description: String) {
        val currentState = (_uiState.value as? AlertDetailUiState.Success) ?: return
        if (!ensureOwner(currentState)) return
        if (!isOnline.value) {
            _snackbarMessage.value = "Sin conexión. Los cambios no se pueden guardar sin internet."
            return
        }

        viewModelScope.launch {
            try {
                val updated = currentState.alert.copy(
                    description = description.trim(),
                    updatedAt = System.currentTimeMillis()
                )
                updateAlertUseCase(updated)
                _uiState.value = AlertDetailUiState.Success(updated, currentState.isOwner)
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Error al guardar descripcion"
            }
        }
    }

    fun saveColorEdit(color: String) {
        val currentState = (_uiState.value as? AlertDetailUiState.Success) ?: return
        if (!ensureOwner(currentState)) return
        if (!isOnline.value) {
            _snackbarMessage.value = "Sin conexión. Los cambios no se pueden guardar sin internet."
            return
        }

        viewModelScope.launch {
            try {
                val updated = currentState.alert.copy(
                    color = color.trim().ifBlank { null },
                    updatedAt = System.currentTimeMillis()
                )
                updateAlertUseCase(updated)
                _uiState.value = AlertDetailUiState.Success(updated, currentState.isOwner)
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Error al guardar color"
            }
        }
    }

    fun deleteAlert() {
        val currentState = (_uiState.value as? AlertDetailUiState.Success) ?: return
        if (!ensureOwner(currentState)) return
        if (!isOnline.value) {
            _snackbarMessage.value = "Sin conexión. No es posible eliminar sin internet."
            return
        }

        viewModelScope.launch {
            try {
                deleteAlertUseCase(currentState.alert)
                _uiState.value = AlertDetailUiState.Deleted
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Error al eliminar aviso"
            }
        }
    }

    private fun ensureOwner(currentState: AlertDetailUiState.Success): Boolean {
        if (currentState.isOwner) return true
        _snackbarMessage.value = "Solo el autor puede modificar este aviso"
        return false
    }

    private suspend fun resolveLocation(currentLocation: Location, editedAddress: String): Location {
        val trimmedAddress = editedAddress.trim()
        val currentAddress = currentLocation.address.orEmpty().trim()

        if (trimmedAddress.isBlank() || trimmedAddress.equals(currentAddress, ignoreCase = true)) {
            return currentLocation.copy(address = currentLocation.address ?: trimmedAddress.ifBlank { null })
        }

        return geocodeAddressUseCase(buildGeocodingQuery(trimmedAddress, currentAddress))
            ?: currentLocation.copy(address = trimmedAddress)
    }

    private fun buildGeocodingQuery(address: String, currentAddress: String): String {
        if (address.contains(",")) return address
        return listOf(address, currentAddress.ifBlank { DEFAULT_LOCATION_HINT })
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    private companion object {
        private const val DEFAULT_LOCATION_HINT = "CABA, Argentina"
    }
}
