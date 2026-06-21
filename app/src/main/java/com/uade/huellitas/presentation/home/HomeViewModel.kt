package com.uade.huellitas.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.ReferenceLocationSource
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType
import java.text.Normalizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appContainer = (application as HuellitasApplication).appContainer

    private val getActiveAlertsUseCase = appContainer.getActiveAlertsUseCase
    private val getCurrentUserUseCase = appContainer.getCurrentUserUseCase
    private val resolveReferenceLocationUseCase = appContainer.resolveReferenceLocationUseCase
    private val filterAlertsByRadiusUseCase = appContainer.filterAlertsByRadiusUseCase

    private val networkMonitor = appContainer.networkMonitor
    private val pushPendingAlertsUseCase = appContainer.pushPendingAlertsUseCase

    private val _filterState = MutableStateFlow(HomeFilterState())
    private val _locationRefreshTrigger = MutableStateFlow(0)
    val filterState: StateFlow<HomeFilterState> = _filterState.asStateFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .filter { it }
                .collect { runCatching { pushPendingAlertsUseCase() } }
        }
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val uiState: StateFlow<HomeUiState> = combine(
        getActiveAlertsUseCase(),
        _filterState,
        getCurrentUserUseCase(),
        _locationRefreshTrigger
    ) { alerts, filter, user, _ ->
        Triple(alerts, filter, user)
    }
        .mapLatest { (alerts, filter, user) ->
            val normalizedQuery = filter.query.normalizedForSearch()
            val alertsByType = alerts.filter { alert ->
                (filter.petType == null || alert.petType == filter.petType) &&
                    (filter.alertType == null || alert.type == filter.alertType) &&
                    (
                        normalizedQuery.isBlank() ||
                            alert.petName.normalizedForSearch().contains(normalizedQuery)
                        )
            }

            val referenceLocation = resolveReferenceLocationUseCase(user?.location)
            val filteredAlerts = if (referenceLocation.source == ReferenceLocationSource.DEFAULT) {
                alertsByType
            } else {
                filterAlertsByRadiusUseCase(
                    alerts = alertsByType,
                    center = referenceLocation.location,
                    radiusKm = filter.radiusKm
                )
            }

            HomeUiState.Success(
                alerts = filteredAlerts,
                currentUserName = user?.name
            ) as HomeUiState
        }
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Error al cargar avisos")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun updateSearchQuery(query: String) {
        _filterState.update { current -> current.copy(query = query) }
    }

    fun setFilter(petType: PetType? = null, alertType: AlertType? = null, radiusKm: Int = 10) {
        _filterState.update { current ->
            current.copy(
                petType = petType,
                alertType = alertType,
                radiusKm = radiusKm
            )
        }
    }

    fun clearAdvancedFilters() {
        _filterState.update { current ->
            current.copy(
                petType = null,
                alertType = null,
                radiusKm = HomeFilterState().radiusKm
            )
        }
    }

    fun clearAllFilters() {
        _filterState.value = HomeFilterState()
    }

    fun refreshReferenceLocation() {
        _locationRefreshTrigger.update { current -> current + 1 }
    }
}

private fun String.normalizedForSearch(): String = Normalizer
    .normalize(trim(), Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .lowercase()
