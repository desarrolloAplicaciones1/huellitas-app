package com.uade.huellitas.domain.usecase.settings

import com.uade.huellitas.domain.model.AppSettings
import com.uade.huellitas.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAppSettingsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var getAppSettingsUseCase: GetAppSettingsUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        getAppSettingsUseCase = GetAppSettingsUseCase(settingsRepository)
    }

    @Test
    fun `returns settings flow from repository`() = runTest {
        val settings = AppSettings(darkModeEnabled = true, alertRadiusKm = 5)
        every { settingsRepository.getSettings() } returns flowOf(settings)

        val result = getAppSettingsUseCase().first()

        assertEquals(settings, result)
        verify(exactly = 1) { settingsRepository.getSettings() }
    }

    @Test
    fun `returns default settings when repository emits defaults`() = runTest {
        val defaults = AppSettings()
        every { settingsRepository.getSettings() } returns flowOf(defaults)

        val result = getAppSettingsUseCase().first()

        assertEquals(defaults.alertRadiusKm, result.alertRadiusKm)
        assertEquals(defaults.followSystemTheme, result.followSystemTheme)
    }
}
