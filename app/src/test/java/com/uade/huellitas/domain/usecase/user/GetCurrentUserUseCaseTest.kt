package com.uade.huellitas.domain.usecase.user

import com.uade.huellitas.makeUser
import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetCurrentUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        getCurrentUserUseCase = GetCurrentUserUseCase(userRepository)
    }

    @Test
    fun `returns user flow when user is logged in`() = runTest {
        val user = makeUser(uid = "uid-1")
        every { userRepository.currentUserId } returns "uid-1"
        every { userRepository.getUser("uid-1") } returns flowOf(user)

        val result = getCurrentUserUseCase().first()

        assertEquals(user, result)
        verify(exactly = 1) { userRepository.getUser("uid-1") }
    }

    @Test
    fun `returns null flow when no user is logged in`() = runTest {
        every { userRepository.currentUserId } returns null

        val result = getCurrentUserUseCase().first()

        assertNull(result)
    }
}
