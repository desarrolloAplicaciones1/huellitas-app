package com.uade.huellitas.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.uade.huellitas.data.local.AppDatabase
import com.uade.huellitas.makeAlertEntity
import com.uade.huellitas.makeUserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AlertDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AlertDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.alertDao()
        runBlocking { db.userDao().insert(makeUserEntity()) }
    }

    @After
    fun teardown() = db.close()

    @Test
    fun `getActiveAlerts returns only ACTIVE status alerts`() = runTest {
        dao.insert(makeAlertEntity(id = "a1", status = "ACTIVE"))
        dao.insert(makeAlertEntity(id = "a2", status = "RESOLVED"))

        val result = dao.getActiveAlerts().first()

        assertEquals(1, result.size)
        assertEquals("a1", result[0].id)
    }

    @Test
    fun `getActiveAlerts is ordered by createdAt descending`() = runTest {
        dao.insert(makeAlertEntity(id = "a1").copy(createdAt = 1000L))
        dao.insert(makeAlertEntity(id = "a2").copy(createdAt = 3000L))
        dao.insert(makeAlertEntity(id = "a3").copy(createdAt = 2000L))

        val result = dao.getActiveAlerts().first()

        assertEquals(listOf("a2", "a3", "a1"), result.map { it.id })
    }

    @Test
    fun `getMyAlerts returns only alerts for given owner`() = runTest {
        runBlocking { db.userDao().insert(makeUserEntity(uid = "user-2")) }
        dao.insert(makeAlertEntity(id = "a1", ownerId = "user-1"))
        dao.insert(makeAlertEntity(id = "a2", ownerId = "user-2"))

        val result = dao.getMyAlerts("user-1").first()

        assertEquals(1, result.size)
        assertEquals("a1", result[0].id)
    }

    @Test
    fun `getById returns matching entity`() = runTest {
        dao.insert(makeAlertEntity(id = "a1"))

        val result = dao.getById("a1")

        assertNotNull(result)
        assertEquals("a1", result!!.id)
    }

    @Test
    fun `getById returns null when entity does not exist`() = runTest {
        assertNull(dao.getById("missing"))
    }

    @Test
    fun `insert with REPLACE replaces existing entity`() = runTest {
        dao.insert(makeAlertEntity(id = "a1", petName = "Firulais"))
        dao.insert(makeAlertEntity(id = "a1", petName = "Canela"))

        val result = dao.getById("a1")
        assertEquals("Canela", result!!.petName)
    }

    @Test
    fun `update modifies entity fields`() = runTest {
        dao.insert(makeAlertEntity(id = "a1", petName = "Firulais"))
        dao.update(makeAlertEntity(id = "a1", petName = "Canela"))

        val result = dao.getById("a1")
        assertEquals("Canela", result!!.petName)
    }

    @Test
    fun `delete removes entity from database`() = runTest {
        val entity = makeAlertEntity(id = "a1")
        dao.insert(entity)
        dao.delete(entity)

        assertNull(dao.getById("a1"))
    }

    @Test
    fun `getPendingAlerts returns only pendingSync true alerts`() = runTest {
        dao.insert(makeAlertEntity(id = "a1", pendingSync = true))
        dao.insert(makeAlertEntity(id = "a2", pendingSync = false))
        dao.insert(makeAlertEntity(id = "a3", pendingSync = true))

        val result = dao.getPendingAlerts()

        assertEquals(2, result.size)
        assertTrue(result.all { it.pendingSync })
        assertTrue(result.map { it.id }.containsAll(listOf("a1", "a3")))
    }

    @Test
    fun `getPendingAlerts returns empty list when none are pending`() = runTest {
        dao.insert(makeAlertEntity(id = "a1", pendingSync = false))

        val result = dao.getPendingAlerts()

        assertTrue(result.isEmpty())
    }
}
