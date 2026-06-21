package com.uade.huellitas.presentation.alert.create

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.showAlertNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CreateAlertViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as HuellitasApplication).appContainer
    private val createAlertUseCase = appContainer.createAlertUseCase
    private val getCurrentUserIdUseCase = appContainer.getCurrentUserIdUseCase
    private val getCurrentUserUseCase = appContainer.getCurrentUserUseCase
    private val geocodeAddressUseCase = appContainer.geocodeAddressUseCase
    private val uploadAlertPhotoUseCase = appContainer.uploadAlertPhotoUseCase

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

    fun onPhotoSelected(uri: Uri) {
        _formState.value = _formState.value.copy(selectedPhotoUri = uri)
    }

    fun submitAlert() {
        val form = _formState.value
        val ownerId = getCurrentUserIdUseCase() ?: run {
            _uiState.value = CreateAlertUiState.Error("No hay sesion activa. Iniciá sesión e intentá de nuevo.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateAlertUiState.Loading
            try {
                val now = System.currentTimeMillis()

                val userLocationHint = try {
                    getCurrentUserUseCase().first()?.location
                } catch (_: Exception) { null }

                val resolvedLocation = resolveLocation(form, userLocationHint)

                val uploadedPhotoUrl = form.selectedPhotoUri?.let { uri ->
                    _uiState.value = CreateAlertUiState.UploadingPhoto
                    runCatching { uploadAlertPhotoUseCase(ownerId, uri.toString()) }
                        .onSuccess { _uiState.value = CreateAlertUiState.Loading }
                        .getOrElse { error ->
                            throw IllegalStateException(
                                "No se pudo subir la foto. El aviso puede publicarse sin foto: eliminá la imagen y volvé a intentarlo.",
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
                val notifBody = "${form.petName.trim()} — ${form.address.ifBlank { "sin dirección" }}"

                runCatching {
                    FirebaseFirestore.getInstance()
                        .collection("notifications")
                        .add(
                            mapOf(
                                "title" to notifTitle,
                                "body" to notifBody,
                                "alertId" to alert.id,
                                "createdAt" to alert.createdAt
                            )
                        ).await()
                }

                showAlertNotification(getApplication(), notifTitle, notifBody)

                _uiState.value = CreateAlertUiState.Success
            } catch (e: Exception) {
                _uiState.value = CreateAlertUiState.Error(
                    e.message ?: "No se pudo publicar. Revisá tu conexión."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateAlertUiState.Idle
    }

    private suspend fun resolveLocation(form: CreateAlertFormState, userLocationHint: String?): Location {
        if (form.address.isNotBlank()) {
            val query = buildGeocodingQuery(form.address, userLocationHint)
            val geocoded = runCatching { geocodeAddressUseCase(query) }.getOrNull()
            if (geocoded != null) return geocoded
        }
        return Location(
            latitude = form.latitude ?: DEFAULT_LATITUDE,
            longitude = form.longitude ?: DEFAULT_LONGITUDE,
            address = form.address.ifBlank { null }
        )
    }

    private fun buildGeocodingQuery(address: String, userLocationHint: String?): String {
        val normalizedAddress = address.trim()
        if (normalizedAddress.contains(",")) return normalizedAddress
        val contextHint = userLocationHint?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_LOCATION_HINT
        return "$normalizedAddress, $contextHint"
    }

    companion object {
        private const val DEFAULT_LOCATION_HINT = "CABA, Argentina"
        private const val DEFAULT_LATITUDE = -34.6037
        private const val DEFAULT_LONGITUDE = -58.3816
    }
}
