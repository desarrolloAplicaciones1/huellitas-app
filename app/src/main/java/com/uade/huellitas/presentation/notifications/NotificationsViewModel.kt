package com.uade.huellitas.presentation.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.domain.model.Alert
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as HuellitasApplication).appContainer
    private val getActiveAlertsUseCase = appContainer.getActiveAlertsUseCase
    private val getCurrentUserIdUseCase = appContainer.getCurrentUserIdUseCase

    val alerts: StateFlow<List<Alert>> = getActiveAlertsUseCase()
        .map { list ->
            val currentUid = getCurrentUserIdUseCase()
            list.filter { it.ownerId != currentUid }
                .sortedByDescending { it.createdAt }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
