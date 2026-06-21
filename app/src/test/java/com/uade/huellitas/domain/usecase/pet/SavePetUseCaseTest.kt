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

class SavePetUseCaseTest {

    private lateinit var petRepository: PetRepository
    private lateinit var savePetUseCase: SavePetUseCase

    @Before
    fun setUp() {
        petRepository = mockk()
        savePetUseCase = SavePetUseCase(petRepository)
    }

    @Test
    fun `delegates savePet to repository with correct pet`() = runTest {
        val pet = makePet(id = "pet-new")
        coEvery { petRepository.savePet(pet) } just Runs

        savePetUseCase(pet)

        coVerify(exactly = 1) { petRepository.savePet(pet) }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { petRepository.savePet(any()) } throws RuntimeException("Error al guardar")

        savePetUseCase(makePet())
    }
}
