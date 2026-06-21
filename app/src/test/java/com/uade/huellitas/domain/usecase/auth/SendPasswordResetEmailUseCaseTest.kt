package com.uade.huellitas.domain.usecase.auth

import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendPasswordResetEmailUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        sendPasswordResetEmailUseCase = SendPasswordResetEmailUseCase(userRepository)
    }

    @Test
    fun `returns failure when email is blank`() = runTest {
        val result = sendPasswordResetEmailUseCase("")

        assertTrue(result.isFailure)
        assertEquals("El email no puede estar vacío", result.exceptionOrNull()?.message)
    }

    @Test
    fun `returns failure when email has no at sign`() = runTest {
        val result = sendPasswordResetEmailUseCase("emailsinformato.com")

        assertTrue(result.isFailure)
        assertEquals("El email no tiene un formato válido", result.exceptionOrNull()?.message)
    }

    @Test
    fun `delegates to repository with valid email`() = runTest {
        coEvery { userRepository.sendPasswordResetEmail("user@test.com") } returns Result.success(Unit)

        val result = sendPasswordResetEmailUseCase("user@test.com")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userRepository.sendPasswordResetEmail("user@test.com") }
    }

    @Test
    fun `propagates repository failure for valid email`() = runTest {
        coEvery { userRepository.sendPasswordResetEmail(any()) } returns Result.failure(RuntimeException("Error Firebase"))

        val result = sendPasswordResetEmailUseCase("user@test.com")

        assertTrue(result.isFailure)
        assertEquals("Error Firebase", result.exceptionOrNull()?.message)
    }
}
