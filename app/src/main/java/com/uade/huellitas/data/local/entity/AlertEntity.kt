package com.uade.huellitas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["uid"],
            childColumns = ["ownerId"]
        ),
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class AlertEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val ownerId: String,
    val ownerName: String? = null,
    @ColumnInfo(index = true) val petId: String?,
    val type: String,
    val status: String,
    val petName: String,
    val petType: String,
    val breed: String?,
    val color: String?,
    val size: String?,
    val hasCollar: Boolean?,
    val isCastrated: Boolean?,
    val description: String,
    val photoUrlsJson: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val contactPhone: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingSync: Boolean = false
)
