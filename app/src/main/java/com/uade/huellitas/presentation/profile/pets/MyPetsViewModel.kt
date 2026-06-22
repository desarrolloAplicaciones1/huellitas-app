package com.uade.huellitas.presentation.profile.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.domain.model.Pet
import com.uade.huellitas.domain.usecase.pet.DeletePetUseCase
import com.uade.huellitas.domain.usecase.pet.GetMyPetsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyPetsViewModel(
    private val getMyPetsUseCase: GetMyPetsUseCase,
    private val deletePetUseCase: DeletePetUseCase
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<MyPetsUiState> = _retryTrigger
        .flatMapLatest {
            getMyPetsUseCase()
                .map { pets ->
                    if (pets.isEmpty()) MyPetsUiState.Empty
                    else MyPetsUiState.Success(pets)
                }
                .catch { e ->
                    emit(MyPetsUiState.Error(e.message ?: "Error al cargar mascotas"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MyPetsUiState.Loading
        )

    fun retry() {
        _retryTrigger.value++
    }

    fun deletePet(pet: Pet) {
        viewModelScope.launch {
            runCatching { deletePetUseCase(pet) }
        }
    }
}
