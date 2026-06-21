package com.uade.huellitas.domain.usecase.auth

import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        logoutUseCase = LogoutUseCase(userRepository)
    }

    @Test
    fun `delegates logout to repository`() {
        every { userRepository.logout() } just Runs

        logoutUseCase()

        verify(exactly = 1) { userRepository.logout() }
    }
}
