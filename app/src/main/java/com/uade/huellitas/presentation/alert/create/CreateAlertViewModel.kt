package com.uade.huellitas.presentation.alert.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.domain.usecase.alert.CreateAlertUseCase
import com.uade.huellitas.domain.usecase.auth.GetCurrentUserIdUseCase
import com.uade.huellitas.domain.usecase.location.GeocodeAddressUseCase
import com.uade.huellitas.domain.usecase.media.UploadAlertPhotoUseCase
import com.uade.huellitas.domain.usecase.user.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class CreateAlertViewModel(
    private val createAlertUseCase: CreateAlertUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val geocodeAddressUseCase: GeocodeAddressUseCase,
    private val uploadAlertPhotoUseCase: UploadAlertPhotoUseCase,
    private val persistPublishedNotification: suspend (String, String, String, Long) -> Unit =
        { _, _, _, _ -> },
    private val showLocalNotification: (String, String) -> Unit = { _, _ -> }
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateAlertUiState>(CreateAlertUiState.Idle)
    val uiState: StateFlow<CreateAlertUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateAlertFormState())
    val formState: StateFlow<CreateAlertFormState> = _formState.asStateFlow()

    private var contactPhoneInitialized = false

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { user ->
                val phone = user?.phone?.trim().orEmpty()
                if (!contactPhoneInitialized && phone.isNotEmpty()) {
                    _formState.value = _formState.value.copy(contactPhone = phone)
                    contactPhoneInitialized = true
                }
            }
        }
    }

    fun onAlertTypeChange(type: AlertType) { _formState.value = _formState.value.copy(alertType = type) }
    fun onPetNameChange(value: String) { _formState.value = _formState.value.copy(petName = value) }
    fun onPetTypeChange(type: PetType) { _formState.value = _formState.value.copy(petType = type) }
    fun onBreedChange(value: String) { _formState.value = _formState.value.copy(breed = value) }
    fun onColorChange(value: String) { _formState.value = _formState.value.copy(color = value) }
    fun onSizeChange(value: String) { _formState.value = _formState.value.copy(size = value) }
    fun onHasCollarChange(value: Boolean) { _formState.value = _formState.value.copy(hasCollar = value) }
    fun onIsCastratedChange(value: Boolean) { _formState.value = _formState.value.copy(isCastrated = value) }
    fun onDescriptionChange(value: String) { _formState.value = _formState.value.copy(description = value) }
    fun onContactPhoneChange(value: String) {
        contactPhoneInitialized = true
        _formState.value = _formState.value.copy(contactPhone = value)
    }

    fun onLocationChange(lat: Double, lng: Double, address: String) {
        _formState.value = _formState.value.copy(latitude = lat, longitude = lng, address = address)
    }

    fun onAddressTyped(address: String) {
        _formState.value = _formState.value.copy(address = address, latitude = null, longitude = null)
    }

    fun onStreetChange(value: String) { _formState.value = _formState.value.copy(street = value) }

    fun onPhotoSelected(uri: Uri) {
        _formState.value = _formState.value.copy(selectedPhotoUri = uri)
    }

    fun submitAlert() {
        val form = _formState.value

        if (form.petName.isBlank()) {
            _uiState.value = CreateAlertUiState.Error("El nombre de la mascota es obligatorio.")
            return
        }

        val ownerId = getCurrentUserIdUseCase() ?: run {
            _uiState.value = CreateAlertUiState.Error("No hay sesion activa. Inicia sesion e intenta de nuevo.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateAlertUiState.Loading
            try {
                val now = System.currentTimeMillis()

                val userLocationHint = try {
                    getCurrentUserUseCase().first()?.location
                } catch (_: Exception) {
                    null
                }

                val resolvedLocation = resolveLocation(form, userLocationHint)

                val uploadedPhotoUrl = form.selectedPhotoUri?.let { uri ->
                    _uiState.value = CreateAlertUiState.UploadingPhoto
                    runCatching { uploadAlertPhotoUseCase(ownerId, uri.toString()) }
                        .onSuccess { _uiState.value = CreateAlertUiState.Loading }
                        .getOrElse { error ->
                            throw IllegalStateException(
                                "No se pudo subir la foto. El aviso puede publicarse sin foto: elimina la imagen y vuelve a intentarlo.",
                                error
                            )
                        }
                }

                val alert = Alert(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    type = form.alertType,
                    status = AlertStatus.ACTIVE,
                    petName = form.petName.trim(),
                    petType = form.petType,
                    breed = form.breed.ifBlank { null },
                    color = form.color.ifBlank { null },
                    size = form.size,
                    hasCollar = form.hasCollar,
                    isCastrated = form.isCastrated,
                    description = form.description.trim(),
                    photoUrls = if (uploadedPhotoUrl != null) listOf(uploadedPhotoUrl) else emptyList(),
                    location = resolvedLocation,
                    contactPhone = form.contactPhone.ifBlank { null },
                    createdAt = now,
                    updatedAt = now
                )

                createAlertUseCase(alert)

                val typeLabel = if (form.alertType == AlertType.LOST) "perdida" else "encontrada"
                val notifTitle = "Nueva mascota $typeLabel cerca tuyo"
                val notifBody = "${form.petName.trim()} - ${form.address.ifBlank { "sin direccion" }}"

                runCatching {
                    persistPublishedNotification(notifTitle, notifBody, alert.id, alert.createdAt)
                }

                _uiState.value = CreateAlertUiState.Success
            } catch (e: Exception) {
                _uiState.value = CreateAlertUiState.Error(
                    e.message ?: "No se pudo publicar. Revisa tu conexion."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateAlertUiState.Idle
    }

    private suspend fun resolveLocation(form: CreateAlertFormState, userLocationHint: String?): Location {
        val displayAddress = listOf(form.street, form.address)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { null }

        if (displayAddress != null) {
            val query = buildGeocodingQuery(form.street, form.address, userLocationHint)
            val geocoded = runCatching { geocodeAddressUseCase(query) }.getOrNull()
            if (geocoded != null) return geocoded.copy(address = displayAddress)
        }
        return Location(
            latitude = form.latitude ?: DEFAULT_LATITUDE,
            longitude = form.longitude ?: DEFAULT_LONGITUDE,
            address = displayAddress ?: form.address.ifBlank { null }
        )
    }

    private fun buildGeocodingQuery(street: String, address: String, userLocationHint: String?): String {
        val parts = listOf(street.trim(), address.trim()).filter { it.isNotBlank() }
        val combined = parts.joinToString(", ")
        if (combined.contains(",")) return combined
        val contextHint = userLocationHint?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_LOCATION_HINT
        return "$combined, $contextHint"
    }

    companion object {
        private const val DEFAULT_LOCATION_HINT = "CABA, Argentina"
        private const val DEFAULT_LATITUDE = -34.6037
        private const val DEFAULT_LONGITUDE = -58.3816
    }
}
