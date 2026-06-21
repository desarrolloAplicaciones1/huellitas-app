package com.uade.huellitas.domain.usecase.user

import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ChangePasswordUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        changePasswordUseCase = ChangePasswordUseCase(userRepository)
    }

    @Test
    fun `delegates updatePassword to repository with correct password`() = runTest {
        coEvery { userRepository.updatePassword("nuevaPass123") } just Runs

        changePasswordUseCase("nuevaPass123")

        coVerify(exactly = 1) { userRepository.updatePassword("nuevaPass123") }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { userRepository.updatePassword(any()) } throws RuntimeException("Error al cambiar contraseña")

        changePasswordUseCase("cualquierPass")
    }
}
