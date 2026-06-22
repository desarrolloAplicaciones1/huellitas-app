package com.uade.huellitas.domain.usecase.media

import com.uade.huellitas.domain.repository.PhotoStorageRepository

class UploadPetPhotoUseCase(
    private val photoStorageRepository: PhotoStorageRepository
) {
    suspend operator fun invoke(ownerId: String, localUri: String) =
        photoStorageRepository.uploadPetPhoto(ownerId, localUri)
}
