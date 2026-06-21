package com.uade.huellitas.domain.usecase.pet

import com.uade.huellitas.makePet
import com.uade.huellitas.domain.repository.PetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetPetByIdUseCaseTest {

    private lateinit var petRepository: PetRepository
    private lateinit var getPetByIdUseCase: GetPetByIdUseCase

    @Before
    fun setUp() {
        petRepository = mockk()
        getPetByIdUseCase = GetPetByIdUseCase(petRepository)
    }

    @Test
    fun `returns pet when found`() = runTest {
        val pet = makePet(id = "pet-7")
        coEvery { petRepository.getById("pet-7") } returns pet

        val result = getPetByIdUseCase("pet-7")

        assertEquals(pet, result)
        coVerify(exactly = 1) { petRepository.getById("pet-7") }
    }

    @Test
    fun `returns null when pet is not found`() = runTest {
        coEvery { petRepository.getById(any()) } returns null

        val result = getPetByIdUseCase("non-existent")

        assertNull(result)
    }
}
