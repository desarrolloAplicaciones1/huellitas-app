package com.uade.huellitas.data.repository

import app.cash.turbine.test
import com.uade.huellitas.data.local.dao.AlertDao
import com.uade.huellitas.data.mapper.toDomain
import com.uade.huellitas.data.mapper.toEntity
import com.uade.huellitas.data.remote.FirestoreAlertDataSource
import com.uade.huellitas.domain.repository.PhotoStorageRepository
import com.uade.huellitas.makeAlert
import com.uade.huellitas.makeAlertEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AlertRepositoryImplTest {

    private val alertDao: AlertDao = mockk()
    private val remoteDataSource: FirestoreAlertDataSource = mockk()
    private val photoStorageRepository: PhotoStorageRepository = mockk()

    private lateinit var repository: AlertRepository

    @Before
    fun setup() {
        repository = AlertRepository(alertDao, remoteDataSource, photoStorageRepository)
    }

    // ── getMyAlerts ──────────────────────────────────────────────────────────

    @Test
    fun `getMyAlerts maps entities to domain models`() = runTest {
        every { alertDao.getMyAlerts("user-1") } returns flowOf(listOf(makeAlertEntity()))

        repository.getMyAlerts("user-1").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Luna", result[0].petName)
            awaitComplete()
        }
    }

    @Test
    fun `getMyAlerts returns empty list when owner has no alerts`() = runTest {
        every { alertDao.getMyAlerts("user-1") } returns flowOf(emptyList())

        repository.getMyAlerts("user-1").test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
    }

    // ── getActiveAlerts ──────────────────────────────────────────────────────

    @Test
    fun `getActiveAlerts syncs Firestore to Room then emits from Room`() = runTest {
        val remoteAlert = makeAlert()
        coEvery { remoteDataSource.getActiveAlerts() } returns listOf(remoteAlert)
        coEvery { alertDao.insert(any()) } just Runs
        every { alertDao.getActiveAlerts() } returns flowOf(listOf(makeAlertEntity()))

        repository.getActiveAlerts().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            awaitComplete()
        }

        coVerify { remoteDataSource.getActiveAlerts() }
        coVerify { alertDao.insert(remoteAlert.toEntity(pendingSync = false)) }
    }

    @Test
    fun `getActiveAlerts uses Room cache when Firestore throws`() = runTest {
        coEvery { remoteDataSource.getActiveAlerts() } throws RuntimeException("No network")
        every { alertDao.getActiveAlerts() } returns flowOf(listOf(makeAlertEntity()))

        repository.getActiveAlerts().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            awaitComplete()
        }
    }

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    fun `getById returns domain model when entity exists`() = runTest {
        coEvery { alertDao.getById("alert-1") } returns makeAlertEntity()

        val result = repository.getById("alert-1")

        assertNotNull(result)
        assertEquals("alert-1", result!!.id)
    }

    @Test
    fun `getById returns null when entity does not exist`() = runTest {
        coEvery { alertDao.getById("missing") } returns null

        assertNull(repository.getById("missing"))
    }

    // ── saveAlert ────────────────────────────────────────────────────────────

    @Test
    fun `saveAlert inserts locally with pendingSync then syncs to Firestore`() = runTest {
        val alert = makeAlert()
        coEvery { alertDao.insert(any()) } just Runs
        coEvery { remoteDataSource.saveAlert(alert) } just Runs

        repository.saveAlert(alert)

        coVerify { alertDao.insert(alert.toEntity(pendingSync = true)) }
        coVerify { remoteDataSource.saveAlert(alert) }
        coVerify { alertDao.insert(alert.toEntity(pendingSync = false)) }
    }

    @Test
    fun `saveAlert keeps local pendingSync=true when Firestore fails with network error`() = runTest {
        val alert = makeAlert()
        coEvery { alertDao.insert(any()) } just Runs
        coEvery { remoteDataSource.saveAlert(alert) } throws RuntimeException("Network error")

        repository.saveAlert(alert) // must NOT throw

        coVerify { alertDao.insert(alert.toEntity(pendingSync = true)) }
        coVerify(exactly = 0) { alertDao.insert(alert.toEntity(pendingSync = false)) }
    }

    // ── updateAlert ──────────────────────────────────────────────────────────

    @Test
    fun `updateAlert updates locally then syncs to Firestore`() = runTest {
        val alert = makeAlert()
        coEvery { alertDao.getById(alert.id) } returns makeAlertEntity()
        coEvery { alertDao.update(any()) } just Runs
        coEvery { remoteDataSource.updateAlert(alert) } just Runs

        repository.updateAlert(alert)

        coVerify { alertDao.update(alert.toEntity(pendingSync = true)) }
        coVerify { remoteDataSource.updateAlert(alert) }
        coVerify { alertDao.update(alert.toEntity(pendingSync = false)) }
    }

    @Test
    fun `updateAlert rethrows when Firestore fails with network error`() = runTest {
        val alert = makeAlert()
        val networkError = RuntimeException("Network error")
        coEvery { alertDao.getById(alert.id) } returns makeAlertEntity()
        coEvery { alertDao.update(any()) } just Runs
        coEvery { remoteDataSource.updateAlert(alert) } throws networkError

        var caught: Exception? = null
        try {
            repository.updateAlert(alert)
        } catch (e: Exception) {
            caught = e
        }

        assertNotNull(caught)
    }

    // ── deleteAlert ──────────────────────────────────────────────────────────

    @Test
    fun `deleteAlert removes from Firestore then from Room`() = runTest {
        val alert = makeAlert(photoUrls = emptyList())
        coEvery { remoteDataSource.deleteAlert(alert.id) } just Runs
        coEvery { alertDao.delete(any()) } just Runs

        repository.deleteAlert(alert)

        coVerify { remoteDataSource.deleteAlert(alert.id) }
        coVerify { alertDao.delete(alert.toEntity()) }
    }

    @Test
    fun `deleteAlert deletes associated photos`() = runTest {
        val alert = makeAlert(photoUrls = listOf("https://storage/photo1.jpg"))
        coEvery { remoteDataSource.deleteAlert(alert.id) } just Runs
        coEvery { alertDao.delete(any()) } just Runs
        coEvery { photoStorageRepository.deletePhoto(any()) } just Runs

        repository.deleteAlert(alert)

        coVerify { photoStorageRepository.deletePhoto("https://storage/photo1.jpg") }
    }

    // ── syncFromFirestore ────────────────────────────────────────────────────

    @Test
    fun `syncFromFirestore inserts remote alerts into Room`() = runTest {
        val remoteAlert = makeAlert()
        coEvery { remoteDataSource.getActiveAlerts() } returns listOf(remoteAlert)
        coEvery { alertDao.insert(any()) } just Runs

        repository.syncFromFirestore()

        coVerify { alertDao.insert(remoteAlert.toEntity(pendingSync = false)) }
    }

    @Test
    fun `syncFromFirestore does not throw when Firestore fails`() = runTest {
        coEvery { remoteDataSource.getActiveAlerts() } throws RuntimeException("No network")

        repository.syncFromFirestore() // must NOT throw
    }

    // ── syncPendingToFirestore ───────────────────────────────────────────────

    @Test
    fun `syncPendingToFirestore pushes pending alerts to Firestore and clears flag`() = runTest {
        val entity = makeAlertEntity(id = "a1", pendingSync = true)
        coEvery { alertDao.getPendingAlerts() } returns listOf(entity)
        coEvery { remoteDataSource.saveAlert(any()) } just Runs
        coEvery { alertDao.insert(any()) } just Runs

        repository.syncPendingToFirestore()

        coVerify { remoteDataSource.saveAlert(entity.toDomain()) }
        coVerify { alertDao.insert(entity.copy(pendingSync = false)) }
    }

    @Test
    fun `syncPendingToFirestore skips failed alerts without throwing`() = runTest {
        val entity = makeAlertEntity(id = "a1", pendingSync = true)
        coEvery { alertDao.getPendingAlerts() } returns listOf(entity)
        coEvery { remoteDataSource.saveAlert(any()) } throws RuntimeException("No network")

        repository.syncPendingToFirestore() // must NOT throw

        coVerify(exactly = 0) { alertDao.insert(entity.copy(pendingSync = false)) }
    }

    @Test
    fun `syncPendingToFirestore continues processing after one alert fails`() = runTest {
        val failEntity = makeAlertEntity(id = "fail", pendingSync = true)
        val okEntity = makeAlertEntity(id = "ok", pendingSync = true)
        coEvery { alertDao.getPendingAlerts() } returns listOf(failEntity, okEntity)
        coEvery { remoteDataSource.saveAlert(failEntity.toDomain()) } throws RuntimeException("Fail")
        coEvery { remoteDataSource.saveAlert(okEntity.toDomain()) } just Runs
        coEvery { alertDao.insert(any()) } just Runs

        repository.syncPendingToFirestore()

        coVerify(exactly = 0) { alertDao.insert(failEntity.copy(pendingSync = false)) }
        coVerify { alertDao.insert(okEntity.copy(pendingSync = false)) }
    }
}
