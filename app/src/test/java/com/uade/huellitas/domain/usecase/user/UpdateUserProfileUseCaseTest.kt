package com.uade.huellitas.domain.usecase.user

import com.uade.huellitas.makeUser
import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateUserProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var updateUserProfileUseCase: UpdateUserProfileUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        updateUserProfileUseCase = UpdateUserProfileUseCase(userRepository)
    }

    @Test
    fun `delegates updateUserProfile to repository with correct user`() = runTest {
        val user = makeUser(uid = "uid-1", name = "Ana García")
        coEvery { userRepository.updateUserProfile(user) } just Runs

        updateUserProfileUseCase(user)

        coVerify(exactly = 1) { userRepository.updateUserProfile(user) }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { userRepository.updateUserProfile(any()) } throws RuntimeException("Error al actualizar perfil")

        updateUserProfileUseCase(makeUser())
    }
}
