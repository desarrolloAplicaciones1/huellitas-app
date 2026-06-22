package com.uade.huellitas.domain.usecase.auth

import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetCurrentUserIdUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var getCurrentUserIdUseCase: GetCurrentUserIdUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        getCurrentUserIdUseCase = GetCurrentUserIdUseCase(userRepository)
    }

    @Test
    fun `returns userId when user is logged in`() {
        every { userRepository.currentUserId } returns "uid-42"

        val result = getCurrentUserIdUseCase()

        assertEquals("uid-42", result)
    }

    @Test
    fun `returns null when no user is logged in`() {
        every { userRepository.currentUserId } returns null

        val result = getCurrentUserIdUseCase()

        assertNull(result)
    }
}
