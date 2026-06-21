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

class SetOfflineModeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var setOfflineModeUseCase: SetOfflineModeUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        setOfflineModeUseCase = SetOfflineModeUseCase(settingsRepository)
    }

    @Test
    fun `delegates setOfflineModeEnabled true to repository`() = runTest {
        coEvery { settingsRepository.setOfflineModeEnabled(true) } just Runs

        setOfflineModeUseCase(true)

        coVerify(exactly = 1) { settingsRepository.setOfflineModeEnabled(true) }
    }

    @Test
    fun `delegates setOfflineModeEnabled false to repository`() = runTest {
        coEvery { settingsRepository.setOfflineModeEnabled(false) } just Runs

        setOfflineModeUseCase(false)

        coVerify(exactly = 1) { settingsRepository.setOfflineModeEnabled(false) }
    }
}
