package com.uade.huellitas.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.uade.huellitas.data.local.dao.AlertDao
import com.uade.huellitas.data.mapper.toDomain
import com.uade.huellitas.data.mapper.toEntity
import com.uade.huellitas.data.remote.FirestoreAlertDataSource
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.repository.PhotoStorageRepository as PhotoStorageRepositoryContract
import com.uade.huellitas.domain.repository.AlertRepository as AlertRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AlertRepository(
    private val alertDao: AlertDao,
    private val remoteDataSource: FirestoreAlertDataSource,
    private val photoStorageRepository: PhotoStorageRepositoryContract
) : AlertRepositoryContract {

    override fun getActiveAlerts(): Flow<List<Alert>> = flow {
        // 1. Fetch desde Firestore y sincroniza Room
        try {
            remoteDataSource.getActiveAlerts()
                .forEach { alertDao.insert(it.toEntity(pendingSync = false)) }
        } catch (_: Exception) {
            // Sin red; usa el cache de Room
        }
        // 2. Emite desde Room (ya tiene los datos de Firestore o el cache offline)
        emitAll(alertDao.getActiveAlerts().map { list -> list.map { it.toDomain() } })
    }

    override fun getMyAlerts(uid: String): Flow<List<Alert>> =
        alertDao.getMyAlerts(uid).map { it.map { entity -> entity.toDomain() } }

    override suspend fun getById(id: String): Alert? =
        alertDao.getById(id)?.toDomain()

    override suspend fun saveAlert(alert: Alert) {
        val pendingEntity = alert.toEntity(pendingSync = true)
        alertDao.insert(pendingEntity)
        try {
            remoteDataSource.saveAlert(alert)
            alertDao.insert(alert.toEntity(pendingSync = false))
        } catch (e: Exception) {
            if (e.isPermissionDenied()) {
                alertDao.delete(pendingEntity)
                throw IllegalStateException("No tenes permisos para crear este aviso.", e)
            }
            // Saved locally with pendingSync=true; Firestore sync will retry when online.
        }
    }

    override suspend fun updateAlert(alert: Alert) {
        val previousEntity = alertDao.getById(alert.id)
        alertDao.update(alert.toEntity(pendingSync = true))
        try {
            remoteDataSource.updateAlert(alert)
            alertDao.update(alert.toEntity(pendingSync = false))
        } catch (e: Exception) {
            if (e.isPermissionDenied()) {
                previousEntity?.let { alertDao.insert(it) }
                throw IllegalStateException("No tenes permisos para modificar este aviso.", e)
            }
            throw e
        }
    }

    override suspend fun resolveAlert(alert: Alert) {
        val resolved = alert.copy(
            status = AlertStatus.RESOLVED,
            updatedAt = System.currentTimeMillis()
        )
        updateAlert(resolved)
    }

    override suspend fun deleteAlert(alert: Alert) {
        try {
            remoteDataSource.deleteAlert(alert.id)
            alertDao.delete(alert.toEntity())
            deleteAlertPhotos(alert.photoUrls)
        } catch (e: Exception) {
            if (e.isPermissionDenied()) {
                throw IllegalStateException("No tenes permisos para eliminar este aviso.", e)
            }
            throw e
        }
    }

    override suspend fun syncFromFirestore() {
        try {
            remoteDataSource.getActiveAlerts()
                .forEach { alertDao.insert(it.toEntity(pendingSync = false)) }
        } catch (_: Exception) {
        }
    }

    override suspend fun syncPendingToFirestore() {
        alertDao.getPendingAlerts().forEach { entity ->
            runCatching { remoteDataSource.saveAlert(entity.toDomain()) }
                .onSuccess { alertDao.insert(entity.copy(pendingSync = false)) }
        }
    }

    companion object {
        private const val TAG = "AlertRepository"
        @Volatile private var INSTANCE: AlertRepository? = null

        fun getInstance(
            alertDao: AlertDao,
            remoteDataSource: FirestoreAlertDataSource,
            photoStorageRepository: PhotoStorageRepositoryContract
        ): AlertRepository =
            INSTANCE ?: synchronized(this) {
                AlertRepository(
                    alertDao = alertDao,
                    remoteDataSource = remoteDataSource,
                    photoStorageRepository = photoStorageRepository
                ).also { INSTANCE = it }
            }
    }

    private suspend fun deleteAlertPhotos(photoUrls: List<String>) {
        // Firestore and Storage are not transactional. We delete the alert first to avoid
        // leaving a visible alert that points to a photo that no longer exists.
        photoUrls
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .forEach { photoUrl ->
                runCatching { photoStorageRepository.deletePhoto(photoUrl) }
                    .onFailure { error ->
                        Log.w(TAG, "No se pudo borrar la foto remota del aviso: $photoUrl", error)
                    }
            }
    }
}

private fun Exception.isPermissionDenied(): Boolean =
    this is FirebaseFirestoreException && this.code.name == "PERMISSION_DENIED"
