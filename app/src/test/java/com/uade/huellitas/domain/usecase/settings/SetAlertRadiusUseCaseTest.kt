package com.uade.huellitas.domain.usecase.settings

import com.uade.huellitas.domain.repository.SettingsRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetAlertRadiusUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var setAlertRadiusUseCase: SetAlertRadiusUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        setAlertRadiusUseCase = SetAlertRadiusUseCase(settingsRepository)
    }

    @Test
    fun `delegates setAlertRadiusKm to repository`() = runTest {
        coEvery { settingsRepository.setAlertRadiusKm(10) } just Runs

        setAlertRadiusUseCase(10)

        coVerify(exactly = 1) { settingsRepository.setAlertRadiusKm(10) }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { settingsRepository.setAlertRadiusKm(any()) } throws RuntimeException("Error al guardar")

        setAlertRadiusUseCase(5)
    }
}
