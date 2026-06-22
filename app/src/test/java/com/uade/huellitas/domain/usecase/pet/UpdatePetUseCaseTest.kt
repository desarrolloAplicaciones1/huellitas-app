package com.uade.huellitas.domain.usecase.pet

import com.uade.huellitas.makePet
import com.uade.huellitas.domain.repository.PetRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdatePetUseCaseTest {

    private lateinit var petRepository: PetRepository
    private lateinit var updatePetUseCase: UpdatePetUseCase

    @Before
    fun setUp() {
        petRepository = mockk()
        updatePetUseCase = UpdatePetUseCase(petRepository)
    }

    @Test
    fun `delegates updatePet to repository with correct pet`() = runTest {
        val pet = makePet(id = "pet-5", name = "Max")
        coEvery { petRepository.updatePet(pet) } just Runs

        updatePetUseCase(pet)

        coVerify(exactly = 1) { petRepository.updatePet(pet) }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { petRepository.updatePet(any()) } throws RuntimeException("Error al actualizar")

        updatePetUseCase(makePet())
    }
}
