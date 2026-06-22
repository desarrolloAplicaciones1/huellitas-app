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

class SetDarkModeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var setDarkModeUseCase: SetDarkModeUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        setDarkModeUseCase = SetDarkModeUseCase(settingsRepository)
    }

    @Test
    fun `delegates setDarkModeEnabled true to repository`() = runTest {
        coEvery { settingsRepository.setDarkModeEnabled(true) } just Runs

        setDarkModeUseCase(true)

        coVerify(exactly = 1) { settingsRepository.setDarkModeEnabled(true) }
    }

    @Test
    fun `delegates setDarkModeEnabled false to repository`() = runTest {
        coEvery { settingsRepository.setDarkModeEnabled(false) } just Runs

        setDarkModeUseCase(false)

        coVerify(exactly = 1) { settingsRepository.setDarkModeEnabled(false) }
    }
}
