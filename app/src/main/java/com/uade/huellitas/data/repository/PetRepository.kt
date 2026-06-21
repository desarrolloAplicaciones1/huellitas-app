package com.uade.huellitas.data.repository

import com.uade.huellitas.data.local.dao.PetDao
import com.uade.huellitas.data.mapper.toDomain
import com.uade.huellitas.data.mapper.toEntity
import com.uade.huellitas.data.remote.FirestorePetDataSource
import com.uade.huellitas.domain.model.Pet
import com.uade.huellitas.domain.repository.PetRepository as PetRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetRepository(
    private val petDao: PetDao,
    private val firestorePetDataSource: FirestorePetDataSource
) : PetRepositoryContract {

    override fun getPetsByOwner(ownerId: String): Flow<List<Pet>> =
        petDao.getPetsByOwner(ownerId).map { it.map { entity -> entity.toDomain() } }

    override suspend fun getById(id: String): Pet? = petDao.getById(id)?.toDomain()

    override suspend fun savePet(pet: Pet) {
        petDao.insert(pet.toEntity())
        runCatching { firestorePetDataSource.savePet(pet) }
    }

    override suspend fun updatePet(pet: Pet) {
        petDao.update(pet.toEntity())
        runCatching { firestorePetDataSource.savePet(pet) }
    }

    override suspend fun deletePet(pet: Pet) {
        petDao.delete(pet.toEntity())
        runCatching { firestorePetDataSource.deletePet(pet.id) }
    }
}
