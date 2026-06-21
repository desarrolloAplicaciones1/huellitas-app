package com.uade.huellitas.domain.usecase.pet

import com.uade.huellitas.makePet
import com.uade.huellitas.domain.repository.PetRepository
import com.uade.huellitas.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetMyPetsUseCaseTest {

    private lateinit var petRepository: PetRepository
    private lateinit var userRepository: UserRepository
    private lateinit var getMyPetsUseCase: GetMyPetsUseCase

    @Before
    fun setUp() {
        petRepository = mockk()
        userRepository = mockk()
        getMyPetsUseCase = GetMyPetsUseCase(petRepository, userRepository)
    }

    @Test
    fun `returns pets for current user when logged in`() = runTest {
        val pets = listOf(makePet(id = "pet-1"), makePet(id = "pet-2"))
        every { userRepository.currentUserId } returns "uid-1"
        every { petRepository.getPetsByOwner("uid-1") } returns flowOf(pets)

        val result = getMyPetsUseCase().first()

        assertEquals(pets, result)
        verify(exactly = 1) { petRepository.getPetsByOwner("uid-1") }
    }

    @Test
    fun `returns empty list when no user is logged in`() = runTest {
        every { userRepository.currentUserId } returns null

        val result = getMyPetsUseCase().first()

        assertTrue(result.isEmpty())
    }
}
