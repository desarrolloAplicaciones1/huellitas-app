package com.uade.huellitas.data.local.dao

import androidx.room.*
import com.uade.huellitas.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE ownerId = :uid ORDER BY createdAt DESC")
    fun getMyAlerts(uid: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getById(id: String): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity)

    @Update
    suspend fun update(alert: AlertEntity)

    @Delete
    suspend fun delete(alert: AlertEntity)

    @Query("DELETE FROM alerts WHERE id = :id AND pendingSync = 0")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM alerts WHERE status = 'ACTIVE' AND pendingSync = 0")
    suspend fun getActiveAlertsSnapshot(): List<AlertEntity>

    @Query("SELECT * FROM alerts WHERE pendingSync = 1")
    suspend fun getPendingAlerts(): List<AlertEntity>
}
