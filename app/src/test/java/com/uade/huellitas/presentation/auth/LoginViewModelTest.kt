package com.uade.huellitas.presentation.auth

import com.uade.huellitas.domain.usecase.auth.LoginUseCase
import com.uade.huellitas.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.uade.huellitas.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(loginUseCase, sendPasswordResetEmailUseCase)
    }

    @Test
    fun `login exitoso emite Success`() = runTest {
        coEvery { loginUseCase("user@test.com", "pass123") } returns "uid-123"

        viewModel.login("user@test.com", "pass123")
        advanceUntilIdle()

        assertEquals(AuthUiState.Success("uid-123"), viewModel.uiState.value)
    }

    @Test
    fun `email invalido emite Error con mensaje`() = runTest {
        coEvery { loginUseCase("no-es-email", any()) } throws
            IllegalArgumentException("El email ingresado no es valido")

        viewModel.login("no-es-email", "pass123")
        advanceUntilIdle()

        assertEquals(
            AuthUiState.Error("El email ingresado no es valido"),
            viewModel.uiState.value
        )
    }

    @Test
    fun `contrasena vacia emite Error`() = runTest {
        coEvery { loginUseCase(any(), "") } throws
            IllegalArgumentException("La contrasena no puede estar vacia")

        viewModel.login("user@test.com", "")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
    }

    @Test
    fun `error de Firebase emite Error con mensaje del servidor`() = runTest {
        val firebaseError = "The password is invalid or the user does not have a password"
        coEvery { loginUseCase(any(), any()) } throws RuntimeException(firebaseError)

        viewModel.login("user@test.com", "wrongpass")
        advanceUntilIdle()

        assertEquals(AuthUiState.Error(firebaseError), viewModel.uiState.value)
    }
}
