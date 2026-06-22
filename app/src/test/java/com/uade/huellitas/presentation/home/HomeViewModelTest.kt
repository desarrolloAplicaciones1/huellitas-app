package com.uade.huellitas.presentation.home

import app.cash.turbine.test
import com.uade.huellitas.data.local.NetworkMonitor
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.domain.model.ReferenceLocation
import com.uade.huellitas.domain.model.ReferenceLocationSource
import com.uade.huellitas.domain.usecase.alert.FilterAlertsByRadiusUseCase
import com.uade.huellitas.domain.usecase.alert.GetActiveAlertsUseCase
import com.uade.huellitas.domain.usecase.location.ResolveReferenceLocationUseCase
import com.uade.huellitas.domain.usecase.user.GetCurrentUserUseCase
import com.uade.huellitas.makeAlert
import com.uade.huellitas.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getActiveAlertsUseCase: GetActiveAlertsUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val resolveReferenceLocationUseCase: ResolveReferenceLocationUseCase = mockk()
    private val filterAlertsByRadiusUseCase: FilterAlertsByRadiusUseCase = mockk()
    private val networkMonitor: NetworkMonitor = mockk()

    private val defaultRef = ReferenceLocation(
        label = "Default",
        location = Location(-34.6037, -58.3816),
        source = ReferenceLocationSource.DEFAULT
    )

    @Before
    fun setUp() {
        every { networkMonitor.isOnline } returns flowOf(true)
        every { getCurrentUserUseCase() } returns flowOf(null)
        coEvery { resolveReferenceLocationUseCase(any()) } returns defaultRef
    }

    private fun createViewModel() = HomeViewModel(
        getActiveAlertsUseCase,
        getCurrentUserUseCase,
        resolveReferenceLocationUseCase,
        filterAlertsByRadiusUseCase,
        networkMonitor
    )

    @Test
    fun `carga exitosa emite Success con lista de alertas`() = runTest {
        val alerts = listOf(makeAlert(id = "1"), makeAlert(id = "2"))
        every { getActiveAlertsUseCase() } returns flowOf(alerts)

        createViewModel().uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val state = awaitItem() as HomeUiState.Success
            assertEquals(2, state.alerts.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `lista vacia emite Empty`() = runTest {
        every { getActiveAlertsUseCase() } returns flowOf(emptyList())

        createViewModel().uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            assertEquals(HomeUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `excepcion emite Error`() = runTest {
        every { getActiveAlertsUseCase() } returns flow {
            throw RuntimeException("Error de red")
        }

        createViewModel().uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            assertEquals(HomeUiState.Error("Error de red"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtro por tipo de alerta reduce la lista`() = runTest {
        val alerts = listOf(
            makeAlert(id = "1", type = AlertType.LOST),
            makeAlert(id = "2", type = AlertType.FOUND),
        )
        every { getActiveAlertsUseCase() } returns flowOf(alerts)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val initial = awaitItem() as HomeUiState.Success
            assertEquals(2, initial.alerts.size)

            viewModel.setFilter(alertType = AlertType.LOST)
            val filtered = awaitItem() as HomeUiState.Success
            assertEquals(1, filtered.alerts.size)
            assertEquals(AlertType.LOST, filtered.alerts[0].type)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtro por especie reduce la lista`() = runTest {
        val alerts = listOf(
            makeAlert(id = "1", petType = PetType.DOG),
            makeAlert(id = "2", petType = PetType.CAT),
            makeAlert(id = "3", petType = PetType.CAT),
        )
        every { getActiveAlertsUseCase() } returns flowOf(alerts)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val initial = awaitItem() as HomeUiState.Success
            assertEquals(3, initial.alerts.size)

            viewModel.setFilter(petType = PetType.CAT)
            val filtered = awaitItem() as HomeUiState.Success
            assertEquals(2, filtered.alerts.size)
            assertTrue(filtered.alerts.all { it.petType == PetType.CAT })

            cancelAndIgnoreRemainingEvents()
        }
    }
}
