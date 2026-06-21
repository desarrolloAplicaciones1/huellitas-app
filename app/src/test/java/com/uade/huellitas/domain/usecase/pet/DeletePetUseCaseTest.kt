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

class DeletePetUseCaseTest {

    private lateinit var petRepository: PetRepository
    private lateinit var deletePetUseCase: DeletePetUseCase

    @Before
    fun setUp() {
        petRepository = mockk()
        deletePetUseCase = DeletePetUseCase(petRepository)
    }

    @Test
    fun `delegates deletePet to repository with correct pet`() = runTest {
        val pet = makePet(id = "pet-10")
        coEvery { petRepository.deletePet(pet) } just Runs

        deletePetUseCase(pet)

        coVerify(exactly = 1) { petRepository.deletePet(pet) }
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { petRepository.deletePet(any()) } throws RuntimeException("Error al eliminar")

        deletePetUseCase(makePet())
    }
}
