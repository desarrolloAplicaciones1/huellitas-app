package com.uade.huellitas.domain.usecase.user

import com.uade.huellitas.makeUser
import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SyncCurrentUserProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var syncCurrentUserProfileUseCase: SyncCurrentUserProfileUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        syncCurrentUserProfileUseCase = SyncCurrentUserProfileUseCase(userRepository)
    }

    @Test
    fun `returns user when sync succeeds`() = runTest {
        val user = makeUser(uid = "uid-1")
        coEvery { userRepository.syncCurrentUserProfile() } returns user

        val result = syncCurrentUserProfileUseCase()

        assertEquals(user, result)
        coVerify(exactly = 1) { userRepository.syncCurrentUserProfile() }
    }

    @Test
    fun `returns null when no profile found`() = runTest {
        coEvery { userRepository.syncCurrentUserProfile() } returns null

        val result = syncCurrentUserProfileUseCase()

        assertNull(result)
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { userRepository.syncCurrentUserProfile() } throws RuntimeException("Sin conexión")

        syncCurrentUserProfileUseCase()
    }
}
