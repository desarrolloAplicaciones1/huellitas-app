package com.uade.huellitas.domain.repository

interface PhotoStorageRepository {
    suspend fun uploadAlertPhoto(ownerId: String, localUri: String): String
    suspend fun uploadProfilePhoto(userId: String, localUri: String): String
    suspend fun uploadPetPhoto(ownerId: String, localUri: String): String
    suspend fun deletePhoto(remoteUrl: String)
}
