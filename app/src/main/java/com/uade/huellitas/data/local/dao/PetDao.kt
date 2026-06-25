package com.uade.huellitas.data.local.dao

import androidx.room.*
import com.uade.huellitas.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getPetsByOwner(ownerId: String): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getById(id: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: PetEntity)

    @Update
    suspend fun update(pet: PetEntity)

    @Delete
    suspend fun delete(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId")
    suspend fun getPetsByOwnerSnapshot(ownerId: String): List<PetEntity>

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun deleteById(id: String)
}
