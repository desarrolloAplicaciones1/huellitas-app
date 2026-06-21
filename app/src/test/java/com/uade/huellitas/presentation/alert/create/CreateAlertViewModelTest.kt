package com.uade.huellitas.presentation.alert.create

import app.cash.turbine.test
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.usecase.alert.CreateAlertUseCase
import com.uade.huellitas.domain.usecase.auth.GetCurrentUserIdUseCase
import com.uade.huellitas.domain.usecase.location.GeocodeAddressUseCase
import com.uade.huellitas.domain.usecase.media.UploadAlertPhotoUseCase
import com.uade.huellitas.domain.usecase.user.GetCurrentUserUseCase
import com.uade.huellitas.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateAlertViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createAlertUseCase: CreateAlertUseCase = mockk()
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val geocodeAddressUseCase: GeocodeAddressUseCase = mockk()
    private val uploadAlertPhotoUseCase: UploadAlertPhotoUseCase = mockk()

    private lateinit var viewModel: CreateAlertViewModel

    @Before
    fun setUp() {
        every { getCurrentUserIdUseCase() } returns "uid-1"
        every { getCurrentUserUseCase() } returns flowOf(null)
        coEvery { geocodeAddressUseCase(any()) } returns null
        viewModel = CreateAlertViewModel(
            createAlertUseCase,
            getCurrentUserIdUseCase,
            getCurrentUserUseCase,
            geocodeAddressUseCase,
            uploadAlertPhotoUseCase
        )
    }

    @Test
    fun `formulario sin nombre emite Error de validacion`() = runTest {
        viewModel.uiState.test {
            assertEquals(CreateAlertUiState.Idle, awaitItem())
            viewModel.submitAlert()
            assertEquals(
                CreateAlertUiState.Error("El nombre de la mascota es obligatorio."),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `formulario completo llama CreateAlertUseCase con datos correctos`() = runTest {
        val alertSlot = slot<Alert>()
        coEvery { createAlertUseCase(capture(alertSlot)) } returns Unit

        viewModel.onPetNameChange("Luna")
        viewModel.onDescriptionChange("Se perdió en el parque")

        viewModel.uiState.test {
            assertEquals(CreateAlertUiState.Idle, awaitItem())
            viewModel.submitAlert()
            assertEquals(CreateAlertUiState.Loading, awaitItem())
            assertEquals(CreateAlertUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { createAlertUseCase(any()) }
        assertEquals("Luna", alertSlot.captured.petName)
        assertEquals("uid-1", alertSlot.captured.ownerId)
    }

    @Test
    fun `error de Firestore emite Error`() = runTest {
        coEvery { createAlertUseCase(any()) } throws RuntimeException("Firestore: permiso denegado")

        viewModel.onPetNameChange("Luna")

        viewModel.uiState.test {
            assertEquals(CreateAlertUiState.Idle, awaitItem())
            viewModel.submitAlert()
            assertEquals(CreateAlertUiState.Loading, awaitItem())
            assertEquals(
                CreateAlertUiState.Error("Firestore: permiso denegado"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `exito emite Success`() = runTest {
        coEvery { createAlertUseCase(any()) } returns Unit

        viewModel.onPetNameChange("Firulais")

        viewModel.uiState.test {
            assertEquals(CreateAlertUiState.Idle, awaitItem())
            viewModel.submitAlert()
            assertEquals(CreateAlertUiState.Loading, awaitItem())
            assertEquals(CreateAlertUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
