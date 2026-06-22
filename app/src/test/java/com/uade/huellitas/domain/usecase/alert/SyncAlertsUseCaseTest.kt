package com.uade.huellitas.domain.usecase.alert

import com.uade.huellitas.domain.repository.AlertRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyncAlertsUseCaseTest {

    private lateinit var alertRepository: AlertRepository
    private lateinit var syncAlertsUseCase: SyncAlertsUseCase

    @Before
    fun setUp() {
        alertRepository = mockk()
        syncAlertsUseCase = SyncAlertsUseCase(alertRepository)
    }

    @Test
    fun `delegates syncFromFirestore to repository`() = runTest {
        coEvery { alertRepository.syncFromFirestore() } just Runs

        syncAlertsUseCase()

        coVerify(exactly = 1) { alertRepository.syncFromFirestore() }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { alertRepository.syncFromFirestore() } throws RuntimeException("Error de red")

        syncAlertsUseCase()
    }
}
