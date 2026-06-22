package com.uade.huellitas.presentation.auth

import app.cash.turbine.test
import com.uade.huellitas.domain.usecase.auth.LoginUseCase
import com.uade.huellitas.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.uade.huellitas.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    fun `login exitoso emite Loading luego Success`() = runTest {
        coEvery { loginUseCase("user@test.com", "pass123") } returns "uid-123"

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())
            viewModel.login("user@test.com", "pass123")
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Success("uid-123"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `email invalido emite Error con mensaje`() = runTest {
        coEvery { loginUseCase("no-es-email", any()) } throws
            IllegalArgumentException("El email ingresado no es válido")

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())
            viewModel.login("no-es-email", "pass123")
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(
                AuthUiState.Error("El email ingresado no es válido"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `contrasena vacia emite Error`() = runTest {
        coEvery { loginUseCase(any(), "") } throws
            IllegalArgumentException("La contraseña no puede estar vacía")

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())
            viewModel.login("user@test.com", "")
            assertEquals(AuthUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error de Firebase emite Error con mensaje del servidor`() = runTest {
        val firebaseError = "The password is invalid or the user does not have a password"
        coEvery { loginUseCase(any(), any()) } throws RuntimeException(firebaseError)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())
            viewModel.login("user@test.com", "wrongpass")
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Error(firebaseError), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
