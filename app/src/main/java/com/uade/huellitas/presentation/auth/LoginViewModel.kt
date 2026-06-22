package com.uade.huellitas.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uade.huellitas.domain.usecase.auth.LoginUseCase
import com.uade.huellitas.domain.usecase.auth.SendPasswordResetEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val uid = loginUseCase(email, password)
                _uiState.value = AuthUiState.Success(uid)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun onForgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = sendPasswordResetEmailUseCase(email)
            _uiState.value = AuthUiState.Idle
            _snackbarMessage.value = if (result.isSuccess) {
                "Revisá tu email para restablecer tu contraseña"
            } else {
                result.exceptionOrNull()?.message ?: "Error al enviar el email"
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
