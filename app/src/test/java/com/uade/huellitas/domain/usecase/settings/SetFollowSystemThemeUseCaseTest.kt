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

class SetFollowSystemThemeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var setFollowSystemThemeUseCase: SetFollowSystemThemeUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        setFollowSystemThemeUseCase = SetFollowSystemThemeUseCase(settingsRepository)
    }

    @Test
    fun `delegates setFollowSystemTheme true to repository`() = runTest {
        coEvery { settingsRepository.setFollowSystemTheme(true) } just Runs

        setFollowSystemThemeUseCase(true)

        coVerify(exactly = 1) { settingsRepository.setFollowSystemTheme(true) }
    }

    @Test
    fun `delegates setFollowSystemTheme false to repository`() = runTest {
        coEvery { settingsRepository.setFollowSystemTheme(false) } just Runs

        setFollowSystemThemeUseCase(false)

        coVerify(exactly = 1) { settingsRepository.setFollowSystemTheme(false) }
    }
}
