package com.uade.huellitas.domain.usecase.onboarding

import com.uade.huellitas.data.local.OnboardingPreferences
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CompleteOnboardingUseCaseTest {

    private lateinit var onboardingPreferences: OnboardingPreferences
    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase

    @Before
    fun setUp() {
        onboardingPreferences = mockk()
        completeOnboardingUseCase = CompleteOnboardingUseCase(onboardingPreferences)
    }

    @Test
    fun `delegates setOnboardingCompleted to preferences`() = runTest {
        coEvery { onboardingPreferences.setOnboardingCompleted() } just Runs

        completeOnboardingUseCase()

        coVerify(exactly = 1) { onboardingPreferences.setOnboardingCompleted() }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from preferences`() = runTest {
        coEvery { onboardingPreferences.setOnboardingCompleted() } throws RuntimeException("Error de escritura")

        completeOnboardingUseCase()
    }
}
