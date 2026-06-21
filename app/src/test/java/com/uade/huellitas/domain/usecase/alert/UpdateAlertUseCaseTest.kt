package com.uade.huellitas.domain.usecase.alert

import com.uade.huellitas.makeAlert
import com.uade.huellitas.domain.repository.AlertRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateAlertUseCaseTest {

    private lateinit var alertRepository: AlertRepository
    private lateinit var updateAlertUseCase: UpdateAlertUseCase

    @Before
    fun setUp() {
        alertRepository = mockk()
        updateAlertUseCase = UpdateAlertUseCase(alertRepository)
    }

    @Test
    fun `delegates updateAlert to repository with correct alert`() = runTest {
        val alert = makeAlert(id = "alert-5")
        coEvery { alertRepository.updateAlert(alert) } just Runs

        updateAlertUseCase(alert)

        coVerify(exactly = 1) { alertRepository.updateAlert(alert) }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { alertRepository.updateAlert(any()) } throws RuntimeException("Fallo al actualizar")

        updateAlertUseCase(makeAlert())
    }
}
